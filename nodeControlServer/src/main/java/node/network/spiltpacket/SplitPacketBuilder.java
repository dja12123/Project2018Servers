package node.network.spiltpacket;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.Stack;

import node.network.NetworkUtil;

public class SplitPacketBuilder
{
	private Stack<byte[]> splitPacket;
	
	private InetAddress inetAddress;
	private byte[] id;
	private int lastSegNo;
	private int fullSegmentCount;
	private int payloadSize;
	private Date lastUpdateTime;
	private boolean isgetInstace;

	public SplitPacketBuilder(InetAddress inetAddress)
	{
		this.splitPacket = new Stack<>();
		
		this.inetAddress = inetAddress;
		this.id = null;
		this.lastSegNo = -1;
		this.fullSegmentCount = -1;
		this.payloadSize = 0;
		this.isgetInstace = false;
		
		this.updateTime();
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
		this.payloadSize += rawData.length - SplitPacketUtil.PACKET_METADATA_SIZE;
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
	
	public InetAddress getInetAddress()
	{
		return this.inetAddress;
	}
	
	public boolean checkPacket(byte[] rawData) throws SplitPacketBuildFailureException
	{
		int fullSegmentSize = SplitPacketUtil.getFullSegmentSize(rawData);
		int nowSegmentNo = SplitPacketUtil.getNowSegmentNum(rawData);
		
		if(this.id == null)
			throw new SplitPacketBuildFailureException("아이디 미설정");
		
		if(this.fullSegmentCount == -1)
			throw new SplitPacketBuildFailureException("full세그먼트 카운트 미설정");
		
		if(this.fullSegmentCount <= nowSegmentNo)
			throw new SplitPacketBuildFailureException(String.format("세그먼트 카운트 어긋남 (all:%d, now:%d)", this.fullSegmentCount, nowSegmentNo));
		
		if(!SplitPacketUtil.comparePacketID(rawData, this.id))
		{// ID다름
			return false;
		}
		
		if(this.fullSegmentCount != fullSegmentSize)
		{// 세그먼트 개수 다름
			return false;
		}
		
		if(this.lastSegNo + 1 != nowSegmentNo)
		{// 세그먼트 순서 틀림
			return false;
		}
		
		return true;
	}
	
	public boolean isBuilded()
	{
		return this.fullSegmentCount == this.lastSegNo + 1;
	}
	
	public SplitPacket getInstance() throws SplitPacketBuildFailureException
	{
		if(!this.isBuilded())
			throw new SplitPacketBuildFailureException("빌드가 모두 완료되지 않음");
		
		if(this.isgetInstace)
			throw new SplitPacketBuildFailureException("이미 빌드된 패킷");
		this.isgetInstace = true;
		
		byte[] payload = new byte[this.payloadSize];
		int storeSize = 0;
		int segPayloadSize;
		byte[] segment;
		for(int i = 0; i < this.splitPacket.size(); ++i)
		{
			segment = this.splitPacket.get(i);
			segPayloadSize = segment.length - SplitPacketUtil.PACKET_METADATA_SIZE;
			System.arraycopy(segment, SplitPacketUtil.START_PAYLOAD, payload, storeSize, segPayloadSize);
			storeSize += segPayloadSize;
		}
		SplitPacket packet = new SplitPacket(this.id, payload);
		return packet;
	}
}
