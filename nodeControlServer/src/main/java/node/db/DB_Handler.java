package node.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import org.sqlite.JDBC;
import org.sqlite.SQLiteConfig;

import node.IServiceModule;
import node.NodeControlCore;
import node.util.tablebuilder.Row;
import node.util.tablebuilder.StringTableBuilder;
import node.log.LogWriter;

/**
 * @FileName : DB_Handler.java
 * @Project : Project2018Servers
 * @Date : 2018. 9. 23.
 * @작성자 : dja12123
 * @변경이력 :
 * @프로그램 설명 :
 */
public class DB_Handler implements IServiceModule
{
	public static final String PROP_DB_FILE = "databaseFile";
	private final String TABLE_SEARCH_QUERY = "select * from sqlite_master %s;";
	private final String TABLE_CHECK_QUERY = "PRAGMA table_info(%s);";

	public static final Logger databaseLogger = LogWriter.createLogger(DB_Handler.class, "db");

	private static final String Variable_Property_Schema = "CREATE TABLE deviceInfo(" + "class_path varchar(128), "
			+ "key varchar(128), " + "value varchar(128))";

	private Connection connection;
	private SQLiteConfig config;
	private boolean isOpened = false;

	static
	{// test22
		try
		{
			// test11
			Class.forName("org.sqlite.JDBC");
		}
		catch (Exception e)
		{
			databaseLogger.log(Level.SEVERE, "JDBC 로드 실패", e);
		}
	}

	public DB_Handler()
	{
		this.config = new SQLiteConfig();
	}

