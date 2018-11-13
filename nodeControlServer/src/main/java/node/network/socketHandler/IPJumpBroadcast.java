package node.network.socketHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
import node.network.packet.PacketUtil;

public class IPJumpBroadcast
{
	public static final Logger logger = LogWriter.createLogger(IPJumpBroadcast.class, "broadcastS");
	
	private static final String PROP_BroadcastIPstart = "broadcastIPstart";
	private static final String PROP_BroadcastIPend = "broadcastIPend";
	
	private static final String VNIC = "node_echo";
	
	private int ipStart;
	private int ipEnd;
	
	private int nowIP;
	
	private DatagramSocket socket;

	private boolean isWork;

	private BiConsumer<InetAddress, byte[]> receiveCallback;
	
	private Random random;
	
	public IPJumpBroadcast(BiConsumer<InetAddress, byte[]> receiveCallback)
	{
		this.receiveCallback = receiveCallback;
		
		this.socket = null;
		this.isWork = false;
		
		this.random = new Random();
	}
	
	public void start()
	{
		if(this.isWork) return;
		this.isWork = true;
		
		logger.log(Level.INFO, "브로드캐스트 송신기 로드");
		
		this.ipStart = Integer.parseInt(NodeControlCore.getProp(PROP_BroadcastIPstart));
		this.ipEnd = Integer.parseInt(NodeControlCore.getProp(PROP_BroadcastIPend));
		
		this.nowIP = this.ipStart + this.random.nextInt(this.ipEnd - this.ipStart + 1);
	}
	
	public synchronized void sendMessage(boolean jump, byte[] data)
	{
		if(!this.isWork)
		{
			logger.log(Level.WARNING, "소켓 닫힘");
			return;
		}
		
		String nowAddr = String.format("%s.%d", NetworkUtil.DEFAULT_SUBNET, this.nowIP);
		
		if(jump)
		{
			int beforeIP = this.nowIP;
			this.nowIP = this.ipStart + this.random.nextInt(this.ipEnd - this.ipStart);
			if(this.nowIP >= beforeIP) ++this.nowIP;
			//IP이전꺼랑 안겹치게 랜덤 점프 하는 로직
			
			String ipSetCommand = String.format("ifconfig %s:%s %s/24", NetworkUtil.getNIC(), VNIC, nowAddr);
			try
			{
				CommandExecutor.executeCommand(ipSetCommand, false);
			}
			catch (Exception e)
			{
				logger.log(Level.SEVERE, "가상NIC설정 실패", e);
				return;
			}
			try
			{
				if(this.socket != null && !this.socket.isClosed())
				{
					this.socket.close();
				}
				this.socket = new DatagramSocket(null);
				this.socket.bind(new InetSocketAddress(nowAddr, NetworkUtil.broadcastPort()));
				this.socket.setBroadcast(true);
			}
			catch (IllegalStateException | IOException e)
			{
				logger.log(Level.SEVERE, "소켓 열기 실패", e);
				return;
			}
		}
		
		DatagramPacket packet = new DatagramPacket(data, data.length);
		packet.setAddress(NetworkUtil.broadcastIA(NetworkUtil.DEFAULT_SUBNET));
		packet.setPort(NetworkUtil.broadcastPort());
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
		/*logger.log(Level.INFO, "네트워크 수신 시작");
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
		logger.log(Level.WARNING, "브로드캐스트 소켓 전송기 중지");*/
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