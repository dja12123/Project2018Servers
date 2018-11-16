package node.network.spiltpacket;

public class SplitPacket
{
	private byte[][] source;
	
	public int segCount;
	public int code;
	
	public static void main(String[] args)
	{
		byte[] testData = "Hello Worldaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbcccccccccccccccccddddddddddddd".getBytes();
		System.out.println(testData.length);
		//SplitPacket packet = new SplitPacket(testData);
	}
	
	public SplitPacket(byte[][] source)
	{
		this.segCount = source.length;
	}
	
	public byte[] getSegment(int segno)
	{
		return this.source[segno];
	}
	
}