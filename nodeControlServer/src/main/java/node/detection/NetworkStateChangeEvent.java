package node.detection;

public class NetworkStateChangeEvent {
	public static final int STATE_UPLINK = 0;
	public static final int STATE_FAIL = 1;
	
	public final boolean isDHCP;
	public final String DHCPIp;
	public final int state;
	
	public NetworkStateChangeEvent(String DHCPIp, boolean isDHCP, int what) {
		this.DHCPIp = DHCPIp;
		this.isDHCP = isDHCP;
		this.state = what;
	}
}
