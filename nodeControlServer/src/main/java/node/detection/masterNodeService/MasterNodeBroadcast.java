package node.detection.masterNodeService;

import java.net.Socket;

import node.IServiceModule;
import node.NodeControlCore;
import node.db.DB_Handler;
import node.network.communicator.SocketHandler;

public class MasterNodeBroadcast implements IServiceModule, Runnable
{
	public static final String PROP_DELAY_MASTER_MSG = "delayMasterNodeBroadcast";
	
	private DB_Handler dbHandler;
	private SocketHandler socketHandler;
	private boolean isRun;
	private Thread broadcastThread;
	private int broadCastDelay;
	
	public static void main(String[] args)
	{
		NodeControlCore.init();
		DB_Handler db = new DB_Handler();
		db.startModule();
		SocketHandler sock = new SocketHandler();
		sock.startModule();
		MasterNodeBroadcast inst = new MasterNodeBroadcast(db, sock);
		inst.startModule();
	}
	
	public MasterNodeBroadcast(DB_Handler dbHandler, SocketHandler socketHandler)
	{
		this.dbHandler = dbHandler;
		this.socketHandler = socketHandler;
	}

	@Override
	public boolean startModule()
	{
		if(this.isRun) return true;
		this.isRun = true;
		
		this.broadCastDelay = Integer.parseInt(NodeControlCore.getProp(PROP_DELAY_MASTER_MSG));
		this.broadcastThread = new Thread(this);
		this.broadcastThread.start();
		return true;
	}

	@Override
	public void stopModule()
	{
		if(!this.isRun) return;
		this.isRun = false;
		
		this.broadcastThread.interrupt();
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
