package node.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import node.NodeControlCore;

public class LogWriter {
	public static final String logFormat = "[%1$tT][%2$s][%3$s] %4$s %5$s %n";// 로그 포맷
	
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
