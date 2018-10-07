package node.network.nodeInit;

import java.sql.PreparedStatement;
import java.sql.Time;
import node.NodeControlCore;
import node.db.DB_Handler;
import node.network.communicator.INetworkObserver;
import node.network.communicator.NetworkEvent;
import node.network.communicator.SocketHandler;
import node.util.observer.Observable;

public class NodeScanner implements INetworkObserver
{
	private DB_Handler dbHandler;
	private SocketHandler socketHandler;
	
	public static void main(String[] args)
	{
		NodeControlCore.init();
		DB_Handler dbHandler = new DB_Handler();
		SocketHandler socketHandler = new SocketHandler();
		
		dbHandler.startModule();
		socketHandler.startModule();
		
		NodeScanner sc = new NodeScanner(dbHandler, socketHandler);
	}
	
	private static final String NODE_TABLE_SCHEMA = 
			"CREATE TABLE node_info("
		+		"uuid varchar(36) primary key,"
		+		"inetaddr varchar(15),"
		+ 		"updateTime datetime)";
	
	NodeScanner(DB_Handler dbHandler, SocketHandler socketHandler)
	{
		this.dbHandler = dbHandler;
		this.socketHandler = socketHandler;
	
		this.dbHandler.checkAndCreateTable(NODE_TABLE_SCHEMA);
		this.socketHandler.addObserver(InfoBroadcast.NODE_BROADCAST_MSG, this);
	}

	@Override
	public void update(Observable<NetworkEvent> object, NetworkEvent data)
	{
		if(data.key.equals(InfoBroadcast.NODE_BROADCAST_MSG))
		{
			String addr = data.inetAddr.getHostAddress();
			String uuid = data.packet.getSender().toString();
			Time time = new Time(System.currentTimeMillis());
			
			this.dbHandler.executeQuery("insert into node_info values(?, ?, ?)",(PreparedStatement prep)->
			{
				prep.setString(1, addr);
				prep.setString(2, uuid);
				prep.setTime(3, time);
			});
		}
	}
	
	public void stopScan()
	{
		this.socketHandler.removeObserver(this);
	}
}	
