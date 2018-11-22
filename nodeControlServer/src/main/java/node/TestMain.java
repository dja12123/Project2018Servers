package node;

import java.awt.Color;
import java.util.logging.Logger;

import com.diozero.util.SleepUtil;
import com.diozero.ws281xj.LedDriverInterface;
import com.diozero.ws281xj.PixelAnimations;
import com.diozero.ws281xj.StripType;
import com.diozero.ws281xj.spi.WS281xSpi;

import node.gpio.led.LEDControl;
import node.log.LogWriter;
import node.network.socketHandler.RawSocketReceiver;

public class TestMain
{
	public static final Logger logger = LogWriter.createLogger(RawSocketReceiver.class, "rawsocket");

	public static void main(String[] args)
	{
		LEDControl.ledControl.createLEDControl(0, 500, 300, 30, 100, 200, 100, 0, 0, 0);
		LEDControl.ledControl.createLEDControl(1, 500, 300, 30, 100, 200, 100, 0, 0, 0);
		LEDControl.ledControl.createLEDControl(2, 500, 300, 30, 100, 200, 100, 0, 0, 0);
		LEDControl.ledControl.createLEDControl(3, 500, 300, 30, 100, 200, 100, 0, 0, 0);

	}
}
