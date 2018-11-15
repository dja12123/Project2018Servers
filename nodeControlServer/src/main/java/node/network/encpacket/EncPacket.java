package node.network.encpacket;

public class EncPacket 
{
	public byte[][] payLoad;
	public int partCount;
	
	public EncPacket(byte[][] payLoad, int partCount)
	{
		this.payLoad = payLoad;
		this.partCount = partCount;
	}
}