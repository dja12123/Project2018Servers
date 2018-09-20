package project2018.node.device;

import java.net.Inet4Address;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import project2018.node.IServiceModule;
import project2018.node.NodeControlCore;
import project2018.node.db.DB_Handler;

public class DeviceInfo implements IServiceModule
{
	public static final Logger deviceInfoLogger = NodeControlCore.createLogger(DB_Handler.class.getName().toLowerCase(), "deviceInfo");
	
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
	public boolean start()
	{
		CachedRowSet rs;
		rs = this.dbHandler.query("select * from sqlite_master;");
		
		if(!DB_Handler.isExist(rs, INFO_TABLE_SCHEMA, 5));
		{
			deviceInfoLogger.log(Level.WARNING, "데이터베이스 스키마 없음, 재생성");
			this.dbHandler.executeQuery(INFO_TABLE_SCHEMA);
		}
		
		rs = this.dbHandler.query("select * from deviceInfo");
		
		
		
			
		return true;
	}

	@Override
	public void stop()
	{
		// TODO Auto-generated method stub
		
	}
	
}
