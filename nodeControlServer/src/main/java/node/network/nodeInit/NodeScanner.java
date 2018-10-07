package node.network.nodeInit;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import node.NodeControlCore;
import node.db.DB_Handler;
import node.network.communicator.INetworkObserver;
import node.network.communicator.NetworkEvent;
import node.network.communicator.SocketHandler;
import node.util.observer.Observable;

public class NodeScanner implements INetworkObserver
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
		
		NodeScanner sc = new NodeScanner(dbHandler, socketHandler);
		
		
	}
	
	private static final String NODE_TABLE_SCHEMA = 
			"CREATE TABLE nodeInfo("
		+		"uuid varchar(36),"
		+		"inetaddr varchar(15),"
		+ 		"updateTime datetime)";
	
	NodeScanner(DB_Handler dbHandler, SocketHandler socketHandler)
	{
		this.dbHandler = dbHandler;
		this.socketHandler = socketHandler;
	
		this.dbHandler.checkAndCreateTable(NODE_TABLE_SCHEMA);
		this.socketHandler.addObserver(InfoBroadcast.NODE_BROADCAST_MSG, this);
	}

	@Override
	public void update(Observable<NetworkEvent> object, NetworkEvent data)
	{
		if(data.key.equals(InfoBroadcast.NODE_BROADCAST_MSG))
		{
			String addr = data.inetAddr.getHostAddress();
			String uuid = data.packet.getSender().toString();
			
			
		}
	}
}	
