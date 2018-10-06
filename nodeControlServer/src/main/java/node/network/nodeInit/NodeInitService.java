package node.network.nodeInit;

import node.db.DB_Handler;
import node.network.communicator.SocketHandler;

public class NodeInitService
{
	private DB_Handler dbHandler;
	private SocketHandler socketHandler;
	
	NodeInitService(DB_Handler dbHandler, SocketHandler socketHandler)
	{
		this.dbHandler = dbHandler;
		this.socketHandler = socketHandler;
	}
	
	
	
}
