package node.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.CloseCode;
import org.nanohttpd.protocols.websockets.NanoWSD;
import org.nanohttpd.protocols.websockets.WebSocket;
import org.nanohttpd.protocols.websockets.WebSocketFrame;

import node.IServiceModule;
import node.web.WebEvent;
import node.log.LogWriter;
import node.util.observer.Observable;
import node.util.observer.Observer;

//NanoWSD를 상속받아야 함 -> nanohttpd-websocket 라이브러리에 protocols.http.NanoHTTPD 클래스가 있어야 함
public class WebSocketManager extends NanoWSD implements IServiceModule {
	public static final Logger logger = LogWriter.createLogger(WebSocketManager.class, "websocket");
	
	private HashMap<String, Observable<WebEvent>> observerMap;
	
	private final boolean debug;
	
	public WebSocketManager(int port, boolean debug) {
		super(port);
		this.observerMap = new HashMap<String, Observable<WebEvent>>();
		this.debug = debug;
	}
	
	/*
	 * @see org.nanohttpd.protocols.websockets.NanoWSD#openWebSocket(org.nanohttpd.protocols.http.IHTTPSession)
	 * @param IHTTPSession handshake: 세션
	 */
	@Override
	protected WebSocket openWebSocket(IHTTPSession handshake) {
		return new WebSocketData(this, handshake);
	}
	
	/*
	 * WebSocket의 데이터를 정의한 클래스
	 * @extends WebSocket
	 */
	private static class WebSocketData extends WebSocket {
		private final WebSocketManager server;
		
		public WebSocketData(WebSocketManager server, IHTTPSession handshakeRequest) {
			super(handshakeRequest);
			this.server = server;
		}
		
		@Override
		protected void onOpen() { 
			logger.log(Level.INFO, "웹소켓 열림");
		}
		
		@Override
		protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {
			if (server.debug) {
				String logMsg = "웹소켓 닫힘 [" + (initiatedByRemote ? "Remote" : "Self") + "]"
								+ (code != null ? code : "UnknownCloseCode[" + code + "]")
								+ (reason != null && !reason.isEmpty() ? ": " + reason : "");
				System.out.println(logMsg);
			}
		}
		
		@Override
		protected void onMessage(WebSocketFrame message) {
			try {
				message.setUnmasked();
				sendFrame(message);
			} catch (IOException e) {
				System.out.println("웹 소켓 메시지 전송 파일 입출력 오류");
				//throw new RuntimeException(e);
			}
		}
		
		@Override
		protected void onPong(WebSocketFrame pong) {
			if (server.debug) {
				System.out.println("웹 소켓 Pong " + pong);
			}
		}
		
		@Override
		protected void onException(IOException exception) {
			logger.log(Level.SEVERE, "웹 소켓 예외가 발생함", exception);
		}
		
		@Override
		protected void debugFrameReceived(WebSocketFrame frame) {
			if (server.debug) {
				System.out.println("프레임 받음 >> " + frame);
			}
		}
		
		@Override
		protected void debugFrameSent(WebSocketFrame frame) {
			if (server.debug) {
				System.out.println("프레임 보냄 >> " + frame);
			}
		}
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
