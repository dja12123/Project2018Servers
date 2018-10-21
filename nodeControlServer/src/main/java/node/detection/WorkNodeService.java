package node.detection;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.logging.Level;

import javafx.scene.control.Tab;
import node.IServiceModule;
import node.NodeControlCore;
import node.db.DB_Handler;
import node.device.Device;
import node.device.DeviceInfoManager;
import node.network.NetworkUtil;
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
	
	private final DeviceInfoManager deviceInfoManager;
	private final IDeviceStateUpdater deviceStateUpdater; 
	private final SocketHandler socketHandler;
	
	private int broadCastDelay;
	private Thread broadcastThread = null;
	private boolean isRun = false;
	
	private Packet packet;
	private Device dhcpDevice;
	
	/*public static void main(String[] args)
	{
		NodeControlCore.init();
		DB_Handler db = new DB_Handler();
		db.startModule();
		DeviceInfoManager infoManager = new DeviceInfoManager(db);
		infoManager.startModule();
		SocketHandler sock = new SocketHandler();
		sock.startModule();
		inst.startModule();
		
		sock.addObserver(NODE_INIT_BROADCAST_MSG, new Observer<NetworkEvent>()
		{
			@Override
			public void update(Observable<NetworkEvent> object, NetworkEvent data)
			{
				System.out.println(data.packet.toString());
			}
		});
	}*/
	
	public WorkNodeService(DeviceInfoManager deviceInfoManager, IDeviceStateUpdater deviceStateUpdater, SocketHandler socketHandler)
	{
		this.deviceInfoManager = deviceInfoManager;
		this.deviceStateUpdater = deviceStateUpdater;
		this.socketHandler = socketHandler;
	}

	@Override
	public void run()
	{
		NodeDetectionService.nodeDetectionLogger.log(Level.INFO, "노드 알림 시작");
		while(this.isRun)
		{
			try
			{
				Thread.sleep(this.broadCastDelay);
			}
			catch (InterruptedException e) {}
			this.socketHandler.sendMessage(this.dhcpDevice.getInetAddr(), packet);
		}
	}
	
	public void start(Packet masterNodePacket)
	{	
		if(this.isRun) return;
		this.isRun = true;
		
		NodeTable nodeTable = new NodeTable(masterNodePacket);
		
		for(int i = 0; i < nodeTable.size; ++i)
		{
			UUID taskUID = nodeTable.uuids[i];
			InetAddress taskAddr = nodeTable.addrs[i];
			boolean taskIsDhcp = false;
			
			if(masterNodePacket.getSender().equals(taskUID))
			{
				taskIsDhcp = true;
			}
			
			this.deviceStateUpdater.updateDevice(taskUID, taskAddr, taskIsDhcp);
		}
		
		this.dhcpDevice = this.deviceInfoManager.getDevice(masterNodePacket.getSender());
		
		PacketBuilder builder = new PacketBuilder();
		
		try
		{
			builder.setSender(this.deviceInfoManager.getMyDevice().uuid);
			builder.setBroadCast();
			builder.setKey(KPROTO_NODE_INFO_MSG);
			this.packet = builder.createPacket();
		}
		catch (PacketBuildFailureException e)
		{
			NodeDetectionService.nodeDetectionLogger.log(Level.SEVERE, "브로드케스트 패킷 생성 오류", e);
			return;
		}
		
		this.broadCastDelay = Integer.parseInt(NodeControlCore.getProp(PROP_DELAY_INFOMSG));
		
		this.broadcastThread = new Thread(this);
		this.broadcastThread.start();
		return;
	}
	
	public void stop()
	{
		if(!this.isRun) return;
		this.isRun = false;
		
		this.broadcastThread.interrupt();
	}
}

class NodeTable
{	
	public final UUID dhcpNode;
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
		
		this.dhcpNode = masterNodePacket.getSender();
	}
}