package node.network;

import java.net.InetAddress;

import node.network.protocol.keyvaluePacket.Packet;

public class NetworkEvent
{
	public final String key;
	public final InetAddress inetAddr;
	public final Packet packet;
	
	NetworkEvent(String key, InetAddress inetAddr, Packet packet)
	{
		this.key = key;
		this.inetAddr = inetAddr;
		this.packet = packet;
	}
}
