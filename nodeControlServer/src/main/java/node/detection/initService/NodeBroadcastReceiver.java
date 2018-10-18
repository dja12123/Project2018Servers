package node.detection.initService;

import java.sql.PreparedStatement;
import java.sql.Time;
import java.util.logging.Level;

import node.IServiceModule;
import node.NodeControlCore;
import node.db.DB_Handler;
import node.detection.masterNodeService.MasterNodeBroadcast;
import node.device.DeviceInfoManager;
import node.network.NetworkManager;
import node.network.communicator.INetworkObserver;
import node.network.communicator.NetworkEvent;
import node.network.communicator.SocketHandler;
import node.util.observer.Observable;

public class NodeBroadcastReceiver implements IServiceModule, INetworkObserver
{
	private DeviceInfoManager deviceInfoManager;
	private SocketHandler socketHandler;
	
	public static void main(String[] args)
	{
		NodeControlCore.init();
		DB_Handler db = new DB_Handler();
		db.startModule();
		DeviceInfoManager infoManager = new DeviceInfoManager(db);
		infoManager.startModule();
		SocketHandler sock = new SocketHandler();
		sock.startModule();
		NodeBroadcastReceiver inst = new NodeBroadcastReceiver(infoManager, sock);
		NodeBroadcast binst = new NodeBroadcast(infoManager, sock);
		inst.startModule();
		binst.startModule();
	}
	
	private static final String NODE_TABLE_SCHEMA = 
			"CREATE TABLE node_info("
		+		"uuid varchar(36) primary key,"
		+		"inetaddr varchar(15),"
		+ 		"updateTime datetime)";
	
	public NodeBroadcastReceiver(DeviceInfoManager deviceInfoManager, SocketHandler socketHandler)
	{
		this.deviceInfoManager = deviceInfoManager;
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
			
			
			System.out.println(data.packet.toString());
		}
	}
	
	@Override
	public boolean startModule()
	{
		NetworkManager.networkLogger.log(Level.INFO, "노드 알림 수신 시작");
		this.socketHandler.addObserver(NodeBroadcast.NODE_INIT_BROADCAST_MSG, this);
		return true;
	}
	
	@Override
	public void stopModule()
	{
		this.socketHandler.removeObserver(this);
	}
}	
