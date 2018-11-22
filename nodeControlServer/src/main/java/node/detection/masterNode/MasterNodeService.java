package node.detection.masterNode;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.NodeControlCore;
import node.detection.DetectionUtil;
import node.detection.NodeDetectionService;
import node.detection.NodeInfoProtocol;
import node.detection.workNode.WorkNodeService;
import node.device.Device;
import node.device.DeviceInfoManager;
import node.gpio.led.LEDControl;
import node.device.DeviceChangeEvent;
import node.log.LogWriter;
import node.network.NetworkManager;
import node.network.NetworkUtil;
import node.network.protocol.keyvaluePacket.Packet;
import node.network.protocol.keyvaluePacket.PacketBuildFailureException;
import node.network.protocol.keyvaluePacket.PacketBuilder;
import node.network.NetworkEvent;
import node.util.observer.Observable;
import node.util.observer.Observer;

public class MasterNodeService implements Runnable
{	
	public static final Logger logger = LogWriter.createLogger(MasterNodeService.class, "masterNodeService");
	

	
	public static final String KPROTO_MASTER_BROADCAST = "masterNodeBroadcast";
	private NodeDetectionService nodeDetectionService;
	private DeviceInfoManager deviceInfoManager;
	private NetworkManager networkManager;
	private boolean isRun;
	private Thread broadcastThread;
	private int broadCastDelay;
	private Observer<NetworkEvent> networkObserverFunc;
	private Observer<DeviceChangeEvent> deviceObserverFunc;
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
		this.deviceObserverFunc = this::updateDevice;
	}

	public synchronized void start()
	{
		if(this.isRun) return;
		this.isRun = true;
		logger.log(Level.INFO, "마스터 노드 서비스 시작");
		this.deviceInfoManager.updateDevice(this.deviceInfoManager.getMyDevice().uuid, DetectionUtil.masterAddr(), true);
		this.networkManager.setInetAddr(DetectionUtil.masterAddr());
		this.networkManager.addObserver(WorkNodeService.KPROTO_NODE_INFO_MSG, this.networkObserverFunc);
		this.networkManager.addObserver(KPROTO_MASTER_BROADCAST, this.networkObserverFunc);
		this.deviceInfoManager.addObserver(this.deviceObserverFunc);
		this.ipManager.clear();
		this.broadCastDelay = Integer.parseInt(NodeControlCore.getProp(DetectionUtil.PROP_delayMasterNodeBroadcast));
		this.broadcastThread = new Thread(this);
		this.broadcastThread.start();
		return;
	}

	public synchronized void stop()
	{
		if(!this.isRun) return;
		this.isRun = false;
		logger.log(Level.INFO, "마스터 노드 서비스 중지");
		this.networkManager.removeObserver(this.networkObserverFunc);
		this.deviceInfoManager.removeObserver(this.deviceObserverFunc);
		this.broadcastThread.interrupt();
	}
	
	public synchronized void updateNetwork(Observable<NetworkEvent> object, NetworkEvent data)
	{
		try
		{
			UUID sender = data.packet.getSender();
			if(sender.equals(this.deviceInfoManager.getMyDevice().uuid))
			{// 내가 보낸 패킷이면 버림.
				return;
			}
			if(data.key.equals(WorkNodeService.KPROTO_NODE_INFO_MSG))
			{
				LEDControl.ledControl.flick(1, 200, 100, 100, 0);
				if(this.deviceInfoManager.deviceExist(sender))
				{// 기존 노드일때
					Device device = this.deviceInfoManager.getDevice(sender);
					InetAddress deviceInet = this.ipManager.getInetAddr(sender);
					if(device.getInetAddr() == null || deviceInet == null)
					{// ip가 없을때 ip를 새로 할당
						deviceInet = this.ipManager.assignmentInetAddr(sender);
						LEDControl.ledControl.flick(0, 200, 4, 0, 100, 0);
						logger.log(Level.INFO, String.format("노드에 IP 할당 (%s, %s)", device.uuid.toString(), deviceInet.getHostAddress()));
					}
					
					this.deviceInfoManager.updateDevice(sender, deviceInet, false);
				}
				else
				{// 처음 접근하는 노드일때
					InetAddress inetAddr =  this.ipManager.assignmentInetAddr(sender);
					this.deviceInfoManager.updateDevice(sender, inetAddr, false);
					logger.log(Level.INFO, String.format("새 노드 접근 (%s %s)", sender.toString(), inetAddr.getHostAddress()));
					LEDControl.ledControl.flick(0, 200, 4, 200, 0, 200);
				}
			}
			if(data.key.equals(KPROTO_MASTER_BROADCAST))
			{
				NodeInfoProtocol nodeInfoProtocol = new NodeInfoProtocol(data.packet);
				if(DetectionUtil.isChangeMasterNode(nodeInfoProtocol, this.deviceInfoManager.getMyDevice().uuid, this.deviceInfoManager))
				{
					this.nodeDetectionService.workNodeSelectionCallback(nodeInfoProtocol);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public synchronized void updateDevice(Observable<DeviceChangeEvent> object, DeviceChangeEvent data)
	{
		if(data.getState(DeviceChangeEvent.DISCONNECT_DEVICE))
		{
			InetAddress deviceInetAddr = data.device.getInetAddr();
			if(deviceInetAddr != null)
			{
				logger.log(Level.INFO, String.format("IP할당 해제 (%s %s)",
						data.device.uuid.toString(), data.device.getInetAddr().getHostAddress()));
				this.ipManager.removeInetAddr(data.device.uuid);
			}
			logger.log(Level.INFO, String.format("노드 연결 끊김  (%s)", data.device.uuid));
			LEDControl.ledControl.flick(0, 200, 4, 200, 0, 0);
		}
	}

	@Override
	public void run()
	{
		logger.log(Level.INFO, String.format("마스터 노드 알림 시작 (%dms 간격)", this.broadCastDelay));
		
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
					logger.log(Level.SEVERE, "패킷 빌드중 오류", e);
				}
				
				this.networkManager.sendMessage(packet);
			}
			
			try
			{
				Thread.sleep(this.broadCastDelay);
			}
			catch (InterruptedException e) {}
		}
		
	}
	

}