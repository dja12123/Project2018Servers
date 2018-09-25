package node.network;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
  * @FileName : NetworkPacket.java
  * @Project : Project2018Servers
  * @Date : 2018. 9. 23. 
  * @작성자 : dja12123
  * @변경이력 :
  * @프로그램 설명 : 네트워크 패킷 처리기
  */
public class Packet
{
	private static final byte[] MAGIC_NO = new byte[] {0b01000011, 0b00110101, 0b00110000, 0b00110111};
	// 메직넘버(C507)
	
	private static final int HEADER_SIZE = 48;
	private static final int ADDR_SIZE = 16;
	
	private static final int START_MAGICNO = 0;
	private static final int RANGE_MAGICNO = 4;
	private static final int START_OPTION = 4;
	private static final int RANGE_OPTION = 1;
	// 0:null 1:isBroadCast 2:isStringdata 3:null 4:null 5:null 6:null 7:null
	private static final int START_KEYLEN = 5;
	private static final int RANGE_KEYLEN = 3;
	private static final int START_DATALEN = 8;
	private static final int RANGE_DATALEN = 8;
	private static final int START_SENDER = 16;
	private static final int RANGE_SENDER = ADDR_SIZE;
	private static final int START_RECEIVER = 32;
	private static final int RANGE_RECEIVER = ADDR_SIZE;
	
	private static final int OPT_ISBROADCAST = 1;
	private static final int OPT_ISSTRINGDATA = 2;

	private byte option; 
	private UUID sender;
	private UUID receiver;
	private String key;
	private byte[] data;
	
	private boolean setSender;
	private boolean setReceiver;
	private boolean setKey;
	private boolean setData;
	private boolean isBuilded;
	
	public Packet()
	{
		
	}
	
	public void setSender(UUID sender)
	{
		if(setSender)
		{
			return;
		}
		
		
	}
	
	public void setBroadCast()
	{
		
	}
	
	public void setReceiver()
	{
		
	}
	
	public void setKey(String key)
	{
		
	}
	
	public void setData(byte[] data)
	{
		
	}
	
	public void setData(String data)
	{
		
	}
	
	public byte[] getByteArr()
	{
		byte[] packet = new byte[128];
		return packet;
	}
	
	
	public static void main(String[] args)
	{
		UUID sender = UUID.randomUUID();
		UUID receiver = UUID.randomUUID();
		
		byte[] key = "Hello".getBytes();
		byte[] data = "world!!".getBytes();
		
	}
	
	public static String printPacket(byte[] rawPacket)
	{
		StringBuffer strBuf = new StringBuffer();
		ByteBuffer packet = ByteBuffer.wrap(rawPacket);
		byte[] addrBuf = new byte[ADDR_SIZE];
		UUID sender, receiver;
		
		packet.get(addrBuf, START_SENDER, ADDR_SIZE);
		asUuid(addrBuf);
		
		
		return strBuf.toString();
	}
	
	public static boolean checkOption(byte[] rawPacket, int option)
	{
		option -= 1;
		int checkPointer = 0b01 >> option;
		if((rawPacket[START_OPTION] & checkPointer) == 1)
		{
			return true;
		}
		return false;
	}
	
	public static byte writeOption(byte optionByte, int option)
	{
		int mask = (0b01 >> (option - 1));
		optionByte = (byte) (optionByte | mask);
		return optionByte;
	}
	
	public static byte[] createPacket(UUID sender, UUID receiver, byte option, String key, byte[] data)
	{
		byte[] keyByte = key.getBytes();
		ByteBuffer packet = ByteBuffer.allocate(HEADER_SIZE + keyByte.length + data.length);
		
		packet.put(MAGIC_NO, START_MAGICNO, RANGE_MAGICNO);
		
		return packet.array();
	}
	
	private static UUID asUuid(byte[] bytes)
	{//https://stackoverflow.com/questions/17893609/convert-uuid-to-byte-that-works-when-using-uuid-nameuuidfrombytesb
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		long firstLong = bb.getLong();
		long secondLong = bb.getLong();
		return new UUID(firstLong, secondLong);
	}

	private static byte[] asBytes(UUID uuid)
	{
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return bb.array();
	}
}