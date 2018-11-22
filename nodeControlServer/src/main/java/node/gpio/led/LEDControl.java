package node.gpio.led;

import java.util.ArrayList;

import com.diozero.ws281xj.LedDriverInterface;
import com.diozero.ws281xj.PixelAnimations;
import com.diozero.ws281xj.StripType;
import com.diozero.ws281xj.spi.WS281xSpi;

public class LEDControl
{
	public static final int NUM_LED = 4;
	private static final int SLEEP_TIME = 10;

	public static final LEDControl ledControl = new LEDControl();

	private LedDriverInterface ledDriver;
	private LEDControlInst[] infControllers;
	private ArrayList<LEDControlInst> controllers;

	private Thread worker;

	private LEDControl()
	{
		System.out.println("시작");
		this.infControllers = new LEDControlInst[NUM_LED];
		this.controllers = new ArrayList<>();

		this.worker = new Thread(this::run);
		this.worker.start();
	}

	public synchronized LEDControlInst createLEDControl(int pixel, int lightTime, int blackTime, int repeat, int r,
			int g, int b, int br, int bg, int bb)
	{
		if(this.ledDriver == null)
		{
			try
			{
				this.wait();
			}
			catch (InterruptedException e)
			{

				e.printStackTrace();
			}
		}
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

	public LEDControlInst createLEDControl(int pixel, int lightTime, int blackTime, int repeat, int r, int g, int b)
	{
		return this.createLEDControl(pixel, lightTime, blackTime, repeat, r, g, b, 0, 0, 0);
	}

	public synchronized void killLEDControl(LEDControlInst inst)
	{
		if (inst.getRepeat() == -1)
		{
			this.infControllers[inst.pixel()] = null;
			return;
		}

		int index = this.controllers.indexOf(inst);
		if (index == -1)
		{
			return;
		}
		this.controllers.remove(index);
	}

	public synchronized void killInfLED(int pixel)
	{
		this.infControllers[pixel] = null;
	}

	private void run()
	{
		try (LedDriverInterface iface = new WS281xSpi(2, 0, StripType.WS2812, NUM_LED, 255))
		{
			this.ledDriver = iface;
			synchronized (this)
			{
				this.notifyAll();
			}
			
			System.out.println(Thread.currentThread().getName());

			for (int i = 0; i < NUM_LED; ++i)
			{
				this.ledDriver.setPixelColourRGB(i, 255, 255, 255);
			}
			this.ledDriver.render();

			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e1)
			{
				e1.printStackTrace();
			}
			this.ledDriver.allOff();

			boolean[] isUpdateLOW = new boolean[NUM_LED];
			boolean[] isLight = new boolean[NUM_LED];
			while (true)
			{

				for (int i = 0; i < NUM_LED; ++i)
				{
					isUpdateLOW[i] = false;
					isLight[i] = false;
				}
				synchronized (this)
				{
					System.out.println(this.controllers.size());
					for (int i = this.controllers.size() - 1; i >= 0; --i)
					{
						LEDControlInst inst = this.controllers.get(i);
						int updateResult = inst.update();
						if (updateResult == LEDControlInst.STATE_CHANGE_LOW)
						{
							isUpdateLOW[inst.pixel()] = true;
						}
						else if (updateResult == LEDControlInst.STATE_END)
						{
							System.out.println("kill");
							isUpdateLOW[inst.pixel()] = true;
							this.controllers.remove(i);
						}
					}
					for (int i = 0; i < NUM_LED; ++i)
					{
						if (isUpdateLOW[i] != true)
						{
							continue;
						}
						for (int j = 0; j < this.controllers.size(); ++i)
						{
							LEDControlInst inst = this.controllers.get(j);
							if (inst.pixel() == i)
							{
								if (inst.setLight())
								{
									isLight[i] = true;
								}

							}
						}
					}
					for (int i = 0; i < NUM_LED; ++i)
					{
						if (this.infControllers[i] == null || isUpdateLOW[i] != true || isLight[i] == true)
						{
							continue;
						}
						this.infControllers[i].update();
					}
				}
				try
				{
					Thread.sleep(SLEEP_TIME);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
