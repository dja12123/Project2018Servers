package node;


import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.Scanner;
import java.util.logging.Logger;


import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiFactory;


import de.pi3g.pi.oled.OLEDDisplay;
import node.util.observer.Observable;
import node.util.observer.Observer;
import node.web.WebEvent;
import node.web.WebManager;

public class TestMain
{
	public static Font font;
	public static void main(String[] args) throws IOException, ReflectiveOperationException, UnsupportedBusNumberException, InterruptedException, FontFormatException
	{
		font = Font.createFont(Font.TRUETYPE_FONT, TestMain.class.getResourceAsStream("font/D2Coding"));
		OLEDDisplay display = new OLEDDisplay();
		int i = 0;
		while(true)
		{
			display.clear();
			display.drawImage(stringToBufferedImage("count:"+i), 25, 25);
			display.update();
			++i;
		
			Thread.sleep(100);
		}
		
		
	}
	public static BufferedImage stringToBufferedImage(String s) {
	    //First, we have to calculate the string's width and height
		
	    BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
	    Graphics g = img.getGraphics();
	    //Set the font to be used when drawing the string
	    g.setFont(font);

	    //Get the string visual bounds
	    FontRenderContext frc = g.getFontMetrics().getFontRenderContext();
	    Rectangle2D rect = font.getStringBounds(s, frc);
	    //Release resources
	    g.dispose();
	    //Then, we have to draw the string on the final image

	    //Create a new image where to print the character
	    img = new BufferedImage((int) Math.ceil(rect.getWidth()), (int) Math.ceil(rect.getHeight()), BufferedImage.TYPE_4BYTE_ABGR);
	    g = img.getGraphics();
	    g.setFont(font);

	    //Calculate x and y for that string
	    FontMetrics fm = g.getFontMetrics();
	    int x = 0;
	    int y = fm.getAscent(); //getAscent() = baseline
	    g.drawString(s, x, y);
	    
	    //Release resources
	    g.dispose();
	    //Return the image
	    return img;
	}
}
