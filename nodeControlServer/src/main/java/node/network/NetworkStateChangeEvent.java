package node.network;

import java.util.Map;

public class NetworkStateChangeEvent {
	public static final int STATE_UPLINK = 0;
	public static final int STATE_FAIL = 1;
	
	public final Map<String, String> ipTable;
	public final boolean isDHCP;
	
	public NetworkStateChangeEvent(Map<String, String> ipTable, boolean isDHCP) {
		this.ipTable = ipTable;
		this.isDHCP = isDHCP;
	}
}
