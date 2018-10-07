package node.cluster.zookeeper;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import node.IServiceModule;
import node.bash.CommandExecutor;
import node.fileIO.FileHandler;

public class ZookeeperServerManager{

	public static final File start_zkServer = FileHandler.getResourceFile("extResources/Shscript/start_zkServer.sh");
	public static final File stop_zkServer = FileHandler.getResourceFile("extResources/Shscript/stop_zkServer.sh");
	
	public void startZookeeper() {
		//현재 노드에서 주키퍼 서버 실행
		ArrayList<String> tempSh = new ArrayList<String>();
		
		tempSh.add(start_zkServer.getAbsolutePath());
		
		try {
			CommandExecutor.executeBash(tempSh);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void stopZookeeper() {
		//현재 노드에서 주키퍼 서버 종료
		ArrayList<String> tempSh = new ArrayList<String>();
		
		tempSh.add(stop_zkServer.getAbsolutePath());
		
		try {
			CommandExecutor.executeBash(tempSh);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
