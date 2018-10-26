package node.detection;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import node.NodeControlCore;
import node.device.Device;
import node.device.DeviceInfoManager;
import node.device.DeviceStateChangeEvent;
import node.network.NetworkManager;
import node.network.communicator.NetworkEvent;
import node.network.communicator.SocketHandler;
import node.network.packet.Packet;
import node.network.packet.PacketBuildFailureException;
import node.network.packet.PacketBuilder;
import node.network.packet.PacketUtil;
import node.util.observer.Observable;
import node.util.observer.Observer;

public class WorkNodeService implements Runnable
{
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
	private Observer<DeviceStateChangeEvent> deviceStateObserverFunc;
	
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
		NodeDetectionService.nodeDetectionLogger.log(Level.INFO, "노드 알림 시작");
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
					NodeDetectionService.nodeDetectionLogger.log(Level.SEVERE, "마스터노드에게 알리는 패킷 생성중 오류.", e);
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
		if(this.masterNode != null && !nodeInfoProtocol.getMasterNode().equals(this.masterNode))
		{// 마스터 노드가 달라졌으면.
			NodeDetectionService.nodeDetectionLogger.log(Level.WARNING, "마스터 노드 변경. ("+nodeInfoProtocol.getMasterNode().toString()+")");
			List<Device> devices = new ArrayList<>();
			for(Device removeDevice : devices)
			{
				this.deviceInfoManager.removeDevice(removeDevice.uuid);
			}
			
			this.processFromMasterNodePacket(nodeInfoProtocol);
		}
		if(this.isRun) return;
		this.networkManager.socketHandler.addObserver(WorkNodeService.KPROTO_NODE_INFO_MSG, this.networkObserverFunc);
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
		this.networkManager.socketHandler.removeObserver(this.networkObserverFunc);
		this.deviceInfoManager.removeObserver(this.deviceStateObserverFunc);
		this.isRun = false;
		this.broadcastThread.interrupt();
	}
	
	private void processFromMasterNodePacket(NodeInfoProtocol nodeInfoProtocol)
	{
		
		for(int i = 0; i < nodeInfoProtocol.getSize(); ++i)
		{
			UUID taskUID = nodeInfoProtocol.getUUID(i);
			InetAddress taskAddr = nodeInfoProtocol.getAddr(i);
			boolean taskIsMaster = false;
			
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
					this.nodeDetectionService.workNodeSelectionCallback(nodeInfoProtocol);
				}
			}
		}
	}
	
	public synchronized void updateDeviceState(Observable<DeviceStateChangeEvent> object, DeviceStateChangeEvent data)
	{
		if(data.device.uuid.equals(this.masterNode))
		{
			if(data.getState(DeviceStateChangeEvent.DISCONNECT_DEVICE))
			{
				this.nodeDetectionService.nodeInit();
			}
		}
	}
}

