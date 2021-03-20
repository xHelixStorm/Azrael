package de.azrael.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Patchnote;
import de.azrael.fileManagement.IniFileReader;
import de.azrael.util.STATIC;

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
		logger.trace("SQLgetPrivatePatchnotes launched without params");
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Patchnotes", ip), username, password);
			stmt = myConn.prepareStatement(PatchnotesStatements.SQLgetPrivatePatchnotes);
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
		logger.trace("SQLgetPublicPatchnotes launched without params");
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Patchnotes", ip), username, password);
			stmt = myConn.prepareStatement(PatchnotesStatements.SQLgetPublicPatchnotes);
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
	
	public static void SQLInsertPublishedPatchnotes(long guild_id) {
		logger.trace("SQLInsertPublishedPatchnotes launched. Params passed {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Patchnotes", ip), username, password);
			stmt = myConn.prepareStatement(PatchnotesStatements.SQLInsertPublishedPatchnotes);
			stmt.setString(1, STATIC.getVersion());
			stmt.setLong(2, guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertPublishedPatchnotes Exception", e);
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLcheckPublishedPatchnotes(long guild_id) {
		logger.trace("SQLcheckPublishedPatchnotes launched. Params passed {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Patchnotes", ip), username, password);
			stmt = myConn.prepareStatement(PatchnotesStatements.SQLcheckPublishedPatchnotes);
			stmt.setString(1, STATIC.getVersion());
			stmt.setLong(2, guild_id);
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
	
	public static long SQLgetGuild(long guild_id) {
		logger.trace("SQLgetGuild launched. Passed params {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Patchnotes", ip), username, password);
			stmt = myConn.prepareStatement(PatchnotesStatements.SQLgetGuild);
			stmt.setLong(1, guild_id);
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
	
	public static int SQLInsertGuild(long guild_id, String name) {
		logger.trace("SQLInsertGuilds launched. Params passed {}, {}", guild_id, name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Patchnotes", ip), username, password);
			stmt = myConn.prepareStatement(PatchnotesStatements.SQLInsertGuild);
			stmt.setLong(1, guild_id);
			stmt.setString(2, name);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertGuilds Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLcheckPublishedBotPatchnotes(long guild_id) {
		logger.trace("SQLcheckPublishedBotPatchnotes launched. Params passed {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Patchnotes", ip), username, password);
			stmt = myConn.prepareStatement(PatchnotesStatements.SQLcheckPublishedBotPatchnotes);
			stmt.setLong(1, guild_id);
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
		logger.trace("SQLgetPrivatePatchnotesArray launched without params");
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<Patchnote> patchnotes = new ArrayList<Patchnote>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Patchnotes", ip), username, password);
			stmt = myConn.prepareStatement(PatchnotesStatements.SQLgetPrivatePatchnotesArray);
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
		logger.trace("SQLgetPublicPatchnotesArray launched without params");
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<Patchnote> patchnotes = new ArrayList<Patchnote>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Patchnotes", ip), username, password);
			stmt = myConn.prepareStatement(PatchnotesStatements.SQLgetPublicPatchnotesArray);
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
	
	public static ArrayList<Patchnote> SQLgetGamePatchnotesArray(long guild_id) {
		logger.trace("SQLgetGamePatchnotesArray launched. Params passed {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<Patchnote> patchnotes = new ArrayList<Patchnote>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Patchnotes", ip), username, password);
			stmt = myConn.prepareStatement(PatchnotesStatements.SQLgetGamePatchnotesArray);
			stmt.setLong(1, guild_id);
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
