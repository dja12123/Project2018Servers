package node.device;

public class DeviceChangeEvent
{
	public static final int CONNECT_NEW_DEVICE = 0b1;
	public static final int DISCONNECT_DEVICE = 0b10;
	public static final int CHANGE_INETADDR = 0b100;
	public static final int IS_MASTER_NODE = 0b10000;
	public static final int IS_NOT_MASTER_NODE = 0b100000;
	
	private final int state;
	public final Device device;
	
	public DeviceChangeEvent(int state, Device device)
	{
		this.state = state;
		this.device = device;
	}
	
	public boolean getState(int state)
	{
		if((this.state & state) != 0)
		{
			return true;
		}
		return false;
	}
}
