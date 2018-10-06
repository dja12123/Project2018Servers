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
	
	NodeScanner(DB_Handler dbHandler, SocketHandler socketHandler)
	{
		this.dbHandler = dbHandler;
		this.socketHandler = socketHandler;
		
		//this.socketHandler
	}

	@Override
	public void update(Observable<NetworkEvent> object, NetworkEvent data)
	{
		
		
	}
}
