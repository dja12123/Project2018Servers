package node;

import java.awt.Font;
import java.awt.Image;
import java.io.IOException;
import java.util.logging.Level;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiFactory;

import eu.ondryaso.ssd1306.Display;
import eu.ondryaso.ssd1306.examples.BasicGraphics;
import node.util.observer.Observable;
import node.util.observer.Observer;
import node.web.WebEvent;
import node.web.WebManager;

public class TestMain
{
	public static void main(String[] args) throws IOException, ReflectiveOperationException, UnsupportedBusNumberException, InterruptedException
	{
		Display disp = new Display(128, 64, GpioFactory.getInstance(), I2CFactory.getInstance(I2CBus.BUS_1), 0x3D);

		// Create 128x64 display on CE1 (change to SpiChannel.CS0 for using CE0) with
		// D/C pin on WiringPi pin 04

		disp.begin();
		// Init the display

		disp.getGraphics().setFont(new Font("Monospaced", Font.PLAIN, 10));
		disp.getGraphics().drawString("Praise him", 64, 60);
		disp.getGraphics().drawRect(0, 0, disp.getWidth() - 1, disp.getHeight() - 1);
		// Deal with the image using AWT

		disp.displayImage();
		// Copy AWT image to an inner buffer and send to the display

		for (int x = 70; x < 90; x += 2)
		{
			for (int y = 10; y < 30; y += 2)
			{
				disp.setPixel(x, y, true);
			}
		}
		// Set some pixels in the buffer manually

		disp.display();
		Thread.sleep(3000);
		// Send the buffer to the display again, now with the modified pixels
	}
}
