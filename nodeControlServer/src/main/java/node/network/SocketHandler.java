package node.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.IServiceModule;
import node.NodeControlCore;
import node.device.Device;
import node.device.DeviceInfoManager;
import node.log.LogWriter;
import node.network.NetworkManager;
import node.network.NetworkUtil;
import node.network.packet.Packet;
import node.network.packet.PacketUtil;
import node.util.observer.Observable;
import node.util.observer.Observer;

public class SocketHandler implements Runnable
{
	public static final Logger logger = LogWriter.createLogger(SocketHandler.class, "socket");
	
	private final DeviceInfoManager deviceInfoManager;
	private NetworkManager networkManager;
	
	private Thread worker = null;
	private boolean isWork;
	
	private DatagramSocket socket;

	private int port;
	
	public SocketHandler(NetworkManager networkManager, DeviceInfoManager deviceInfoManager)
	{
		this.networkManager = networkManager;
		this.deviceInfoManager = deviceInfoManager;
	}

	public void start()
	{
		if(this.isWork) return;
		logger.log(Level.INFO, "소켓 핸들러 로드");
		if(this.worker == null || !this.worker.isAlive())
		{
			this.worker = new Thread(this);
		}
		
		try
		{
			this.port = Integer.parseInt(NodeControlCore.getProp(NetworkManager.PROP_INFOBROADCAST_PORT));

			String interfaceStr = NodeControlCore.getProp(NetworkManager.PROP_INTERFACE);
			//NetworkUtil.getNetworkInterface(interfaceStr);
			//this.socket = new DatagramSocket(NetworkManager.PROP_SOCKET_INTERFACE)
			this.socket = new DatagramSocket(this.port);
			this.socket.setBroadcast(true);
		}
		catch (SocketException e)
		{
			logger.log(Level.SEVERE, "소켓 열기 실패", e);
			return;
		}
		
		this.isWork = true;
		this.worker.start();
		return;
	}

	public void stop()
	{
		if(!this.isWork) return;
		logger.log(Level.INFO, "소켓 핸들러 종료");
		this.isWork = false;
		this.worker.interrupt();
		this.socket.close();
	}

	@Override
	public void run()
	{
		logger.log(Level.INFO, "네트워크 수신 시작");
		byte[] packetBuffer = new byte[PacketUtil.HEADER_SIZE + PacketUtil.MAX_SIZE_KEY + PacketUtil.MAX_SIZE_DATA];
		DatagramPacket dgramPacket;
		
		while(this.isWork)
		{
			dgramPacket = new DatagramPacket(packetBuffer, packetBuffer.length);
			try
			{
				this.socket.receive(dgramPacket);
				this.networkManager.socketReadCallback(dgramPacket.getAddress(), packetBuffer);
			}
			catch (IOException e)
			{
				if(this.socket.isClosed())
				{
					logger.log(Level.INFO, "소켓 종료");
					return;
				}
				logger.log(Level.SEVERE, "수신 실패", e);
			}
		}
	}
	
	public void sendMessage(Packet packet)
	{
		InetAddress inetAddr;
		if(packet.isBroadcast())
		{
			inetAddr = NetworkUtil.broadcastIA();
		}
		else
		{
			inetAddr = this.deviceInfoManager.getDevice(packet.getReceiver()).getInetAddr();
		}
		if(inetAddr == null)
		{
			logger.log(Level.WARNING, "null주소: " + packet.getReceiver());
		}
		
		byte[] rawPacket = packet.getNativeArr();
		DatagramPacket dgramPacket = new DatagramPacket(rawPacket, rawPacket.length, inetAddr, port);
		
		try
		{
			this.socket.send(dgramPacket);
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "패킷 전송 실패", e);
		}
	}
}