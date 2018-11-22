package node.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.sql.SQLException;
import node.log.LogWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.CachedRowSet;

import node.db.DB_Handler;

public class DB_Installer
{
	private final String TABLE_SEARCH_QUERY = "select * from sqlite_master %s;";
	private final String TABLE_CHECK_QUERY = "PRAGMA table_info(%s);";

	public static final Logger databaseLogger = LogWriter.createLogger(DB_Installer.class, "db_install");

	private DB_Handler instance = null;
	private ArrayList<String> _LtableName = new ArrayList<>();

	DB_Installer(DB_Handler instance)
	{
		this.instance = instance;
		getDBTableList();
	}

	private void getDBTableList()
	{
		CachedRowSet rs = instance.query(String.format(TABLE_SEARCH_QUERY, "where type = 'table'"));
		try
		{
			rs.beforeFirst();

			while (rs.next())
			{
				_LtableName.add(rs.getString(3));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	private String getTableName(String rawQuery)
	{
		rawQuery = rawQuery.replaceAll("\\s+", " ");
		try
		{
			return rawQuery.substring(0, rawQuery.indexOf("(")).split(" ")[2];
		}
		catch (IndexOutOfBoundsException e)
		{
			e.printStackTrace();
		}

		return "";
	}

	private boolean checkTable(String query)
	{
		try
		{
			CachedRowSet rs = instance.query(String.format(TABLE_SEARCH_QUERY,
					String.format("where %s = '%s'", "tbl_name", getTableName(query))));

			if ((rs.last() ? rs.getRow() : 0) == 0)
				return false;
		}
		catch (SQLException e)
		{
			// err message HERE!
		}

		return true;
	}

	private boolean checkStruct(String query)
	{
		try
		{
			String tableName = getTableName(query);
			CachedRowSet rs = instance.query(String.format(TABLE_CHECK_QUERY, tableName));
			HashMap<String, String> dic = new HashMap<>();

			while (rs.next())
			{
				dic.put(rs.getString(2).toLowerCase(),
						String.format("%s,%d,%d", rs.getString(3), rs.getInt(4), rs.getInt(6)));
			}

			int rowCount = rs.last() ? rs.getRow() : 0;

			String rawStruct = query.substring(query.indexOf("("), query.length());

			instance.executeQuery(String.format("CREATE TABLE tmp_%s%s", tableName, rawStruct));
			rs = instance.query(String.format(TABLE_CHECK_QUERY, "tmp_" + tableName));
			instance.executeQuery(String.format("DROP TABLE tmp_%s;", tableName));

			if (rowCount != (rs.last() ? rs.getRow() : 0))
				return false;

			rs.beforeFirst();
			while (rs.next())
			{
				String key = rs.getString(2).toLowerCase();
				if (dic.containsKey(key))
				{
					String dest = dic.get(key);

					String[] temp = dest.split(",");

					String type = temp[0];
					int notNull = Integer.valueOf(temp[1]);
					int pk = Integer.valueOf(temp[2]);

					if (!(type.equals(rs.getString(3)) && notNull == rs.getInt(4) && pk == rs.getInt(6)))
						return false;
				}
				else
					return false;
			}
		}
		catch (SQLException e)
		{
			// err message HERE!
		}
		return true;
	}

	public void checkAndCreateTable(String schema)
	{
		databaseLogger.log(Level.INFO, "테이블 확인(" + schema + ")");
		String tableName = getTableName(schema);
		if (_LtableName.contains(tableName))
			_LtableName.remove(tableName);

		if (checkTable(schema))
		{
			if (!checkStruct(schema))
			{
				instance.executeQuery(String.format("drop table %s", getTableName(schema)));
				instance.executeQuery(schema);
				databaseLogger.log(Level.INFO, "테이블 재생성(" + schema + ")");
			}
			return;
		}
		instance.executeQuery(schema);
		databaseLogger.log(Level.INFO, "테이블 생성(" + schema + ")");
	}

	public void complete()
	{
		String[] appArray = new String[_LtableName.size()];
		appArray = _LtableName.toArray(appArray);

		for (String var : appArray)
		{
			databaseLogger.log(Level.INFO, String.format("테이블 삭제: %s", var));
			instance.executeQuery(String.format("DROP TABLE IF EXISTS %s", var));
		}

		databaseLogger.log(Level.INFO, "DB모듈 초기화 완료.");
	}
}