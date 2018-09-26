package node.network.communicator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.IServiceModule;
import node.NodeControlCore;
import node.db.DB_Handler;
import node.network.NetworkManager;
import node.util.observer.Observable;

public class SocketHandler implements IServiceModule, Runnable
{
	public static final Logger netScannerLogger = NodeControlCore.createLogger(SocketHandler.class, "netScanner");
	
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
	}

	@Override
	public void run()
	{
		byte[] packetBuffer = new byte[8192];
		DatagramPacket packet;
		while(this.isWork)
		{
			packet = new DatagramPacket(packetBuffer, packetBuffer.length);
			try
			{
				socket.receive(packet);
				
			}
			catch (IOException e)
			{
				netScannerLogger.log(Level.SEVERE, "수신 실패", e);
			}
		}
	}
}
