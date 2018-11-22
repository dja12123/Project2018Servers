package node;

import java.awt.Color;
import java.util.logging.Logger;

import de.cacodaemon.rpiws28114j.StripType;
import de.cacodaemon.rpiws28114j.WS2811;
import de.cacodaemon.rpiws28114j.WS2811Channel;
import de.pi3g.pi.ws2812.WS2812;
public class TestMain
{
	public static void main(String[] args) throws InterruptedException
	{

		
		WS2811.init(new WS2811Channel(
	            10, // gpioPin
	            4, //ledCount
	            StripType.WS2811_STRIP_RGB,
	            false, // invert
	            128 // brightness
	    ));
	    

		WS2811.setPixel(0, 0x00FF00);

		WS2811.render();
		Thread.sleep(10000);
	}
}