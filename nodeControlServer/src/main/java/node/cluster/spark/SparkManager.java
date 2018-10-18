package node.cluster.spark;

import java.util.HashMap;

import node.bash.BashSet;
import node.cluster.ClusterService;

public class SparkManager {
	private int sparkWorkerInstances;
	private int sparkWorkerCores;
	private int sparkPort;
	private int sparkWebPort;
	private String sparkWorkerMemory;
	private String sparkDeployRecoveryMode;
	private String sparkDeployZookeeperUrl;
	
	public SparkManager() {
	}
	public void startSparkMaster() {
		BashSet.execSh(BashSet.start_spkMaster);
	}
	public void stopSparkMaster() {
		BashSet.execSh(BashSet.stop_spkMaster);
	}
	//option example spark://worker-11:7077 -m 512M -c 2
	public void startSparkWorker(String option) {
		BashSet.execSh(BashSet.start_spkWorker, option);
	}
	public void stopSparkWorker() {
		BashSet.execSh(BashSet.stop_spkWorker);
	}
	
	protected void confSpark() {
		
	}
	
	
	public void instSpark() {
		BashSet.execSh(BashSet.install_spark);
	}
}
