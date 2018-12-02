package node;


import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import de.pi3g.pi.oled.OLEDDisplay;
import node.gpio.lcd.LCDControl;
import node.gpio.lcd.LCDObject;

public class TestMain
{
	public static Font font;
	public static void main(String[] args) throws IOException, ReflectiveOperationException, UnsupportedBusNumberException, InterruptedException, FontFormatException
	{
		

		font = Font.createFont(Font.TRUETYPE_FONT, TestMain.class.getResourceAsStream("/font/D2Coding.ttf"));
		font = font.deriveFont(Font.PLAIN, 14);

		LCDControl.inst.init();
		LCDObject obj = LCDControl.inst.showShape(25, 25, "안녕");
		Thread.sleep(500);
		LCDControl.inst.removeShape(obj);
		Thread.sleep(500);
		obj = LCDControl.inst.showShape(25, 25, "안녕");
		Thread.sleep(500);
		obj = LCDControl.inst.replaceShape(obj, "테스트");
		Thread.sleep(500);
		LCDControl.inst.showShape(30, 30, "안녕");
		LCDControl.inst.showShape(20, 30, "1234");
		Thread.sleep(500);
		LCDControl.inst.removeShape(obj);
		Thread.sleep(100000);
		
	}

}
