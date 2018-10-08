package node.cluster.zookeeper;

import node.bash.BashSet;

public class ZookeeperServerManager {
	private int isMaster;
	private int zookeeperPort;
	
	
	public ZookeeperServerManager(int isMaster) {
		this.isMaster = isMaster;
	}
	
	public void setIsMaster(int isMaster)	{	this.isMaster = isMaster;	}
	public int getIsMaster()				{	return this.isMaster;	}
	
	public void setZkServer(String arg) throws Exception {
		if (isMaster != 1) throw new Exception("This Node is not Master");
		BashSet.execSh(BashSet.set_zkServer, arg);
	}
	
	public static void instZookeeper() {
		BashSet.execSh(BashSet.install_zookeeper);
	}
}
