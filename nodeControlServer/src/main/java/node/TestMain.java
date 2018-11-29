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

public class TestMain
{
	public static Font font;
	public static void main(String[] args) throws IOException, ReflectiveOperationException, UnsupportedBusNumberException, InterruptedException, FontFormatException
	{
		

		font = Font.createFont(Font.TRUETYPE_FONT, TestMain.class.getResourceAsStream("/font/D2Coding.ttf"));
		font = font.deriveFont(Font.PLAIN, 14);

		OLEDDisplay display = new OLEDDisplay();
		int x = 0;
		while(true)
		{
			display.clear();
			BufferedImage img = stringToBufferedImage("테스트카운트:"+x);
			System.out.println(img.getWidth() + " " + img.getHeight());
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

			
			display.update();
			++x;
		
			Thread.sleep(100);
		}

		
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
