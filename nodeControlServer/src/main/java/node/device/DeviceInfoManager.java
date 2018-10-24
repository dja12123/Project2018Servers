package node.device;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.rowset.CachedRowSet;

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
	
	private static final String INFO_TABLE_SCHEMA = 
			"CREATE TABLE device_info("
		+		"uuid varchar(36) primary key,"
		+		"inet_addr varchar(15),"
		+		"update_time datetime"
		+		")";
	
	
	public static final int CHECK_INTERVAL = 5000;
	public static final int TIMEOUT = 3000;
	
	private Device myDevice;
	private DB_Handler dbHandler;
	private HashMap<UUID, Device> deviceMap;
	
	private Thread manageThread;
	private boolean isRun;
	
	public static void main(String[] args)
	{
		NodeControlCore.init();
		DB_Handler db = new DB_Handler();
		DeviceInfoManager infoManager = new DeviceInfoManager(db);
		db.startModule();
		
		infoManager.startModule();
		
		db.getInstaller().complete();
		String prop = db.getOrSetDefaultVariableProperty(DB_Handler.class, "test", "1");
		db.setVariableProperty(DB_Handler.class, "test", String.valueOf(Integer.valueOf(prop) + 1));
		System.out.println(prop);
	}
	
	public DeviceInfoManager(DB_Handler dbhandler)
	{
		this.dbHandler = dbhandler;
		this.deviceMap = new HashMap<UUID, Device>();
	}
	
	public Device getDevice(UUID uuid)
	{
		return this.deviceMap.getOrDefault(uuid, null);
	}
	
	public boolean deviceExist(UUID uuid)
	{
		return this.deviceMap.containsKey(uuid);
	}
	
	public Device[] getDevices()
	{
		Device[] deviceArr = new Device[this.deviceMap.size()];
		this.deviceMap.values().toArray(deviceArr);
		return deviceArr;
	}
	
	@Override
	public boolean startModule()
	{
		if(this.isRun) return true;
		deviceInfoLogger.log(Level.INFO, "노드 정보 관리 서비스 시작");
		String uidStr = this.dbHandler.getOrSetDefaultVariableProperty(this.getClass(), VP_MYDEVICE_INFO, UUID.randomUUID().toString());
		UUID myUUID = UUID.fromString(uidStr);

		this.myDevice = new Device(myUUID);
		
		this.manageThread.start();
		return true;
	}

	@Override
	public void stopModule()
	{
		if(!this.isRun) return;
		deviceInfoLogger.log(Level.INFO, "노드 정보 관리 서비스 종료");
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
	
	public synchronized void updateDevice(UUID uuid, InetAddress inetAddr, boolean isMasterNode)
	{// 장치 정보관리 모듈과 연결해줌.
	 // 장치 정보가 수정되었을 때.
		Device device = this.deviceMap.getOrDefault(uuid, null);
		if(device == null)
		{
			device = new Device(uuid);
			this.deviceMap.put(uuid, device);
			DeviceStateChangeEvent eventObj = new DeviceStateChangeEvent(DeviceStateChangeEvent.CONNECT_NEW_DEVICE, device);
			this.notifyObservers(NodeControlCore.mainThreadPool, eventObj);
		}
		else
		{
			int changeState = 0;
			if(!device.inetAddr.equals(inetAddr))
			{
				changeState = changeState | DeviceStateChangeEvent.CHANGE_INETADDR;
				device.inetAddr = inetAddr;
			}
			
			if(device.masterNode != isMasterNode)
			{
				if(device.masterNode)
				{
					changeState = changeState | DeviceStateChangeEvent.IS_MASTER_NODE;
				}
				else
				{
					changeState = changeState | DeviceStateChangeEvent.IS_NOT_MASTER_NODE;
				}
			}
			
			if(changeState != 0)
			{
				DeviceStateChangeEvent eventObj = new DeviceStateChangeEvent(changeState, device);
				this.notifyObservers(NodeControlCore.mainThreadPool, eventObj);
			}
			
		}
		device.updateTime = new Date(System.currentTimeMillis());
	}
	
	public synchronized void removeDevice(UUID uuid)
	{
		this.deviceMap.remove(uuid);
		DeviceStateChangeEvent eventObj = new DeviceStateChangeEvent(DeviceStateChangeEvent.DISCONNECT_DEVICE, this.getDevice(uuid));
		this.notifyObservers(NodeControlCore.mainThreadPool, eventObj);
	}
	
	@Override
	public void run()
	{// 장치가 타임아웃 됬을 경우를 감지.
		Date compareTime;
		LinkedList<Device> removeDevices = new LinkedList<Device>();
		while(this.isRun)
		{
			synchronized (this)
			{
				compareTime = new Date(System.currentTimeMillis() + TIMEOUT);
				removeDevices.clear();
				
				for(Device device : this.getDevices())
				{
					if(device.getUpdateTime().after(compareTime))
					{//타임아웃일때
						removeDevices.add(device);
					}
				}
				
				for(Device device : removeDevices)
				{
					this.removeDevice(device.uuid);
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
