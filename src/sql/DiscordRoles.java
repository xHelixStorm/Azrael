package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Roles;
import core.Hashes;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.entities.Role;
import util.STATIC;

public class DiscordRoles {
	private static final Logger logger = LoggerFactory.getLogger(DiscordRoles.class);
	
	private static String ip = IniFileReader.getSQLIP3();
	private static String username = IniFileReader.getSQLUsername3();
	private static String password = IniFileReader.getSQLPassword3();
	
	public static void SQLconnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			logger.error("JDBC driver couldn't be loaded", e);
		}
	}
	
	public static long SQLgetGuild(long guild_id) {
		logger.trace("SQLgetGuild launched. Passed params {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("DiscordRoles", ip), username, password);
			String sql = ("SELECT guild_id FROM guilds WHERE guild_id= ?");
			stmt = myConn.prepareStatement(sql);
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
	
	public static int SQLInsertGuild(long guild_id, String guild_name) {
		logger.trace("SQLInsertGuild launched. Passed params {}, {}", guild_id, guild_name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("DiscordRoles", ip), username, password);
			String sql = ("INSERT INTO guilds(guild_id, name) VALUES(?,?) ON DUPLICATE KEY UPDATE name=VALUES(name)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, guild_id);
			stmt.setString(2, guild_name);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertGuild Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertRole(long guild_id, long role_id, int level, String role_name, String category_abv, boolean persistant) {
		logger.trace("SQLInsertRole launched. Passed params {}, {}, {}, {}, {}, {}", guild_id, role_id, level, role_name, category_abv, persistant);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("DiscordRoles", ip), username, password);
			String sql = ("INSERT INTO roles(role_id, name, level, fk_category_abv, fk_guild_id, persistant) VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE name=VALUES(name), level=VALUES(level), fk_category_abv=VALUES(fk_category_abv), persistant=VALUES(persistant)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, role_id);
			stmt.setString(2, role_name);
			stmt.setInt(3, level);
			stmt.setString(4, category_abv);
			stmt.setLong(5, guild_id);
			stmt.setBoolean(6, persistant);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertRole Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int[] SQLInsertRoles(long guild_id, List<Role> roles) {
		logger.trace("SQLInsertRoles launched. Passed params {}, roles array", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("DiscordRoles", ip), username, password);
			myConn.setAutoCommit(false);
			String sql = ("INSERT INTO roles(role_id, name, level, fk_category_abv, fk_guild_id, persistant) VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE name=VALUES(name), level=VALUES(level), fk_category_abv=VALUES(fk_category_abv), persistant=VALUES(persistant)");
			stmt = myConn.prepareStatement(sql);
			for(var role : roles) {
				if(!role.getName().equals("@everyone")) {
					stmt.setLong(1, role.getIdLong());
					stmt.setString(2, role.getName());
					stmt.setInt(3, 0);
					stmt.setString(4, "def");
					stmt.setLong(5, guild_id);
					stmt.setBoolean(6, false);
					stmt.addBatch();
				}
			}
			var result = stmt.executeBatch();
			myConn.commit();
			return result;
		} catch (SQLException e) {
			logger.error("SQLInsertRole Exception", e);
			return null;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateAllRoles(long guild_id) {
		logger.trace("SQLUpdateAllRoles launched. Passed params {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("DiscordRoles", ip), username, password);
			String sql = ("UPDATE roles SET level = 0, persistant = 0, fk_category_abv = 'def' WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateAllRoles Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateLevel(long guild_id, long role_id, int level) {
		logger.trace("SQLInsertRole launched. Passed params {}, {}, {}", guild_id, role_id, level);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("DiscordRoles", ip), username, password);
			String sql = ("UPDATE roles SET level = ? WHERE role_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, level);
			stmt.setLong(2, role_id);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertRole Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}

	
	public static ArrayList<Roles> SQLgetRoles(long guild_id) {
		final var cachedRoles = Hashes.getDiscordRole(guild_id);
		if(cachedRoles == null) {
			logger.trace("SQLgetRoles launched. Passed params {}", guild_id);
			ArrayList<Roles> roles = new ArrayList<Roles>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("DiscordRoles", ip), username, password);
				String sql = ("SELECT * FROM all_roles WHERE guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					Roles roleDetails = new Roles(
						rs.getLong(1),
						rs.getString(2),
						rs.getInt(3),
						rs.getString(4),
						rs.getString(5),
						rs.getBoolean(7)
					);
					roles.add(roleDetails);
				}
				Hashes.addDiscordRole(guild_id, roles);
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
		return cachedRoles;
	}
	
	public static ArrayList<Roles> SQLgetReactionRoles(long guild_id) {
		final var roles = Hashes.getReactionRoles(guild_id);
		if(roles == null) {
			logger.trace("SQLgetReactionRoles launched. Passed params {}", guild_id);
			ArrayList<Roles> reactionRoles = new ArrayList<Roles>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("DiscordRoles", ip), username, password);
				String sql = ("SELECT * FROM all_roles WHERE guild_id = ? AND category_abv = \"rea\"");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					reactionRoles.add(new Roles(
						rs.getLong(1),
						rs.getString(2),
						rs.getInt(3),
						rs.getString(4),
						rs.getString(5),
						rs.getBoolean(7)
					));
				}
				Hashes.addReactionRoles(guild_id, reactionRoles);
				return reactionRoles;
			} catch (SQLException e) {
				logger.error("SQLgetReactionRoles Exception", e);
				return reactionRoles;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return roles;
	}
	
	public static int SQLUpdateRoleName(long guild_id, long role_id, String name) {
		logger.trace("SQLUpdateRoleName launched. Passed params {}, {}, {}", guild_id, role_id, name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("DiscordRoles", ip), username, password);
			String sql = ("UPDATE roles SET name = ? WHERE role_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, name);
			stmt.setLong(2, role_id);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateRoleName Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Roles> SQLgetCategories() {
		logger.trace("SQLgetCategories launched. No params passed");
		ArrayList<Roles> roles = new ArrayList<Roles>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("DiscordRoles", ip), username, password);
			String sql = ("SELECT * FROM role_category");
			stmt = myConn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while(rs.next()) {
				roles.add(new Roles(
						rs.getString(1),
						rs.getString(2)
				));
			}
			return roles;
		} catch (SQLException e) {
			logger.error("SQLgetCategories Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteRole(long role_id, long guild_id) {
		logger.trace("SQLDeleteRole launched. Passed params {}, {}", role_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("DiscordRoles", ip), username, password);
			String sql = ("DELETE FROM roles WHERE role_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
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
	
	public static int SQLUpdateRole(long role_id, long guild_id) {
		logger.trace("SQLUpdateRole launched. Passed params {}, {}", role_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("DiscordRoles", ip), username, password);
			String sql = ("UPDATE roles SET level = 0, persistant = 0, fk_category_abv = 'def' WHERE role_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, role_id);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateRole Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertReaction(long message_id, String emoji, long role_id) {
		logger.trace("SQLInsertReaction launched. Passed params {}, {}, {}", message_id, emoji, role_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("DiscordRoles", ip), username, password);
			String sql = ("INSERT INTO reactions (message_id, emoji, role_id) VALUES(?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, message_id);
			stmt.setString(2, emoji);
			stmt.setLong(3, role_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertReaction Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static long SQLgetReactionRole(long message_id, String emoji) {
		logger.trace("SQLgetReactionRole launched. Passed params {}", message_id, emoji);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("DiscordRoles", ip), username, password);
			String sql = ("SELECT role_id FROM reactions WHERE message_id = ? and emoji = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, message_id);
			stmt.setString(2, emoji);
			rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getLong(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetReactionRole Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteReactions(long message_id) {
		logger.trace("SQLDeleteReactions launched. Passed params {}", message_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("DiscordRoles", ip), username, password);
			String sql = ("DELETE FROM reactions WHERE message_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, message_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteReactions Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
}
