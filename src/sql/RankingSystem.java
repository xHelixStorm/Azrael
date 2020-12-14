package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Dailies;
import constructors.Guilds;
import constructors.Inventory;
import constructors.InventoryContent;
import constructors.Level;
import constructors.Ranking;
import constructors.Roles;
import constructors.Skins;
import constructors.UserIcon;
import constructors.UserLevel;
import constructors.UserProfile;
import constructors.UserRank;
import core.Hashes;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.entities.Member;
import util.STATIC;

public class RankingSystem {
	private static final Logger logger = LoggerFactory.getLogger(RankingSystem.class);
	
	private static String ip = IniFileReader.getSQLIP2();
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
		logger.trace("SQLInsertActionLog launched. Passed params {}, {}, {}, {}, {}", _warning_level, _entity, _guild_id, _event, _notes);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
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
		logger.trace("SQLInsertUser launched. Passed params {}, {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _name, _level_skin, _rank_skin, _profile_skin, _icon_skin);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
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
	
	public static void SQLBulkInsertUsers(List<Member> members, int _level_skin, int _rank_skin, int _profile_skin, int _icon_skin) {
		logger.trace("SQLBulkInsertUsers launched. Passed params member list, {}, {}, {}, {}", _level_skin, _rank_skin, _profile_skin, _icon_skin);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip, "&rewriteBatchedStatements=true"), username, password);
			myConn.setAutoCommit(false);
			String sql = ("INSERT INTO users (user_id, name, level_skin, rank_skin, profile_skin, icon_skin, fk_guild_id) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)");
			stmt = myConn.prepareStatement(sql);
			for(Member member : members) {
				stmt.setLong(1, member.getUser().getIdLong());
				stmt.setString(2, member.getUser().getName()+"#"+member.getUser().getDiscriminator());
				stmt.setInt(3, _level_skin);
				stmt.setInt(4, _rank_skin);
				stmt.setInt(5, _profile_skin);
				stmt.setInt(6, _icon_skin);
				stmt.setLong(7, member.getGuild().getIdLong());
				stmt.addBatch();
			}
			stmt.executeBatch();
			myConn.commit();
		} catch (SQLException e) {
			logger.error("SQLBulkInsertUsers Exception", e);
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUserLevelSkin(long _user_id, long _guild_id, String _name, int _skin_id) {
		logger.trace("SQLUpdateUserLevelSkin launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _name, _skin_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE users SET level_skin = ?, name = ? WHERE user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			if(_skin_id > 0)
				stmt.setInt(1, _skin_id);
			else
				stmt.setNull(1, Types.INTEGER);
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
	
	public static int SQLUpdateUsersDefaultLevelSkin(int _skin_id_old, int _skin_id_new, long _guild_id) {
		logger.trace("SQLUpdateUsersDefaultLevelSkin launched. Passed params {}, {}, {}", _skin_id_old, _skin_id_new, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE users SET level_skin = ? WHERE (level_skin = ? OR level_skin IS NULL) && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			if(_skin_id_new > 0)
				stmt.setInt(1, _skin_id_new);
			else
				stmt.setNull(1, Types.INTEGER);
			stmt.setInt(2, _skin_id_old);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUsersDefaultLevelSkin Exception", e);
			return -1;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUserRankSkin(long _user_id, long _guild_id, String _name, int _skin_id) {
		logger.trace("SQLUpdateUserRankSkin launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _name, _skin_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE users SET rank_skin = ?, name = ? WHERE user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			if(_skin_id > 0)
				stmt.setInt(1, _skin_id);
			else
				stmt.setNull(1, Types.INTEGER);
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
	
	public static int SQLUpdateUsersDefaultRankSkin(int _skin_id_old, int _skin_id_new, long _guild_id) {
		logger.trace("SQLUpdateUsersDefaultRankSkin launched. Passed params {}, {}, {}", _skin_id_old, _skin_id_new, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE users SET rank_skin = ? WHERE (rank_skin = ? OR rank_skin IS NULL) && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			if(_skin_id_new > 0)
				stmt.setInt(1, _skin_id_new);
			else
				stmt.setNull(1, Types.INTEGER);
			stmt.setInt(2, _skin_id_old);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUsersDefaultRankSkin Exception", e);
			return -1;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUserProfileSkin(long _user_id, long _guild_id, String _name, int _skin_id) {
		logger.trace("SQLUpdateUserProfileSkin launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _name, _skin_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE users SET profile_skin = ?, name = ? WHERE user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			if(_skin_id > 0)
				stmt.setInt(1, _skin_id);
			else
				stmt.setNull(1, Types.INTEGER);
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
	
	public static int SQLUpdateUsersDefaultProfileSkin(int _skin_id_old, int _skin_id_new, long _guild_id) {
		logger.trace("SQLUpdateUsersDefaultProfileSkin launched. Passed params {}, {}, {}", _skin_id_old, _skin_id_new, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE users SET profile_skin = ? WHERE (profile_skin = ? OR profile_skin IS NULL) && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			if(_skin_id_new > 0)
				stmt.setInt(1, _skin_id_new);
			else
				stmt.setNull(1, Types.INTEGER);
			stmt.setInt(2, _skin_id_old);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUsersDefaultProfileSkin Exception", e);
			return -1;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUserIconSkin(long _user_id, long _guild_id, String _name, int _skin_id) {
		logger.trace("SQLUpdateUserIconSkin launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _name, _skin_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE users SET icon_skin = ?, name = ? WHERE user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			if(_skin_id > 0)
				stmt.setInt(1, _skin_id);
			else
				stmt.setNull(1, Types.INTEGER);
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
	
	public static int SQLUpdateUsersDefaultIconSkin(int _skin_id_old, int _skin_id_new, long _guild_id) {
		logger.trace("SQLUpdateUsersDefaultIconSkin launched. Passed params {}, {}, {}", _skin_id_old, _skin_id_new, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE users SET icon_skin = ? WHERE (icon_skin = ? OR icon_skin IS NULL) && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			if(_skin_id_new > 0)
				stmt.setInt(1, _skin_id_new);
			else
				stmt.setNull(1, Types.INTEGER);
			stmt.setInt(2, _skin_id_old);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUsersDefaulticonSkin Exception", e);
			return -1;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//guilds table 
	public static int SQLInsertGuild(long _guild_id, String _name, boolean _enabled) {
		logger.trace("SQLInsertGuild launched. Passed params {}, {}, {}", _guild_id, _name, _enabled);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("INSERT INTO guilds (guild_id, name, ranking_state, max_experience, enabled) VALUES (?, ?, ?, 0, 0) ON DUPLICATE KEY UPDATE name=VALUES(name), ranking_state=VALUES(ranking_state)");
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
	
	public static int SQLUpdateMaxExperience(long _experience, boolean _enabled, long _guild_id) {
		logger.trace("SQLUpdateMaxExperience launched. Passed params {}, {}, {}", _experience, _enabled, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE guilds SET max_experience = ?, enabled = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _experience);
			stmt.setBoolean(2, _enabled);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateMaxExperience Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateRankingSystem(long _guild_id, String _guild_name, boolean _ranking_state) {
		logger.trace("SQLUpdateRankingSystem launched. Passed params {}, {}, {}", _guild_id, _guild_name, _ranking_state);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
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
		logger.trace("SQLUpdateLevelDefaultSkin launched. Passed params {}, {}, {}", _guild_id, _guild_name, _level_skin);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE guilds SET name = ?, fk_level_id = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _guild_name);
			if(_level_skin > 0)
				stmt.setInt(2, _level_skin);
			else
				stmt.setNull(2, Types.INTEGER);
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
		logger.trace("SQLUpdateRankDefaultSkin launched. Passed params {}, {}, {}", _guild_id, _guild_name, _rank_skin);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE guilds SET name = ?, fk_rank_id = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _guild_name);
			if(_rank_skin > 0)
				stmt.setInt(2, _rank_skin);
			else
				stmt.setNull(2, Types.INTEGER);
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
		logger.trace("SQLUpdateProfileDefaultSkin launched. Passed params {}, {}, {}", _guild_id, _guild_name, _profile_skin);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE guilds SET name = ?, fk_profile_id = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _guild_name);
			if(_profile_skin > 0)
				stmt.setInt(2, _profile_skin);
			else
				stmt.setNull(2, Types.INTEGER);
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
		logger.trace("SQLUpdateIconDefaultSkin launched. Passed params {}, {}, {}", _guild_id, _guild_name, _icon_skin);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE guilds SET name = ?, fk_icon_id = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _guild_name);
			if(_icon_skin > 0)
				stmt.setInt(2, _icon_skin);
			else
				stmt.setNull(2, Types.INTEGER);
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
	public static int SQLInsertRole(long _role_id, String _name, int _role_level_requirement, long _guild_id) {
		logger.trace("SQLInsertRole launched. Passed params {}, {}, {}, {}", _role_id, _name, _role_level_requirement, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("INSERT INTO roles (role_id, name, level_requirement, fk_guild_id) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), level_requirement=VALUES(level_requirement)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _role_id);
			stmt.setString(2, _name);
			stmt.setInt(3, _role_level_requirement);
			stmt.setLong(4, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertRole Exception", e);
			return -1;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLremoveSingleRole(long _role_id, long _guild_id) {
		logger.trace("SQLremoveSingleRole launched. Passed params {}, {}", _role_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
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
		logger.trace("SQLDeleteRole launched. Passed params {}, {}", _role_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
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
		logger.trace("SQLclearRoles launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("DELETE FROM roles WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLclearRoles Exception", e);
			return -1;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Roles> SQLgetRoles(long _guild_id) {
		final var cachedRoles = Hashes.getRankingRoles(_guild_id);
		if(cachedRoles == null) {
			logger.trace("SQLgetRoles launched. Passed params {}", _guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			ArrayList<Roles> roles = new ArrayList<Roles>();
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				String sql = ("SELECT * FROM roles WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					roles.add(new Roles(rs.getLong(1), rs.getString(2), rs.getInt(3)));
				}
				Hashes.addRankingRoles(_guild_id, roles);
				return roles;
			} catch (SQLException e) {
				logger.error("SQLgetRoles Exception", e);
				return null;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return cachedRoles;
	}
	
	public static boolean SQLgetRole(long _role_id, long _guild_id) {
		logger.trace("SQLgetRole launched. Passed params {}, {}", _role_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
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
		logger.trace("SQLInsertUserDetails launched. Passed params {}, {}, {}, {}, {}", _user_id, _level, _experience, _currency, _assigned_role);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("INSERT IGNORE INTO user_details (`fk_user_id`, `level`, `experience`, `currency`, `current_role`, `last_update`, `fk_guild_id`) VALUES (?, ?, ?, ?, ?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _level);
			stmt.setLong(3, _experience);
			stmt.setLong(4, _currency);
			stmt.setLong(5, _assigned_role);
			stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
			stmt.setLong(7, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertUserDetails Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLBulkInsertUserDetails(List<Member> members,  int _level, long _experience, long _currency, long _assigned_role) {
		logger.trace("SQLBulkInsertUserDetails launched. Passed params members list, {}, {}, {}, {}", _level, _experience, _currency, _assigned_role);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip, "&rewriteBatchedStatements=true"), username, password);
			myConn.setAutoCommit(false);
			String sql = ("INSERT IGNORE INTO user_details (`fk_user_id`, `level`, `experience`, `currency`, `current_role`, `last_update`, `fk_guild_id`) VALUES (?, ?, ?, ?, ?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			for(Member member : members) {
				stmt.setLong(1, member.getUser().getIdLong());
				stmt.setInt(2, _level);
				stmt.setLong(3, _experience);
				stmt.setLong(4, _currency);
				stmt.setLong(5, _assigned_role);
				stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
				stmt.setLong(7, member.getGuild().getIdLong());
				stmt.addBatch();
			}
			stmt.executeBatch();
			myConn.commit();
		} catch (SQLException e) {
			logger.error("SQLBulkInsertUserDetails Exception", e);
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static long SQLgetAssignedRole(long _user_id, long _guild_id) {
		logger.trace("SQLgetAssignedRole launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
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
	
	public static int SQLUpdateExperience(long _user_id, long _guild_id, long _experience, Timestamp _last_update) {
		logger.trace("SQLUpdateExperience launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _experience, _last_update);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE user_details SET `experience` = ?, `last_update` = ? WHERE `fk_user_id` = ? AND `fk_guild_id` = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _experience);
			stmt.setTimestamp(2, _last_update);
			stmt.setLong(3, _user_id);
			stmt.setLong(4, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateExperience Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLsetLevelUp(long _user_id, long _guild_id, int _level, long _experience, long _currency, long _assigned_role, Timestamp _last_update) {
		logger.trace("SQLsetLevelUp launched. Passed params {}, {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _level, _experience, _currency, _assigned_role, _last_update);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE user_details SET `level` = ?, `experience` = ?, `currency` = ?, `current_role` = ?, `last_update` = ? WHERE `fk_user_id` = ? AND `fk_guild_id` = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _level);
			stmt.setLong(2, _experience);
			stmt.setLong(3, _currency);
			stmt.setLong(4, _assigned_role);
			stmt.setTimestamp(5, _last_update);
			stmt.setLong(6, _user_id);
			stmt.setLong(7, _guild_id);
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
		logger.trace("SQLUpdateCurrentRole launched. Passed params {}, {}", _guild_id, _role_assign);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
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
	
	public static int SQLUpdateCurrency(long _user_id, long _guild_id, long _currency, Timestamp _last_update) {
		logger.trace("SQLUpdateCurrency launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _currency, _last_update);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE user_details SET `currency` = ?, `last_update` = ? WHERE `fk_user_id` = ? AND `fk_guild_id` = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _currency);
			stmt.setTimestamp(2, _last_update);
			stmt.setLong(3, _user_id);
			stmt.setLong(4, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateCurrency Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Ranking> SQLRanking(long _guild_id) {
		logger.trace("SQLgetRanking launched. Passed params {}", _guild_id);
		ArrayList<Ranking> rankList = new ArrayList<Ranking>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT `fk_user_id`, `Level`, `experience`, @curRank := @curRank + 1 AS Rank FROM `user_details`, (SELECT @curRank := 0) r WHERE fk_guild_id = ? ORDER BY `experience` DESC");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				Ranking rank = new Ranking();
				rank.setUser_ID(rs.getLong(1));
				rank.setLevel(rs.getInt(2));
				rank.setExperience(rs.getLong(3));
				rank.setRank(rs.getInt(4));
				rankList.add(rank);
			}
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
	
	//daily_experience table
	public static int SQLInsertDailyExperience(long _experience, long _user_id, long _guild_id, Timestamp _reset) {
		logger.trace("SQLInsertDailyExperience launched. Passed params {}, {}, {}, {}", _experience, _user_id, _guild_id, _reset);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
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
		logger.trace("SQLDeleteDailyExperience launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
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
	public static UserLevel SQLgetRankingLevel(int _skin_id, long _guild_id) {
		UserLevel skin = Hashes.getLevelSkin(_guild_id, _skin_id);
		if(skin == null) {
			logger.trace("SQLgetRankingLevel launched. Params passed {}, {}", _skin_id, _guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				String sql = ("SELECT * FROM ranking_level WHERE level_id = ? AND fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setInt(1, _skin_id);
				stmt.setLong(2, _guild_id);
				rs = stmt.executeQuery();
				if(rs.next()) {
					UserLevel levelSkin = new UserLevel();
					levelSkin.setSkin(rs.getInt(1));
					levelSkin.setSkinDescription(rs.getString(2));
					levelSkin.setFileType(rs.getString(3));
					levelSkin.setColorR(rs.getInt(4));
					levelSkin.setColorG(rs.getInt(5));
					levelSkin.setColorB(rs.getInt(6));
					levelSkin.setIconX(rs.getInt(7));
					levelSkin.setIconY(rs.getInt(8));
					levelSkin.setIconWidth(rs.getInt(9));
					levelSkin.setIconHeight(rs.getInt(10));
					levelSkin.setLevelX(rs.getInt(11));
					levelSkin.setLevelY(rs.getInt(12));
					levelSkin.setNameX(rs.getInt(13));
					levelSkin.setNameY(rs.getInt(14));
					levelSkin.setNameLengthLimit(rs.getInt(15));
					levelSkin.setTextFontSize(rs.getInt(16));
					levelSkin.setNameFontSize(rs.getInt(17));
					levelSkin.setLine(rs.getInt(18));
					levelSkin.setSource(rs.getString(19));
					Hashes.addLevelSkin(_guild_id, levelSkin.getSkin(), levelSkin);
					return levelSkin;
				}
				return null;
			} catch (SQLException e) {
				logger.error("SQLgetRankingLevel Exception", e);
				return null;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return skin;
	}
	
	public static ArrayList<UserLevel> SQLgetRankingLevelList(long _guild_id) {
		ArrayList<UserLevel> levelList = new ArrayList<UserLevel>();
		final var skins = Hashes.getLevelSkins(_guild_id);
		if(skins == null) {
			logger.trace("SQLgetRankingLevelList launched. Params passed {}", _guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				String sql = ("SELECT * FROM ranking_level WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					UserLevel levelSkin = new UserLevel();
					levelSkin.setSkin(rs.getInt(1));
					levelSkin.setSkinDescription(rs.getString(2));
					levelSkin.setFileType(rs.getString(3));
					levelSkin.setColorR(rs.getInt(4));
					levelSkin.setColorG(rs.getInt(5));
					levelSkin.setColorB(rs.getInt(6));
					levelSkin.setIconX(rs.getInt(7));
					levelSkin.setIconY(rs.getInt(8));
					levelSkin.setIconWidth(rs.getInt(9));
					levelSkin.setIconHeight(rs.getInt(10));
					levelSkin.setLevelX(rs.getInt(11));
					levelSkin.setLevelY(rs.getInt(12));
					levelSkin.setNameX(rs.getInt(13));
					levelSkin.setNameY(rs.getInt(14));
					levelSkin.setNameLengthLimit(rs.getInt(15));
					levelSkin.setTextFontSize(rs.getInt(16));
					levelSkin.setNameFontSize(rs.getInt(17));
					levelSkin.setLine(rs.getInt(18));
					levelSkin.setSource(rs.getString(19));
					levelList.add(levelSkin);
					Hashes.addLevelSkin(_guild_id, levelSkin.getSkin(), levelSkin);
				}
				return levelList;
			} catch (SQLException e) {
				logger.error("SQLgetRankingLevelList Exception", e);
				return null;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		for(final var skin : skins.values()) {
			levelList.add(skin);
		}
		return levelList;
	}
	
	//ranking_rank
	public static UserRank SQLgetRankingRank(int _skin_id, long _guild_id) {
		UserRank skin = Hashes.getRankSkin(_guild_id, _skin_id);
		if(skin == null) {
			logger.trace("SQLgetRankingRank launched. Params passed {}, {}", _skin_id, _guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				String sql = ("SELECT * FROM ranking_rank WHERE rank_id = ? AND fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setInt(1, _skin_id);
				stmt.setLong(2, _guild_id);
				rs = stmt.executeQuery();
				if(rs.next()) {
					UserRank rankSkin = new UserRank();
					rankSkin.setSkin(rs.getInt(1));
					rankSkin.setSkinDescription(rs.getString(2));
					rankSkin.setFileType(rs.getString(3));
					rankSkin.setBarColor(rs.getInt(4));
					rankSkin.setColorR(rs.getInt(5));
					rankSkin.setColorG(rs.getInt(6));
					rankSkin.setColorB(rs.getInt(7));
					rankSkin.setIconX(rs.getInt(8));
					rankSkin.setIconY(rs.getInt(9));
					rankSkin.setIconWidth(rs.getInt(10));
					rankSkin.setIconHeight(rs.getInt(11));
					rankSkin.setNameX(rs.getInt(12));
					rankSkin.setNameY(rs.getInt(13));
					rankSkin.setBarX(rs.getInt(14));
					rankSkin.setBarY(rs.getInt(15));
					rankSkin.setAvatarX(rs.getInt(16));
					rankSkin.setAvatarY(rs.getInt(17));
					rankSkin.setAvatarWidth(rs.getInt(18));
					rankSkin.setAvatarHeight(rs.getInt(19));
					rankSkin.setExpTextX(rs.getInt(20));
					rankSkin.setExpTextY(rs.getInt(21));
					rankSkin.setPercentTextX(rs.getInt(22));
					rankSkin.setPercentTextY(rs.getInt(23));
					rankSkin.setPlacementX(rs.getInt(24));
					rankSkin.setPlacementY(rs.getInt(25));
					rankSkin.setNameLengthLimit(rs.getInt(26));
					rankSkin.setTextFontSize(rs.getInt(27));
					rankSkin.setNameFontSize(rs.getInt(28));
					rankSkin.setLine(rs.getInt(29));
					rankSkin.setSource(rs.getString(30));
					Hashes.addRankSkin(_guild_id, rankSkin.getSkin(), rankSkin);
					return rankSkin;
				}
				return null;
			} catch (SQLException e) {
				logger.error("SQLgetRankingRank Exception", e);
				return null;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return skin;
	}
	
	public static ArrayList<UserRank> SQLgetRankingRankList(long _guild_id) {
		ArrayList<UserRank> skinList = new ArrayList<UserRank>();
		final var skins = Hashes.getRankSkins(_guild_id);
		if(skins == null) {
			logger.trace("SQLgetRankingRankList launched. Params passed {}", _guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				String sql = ("SELECT * FROM ranking_rank WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					UserRank rankSkin = new UserRank();
					rankSkin.setSkin(rs.getInt(1));
					rankSkin.setSkinDescription(rs.getString(2));
					rankSkin.setFileType(rs.getString(3));
					rankSkin.setBarColor(rs.getInt(4));
					rankSkin.setColorR(rs.getInt(5));
					rankSkin.setColorG(rs.getInt(6));
					rankSkin.setColorB(rs.getInt(7));
					rankSkin.setIconX(rs.getInt(8));
					rankSkin.setIconY(rs.getInt(9));
					rankSkin.setIconWidth(rs.getInt(10));
					rankSkin.setIconHeight(rs.getInt(11));
					rankSkin.setNameX(rs.getInt(12));
					rankSkin.setNameY(rs.getInt(13));
					rankSkin.setBarX(rs.getInt(14));
					rankSkin.setBarY(rs.getInt(15));
					rankSkin.setAvatarX(rs.getInt(16));
					rankSkin.setAvatarY(rs.getInt(17));
					rankSkin.setAvatarWidth(rs.getInt(18));
					rankSkin.setAvatarHeight(rs.getInt(19));
					rankSkin.setExpTextX(rs.getInt(20));
					rankSkin.setExpTextY(rs.getInt(21));
					rankSkin.setPercentTextX(rs.getInt(22));
					rankSkin.setPercentTextY(rs.getInt(23));
					rankSkin.setPlacementX(rs.getInt(24));
					rankSkin.setPlacementY(rs.getInt(25));
					rankSkin.setNameLengthLimit(rs.getInt(26));
					rankSkin.setTextFontSize(rs.getInt(27));
					rankSkin.setNameFontSize(rs.getInt(28));
					rankSkin.setLine(rs.getInt(29));
					rankSkin.setSource(rs.getString(30));
					Hashes.addRankSkin(_guild_id, rankSkin.getSkin(), rankSkin);
					skinList.add(rankSkin);
				}
				return skinList;
			} catch (SQLException e) {
				logger.error("SQLgetRankingRankList Exception", e);
				return null;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		for(final var skin : skins.values()) {
			skinList.add(skin);
		}
		return skinList;
	}
	
	//ranking_profile
	public static UserProfile SQLgetRankingProfile(int _skin_id, long _guild_id) {
		UserProfile skin = Hashes.getProfileSkin(_guild_id, _skin_id);
		if(skin == null) {
			logger.trace("SQLgetRankingProfile launched. Params passed {}, {}", _skin_id, _guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				String sql = ("SELECT * FROM ranking_profile WHERE profile_id = ? AND fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setInt(1, _skin_id);
				stmt.setLong(2, _guild_id);
				rs = stmt.executeQuery();
				if(rs.next()) {
					UserProfile profileSkin = new UserProfile();
					profileSkin.setSkin(rs.getInt(1));
					profileSkin.setSkinDescription(rs.getString(2));
					profileSkin.setFileType(rs.getString(3));
					profileSkin.setBarColor(rs.getInt(4));
					profileSkin.setColorR(rs.getInt(5));
					profileSkin.setColorG(rs.getInt(6));
					profileSkin.setColorB(rs.getInt(7));
					profileSkin.setIconX(rs.getInt(8));
					profileSkin.setIconY(rs.getInt(9));
					profileSkin.setIconWidth(rs.getInt(10));
					profileSkin.setIconHeight(rs.getInt(11));
					profileSkin.setLevelX(rs.getInt(12));
					profileSkin.setLevelY(rs.getInt(13));
					profileSkin.setNameX(rs.getInt(14));
					profileSkin.setNameY(rs.getInt(15));
					profileSkin.setBarX(rs.getInt(16));
					profileSkin.setBarY(rs.getInt(17));
					profileSkin.setAvatarX(rs.getInt(18));
					profileSkin.setAvatarY(rs.getInt(19));
					profileSkin.setAvatarWidth(rs.getInt(20));
					profileSkin.setAvatarHeight(rs.getInt(21));
					profileSkin.setExpTextX(rs.getInt(22));
					profileSkin.setExpTextY(rs.getInt(23));
					profileSkin.setPercentTextX(rs.getInt(24));
					profileSkin.setPercentTextY(rs.getInt(25));
					profileSkin.setPlacementX(rs.getInt(26));
					profileSkin.setPlacementY(rs.getInt(27));
					profileSkin.setExperienceX(rs.getInt(28));
					profileSkin.setExperienceY(rs.getInt(29));
					profileSkin.setCurrencyX(rs.getInt(30));
					profileSkin.setCurrencyY(rs.getInt(31));
					profileSkin.setExpReachX(rs.getInt(32));
					profileSkin.setExpReachY(rs.getInt(33));
					profileSkin.setNameLengthLimit(rs.getInt(34));
					profileSkin.setTextFontSize(rs.getInt(35));
					profileSkin.setNameFontSize(rs.getInt(36));
					profileSkin.setDescriptionMode(rs.getInt(37));
					profileSkin.setLine(rs.getInt(38));
					profileSkin.setSource(rs.getString(39));
					Hashes.addProfileSkin(_guild_id, profileSkin.getSkin(), profileSkin);
					return profileSkin;
				}
				return null;
			} catch (SQLException e) {
				logger.error("SQLgetRankingProfile Exception", e);
				return null;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return skin;
	}
	
	public static ArrayList<UserProfile> SQLgetRankingProfileList(long _guild_id) {
		ArrayList<UserProfile> skinList = new ArrayList<UserProfile>();
		final var skins = Hashes.getProfileSkins(_guild_id);
		if(skins == null) {
			logger.trace("SQLgetRankingProfileList launched. Params passed {}", _guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				String sql = ("SELECT * FROM ranking_profile WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					UserProfile profileSkin = new UserProfile();
					profileSkin.setSkin(rs.getInt(1));
					profileSkin.setSkinDescription(rs.getString(2));
					profileSkin.setFileType(rs.getString(3));
					profileSkin.setBarColor(rs.getInt(4));
					profileSkin.setColorR(rs.getInt(5));
					profileSkin.setColorG(rs.getInt(6));
					profileSkin.setColorB(rs.getInt(7));
					profileSkin.setIconX(rs.getInt(8));
					profileSkin.setIconY(rs.getInt(9));
					profileSkin.setIconWidth(rs.getInt(10));
					profileSkin.setIconHeight(rs.getInt(11));
					profileSkin.setLevelX(rs.getInt(12));
					profileSkin.setLevelY(rs.getInt(13));
					profileSkin.setNameX(rs.getInt(14));
					profileSkin.setNameY(rs.getInt(15));
					profileSkin.setBarX(rs.getInt(16));
					profileSkin.setBarY(rs.getInt(17));
					profileSkin.setAvatarX(rs.getInt(18));
					profileSkin.setAvatarY(rs.getInt(19));
					profileSkin.setAvatarWidth(rs.getInt(20));
					profileSkin.setAvatarHeight(rs.getInt(21));
					profileSkin.setExpTextX(rs.getInt(22));
					profileSkin.setExpTextY(rs.getInt(23));
					profileSkin.setPercentTextX(rs.getInt(24));
					profileSkin.setPercentTextY(rs.getInt(25));
					profileSkin.setPlacementX(rs.getInt(26));
					profileSkin.setPlacementY(rs.getInt(27));
					profileSkin.setExperienceX(rs.getInt(28));
					profileSkin.setExperienceY(rs.getInt(29));
					profileSkin.setCurrencyX(rs.getInt(30));
					profileSkin.setCurrencyY(rs.getInt(31));
					profileSkin.setExpReachX(rs.getInt(32));
					profileSkin.setExpReachY(rs.getInt(33));
					profileSkin.setNameLengthLimit(rs.getInt(34));
					profileSkin.setTextFontSize(rs.getInt(35));
					profileSkin.setNameFontSize(rs.getInt(36));
					profileSkin.setDescriptionMode(rs.getInt(37));
					profileSkin.setLine(rs.getInt(38));
					profileSkin.setSource(rs.getString(39));
					Hashes.addProfileSkin(_guild_id, profileSkin.getSkin(), profileSkin);
					skinList.add(profileSkin);
				}
				return skinList;
			} catch (SQLException e) {
				logger.error("SQLgetRankingProfileList Exception", e);
				return null;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		for(final var skin : skins.values()) {
			skinList.add(skin);
		}
		return skinList;
	}
	
	//ranking_icon
	public static UserIcon SQLgetRankingIcons(int _skin_id, long _guild_id) {
		UserIcon skin = Hashes.getIconSkin(_guild_id, _skin_id);
		if(skin == null) {
			logger.trace("SQLgetRankingIcons launched. Params passed {}, {}", _skin_id, _guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				String sql = ("SELECT * FROM ranking_icons WHERE fk_guild_id = ? AND icon_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				stmt.setInt(2, _skin_id);
				rs = stmt.executeQuery();
				if(rs.next()) {
					UserIcon iconSkin = new UserIcon();
					iconSkin.setSkin(rs.getInt(1));
					iconSkin.setSkinDescription(rs.getString(2));
					iconSkin.setFileType(rs.getString(3));
					iconSkin.setLine(rs.getInt(4));
					Hashes.addIconSkin(_guild_id, iconSkin.getSkin(), iconSkin);
					return iconSkin;
				}
				return null;
			} catch (SQLException e) {
				logger.error("SQLgetRankingIcons Exception", e);
				return null;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return skin;
	}
	
	public static ArrayList<UserIcon> SQLgetRankingIconsList(long _guild_id) {
		ArrayList<UserIcon> skinList = new ArrayList<UserIcon>();
		final var skins = Hashes.getIconSkins(_guild_id);
		if(skins == null) {
			logger.trace("SQLgetRankingIconsList launched. Params passed {}", _guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				String sql = ("SELECT * FROM ranking_icons WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					UserIcon iconSkin = new UserIcon();
					iconSkin.setSkin(rs.getInt(1));
					iconSkin.setSkinDescription(rs.getString(2));
					iconSkin.setFileType(rs.getString(3));
					iconSkin.setLine(rs.getInt(4));
					Hashes.addIconSkin(_guild_id, iconSkin.getSkin(), iconSkin);
					skinList.add(iconSkin);
				}
				return skinList;
			} catch (SQLException e) {
				logger.error("SQLgetRankingIconsList Exception", e);
				return null;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		for(final var skin : skins.values()) {
			skinList.add(skin);
		}
		return skinList;
	}
	
	//daily_items
	public static int SQLInsertDailyItems(String _description, int _weight, String _type, long _guild_id) {
		logger.trace("SQLInsertDailyItems launched. Passed params {}, {}, {}, {}", _description, _weight, _type, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("INSERT INTO daily_items (description, weight, fk_type, action, fk_guild_id) VALUES(?, ?, ?, \"use\", ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _description);
			stmt.setInt(2, _weight);
			stmt.setString(3, _type);
			stmt.setLong(4, _guild_id);
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
	public static int SQLInsertInventory(long _user_id, long _guild_id, int _item_id, Timestamp _position, int _number, String _status) {
		logger.trace("SQLInsertInventory launched. Passed params {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _item_id, _position, _number, _status);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("INSERT INTO inventory (fk_user_id, fk_item_id, position, number, fk_status, fk_guild_id) VALUES(?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE number=VALUES(number)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setTimestamp(3, _position);
			stmt.setInt(4, _number);
			stmt.setString(5, _status);
			stmt.setLong(6, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertInventory Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertInventoryWithLimit(long _user_id, long _guild_id, int _item_id, Timestamp _position, int _number, String _status, Timestamp _expires) {
		logger.trace("SQLInsertInventoryWithLimit launched. Passed params {}, {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _item_id, _position, _number, _status, _expires);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("INSERT INTO inventory (fk_user_id, fk_item_id, position, number, fk_status, expires, fk_guild_id) VALUES(?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE number=VALUES(number), expires=VALUES(expires)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setTimestamp(3, _position);
			stmt.setInt(4, _number);
			stmt.setString(5, _status);
			stmt.setTimestamp(6, _expires);
			stmt.setLong(7, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertInventoryWithLimitException", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetNumberLimitFromInventory(long _user_id, long _guild_id, int _item_id) {
		logger.trace("SQLgetNumberLimitFromInventory launched. Passed params {}, {}, {}", _user_id, _guild_id, _item_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT number FROM inventory WHERE fk_user_id = ? AND fk_item_id = ? AND fk_guild_id = ? AND fk_status = \"limit\"");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setLong(3, _guild_id);
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
	
	public static Timestamp SQLgetExpirationFromInventory(long _user_id, long _guild_id, int _item_id) {
		logger.trace("SQLgetExpirationFromInventory launched. Passed params {}, {}, {}", _user_id, _guild_id, _item_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT expires FROM inventory WHERE fk_user_id = ? AND fk_item_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setLong(3, _guild_id);
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
		logger.trace("SQLDeleteInventory launched. No params passed");
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("DELETE FROM inventory WHERE fk_status = \"limit\" AND expires-CURRENT_TIMESTAMP <= 0");
			stmt = myConn.prepareStatement(sql);
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteInventory Exception", e);
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetTotalItemNumber(long _user_id, long _guild_id, int _maxItems) {
		logger.trace("SQLgetTotalItemNumber launched. Passed params {}, {}, {}", _user_id, _guild_id, _maxItems);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT COUNT(*) FROM inventory WHERE fk_user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
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
	
	public static int SQLgetTotalItemNumber(long _user_id, long _guild_id, String _type, int _maxItems) {
		logger.trace("SQLgetTotalItemNumber launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _type, _maxItems);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT COUNT(*) FROM inventory INNER JOIN shop_content ON fk_item_id = item_id && inventory.fk_guild_id = shop_content.fk_guild_id WHERE fk_user_id = ? AND inventory.fk_guild_id = ? AND shop_content.fk_skin = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, _type);
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
	
	public static int SQLgetTotalItemNumber(long _user_id, long _guild_id, int _maxItems, String _ignore, boolean _boolIgnore) {
		logger.trace("SQLgetTotalItemNumber launched. Passed params {}, {}, {}, {}, {}", _user_id, _guild_id, _maxItems, _ignore, _boolIgnore);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT COUNT(*) FROM inventory INNER JOIN shop_content ON fk_item_id = item_id AND inventory.fk_guild_id = shop_content.fk_guild_id WHERE fk_user_id = ? AND inventory.fk_guild_id = ? AND fk_skin != ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, _ignore);
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
	
	public static int SQLgetTotalItemNumber(long _user_id, long _guild_id, int _maxItems, boolean _oneType) {
		logger.trace("SQLgetTotalItemNumber launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _maxItems, _oneType);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT COUNT(*) FROM inventory INNER JOIN weapon_shop_content ON fk_weapon_id = weapon_id AND inventory.fk_guild_id = weapon_shop_content.fk_guild_id WHERE fk_user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
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
	
	public static int SQLgetTotalItemNumber(long _user_id, long _guild_id, int _maxItems, boolean _oneType, String _category) {
		logger.trace("SQLgetTotalItemNumber launched. Passed params {}, {}, {}, {}, {}", _user_id, _guild_id, _maxItems, _oneType, _category);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT COUNT(*) FROM inventory INNER JOIN weapon_shop_content ON fk_weapon_id = weapon_id AND inventory.fk_guild_id = weapon_shop_content.fk_guild_id INNER JOIN weapon_category ON fk_category_id = category_id AND weapon_shop_content.fk_guild_id = weapon_category.fk_guild_id WHERE fk_user_id = ? AND fk_guild_id = ? AND weapon_category.name = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, _category);
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
		logger.trace("SQLgetDailiesUsage launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
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
		logger.trace("SQLInsertDailiesUsage launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _opened, _next_daily);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
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
		logger.trace("SQLbulkInsertGiveawayRewards launched. Passed params {}, {}, {}", rewards, timestamp, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
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
		logger.trace("SQLRetrieveGiveawayReward launched. Params passed {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
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
		logger.trace("SQLUpdateUserOnReward launched. Passed params {}, {}", _code, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
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
	
	public static int SQLUpdateRewardExpiration(long _guild_id, Timestamp _timestamp) {
		logger.trace("SQLUpdateRewardExpiration launched. Passed params {}, {}", _guild_id, _timestamp);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE giveaway SET expires = ? WHERE used = 0 && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setTimestamp(1, _timestamp);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateRewardExpiration Exception", e);
			return -1;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//JOINS
	public synchronized static Ranking SQLgetWholeRankView(long _user_id, long _guild_id) {
		final var user = Hashes.getRanking(_guild_id, _user_id); 
		if(user == null) {
			logger.trace("SQLgetWholeRankView launched. Passed params {}, {}", _user_id, _guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				String sql = ("SELECT * from all_ranking_users WHERE fk_user_id = ? && fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _user_id);
				stmt.setLong(2, _guild_id);
				rs = stmt.executeQuery();
				if(rs.next()) {
					Ranking rank = new Ranking();
					rank.setUser_ID(rs.getLong(1));
					rank.setLevel(rs.getInt(2));
					rank.setDisplayLevel(rs.getInt(3));
					rank.setCurrentExperience(rs.getInt(4));
					rank.setRankUpExperience(rs.getInt(5));
					rank.setExperience(rs.getLong(6));
					rank.setCurrency(rs.getLong(7));
					rank.setCurrentRole(rs.getLong(8));
					rank.setRankingLevel(rs.getInt(9));
					rank.setRankingRank(rs.getInt(10));
					rank.setRankingProfile(rs.getInt(11));
					rank.setRankingIcon(rs.getInt(12));
					rank.setDailyExperience(rs.getInt(13));
					rank.setDailyReset(rs.getTimestamp(14));
					rank.setWeapon1(rs.getInt(15));
					rank.setWeapon2(rs.getInt(16));
					rank.setWeapon3(rs.getInt(17));
					rank.setSkill(rs.getInt(18));
					rank.setLastUpdate(rs.getTimestamp(19));
					Hashes.addRanking(_guild_id, _user_id, rank);
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
		return user;
	}
	
	public static Guilds SQLgetGuild(long _guild_id) {
		final var status = Hashes.getStatus(_guild_id);
		if(status == null) {
			logger.trace("SQLgetGuild launched. Passed params {}", _guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
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
					guild.setFileTypeLevel(rs.getString(6));
					guild.setRankID(rs.getInt(7));
					guild.setRankDescription(rs.getString(8));
					guild.setFileTypeRank(rs.getString(9));
					guild.setProfileID(rs.getInt(10));
					guild.setProfileDescription(rs.getString(11));
					guild.setFileTypeProfile(rs.getString(12));
					guild.setIconID(rs.getInt(13));
					guild.setIconDescription(rs.getString(14));
					guild.setFileTypeIcon(rs.getString(15));
					guild.setRankingState(rs.getBoolean(16));
					guild.setMaxExperience(rs.getLong(17));
					guild.setMaxExpEnabled(rs.getBoolean(18));
					guild.setCurrency(rs.getString(19));
					guild.setRandomshopPrice(rs.getLong(20));
					guild.setStartCurrency(rs.getLong(21));
					guild.setInventoryStartX(rs.getInt(22));
					guild.setInventoryStartY(rs.getInt(23));
					guild.setInventoryTabX(rs.getInt(24));
					guild.setInventoryTabY(rs.getInt(25));
					guild.setInventoryPageFontSize(rs.getInt(26));
					guild.setInventoryPageX(rs.getInt(27));
					guild.setInventoryPageY(rs.getInt(28));
					guild.setInventoryTextFontSize(rs.getInt(29));
					guild.setInventoryBoxSizeX(rs.getInt(30));
					guild.setInventoryBoxSizeY(rs.getInt(31));
					guild.setInventoryDescriptionY(rs.getInt(32));
					guild.setInventoryItemSizex(rs.getInt(33));
					guild.setInventoryItemSizey(rs.getInt(34));
					guild.setInventoryNextBoxX(rs.getInt(35));
					guild.setInventoryNextBoxY(rs.getInt(36));
					guild.setInventoryExpirationPositionY(rs.getInt(37));
					guild.setInventoryRowLimit(rs.getInt(38));
					guild.setInventoryMaxItems(rs.getInt(39));
					guild.setRandomshopStartX(rs.getInt(40));
					guild.setRandomshopStartY(rs.getInt(41));
					guild.setRandomshopPageX(rs.getInt(42));
					guild.setRandomshopPageY(rs.getInt(43));
					guild.setRandomshopTextFontSize(rs.getInt(44));
					guild.setRandomshopBoxSizeX(rs.getInt(45));
					guild.setRandomshopBoxSizeY(rs.getInt(46));
					guild.setRandomshopItemSizeX(rs.getInt(47));
					guild.setRandomshopItemSizeY(rs.getInt(48));
					guild.setRandomshopNextBoxX(rs.getInt(49));
					guild.setRandomshopNextBoxY(rs.getInt(50));
					guild.setRandomshopRowLimit(rs.getInt(51));
					guild.setRandomshopMaxItems(rs.getInt(52));
					guild.setRandomshopRewardItemSizeX(rs.getInt(53));
					guild.setRandomshopRewardItemSizeY(rs.getInt(54));
					guild.setDailyRewardX(rs.getInt(55));
					guild.setDailyRewardY(rs.getInt(56));
					guild.setDailyTextFontSize(rs.getInt(57));
					guild.setDailyDescriptionMode(rs.getInt(58));
					guild.setDailyDescriptionX(rs.getInt(59));
					guild.setDailyDescriptionY(rs.getInt(60));
					guild.setDailyDescriptionStartX(rs.getInt(61));
					guild.setDailyFieldSizeX(rs.getInt(62));
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
		return status;
	}
	
	public static ArrayList<Level> SQLgetLevels(long _guild_id) {
		final var levelList = Hashes.getRankingLevels(_guild_id);
		if(levelList == null) {
			logger.trace("SQLgetLevels launched. Passed params {}", _guild_id);
			ArrayList<Level> levels = new ArrayList<Level>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				String sql = ("SELECT * FROM level_list WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					levels.add(new Level(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getInt(7)));
				}
				Hashes.addRankingLevels(_guild_id, levels);
				return levels;
			} catch (SQLException e) {
				logger.error("SQLgetLevels Exception", e);
				return levels;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return levelList;
	}
	
	public static ArrayList<Skins> SQLgetSkinshopContentAndType(long _guild_id) {
		final var shop = Hashes.getShopContent(_guild_id);
		if(shop == null) {
			logger.trace("SQLgetSkinshopContentAndType launched. Params passed {}", _guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			ArrayList<Skins> set_skin = new ArrayList<Skins>();
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				String sql = ("SELECT * FROM all_enabled_skins WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
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
				return null;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return shop;
	}
	
	public static ArrayList<Skins> SQLgetSkinshopContentAndType(long _guild_id, boolean _enabled) {
		final var shop = Hashes.getShopContent(_guild_id);
		if(shop == null) {
			logger.trace("SQLgetSkinshopContentAndType launched. Params passed {}, {}", _guild_id, _enabled);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			ArrayList<Skins> set_skin = new ArrayList<Skins>();
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				String sql = ("SELECT * FROM all_enabled_skins WHERE guild_id = ? AND enabled = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				stmt.setBoolean(2, _enabled);
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
				return null;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return shop;
	}
	
	public static int SQLgetItemID(long _user_id, long _guild_id, int _item_id) {
		logger.trace("SQLgetItemID launched. Passed params {}, {}, {}", _user_id, _guild_id, _item_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT item_id FROM inventory_items WHERE user_id = ? AND item_id = ? AND guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setLong(3, _guild_id);
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
	
	public static Inventory SQLgetItemIDAndSkinType(long _user_id, long _guild_id, String _description) {
		logger.trace("SQLgetItemIDAndSkinType launched. Passed params {}, {}, {}", _user_id, _guild_id, _description);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT item_id, skin, status FROM inventory_items WHERE user_id = ? AND description = ? AND guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setString(2, _description);
			stmt.setLong(3, _guild_id);
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
	
	public static int SQLgetInventoryNumber(long _user_id, long _guild_id, String _description, String _status) {
		logger.trace("SQLgetInventoryNumber launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _description, _status);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT number FROM inventory_items WHERE user_id = ? AND description = ? AND status = ? AND guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setString(2, _description);
			stmt.setString(3, _status);
			stmt.setLong(4, _guild_id);
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
	
	public static ArrayList<InventoryContent> SQLgetInventoryAndDescriptions(long _user_id, long _guild_id, int _limit, int _maxItems) {
		logger.trace("SQLgetInventoryAndDescriptions launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _limit, _maxItems);
		ArrayList<InventoryContent> inventory = new ArrayList<InventoryContent>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT * FROM inventory_list WHERE user_id = ? AND guild_id = ? ORDER BY position desc LIMIT ?, ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _limit);
			stmt.setInt(4, _maxItems);
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
				setInventory.setFileType(rs.getString(8));
				setInventory.setWeaponDescription(rs.getString(9));
				setInventory.setStat(rs.getString(10));
				setInventory.setWeaponCategoryID(rs.getInt(11));
				setInventory.setWeaponCategoryDescription(rs.getString(12));
				setInventory.setSkillDescription(rs.getString(13));
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
	
	public static ArrayList<InventoryContent> SQLgetInventoryAndDescriptionsItems(long _user_id, long _guild_id, int _limit, int _maxItems) {
		logger.trace("SQLgetInventoryAndDescriptionsItems launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _limit, _maxItems);
		ArrayList<InventoryContent> inventory = new ArrayList<InventoryContent>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT * FROM inventory_list WHERE user_id = ? AND guild_id = ? AND skin = \"ite\" ORDER BY position desc LIMIT ?, ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _limit);
			stmt.setInt(4, _maxItems);
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
				setInventory.setFileType(rs.getString(8));
				setInventory.setWeaponDescription(rs.getString(9));
				setInventory.setStat(rs.getString(10));
				setInventory.setWeaponCategoryID(rs.getInt(11));
				setInventory.setWeaponCategoryDescription(rs.getString(12));
				setInventory.setSkillDescription(rs.getString(13));
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
	
	public static ArrayList<InventoryContent> SQLgetInventoryAndDescriptionsWeapons(long _user_id, long _guild_id, int _limit, int _maxItems) {
		logger.trace("SQLgetInventoryAndDescriptionsWeapons launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _limit, _maxItems);
		ArrayList<InventoryContent> inventory = new ArrayList<InventoryContent>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT * FROM inventory_list WHERE user_id = ? AND guild_id = ? AND (weapon_id IS NOT NULL OR skill_id IS NOT NULL) ORDER BY position desc LIMIT ?, ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _limit);
			stmt.setInt(4, _maxItems);
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
				setInventory.setFileType(rs.getString(8));
				setInventory.setWeaponDescription(rs.getString(9));
				setInventory.setStat(rs.getString(10));
				setInventory.setWeaponCategoryID(rs.getInt(11));
				setInventory.setWeaponCategoryDescription(rs.getString(12));
				setInventory.setSkillDescription(rs.getString(13));
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
	
	public static ArrayList<InventoryContent> SQLgetInventoryAndDescriptionsWeapons(long _user_id, long _guild_id, int _limit, int _maxItems, String _category) {
		logger.trace("SQLgetInventoryAndDescriptionsWeapons launched. Passed params {}, {}, {}, {}, {}", _user_id, _guild_id, _limit, _maxItems, _category);
		ArrayList<InventoryContent> inventory = new ArrayList<InventoryContent>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = "";
			if(!_category.equalsIgnoreCase("skill")) {
				sql = ("SELECT * FROM inventory_list WHERE user_id = ? AND guild_id = ? AND weapon_id IS NOT NULL AND name = ? ORDER BY position desc LIMIT ?, ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setString(3, _category);
				stmt.setInt(4, _limit);
				stmt.setInt(5, _maxItems);
			}
			else {
				sql = ("SELECT * FROM inventory_list WHERE user_id = ? AND guild_id = ? AND skill_id IS NOT NULL ORDER BY position desc LIMIT ?, ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setInt(3, _limit);
				stmt.setInt(4, _maxItems);
			}
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
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
				setInventory.setFileType(rs.getString(8));
				setInventory.setWeaponDescription(rs.getString(9));
				setInventory.setStat(rs.getString(10));
				setInventory.setWeaponCategoryID(rs.getInt(11));
				setInventory.setWeaponCategoryDescription(rs.getString(12));
				setInventory.setSkillDescription(rs.getString(13));
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
	
	public static ArrayList<InventoryContent> SQLgetInventoryAndDescriptionsSkins(long _user_id, long _guild_id, int _limit, int _maxItems) {
		logger.trace("SQLgetInventoryAndDescriptionsSkins launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _limit, _maxItems);
		ArrayList<InventoryContent> inventory = new ArrayList<InventoryContent>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT * FROM inventory_list WHERE user_id = ? AND guild_id = ? AND skin != 'ite' ORDER BY position desc LIMIT ?, ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _limit);
			stmt.setInt(4, _maxItems);
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
				setInventory.setFileType(rs.getString(8));
				inventory.add(setInventory);
			}
			return inventory;
		} catch (SQLException e) {
			logger.error("SQLgetInventoryAndDescriptionsSkins Exception", e);
			return inventory;
		} finally {
		  try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<InventoryContent> SQLgetInventoryAndDescriptionWithoutLimit(long _user_id, long _guild_id) {
		logger.trace("SQLgetInventoryAndDescritpionWithoutLimit launched. Passed params {}, {}", _user_id, _guild_id);
		ArrayList<InventoryContent> inventory = new ArrayList<InventoryContent>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT user_id, position, description FROM inventory_items WHERE user_id = ? AND guild_id = ? ORDER BY position desc");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
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
	
	public static InventoryContent SQLgetNumberAndExpirationFromInventory(long _user_id, long _guild_id, String _description, String _status) {
		logger.trace("SQLgetNumberAndExpirationFromInventory launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _description, _status);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT number, expires FROM inventory INNER JOIN shop_content ON fk_item_id = item_id AND inventory.fk_guild_id = shop_content.fk_guild_id WHERE fk_user_id = ? AND description = ? AND fk_status = ? AND shop_content.fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setString(2, _description);
			stmt.setString(3, _status);
			stmt.setLong(4, _guild_id);
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
	
	public static ArrayList<Dailies> SQLgetDailiesAndType(long _guild_id) {
		final var dailyItems = Hashes.getDailyItems(_guild_id);
		if(dailyItems == null) {
			logger.trace("SQLgetDailiesAndType launched. Params passed {}", _guild_id);
			ArrayList<Dailies> dailies = new ArrayList<Dailies>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				String sql = ("SELECT item_id, daily_items.description, weight, type, daily_type.description, action FROM daily_items INNER JOIN daily_type ON fk_type = type WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
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
				Hashes.addDailyItems(_guild_id, dailies);
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
		return dailyItems;
	}
	
	//Transaction
	@SuppressWarnings("resource")
	public static int SQLUpdateCurrencyAndInsertInventory(long _user_id, long _guild_id, long _currency, int _item_id, Timestamp _position, int _number) {
		logger.trace("SQLUpdateCurrencyAndInsertInventory launched. Passed params {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _currency, _item_id, _position, _number);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			myConn.setAutoCommit(false);
			String sql = ("UPDATE user_details SET currency = ?, last_update = ? WHERE fk_user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _currency);
			stmt.setTimestamp(2, _position);
			stmt.setLong(3, _user_id);
			stmt.setLong(4, _guild_id);
			stmt.executeUpdate();
			
			String sql2 = ("INSERT INTO inventory (fk_user_id, fk_item_id, position, number, fk_status, fk_guild_id) VALUES(?, ?, ?, ?, \"perm\", ?) ON DUPLICATE KEY UPDATE position=VALUES(position), number=VALUES(number) ");
			stmt = myConn.prepareStatement(sql2);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setTimestamp(3, _position);
			stmt.setInt(4, _number);
			stmt.setLong(5, _guild_id);
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
	public static int SQLUpdateCurrencyAndRemoveInventory(long _user_id, long _guild_id, long _currency, int _item_id, Timestamp _last_update) {
		logger.trace("SQLUpdateCurrencyAndRemoveInventory launched. Passed params {}, {}, {}, {}, {}", _user_id, _guild_id, _currency, _item_id, _last_update);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			myConn.setAutoCommit(false);
			String sql = ("UPDATE user_details SET currency = ?, last_update = ? WHERE fk_user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _currency);
			stmt.setTimestamp(2, _last_update);
			stmt.setLong(3, _user_id);
			stmt.setLong(4, _guild_id);
			stmt.executeUpdate();
			
			String sql2 = ("DELETE FROM inventory WHERE fk_user_id = ? AND fk_item_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql2);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setLong(3, _guild_id);
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
	public static int SQLUpdateAndInsertInventory(long _user_id, long _guild_id, int _number, int _number_limit, int _item_id, Timestamp _position, Timestamp _expiration) {
		logger.trace("SQLUpdateAndInsertInventory launched. Passed params {}, {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _number, _number_limit, _item_id, _position, _expiration);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			myConn.setAutoCommit(false);
			String sql = ("UPDATE inventory SET number = ? WHERE fk_user_id = ? AND fk_status = \"perm\" AND fk_item_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _number-1);
			stmt.setLong(2, _user_id);
			stmt.setInt(3, _item_id);
			stmt.setLong(4, _guild_id);
			stmt.executeUpdate();
			
			String sql2 = ("INSERT INTO inventory (fk_user_id, fk_item_id, position, number, fk_status, expires, fk_guild_id) VALUES(?, ?, ?, ?, \"limit\", ?, ?) ON DUPLICATE KEY UPDATE position=VALUES(position), number=VALUES(number), expires=VALUES(expires) ");
			stmt = myConn.prepareStatement(sql2);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setTimestamp(3, _position);
			stmt.setInt(4, _number_limit);
			stmt.setTimestamp(5, _expiration);
			stmt.setLong(6, _guild_id);
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
	public static int SQLDeleteAndInsertInventory(long _user_id, long _guild_id, int _number, int _item_id, Timestamp _position, Timestamp _expiration) {
		logger.trace("SQLDeleteAndInsertInventory launched. Passed params {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _number, _item_id, _position, _expiration);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			myConn.setAutoCommit(false);
			String sql = ("DELETE FROM inventory WHERE fk_user_id = ? AND fk_status = \"perm\" AND fk_item_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
			
			String sql2 = ("INSERT INTO inventory (fk_user_id, fk_item_id, position, number, fk_status, expires, fk_guild_id) VALUES(?, ?, ?, ?, \"limit\", ?, ?) ON DUPLICATE KEY UPDATE number=VALUES(number), expires=VALUES(expires)");
			stmt = myConn.prepareStatement(sql2);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setTimestamp(3, _position);
			stmt.setInt(4, _number);
			stmt.setTimestamp(5, _expiration);
			stmt.setLong(6, _guild_id);
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
	public static String SQLExpBoosterExistsInInventory(long _user_id, long _guild_id) {
		logger.trace("SQLExpBoosterExistsInInventory launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT DISTINCT description FROM inventory INNER JOIN shop_content ON fk_item_id = item_id AND inventory.fk_guild_id = shop_content.fk_guild_id WHERE fk_status = \"limit\" AND EXISTS (SELECT description FROM daily_items WHERE fk_type = \"exp\" AND daily_items.fk_guild_id = ?) AND fk_user_id = ? AND inventory.fk_guild_id = ?");
			var description = "0";
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
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