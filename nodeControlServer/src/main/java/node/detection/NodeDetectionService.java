package node.detection;

import java.util.logging.Level;
import java.util.logging.Logger;

import node.IServiceModule;
import node.db.DB_Handler;
import node.device.DeviceInfoManager;
import node.device.DeviceStateChangeEvent;
import node.log.LogWriter;
import node.network.communicator.NetworkEvent;
import node.network.communicator.SocketHandler;
import node.util.observer.Observable;
import node.util.observer.Observer;

public class NodeDetectionService extends Observable<NetworkStateChangeEvent> implements IServiceModule, Observer<DeviceStateChangeEvent>
{// 마스터 노드 변경 관련 서비스.
	public static final Logger nodeDetectionLogger = LogWriter.createLogger(NodeDetectionService.class, "nodeDetection");
	
	public static final int STATE_INIT = 0;
	public static final int STATE_WORKNODE = 1;
	public static final int STATE_MASTERNODE = 2;
	
	private int state;
	
	private DB_Handler dbHandler;
	private DeviceInfoManager deviceInfoManager;
	private SocketHandler socketHandler;
	
	//private NodeBroadcast nodeBroadcast;
	private NodeInstaller nodeInstaller;
	private WorkNodeService workNodeService;
	private MasterNodeService masterNodeService;
	
	public NodeDetectionService(DB_Handler dbHandler, DeviceInfoManager deviceInfoManager, SocketHandler socketHandler)
	{
		this.dbHandler = dbHandler;
		this.socketHandler = socketHandler;
		this.deviceInfoManager = deviceInfoManager;

		//this.nodeBroadcast = new NodeBroadcast(this.deviceInfoManager, this.socketHandler);
		this.nodeInstaller = new NodeInstaller(this, this.socketHandler);
		this.workNodeService = new WorkNodeService(this.deviceInfoManager, this.socketHandler);
		this.masterNodeService = new MasterNodeService(this.deviceInfoManager, this.socketHandler);
		
		this.nodeInit();
	}
	
	private void nodeInit()
	{
		this.state = STATE_INIT;
		this.masterNodeService.stop();
		this.workNodeService.stop();
		this.nodeInstaller.start();
	}
	
	public void workNodeSelectionCallback(NetworkEvent masterNodeEvent)
	{
		this.state = STATE_WORKNODE;
		this.nodeInstaller.stop();
		this.masterNodeService.stop();
		this.workNodeService.start(masterNodeEvent.packet);
	}
	
	public void masterNodeSelectionCallback()
	{
		this.state = STATE_MASTERNODE;
		this.nodeInstaller.stop();
		this.workNodeService.stop();
		this.masterNodeService.start();
	}

	@Override
	public boolean startModule()
	{
		nodeDetectionLogger.log(Level.INFO, "노드 감지 서비스 활성화");

		this.nodeInstaller.start();
		return true;
	}

	@Override
	public void stopModule()
	{
		this.nodeInstaller.stop();
		this.workNodeService.stop();
		this.masterNodeService.stop();
	}

	@Override
	public void update(Observable<DeviceStateChangeEvent> object, DeviceStateChangeEvent data)
	{
		if(this.state == STATE_WORKNODE)
		{
			data.device.equals(this.workNodeService.getDhcpDevice());
		}
		
		
	}
}
