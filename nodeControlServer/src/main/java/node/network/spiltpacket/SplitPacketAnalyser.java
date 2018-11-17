package node.network.spiltpacket;
// 현재 splitPacket 프로토콜은 UDP패킷을 수신할 때 IP스푸핑을 사용한 응용 레벨 서비스 거부 공격에 대응할 수 없습니다. 개선 필요

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
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
		Random r = new Random();
		

		
		for(int x = 0; x < 1000; ++x)
		{
			byte[] test = new byte[r.nextInt(49) + 1];
			for(int i = 0; i < test.length; ++i)
			{
				test[i] = (byte)i;
			}
			SplitPacket p = new SplitPacket(SplitPacketUtil.createSplitPacketID(addr1), test);
			System.out.println(p.segCount);
			byte[] receiveTest = new byte[test.length + (p.segCount * SplitPacketUtil.PACKET_METADATA_SIZE) + 1000];
			ByteBuffer buf = ByteBuffer.wrap(receiveTest);
			buf.putInt(0xBABABA);
			for(int i = 0; i < p.segCount; ++i)
			{
				byte[] seg = p.getSegment(i);
				
				System.out.println("SEG "+NetworkUtil.bytesToHex(seg, seg.length));
				buf.put(seg);
				
				for(int j = 0; j < r.nextInt(10); ++j)
				{
					buf.put((byte) 0xAA);
				}
			}
			analyser.analysePacket(addr1, receiveTest);
			if(!p.equals(analyser.p))
			{
				
				System.out.println("미션 실패");
				return;
			}
		}
		System.out.println("미션 석쎾쓰");

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
			if(queue.enQueue(copyedBuffer[i]))
			{
				if(queue.getSize() > SplitPacketUtil.PACKET_METADATA_SIZE)
				{
					byte[] snapShot = queue.getSnapShot(queue.getPacketStartPosition(), SplitPacketUtil.SPLIT_SIZE);
					System.out.println("SNAP " + queue.getPacketStartPosition());
					this.processPacket(snapShot);
				}
			}
		}
	}
	
	private SplitPacket p;
	private void processPacket(byte[] snapShot)
	{
		long id = SplitPacketUtil.headerToLong(snapShot);
		System.out.println(NetworkUtil.bytesToHex(Arrays.copyOfRange(snapShot, SplitPacketUtil.START_PACKET_ID, SplitPacketUtil.START_PACKET_ID+ SplitPacketUtil.RANGE_PACKET_ID), SplitPacketUtil.RANGE_PACKET_ID));
		SplitPacketBuilder builder = this.builderStack.get(id);
		try
		{
			if(builder == null)
			{
				builder = new SplitPacketBuilder();
				this.builderStack.put(id, builder);
				System.out.println("풋" + this.builderStack.size());
				builder.setID(id);
				builder.setFullSegment(SplitPacketUtil.getFullSegmentSize(snapShot));
				System.out.println("빌더 생성" +SplitPacketUtil.getFullSegmentSize(snapShot));
			}
			if(!builder.checkPacket(snapShot))
			{
				this.builderStack.remove(id);
				System.out.println("오류 빌더 제거");
				return;
			}
			System.out.println("빌더에 패킷 추가");
			builder.addRawPacket(snapShot);
			builder.updateTime();
			if(builder.isBuilded())
			{
				this.builderStack.remove(id);
				p = builder.getInstance();
				System.out.println("빌드 완성!");
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