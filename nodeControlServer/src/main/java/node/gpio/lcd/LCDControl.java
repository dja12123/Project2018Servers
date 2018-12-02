package node.gpio.lcd;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
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
	private ArrayList<LCDObject> lcdObjList;
	
	private boolean isInit;
	
	private LCDControl()
	{
		this.font = null;
		this.isInit = false;
		this.lcdObjList = new ArrayList<>();
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
	
	public LCDObject showShape(int x, int y, String str)
	{
		boolean[][] bitmap = this.stringToBitMap(str);
		return showShape(x, y, bitmap.length, bitmap[0].length, bitmap);
	}
	
	public LCDObject showShape(int x, int y, int width, int height, boolean[][] shape)
	{
		LCDObject obj = new LCDObject(x, y, width, height, shape);
		this.addLCDObj(obj);
		this.updateDisplay();
		return obj;
	}
	
	public LCDObject replaceShape(LCDObject before, String str)
	{
		boolean[][] bitmap = this.stringToBitMap(str);
		return replaceShape(before, bitmap);
	}
	
	public LCDObject replaceShape(LCDObject before, boolean[][] shape)
	{
		LCDObject obj = new LCDObject(before.x, before.y, before.width, before.height, shape);
		this.removeLCDObj(before);
		this.addLCDObj(obj);
		this.updateDisplay();
		return obj;
	}
	
	public void removeShape(LCDObject obj)
	{
		this.removeLCDObj(obj);
	}
	
	private void addLCDObj(LCDObject obj)
	{
		this.lcdObjList.add(obj);
		for(int x = 0; x < obj.shape.length; ++x)
		{
			for(int y = 0; y < obj.shape[x].length; ++y)
			{
				if(obj.shape[x][y] != true)
				{
					continue;
				}
				this.display.setPixel(x + obj.x, y + obj.y, obj.shape[x][y]);
			}
		}
	}
	
	private void removeLCDObj(LCDObject obj)
	{
		this.lcdObjList.remove(obj);
		for(int i = 0; i < obj.width; ++i)
		{
			for(int j = 0; j < obj.height; ++j)
			{
				if(obj.shape[i][j] != true)
				{
					continue;
				}
				this.display.setPixel(i + obj.x, j + obj.y, false);
				// 켜진 픽셀 끄기.
			}
		}
		for(int i = 0; i < this.lcdObjList.size(); ++i)
		{
			LCDObject beforeObj = this.lcdObjList.get(i);
			if((obj.x < beforeObj.x + beforeObj.width && obj.x + obj.width > beforeObj.x
					&& obj.y > beforeObj.y + beforeObj.height && obj.y + obj.height < beforeObj.y))
			{// 사각형 겹치는지 확인
				continue;
			}
			int cx = Math.max(obj.x, beforeObj.x);
			int cy = Math.max(obj.y, beforeObj.y);
			int cwidth = Math.min(obj.x + obj.width, beforeObj.x + beforeObj.width);
			int cheight = Math.min(obj.y + obj.height, beforeObj.y + beforeObj.height);
			for(int x = 0; x < cwidth; ++x)
			{
				for(int y = 0; y < cheight; ++y)
				{
					if(beforeObj.shape[cx + x - beforeObj.x][cy + y - beforeObj.y] != true)
					{
						continue;
					}
					this.display.setPixel(cx + x, cy + y, true);
					// 이전 픽셀 보이게
				}
			}
		}
	}
	
	private void updateDisplay()
	{
		try
		{
			this.display.update();
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "LCD컨트롤 오류", e);
		}
	}
	
	private boolean[][] stringToBitMap(String s) {
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
        
        boolean[][] result = new boolean[img.getWidth()][img.getHeight()];
        for(int x = 0; x < img.getWidth(); ++x)
        {
        	for(int y = 0; y < img.getHeight(); ++y)
        	{
        		if(img.getRGB(x, y) != -1)
        		{
        			continue;
        		}
        		result[x][y] = true;
        	}
        }
        
        return result;
	}
}
