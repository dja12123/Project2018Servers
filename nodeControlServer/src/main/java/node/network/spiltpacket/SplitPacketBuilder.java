package node.network.spiltpacket;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.Stack;

public class SplitPacketBuilder
{
	private Stack<byte[]> splitPacket;
	private byte[] id;
	private int lastSegNo;
	private int fullSegmentCount;
	private Date lastUpdateTime;

	public SplitPacketBuilder()
	{
		this.splitPacket = new Stack<>();
		
		this.id = null;
		this.lastSegNo = -1;
		this.fullSegmentCount = -1;
	}
	
	public SplitPacketBuilder setID(byte[] id) throws SplitPacketBuildFailureException
	{
		if(this.id != null)
			throw new SplitPacketBuildFailureException("이미 샛팅 된 ID");
		this.id = Arrays.copyOf(id, id.length);
		return this;
	}
	
	public SplitPacketBuilder setID(long id) throws SplitPacketBuildFailureException
	{
		if(this.id != null)
			throw new SplitPacketBuildFailureException("이미 샛팅 된 ID");
		this.id = SplitPacketUtil.longToBytes(id);
		return this;
	}

	public SplitPacketBuilder setFullSegment(int fullSegment) throws SplitPacketBuildFailureException
	{
		if(this.fullSegmentCount != -1)
			throw new SplitPacketBuildFailureException("이미 샛팅 된 세그먼트 개수");
		this.fullSegmentCount = fullSegment;
		return this;
	}
	
	public SplitPacketBuilder addRawPacket(byte[] rawData) throws SplitPacketBuildFailureException
	{
		if(this.fullSegmentCount == -1)
			throw new SplitPacketBuildFailureException("세그먼트 개수가 설정되지 않음");
		
		++this.lastSegNo;
		this.splitPacket.push(rawData);
		return this;
	}
	
	public void updateTime()
	{
		this.lastUpdateTime = new Date(System.currentTimeMillis());
	}
	
	public Date getTime()
	{
		return this.lastUpdateTime;
	}
	
	public boolean checkPacket(byte[] rawData) throws SplitPacketBuildFailureException
	{
		ByteBuffer buf = ByteBuffer.wrap(rawData);
		byte[] compareID = new byte[SplitPacketUtil.RANGE_PACKET_ID];
		buf.position(SplitPacketUtil.START_PACKET_ID);
		buf.get(compareID);
		int fullSegmentSize = buf.getInt();
		int nowSegmentNo = buf.getInt();
		
		if(this.id == null)
			throw new SplitPacketBuildFailureException("아이디 미설정");
		
		if(this.fullSegmentCount <= buf.getInt())
			throw new SplitPacketBuildFailureException("세그먼트 카운트 어긋남");
		
		if(!Arrays.equals(this.id, compareID))
			return false;
		
		if(this.fullSegmentCount != fullSegmentSize)
			return false;
		
		if(this.lastSegNo + 1 != nowSegmentNo)
			return false;
		
		return true;
	}
	
	public boolean isBuilded()
	{
		return this.fullSegmentCount != this.lastSegNo + 1;
	}
	
	public SplitPacket getInstance() throws SplitPacketBuildFailureException
	{
		if(!this.isBuilded())
			throw new SplitPacketBuildFailureException("빌드가 모두 완료되지 않음");
		
		byte[][] arr = new byte[this.splitPacket.size()][];
		this.splitPacket.toArray(arr);
		//return new SplitPacket(arr);
		return null;
	}
}
