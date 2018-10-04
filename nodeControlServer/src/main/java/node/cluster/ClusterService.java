package node.cluster;

import java.util.HashMap;

import node.IServiceModule;
import node.network.NetworkConnectEvent;
import node.network.NetworkManager;

public class ClusterService implements IServiceModule {
	
	private HashMap<String, String> ipTable = null; 
	private String masterName = null , masterIp = null;
	public final NetworkConnectEventReceiver ncReceiver = new NetworkConnectEventReceiver();
	public final NodeInfoChangeEventSender nicSender = new NodeInfoChangeEventSender();
	public final NetworkManager networkManager;
	
	public ClusterService(NetworkManager networkManager) { this.networkManager = networkManager;	}
	
	public boolean putData(String name, String ip) {
		// 해쉬맵에 넣는대신 안에 데이터와 겹치는게 없으면 true 아니면 false
		if(null == this.ipTable.put(name, ip))
			return true;
		else
			return false;
	}
	public String getData(String name) {
		return this.ipTable.get(name);
	}
	
	public void delRow(String key) {
		ipTable.remove(key);
	}
	
	@Override
	public boolean startModule() {
		// TODO Auto-generated method stub
		NetworkConnectEvent eventInfo = null;
		
		if((eventInfo = ncReceiver.getEvent()) == null) return false;
		if(networkManager == null) return false;
		
		this.masterIp = eventInfo.dhcpIp;
		this.masterName = eventInfo.dhcpName;
		this.ipTable = (HashMap<String, String>) eventInfo.ipTable;
		
		return true;
	}

	@Override
	public void stopModule() {
		// TODO Auto-generated method stub

		
		
	}

}
