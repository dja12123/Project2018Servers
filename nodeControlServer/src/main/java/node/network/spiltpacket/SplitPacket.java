package node.network.spiltpacket;

import java.nio.ByteBuffer;
import java.util.Arrays;

import node.network.NetworkUtil;
import node.network.packet.PacketUtil;

public class SplitPacket
{
	public final byte[] id;
	public final byte[] payload;
	public final int segCount;
	
	public SplitPacket(byte[] id, byte[] payload) throws SplitPacketBuildFailureException
	{
		if(payload.length == 0)
		{
			throw new SplitPacketBuildFailureException("패이로드가 0일수는 없습니다");
		}
		if(payload.length > SplitPacketUtil.FULL_PACKET_LIMIT)
		{
			throw new SplitPacketBuildFailureException("용량 제한을 초과했습니다");
		}
		
		this.id = id;
		int segmentCount = payload.length / SplitPacketUtil.RANGE_PAYLOAD;
		if((payload.length % SplitPacketUtil.RANGE_PAYLOAD) != 0) ++segmentCount;
		this.segCount = segmentCount;
		this.payload = payload;
		
	}
	
	public byte[][] getSplitePacket()
	{
		byte[][] splitPacket = new byte[this.segCount][];
		
		for(int i = 0; i < this.segCount; ++i)
		{
			int payloadStart = i * SplitPacketUtil.RANGE_PAYLOAD;
			int payloadSize;
			
			if(payloadStart + SplitPacketUtil.RANGE_PAYLOAD <= payload.length)
				payloadSize =  SplitPacketUtil.RANGE_PAYLOAD;
			else payloadSize = payload.length - payloadStart;
			
			byte[] segment = new byte[payloadSize + SplitPacketUtil.PACKET_METADATA_SIZE];
			splitPacket[i] = segment;
			ByteBuffer segmentBuffer = ByteBuffer.wrap(segment);
			segmentBuffer.put(SplitPacketUtil.MAGIC_NO_START);
			segmentBuffer.put(this.id);
			segmentBuffer.putInt(this.segCount);
			segmentBuffer.putInt(i);
			segmentBuffer.put(payload, payloadStart, payloadSize);
			segmentBuffer.put(SplitPacketUtil.MAGIC_NO_END);
		}
		return splitPacket;
	}

	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof SplitPacket))
		{
			return false;
		}
		SplitPacket target = (SplitPacket)o;
		
		if(!Arrays.equals(target.id, this.id))
		{
			return false;
		}
		
		if(target.segCount != this.segCount)
		{
			return false;
		}
		
		if(!Arrays.equals(target.payload, this.payload))
		{
			return false;
		}
		
		return true;
	}
}