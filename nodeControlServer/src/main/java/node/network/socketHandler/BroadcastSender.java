package node.network.socketHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.NodeControlCore;
import node.bash.CommandExecutor;
import node.log.LogWriter;
import node.network.NetworkManager;
import node.network.NetworkUtil;
import node.network.protocol.keyvaluePacket.PacketUtil;

public class BroadcastSender
{
	public static final Logger logger = LogWriter.createLogger(BroadcastSender.class, "broadcastS");
	
	private DatagramSocket socket;
	private InetAddress broadcastAddr;
	private int port;

	private boolean isWork;

	public BroadcastSender()
	{
		this.socket = null;
		this.isWork = false;
	}
	
	public void start(InetAddress broadcastAddr, int port)
	{
		if(this.isWork) return;
		
		this.broadcastAddr = broadcastAddr;
		this.port = port;
		
		logger.log(Level.INFO, "브로드캐스트 송신기 로드");
		//this.nowIP = this.ipStart + this.random.nextInt(this.ipEnd - this.ipStart + 1);
		//this.ipJump();
		
		try
		{
			this.socket = new DatagramSocket();
			//this.socket.bind(new InetSocketAddress(this.sendAddr, this.port));
			this.socket.setBroadcast(true);
		}
		catch (SocketException e)
		{
			logger.log(Level.SEVERE, String.format("소켓 열기 실패(%s:%d)", this.broadcastAddr.getHostAddress(), this.port), e);
			return;
		}
		this.isWork = true;
	}
	
	public synchronized void sendMessage(byte[] data)
	{
		if(!this.isWork)
		{
			logger.log(Level.WARNING, "소켓 닫힘");
			return;
		}
		
		DatagramPacket packet = new DatagramPacket(data, data.length);
		packet.setAddress(this.broadcastAddr);
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

	public void stop()
	{
		if(!this.isWork) return;
		this.isWork = false;
		
		logger.log(Level.INFO, "브로드캐스트 송신기 종료");
		
		if(this.socket != null && !this.socket.isClosed())
		{
			this.socket.close();
		}
	}
}