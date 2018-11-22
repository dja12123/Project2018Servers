package node.network;

import java.net.InetAddress;
import java.net.UnknownHostException;

import node.NodeControlCore;

public class NetworkConfig
{
	public static final int DFT_WINDOW_SIZE = 1500;
	public static final String DEFAULT_SUBNET = "192.168.0";
	
	public static final String PROP_broadcastPort = "broadcastPort";
	public static final String PROP_unicastPort = "unicastPort";
	public static final String PROP_networkInterface = "networkInterface";
	public static final String PROP_defaultAddr = "defaultAddr";
	
	private String Subnet = null;
	private InetAddress defaultAddr = null;
	private InetAddress BROADCAST_IA = null;
	private InetAddress ALL_IA = null;
	private String nic = null;
	private int broadcastPort;
	private int unicastPort;
	
	public NetworkConfig()
	{

	}
	
	public void loadSetting()
	{
		String defaultInet = String.format("%s.%s",DEFAULT_SUBNET, NodeControlCore.getProp(PROP_defaultAddr));
		try
		{
			defaultAddr = InetAddress.getByName(defaultInet);
			ALL_IA = InetAddress.getByName("0.0.0.0");
			broadcastPort = Integer.parseInt(NodeControlCore.getProp(PROP_broadcastPort));
			unicastPort = Integer.parseInt(NodeControlCore.getProp(PROP_unicastPort));
			nic = NodeControlCore.getProp(PROP_networkInterface);
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
	}
	
	private boolean isSetSubnet(String subnet)
	{
		if(Subnet == null || !Subnet.equals(subnet))
		{
			Subnet = subnet;
			return false;
		}
		return true;
	}
	
	public InetAddress defaultAddr()
	{
		return defaultAddr;
	}
	
	public InetAddress broadcastIA(String subnet)
	{
		if(!isSetSubnet(subnet) || BROADCAST_IA == null)
		{
			try
			{
				BROADCAST_IA = InetAddress.getByName(Subnet + ".255");
			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
			}
		}
		return BROADCAST_IA;
	}
	
	public String getNIC()
	{
		return nic;
	}
	
	public InetAddress allIA()
	{
		return ALL_IA;
	}
	
	public int broadcastPort()
	{
		return broadcastPort;
	}
	
	public int unicastPort()
	{
		return unicastPort;
	}
}
