package node.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.xml.soap.Node;

import node.IServiceModule;
import node.NodeControlCore;
import node.bash.CommandExecutor;
import node.db.DB_Handler;
import node.log.LogWriter;
import node.network.communicator.SocketHandler;

public class NetworkManager implements IServiceModule
{
	public static final Logger networkLogger = LogWriter.createLogger(NetworkManager.class, "network");
	
	public static final String PROP_INFOBROADCAST_PORT = "infoBroadcastPort";
	public static final String PROP_INTERFACE = "networkInterface";
	
	public final SocketHandler socketHandler;
	
	public NetworkManager()
	{
		this.socketHandler = new SocketHandler();
	}

	@Override
	public boolean startModule()
	{
		this.socketHandler.start();
		return true;
	}

	@Override
	public void stopModule()
	{
		this.socketHandler.stop();
	}
	
	public static void main(String[] args) throws UnknownHostException
	{
		NodeControlCore.init();
		NetworkManager networkManager = new NetworkManager();
		networkManager.startModule();
		networkManager.setInetAddr(InetAddress.getByName("192.168.0.99"));
	}
	
	public void setInetAddr(InetAddress inetAddress)
	{
		ArrayList<String> command = new ArrayList<String>();
		String iface = NodeControlCore.getProp(PROP_INTERFACE);
		System.out.println("interface: " + iface);
		
		byte[] myAddrByte = inetAddress.getAddress();
		myAddrByte[3] = 1;
		String gatewayAddr = null;
		try
		{
			gatewayAddr = InetAddress.getByAddress(myAddrByte).getHostAddress();
		}
		catch (UnknownHostException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(gatewayAddr);
		command.add(String.format("ifdown %s", iface));
		command.add(String.format("ip addr flush dev %s", iface));
		command.add(String.format("ip addr change dev %s %s/24", iface, inetAddress.getHostAddress()));
		command.add(String.format("ip route add default via %s", gatewayAddr));
		command.add(String.format("ifup %s", iface));
		
		
		synchronized (this.socketHandler)
		{
			this.socketHandler.stop();
			try
			{
				CommandExecutor.executeBash(command);
			}
			catch (Exception e)
			{
				
				e.printStackTrace();
			}
			this.socketHandler.start();
			System.out.println("소켓시작");
		}
		
	}
}
