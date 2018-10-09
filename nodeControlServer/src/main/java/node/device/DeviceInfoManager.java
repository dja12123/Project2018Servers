package node.device;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import node.IServiceModule;
import node.db.DB_Handler;
import node.log.LogWriter;
import node.util.observer.Observable;

public class DeviceInfoManager extends Observable<DeviceStateChangeEvent> implements IServiceModule
{
	public static final Logger deviceInfoLogger = LogWriter.createLogger(DeviceInfoManager.class, "deviceInfo");
	
	private static final String INFO_TABLE_SCHEMA = 
			"CREATE TABLE deviceInfo("
		+		"device_id varchar(36)"
		+		")";
	
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
		this.dbHandler.getInstaller().checkAndCreateTable(INFO_TABLE_SCHEMA);
		return true;
	}

	@Override
	public void stopModule()
	{
		// TODO Auto-generated method stub
		
	}

}
