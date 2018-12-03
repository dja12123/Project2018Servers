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
import node.gpio.lcd.LCDControl;
import node.gpio.lcd.LCDObject;
import node.log.LogWriter;
import node.network.NetworkManager;
import node.network.protocol.keyvaluePacket.PacketBuilder;
import node.network.protocol.keyvaluePacket.PacketUtil;
import node.util.observer.Observable;

public class DeviceInfoManager extends Observable<DeviceChangeEvent> implements IServiceModule, Runnable
{
	public static final Logger logger = LogWriter.createLogger(DeviceInfoManager.class, "deviceInfo");
	public static final String VP_MYDEVICE_INFO = "myuuid";
	
	private static final String INFO_TABLE_SCHEMA = 
			"CREATE TABLE device_info("
		+		"uuid varchar(36) primary key,"
		+		"inet_addr varchar(15),"
		+		"update_time datetime"
		+		")";
	
	
	public static final String PROP_checkInterval = "checkInterval";
	public static final String PROP_nodeTimeout = "nodeTimeout";
	
	private int checkInterval;
	private int timeOut;
	
	private Device myDevice;
	private DB_Handler dbHandler;
	private HashMap<UUID, Device> deviceMap;
	
	private Thread manageThread;
	private boolean isRun;
	
	private LCDObject myUIDString;
	private LCDObject checkDeviceRect;
	
	/*public static void main(String[] args)
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
	}*/
	
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
		this.isRun = true;
		
		logger.log(Level.INFO, "노드 정보 관리 서비스 시작");

		
		this.checkInterval = Integer.parseInt(NodeControlCore.getProp(PROP_checkInterval));
		this.timeOut = Integer.parseInt(NodeControlCore.getProp(PROP_nodeTimeout));
		
		String uidStr = this.dbHandler.getOrSetDefaultVariableProperty(this.getClass(), VP_MYDEVICE_INFO, UUID.randomUUID().toString());
		UUID myUUID = UUID.fromString(uidStr);

		this.myDevice = new Device(myUUID);
		this.deviceMap.put(this.myDevice.uuid, this.myDevice);
		
		String myUID = this.myDevice.uuid.toString();
		logger.log(Level.INFO, String.format("my UUID: %s", myUID));
		myUID = myUID.substring(myUID.length() - 4, myUID.length() - 1);
		this.myUIDString = LCDControl.inst.showString(90, 15, String.format("ID:%s", myUID));
		this.checkDeviceRect = LCDControl.inst.showFillRect(0, 18, 5, 5);
		
		this.manageThread = new Thread(this);
		this.manageThread.start();
		return true;
	}

	@Override
	public void stopModule()
	{
		if(!this.isRun) return;
		logger.log(Level.INFO, "노드 정보 관리 서비스 종료");
		LCDControl.inst.removeShape(this.myUIDString);
		LCDControl.inst.removeShape(this.checkDeviceRect);
		this.deviceMap.clear();
		this.isRun = false;
		this.manageThread.interrupt();
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
			device.inetAddr = inetAddr;
			device.masterNode = isMasterNode;
			DeviceChangeEvent eventObj = new DeviceChangeEvent(DeviceChangeEvent.CONNECT_NEW_DEVICE, device);
			this.notifyObservers(NodeControlCore.mainThreadPool, eventObj);
		}
		else
		{
			int changeState = 0;
			if((device.inetAddr == null && inetAddr != null) ||
					(device.inetAddr != null && inetAddr == null) ||
					!device.inetAddr.equals(inetAddr))
			{
				changeState = changeState | DeviceChangeEvent.CHANGE_INETADDR;
				device.inetAddr = inetAddr;
			}
			
			if(device.masterNode != isMasterNode)
			{
				if(device.masterNode)
				{
					changeState = changeState | DeviceChangeEvent.IS_MASTER_NODE;
				}
				else
				{
					changeState = changeState | DeviceChangeEvent.IS_NOT_MASTER_NODE;
				}
			}
			
			if(changeState != 0)
			{
				DeviceChangeEvent eventObj = new DeviceChangeEvent(changeState, device);
				this.notifyObservers(NodeControlCore.mainThreadPool, eventObj);
			}
			
		}
		device.updateTime = new Date(System.currentTimeMillis());
	}
	
	public synchronized void removeDevice(UUID uuid)
	{
		if(uuid.equals(this.myDevice.uuid)) return;
		DeviceChangeEvent eventObj = new DeviceChangeEvent(DeviceChangeEvent.DISCONNECT_DEVICE, this.getDevice(uuid));
		this.deviceMap.remove(uuid);
		this.notifyObservers(NodeControlCore.mainThreadPool, eventObj);
		String uid = uuid.toString();
		logger.log(Level.INFO, String.format("노드 삭제(%s)", uid));
		uid = uid.substring(uid.length() - 4, uid.length() - 1);
		LCDControl.inst.removeShapeTimer(LCDControl.inst.showString(7, 15, String.format("노드삭제:%s", uid)), 2000);
	}
	
	public synchronized int getNodeCount()
	{
		return this.deviceMap.size();
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
				compareTime = new Date(System.currentTimeMillis() - this.timeOut);
				removeDevices.clear();
				
				for(Device device : this.getDevices())
				{
					if(device.equals(this.myDevice))
					{
						continue;
					}
					if(compareTime.after(device.updateTime))
					{//타임아웃일때
						removeDevices.add(device);
					}
				}
				
				for(Device device : removeDevices)
				{
					this.removeDevice(device.uuid);
				}
			}
			LCDControl.inst.blinkShape(this.checkDeviceRect, 300, 1);
			try
			{
				Thread.sleep(this.checkInterval);
			}
			catch (InterruptedException e)
			{
				break;
			}
		}
	}
}
