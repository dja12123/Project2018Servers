package node.detection.masterNodeService;

import node.IServiceModule;
import node.db.DB_Handler;
import node.network.communicator.SocketHandler;

public class MasterNodeBroadcast implements IServiceModule, Runnable
{
	private DB_Handler dbHandler;
	private SocketHandler socketHandler;
	private boolean isRun;
	private Thread broadcastThread;
	private int broadCastDelay;
	
	public MasterNodeBroadcast(DB_Handler dbHandler, SocketHandler socketHandler)
	{
		this.dbHandler = dbHandler;
		this.socketHandler = socketHandler;
	}

	@Override
	public boolean startModule()
	{
		this.isRun = true;
		this.broadcastThread = new Thread(this);
		this.broadcastThread.start();
		return true;
	}

	@Override
	public void stopModule()
	{
		
		
	}

	@Override
	public void run()
	{
		while(this.isRun)
		{
			try
			{
				Thread.sleep(this.broadCastDelay);
			}
			catch (InterruptedException e) {}
			//this.socketHandler.sendMessage(this.broadcastIA, packet);
		}
		
	}

}
