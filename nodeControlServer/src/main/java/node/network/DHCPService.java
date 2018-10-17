package node.network;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dhcp4java.DHCPCoreServer;
import org.dhcp4java.DHCPPacket;
import org.dhcp4java.DHCPResponseFactory;
import org.dhcp4java.DHCPServerInitException;
import org.dhcp4java.DHCPServlet;

import node.IServiceModule;
import node.NodeControlCore;
import node.log.LogWriter;

public class DHCPService extends DHCPServlet implements IServiceModule
{
	public static final Logger dhcpLogger = DHCPCoreServer.logger;
	public static final String PROP_INTERFACE = "dhcpInterface";
	
	static
	{
		LogWriter.initLogger(dhcpLogger, "dhcp");
	}
	
	public DHCPService()
	{
		
	}
	
	@Override
	public DHCPPacket service(DHCPPacket request)
	{
		dhcpLogger.log(Level.INFO, request.toString());
		byte[] deviceInfo = request.getOptionRaw((byte)224);
		dhcpLogger.log(Level.INFO, "DeviceInfo="+new String(deviceInfo));

		//DHCPResponseFactory.makeDHCPAck(request, offeredAddress, leaseTime, serverIdentifier, message, options)
		//DHCPResponseFactory.makeDHCPOffer(request, offeredAddress, leaseTime, serverIdentifier, message, options)
		return null;
	}
	
	

	@Override
	public boolean startModule()
	{
		dhcpLogger.log(Level.INFO, "DHCP 로드");
		String loadInterface = NodeControlCore.getProp(PROP_INTERFACE).toString();
		dhcpLogger.log(Level.INFO, "네트워크 인터페이스 선택: " + loadInterface);
		NetworkInterface network = NetworkUtil.getNetworkInterface(loadInterface);
		InetAddress inetAddress = NetworkUtil.getInterface4Addr(network);
		
		if(network == null || inetAddress == null)
		{
			dhcpLogger.log(Level.SEVERE, "올바르지 않은 인터페이스");
			return false;
		}
		
		String addrString = inetAddress.getHostAddress();
		
		dhcpLogger.log(Level.INFO, "선택 주소: " + addrString);
		try
		{
			Properties prop = new Properties();
			prop.setProperty(DHCPCoreServer.SERVER_ADDRESS, addrString + ":67");
			
			DHCPCoreServer server = DHCPCoreServer.initServer(this, prop);
			new Thread(server).start();
		}
		catch (DHCPServerInitException e)
		{
			dhcpLogger.log(Level.SEVERE, "dhcp초기화 실패", e);
			return false;
		}
		return true;
	}

	@Override
	public void stopModule()
	{
		// TODO Auto-generated method stub
		
	}
}
