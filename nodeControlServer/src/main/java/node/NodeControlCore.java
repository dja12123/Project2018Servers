package node;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.cluster.ClusterService;
import node.db.DB_Handler;
import node.device.DeviceInfoManager;
import node.log.LogWriter;
import node.network.DHCPService;
import node.network.NetworkManager;

/**
  * @FileName : NodeControlCore.java
  * @Project : Project2018Servers
  * @Date : 2018. 9. 23. 
  * @작성자 : dja12123
  * @변경이력 :
  * @프로그램 설명 : 전체 모듈의 시작과 종료를 관리.
  */
public class NodeControlCore
{

	public static final Logger mainLogger = LogWriter.createLogger(NodeControlCore.class, "main");// 메인 로거
	
	private static final Properties properties = new Properties();
	
	private final DB_Handler dbHandler;
	private final NetworkManager networkManager;
	private final DHCPService dhcp;
	private final DeviceInfoManager deviceInfoManager;
	private final ClusterService clusterService;
	
	public NodeControlCore()
	{
		this.dbHandler = new DB_Handler();
		this.networkManager = new NetworkManager();
		this.dhcp = new DHCPService();
		this.deviceInfoManager = new DeviceInfoManager(this.dbHandler);
		this.clusterService = new ClusterService(networkManager);
	}
    
    public static void main(String[] args) throws InterruptedException
	{
    	init();
		NodeControlCore core = new NodeControlCore();
		core.startService();
	}
    
    public static void init()
	{
		Logger.getGlobal().setLevel(Level.FINER);
		
		mainLogger.log(Level.INFO, "서버 시작");
		
		try
		{
			InputStream stream = NodeControlCore.class.getResourceAsStream("/config.properties");
            
			properties.load(stream);
		}
		catch (Exception e)
		{
			mainLogger.log(Level.SEVERE, "config 로드 실패", e);
			return;
		}
	}
	
	private void startService()
	{
		try
		{
			if(!this.dbHandler.startModule()) throw new Exception("DB핸들러 로드 실패");
			if(!this.deviceInfoManager.startModule()) throw new Exception("장치 정보 모듈 로드 실패");
			if(!this.clusterService.startModule()) throw new Exception("스파크 모듈 로드 실패");
		}
		catch(Exception e)
		{
			mainLogger.log(Level.SEVERE, "서비스 시작중 오류", e);
			this.stopService();
			return;
		}
		mainLogger.log(Level.INFO, "서비스 시작 완료");
	}
	
	private void stopService()
	{
		this.dbHandler.stopModule();
		this.deviceInfoManager.stopModule();
		mainLogger.log(Level.INFO, "서비스 중지");
	}

	
	
	public static String getProp(String key)
	{
		return properties.getProperty(key);
	}
	
}