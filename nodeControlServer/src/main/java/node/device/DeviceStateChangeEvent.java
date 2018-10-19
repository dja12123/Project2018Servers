package node.device;

public class DeviceStateChangeEvent
{
	public static final int CONNECT_NEW_DEVICE = 0;
	public static final int DISCONNECT_DEVICE = 1;
	
	public final int state;
	public final Device device;
	
	DeviceStateChangeEvent(int state, Device device)
	{
		this.state = state;
		this.device = device;
	}
}
