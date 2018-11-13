package node.detection;

import java.net.InetAddress;

public class NodeDetectionEvent {
	public static final int STATE_UPLINK = 0;
	public static final int STATE_FAIL = 1;
	
	public final InetAddress masterIP;
	public final boolean isMaster;
	public final int state;
	
	public NodeDetectionEvent(InetAddress masterIP, boolean isMaster, int what) {
		this.masterIP = masterIP;
		this.isMaster = isMaster;
		this.state = what;
	}
}
