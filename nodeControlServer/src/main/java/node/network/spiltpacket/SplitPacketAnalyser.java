package node.network.spiltpacket;
// 현재 splitPacket 프로토콜은 UDP패킷을 수신할 때 IP스푸핑을 사용한 응용 레벨 서비스 거부 공격에 대응할 수 없습니다. 개선 필요

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.log.LogWriter;
import node.network.NetworkUtil;

public class SplitPacketAnalyser
{
	public static final Logger logger = LogWriter.createLogger(SplitPacketAnalyser.class, "splitPacket");
	
	private static final int SEGMENT_TIMEOUT = 100;
	private static final int CHECK_DELAY = 1000;
	
	private Thread worker;
	private boolean isRun;
	
	private HashMap<InetAddress, PacketQueue> ipQueueStack;
	private HashMap<Long, SplitPacketBuilder> builderStack;
	
	public static void main(String[] args) throws Exception
	{
		SplitPacketAnalyser analyser = new SplitPacketAnalyser();
		analyser.start();
		System.out.println("시작");
		InetAddress addr1 = InetAddress.getByName("192.168.0.1");
		InetAddress addr2 = InetAddress.getByName("192.168.0.1");
		InetAddress addr3 = InetAddress.getByName("192.168.0.1");
		
		byte[] test = new byte[50];
		for(int i = 0; i < test.length; ++i)
		{
			test[i] = 0x1F;
		}
		
		SplitPacket p = new SplitPacket(SplitPacketUtil.createSplitPacketID(addr1), test);
		System.out.println(p.segCount);
		byte[] receiveTest = new byte[test.length + (p.segCount * SplitPacketUtil.PACKET_METADATA_SIZE)];
		ByteBuffer buf = ByteBuffer.wrap(receiveTest);
		
		for(int i = 0; i < p.segCount; ++i)
		{
			byte[] seg = p.getSegment(i);
			buf.put(seg);
			
			System.out.println(NetworkUtil.bytesToHex(seg, seg.length));
		}

		analyser.analysePacket(addr1, receiveTest);
	}
	
	public SplitPacketAnalyser()
	{
		this.worker = null;
		this.isRun = false;
		this.ipQueueStack = new HashMap<>();
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
		this.ipQueueStack.clear();
		this.builderStack.clear();
	}
	
	public synchronized void analysePacket(InetAddress inetAddr, byte[] copyedBuffer)
	{// 패킷 분석해서 스택에 넣어줌...
		PacketQueue queue = this.ipQueueStack.get(inetAddr);
		if(queue == null)
		{
			queue = new PacketQueue(SplitPacketUtil.SPLIT_SIZE);
			this.ipQueueStack.put(inetAddr, queue);
		}
		queue.updateTime();
		
		for(int i = 0; i < copyedBuffer.length; ++i)
		{
			System.out.println(queue);
			if(queue.enQueue(copyedBuffer[i]))
			{
				System.out.println("감지완료");
				if(queue.getSize() > SplitPacketUtil.PACKET_METADATA_SIZE)
				{
					this.processPacket(queue.getSnapShot());
				}
			}
		}
	}
	
	private void processPacket(byte[] snapShot)
	{
		long id = SplitPacketUtil.headerToLong(snapShot);
		System.out.println("패킷이당");
		SplitPacketBuilder builder = this.builderStack.get(id);
		try
		{
			if(builder == null)
			{
				builder = new SplitPacketBuilder();
				this.builderStack.put(id, builder);
				builder.setID(id);
				System.out.println("빌더 생성");
			}
			if(!builder.checkPacket(snapShot))
			{
				this.builderStack.remove(id);
				System.out.println("오류 빌더 제거");
				return;
			}
			System.out.println("빌더에 패킷 추가");
			builder.addRawPacket(snapShot);
			if(builder.isBuilded())
			{
				this.builderStack.remove(id);
				System.out.println("빌드 완성!");
				return;
			}
			builder.updateTime();
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
					System.out.println("IP타임아웃이당");
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
					System.out.println("빌더타임아웃이당");
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

