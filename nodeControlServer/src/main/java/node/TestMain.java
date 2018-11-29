package node;

import java.io.IOException;
import java.util.logging.Level;
import java.util.Scanner;
import java.util.logging.Logger;

import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.pi4j.io.spi.SpiChannel;

import net.fauxpark.oled.Graphics;
import net.fauxpark.oled.SSD1306;
import net.fauxpark.oled.font.CodePage1252;
import net.fauxpark.oled.impl.SSD1306I2CImpl;
import net.fauxpark.oled.impl.SSD1306MockImpl;
import net.fauxpark.oled.impl.SSD1306SPIImpl;
import node.util.observer.Observable;
import node.util.observer.Observer;
import node.web.WebEvent;
import node.web.WebManager;

public class TestMain
{
	public static void main(String[] args) throws InterruptedException, IOException, UnsupportedBusNumberException
	{
		SSD1306 ssd1306 = new SSD1306I2CImpl(128, 64, RaspiPin.GPIO_15, I2CBus.BUS_1, 0x3C);
		Graphics graphics = ssd1306.getGraphics();

		ssd1306.startup(false);
		
		// Draws a line from the top left to the bottom right of the display
		graphics.line(0, 0, 127, 63);

		// Draws an arc from (63,31) with a radius of 8 pixels and an angle of 15 degrees
		graphics.arc(63, 31, 8, 0, 15);

		// Writes "Hello world!" at (20,20) using the Windows-1252 charset
		graphics.text(20, 20, new CodePage1252(), "Hello world!");
		ssd1306.display();
		ssd1306.reset();

		
	}

}