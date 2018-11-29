package node;

import java.io.IOException;
import java.util.logging.Level;
import java.util.Scanner;
import java.util.logging.Logger;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import de.pi3g.pi.oled.Font;
import de.pi3g.pi.oled.OLEDDisplay;
import node.util.observer.Observable;
import node.util.observer.Observer;
import node.web.WebEvent;
import node.web.WebManager;

public class TestMain
{
	public static void main(String[] args) throws InterruptedException, IOException, UnsupportedBusNumberException
	{
		OLEDDisplay display = new OLEDDisplay();
		display.drawStringCentered("Hello World!", Font.FONT_5X8, 25, true);
		display.update();
		Thread.sleep(10000);
	}

}