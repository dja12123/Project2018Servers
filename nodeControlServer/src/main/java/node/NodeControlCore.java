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

import node.db.DB_Handler;
import node.device.DeviceInfo;
import node.network.DHCPService;

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
	public static final String logFormat = "[%1$tT][%2$s][%3$s] %4$s %5$s %n";// 로그 포맷
	public static final Logger mainLogger = createLogger(NodeControlCore.class, "main");// 메인 로거
	
	private static final Properties properties = new Properties();
	
	private final DB_Handler dbHandler;
	private final DHCPService dhcp;
	private final DeviceInfo deviceInfo;
	
	public NodeControlCore()
	{
		this.dbHandler = new DB_Handler();
		this.dhcp = new DHCPService();
		this.deviceInfo = new DeviceInfo(this.dbHandler);
		
		this.startService();
	}
    
    public static void main(String[] args) throws InterruptedException
	{
		Logger.getGlobal().setLevel(Level.FINER);
		
		mainLogger.log(Level.INFO, "서버 시작");
		
		try
		{
			properties.load(NodeControlCore.class.getResourceAsStream("/config.properties"));
		}
		catch (IOException e)
		{
			mainLogger.log(Level.SEVERE, "config 로드 실패", e);
		}
		
		NodeControlCore core = new NodeControlCore();
	}
	
	private void startService()
	{
		try
		{
			if(!this.dbHandler.startModule()) throw new Exception("DB핸들러 로드 실패");
			if(!this.deviceInfo.startModule()) throw new Exception("장치 정보 모듈 로드 실패");
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
		this.deviceInfo.stopModule();
		mainLogger.log(Level.INFO, "서비스 중지");
	}

	
	
	public static String getProp(String key)
	{
		return properties.getProperty(key);
	}
	
	public static void initLogger(Logger logger, String loggerName)
	{// 로그 초기화 기능
	 // 다른 모듈에서 기존에 사용하던 logger이 있을경우 우리 시스템 메인 로거에 등록함.
		logger.setUseParentHandlers(false);
		ConsoleHandler handler = new ConsoleHandler();

		handler.setFormatter(new SimpleFormatter()
		{
			@Override
			public synchronized String format(LogRecord lr)
			{
				String errMsg;
				Throwable throwable = lr.getThrown();
				if (throwable == null)
					errMsg = "";
				else
				{
					StringWriter sw = new StringWriter();
					sw.write(throwable.getLocalizedMessage());
					sw.write("=>\n");
					PrintWriter pw = new PrintWriter(sw);
					throwable.printStackTrace(pw);
					errMsg = sw.toString();
				}

				return String.format(logFormat, new Date(lr.getMillis()), lr.getLevel().getLocalizedName(), loggerName,
						lr.getMessage(), errMsg);
			}
		});
		logger.addHandler(handler);
	}

	public static Logger createLogger(Class<?> module, String loggerName)
	{// 해당 모듈에서 사용할 로거를 만들고 초기화.
		Logger logger = Logger.getLogger(module.getName().toLowerCase());
		initLogger(logger, loggerName);
		return logger;
	}
}