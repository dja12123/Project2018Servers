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
import node.network.communicator.NetworkEvent;
import node.network.communicator.SocketHandler;
import node.network.packet.Packet;
import node.network.packet.PacketBuildFailureException;
import node.network.packet.PacketBuilder;
import node.network.packet.PacketUtil;
import node.util.observer.Observable;
import node.util.observer.Observer;

public class WorkNodeService implements Runnable, Observer<NetworkEvent>
{
	public static final String PROP_DELAY_INFOMSG = "delayInitBroadcast";
	public static final String KPROTO_NODE_INFO_MSG = "workNodeAlert";
	
	private final DeviceInfoManager deviceInfoManager;
	private final SocketHandler socketHandler;
	
	private int broadCastDelay;
	private Thread broadcastThread = null;
	private boolean isRun = false;
	
	private Device masterNode;
	private NodeTable myMasterNodeTable;
	
	public WorkNodeService(DeviceInfoManager deviceInfoManager, SocketHandler socketHandler)
	{
		this.deviceInfoManager = deviceInfoManager;
		this.socketHandler = socketHandler;
	}
	
	public Device getMasterNode()
	{
		return this.masterNode;
	}

	@Override
	public void run()
	{
		NodeDetectionService.nodeDetectionLogger.log(Level.INFO, "노드 알림 시작");
		while(this.isRun)
		{
			PacketBuilder builder = new PacketBuilder();
			
			Packet packet;
			try
			{
				builder.setSender(this.deviceInfoManager.getMyDevice().uuid);
				builder.setReceiver(this.masterNode.uuid);
				builder.setKey(KPROTO_NODE_INFO_MSG);
				packet = builder.createPacket();
			}
			catch (PacketBuildFailureException e)
			{
				NodeDetectionService.nodeDetectionLogger.log(Level.SEVERE, "마스터노드에게 알리는 패킷 생성중 오류.", e);
				return;
			}
			this.socketHandler.sendMessage(this.masterNode.getInetAddr(), packet);
			try
			{
				Thread.sleep(this.broadCastDelay);
			}
			
			catch (InterruptedException e) {}
		}
	}
	
	public void start(Packet masterNodePacket)
	{	
		if(this.isRun) return;
		this.socketHandler.addObserver(MasterNodeService.KPROTO_MASTER_BROADCAST, this);
		this.isRun = true;
		
		this.processFromMasterNodePacket(masterNodePacket);
		
		this.masterNode = this.deviceInfoManager.getDevice(masterNodePacket.getSender());
		
		this.broadCastDelay = Integer.parseInt(NodeControlCore.getProp(PROP_DELAY_INFOMSG));
		
		this.broadcastThread = new Thread(this);
		this.broadcastThread.start();
		return;
	}
	
	public void stop()
	{
		if(!this.isRun) return;
		this.socketHandler.removeObserver(this);
		this.isRun = false;
		this.broadcastThread.interrupt();
	}
	
	private void processFromMasterNodePacket(Packet masterNodePacket)
	{
		NodeTable nodeTable = new NodeTable(masterNodePacket);
		this.myMasterNodeTable = nodeTable;
		
		for(int i = 0; i < nodeTable.size; ++i)
		{
			UUID taskUID = nodeTable.uuids[i];
			InetAddress taskAddr = nodeTable.addrs[i];
			boolean taskIsMaster = false;
			
			if(masterNodePacket.getSender().equals(taskUID))
			{
				taskIsMaster = true;
			}
			
			this.deviceInfoManager.updateDevice(taskUID, taskAddr, taskIsMaster);
		}
	}

	@Override
	public synchronized void update(Observable<NetworkEvent> object, NetworkEvent data)
	{
		if(data.key.equals(MasterNodeService.KPROTO_MASTER_BROADCAST))
		{
			if(data.packet.getSender().equals(this.masterNode.uuid))
			{// 내 마스터 노드일경우!!
				this.processFromMasterNodePacket(data.packet);
			}
			else
			{// 새로운 마스터 노드가 내 마스터 노드가 아닐경우!
				NodeTable nodeTable = new NodeTable(data.packet);
				NodeDetectionService.nodeDetectionLogger.log(Level.WARNING, "마스터 노드 겹침 확인. ("+nodeTable.masterNode.toString()+")");
				if(nodeTable.size == this.myMasterNodeTable.size)
				{// 상대 마스터 노드와 내 마스터 노드의 추종자 노드 개수가 같을때.
					if(nodeTable.masterNode.toString().compareTo(this.masterNode.uuid.toString()) > 0)
					{// UUID 비교.
						this.changeMasterNode(data.packet);
					}
				}
				else if(nodeTable.size > this.myMasterNodeTable.size)
				{// 상대 마스터 노드의 추종자 노드가 더 많을때.
					this.changeMasterNode(data.packet);
				}
			}
		}
	}
	
	private void changeMasterNode(Packet masterNodePacket)
	{
		NodeDetectionService.nodeDetectionLogger.log(Level.WARNING, "마스터 노드 변경. ("+masterNodePacket.getSender().toString()+")");
		List<Device> devices = new ArrayList<>();
		for(Device removeDevice : devices)
		{
			this.deviceInfoManager.removeDevice(removeDevice.uuid);
		}
		
		this.processFromMasterNodePacket(masterNodePacket);
	}
}

class NodeTable
{	
	public final UUID masterNode;
	public final UUID[] uuids;
	public final InetAddress[] addrs;
	public final int size;
	
	public NodeTable(Packet masterNodePacket)
	{
		String[][] nodeInfoStr = PacketUtil.getDataArray(masterNodePacket);
		
		this.size = nodeInfoStr.length;
		this.uuids = new UUID[this.size];
		this.addrs = new InetAddress[this.size];
		
		for(int i = 0; i < this.size; ++i)
		{
			this.uuids[i] = UUID.fromString(nodeInfoStr[i][0]);
			try
			{
				this.addrs[i] = InetAddress.getByName(nodeInfoStr[i][1]);
			}
			catch (UnknownHostException e)
			{
				NodeDetectionService.nodeDetectionLogger.log(Level.SEVERE, "마스터 노드로부터 전송된 IP 정보 손상", e);
			}
		}
		
		this.masterNode = masterNodePacket.getSender();
	}
}