package node.network.socketHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.savarese.vserv.tcpip.ICMPEchoPacket;
import org.savarese.vserv.tcpip.ICMPPacket;
import org.savarese.vserv.tcpip.IPPacket;
import org.savarese.vserv.tcpip.UDPPacket;

import com.savarese.rocksaw.net.RawSocket;

import node.NodeControlCore;
import node.bash.CommandExecutor;
import node.device.DeviceInfoManager;
import node.log.LogWriter;
import node.network.NetworkConfig;
import node.network.NetworkManager;
import node.network.NetworkUtil;
import node.network.packet.PacketUtil;

public class RawSocketReceiver implements Runnable
{
	public static final Logger logger = LogWriter.createLogger(RawSocketReceiver.class, "rawsocket");
	
	private Thread worker;
	private boolean isWork;
	private DatagramSocket dgramSocket;
	
	private RawSocket rawSocket;

	private String nic;
	private int port;
	
	private BiConsumer<InetAddress, byte[]> receiveCallback;
	
	public RawSocketReceiver(BiConsumer<InetAddress, byte[]> receiveCallback)
	{
		this.receiveCallback = receiveCallback;
		this.rawSocket = null;
		this.dgramSocket = null;
	}

	public void start(String nic, int port)
	{
		if(this.isWork) return;
		
		this.nic = nic;
		this.port = port;
		
		this.rawSocket = new RawSocket();
		this.worker = new Thread(this);
		
		try
		{
			CommandExecutor.executeCommand(String.format("ip link set %s promisc on", nic, false));
		}
		catch (Exception e)
		{
			logger.log(Level.SEVERE, "무작위 모드 변경 실패", e);
			return;
		}
		
		try
		{
			this.rawSocket.pmodeOpen(nic);
		}
		catch (IllegalStateException | IOException e)
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
		this.isWork = false;
		
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
		logger.log(Level.INFO, "로우 소켓 수신 시작");
		byte[] packetBuffer = new byte[NetworkConfig.DFT_WINDOW_SIZE];
		int readLen = 0;
		
		while(this.isWork)
		{
			try
			{
				readLen = this.rawSocket.read(packetBuffer);

				if(readLen <= 42)
				{// header
					continue;
				}
				ByteBuffer buf = ByteBuffer.wrap(packetBuffer);
				buf.position(23);
				if(buf.get() != 0x11)
				{//isudp?
					continue;
				}
				buf.position(35);
				int recvPort = buf.getShort();
				System.out.println("pass2" + recvPort);
				if(recvPort != this.port)
				{//dest port is 20080?
					continue;
				}
				System.out.println("pass3" + NetworkUtil.bytesToHex(packetBuffer, packetBuffer.length));
				readLen = buf.getShort() - 8;
				byte[] copyBuf = Arrays.copyOfRange(packetBuffer, 42, 42 + readLen);
				System.out.println("완료");
				this.receiveCallback.accept(null, copyBuf);
				System.out.println(NetworkUtil.bytesToHex(packetBuffer, 10));
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
		logger.log(Level.INFO, "로우 소켓 수신 종료");
	}
}