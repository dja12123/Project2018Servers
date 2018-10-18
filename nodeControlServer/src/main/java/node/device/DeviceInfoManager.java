package node.device;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import node.IServiceModule;
import node.db.DB_Handler;
import node.log.LogWriter;
import node.network.packet.PacketBuilder;
import node.network.packet.PacketUtil;
import node.util.observer.Observable;

public class DeviceInfoManager extends Observable<DeviceStateChangeEvent> implements IServiceModule
{
	public static final Logger deviceInfoLogger = LogWriter.createLogger(DeviceInfoManager.class, "deviceInfo");
	public static final String VP_MYDEVICE_INFO = "myuuid";
	
	private static final String INFO_TABLE_SCHEMA = 
			"CREATE TABLE device_info("
		+		"uuid varchar(36),"
		+		"inet_addr varchar(15)"
		+		")";
	
	private Device myDevice;
	private DB_Handler dbHandler;
	private HashMap<UUID, Device> deviceMap;
	
	public DeviceInfoManager(DB_Handler dbhandler)
	{
		this.dbHandler = dbhandler;
		this.deviceMap = new HashMap<UUID, Device>();
	}
	
	public void addDevice(UUID uuid)
	{
		
		DeviceStateChangeEvent e = new DeviceStateChangeEvent();
		this.notifyObservers(e);
	}
	
	public void getDevice(UUID uuid)
	{
		
	}
	
	public void removeDevice(UUID uuid)
	{
		
	}
	
	@Override
	public boolean startModule()
	{
		this.myDevice = new Device(UUID.randomUUID());//TODO 디비에서 긁어올것
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
		
		return true;
	}

	@Override
	public void stopModule()
	{
		// TODO Auto-generated method stub
		
	}
	
	public String[][] getDeviceIPTable()
	{
		String[][] queryArr = DB_Handler.toArray(this.dbHandler.query("select uuid, inet_addr from device_info"));
		return queryArr;
	}

	public Device getMyDevice()
	{
		// TODO Auto-generated method stub
		return this.myDevice;
	}

}
