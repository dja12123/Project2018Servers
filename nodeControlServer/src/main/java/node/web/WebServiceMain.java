package node.web;

import node.IServiceModule;
import node.log.LogWriter;
import fileIO.FileHandler;

import java.util.Map;
import java.util.logging.Logger;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.util.ServerRunner;

public class WebServiceMain extends NanoHTTPD implements IServiceModule 
{
	//NodeControlCore.createLogger를 이용
	private static final Logger LOG = LogWriter.createLogger(WebServiceMain.class, "WebServiceMain");
	
	public WebServiceMain() {
		super(80);
	}
	
	public static void main(String[] args)
	{
		System.out.println("Hello!");
		WebServiceMain main = new WebServiceMain();
		main.startModule();
	}
	
	@Override
	public Response serve(IHTTPSession session) {
		Method method = session.getMethod();
		String uri = session.getUri();
		WebServiceMain.LOG.info(method + " '" + uri + "' ");
		
		//웹서비스 할 때 필요한 파일 스트림 모듈로 만들기(fileIO 패키지)
		//StringBuffer 적극 사용
		//url이용해서 어떤 요청인지 구분 ->
		//   refer:: https://github.com/Teaonly/android-eye/blob/master/src/teaonly/droideye/TeaServer.java
		
		String msg = FileHandler.readFileString("/root/Project2018Servers/nodeControlServer/resources/www/index.html");
        System.out.println("Response Data Recieve...");
		return newFixedLengthResponse(msg);
	}
	
	@Override
	public boolean startModule()
	{
		ServerRunner.run(WebServiceMain.class);
		return true;
	}

	@Override
	public void stopModule()
	{
		
		
	}

}
