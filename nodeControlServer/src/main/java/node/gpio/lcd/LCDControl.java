package node.gpio.lcd;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import de.pi3g.pi.oled.OLEDDisplay;
import node.NodeControlCore;
import node.log.LogWriter;

public class LCDControl
{
	public static final Logger logger = LogWriter.createLogger(LCDControl.class, "LCDControl");
	
	public static final LCDControl inst = new LCDControl();
	
	private Font font;
	private OLEDDisplay display;
	private ArrayList<LCDObject> lcdObjList;
	private Timer timer;
	
	private boolean isInit;
	
	private LCDControl()
	{
		this.font = null;
		this.isInit = false;
		this.lcdObjList = new ArrayList<>();
		this.timer = new Timer(true);
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
	
	public LCDObject showString(int x, int y, String str)
	{// 해당 좌표에 문자열 출력(한글지원)
		boolean[][] bitmap = this.stringToBitMap(str);
		return showShape(x, y, bitmap);
	}
	
	public LCDObject replaceString(LCDObject before, String str)
	{// 문자열 교체
		boolean[][] bitmap = this.stringToBitMap(str);
		return replaceShape(before, bitmap);
	}
	
	public LCDObject showRect(int x, int y, int width, int height)
	{// 사각형 출력(기준좌표, 사각형크기)
		boolean[][] bitmap = new boolean[width][height];
		for(int i = 0; i < height; ++i)
		{
			bitmap[0][i] = true;
			bitmap[width - 1][i] = true;
		}
		for(int i = 0; i < width; ++i)
		{
			bitmap[i][0] = true;
			bitmap[i][height - 1] = true;
		}
		return showShape(x, y, bitmap);
	}
	
	public LCDObject showFillRect(int x, int y, int width, int height)
	{// 꽉찬 사각형 출력(기준좌표, 사각형크기)
		boolean[][] bitmap = new boolean[width][height];
		for(int i = 0; i < height; ++i)
		{
			for(int j = 0; j < width; ++j)
			{
				bitmap[i][j] = true;
			}
		}
		return showShape(x, y, bitmap);
	}
	
	public LCDObject showLine(int x0, int y0, int x1, int y1)
	{// 선 출력(시작좌표, 끝좌표)
		int temp;
		if (x0 > x1)
		{
			temp = x1;
			x1 = x0;
			x0 = temp;
		}
		if (y0 > y1)
		{
			temp = y1;
			y1 = x0;
			y0 = temp;
		}
		int dx = x1 - x0;
		int dy = y1 - y0;
		int basex = x0;
		int basey = y0;
		boolean[][] bitmap = new boolean[dx + 1][dy + 1];
		if (Math.abs(dx) > Math.abs(dy))
		{
			float m = (float) dy / (float) dx;
			float n = y0 - m * x0;
			dx = (x1 > x0) ? 1 : -1;
			while (x0 != x1)
			{
				x0 += dx;
				y0 = (int) (m * (float) x0 + n + (float) 0.5);
				
				bitmap[x0 - basex][y0 - basey] = true;
			}
		}
		else if (dy != 0)
		{
			float m = (float) dx / (float) dy;
			float n = x0 - m * y0;
			dy = (dy < 0) ? -1 : 1;
			while (y0 != y1)
			{
				y0 += dy;
				x0 = (int) (m * (float) y0 + n + (float) 0.5);
				bitmap[x0 - basex][y0 - basey] = true;
			}
		}
		return showShape(basex, basey, bitmap);
	}
	
	public LCDObject showShape(int x, int y, boolean[][] shape)
	{// 비트맵 도형 출력
		if(shape.length <= 0)
		{
			return null;
		}
		LCDObject obj = new LCDObject(x, y, shape.length, shape[0].length, shape);
		this.addLCDObj(obj);
		this.updateDisplay();
		return obj;
	}
	
	public LCDObject replaceShape(LCDObject before, boolean[][] shape)
	{
		LCDObject obj = new LCDObject(before.x, before.y, shape.length, shape[0].length, shape);
		this.removeLCDObj(before);
		this.addLCDObj(obj);
		this.updateDisplay();
		return obj;
	}
	
	public void removeShape(LCDObject obj)
	{
		this.removeLCDObj(obj);
		this.updateDisplay();
	}
	
	public LCDObject removeShapeTimer(LCDObject obj, int time)
	{
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				LCDControl.this.removeLCDObj(obj);
				LCDControl.this.updateDisplay();
			}
		};
		this.timer.schedule(task, time);
		return obj;
	}
	
	public LCDObject blinkShape(LCDObject obj, int time, int count)
	{
		TimerTask task = new TimerTask()
		{
			boolean blink = true;
			int c = count;
			@Override
			public void run()
			{
				if(this.blink)
				{
					this.blink = false;
					LCDControl.this.removeLCDObj(obj);
					--this.c;
					if(this.c <= 0)
					{
						this.cancel();
					}
				}
				else
				{
					this.blink = true;
					LCDControl.this.addLCDObj(obj);

				}
				LCDControl.this.updateDisplay();
			}
		};
		this.timer.schedule(task, 0, time);
		return obj;
	}
	
	private synchronized void addLCDObj(LCDObject obj)
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
	
	private synchronized void removeLCDObj(LCDObject obj)
	{
		this.lcdObjList.remove(obj);
		for(int x = 0; x < obj.width; ++x)
		{
			for(int y = 0; y < obj.height; ++y)
			{
				if(obj.shape[x][y] != true)
				{
					continue;
				}
				this.display.setPixel(x + obj.x, y + obj.y, false);
				// 켜진 픽셀 끄기.
			}
		}
		for(int i = 0; i < this.lcdObjList.size(); ++i)
		{
			LCDObject nextObj = this.lcdObjList.get(i);
			if(obj.x > nextObj.x + nextObj.width) continue;
			if(obj.x + obj.width < nextObj.x) continue;
			if(obj.y > nextObj.y + nextObj.height) continue;
			if(obj.y + obj.height < nextObj.y) continue;
			//사각형 겹치는지 확인
			
			int cx = Math.max(obj.x, nextObj.x);
			int cy = Math.max(obj.y, nextObj.y);
			int cwidth = Math.min(obj.x + obj.width, nextObj.x + nextObj.width) - cx;
			int cheight = Math.min(obj.y + obj.height, nextObj.y + nextObj.height) - cy;

			for(int x = 0; x < cwidth; ++x)
			{
				for(int y = 0; y < cheight; ++y)
				{
					if(nextObj.shape[cx + x - nextObj.x][cy + y - nextObj.y] != true)
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
		NodeControlCore.mainThreadPool.execute(()->
		{
			try
			{
				this.display.update();
			}
			catch (IOException e)
			{
				logger.log(Level.SEVERE, "LCD컨트롤 오류", e);
			}
		});
	}
	
	private boolean[][] stringToBitMap(String s)
	{
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
