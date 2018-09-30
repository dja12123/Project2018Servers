package node.device;

import java.net.Inet4Address;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import node.IServiceModule;
import node.NodeControlCore;
import node.db.DB_Handler;
import node.log.LogWriter;

public class DeviceInfo implements IServiceModule
{
	public static final Logger deviceInfoLogger = LogWriter.createLogger(DeviceInfo.class, "deviceInfo");
	
	private static final String INFO_TABLE_SCHEMA = 
				"CREATE TABLE deviceInfo("
			+		"device_id varchar(36)"
			+		")";
	
	private DB_Handler dbHandler;
	
	public DeviceInfo(DB_Handler dbHandler)
	{
		this.dbHandler = dbHandler;
	}

	@Override
	public boolean startModule()
	{
		this.dbHandler.checkAndCreateTable(INFO_TABLE_SCHEMA);
		return true;
	}

	@Override
	public void stopModule()
	{
		// TODO Auto-generated method stub
		
	}
	
}
