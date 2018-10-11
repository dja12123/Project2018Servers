package node.detection;

import java.util.logging.Logger;

import node.IServiceModule;
import node.db.DB_Handler;
import node.detection.initService.NodeBroadcast;
import node.detection.initService.NodeBroadcastReceiver;
import node.detection.masterNodeService.MasterNodeBroadcast;
import node.detection.masterNodeService.MasterNodeReceiver;
import node.device.Device;
import node.device.DeviceInfoManager;
import node.log.LogWriter;
import node.network.communicator.SocketHandler;

public class NodeDetectionService implements IServiceModule
{
	public static final Logger nodeDetectionLogger = LogWriter.createLogger(NodeDetectionService.class, "nodeDetection");
	
	private DB_Handler dbHandler;
	private DeviceInfoManager deviceInfoManager;
	private SocketHandler socketHandler;
	
	private NodeBroadcast nodeBroadcast;
	private NodeBroadcastReceiver nodeBroadcastReceiver;
	private MasterNodeBroadcast masterNodeBroadcast;
	private MasterNodeReceiver masterNodeReceiver;
	
	private boolean isDHCPNode;

	
	
	public NodeDetectionService(DB_Handler dbHandler, DeviceInfoManager deviceInfoManager, SocketHandler socketHandler)
	{
		this.dbHandler = dbHandler;
		this.socketHandler = socketHandler;
		this.deviceInfoManager = deviceInfoManager;
		this.deviceInfoManager = deviceInfoManager;
		
		this.nodeBroadcast = new NodeBroadcast(this.deviceInfoManager, this.socketHandler);
		this.nodeBroadcastReceiver = new NodeBroadcastReceiver(this.dbHandler, this.socketHandler);
		this.masterNodeBroadcast = new MasterNodeBroadcast(this.dbHandler, this.deviceInfoManager, this.socketHandler);
		this.masterNodeReceiver = new MasterNodeReceiver();
		
		
		this.isDHCPNode = false;
	}
	
	private void startScan()
	{
		this.nodeBroadcast.startModule();
		this.nodeBroadcastReceiver.startModule();
	
	}

	@Override
	public boolean startModule()
	{
		
		
		
		return true;
	}

	@Override
	public void stopModule()
	{
		// TODO Auto-generated method stub
		
	}
}
