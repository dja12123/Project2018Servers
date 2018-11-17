package node.network.spiltpacket;

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
	}

	public boolean enQueue(byte item)
	{
		if (this.queueSize - 1 == this.size)
		{// 큐가 찼을때
			this.front = (this.front + 1) % this.queueSize;
		}
		
		this.rear = (this.rear + 1) % (this.queueSize);
		this.itemArray[this.rear] = item;
		
		this.size = this.front > this.rear ? (this.queueSize - this.front + this.rear) : (this.rear - this.front);
		
		if(this.isStart)
		{
			--this.isStartLife;
			if(this.isStartLife < 0)
			{
				this.isStart = false;
			}
		}
		
		if(this.size >= SplitPacketUtil.RANGE_MAGICNO_START)
		{
			if(this.findPattern((this.front + this.size - SplitPacketUtil.RANGE_MAGICNO_START)
					, SplitPacketUtil.MAGIC_NO_START))
			{
				this.isStart = true;
				this.isStartLife = this.queueSize - SplitPacketUtil.RANGE_MAGICNO_START - 1;
			}
			else if(this.isStart && this.isStartLife == this.queueSize - 1 - this.size &&
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
	
	public byte[] getSnapShot()
	{
		if(this.front == this.rear)
			return null;
		byte[] snapShot = new byte[this.size];
		if(this.front < this.rear)
		{
			System.arraycopy(this.itemArray, this.front + 1, snapShot, 0, this.size);
		}
		else
		{
			System.arraycopy(this.itemArray, this.front + 1, snapShot , 0, this.queueSize - 1 - this.front);
			System.arraycopy(this.itemArray, 0, snapShot , this.queueSize - 1 - this.front, this.rear + 1);
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
		byte[] snapShot = this.getSnapShot();
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
