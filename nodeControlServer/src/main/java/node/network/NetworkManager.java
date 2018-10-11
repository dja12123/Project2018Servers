package node.network;

import java.util.logging.Logger;

import node.IServiceModule;
import node.db.DB_Handler;
import node.log.LogWriter;
import node.network.communicator.SocketHandler;

public class NetworkManager implements IServiceModule
{
	public static final Logger networkLogger = LogWriter.createLogger(NetworkManager.class, "network");
	
	public static final String PROP_INFOBROADCAST_PORT = "infoBroadcastPort";
	
	public final SocketHandler socketHandler;
	
	public NetworkManager()
	{
		this.socketHandler = new SocketHandler();
	}

	@Override
	public boolean startModule()
	{
		if(!this.socketHandler.startModule()) return false;
		return true;
	}

	@Override
	public void stopModule()
	{
		this.socketHandler.stopModule();
		
	}
}
