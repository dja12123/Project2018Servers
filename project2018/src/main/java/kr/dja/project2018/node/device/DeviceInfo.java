package kr.dja.project2018.node.device;

import java.net.Inet4Address;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.UUID;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import kr.dja.project2018.node.IServiceModule;
import kr.dja.project2018.node.db.DB_Handler;

public class DeviceInfo implements IServiceModule
{
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
		//this.dbHandler.executeQuery(INFO_TABLE_SCHEMA);
		CachedRowSet rs = this.dbHandler.query("select * from sqlite_master;");
		System.out.println(DB_Handler.isExist(rs, INFO_TABLE_SCHEMA, 5));
		DB_Handler.printResultSet(rs);
		return true;
	}

	@Override
	public void stop()
	{
		// TODO Auto-generated method stub
		
	}
	
}
