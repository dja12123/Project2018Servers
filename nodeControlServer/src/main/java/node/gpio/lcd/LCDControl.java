package node.gpio.lcd;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import de.pi3g.pi.oled.OLEDDisplay;
import node.TestMain;
import node.gpio.led.LEDControl;
import node.log.LogWriter;

public class LCDControl
{
	public static final Logger logger = LogWriter.createLogger(LCDControl.class, "LCDControl");
	
	public static final LCDControl inst = new LCDControl();
	
	private Font font;
	private OLEDDisplay display;
	
	private boolean isInit;
	
	private LCDControl()
	{
		this.font = null;
		this.isInit = false;
	}
	
	public void init()
	{
		if(this.isInit) return;
		
		try
		{
			this.display = new OLEDDisplay();
		}
		catch (IOException | UnsupportedBusNumberException e)
		{
			logger.log(Level.SEVERE, "디스플레이 로드 불가", e);
			return;
		}
		
		try
		{
			this.font = Font.createFont(Font.TRUETYPE_FONT, LCDControl.class.getResourceAsStream("/font/D2Coding.ttf"));
		}
		catch (FontFormatException | IOException e)
		{
			logger.log(Level.SEVERE, "폰트 로드 불가", e);
			return;
		}
		
		this.font = this.font.deriveFont(Font.PLAIN, 14);

		this.isInit = true;
	}
	
	public void showString(String str)
	{
		BufferedImage img = this.stringToBufferedImage(str);
		for(int i = 0; i < img.getWidth(); ++i)
		{
			for(int j = 0; j < img.getHeight(); ++j)
			{

				if(img.getRGB(i, j) == -1)
				{

					display.setPixel(i, j, true);
				}
			}
		}
	}
	
	public BufferedImage stringToBufferedImage(String s) {
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = img.createGraphics();
   
        g2d.setFont(this.font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(s);
        int height = fm.getHeight();
        g2d.dispose();

        img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        g2d.setFont(this.font);
        fm = g2d.getFontMetrics();
        g2d.drawString(s, 0, fm.getAscent());
        g2d.dispose();
        return img;
	}
}
