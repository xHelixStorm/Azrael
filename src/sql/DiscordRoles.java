package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import core.Roles;
import fileManagement.IniFileReader;

public class DiscordRoles {
	private static final Logger logger = LoggerFactory.getLogger(DiscordRoles.class);
	
	private static String username = IniFileReader.getSQLUsername3();
	private static String password = IniFileReader.getSQLPassword3();
	
	public static void SQLconnection(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			logger.error("JDBC driver couldn't be loaded", e);
		}
	}
	
	public static int SQLInsertGuild(long _guild_id, String _guild_name){
		logger.debug("SQLInsertGuild launched. Passed params {}, {}", _guild_id, _guild_name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/DiscordRoles?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO guilds(guild_id, name) VALUES(?,?) ON DUPLICATE KEY UPDATE name=VALUES(name)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setString(2, _guild_name);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertGuild Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static long SQLgetRole(long _guild_id, String _category_abv){
		logger.debug("SQLgetRole launched. Passed params {}, {}", _guild_id, _category_abv);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/DiscordRoles?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT role_id FROM roles WHERE fk_guild_id = ? && fk_category_abv LIKE ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setString(2, _category_abv);
			rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getLong(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetRole Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertRole(long _guild_id, long _role_id, String _role_name, String _category_abv){
		logger.debug("SQLInsertRole launched. Passed params {}, {}, {}, {}", _guild_id, _role_id, _role_name, _category_abv);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/DiscordRoles?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO roles(role_id, name, fk_category_abv, fk_guild_id) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE name=VALUES(name), fk_category_abv=VALUES(fk_category_abv)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _role_id);
			stmt.setString(2, _role_name);
			stmt.setString(3, _category_abv);
			stmt.setLong(4, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertRole Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Roles> SQLgetRoles(long _guild_id){
		logger.debug("SQLgetRoles launched. Passed params {}", _guild_id);
		ArrayList<Roles> roles = new ArrayList<Roles>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/DiscordRoles?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT guilds.guild_id, guilds.name, roles.role_id, roles.name, role_category.category_abv, role_category.rank FROM guilds INNER JOIN roles ON guilds.guild_id = roles.fk_guild_id INNER JOIN role_category ON roles.fk_category_abv = role_category.category_abv WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			while(rs.next()){
				Roles roleDetails = new Roles();
				roleDetails.setGuild_ID(rs.getLong(1));
				roleDetails.setGuild_Name(rs.getString(2));
				roleDetails.setRole_ID(rs.getLong(3));
				roleDetails.setRole_Name(rs.getString(4));
				roleDetails.setCategory_ABV(rs.getString(5));
				roleDetails.setCategory_Name(rs.getString(6));
				roles.add(roleDetails);
				Hashes.addDiscordRole(roleDetails.getRole_ID(), roleDetails);
			}
			return roles;
		} catch (SQLException e) {
			logger.error("SQLgetRoles Exception", e);
			return roles;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLgetRolesByCategory(long _guild_id, String _channel_type){
		logger.debug("SQLgetRolesByCategory launched. Passed params {}, {}", _guild_id, _channel_type);
		if(Hashes.getRoles(1+"_"+_guild_id) == null) {
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/DiscordRoles?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT guilds.guild_id, guilds.name, roles.role_id, roles.name, role_category.category_abv, role_category.rank FROM guilds INNER JOIN roles ON guilds.guild_id = roles.fk_guild_id INNER JOIN role_category ON roles.fk_category_abv = role_category.category_abv WHERE guild_id = ? AND role_category.category_abv LIKE ?");
				boolean success = false;
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				stmt.setString(2, _channel_type);
				rs = stmt.executeQuery();
				int i = 1;
				while(rs.next()){
					Roles roleDetails = new Roles();
					roleDetails.setGuild_ID(rs.getLong(1));
					roleDetails.setGuild_Name(rs.getString(2));
					roleDetails.setRole_ID(rs.getLong(3));
					roleDetails.setRole_Name(rs.getString(4));
					roleDetails.setCategory_ABV(rs.getString(5));
					roleDetails.setCategory_Name(rs.getString(6));
					Hashes.addRoles(i+"_"+_guild_id, roleDetails);
					success = true;
					i++;
				}
				return success;
			} catch (SQLException e) {
				logger.error("SQLgetRolesByCategory Exception", e);
				return false;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return true;
	}
	
	public static ArrayList<Roles> SQLgetCategories(){
		logger.debug("SQLgetCategories launched. No params passed");
		ArrayList<Roles> roles = new ArrayList<Roles>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/DiscordRoles?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM role_category");
			stmt = myConn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while(rs.next()){
				Roles roleDetails = new Roles();
				roleDetails.setCategory_ABV(rs.getString(1));
				roleDetails.setCategory_Name(rs.getString(2));
				roles.add(roleDetails);
			}
			return roles;
		} catch (SQLException e) {
			logger.error("SQLgetCategories Exception", e);
			return roles;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
}
