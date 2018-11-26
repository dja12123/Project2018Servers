package node.web;

import org.nanohttpd.protocols.websockets.WebSocketFrame;

//channel 이용해야 함

public class WebEvent 
{
	public final WebSocketData channel;
	public final String key;
	public final String value;
	
	WebEvent(WebSocketData webSocketData, String key, String value) 
	{
		this.key = key;
		this.value = value;
		this.channel = webSocketData;
	}
}