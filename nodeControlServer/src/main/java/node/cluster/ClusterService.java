package node.cluster;

import java.util.concurrent.ConcurrentHashMap;

import node.IServiceModule;
import node.util.observer.Observable;
import node.util.observer.Observer;

public class ClusterService extends Observable implements IServiceModule, Observer {
	
	private ConcurrentHashMap<String, String> IpTable = new ConcurrentHashMap<String, String>(); 
	
	public boolean putData(String name, String ip) {
		// 해쉬맵에 넣는대신 안에 데이터와 겹치는게 없으면 true 아니면 false
		if(null == this.IpTable.put(name, ip))
			return true;
		else
			return false;
	}
	public String getData(String name) {
		return this.IpTable.get(name);
	}
	
	public void delRow(String key) {
		IpTable.remove(key);
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
