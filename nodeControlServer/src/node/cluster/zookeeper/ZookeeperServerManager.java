package node.cluster.zookeeper;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ZookeeperServerManager {

	private ConcurrentHashMap<String, String> IpTable = new ConcurrentHashMap<String, String>(); 
	
	public boolean putData(String name, String ip) {
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
	public void startZookeeper() {
		//내부bash파일이용해서 실행시키는 방법 알아내기
	}
	
	
}
