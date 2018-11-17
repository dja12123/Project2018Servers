package node.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.web.WebEvent;
import node.log.LogWriter;
import node.network.NetworkManager;
import node.util.observer.Observable;
import node.util.observer.Observer;

//nanohttpd websocket 이용
public class WebSocketManager {
	public static final Logger logger = LogWriter.createLogger(WebSocketManager.class, "websocket");
	
	private HashMap<String, Observable<WebEvent>> observerMap;
	
	public WebSocketManager() {
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
	
	public void sendMessage() {
		
	}
	
	public void socketReadCallback() {
		
	}	
}
