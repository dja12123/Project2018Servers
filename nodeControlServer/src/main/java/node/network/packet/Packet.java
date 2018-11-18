package node.network.packet;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
  * @FileName : NetworkPacket.java
  * @Project : Project2018Servers
  * @Date : 2018. 9. 23. 
  * @작성자 : dja12123
  * @변경이력 :
  * @프로그램 설명 : 네트워크 패킷
  */
public class Packet
{
	private ByteBuffer source;
	private int keySize;
	private int dataSize;
	
	private boolean isBroadcast;
	private boolean hasData;
	private boolean isStringData;
	
	public Packet(byte[] nativeArr)
	{
		this.source = ByteBuffer.wrap(nativeArr);
		this.keySize = this.source.getInt(PacketUtil.START_KEYLEN);
		this.dataSize = this.source.getInt(PacketUtil.START_DATALEN);
		
		short option = this.source.getShort(PacketUtil.START_OPTION);
		this.isBroadcast = PacketUtil.checkOption(option, PacketUtil.OPT_ISBROADCAST);
		this.hasData = PacketUtil.checkOption(option, PacketUtil.OPT_HASDATA);
		this.isStringData = PacketUtil.checkOption(option, PacketUtil.OPT_ISSTRINGDATA);
	}
	
	public byte[] getRawData()
	{
		return this.source.array();
	}
	
	public boolean isBroadcast()
	{
		return this.isBroadcast;
	}
	
	public boolean hasData()
	{
		return this.hasData;
	}
	
	public boolean isStringData()
	{
		return this.isStringData;
	}
	
	public int getKeySize()
	{
		return this.keySize;
	}
	
	public long getDataSize()
	{
		return this.dataSize;
	}
	
	public String getKey()
	{
		byte[] keyByte = new byte[this.keySize];
		this.source.position(PacketUtil.HEADER_SIZE);
		this.source.get(keyByte);
		return new String(keyByte);
	}
	
	public byte[] getDataByte()
	{
		if(!this.hasData) return null;
		
		byte[] data = new byte[this.dataSize];
		this.source.position(PacketUtil.HEADER_SIZE + this.keySize);
		this.source.get(data);
		return data;
	}
	
	public String getDataString()
	{
		if(!this.hasData || !this.isStringData)
			return null;
		return new String(this.getDataByte());
	}
	
	public UUID getSender()
	{
		byte[] sender = new byte[PacketUtil.ADDR_SIZE];
		this.source.position(PacketUtil.START_SENDER);
		this.source.get(sender);
		return PacketUtil.byteTouuid(sender);
	}
	
	public UUID getReceiver()
	{
		if(this.isBroadcast)
			return null;
		
		byte[] receiver = new byte[PacketUtil.ADDR_SIZE];
		this.source.position(PacketUtil.START_RECEIVER);
		this.source.get(receiver);
		return PacketUtil.byteTouuid(receiver);
	}
	
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		
		buf.append("sender:");
		buf.append(this.getSender().toString());
		buf.append('\n');
		buf.append("receiver");
		if(!this.isBroadcast) buf.append(":"+this.getReceiver().toString());
		else buf.append("[broadcast]");
		buf.append('\n');
		buf.append("option:");
		buf.append(String.format("%16s", Integer.toBinaryString(this.source.getShort(PacketUtil.START_OPTION))).replace(' ', '0'));
		buf.append('\n');
		buf.append("key("+this.keySize+"):");
		buf.append(this.getKey());
		buf.append('\n');
		buf.append("value");
		if(this.hasData)
			if(this.isStringData) buf.append("("+this.dataSize+"):"+this.getDataString());
			else buf.append("[binary data]");
		else buf.append("[no data]");
		
		return buf.toString();
	}
}