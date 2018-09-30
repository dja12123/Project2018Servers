package node.web;

import node.IServiceModule;
import node.log.LogWriter;
import fileIO.FileHandler;

import java.util.logging.Logger;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.util.ServerRunner;

enum MIME_TYPE {
	IMAGE_JPEG("image/jpeg");
	
	String typeString;
	
	MIME_TYPE(String typeString) {
		this.typeString = typeString;
	}
	
	@Override
	public String toString() {
		return typeString;
	}
}

public class WebServiceMain extends NanoHTTPD implements IServiceModule 
{
	private static final Logger LOG = LogWriter.createLogger(WebServiceMain.class, "WebServiceMain");
	private static final int MAXIMUM_SIZE_OF_IMAGE = 1000000;
	public static final String rootDirectory = "/root/Project2018Servers/nodeControlServer/resources/www";
	
	public WebServiceMain() {
		super(80);
	}
	
	public static void main(String[] args)
	{
		System.out.println("Hello!");
		WebServiceMain main = new WebServiceMain();
		main.startModule();
	}
	
	private static Response serveImage(MIME_TYPE imageType, String path) {
		return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, imageType.toString(), 
				FileHandler.getFileInputStream(path), MAXIMUM_SIZE_OF_IMAGE);
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
		
		String msg = "";
		if (uri.startsWith("/")) { //Root Mapping
			if (uri.contains(".jpg")) {
				return WebServiceMain.serveImage(MIME_TYPE.IMAGE_JPEG, rootDirectory + uri);
			}
			msg = FileHandler.readFileString("/root/Project2018Servers/nodeControlServer/resources/www/index.html");
			
		}
		
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
