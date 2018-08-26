package kr.dja.project2018Servers.router;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.dhcp4java.DHCPPacket;
import org.dhcp4java.DHCPCoreServer;
import org.dhcp4java.DHCPServerInitException;
import org.dhcp4java.DHCPServlet;

import kr.dja.project2018Servers.router.dhcp.DHCPService;

/**
 * A simple DHCP sniffer based on DHCP servlets.
 *
 * @author Stephan Hadinger
 * @version 1.00
 */
public class RouterCore
{
	public static final String CONF_INTERFACE = "interface";

	public static final String logFormat = "[%1$tT][%2$s][%3$s] %4$s %5$s %n";
	public static final Logger mainLogger = Logger.getLogger(RouterCore.class.getName().toLowerCase());
	public static final Logger dhcpLogger = DHCPCoreServer.logger;
	public static final Properties properties = new Properties();

	public static void main(String[] args)
	{
		
		initLogger(mainLogger, "main");
		initLogger(dhcpLogger, "dhcp");
		mainLogger.log(Level.INFO, "서버 시작");
		
		try
		{
			properties.load(RouterCore.class.getResourceAsStream("/config.properties"));
		}
		catch (IOException e)
		{
			mainLogger.log(Level.SEVERE, "config 로드 실패", e);
		}
		
		DHCPService dhcp = new DHCPService();
		
	}

	private static void initLogger(Logger logger, String loggerName)
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

	
}
