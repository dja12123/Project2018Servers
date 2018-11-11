package node;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.bash.CommandExecutor;
import node.cluster.ClusterService;
import node.db.DB_Handler;
import node.detection.NodeDetectionService;
import node.device.DeviceInfoManager;
import node.fileIO.FileHandler;
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

	public static final Logger logger = LogWriter.createLogger(NodeControlCore.class, "main");// 메인 로거
	
	public static final ExecutorService mainThreadPool = Executors.newCachedThreadPool();
	
	private static final Properties properties = new Properties();
	
	private final DB_Handler dbHandler;
	private final NetworkManager networkManager;
	private final DHCPService dhcp;
	private final DeviceInfoManager deviceInfoManager;
	private final NodeDetectionService nodeDetectionService;
	private final ClusterService clusterService;
	
	public NodeControlCore()
	{
		this.dbHandler = new DB_Handler();
		this.deviceInfoManager = new DeviceInfoManager(this.dbHandler);
		this.networkManager = new NetworkManager(this.deviceInfoManager);
		this.dhcp = new DHCPService();
		this.nodeDetectionService = new NodeDetectionService(this.dbHandler, this.deviceInfoManager, this.networkManager);
		this.clusterService = new ClusterService(this.nodeDetectionService);
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
		
		logger.log(Level.INFO, "서버 시작");
		
		//CONFIG 로드 부분
		try
		{
			InputStream stream = FileHandler.getResourceAsStream("/config.properties");
            
			properties.load(stream);
		}
		catch (Exception e)
		{
			logger.log(Level.SEVERE, "config 로드 실패", e);
			return;
		}
		logger.log(Level.INFO, "config 로드");
		
		//환경 변수 설정 부분
		try
		{
			String javaHome = CommandExecutor.executeCommandResult("echo $JAVA_HOME");
			if(javaHome.equals(""))
			{// 환경 변수가 설정되지 않았을경우
				logger.log(Level.INFO, "환경변수(JAVA_HOME) 설정");
			}
		}
		catch (Exception e)
		{
			logger.log(Level.SEVERE, "환경변수 변경 명령 실행중 오류", e);
			return;
		}
		
		
		//JNI링크 부분
		File rawSocketLib = FileHandler.getExtResourceFile("rawsocket");
		StringBuffer libPathBuffer = new StringBuffer();
		libPathBuffer.append(rawSocketLib.toString());
		libPathBuffer.append(":");
		libPathBuffer.append(System.getProperty("java.library.path"));
		
		System.setProperty("java.library.path", libPathBuffer.toString());
		Field sysPathsField = null;
		try
		{
			sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
			sysPathsField.setAccessible(true);
			sysPathsField.set(null, null);
		}
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1)
		{
			// TODO Auto-generated catch blsock
			logger.log(Level.SEVERE, "JNI 라이브러리 폴더 링크 실패", e1);
			return;
		}
		System.loadLibrary("rocksaw");
		logger.log(Level.INFO, "JNI 라이브러리 로드");
		
	}
	
	private void startService()
	{
		try
		{
			if(!this.dbHandler.startModule()) throw new Exception("DB핸들러 로드 실패");
			if(!this.deviceInfoManager.startModule()) throw new Exception("노드 정보 모듈 로드 실패");
			if(!this.networkManager.startModule()) throw new Exception("네트워크 모듈 로드 실패");
			if(!this.nodeDetectionService.startModule()) throw new Exception("노드 감지 서비스 모듈 로드 실패");
			//if(!this.clusterService.startModule()) throw new Exception("스파크 모듈 로드 실패");
			
			this.dbHandler.getInstaller().complete();
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "서비스 시작중 오류", e);
			this.stopService();
			return;
		}
		logger.log(Level.INFO, "서비스 시작 완료");
	}
	
	private void stopService()
	{
		this.dbHandler.stopModule();
		this.deviceInfoManager.stopModule();
		logger.log(Level.INFO, "서비스 중지");
	}

	
	
	public static String getProp(String key)
	{
		return properties.getProperty(key);
	}
	
}