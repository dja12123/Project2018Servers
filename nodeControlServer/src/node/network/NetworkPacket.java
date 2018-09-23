package node.network;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
  * @FileName : NetworkPacket.java
  * @Project : Project2018Servers
  * @Date : 2018. 9. 23. 
  * @작성자 : dja12123
  * @변경이력 :
  * @프로그램 설명 :
  */
public class NetworkPacket
{
	private static final byte[] MAGIC_NO = new byte[] {0b01000011, 0b00110101, 0b00110000, 0b00110111};
	// 메직넘버(C507)
	
	private static final int START_HEADER = 0;
	private static final int RANGE_HEADER = 4;
	private static final int START_OPTION = 4;
	private static final int RANGE_OPTION = 1;
	// 0:null 1:isBroadCast 2:isStringValue 3:null 4:null 5:null 6:null 7:null
	private static final int START_SENDER = 5;
	private static final int RANGE_SENDER = 16;
	private static final int START_RECEIVER = 16;
	private static final int RANGE_RECEIVER = 16;
	private static final int RANGE_KEYLEN = 2;
	private static final int RANGE_VALUELEN = 4;
	
	private UUID sender;
	private boolean isBroadCast;
	private UUID receiver;
	private String key;
	private byte[] value;
	private byte[] nativePacket;
	
	public UUID getSender()
	{
		return this.sender;
	}
	
	public String getKey()
	{
		return this.key;
	}
	
	public byte[] getValue()
	{
		return this.value;
	}
	
	public boolean isBroadcast()
	{
		byte option = this.nativePacket[RANGE_HEADER];
		boolean isBroadcast = (option & 0b01000000) == 0b01000000;
		return isBroadcast;
	}
	
	public boolean isStringValue()
	{
		byte option = this.nativePacket[RANGE_HEADER];
		boolean isStringValue = (option & 0b00100000) == 0b00100000;
		return isStringValue;
	}
	
	@Override
	public String toString()
	{
		String str = "NetworkPacket: "+this.key+" "+this.value;
		return str;
	}
	
	public static NetworkPacket getPacketInst(UUID sender, UUID receiver, String key, byte[] value)
	{
		NetworkPacket networkPacket = new NetworkPacket();
		return networkPacket;
	}
	
	public static NetworkPacket getBroadcastPacketInst(UUID sender, String key, byte[] value)
	{
		NetworkPacket networkPacket = new NetworkPacket();
		return networkPacket;
	}
	
	public static NetworkPacket getPacketInst(UUID sender, UUID receiver, String key, String value)
	{
		NetworkPacket networkPacket = new NetworkPacket();
		return networkPacket;
	}
	
	public static NetworkPacket getBroadcastPacketInst(UUID sender, String key, String value)
	{
		NetworkPacket networkPacket = new NetworkPacket();
		return networkPacket;
	}
	
	public static String getStringValue(NetworkPacket packet)
	{
		//if(packet)
		{
			
		}
		return null;
	}
	
	public static NetworkPacket getPacketInst(byte[] nativePacket)
	{
		NetworkPacket networkPacket = new NetworkPacket();
		return networkPacket;
	}
	
	public static boolean isNetworkPacket(byte[] nativePacket)
	{
		return false;
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