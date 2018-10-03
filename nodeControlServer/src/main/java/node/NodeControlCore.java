package node;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import node.cluster.spark.SparkManager;
import node.cluster.zookeeper.ZookeeperServerManager;
import node.db.DB_Handler;
import node.device.DeviceInfo;
import node.network.DHCPService;
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
	private final DHCPService dhcp;
	private final DeviceInfo deviceInfo;
	private final SparkManager sparkManager;
	private final ZookeeperServerManager zookeeperServerManager;
	
	public NodeControlCore()
	{
		this.dbHandler = new DB_Handler();
		this.dhcp = new DHCPService();
		this.deviceInfo = new DeviceInfo(this.dbHandler);
		this.sparkManager = new SparkManager();
		this.zookeeperServerManager = new ZookeeperServerManager();
		
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
			if(!this.zookeeperServerManager.startModule()) throw new Exception("주키퍼 모듈 로드 실패");
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