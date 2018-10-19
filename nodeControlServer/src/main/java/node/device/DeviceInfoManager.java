package node.device;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.IServiceModule;
import node.NodeControlCore;
import node.db.DB_Handler;
import node.log.LogWriter;
import node.network.packet.PacketBuilder;
import node.network.packet.PacketUtil;
import node.util.observer.Observable;

public class DeviceInfoManager extends Observable<DeviceStateChangeEvent> implements IServiceModule, Runnable
{
	public static final Logger deviceInfoLogger = LogWriter.createLogger(DeviceInfoManager.class, "deviceInfo");
	public static final String VP_MYDEVICE_INFO = "myuuid";
	public static final int CHECK_INTERVAL = 5000;
	public static final int TIMEOUT = 3000;
	
	private static final String INFO_TABLE_SCHEMA = 
			"CREATE TABLE device_info("
		+		"uuid varchar(36),"
		+		"inet_addr varchar(15),"
		+		"update_time datetime"
		+		")";
	
	private Device myDevice;
	private DB_Handler dbHandler;
	private HashMap<UUID, Device> deviceMap;
	
	private Thread manageThread;
	private boolean isRun;
	
	public static void main(String[] args)
	{
		
	}
	
	public DeviceInfoManager(DB_Handler dbhandler)
	{
		this.dbHandler = dbhandler;
		this.deviceMap = new HashMap<UUID, Device>();
	}
	
	public synchronized void updateDevice(UUID uuid, InetAddress inetAddr)
	{
		Device device = this.deviceMap.getOrDefault(uuid, null);
		if(device == null)
		{
			device = new Device(uuid);
			this.deviceMap.put(uuid, device);
			
			DeviceStateChangeEvent eventObj = new DeviceStateChangeEvent(DeviceStateChangeEvent.CONNECT_NEW_DEVICE, device);
			this.notifyObservers(NodeControlCore.mainThreadPool, eventObj);
		}
		device.setInetAddr(inetAddr);
		device.updateTime();
	}
	
	public Device getDevice(UUID uuid)
	{
		return this.deviceMap.getOrDefault(uuid, null);
	}
	
	@Override
	public boolean startModule()
	{
		if(this.isRun) return true;
		deviceInfoLogger.log(Level.INFO, "장치 정보 관리모듈 활성화");
		this.dbHandler.getInstaller().checkAndCreateTable(INFO_TABLE_SCHEMA);
		String uidStr = this.dbHandler.getVariableProperty(this.getClass(), VP_MYDEVICE_INFO);
		UUID myUUID;
		
		if(uidStr == null)
		{
			myUUID = UUID.randomUUID();
			uidStr = myUUID.toString();
			this.dbHandler.setVariableProperty(this.getClass(), VP_MYDEVICE_INFO, uidStr);
		}
		else
		{
			myUUID = UUID.fromString(uidStr);
		}
		this.myDevice = new Device(myUUID);
		
		this.manageThread = new Thread(this);
		this.isRun = true;
		this.manageThread.run();
		return true;
	}

	@Override
	public void stopModule()
	{
		if(!this.isRun) return;
		deviceInfoLogger.log(Level.INFO, "장치 정보 관리모듈 종료");
		this.isRun = false;
		this.manageThread.interrupt();
	}
	
	public String[][] getDeviceIPTable()
	{
		String[][] queryArr = DB_Handler.toArray(this.dbHandler.query("select uuid, inet_addr from device_info"));
		return queryArr;
	}

	public Device getMyDevice()
	{
		return this.myDevice;
	}

	@Override
	public void run()
	{
		Date compareTime;
		LinkedList<Device> removeDevices = new LinkedList<Device>();
		while(this.isRun)
		{
			synchronized (this)
			{
				compareTime = new Date(System.currentTimeMillis() + TIMEOUT);
				removeDevices.clear();

				for(Device device : this.deviceMap.values())
				{
					if(device.getUpdateTime().after(compareTime))
					{//타임아웃일때
						removeDevices.add(device);
					}
				}
				
				for(Device device : removeDevices)
				{
					this.deviceMap.remove(device.uuid);
				}
			}

			try
			{
				Thread.sleep(CHECK_INTERVAL);
			}
			catch (InterruptedException e)
			{
				break;
			}
		}
	}
}
