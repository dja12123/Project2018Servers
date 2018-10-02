package node.cluster.zookeeper;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import node.bash.CommandExecutor;

public class ZookeeperServerManager {

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
		try {
			String path = ZookeeperServerManager.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			String decodedPath = URLDecoder.decode(path, "UTF-8");
			
			System.out.println(decodedPath);
			
			System.out.println("Current Working Directory = " + System.getProperty("user.dir"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
}
