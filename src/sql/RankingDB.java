package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import fileManagement.IniFileReader;
import inventory.Dailies;
import inventory.InventoryContent;
import rankingSystem.Rank;
import rankingSystem.Skins;

public class RankingDB {
	
	private static long user_id = 0;
	private static String user_name = null;
	private static int level_skin = 0;
	private static int rank_skin = 0;
	private static int profile_skin = 0;
	private static int icon_skin = 0;
	private static long guild_id = 0;
	private static String guild_name = null;
	private static long role_id = 0;
	private static String role_name = null;
	private static int role_level_requirement = 0;
	private static int ranking_level = 0;
	private static int ranking_rank = 0;
	private static int ranking_profile = 0;
	private static int ranking_icon = 0;
	private static boolean ranking_state = false;
	private static int max_level = 0;
	private static String description = null;
	private static int level = 0;
	private static int current_experience = 0;
	private static int rank_up_experience = 0;
	private static long experience = 0;
	private static long currency = 0;
	private static long assigned_role = 0;
	private static boolean enabled = false;
	private static long max_experience = 0;
	private static long daily_experience = 0;
	private static Date daily_reset = null;
	private static int item_id = 0;
	private static long price = 0;
	private static Date position = null;
	private static String skin_type = "";
	private static int weight = 0;
	private static int number = 0;
	private static Date opened = null;
	private static Date next_daily = null;
	private static Date expiration = null;
	private static int number_limit = 0;
	private static String status = null;
	private static int color_profile = 0;
	private static int color_rank = 0;
	private static boolean exp_and_percent_profile = false;
	private static boolean exp_and_percent_rank = false;
	private static int item_number = 0;
	private static int text_color_r_profile = 0;
	private static int text_color_r_rank = 0;
	private static int text_color_r_level = 0;
	private static int text_color_g_profile = 0;
	private static int text_color_g_rank = 0;
	private static int text_color_g_level = 0;
	private static int text_color_b_profile = 0;
	private static int text_color_b_rank = 0;
	private static int text_color_b_level = 0;
	private static int rankx_level = 0;
	private static int ranky_level = 0;
	private static int rank_width_level = 0;
	private static int rank_height_level = 0;
	private static int rankx_rank = 0;
	private static int ranky_rank = 0;
	private static int rank_width_rank = 0;
	private static int rank_height_rank = 0;
	private static int rankx_profile = 0;
	private static int ranky_profile = 0;
	private static int rank_width_profile = 0;
	private static int rank_height_profile = 0;
	
	private static String level_description = "";
	private static String rank_description = "";
	private static String profile_description = "";
	private static String icon_description = "";
	
	private static ArrayList<Rank> rankList = new ArrayList<Rank>();
	private static ArrayList<Skins> set_skin = new ArrayList<Skins>();
	private static ArrayList<InventoryContent> inventory = new ArrayList<InventoryContent>();
	private static ArrayList<Dailies> dailies = new ArrayList<Dailies>();
	
	private static String username = IniFileReader.getSQLUsername2();
	private static String password = IniFileReader.getSQLPassword2();
	
	public static void SQLconnection(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	//users table
	public static void SQLgetUser(long _user_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM users WHERE user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setUserID(rs.getLong(1));
				setUserName(rs.getString(2));
				setLevelSkin(rs.getInt(3));
				setRankSkin(rs.getInt(4));
				setProfileSkin(rs.getInt(5));
				setIconSkin(rs.getInt(6));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertUser(long _user_id, String _name, int _level_skin, int _rank_skin, int _profile_skin, int _icon_skin){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO users (user_id, name, level_skin, rank_skin, profile_skin, icon_skin) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setString(2, _name);
			stmt.setInt(3, _level_skin);
			stmt.setInt(4, _rank_skin);
			stmt.setInt(5, _profile_skin);
			stmt.setInt(6, _icon_skin);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateUserLevelSkin(long _user_id, String _name, int _skin_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE users SET level_skin = ?, name = ? WHERE user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _skin_id);
			stmt.setString(2, _name);
			stmt.setLong(3, _user_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateUserRankSkin(long _user_id, String _name, int _skin_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE users SET rank_skin = ?, name = ? WHERE user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _skin_id);
			stmt.setString(2, _name);
			stmt.setLong(3, _user_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateUserProfileSkin(long _user_id, String _name, int _skin_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE users SET profile_skin = ?, name = ? WHERE user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _skin_id);
			stmt.setString(2, _name);
			stmt.setLong(3, _user_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateUserIconSkin(long _user_id, String _name, int _skin_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE users SET icon_skin = ?, name = ? WHERE user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _skin_id);
			stmt.setString(2, _name);
			stmt.setLong(3, _user_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//guilds table 
	public static void SQLgetGuild(long _guild_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM guilds WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setGuildID(rs.getLong(1));
				setGuildName(rs.getString(2));
				setMaxLevel(rs.getInt(3));
				setRankingLevel(rs.getInt(4));
				setRankingRank(rs.getInt(5));
				setRankingProfile(rs.getInt(6));
				setRankingIcon(rs.getInt(7));
				setRankingState(rs.getBoolean(8));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertGuild(long _guild_id, String _name, int _max_level, int _level_skin, int _rank_skin, int _profile_skin, int _icon_skin, boolean _enabled){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO guilds (guild_id, name, max_level, fk_level_id, fk_rank_id, fk_profile_id, fk_icon_id, ranking_state) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), max_level=VALUES(max_level), fk_profile_id=VALUES(fk_profile_id), ranking_state=VALUES(ranking_state)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setString(2, _name);
			stmt.setInt(3, _max_level);
			stmt.setInt(4, _level_skin);
			stmt.setInt(5, _rank_skin);
			stmt.setInt(6, _profile_skin);
			stmt.setInt(7, _icon_skin);
			stmt.setBoolean(8, _enabled);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateMaxLevel(long _guild_id, String _guild_name, int _max_level){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE guilds SET name = ?, max_level = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _guild_name);
			stmt.setInt(2, _max_level);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateRankingSystem(long _guild_id, String _guild_name, boolean _ranking_state){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE guilds SET name = ?, ranking_state = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _guild_name);
			stmt.setBoolean(2, _ranking_state);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}	
	}
	
	public static void SQLUpdateLevelDefaultSkin(long _guild_id, String _guild_name, int _level_skin){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE guilds SET name = ?, fk_level_id = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _guild_name);
			stmt.setInt(2, _level_skin);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}	
	}
	
	public static void SQLUpdateRankDefaultSkin(long _guild_id, String _guild_name, int _rank_skin){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE guilds SET name = ?, fk_rank_id = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _guild_name);
			stmt.setInt(2, _rank_skin);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}	
	}
	
	public static void SQLUpdateProfileDefaultSkin(long _guild_id, String _guild_name, int _profile_skin){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE guilds SET name = ?, fk_profile_id = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _guild_name);
			stmt.setInt(2, _profile_skin);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}	
	}
	
