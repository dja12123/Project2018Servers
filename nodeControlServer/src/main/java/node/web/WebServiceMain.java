package node.web;

import node.IServiceModule;
import node.fileIO.FileHandler;
import node.log.LogWriter;

import java.util.logging.Logger;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.util.ServerRunner;

enum MIME_TYPE
{
	IMAGE_JPEG("image/jpeg"),
	IMAGE_PNG("image/png");

	String typeString;

	MIME_TYPE(String typeString)
	{
		this.typeString = typeString;
	}

	@Override
	public String toString()
	{
		return typeString;
	}
}

public class WebServiceMain extends NanoHTTPD implements IServiceModule
{
	private static final Logger LOG = LogWriter.createLogger(WebServiceMain.class, "WebServiceMain");
	// private static final int MAXIMUM_SIZE_OF_IMAGE = 1000000;
	public static final String rootDirectory = FileHandler.getExtResourceFile("www").toString();
	
	public WebServiceMain()
	{
		super(80);
	}

	public static void main(String[] args)
	{
		WebServiceMain main = new WebServiceMain();
		main.startModule();
	}

	private static Response serveImage(MIME_TYPE imageType, String path)
	{
		IStatus status = NanoHTTPD.Response.Status.OK;
		String imageTypeStr = imageType.toString();

		return newFixedLengthResponse(status, imageTypeStr, FileHandler.getInputStream(path), -1);
	}

	@Override
	public Response serve(IHTTPSession session)
	{
		Method method = session.getMethod();
		String uri = session.getUri();

		WebServiceMain.LOG.info(method + " '" + uri + "' ");

		// 웹서비스 할 때 필요한 파일 스트림 모듈로 만들기(fileIO 패키지)
		// StringBuffer 적극 사용
		// url이용해서 어떤 요청인지 구분 ->
		// refer::
		// https://github.com/Teaonly/android-eye/blob/master/src/teaonly/droideye/TeaServer.java
		
		//System.out.println("root >> " + rootDirectory);
		//
		String msg = "";
		if (uri.startsWith("/"))
		{ // Root Mapping
			if (uri.contains(".jpg"))
			{
				//System.out.println("uri path >> " + (rootDirectory + uri));
				return WebServiceMain.serveImage(MIME_TYPE.IMAGE_JPEG, "www" + uri);
			}
			else if (uri.contains(".png")) 
			{
				return WebServiceMain.serveImage(MIME_TYPE.IMAGE_PNG, "www" + uri);
			}
			else if (uri.contains(".js"))
			{
				msg = FileHandler.readFileString("www/index.js");
			}
			else if (uri.contains(".css"))
			{
				msg = FileHandler.readFileString("www/index.css");
			}
			else 
			{
				msg = FileHandler.readFileString("www/index.html");
			}
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
