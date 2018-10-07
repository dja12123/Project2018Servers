package node.detection;

import java.util.logging.Logger;

import node.IServiceModule;
import node.db.DB_Handler;
import node.detection.workNodeService.BroadcastNodeReceiver;
import node.detection.workNodeService.NodeBroadcast;
import node.device.Device;
import node.log.LogWriter;
import node.network.communicator.SocketHandler;

public class NodeDetectionService implements IServiceModule
{
	public static final Logger nodeInitLogger = LogWriter.createLogger(NodeDetectionService.class, "nodeInit");
	
	private DB_Handler dbHandler;
	private SocketHandler socketHandler;
	
	private NodeBroadcast infoBroadCast;
	private BroadcastNodeReceiver nodeScanner;
	
	private boolean isDHCPNode;
	
	NodeDetectionService(DB_Handler dbHandler, SocketHandler socketHandler, Device deviceInfo)
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
