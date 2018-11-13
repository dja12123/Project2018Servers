package node.bash;

import java.io.File;
import java.util.ArrayList;

import node.fileIO.FileHandler;

public class BashSet {
	//sh 스크립트 추가 하실때 여기에 파일 상수 하나 추가해서 쓰세요
	public static final File start_spkMaster = FileHandler.getExtResourceFile("Shscript/start_spkMaster.sh");
	public static final File stop_spkMaster = FileHandler.getExtResourceFile("Shscript/stop_spkMaster.sh");
	public static final File start_spkWorker = FileHandler.getExtResourceFile("Shscript/start_spkWorker.sh");
	public static final File stop_spkWorker = FileHandler.getExtResourceFile("Shscript/stop_spkWorker.sh");
	public static final File set_zkServer = FileHandler.getExtResourceFile("Shscript/set_zkServer.sh");
	public static final File install_spark = FileHandler.getExtResourceFile("Shscript/install_spark.sh");
	public static final File install_zookeeper = FileHandler.getExtResourceFile("Shscript/install_zookeeper.sh");
	
	//파일상수, 매개변수(옵션)1, 매개변수(옵션)2,... 이런식으로 사용
	public static void execSh(File shFile, String... arg) {
		//현재 노드에서 주키퍼 서버 실행
		StringBuffer cmdline = new StringBuffer("bash ");
		
		cmdline.append(shFile.getAbsolutePath());
		for(int i = 0; i < arg.length; i++) {
			cmdline.append(" " + arg[i]);
		}
			
		try {
			CommandExecutor.executeCommand(cmdline.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
