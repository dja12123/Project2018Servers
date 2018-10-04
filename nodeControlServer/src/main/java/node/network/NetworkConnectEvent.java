package node.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkConnectEvent {
	public final Map<String, String> ipTable = new ConcurrentHashMap<String, String>();
	public final StringBuffer nameDHCP, ipDHCP;
	
}
