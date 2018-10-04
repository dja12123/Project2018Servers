package node.network.communicator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.network.packet.Packet;
import node.network.packet.PacketUtil;
import node.util.observer.Observable;
import node.util.observer.Observer;
import node.IServiceModule;
import node.NodeControlCore;
import node.log.LogWriter;
import node.network.NetworkManager;

public class SocketHandler implements IServiceModule, Runnable
{
	public static final Logger netScannerLogger = LogWriter.createLogger(SocketHandler.class, "netScanner");
	
	private HashMap<String, Observable<NetworkEvent>> observerMap;
	
	private ExecutorService packetProcessService = Executors.newCachedThreadPool();
	
	private Thread worker = null;
	private boolean isWork;
	
	private DatagramSocket socket;
	
	public SocketHandler()
	{
		this.observerMap = new HashMap<String, Observable<NetworkEvent>>();
	}
	
	public void addObserver(String key, Observer<NetworkEvent> observer)
	{
		Observable<NetworkEvent> ob = this.observerMap.getOrDefault(key, null);
		if(ob == null)
		{
			ob = (new Observable<NetworkEvent>());
			this.observerMap.put(key, ob);
		}
		
		ob.addObserver(observer);
	}
	
	public void removeObserver(String key, Observer<NetworkEvent> observer)
	{
		Observable<NetworkEvent> observable = this.observerMap.getOrDefault(key, null);
		if(observable == null)
		{
			return;
		}
		observable.removeObserver(observer);
		
		if(observable.size() == 0)
		{
			this.observerMap.remove(key);
		}
	}
	
	public void removeObserver(Observer<NetworkEvent> observer)
	{
		Observable<NetworkEvent> observable;
		for(String key : this.observerMap.keySet())
		{
			observable = this.observerMap.get(key);
			observable.removeObserver(observer);
			
			if(observable.size() == 0)
			{
				this.observerMap.remove(key);
			}
		}
	}

	@Override
	public boolean startModule()
	{
		if(this.isWork) return true;
		
		if(this.worker == null || !this.worker.isAlive())
		{
			this.worker = new Thread(this);
		}
		
		try
		{
			int port = Integer.parseInt(NodeControlCore.getProp(NetworkManager.PROP_INFOBROADCAST_PORT));
			InetAddress addr = InetAddress.getByName("0.0.0.0");
			this.socket = new DatagramSocket(port, addr);
		}
		catch (SocketException | UnknownHostException e)
		{
			netScannerLogger.log(Level.SEVERE, "소켓 열기 실패", e);
			return false;
		}
		
		this.isWork = true;
		this.worker.start();
		return true;
	}

	@Override
	public void stopModule()
	{
		if(!this.isWork) return;
		
		this.isWork = false;
		this.worker.interrupt();
		this.socket.close();
	}

	@Override
	public void run()
	{
		byte[] packetBuffer = new byte[PacketUtil.HEADER_SIZE + PacketUtil.MAX_SIZE_KEY + PacketUtil.MAX_SIZE_DATA];
		DatagramPacket packet;
		while(this.isWork)
		{
			packet = new DatagramPacket(packetBuffer, packetBuffer.length);
			try
			{
				this.socket.receive(packet);
				if(!PacketUtil.isPacket(packetBuffer))
				{
					continue;
				}
				
				PacketProcess process = new PacketProcess(packetBuffer, this.observerMap);
				this.packetProcessService.execute(process);
			}
			catch (IOException e)
			{
				netScannerLogger.log(Level.SEVERE, "수신 실패", e);
			}
		}
	}
	
	public void sendMessage(Packet packet)
	{// 장치 테이블 조회후 날리기?
		
	}
}

class PacketProcess implements Runnable
{
	private byte[] rawPacket;
	private HashMap<String, Observable<NetworkEvent>> observerMap;

	public PacketProcess(byte[] rawPakcet, HashMap<String, Observable<NetworkEvent>> observerMap)
	{
		this.rawPacket = PacketUtil.clonePacketByte(rawPakcet);
		this.observerMap = observerMap;
	}

	@Override
	public void run()
	{
		Packet packet = new Packet(this.rawPacket);
		Observable<NetworkEvent> observable = observerMap.getOrDefault(packet.getKey(), null);
		if(observable != null)
		{
			observable.notify();
		}
	}
	
}