	// 실행'만' 하는 쿼리(테이블 생성, 컬럼 삭제 등)
	public boolean executeQuery(String query)
	{
		if (!this.isOpened)
			return false;
		PreparedStatement prep = null;
		try
		{
			prep = this.connection.prepareStatement(query);
			prep.execute();
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.SEVERE, "질의 실패(" + query + ")", e);
			return false;
		}
		return true;
	}

	// 결과가 나오는 쿼리 (select문)
	public CachedRowSet query(String query)
	{
		if (!this.isOpened)
		{
			databaseLogger.log(Level.SEVERE, "세션 닫힘");
			return null;
		}
		CachedRowSet crs = null;
		PreparedStatement prep = null;
		ResultSet rs = null;

		try
		{
			prep = this.connection.prepareStatement(query);
			rs = prep.executeQuery();
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.SEVERE, "질의 실패(" + query + ")", e);
			return null;
		}
		try
		{
			crs = RowSetProvider.newFactory().createCachedRowSet();
			crs.populate(rs);
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.SEVERE, "CachedRowSet 만들기 실패", e);
		}

		return crs;
	}

	@Override
	public boolean startModule()
	{
		if (this.isOpened)
			this.stopModule();
		String path = DB_Handler.class.getProtectionDomain().getCodeSource().getLocation().getPath()
				+ NodeControlCore.getProp(PROP_DB_FILE);
		databaseLogger.log(Level.INFO, "데이터베이스 열기 (" + path + ")");
		try
		{
			this.connection = DriverManager.getConnection(JDBC.PREFIX + path, this.config.toProperties());
			this.connection.setAutoCommit(true);
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.SEVERE, "데이터베이스 열기 실패", e);
			return false;
		}

		this.isOpened = true;
		this.checkAndCreateTable(Variable_Property_Schema);
		return true;
	}

	@Override
	public void stopModule()
	{
		if (!this.isOpened)
			return;

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

	public void storeKeyValue(Class<?> classPath, String key, String value)
	{
		String module = classPath.toString();

		// this.executeQuery("insert into ")
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
			CachedRowSet rs = query(String.format(TABLE_SEARCH_QUERY,
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
			CachedRowSet rs = query(String.format(TABLE_CHECK_QUERY, tableName));
			HashMap<String, String> dic = new HashMap<>();

			while (rs.next())
			{
				dic.put(rs.getString(2).toLowerCase(),
						String.format("%s,%d,%d", rs.getString(3), rs.getInt(4), rs.getInt(6)));
			}

			int rowCount = rs.last() ? rs.getRow() : 0;

			String rawStruct = query.substring(query.indexOf("("), query.length());

			executeQuery(String.format("CREATE TABLE tmp_%s%s", tableName, rawStruct));
			rs = query(String.format(TABLE_CHECK_QUERY, "tmp_" + tableName));
			executeQuery(String.format("DROP TABLE tmp_%s;", tableName));

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

	public static void printResultSet(CachedRowSet rs)
	{// https://gist.github.com/jimjam88/8559599
		databaseLogger.log(Level.INFO, "-- ResultSet INFO --");
		StringTableBuilder tb = new StringTableBuilder("No", "");
		try
		{
			if (!rs.isBeforeFirst())
				rs.beforeFirst();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();

			for (int i = 1; i <= columnsNumber; ++i)
			{
				tb.addHeadData(rsmd.getColumnName(i));
			}

			for (int i = 1; rs.next(); ++i)
			{
				Row r = tb.addRow(String.valueOf(i));
				for (int j = 1; j <= columnsNumber; ++j)
				{
					r.put(rs.getString(j));
				}
			}
			rs.beforeFirst();
		}
		catch (Exception e)
		{
			databaseLogger.log(Level.WARNING, "프린트 오류", e);
		}
		System.out.println(tb.build());
	}

	public void checkAndCreateTable(String schema)
	{
        databaseLogger.log(Level.INFO, "테이블 확인시작(" + schema + ")");
		if (checkTable(schema))
		{
            if(!checkStruct(schema))
            {
                String dropQuery = String.format("drop table %s", getTableName(schema));
                query(dropQuery);
                databaseLogger.log(Level.INFO, "테이블 삭제(" + dropQuery + ")");
                executeQuery(schema);
                databaseLogger.log(Level.INFO, "테이블 재생성(" + schema + ")");
                return;
            }
            return;
		}
        
        executeQuery(schema);
        databaseLogger.log(Level.INFO, "테이블 생성(" + schema + ")");
	}

	/*
	 * public void checkAndCreateTable(String schema) { String nativeSQL = null; try
	 * { nativeSQL = this.connection.nativeSQL(schema); } catch (SQLException e) {
	 * // TODO Auto-generated catch block e.printStackTrace(); } String str =
	 * nativeSQL.toString(); System.out.println(str);
	 * 
	 * 
	 * CachedRowSet rs; rs =
	 * this.query("select tbl_name from sqlite_master where lower(sql) = lower('"
	 * +schema+"')"); if(rs.size() == 0) { System.out.println("Helo");
	 * //asdasdsadsaddsasdadsa
	 * /*if(this.query("select tbl_name from sqlite_master where tbl_name = lower("+
	 * +")")) {
	 * 
	 * }
	 */
	/*
	 * databaseLogger.log(Level.INFO, "테이블 생성("+schema+")");
	 * this.executeQuery(schema); return; } String[][] result = toArray(rs);
	 * databaseLogger.log(Level.INFO, "테이블 확인("+result[0][0]+")"); }
	 */

	public static boolean isExist(CachedRowSet rs, String key, int col)
	{
		try
		{
			if (!rs.isBeforeFirst())
				rs.beforeFirst();
			int columnsNumber = rs.getMetaData().getColumnCount();
			if (!(col > 0 && col <= columnsNumber))
			{
				databaseLogger.log(Level.WARNING, "검색 column no 오류(1~" + columnsNumber + " input:" + col);
				return false;
			}
			while (rs.next())
			{
				if (rs.getString(col).equals(key))
				{
					rs.beforeFirst();
					return true;
				}
			}
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.WARNING, "검색 오류", e);
		}
		return false;
	}

	public static String[][] toArray(CachedRowSet rs)
	{
		LinkedList<String[]> list = null;
		try
		{
			if (!rs.isBeforeFirst())
				rs.beforeFirst();
			list = new LinkedList<String[]>();
			int columnsNumber = rs.getMetaData().getColumnCount();

			while (rs.next())
			{
				String[] rowArr = new String[columnsNumber];
				for (int i = 1; i <= columnsNumber; ++i)
				{
					rowArr[i - 1] = rs.getString(i);
				}
				list.add(rowArr);
			}
			rs.beforeFirst();
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.WARNING, "toArray 오류", e);
			return null;
		}
		String[][] arr = new String[list.size()][];
		list.toArray(arr);
		return arr;
	}
}