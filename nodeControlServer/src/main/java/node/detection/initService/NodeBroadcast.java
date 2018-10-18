package node.detection.initService;

import java.util.logging.Level;
import node.IServiceModule;
import node.NodeControlCore;
import node.db.DB_Handler;
import node.detection.NodeDetectionService;
import node.detection.masterNodeService.MasterNodeBroadcast;
import node.device.DeviceInfoManager;
import node.network.NetworkUtil;
import node.network.communicator.NetworkEvent;
import node.network.communicator.SocketHandler;
import node.network.packet.Packet;
import node.network.packet.PacketBuildFailureException;
import node.network.packet.PacketBuilder;
import node.util.observer.Observable;
import node.util.observer.Observer;

public class NodeBroadcast implements Runnable, IServiceModule
{
	public static final String PROP_DELAY_INFOMSG = "delayInitBroadcast";
	public static final String NODE_INIT_BROADCAST_MSG = "infoBroadcast";
	
	private final DeviceInfoManager deviceInfoManager;
	private final SocketHandler socketHandler;
	
	private int broadCastDelay;
	private Thread broadcastThread = null;
	private boolean isRun = false;
	
	private Packet packet;
	
	public static void main(String[] args)
	{
		NodeControlCore.init();
		DB_Handler db = new DB_Handler();
		db.startModule();
		DeviceInfoManager infoManager = new DeviceInfoManager(db);
		infoManager.startModule();
		SocketHandler sock = new SocketHandler();
		sock.startModule();
		NodeBroadcast inst = new NodeBroadcast(infoManager, sock);
		inst.startModule();
		
		sock.addObserver(NODE_INIT_BROADCAST_MSG, new Observer<NetworkEvent>()
		{
			@Override
			public void update(Observable<NetworkEvent> object, NetworkEvent data)
			{
				System.out.println(data.packet.toString());
			}
		});
	}
	
	public NodeBroadcast(DeviceInfoManager deviceInfoManager, SocketHandler socketHandler)
	{
		this.deviceInfoManager = deviceInfoManager;
		this.socketHandler = socketHandler;
	}

	@Override
	public void run()
	{
		NodeDetectionService.nodeDetectionLogger.log(Level.INFO, "노드 알림 시작");
		while(this.isRun)
		{
			try
			{
				Thread.sleep(this.broadCastDelay);
			}
			catch (InterruptedException e) {}
			this.socketHandler.sendMessage(NetworkUtil.broadcastIA(), packet);
		}
	}
	
	public boolean startModule()
	{
		if(this.isRun) return true;
		this.isRun = true;
		
		PacketBuilder builder = new PacketBuilder();
		
		try
		{
			builder.setSender(this.deviceInfoManager.getMyDevice().uuid);
			builder.setBroadCast();
			builder.setKey(NODE_INIT_BROADCAST_MSG);
			this.packet = builder.createPacket();
		}
		catch (PacketBuildFailureException e)
		{
			NodeDetectionService.nodeDetectionLogger.log(Level.SEVERE, "브로드케스트 패킷 생성 오류", e);
			return false;
		}
		
		this.broadCastDelay = Integer.parseInt(NodeControlCore.getProp(PROP_DELAY_INFOMSG));
		
		this.broadcastThread = new Thread(this);
		this.broadcastThread.start();
		return true;
	}
	
	public void stopModule()
	{
		if(!this.isRun) return;
		this.isRun = false;
		
		this.broadcastThread.interrupt();
	}
}