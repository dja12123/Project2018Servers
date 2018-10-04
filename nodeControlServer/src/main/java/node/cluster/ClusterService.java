package node.cluster;

import java.util.concurrent.ConcurrentHashMap;

import node.IServiceModule;

public class ClusterService implements IServiceModule {
	
	private ConcurrentHashMap<String, String> ipTable = new ConcurrentHashMap<String, String>(); 
	private NetworkConnectEventReceiver infoReceiver = new NetworkConnectEventReceiver();
	private NodeInfoChangeEventSender infoSender = new NodeInfoChangeEventSender();
	
	
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
		
		
		return false;
	}

	@Override
	public void stopModule() {
		// TODO Auto-generated method stub

		
		
	}

}
