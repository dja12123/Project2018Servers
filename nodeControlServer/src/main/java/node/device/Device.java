package node.device;

import java.net.InetAddress;
import java.util.UUID;

public class Device
{
	public final UUID uuid;
	private InetAddress inetAddr;
	//private String deviceName;
	
	private Device(UUID uuid)
	{
		this.uuid = uuid;
		this.inetAddr = null;
	}
	
	public InetAddress getInetAddr()
	{
		return this.inetAddr;
	}
}
