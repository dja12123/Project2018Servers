package node.network.socketHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.NodeControlCore;
import node.log.LogWriter;
import node.network.NetworkManager;
import node.network.NetworkUtil;
import node.network.packet.PacketUtil;

public class UDPBroadcast
{
	public static final Logger logger = LogWriter.createLogger(UDPBroadcast.class, "broadcast");
	
	private DatagramSocket socket;
	private int port;

	private boolean isWork;

	private Thread worker;

	private BiConsumer<InetAddress, byte[]> receiveCallback;
	
	public UDPBroadcast(BiConsumer<InetAddress, byte[]> receiveCallback)
	{
		this.receiveCallback = receiveCallback;
		
		this.socket = null;
		this.isWork = false;
		this.worker = null;
	}
	
	public void start()
	{
		if(this.isWork) return;
		this.isWork = true;
		
		this.worker = new Thread(this::run);
		
		logger.log(Level.INFO, "브로드캐스트 소켓 전송기 로드");

		try
		{
			this.port = Integer.parseInt(NodeControlCore.getProp(NetworkManager.PROP_INFOBROADCAST_PORT));

			this.socket = new DatagramSocket(null);
			SocketAddress addr = new InetSocketAddress(NetworkUtil.listenIA(NetworkUtil.DEFAULT_SUBNET), 49800);
			logger.log(Level.INFO, String.format("바인딩(%s)", addr.toString()));
			this.socket.bind(addr);
			this.socket.setBroadcast(true);
		}
		catch (IllegalStateException | IOException e)
		{
			logger.log(Level.SEVERE, "소켓 열기 실패", e);
			return;
		}

	}
	
	public void sendMessage(byte[] stream)
	{
		if(!this.isWork)
		{
			logger.log(Level.WARNING, "소켓 닫힘");
			return;
		}
		DatagramPacket packet = new DatagramPacket(stream, stream.length);
		packet.setAddress(NetworkUtil.broadcastIA(NetworkUtil.DEFAULT_SUBNET));
		packet.setPort(this.port);
		try
		{
			this.socket.send(packet);
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "브로드캐스트 실패", e);
		}
	}
	
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
				byte[] copyBuf = Arrays.copyOf(packetBuffer, dgramPacket.getLength());
				this.receiveCallback.accept(dgramPacket.getAddress(), copyBuf);
				logger.log(Level.INFO, dgramPacket.getAddress().toString());
			}
			catch (IOException e)
			{
				
				logger.log(Level.SEVERE, "수신 실패", e);
			}
		}
		logger.log(Level.WARNING, "브로드캐스트 소켓 전송기 중지");
	}

	public void stop()
	{
		if(!this.isWork) return;
		this.isWork = false;
		this.worker.interrupt();
		
		
		
		this.socket.close();
		
	}
}