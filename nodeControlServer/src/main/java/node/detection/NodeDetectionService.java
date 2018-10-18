package node.detection;

import java.util.logging.Logger;

import node.IServiceModule;
import node.db.DB_Handler;
import node.detection.initService.NodeBroadcast;
import node.detection.initService.NodeInstaller;
import node.detection.masterNodeService.MasterNodeBroadcast;
import node.detection.masterNodeService.MasterNodeReceiver;
import node.device.DeviceInfoManager;
import node.log.LogWriter;
import node.network.communicator.NetworkEvent;
import node.network.communicator.SocketHandler;
import node.util.observer.Observable;

public class NodeDetectionService extends Observable<NetworkStateChangeEvent> implements IServiceModule
{
	public static final Logger nodeDetectionLogger = LogWriter.createLogger(NodeDetectionService.class, "nodeDetection");
	
	private DB_Handler dbHandler;
	private DeviceInfoManager deviceInfoManager;
	private SocketHandler socketHandler;
	
	private NodeBroadcast nodeBroadcast;
	private NodeInstaller nodeInstaller;
	private MasterNodeBroadcast masterNodeBroadcast;
	private MasterNodeReceiver masterNodeReceiver;
	
	private boolean isDHCPNode;

	public NodeDetectionService(DB_Handler dbHandler, DeviceInfoManager deviceInfoManager, SocketHandler socketHandler)
	{
		this.dbHandler = dbHandler;
		this.socketHandler = socketHandler;
		this.deviceInfoManager = deviceInfoManager;

		this.nodeBroadcast = new NodeBroadcast(this.deviceInfoManager, this.socketHandler);
		this.nodeInstaller = new NodeInstaller(this, this.socketHandler);
		this.masterNodeBroadcast = new MasterNodeBroadcast(this.deviceInfoManager, this.socketHandler);
		
		this.masterNodeReceiver = new MasterNodeReceiver();
		
		
		this.isDHCPNode = false;
	}
	
	private void startScan()
	{
		
	
	}
	
	public void masterNodeDetection(NetworkEvent masterNodeData)
	{
		
	}
	
	public void myMasterNode()
	{
		
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
