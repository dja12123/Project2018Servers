package node.network.packet;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
  * @FileName : PacketBuilder.java
  * @Project : Project2018Servers
  * @Date : 2018. 9. 25. 
  * @작성자 : dja12123
  * @변경이력 :
  * @프로그램 설명 : 패킷 빌더
  */
public class PacketBuilder
{
	private boolean setSender;
	private boolean setReceiver;
	private boolean setKey;
	private boolean setData;
	private boolean isBuilded;
	
	private byte[] sender;
	private byte[] receiver;
	private byte[] key;
	private byte[] data;
	private short option;
	
	public static void main(String[] args)
	{
		PacketBuilder builder = new PacketBuilder();
		UUID sender = UUID.randomUUID();
		UUID receiver = UUID.randomUUID();
		Packet packet = null;
		
		try
		{
			builder.setSender(sender)
			.setReceiver(receiver)
			.setKey("Hello")
			.setData("world!!");
			
			packet = builder.createPacket();
		}
		catch (PacketBuildFailureException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(packet.toString());
		
	}
	
	public PacketBuilder()
	{
		this.sender = null;
		this.receiver = null;
		this.key = null;
		this.data = null;
		this.option = 0b0000000000000000;
		
		this.setSender = false;
		this.setReceiver = false;
		this.setKey = false;
		this.setData = false;
		this.isBuilded = false;
	}
	
	public PacketBuilder setSender(UUID sender) throws PacketBuildFailureException
	{
		if(this.setSender) throw new PacketBuildFailureException("The sender has already been assigned");
		
		this.sender = PacketUtil.uuidToByte(sender);
		this.setSender = true;
		return this;
	}
	
	public PacketBuilder setBroadCast() throws PacketBuildFailureException
	{
		if(this.setReceiver) throw new PacketBuildFailureException("The receiver has already been assigned");
		
		this.option = PacketUtil.writeOption(this.option, PacketUtil.OPT_ISBROADCAST);
		this.setReceiver = true;
		return this;
	}
	
	public PacketBuilder setReceiver(UUID receiver) throws PacketBuildFailureException
	{
		if(this.setReceiver) throw new PacketBuildFailureException("The receiver has already been assigned");
		
		this.receiver = PacketUtil.uuidToByte(receiver);
		this.setReceiver = true;
		return this;
	}
	
	public PacketBuilder setKey(String key) throws PacketBuildFailureException
	{
		if(this.setKey) throw new PacketBuildFailureException("The key has already been assigned");
		byte[] keyByte = key.getBytes();
		
		if(keyByte.length >= PacketUtil.MAX_SIZE_KEY)
			throw new PacketBuildFailureException("The key has too long");
		
		this.key = key.getBytes();
		this.setKey = true;
		return this;
	}
	
	public PacketBuilder setData(byte[] data) throws PacketBuildFailureException
	{
		if(this.setData) throw new PacketBuildFailureException("The data has already been assigned");
		
		if(data.length >= PacketUtil.MAX_SIZE_DATA)
			throw new PacketBuildFailureException("The data has too long");
		
		this.option = PacketUtil.writeOption(this.option, PacketUtil.OPT_HASDATA);
		this.data = data;
		this.setData = true;
		return this;
	}
	
	public PacketBuilder setData(String strData) throws PacketBuildFailureException
	{
		byte[] data = strData.getBytes();
		
		if(data.length >= PacketUtil.MAX_SIZE_DATA)
			throw new PacketBuildFailureException("The data has too long");
		
		this.setData(data);
		this.option = PacketUtil.writeOption(this.option, PacketUtil.OPT_ISSTRINGDATA);
		return this;
	}
	
	public Packet createPacket() throws PacketBuildFailureException
	{
		if(this.isBuilded) new PacketBuildFailureException("already builded");
		if(!(this.setSender && this.setReceiver && this.setKey))
			throw new PacketBuildFailureException("There is not enough data");
		
		int keyLen = key.length;
		int dataLen = 0;
		
		if(this.data != null)
		{
			dataLen = data.length;
		}
		
		ByteBuffer buffer = ByteBuffer.allocate(PacketUtil.HEADER_SIZE + keyLen + dataLen + PacketUtil.RANGE_MAGICNOEND);
		
		buffer.put(PacketUtil.MAGIC_NO_START);
		buffer.putShort(this.option);
		buffer.putInt(keyLen);
		buffer.putInt(dataLen);
		buffer.put(this.sender);
		if(this.receiver != null)
			buffer.put(this.receiver);
		else
			buffer.put(PacketUtil.BROADCAST_RECEIVER);
		buffer.put(this.key);
		buffer.put(this.data);
		buffer.put(PacketUtil.MAGIC_NO_END);
		
		Packet createdPacket = new Packet(buffer.array());
		
		this.isBuilded = true;
		
		return createdPacket;
	}
}