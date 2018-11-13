package node.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.IServiceModule;
import node.NodeControlCore;
import node.bash.CommandExecutor;
import node.device.DeviceInfoManager;
import node.log.LogWriter;
import node.network.NetworkEvent;
import node.network.packet.Packet;
import node.network.packet.PacketUtil;
import node.network.socketHandler.BroadcastSocketReceiver;
import node.network.socketHandler.IPJumpBroadcast;
import node.util.observer.Observable;
import node.util.observer.Observer;

public class NetworkManager implements IServiceModule
{
	public static final Logger logger = LogWriter.createLogger(NetworkManager.class, "network");
	
	public final DeviceInfoManager deviceInfoManager;
	
	private final IPJumpBroadcast ipJumpBroadcast;
	private BroadcastSocketReceiver rawSocketReceiver;
	
	private HashMap<String, Observable<NetworkEvent>> observerMap;
	
	private InetAddress inetAddress;
	
	public NetworkManager(DeviceInfoManager deviceInfoManager)
	{
		this.deviceInfoManager = deviceInfoManager;

		this.ipJumpBroadcast = new IPJumpBroadcast(this::socketReadCallback);
		this.rawSocketReceiver = new BroadcastSocketReceiver(this::socketReadCallback);
		
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
		for(String key : this.observerMap.keySet())
		{
			observable = this.observerMap.get(key);
			observable.removeObserver(observer);
			
			if(observable.size() == 0)
			{
				this.observerMap.remove(key);
			}
		}
	}
	
	public void sendMessage(Packet packet)
	{
		if(packet.isBroadcast())
		{
			this.ipJumpBroadcast.sendMessage(true, packet.getDataByte());
		}
	}
	
	public void socketReadCallback(InetAddress addr, byte[] packetBuffer)
	{
		if(!PacketUtil.isPacket(packetBuffer))
		{
			return;
		}
		
		Packet packetObj = new Packet(packetBuffer);
		
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
		NetworkInterface ni = NetworkUtil.getNetworkInterface(NetworkUtil.getNIC());
		this.inetAddress = ni.getInetAddresses().nextElement();
		
		this.rawSocketReceiver.start(this.inetAddress);
		this.ipJumpBroadcast.start();

		logger.log(Level.INFO, "네트워크 메니저 로드");
		return true;
	}

	@Override
	public void stopModule()
	{
		this.observerMap.clear();
		this.ipJumpBroadcast.stop();
		this.rawSocketReceiver.stop();
	}
	
	public void setInetAddr(InetAddress inetAddress)
	{
		ArrayList<String> command = new ArrayList<String>();
		
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
		command.add(String.format("ifdown -a"));

		command.add(String.format("ip addr flush dev %s", NetworkUtil.getNIC()));
		command.add(String.format("ip addr add %s/24 brd + dev %s", inetAddress.getHostAddress(), NetworkUtil.getNIC()));
		command.add(String.format("ifconfig %s:0 %s/24", NetworkUtil.getNIC(), "192.168.0.251"));
		
		command.add(String.format("ip route add default via %s", gatewayAddr));
		command.add(String.format("ifup -a"));
		
		synchronized (this)
		{
			logger.log(Level.INFO, "IP변경 시작");
			this.ipJumpBroadcast.stop();
			this.rawSocketReceiver.stop();
			try
			{
				CommandExecutor.executeBash(command);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			this.ipJumpBroadcast.start();
			this.rawSocketReceiver.start(this.inetAddress);
			logger.log(Level.INFO, "IP변경 완료");
		}
		
		this.inetAddress = inetAddress;
	}
}