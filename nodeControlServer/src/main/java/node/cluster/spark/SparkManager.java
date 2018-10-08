package node.cluster.spark;

import node.bash.BashSet;

public class SparkManager {
	
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
	
	public static void instSpark() {
		BashSet.execSh(BashSet.install_spark);
	}
}
