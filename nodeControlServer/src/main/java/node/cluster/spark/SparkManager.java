package node.cluster.spark;

import java.io.File;
import java.util.ArrayList;

import node.bash.CommandExecutor;
import node.fileIO.FileHandler;

public class SparkManager {

	public static final File start_spkMaster = FileHandler.getResourceFile("extResources/Shscript/start_spkMaster.sh");
	public static final File stop_spkMaster = FileHandler.getResourceFile("extResources/Shscript/stop_spkMaster.sh");
	public static final File start_spkWorker = FileHandler.getResourceFile("extResources/Shscript/start_spkWorker.sh");
	public static final File stop_spkWorker = FileHandler.getResourceFile("extResources/Shscript/stop_spkWorker.sh");
	public static final File start_zkServer = FileHandler.getResourceFile("extResources/Shscript/start_zkServer.sh");
	public static final File stop_zkServer = FileHandler.getResourceFile("extResources/Shscript/stop_zkServer.sh");
	
	public static void exec(String cmd) {
		//현재 노드에서 주키퍼 서버 실행
		ArrayList<String> tempSh = new ArrayList<String>();
		
		tempSh.add(cmd);
		
		try {
			CommandExecutor.executeBash(tempSh);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
