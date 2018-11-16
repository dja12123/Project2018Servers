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
	
	public static void main(String[] args)
	{
		byte[] src = new byte[]{0x11,0x22,0x11,0x22,0x1E,0x32,0x28,0x11,0x22,0x11,0x22};
		byte[] pattern = new byte[] {0x11,0x22,0x11,0x22};
		
		//System.out.println(findPatternIndex(src, 1, pattern));
		PacketQueue queue = new PacketQueue(5);
		for(int i = 0; i < 100; ++i)
		{
		
			queue.enQueue((byte) i);
			
			System.out.println(queue.get(1));
		}
		System.out.println();
	}
	
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
	
	private synchronized void analysePacket(InetAddress inetAddr, byte[] copyedBuffer)
	{// 패킷 분석해서 스택에 넣어줌...
		ByteBuffer orgBuffer = ByteBuffer.wrap(copyedBuffer);
		PacketQueue targetBuffer = this.packetStack.get(inetAddr);
		
		if(targetBuffer == null)
		{
			targetBuffer = new PacketQueue(SplitPacketUtil.FULL_PACKET_LIMIT);
			this.packetStack.put(inetAddr, targetBuffer);
		}
		
		//int bufStartPos = findPatternIndex(copyedBuffer, 0, SplitPacketUtil.MAGIC_NO_START);
		//int bufEndPos = findPatternIndex(copyedBuffer, 0, SplitPacketUtil.MAGIC_NO_END);
		
		int size = 0;
		for(int i = 0; i < copyedBuffer.length; ++i)
		{
			
		}
		
		
		int orgBufferSegmentCount = copyedBuffer.length / SplitPacketUtil.SPLIT_SIZE;
		if(copyedBuffer.length % SplitPacketUtil.SPLIT_SIZE != 0)
			orgBufferSegmentCount += 1;
		for(int i = 0;i < orgBufferSegmentCount; ++i)
		{
			
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
	private int maxSize;
	private byte itemArray[];
	
	private int frontIndex;
	private PacketQueue isExistFrontCode;
	private int size;

	@SuppressWarnings("unchecked")
	public PacketQueue(int queueSize)
	{
		this.front = 0;
		this.rear = 0;
		this.maxSize = queueSize + 1;
		this.itemArray = new byte[queueSize + 1];
	}

	// 큐가 비어있는지 확인
	public boolean isEmpty()
	{
		return (front == rear);
	}

	// 큐가 가득차 있는지 확인
	public boolean isFull()
	{
		return ((this.rear + 1) % this.maxSize == this.front);
	}

	// 큐의 삽입 연산
	public void enQueue(byte item)
	{
		if (isFull())
		{
			System.out.println("큐가 포화 상태");
			this.delete();
		}
		
		rear = (rear + 1) % (maxSize);
		this.setSize();
		itemArray[rear] = item;
		
		if(this.size >= SplitPacketUtil.MAGIC_NO_START.length)
		{
			//this.front + this.size - SplitPacketUtil.MAGIC_NO_START.length;
		}
	}

	// 큐의 삭제 후 반환 연산
	public byte deQueue()
	{
		if (isEmpty())
		{
			System.out.println("큐가 공백 상태");
			return 0;
		}
	
		front = (front + 1) % maxSize;
		this.setSize();
		return itemArray[front];
		
	}
	
	public byte get(int index)
	{
		return this.itemArray[(index + this.front + 1) % this.maxSize];
	}

	// 큐의 삭제 연산
	public void delete()
	{
		if (isEmpty())
		{
			System.out.println("삭제할 큐가 없음");
		}
		front = (front + 1) % maxSize;
		this.setSize();
	}

	// 큐의 현재 front값 출력
	public byte peek()
	{
		if (isEmpty())
		{
			System.out.println("큐가 비어있음");
			return 0;
		}
		else
		{
			return itemArray[(front + 1) % maxSize];
		}
	}
	
	// 전체 큐값 출력
	public void print()
	{
		if (isEmpty())
		{
			System.out.println("큐가 비어있음");
		}
		else
		{
			int f = front;

			while (f != rear)
			{
				f = (f + 1) % maxSize;
				System.out.print(itemArray[f] + " ");
			}
			System.out.println();
		}
	}
	
	private void setSize()
	{
		this.size = front > rear ? (maxSize - front + rear) : (rear - front);
	}
	
	private boolean findPattern(int front, byte[] pattern)
	{
		int findTargetPointer = 0;
		for(int i = front; i < front + pattern.length; ++i)
		{
			if(findTargetPointer == pattern.length)
			{
				return true;
			}
			if(this.itemArray[(i + front + 1) % this.maxSize] == pattern[findTargetPointer])
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
