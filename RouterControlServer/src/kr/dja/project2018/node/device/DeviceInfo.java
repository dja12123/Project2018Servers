package kr.dja.project2018.node.device;

import kr.dja.project2018.node.IServiceModule;
import kr.dja.project2018.node.db.DB_Handler;

public class DeviceInfo implements IServiceModule
{
	private DB_Handler dbHandler;
	
	public DeviceInfo(DB_Handler dbHandler)
	{
		this.dbHandler = dbHandler;
		
	}

	@Override
	public boolean start()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void stop()
	{
		// TODO Auto-generated method stub
		
	}
	
}
