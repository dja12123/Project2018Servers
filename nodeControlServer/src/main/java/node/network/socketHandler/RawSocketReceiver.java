package node.network.socketHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
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

	private int port;
	private String nic;
	
	private BiConsumer<InetAddress, byte[]> receiveCallback;
	
	public RawSocketReceiver(BiConsumer<InetAddress, byte[]> receiveCallback)
	{
		this.receiveCallback = receiveCallback;
		this.rawSocket = null;
		this.dgramSocket = null;
	}

	public void start(String nic)
	{
		if(this.isWork) return;
		this.isWork = true;
		
		this.rawSocket = new RawSocket();
		this.worker = new Thread(this);
		
		try
		{
			this.rawSocket.open(RawSocket.PF_INET, RawSocket.getProtocolByName("UDP"));
			//this.rawSocket.bindDevice(nic);
			//logger.log(Level.INFO, String.format("바인드:(%s)", nic));
		}
		catch (IllegalStateException | IOException e)
		{
			logger.log(Level.SEVERE, "소켓 열기 실패", e);
			return;
		}
		
		try
		{
			CommandExecutor.executeCommand(String.format("ip link set %s promisc on", nic));
		}
		catch (Exception e)
		{
			logger.log(Level.SEVERE, "무작위 모드 변경 실패", e);
			return;
		}
		
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
		byte[] packetBuffer = new byte[PacketUtil.HEADER_SIZE + PacketUtil.MAX_SIZE_KEY + PacketUtil.MAX_SIZE_DATA];
		int readLen = 0;
		
		while(this.isWork)
		{
			try
			{
				readLen = this.rawSocket.read(packetBuffer, NetworkUtil.broadcastIA(NetworkUtil.DEFAULT_SUBNET).getAddress());
				if(readLen < 28)
				{
					continue;
				}
				byte[] copyBuf = Arrays.copyOfRange(packetBuffer, 28, readLen);
			
				this.receiveCallback.accept(NetworkUtil.broadcastIA(NetworkUtil.DEFAULT_SUBNET), copyBuf);
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


class NodeControlICMP extends ICMPPacket
{
	private static int IP_HEADER_SIZE = 20;
	
	private static int TYPE_NODE_ASSIGN = 14;
	private static int CODE_BROADCAST = 0;
	
	public NodeControlICMP(byte[] pdata)
	{
		super(1);
		byte[] fullPacket = new byte[IP_HEADER_SIZE + 8 + pdata.length];
		System.arraycopy(pdata, 0, fullPacket, 20 + 8, pdata.length);
		
		this.setData(fullPacket);
		
		this.setIPVersion(4);
		this.setIPHeaderLength(5);
		this.setIPPacketLength(fullPacket.length);
		this.setFragmentOffset(0x0400);
		this.setTTL(0x64);
		this.setProtocol(IPPacket.PROTOCOL_ICMP);
		
		this.setType(TYPE_NODE_ASSIGN);
		this.setCode(CODE_BROADCAST);
		
		this.computeICMPChecksum();
		this.computeIPChecksum();
	}
	
	public byte[] getData()
	{
		return this._data_;
	}

	@Override
	public int getICMPHeaderByteLength()
	{
		return 4;
	}
	
}