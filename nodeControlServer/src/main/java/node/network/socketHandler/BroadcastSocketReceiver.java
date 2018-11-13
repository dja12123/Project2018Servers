package node.network.socketHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
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
import node.device.DeviceInfoManager;
import node.log.LogWriter;
import node.network.NetworkManager;
import node.network.NetworkUtil;
import node.network.packet.PacketUtil;

public class BroadcastSocketReceiver implements Runnable
{
	public static final Logger logger = LogWriter.createLogger(BroadcastSocketReceiver.class, "broadcastR");
	
	private Thread worker;
	private boolean isWork;
	private DatagramSocket dgramSocket;
	
	private BiConsumer<InetAddress, byte[]> receiveCallback;
	
	public BroadcastSocketReceiver(BiConsumer<InetAddress, byte[]> receiveCallback)
	{
		this.receiveCallback = receiveCallback;
		this.dgramSocket = null;
	}

	public void start(InetAddress addr)
	{
		if(this.isWork) return;
		this.isWork = true;
		
		logger.log(Level.INFO, String.format("브로드캐스트 수신기 로드(%s)", addr.getHostAddress()));
		
		try
		{
			this.dgramSocket = new DatagramSocket(null);
			
			this.dgramSocket.bind(new InetSocketAddress(addr, NetworkUtil.broadcastPort()));
			this.dgramSocket.setBroadcast(true);
		}
		catch (SocketException e)
		{
			logger.log(Level.SEVERE, "소캣 열기 실패", e);
			return;
		}
		
		
		this.worker = new Thread(this);

		this.worker.start();
		return;
	}

	public void stop()
	{
		if(!this.isWork) return;
		this.isWork = false;
		
		this.dgramSocket.close();
		this.worker.interrupt();
	}

	@Override
	public void run()
	{
		byte[] packetBuffer = new byte[PacketUtil.HEADER_SIZE + PacketUtil.MAX_SIZE_KEY + PacketUtil.MAX_SIZE_DATA];
		DatagramPacket dgramPacket;
		
		while(this.isWork)
		{
			dgramPacket = new DatagramPacket(packetBuffer, packetBuffer.length);
			try
			{
				System.out.println("receive start");
				this.dgramSocket.receive(dgramPacket);
				System.out.println("UDPRECEIVE " + dgramPacket.getLength());
			}
			catch (IOException e)
			{
				continue;
			}
		}
		logger.log(Level.INFO, "브로드캐스트 수신기 종료");
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