package node.detection.initService;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.util.logging.Level;

import node.IServiceModule;
import node.NodeControlCore;
import node.db.DB_Handler;
import node.network.NetworkManager;
import node.network.communicator.INetworkObserver;
import node.network.communicator.NetworkEvent;
import node.network.communicator.SocketHandler;
import node.util.observer.Observable;

public class NodeBroadcastReceiver implements IServiceModule, INetworkObserver
{
	private DB_Handler dbHandler;
	private SocketHandler socketHandler;
	
	public static void main(String[] args)
	{
		NodeControlCore.init();
		DB_Handler dbHandler = new DB_Handler();
		SocketHandler socketHandler = new SocketHandler();
		
		dbHandler.startModule();
		socketHandler.startModule();
		
		NodeBroadcastReceiver sc = new NodeBroadcastReceiver(dbHandler, socketHandler);
	}
	
	private static final String NODE_TABLE_SCHEMA = 
			"CREATE TABLE node_info("
		+		"uuid varchar(36) primary key,"
		+		"inetaddr varchar(15),"
		+ 		"updateTime datetime)";
	
	public NodeBroadcastReceiver(DB_Handler dbHandler, SocketHandler socketHandler)
	{
		this.dbHandler = dbHandler;
		this.socketHandler = socketHandler;
	
		this.dbHandler.checkAndCreateTable(NODE_TABLE_SCHEMA);
	}

	@Override
	public void update(Observable<NetworkEvent> object, NetworkEvent data)
	{
		if(data.key.equals(NodeBroadcast.NODE_BROADCAST_MSG))
		{
			String addr = data.inetAddr.getHostAddress();
			String uuid = data.packet.getSender().toString();
			Time time = new Time(System.currentTimeMillis());
			
			this.dbHandler.executeQuery("insert into node_info values(?, ?, ?)",(PreparedStatement prep)->
			{
				prep.setString(1, addr);
				prep.setString(2, uuid);
				prep.setTime(3, time);
			});
		}
	}
	
	@Override
	public boolean startModule()
	{
		this.socketHandler.addObserver(NodeBroadcast.NODE_BROADCAST_MSG, this);
		return true;
	}
	
	@Override
	public void stopModule()
	{
		this.socketHandler.removeObserver(this);
	}
}	
