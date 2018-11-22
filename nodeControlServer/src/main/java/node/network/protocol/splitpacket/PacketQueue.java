package node.network.protocol.splitpacket;

import java.util.Date;

import node.network.NetworkUtil;

class PacketQueue
{
	private int front;
	private int rear;
	private final int queueSize;
	private byte itemArray[];
	
	private boolean isStart;
	private int isStartLife;
	private int size;
	private Date lastUpdateTime;

	public PacketQueue(int queueSize)
	{
		this.front = 0;
		this.rear = 0;
		this.queueSize = queueSize + 1;
		this.itemArray = new byte[queueSize + 1];
		
		this.isStart = false;
		
		this.updateTime();
	}

	public boolean enQueue(byte b)
	{// 패킷 바이트 삽입
		if (this.queueSize - 1 == this.size)
		{// 큐가 찼을때
			this.front = (this.front + 1) % this.queueSize;
			if(this.isStart)
			{
				--this.isStartLife;
				if(this.isStartLife < 0)
				{
					this.isStart = false;
				}
			}
		}
		
		this.rear = (this.rear + 1) % (this.queueSize);
		this.itemArray[this.rear] = b;
		
		this.size = this.front > this.rear ? (this.queueSize - this.front + this.rear) : (this.rear - this.front);
		
		if(this.size >= SplitPacketUtil.RANGE_MAGICNO_START)
		{
			if(this.findPattern((this.front + this.size - SplitPacketUtil.RANGE_MAGICNO_START)
					, SplitPacketUtil.MAGIC_NO_START))
			{
				this.isStart = true;
				this.isStartLife = this.size - SplitPacketUtil.RANGE_MAGICNO_START;
			}
			else if(this.isStart && 
					this.findPattern((this.front + this.size - SplitPacketUtil.RANGE_MAGIC_NO_END) , SplitPacketUtil.MAGIC_NO_END))
			{
				return true;
			}
		}
		return false;
	}
	
	public void updateTime()
	{
		this.lastUpdateTime = new Date(System.currentTimeMillis());
	}
	
	public Date getTime()
	{
		return this.lastUpdateTime;
	}
	
	public int getSize()
	{
		return this.size;
	}
	
	public int getPacketStartPosition()
	{
		return this.isStartLife;
	}
	
	public byte[] getSnapShot(int start, int end)
	{
		if(this.front == this.rear)
			return null;
		byte[] snapShot = new byte[end - start];
		int front = (this.front + start) % this.queueSize;
		int rear = (this.queueSize + this.rear - (this.size - end)) % this.queueSize;
		if(front < rear)
		{
			System.arraycopy(this.itemArray, front + 1, snapShot, 0, end - start);
		}
		else
		{
			System.arraycopy(this.itemArray, front + 1, snapShot, 0, this.queueSize - 1 - front);
			System.arraycopy(this.itemArray, 0, snapShot, this.queueSize - 1 - front, rear + 1);
		}
		return snapShot;
	}
	
	// 전체 큐값 출력
	@Override
	public String toString()
	{
		if(this.front == this.rear)
		{
			return "";
		}
		byte[] snapShot = this.getSnapShot(0, this.queueSize - 1);
		return NetworkUtil.bytesToHex(snapShot, snapShot.length);
	}
	
	private boolean findPattern(int front, byte[] pattern)
	{
		int findTargetPointer = 0;
		
		for(int i = 0; i < pattern.length; ++i)
		{
			if(findTargetPointer == pattern.length)
			{
				return true;
			}
			if(this.itemArray[(i + front + 1) % this.queueSize] == pattern[findTargetPointer])
			{
				++findTargetPointer;
			}
			else
			{
				findTargetPointer = 0;
			}
		}
		if(findTargetPointer == pattern.length)
		{
			return true;
		}
		return false;
	}
}
