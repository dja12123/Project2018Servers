package node.network;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.logging.Level;

public class NetworkUtil
{
	private static InetAddress BROADCAST_IA;
	private static InetAddress ALL_IA;
	
	static
	{
		try
		{
			BROADCAST_IA = InetAddress.getByName("255.255.255.255");
			ALL_IA = InetAddress.getByName("0.0.0.0");
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
	}
	
	public static InetAddress broadcastIA()
	{
		return BROADCAST_IA;
	}
	
	public static InetAddress allIA()
	{
		return ALL_IA;
	}
	
	public static NetworkInterface getNetworkInterface(String name)
	{
		Enumeration<NetworkInterface> nets = null;
		NetworkInterface findInterface = null;

		try
		{
			nets = NetworkInterface.getNetworkInterfaces();
		}
		catch (SocketException e)
		{
			NetworkManager.logger.log(Level.SEVERE, "네트워크 인터페이스 목록을 가져올 수 없습니다.", e);
		}
		if (nets == null)
			return null;

		StringBuffer netInfoBuf = new StringBuffer();
		netInfoBuf.append("네트워크 인터페이스 스캔\n");
		while (nets.hasMoreElements())
		{
			NetworkInterface net = nets.nextElement();
			try
			{
				if (!net.isUp())
					continue;
			}
			catch (SocketException e)
			{
				continue;
			}

			if (net.getName().equals(name))
			{
				findInterface = net;
				netInfoBuf.append("<SELECT>");
			}

			netInfoBuf.append("  Name:");
			netInfoBuf.append(net.getName());
			netInfoBuf.append(" Addr=>\n");

			int count = 0;
			Enumeration<InetAddress> addressItr = net.getInetAddresses();
			while (addressItr.hasMoreElements())
			{
				++count;
				InetAddress addr = addressItr.nextElement();
				netInfoBuf.append("    IP");
				netInfoBuf.append(count);
				netInfoBuf.append(": ");
				netInfoBuf.append(addr.getHostAddress());
				netInfoBuf.append("\n");
			}
			// netInfoBuf.deleteCharAt(netInfoBuf.length() - 1);

		}
		NetworkManager.logger.log(Level.INFO, netInfoBuf.toString());

		return findInterface;
	}
	
	public static Inet4Address getInterface4Addr(NetworkInterface network)
	{
		Enumeration<InetAddress> addrList = network.getInetAddresses();
		Inet4Address addr4 = null;
		InetAddress addr;
		while(addrList.hasMoreElements())
		{
			addr = addrList.nextElement();
			if(addr instanceof Inet4Address)
			{
				addr4 = (Inet4Address) addr;
				break;
			}
		}
		return addr4;
		
	}
}
