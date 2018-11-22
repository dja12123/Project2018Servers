package node.gpio.led;

import com.diozero.ws281xj.LedDriverInterface;

public class LEDControlInst
{
	public static int STATE_CHANGE_LOW = 100;
	public static int STATE_CHANGE_HIGH = 1;
	public static int STATE_NORMAL = 0;
	public static int STATE_END = -1;
	
	private LedDriverInterface led_driver;
	private int pixel;
	private int lightTime;
	private int blackTime;
	private int repeat;
	private int r, g, b;
	private int br, bg, bb;
	
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
		
		this.isLight = false;
		this.time = 0;
	}
	
	int getRepeat()
	{
		return this.repeat;
	}
	
	int pixel()
	{
		return this.pixel;
	}
	
	int calcLED()
	{
		if(this.repeat == 0)
		{// -1일경우 계속 작동.
			System.out.println("return");
			return STATE_END;
			
		}
		int rtnValue = STATE_NORMAL;
		
		if(this.isLight)
		{
			if(this.time > 0)
			{
				--this.time;
			}
			else
			{//led off
				//this.led_driver.setPixelColourRGB(this.pixel, this.br, this.bg, this.bb);
				//this.led_driver.render();
				
				this.isLight = false;
				this.time = this.blackTime;
				System.out.println("LEDOFF");
				rtnValue = STATE_CHANGE_LOW;
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
				//this.led_driver.setPixelColourRGB(this.pixel, this.r, this.g, this.b);
				//this.led_driver.render();
				
				this.isLight = true;
				this.time = this.lightTime;
				if(this.repeat != -1)
				{
					--this.repeat;
				}
				System.out.println("LEDON");
				rtnValue = STATE_CHANGE_HIGH;
			}
		}

		return rtnValue;
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
