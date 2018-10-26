package node.detection;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import node.NodeControlCore;
import node.device.Device;
import node.device.DeviceInfoManager;
import node.device.DeviceStateChangeEvent;
import node.network.NetworkManager;
import node.network.NetworkUtil;
import node.network.communicator.NetworkEvent;
import node.network.communicator.SocketHandler;
import node.network.packet.Packet;
import node.network.packet.PacketBuildFailureException;
import node.network.packet.PacketBuilder;
import node.network.packet.PacketUtil;
import node.util.observer.Observable;
import node.util.observer.Observer;

public class MasterNodeService implements Runnable
{	
	public static final String PROP_DELAY_MASTER_MSG = "delayMasterNodeBroadcast";
	public static final String KPROTO_MASTER_BROADCAST = "masterNodeBroadcast";
	
	private DeviceInfoManager deviceInfoManager;
	private NetworkManager networkManager;
	private boolean isRun;
	private Thread broadcastThread;
	private int broadCastDelay;
	
	private UUID[] ipArr;
	
	/*public static void main(String[] args)
	{
		NodeControlCore.init();
		DB_Handler db = new DB_Handler();
		db.startModule();
		DeviceInfoManager infoManager = new DeviceInfoManager(db);
		infoManager.startModule();
		SocketHandler sock = new SocketHandler();
		sock.startModule();
		MasterNodeBroadcast inst = new MasterNodeBroadcast(infoManager, sock);
		inst.startModule();
		
		sock.addObserver(KPROTO_MASTER_BROADCAST, new Observer<NetworkEvent>()
		{
			@Override
			public void update(Observable<NetworkEvent> object, NetworkEvent data)
			{
				System.out.println(data.packet.toString());
				
			}
		});
		
		db.getInstaller().complete();
	}*/
	
	public MasterNodeService(DeviceInfoManager deviceInfoManager, NetworkManager networkManager)
	{
		this.deviceInfoManager = deviceInfoManager;
		this.networkManager = networkManager;
		this.ipArr = new UUID[255];
		
	}
	
	private void clearIpArr()
	{
		for(int i = 1; i < 255; ++i)
		{
			this.ipArr[i] = null;
		}
		this.ipArr[0] = this.deviceInfoManager.getMyDevice().uuid;
	}

	public void start()
	{
		if(this.isRun) return;
		this.isRun = true;
		
		this.networkManager.socketHandler.addObserver(WorkNodeService.KPROTO_NODE_INFO_MSG, this::updateNetwork);
		this.clearIpArr();
		this.broadCastDelay = Integer.parseInt(NodeControlCore.getProp(PROP_DELAY_MASTER_MSG));
		this.broadcastThread = new Thread(this);
		this.broadcastThread.start();
		return;
	}

	public void stop()
	{
		if(!this.isRun) return;
		this.isRun = false;
		this.networkManager.socketHandler.removeObserver(this::updateNetwork);
		this.broadcastThread.interrupt();
	}
	
	public synchronized void updateNetwork(Observable<NetworkEvent> object, NetworkEvent data)
	{
		if(data.key.equals(WorkNodeService.KPROTO_NODE_INFO_MSG))
		{
			UUID sender = data.packet.getSender();
			if(this.deviceInfoManager.deviceExist(sender))
			{// 기존 노드일때
				this.deviceInfoManager.updateDevice(sender, data.inetAddr, false);
			}
			else
			{// 처음 접근하는 노드일때
				InetAddress inetAddr =  this.setDeviceInetAddr(sender);
				this.deviceInfoManager.updateDevice(sender, inetAddr, false);
			}
		}
	}
	
	private InetAddress setDeviceInetAddr(UUID uuid)
	{
		InetAddress addr = null;
		for(int i = 0; i < 255; ++i)
		{
			if(this.ipArr[i] == null)
			{
				try
				{
					addr = InetAddress.getByName(String.format("192.168.0.%d", i));
				}
				catch (UnknownHostException e)
				{
					NodeDetectionService.nodeDetectionLogger.log(Level.SEVERE, "IP할당 오류", e);
				}
			}
		}
		return addr;
	}

	@Override
	public void run()
	{
		StringBuffer msgBuffer;
		PacketBuilder packetBuilder;
		Device[] deviceArr;
		Packet packet;
		
		NodeDetectionService.nodeDetectionLogger.log(Level.INFO, "마스터 브로드캐스트 간격: " + this.broadCastDelay);
		
		while(this.isRun)
		{
			try
			{
				Thread.sleep(this.broadCastDelay);
			}
			catch (InterruptedException e) {}
			
			msgBuffer = new StringBuffer();
			packetBuilder = new PacketBuilder();
			deviceArr = this.deviceInfoManager.getDevices();
			
			for(Device d : deviceArr)
			{
				msgBuffer.append(d.uuid);
				msgBuffer.append(PacketUtil.DPROTO_SEP_COL);
				msgBuffer.append(d.getInetAddr().getHostAddress());
				msgBuffer.append(PacketUtil.DPROTO_SEP_ROW);
			}
			
			try
			{
				packetBuilder.setSender(this.deviceInfoManager.getMyDevice().uuid);
				packetBuilder.setBroadCast();
				packetBuilder.setKey(KPROTO_MASTER_BROADCAST);
				packetBuilder.setData(msgBuffer.toString());
				packet = packetBuilder.createPacket();
			}
			catch (PacketBuildFailureException e)
			{
				
				NodeDetectionService.nodeDetectionLogger.log(Level.SEVERE, "패킷 빌드중 오류", e);
				continue;
			}
			
			
			this.networkManager.socketHandler.sendMessage(NetworkUtil.broadcastIA(), packet);
			
		}
		
	}

}