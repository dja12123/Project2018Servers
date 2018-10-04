package node.cluster;

import node.device.DeviceInfo;

public class NodeInfoChangeEvent {
	public static final int DEVICE_CONNECTED = 0;
	public static final int DEVICE_DISCONNECTED = 1;
	
	public final DeviceInfo deviceInfo;
	
	public NodeInfoChangeEvent(DeviceInfo deviceInfo)
	{
		this.deviceInfo = deviceInfo;
	}
	
	
}
