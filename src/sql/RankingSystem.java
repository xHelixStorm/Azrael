package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Dailies;
import constructors.Guilds;
import constructors.Inventory;
import constructors.InventoryContent;
import constructors.Rank;
import constructors.Ranks;
import constructors.Skins;
import core.Hashes;
import fileManagement.IniFileReader;

public class RankingSystem {
	private static final Logger logger = LoggerFactory.getLogger(RankingSystem.class);
	
	private static String username = IniFileReader.getSQLUsername2();
	private static String password = IniFileReader.getSQLPassword2();
		
	public static void SQLconnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			logger.error("JDBC driver couldn't be loaded", e);
		}
	}
	
	//action_log
	public static void SQLInsertActionLog(String _warning_level, long _entity, long _guild_id, String _event, String _notes) {
		logger.debug("SQLInsertActionLog launched. Passed params {}, {}, {}, {}, {}", _warning_level, _entity, _guild_id, _event, _notes);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO action_log (warning_level, affected_entity, affected_server, event, notes) VALUES (?, ?, ?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _warning_level);
			stmt.setLong(2, _entity);
			stmt.setLong(3, _guild_id);
			stmt.setString(4, _event);
			stmt.setString(5, _notes);
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertActionLog Exception", e);
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//users table
	public static int SQLInsertUser(long _user_id, long _guild_id, String _name, int _level_skin, int _rank_skin, int _profile_skin, int _icon_skin) {
		logger.debug("SQLInsertUser launched. Passed params {}, {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _name, _level_skin, _rank_skin, _profile_skin, _icon_skin);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql;
			if(_level_skin != 0 && _rank_skin != 0 && _profile_skin != 0 && _icon_skin != 0) {
				sql = ("INSERT INTO users (user_id, name, level_skin, rank_skin, profile_skin, icon_skin, fk_guild_id) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _user_id);
				stmt.setString(2, _name);
				stmt.setInt(3, _level_skin);
				stmt.setInt(4, _rank_skin);
				stmt.setInt(5, _profile_skin);
				stmt.setInt(6, _icon_skin);
				stmt.setLong(7, _guild_id);
			}
			else {
				sql = ("INSERT INTO users (user_id, name, fk_guild_id) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _user_id);
				stmt.setString(2, _name);
				stmt.setLong(3, _guild_id);
			}
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertUser Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUserLevelSkin(long _user_id, long _guild_id, String _name, int _skin_id) {
		logger.debug("SQLUpdateUserLevelSkin launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _name, _skin_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE users SET level_skin = ?, name = ? WHERE user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _skin_id);
			stmt.setString(2, _name);
			stmt.setLong(3, _user_id);
			stmt.setLong(4, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUserLevelSkin Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUserRankSkin(long _user_id, long _guild_id, String _name, int _skin_id) {
		logger.debug("SQLUpdateUserRankSkin launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _name, _skin_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE users SET rank_skin = ?, name = ? WHERE user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _skin_id);
			stmt.setString(2, _name);
			stmt.setLong(3, _user_id);
			stmt.setLong(4, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUserRankSkin Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUserProfileSkin(long _user_id, long _guild_id, String _name, int _skin_id) {
		logger.debug("SQLUpdateUserProfileSkin launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _name, _skin_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE users SET profile_skin = ?, name = ? WHERE user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _skin_id);
			stmt.setString(2, _name);
			stmt.setLong(3, _user_id);
			stmt.setLong(4, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUserProfileSkin Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUserIconSkin(long _user_id, long _guild_id, String _name, int _skin_id) {
		logger.debug("SQLUpdateUserIconSkin launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _name, _skin_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE users SET icon_skin = ?, name = ? WHERE user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _skin_id);
			stmt.setString(2, _name);
			stmt.setLong(3, _user_id);
			stmt.setLong(4, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUserIconSkin Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//guilds table 
	public static int SQLInsertGuild(long _guild_id, String _name, boolean _enabled) {
		logger.debug("SQLInsertGuild launched. Passed params {}, {}, {}", _guild_id, _name, _enabled);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO guilds (guild_id, name, ranking_state) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), ranking_state=VALUES(ranking_state)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setString(2, _name);
			stmt.setBoolean(3, _enabled);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertGuild Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateRankingSystem(long _guild_id, String _guild_name, boolean _ranking_state) {
		logger.debug("SQLUpdateRankingSystem launched. Passed params {}, {}, {}", _guild_id, _guild_name, _ranking_state);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE guilds SET name = ?, ranking_state = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _guild_name);
			stmt.setBoolean(2, _ranking_state);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateRankingSystem Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}	
	}
	
	public static int SQLUpdateLevelDefaultSkin(long _guild_id, String _guild_name, int _level_skin) {
		logger.debug("SQLUpdateLevelDefaultSkin launched. Passed params {}, {}, {}", _guild_id, _guild_name, _level_skin);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE guilds SET name = ?, fk_level_id = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _guild_name);
			stmt.setInt(2, _level_skin);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateLevelDefaultSkin Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}	
	}
	
	public static int SQLUpdateRankDefaultSkin(long _guild_id, String _guild_name, int _rank_skin) {
		logger.debug("SQLUpdateRankDefaultSkin launched. Passed params {}, {}, {}", _guild_id, _guild_name, _rank_skin);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE guilds SET name = ?, fk_rank_id = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _guild_name);
			stmt.setInt(2, _rank_skin);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateRankDefaultSkin Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}	
	}
	
	public static int SQLUpdateProfileDefaultSkin(long _guild_id, String _guild_name, int _profile_skin) {
		logger.debug("SQLUpdateProfileDefaultSkin launched. Passed params {}, {}, {}", _guild_id, _guild_name, _profile_skin);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE guilds SET name = ?, fk_profile_id = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _guild_name);
			stmt.setInt(2, _profile_skin);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateProfileDefaultSkin Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}	
	}
	
	public static int SQLUpdateIconDefaultSkin(long _guild_id, String _guild_name, int _icon_skin) {
		logger.debug("SQLUpdateIconDefaultSkin launched. Passed params {}, {}, {}", _guild_id, _guild_name, _icon_skin);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE guilds SET name = ?, fk_icon_id = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _guild_name);
			stmt.setInt(2, _icon_skin);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateIconDefaultSkin Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}	
	}
	
	//roles table
	public static int SQLInsertRoles(long _role_id, String _name, int _role_level_requirement, long _guild_id) {
		logger.debug("SQLInsertRoles launched. Passed params {}, {}, {}, {}", _role_id, _name, _role_level_requirement, _guild_id);
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
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertRoles Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLremoveSingleRole(long _role_id, long _guild_id) {
		logger.debug("SQLremoveSingleRole launched. Passed params {}, {}", _role_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM roles WHERE role_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _role_id);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLremoveSingleRole Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteRole(long _role_id, long _guild_id) {
		logger.debug("SQLDeleteRole launched. Passed params {}, {}", _role_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM roles WHERE role_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _role_id);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteRole Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLclearRoles(long _guild_id) {
		logger.debug("SQLclearRoles launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM roles WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLclearRoles Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Rank> SQLgetRoles(long _guild_id) {
		if(Hashes.getRankingRoles(_guild_id) == null) {
			logger.debug("SQLgetRoles launched. Passed params {}", _guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			ArrayList<Rank> ranks = new ArrayList<Rank>();
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT * FROM roles WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					Rank rank = new Rank();
					rank.setRoleID(rs.getLong(1));
					rank.setRole_Name(rs.getString(2));
					rank.setLevel_Requirement(rs.getInt(3));
					rank.setGuildID(rs.getLong(4));
					ranks.add(rank);
				}
				Hashes.addRankingRoles(_guild_id, ranks);
				return ranks;
			} catch (SQLException e) {
				logger.error("SQLgetRoles Exception", e);
				return null;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return Hashes.getRankingRoles(_guild_id);
	}
	
	public static boolean SQLgetRole(long _role_id, long _guild_id) {
		logger.debug("SQLgetRole launched. Passed params {}, {}", _role_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM roles WHERE role_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _role_id);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				return true;
			}
			return false;
		} catch (SQLException e) {
			logger.error("SQLgetRole Exception", e);
			return false;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//user_details table
	public static int SQLInsertUserDetails(long _user_id, long _guild_id, int _level, long _experience, long _currency, long _assigned_role) {
		logger.debug("SQLInsertUserDetails launched. Passed params {}, {}, {}, {}, {}", _user_id, _level, _experience, _currency, _assigned_role);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT IGNORE INTO user_details (`fk_user_id`, `level`, `experience`, `currency`, `current_role`, `fk_guild_id`) VALUES (?, ?, ?, ?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _level);
			stmt.setLong(3, _experience);
			stmt.setLong(4, _currency);
			stmt.setLong(5, _assigned_role);
			stmt.setLong(6, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertUserDetails Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static long SQLgetAssignedRole(long _user_id, long _guild_id) {
		logger.debug("SQLgetAssignedRole launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT `current_role` FROM `user_details` WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getLong(1);
			}
			return 0;
		} catch(SQLException e) {
			logger.error("SQLgetAssignedRole Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
			try { stmt.close(); } catch (Exception e) { /* ignored */ }
			try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateExperience(long _user_id, long _guild_id, long _experience) {
		logger.debug("SQLUpdateExperience launched. Passed params {}, {}, {}", _user_id, _guild_id, _experience);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE user_details SET `experience` = ? WHERE `fk_user_id` = ? AND `fk_guild_id` = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _experience);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateExperience Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLsetLevelUp(long _user_id, long _guild_id, int _level, long _experience, long _currency, long _assigned_role) {
		logger.debug("SQLsetLevelUp launched. Passed params {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _level, _experience, _currency, _assigned_role);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE user_details SET `level` = ?, `experience` = ?, `currency` = ?, `current_role` = ? WHERE `fk_user_id` = ? AND `fk_guild_id` = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _level);
			stmt.setLong(2, _experience);
			stmt.setLong(3, _currency);
			stmt.setLong(4, _assigned_role);
			stmt.setLong(5, _user_id);
			stmt.setLong(6, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLsetLevelUp Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateCurrentRole(long _guild_id, long _role_assign) {
		logger.debug("SQLUpdateCurrentRole launched. Passed params {}, {}", _guild_id, _role_assign);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE user_details SET `current_role` = ? WHERE `fk_guild_id` = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _role_assign);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateCurrentRole Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateCurrency(long _user_id, long _guild_id, long _currency) {
		logger.debug("SQLUpdateCurrency launched. Passed params {}, {}, {}", _user_id, _guild_id, _currency);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE user_details SET `currency` = ? WHERE `fk_user_id` = ? AND `fk_guild_id` = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _currency);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateCurrency Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Rank> SQLRanking(long _guild_id) {
		if(Hashes.getRankList("ranking_"+_guild_id) == null) {
			logger.debug("SQLgetRanking launched. Passed params {}", _guild_id);
			ArrayList<Rank> rankList = new ArrayList<Rank>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT `fk_user_id`, `Level`, `experience`, @curRank := @curRank + 1 AS Rank FROM `user_details`, (SELECT @curRank := 0) r WHERE fk_guild_id = ? ORDER BY `experience` DESC");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					Rank rank = new Rank();
					rank.setUser_ID(rs.getLong(1));
					rank.setLevel(rs.getInt(2));
					rank.setExperience(rs.getLong(3));
					rank.setRank(rs.getInt(4));
					rankList.add(rank);
				}
				Hashes.addRankList("ranking_"+_guild_id, rankList);
				return rankList;
			} catch (SQLException e) {
				logger.error("SQLRanking Exception", e);
				return rankList;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
				try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return Hashes.getRankList("ranking_"+_guild_id);
	}
	
	//max_exp table
	public static int SQLInsertMaxExperience(long _experience, boolean _enabled, long _guild_id) {
		logger.debug("SQLInsertMaxExperience launched. Passed params {}, {}, {}", _experience, _enabled, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO max_exp (max_exp_id, experience, enabled, fk_guild_id) VALUES (NULL, ?, ?, ?) ON DUPLICATE KEY UPDATE experience=VALUES(experience), enabled=VALUES(enabled)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _experience);
			stmt.setBoolean(2, _enabled);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertMaxExperience Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//daily_experience table
	public static int SQLInsertDailyExperience(long _experience, long _user_id, long _guild_id, Timestamp _reset) {
		logger.debug("SQLInsertDailyExperience launched. Passed params {}, {}, {}, {}", _experience, _user_id, _guild_id, _reset);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO daily_experience (user_id, experience, reset, fk_guild_id) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE experience=VALUES(experience), reset=VALUES(reset)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _experience);
			stmt.setTimestamp(3, _reset);
			stmt.setLong(4, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertDailyExperience Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteDailyExperience(long _user_id, long _guild_id) {
		logger.debug("SQLDeleteDailyExperience launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM daily_experience WHERE user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteDailyExperience Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//ranking_level
	public static ArrayList<Rank> SQLgetRankingLevel() {
		if(Hashes.getRankList("ranking-level") == null) {
			logger.debug("SQLgetRankingLevel launched. No params passed");
			ArrayList<Rank> rankList = new ArrayList<Rank>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT * FROM ranking_level");
				stmt = myConn.prepareStatement(sql);
				rs = stmt.executeQuery();
				while(rs.next()) {
					Rank rankingSystem = new Rank();
					rankingSystem.setRankingLevel(rs.getInt(1));
					rankingSystem.setLevelDescription(rs.getString(2));
					rankingSystem.setColorRLevel(rs.getInt(3));
					rankingSystem.setColorGLevel(rs.getInt(4));
					rankingSystem.setColorBLevel(rs.getInt(5));
					rankingSystem.setRankXLevel(rs.getInt(6));
					rankingSystem.setRankYLevel(rs.getInt(7));
					rankingSystem.setRankWidthLevel(rs.getInt(8));
					rankingSystem.setRankHeightLevel(rs.getInt(9));
					rankingSystem.setLevelXLevel(rs.getInt(10));
					rankingSystem.setLevelYLevel(rs.getInt(11));
					rankingSystem.setNameXLevel(rs.getInt(12));
					rankingSystem.setNameYLevel(rs.getInt(13));
					rankingSystem.setThemeID(rs.getInt(14));
					rankList.add(rankingSystem);
				}
				Hashes.addRankList("ranking-level", rankList);
				return rankList;
			} catch (SQLException e) {
				logger.error("SQLgetRankingLevel Exception", e);
				return rankList;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return Hashes.getRankList("ranking-level");
	}
	
	//ranking_rank
	public static ArrayList<Rank> SQLgetRankingRank() {
		if(Hashes.getRankList("ranking-rank") == null) {
			logger.debug("SQLgetRankingRank launched. No params passed");
			ArrayList<Rank> rankList = new ArrayList<Rank>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT * FROM ranking_rank");
				stmt = myConn.prepareStatement(sql);
				rs = stmt.executeQuery();
				while(rs.next()) {
					Rank rankingSystem = new Rank();
					rankingSystem.setRankingRank(rs.getInt(1));
					rankingSystem.setRankDescription(rs.getString(2));
					rankingSystem.setBarColorRank(rs.getInt(3));
					rankingSystem.setColorRRank(rs.getInt(4));
					rankingSystem.setColorGRank(rs.getInt(5));
					rankingSystem.setColorBRank(rs.getInt(6));
					rankingSystem.setRankXRank(rs.getInt(7));
					rankingSystem.setRankYRank(rs.getInt(8));
					rankingSystem.setRankWidthRank(rs.getInt(10));
					rankingSystem.setRankHeightRank(rs.getInt(11));
					rankingSystem.setNameXRank(rs.getInt(12));
					rankingSystem.setNameYRank(rs.getInt(13));
					rankingSystem.setBarXRank(rs.getInt(14));
					rankingSystem.setBarYRank(rs.getInt(15));
					rankingSystem.setAvatarXRank(rs.getInt(16));
					rankingSystem.setAvatarYRank(rs.getInt(17));
					rankingSystem.setAvatarWidthRank(rs.getInt(18));
					rankingSystem.setAvatarHeightRank(rs.getInt(19));
					rankingSystem.setExpTextXRank(rs.getInt(20));
					rankingSystem.setExpTextYRank(rs.getInt(21));
					rankingSystem.setPercentTextXRank(rs.getInt(22));
					rankingSystem.setPercentTextYRank(rs.getInt(23));
					rankingSystem.setPlacementXRank(rs.getInt(24));
					rankingSystem.setPlacementYRank(rs.getInt(25));
					rankingSystem.setThemeID(rs.getInt(26));
					rankList.add(rankingSystem);
				}
				Hashes.addRankList("ranking-rank", rankList);
				return rankList;
			} catch (SQLException e) {
				logger.error("SQLgetRankingRank Exception", e);
				return rankList;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return Hashes.getRankList("ranking-rank");
	}
	
	//ranking_profile
	public static ArrayList<Rank> SQLgetRankingProfile() {
		if(Hashes.getRankList("ranking-profile") == null) {
			logger.debug("SQLgetRankingProfile launched. No params passed");
			ArrayList<Rank> rankList = new ArrayList<Rank>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT * FROM ranking_profile");
				stmt = myConn.prepareStatement(sql);
				rs = stmt.executeQuery();
				while(rs.next()) {
					Rank rankingSystem = new Rank();
					rankingSystem.setRankingProfile(rs.getInt(1));
					rankingSystem.setProfileDescription(rs.getString(2));
					rankingSystem.setBarColorProfile(rs.getInt(3));
					rankingSystem.setColorRProfile(rs.getInt(4));
					rankingSystem.setColorGProfile(rs.getInt(5));
					rankingSystem.setColorBProfile(rs.getInt(6));
					rankingSystem.setRankXProfile(rs.getInt(7));
					rankingSystem.setRankYProfile(rs.getInt(8));
					rankingSystem.setRankWidthProfile(rs.getInt(9));
					rankingSystem.setRankHeightProfile(rs.getInt(10));
					rankingSystem.setLevelXProfile(rs.getInt(11));
					rankingSystem.setLevelYProfile(rs.getInt(12));
					rankingSystem.setNameXProfile(rs.getInt(13));
					rankingSystem.setNameYProfile(rs.getInt(14));
					rankingSystem.setBarXProfile(rs.getInt(15));
					rankingSystem.setBarYProfile(rs.getInt(16));
					rankingSystem.setAvatarXProfile(rs.getInt(17));
					rankingSystem.setAvatarYProfile(rs.getInt(18));
					rankingSystem.setAvatarWidthProfile(rs.getInt(19));
					rankingSystem.setAvatarHeightProfile(rs.getInt(20));
					rankingSystem.setExpTextXRank(rs.getInt(21));
					rankingSystem.setExpTextYRank(rs.getInt(22));
					rankingSystem.setPercentTextXRank(rs.getInt(23));
					rankingSystem.setPercentTextYRank(rs.getInt(24));
					rankingSystem.setPlacementXProfile(rs.getInt(25));
					rankingSystem.setPlacementYProfile(rs.getInt(26));
					rankingSystem.setExperienceXProfile(rs.getInt(27));
					rankingSystem.setExperienceYProfile(rs.getInt(28));
					rankingSystem.setCurrencyXProfile(rs.getInt(29));
					rankingSystem.setCurrencyYProfile(rs.getInt(30));
					rankingSystem.setExpReachXProfile(rs.getInt(31));
					rankingSystem.setExpReachYProfile(rs.getInt(32));
					rankingSystem.setThemeID(rs.getInt(33));
					rankList.add(rankingSystem);
				}
				Hashes.addRankList("ranking-profile", rankList);
				return rankList;
			} catch (SQLException e) {
				logger.error("SQLgetRankingProfile Exception", e);
				return rankList;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return Hashes.getRankList("ranking-profile");
	}
	
	//ranking_icon
	public static ArrayList<Rank> SQLgetRankingIcons() {
		if(Hashes.getRankList("ranking-icons") == null) {
			logger.debug("SQLgetRankingIcons launched. No params passed");
			ArrayList<Rank> rankList = new ArrayList<Rank>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT * FROM ranking_icons");
				stmt = myConn.prepareStatement(sql);
				rs = stmt.executeQuery();
				while(rs.next()) {
					Rank rankingSystem = new Rank();
					rankingSystem.setRankingIcon(rs.getInt(1));
					rankingSystem.setIconDescription(rs.getString(2));
					rankingSystem.setThemeID(rs.getInt(3));
					rankList.add(rankingSystem);
				}
				Hashes.addRankList("ranking-icons", rankList);
				return rankList;
			} catch (SQLException e) {
				logger.error("SQLgetRankingIcons Exception", e);
				return rankList;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return Hashes.getRankList("ranking-icons");
	}
	
	//daily_items
	public static int SQLInsertDailyItems(String _description, int _weight, String _type, long _guild_id, int _theme_id) {
		logger.debug("SQLInsertDailyItems launched. Passed params {}, {}, {}, {}, {}", _description, _weight, _type, _guild_id, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO daily_items (description, weight, fk_type, action, fk_theme_id) VALUES(?, ?, ?, \"use\", ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _description);
			stmt.setInt(2, _weight);
			stmt.setString(3, _type);
			stmt.setInt(4, _theme_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertDailyItems Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//inventory
	public static int SQLInsertInventory(long _user_id, long _guild_id, int _item_id, Timestamp _position, int _number, String _status, int _theme_id) {
		logger.debug("SQLInsertInventory launched. Passed params {}, {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _item_id, _position, _number, _status, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO inventory (fk_user_id, fk_item_id, position, number, fk_status, fk_guild_id, fk_theme_id) VALUES(?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE number=VALUES(number)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setTimestamp(3, _position);
			stmt.setInt(4, _number);
			stmt.setString(5, _status);
			stmt.setLong(6, _guild_id);
			stmt.setInt(7, _theme_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertInventory Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertInventoryWithLimit(long _user_id, long _guild_id, int _item_id, Timestamp _position, int _number, String _status, Timestamp _expires, int _theme_id) {
		logger.debug("SQLInsertInventoryWithLimit launched. Passed params {}, {}, {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _item_id, _position, _number, _status, _expires, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO inventory (fk_user_id, fk_item_id, position, number, fk_status, expires, fk_guild_id, fk_theme_id) VALUES(?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE number=VALUES(number), expires=VALUES(expires)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setTimestamp(3, _position);
			stmt.setInt(4, _number);
			stmt.setString(5, _status);
			stmt.setTimestamp(6, _expires);
			stmt.setLong(7, _guild_id);
			stmt.setInt(8, _theme_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertInventoryWithLimitException", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetNumberLimitFromInventory(long _user_id, long _guild_id, int _item_id, int _theme_id) {
		logger.debug("SQLgetNumberLimitFromInventory launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _item_id, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT number FROM inventory WHERE fk_user_id = ? AND fk_item_id = ? AND fk_guild_id = ? AND fk_theme_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setLong(3, _guild_id);
			stmt.setInt(4, _theme_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetNumberLimitFromInventory Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static Timestamp SQLgetExpirationFromInventory(long _user_id, long _guild_id, int _item_id, int _theme_id) {
		logger.debug("SQLgetExpirationFromInventory launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _item_id, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT expires FROM inventory WHERE fk_user_id = ? AND fk_item_id = ? AND fk_guild_id = ? AND fk_theme_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setLong(3, _guild_id);
			stmt.setInt(4, _theme_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getTimestamp(1);
			}
			return null;
		} catch (SQLException e) {
			logger.error("SQLgetExpirationFromInventory Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLDeleteInventory() {
		logger.debug("SQLDeleteInventory launched. No params passed");
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM inventory WHERE fk_status LIKE \"limit\" AND expires-CURRENT_TIMESTAMP <= 0");
			stmt = myConn.prepareStatement(sql);
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteInventory Exception", e);
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetTotalItemNumber(long _user_id, long _guild_id, int _maxItems, int _theme_id) {
		logger.debug("SQLgetTotalItemNumber launched. Passed params {}, {}, {}", _user_id, _guild_id, _maxItems, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT COUNT(*) FROM inventory WHERE fk_user_id = ? && fk_guild_id = ? && fk_theme_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _theme_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1)/_maxItems;
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetTotalItemNumber Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetTotalItemNumber(long _user_id, long _guild_id, String _type, int _maxItems, int _theme_id) {
		logger.debug("SQLgetTotalItemNumber launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _type, _maxItems, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT COUNT(*) FROM inventory INNER JOIN shop_content ON fk_item_id = item_id && inventory.fk_theme_id = shop_content.fk_theme_id WHERE fk_user_id = ? && inventory.fk_guild_id = ? && inventory.fk_theme_id = ? && shop_content.fk_skin = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _theme_id);
			stmt.setString(4, _type);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1)/_maxItems;
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetTotalItemNumber Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetTotalItemNumber(long _user_id, long _guild_id, int _maxItems, String _ignore, boolean _boolIgnore, int _theme_id) {
		logger.debug("SQLgetTotalItemNumber launched. Passed params {}, {}, {}, {}, {}", _user_id, _guild_id, _maxItems, _ignore, _boolIgnore, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT COUNT(*) FROM inventory INNER JOIN shop_content ON fk_item_id = item_id AND inventory.fk_theme_id = shop_content.fk_theme_id WHERE fk_user_id = ? && inventory.fk_guild_id = ? && inventory.fk_theme_id = ? && fk_skin NOT LIKE ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _theme_id);
			stmt.setString(4, _ignore);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1)/_maxItems;
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetTotalItemNumber Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetTotalItemNumber(long _user_id, long _guild_id, int _maxItems, boolean _oneType, int _theme_id) {
		logger.debug("SQLgetTotalItemNumber launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _maxItems, _oneType, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT COUNT(*) FROM inventory INNER JOIN weapon_shop_content ON fk_weapon_id = weapon_id AND inventory.fk_theme_id = weapon_shop_content.fk_theme_id WHERE fk_user_id = ? && fk_guild_id = ? && inventory.fk_theme_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _theme_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1)/_maxItems;
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetTotalItemNumber Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetTotalItemNumber(long _user_id, long _guild_id, int _maxItems, boolean _oneType, String _category, int _theme_id) {
		logger.debug("SQLgetTotalItemNumber launched. Passed params {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _maxItems, _oneType, _category, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT COUNT(*) FROM inventory INNER JOIN weapon_shop_content ON fk_weapon_id = weapon_id AND inventory.fk_theme_id = weapon_shop_content.fk_theme_id INNER JOIN weapon_category ON fk_category_id = category_id && weapon_shop_content.fk_theme_id = weapon_category.fk_theme_id WHERE fk_user_id = ? && fk_guild_id = ? && weapon_category.name = ? && inventory.fk_theme_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, _category);
			stmt.setInt(4, _theme_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1)/_maxItems;
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetTotalItemNumber Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//dailies_usage
	public static Timestamp SQLgetDailiesUsage(long _user_id, long _guild_id) {
		logger.debug("SQLgetDailiesUsage launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT next_daily FROM dailies_usage WHERE fk_user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getTimestamp(1);
			}
			return null;
		} catch (SQLException e) {
			logger.error("SQLgetDailiesUsage Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertDailiesUsage(long _user_id, long _guild_id, Timestamp _opened, Timestamp _next_daily) {
		logger.debug("SQLInsertDailiesUsage launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _opened, _next_daily);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO dailies_usage (fk_user_id, opened, next_daily, fk_guild_id) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE opened=VALUES(opened), next_daily=VALUES(next_daily)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setTimestamp(2, _opened);
			stmt.setTimestamp(3, _next_daily);
			stmt.setLong(4, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertDailiesUsage Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//giveaway
	public static boolean SQLBulkInsertGiveawayRewards(String [] rewards, Timestamp timestamp, long _guild_id) {
		logger.debug("SQLbulkInsertGiveawayRewards launched. Passed params {}, {}, {}", rewards, timestamp, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			myConn.setAutoCommit(false); 
			String sql = ("INSERT INTO giveaway (code, enabled, used, expires, fk_guild_id) VALUES (?, ?, ?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			for(String reward : rewards) {
				stmt.setString(1, reward);
				stmt.setBoolean(2, true);
				stmt.setBoolean(3, false);
				stmt.setTimestamp(4, timestamp);
				stmt.setLong(5, _guild_id);
				stmt.addBatch();
			}
			stmt.executeBatch();
			myConn.commit();
			return false;
		} catch (SQLException e) {
			logger.error("SQLBulkInsertGiveawayRewards Exception", e);
			return true;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static String SQLRetrieveGiveawayReward(long _guild_id) {
		logger.debug("SQLRetrieveGiveawayReward launched. Params passed {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT code FROM giveaway WHERE enabled = 1 && used = 0 && expires >= ? && fk_guild_id = ? LIMIT 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getString(1);
			}
			else
				return "";
		} catch (SQLException e) {
			logger.error("SQLRetrieveGiveawayReward Exception", e);
			return "";
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUsedOnReward(String _code, long _guild_id) {
		logger.debug("SQLUpdateUserOnReward launched. Passed params {}, {}", _code, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE giveaway SET used = 1 WHERE code = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _code);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUsedOnReward Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//themes
	public static boolean SQLgetThemes() {
		logger.debug("SQLgetThemes launched. No params has been passed");
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT theme, theme_id FROM themes");
			var success = false;
			stmt = myConn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while(rs.next()) {
				Hashes.addTheme(rs.getString(1), rs.getInt(2));
				success = true;
			}
			return success;
		} catch (SQLException e) {
			logger.error("SQLgetThemes Exception", e);
			return false;
		} finally {
		try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//JOINS
	public synchronized static Rank SQLgetWholeRankView(long _user_id, long _guild_id) {
		if(Hashes.getRanking(_guild_id+"_"+_user_id) == null) {
			logger.debug("SQLgetWholeRankView launched. Passed params {}, {}", _user_id, _guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT * from all_ranking_users WHERE fk_user_id = ? && fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _user_id);
				stmt.setLong(2, _guild_id);
				rs = stmt.executeQuery();
				if(rs.next()) {
					Rank rank = new Rank();
					rank.setUser_ID(rs.getLong(1));
					rank.setLevel(rs.getInt(2));
					rank.setCurrentExperience(rs.getInt(3));
					rank.setRankUpExperience(rs.getInt(4));
					rank.setExperience(rs.getLong(5));
					rank.setCurrency(rs.getLong(6));
					rank.setCurrentRole(rs.getLong(7));
					rank.setRankingLevel(rs.getInt(8));
					rank.setLevelDescription(rs.getString(9));
					rank.setRankingRank(rs.getInt(10));
					rank.setRankDescription(rs.getString(11));
					rank.setRankingProfile(rs.getInt(12));
					rank.setProfileDescription(rs.getString(13));
					rank.setRankingIcon(rs.getInt(14));
					rank.setIconDescription(rs.getString(15));
					rank.setBarColorProfile(rs.getInt(16));
					rank.setBarColorRank(rs.getInt(17));
					rank.setColorRProfile(rs.getInt(18));
					rank.setColorRRank(rs.getInt(19));
					rank.setColorRLevel(rs.getInt(20));
					rank.setColorGProfile(rs.getInt(21));
					rank.setColorGRank(rs.getInt(22));
					rank.setColorGLevel(rs.getInt(23));
					rank.setColorBProfile(rs.getInt(24));
					rank.setColorBRank(rs.getInt(25));
					rank.setColorBLevel(rs.getInt(26));
					rank.setRankXLevel(rs.getInt(27));
					rank.setRankYLevel(rs.getInt(28));
					rank.setRankWidthLevel(rs.getInt(29));
					rank.setRankHeightLevel(rs.getInt(30));
					rank.setRankXRank(rs.getInt(31));
					rank.setRankYRank(rs.getInt(32));
					rank.setRankWidthRank(rs.getInt(33));
					rank.setRankHeightRank(rs.getInt(34));
					rank.setRankXProfile(rs.getInt(35));
					rank.setRankYProfile(rs.getInt(36));
					rank.setRankWidthProfile(rs.getInt(37));
					rank.setRankHeightProfile(rs.getInt(38));
					rank.setLevelXLevel(rs.getInt(39));
					rank.setLevelYLevel(rs.getInt(40));
					rank.setLevelXProfile(rs.getInt(41));
					rank.setLevelYProfile(rs.getInt(42));
					rank.setNameXLevel(rs.getInt(43));
					rank.setNameYLevel(rs.getInt(44));
					rank.setNameXRank(rs.getInt(45));
					rank.setNameYRank(rs.getInt(46));
					rank.setNameXProfile(rs.getInt(47));
					rank.setNameYProfile(rs.getInt(48));
					rank.setBarXRank(rs.getInt(49));
					rank.setBarYRank(rs.getInt(50));
					rank.setBarXProfile(rs.getInt(51));
					rank.setBarYProfile(rs.getInt(52));
					rank.setAvatarXRank(rs.getInt(53));
					rank.setAvatarYRank(rs.getInt(54));
					rank.setAvatarXProfile(rs.getInt(55));
					rank.setAvatarYProfile(rs.getInt(56));
					rank.setAvatarWidthRank(rs.getInt(57));
					rank.setAvatarHeightRank(rs.getInt(58));
					rank.setAvatarWidthProfile(rs.getInt(59));
					rank.setAvatarHeightProfile(rs.getInt(60));
					rank.setExpTextXRank(rs.getInt(61));
					rank.setExpTextYRank(rs.getInt(62));
					rank.setExpTextXProfile(rs.getInt(63));
					rank.setExpTextYProfile(rs.getInt(64));
					rank.setPercentTextXRank(rs.getInt(65));
					rank.setPercentTextYRank(rs.getInt(66));
					rank.setPercentTextXProfile(rs.getInt(67));
					rank.setPercentTextYProfile(rs.getInt(68));
					rank.setPlacementXRank(rs.getInt(69));
					rank.setPlacementYRank(rs.getInt(70));
					rank.setPlacementXProfile(rs.getInt(71));
					rank.setPlacementYProfile(rs.getInt(72));
					rank.setExperienceXProfile(rs.getInt(73));
					rank.setExperienceYProfile(rs.getInt(74));
					rank.setCurrencyXProfile(rs.getInt(75));
					rank.setCurrencyYProfile(rs.getInt(76));
					rank.setExpReachXProfile(rs.getInt(77));
					rank.setExpReachYProfile(rs.getInt(78));
					rank.setDailyExperience(rs.getInt(79));
					rank.setDailyReset(rs.getTimestamp(80));
					rank.setWeapon1(rs.getInt(81));
					rank.setWeapon2(rs.getInt(82));
					rank.setWeapon3(rs.getInt(83));
					rank.setSkill(rs.getInt(84));
					Hashes.addRanking(_guild_id+"_"+_user_id, rank);
					return rank;
				}
				return null;
			} catch (SQLException e) {
				logger.error("SQLgetWholeRankView Exception", e);
				return null;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return Hashes.getRanking(_guild_id+"_"+_user_id);
	}
	
	public static Guilds SQLgetGuild(long _guild_id) {
		if(Hashes.getStatus(_guild_id) == null) {
			logger.debug("SQLgetGuild launched. Passed params {}", _guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT * FROM guild_settings WHERE guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				rs = stmt.executeQuery();
				if(rs.next()) {
					Guilds guild = new Guilds();
					guild.setName(rs.getString(2));
					guild.setMaxLevel(rs.getInt(3));
					guild.setLevelID(rs.getInt(4));
					guild.setLevelDescription(rs.getString(5));
					guild.setRankID(rs.getInt(6));
					guild.setRankDescription(rs.getString(7));
					guild.setProfileID(rs.getInt(8));
					guild.setProfileDescription(rs.getString(9));
					guild.setIconID(rs.getInt(10));
					guild.setIconDescription(rs.getString(11));
					guild.setRankingState(rs.getBoolean(12));
					guild.setMaxExperience(rs.getLong(13));
					guild.setMaxExpEnabled(rs.getBoolean(14));
					guild.setThemeID(rs.getInt(15));
					guild.setCurrency(rs.getString(16));
					guild.setRandomshopPrice(rs.getLong(17));
					guild.setMessageTimeout(IniFileReader.getMessageTimeout());
					Hashes.addStatus(_guild_id, guild);
					return guild;
				}
				return null;
			} catch (SQLException e) {
				logger.error("SQLgetGuild Exception", e);
				return null;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return Hashes.getStatus(_guild_id);
	}
	
	public static int SQLgetLevels(long _guild_id, int _theme_id) {
		logger.debug("SQLgetLevels launched. Passed params {}, {}", _guild_id, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM level_list_join_roles WHERE fk_theme_id = ? AND (fk_guild_id = ? OR fk_guild_id IS NULL) ORDER BY level");
			var count = 0;
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _theme_id);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				Ranks ranks = new Ranks();
				ranks.setLevel(rs.getInt(1));
				ranks.setExperience(rs.getInt(2));
				ranks.setCurrency(rs.getInt(3));
				ranks.setAssignRole(rs.getLong(4));
				Hashes.addRankingLevels(_guild_id+"_"+rs.getInt(1), ranks);
				count++;
			}
			return count;
		} catch (SQLException e) {
			logger.error("SQLgetLevels Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Skins> SQLgetSkinshopContentAndType(long _guild_id, int _theme_id) {
		logger.debug("SQLgetSkinshopContentAndType launched. Params passed {}, {}", _guild_id, _theme_id);
		ArrayList<Skins> set_skin = new ArrayList<Skins>();
		if(Hashes.getShopContent(_guild_id) == null) {
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT * FROM all_enabled_skins WHERE fk_theme_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setInt(1, _theme_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					Skins insert_skin = new Skins();
					insert_skin.setItemID(rs.getInt(1));
					insert_skin.setShopDescription(rs.getString(2));
					insert_skin.setPrice(rs.getLong(3));
					insert_skin.setSkinType(rs.getString(4));
					insert_skin.setSkinDescription(rs.getString(5));
					insert_skin.setSkinFullDescription(rs.getString(6));
					insert_skin.setThumbnail(rs.getString(7));
					set_skin.add(insert_skin);
				}
				Hashes.addShopContent(_guild_id, set_skin);
				return set_skin;
			} catch (SQLException e) {
				logger.error("SQLgetSkinshopContentAndType Exception", e);
				return set_skin;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return Hashes.getShopContent(_guild_id);
	}
	
	public static int SQLgetItemID(long _user_id, long _guild_id, int _item_id, int _theme_id) {
		logger.debug("SQLgetItemID launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _item_id, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT item_id FROM inventory_items WHERE user_id = ? AND item_id = ? AND guild_id = ? AND theme_id = ? AND enabled = 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setLong(3, _guild_id);
			stmt.setInt(4, _theme_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetItemID Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static Inventory SQLgetItemIDAndSkinType(long _user_id, long _guild_id, String _description, int _theme_id) {
		logger.debug("SQLgetItemIDAndSkinType launched. Passed params {}, {}, {}", _user_id, _guild_id, _description, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT item_id, skin, status FROM inventory_items WHERE user_id = ? AND description LIKE ? AND status LIKE \"perm\" AND guild_id = ? AND theme_id = ? AND enabled = 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setString(2, _description);
			stmt.setLong(3, _guild_id);
			stmt.setInt(4, _theme_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return new Inventory(rs.getInt(1), rs.getString(2), rs.getString(3));
			}
			return null;
		} catch (SQLException e) {
			logger.error("SQLgetItemIDAndSkinType Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetInventoryNumber(long _user_id, long _guild_id, String _description, String _status, int _theme_id) {
		logger.debug("SQLgetInventoryNumber launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _description, _status, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT number FROM inventory_items WHERE user_id = ? AND description LIKE ? AND status LIKE ? AND guild_id = ? AND theme_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setString(2, _description);
			stmt.setString(3, _status);
			stmt.setLong(4, _guild_id);
			stmt.setInt(5, _theme_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetInventoryNumber Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<InventoryContent> SQLgetInventoryAndDescriptions(long _user_id, long _guild_id, int _limit, int _maxItems, int _theme_id) {
		logger.debug("SQLgetInventoryAndDescriptions launched. Passed params {}, {}, {}, {}, {}", _user_id, _guild_id, _limit, _maxItems, _theme_id);
		ArrayList<InventoryContent> inventory = new ArrayList<InventoryContent>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM inventory_list WHERE user_id = ? AND guild_id = ? AND theme_id = ? ORDER BY position desc LIMIT ?, ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _theme_id);
			stmt.setInt(4, _limit);
			stmt.setInt(5, _maxItems);
			rs = stmt.executeQuery();
			while(rs.next()) {
				InventoryContent setInventory = new InventoryContent();
				setInventory.setUserID(rs.getLong(1));
				setInventory.setTimestamp(rs.getTimestamp(2));
				setInventory.setNumber(rs.getInt(3));
				setInventory.setStatus(rs.getString(4));
				setInventory.setExpiration(rs.getTimestamp(5));
				setInventory.setDescription(rs.getString(6));
				setInventory.setType(rs.getString(7));
				setInventory.setWeaponDescription(rs.getString(8));
				setInventory.setStat(rs.getString(9));
				setInventory.setWeaponCategoryID(rs.getInt(10));
				setInventory.setWeaponCategoryDescription(rs.getString(11));
				setInventory.setSkillDescription(rs.getString(12));
				inventory.add(setInventory);
			}
			return inventory;
		} catch (SQLException e) {
			logger.error("SQLgetInventoryAndDescriptions Exception", e);
			return inventory;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<InventoryContent> SQLgetInventoryAndDescriptionsItems(long _user_id, long _guild_id, int _limit, int _maxItems, int _theme_id) {
		logger.debug("SQLgetInventoryAndDescriptionsItems launched. Passed params {}, {}, {}, {}, {}", _user_id, _guild_id, _limit, _maxItems, _theme_id);
		ArrayList<InventoryContent> inventory = new ArrayList<InventoryContent>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM inventory_list WHERE user_id = ? AND guild_id = ? AND theme_id = ? AND skin = \"ite\" ORDER BY position desc LIMIT ?, ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _theme_id);
			stmt.setInt(4, _limit);
			stmt.setInt(5, _maxItems);
			rs = stmt.executeQuery();
			while(rs.next()) {
				InventoryContent setInventory = new InventoryContent();
				setInventory.setUserID(rs.getLong(1));
				setInventory.setTimestamp(rs.getTimestamp(2));
				setInventory.setNumber(rs.getInt(3));
				setInventory.setStatus(rs.getString(4));
				setInventory.setExpiration(rs.getTimestamp(5));
				setInventory.setDescription(rs.getString(6));
				setInventory.setType(rs.getString(7));
				setInventory.setWeaponDescription(rs.getString(8));
				setInventory.setStat(rs.getString(9));
				setInventory.setWeaponCategoryID(rs.getInt(10));
				setInventory.setWeaponCategoryDescription(rs.getString(11));
				setInventory.setSkillDescription(rs.getString(12));
				inventory.add(setInventory);
			}
			return inventory;
		} catch (SQLException e) {
			logger.error("SQLgetInventoryAndDescriptionsItems Exception", e);
			return inventory;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<InventoryContent> SQLgetInventoryAndDescriptionsWeapons(long _user_id, long _guild_id, int _limit, int _maxItems, int _theme_id) {
		logger.debug("SQLgetInventoryAndDescriptionsWeapons launched. Passed params {}, {}, {}, {}, {}", _user_id, _guild_id, _limit, _maxItems, _theme_id);
		ArrayList<InventoryContent> inventory = new ArrayList<InventoryContent>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM inventory_list WHERE user_id = ? AND guild_id = ? AND theme_id = ? AND (weapon_id IS NOT NULL OR skill_id IS NOT NULL) ORDER BY position desc LIMIT ?, ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _theme_id);
			stmt.setInt(4, _limit);
			stmt.setInt(5, _maxItems);
			rs = stmt.executeQuery();
			while(rs.next()) {
				InventoryContent setInventory = new InventoryContent();
				setInventory.setUserID(rs.getLong(1));
				setInventory.setTimestamp(rs.getTimestamp(2));
				setInventory.setNumber(rs.getInt(3));
				setInventory.setStatus(rs.getString(4));
				setInventory.setExpiration(rs.getTimestamp(5));
				setInventory.setDescription(rs.getString(6));
				setInventory.setType(rs.getString(7));
				setInventory.setWeaponDescription(rs.getString(8));
				setInventory.setStat(rs.getString(9));
				setInventory.setWeaponCategoryID(rs.getInt(10));
				setInventory.setWeaponCategoryDescription(rs.getString(11));
				setInventory.setSkillDescription(rs.getString(12));
				inventory.add(setInventory);
			}
			return inventory;
		} catch (SQLException e) {
			logger.error("SQLgetInventoryAndDescriptionsWeapons Exception", e);
			return inventory;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<InventoryContent> SQLgetInventoryAndDescriptionsWeapons(long _user_id, long _guild_id, int _limit, int _maxItems, String _category, int _theme_id) {
		logger.debug("SQLgetInventoryAndDescriptionsWeapons launched. Passed params {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _limit, _maxItems, _category, _theme_id);
		ArrayList<InventoryContent> inventory = new ArrayList<InventoryContent>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = "";
			if(!_category.equalsIgnoreCase("skill")) {
				sql = ("SELECT * FROM inventory_list WHERE user_id = ? AND guild_id = ? AND theme_id = ? AND weapon_id IS NOT NULL AND name LIKE ? ORDER BY position desc LIMIT ?, ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setString(4, _category);
				stmt.setInt(5, _limit);
				stmt.setInt(6, _maxItems);
			}
			else {
				sql = ("SELECT * FROM inventory_list WHERE user_id = ? AND guild_id = ? AND theme_id = ? AND skill_id IS NOT NULL ORDER BY position desc LIMIT ?, ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setInt(4, _limit);
				stmt.setInt(5, _maxItems);
			}
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _theme_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				InventoryContent setInventory = new InventoryContent();
				setInventory.setUserID(rs.getLong(1));
				setInventory.setTimestamp(rs.getTimestamp(2));
				setInventory.setNumber(rs.getInt(3));
				setInventory.setStatus(rs.getString(4));
				setInventory.setExpiration(rs.getTimestamp(5));
				setInventory.setDescription(rs.getString(6));
				setInventory.setType(rs.getString(7));
				setInventory.setWeaponDescription(rs.getString(8));
				setInventory.setStat(rs.getString(9));
				setInventory.setWeaponCategoryID(rs.getInt(10));
				setInventory.setWeaponCategoryDescription(rs.getString(11));
				setInventory.setSkillDescription(rs.getString(12));
				inventory.add(setInventory);
			}
			return inventory;
		} catch (SQLException e) {
			logger.error("SQLgetInventoryAndDescriptionsWeapons Exception", e);
			return inventory;
		} finally {
		  try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<InventoryContent> SQLgetInventoryAndDescriptionsSkins(long _user_id, long _guild_id, int _limit, int _maxItems, int _theme_id) {
		logger.debug("SQLgetInventoryAndDescriptions launched. Passed params {}, {}, {}, {}, {}", _user_id, _guild_id, _limit, _maxItems, _theme_id);
		ArrayList<InventoryContent> inventory = new ArrayList<InventoryContent>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM inventory_list WHERE user_id = ? AND guild_id = ? AND theme_id = ? AND skin != 'ite' ORDER BY position desc LIMIT ?, ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _theme_id);
			stmt.setInt(4, _limit);
			stmt.setInt(5, _maxItems);
			rs = stmt.executeQuery();
			while(rs.next()) {
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
			return inventory;
		} catch (SQLException e) {
			logger.error("SQLgetInventoryAndDescriptions Exception", e);
			return inventory;
		} finally {
		  try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<InventoryContent> SQLgetInventoryAndDescriptionWithoutLimit(long _user_id, long _guild_id, int _theme_id) {
		logger.debug("SQLgetInventoryAndDescritpionWithoutLimit launched. Passed params {}, {}, {}", _user_id, _guild_id, _theme_id);
		ArrayList<InventoryContent> inventory = new ArrayList<InventoryContent>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT user_id, position, description FROM inventory_items WHERE user_id = ? AND guild_id = ? AND theme_id = ? ORDER BY position desc");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _theme_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				InventoryContent setInventory = new InventoryContent();
				setInventory.setUserID(rs.getLong(1));
				setInventory.setTimestamp(rs.getTimestamp(2));
				setInventory.setDescription(rs.getString(3));
				inventory.add(setInventory);
			}
			return inventory;
		} catch (SQLException e) {
			logger.error("SQLgetInventoryAndDescriptionWithoutLimit Exception", e);
			return inventory;
		} finally {
		  try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static InventoryContent SQLgetNumberAndExpirationFromInventory(long _user_id, long _guild_id, String _description, String _status, int _theme_id) {
		logger.debug("SQLgetNumberAndExpirationFromInventory launched. Passed params {}, {}, {}, {}, {}", _user_id, _guild_id, _description, _status, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT number, expires FROM inventory INNER JOIN shop_content ON fk_item_id = item_id AND inventory.fk_theme_id = shop_content.fk_theme_id WHERE fk_user_id = ? AND description = ? AND fk_status = ? AND fk_guild_id = ? AND inventory.fk_theme_id = ? AND enabled = 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setString(2, _description);
			stmt.setString(3, _status);
			stmt.setLong(4, _guild_id);
			stmt.setInt(5, _theme_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				InventoryContent inventory = new InventoryContent();
				inventory.setNumber(rs.getInt(1));
				inventory.setExpiration(rs.getTimestamp(2));
				return inventory;
			}
			return null;
		} catch (SQLException e) {
			logger.error("SQLgetNumberExpirationFromInventory Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Dailies> SQLgetDailiesAndType(long _guild_id, int _theme_id) {
		if(Hashes.getDailyItems("dailies") == null) {
			logger.debug("SQLgetDailiesAndType launched. Params passed {}, {}", _guild_id, _theme_id);
			ArrayList<Dailies> dailies = new ArrayList<Dailies>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT item_id, daily_items.description, weight, type, daily_type.description, action FROM daily_items INNER JOIN daily_type ON fk_type = type WHERE fk_theme_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setInt(1, _theme_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					Dailies setDaily = new Dailies();
					setDaily.setItemId(rs.getInt(1));
					setDaily.setDescription(rs.getString(2));
					setDaily.setWeight(rs.getInt(3));
					setDaily.SetType(rs.getString(4));
					setDaily.setTypeDescription(rs.getString(5));
					setDaily.setAction(rs.getString(6));
					dailies.add(setDaily);
				}
				Hashes.addDailyItems("dailies", dailies);
				return dailies;
			} catch (SQLException e) {
				logger.error("SQLgetDailiesAndType Exception", e);
				return dailies;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return Hashes.getDailyItems("dailies");
	}
	
	//Transaction
	@SuppressWarnings("resource")
	public static int SQLUpdateCurrencyAndInsertInventory(long _user_id, long _guild_id, long _currency, int _item_id, Timestamp _position, int _number, int _theme_id) {
		logger.debug("SQLUpdateCurrencyAndInsertInventory launched. Passed params {}, {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _currency, _item_id, _position, _number, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			myConn.setAutoCommit(false);
			String sql = ("UPDATE user_details SET currency = ? WHERE fk_user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _currency);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
			
			String sql2 = ("INSERT INTO inventory (fk_user_id, fk_item_id, position, number, fk_status, fk_guild_id, fk_theme_id) VALUES(?, ?, ?, ?, \"perm\", ?, ?) ON DUPLICATE KEY UPDATE position=VALUES(position), number=VALUES(number) ");
			stmt = myConn.prepareStatement(sql2);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setTimestamp(3, _position);
			stmt.setInt(4, _number);
			stmt.setLong(5, _guild_id);
			stmt.setInt(6, _theme_id);
			var editedRows = stmt.executeUpdate();
			myConn.commit();	
			return editedRows;
		} catch (SQLException e) {
			logger.error("SQLUpdateCurrencyAndInsertInventory Exception", e);
			try {
				myConn.rollback();
			} catch (SQLException e1) {
				logger.error("SQLUpdateCurrencyAndInsertInventory rollback Exception", e);
			}
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLUpdateCurrencyAndRemoveInventory(long _user_id, long _guild_id, long _currency, int _item_id, int _theme_id) {
		logger.debug("SQLUpdateCurrencyAndRemoveInventory launched. Passed params {}, {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _currency, _item_id, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			myConn.setAutoCommit(false);
			String sql = ("UPDATE user_details SET currency = ? WHERE fk_user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _currency);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
			
			String sql2 = ("DELETE FROM inventory WHERE fk_user_id = ? AND fk_item_id = ? AND fk_guild_id = ? AND fk_theme_id = ?");
			stmt = myConn.prepareStatement(sql2);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setLong(3, _guild_id);
			stmt.setInt(4, _theme_id);
			var editedRows = stmt.executeUpdate();
			myConn.commit();	
			return editedRows;
		} catch (SQLException e) {
			logger.error("SQLUpdateCurrencyAndRemoveInventory Exception", e);
			try {
				myConn.rollback();
			} catch (SQLException e1) {
				logger.error("SQLUpdateCurrencyAndRemoveInventory rollback Exception", e);
			}
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLUpdateAndInsertInventory(long _user_id, long _guild_id, int _number, int _number_limit, int _item_id, Timestamp _position, Timestamp _expiration, int _theme_id) {
		logger.debug("SQLUpdateAndInsertInventory launched. Passed params {}, {}, {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _number, _number_limit, _item_id, _position, _expiration, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			myConn.setAutoCommit(false);
			String sql = ("UPDATE inventory SET number = ? WHERE fk_user_id = ? AND fk_status LIKE \"perm\" AND fk_item_id = ? AND fk_guild_id = ? AND fk_theme_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _number-1);
			stmt.setLong(2, _user_id);
			stmt.setInt(3, _item_id);
			stmt.setLong(4, _guild_id);
			stmt.setInt(5, _theme_id);
			stmt.executeUpdate();
			
			String sql2 = ("INSERT INTO inventory (fk_user_id, fk_item_id, position, number, fk_status, expires, fk_guild_id, fk_theme_id) VALUES(?, ?, ?, ?, \"limit\", ?, ?, ?) ON DUPLICATE KEY UPDATE position=VALUES(position), number=VALUES(number), expires=VALUES(expires) ");
			stmt = myConn.prepareStatement(sql2);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setTimestamp(3, _position);
			stmt.setInt(4, _number_limit);
			stmt.setTimestamp(5, _expiration);
			stmt.setLong(6, _guild_id);
			stmt.setInt(7, _theme_id);
			var editedRows = stmt.executeUpdate();
			myConn.commit();
			return editedRows;
		} catch (SQLException e) {
			logger.error("SQLUpdateAndInsertInventory Exception", e);
			try {
				myConn.rollback();
			} catch (SQLException e1) {
				logger.error("SQLUpdateAndInsertIventory rollback Exception", e);
			}
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLDeleteAndInsertInventory(long _user_id, long _guild_id, int _number, int _item_id, Timestamp _position, Timestamp _expiration, int _theme_id) {
		logger.debug("SQLDeleteAndInsertInventory launched. Passed params {}, {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _number, _item_id, _position, _expiration, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			myConn.setAutoCommit(false);
			String sql = ("DELETE FROM inventory WHERE fk_user_id = ? AND fk_status LIKE \"perm\" AND fk_item_id = ? AND fk_guild_id = ? AND fk_theme_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setLong(3, _guild_id);
			stmt.setInt(4, _theme_id);
			stmt.executeUpdate();
			
			String sql2 = ("INSERT INTO inventory (fk_user_id, fk_item_id, position, number, fk_status, expires, fk_guild_id, fk_theme_id) VALUES(?, ?, ?, ?, \"limit\", ?, ?, ?) ON DUPLICATE KEY UPDATE number=VALUES(number), expires=VALUES(expires)");
			stmt = myConn.prepareStatement(sql2);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setTimestamp(3, _position);
			stmt.setInt(4, _number);
			stmt.setTimestamp(5, _expiration);
			stmt.setLong(6, _guild_id);
			stmt.setInt(7, _theme_id);
			var editedRows = stmt.executeUpdate();
			myConn.commit();
			return editedRows;
		} catch (SQLException e) {
			logger.error("SQLDeleteAndInsertIventory Exception", e);
			try {
				myConn.rollback();
			} catch (SQLException e1) {
				logger.error("SQLDeleteAndInsertInventory rollback Exception", e);
			}
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//EXISTS
	public static String SQLExpBoosterExistsInInventory(long _user_id, long _guild_id, int _theme_id) {
		logger.debug("SQLExpBoosterExistsInInventory launched. Passed params {}, {}, {}", _user_id, _guild_id, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT DISTINCT description FROM inventory INNER JOIN shop_content ON fk_item_id = item_id AND inventory.fk_theme_id = shop_content.fk_theme_id WHERE fk_status LIKE \"limit\" AND EXISTS (SELECT description FROM daily_items WHERE fk_type LIKE \"exp\" AND daily_items.fk_theme_id = ?) AND fk_user_id = ? AND fk_guild_id = ? AND inventory.fk_theme_id = ? AND enabled = 1");
			var description = "0";
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _theme_id);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			stmt.setInt(4, _theme_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				description = rs.getString(1);
			}
			return description;
		} catch (SQLException e) {
			logger.error("SQLExpBoosterExistsInInventory Exception", e);
			return "0";
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
}