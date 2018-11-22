package node.gpio.led;

import java.util.ArrayList;

import com.diozero.util.SleepUtil;
import com.diozero.ws281xj.LedDriverInterface;
import com.diozero.ws281xj.PixelAnimations;
import com.diozero.ws281xj.StripType;
import com.diozero.ws281xj.spi.WS281xSpi;

public class LEDControl implements Runnable
{
	public static final int NUM_LED = 4;
	private static final int SLEEP_TIME = 10;
	private static final int LIGHT = 128;

	public static final LEDControl ledControl = new LEDControl();

	private LedDriverInterface ledDriver;
	private LEDControlInst[] infControllers;
	private ArrayList<LEDControlInst> controllers;

	private Thread worker;

	private LEDControl()
	{
		System.out.println("시작");
		this.ledDriver = new WS281xSpi(2, 0, StripType.WS2812, NUM_LED, LIGHT);
		this.ledDriver.allOff();

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
		lightTime /= SLEEP_TIME;
		blackTime /= SLEEP_TIME;
		LEDControlInst controlInst = new LEDControlInst(this.ledDriver, pixel, lightTime, blackTime, repeat, r, g, b,
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
				SleepUtil.sleepMillis(SLEEP_TIME);
			}
		}
	}
}
