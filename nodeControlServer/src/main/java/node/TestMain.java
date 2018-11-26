package node;

import java.io.IOException;
import java.util.Scanner;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import node.util.observer.Observable;
import node.util.observer.Observer;
import node.web.WebEvent;
import node.web.WebManager;

public class TestMain
{
	public static void main(String[] args) throws InterruptedException
	{
		NodeControlCore.init();
		WebManager manager = new WebManager();
		manager.startModule();
		manager.webSocketHandler.addObserver("test", new Observer<WebEvent>() {
			
			@Override
			public void update(Observable<WebEvent> object, WebEvent data) {
				System.out.println(data.key);
				try {
					data.channel.send("Hello World!!");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
}
	
}