package node.network.communicator;

import java.net.InetAddress;

import node.network.packet.Packet;

public class NetworkEvent
{
	public final InetAddress inetAddr;
	public Packet packet;
	
	NetworkEvent(InetAddress inetAddr, Packet packet)
	{
		this.inetAddr = inetAddr;
		this.packet = packet;
	}
}
