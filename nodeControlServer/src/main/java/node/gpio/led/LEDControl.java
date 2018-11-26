package node.gpio.led;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import node.log.LogWriter;
import node.network.NetworkManager;


public class LEDControl implements Runnable
{
	public static final Logger logger = LogWriter.createLogger(LEDControl.class, "LEDControl");
	
	public static final int NUM_LED = 4;
	private static final int SLEEP_TIME = 10;
	private static final double LIGHT = 0.5;

	public static final LEDControl ledControl = new LEDControl();

	private I2CDevice i2cDevice;
	
	private LEDControlInst[] infControllers;
	private ArrayList<LEDControlInst> controllers;

	private Thread worker;

	private LEDControl()
	{
		
		this.i2cDevice = null;
		try
		{
			I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
			this.i2cDevice = bus.getDevice(0x04);
		}
		catch (UnsupportedBusNumberException | IOException e)
		{
			logger.log(Level.SEVERE, "LED컨트롤 초기화 오류", e);
			return;
		}
		
		this.infControllers = new LEDControlInst[NUM_LED];
		this.controllers = new ArrayList<>();

		this.worker = new Thread(this);
		this.worker.start();
	}
	
	public LEDControlInst setDefaultFlick(int pixel, int lightTime, int blackTime, int r,
			int g, int b, int br, int bg, int bb)
	{// LED의 기본 플리크 상태를 설정
		return this.flick(pixel, lightTime, blackTime, -1, r, g, b, br, bg, bb);
	}

	public synchronized LEDControlInst flick(int pixel, int lightTime, int blackTime, int repeat, int r,
			int g, int b, int br, int bg, int bb)
	{// LED번호, 켜지는시간, 꺼지는시간, 반복횟수, 켜짐색, 꺼짐색
		if(this.i2cDevice == null)
		{
			return null;
		}
		lightTime /= SLEEP_TIME;
		blackTime /= SLEEP_TIME;
		r = (int)(LIGHT * r);
		g = (int)(LIGHT * g);
		b = (int)(LIGHT * b);
		br = (int)(LIGHT * br);
		bg = (int)(LIGHT * bg);
		bb = (int)(LIGHT * bb);
		LEDControlInst controlInst = new LEDControlInst(this.i2cDevice, pixel, lightTime, blackTime, repeat, r, g, b,
				br, bg, bb);
		if (repeat == -1)
		{
			this.infControllers[pixel] = controlInst;
		}
		else
		{
			this.controllers.add(controlInst);
		}

		return controlInst;
	}

	public LEDControlInst flick(int pixel, int lightTime, int blackTime, int repeat, int r, int g, int b)
	{// 꺼짐색이 000
		return this.flick(pixel, lightTime, blackTime, repeat, r, g, b, 0, 0, 0);
	}
	
	public LEDControlInst flick(int pixel, int time, int repeat, int r, int g, int b)
	{// 켜지는시간, 꺼지는시간 설정 없음
		return this.flick(pixel, time, time, repeat, r, g, b, 0, 0, 0);
	}
	
	public LEDControlInst flick(int pixel, int lightTime, int r, int g, int b)
	{// 지정한 시간만큼 LED점등
		return this.flick(pixel, lightTime, 0, 1, r, g, b, 0, 0, 0);
	}

	public synchronized void killLEDControl(LEDControlInst inst)
	{
		if (inst.repeat == -1)
		{
			this.killInfLED(inst.pixel);
			return;
		}

		int index = this.controllers.indexOf(inst);
		if (index == -1)
		{
			return;
		}
		this.controllers.get(index).killLED();
		this.controllers.remove(index);
	}

	public synchronized void killInfLED(int pixel)
	{
		if(this.infControllers[pixel] != null)
		{
			this.infControllers[pixel].killLED();
		}
		
		this.infControllers[pixel] = null;
	}
	
	@Override
	public void run()
	{
		boolean[] ledState = new boolean[NUM_LED];
		int updateResult;
		while (true)
		{
			synchronized (this)
			{
				for (int i = 0; i < NUM_LED; ++i)
				{
					ledState[i] = false;
				}

				for (int i = this.controllers.size() - 1; i >= 0; --i)
				{
					LEDControlInst inst = this.controllers.get(i);
					
					updateResult = inst.calcLED();
					if (updateResult == LEDControlInst.STATE_HIGH)
					{
						ledState[inst.pixel] = true;
					}
					else if (updateResult == LEDControlInst.STATE_END)
					{
						this.controllers.remove(i);
					}
				}
				for (int i = 0; i < NUM_LED; ++i)
				{
					if (ledState[i] == true)
					{
						continue;
					}
					for (int j = 0; j < this.controllers.size(); ++j)
					{
						LEDControlInst inst = this.controllers.get(j);
						if (inst.pixel == i)
						{
							if (inst.setLight())
							{
								ledState[i] = true;
							}

						}
					}
				}
				for (int i = 0; i < NUM_LED; ++i)
				{
					if (ledState[i] == true || this.infControllers[i] == null)
					{
						continue;
					}
					this.infControllers[i].calcLED();
				}
				try
				{
					Thread.sleep(SLEEP_TIME);
				}
				catch (InterruptedException e)
				{
					logger.log(Level.SEVERE, "인터럽트", e);
				}
			}
		}
	}
}