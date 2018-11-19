package node.web;
//
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.IServiceModule;
import node.log.LogWriter;

public class WebManager implements IServiceModule {
	private static final Logger logger = LogWriter.createLogger(WebManager.class, "WebService");
	HTTPServer httpServer;
	WebSocketHandler webSocketHandler;
	// 옵저버 관련된 코드 이곳에 모두 추가
	
	public WebManager() {
		this.httpServer = new HTTPServer();
		this.webSocketHandler = new WebSocketHandler(8080, true);
	}
	
	public static void main(String[] args) {
		WebManager webServer = new WebManager();
		
		webServer.startModule();
	}

	@Override
	public boolean startModule()
	{
		logger.log(Level.INFO, "웹 서비스 시작");
		try
		{
			this.webSocketHandler.start();
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "웹 서비스 시작중 오류", e);
			return false;
		}
		this.httpServer.start();
		return true;
	}

	@Override
	public void stopModule()
	{
		this.webSocketHandler.stop();
		this.httpServer.stop();
		
	}

}
