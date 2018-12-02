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
		obj = LCDControl.inst.showShape(30, 30, "안녕");
		Thread.sleep(500);
		LCDControl.inst.removeShape(obj);
		Thread.sleep(5000);
		
	}
	public static BufferedImage stringToBufferedImage(String s) {
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = img.createGraphics();
   
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(s);
        int height = fm.getHeight();
        g2d.dispose();

        img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        g2d.drawString(s, 0, fm.getAscent());
        g2d.dispose();
        return img;
	}
}
