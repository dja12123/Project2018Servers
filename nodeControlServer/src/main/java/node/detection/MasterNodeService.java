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
import node.network.NetworkEvent;
import node.network.SocketHandler;
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
	
	private NodeDetectionService nodeDetectionService;
	private DeviceInfoManager deviceInfoManager;
	private NetworkManager networkManager;
	private boolean isRun;
	private Thread broadcastThread;
	private int broadCastDelay;
	private Observer<NetworkEvent> networkObserverFunc;
	private IPManager ipManager;
	
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
	
	public MasterNodeService(NodeDetectionService nodeDetectionService, DeviceInfoManager deviceInfoManager, NetworkManager networkManager)
	{
		this.nodeDetectionService = nodeDetectionService;
		this.deviceInfoManager = deviceInfoManager;
		this.networkManager = networkManager;
		
		this.ipManager = new IPManager();
		this.networkObserverFunc = this::updateNetwork;
	}

	public synchronized void start()
	{
		if(this.isRun) return;
		this.isRun = true;
		
		this.networkManager.addObserver(WorkNodeService.KPROTO_NODE_INFO_MSG, this.networkObserverFunc);
		this.networkManager.addObserver(KPROTO_MASTER_BROADCAST, this.networkObserverFunc);
		this.ipManager.clear();
		this.broadCastDelay = Integer.parseInt(NodeControlCore.getProp(PROP_DELAY_MASTER_MSG));
		this.broadcastThread = new Thread(this);
		this.broadcastThread.start();
		return;
	}

	public synchronized void stop()
	{
		if(!this.isRun) return;
		this.isRun = false;
		this.networkManager.removeObserver(this.networkObserverFunc);
		this.broadcastThread.interrupt();
	}
	
	public synchronized void updateNetwork(Observable<NetworkEvent> object, NetworkEvent data)
	{
		if(data.key.equals(WorkNodeService.KPROTO_NODE_INFO_MSG))
		{
			UUID sender = data.packet.getSender();
			if(this.deviceInfoManager.deviceExist(sender))
			{// 기존 노드일때
				Device device = this.deviceInfoManager.getDevice(sender);
				InetAddress deviceInet = this.ipManager.getInetAddr(sender);
				if(device.getInetAddr() == null || deviceInet == null)
				{// ip가 없을때 ip를 새로 할당
					deviceInet = this.ipManager.assignmentInetAddr(sender);
				}
				this.deviceInfoManager.updateDevice(sender, deviceInet, false);
			}
			else
			{// 처음 접근하는 노드일때
				InetAddress inetAddr =  this.ipManager.assignmentInetAddr(sender);
				this.deviceInfoManager.updateDevice(sender, inetAddr, false);
			}
		}
		if(data.key.equals(KPROTO_MASTER_BROADCAST))
		{
			UUID sender = data.packet.getSender();
			if(sender.equals(this.deviceInfoManager.getMyDevice().uuid))
			{// 내가 보낸 패킷이면 버림
				return;
			}
			NodeInfoProtocol nodeInfoProtocol = new NodeInfoProtocol(data.packet);
			if(DetectionUtil.isChangeMasterNode(nodeInfoProtocol, this.deviceInfoManager.getMyDevice().uuid, this.deviceInfoManager))
			{
				this.nodeDetectionService.workNodeSelectionCallback(nodeInfoProtocol);
			}
		}
	}

	@Override
	public void run()
	{
		
		NodeDetectionService.nodeDetectionLogger.log(Level.INFO, "마스터 브로드캐스트 간격: " + this.broadCastDelay);
		
		while(this.isRun)
		{
			synchronized (this)
			{
				PacketBuilder packetBuilder = new PacketBuilder();
				Device[] deviceArr = this.deviceInfoManager.getDevices();
				UUID[] uuids = new UUID[deviceArr.length];
				InetAddress addrs[] = new InetAddress[deviceArr.length];
				Packet packet = null;
				
				for(int i = 0; i < deviceArr.length; ++i)
				{
					uuids[i] = deviceArr[i].uuid;
					addrs[i] = deviceArr[i].getInetAddr();
				}
				
				NodeInfoProtocol nodeInfoProtocol = new NodeInfoProtocol(this.deviceInfoManager.getMyDevice().uuid, uuids, addrs, deviceArr.length);
				
				try
				{
					packetBuilder.setSender(this.deviceInfoManager.getMyDevice().uuid);
					packetBuilder.setBroadCast();
					packetBuilder.setKey(KPROTO_MASTER_BROADCAST);
					packetBuilder.setData(nodeInfoProtocol.getPacketDataField());
					packet = packetBuilder.createPacket();
				}
				catch (PacketBuildFailureException e)
				{
					
					NodeDetectionService.nodeDetectionLogger.log(Level.SEVERE, "패킷 빌드중 오류", e);
					continue;
				}
				
				
				this.networkManager.socketHandler.sendMessage(packet);
			}
			
			try
			{
				Thread.sleep(this.broadCastDelay);
			}
			catch (InterruptedException e) {}
		}
		
	}

}