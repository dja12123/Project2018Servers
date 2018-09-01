package kr.dja.project2018.node;

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

import kr.dja.project2018.node.db.DB_Handler;
import kr.dja.project2018.node.network.DHCPService;

/**
 * A simple DHCP sniffer based on DHCP servlets.
 *
 * @author Stephan Hadinger
 * @version 1.00
 */
public class NodeControlCore
{
	public static final String logFormat = "[%1$tT][%2$s][%3$s] %4$s %5$s %n";
	public static final Logger mainLogger = createLogger(NodeControlCore.class.getName().toLowerCase(), "main");
	
	private static final Properties properties = new Properties();
	
	private final DB_Handler dbHandler;
	private final DHCPService dhcp;
	
	public NodeControlCore()
	{
		this.dbHandler = new DB_Handler();
		this.dhcp = new DHCPService();
	}

	public static void main(String[] args) throws InterruptedException
	{
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
	
	public static String getProp(String key)
	{
		return properties.getProperty(key);
	}
	
	public static void initLogger(Logger logger, String loggerName)
	{
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

	public static Logger createLogger(String path, String loggerName)
	{
		Logger logger = Logger.getLogger(path);
		initLogger(logger, loggerName);
		return logger;
	}
}
