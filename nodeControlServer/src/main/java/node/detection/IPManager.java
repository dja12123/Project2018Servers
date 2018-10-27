package node.detection;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class IPManager
{
	private HashMap<UUID, InetAddress> ipMap;
	private UUID[] ipArr;
	
	public IPManager()
	{
		this.ipMap = new HashMap<>();
		this.ipArr = new UUID[255];
		this.clear();
	}
	
	public void clear()
	{
		this.ipMap.clear();
		for(int i = 0; i < 255; ++i)
		{
			this.ipArr[i] = null;
		}
	}
	
	public InetAddress assignmentInetAddr(UUID uuid)
	{
		InetAddress addr = null;
		for(int i = 100; i < 255; ++i)
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
				this.ipMap.put(uuid, addr);
				break;
			}
		}
		return addr;
	}
	
	public void removeInetAddr(UUID uuid)
	{
		InetAddress addr = this.ipMap.get(uuid);
		this.ipArr[addr.getAddress()[3]] = null;
		this.ipMap.remove(uuid);
	}
	
	public InetAddress getInetAddr(UUID uuid)
	{
		return this.ipMap.get(uuid);
	}
}
