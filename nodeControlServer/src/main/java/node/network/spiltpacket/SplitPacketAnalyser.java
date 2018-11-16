package node.network.spiltpacket;
// 현재 splitPacket 프로토콜은 UDP패킷을 수신할 때 IP스푸핑을 사용한 응용 레벨 서비스 거부 공격에 대응할 수 없습니다. 개선 필요

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class SplitPacketAnalyser
{
	private final int SEGMENT_TIMEOUT = 100;
	private final int CHECK_DELAY = 1000;
	
	private Thread worker;
	private boolean isRun;
	
	private HashMap<InetAddress, PacketQueue> packetStack;
	private HashMap<InetAddress, SplitPacketBuilder> builderStack;
	
	public SplitPacketAnalyser()
	{
		this.worker = null;
		this.isRun = false;
		this.packetStack = new HashMap<>();
		this.builderStack = new HashMap<>();
	}
	
	public void start()
	{
		if(this.isRun)
			return;
		this.isRun = true;
		
		this.worker = new Thread(this::run);
		this.worker.start();
	}
	
	public void end()
	{
		if(!this.isRun)
			return;
		this.isRun = false;
		
		this.worker.interrupt();
		this.packetStack.clear();
		this.builderStack.clear();
	}
	
	public synchronized void analysePacket(InetAddress inetAddr, byte[] copyedBuffer)
	{// 패킷 분석해서 스택에 넣어줌...
		ByteBuffer orgBuffer = ByteBuffer.wrap(copyedBuffer);
		PacketQueue queue = this.packetStack.get(inetAddr);
		
		if(queue == null)
		{
			queue = new PacketQueue(SplitPacketUtil.FULL_PACKET_LIMIT);
			this.packetStack.put(inetAddr, queue);
		}
		
		for(int i = 0; i < copyedBuffer.length; ++i)
		{
			if(queue.enQueue(copyedBuffer[i]))
			{
				System.out.println("패킷이당");
			}
		}
		
	}
	
	private void run()
	{
		while(this.isRun)
		{
			synchronized (this)
			{
				for(SplitPacketBuilder builder : this.builderStack.values())
				{
					
				}
			}
			
			try
			{
				Thread.sleep(CHECK_DELAY);
			}
			catch (InterruptedException e)
			{
				break;
			}
		}
	}
}


class PacketQueue
{
	private int front;
	private int rear;
	private final int queueSize;
	private byte itemArray[];
	
	private boolean isStart;
	private boolean isEnd;
	private int size;
	
	private int isStartLife;
	private int isEndLife;

	@SuppressWarnings("unchecked")
	public PacketQueue(int queueSize)
	{
		this.front = 0;
		this.rear = 0;
		this.queueSize = queueSize + 1;
		this.itemArray = new byte[queueSize + 1];
		
		this.isStart = false;
		this.isEnd = false;
	}

	// 큐가 비어있는지 확인
	public boolean isEmpty()
	{
		return (front == rear);
	}

	// 큐가 가득차 있는지 확인
	public boolean isFull()
	{
		return this.queueSize - 1 == this.size;
	}

	// 큐의 삽입 연산
	public boolean enQueue(byte item)
	{
		if (this.isFull())
		{
			this.front = (this.front + 1) % this.queueSize;
		}
		
		this.rear = (this.rear + 1) % (this.queueSize);
		this.setSize();
		this.itemArray[this.rear] = item;
		
		if(this.size >= SplitPacketUtil.MAGIC_NO_START.length)
		{
			if(this.findPattern((this.front + this.size - SplitPacketUtil.MAGIC_NO_START.length)
					, SplitPacketUtil.MAGIC_NO_START))
			{
				System.out.println("스타트감지");
				this.isStart = true;
				this.isStartLife = this.queueSize - SplitPacketUtil.MAGIC_NO_START.length;
			}
			
			if(this.findPattern((this.front + this.size - SplitPacketUtil.MAGIC_NO_END.length)
					, SplitPacketUtil.MAGIC_NO_END))
			{
				
				this.isEnd = true;
				this.isEndLife = this.queueSize - SplitPacketUtil.MAGIC_NO_END.length;
			}
		}
		
		if(this.isStart)
		{
			--this.isStartLife;
			if(this.isStartLife < 0)
			{
				this.isStart = false;
			}
		}
		
		if(this.isEnd)
		{
			--this.isEndLife;
			if(this.isEndLife < 0)
			{
				this.isEnd = false;
			}
		}
		
		if(this.isStart && this.isEnd && this.isStartLife == 0 && this.isEndLife == this.queueSize - SplitPacketUtil.MAGIC_NO_END.length - 1)
		{
			return true;
		}
		return false;
	}
	
	public byte get(int index)
	{
		return this.itemArray[(index + this.front + 1) % this.queueSize];
	}

	// 큐의 현재 front값 출력
	public byte peek()
	{
		if (this.isEmpty())
		{
			return 0;
		}
		else
		{
			return this.itemArray[(this.front + 1) % this.queueSize];
		}
	}
	
	// 전체 큐값 출력
	@Override
	public String toString()
	{
		if(this.isEmpty())
		{
			return "";
		}
		StringBuffer buf = new StringBuffer();
		int f = this.front;

		while (f != this.rear)
		{
			f = (f + 1) % this.queueSize;
			buf.append(String.format("%2s ", Integer.toHexString(this.itemArray[f])));
		}
		buf.append('\n');
		return buf.toString();
	}
	
	private void setSize()
	{
		this.size = this.front > this.rear ? 
				(this.queueSize - this.front + this.rear) : (this.rear - this.front);
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