package node.network.socketHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.savarese.rocksaw.net.RawSocket;

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

public class RawSocketReceiver implements Runnable
{
	public static final Logger logger = LogWriter.createLogger(RawSocketReceiver.class, "rawsocket");
	
	private final DeviceInfoManager deviceInfoManager;
	private NetworkManager networkManager;
	
	private Thread worker = null;
	private boolean isWork;
	
	//private DatagramSocket socket;
	private RawSocket rawSocket;

	private int port;
	
	public RawSocketReceiver(NetworkManager networkManager, DeviceInfoManager deviceInfoManager)
	{
		this.networkManager = networkManager;
		this.deviceInfoManager = deviceInfoManager;
		this.rawSocket = new RawSocket();
		
	}

	public void start()
	{
		if(this.isWork) return;
		this.isWork = true;
		
		logger.log(Level.INFO, "로우 소켓 핸들러 로드");
		
		this.worker = new Thread(this);
		
		try
		{
			this.port = Integer.parseInt(NodeControlCore.getProp(NetworkManager.PROP_INFOBROADCAST_PORT));

			//String interfaceStr = NodeControlCore.getProp(NetworkManager.PROP_INTERFACE);
			this.rawSocket.open(RawSocket.PF_INET, RawSocket.getProtocolByName("UDP"));
			//NetworkUtil.getNetworkInterface(interfaceStr);
			//this.socket = new DatagramSocket(NetworkManager.PROP_SOCKET_INTERFACE)
			//this.socket = new DatagramSocket(49800);

			//this.socket.setReuseAddress(false);
			
			//this.socket.setBroadcast(true);
		}
		catch (IllegalStateException | IOException e)
		{
			logger.log(Level.SEVERE, "소켓 열기 실패", e);
			return;
		}
		
		this.worker.start();
		return;
	}

	public void stop()
	{
		if(!this.isWork) return;
		this.isWork = false;
		logger.log(Level.INFO, "소켓 핸들러 종료");
		try
		{
			this.rawSocket.close();
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "로우 소켓 종료중 오류", e);
		}
		this.worker.interrupt();
	}

	@Override
	public void run()
	{
		logger.log(Level.INFO, "네트워크 수신 시작");
		byte[] packetBuffer = new byte[PacketUtil.HEADER_SIZE + PacketUtil.MAX_SIZE_KEY + PacketUtil.MAX_SIZE_DATA];
		int readLen = 0;
		//DatagramPacket dgramPacket;
		
		while(this.isWork)
		{
			//dgramPacket = new DatagramPacket(packetBuffer, packetBuffer.length);

			try
			{
				readLen = this.rawSocket.read(packetBuffer);
				logger.log(Level.INFO, NetworkUtil.bytesToHex(packetBuffer, readLen));
				byte[] copyBuf = Arrays.copyOf(packetBuffer, readLen);
				this.networkManager.socketReadCallback(NetworkUtil.broadcastIA(), copyBuf);
				//System.out.println("receive" + dgramPacket.getAddress());
			}
			catch (IOException e)
			{
				if(!this.rawSocket.isOpen())
				{
					logger.log(Level.INFO, "소켓 종료");
					return;
				}
				logger.log(Level.SEVERE, "수신 실패", e);
			}
		}
	}
}