package node.network.spiltpacket;

public class SplitPacketUtil
{
	public static final byte[] MAGIC_NO_START = new byte[] {0x31, 0x11, 0x31, 0x11};
	public static final byte[] MAGIC_NO_END = new byte[] {0x01, 0x12, 0x01, 0x12};
	
	public static final int START_MAGICNO_START = 0;
	public static final int RANGE_MAGICNO_START = 4;
	public static final int START_PACKET_ID = 4;
	public static final int RANGE_PACKET_ID = 4;
	public static final int START_PACKET_FULLSEG = 8;
	public static final int RANGE_PACKET_FULLSEG = 4;
	public static final int START_PACKET_NUM = 12;
	public static final int RANGE_PACKET_NUM = 4;
	public static final int START_PAYLOAD = 16;
	public static final int RANGE_PAYLOAD = 12;
	public static final int START_MAGIC_NO_END = 28;
	public static final int RANGE_MAGIC_NO_END = 4;
	
	public static final int SPLIT_SIZE = 32;
	public static final int FULL_PACKET_LIMIT = 1024 * 1024 * 100;// 100mbyte제한
}
