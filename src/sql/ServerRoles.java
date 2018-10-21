package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import core.Hashes;
import core.Roles;
import fileManagement.IniFileReader;

public class ServerRoles {
	
	private static long guild_id = 0;
	private static String guild_name = null;
	private static long role_id = 0;
	private static String role_name = null;
	private static String category_abv = null;
	private static String category_name = null;
	
	private static ArrayList<Roles> roles = new ArrayList<Roles>();
	
	private static String username = IniFileReader.getSQLUsername3();
	private static String password = IniFileReader.getSQLPassword3();
	
	public static void SQLconnection(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void SQLInsertGuild(long _guild_id, String _guild_name){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/DiscordRoles?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO guilds(guild_id, name) VALUES(?,?) ON DUPLICATE KEY UPDATE name=VALUES(name)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setString(2, _guild_name);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetRole(long _guild_id, String _category_abv){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/DiscordRoles?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT role_id, name FROM roles WHERE fk_guild_id = ? && fk_category_abv LIKE ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setString(2, _category_abv);
			rs = stmt.executeQuery();
			if(rs.next()){
				setRole_ID(rs.getLong(1));
				setRole_Name(rs.getString(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertRole(long _guild_id, long _role_id, String _role_name, String _category_abv){
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
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetCategory(long _role_id, long _guild_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/DiscordRoles?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT role_category.category_abv, role_category.rank FROM roles INNER JOIN role_category ON roles.fk_category_abv = role_category.category_abv WHERE role_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _role_id);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setCategory_ABV(rs.getString(1));
				setCategory_Name(rs.getString(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetRoles(long _guild_id){
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
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetRolesByCategory(long _guild_id, String channel_type){
		if(Hashes.getRoles(1+"_"+_guild_id) == null) {
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/DiscordRoles?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT guilds.guild_id, guilds.name, roles.role_id, roles.name, role_category.category_abv, role_category.rank FROM guilds INNER JOIN roles ON guilds.guild_id = roles.fk_guild_id INNER JOIN role_category ON roles.fk_category_abv = role_category.category_abv WHERE guild_id = ? AND role_category.category_abv LIKE ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				stmt.setString(2, channel_type);
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
					i++;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
	}
	
	public static void SQLgetCategories(){
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
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void setGuild_ID(long _guild_id){
		guild_id = _guild_id;
	}
	public static void setGuild_Name(String _guild_name){
		guild_name = _guild_name;
	}
	public static void setRole_ID(long _role_id){
		role_id = _role_id;
	}
	public static void setRole_Name(String _role_name){
		role_name = _role_name;
	}
	public static void setCategory_ABV(String _category_abv){
		category_abv = _category_abv;
	}
	public static void setCategory_Name(String _category_name){
		category_name = _category_name;
	}
	
	public static long getGuild_ID(){
		return guild_id;
	}
	public static String getGuild_Name(){
		return guild_name;
	}
	public static ArrayList<Roles> getRoles_ID(){
		return roles;
	}
	public static long getRole_ID(){
		return role_id;
	}
	public static String getRole_Name(){
		return role_name;
	}
	public static String getCategory_ABV(){
		return category_abv;
	}
	public static String getCategory_Name(){
		return category_name;
	}
	
	public static void clearRolesArray(){
		roles.clear();
	}
	public static void clearAllVariables(){
		setGuild_ID(0);
		setGuild_Name("");
		setRole_ID(0);
		setRole_Name("");
		setCategory_ABV("");
	}
}
