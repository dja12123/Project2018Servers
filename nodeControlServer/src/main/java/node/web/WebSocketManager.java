package node.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import org.nanohttpd.protocols.websockets.CloseCode;
import org.nanohttpd.protocols.websockets.NanoWSD;
import org.nanohttpd.protocols.websockets.WebSocket;
import org.nanohttpd.protocols.websockets.WebSocketFrame;

import node.IServiceModule;
import node.web.WebEvent;
import node.log.LogWriter;
import node.util.observer.Observable;
import node.util.observer.Observer;

//nanohttpd websocket 이용
public class WebSocketManager /*extends NanoWSD*/ implements IServiceModule {
	public static final Logger logger = LogWriter.createLogger(WebSocketManager.class, "websocket");
	
	private HashMap<String, Observable<WebEvent>> observerMap;
	
	public WebSocketManager(int port) {
		/*super(port);*/
		this.observerMap = new HashMap<String, Observable<WebEvent>>();
	}
	
	public void addObserver(String key, Observer<WebEvent> observer) {
		Observable<WebEvent> ob = this.observerMap.getOrDefault(key, null);
		if (ob == null) {
			ob = new Observable<WebEvent>();
			this.observerMap.put(key, ob);
		}
		
		ob.addObserver(observer);
	}
	
	public void removeObserver(String key, Observer<WebEvent> observer) {
		Observable<WebEvent> ob = this.observerMap.getOrDefault(key, null);
		
		if (ob == null) {
			return;
		}
		
		ob.removeObserver(observer);
		
		if (ob.size() == 0) {
			this.observerMap.remove(key);
		}
	}
	
	public void removeObserver(Observer<WebEvent> observer) {
		Observable<WebEvent> ob;
		ArrayList<String> removeObservableKey = new ArrayList<>();
		
		for (String key : this.observerMap.keySet()) {
			ob = this.observerMap.get(key);
			ob.removeObserver(observer);
			
			if (ob.size() == 0) {
				removeObservableKey.add(key);
			}
		}
		
		for (int i = 0; i < removeObservableKey.size(); ++i) {
			this.observerMap.remove(removeObservableKey.get(i));
		}
	}
	
	//소켓 통신 부분(클라이언트에서 넘어온 것 체크 -> 통신)
	
	public void sendMessage() {
		
	}
	
	@Override
	public boolean startModule() {
		logger.log(Level.INFO, "웹소켓 매니저 로드");
		
		return true;
	}
	
	@Override
	public void stopModule() {
		logger.log(Level.INFO, "웹소켓 매니저 종료");
		
		this.observerMap.clear();
	}
	
	
}
