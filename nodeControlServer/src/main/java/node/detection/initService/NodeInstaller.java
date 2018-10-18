package node.detection.initService;

import java.sql.PreparedStatement;
import java.sql.Time;
import java.util.Random;
import java.util.logging.Level;

import node.IServiceModule;
import node.NodeControlCore;
import node.db.DB_Handler;
import node.detection.NodeDetectionService;
import node.detection.masterNodeService.MasterNodeBroadcast;
import node.device.DeviceInfoManager;
import node.network.NetworkManager;
import node.network.communicator.NetworkEvent;
import node.network.communicator.SocketHandler;
import node.util.observer.Observable;
import node.util.observer.Observer;

public class NodeInstaller implements IServiceModule, Runnable, Observer<NetworkEvent>
{
	private static final int DEFAULT_WAIT_TIME = 5000;
	private static final int RANDOM_WAIT_TIME = 5000;
	private SocketHandler socketHandler;
	private Thread waitThread;
	private NodeDetectionService nodeDetectionService;
	private NetworkEvent masterNodeData;
	
	private static final String NODE_TABLE_SCHEMA = 
			"CREATE TABLE node_info("
		+		"uuid varchar(36) primary key,"
		+		"inetaddr varchar(15),"
		+ 		"updateTime datetime)";
	
	public NodeInstaller(NodeDetectionService nodeDetectionService, SocketHandler socketHandler)
	{
		this.nodeDetectionService = nodeDetectionService;
		this.socketHandler = socketHandler;
		//this.dbHandler.getInstaller().checkAndCreateTable(NODE_TABLE_SCHEMA);
		
	}

	@Override
	public void update(Observable<NetworkEvent> object, NetworkEvent data)
	{
		String addr = data.inetAddr.getHostAddress();
		String uuid = data.packet.getSender().toString();
		Time time = new Time(System.currentTimeMillis());
		
		if(data.key.equals(MasterNodeBroadcast.KPROTO_MASTER_BROADCAST))
		{
			this.masterNodeData = data;
			this.waitThread.interrupt();
		}
	}
	
	@Override
	public boolean startModule()
	{
		NetworkManager.networkLogger.log(Level.INFO, "노드 알림 수신 시작");
		this.waitThread = new Thread(this);
		this.waitThread.start();
		this.socketHandler.addObserver(NodeBroadcast.NODE_INIT_BROADCAST_MSG, this);
		return true;
	}
	
	@Override
	public void stopModule()
	{
		this.socketHandler.removeObserver(this);
	}

	@Override
	public void run()
	{
		try
		{
			Thread.sleep(DEFAULT_WAIT_TIME);
		}
		catch (InterruptedException e)
		{
			//마스터 노드 감지
			this.nodeDetectionService.masterNodeDetection(this.masterNodeData);
			return;
		}
		
		int randomWaitTime = new Random(RANDOM_WAIT_TIME).nextInt();
		try
		{
			Thread.sleep(randomWaitTime);
		}
		catch (InterruptedException e)
		{
			this.nodeDetectionService.masterNodeDetection(this.masterNodeData);
			return;
		}
		this.nodeDetectionService.myMasterNode();
	}
}	
