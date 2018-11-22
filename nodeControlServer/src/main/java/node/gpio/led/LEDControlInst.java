package node.gpio.led;

import de.cacodaemon.rpiws28114j.WS2811;
import de.pi3g.pi.ws2812.WS2812;

public class LEDControlInst
{
	public static final int STATE_LOW = 2;
	public static final int STATE_HIGH = 1;
	public static final int STATE_NORMAL = 0;
	public static final int STATE_END = -1;
	
	private final WS2812 device;
	final int pixel;
	final int lightTime;
	final int blackTime;
	int repeat;
	final byte r, g, b;
	final byte br, bg, bb;
	
	private int time;
	private boolean isLight;
	
	LEDControlInst(WS2812 device, int pixel, int lightTime, int blackTime, int repeat, byte r, byte g, byte b, byte br, byte bg, byte bb)
	{
		this.device = device;
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
		device.setPixelColor(this.pixel, this.r, this.g, this.b);
		device.show();
	}
	
	int calcLED()
	{
		if(this.repeat == 0)
		{// -1일경우 계속 작동.
			this.killLED();
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
				//this.led_driver.setPixelColourRGB(this.pixel, this.br, this.bg, this.bb);
				//this.led_driver.render();
				
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
				//this.led_driver.setPixelColourRGB(this.pixel, this.r, this.g, this.b);
				//this.led_driver.render();
				
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
	
	void killLED()
	{
		//this.led_driver.setPixelColourRGB(this.pixel, 0, 0, 0);
		//this.led_driver.render();
	}
	
	private static int rgbToInt(int r, int g, int b)
	{
		int rgb = r;
		rgb = (rgb << 8) + g;
		rgb = (rgb << 8) + b;
		return rgb;
	}
}
