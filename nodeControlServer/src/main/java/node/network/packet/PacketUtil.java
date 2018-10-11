package node.network.packet;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

/**
  * @FileName : PacketUtil.java
  * @Project : Project2018Servers
  * @Date : 2018. 9. 25. 
  * @작성자 : dja12123
  * @변경이력 :
  * @프로그램 설명 :
  */
public class PacketUtil
{
	public static final byte[] MAGIC_NO = new byte[] {0x43, 0x35, 0x30, 0x37, 0x6D, 0x68};
	// 메직넘버(C507mh)
	public static final byte[] BROADCAST_RECEIVER = new byte[]
			{-0x7F, -0x7F, -0x7F, -0x7F, -0x7F, -0x7F, -0x7F, -0x7F, -0x7F, -0x7F, -0x7F, -0x7F, -0x7F, -0x7F, -0x7F, -0x7F};
	
	private static InetAddress BROADCAST_IA;
	
	public static final int HEADER_SIZE = 48;
	public static final int ADDR_SIZE = 16;
	
	public static final int START_MAGICNO = 0;
	public static final int RANGE_MAGICNO = 6;
	public static final int START_OPTION = 6;
	public static final int RANGE_OPTION = 2;
	// 0:null 1:isBroadCast 2:hasData 3:isStringData 4:null 5:null 6:null 7:null
	// 8:null 9:null A:null B:null C:null D:null E:null F:null
	public static final int START_KEYLEN = 8;
	public static final int RANGE_KEYLEN = 4;
	public static final int START_DATALEN = 12;
	public static final int RANGE_DATALEN = 4;
	public static final int START_SENDER = 16;
	public static final int START_RECEIVER = 32;

	public static final int MAX_SIZE_KEY = 8192;
	public static final int MAX_SIZE_DATA = 1048576;
	
	public static final int OPT_ISBROADCAST = 1;
	public static final int OPT_HASDATA = 2;
	public static final int OPT_ISSTRINGDATA = 3;
	
	public static final String DPROTO_SEP_ROW = ",";
	public static final String DPROTO_SEP_COL = "/\n";
	
	static
	{
		try
		{
			BROADCAST_IA = InetAddress.getByName("255.255.255.255");
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
	}
	
	public static InetAddress broadcastIA()
	{
		return BROADCAST_IA;
	}
	
	public static String printPacket(byte[] rawPacket)
	{
		StringBuffer strBuf = new StringBuffer();
		ByteBuffer packet = ByteBuffer.wrap(rawPacket);
		byte[] addrBuf = new byte[ADDR_SIZE];
		UUID sender, receiver;
		
		packet.get(addrBuf, START_SENDER, ADDR_SIZE);
	//(addrBuf);
		
		
		return strBuf.toString();
	}
	
	public static boolean checkOption(short optionArea, int option)
	{
		int checkPointer = 0b0100000000000000 >> (option - 1);
		if((optionArea & checkPointer) != 0)
		{
			return true;
		}
		return false;
	}
	
	public static short writeOption(short optionArea, int option)
	{
		int mask = 0b0100000000000000 >> (option - 1);
		optionArea = (short)(optionArea | mask);
		return optionArea;
	}
	
	public static UUID byteTouuid(byte[] bytes)
	{//https://stackoverflow.com/questions/17893609/convert-uuid-to-byte-that-works-when-using-uuid-nameuuidfrombytesb
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		long firstLong = bb.getLong();
		long secondLong = bb.getLong();
		return new UUID(firstLong, secondLong);
	}

	public static byte[] uuidToByte(UUID uuid)
	{
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return bb.array();
	}
	
	public static boolean isPacket(byte[] arr)
	{
		if(arr.length < PacketUtil.HEADER_SIZE)
			return false;
		
		ByteBuffer buf;
		buf = ByteBuffer.wrap(arr, PacketUtil.START_MAGICNO, PacketUtil.RANGE_MAGICNO);
		
		if(!buf.equals(ByteBuffer.wrap(PacketUtil.MAGIC_NO)))
			return false;
		
		buf = ByteBuffer.wrap(arr);
		
		if(buf.getInt(PacketUtil.START_KEYLEN) + buf.getInt(PacketUtil.START_DATALEN) + PacketUtil.HEADER_SIZE != arr.length)
			return false;
		
		return true;
	}
	
	public static byte[] clonePacketByte(byte[] packetBuffer)
	{
		byte[] copyBuffer;
		int keySize, dataSize;
		
		ByteBuffer byteBuffer = ByteBuffer.wrap(packetBuffer);
		byteBuffer.position(START_KEYLEN);
		keySize = byteBuffer.getInt();
		byteBuffer.position(START_DATALEN);
		dataSize = byteBuffer.getInt();
		
		copyBuffer = Arrays.copyOf(packetBuffer, HEADER_SIZE + keySize + dataSize);
		
		return copyBuffer;
	}
}
