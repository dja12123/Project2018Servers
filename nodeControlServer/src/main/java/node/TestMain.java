package node;

import java.io.IOException;
import java.util.Scanner;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class TestMain
{
	public static void main(String[] args) throws InterruptedException
	{
		Scanner sc = new Scanner(System.in);
		
		try
		{
			System.out.println("Creatingbus");
			I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
			System.out.println("Creatingdevice");
			I2CDevice device = bus.getDevice(0x04);

			byte[] writeData = new byte[8];
			
			while (true)
			{
				System.out.println("Input");
				int no = sc.nextInt();
				int r = sc.nextInt();
				int g = sc.nextInt();
				int b = sc.nextInt();
				writeBuf(writeData, no, r, g, b);
				device.write(writeData);
				System.out.println("Waitingconds");

			}
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		catch (UnsupportedBusNumberException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	static void writeBuf(byte[] buffer, int pixel, int r, int g, int b)
	{
		buffer[0] = 0x00;
		buffer[1] =	(byte)pixel;
		buffer[2] = 0x00;
		buffer[3] =	(byte)r;
		buffer[4] = 0x00;
		buffer[5] =	(byte)g;
		buffer[6] = 0x00;
		buffer[7] =	(byte)b;
	}
}