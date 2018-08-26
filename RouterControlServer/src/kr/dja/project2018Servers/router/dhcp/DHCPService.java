package kr.dja.project2018Servers.router.dhcp;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;

import org.dhcp4java.DHCPCoreServer;
import org.dhcp4java.DHCPOption;
import org.dhcp4java.DHCPPacket;
import org.dhcp4java.DHCPServerInitException;
import org.dhcp4java.DHCPServlet;

import kr.dja.project2018Servers.router.RouterCore;

public class DHCPService extends DHCPServlet
{
	public DHCPService()
	{
		String loadInterface = RouterCore.properties.get(RouterCore.CONF_INTERFACE).toString();
		RouterCore.dhcpLogger.log(Level.INFO, "네트워크 인터페이스 선택: " + loadInterface);
		NetworkInterface network = getNetworkInterfaces(loadInterface);
		
		if(network == null)
		{
			RouterCore.dhcpLogger.log(Level.SEVERE, "올바르지 않은 인터페이스");
			return;
		}
		
		Enumeration<InetAddress> addrList = network.getInetAddresses();
		String addrStr = null;
		while(addrList.hasMoreElements())
		{
			InetAddress addr = addrList.nextElement();
			if(addr instanceof Inet4Address)
			{
				addrStr = addr.getHostAddress() + ":67";
				break;
			}
		}
		
		RouterCore.dhcpLogger.log(Level.INFO, "선택 주소: " + addrStr);
		try
		{
			Properties prop = new Properties();
			prop.setProperty(DHCPCoreServer.SERVER_ADDRESS, addrStr);
			
			DHCPCoreServer server = DHCPCoreServer.initServer(this, prop);
			new Thread(server).start();
		}
		catch (DHCPServerInitException e)
		{
			RouterCore.dhcpLogger.log(Level.SEVERE, "Server init", e);
		}
	}
	
	@Override
	public DHCPPacket service(DHCPPacket request)
	{
		RouterCore.dhcpLogger.log(Level.INFO, request.toString());
		String deviceInfo = request.getOptionAsString((byte)224);
		RouterCore.dhcpLogger.log(Level.INFO, "DeviceInfo="+deviceInfo);

		DHCPPacket packet = new DHCPPacket();
		return null;
	}
	
	private static NetworkInterface getNetworkInterfaces(String name)
	{
		Enumeration<NetworkInterface> nets = null;
		NetworkInterface findInterface = null;

		try
		{
			nets = NetworkInterface.getNetworkInterfaces();
		}
		catch (SocketException e)
		{
			RouterCore.dhcpLogger.log(Level.SEVERE, "네트워크 인터페이스 목록을 가져올 수 없습니다.", e);
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
		RouterCore.dhcpLogger.log(Level.INFO, netInfoBuf.toString());

		return findInterface;
	}
}
