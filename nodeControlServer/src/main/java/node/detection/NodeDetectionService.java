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
import node.device.DeviceInfoDelegate;
import node.device.DeviceInfoManager;
import node.device.DeviceStateChangeEvent;
import node.log.LogWriter;
import node.network.communicator.NetworkEvent;
import node.network.communicator.SocketHandler;
import node.network.packet.Packet;
import node.network.packet.PacketUtil;
import node.util.observer.Observable;

public class NodeDetectionService extends Observable<NetworkStateChangeEvent> implements IServiceModule, Runnable, IDeviceStateUpdater
{// 마스터 노드 변경 관련 서비스.
	public static final Logger nodeDetectionLogger = LogWriter.createLogger(NodeDetectionService.class, "nodeDetection");
	
	public static final int CHECK_INTERVAL = 5000;
	public static final int TIMEOUT = 3000;
	
	private DB_Handler dbHandler;
	private DeviceInfoManager deviceInfoManager;
	private DeviceInfoDelegate deviceInfoDelegate;
	private SocketHandler socketHandler;
	
	//private NodeBroadcast nodeBroadcast;
	private NodeInstaller nodeInstaller;
	private WorkNodeService workNodeService;
	private MasterNodeService masterNodeService;
	
	private boolean isDHCPNode;
	private Thread manageThread;
	private boolean isRun;

	public NodeDetectionService(DB_Handler dbHandler, DeviceInfoManager deviceInfoManager, SocketHandler socketHandler)
	{
		this.dbHandler = dbHandler;
		this.socketHandler = socketHandler;
		this.deviceInfoManager = deviceInfoManager;
		this.deviceInfoDelegate = new DeviceInfoDelegate(this.deviceInfoManager);

		//this.nodeBroadcast = new NodeBroadcast(this.deviceInfoManager, this.socketHandler);
		this.nodeInstaller = new NodeInstaller(this, this.socketHandler);
		this.workNodeService = new WorkNodeService(this.deviceInfoManager, this, this.socketHandler);
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
		if(this.isRun) return true;
		nodeDetectionLogger.log(Level.INFO, "노드 감지 서비스 활성화");
		this.manageThread.start();
		this.nodeInstaller.start();
		return true;
	}

	@Override
	public void stopModule()
	{
		if(!this.isRun) return;
		nodeDetectionLogger.log(Level.INFO, "노드 감지 서비스 종료");
		this.isRun = false;
		this.manageThread.interrupt();
	}
	
	public synchronized void updateDevice(UUID uuid, InetAddress inetAddr, boolean isDHCP)
	{// 장치 정보관리 모듈과 연결해줌.
	 // 장치 정보가 수정되었을 때.
		this.deviceInfoDelegate.updateDevice(uuid, inetAddr, isDHCP);
	}
	
	@Override
	public void run()
	{// 장치가 타임아웃 됬을 경우를 감지.
		Date compareTime;
		LinkedList<Device> removeDevices = new LinkedList<Device>();
		while(this.isRun)
		{
			synchronized (this)
			{
				compareTime = new Date(System.currentTimeMillis() + TIMEOUT);
				removeDevices.clear();
				
				for(Device device : this.deviceInfoManager.getDevices())
				{
					if(device.getUpdateTime().after(compareTime))
					{//타임아웃일때
						removeDevices.add(device);
					}
				}
				
				for(Device device : removeDevices)
				{
					this.deviceInfoDelegate.removeDevice(device.uuid);
				}
			}

			try
			{
				Thread.sleep(CHECK_INTERVAL);
			}
			catch (InterruptedException e)
			{
				break;
			}
		}
	}
}
