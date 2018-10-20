package node.device;

public class DeviceStateChangeEvent
{
	public static final int CONNECT_NEW_DEVICE = 0b01;
	public static final int DISCONNECT_DEVICE = 0b001;
	public static final int CHANGE_INETADDR = 0b0001;
	public static final int CHANGE_DHCPNODE = 0b00001;
	
	public final int state;
	public final Device device;
	
	public DeviceStateChangeEvent(int state, Device device)
	{
		this.state = state;
		this.device = device;
	}
}
