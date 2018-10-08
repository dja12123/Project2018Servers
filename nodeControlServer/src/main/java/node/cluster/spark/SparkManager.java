package node.cluster.spark;

import java.util.HashMap;

import node.bash.BashSet;

public class SparkManager {
	private int sparkWorkerInstances;
	private int sparkWorkerCores;
	private int sparkPort;
	private int sparkWebPort;
	private String sparkWorkerMemory;
	private String sparkDeployRecoveryMode;
	private String sparkDeployZookeeperUrl;
	private HashMap<String, String> ipTable;
	
	public SparkManager(HashMap<String, String> ipTable) {
		this.ipTable = ipTable;
		
	}
	public void startSparkMaster(String option) {
		BashSet.execSh(BashSet.start_spkMaster, option);
	}
	public void stopSparkMaster() {
		BashSet.execSh(BashSet.stop_spkMaster);
	}
	public void startSparkWorker(String option) {
		BashSet.execSh(BashSet.start_spkWorker, option);
	}
	public void stopSparkWorker() {
		BashSet.execSh(BashSet.stop_spkWorker);
	}
	
	protected void confSpark() {
		
	}
	
	
	public static void instSpark() {
		BashSet.execSh(BashSet.install_spark);
	}
}
