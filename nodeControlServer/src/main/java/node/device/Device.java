package node.device;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class Device
{
	public final UUID uuid;
	private InetAddress inetAddr;
	private Date updateTime;
	//private String deviceName;
	
	Device(UUID uuid)
	{
		this.uuid = uuid;
		this.inetAddr = null;
	}
	
	void setInetAddr(InetAddress inetAddr)
	{
		this.inetAddr = inetAddr;
	}
	
	void updateTime()
	{
		this.updateTime = new Date(System.currentTimeMillis());
	}
	
	public InetAddress getInetAddr()
	{// 장치의 IP를 가져옵니다.
		return this.inetAddr;
	}
	
	public Date getUpdateTime()
	{// 장치가 최종적으로 확인된 시간을 가지고옵니다.
		return this.updateTime;
	}
}
