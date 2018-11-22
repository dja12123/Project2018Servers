package node.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.IServiceModule;
import node.NodeControlCore;
import node.bash.CommandExecutor;
import node.device.Device;
import node.device.DeviceInfoManager;
import node.log.LogWriter;
import node.network.NetworkEvent;
import node.network.protocol.keyvaluePacket.Packet;
import node.network.protocol.keyvaluePacket.PacketBuildFailureException;
import node.network.protocol.keyvaluePacket.PacketBuilder;
import node.network.protocol.keyvaluePacket.PacketUtil;
import node.network.protocol.splitpacket.SplitPacket;
import node.network.protocol.splitpacket.SplitPacketAnalyser;
import node.network.protocol.splitpacket.SplitPacketBuildFailureException;
import node.network.protocol.splitpacket.SplitPacketUtil;
import node.network.socketHandler.RawSocketReceiver;
import node.network.socketHandler.BroadcastSender;
import node.network.socketHandler.UnicastHandler;
import node.util.observer.Observable;
import node.util.observer.Observer;

public class NetworkManager implements IServiceModule
{
	public static final Logger logger = LogWriter.createLogger(NetworkManager.class, "network");
	
	private final NetworkConfig netConfig;
	
	public final DeviceInfoManager deviceInfoManager;
	
	private final BroadcastSender broadcastSender;
	private final RawSocketReceiver rawSocketReceiver;
	private final UnicastHandler unicastHandler;
	
	private final SplitPacketAnalyser splitPacketAnalyser;
	
	private HashMap<String, Observable<NetworkEvent>> observerMap;
	
	private InetAddress inetAddress;
	
	public NetworkManager(DeviceInfoManager deviceInfoManager)
	{
		this.netConfig = new NetworkConfig();
		
		this.deviceInfoManager = deviceInfoManager;

		this.broadcastSender = new BroadcastSender();
		this.rawSocketReceiver = new RawSocketReceiver(this::socketRawByteReadCallback);
		this.unicastHandler = new UnicastHandler(this::socketRawByteReadCallback);
		
		this.splitPacketAnalyser = new SplitPacketAnalyser(this::splitPacketCallback);
		
		this.observerMap = new HashMap<String, Observable<NetworkEvent>>();
		
		this.inetAddress = null;
	}
	
	public void addObserver(String key, Observer<NetworkEvent> observer)
	{
		Observable<NetworkEvent> ob = this.observerMap.getOrDefault(key, null);
		if(ob == null)
		{
			ob = (new Observable<NetworkEvent>());
			this.observerMap.put(key, ob);
		}
		
		ob.addObserver(observer);
	}
	
	public void removeObserver(String key, Observer<NetworkEvent> observer)
	{
		Observable<NetworkEvent> observable = this.observerMap.getOrDefault(key, null);
		if(observable == null)
		{
			return;
		}
		observable.removeObserver(observer);
		
		if(observable.size() == 0)
		{
			this.observerMap.remove(key);
		}
	}
	
	public void removeObserver(Observer<NetworkEvent> observer)
	{
		Observable<NetworkEvent> observable;
		ArrayList<String> removeObservableKey = new ArrayList<>();
		for(String key : this.observerMap.keySet())
		{
			observable = this.observerMap.get(key);
			observable.removeObserver(observer);
			
			if(observable.size() == 0)
			{
				removeObservableKey.add(key);
			}
		}
		
		for(int i = 0; i < removeObservableKey.size(); ++i)
		{
			this.observerMap.remove(removeObservableKey.get(i));
		}
	}
	
	public void sendMessage(Packet packet)
	{

			byte[] id;
			byte[][] splitData;
			SplitPacket splitPacket;
			if(packet.isBroadcast())
			{
				id = SplitPacketUtil.createSplitPacketID(this.netConfig.broadcastIA(NetworkConfig.DEFAULT_SUBNET));
				try
				{
					splitPacket = new SplitPacket(id, packet.getRawData());
				}
				catch (SplitPacketBuildFailureException e)
				{
					logger.log(Level.WARNING, "패킷 빌드중 오류", e);
					return;
				}
				
				splitData = splitPacket.getSplitePacket();
				for(int i = 0; i < splitData.length; ++i)
				{
					this.broadcastSender.sendMessage(splitData[i]);
				}
				//this.ipJumpBroadcast.ipJump();
			}
			else
			{
				Device d = this.deviceInfoManager.getDevice(packet.getReceiver());
				if(d == null || d.getInetAddr() == null)
				{
					return;
				}
				id = SplitPacketUtil.createSplitPacketID(d.getInetAddr());
				try
				{
					splitPacket = new SplitPacket(id, packet.getRawData());
				}
				catch (SplitPacketBuildFailureException e)
				{
					logger.log(Level.WARNING, "패킷 전송중 오류", e);
					return;
				}
				splitData = splitPacket.getSplitePacket();
				for(int i = 0; i < splitData.length; ++i)
				{
					logger.log(Level.INFO, "전송시작");
					this.unicastHandler.sendMessage(splitData[i], d.getInetAddr());
				}
			}
		
	}
	
