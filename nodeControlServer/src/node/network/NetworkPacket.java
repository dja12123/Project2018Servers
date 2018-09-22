package node.network;

import java.util.UUID;

public class NetworkPacket
{
	private static final byte[] PACKET_HEADER = new byte[] {0b01010101, 0b00101010, 0b01010101, 0b00101010};
	private static final int RANGE_SENDER = 8;
	private static final int RANGE_HEADER = 4;
	private static final int RANGE_KEYLEN = 2;
	private static final int RANGE_VALUELEN = 2;
	
	private UUID sender;
	private boolean isBroadCast;
	private UUID receiver;
	private String key;
	private String value;
	private byte[] nativePacket;
	
	private NetworkPacket() {}
	
	public UUID getSender()
	{
		return this.sender;
	}
	
	public String getKey()
	{
		return this.key;
	}
	
	public String getValue()
	{
		return this.value;
	}
	
	@Override
	public String toString()
	{
		String str = "NetworkPacket: "+this.key+" "+this.value;
		return str;
	}
	
	public static NetworkPacket getPacketInst(UUID sender, UUID receiver, String key, String value)
	{
		NetworkPacket networkPacket = new NetworkPacket();
		return networkPacket;
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
}