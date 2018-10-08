package node.cluster;

import java.io.File;
import java.util.ArrayList;

import node.bash.CommandExecutor;
import node.fileIO.FileHandler;

public class BashSet {
	public static final File start_spkMaster = FileHandler.getResourceFile("extResources/Shscript/start_spkMaster.sh");
	public static final File stop_spkMaster = FileHandler.getResourceFile("extResources/Shscript/stop_spkMaster.sh");
	public static final File start_spkWorker = FileHandler.getResourceFile("extResources/Shscript/start_spkWorker.sh");
	public static final File stop_spkWorker = FileHandler.getResourceFile("extResources/Shscript/stop_spkWorker.sh");
	public static final File start_zkServer = FileHandler.getResourceFile("extResources/Shscript/start_zkServer.sh");
	public static final File stop_zkServer = FileHandler.getResourceFile("extResources/Shscript/stop_zkServer.sh");
	public static final File install_spark = FileHandler.getResourceFile("extResources/Shscript/install_spark.sh");
	public static final File install_zookeeper = FileHandler.getResourceFile("extResources/Shscript/install_zookeeper.sh");
	
	//파일상수, 매개변수(옵션)1, 매개변수(옵션)2,... 이런식으로 사용
	public static void execSh(File shFile, String... arg) {
		//현재 노드에서 주키퍼 서버 실행
		ArrayList<String> tempSh = new ArrayList<String>();
		
		StringBuffer cmdline = new StringBuffer(shFile.getAbsolutePath());
		for(int i = 0; i < arg.length; i++) {
			cmdline.append(" " + arg[i]);
		}
		
		tempSh.add(cmdline.toString());
		
		try {
			CommandExecutor.executeBash(tempSh);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
