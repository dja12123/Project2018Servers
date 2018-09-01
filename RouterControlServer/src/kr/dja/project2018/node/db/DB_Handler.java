package kr.dja.project2018.node.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sqlite.JDBC;
import org.sqlite.SQLiteConfig;

import kr.dja.project2018.node.IServiceModule;
import kr.dja.project2018.node.NodeControlCore;

public class DB_Handler implements IServiceModule
{
	public static final String PROP_DB_FILE = "databaseFile";
	
	public static final Logger databaseLogger = NodeControlCore.createLogger(DB_Handler.class.getName().toLowerCase(), "db");
	private Connection connection;
	private SQLiteConfig config;
	private boolean isOpened = false;
	
	static
	{
		try
		{
			Class.forName("org.sqlite.JDBC");
		}
		catch (Exception e)
		{
			databaseLogger.log(Level.SEVERE, "JDBC 로드 실패", e);
		}
	}
	
	public DB_Handler()
	{
		databaseLogger.log(Level.INFO, "데이터베이스 로드");
		this.config = new SQLiteConfig();
		System.out.println();
	}
	
	public boolean executeQuery(String query)
	{
		if(!this.isOpened) return false;
		PreparedStatement prep = null;
		try
		{
			prep = this.connection.prepareStatement(query);
			prep.execute();
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.SEVERE, "질의 실패("+query+")", e);
			return false;
		}
		return true;
	}
	
	public ResultSet query(String query)
	{
		if(!this.isOpened) return null;
		PreparedStatement prep = null;
		ResultSet rs = null;
		try
		{
			prep = this.connection.prepareStatement(query);
			rs = prep.executeQuery();
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.SEVERE, "질의 실패("+query+")", e);
			return null;
		}
		return rs;
	}
	
	@Override
	public boolean start()
	{
		if(this.isOpened) this.stop();
		String path = DB_Handler.class.getProtectionDomain().getCodeSource().getLocation().getPath()+
				NodeControlCore.getProp(PROP_DB_FILE);
		databaseLogger.log(Level.INFO, "데이터베이스 열기 ("+path+")");
		try
		{
			this.connection = DriverManager.getConnection(JDBC.PREFIX+path, this.config.toProperties());
			this.connection.setAutoCommit(true);
		}
		catch(SQLException e)
		{
			databaseLogger.log(Level.SEVERE, "데이터베이스 열기 실패", e);
			return false;
		}
		this.isOpened = true;
		return true;
	}

	@Override
	public void stop()
	{
		if (!this.isOpened) return;

		try
		{
			this.connection.close();
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.SEVERE, "데이터베이스 닫기 실패", e);
		}
		this.isOpened = false;

	}
}
