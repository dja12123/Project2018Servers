package node.network.communicator;

import java.net.InetAddress;

import node.network.packet.Packet;

public class NetworkEvent
{
	public final Packet packet;
	public final DeviceInfo sender;
	
	public NetworkEvent(Packet packet, DeviceInfo sender)
	{
		this.packet = packet;
		this.sender = sender;
	}
}
