package node.network.packet.encpacket;

public class EncPacket 
{
	public byte[][] payLoad;
	public boolean isPart;
	public int partCount;
	
	public EncPacket(byte[][] payLoad, boolean isPart, int partCount)
	{
		this.payLoad = payLoad;
		this.isPart = isPart;
		this.partCount = partCount;
	}
}