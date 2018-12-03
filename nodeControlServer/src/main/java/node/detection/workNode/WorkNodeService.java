package node.detection.workNode;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.NodeControlCore;
import node.detection.DetectionUtil;
import node.detection.NodeDetectionService;
import node.detection.NodeInfoProtocol;
import node.detection.masterNode.MasterNodeService;
import node.device.Device;
import node.device.DeviceInfoManager;
import node.gpio.lcd.LCDControl;
import node.gpio.lcd.LCDObject;
import node.device.DeviceChangeEvent;
import node.log.LogWriter;
import node.network.NetworkManager;
import node.network.protocol.keyvaluePacket.Packet;
import node.network.protocol.keyvaluePacket.PacketBuildFailureException;
import node.network.protocol.keyvaluePacket.PacketBuilder;
import node.network.NetworkEvent;
import node.util.observer.Observable;
import node.util.observer.Observer;

public class WorkNodeService implements Runnable
{
	public static final Logger logger = LogWriter.createLogger(WorkNodeService.class, "workNodeService");
	
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

	private LCDObject ipNoStr;
	private LCDObject masterSigRect;
	private LCDObject sendMsgRect;
	private LCDObject stateStr;
	
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
			try
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
					this.networkManager.sendMessage(packet);
					LCDControl.inst.blinkShape(this.sendMsgRect, 300, 1);
				}
			}
			catch(Exception e)
			{
			
				e.printStackTrace();
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
		this.isRun = true;
		
		logger.log(Level.INFO, "워커 노드 서비스 시작");
		
		this.networkManager.setInetAddr(DetectionUtil.workDefaultAddr());
		this.networkManager.addObserver(MasterNodeService.KPROTO_MASTER_BROADCAST, this.networkObserverFunc);
		this.deviceInfoManager.addObserver(this.deviceStateObserverFunc);
		
		this.processFromMasterNodePacket(nodeInfoProtocol);
		
		this.masterNode = nodeInfoProtocol.getMasterNode();
		this.broadCastDelay = Integer.parseInt(NodeControlCore.getProp(DetectionUtil.PROP_delayWorkerBroadcast));
		
		this.broadcastThread = new Thread(this);
		this.broadcastThread.start();
		
		this.ipNoStr = LCDControl.inst.showString(7, 0, "W:noip");
		this.masterSigRect = LCDControl.inst.showFillRect(0, 1, 5, 5);
		this.sendMsgRect = LCDControl.inst.showRect(0, 7, 5, 5);
		this.stateStr = LCDControl.inst.showString(100, 0, "시작");
	}
	
	public synchronized void stop()
	{
		if(!this.isRun) return;
		logger.log(Level.INFO, "워커 노드 서비스 중지");
		LCDControl.inst.removeShape(this.ipNoStr);
		LCDControl.inst.removeShape(this.masterSigRect);
		LCDControl.inst.removeShape(this.sendMsgRect);
		LCDControl.inst.removeShape(this.stateStr);
		this.networkManager.removeObserver(this.networkObserverFunc);
		this.deviceInfoManager.removeObserver(this.deviceStateObserverFunc);
		this.isRun = false;
		this.broadcastThread.interrupt();
	}
	
	private void processFromMasterNodePacket(NodeInfoProtocol nodeInfoProtocol)
	{
		LCDControl.inst.blinkShape(this.masterSigRect, 300, 1);
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
					this.stateStr = LCDControl.inst.replaceString(this.stateStr, "정상");
					byte[] addr = taskAddr.getAddress();
					String master = this.masterNode.toString();
					master = master.substring(master.length() - 4, master.length() - 1);
					this.ipNoStr = LCDControl.inst.replaceString(this.ipNoStr, String.format("W:%s:%d.%d", master, addr[2], addr[3]));
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
					LCDControl.inst.blinkShape(this.stateStr, 2000, 1);
					LCDControl.inst.removeShapeTimer(LCDControl.inst.showString(100, 0, "충돌"), 1900);
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