	public static void SQLUpdateIconDefaultSkin(long _guild_id, String _guild_name, int _icon_skin){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE guilds SET name = ?, fk_icon_id = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _guild_name);
			stmt.setInt(2, _icon_skin);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}	
	}
	
	//roles table
	public static void SQLgetRole(int _role_level_requirement){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM roles WHERE level_requirement <= ? ORDER BY level_requirement DESC LIMIT 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _role_level_requirement);
			rs = stmt.executeQuery();
			if(rs.next()){
				setRoleID(rs.getLong(1));
				setRoleName(rs.getString(2));
				setRoleLevelRequirement(rs.getInt(3));
				setGuildID(rs.getLong(4));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetSingleRole(){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM roles ORDER BY level_requirement LIMIT 1");
			stmt = myConn.prepareStatement(sql);
			rs = stmt.executeQuery();
			if(rs.next()){
				setRoleID(rs.getLong(1));
				setRoleName(rs.getString(2));
				setRoleLevelRequirement(rs.getInt(3));
				setGuildID(rs.getLong(4));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertRoles(long _role_id, String _name, int _role_level_requirement, long _guild_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO roles (role_id, name, level_requirement, fk_guild_id) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), level_requirement=VALUES(level_requirement)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _role_id);
			stmt.setString(2, _name);
			stmt.setInt(3, _role_level_requirement);
			stmt.setLong(4, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLclearRoles(long _guild_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM roles WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetRoles(long _guild_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM roles WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			while(rs.next()){
				Rank ranks = new Rank();
				ranks.setRoleID(rs.getLong(1));
				ranks.setRole_Name(rs.getString(2));
				ranks.setLevel_Requirement(rs.getInt(3));
				rankList.add(ranks);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//user_details table
	public static void SQLgetUserDetails(long _user_id, long _guild_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM user_details WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setUserID(rs.getLong(1));
				setGuildID(rs.getLong(2));
				setLevel(rs.getInt(3));
				setCurrentExperience(rs.getInt(4));
				setRankUpExperience(rs.getInt(5));
				setExperience(rs.getLong(6));
				setCurrency(rs.getLong(7));
				setAssignedRole(rs.getLong(8));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertUserDetails(long _user_id, long _guild_id, int _level, int _current_experience, int _rank_up_experience, long _experience, long _currency, long _assigned_role){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO user_details (`fk_user_id`, `fk_guild_id`, `level`, `current_experience`, `rank_up_experience`, `total_experience`, `currency`, `current_role`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _level);
			stmt.setInt(4, _current_experience);
			stmt.setInt(5, _rank_up_experience);
			stmt.setLong(6, _experience);
			stmt.setLong(7, _currency);
			stmt.setLong(8, _assigned_role);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public synchronized static void SQLUpdateExperience(long _user_id, long _guild_id, int _current_experience, long _experience){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE user_details SET `current_experience` = ?, `total_experience` = ? WHERE `fk_user_id` = ? && `fk_guild_id` = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _current_experience);
			stmt.setLong(2, _experience);
			stmt.setLong(3, _user_id);
			stmt.setLong(4, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public synchronized static void SQLsetLevelUp(long _user_id, long _guild_id, int _level, int _current_experience, int _rank_up_experience, long _experience, long _currency, long _assigned_role){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE user_details SET `Level` = ?, `current_experience` = ?, `rank_up_experience` = ?, `total_experience` = ?, `currency` = ?, `current_role` = ? WHERE `fk_user_id` = ? && `fk_guild_id` = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _level);
			stmt.setInt(2, _current_experience);
			stmt.setInt(3, _rank_up_experience);
			stmt.setLong(4, _experience);
			stmt.setLong(5, _currency);
			stmt.setLong(6, _assigned_role);
			stmt.setLong(7, _user_id);
			stmt.setLong(8, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public synchronized static void SQLUpdateLevel(long _user_id, long _guild_id, int _level, int _current_experience, int _rank_up_experience, long _assigned_role){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE user_details SET `Level` = ?, `current_experience` = ?, `rank_up_experience` = ?, `current_role` = ? WHERE `fk_user_id` = ? && `fk_guild_id` = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _level);
			stmt.setInt(2, _current_experience);
			stmt.setInt(3, _rank_up_experience);
			stmt.setLong(4, _assigned_role);
			stmt.setLong(5, _user_id);
			stmt.setLong(6, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateCurrency(long _user_id, long _guild_id, long _currency){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE user_details SET `currency` = ? WHERE `fk_user_id` = ? && `fk_guild_id` = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _currency);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLRanking(long _guild_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT `fk_user_id`, `Level`, `total_experience`, @curRank := @curRank + 1 AS Rank FROM `user_details`, (SELECT @curRank := 0) r WHERE fk_guild_id = ? ORDER BY `total_experience` DESC");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			while(rs.next()){
				Rank rank = new Rank();
				rank.setUser_id(rs.getLong(1));
				rank.setLevel(rs.getInt(2));
				rank.setExperience(rs.getLong(3));
				rank.setRank(rs.getInt(4));
				rankList.add(rank);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
			try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLTopRanking(long _guild_id, int _page){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int page = (_page -1)*10;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT `fk_user_id`, `Level`, `total_experience`, @curRank := @curRank + 1 AS `Rank` FROM `user_details`, (SELECT @curRank := 0) r WHERE fk_guild_id = ? ORDER BY `total_experience` DESC LIMIT ?, 10");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, page);
			rs = stmt.executeQuery();
			while(rs.next()){
				Rank rank = new Rank();
				rank.setUser_id(rs.getLong(1));
				rank.setLevel(rs.getInt(2));
				rank.setExperience(rs.getLong(3));
				rank.setRank(rs.getInt(4));
				rankList.add(rank);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
			try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//max_exp table
	public static void SQLgetMaxExperience(long _fk_guild_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT experience, enabled, fk_guild_id FROM max_exp WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _fk_guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setMaxExperience(rs.getLong(1));
				setEnabled(rs.getBoolean(2));
				setGuildID(rs.getLong(3));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertMaxExperience(long _experience, boolean _enabled, long _guild_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO max_exp (max_exp_id, experience, enabled, fk_guild_id) VALUES (NULL, ?, ?, ?) ON DUPLICATE KEY UPDATE experience=VALUES(experience), enabled=VALUES(enabled)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _experience);
			stmt.setBoolean(2, _enabled);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//daily_experience table
	public synchronized static void SQLgetDailyExperience(long _user_id, long _guild_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM daily_experience WHERE user_id = ? && guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setUserID(rs.getLong(1));
				setGuildID(rs.getLong(2));
				setDailyExperience(rs.getLong(3));
				setDailyReset(rs.getTimestamp(4));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public synchronized static void SQLInsertDailyExperience(long _experience, long _user_id, long _guild_id, Timestamp _reset){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO daily_experience (user_id, guild_id, experience, reset) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE experience=VALUES(experience), reset=VALUES(reset)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setLong(3, _experience);
			stmt.setTimestamp(4, _reset);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public synchronized static void SQLDeleteDailyExperience(long _user_id, long _guild_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM daily_experience WHERE user_id = ? && guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//ranking_level
	public static void SQLgetRankingLevel(){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM ranking_level");
			stmt = myConn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while(rs.next()){
				Rank rankingSystem = new Rank();
				rankingSystem.setRankingLevel(rs.getInt(1));
				rankingSystem.setDescription(rs.getString(2));
				rankList.add(rankingSystem);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetRankingLevelID(String _description){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT level_id FROM ranking_level WHERE description LIKE ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _description);
			rs = stmt.executeQuery();
			if(rs.next()){
				setLevelSkin(rs.getInt(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//ranking_rank
	public static void SQLgetRankingRank(){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM ranking_rank");
			stmt = myConn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while(rs.next()){
				Rank rankingSystem = new Rank();
				rankingSystem.setRankingRank(rs.getInt(1));
				rankingSystem.setDescription(rs.getString(2));
				rankList.add(rankingSystem);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetRankingRankID(String _description){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT rank_id FROM ranking_rank WHERE description LIKE ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _description);
			rs = stmt.executeQuery();
			if(rs.next()){
				setRankSkin(rs.getInt(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//ranking_profile
	public static void SQLgetRankingProfile(){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM ranking_profile");
			stmt = myConn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while(rs.next()){
				Rank rankingSystem = new Rank();
				rankingSystem.setRankingProfile(rs.getInt(1));
				rankingSystem.setDescription(rs.getString(2));
				rankList.add(rankingSystem);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetRankingProfileID(String _description){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT profile_id FROM ranking_profile WHERE description LIKE ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _description);
			rs = stmt.executeQuery();
			if(rs.next()){
				setProfileSkin(rs.getInt(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//ranking_icon
	public static void SQLgetRankingIcons(){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM ranking_icons");
			stmt = myConn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while(rs.next()){
				Rank rankingSystem = new Rank();
				rankingSystem.setRankingIcon(rs.getInt(1));
				rankingSystem.setDescription(rs.getString(2));
				rankList.add(rankingSystem);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetRankingIconsID(String _description){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT icon_id FROM ranking_level WHERE description LIKE ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _description);
			rs = stmt.executeQuery();
			if(rs.next()){
				setIconSkin(rs.getInt(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//shop_content
	public static void SQLgetShopContent(String _description){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM shop_content WHERE description LIKE ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _description);
			rs = stmt.executeQuery();
			while(rs.next()){
				setItemID(rs.getInt(1));
				setDescription(rs.getString(2));
				setPrice(rs.getLong(3));
				setSkinType(rs.getString(4));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//daily_items
	public static void SQLInsertDailyItems(String _description, int _weight, String _type){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO daily_items (description, weight, fk_type, action) VALUES(?, ?, ?, \"use\")");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _description);
			stmt.setInt(2, _weight);
			stmt.setString(3, _type);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetSumWeightFromDailyItems(){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT SUM(weight) FROM daily_items");
			stmt = myConn.prepareStatement(sql);
			rs = stmt.executeQuery();
			if(rs.next()){
				setWeight(rs.getInt(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//inventory
	public static void SQLInsertInventory(long _user_id, int _item_id, Timestamp _position, int _number, String _status){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO inventory (fk_user_id, fk_item_id, position, number, fk_status) VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE number=VALUES(number)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setTimestamp(3, _position);
			stmt.setInt(4, _number);
			stmt.setString(5, _status);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertInventoryWithLimit(long _user_id, int _item_id, Timestamp _position, int _number, String _status, Timestamp _expires){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO inventory (fk_user_id, fk_item_id, position, number, fk_status, expires) VALUES(?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE number=VALUES(number), expires=VALUES(expires)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setTimestamp(3, _position);
			stmt.setInt(4, _number);
			stmt.setString(5, _status);
			stmt.setTimestamp(6, _expires);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetNumberLimitFromInventory(long _user_id, int _item_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT number FROM inventory WHERE fk_user_id = ? AND fk_item_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setNumberLimit(rs.getInt(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetExpirationFromInventory(long _user_id, int _item_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT expires FROM inventory WHERE fk_user_id = ? AND fk_item_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setExpiration(rs.getTimestamp(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLDeleteInventory(){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM inventory WHERE fk_status LIKE \"limit\" AND expires-CURRENT_TIMESTAMP <= 0");
			stmt = myConn.prepareStatement(sql);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetTotalItemNumber(long _user_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT COUNT(*) FROM inventory WHERE fk_user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setItemNumber((rs.getInt(1)-1)/12);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//shop_content
	public static void SQLgetItemIDFromShopContent(String _description){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT item_id FROM shop_content WHERE description LIKE ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _description);
			rs = stmt.executeQuery();
			if(rs.next()){
				setItemID(rs.getInt(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//dailies_usage
	public static void SQLgetDailiesUsage(long _user_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM dailies_usage WHERE fk_user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setUserID(rs.getLong(1));
				setOpened(rs.getTimestamp(2));
				setNextDaily(rs.getTimestamp(3));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertDailiesUsage(long _user_id, Timestamp _opened, Timestamp _next_daily){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO dailies_usage (fk_user_id, opened, next_daily) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE opened=VALUES(opened), next_daily=VALUES(next_daily)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setTimestamp(2, _opened);
			stmt.setTimestamp(3, _next_daily);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//JOINS
	public synchronized static void SQLgetUserUserDetailsGuildRanking(long _user_id, long _guild_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT guilds.guild_id, guilds.max_level, guilds.ranking_state, user_details.fk_user_id, user_details.level, user_details.current_experience, user_details.rank_up_experience, user_details.total_experience, user_details.currency, user_details.current_role, users.level_skin, ranking_level.description, users.rank_skin, ranking_rank.description, users.profile_skin, ranking_profile.description, users.icon_skin, ranking_icons.description, ranking_profile.fk_bar_id, ranking_rank.fk_bar_id, ranking_profile.exp_percent_txt, ranking_rank.exp_percent_txt, ranking_profile.tcolor_r, ranking_rank.tcolor_r, ranking_level.tcolor_r, ranking_profile.tcolor_g, ranking_rank.tcolor_g, ranking_level.tcolor_g, ranking_profile.tcolor_b, ranking_rank.tcolor_b, ranking_level.tcolor_b, ranking_level.rankx, ranking_level.ranky, ranking_level.rank_width, ranking_level.rank_height, ranking_rank.rankx, ranking_rank.ranky, ranking_rank.rank_width, ranking_rank.rank_height, ranking_profile.rankx, ranking_profile.ranky, ranking_profile.rank_width, ranking_profile.rank_height FROM guilds LEFT JOIN user_details ON guilds.guild_id = user_details.fk_guild_id INNER JOIN users ON user_details.fk_user_id = users.user_id INNER JOIN ranking_level ON level_skin = level_id INNER JOIN ranking_rank ON rank_skin = rank_id INNER JOIN ranking_profile ON profile_skin = profile_id INNER JOIN ranking_icons ON icon_skin = icon_id WHERE users.user_id = ? && guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setGuildID(rs.getLong(1));
				setMaxLevel(rs.getInt(2));
				setRankingState(rs.getBoolean(3));
				setUserID(rs.getLong(4));
				setLevel(rs.getInt(5));
				setCurrentExperience(rs.getInt(6));
				setRankUpExperience(rs.getInt(7));
				setExperience(rs.getLong(8));
				setCurrency(rs.getLong(9));
				setAssignedRole(rs.getLong(10));
				setLevelSkin(rs.getInt(11));
				setLevelDescription(rs.getString(12));
				setRankSkin(rs.getInt(13));
				setRankDescription(rs.getString(14));
				setProfileSkin(rs.getInt(15));
				setProfileDescription(rs.getString(16));
				setIconSkin(rs.getInt(17));
				setIconDescription(rs.getString(18));
				setColorProfile(rs.getInt(19));
				setColorRank(rs.getInt(20));
				setExpAndPercentProfileAllowed(rs.getBoolean(21));
				setExpAndPercentRankAllowed(rs.getBoolean(22));
				setTextColorRProfile(rs.getInt(23));
				setTextColorRRank(rs.getInt(24));
				setTextColorRLevel(rs.getInt(25));
				setTextColorGProfile(rs.getInt(26));
				setTextColorGRank(rs.getInt(27));
				setTextColorGLevel(rs.getInt(28));
				setTextColorBProfile(rs.getInt(29));
				setTextColorBRank(rs.getInt(30));
				setTextColorBLevel(rs.getInt(31));
				setRankXLevel(rs.getInt(32));
				setRankYLevel(rs.getInt(33));
				setRankWidthLevel(rs.getInt(34));
				setRankHeightLevel(rs.getInt(35));
				setRankXRank(rs.getInt(36));
				setRankYRank(rs.getInt(37));
				setRankWidthRank(rs.getInt(38));
				setRankHeightRank(rs.getInt(39));
				setRankXProfile(rs.getInt(40));
				setRankYProfile(rs.getInt(41));
				setRankWidthProfile(rs.getInt(42));
				setRankHeightProfile(rs.getInt(43));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetSkinshopContentAndType(String _skin_type){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT item_id, shop_content.description, price, skin, skin_type.description FROM shop_content INNER JOIN skin_type ON fk_skin = skin WHERE skin LIKE ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _skin_type);
			rs = stmt.executeQuery();
			while(rs.next()){
				Skins insert_skin = new Skins();
				insert_skin.setItemID(rs.getInt(1));
				insert_skin.setShopDescription(rs.getString(2));
				insert_skin.setPrice(rs.getLong(3));
				insert_skin.setSkinType(rs.getString(4));
				insert_skin.setSkinDescription(rs.getString(5));
				set_skin.add(insert_skin);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetDefaultSkins(long _guild_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT ranking_level.description, ranking_rank.description, ranking_profile.description, ranking_icons.description FROM guilds INNER JOIN ranking_level ON fk_level_id = level_id INNER JOIN ranking_rank ON fk_rank_id = rank_id INNER JOIN ranking_profile ON fk_profile_id = profile_id INNER JOIN ranking_icons ON fk_icon_id = icon_id WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setLevelDescription(rs.getString(1));
				setRankDescription(rs.getString(2));
				setProfileDescription(rs.getString(3));
				setIconDescription(rs.getString(4));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetItemID(long _user_id, int _item_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT item_id FROM inventory INNER JOIN shop_content ON fk_item_id = item_id WHERE fk_user_id = ? AND item_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setItemID(rs.getInt(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetItemIDAndSkinType(long _user_id, String _description){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT item_id, fk_skin, fk_status FROM inventory INNER JOIN shop_content ON fk_item_id = item_id WHERE fk_user_id = ? AND shop_content.description LIKE ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setString(2, _description);
			rs = stmt.executeQuery();
			if(rs.next()){
				setItemID(rs.getInt(1));
				setSkinType(rs.getString(2));
				setStatus(rs.getString(3));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetInventoryAndDescription(long _user_id, String _description, String _status){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT fk_user_id, position, number, fk_status, description, fk_skin FROM inventory INNER JOIN shop_content ON fk_item_id = item_id WHERE fk_user_id = ? AND description LIKE ? AND fk_status LIKE ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setString(2, _description);
			stmt.setString(3, _status);
			rs = stmt.executeQuery();
			if(rs.next()){
				setUserID(rs.getLong(1));
				setPosition(rs.getTimestamp(2));
				setNumber(rs.getInt(3));
				setDescription(rs.getString(4));
				setSkinType(rs.getString(5));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetInventoryAndDescriptions(long _user_id, int _limit){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT fk_user_id, position, number, fk_status, expires, description, fk_skin FROM inventory INNER JOIN shop_content ON fk_item_id = item_id WHERE fk_user_id = ? ORDER BY position desc LIMIT ?, 12");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _limit);
			rs = stmt.executeQuery();
			while(rs.next()){
				InventoryContent setInventory = new InventoryContent();
				setInventory.setUserID(rs.getLong(1));
				setInventory.setTimestamp(rs.getTimestamp(2));
				setInventory.setNumber(rs.getInt(3));
				setInventory.setStatus(rs.getString(4));
				setInventory.setExpiration(rs.getTimestamp(5));
				setInventory.setDescription(rs.getString(6));
				setInventory.setType(rs.getString(7));
				inventory.add(setInventory);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetInventoryAndDescriptionWithoutLimit(long _user_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT fk_user_id, position, description FROM inventory INNER JOIN shop_content ON fk_item_id = item_id WHERE fk_user_id = ? ORDER BY position desc");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			rs = stmt.executeQuery();
			while(rs.next()){
				InventoryContent setInventory = new InventoryContent();
				setInventory.setUserID(rs.getLong(1));
				setInventory.setTimestamp(rs.getTimestamp(2));
				setInventory.setDescription(rs.getString(3));
				inventory.add(setInventory);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetNumberAndExpirationFromInventory(long _user_id, String _description, String _status){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT number, expires FROM inventory INNER JOIN shop_content ON fk_item_id = item_id WHERE fk_user_id = ? AND description = ? AND fk_status = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setString(2, _description);
			stmt.setString(3, _status);
			rs = stmt.executeQuery();
			while(rs.next()){
				setNumber(rs.getInt(1));
				setExpiration(rs.getTimestamp(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetDailiesAndType(){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT item_id, daily_items.description, weight, type, daily_type.description, action FROM daily_items INNER JOIN daily_type ON fk_type = type");
			stmt = myConn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while(rs.next()){
				Dailies setDaily = new Dailies();
				setDaily.setItemId(rs.getInt(1));
				setDaily.setDescription(rs.getString(2));
				setDaily.setWeight(rs.getInt(3));
				setDaily.SetType(rs.getString(4));
				setDaily.setTypeDescription(rs.getString(5));
				setDaily.setAction(rs.getString(6));
				dailies.add(setDaily);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//Transaction
	@SuppressWarnings("resource")
	public static void SQLUpdateCurrencyAndInsertInventory(long _user_id, long _currency, int _item_id, Timestamp _position, int _number){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			myConn.setAutoCommit(false);
			String sql = ("UPDATE user_details SET currency = ? WHERE fk_user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _currency);
			stmt.setLong(2, _user_id);
			stmt.executeUpdate();
			
			String sql2 = ("INSERT INTO inventory (fk_user_id, fk_item_id, position, number, fk_status) VALUES(?, ?, ?, ?, \"perm\") ON DUPLICATE KEY UPDATE position=VALUES(position), number=VALUES(number) ");
			stmt = myConn.prepareStatement(sql2);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setTimestamp(3, _position);
			stmt.setInt(4, _number);
			stmt.executeUpdate();
			myConn.commit();	
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				myConn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static void SQLUpdateAndInsertInventory(long _user_id, int _number, int _number_limit, int _item_id, Timestamp _position, Timestamp _expiration){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			myConn.setAutoCommit(false);
			String sql = ("UPDATE inventory SET number = ? WHERE fk_user_id = ? AND fk_status LIKE \"perm\" AND fk_item_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _number-1);
			stmt.setLong(2, _user_id);
			stmt.setInt(3, _item_id);
			stmt.executeUpdate();
			
			String sql2 = ("INSERT INTO inventory (fk_user_id, fk_item_id, position, number, fk_status, expires) VALUES(?, ?, ?, ?, \"limit\", ?) ON DUPLICATE KEY UPDATE position=VALUES(position), number=VALUES(number), expires=VALUES(expires) ");
			stmt = myConn.prepareStatement(sql2);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setTimestamp(3, _position);
			stmt.setInt(4, _number_limit);
			stmt.setTimestamp(5, _expiration);
			stmt.executeUpdate();
			myConn.commit();	
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				myConn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static void SQLDeleteAndInsertInventory(long _user_id, int _number, int _item_id, Timestamp _position, Timestamp _expiration){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			myConn.setAutoCommit(false);
			String sql = ("DELETE FROM inventory WHERE fk_user_id = ? AND fk_status LIKE \"perm\" AND fk_item_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.executeUpdate();
			
			String sql2 = ("INSERT INTO inventory (fk_user_id, fk_item_id, position, number, fk_status, expires) VALUES(?, ?, ?, ?, \"limit\", ?) ON DUPLICATE KEY UPDATE number=VALUES(number), expires=VALUES(expires) ");
			stmt = myConn.prepareStatement(sql2);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setTimestamp(3, _position);
			stmt.setInt(4, _number);
			stmt.setTimestamp(5, _expiration);
			stmt.executeUpdate();
			myConn.commit();	
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				myConn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//EXISTS
	public static void SQLExpBoosterExistsInInventory(){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT DISTINCT description FROM inventory INNER JOIN shop_content ON fk_item_id = item_id WHERE fk_status LIKE \"limit\" AND EXISTS (SELECT description FROM daily_items WHERE fk_type LIKE \"exp\")");
			stmt = myConn.prepareStatement(sql);
			rs = stmt.executeQuery();
			if(rs.next()){
				setDescription(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public synchronized static void setUserID(long _user_id){
		user_id = _user_id;
	}
	public static void setUserName(String _user_name){
		user_name = _user_name;
	}
	public static void setLevelSkin(int _level_skin){
		level_skin = _level_skin;
	}
	public static void setRankSkin(int _rank_skin){
		rank_skin = _rank_skin;
	}
	public static void setProfileSkin(int _profile_skin){
		profile_skin = _profile_skin;
	}
	public static void setIconSkin(int _icon_skin){
		icon_skin = _icon_skin;
	}
	public synchronized static void setGuildID(long _guild_id){
		guild_id = _guild_id;
	}
	public static void setGuildName(String _guild_name){
		guild_name = _guild_name;
	}
	public static void setRoleID(long _role_id){
		role_id = _role_id;
	}
	public static void setRoleName(String _role_name){
		role_name = _role_name;
	}
	public static void setRoleLevelRequirement(int _role_level_requirement){
		role_level_requirement = _role_level_requirement;
	}
	public static void setRankingLevel(int _ranking_level){
		ranking_level = _ranking_level;
	}
	public static void setRankingRank(int _ranking_rank){
		ranking_rank = _ranking_rank;
	}
	public static void setRankingProfile(int _ranking_profile){
		ranking_profile = _ranking_profile;
	}
	public static void setRankingIcon(int _ranking_icon){
		ranking_icon = _ranking_icon;
	}
	public static void setRankingState(boolean _ranking_state){
		ranking_state = _ranking_state;
	}
	public static void setMaxLevel(int _max_level){
		max_level = _max_level;
	}
	public static void setDescription(String _description){
		description = _description;
	}
	public synchronized static void setLevel(int _level){
		level = _level;
	}
	public synchronized static void setCurrentExperience(int _current_experience){
		current_experience = _current_experience;
	}
	public synchronized static void setRankUpExperience(int _rank_up_experience){
		rank_up_experience = _rank_up_experience;
	}
	public synchronized static void setExperience(long _experience){
		experience = _experience;
	}
	public synchronized static void setCurrency(long _currency){
		currency = _currency;
	}
	public synchronized static void setAssignedRole(long _assigned_role){
		assigned_role = _assigned_role;
	}
	public static void setEnabled(boolean _enabled){
		enabled = _enabled;
	}
	public static void setMaxExperience(long _max_experience){
		max_experience = _max_experience;
	}
	public synchronized static void setDailyExperience(long _daily_experience){
		daily_experience = _daily_experience;
	}
	public synchronized static void setDailyReset(Date _daily_reset){
		daily_reset = _daily_reset;
	}
	public static void setItemID(int _item_id){
		item_id = _item_id;
	}
	public static void setPrice(long _price){
		price = _price;
	}
	public static void setPosition(Date _position){
		position = _position;
	}
	public static void setSkinType(String _skin_type){
		skin_type = _skin_type;
	}
	public static void setWeight(int _weight){
		weight = _weight;
	}
	public static void setNumber(int _number){
		number = _number;
	}
	public static void setOpened(Date _opened){
		opened = _opened;
	}
	public static void setNextDaily(Date _next_daily){
		next_daily = _next_daily;
	}
	public static void setExpiration(Date _expiration){
		expiration = _expiration;
	}
	public static void setNumberLimit(int _number_limit){
		number_limit = _number_limit;
	}
	public static void setStatus(String _status){
		status = _status;
	}
	public static void setColorProfile(int _color_profile){
		color_profile = _color_profile;
	}
	public static void setColorRank(int _color_rank){
		color_rank = _color_rank;
	}
	public static void setExpAndPercentProfileAllowed(boolean _exp_and_percent_profile){
		exp_and_percent_profile = _exp_and_percent_profile;
	}
	public static void setExpAndPercentRankAllowed(boolean _exp_and_percent_rank){
		exp_and_percent_rank = _exp_and_percent_rank;
	}
	public static void setItemNumber(int _item_number){
		item_number = _item_number;
	}
	public static void setTextColorRProfile(int _text_color_r_profile){
		text_color_r_profile = _text_color_r_profile;
	}
	public static void setTextColorRRank(int _text_color_r_rank){
		text_color_r_rank = _text_color_r_rank;
	}
	public static void setTextColorRLevel(int _text_color_r_level){
		text_color_r_level = _text_color_r_level;
	}
	public static void setTextColorGProfile(int _text_color_g_profile){
		text_color_g_profile = _text_color_g_profile;
	}
	public static void setTextColorGRank(int _text_color_g_rank){
		text_color_g_rank = _text_color_g_rank;
	}
	public static void setTextColorGLevel(int _text_color_g_level){
		text_color_g_level = _text_color_g_level;
	}
	public static void setTextColorBProfile(int _text_color_b_profile){
		text_color_b_profile = _text_color_b_profile;
	}
	public static void setTextColorBRank(int _text_color_b_rank){
		text_color_b_rank = _text_color_b_rank;
	}
	public static void setTextColorBLevel(int _text_color_b_level){
		text_color_b_level = _text_color_b_level;
	}
	public static void setRankXLevel(int _rankx_level){
		rankx_level = _rankx_level;
	}
	public static void setRankYLevel(int _ranky_level){
		ranky_level = _ranky_level;
	}
	public static void setRankWidthLevel(int _rank_width_level){
		rank_width_level = _rank_width_level;
	}
	public static void setRankHeightLevel(int _rank_height_level){
		rank_height_level = _rank_height_level;
	}
	public static void setRankXRank(int _rankx_rank){
		rankx_rank = _rankx_rank;
	}
	public static void setRankYRank(int _ranky_rank){
		ranky_rank = _ranky_rank;
	}
	public static void setRankWidthRank(int _rank_width_rank){
		rank_width_rank = _rank_width_rank;
	}
	public static void setRankHeightRank(int _rank_height_rank){
		rank_height_rank = _rank_height_rank;
	}
	public static void setRankXProfile(int _rankx_profile){
		rankx_profile = _rankx_profile;
	}
	public static void setRankYProfile(int _ranky_profile){
		ranky_profile = _ranky_profile;
	}
	public static void setRankWidthProfile(int _rank_width_profile){
		rank_width_profile = _rank_width_profile;
	}
	public static void setRankHeightProfile(int _rank_height_profile){
		rank_height_profile = _rank_height_profile;
	}
	public static void setLevelDescription(String _level_description){
		level_description = _level_description;
	}
	public static void setRankDescription(String _rank_description){
		rank_description = _rank_description;
	}
	public static void setProfileDescription(String _profile_description){
		profile_description = _profile_description;
	}
	public static void setIconDescription(String _icon_description){
		icon_description = _icon_description;
	}
	
	public synchronized static long getUserID(){
		return user_id;
	}
	public static String getUserName(){
		return user_name;
	}
	public static int getLevelSkin(){
		return level_skin;
	}
	public static int getRankSkin(){
		return rank_skin;
	}
	public static int getProfileSkin(){
		return profile_skin;
	}
	public static int getIconSkin(){
		return icon_skin;
	}
	public synchronized static long getGuildID(){
		return guild_id;
	}
	public static String getGuildName(){
		return guild_name;
	}
	public static long getRoleID(){
		return role_id;
	}
	public static String getRoleName(){
		return role_name;
	}
	public static int getRoleLevelRequirement(){
		return role_level_requirement;
	}
	public static int getRankingLevel(){
		return ranking_level;
	}
	public static int getRankingRank(){
		return ranking_rank;
	}
	public static int getRankingProfile(){
		return ranking_profile;
	}
	public static int getRankingIcon(){
		return ranking_icon;
	}
	public static boolean getRankingState(){
		return ranking_state;
	}
	public static int getMaxLevel(){
		return max_level;
	}
	public static String getDescription(){
		return description;
	}
	public synchronized static int getLevel(){
		return level;
	}
	public synchronized static int getCurrentExperience(){
		return current_experience;
	}
	public synchronized static int getRankUpExperience(){
		return rank_up_experience;
	}
	public synchronized static long getExperience(){
		return experience;
	}
	public synchronized static long getCurrency(){
		return currency;
	}
	public synchronized static long getAssignedRole(){
		return assigned_role;
	}
	public static boolean getEnabled(){
		return enabled;
	}
	public static long getMaxExperience(){
		return max_experience;
	}
	public synchronized static long getDailyExperience(){
		return daily_experience;
	}
	public synchronized static Date getDailyReset(){
		return daily_reset;
	}
	public static int getItemID(){
		return item_id;
	}
	public static long getPrice(){
		return price;
	}
	public static Date getPosition(){
		return position;
	}
	public static String getSkinType(){
		return skin_type;
	}
	public static int getWeight(){
		return weight;
	}
	public static int getNumber(){
		return number;
	}
	public static Date getOpened(){
		return opened;
	}
	public static Date getNextDaily(){
		return next_daily;
	}
	public static Date getExpiration(){
		return expiration;
	}
	public static int getNumberLimit(){
		return number_limit;
	}
	public static String getStatus(){
		return status;
	}
	public static int getColorProfile(){
		return color_profile;
	}
	public static int getColorRank(){
		return color_rank;
	}
	public static boolean getExpAndPercentAllowedProfile(){
		return exp_and_percent_profile;
	}
	public static boolean getExpAndPercentAllowedRank(){
		return exp_and_percent_rank;
	}
	public static int getItemNumber(){
		return item_number;
	}
	public static int getTextColorRProfile(){
		return text_color_r_profile;
	}
	public static int getTextColorRRank(){
		return text_color_r_rank;
	}
	public static int getTextColorRLevel(){
		return text_color_r_level;
	}
	public static int getTextColorBProfile(){
		return text_color_b_profile;
	}
	public static int getTextColorBRank(){
		return text_color_b_rank;
	}
	public static int getTextColorBLevel(){
		return text_color_b_level;
	}
	public static int getTextColorGProfile(){
		return text_color_g_profile;
	}
	public static int getTextColorGRank(){
		return text_color_g_rank;
	}
	public static int getTextColorGLevel(){
		return text_color_g_level;
	}
	public static int getRankXLevel(){
		return rankx_level;
	}
	public static int getRankYLevel(){
		return ranky_level;
	}
	public static int getRankWidthLevel(){
		return rank_width_level;
	}
	public static int getRankHeightLevel(){
		return rank_height_level;
	}
	public static int getRankXRank(){
		return rankx_rank;
	}
	public static int getRankYRank(){
		return ranky_rank;
	}
	public static int getRankWidthRank(){
		return rank_width_rank;
	}
	public static int getRankHeightRank(){
		return rank_height_rank;
	}
	public static int getRankXProfile(){
		return rankx_profile;
	}
	public static int getRankYProfile(){
		return ranky_profile;
	}
	public static int getRankWidthProfile(){
		return rank_width_profile;
	}
	public static int getRankHeightProfile(){
		return rank_height_profile;
	}
	public static ArrayList<Rank> getRankList(){
		return rankList;
	}
	public static ArrayList<Skins> getSkins(){
		return set_skin;
	}
	public static ArrayList<InventoryContent> getInventory(){
		return inventory;
	}
	public static ArrayList<Dailies> getDailies(){
		return dailies;
	}
	public static String getLevelDescription(){
		return level_description;
	}
	public static String getRankDescription(){
		return rank_description;
	}
	public static String getProfileDescription(){
		return profile_description;
	}
	public static String getIconDescription(){
		return icon_description;
	}
	public static void clearArrayList(){
		rankList.clear();
	}
	public static void clearSkinArray(){
		set_skin.clear();
	}
	public static void clearInventoryArray(){
		inventory.clear();
	}
	public static void clearDailiesArray(){
		dailies.clear();
	}
	public static void clearAllVariables(){
		user_id = 0;
		user_name = "";
		level_skin = 0;
		rank_skin = 0;
		profile_skin = 0;
		icon_skin = 0;
		guild_id = 0;
		guild_name = "";
		role_id = 0;
		role_name = "";
		role_level_requirement = 0;
		ranking_level = 0;
		ranking_rank = 0;
		ranking_profile = 0;
		ranking_icon = 0;
		ranking_state = false;
		max_level = 0;
		description = "";
		level = 0;
		current_experience = 0;
		rank_up_experience = 0;
		experience = 0;
		currency = 0;
		assigned_role = 0;
		enabled = false;
		max_experience = 0;
		daily_experience = 0;
		daily_reset = null;
		item_id = 0;
		position = null;
		skin_type = "";
		weight = 0;
		number = 0;
		opened = null;
		next_daily = null;
		expiration = null;
		number_limit = 0;
		status = null;
		color_profile = 0;
		color_rank = 0;
		exp_and_percent_profile = false; 
		exp_and_percent_rank = false;
		item_number = 0;
		text_color_r_profile = 0;
		text_color_r_rank = 0;
		text_color_r_level = 0;
		text_color_g_profile = 0;
		text_color_g_rank = 0;
		text_color_g_level = 0;
		text_color_b_profile = 0;
		text_color_b_rank = 0;
		text_color_b_level = 0;
		rankx_level = 0;
		ranky_level = 0;
		rank_width_level = 0;
		rank_height_level = 0;
		rankx_rank = 0;
		ranky_rank = 0;
		rank_width_rank = 0;
		rank_height_rank = 0;
		rankx_profile = 0;
		ranky_profile = 0;
		rank_width_profile = 0;
		rank_height_profile = 0;
	}
	public static void clearDescriptionVariables(){
		level_description = "";
		rank_description = "";
		profile_description = "";
		icon_description = "";
	}
}