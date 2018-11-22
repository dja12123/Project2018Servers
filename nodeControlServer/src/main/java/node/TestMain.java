package node;

import java.awt.Color;
import java.util.logging.Logger;

import com.diozero.util.SleepUtil;
import com.diozero.ws281xj.LedDriverInterface;
import com.diozero.ws281xj.PixelAnimations;
import com.diozero.ws281xj.StripType;
import com.diozero.ws281xj.spi.WS281xSpi;

import de.pi3g.pi.ws2812.WS2812;
import node.log.LogWriter;
import node.network.socketHandler.RawSocketReceiver;

public class TestMain
{
	public static final Logger logger = LogWriter.createLogger(RawSocketReceiver.class, "rawsocket");
	
	public static void main(String[] args) {
		StripType strip_type = StripType.WS2812;
		
		int pixels = 4;
		if (args.length > 0) {
			pixels = Integer.parseInt(args[0]);
		}
		int brightness = 127;
		
		try (LedDriverInterface led_driver = new WS281xSpi(2, 0, strip_type, pixels, brightness)) {
			logger.info("All off");
			led_driver.allOff();
			SleepUtil.sleepMillis(500);

			for (int i=0; i<5; i++) {
				logger.info("Incremental red");
				int red = 0;
				for (int pixel=0; pixel<pixels; pixel++) {
					led_driver.setPixelColourRGB(pixel, red, 0, 0);
					red += 255/pixels;
				}
				led_driver.render();
				SleepUtil.sleepMillis(500);
				
				logger.info("Incremental green");
				int green = 0;
				for (int pixel=0; pixel<pixels; pixel++) {
					led_driver.setPixelColourRGB(pixel, 0, green, 0);
					green += 255/pixels;
				}
				led_driver.render();
				SleepUtil.sleepMillis(500);
				
				logger.info("Incremental blue");
				int blue = 0;
				for (int pixel=0; pixel<pixels; pixel++) {
					led_driver.setPixelColourRGB(pixel, 0, 0, blue);
					blue += 255/pixels;
				}
				led_driver.render();
				SleepUtil.sleepMillis(500);
			}
			
			logger.info("All off");
			led_driver.allOff();
			led_driver.render();
			SleepUtil.sleepMillis(500);
			
			PixelAnimations.demo(led_driver);
		}
	}
}

