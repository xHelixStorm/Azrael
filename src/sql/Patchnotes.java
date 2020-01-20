package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Patchnote;
import fileManagement.IniFileReader;
import util.STATIC;

public class Patchnotes {
	private static final Logger logger = LoggerFactory.getLogger(Patchnotes.class);
	
	private static String ip = IniFileReader.getSQLIP4();
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
		logger.info("SQLgetPrivatePatchnotes launched without params");
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Patchnotes", ip), username, password);
			String sql = ("SELECT message1, message2, date FROM priv_notes WHERE version_number LIKE ?");
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
		logger.info("SQLgetPublicPatchnotes launched without params");
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Patchnotes", ip), username, password);
			String sql = ("SELECT message1, message2, date FROM publ_notes WHERE version_number LIKE ?");
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
	
	public static void SQLInsertPublishedPatchnotes(long _guild_id) {
		logger.info("SQLInsertPublishedPatchnotes launched. Params passed {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Patchnotes", ip), username, password);
			String sql = ("INSERT INTO published (fk_version_number, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, STATIC.getVersion());
			stmt.setLong(2, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertPublishedPatchnotes Exception", e);
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLcheckPublishedPatchnotes(long _guild_id) {
		logger.info("SQLcheckPublishedPatchnotes launched. Params passed {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Patchnotes", ip), username, password);
			String sql = ("SELECT * FROM published WHERE fk_version_number LIKE ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, STATIC.getVersion());
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				return true;
			}
			return false;
		} catch (SQLException e) {
			logger.error("SQLcheckPublishedPatchnotes Exception", e);
			return false;
		} finally {
		  try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static long SQLgetGuild(long _guild_id) {
		logger.info("SQLgetGuild launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Patchnotes", ip), username, password);
			String sql = ("SELECT guild_id FROM guilds WHERE guild_id= ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getLong(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetGuild Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertGuild(long _guild_id, String _name) {
		logger.info("SQLInsertGuilds launched. Params passed {}, {}", _guild_id, _name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Patchnotes", ip), username, password);
			String sql = ("INSERT INTO guilds (guild_id, name) VALUES(?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setString(2, _name);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertGuilds Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLcheckPublishedBotPatchnotes(long _guild_id) {
		logger.info("SQLcheckPublishedBotPatchnotes launched. Params passed {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Patchnotes", ip), username, password);
			String sql = ("SELECT * FROM published WHERE fk_guild_id = ? LIMIT 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				return true;
			}
			return false;
		} catch (SQLException e) {
			logger.error("SQLcheckPublishedBotPatchnotes Exception", e);
			return false;
		} finally {
		  try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Patchnote> SQLgetPrivatePatchnotesArray() {
		logger.info("SQLgetPrivatePatchnotesArray launched without params");
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<Patchnote> patchnotes = new ArrayList<Patchnote>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Patchnotes", ip), username, password);
			String sql = ("SELECT message1, message2, date, version_number FROM priv_notes ORDER BY date desc LIMIT 10");
			stmt = myConn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while(rs.next()){
				patchnotes.add(
					new Patchnote(
						rs.getString(1),
						rs.getString(2),
						rs.getString(3),
						rs.getString(4)
					)
				);
			}
			if(patchnotes.size() > 0)
				return patchnotes;
			else
				return null;
		} catch (SQLException e) {
			logger.error("SQLgetPrivatePatchnotesArray Exception", e);
			return null;
		} finally {
		  try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Patchnote> SQLgetPublicPatchnotesArray() {
		logger.info("SQLgetPublicPatchnotesArray launched without params");
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<Patchnote> patchnotes = new ArrayList<Patchnote>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Patchnotes", ip), username, password);
			String sql = ("SELECT message1, message2, date, version_number FROM publ_notes ORDER BY date desc LIMIT 10");
			stmt = myConn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while(rs.next()){
				patchnotes.add(
					new Patchnote(
						rs.getString(1),
						rs.getString(2),
						rs.getString(3),
						rs.getString(4)
					)
				);
			}
			if(patchnotes.size() > 0)
				return patchnotes;
			else
				return null;
		} catch (SQLException e) {
			logger.error("SQLgetPublicPatchnotesArray Exception", e);
			return null;
		} finally {
		  try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Patchnote> SQLgetGamePatchnotesArray(long _guild_id) {
		logger.info("SQLgetGamePatchnotesArray launched. Params passed {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<Patchnote> patchnotes = new ArrayList<Patchnote>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Patchnotes", ip), username, password);
			String sql = ("SELECT message1, message2, date, title FROM game_notes WHERE fk_guild_id = ? ORDER BY date desc LIMIT 10");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			while(rs.next()){
				patchnotes.add(
					new Patchnote(
						rs.getString(1),
						rs.getString(2),
						rs.getString(3),
						rs.getString(4)
					)
				);
			}
			if(patchnotes.size() > 0)
				return patchnotes;
			else
				return null;
		} catch (SQLException e) {
			logger.error("SQLgetPrivatePatchnotesArray Exception", e);
			return null;
		} finally {
		  try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
}
