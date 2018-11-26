package node.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.NanoWSD;
import org.nanohttpd.protocols.websockets.WebSocket;

import node.IServiceModule;
import node.web.WebEvent;
import node.log.LogWriter;
import node.util.observer.Observable;
import node.util.observer.Observer;

//NanoWSD를 상속받아야 함 -> nanohttpd-websocket 라이브러리에 protocols.http.NanoHTTPD 클래스가 있어야 함
public class WebSocketHandler extends NanoWSD implements IServiceModule 
{
	public static final String KEY_DATA_SEPERATOR = "=";
	
	public static final Logger logger = LogWriter.createLogger(WebSocketHandler.class, "websocket");
	
	private HashMap<String, Observable<WebEvent>> observerMap;
	
	private final boolean debug;
	
	public WebSocketHandler(int port, boolean debug) 
	{
		super(port);
		System.out.println("port open >> " + port);
		this.observerMap = new HashMap<String, Observable<WebEvent>>();
		this.debug = debug;
	}
	
	public boolean getDebug() 
	{
		return this.debug;
	}
	
	/*
	 * @see org.nanohttpd.protocols.websockets.NanoWSD#openWebSocket(org.nanohttpd.protocols.http.IHTTPSession)
	 * @param IHTTPSession handshake: 세션
	 */
	@Override
	protected WebSocket openWebSocket(IHTTPSession handshake) 
	{
		logger.log(Level.INFO, handshake.toString());
		return new WebSocketData(handshake, this.observerMap);
	}
	
	public void addObserver(String key, Observer<WebEvent> observer) 
	{
		Observable<WebEvent> ob = this.observerMap.getOrDefault(key, null);
		if (ob == null) 
		{
			ob = new Observable<WebEvent>();
			this.observerMap.put(key, ob);
		}
		
		ob.addObserver(observer);
	}
	
	public void removeObserver(String key, Observer<WebEvent> observer) 
	{
		Observable<WebEvent> ob = this.observerMap.getOrDefault(key, null);
		
		if (ob == null) 
		{
			return;
		}
		
		ob.removeObserver(observer);
		
		if (ob.size() == 0) 
		{
			this.observerMap.remove(key);
		}
	}
	
	public void removeObserver(Observer<WebEvent> observer) 
	{
		Observable<WebEvent> ob;
		ArrayList<String> removeObservableKey = new ArrayList<>();
		
		for (String key : this.observerMap.keySet()) 
		{
			ob = this.observerMap.get(key);
			ob.removeObserver(observer);
			
			if (ob.size() == 0) 
			{
				removeObservableKey.add(key);
			}
		}
		
		for (int i = 0; i < removeObservableKey.size(); ++i) 
		{
			this.observerMap.remove(removeObservableKey.get(i));
		}
	}
	
	@Override
	public boolean startModule() 
	{
		logger.log(Level.INFO, "웹소켓 매니저 로드");
		
		return true;
	}
	
	@Override
	public void stopModule() 
	{
		logger.log(Level.INFO, "웹소켓 매니저 종료");
		
		this.observerMap.clear();
	}
}
