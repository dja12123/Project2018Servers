package node.network.spiltpacket;

import java.net.InetAddress;
import java.nio.ByteBuffer;

public class SplitPacketUtil
{
	public static final byte[] MAGIC_NO_START = new byte[] { 0x31, 0x11, 0x31, 0x11 };
	public static final byte[] MAGIC_NO_END = new byte[] { 0x01, 0x12, 0x01, 0x12 };

	public static final int START_MAGICNO_START = 0;
	public static final int RANGE_MAGICNO_START = 4;
	public static final int START_PACKET_ID = 4;
	public static final int RANGE_PACKET_ID = 8;
	public static final int START_PACKET_FULLSEG = 12;
	public static final int RANGE_PACKET_FULLSEG = 4;
	public static final int START_PACKET_NUM = 16;
	public static final int RANGE_PACKET_NUM = 4;
	public static final int START_MAGIC_NO_END = 28;
	public static final int RANGE_MAGIC_NO_END = 4;

	public static final int SPLIT_SIZE = 32;
	public static final int PACKET_METADATA_SIZE = RANGE_MAGICNO_START + RANGE_PACKET_ID + RANGE_PACKET_FULLSEG
			+ RANGE_PACKET_NUM + RANGE_MAGIC_NO_END;
	public static final int START_PAYLOAD = RANGE_MAGICNO_START + RANGE_PACKET_ID + RANGE_PACKET_FULLSEG
			+ RANGE_PACKET_NUM;
	public static final int RANGE_PAYLOAD = SPLIT_SIZE - PACKET_METADATA_SIZE;
	public static final int FULL_PACKET_LIMIT = 1024 * 1024 * 100;// 100mbyte제한

	public static long headerToLong(byte[] rawPacket)
	{
		long result = 0;
		for (int i = START_PACKET_ID; i < RANGE_PACKET_ID; i++)
		{
			result <<= 8;
			result |= (rawPacket[i] & 0xFF);
		}
		return result;
	}

	public static long bytesToLong(byte[] b)
	{
		long result = 0;
		for (int i = 0; i < 8; i++)
		{
			result <<= 8;
			result |= (b[i] & 0xFF);
		}
		return result;
	}

	public static byte[] longToBytes(long l)
	{
		byte[] result = new byte[8];
		for (int i = 7; i >= 0; i--)
		{
			result[i] = (byte) (l & 0xFF);
			l >>= 8;
		}
		return result;
	}
	
	public static byte[] createSplitPacketID(InetAddress addr)
	{
		byte[] id = new byte[SplitPacketUtil.RANGE_PACKET_ID];
		ByteBuffer buf = ByteBuffer.wrap(id);
		buf.put(addr.getAddress(), 0, 4);
		buf.putInt((int)System.currentTimeMillis());
		return id;
	}
}
