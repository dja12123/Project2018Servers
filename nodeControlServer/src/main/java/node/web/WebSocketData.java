package node.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.CloseCode;
import org.nanohttpd.protocols.websockets.WebSocket;
import org.nanohttpd.protocols.websockets.WebSocketFrame;

import node.NodeControlCore;
import node.util.observer.Observable;

/*
 * WebSocket의 데이터를 정의한 클래스
 * @extends WebSocket
 */
public class WebSocketData extends WebSocket
{
	private static final String KEY_VALUE_SEPERATOR = "=";
	public static final Logger logger = WebSocketHandler.logger;
	private HashMap<String, Observable<WebEvent>> observerMap;

	public WebSocketData(IHTTPSession handshakeRequest, HashMap<String, Observable<WebEvent>> observerMap)
	{
		super(handshakeRequest);
		this.observerMap = observerMap;
	}
	
	@Override
	protected void onOpen() 
	{
		logger.log(Level.INFO, "웹소켓 열림");
	}
	
	@Override
	protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) 
	{
		String logMsg = "웹소켓 닫힘 [" + (initiatedByRemote ? "Remote" : "Self") + "]"
						+ (code != null ? code : "UnknownCloseCode[" + code + "]")
						+ (reason != null && !reason.isEmpty() ? ": " + reason : "");
		logger.log(Level.INFO, logMsg);
	}
	
	@Override
	protected void onMessage(WebSocketFrame frame) 
	{
		frame.setUnmasked();
		
		String str = frame.getTextPayload();
		String[] kv = str.split(KEY_VALUE_SEPERATOR);
		WebEvent send;
		
		if (kv.length == 1) 
		{
			send = new WebEvent(this, kv[0], null);
		}
		else if(kv.length == 2)
		{
			send = new WebEvent(this, kv[0], kv[1]);
		}
		else
		{
			return;
		}
		Observable<WebEvent> observable = observerMap.get(send.key);
		
		if (observable == null) 
		{
			return;
		}
		
		logger.log(Level.INFO, frame.toString());
		try {
			sendFrame(frame);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < observable.size(); ++i)
		{
			observable.notifyObservers(NodeControlCore.mainThreadPool, send);
		}
	}
	
	@Override
	protected void onPong(WebSocketFrame pong) 
	{
		logger.log(Level.INFO, "웹 소켓 Pong " + pong);
	}
	
	@Override
	protected void onException(IOException exception) 
	{
		logger.log(Level.SEVERE, "웹 소켓 예외가 발생함", exception);
	}
	
	@Override
	protected void debugFrameReceived(WebSocketFrame frame) 
	{
		logger.log(Level.INFO, "프레임 받음 >> " + frame);
	}
	
	@Override
	protected void debugFrameSent(WebSocketFrame frame) 
	{
		logger.log(Level.INFO, "프레임 보냄 >> " + frame);
	}
}