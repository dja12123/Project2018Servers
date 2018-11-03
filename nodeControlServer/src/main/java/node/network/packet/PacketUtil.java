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
	public static final byte[] MAGIC_NO_START = new byte[] {0x43, 0x35, 0x30, 0x37, 0x6D, 0x68};
	// 메직넘버(C507mh)
	public static final byte[] MAGIC_NO_END = new byte[] {0x00, 0x45, 0x4E, 0x44};
	// 메직넘버(ENDP)
	public static final byte[] BROADCAST_RECEIVER = new byte[]
			{-0x7F, -0x7F, -0x7F, -0x7F, -0x7F, -0x7F, -0x7F, -0x7F, -0x7F, -0x7F, -0x7F, -0x7F, -0x7F, -0x7F, -0x7F, -0x7F};
	
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
	public static final int RANGE_MAGICNOEND = 4;

	public static final int MAX_SIZE_KEY = 8192;
	public static final int MAX_SIZE_DATA = 1048576;
	
	public static final int OPT_ISBROADCAST = 1;
	public static final int OPT_HASDATA = 2;
	public static final int OPT_ISSTRINGDATA = 3;
	
	public static final String DPROTO_SEP_COL = ",";
	public static final String DPROTO_SEP_ROW = "/\n";
	
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
		if(arr.length < HEADER_SIZE + RANGE_MAGICNOEND)
			return false;
		ByteBuffer buf;
		buf = ByteBuffer.wrap(arr, START_MAGICNO, RANGE_MAGICNO);

		for(int i = 0; i < RANGE_MAGICNO; ++i)
		{
			byte b = buf.get();
			if(b != MAGIC_NO_START[i])
			{
				return false;
			}
		}
		buf = ByteBuffer.wrap(arr);
		buf.position(START_KEYLEN);
		int keyLen = buf.getInt();
		buf.position(START_DATALEN);
		int dataLen = buf.getInt();
		buf.position(HEADER_SIZE + keyLen + dataLen);
		for(int i = 0; i < RANGE_MAGICNOEND; ++i)
		{
			byte b = buf.get();
			if(b != MAGIC_NO_END[i])
			{
				return false;
			}
		}
		return true;
	}
	
	public static String[][] getDataArray(Packet packet)
	{
		if(!packet.isStringData()) return null;
		
		String fullData = packet.getDataString();
		String[] rowArr = fullData.split(DPROTO_SEP_ROW);
		String[][] returnArr = new String[rowArr.length][];
		for(int i = 0; i < rowArr.length; ++i)
		{
			String[] colArr = rowArr[i].split(DPROTO_SEP_COL);
			returnArr[i] = colArr;
		}
		return returnArr;
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
		
		copyBuffer = Arrays.copyOf(packetBuffer, HEADER_SIZE + keySize + dataSize + RANGE_MAGICNOEND);
		
		return copyBuffer;
	}
}
