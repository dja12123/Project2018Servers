package node.device;

import java.net.InetAddress;
import java.util.Date;
import java.util.UUID;

public class Device
{
	public final UUID uuid;
	InetAddress inetAddr;
	Date updateTime;
	boolean dhcpNode;
	//private String deviceName;
	
	Device(UUID uuid)
	{
		this.uuid = uuid;
		this.inetAddr = null;
		this.dhcpNode = false;
	}
	
	public InetAddress getInetAddr()
	{// 장치의 IP를 가져옵니다.
		return this.inetAddr;
	}
	
	public Date getUpdateTime()
	{// 장치가 최종적으로 확인된 시간을 가지고옵니다.
		return this.updateTime;
	}
	
	public boolean isDHCPNode()
	{
		return this.dhcpNode;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(!(obj instanceof Device)) return false;
		
		Device compDevice = (Device)obj;
		
		if(!compDevice.uuid.equals(this.uuid)) return false;
		return true;
	}
}
