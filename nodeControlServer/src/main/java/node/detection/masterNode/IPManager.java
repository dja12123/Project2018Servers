package node.detection.masterNode;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import node.NodeControlCore;
import node.detection.NodeDetectionService;

public class IPManager
{
	public static final String PROP_ipAssignStart = "ipAssignStart";
	public static final String PROP_ipAssignEnd = "ipAssignEnd";
	
	private int assignStart;
	private int assignEnd;
	
	private HashMap<UUID, InetAddress> ipMap;
	private UUID[] ipArr;
	
	public IPManager()
	{
		this.assignStart = Integer.parseInt(NodeControlCore.getProp(PROP_ipAssignStart));
		this.assignEnd = Integer.parseInt(NodeControlCore.getProp(PROP_ipAssignEnd));
		
		this.ipMap = new HashMap<>();
		this.ipArr = new UUID[255];
		this.clear();
	}
	
	public void clear()
	{
		this.ipMap.clear();
		for(int i = assignStart; i < assignEnd; ++i)
		{
			this.ipArr[i] = null;
		}
	}
	
	public InetAddress assignmentInetAddr(UUID uuid)
	{
		InetAddress addr = null;
		for(int i = assignStart; i <= assignEnd; ++i)
		{
			if(this.ipArr[i] == null)
			{
				try
				{
					addr = InetAddress.getByName(String.format("192.168.0.%d", i));
				}
				catch (UnknownHostException e)
				{
					NodeDetectionService.logger.log(Level.SEVERE, "IP할당 오류", e);
					return null;
				}
				this.ipArr[i] = uuid;
				this.ipMap.put(uuid, addr);
				break;
			}
		}
		return addr;
	}
	
	public void removeInetAddr(UUID uuid)
	{
		InetAddress addr = this.ipMap.get(uuid);
		if(addr == null) return;
		this.ipArr[addr.getAddress()[3]] = null;
		this.ipMap.remove(uuid);
	}
	
	public InetAddress getInetAddr(UUID uuid)
	{
		return this.ipMap.get(uuid);
	}
}
