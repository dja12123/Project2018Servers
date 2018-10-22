package node.detection;

import java.net.InetAddress;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.IServiceModule;
import node.NodeControlCore;
import node.db.DB_Handler;
import node.device.Device;
import node.device.DeviceInfoManager;
import node.device.DeviceStateChangeEvent;
import node.log.LogWriter;
import node.network.communicator.NetworkEvent;
import node.network.communicator.SocketHandler;
import node.network.packet.Packet;
import node.network.packet.PacketUtil;
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
		
		this.isDHCPNode = false;
	}
	
	public void masterNodeDetection(NetworkEvent masterNodeEvent)
	{
		this.workNodeService.start(masterNodeEvent.packet);
	}
	
	public void myMasterNode()
	{
		
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

	}

	@Override
	public void update(Observable<DeviceStateChangeEvent> object, DeviceStateChangeEvent data)
	{
		
		
	}
}
