package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Patchnote;
import fileManagement.IniFileReader;
import util.STATIC;

public class Patchnotes {
	private static final Logger logger = LoggerFactory.getLogger(Patchnotes.class);
	
	private static String username = IniFileReader.getSQLUsername4();
	private static String password = IniFileReader.getSQLPassword4();
		
	public static void SQLconnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			logger.error("JDBC driver couldn't be loaded", e);
		}
	}
	
	public static Patchnote SQLgetPrivatePatchnotes() {
		logger.debug("SQLgetPrivatePatchnotes launched without params");
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Patchnotes?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT message1, message2, date FROM priv_notes WHERE version_number LIKE ? AND published = 0");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, STATIC.getVersion());
			rs = stmt.executeQuery();
			if(rs.next()){
				return new Patchnote(
					rs.getString(1),
					rs.getString(2),
					rs.getString(3)
				);
			}
			return null;
		} catch (SQLException e) {
			logger.error("SQLgetPrivatePatchnotes Exception", e);
			return null;
		} finally {
		  try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static Patchnote SQLgetPublicPatchnotes() {
		logger.debug("SQLgetPublicPatchnotes launched without params");
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Patchnotes?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT message1, message2, date FROM publ_notes WHERE version_number LIKE ? AND published = 0");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, STATIC.getVersion());
			rs = stmt.executeQuery();
			if(rs.next()){
				return new Patchnote(
					rs.getString(1),
					rs.getString(2),
					rs.getString(3)
				);
			}
			return null;
		} catch (SQLException e) {
			logger.error("SQLgetPublicPatchnotes Exception", e);
			return null;
		} finally {
		  try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdatePrivPatchnotesPublished() {
		logger.debug("SQLUpdatePrivPatchnotesPublished launched without params");
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Patchnotes?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE priv_notes SET published = 1 WHERE version_number LIKE ? AND published = 0");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, STATIC.getVersion());
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdatePrivPatchnotesPublished Exception", e);
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdatePublPatchnotesPublished() {
		logger.debug("SQLUpdatePublPatchnotesPublished launched without params");
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Patchnotes?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE publ_notes SET published = 1 WHERE version_number LIKE ? AND published = 0");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, STATIC.getVersion());
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdatePublPatchnotesPublished Exception", e);
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
}
