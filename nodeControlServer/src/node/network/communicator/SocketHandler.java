package node.network.communicator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.IServiceModule;
import node.NodeControlCore;
import node.network.NetworkManager;
import node.network.packet.Packet;
import node.network.packet.PacketUtil;

public class SocketHandler implements IServiceModule, Runnable
{
	public static final Logger netScannerLogger = NodeControlCore.createLogger(SocketHandler.class, "netScanner");
	private ExecutorService packetProcessService = Executors.newCachedThreadPool();
	
	private Thread worker = null;
	private boolean isWork;
	
	private DatagramSocket socket;
	
	public SocketHandler()
	{
		
	}

	@Override
	public boolean start()
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
	public void stop()
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
				//PacketUtil.
				this.packetProcessService.execute(new PacketProcess(packet));
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

	public PacketProcess(DatagramPacket packet)
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run()
	{
		// TODO Auto-generated method stub
		
	}
	
}
