package node.cluster.zookeeper;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import node.IServiceModule;
import node.bash.CommandExecutor;

public class ZookeeperServerManager implements IServiceModule{

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
	public void startZookeeper() {
		//내부bash파일이용해서 실행시키는 방법 알아내기
		String start_zkServer = System.getProperty("user.dir") + "resources/Shscript/start_zkServer.sh";
		ArrayList<String> tempSh = new ArrayList<String>();
		
		tempSh.add(start_zkServer);
		
		try {
			CommandExecutor.executeBash(tempSh);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void stopZookeeper() {
		//내부bash파일이용해서 실행시키는 방법 알아내기
		String stop_zkServer = System.getProperty("user.dir") + "resources/Shscript/stop_zkServer.sh";
		ArrayList<String> tempSh = new ArrayList<String>();
		
		tempSh.add(stop_zkServer);
		
		try {
			CommandExecutor.executeBash(tempSh);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
