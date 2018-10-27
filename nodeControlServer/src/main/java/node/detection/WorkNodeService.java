package node.detection;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.NodeControlCore;
import node.device.Device;
import node.device.DeviceInfoManager;
import node.device.DeviceChangeEvent;
import node.log.LogWriter;
import node.network.NetworkManager;
import node.network.NetworkEvent;
import node.network.packet.Packet;
import node.network.packet.PacketBuildFailureException;
import node.network.packet.PacketBuilder;
import node.util.observer.Observable;
import node.util.observer.Observer;

public class WorkNodeService implements Runnable
{
	public static final Logger logger = LogWriter.createLogger(WorkNodeService.class, "workNodeService");
	
	public static final String PROP_DELAY_INFOMSG = "delayInitBroadcast";
	public static final String KPROTO_NODE_INFO_MSG = "workNodeAlert";
	
	private final NodeDetectionService nodeDetectionService;
	private final DeviceInfoManager deviceInfoManager;
	private final NetworkManager networkManager;
	
	private int broadCastDelay;
	private Thread broadcastThread = null;
	private boolean isRun = false;
	
	private UUID masterNode;
	
	private Observer<NetworkEvent> networkObserverFunc;
	private Observer<DeviceChangeEvent> deviceStateObserverFunc;
	
	public WorkNodeService(NodeDetectionService nodeDetectionService, DeviceInfoManager deviceInfoManager, NetworkManager networkManager)
	{
		this.nodeDetectionService = nodeDetectionService;
		this.deviceInfoManager = deviceInfoManager;
		this.networkManager = networkManager;
		
		this.networkObserverFunc = this::updateNetwork;
		this.deviceStateObserverFunc = this::updateDeviceState;
	}

	@Override
	public void run()
	{
		while(this.isRun)
		{
			synchronized (this)
			{
				PacketBuilder builder = new PacketBuilder();
				
				Packet packet;
				try
				{
					builder.setSender(this.deviceInfoManager.getMyDevice().uuid);
					builder.setReceiver(this.masterNode);
					builder.setKey(KPROTO_NODE_INFO_MSG);
					packet = builder.createPacket();
				}
				catch (PacketBuildFailureException e)
				{
					logger.log(Level.SEVERE, "마스터노드에게 알리는 패킷 생성중 오류.", e);
					return;
				}
				this.networkManager.socketHandler.sendMessage(packet);
			}
			
			try
			{
				Thread.sleep(this.broadCastDelay);
			}
			
			catch (InterruptedException e) {}
		}
	}
	
	public synchronized void start(NodeInfoProtocol nodeInfoProtocol)
	{
		/*if(this.masterNode != null && !nodeInfoProtocol.getMasterNode().equals(this.masterNode))
		{// 마스터 노드가 달라졌으면.
			NodeDetectionService.logger.log(Level.WARNING, "마스터 노드 변경 ("+nodeInfoProtocol.getMasterNode().toString()+")");
			List<Device> devices = new ArrayList<>();
			for(Device removeDevice : devices)
			{
				this.deviceInfoManager.removeDevice(removeDevice.uuid);
			}
			
			this.processFromMasterNodePacket(nodeInfoProtocol);
		}*/
		if(this.isRun) return;
		logger.log(Level.INFO, "워커 노드 서비스 시작");
		this.networkManager.addObserver(WorkNodeService.KPROTO_NODE_INFO_MSG, this.networkObserverFunc);
		this.deviceInfoManager.addObserver(this.deviceStateObserverFunc);
		
		this.processFromMasterNodePacket(nodeInfoProtocol);
		
		this.masterNode = nodeInfoProtocol.getMasterNode();
		
		this.broadCastDelay = Integer.parseInt(NodeControlCore.getProp(PROP_DELAY_INFOMSG));
		
		this.isRun = true;
		this.broadcastThread = new Thread(this);
		this.broadcastThread.start();
		return;
	}
	
	public synchronized void stop()
	{
		if(!this.isRun) return;
		new Exception().printStackTrace();
		logger.log(Level.INFO, "워커 노드 서비스 중지");
		this.networkManager.removeObserver(this.networkObserverFunc);
		this.deviceInfoManager.removeObserver(this.deviceStateObserverFunc);
		this.isRun = false;
		this.broadcastThread.interrupt();
	}
	
	private void processFromMasterNodePacket(NodeInfoProtocol nodeInfoProtocol)
	{
		Device myDevice = this.deviceInfoManager.getMyDevice();
		for(int i = 0; i < nodeInfoProtocol.getSize(); ++i)
		{
			UUID taskUID = nodeInfoProtocol.getUUID(i);
			InetAddress taskAddr = nodeInfoProtocol.getAddr(i);
			boolean taskIsMaster = false;
			if(taskUID.equals(myDevice.uuid))
			{
				InetAddress myDeviceInet = myDevice.getInetAddr();
				if(myDeviceInet != null && myDeviceInet.equals(taskAddr))
				{// 내 아이피가 문제 없을때.
					
				}
				else
				{// 내 아이피가 문제 있을때.
					this.networkManager.setInetAddr(taskAddr);
					logger.log(Level.INFO, String.format("IP설정 (%s)", taskAddr.getHostAddress()));
				}
			}
			
			if(nodeInfoProtocol.getMasterNode().equals(taskUID))
			{
				taskIsMaster = true;
			}
			
			this.deviceInfoManager.updateDevice(taskUID, taskAddr, taskIsMaster);
		}
	}

	public synchronized void updateNetwork(Observable<NetworkEvent> object, NetworkEvent data)
	{
		if(data.key.equals(MasterNodeService.KPROTO_MASTER_BROADCAST))
		{
			NodeInfoProtocol nodeInfoProtocol = new NodeInfoProtocol(data.packet);
			if(nodeInfoProtocol.getMasterNode().equals(this.masterNode))
			{// 내 마스터 노드일경우!!
				this.processFromMasterNodePacket(nodeInfoProtocol);
			}
			else
			{// 새로운 마스터 노드가 내 마스터 노드가 아닐경우!
				if(DetectionUtil.isChangeMasterNode(nodeInfoProtocol, this.masterNode, this.deviceInfoManager))
				{
					logger.log(Level.INFO, String.format("마스터 노드 변경 (%s -> %s)",
							this.masterNode.toString(), nodeInfoProtocol.getMasterNode().toString()));
					this.nodeDetectionService.workNodeSelectionCallback(nodeInfoProtocol);
				}
			
			}
		}
	}
	
	public synchronized void updateDeviceState(Observable<DeviceChangeEvent> object, DeviceChangeEvent data)
	{
		if(data.device.uuid.equals(this.masterNode))
		{
			if(data.getState(DeviceChangeEvent.DISCONNECT_DEVICE))
			{
				this.nodeDetectionService.nodeInit();
			}
		}
	}
}

