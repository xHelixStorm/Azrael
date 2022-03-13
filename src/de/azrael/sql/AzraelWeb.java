package de.azrael.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.util.STATIC;

public class AzraelWeb {
private static final Logger logger = LoggerFactory.getLogger(AzraelWeb.class);
	
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
			myConn = STATIC.getDatabaseURL(4);
			stmt = myConn.prepareStatement(AzraelWebStatements.SQLInsertActionLog);
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
	
	public static int SQLInsertLoginInfo(long user_id, int type, String code) {
		logger.trace("SQLInsertLoginInfo launched. Passed params {}, {}, {}", user_id, type, code);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = STATIC.getDatabaseURL(4);
			stmt = myConn.prepareStatement(AzraelWebStatements.SQLInsertLoginInfo);
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
			myConn = STATIC.getDatabaseURL(4);
			stmt = myConn.prepareStatement(AzraelWebStatements.SQLCodeUsageLog);
			stmt.setLong(1, user_id);
			stmt.setString(2, address);
			final int result = stmt.executeUpdate();
			if(result == 0) {
				stmt = myConn.prepareStatement(AzraelWebStatements.SQLCodeUsageLog2);
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
	
	public static int SQLgetLoginType(long user_id, long bot_id) {
		logger.trace("SQLgetLoginType launched. Passed params {}, {}", user_id, bot_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = STATIC.getDatabaseURL(4);
			stmt = myConn.prepareStatement(AzraelWebStatements.SQLgetLoginType);
			stmt.setLong(1, user_id);
			stmt.setLong(2, bot_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetLoginType Exception", e);
			return -1;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */}
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLisDefaultBot(long bot_id) {
		logger.trace("SQLisDefaultBot launched. Passed params {}", bot_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = STATIC.getDatabaseURL(4);
			stmt = myConn.prepareStatement(AzraelWebStatements.SQLisDefaultBot);
			stmt.setLong(1, bot_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getBoolean(1);
			}
			return false;
		} catch (SQLException e) {
			logger.error("SQLisDefaultBot Exception", e);
			return false;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */}
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
}
