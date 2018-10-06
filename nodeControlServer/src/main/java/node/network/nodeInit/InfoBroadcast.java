package node.network.nodeInit;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.IServiceModule;
import node.NodeControlCore;
import node.db.DB_Handler;
import node.device.Device;
import node.log.LogWriter;
import node.network.NetworkManager;

public class InfoBroadcast implements Runnable, IServiceModule
{
	public static final String PROP_DELAY_INFOMSG = "delayInfoBroadcast";
	
	public static final Logger broadcastLogger = LogWriter.createLogger(DB_Handler.class, "broadcast");
	private static InetAddress broadcastIA;
	
	private final Device device;
	
	private int broadCastDelay;
	private Thread broadcastThread = null;
	private boolean isRun = false;
	private DatagramSocket dgramSocket = null;
	
	private String infoString;
	private DatagramPacket packet;
	
	static
	{
		try
		{
			broadcastIA = InetAddress.getByName("255.255.255.255");
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
	}
	
	public InfoBroadcast(Device device)
	{
		this.device = device;
	}

	@Override
	public void run()
	{
		broadcastLogger.log(Level.INFO, "노드 알림 시작");
		while(this.isRun)
		{
			try
			{
				Thread.sleep(this.broadCastDelay);
			}
			catch (InterruptedException e) {}
			try
			{
				this.dgramSocket.send(packet);
			}
			catch (IOException e)
			{
				broadcastLogger.log(Level.SEVERE, "패킷 전송 오류");
			}
		}
	}
	
	private void broadCastInfo()
	{
		byte[] infoMessage = this.infoString.getBytes();
	
		
	}
	
	public boolean startModule()
	{
		if(this.isRun) this.stopModule();
		this.isRun = true;
		
		try
		{
			this.dgramSocket = new DatagramSocket();
		}
		catch (SocketException e)
		{
			broadcastLogger.log(Level.SEVERE, "소캣 생성 오류", e);
			return false;
		}
		
		this.broadCastDelay = Integer.parseInt(NodeControlCore.getProp(PROP_DELAY_INFOMSG));
		
		StringBuffer infoStringBuffer = new StringBuffer();
		
		
		byte[] infoMessage = this.infoString.getBytes();
		int port = Integer.valueOf(NodeControlCore.getProp(NetworkManager.PROP_INFOBROADCAST_PORT));
		this.packet = new DatagramPacket(infoMessage, infoMessage.length, broadcastIA, port);
		
		this.broadcastThread = new Thread(this);
		this.broadcastThread.start();
		return true;
	}
	
	public void stopModule()
	{
		if(!this.isRun) return;
		this.isRun = false;
		
		this.broadcastThread.interrupt();
	}
}