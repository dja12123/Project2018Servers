package node.cluster;

import node.device.Device;

public class NodeInfoChangeEvent {
	public static final int DEVICE_CONNECTED = 0;
	public static final int DEVICE_DISCONNECTED = 1;
	
	public final Device deviceInfo;
	
	public NodeInfoChangeEvent(Device deviceInfo)
	{
		this.deviceInfo = deviceInfo;
	}
	
	
}
