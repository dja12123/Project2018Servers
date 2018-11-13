package node.cluster;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import node.IServiceModule;
import node.cluster.spark.SparkManager;
import node.db.DB_Handler;
import node.log.LogWriter;
import node.detection.NetworkStateChangeEvent;
import node.detection.NodeDetectionService;
import node.network.NetworkManager;

public class ClusterService implements IServiceModule {
	public static final int SPARK_INSTALLED = 0;
	public static final int SPARK_NOT_INSTALLED = 1;
	
	private boolean isMaster;
	private String masterIp;
	private int connectState;
	private int instFlag;
	
	public final NetworkStateChangeEventReceiver nscEventReceiver = new NetworkStateChangeEventReceiver(this);
	public final NodeInfoChangeEventSender nicEventSender = new NodeInfoChangeEventSender();
	public final SparkManager sparkManager = new SparkManager();
	public final NodeDetectionService nds;
	
	public static final Logger clusterLogger = LogWriter.createLogger(ClusterService.class, "cluster");
	
	public ClusterService(NodeDetectionService nds) {
		this.isMaster = false;
		this.instFlag = SPARK_NOT_INSTALLED;
		this.connectState = NetworkStateChangeEvent.STATE_FAIL;
		this.nds = nds;
		nds.addObserver(nscEventReceiver);
		this.masterIp = null;
		
		instSpark();
	}
	public void instSpark() {
		sparkManager.instSpark();
		instFlag = SPARK_INSTALLED;
	}
	
	public boolean cmpMaster() {
		try {
			if(masterIp.equals(InetAddress.getLocalHost().getHostAddress())) {
				return true;
			} else {
				return false;
			}
		} catch(UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	public boolean reciveEvent(NetworkStateChangeEvent eventInfo) {
		if(eventInfo == null) {
			clusterLogger.log(Level.SEVERE, "Not Given Network State Change Event", new Exception("Not Given Network State Change Event"));
			return false;
		}
		if(masterIp != null && !masterIp.equals(eventInfo.DHCPIp)) {	//마스터가 아니였다가 마스터가 될때 마스터프로세스를 종료시켜준다.(잔존 프로세스 제거)
			sparkManager.stopSparkMaster();
			sparkManager.stopSparkWorker();
		}
		this.isMaster = eventInfo.isDHCP;
		this.masterIp = eventInfo.DHCPIp;
		this.connectState = eventInfo.state;
		
		return true;
		
	}
	public boolean startSpark() {		//스파크의 서버(마스터일때만) 와 워커를 실행
		
		if(this.instFlag == SPARK_NOT_INSTALLED) {
			clusterLogger.log(Level.SEVERE, "Not Installed Spark", new Exception("Not Installed Spark"));
			
			return false;
		}
		
		if(isMaster == true)	{
			sparkManager.startSparkMaster(masterIp, "");
		}
		sparkManager.startSparkWorker(masterIp, "");
		return true;
	}
	
	
	@Override
	public boolean startModule() {		//객체 초기화 생성및 쓰레드 초기화 생성
		// TODO Auto-generated method stub
		this.nds.addObserver(nscEventReceiver);
		
		return true;
	}

	@Override
	public void stopModule() {		//스타트 모듈에서 생성된 객체들을 다시 종료시키는곳.
		// TODO Auto-generated method stub
		
	}

}