package de.azrael.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Dailies;
import de.azrael.constructors.Guilds;
import de.azrael.constructors.Inventory;
import de.azrael.constructors.InventoryContent;
import de.azrael.constructors.Level;
import de.azrael.constructors.Ranking;
import de.azrael.constructors.Roles;
import de.azrael.constructors.Skins;
import de.azrael.constructors.UserIcon;
import de.azrael.constructors.UserLevel;
import de.azrael.constructors.UserProfile;
import de.azrael.constructors.UserRank;
import de.azrael.core.Hashes;
import de.azrael.fileManagement.GuildIni;
import de.azrael.fileManagement.IniFileReader;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.entities.Member;

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
	public static void SQLInsertActionLog(String warning_level, long entity, long guild_id, String event, String notes) {
		logger.trace("SQLInsertActionLog launched. Passed params {}, {}, {}, {}, {}", warning_level, entity, guild_id, event, notes);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLInsertActionLog);
			stmt.setString(1, warning_level);
			stmt.setLong(2, entity);
			stmt.setLong(3, guild_id);
			stmt.setString(4, event);
			stmt.setString(5, notes);
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertActionLog Exception", e);
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//users table
	public static int SQLInsertUser(long user_id, long guild_id, String name, int level_skin, int rank_skin, int profile_skin, int icon_skin) {
		logger.trace("SQLInsertUser launched. Passed params {}, {}, {}, {}, {}, {}, {}", user_id, guild_id, name, level_skin, rank_skin, profile_skin, icon_skin);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			if(level_skin != 0 || rank_skin != 0 || profile_skin != 0 || icon_skin != 0) {
				stmt = myConn.prepareStatement(RankingSystemStatements.SQLInsertUser);
				stmt.setLong(1, user_id);
				stmt.setString(2, name);
				if(level_skin != 0)
					stmt.setInt(3, level_skin);
				else
					stmt.setNull(3, Types.INTEGER);
				if(rank_skin != 0)
					stmt.setInt(4, rank_skin);
				else
					stmt.setNull(4, Types.INTEGER);
				if(profile_skin != 0)
					stmt.setInt(5, profile_skin);
				else
					stmt.setNull(5, Types.INTEGER);
				if(icon_skin != 0)
					stmt.setInt(6, icon_skin);
				else
					stmt.setInt(6, Types.INTEGER);
				stmt.setLong(7, guild_id);
			}
			else {
				stmt = myConn.prepareStatement(RankingSystemStatements.SQLInsertUser2);
				stmt.setLong(1, user_id);
				stmt.setString(2, name);
				stmt.setLong(3, guild_id);
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
	
	public static void SQLBulkInsertUsers(List<Member> members, int level_skin, int rank_skin, int profile_skin, int icon_skin) {
		logger.trace("SQLBulkInsertUsers launched. Passed params member list, {}, {}, {}, {}", level_skin, rank_skin, profile_skin, icon_skin);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip, "&rewriteBatchedStatements=true"), username, password);
			myConn.setAutoCommit(false);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLBulkInsertUsers);
			for(Member member : members) {
				stmt.setLong(1, member.getUser().getIdLong());
				stmt.setString(2, member.getUser().getName()+"#"+member.getUser().getDiscriminator());
				if(level_skin != 0)
					stmt.setInt(3, level_skin);
				else
					stmt.setNull(3, Types.INTEGER);
				if(rank_skin != 0)
					stmt.setInt(4, rank_skin);
				else
					stmt.setNull(4, Types.INTEGER);
				if(profile_skin != 0)
					stmt.setInt(5, profile_skin);
				else
					stmt.setNull(5, Types.INTEGER);
				if(icon_skin != 0)
					stmt.setInt(6, icon_skin);
				else
					stmt.setInt(6, Types.INTEGER);
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
	
	public static int SQLUpdateUserLevelSkin(long user_id, long guild_id, String name, int skin_id) {
		logger.trace("SQLUpdateUserLevelSkin launched. Passed params {}, {}, {}, {}", user_id, guild_id, name, skin_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateUserLevelSkin);
			if(skin_id > 0)
				stmt.setInt(1, skin_id);
			else
				stmt.setNull(1, Types.INTEGER);
			stmt.setString(2, name);
			stmt.setLong(3, user_id);
			stmt.setLong(4, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUserLevelSkin Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUsersDefaultLevelSkin(int skin_id_old, int skin_id_new, long guild_id) {
		logger.trace("SQLUpdateUsersDefaultLevelSkin launched. Passed params {}, {}, {}", skin_id_old, skin_id_new, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateUsersDefaultLevelSkin);
			if(skin_id_new > 0)
				stmt.setInt(1, skin_id_new);
			else
				stmt.setNull(1, Types.INTEGER);
			stmt.setInt(2, skin_id_old);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUsersDefaultLevelSkin Exception", e);
			return -1;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUserRankSkin(long user_id, long guild_id, String name, int skin_id) {
		logger.trace("SQLUpdateUserRankSkin launched. Passed params {}, {}, {}, {}", user_id, guild_id, name, skin_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateUserRankSkin);
			if(skin_id > 0)
				stmt.setInt(1, skin_id);
			else
				stmt.setNull(1, Types.INTEGER);
			stmt.setString(2, name);
			stmt.setLong(3, user_id);
			stmt.setLong(4, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUserRankSkin Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUsersDefaultRankSkin(int skin_id_old, int skin_id_new, long guild_id) {
		logger.trace("SQLUpdateUsersDefaultRankSkin launched. Passed params {}, {}, {}", skin_id_old, skin_id_new, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateUsersDefaultRankSkin);
			if(skin_id_new > 0)
				stmt.setInt(1, skin_id_new);
			else
				stmt.setNull(1, Types.INTEGER);
			stmt.setInt(2, skin_id_old);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUsersDefaultRankSkin Exception", e);
			return -1;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUserProfileSkin(long user_id, long guild_id, String name, int skin_id) {
		logger.trace("SQLUpdateUserProfileSkin launched. Passed params {}, {}, {}, {}", user_id, guild_id, name, skin_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateUserProfileSkin);
			if(skin_id > 0)
				stmt.setInt(1, skin_id);
			else
				stmt.setNull(1, Types.INTEGER);
			stmt.setString(2, name);
			stmt.setLong(3, user_id);
			stmt.setLong(4, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUserProfileSkin Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUsersDefaultProfileSkin(int skin_id_old, int skin_id_new, long guild_id) {
		logger.trace("SQLUpdateUsersDefaultProfileSkin launched. Passed params {}, {}, {}", skin_id_old, skin_id_new, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateUsersDefaultProfileSkin);
			if(skin_id_new > 0)
				stmt.setInt(1, skin_id_new);
			else
				stmt.setNull(1, Types.INTEGER);
			stmt.setInt(2, skin_id_old);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUsersDefaultProfileSkin Exception", e);
			return -1;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUserIconSkin(long user_id, long guild_id, String name, int skin_id) {
		logger.trace("SQLUpdateUserIconSkin launched. Passed params {}, {}, {}, {}", user_id, guild_id, name, skin_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateUserIconSkin);
			if(skin_id > 0)
				stmt.setInt(1, skin_id);
			else
				stmt.setNull(1, Types.INTEGER);
			stmt.setString(2, name);
			stmt.setLong(3, user_id);
			stmt.setLong(4, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUserIconSkin Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUsersDefaultIconSkin(int skin_id_old, int skin_id_new, long guild_id) {
		logger.trace("SQLUpdateUsersDefaultIconSkin launched. Passed params {}, {}, {}", skin_id_old, skin_id_new, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateUsersDefaultIconSkin);
			if(skin_id_new > 0)
				stmt.setInt(1, skin_id_new);
			else
				stmt.setNull(1, Types.INTEGER);
			stmt.setInt(2, skin_id_old);
			stmt.setLong(3, guild_id);
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
	public static int SQLInsertGuild(long guild_id, String name, boolean enabled) {
		logger.trace("SQLInsertGuild launched. Passed params {}, {}, {}", guild_id, name, enabled);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLInsertGuild);
			stmt.setLong(1, guild_id);
			stmt.setString(2, name);
			stmt.setBoolean(3, enabled);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertGuild Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateMaxExperience(long experience, boolean enabled, long guild_id) {
		logger.trace("SQLUpdateMaxExperience launched. Passed params {}, {}, {}", experience, enabled, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateMaxExperience);
			stmt.setLong(1, experience);
			stmt.setBoolean(2, enabled);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateMaxExperience Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateRankingSystem(long guild_id, String guild_name, boolean ranking_state) {
		logger.trace("SQLUpdateRankingSystem launched. Passed params {}, {}, {}", guild_id, guild_name, ranking_state);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateRankingSystem);
			stmt.setString(1, guild_name);
			stmt.setBoolean(2, ranking_state);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateRankingSystem Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}	
	}
	
	public static int SQLUpdateLevelDefaultSkin(long guild_id, String guild_name, int level_skin) {
		logger.trace("SQLUpdateLevelDefaultSkin launched. Passed params {}, {}, {}", guild_id, guild_name, level_skin);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateLevelDefaultSkin);
			stmt.setString(1, guild_name);
			if(level_skin > 0)
				stmt.setInt(2, level_skin);
			else
				stmt.setNull(2, Types.INTEGER);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateLevelDefaultSkin Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}	
	}
	
	public static int SQLUpdateRankDefaultSkin(long guild_id, String guild_name, int rank_skin) {
		logger.trace("SQLUpdateRankDefaultSkin launched. Passed params {}, {}, {}", guild_id, guild_name, rank_skin);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateRankDefaultSkin);
			stmt.setString(1, guild_name);
			if(rank_skin > 0)
				stmt.setInt(2, rank_skin);
			else
				stmt.setNull(2, Types.INTEGER);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateRankDefaultSkin Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}	
	}
	
	public static int SQLUpdateProfileDefaultSkin(long guild_id, String guild_name, int profile_skin) {
		logger.trace("SQLUpdateProfileDefaultSkin launched. Passed params {}, {}, {}", guild_id, guild_name, profile_skin);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateProfileDefaultSkin);
			stmt.setString(1, guild_name);
			if(profile_skin > 0)
				stmt.setInt(2, profile_skin);
			else
				stmt.setNull(2, Types.INTEGER);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateProfileDefaultSkin Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}	
	}
	
	public static int SQLUpdateIconDefaultSkin(long guild_id, String guild_name, int icon_skin) {
		logger.trace("SQLUpdateIconDefaultSkin launched. Passed params {}, {}, {}", guild_id, guild_name, icon_skin);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateIconDefaultSkin);
			stmt.setString(1, guild_name);
			if(icon_skin > 0)
				stmt.setInt(2, icon_skin);
			else
				stmt.setNull(2, Types.INTEGER);
			stmt.setLong(3, guild_id);
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
	public static int SQLInsertRole(long role_id, String name, int role_level_requirement, long guild_id) {
		logger.trace("SQLInsertRole launched. Passed params {}, {}, {}, {}", role_id, name, role_level_requirement, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLInsertRole);
			stmt.setLong(1, role_id);
			stmt.setString(2, name);
			stmt.setInt(3, role_level_requirement);
			stmt.setLong(4, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertRole Exception", e);
			return -1;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteRole(long role_id, long guild_id) {
		logger.trace("SQLDeleteRole launched. Passed params {}, {}", role_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLDeleteRole);
			stmt.setLong(1, role_id);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteRole Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLclearRoles(long guild_id) {
		logger.trace("SQLclearRoles launched. Passed params {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLclearRoles);
			stmt.setLong(1, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLclearRoles Exception", e);
			return -1;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Roles> SQLgetRoles(long guild_id) {
		final var cachedRoles = Hashes.getRankingRoles(guild_id);
		if(cachedRoles == null) {
			logger.trace("SQLgetRoles launched. Passed params {}", guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			ArrayList<Roles> roles = new ArrayList<Roles>();
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetRoles);
				stmt.setLong(1, guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					roles.add(new Roles(rs.getLong(1), rs.getString(2), rs.getInt(3)));
				}
				Hashes.addRankingRoles(guild_id, roles);
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
	
	public static boolean SQLgetRole(long role_id, long guild_id) {
		logger.trace("SQLgetRole launched. Passed params {}, {}", role_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetRole);
			stmt.setLong(1, role_id);
			stmt.setLong(2, guild_id);
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
	public static int SQLInsertUserDetails(long user_id, long guild_id, int level, long experience, long currency, long assigned_role) {
		logger.trace("SQLInsertUserDetails launched. Passed params {}, {}, {}, {}, {}", user_id, level, experience, currency, assigned_role);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLInsertUserDetails);
			stmt.setLong(1, user_id);
			stmt.setInt(2, level);
			stmt.setLong(3, experience);
			stmt.setLong(4, currency);
			stmt.setLong(5, assigned_role);
			stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
			stmt.setLong(7, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertUserDetails Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLBulkInsertUserDetails(List<Member> members,  int level, long experience, long currency, long assigned_role) {
		logger.trace("SQLBulkInsertUserDetails launched. Passed params members list, {}, {}, {}, {}", level, experience, currency, assigned_role);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip, "&rewriteBatchedStatements=true"), username, password);
			myConn.setAutoCommit(false);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLBulkInsertUserDetails);
			for(Member member : members) {
				stmt.setLong(1, member.getUser().getIdLong());
				stmt.setInt(2, level);
				stmt.setLong(3, experience);
				stmt.setLong(4, currency);
				stmt.setLong(5, assigned_role);
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
	
	public static long SQLgetAssignedRole(long user_id, long guild_id) {
		logger.trace("SQLgetAssignedRole launched. Passed params {}, {}", user_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetAssignedRole);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
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
	
	public static int SQLUpdateExperience(long user_id, long guild_id, long experience, Timestamp last_update) {
		logger.trace("SQLUpdateExperience launched. Passed params {}, {}, {}, {}", user_id, guild_id, experience, last_update);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateExperience);
			stmt.setLong(1, experience);
			stmt.setTimestamp(2, last_update);
			stmt.setLong(3, user_id);
			stmt.setLong(4, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateExperience Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLsetLevelUp(long user_id, long guild_id, int level, long experience, long currency, long assigned_role, Timestamp last_update) {
		logger.trace("SQLsetLevelUp launched. Passed params {}, {}, {}, {}, {}, {}, {}", user_id, guild_id, level, experience, currency, assigned_role, last_update);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLsetLevelUp);
			stmt.setInt(1, level);
			stmt.setLong(2, experience);
			stmt.setLong(3, currency);
			stmt.setLong(4, assigned_role);
			stmt.setTimestamp(5, last_update);
			stmt.setLong(6, user_id);
			stmt.setLong(7, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLsetLevelUp Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateCurrentRole(long guild_id, long role_assign) {
		logger.trace("SQLUpdateCurrentRole launched. Passed params {}, {}", guild_id, role_assign);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateCurrentRole);
			stmt.setLong(1, role_assign);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateCurrentRole Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateCurrency(long user_id, long guild_id, long currency, Timestamp last_update) {
		logger.trace("SQLUpdateCurrency launched. Passed params {}, {}, {}, {}", user_id, guild_id, currency, last_update);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateCurrency);
			stmt.setLong(1, currency);
			stmt.setTimestamp(2, last_update);
			stmt.setLong(3, user_id);
			stmt.setLong(4, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateCurrency Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Ranking> SQLRanking(long guild_id) {
		logger.trace("SQLgetRanking launched. Passed params {}", guild_id);
		ArrayList<Ranking> rankList = new ArrayList<Ranking>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLRanking);
			stmt.setLong(1, guild_id);
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
	public static int SQLInsertDailyExperience(long experience, long user_id, long guild_id, Timestamp reset) {
		logger.trace("SQLInsertDailyExperience launched. Passed params {}, {}, {}, {}", experience, user_id, guild_id, reset);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLInsertDailyExperience);
			stmt.setLong(1, user_id);
			stmt.setLong(2, experience);
			stmt.setTimestamp(3, reset);
			stmt.setLong(4, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertDailyExperience Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteDailyExperience(long user_id, long guild_id) {
		logger.trace("SQLDeleteDailyExperience launched. Passed params {}, {}", user_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLDeleteDailyExperience);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
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
	public static UserLevel SQLgetRankingLevel(int skin_id, long guild_id) {
		UserLevel skin = Hashes.getLevelSkin(guild_id, skin_id);
		if(skin == null) {
			SQLgetRankingLevelList(guild_id);
			return Hashes.getLevelSkin(guild_id, skin_id);
		}
		return skin;
	}
	
	public static ArrayList<UserLevel> SQLgetRankingLevelList(long guild_id) {
		ArrayList<UserLevel> levelList = new ArrayList<UserLevel>();
		final var skins = Hashes.getLevelSkins(guild_id);
		if(skins == null) {
			logger.trace("SQLgetRankingLevelList launched. Params passed {}", guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetRankingLevelList);
				stmt.setLong(1, guild_id);
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
					levelSkin.setFont(rs.getString(18));
					levelSkin.setLine(rs.getInt(19));
					levelSkin.setSource(rs.getString(20));
					levelList.add(levelSkin);
					Hashes.addLevelSkin(guild_id, levelSkin.getSkin(), levelSkin);
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
	public static UserRank SQLgetRankingRank(int skin_id, long guild_id) {
		UserRank skin = Hashes.getRankSkin(guild_id, skin_id);
		if(skin == null) {
			SQLgetRankingRankList(guild_id);
			return Hashes.getRankSkin(guild_id, skin_id);
		}
		return skin;
	}
	
	public static ArrayList<UserRank> SQLgetRankingRankList(long guild_id) {
		ArrayList<UserRank> skinList = new ArrayList<UserRank>();
		final var skins = Hashes.getRankSkins(guild_id);
		if(skins == null) {
			logger.trace("SQLgetRankingRankList launched. Params passed {}", guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetRankingRankList);
				stmt.setLong(1, guild_id);
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
					rankSkin.setFont(rs.getString(29));
					rankSkin.setLine(rs.getInt(30));
					rankSkin.setSource(rs.getString(31));
					Hashes.addRankSkin(guild_id, rankSkin.getSkin(), rankSkin);
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
	public static UserProfile SQLgetRankingProfile(int skin_id, long guild_id) {
		UserProfile skin = Hashes.getProfileSkin(guild_id, skin_id);
		if(skin == null) {
			SQLgetRankingProfileList(guild_id);
			return Hashes.getProfileSkin(guild_id, skin_id);
		}
		return skin;
	}
	
	public static ArrayList<UserProfile> SQLgetRankingProfileList(long guild_id) {
		ArrayList<UserProfile> skinList = new ArrayList<UserProfile>();
		final var skins = Hashes.getProfileSkins(guild_id);
		if(skins == null) {
			logger.trace("SQLgetRankingProfileList launched. Params passed {}", guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetRankingProfileList);
				stmt.setLong(1, guild_id);
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
					profileSkin.setFont(rs.getString(38));
					profileSkin.setLine(rs.getInt(39));
					profileSkin.setSource(rs.getString(40));
					Hashes.addProfileSkin(guild_id, profileSkin.getSkin(), profileSkin);
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
	public static UserIcon SQLgetRankingIcons(int skin_id, long guild_id) {
		UserIcon skin = Hashes.getIconSkin(guild_id, skin_id);
		if(skin == null) {
			SQLgetRankingIconsList(guild_id);
			return Hashes.getIconSkin(guild_id, skin_id);
		}
		return skin;
	}
	
	public static ArrayList<UserIcon> SQLgetRankingIconsList(long guild_id) {
		ArrayList<UserIcon> skinList = new ArrayList<UserIcon>();
		final var skins = Hashes.getIconSkins(guild_id);
		if(skins == null) {
			logger.trace("SQLgetRankingIconsList launched. Params passed {}", guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetRankingIconsList);
				stmt.setLong(1, guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					UserIcon iconSkin = new UserIcon();
					iconSkin.setSkin(rs.getInt(1));
					iconSkin.setSkinDescription(rs.getString(2));
					iconSkin.setFileType(rs.getString(3));
					iconSkin.setLine(rs.getInt(4));
					Hashes.addIconSkin(guild_id, iconSkin.getSkin(), iconSkin);
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
	public static int SQLInsertDailyItems(String description, int weight, String type, long guild_id) {
		logger.trace("SQLInsertDailyItems launched. Passed params {}, {}, {}, {}", description, weight, type, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLInsertDailyItems);
			stmt.setString(1, description);
			stmt.setInt(2, weight);
			stmt.setString(3, type);
			stmt.setLong(4, guild_id);
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
	public static int SQLInsertInventory(long user_id, long guild_id, int item_id, Timestamp position, int number, String status) {
		logger.trace("SQLInsertInventory launched. Passed params {}, {}, {}, {}, {}, {}", user_id, guild_id, item_id, position, number, status);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLInsertInventory);
			stmt.setLong(1, user_id);
			stmt.setInt(2, item_id);
			stmt.setTimestamp(3, position);
			stmt.setInt(4, number);
			stmt.setString(5, status);
			stmt.setLong(6, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertInventory Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertInventoryWithLimit(long user_id, long guild_id, int item_id, Timestamp position, int number, String status, Timestamp expires) {
		logger.trace("SQLInsertInventoryWithLimit launched. Passed params {}, {}, {}, {}, {}, {}, {}", user_id, guild_id, item_id, position, number, status, expires);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLInsertInventoryWithLimit);
			stmt.setLong(1, user_id);
			stmt.setInt(2, item_id);
			stmt.setTimestamp(3, position);
			stmt.setInt(4, number);
			stmt.setString(5, status);
			stmt.setTimestamp(6, expires);
			stmt.setLong(7, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertInventoryWithLimitException", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetNumberLimitFromInventory(long user_id, long guild_id, int item_id) {
		logger.trace("SQLgetNumberLimitFromInventory launched. Passed params {}, {}, {}", user_id, guild_id, item_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetNumberLimitFromInventory);
			stmt.setLong(1, user_id);
			stmt.setInt(2, item_id);
			stmt.setLong(3, guild_id);
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
	
	public static Timestamp SQLgetExpirationFromInventory(long user_id, long guild_id, int item_id) {
		logger.trace("SQLgetExpirationFromInventory launched. Passed params {}, {}, {}", user_id, guild_id, item_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetExpirationFromInventory);
			stmt.setLong(1, user_id);
			stmt.setInt(2, item_id);
			stmt.setLong(3, guild_id);
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
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLDeleteInventory);
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteInventory Exception", e);
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetTotalItemNumber(long user_id, long guild_id, int maxItems) {
		logger.trace("SQLgetTotalItemNumber launched. Passed params {}, {}, {}", user_id, guild_id, maxItems);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetTotalItemNumber);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1)/maxItems;
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
	
	public static int SQLgetTotalItemNumber(long user_id, long guild_id, String type, int maxItems) {
		logger.trace("SQLgetTotalItemNumber launched. Passed params {}, {}, {}, {}", user_id, guild_id, type, maxItems);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetTotalItemNumber2);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			stmt.setString(3, type);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1)/maxItems;
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
	
	public static int SQLgetTotalItemNumber(long user_id, long guild_id, int maxItems, String ignore, boolean boolIgnore) {
		logger.trace("SQLgetTotalItemNumber launched. Passed params {}, {}, {}, {}, {}", user_id, guild_id, maxItems, ignore, boolIgnore);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetTotalItemNumber3);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			stmt.setString(3, ignore);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1)/maxItems;
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
	
	public static int SQLgetTotalItemNumber(long user_id, long guild_id, int maxItems, boolean oneType) {
		logger.trace("SQLgetTotalItemNumber launched. Passed params {}, {}, {}, {}", user_id, guild_id, maxItems, oneType);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetTotalItemNumber4);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1)/maxItems;
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
	
	public static int SQLgetTotalItemNumber(long user_id, long guild_id, int maxItems, boolean oneType, String category) {
		logger.trace("SQLgetTotalItemNumber launched. Passed params {}, {}, {}, {}, {}", user_id, guild_id, maxItems, oneType, category);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetTotalItemNumber5);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			stmt.setString(3, category);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1)/maxItems;
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
	public static Timestamp SQLgetDailiesUsage(long user_id, long guild_id) {
		logger.trace("SQLgetDailiesUsage launched. Passed params {}, {}", user_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetDailiesUsage);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
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
	
	public static int SQLInsertDailiesUsage(long user_id, long guild_id, Timestamp opened, Timestamp next_daily) {
		logger.trace("SQLInsertDailiesUsage launched. Passed params {}, {}, {}, {}", user_id, guild_id, opened, next_daily);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLInsertDailiesUsage);
			stmt.setLong(1, user_id);
			stmt.setTimestamp(2, opened);
			stmt.setTimestamp(3, next_daily);
			stmt.setLong(4, guild_id);
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
	public static boolean SQLBulkInsertGiveawayRewards(String [] rewards, Timestamp timestamp, long guild_id) {
		logger.trace("SQLbulkInsertGiveawayRewards launched. Passed params {}, {}, {}", rewards, timestamp, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			myConn.setAutoCommit(false); 
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLBulkInsertGiveawayRewards);
			for(String reward : rewards) {
				stmt.setString(1, reward);
				stmt.setBoolean(2, true);
				stmt.setBoolean(3, false);
				stmt.setTimestamp(4, timestamp);
				stmt.setLong(5, guild_id);
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
	
	public static String SQLRetrieveGiveawayReward(long guild_id) {
		logger.trace("SQLRetrieveGiveawayReward launched. Params passed {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLRetrieveGiveawayReward);
			stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
			stmt.setLong(2, guild_id);
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
	
	public static int SQLUpdateUsedOnReward(String code, long guild_id) {
		logger.trace("SQLUpdateUserOnReward launched. Passed params {}, {}", code, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateUsedOnReward);
			stmt.setString(1, code);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUsedOnReward Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateRewardExpiration(long guild_id, Timestamp timestamp) {
		logger.trace("SQLUpdateRewardExpiration launched. Passed params {}, {}", guild_id, timestamp);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateRewardExpiration);
			stmt.setTimestamp(1, timestamp);
			stmt.setLong(2, guild_id);
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
	public synchronized static Ranking SQLgetWholeRankView(long user_id, long guild_id) {
		final var user = Hashes.getRanking(guild_id, user_id); 
		if(user == null) {
			logger.trace("SQLgetWholeRankView launched. Passed params {}, {}", user_id, guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetWholeRankView);
				stmt.setLong(1, user_id);
				stmt.setLong(2, guild_id);
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
					Hashes.addRanking(guild_id, user_id, rank);
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
	
	public static Guilds SQLgetGuild(long guild_id) {
		final var status = Hashes.getStatus(guild_id);
		if(status == null) {
			logger.trace("SQLgetGuild launched. Passed params {}", guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetGuild);
				stmt.setLong(1, guild_id);
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
					guild.setMessageTimeout(GuildIni.getMessagesExpRateLimit(guild_id));
					Hashes.addStatus(guild_id, guild);
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
	
	public static ArrayList<Level> SQLgetLevels(long guild_id) {
		final var levelList = Hashes.getRankingLevels(guild_id);
		if(levelList == null) {
			logger.trace("SQLgetLevels launched. Passed params {}", guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				ArrayList<Level> levels = new ArrayList<Level>();
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetLevels);
				stmt.setLong(1, guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					levels.add(new Level(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getInt(7)));
				}
				Hashes.addRankingLevels(guild_id, levels);
				return levels;
			} catch (SQLException e) {
				logger.error("SQLgetLevels Exception", e);
				return null;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return levelList;
	}
	
	public static ArrayList<Skins> SQLgetSkinshopContentAndType(long guild_id) {
		final var shop = Hashes.getShopContent(guild_id);
		if(shop == null) {
			logger.trace("SQLgetSkinshopContentAndType launched. Params passed {}", guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			ArrayList<Skins> set_skin = new ArrayList<Skins>();
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetSkinshopContentAndType);
				stmt.setLong(1, guild_id);
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
				Hashes.addShopContent(guild_id, set_skin);
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
	
	public static ArrayList<Skins> SQLgetSkinshopContentAndType(long guild_id, boolean enabled) {
		final var shop = Hashes.getShopContent(guild_id);
		if(shop == null) {
			logger.trace("SQLgetSkinshopContentAndType launched. Params passed {}, {}", guild_id, enabled);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			ArrayList<Skins> set_skin = new ArrayList<Skins>();
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetSkinshopContentAndType2);
				stmt.setLong(1, guild_id);
				stmt.setBoolean(2, enabled);
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
				Hashes.addShopContent(guild_id, set_skin);
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
	
	public static int SQLgetItemID(long user_id, long guild_id, int item_id) {
		logger.trace("SQLgetItemID launched. Passed params {}, {}, {}", user_id, guild_id, item_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetItemID);
			stmt.setLong(1, user_id);
			stmt.setInt(2, item_id);
			stmt.setLong(3, guild_id);
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
	
	public static Inventory SQLgetItemIDAndSkinType(long user_id, long guild_id, String description) {
		logger.trace("SQLgetItemIDAndSkinType launched. Passed params {}, {}, {}", user_id, guild_id, description);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetItemIDAndSkinType);
			stmt.setLong(1, user_id);
			stmt.setString(2, description);
			stmt.setLong(3, guild_id);
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
	
	public static int SQLgetInventoryNumber(long user_id, long guild_id, String description, String status) {
		logger.trace("SQLgetInventoryNumber launched. Passed params {}, {}, {}, {}", user_id, guild_id, description, status);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetInventoryNumber);
			stmt.setLong(1, user_id);
			stmt.setString(2, description);
			stmt.setString(3, status);
			stmt.setLong(4, guild_id);
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
	
	public static ArrayList<InventoryContent> SQLgetInventoryAndDescriptions(long user_id, long guild_id, int limit, int maxItems) {
		logger.trace("SQLgetInventoryAndDescriptions launched. Passed params {}, {}, {}, {}", user_id, guild_id, limit, maxItems);
		ArrayList<InventoryContent> inventory = new ArrayList<InventoryContent>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetInventoryAndDescriptions);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			stmt.setInt(3, limit);
			stmt.setInt(4, maxItems);
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
	
	public static ArrayList<InventoryContent> SQLgetInventoryAndDescriptionsItems(long user_id, long guild_id, int limit, int maxItems) {
		logger.trace("SQLgetInventoryAndDescriptionsItems launched. Passed params {}, {}, {}, {}", user_id, guild_id, limit, maxItems);
		ArrayList<InventoryContent> inventory = new ArrayList<InventoryContent>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetInventoryAndDescriptionsItems);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			stmt.setInt(3, limit);
			stmt.setInt(4, maxItems);
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
	
	public static ArrayList<InventoryContent> SQLgetInventoryAndDescriptionsWeapons(long user_id, long guild_id, int limit, int maxItems) {
		logger.trace("SQLgetInventoryAndDescriptionsWeapons launched. Passed params {}, {}, {}, {}", user_id, guild_id, limit, maxItems);
		ArrayList<InventoryContent> inventory = new ArrayList<InventoryContent>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetInventoryAndDescriptionsWeapons);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			stmt.setInt(3, limit);
			stmt.setInt(4, maxItems);
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
	
	public static ArrayList<InventoryContent> SQLgetInventoryAndDescriptionsWeapons(long user_id, long guild_id, int limit, int maxItems, String category) {
		logger.trace("SQLgetInventoryAndDescriptionsWeapons launched. Passed params {}, {}, {}, {}, {}", user_id, guild_id, limit, maxItems, category);
		ArrayList<InventoryContent> inventory = new ArrayList<InventoryContent>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			if(!category.equalsIgnoreCase("skill")) {
				stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetInventoryAndDescriptionsWeapons2);
				stmt.setString(3, category);
				stmt.setInt(4, limit);
				stmt.setInt(5, maxItems);
			}
			else {
				stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetInventoryAndDescriptionsWeapons3);
				stmt.setInt(3, limit);
				stmt.setInt(4, maxItems);
			}
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
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
	
	public static ArrayList<InventoryContent> SQLgetInventoryAndDescriptionsSkins(long user_id, long guild_id, int limit, int maxItems) {
		logger.trace("SQLgetInventoryAndDescriptionsSkins launched. Passed params {}, {}, {}, {}", user_id, guild_id, limit, maxItems);
		ArrayList<InventoryContent> inventory = new ArrayList<InventoryContent>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetInventoryAndDescriptionsSkins);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			stmt.setInt(3, limit);
			stmt.setInt(4, maxItems);
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
	
	public static ArrayList<InventoryContent> SQLgetInventoryAndDescriptionWithoutLimit(long user_id, long guild_id) {
		logger.trace("SQLgetInventoryAndDescritpionWithoutLimit launched. Passed params {}, {}", user_id, guild_id);
		ArrayList<InventoryContent> inventory = new ArrayList<InventoryContent>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetInventoryAndDescriptionWithoutLimit);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
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
	
	public static InventoryContent SQLgetNumberAndExpirationFromInventory(long user_id, long guild_id, String description, String status) {
		logger.trace("SQLgetNumberAndExpirationFromInventory launched. Passed params {}, {}, {}, {}", user_id, guild_id, description, status);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetNumberAndExpirationFromInventory);
			stmt.setLong(1, user_id);
			stmt.setString(2, description);
			stmt.setString(3, status);
			stmt.setLong(4, guild_id);
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
	
	public static ArrayList<Dailies> SQLgetDailiesAndType(long guild_id) {
		final var dailyItems = Hashes.getDailyItems(guild_id);
		if(dailyItems == null) {
			logger.trace("SQLgetDailiesAndType launched. Params passed {}", guild_id);
			ArrayList<Dailies> dailies = new ArrayList<Dailies>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetDailiesAndType);
				stmt.setLong(1, guild_id);
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
				Hashes.addDailyItems(guild_id, dailies);
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
	
	public static HashMap<String, Long> SQLgetItemEffects(long guild_id) {
		final var effects = Hashes.getItemEffects(guild_id);
		if(effects == null || effects.isEmpty()) {
			logger.trace("SQLgetDailiesAndType launched. Params passed {}", guild_id);
			HashMap<String, Long> itemEffects = new HashMap<String, Long>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				stmt = myConn.prepareStatement(RankingSystemStatements.SQLgetitemEffects);
				stmt.setLong(1, guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					itemEffects.put(rs.getString(1), rs.getLong(2));
				}
				Hashes.addItemEffects(guild_id, itemEffects);
				return itemEffects;
			} catch (SQLException e) {
				logger.error("SQLgetDailiesAndType Exception", e);
				return itemEffects;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return effects;
	}
	
	//Transaction
	@SuppressWarnings("resource")
	public static int SQLUpdateCurrencyAndInsertInventory(long user_id, long guild_id, long currency, int item_id, Timestamp position, int number) {
		logger.trace("SQLUpdateCurrencyAndInsertInventory launched. Passed params {}, {}, {}, {}, {}, {}", user_id, guild_id, currency, item_id, position, number);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			myConn.setAutoCommit(false);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateCurrencyAndInsertInventory);
			stmt.setLong(1, currency);
			stmt.setTimestamp(2, position);
			stmt.setLong(3, user_id);
			stmt.setLong(4, guild_id);
			stmt.executeUpdate();
			
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateCurrencyAndInsertInventory2);
			stmt.setLong(1, user_id);
			stmt.setInt(2, item_id);
			stmt.setTimestamp(3, position);
			stmt.setInt(4, number);
			stmt.setLong(5, guild_id);
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
	public static int SQLUpdateCurrencyAndRemoveInventory(long user_id, long guild_id, long currency, int item_id, Timestamp last_update) {
		logger.trace("SQLUpdateCurrencyAndRemoveInventory launched. Passed params {}, {}, {}, {}, {}", user_id, guild_id, currency, item_id, last_update);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			myConn.setAutoCommit(false);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateCurrencyAndRemoveInventory);
			stmt.setLong(1, currency);
			stmt.setTimestamp(2, last_update);
			stmt.setLong(3, user_id);
			stmt.setLong(4, guild_id);
			stmt.executeUpdate();
			
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateCurrencyAndRemoveInventory2);
			stmt.setLong(1, user_id);
			stmt.setInt(2, item_id);
			stmt.setLong(3, guild_id);
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
	public static int SQLUpdateAndInsertInventory(long user_id, long guild_id, int number, int number_limit, int item_id, Timestamp position, Timestamp expiration) {
		logger.trace("SQLUpdateAndInsertInventory launched. Passed params {}, {}, {}, {}, {}, {}, {}", user_id, guild_id, number, number_limit, item_id, position, expiration);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			myConn.setAutoCommit(false);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateAndInsertInventory);
			stmt.setInt(1, number-1);
			stmt.setLong(2, user_id);
			stmt.setInt(3, item_id);
			stmt.setLong(4, guild_id);
			stmt.executeUpdate();
			
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLUpdateAndInsertInventory2);
			stmt.setLong(1, user_id);
			stmt.setInt(2, item_id);
			stmt.setTimestamp(3, position);
			stmt.setInt(4, number_limit);
			stmt.setTimestamp(5, expiration);
			stmt.setLong(6, guild_id);
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
	public static int SQLDeleteAndInsertInventory(long user_id, long guild_id, int number, int item_id, Timestamp position, Timestamp expiration) {
		logger.trace("SQLDeleteAndInsertInventory launched. Passed params {}, {}, {}, {}, {}, {}", user_id, guild_id, number, item_id, position, expiration);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			myConn.setAutoCommit(false);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLDeleteAndInsertInventory);
			stmt.setLong(1, user_id);
			stmt.setInt(2, item_id);
			stmt.setLong(3, guild_id);
			stmt.executeUpdate();
			
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLDeleteAndInsertInventory2);
			stmt.setLong(1, user_id);
			stmt.setInt(2, item_id);
			stmt.setTimestamp(3, position);
			stmt.setInt(4, number);
			stmt.setTimestamp(5, expiration);
			stmt.setLong(6, guild_id);
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
	public static ArrayList<String> SQLExpBoosterExistsInInventory(long user_id, long guild_id) {
		logger.trace("SQLExpBoosterExistsInInventory launched. Passed params {}, {}", user_id, guild_id);
		final ArrayList<String> boosters = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			stmt = myConn.prepareStatement(RankingSystemStatements.SQLExpBoosterExistsInInventory);
			stmt.setLong(1, guild_id);
			stmt.setLong(2, user_id);
			stmt.setLong(3, guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				boosters.add(rs.getString(1));
			}
			return boosters;
		} catch (SQLException e) {
			logger.error("SQLExpBoosterExistsInInventory Exception", e);
			return boosters;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
}