package node.device;

public class DeviceStateChangeEvent
{
	public static final int CONNECT_NEW_DEVICE = 0b01;
	public static final int DISCONNECT_DEVICE = 0b001;
	public static final int CHANGE_INETADDR = 0b0001;
	public static final int IS_MASTER_NODE = 0b00001;
	public static final int IS_NOT_MASTER_NODE = 0b000001;
	
	private final int state;
	public final Device device;
	
	public DeviceStateChangeEvent(int state, Device device)
	{
		this.state = state;
		this.device = device;
	}
	
	public boolean getState(int state)
	{
		if((this.state & state) == 1)
		{
			return true;
		}
		return false;
	}
}
