package node.cluster;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.IServiceModule;
import node.db.DB_Handler;
import node.log.LogWriter;
import node.network.NetworkStateChangeEvent;
import node.network.NetworkManager;

public class ClusterService implements IServiceModule {
	
	private HashMap<String, String> ipTable = null; 
	private boolean isMaster = false;
	
	public final NetworkStateChangeEventReceiver nscEventReceiver = new NetworkStateChangeEventReceiver();
	public final NodeInfoChangeEventSender nicEventSender = new NodeInfoChangeEventSender();
	public final NetworkManager networkManager;
	public static final Logger clusterLogger = LogWriter.createLogger(ClusterService.class, "cluster");

	
	public ClusterService(NetworkManager networkManager) { this.networkManager = networkManager;	}
	
	public boolean putData(String name, String ip) {
		// 해쉬맵에 넣는대신 안에 데이터와 겹치는게 없으면 true 아니면 false
		if(null == this.ipTable.put(name, ip))
			return true;
		else
			return false;
	}
	public String getData(String name) {
		return this.ipTable.get(name);
	}
	
	public void delRow(String key) {
		ipTable.remove(key);
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
		this.ipTable = (HashMap<String, String>) eventInfo.ipTable;
		
		return true;
	}

	@Override
	public void stopModule() {
		// TODO Auto-generated method stub

		
		
	}

}