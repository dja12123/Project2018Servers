package node.detection;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.IServiceModule;
import node.NodeControlCore;
import node.db.DB_Handler;
import node.detection.masterNode.MasterNodeService;
import node.detection.workNode.WorkNodeService;
import node.device.Device;
import node.device.DeviceInfoManager;
import node.gpio.lcd.LCDControl;
import node.gpio.lcd.LCDObject;
import node.log.LogWriter;
import node.network.NetworkManager;
import node.util.observer.Observable;

public class NodeDetectionService extends Observable<NodeDetectionEvent> implements IServiceModule
{// 마스터 노드 변경 관련 서비스.
	public static final Logger logger = LogWriter.createLogger(NodeDetectionService.class, "nodeDetection");
	
	public static final String PROP_delayMasterNodeBroadcast = "delayMasterNodeBroadcast";
	public static final String PROP_masternodeIP = "masternodeIP";
	
	public static final int STATE_INIT = 0;
	public static final int STATE_WORKNODE = 1;
	public static final int STATE_MASTERNODE = 2;
	
	private int state;
	
	private DB_Handler dbHandler;
	private DeviceInfoManager deviceInfoManager;
	private NetworkManager networkManager;
	
	private NodeInstaller nodeInstaller;
	private WorkNodeService workNodeService;
	private MasterNodeService masterNodeService;
	
	private LCDObject nodeDetectionStr;
	
	private UUID masterNode;
	
	public NodeDetectionService(DB_Handler dbHandler, DeviceInfoManager deviceInfoManager, NetworkManager networkManager)
	{
		this.dbHandler = dbHandler;
		this.networkManager = networkManager;
		this.deviceInfoManager = deviceInfoManager;

		this.nodeInstaller = new NodeInstaller(this, this.networkManager);
		this.workNodeService = new WorkNodeService(this, this.deviceInfoManager, this.networkManager);
		this.masterNodeService = new MasterNodeService(this, this.deviceInfoManager, this.networkManager);
		
		this.nodeDetectionStr = null;
	}
	
	public synchronized void nodeInit()
	{
		LCDControl.inst.removeShape(this.nodeDetectionStr);
		this.nodeDetectionStr = LCDControl.inst.showString(-1, 0, "노드감지:init");
		this.state = STATE_INIT;
		this.masterNodeService.stop();
		this.workNodeService.stop();
		this.nodeInstaller.start();
		this.masterNode = null;
		
		NodeDetectionEvent e = new NodeDetectionEvent(null, false, NodeDetectionEvent.STATE_FAIL);
		this.notifyObservers(NodeControlCore.mainThreadPool, e);
	}
	
	public synchronized void workNodeSelectionCallback(NodeInfoProtocol nodeInfoProtocol)
	{
		LCDControl.inst.removeShape(this.nodeDetectionStr);
		this.nodeDetectionStr = LCDControl.inst.removeShapeTimer(LCDControl.inst.showString(-1, 0, "노드감지:worker"), 1000);
		
		this.state = STATE_WORKNODE;
		this.nodeInstaller.stop();
		this.masterNodeService.stop();
		this.workNodeService.start(nodeInfoProtocol);
		this.masterNode = nodeInfoProtocol.getMasterNode();
		
		Device masterNode = this.deviceInfoManager.getDevice(this.masterNode);
		NodeDetectionEvent e = new NodeDetectionEvent(masterNode.getInetAddr(), false, NodeDetectionEvent.STATE_UPLINK);
		this.notifyObservers(NodeControlCore.mainThreadPool, e);
	}
	
	public synchronized void masterNodeSelectionCallback()
	{
		LCDControl.inst.removeShape(this.nodeDetectionStr);
		this.nodeDetectionStr = LCDControl.inst.removeShapeTimer(LCDControl.inst.showString(-1, 0, "노드감지:master"), 1000);
		
		this.state = STATE_MASTERNODE;
		this.nodeInstaller.stop();
		this.workNodeService.stop();
		this.masterNodeService.start();
		this.masterNode = this.deviceInfoManager.getMyDevice().uuid;
		
		Device masterNode = this.deviceInfoManager.getDevice(this.masterNode);
		NodeDetectionEvent e = new NodeDetectionEvent(masterNode.getInetAddr(), true, NodeDetectionEvent.STATE_UPLINK);
		this.notifyObservers(NodeControlCore.mainThreadPool, e);
	}
	
	public UUID getMasterNode()
	{
		return this.masterNode;
	}

	@Override
	public boolean startModule()
	{
		logger.log(Level.INFO, "노드 감지 서비스 활성화");
		this.nodeInit();
		return true;
	}

	@Override
	public void stopModule()
	{
		this.nodeInstaller.stop();
		this.workNodeService.stop();
		this.masterNodeService.stop();
		this.clearObservers();
	}
}
