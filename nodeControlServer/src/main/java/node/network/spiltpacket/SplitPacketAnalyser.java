package node.network.spiltpacket;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class SplitPacketAnalyser
{
	private final int SEGMENT_TIMEOUT = 100;
	private final int CHECK_DELAY = 1000;
	
	private Thread worker;
	private boolean isRun;
	
	private HashMap<InetAddress, ByteBuffer> packetStack;
	private HashMap<InetAddress, SplitPacketBuilder> builderStack;
	
	public static void main(String[] args)
	{
		byte[] src = new byte[]{0x11,0x22,0x11,0x22,0x1E,0x32,0x28,0x11,0x22,0x11,0x22};
		byte[] pattern = new byte[] {0x11,0x22,0x11,0x22};
		
		System.out.println(findPatternIndex(src, 1, pattern));
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
		ByteBuffer targetBuffer = this.packetStack.get(inetAddr);
		
		int packetHeaderIndex = findPatternIndex(copyedBuffer, 0, SplitPacketUtil.MAGIC_NO_START);
		//int packet
		
		if(targetBuffer == null)
		{
			targetBuffer = ByteBuffer.wrap(new byte[SplitPacketUtil.SPLIT_SIZE]);
			this.packetStack.put(inetAddr, targetBuffer);
		}
		
		/*while(false)
		{
			
		}*/
		
		int orgBufferSegmentCount = copyedBuffer.length / SplitPacketUtil.SPLIT_SIZE;
		if(copyedBuffer.length % SplitPacketUtil.SPLIT_SIZE != 0)
			orgBufferSegmentCount += 1;
		for(int i = 0;i < orgBufferSegmentCount; ++i)
		{
			
		}
		
	}
	
	private static int findPatternIndex(byte[] src, int srcOffset, byte[] pattern)
	{
		int findTargetPointer = 0;
		for(int i = srcOffset; i < src.length; ++i)
		{
			if(findTargetPointer == pattern.length)
			{
				return i - pattern.length;
			}
			if(src[i] == pattern[findTargetPointer])
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
			return src.length - pattern.length;
		}
		return -1;
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
