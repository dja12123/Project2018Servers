package node.network;

import java.util.Map;

public class NetworkConnectEvent {
	public final Map<String, String> ipTable;
	public final String dhcpName, dhcpIp;
	
	public NetworkConnectEvent(Map<String, String> ipTable, String dhcpName, String dhcpIp) {
		this.ipTable = ipTable;
		this.dhcpName = dhcpName;
		this.dhcpIp = dhcpIp;
	}
}
