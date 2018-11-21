package node;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.savarese.vserv.tcpip.ICMPPacket;
import org.savarese.vserv.tcpip.IPPacket;

import com.savarese.rocksaw.net.RawSocket;

import node.bash.CommandExecutor;
import node.fileIO.FileHandler;
import node.log.LogWriter;
import node.network.NetworkUtil;
import node.network.packet.PacketUtil;
import node.network.socketHandler.RawSocketReceiver;

public class TestMain implements Runnable
{
	public static final Logger logger = LogWriter.createLogger(RawSocketReceiver.class, "rawsocket");
	
	private Thread worker;
	private boolean isWork;
	private DatagramSocket dgramSocket;
	
	private RawSocket rawSocket;

	private int port;
	private String nic;

	
	public static void setInetAddr(InetAddress inetAddress, String iface)
	{
		ArrayList<String> command = new ArrayList<String>();
		
		byte[] myAddrByte = inetAddress.getAddress();
		myAddrByte[3] = 1;
		String gatewayAddr = null;
		try
		{
			gatewayAddr = InetAddress.getByAddress(myAddrByte).getHostAddress();
		}
		catch (UnknownHostException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		command.add(String.format("ifdown -a"));

		command.add(String.format("ip addr flush dev %s", iface));
		command.add(String.format("ip addr add %s/24 brd + dev %s", inetAddress.getHostAddress(), iface));
		
		command.add(String.format("ip route add default via %s", gatewayAddr));
		command.add(String.format("ifup -a"));
		
		
			logger.log(Level.INFO, "IP변경 시작");

			try
			{
				CommandExecutor.executeBash(command);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			logger.log(Level.INFO, String.format("IP변경 완료(%s)", inetAddress.getHostAddress()));

		
		
		
	}
	public static void main(String[] args) throws UnknownHostException 
	{
		setInetAddr(InetAddress.getByName("192.168.0.242"), "eth0");
		/*try
		{
			CommandExecutor.executeCommand(String.format("ifconfig eth0:0 192.168.0.240/24"));
			CommandExecutor.executeCommand(String.format("ip link set %s promisc on", "eth0:0"));
		}
		catch (Exception e)
		{
			logger.log(Level.SEVERE, "무작위 모드 변경 실패", e);
			return;
		}*/
		
		
		File rawSocketLib = FileHandler.getExtResourceFile("rawsocket");
		StringBuffer libPathBuffer = new StringBuffer();
		libPathBuffer.append(rawSocketLib.toString());
		libPathBuffer.append(":");
		libPathBuffer.append(System.getProperty("java.library.path"));
		
		System.setProperty("java.library.path", libPathBuffer.toString());
		Field sysPathsField = null;
		try
		{
			sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
			sysPathsField.setAccessible(true);
			sysPathsField.set(null, null);
		}
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1)
		{
			// TODO Auto-generated catch blsock
			logger.log(Level.SEVERE, "JNI 라이브러리 폴더 링크 실패", e1);
			return;
		}
		System.loadLibrary("rocksaw");
		logger.log(Level.INFO, "JNI 라이브러리 로드");
		
		TestMain t= new TestMain();
		t.start("eth0");
		
		Thread s = new Thread(()->{
			
			
			try
			{
				DatagramSocket socket = new DatagramSocket();
				//socket.bind(new InetSocketAddress("192.168.0.242", 20080));
				socket.setBroadcast(true);
				byte[] buffer = "HELLO".getBytes();
				
				while(true)
				{
					System.out.println("전송..");
					DatagramPacket dgramSocket = new DatagramPacket(buffer, buffer.length);
					dgramSocket.setAddress(InetAddress.getByName("192.168.0.255"));
					dgramSocket.setPort(20080);
					socket.send(dgramSocket);
					Thread.sleep(1000);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
	
			
		});
		s.start();
	}
	
	public TestMain()
	{

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
			CommandExecutor.executeCommand(String.format("ip link set %s promisc on", nic));
		}
		catch (Exception e)
		{
			logger.log(Level.SEVERE, "무작위 모드 변경 실패", e);
			return;
		}
		
		try
		{
			this.rawSocket.pmodeOpen(nic);
			//this.rawSocket.setIPHeaderInclude(true);
			//logger.log(Level.INFO, String.format("바인드:(%s)", nic));
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
				
				readLen = this.rawSocket.read(packetBuffer);

				if(readLen < 28)
				{
					continue;
				}
				byte[] copyBuf = Arrays.copyOfRange(packetBuffer, 28, readLen);
				System.out.println("수신중...");
				System.out.println(NetworkUtil.bytesToHex(packetBuffer, 20));
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