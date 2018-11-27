package node.gpio.led;

import java.io.IOException;
import java.util.logging.Level;

import com.pi4j.io.i2c.I2CDevice;

public class LEDControlInst
{
	public static final int STATE_LOW = 2;
	public static final int STATE_HIGH = 1;
	public static final int STATE_NORMAL = 0;
	public static final int STATE_END = -1;
	
	final int pixel;
	final int lightTime;
	final int blackTime;
	int repeat;
	final int r, g, b;
	final int br, bg, bb;
	
	private int time;
	private boolean isLight;
	private I2CDevice i2cDevice;
	private byte[] buffer;
	
	LEDControlInst(I2CDevice i2cDevice, int pixel, int lightTime, int blackTime, int repeat, int r, int g, int b, int br, int bg, int bb)
	{
		this.i2cDevice = i2cDevice;
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
		this.buffer = new byte[8];
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
				this.writeRGB(this.br, this.bg, this.bb);
				
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
				this.writeRGB(this.r, this.g, this.b);
				
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
			this.writeRGB(this.r, this.g, this.b);
			return true;
		}
		return false;
	}
	
	void killLED()
	{
		this.writeRGB(0, 0, 0);
	}
	
	private void writeRGB(int r, int g, int b)
	{
		this.buffer[0] = 0x00;
		this.buffer[1] =(byte)this.pixel;
		this.buffer[2] = 0x00;
		this.buffer[3] =(byte)r;
		this.buffer[4] = 0x00;
		this.buffer[5] =(byte)g;
		this.buffer[6] = 0x00;
		this.buffer[7] =(byte)b;
		try
		{
			this.i2cDevice.write(this.buffer);
		}
		catch (IOException e)
		{
			LEDControl.logger.log(Level.SEVERE, "I2C통신 오류", e);
		}
	}
}