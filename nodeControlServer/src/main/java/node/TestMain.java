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
		LCDControl.inst.showRect(0, 0, 10, 10);
		LCDControl.inst.showLine(0, 0, 25, 25);
		LCDControl.inst.showLine(10, 0, 25, 50);
		LCDObject obj = LCDControl.inst.showString(25, 25, "안녕");
		Thread.sleep(500);
		LCDControl.inst.removeShape(obj);
		Thread.sleep(500);
		obj = LCDControl.inst.showString(25, 25, "안녕");
		Thread.sleep(500);
		obj = LCDControl.inst.replaceString(obj, "테스트");
		Thread.sleep(500);
		LCDControl.inst.showString(30, 30, "안녕");
		LCDObject obj1 = LCDControl.inst.showString(20, 30, "1234");
		
		Thread.sleep(500);
		LCDControl.inst.removeShape(obj);
		Thread.sleep(2000);
		LCDControl.inst.removeShape(obj1);
		Thread.sleep(200000);
		
	}

}
