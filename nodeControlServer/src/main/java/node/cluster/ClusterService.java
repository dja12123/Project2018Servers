package node.cluster;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import node.IServiceModule;
import node.db.DB_Handler;
import node.log.LogWriter;
import node.network.NetworkStateChangeEvent;
import node.network.NetworkManager;

public class ClusterService implements IServiceModule {
	
	private boolean isMaster = false;
	private String masterIp;
	private boolean connectState;
	
	public final NetworkStateChangeEventReceiver nscEventReceiver = new NetworkStateChangeEventReceiver();
	public final NodeInfoChangeEventSender nicEventSender = new NodeInfoChangeEventSender();
	public final NetworkManager networkManager;
	public static final Logger clusterLogger = LogWriter.createLogger(ClusterService.class, "cluster");

	
	public ClusterService(NetworkManager networkManager) { this.networkManager = networkManager;	}
	
	public boolean cmpMaster() {
		try {
			if(masterIp.equals(InetAddress.getLocalHost().getHostAddress())) {
				return true;
			}else {
				return false;
			}
		}catch(UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	@Override
	public boolean startModule() {
		// TODO Auto-generated method stub
		NetworkStateChangeEvent eventInfo = null;
		
		if((eventInfo = nscEventReceiver.getEvent()) == null) {
			clusterLogger.log(Level.SEVERE, "Not Given Network State Change Event", new Exception("Not Given Network State Change Event"));
			return false;
		}
		if(networkManager == null) {
			clusterLogger.log(Level.SEVERE, "Not Given Network Manager", new Exception("Not Given Network Manager"));
			return false;
		}
		 
		this.isMaster = eventInfo.isDHCP;
		this.masterIp = eventInfo.DHCPIp;
		this.connectState = eventInfo.state;
		
		return true;
	}

	@Override
	public void stopModule() {
		// TODO Auto-generated method stub

		
		
	}

}