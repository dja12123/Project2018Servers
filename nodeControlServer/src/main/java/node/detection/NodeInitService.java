package node.detection;

import java.util.logging.Logger;

import node.IServiceModule;
import node.db.DB_Handler;
import node.device.Device;
import node.log.LogWriter;
import node.network.communicator.SocketHandler;

public class NodeInitService implements IServiceModule
{
	public static final Logger nodeInitLogger = LogWriter.createLogger(NodeInitService.class, "nodeInit");
	
	private DB_Handler dbHandler;
	private SocketHandler socketHandler;
	
	private NodeBroadcast infoBroadCast;
	private BroadcastNodeReceiver nodeScanner;
	
	private boolean isDHCPNode;
	
	NodeInitService(DB_Handler dbHandler, SocketHandler socketHandler, Device deviceInfo)
	{
		this.dbHandler = dbHandler;
		this.socketHandler = socketHandler;
		
		this.infoBroadCast = new NodeBroadcast(deviceInfo, this.socketHandler);
		this.nodeScanner = new BroadcastNodeReceiver(this.dbHandler, this.socketHandler);
		
		this.isDHCPNode = false;
	}
	
	private void startScan()
	{
		this.infoBroadCast.startModule();
		this.nodeScanner.startModule();
	}

	@Override
	public boolean startModule()
	{
		
		return false;
	}

	@Override
	public void stopModule()
	{
		// TODO Auto-generated method stub
		
	}
}
