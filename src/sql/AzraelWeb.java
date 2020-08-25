package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fileManagement.IniFileReader;
import util.STATIC;

public class AzraelWeb {
private static final Logger logger = LoggerFactory.getLogger(AzraelWeb.class);
	
	private static String ip = IniFileReader.getSQLWebIP();
	private static String username = IniFileReader.getSQLWebUsername();
	private static String password = IniFileReader.getSQLWebPassword();
	
	
	public static void SQLconnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			logger.error("JDBC driver couldn't be loaded", e);
		}
	}
	
	public static int SQLInsertLoginInfo(String _name, long _user_id, String _avatar, int _type) {
		logger.trace("SQLInsertLoginInfo launched. Passed params {}, {}, {}, {}", _name, _user_id, _avatar, _type);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("AzraelWeb", ip), username, password);
			String sql = ("INSERT INTO login (name, user_id, avatar, type) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE user_id=VALUES(user_id), avatar=VALUES(avatar), type=VALUES(type)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _name);
			stmt.setLong(2, _user_id);
			stmt.setString(3, _avatar);
			stmt.setInt(4, _type);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertLoginInfo Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
}
