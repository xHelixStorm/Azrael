package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
	
	public static int SQLInsertActionLog(long user_id, String address, String event, String info) {
		logger.trace("SQLInsertLoginInfo launched. Passed params {}, {}, {}, {}", user_id, address, event, info);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("AzraelWeb", ip), username, password);
			String sql = ("INSERT INTO action_log (user_id, address, event, info) VALUES (?, ?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setString(2, address);
			stmt.setString(3, event);
			stmt.setString(4, info);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertLoginInfo Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertLoginInfo(long user_id, int type) {
		logger.trace("SQLInsertLoginInfo launched. Passed params {}, {}", user_id, type);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("AzraelWeb", ip), username, password);
			String sql = ("INSERT INTO login (user_id, type) VALUES (?, ?) ON DUPLICATE KEY UPDATE type=VALUES(type)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setInt(2, type);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertLoginInfo Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertLoginInfo(long user_id, int type, String code) {
		logger.trace("SQLInsertLoginInfo launched. Passed params {}, {}, {}", user_id, type, code);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("AzraelWeb", ip), username, password);
			String sql = ("INSERT INTO login (user_id, type, code) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE code=VALUES(code)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setInt(2, type);
			stmt.setString(3, code);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertLoginInfo Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static void SQLCodeUsageLog(long user_id, String address) {
		logger.trace("SQLCodeUsageLog launched. Passed params {}", user_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("AzraelWeb", ip), username, password);
			String sql = ("UPDATE code_usage_log SET count = (count+1) WHERE user_id = ? AND address = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setString(2, address);
			final int result = stmt.executeUpdate();
			if(result == 0) {
				sql = ("INSERT INTO code_usage_log (user_id, address, count) VALUES (?, ?, 1)");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, user_id);
				stmt.setString(2, address);
				stmt.executeUpdate();
			}
		} catch (SQLException e) {
			logger.error("SQLCodeUsageLog Exception", e);
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetLoginType(long user_id) {
		logger.trace("SQLgetLoginType launched. Passed params {}", user_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("AzraelWeb", ip), username, password);
			String sql = ("SELECT auth_type FROM users WHERE user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetLoginType Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
}
