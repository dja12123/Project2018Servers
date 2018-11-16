package node;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

import com.savarese.rocksaw.net.RawSocket;

import node.fileIO.FileHandler;
import node.network.NetworkUtil;
import node.cluster.ClusterService;
import node.detection.*;
import node.util.observer.Observable;

public class TestMain extends Observable<NodeDetectionEvent>
{
	
	public static void main(String[] args) throws Exception
	{
		NodeControlCore.init();
		TestMain tm = new TestMain();
		
		ClusterService cs = new ClusterService(tm);
		InetAddress ip = InetAddress.getLocalHost();
		cs.startModule();
		
		NodeDetectionEvent nde = new NodeDetectionEvent(ip, true, 0);
		tm.notifyObservers(nde);
		/*
		RawSocket rawSocket = new RawSocket();
		try
		{
			rawSocket.open(RawSocket.PF_INET, RawSocket.getProtocolByName("ICMP"));
			rawSocket.bindDevice("eth0");
		}
		catch (IllegalStateException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] buffer = new byte[100000];
		while(true)
		{
			System.out.println("정상적으로 수신중입니다...");
			int readLen = 0;
			try
			{
				readLen = rawSocket.read(buffer);
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(NetworkUtil.bytesToHex(buffer, readLen));
		}
		*/
		
	}
	

}