	public void socketRawByteReadCallback(InetAddress addr, byte[] packetBuffer)
	{// 소켓에서 받은 RAW데이터를 패킷 분석기에 집어넣기
		if(addr == null)
		{
			addr = this.netConfig.broadcastIA(NetworkConfig.DEFAULT_SUBNET);
		}
		final InetAddress cAddr = addr;
		NodeControlCore.mainThreadPool.execute(()->{
			System.out.println("로우소켓수신완료");
			this.splitPacketAnalyser.analysePacket(cAddr, packetBuffer);
		});
	}
	
	public void splitPacketCallback(InetAddress addr, SplitPacket p)
	{// 패킷 분석기에서 취합한 패킷을 재분석하여 옵저버들에게 날려줌
		byte[] payload = p.payload;
		if(!PacketUtil.isPacket(payload))
		{
			return;
		}
		
		Packet packetObj = new Packet(payload);
		
		String eventKey = packetObj.getKey();
		Observable<NetworkEvent> observable = observerMap.getOrDefault(eventKey, null);
		if(observable == null)
		{
			return;
		}

		NetworkEvent event = new NetworkEvent(eventKey, addr, packetObj);
		observable.notifyObservers(NodeControlCore.mainThreadPool, event);
	}
	
	public InetAddress getMyAddr()
	{
		return this.inetAddress;
	}

	@Override
	public boolean startModule()
	{
		logger.log(Level.INFO, "네트워크 매니저 로드");
		
		this.netConfig.loadSetting();
		
		try
		{
			CommandExecutor.executeCommand(String.format("ip link set %s promisc on", this.netConfig.getNIC(), false));
		}
		catch (Exception e)
		{
			logger.log(Level.SEVERE, "무작위 모드 변경 실패", e);
			return false;
		}
		
		this.setInetAddr(this.netConfig.defaultAddr());
		
		this.unicastHandler.start(this.inetAddress, this.netConfig.unicastPort());
		this.rawSocketReceiver.start(this.netConfig.getNIC(), this.netConfig.broadcastPort());
		this.broadcastSender.start(this.netConfig.broadcastIA(NetworkConfig.DEFAULT_SUBNET), this.netConfig.broadcastPort());
		return true;
	}

	@Override
	public void stopModule()
	{
		logger.log(Level.INFO, "네트워크 매니저 종료");
		
		this.observerMap.clear();
		this.unicastHandler.stop();
		this.broadcastSender.stop();
		this.rawSocketReceiver.stop();
	}
	
	public void setInetAddr(InetAddress inetAddress)
	{
		byte[] myAddrByte = inetAddress.getAddress();
		myAddrByte[3] = 1;
		String gatewayAddr = null;
		try
		{
			gatewayAddr = InetAddress.getByAddress(myAddrByte).getHostAddress();
		}
		catch (UnknownHostException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
			logger.log(Level.INFO, "IP변경 시작");
			this.unicastHandler.stop();
			this.broadcastSender.stop();
			this.rawSocketReceiver.stop();
			
			boolean ischange = false;
			while(!ischange)
			{
				try
				{
					CommandExecutor.executeCommand(String.format("ifdown -a"));
					
					CommandExecutor.executeCommand(String.format("ip addr flush dev %s", this.netConfig.getNIC()));
					CommandExecutor.executeCommand(String.format("ip addr add %s/24 brd + dev %s", inetAddress.getHostAddress(), this.netConfig.getNIC()));
					
					CommandExecutor.executeCommand(String.format("ip route add default via %s", gatewayAddr));
					CommandExecutor.executeCommand(String.format("ifup -a"));
					ischange = true;
				}
				catch (Exception e)
				{
					ischange = false;
					logger.log(Level.SEVERE, "IP변경중 오류", e);
				}
				
			}

			logger.log(Level.INFO, String.format("IP변경 완료(%s)", inetAddress.getHostAddress()));
			this.inetAddress = inetAddress;
			this.unicastHandler.start(this.inetAddress, this.netConfig.unicastPort());
			this.broadcastSender.start(this.netConfig.broadcastIA(NetworkConfig.DEFAULT_SUBNET), this.netConfig.broadcastPort());
			this.rawSocketReceiver.start(this.netConfig.getNIC(), this.netConfig.broadcastPort());
		
		
		
	}
}