package node.detection.initService;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.IServiceModule;
import node.NodeControlCore;
import node.db.DB_Handler;
import node.detection.NodeDetectionService;
import node.device.Device;
import node.device.DeviceInfoManager;
import node.log.LogWriter;
import node.network.NetworkManager;
import node.network.communicator.SocketHandler;
import node.network.packet.Packet;
import node.network.packet.PacketBuildFailureException;
import node.network.packet.PacketBuilder;
import node.network.packet.PacketUtil;

public class NodeBroadcast implements Runnable, IServiceModule
{
	public static final String PROP_DELAY_INFOMSG = "delayInitBroadcast";
	public static final String NODE_INIT_BROADCAST_MSG = "infoBroadcast";
	
	private final DeviceInfoManager deviceInfoManager;
	private final SocketHandler socketHandler;
	
	private int broadCastDelay;
	private Thread broadcastThread = null;
	private boolean isRun = false;
	
	private Packet packet;
	
	public NodeBroadcast(DeviceInfoManager deviceInfoManager, SocketHandler socketHandler)
	{
		this.deviceInfoManager = deviceInfoManager;
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
			this.socketHandler.sendMessage(PacketUtil.broadcastIA(), packet);
		}
	}
	
	public boolean startModule()
	{
		if(this.isRun) return true;
		this.isRun = true;
		
		PacketBuilder builder = new PacketBuilder();
		
		try
		{
			builder.setSender(this.deviceInfoManager.getMyDevice().uuid);
			builder.setBroadCast();
			builder.setKey(NODE_INIT_BROADCAST_MSG);
			this.packet = builder.createPacket();
		}
		catch (PacketBuildFailureException e)
		{
			NodeDetectionService.nodeDetectionLogger.log(Level.SEVERE, "브로드케스트 패킷 생성 오류", e);
			return false;
		}
		
		this.broadCastDelay = Integer.parseInt(NodeControlCore.getProp(PROP_DELAY_INFOMSG));
		
		this.broadcastThread = new Thread(this);
		this.broadcastThread.start();
		return true;
	}
	
	public void stopModule()
	{
		if(!this.isRun) return;
		this.isRun = false;
		
		this.broadcastThread.interrupt();
	}
}