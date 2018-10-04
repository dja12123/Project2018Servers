package node.network;

import node.IServiceModule;
import node.network.communicator.SocketHandler;

public class NetworkManager implements IServiceModule
{
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
