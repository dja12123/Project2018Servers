package node.network.spiltpacket;

import java.nio.ByteBuffer;

import node.network.packet.PacketUtil;

public class SplitPacket
{
	private final byte[][] source;
	
	public final byte[] id;
	public final int segCount;
	
	public SplitPacket(byte[] id, byte[] payload) throws SplitPacketBuildFailureException
	{
		int segmentCount = payload.length / SplitPacketUtil.RANGE_PAYLOAD;
		if(payload.length % SplitPacketUtil.RANGE_PAYLOAD != 0) ++segmentCount;
		this.segCount = segmentCount;
		this.id = id;
		this.source = new byte[this.segCount][];
		
		for(int i = 0; i < this.segCount; ++i)
		{
			int payloadStart = i * SplitPacketUtil.RANGE_PAYLOAD;
			int payloadSize;
			
			if(payloadStart + SplitPacketUtil.RANGE_PAYLOAD <= payload.length)
				payloadSize =  SplitPacketUtil.RANGE_PAYLOAD;
			else payloadSize = payload.length - payloadStart;
			
			byte[] segment = new byte[payloadSize + SplitPacketUtil.PACKET_METADATA_SIZE];
			this.source[i] = segment;
			ByteBuffer segmentBuffer = ByteBuffer.wrap(segment);
			segmentBuffer.put(SplitPacketUtil.MAGIC_NO_START);
			segmentBuffer.put(this.id);
			segmentBuffer.putInt(this.segCount);
			segmentBuffer.putInt(i);
			segmentBuffer.put(payload, payloadStart, payloadSize);
			segmentBuffer.put(SplitPacketUtil.MAGIC_NO_END);
		}
	}
	
	public byte[] getSegment(int segno)
	{
		return this.source[segno];
	}
}