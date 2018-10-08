package node.detection.masterNodeService;

import node.IServiceModule;
import node.db.DB_Handler;
import node.network.communicator.SocketHandler;

public class MasterNodeBroadcast implements IServiceModule
{
	private DB_Handler dbHandler;
	private SocketHandler socketHandler;
	
	public MasterNodeBroadcast(DB_Handler dbHandler, SocketHandler socketHandler)
	{
		this.dbHandler = dbHandler;
		this.socketHandler = socketHandler;
	}

	@Override
	public boolean startModule()
	{
		
		return true;
	}

	@Override
	public void stopModule()
	{
		
		
	}

}
