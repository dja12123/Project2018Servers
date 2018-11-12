package node.network.socketHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.savarese.vserv.tcpip.ICMPEchoPacket;
import org.savarese.vserv.tcpip.ICMPPacket;
import org.savarese.vserv.tcpip.IPPacket;
import org.savarese.vserv.tcpip.UDPPacket;

import com.savarese.rocksaw.net.RawSocket;

import node.NodeControlCore;
import node.device.DeviceInfoManager;
import node.log.LogWriter;
import node.network.NetworkManager;
import node.network.NetworkUtil;
import node.network.packet.PacketUtil;

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
	private String nic;
	
	public RawSocketReceiver(NetworkManager networkManager, DeviceInfoManager deviceInfoManager)
	{
		this.networkManager = networkManager;
		this.deviceInfoManager = deviceInfoManager;
		this.rawSocket = null;
		
	}

	public void start()
	{
		if(this.isWork) return;
		this.isWork = true;
		
		logger.log(Level.INFO, "로우 소켓 핸들러 로드");
		this.rawSocket = new RawSocket();
		this.worker = new Thread(this);
		
		try
		{
			this.port = Integer.parseInt(NodeControlCore.getProp(NetworkManager.PROP_INFOBROADCAST_PORT));
			this.nic = NodeControlCore.getProp(NetworkManager.PROP_INTERFACE);
			logger.log(Level.INFO, String.format("바인딩 인터페이스 (%s)", this.nic));
			//String interfaceStr = NodeControlCore.getProp(NetworkManager.PROP_INTERFACE);
			this.rawSocket.open(RawSocket.PF_INET, RawSocket.getProtocolByName("RAW"));
			
			//this.rawSocket.bindDevice(this.nic);
			//this.rawSocket.setIPHeaderInclude(true);
			
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
	
	public void sendMessage(byte[] data)
	{
		if(!this.isWork)
		{
			logger.log(Level.WARNING, "소켓 닫힘");
			return;
		}

		try
		{

			
			this.rawSocket.write(NetworkUtil.broadcastIA(), data);
			logger.log(Level.SEVERE, "브로드캐스트...");
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "브로드캐스트 실패", e);
		}
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
				readLen = this.rawSocket.read(packetBuffer, NetworkUtil.broadcastIA().getAddress());

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
	public static void main(String[] args)
	{
		byte[] data = "Hello World!!".getBytes();
		
		/*
		UDPPacket udp = new UDPPacket(1);
		byte[] packet = new byte[20 + UDPPacket.LENGTH_UDP_HEADER + data.length];
		System.arraycopy(data, 0, packet, 20 + UDPPacket.LENGTH_UDP_HEADER, data.length);
		udp.setData(packet);
		
		udp.setIPVersion(4);
		udp.setIPHeaderLength(5);
		udp.setIPPacketLength(packet.length);
		udp.setFragmentOffset(0x0400);
		udp.setTTL(0x64);
		udp.setProtocol(IPPacket.PROTOCOL_UDP);
		System.arraycopy(NetworkUtil.broadcastIA().getAddress(), 0, packet, IPPacket.OFFSET_DESTINATION_ADDRESS, 4);
		
		udp.setSourcePort(33333);
		udp.setDestinationPort(33333);
		udp.setUDPPacketLength(UDPPacket.LENGTH_UDP_HEADER + data.length);
		
		udp.computeUDPChecksum();
		udp.computeIPChecksum();
		
		System.out.println(NetworkUtil.bytesToHex(packet, packet.length));
		System.out.println(udp.getUDPPacketLength() + " " + data.length);
		
		*/
		NodeControlICMP icmp = new NodeControlICMP(data);
		System.out.println(NetworkUtil.bytesToHex(icmp.getData(), icmp.size()));
		
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