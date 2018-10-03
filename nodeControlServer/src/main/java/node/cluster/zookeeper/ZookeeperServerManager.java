package node.cluster.zookeeper;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import node.IServiceModule;
import node.bash.CommandExecutor;

public class ZookeeperServerManager{

	public void startZookeeper() {
		//현재 노드에서 주키퍼 서버 실행
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
		//현재 노드에서 주키퍼 서버 종료
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
	
}
