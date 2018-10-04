package node;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import node.cluster.ClusterService;
import node.db.DB_Handler;
import node.device.DeviceInfo;
import node.network.DHCPService;
import node.network.NetworkManager;
import node.log.LogWriter;

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

	private static final Properties properties = new Properties();
	
	private final DB_Handler dbHandler;
	private final NetworkManager networkManager;
	private final DHCPService dhcp;
	private final DeviceInfo deviceInfo;
	private final ClusterService clusterService;
	
	public NodeControlCore()
	{
		this.dbHandler = new DB_Handler();
		this.networkManager = new NetworkManager();
		this.dhcp = new DHCPService();
		this.deviceInfo = new DeviceInfo(this.dbHandler);
		this.clusterService = new ClusterService(networkManager);
		
		this.startService();
	}
    
    public static void main(String[] args) throws InterruptedException
	{
		Logger.getGlobal().setLevel(Level.FINER);
		
		LogWriter.mainLogger.log(Level.INFO, "서버 시작");
		
		try
		{
			properties.load(NodeControlCore.class.getResourceAsStream("/config.properties"));
		}
		catch (IOException e)
		{
			LogWriter.mainLogger.log(Level.SEVERE, "config 로드 실패", e);
		}
		
		NodeControlCore core = new NodeControlCore();
	}
	
	private void startService()
	{
		try
		{
			if(!this.dbHandler.startModule()) throw new Exception("DB핸들러 로드 실패");
			if(!this.deviceInfo.startModule()) throw new Exception("장치 정보 모듈 로드 실패");
			if(!this.clusterService.startModule()) throw new Exception("스파크 모듈 로드 실패");
		}
		catch(Exception e)
		{
			LogWriter.mainLogger.log(Level.SEVERE, "서비스 시작중 오류", e);
			this.stopService();
			return;
		}
		LogWriter.mainLogger.log(Level.INFO, "서비스 시작 완료");
	}
	
	private void stopService()
	{
		this.dbHandler.stopModule();
		this.deviceInfo.stopModule();
		LogWriter.mainLogger.log(Level.INFO, "서비스 중지");
	}

	
	
	public static String getProp(String key)
	{
		return properties.getProperty(key);
	}
	
}