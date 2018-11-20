package node.cluster;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import node.IServiceModule;
import node.bash.CommandExecutor;
import node.cluster.spark.SparkManager;
import node.log.LogWriter;
import node.util.observer.Observable;
import node.detection.NodeDetectionEvent;

public class ClusterService implements IServiceModule {
	public static final int SPARK_INSTALLED = 0;
	public static final int SPARK_NOT_INSTALLED = 1;
	
	private boolean isMaster;
	private String masterIp;
	private int connectState;
	private int instFlag;
	private boolean isWorkerRun;
	
	public final NodeDetectionEventReceiver ndEventReceiver = new NodeDetectionEventReceiver(this);
	public final NodeInfoChangeEventSender nicEventSender = new NodeInfoChangeEventSender();
	public final SparkManager sparkManager = new SparkManager();
	public final Observable<NodeDetectionEvent> nds;
	
	public static final Logger clusterLogger = LogWriter.createLogger(ClusterService.class, "cluster");
	
	public ClusterService(Observable<NodeDetectionEvent> nds) {
		this.isMaster = false;
		this.instFlag = SPARK_NOT_INSTALLED;
		this.connectState = NodeDetectionEvent.STATE_FAIL;
		this.nds = nds;
		this.masterIp = null;
		this.isWorkerRun = false;
	}
	public void instSpark() {
		clusterLogger.log(Level.INFO, "스파크 설치확인");
		if(sparkManager.initSpark()) {
			instFlag = SPARK_INSTALLED;
		}
	}

	public boolean reciveEvent(NodeDetectionEvent eventInfo) {
		
		if(eventInfo == null) {
			clusterLogger.log(Level.SEVERE, "Not Given Network State Change Event", new Exception("Not Given Network State Change Event"));
			return false;
		}
		clusterLogger.log(Level.INFO, "NodeDetectionEvent 이벤트 받음, Master IP : " + eventInfo.masterIP.getHostAddress() + "Is Master? : " + eventInfo.isMaster);
		this.connectState = eventInfo.state;
		
		if(this.masterIp == null || eventInfo.masterIP == null ) {	//마스터가 현재 없을때 모든 스파크 프로세스 중지
			sparkManager.stopSparkMaster();
			sparkManager.stopSparkWorker();
			isWorkerRun = false;
			
			//스파크 시작
			this.isMaster = eventInfo.isMaster;
			this.masterIp = eventInfo.masterIP.getHostAddress();
			startSpark();
		} else if( !this.masterIp.equals(eventInfo.masterIP.getHostAddress()) || this.isMaster != eventInfo.isMaster) { //마스터가 바뀔때 마스터, 워커 프로세스를 종료시켜준다.(잔존 프로세스 제거)
			sparkManager.stopSparkWorker();
			isWorkerRun = false;
			
			//스파크 시작
			this.isMaster = eventInfo.isMaster;
			this.masterIp = eventInfo.masterIP.getHostAddress();
			startSpark();
			
		}
		
		return true;
		
	}
	public boolean startSpark() {		//스파크의 서버(마스터일때만) 와 워커를 실행
		clusterLogger.log(Level.INFO, "스파크 시작");
		
		if(this.instFlag == SPARK_NOT_INSTALLED) {
			clusterLogger.log(Level.SEVERE, "Not Installed Spark", new Exception("Not Installed Spark"));
			
			return false;
		}
		
		if(isMaster == true)	{
			sparkManager.startSparkMaster(masterIp, "");
		}
		if(isWorkerRun == false) {
			sparkManager.startSparkWorker(masterIp, "");
			isWorkerRun = true;
		}
		nds.addObserver(ndEventReceiver);
		return true;
	}
	
	
	@Override
	public boolean startModule() {		//객체 초기화 생성및 쓰레드 초기화 생성
		// TODO Auto-generated method stub
		this.instSpark();
		if(this.instFlag == SPARK_NOT_INSTALLED) {
			clusterLogger.log(Level.SEVERE, "Not Installed Spark", new Exception("Not Installed Spark"));
			
			return false;
		}
		clusterLogger.log(Level.INFO, "Spark 설치 여부 : " + instFlag);
		
		return true;
	}

	@Override
	public void stopModule() {		//스타트 모듈에서 생성된 객체들을 다시 종료시키는곳.
		// TODO Auto-generated method stub
		
	}

}