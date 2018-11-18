package node.network.spiltpacket;
// 현재 splitPacket 프로토콜은 UDP패킷을 수신할 때 IP스푸핑을 사용한 응용 레벨 서비스 거부 공격에 대응할 수 없습니다. 개선 필요

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.log.LogWriter;

public class SplitPacketAnalyser
{
	public static final Logger logger = LogWriter.createLogger(SplitPacketAnalyser.class, "splitPacket");
	
	private static final int SEGMENT_TIMEOUT = 100;
	private static final int CHECK_DELAY = 1000;
	
	private Thread worker;
	private boolean isRun;
	
	private HashMap<InetAddress, PacketQueue> ipQueueStack;
	private HashMap<Long, SplitPacketBuilder> builderStack;
	
	private final BiConsumer<InetAddress, SplitPacket> receiveCallback;
	
	public SplitPacketAnalyser(BiConsumer<InetAddress, SplitPacket> receiveCallback)
	{
		this.worker = null;
		this.isRun = false;
		this.ipQueueStack = new HashMap<>();
		this.builderStack = new HashMap<>();
		
		this.receiveCallback = receiveCallback;
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
		this.ipQueueStack.clear();
		this.builderStack.clear();
	}
	
	public synchronized void analysePacket(InetAddress inetAddr, byte[] copyedBuffer)
	{// 패킷 분석해서 스택에 넣어줌...
		PacketQueue queue = this.ipQueueStack.get(inetAddr);
		if(queue == null)
		{
			queue = new PacketQueue(SplitPacketUtil.MAX_SEGMENT_SIZE);
			this.ipQueueStack.put(inetAddr, queue);
		}
		queue.updateTime();
		
		for(int i = 0; i < copyedBuffer.length; ++i)
		{
			if(queue.enQueue(copyedBuffer[i]))
			{
				if(queue.getSize() > SplitPacketUtil.PACKET_METADATA_SIZE)
				{// 큐에 완성된 패킷 세그먼트가 있을때.
					byte[] snapShot = queue.getSnapShot(queue.getPacketStartPosition(), SplitPacketUtil.MAX_SEGMENT_SIZE);
					// 해당하는 패킷을 가져옴.
					this.processPacket(inetAddr, snapShot);
				}
			}
		}
	}
	
	private void processPacket(InetAddress inetAddr, byte[] snapShot)
	{
		long id = SplitPacketUtil.headerToLong(snapShot);
		SplitPacketBuilder builder = this.builderStack.get(id);
		try
		{
			if(builder == null)
			{// ID에 해당하는 빌더가 없을경우 빌더 만듬
				builder = new SplitPacketBuilder(inetAddr);
				this.builderStack.put(id, builder);
				builder.setID(id);
				builder.setFullSegment(SplitPacketUtil.getFullSegmentSize(snapShot));
			}
			if(!builder.checkPacket(snapShot))
			{// 오류 빌더 제거
				this.builderStack.remove(id);
				return;
			}
			builder.addRawPacket(snapShot);
			builder.updateTime();
			if(builder.isBuilded())
			{// 패킷 빌드 완료
				SplitPacket splitPacket = builder.getInstance();
				this.receiveCallback.accept(builder.getInetAddress(), splitPacket);
				this.builderStack.remove(id);
				return;
			}
		}
		catch (SplitPacketBuildFailureException e)
		{
			logger.log(Level.WARNING, "패킷 처리중 오류", e);
		}
	}
	
	private void run()
	{
		Date compareTime;
		ArrayList<InetAddress> removeQueue = new ArrayList<>();
		ArrayList<Long> removeBuilders = new ArrayList<>();
		while(this.isRun)
		{
			compareTime = new Date(System.currentTimeMillis() - SEGMENT_TIMEOUT);
			removeQueue.clear();
			removeBuilders.clear();
			
			synchronized (this)
			{
				for(InetAddress key : this.ipQueueStack.keySet())
				{
					PacketQueue queue = this.ipQueueStack.get(key);
					if(compareTime.after(queue.getTime()))
					{//타임아웃일때
						removeQueue.add(key);
					}
				}
				for(int i = 0; i < removeQueue.size(); ++i)
				{
					this.ipQueueStack.remove(removeQueue.get(i));
				}
			}
			
			synchronized (this)
			{
				for(long key : this.builderStack.keySet())
				{
					SplitPacketBuilder builder = this.builderStack.get(key);
					if(compareTime.after(builder.getTime()))
					{//타임아웃일때
						removeBuilders.add(key);
					}
				}
				for(int i = 0; i < removeBuilders.size(); ++i)
				{
					this.builderStack.remove(removeBuilders.get(i));
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