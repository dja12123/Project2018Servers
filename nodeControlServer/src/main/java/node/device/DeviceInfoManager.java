package node.device;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
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

public class DeviceInfoManager extends Observable<DeviceStateChangeEvent> implements IServiceModule
{
	public static final Logger deviceInfoLogger = LogWriter.createLogger(DeviceInfoManager.class, "deviceInfo");
	public static final String VP_MYDEVICE_INFO = "myuuid";
	
	private static final String INFO_TABLE_SCHEMA = 
			"CREATE TABLE device_info("
		+		"uuid varchar(36) primary key,"
		+		"inet_addr varchar(15),"
		+		"update_time datetime"
		+		")";
	
	private Device myDevice;
	private DB_Handler dbHandler;
	HashMap<UUID, Device> deviceMap;
	
	private Thread manageThread;
	
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
	
	public Collection<Device> getDevices()
	{
		return this.deviceMap.values();
	}
	
	@Override
	public boolean startModule()
	{
		
		String uidStr = this.dbHandler.getOrSetDefaultVariableProperty(this.getClass(), VP_MYDEVICE_INFO, UUID.randomUUID().toString());
		UUID myUUID = UUID.fromString(uidStr);

		this.myDevice = new Device(myUUID);
		
		this.manageThread.start();
		return true;
	}

	@Override
	public void stopModule()
	{
		
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

}
