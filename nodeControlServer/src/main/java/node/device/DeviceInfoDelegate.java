package node.device;

import java.net.InetAddress;
import java.util.Date;
import java.util.UUID;

import node.NodeControlCore;

public class DeviceInfoDelegate
{
	public final DeviceInfoManager deviceInfoManager;
	
	public DeviceInfoDelegate(DeviceInfoManager deviceInfoManager)
	{
		this.deviceInfoManager = deviceInfoManager;
	}
	
	public void updateDevice(UUID uuid, InetAddress addr, boolean isDhcpNode)
	{
		Device device = this.deviceInfoManager.deviceMap.getOrDefault(uuid, null);
		if(device == null)
		{
			device = new Device(uuid);
			deviceInfoManager.deviceMap.put(uuid, device);
			DeviceStateChangeEvent eventObj = new DeviceStateChangeEvent(DeviceStateChangeEvent.CONNECT_NEW_DEVICE, device);
			this.deviceInfoManager.notifyObservers(NodeControlCore.mainThreadPool, eventObj);
		}
		else
		{
			int changeState = 0;
			if(!device.inetAddr.equals(addr))
			{
				changeState = changeState | DeviceStateChangeEvent.CHANGE_INETADDR;
				device.inetAddr = addr;
			}
			
			if(device.dhcpNode != isDhcpNode)
			{
				changeState = changeState | DeviceStateChangeEvent.CHANGE_DHCPNODE;
			}
			
			if(changeState != 0)
			{
				DeviceStateChangeEvent eventObj = new DeviceStateChangeEvent(changeState, device);
				this.deviceInfoManager.notifyObservers(NodeControlCore.mainThreadPool, eventObj);
			}
			
		}
		device.updateTime = new Date(System.currentTimeMillis());
	}
	
	public void removeDevice(UUID uuid)
	{
		this.deviceInfoManager.deviceMap.remove(uuid);
		DeviceStateChangeEvent eventObj = new DeviceStateChangeEvent(DeviceStateChangeEvent.DISCONNECT_DEVICE, this.deviceInfoManager.getDevice(uuid));
		this.deviceInfoManager.notifyObservers(NodeControlCore.mainThreadPool, eventObj);
	}
}
