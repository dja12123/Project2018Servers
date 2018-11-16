package node.cluster.spark;

import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.NodeControlCore;
import node.bash.BashSet;
import node.cluster.ClusterService;
import node.log.LogWriter;

public class SparkManager {
	private String sparkInstDir;
	
	private String sparkPort;
	private String sparkWebPort;
	
	private String sparkWorkerInstances;
	private String sparkWorkerCores;
	private String sparkWorkerMemory;
	
	public static final Logger sparkLogger = LogWriter.createLogger(SparkManager.class, "spark");
	
	public SparkManager() {
		
	}
	public void startSparkMaster(String masterIP, String option) {
		sparkLogger.log(Level.INFO, "스파크 마스터 시작");
		StringBuffer confOp = new StringBuffer("-h ")
				.append(masterIP)
				.append(" -p ")
				.append(sparkPort)
				.append(" --webui-port ")
				.append(sparkWebPort);
		BashSet.execSh(BashSet.start_spkMaster, confOp.toString(), option);
	}
	public void stopSparkMaster() {
		sparkLogger.log(Level.INFO, "스파크 마스터 중지");
		BashSet.execSh(BashSet.stop_spkMaster);
	}
	//option example spark://worker-11:7077 -m 512M -c 2
	public void startSparkWorker(String masterIp, String option) {
		sparkLogger.log(Level.INFO, "스파크 워커 시작");
		StringBuffer confOp = new StringBuffer("spark://")
				.append(masterIp)
				.append(":")
				.append(sparkPort)
				.append(" -m ")
				.append(sparkWorkerMemory)
				.append(" -c ")
				.append(sparkWorkerCores);
		
		BashSet.execSh(BashSet.start_spkMaster, confOp.toString(), option);
	}
	public void stopSparkWorker() {
		sparkLogger.log(Level.INFO, "스파크 워커 중지");
		BashSet.execSh(BashSet.stop_spkWorker);
	}
	
	protected void confSpark() {
		sparkLogger.log(Level.INFO, "스파크 property 설정중..");
		sparkInstDir = NodeControlCore.getProp("sparkInstDir");
		
		sparkPort = NodeControlCore.getProp("sparkPort");
		sparkWebPort = NodeControlCore.getProp("sparkWebPort");
		
		sparkWorkerInstances = NodeControlCore.getProp("sparkWorkerInstances");
		sparkWorkerCores = NodeControlCore.getProp("sparkWorkerCores");
		sparkWorkerMemory = NodeControlCore.getProp("sparkWorkerMemory");
		
		
	}
	
	
	public void instSpark() {
		sparkLogger.log(Level.INFO, "스파크 설치중..");
		confSpark();
		//BashSet.execSh(BashSet.install_spark, sparkInstDir);
	}
}
