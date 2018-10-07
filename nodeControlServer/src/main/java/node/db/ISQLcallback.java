package node.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ISQLcallback
{
	void callback(PreparedStatement prep) throws SQLException;
}
