package node.network.spiltpacket;

import java.nio.ByteBuffer;
import java.util.Stack;

import node.network.packet.PacketUtil;

public class SplitPacketBuilder
{
	private Stack<byte[]> splitPacket;
	private int code;
	private int nowTaskSegmentNo;

	public SplitPacketBuilder(byte[] rawData, int code)
	{
		this.splitPacket = new Stack<>();
		this.code = code;
		
	}
	
	public SplitPacketBuilder setFullPacket(byte[] fullPacket) throws SplitPacketBuildFailureException
	{
		if(!this.splitPacket.empty())
			throw new SplitPacketBuildFailureException("이미 샛팅 된 데이터");
		if(this.code == -1)
			throw new SplitPacketBuildFailureException("코드가 샛팅되지 않음");
		
		int segmentCount = fullPacket.length / SplitPacketUtil.RANGE_PAYLOAD;
		if(fullPacket.length % SplitPacketUtil.RANGE_PAYLOAD != 0) ++segmentCount;
		
		for(int i = 0; i < segmentCount; ++i)
		{
			byte[] segment = new byte[SplitPacketUtil.SPLIT_SIZE];
			this.splitPacket.add(segment);
			ByteBuffer segmentBuffer = ByteBuffer.wrap(segment);
			segmentBuffer.put(SplitPacketUtil.MAGIC_NO_START);
			segmentBuffer.putInt(this.code);
			segmentBuffer.putInt(i);
			int payloadStart = i * SplitPacketUtil.RANGE_PAYLOAD;
			int payloadSize;
			
			if(payloadStart + SplitPacketUtil.RANGE_PAYLOAD <= fullPacket.length)
				payloadSize =  SplitPacketUtil.RANGE_PAYLOAD;
			else payloadSize = fullPacket.length - payloadStart;
			segmentBuffer.put(fullPacket, payloadStart, payloadSize);
			segmentBuffer.put(PacketUtil.MAGIC_NO_END);
		}
		return this;
	}
	
	public boolean addPacket(byte[] rawData)
	{
		ByteBuffer buf = ByteBuffer.wrap(rawData);
		buf.position(SplitPacketUtil.START_PACKET_ID);
		if(buf.getInt() == this.code)
		{
			
		}
		this.splitPacket.push(rawData);
		return false;
		//return this;
	}
	
	public SplitPacket getInstance() throws SplitPacketBuildFailureException
	{
		//if(!this.isSetCode) throw new SplitPacketBuildFailureException("패킷이 완성되지 않았습니다");
		
		byte[][] arr = new byte[this.splitPacket.size()][];
		this.splitPacket.toArray(arr);
		return new SplitPacket(arr);
	}
}
