package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Clan;
import constructors.ClanMember;
import constructors.ClanReservation;
import constructors.CompMap;
import constructors.Member;
import constructors.Room;
import constructors.UserStats;
import fileManagement.IniFileReader;
import util.STATIC;

public class Competitive {
	private static final Logger logger = LoggerFactory.getLogger(Competitive.class);
	
	private static String ip = IniFileReader.getSQLIP();
	private static String username = IniFileReader.getSQLUsername();
	private static String password = IniFileReader.getSQLPassword();
	
	
	public static void SQLconnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			logger.error("JDBC driver couldn't be loaded", e);
		}
	}
	
	//comp_servers
	public static int SQLInsertCompServer(long _guild_id, String _server) {
		logger.info("SQLInsertCompServer launched. Passed params {}, {}", _guild_id, _server);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT IGNORE INTO comp_servers (fk_guild_id, server) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setString(2, _server);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertCompServer Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLRemoveCompServer(long _guild_id, String _server) {
		logger.info("SQLRemoveCompServer launched. Passed params {}, {}", _guild_id, _server);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM comp_servers WHERE fk_guild_id = ? AND server = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setString(2, _server);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLRemoveCompServer Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetCompServers(long _guild_id) {
		logger.info("SQLgetCompServers launched. Passed params {}, {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<String> servers = new ArrayList<String>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT server FROM comp_servers WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				servers.add(rs.getString(1));
			}
			return servers;
		} catch (SQLException e) {
			logger.error("SQLgetCompServers Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//user_stats
	public static int SQLUserStatExists(long _guild_id, long _user_id) {
		logger.info("SQLUserStatExists launched. Passed params {}, {}", _guild_id, _user_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM user_stats WHERE fk_guild_id = ? AND fk_user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setLong(2, _user_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return 1;
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLUserStatExists Exception", e);
			return -1;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static String SQLgetServerFromUserStat(long _guild_id, long _user_id) {
		logger.info("SQLUserStatExists launched. Passed params {}, {}", _guild_id, _user_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT server FROM user_stats WHERE fk_guild_id = ? AND fk_user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setLong(2, _user_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getString(1);
			}
			return null;
		} catch (SQLException e) {
			logger.error("SQLUserStatExists Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static UserStats SQLgetUserStats(long _guild_id, long _user_id) {
		logger.info("SQLUserStatExists launched. Passed params {}, {}", _guild_id, _user_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM user_stats WHERE fk_guild_id = ? AND fk_user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setLong(2, _user_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return new UserStats(
					rs.getLong(2),
					rs.getString(3),
					rs.getInt(4),
					rs.getInt(5),
					rs.getInt(6),
					rs.getInt(7),
					rs.getString(8),
					rs.getInt(9)
				);
			}
			return new UserStats();
		} catch (SQLException e) {
			logger.error("SQLUserStatExists Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetRanking(long _guild_id) {
		logger.info("SQLgetRanking launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<String> rankList = new ArrayList<String>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT `fk_user_id`, @curRank := @curRank + 1 AS Rank, elo FROM `user_stats`, (SELECT @curRank := 0) r WHERE fk_guild_id = ? ORDER BY `elo` DESC");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				rankList.add(rs.getString(1)+"-"+rs.getString(2)+"-"+rs.getString(3));
			}
			return rankList;
		} catch (SQLException e) {
			logger.error("SQLRanking Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
			try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetRankingTop10(long _guild_id) {
		logger.info("SQLgetRankingTop10 launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<String> rankList = new ArrayList<String>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT name, elo FROM user_stats WHERE fk_guild_id = ? ORDER BY elo DESC LIMIT 10");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				rankList.add(rs.getString(1)+"-"+rs.getString(2));
			}
			return rankList;
		} catch (SQLException e) {
			logger.error("SQLgetRankingTop10 Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
			try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLisNameTaken(long _guild_id, String _name) {
		logger.info("SQLisNameTaken launched. Passed params {}, {}, {}", _guild_id, _name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM user_stats WHERE fk_guild_id = ? AND name = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setString(2, _name);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return 1;
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLisNameTaken Exception", e);
			return -1;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertUserStat(long _guild_id, long _user_id, String _name) {
		logger.info("SQLInsertUserStat launched. Passed params {}, {}, {}", _guild_id, _user_id, _name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO user_stats (fk_guild_id, fk_user_id, name) VALUES(?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setLong(2, _user_id);
			stmt.setString(3, _name);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertUserStat Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateServerFromUserStats(long _guild_id, String _server) {
		logger.info("SQLUpdateServerFromUserStats launched. Passed params {}, {}", _guild_id, _server);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE user_stats SET server = NULL WHERE fk_guild_id = ? AND server = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setString(2, _server);
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateServerFromUserStats Exception", e);
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateSelectedServerInUserStats(long _guild_id, long _user_id, String _server) {
		logger.info("SQLUpdateSelectedServerInUserStats launched. Passed params {}, {}, {}", _guild_id, _user_id, _server);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE user_stats SET server = ? WHERE fk_guild_id = ? AND fk_user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _server);
			stmt.setLong(2, _guild_id);
			stmt.setLong(3, _user_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateSelectedServerInUserStats Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateNameInUserStats(long _guild_id, long _user_id, String _newName) {
		logger.info("SQLUpdateNameInUserStats launched. Passed params {}, {}, {}", _guild_id, _user_id, _newName);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE user_stats SET name = ? WHERE fk_guild_id = ? AND fk_user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _newName);
			stmt.setLong(2, _guild_id);
			stmt.setLong(3, _user_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateNameInUserStats Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static String SQLgetUsernameFromUserStats(long _guild_id, long _user_id) {
		logger.info("SQLgetUsernameFromUserStats launched. Passed params {}, {}", _guild_id, _user_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT name FROM user_stats WHERE fk_guild_id = ? AND fk_user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setLong(2, _user_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getString(1);
			}
			return null;
		} catch (SQLException e) {
			logger.error("SQLgetUsernameFromUserStats Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//guild
	public static int SQLgetMaxClanMembers(long _guild_id) {
		logger.info("SQLgetMaxClanMembers launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT clan_members FROM guild WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetMaxClanMembers Exception", e);
			return -1;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateMaxClanMembers(long _guild_id, int _members) {
		logger.info("SQLUpdateMaxClanMembers launched. Passed params {}, {}", _guild_id, _members);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE guild SET clan_members = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _members);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateMaxClanMembers Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetMatchmakingMembers(long _guild_id) {
		logger.info("SQLgetMatchmakingMembers launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT matchmaking_members FROM guild WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetMatchmakingMembers Exception", e);
			return -1;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateMatchmakingMembers(long _guild_id, int _members) {
		logger.info("SQLUpdateMatchmakingMembers launched. Passed params {}, {}", _guild_id, _members);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE guild SET matchmaking_members = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _members);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateMatchmakingMembers Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//clan_members
	public static int SQLgetClanMemberLevel(long _user_id, long _guild_id) {
		logger.info("SQLgetClanMemberLevel launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT member_level from clan_members WHERE fk_user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetClanMemberLevel Exception", e);
			return -1;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateClanMemberLevel(long _guild_id, long _user_id, int _clan_id, int _level) {
		logger.info("SQLUpdateClanMemberLevel launched. Passed params {}, {}, {}, {}", _guild_id, _user_id, _clan_id, _level);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE clan_members SET member_level = ? WHERE fk_guild_id = ? AND fk_user_id = ? AND fk_clan_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _level);
			stmt.setLong(2, _guild_id);
			stmt.setLong(3, _user_id);
			stmt.setInt(4, _clan_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateClanMemberLevel Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Long> SQLgetClanManagement(long _guild_id, int _clan_id) {
		logger.info("SQLgetClanManagement launched. Passed params {}, {}", _guild_id, _clan_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<Long> management = new ArrayList<Long>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT fk_user_id from clan_members WHERE fk_guild_id = ? AND fk_clan_id = ? AND (member_level = 2 OR member_level = 3)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _clan_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				management.add(rs.getLong(1));
			}
			return management;
		} catch (SQLException e) {
			logger.error("SQLgetClanManagement Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLInsertClanMember(long _guild_id, long _user_id, int _clan_id) {
		logger.info("SQLInsertClanMember launched. Passed params {}, {}", _guild_id, _user_id, _clan_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			String sql = ("INSERT INTO clan_members (fk_guild_id, fk_user_id, fk_clan_id) VALUES(?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setLong(2, _user_id);
			stmt.setInt(3, _clan_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				String sql2 = ("UPDATE clans SET members = (members + 1) WHERE fk_guild_id = ? AND clan_id = ?");
				stmt = myConn.prepareStatement(sql2);
				stmt.setLong(1, _guild_id);
				stmt.setInt(2, _clan_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0) {
				String sql3 = ("UPDATE user_stats SET fk_clan_id = ? WHERE fk_guild_id = ? AND fk_user_id = ?");
				stmt = myConn.prepareStatement(sql3);
				stmt.setInt(1, _clan_id);
				stmt.setLong(2, _guild_id);
				stmt.setLong(3, _user_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0)
				myConn.commit();
			else
				myConn.rollback();
			return result;
		} catch (SQLException e) {
			logger.error("SQLInsertClanMember Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLRemoveClanMember(long _guild_id, long _user_id, int _clan_id) {
		logger.info("SQLRemoveClanMember launched. Passed params {}, {}, {}", _guild_id, _user_id, _clan_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			String sql = ("DELETE FROM clan_members WHERE fk_guild_id = ? AND fk_user_id = ? AND fk_clan_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setLong(2, _user_id);
			stmt.setInt(3, _clan_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				String sql2 = ("UPDATE clans SET members = (members - 1) WHERE fk_guild_id = ? AND clan_id = ?");
				stmt = myConn.prepareStatement(sql2);
				stmt.setLong(1, _guild_id);
				stmt.setInt(2, _clan_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0) {
				String sql3 = ("UPDATE user_stats SET fk_clan_id = NULL WHERE fk_guild_id = ? AND fk_user_id = ?");
				stmt = myConn.prepareStatement(sql3);
				stmt.setLong(1, _guild_id);
				stmt.setLong(2, _user_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0)
				myConn.commit();
			else
				myConn.rollback();
			return result;
		} catch (SQLException e) {
			logger.error("SQLRemoveClanMember Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLDelegateOwnership(long _guild_id, long _user_id_target, long _user_id_self, int _clan_id) {
		logger.info("SQLDelegateOwnership launched. Passed params {}, {}, {}, {}", _guild_id, _user_id_target, _user_id_self, _clan_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			String sql = ("UPDATE clan_members SET member_level = 3 WHERE fk_guild_id = ? AND fk_user_id = ? AND fk_clan_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setLong(2, _user_id_target);
			stmt.setInt(3, _clan_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				String sql2 = ("UPDATE clan_members SET member_level = 1 WHERE fk_guild_id = ? AND fk_user_id = ? AND fk_clan_id = ?");
				stmt = myConn.prepareStatement(sql2);
				stmt.setLong(1, _guild_id);
				stmt.setLong(2, _user_id_self);
				stmt.setInt(3, _clan_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0)
				myConn.commit();
			else
				myConn.rollback();
			return result;
		} catch (SQLException e) {
			logger.error("SQLDelegateOwnership Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLDisbandClan(long _guild_id, long _user_id, int _clan_id) {
		logger.info("SQLDisbandClan launched. Passed params {}, {}, {}", _guild_id, _user_id, _clan_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			String sql = ("DELETE FROM clan_members WHERE fk_guild_id = ? AND fk_user_id = ? AND fk_clan_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setLong(2, _user_id);
			stmt.setInt(3, _clan_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				String sql2 = ("DELETE FROM clans WHERE fk_guild_id = ? AND clan_id = ?");
				stmt = myConn.prepareStatement(sql2);
				stmt.setLong(1, _guild_id);
				stmt.setInt(2, _clan_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0) {
				String sql3 = ("UPDATE user_stats SET fk_clan_id = NULL WHERE fk_guild_id = ? AND fk_user_id = ?");
				stmt = myConn.prepareStatement(sql3);
				stmt.setLong(1, _guild_id);
				stmt.setLong(2, _user_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0)
				myConn.commit();
			else
				myConn.rollback();
			return result;
		} catch (SQLException e) {
			logger.error("SQLDisbandClan Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//clans
	@SuppressWarnings("resource")
	public static int SQLCreateClan(long _guild_id, long _user_id, String _name) {
		logger.info("SQLCreateClan launched. Passed params {}, {}, {}", _guild_id, _user_id, _name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			String sql = ("INSERT INTO clans (fk_guild_id, name, members, matches, wins, losses) VALUES(?, ?, 1, 0, 0, 0)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setString(2, _name);
			int result = stmt.executeUpdate();
			
			String sql2 = ("SELECT clan_id FROM clans WHERE fk_guild_id = ? AND name = ?");
			stmt = myConn.prepareStatement(sql2);
			stmt.setLong(1, _guild_id);
			stmt.setString(2, _name);
			ResultSet rs = stmt.executeQuery();
			int clan_id = 0;
			if(rs.next()) {
				clan_id = rs.getInt(1);
			}
			
			if(result > 0) {
				String sql3 = ("INSERT INTO clan_members (fk_clan_id, fk_guild_id, fk_user_id, member_level) VALUES(?, ?, ?, 3)");
				stmt = myConn.prepareStatement(sql3);
				stmt.setInt(1, clan_id);
				stmt.setLong(2, _guild_id);
				stmt.setLong(3, _user_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0) {
				String sql4 = ("UPDATE user_stats SET fk_clan_id = ? WHERE fk_guild_id = ? AND fk_user_id = ?");
				stmt = myConn.prepareStatement(sql4);
				stmt.setInt(1, clan_id);
				stmt.setLong(2, _guild_id);
				stmt.setLong(3, _user_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0)
				myConn.commit();
			else
				myConn.rollback();
			return result;
		} catch (SQLException e) {
			logger.error("SQLCreateClan Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Clan> SQLgetClans(long _guild_id) {
		logger.info("SQLgetClans launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<Clan> clans = new ArrayList<Clan>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM clans WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				clans.add(new Clan(
						rs.getInt(1),
						rs.getString(3),
						rs.getInt(4),
						rs.getInt(5),
						rs.getInt(6),
						rs.getInt(7),
						rs.getString(8),
						rs.getTimestamp(9)
				));
			}
			return clans;
		} catch (SQLException e) {
			logger.error("SQLgetClans Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetClanID(long _guild_id, String _name) {
		logger.info("SQLgetClanID launched. Passed params {}, {}", _guild_id, _name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT clan_id FROM clans WHERE fk_guild_id = ? AND name = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setString(2, _name);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetClanID Exception", e);
			return -1;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetClanMemberNumber(long _guild_id, int _clan_id) {
		logger.info("SQLgetClanMemberNumber launched. Passed params {}, {}", _guild_id, _clan_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT members FROM clans WHERE fk_guild_id = ? AND clan_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _clan_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetClanMemberNumber Exception", e);
			return -1;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static String SQLgetClanName(long _guild_id, int _clan_id) {
		logger.info("SQLgetClanName launched. Passed params {}, {}", _guild_id, _clan_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT name FROM clans WHERE fk_guild_id = ? AND clan_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _clan_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getString(1);
			}
			return null;
		} catch (SQLException e) {
			logger.error("SQLgetClanName Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateClanMark(long _guild_id, int _clan_id, String _url) {
		logger.info("SQLUpdateClanMark launched. Passed params {}, {}, {}", _guild_id, _clan_id, _url);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE clans SET clan_mark = ? WHERE fk_guild_id = ? AND clan_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _url);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _clan_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateClanMark Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//clan_reservations
	public static int SQLInsertClanReservation(long _guild_id, long _user_id, int _clan_id, int _type, long _channel_id) {
		logger.info("SQLInsertClanReservation launched. Passed params {}, {}, {}, {}, {}", _guild_id, _user_id, _clan_id, _type, _channel_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO clan_reservations (fk_guild_id, fk_user_id, fk_clan_id, type, channel_id) VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE type=VALUES(type), done=VALUES(done), action=0, channel_id=VALUES(channel_id)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setLong(2, _user_id);
			stmt.setInt(3, _clan_id);
			stmt.setInt(4, _type);
			stmt.setLong(5, _channel_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertClanReservation Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetClanReservationType(long _guild_id, long _user_id, int _clan_id, boolean _done) {
		logger.info("SQLgetClanReservationType launched. Passed params {}, {}, {}, {}", _guild_id, _user_id, _clan_id, _done);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT type FROM clan_reservations WHERE fk_guild_id = ? AND fk_user_id = ? AND fk_clan_id = ? AND done = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setLong(2, _user_id);
			stmt.setInt(3, _clan_id);
			stmt.setBoolean(4, _done);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetClanReservationType Exception", e);
			return -1;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetClanReservationAction(long _guild_id, long _user_id, int _clan_id, boolean _done) {
		logger.info("SQLgetClanReservationType launched. Passed params {}, {}, {}, {}", _guild_id, _user_id, _clan_id, _done);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT action FROM clan_reservations WHERE fk_guild_id = ? AND fk_user_id = ? AND fk_clan_id = ? AND done = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setLong(2, _user_id);
			stmt.setInt(3, _clan_id);
			stmt.setBoolean(4, _done);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetClanReservationAction Exception", e);
			return -1;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ClanReservation SQLgetClanReservation(long _guild_id, int _clan_id, int _type, boolean _done) {
		logger.info("SQLgetClanReservation launched. Passed params {}, {}, {}, {}", _guild_id, _clan_id, _type, _done);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM clan_reservations WHERE fk_guild_id = ? AND fk_clan_id = ? AND type = ? AND done = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _clan_id);
			stmt.setInt(3, _type);
			stmt.setBoolean(4, _done);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return new ClanReservation(
						rs.getLong(1),
						rs.getLong(2),
						rs.getInt(3),
						rs.getInt(4),
						rs.getBoolean(5),
						rs.getInt(6),
						rs.getLong(7),
						rs.getTimestamp(8)
				);
			}
			return new ClanReservation();
		} catch (SQLException e) {
			logger.error("SQLgetClanReservation Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ClanReservation SQLgetClanReservation(long _guild_id, long _user_id, int _clan_id) {
		logger.info("SQLgetClanReservation launched. Passed params {}, {}, {}, {}, {}", _guild_id, _user_id, _clan_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM clan_reservations WHERE fk_guild_id = ? AND fk_user_id = ? AND fk_clan_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setLong(2, _user_id);
			stmt.setInt(3, _clan_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return new ClanReservation(
						rs.getLong(1),
						rs.getLong(2),
						rs.getInt(3),
						rs.getInt(4),
						rs.getBoolean(5),
						rs.getInt(6),
						rs.getLong(7),
						rs.getTimestamp(8)
				);
			}
			return new ClanReservation();
		} catch (SQLException e) {
			logger.error("SQLgetClanReservation Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateClanReservationAction(long _guild_id, long _user_id, int _clan_id, int _type, int _action) {
		logger.info("SQLInsertClanReservation launched. Passed params {}, {}, {}, {}, {}", _guild_id, _user_id, _clan_id, _type, _action);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE clan_reservations SET done = 1, action = ? WHERE fk_guild_id = ? AND fk_user_id = ? AND fk_clan_id = ? AND type = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _action);
			stmt.setLong(2, _guild_id);
			stmt.setLong(3, _user_id);
			stmt.setInt(4, _clan_id);
			stmt.setInt(5, _type);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertClanReservation Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//matchmaking_rooms
	public static Room SQLgetMatchmakingRoom(long _guild_id, int _type, int _status) {
		logger.info("SQLgetMatchmakingRoom launched. Passed params {}, {}, {}", _guild_id, _type, _status);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM matchmaking_rooms WHERE fk_guild_id = ? AND type = ? AND status = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _type);
			stmt.setInt(3, _status);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return new Room(
						rs.getInt(2),
						rs.getInt(3),
						rs.getInt(4),
						rs.getInt(5),
						rs.getInt(6),
						rs.getInt(7),
						rs.getInt(8),
						rs.getInt(9),
						rs.getTimestamp(10),
						rs.getTimestamp(11),
						rs.getLong(12),
						rs.getLong(13)
				);
			}
			return new Room();
		} catch (SQLException e) {
			logger.error("SQLgetMatchmakingRoom Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static Room SQLgetMatchmakingRoom(long _guild_id, int _room_id) {
		logger.info("SQLgetMatchmakingRoom launched. Passed params {}, {}", _guild_id, _room_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM matchmaking_rooms WHERE fk_guild_id = ? AND room_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _room_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return new Room(
						rs.getInt(2),
						rs.getInt(3),
						rs.getInt(4),
						rs.getInt(5),
						rs.getInt(6),
						rs.getInt(7),
						rs.getInt(8),
						rs.getInt(9),
						rs.getTimestamp(10),
						rs.getTimestamp(11),
						rs.getLong(12),
						rs.getLong(13)
				);
			}
			return new Room();
		} catch (SQLException e) {
			logger.error("SQLgetMatchmakingRoom Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static Room SQLgetMatchmakingRoom(long _guild_id, int _clan_id, int _type, int _status) {
		logger.info("SQLgetMatchmakingRoom launched. Passed params {}, {}, {}, {}", _guild_id, _clan_id, _type, _status);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM matchmaking_rooms WHERE fk_guild_id = ? AND (fk_clan_id_1 = ? OR fk_clan_id_2 = ?) AND type = ? AND status = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _clan_id);
			stmt.setInt(3, _clan_id);
			stmt.setInt(4, _type);
			stmt.setInt(5, _status);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return new Room(
						rs.getInt(2),
						rs.getInt(3),
						rs.getInt(4),
						rs.getInt(5),
						rs.getInt(6),
						rs.getInt(7),
						rs.getInt(8),
						rs.getInt(9),
						rs.getTimestamp(10),
						rs.getTimestamp(11),
						rs.getLong(12),
						rs.getLong(13)
				);
			}
			return new Room();
		} catch (SQLException e) {
			logger.error("SQLgetMatchmakingRoom Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Room> SQLgetOngoingMatchmakingRooms(long _guild_id) {
		logger.info("SQLgetOngoingMatchmakingRooms launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<Room> rooms = new ArrayList<Room>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM matchmaking_rooms WHERE fk_guild_id = ? AND (status = 2 OR status = 4)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				rooms.add(new Room(
						rs.getInt(2),
						rs.getInt(3),
						rs.getInt(4),
						rs.getInt(5),
						rs.getInt(6),
						rs.getInt(7),
						rs.getInt(8),
						rs.getInt(9),
						rs.getTimestamp(10),
						rs.getTimestamp(11),
						rs.getLong(12),
						rs.getLong(13)
				));
			}
			return rooms;
		} catch (SQLException e) {
			logger.error("SQLgetOngoingMatchmakingRooms Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLisClanMatchmakingRoomOngoing(long _guild_id, int _type, int _clan_id) {
		logger.info("SQLisClanMatchmakingRoomOngoing launched. Passed params {}, {}, {}", _guild_id, _type, _clan_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM matchmaking_rooms WHERE fk_guild_id = ? AND type = ? AND status < 3 AND (fk_clan_id_1 = ? OR fk_clan_id_2 = ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _type);
			stmt.setInt(3, _clan_id);
			stmt.setInt(4, _clan_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return 1;
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLisClanMatchmakingRoomOngoing Exception", e);
			return -1;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLCreateMatchmakingRoom(long _guild_id, long _user_id, int _type, int _map) {
		logger.info("SQLCreateMatchmakingRoom launched. Passed params {}, {}, {}, {}", _guild_id, _user_id, _type, _map);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			String sql = ("SELECT MAX(room_id) FROM matchmaking_rooms");
			stmt = myConn.prepareStatement(sql);
			rs = stmt.executeQuery();
			int room_id = 0;
			if(rs.next()) {
				room_id = rs.getInt(1);
			}
			room_id++;
			
			String sql2 = ("INSERT INTO matchmaking_rooms (fk_guild_id, room_id, type, members, fk_map_id) VALUES(?, ?, ?, 1, ?)");
			stmt = myConn.prepareStatement(sql2);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, room_id);
			stmt.setInt(3, _type);
			if(_map > 0)
				stmt.setInt(4, _map);
			else
				stmt.setNull(4, Types.INTEGER);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				String sql3 = ("INSERT INTO matchmaking_members (fk_guild_id, fk_user_id, fk_room_id) VALUES(?, ?, ?)");
				stmt = myConn.prepareStatement(sql3);
				stmt.setLong(1, _guild_id);
				stmt.setLong(2, _user_id);
				stmt.setInt(3, room_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0) {
				myConn.commit();
				return room_id;
			}
			else {
				myConn.rollback();
				return 0;
			}
		} catch (SQLException e) {
			logger.error("SQLCreateMatchmakingRoom Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		    try { rs.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLCreateClanMatchmakingRoom(long _guild_id, int _type, int _map, int _clan_id_1, int _clan_id_2) {
		logger.info("SQLCreateMatchmakingRoom launched. Passed params {}, {}, {}, {}, {}", _guild_id, _type, _map, _clan_id_1, _clan_id_2);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			String sql = ("SELECT MAX(room_id) FROM matchmaking_rooms");
			stmt = myConn.prepareStatement(sql);
			rs = stmt.executeQuery();
			int room_id = 0;
			if(rs.next()) {
				room_id = rs.getInt(1);
			}
			room_id++;
			
			String sql2 = ("INSERT INTO matchmaking_rooms (fk_guild_id, room_id, type, members, fk_map_id, status, fk_clan_id_1, fk_clan_id_2) VALUES(?, ?, ?, 0, ?, 1, ?, ?)");
			stmt = myConn.prepareStatement(sql2);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, room_id);
			stmt.setInt(3, _type);
			stmt.setInt(4, _map);
			stmt.setInt(5, _clan_id_1);
			stmt.setInt(6, _clan_id_2);
			if(_map > 0)
				stmt.setInt(4, _map);
			else
				stmt.setNull(4, Types.INTEGER);
			final int result = stmt.executeUpdate();
			
			if(result > 0) {
				myConn.commit();
				return room_id;
			}
			else {
				myConn.rollback();
				return 0;
			}
		} catch (SQLException e) {
			logger.error("SQLCreateMatchmakingRoom Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		    try { rs.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLDeleteMatchmakingRoom(long _guild_id, int _room_id) {
		logger.info("SQLDeleteMatchmakingRoom launched. Passed params {}, {}", _guild_id, _room_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			String sql = ("DELETE FROM matchmaking_members WHERE fk_guild_id = ? AND fk_room_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _room_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				String sql2 = ("DELETE FROM matchmaking_rooms WHERE fk_guild_id = ? AND room_id = ?");
				stmt = myConn.prepareStatement(sql2);
				stmt.setLong(1, _guild_id);
				stmt.setInt(2, _room_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0)
				myConn.commit();
			else
				myConn.rollback();
			return result;
		} catch (SQLException e) {
			logger.error("SQLDeleteMatchmakingRoom Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLJoinRoom(long _guild_id, long _user_id, int _room_id) {
		logger.info("SQLJoinRoom launched. Passed params {}, {}, {}", _guild_id, _user_id, _room_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			String sql = ("INSERT INTO matchmaking_members (fk_guild_id, fk_user_id, fk_room_id) VALUES(?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setLong(2, _user_id);
			stmt.setInt(3, _room_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				String sql2 = ("UPDATE matchmaking_rooms SET members = (members+1) WHERE fk_guild_id = ? AND room_id = ?");
				stmt = myConn.prepareStatement(sql2);
				stmt.setLong(1, _guild_id);
				stmt.setInt(2, _room_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0)
				myConn.commit();
			else
				myConn.rollback();
			return result;
		} catch (SQLException e) {
			logger.error("SQLJoinRoom Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLJoinRoom(long _guild_id, long _user_id, int _room_id, int _team) {
		logger.info("SQLJoinRoom launched. Passed params {}, {}, {}, {}", _guild_id, _user_id, _room_id, _team);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			String sql = ("INSERT INTO matchmaking_members (fk_guild_id, fk_user_id, fk_room_id, team) VALUES(?, ?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setLong(2, _user_id);
			stmt.setInt(3, _room_id);
			stmt.setInt(4, _team);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				String sql2 = ("UPDATE matchmaking_rooms SET members = (members+1) WHERE fk_guild_id = ? AND room_id = ?");
				stmt = myConn.prepareStatement(sql2);
				stmt.setLong(1, _guild_id);
				stmt.setInt(2, _room_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0)
				myConn.commit();
			else
				myConn.rollback();
			return result;
		} catch (SQLException e) {
			logger.error("SQLJoinRoom Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLUpdateTeams(long _guild_id, int _room_id, Member [] _team1, Member [] _team2) {
		logger.info("SQLUpdateTeams launched. Passed params {}, {} and arrays", _guild_id, _room_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			String sql = ("UPDATE matchmaking_rooms SET status = 2 WHERE fk_guild_id = ? AND room_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _room_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				String sql2 = ("UPDATE matchmaking_members SET team = 1 WHERE fk_guild_id = ? AND fk_user_id = ? AND fk_room_id = ?");
				stmt = myConn.prepareStatement(sql2);
				for(int i = 0; i < _team1.length; i++) {
					if(_team1[i] != null) {
						stmt.setLong(1, _guild_id);
						stmt.setLong(2, _team1[i].getUserID());
						stmt.setInt(3, _room_id);
						stmt.addBatch();
					}
				}
				int [] batchResult = stmt.executeBatch();
				if(batchResult[0] <= 0) {
					myConn.rollback();
					return 0;
				}
				
				String sql3 = ("UPDATE matchmaking_members SET team = 2 WHERE fk_guild_id = ? AND fk_user_id = ? AND fk_room_id = ?");
				stmt = myConn.prepareStatement(sql3);
				for(int i = 0; i < _team2.length; i++) {
					if(_team2[i] != null) {
						stmt.setLong(1, _guild_id);
						stmt.setLong(2, _team2[i].getUserID());
						stmt.setInt(3, _room_id);
						stmt.addBatch();
					}
				}
				batchResult = stmt.executeBatch();
				if(batchResult[0] <= 0) {
					myConn.rollback();
					return 0;
				}
			}
			
			myConn.commit();
			return result;
		} catch (SQLException e) {
			logger.error("SQLUpdateTeams Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLUpdateTeams(long _guild_id, int _room_id, long _user_id_1, long _user_id_2) {
		logger.info("SQLUpdateTeams launched. Passed params {}, {}, {}, {}", _guild_id, _room_id, _user_id_1, _user_id_2);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			String sql = ("UPDATE matchmaking_rooms SET status = 2 WHERE fk_guild_id = ? AND room_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _room_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				String sql2 = ("UPDATE matchmaking_members SET team = 1, leader = 1, picker = 1 WHERE fk_guild_id = ? AND fk_user_id = ? AND fk_room_id = ?");
				stmt = myConn.prepareStatement(sql2);
				stmt.setLong(1, _guild_id);
				stmt.setLong(2, _user_id_1);
				stmt.setInt(3, _room_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0) {
				String sql3 = ("UPDATE matchmaking_members SET team = 2, leader = 1 WHERE fk_guild_id = ? AND fk_user_id = ? AND fk_room_id = ?");
				stmt = myConn.prepareStatement(sql3);
				stmt.setLong(1, _guild_id);
				stmt.setLong(2, _user_id_2);
				stmt.setInt(3, _room_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0)
				myConn.commit();
			else
				myConn.rollback();
			return result;
		} catch (SQLException e) {
			logger.error("SQLUpdateTeams Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateRoomMessageID(long _guild_id, int _room_id, long _channel_id, long _message_id) {
		logger.info("SQLUpdateRoomMessageID launched. Passed params {}, {}, {}, {}", _guild_id, _room_id, _channel_id, _message_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE matchmaking_rooms SET channel_id = ?, message_id = ? WHERE fk_guild_id = ? AND room_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			stmt.setLong(2, _message_id);
			stmt.setLong(3, _guild_id);
			stmt.setInt(4, _room_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateRoomMessageID Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateMatchmakingRoomMap(long _guild_id, int _room_id, int _map_id) {
		logger.info("SQLUpdateMatchmakingRoomMap launched. Passed params {}, {}, {}", _guild_id, _room_id, _map_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE matchmaking_rooms SET fk_map_id = ? WHERE fk_guild_id = ? AND room_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _map_id);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _room_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateMatchmakingRoomMap Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateMatchmakingRoomStatus(long _guild_id, int _room_id, int _status) {
		logger.info("SQLUpdateMatchmakingRoomStatus launched. Passed params {}, {}, {}", _guild_id, _room_id, _status);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE matchmaking_rooms SET status = ? WHERE fk_guild_id = ? AND room_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _status);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _room_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateMatchmakingRoomStatus Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLsetWinner(long _guild_id, int _room_id, int _team, boolean _clans) {
		logger.info("SQLsetWinner launched. Passed params {}, {}, {}, {}", _guild_id, _room_id, _team, _clans);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			String sql = ("UPDATE matchmaking_rooms SET winner = ?, status = 3 WHERE fk_guild_id = ? AND room_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _team);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _room_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				String sql2 = ("INSERT INTO comp_elo_log (fk_guild_id, fk_user_id, fk_room_id, elo_before, elo_after) SELECT guild_id, user_id, room_id, elo, (elo+10) FROM matchmaking_view WHERE guild_id = ? AND room_id = ? AND team = ?");
				stmt = myConn.prepareStatement(sql2);
				stmt.setLong(1, _guild_id);
				stmt.setInt(2, _room_id);
				stmt.setInt(3, _team);
				result = stmt.executeUpdate();
			}
			
			if(result > 0) {
				String sql3 = ("INSERT INTO comp_elo_log (fk_guild_id, fk_user_id, fk_room_id, elo_before, elo_after) SELECT guild_id, user_id, room_id, elo, (CASE WHEN (elo-7) > 0 THEN (elo-7) ELSE 0 END) FROM matchmaking_view WHERE guild_id = ? AND room_id = ? AND team != ?");
				stmt = myConn.prepareStatement(sql3);
				stmt.setLong(1, _guild_id);
				stmt.setInt(2, _room_id);
				stmt.setInt(3, _team);
				result = stmt.executeUpdate();
			}
			
			if(result > 0) {
				String sql4 = ("UPDATE user_stats SET games = (games+1), wins = (wins+1), elo = (elo+10) WHERE fk_guild_id = ? AND fk_user_id IN(SELECT fk_user_id FROM matchmaking_members WHERE fk_room_id = ? AND team = ? AND fk_guild_id = ?)");
				stmt = myConn.prepareStatement(sql4);
				stmt.setLong(1, _guild_id);
				stmt.setInt(2, _room_id);
				stmt.setInt(3, _team);
				stmt.setLong(4, _guild_id);
				result = stmt.executeUpdate();
			}
						
			if(result > 0) {
				String sql5 = ("UPDATE user_stats SET games = (games+1), losses = (losses+1), elo = (elo-7) WHERE fk_guild_id = ? AND fk_user_id IN(SELECT fk_user_id FROM matchmaking_members WHERE fk_room_id = ? AND team != ? AND fk_guild_id = ?)");
				stmt = myConn.prepareStatement(sql5);
				stmt.setLong(1, _guild_id);
				stmt.setInt(2, _room_id);
				stmt.setInt(3, _team);
				stmt.setLong(4, _guild_id);
				result = stmt.executeUpdate();
			}
			
			if(_team == 1 && _clans) {
				if(result > 0) {
					String sql6 = ("UPDATE clans SET matches = (matches+1), wins = (wins+1) WHERE fk_guild_id = ? AND clan_id IN(SELECT fk_clan_id_1 FROM matchmaking_rooms WHERE room_id = ? AND fk_guild_id = ?)");
					stmt = myConn.prepareStatement(sql6);
					stmt.setLong(1, _guild_id);
					stmt.setInt(2, _room_id);
					stmt.setLong(3, _guild_id);
					result = stmt.executeUpdate();
				}
				
				if(result > 0) {
					String sql7 = ("UPDATE clans SET matches = (matches+1), losses = (losses+1) WHERE fk_guild_id = ? AND clan_id IN(SELECT fk_clan_id_2 FROM matchmaking_rooms WHERE room_id = ? AND fk_guild_id = ?)");
					stmt = myConn.prepareStatement(sql7);
					stmt.setLong(1, _guild_id);
					stmt.setInt(2, _room_id);
					stmt.setLong(3, _guild_id);
					result = stmt.executeUpdate();
				}
			}
			else if(_team == 2 && _clans) {
				if(result > 0) {
					String sql6 = ("UPDATE clans SET matches = (matches+1), wins = (wins+1) WHERE fk_guild_id = ? AND clan_id IN(SELECT fk_clan_id_2 FROM matchmaking_rooms WHERE room_id = ? AND fk_guild_id = ?)");
					stmt = myConn.prepareStatement(sql6);
					stmt.setLong(1, _guild_id);
					stmt.setInt(2, _room_id);
					stmt.setLong(3, _guild_id);
					result = stmt.executeUpdate();
				}
				
				if(result > 0) {
					String sql7 = ("UPDATE clans SET matches = (matches+1), losses = (losses+1) WHERE fk_guild_id = ? AND clan_id IN(SELECT fk_clan_id_1 FROM matchmaking_rooms WHERE room_id = ? AND fk_guild_id = ?)");
					stmt = myConn.prepareStatement(sql7);
					stmt.setLong(1, _guild_id);
					stmt.setInt(2, _room_id);
					stmt.setLong(3, _guild_id);
					result = stmt.executeUpdate();
				}
			}
			
			String sql6 = ("UPDATE user_stats SET elo = 0 WHERE fk_guild_id = ? AND elo < 0");
			stmt = myConn.prepareStatement(sql6);
			stmt.setLong(1, _guild_id);
			stmt.executeUpdate();
			
			if(result > 0)
				myConn.commit();
			else
				myConn.rollback();
			return result;
		} catch (SQLException e) {
			logger.error("SQLsetWinner Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLrevertWinner(long _guild_id, int _room_id, int _team, boolean _clans) {
		logger.info("SQLrevertWinner launched. Passed params {}, {}, {}, {}", _guild_id, _room_id, _team, _clans);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			String sql = ("UPDATE matchmaking_rooms SET winner = NULL, status = 4 WHERE fk_guild_id = ? AND room_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _room_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				String sql2 = ("UPDATE user_stats a SET a.games = (a.games-1), a.wins = (a.wins-1), a.elo = (a.elo-(SELECT (b.elo_after-b.elo_before) FROM comp_elo_log b WHERE b.fk_guild_id = a.fk_guild_id AND b.fk_user_id = a.fk_user_id AND b.fk_room_id = ?)) WHERE a.fk_guild_id = ? AND a.fk_user_id IN(SELECT c.fk_user_id FROM matchmaking_members c WHERE c.fk_room_id = ? AND c.team = ? AND c.fk_guild_id = ?)");
				stmt = myConn.prepareStatement(sql2);
				stmt.setInt(1, _room_id);
				stmt.setLong(2, _guild_id);
				stmt.setInt(3, _room_id);
				stmt.setInt(4, _team);
				stmt.setLong(5, _guild_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0) {
				String sql3 = ("UPDATE user_stats a SET a.games = (a.games-1), a.losses = (a.losses-1), a.elo = (a.elo-(SELECT (b.elo_after-b.elo_before) FROM comp_elo_log b WHERE b.fk_guild_id = a.fk_guild_id AND b.fk_user_id = a.fk_user_id AND b.fk_room_id = ?)) WHERE a.fk_guild_id = ? AND a.fk_user_id IN(SELECT c.fk_user_id FROM matchmaking_members c WHERE c.fk_room_id = ? AND c.team != ? AND c.fk_guild_id = ?)");
				stmt = myConn.prepareStatement(sql3);
				stmt.setInt(1, _room_id);
				stmt.setLong(2, _guild_id);
				stmt.setInt(3, _room_id);
				stmt.setInt(4, _team);
				stmt.setLong(5, _guild_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0) {
				String sql4 = ("DELETE FROM comp_elo_log WHERE fk_guild_id = ? AND fk_room_id = ?");
				stmt = myConn.prepareStatement(sql4);
				stmt.setLong(1, _guild_id);
				stmt.setInt(2, _room_id);
				result = stmt.executeUpdate();
			}
			
			if(_team == 1 && _clans) {
				if(result > 0) {
					String sql5 = ("UPDATE clans SET matches = (matches-1), wins = (wins-1) WHERE fk_guild_id = ? AND clan_id IN(SELECT fk_clan_id_1 FROM matchmaking_rooms WHERE room_id = ? AND fk_guild_id = ?)");
					stmt = myConn.prepareStatement(sql5);
					stmt.setLong(1, _guild_id);
					stmt.setInt(2, _room_id);
					stmt.setLong(3, _guild_id);
					result = stmt.executeUpdate();
				}
				
				if(result > 0) {
					String sql6 = ("UPDATE clans SET matches = (matches-1), losses = (losses-1) WHERE fk_guild_id = ? AND clan_id IN(SELECT fk_clan_id_2 FROM matchmaking_rooms WHERE room_id = ? AND fk_guild_id = ?)");
					stmt = myConn.prepareStatement(sql6);
					stmt.setLong(1, _guild_id);
					stmt.setInt(2, _room_id);
					stmt.setLong(3, _guild_id);
					result = stmt.executeUpdate();
				}
			}
			else if(_team == 2 && _clans) {
				if(result > 0) {
					String sql5 = ("UPDATE clans SET matches = (matches-1), wins = (wins-1) WHERE fk_guild_id = ? AND clan_id IN(SELECT fk_clan_id_2 FROM matchmaking_rooms WHERE room_id = ? AND fk_guild_id = ?)");
					stmt = myConn.prepareStatement(sql5);
					stmt.setLong(1, _guild_id);
					stmt.setInt(2, _room_id);
					stmt.setLong(3, _guild_id);
					result = stmt.executeUpdate();
				}
				
				if(result > 0) {
					String sql6 = ("UPDATE clans SET matches = (matches-1), losses = (losses-1) WHERE fk_guild_id = ? AND clan_id IN(SELECT fk_clan_id_1 FROM matchmaking_rooms WHERE room_id = ? AND fk_guild_id = ?)");
					stmt = myConn.prepareStatement(sql6);
					stmt.setLong(1, _guild_id);
					stmt.setInt(2, _room_id);
					stmt.setLong(3, _guild_id);
					result = stmt.executeUpdate();
				}
			}
			
			if(result > 0)
				myConn.commit();
			else
				myConn.rollback();
			return result;
		} catch (SQLException e) {
			logger.error("SQLrevertWinner Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//matchmaking_members
	@SuppressWarnings("resource")
	public static int SQLPickMember(long _guild_id, int _room_id, long _user_id, long _leader_id, int _team) {
		logger.info("SQLPickMember launched. Passed params {}, {}, {}, {}, {}", _guild_id, _room_id, _user_id, _leader_id, _team);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			String sql = ("UPDATE matchmaking_members SET team = ? WHERE fk_guild_id = ? AND fk_room_id = ? AND fk_user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _team);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _room_id);
			stmt.setLong(4, _user_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				String sql2 = ("UPDATE matchmaking_members SET picker = 1 WHERE fk_guild_id = ? AND fk_user_id != ? AND fk_room_id = ? AND leader = 1");
				stmt = myConn.prepareStatement(sql2);
				stmt.setLong(1, _guild_id);
				stmt.setLong(2, _leader_id);
				stmt.setInt(3, _room_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0) {
				String sql3 = ("UPDATE matchmaking_members SET picker = 0 WHERE fk_guild_id = ? AND fk_user_id = ? AND fk_room_id = ?");
				stmt = myConn.prepareStatement(sql3);
				stmt.setLong(1, _guild_id);
				stmt.setLong(2, _leader_id);
				stmt.setInt(3, _room_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0)
				myConn.commit();
			else
				myConn.rollback();
			return result;
		} catch (SQLException e) {
			logger.error("SQLPickMember Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//comp_maps
	@SuppressWarnings("resource")
	public static int SQLInsertMap(long _guild_id, String _mapName, String _url) {
		logger.info("SQLInsertMap launched. Passed params {}, {}, {}", _guild_id, _mapName, _url);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			String sql = ("SELECT * FROM comp_maps WHERE fk_guild_id = ? AND name = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setString(2, _mapName);
			rs = stmt.executeQuery();
			boolean exists = false;
			if(rs.next()) {
				exists = true;
			}
			
			int result = 0;
			if(exists) {
				String sql2 = ("UPDATE comp_maps SET name = ?, img = ? WHERE fk_guild_id = ? AND name = ?");
				stmt = myConn.prepareStatement(sql2);
				stmt.setString(1, _mapName);
				if(_url != null)
					stmt.setString(2, _url);
				else
					stmt.setNull(2, Types.VARCHAR);
				stmt.setLong(3, _guild_id);
				stmt.setString(4, _mapName);
				result = stmt.executeUpdate();
			}
			else {
				String sql2 = ("INSERT INTO comp_maps (fk_guild_id, name, img) VALUES(?, ?, ?)");
				stmt = myConn.prepareStatement(sql2);
				stmt.setLong(1, _guild_id);
				stmt.setString(2, _mapName);
				if(_url != null)
					stmt.setString(3, _url);
				else
					stmt.setNull(3, Types.VARCHAR);
				result = stmt.executeUpdate();
			}
			
			if(result > 0)
				myConn.commit();
			else
				myConn.rollback();
			return result;
		} catch (SQLException e) {
			logger.error("SQLInsertMap Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		    try { rs.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static CompMap SQLgetRandomMap(long _guild_id) {
		logger.info("SQLgetRandomMap launched. Passed params {}, {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT map_id, name, img FROM comp_maps WHERE fk_guild_id = ? ORDER BY RAND() limit 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return new CompMap(
					rs.getInt(1),
					rs.getString(2),
					rs.getString(3)
				);
			}
			return new CompMap();
		} catch (SQLException e) {
			logger.error("SQLgetRandomMap Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static CompMap SQLgetMap(int _map_id) {
		logger.info("SQLgetMap launched. Passed params {}", _map_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT map_id, name, img FROM comp_maps WHERE map_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _map_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return new CompMap(
					rs.getInt(1),
					rs.getString(2),
					rs.getString(3)
				);
			}
			return null;
		} catch (SQLException e) {
			logger.error("SQLgetMap Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static CompMap SQLgetMap(long _guild_id, String _map) {
		logger.info("SQLgetMap launched. Passed params {}", _guild_id, _map);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT map_id, name, img FROM comp_maps WHERE fk_guild_id = ? AND name = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setString(2, _map);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return new CompMap(
					rs.getInt(1),
					rs.getString(2),
					rs.getString(3)
				);
			}
			return new CompMap();
		} catch (SQLException e) {
			logger.error("SQLgetMap Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//Joins
	public static ClanMember SQLgetClanDetails(long _user_id, long _guild_id) {
		logger.info("SQLgetClanDetails launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM clan_view WHERE user_id = ? and guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return new ClanMember(
						rs.getLong(1),
						rs.getLong(2),
						rs.getInt(3),
						rs.getString(4),
						rs.getInt(5),
						rs.getInt(6),
						rs.getInt(7),
						rs.getInt(8),
						rs.getString(9),
						rs.getString(10),
						rs.getInt(11),
						rs.getInt(12),
						rs.getTimestamp(13),
						rs.getTimestamp(14)
				);
			}
			return new ClanMember();
		} catch (SQLException e) {
			logger.error("SQLgetClanDetails Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ClanMember SQLgetClanDetailsByName(String _name, long _guild_id) {
		logger.info("SQLgetClanDetailsByName launched. Passed params {}, {}", _name, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM clan_view WHERE username = ? and guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _name);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return new ClanMember(
						rs.getLong(1),
						rs.getLong(2),
						rs.getInt(3),
						rs.getString(4),
						rs.getInt(5),
						rs.getInt(6),
						rs.getInt(7),
						rs.getInt(8),
						rs.getString(9),
						rs.getString(10),
						rs.getInt(11),
						rs.getInt(12),
						rs.getTimestamp(13),
						rs.getTimestamp(14)
				);
			}
			return new ClanMember();
		} catch (SQLException e) {
			logger.error("SQLgetClanDetailsByName Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<ClanMember> SQLgetClanMembers(long _guild_id, int _clan_id) {
		logger.info("SQLgetClanMembers launched. Passed params {}, {}", _guild_id, _clan_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<ClanMember> clanMembers = new ArrayList<ClanMember>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM clan_view WHERE guild_id = ? AND clan_id = ? ORDER BY member_level desc, join_date asc");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _clan_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				clanMembers.add(new ClanMember(
						rs.getLong(1),
						rs.getLong(2),
						rs.getInt(3),
						rs.getString(4),
						rs.getInt(5),
						rs.getInt(6),
						rs.getInt(7),
						rs.getInt(8),
						rs.getString(9),
						rs.getString(10),
						rs.getInt(11),
						rs.getInt(12),
						rs.getTimestamp(13),
						rs.getTimestamp(14)
				));
			}
			return clanMembers;
		} catch (SQLException e) {
			logger.error("SQLgetClanMembers Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<ClanMember> SQLgetClanMembersStaff(long _guild_id, int _clan_id) {
		logger.info("SQLgetClanMembersStaff launched. Passed params {}, {}", _guild_id, _clan_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<ClanMember> clanMembers = new ArrayList<ClanMember>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM clan_view WHERE guild_id = ? AND clan_id = ? AND member_level > 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _clan_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				clanMembers.add(new ClanMember(
						rs.getLong(1),
						rs.getLong(2),
						rs.getInt(3),
						rs.getString(4),
						rs.getInt(5),
						rs.getInt(6),
						rs.getInt(7),
						rs.getInt(8),
						rs.getString(9),
						rs.getString(10),
						rs.getInt(11),
						rs.getInt(12),
						rs.getTimestamp(13),
						rs.getTimestamp(14)
				));
			}
			return clanMembers;
		} catch (SQLException e) {
			logger.error("SQLgetClanMembersStaff Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLisUserInRoom(long _guild_id, long _user_id) {
		logger.info("SQLisUserInRoom launched. Passed params {}, {}", _guild_id, _user_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT room_id FROM matchmaking_view WHERE guild_id = ? AND user_id = ? AND (status = 1 OR status = 2)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setLong(2, _user_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLisUserInRoom Exception", e);
			return -1;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLLeaveRoom(long _guild_id, long _user_id, int _room_id) {
		logger.info("SQLLeaveRoom launched. Passed params {}, {}, {}", _guild_id, _user_id, _room_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			String sql = ("DELETE FROM matchmaking_members WHERE fk_guild_id = ? AND fk_user_id = ? AND fk_room_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setLong(2, _user_id);
			stmt.setInt(3, _room_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				String sql3 = ("UPDATE matchmaking_rooms SET members = (members-1) WHERE fk_guild_id = ? AND room_id = ?");
				stmt = myConn.prepareStatement(sql3);
				stmt.setLong(1, _guild_id);
				stmt.setInt(2, _room_id);
				result = stmt.executeUpdate();
			}
			if(result > 0)
				myConn.commit();
			else
				myConn.rollback();
			return result;
		} catch (SQLException e) {
			logger.error("SQLLeaveRoom Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Member> SQLgetMatchmakingMembers(long _guild_id, int _room_id) {
		logger.info("SQLgetMatchmakingMembers launched. Passed params {}, {}", _guild_id, _room_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<Member> members = new ArrayList<Member>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT user_id, room_id, username, server, elo, team, leader, picker FROM matchmaking_view WHERE guild_id = ? AND room_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _room_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				members.add(new Member(
					rs.getLong(1),
					rs.getInt(2),
					rs.getString(3),
					rs.getString(4),
					rs.getInt(5),
					rs.getInt(6),
					rs.getBoolean(7),
					rs.getBoolean(8)
				));
			}
			return members;
		} catch (SQLException e) {
			logger.error("SQLgetMatchmakingMembers Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static Member SQLRetrievePicker(long _guild_id, int _room_id, int _status) {
		logger.info("SQLRetrievePicker launched. Passed params {}, {}, {}", _guild_id, _room_id, _status);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT user_id, room_id, username, server, elo, team, leader, picker FROM matchmaking_view WHERE guild_id = ? AND room_id = ? AND status = ? AND leader = 1 AND picker = 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _room_id);
			stmt.setInt(3, _status);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return new Member(
					rs.getLong(1),
					rs.getInt(2),
					rs.getString(3),
					rs.getString(4),
					rs.getInt(5),
					rs.getInt(6),
					rs.getBoolean(7),
					rs.getBoolean(8)
				);
			}
			return null;
		} catch (SQLException e) {
			logger.error("SQLRetrievePicker Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static Member SQLRetrieveMember(long _guild_id, int _room_id, int _status, long _user_id) {
		logger.info("SQLRetrieveMember launched. Passed params {}, {}, {}, {}, {}", _guild_id, _room_id, _status, _user_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT user_id, room_id, username, server, elo, team, leader, picker FROM matchmaking_view WHERE guild_id = ? AND room_id = ? AND status = ? AND user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _room_id);
			stmt.setInt(3, _status);
			stmt.setLong(4, _user_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return new Member(
					rs.getLong(1),
					rs.getInt(2),
					rs.getString(3),
					rs.getString(4),
					rs.getInt(5),
					rs.getInt(6),
					rs.getBoolean(7),
					rs.getBoolean(8)
				);
			}
			return null;
		} catch (SQLException e) {
			logger.error("SQLRetrieveMember Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static Member SQLRetrieveMember(long _guild_id, int _room_id, int _status, String _username) {
		logger.info("SQLRetrieveMember launched. Passed params {}, {}, {}, {}, {}", _guild_id, _room_id, _status, _username);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT user_id, room_id, username, server, elo, team, leader, picker FROM matchmaking_view WHERE guild_id = ? AND room_id = ? AND status = ? AND username = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _room_id);
			stmt.setInt(3, _status);
			stmt.setString(4, _username);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return new Member(
					rs.getLong(1),
					rs.getInt(2),
					rs.getString(3),
					rs.getString(4),
					rs.getInt(5),
					rs.getInt(6),
					rs.getBoolean(7),
					rs.getBoolean(8)
				);
			}
			return null;
		} catch (SQLException e) {
			logger.error("SQLRetrieveMember Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
}
