package node.gpio.led;

import com.diozero.ws281xj.LedDriverInterface;

public class LEDControlInst
{
	public static final int STATE_LOW = 2;
	public static final int STATE_HIGH = 1;
	public static final int STATE_NORMAL = 0;
	public static final int STATE_END = -1;
	
	private LedDriverInterface led_driver;
	final int pixel;
	final int lightTime;
	final int blackTime;
	int repeat;
	final int r, g, b;
	final int br, bg, bb;
	
	private int time;
	private boolean isLight;
	
	LEDControlInst(LedDriverInterface led_driver, int pixel, int lightTime, int blackTime, int repeat, int r, int g, int b, int br, int bg, int bb)
	{
		this.led_driver = led_driver;
		this.pixel = pixel;
		this.lightTime = lightTime;
		this.blackTime = blackTime;
		this.repeat = repeat;
		this.r = r;
		this.g = g;
		this.b = b;
		this.br = br;
		this.bg = bg;
		this.bb = bb;
		
		this.isLight = true;
		this.time = this.lightTime;
		this.led_driver.setPixelColourRGB(this.pixel, this.r, this.g, this.b);
		this.led_driver.render();
	}
	
	int calcLED()
	{
		if(this.repeat == 0)
		{// -1일경우 계속 작동.
			this.led_driver.setPixelColourRGB(this.pixel, 0, 0, 0);
			this.led_driver.render();
			return STATE_END;
			
		}
		
		if(this.isLight)
		{
			if(this.time > 0)
			{
				--this.time;
			}
			else
			{//led off
				this.led_driver.setPixelColourRGB(this.pixel, this.br, this.bg, this.bb);
				this.led_driver.render();
				
				this.isLight = false;
				this.time = this.blackTime;
				return STATE_LOW;
			}
		}
		else
		{
			if(this.time > 0)
			{
				--this.time;
			}
			else
			{// led on
				this.led_driver.setPixelColourRGB(this.pixel, this.r, this.g, this.b);
				this.led_driver.render();
				
				this.isLight = true;
				this.time = this.lightTime;
				if(this.repeat != -1)
				{
					--this.repeat;
				}
				return STATE_HIGH;
			}
		}
		return STATE_NORMAL;
	}
	
	boolean setLight()
	{
		if(this.isLight)
		{
			//led_driver.setPixelColourRGB(this.pixel, this.r, this.g, this.b);
			//led_driver.render();
			return true;
		}
		return false;
	}
}
