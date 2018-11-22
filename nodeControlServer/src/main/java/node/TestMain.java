package node;

import java.awt.Color;
import java.util.logging.Logger;

import de.pi3g.pi.ws2812.WS2812;
public class TestMain
{
	public static void main(String[] args) throws InterruptedException
	{
		WS2812.get().init(4); //init a chain of 64 LEDs
		WS2812.get().clear();    
		WS2812.get().setPixelColor(0, Color.RED); //sets the color of the fist LED to red
		WS2812.get().show();
		Thread.sleep(10000);
	}
}