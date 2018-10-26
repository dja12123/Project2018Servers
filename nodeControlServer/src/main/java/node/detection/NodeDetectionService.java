package node.detection;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.IServiceModule;
import node.db.DB_Handler;
import node.device.DeviceInfoManager;
import node.device.DeviceStateChangeEvent;
import node.log.LogWriter;
import node.network.NetworkManager;
import node.network.communicator.NetworkEvent;
import node.network.communicator.SocketHandler;
import node.util.observer.Observable;
import node.util.observer.Observer;

public class NodeDetectionService extends Observable<NetworkStateChangeEvent> implements IServiceModule
{// 마스터 노드 변경 관련 서비스.
	public static final Logger nodeDetectionLogger = LogWriter.createLogger(NodeDetectionService.class, "nodeDetection");
	
	public static final int STATE_INIT = 0;
	public static final int STATE_WORKNODE = 1;
	public static final int STATE_MASTERNODE = 2;
	
	private int state;
	
	private DB_Handler dbHandler;
	private DeviceInfoManager deviceInfoManager;
	private NetworkManager networkManager;
	
	//private NodeBroadcast nodeBroadcast;
	private NodeInstaller nodeInstaller;
	private WorkNodeService workNodeService;
	private MasterNodeService masterNodeService;
	
	public NodeDetectionService(DB_Handler dbHandler, DeviceInfoManager deviceInfoManager, NetworkManager networkManager)
	{
		this.dbHandler = dbHandler;
		this.networkManager = networkManager;
		this.deviceInfoManager = deviceInfoManager;

		//this.nodeBroadcast = new NodeBroadcast(this.deviceInfoManager, this.socketHandler);
		this.nodeInstaller = new NodeInstaller(this, this.networkManager);
		this.workNodeService = new WorkNodeService(this.deviceInfoManager, this.networkManager);
		this.masterNodeService = new MasterNodeService(this.deviceInfoManager, this.networkManager);
		
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
		this.nodeInit();
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

	public void updateNetwork(Observable<NetworkEvent> object, NetworkEvent data)
	{// 마스터노드 관련 변경사항은 각 모듈에서 처리해줌..
		UUID sender = data.packet.getSender();
		/*if(this.state == STATE_WORKNODE)
		{
			if(data.key.equals(MasterNodeService.KPROTO_MASTER_BROADCAST))
			{
				if(data.device.isMasterNode())
				{// 한 네트워크 세그먼트 상에서 내 마스터 노드가 아닌 다른 마스터 노드가 감지될때.
					this.workNodeSelectionCallback(data);
					
				}
				
			}
			if(data.getState(DeviceStateChangeEvent.DISCONNECT_DEVICE))
			{
				if(data.device.equals(this.workNodeService.getMasterNode()))
				{// 마스터 노드가 사망했을때.
					this.nodeInit();
					
					
				}
			}
		}*/
		
		
		
		
	}
}
