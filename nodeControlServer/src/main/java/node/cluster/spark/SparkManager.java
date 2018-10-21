package node.cluster.spark;

import java.util.HashMap;
import java.util.Properties;

import node.NodeControlCore;
import node.bash.BashSet;
import node.cluster.ClusterService;

public class SparkManager {
	private String sparkHome;
	
	private String sparkPort;
	private String sparkWebPort;
	
	private String sparkWorkerInstances;
	private String sparkWorkerCores;
	private String sparkWorkerMemory;
	
	public SparkManager() {
		
	}
	public void startSparkMaster(String masterIP, String option) {
		StringBuffer confOp = new StringBuffer("-h ")
				.append(masterIP)
				.append(" -p ")
				.append(sparkPort)
				.append(" --webui-port ")
				.append(sparkWebPort);
		BashSet.execSh(BashSet.start_spkMaster, confOp.toString(), option);
	}
	public void stopSparkMaster() {
		BashSet.execSh(BashSet.stop_spkMaster);
	}
	//option example spark://worker-11:7077 -m 512M -c 2
	public void startSparkWorker(String masterIp, String option) {
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
		BashSet.execSh(BashSet.stop_spkWorker);
	}
	
	protected void confSpark() {
		sparkHome = NodeControlCore.getProp("sparkHome");
		
		sparkPort = NodeControlCore.getProp("sparkPort");
		sparkWebPort = NodeControlCore.getProp("sparkWebPort");
		
		sparkWorkerInstances = NodeControlCore.getProp("sparkWorkerInstances");
		sparkWorkerCores = NodeControlCore.getProp("sparkWorkerCores");
		sparkWorkerMemory = NodeControlCore.getProp("sparkWorkerMemory");
		
		
	}
	
	
	public void instSpark() {
		confSpark();
		BashSet.execSh(BashSet.install_spark, sparkHome);
	}
}
