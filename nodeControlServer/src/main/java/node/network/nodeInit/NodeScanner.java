package node.network.nodeInit;

import node.db.DB_Handler;
import node.network.communicator.INetworkObserver;
import node.network.communicator.NetworkEvent;
import node.network.communicator.SocketHandler;
import node.util.observer.Observable;

public class NodeScanner implements INetworkObserver
{
	private DB_Handler dbHandler;
	private SocketHandler socketHandler;
	
	private static final String NODE_TABLE_SCHEMA = 
			"CREATE TABLE nodeInfo("
		+		"device_id varchar(36)"
		+		")";
	
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
		//data.inetAddr;
		
	}
}
