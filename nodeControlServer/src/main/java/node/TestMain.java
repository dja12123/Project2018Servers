package node;

import java.awt.Color;
import java.util.logging.Logger;

import de.pi3g.pi.ws2812.WS2812;
import node.log.LogWriter;
import node.network.socketHandler.RawSocketReceiver;

public class TestMain
{
	public static final Logger logger = LogWriter.createLogger(RawSocketReceiver.class, "rawsocket");
	
	public static void main(String[] args)
	{
		System.out.println("test");
	}
	

}

