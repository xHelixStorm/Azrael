package de.azrael.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Clan;
import de.azrael.constructors.ClanMember;
import de.azrael.constructors.ClanReservation;
import de.azrael.constructors.CompMap;
import de.azrael.constructors.Member;
import de.azrael.constructors.Room;
import de.azrael.constructors.UserStats;
import de.azrael.fileManagement.IniFileReader;
import de.azrael.util.STATIC;

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
	public static int SQLInsertCompServer(long guild_id, String server) {
		logger.trace("SQLInsertCompServer launched. Passed params {}, {}", guild_id, server);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLInsertCompServer);
			stmt.setLong(1, guild_id);
			stmt.setString(2, server);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertCompServer Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLRemoveCompServer(long guild_id, String server) {
		logger.trace("SQLRemoveCompServer launched. Passed params {}, {}", guild_id, server);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLRemoveCompServer);
			stmt.setLong(1, guild_id);
			stmt.setString(2, server);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLRemoveCompServer Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetCompServers(long guild_id) {
		logger.trace("SQLgetCompServers launched. Passed params {}, {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<String> servers = new ArrayList<String>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetCompServers);
			stmt.setLong(1, guild_id);
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
	public static int SQLUserStatExists(long guild_id, long user_id) {
		logger.trace("SQLUserStatExists launched. Passed params {}, {}", guild_id, user_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLUserStatExists);
			stmt.setLong(1, guild_id);
			stmt.setLong(2, user_id);
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
	
	public static String SQLgetServerFromUserStat(long guild_id, long user_id) {
		logger.trace("SQLUserStatExists launched. Passed params {}, {}", guild_id, user_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetServerFromUserStat);
			stmt.setLong(1, guild_id);
			stmt.setLong(2, user_id);
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
	
	public static UserStats SQLgetUserStats(long guild_id, long user_id) {
		logger.trace("SQLUserStatExists launched. Passed params {}, {}", guild_id, user_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetUserStats);
			stmt.setLong(1, guild_id);
			stmt.setLong(2, user_id);
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
	
	public static ArrayList<String> SQLgetRanking(long guild_id) {
		logger.trace("SQLgetRanking launched. Passed params {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<String> rankList = new ArrayList<String>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetRanking);
			stmt.setLong(1, guild_id);
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
	
	public static ArrayList<String> SQLgetRankingTop10(long guild_id) {
		logger.trace("SQLgetRankingTop10 launched. Passed params {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<String> rankList = new ArrayList<String>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetRankingTop10);
			stmt.setLong(1, guild_id);
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
	
	public static int SQLisNameTaken(long guild_id, String name) {
		logger.trace("SQLisNameTaken launched. Passed params {}, {}, {}", guild_id, name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLisNameTaken);
			stmt.setLong(1, guild_id);
			stmt.setString(2, name);
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
	
	public static int SQLInsertUserStat(long guild_id, long user_id, String name) {
		logger.trace("SQLInsertUserStat launched. Passed params {}, {}, {}", guild_id, user_id, name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLInsertUserStat);
			stmt.setLong(1, guild_id);
			stmt.setLong(2, user_id);
			stmt.setString(3, name);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertUserStat Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateServerFromUserStats(long guild_id, String server) {
		logger.trace("SQLUpdateServerFromUserStats launched. Passed params {}, {}", guild_id, server);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLUpdateServerFromUserStats);
			stmt.setLong(1, guild_id);
			stmt.setString(2, server);
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateServerFromUserStats Exception", e);
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateSelectedServerInUserStats(long guild_id, long user_id, String server) {
		logger.trace("SQLUpdateSelectedServerInUserStats launched. Passed params {}, {}, {}", guild_id, user_id, server);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLUpdateSelectedServerInUserStats);
			stmt.setString(1, server);
			stmt.setLong(2, guild_id);
			stmt.setLong(3, user_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateSelectedServerInUserStats Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateNameInUserStats(long guild_id, long user_id, String newName) {
		logger.trace("SQLUpdateNameInUserStats launched. Passed params {}, {}, {}", guild_id, user_id, newName);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLUpdateNameInUserStats);
			stmt.setString(1, newName);
			stmt.setLong(2, guild_id);
			stmt.setLong(3, user_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateNameInUserStats Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static String SQLgetUsernameFromUserStats(long guild_id, long user_id) {
		logger.trace("SQLgetUsernameFromUserStats launched. Passed params {}, {}", guild_id, user_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetUsernameFromUserStats);
			stmt.setLong(1, guild_id);
			stmt.setLong(2, user_id);
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
	public static int SQLgetMaxClanMembers(long guild_id) {
		logger.trace("SQLgetMaxClanMembers launched. Passed params {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetMaxClanMembers);
			stmt.setLong(1, guild_id);
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
	
	public static int SQLUpdateMaxClanMembers(long guild_id, int members) {
		logger.trace("SQLUpdateMaxClanMembers launched. Passed params {}, {}", guild_id, members);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLUpdateMaxClanMembers);
			stmt.setInt(1, members);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateMaxClanMembers Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetMatchmakingMembers(long guild_id) {
		logger.trace("SQLgetMatchmakingMembers launched. Passed params {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetMatchmakingMembers);
			stmt.setLong(1, guild_id);
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
	
	public static ArrayList<Member> SQLgetMatchmakingMembers(long guild_id, int room_id) {
		logger.trace("SQLgetMatchmakingMembers launched. Passed params {}, {}", guild_id, room_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<Member> members = new ArrayList<Member>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetMatchmakingMembers2);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, room_id);
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
					rs.getBoolean(8),
					rs.getBoolean(9)
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
	
	public static int SQLUpdateMatchmakingMembers(long guild_id, int members) {
		logger.trace("SQLUpdateMatchmakingMembers launched. Passed params {}, {}", guild_id, members);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLUpdateMatchmakingMembers);
			stmt.setInt(1, members);
			stmt.setLong(2, guild_id);
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
	public static int SQLgetClanMemberLevel(long user_id, long guild_id) {
		logger.trace("SQLgetClanMemberLevel launched. Passed params {}, {}", user_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetClanMemberLevel);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
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
	
	public static int SQLUpdateClanMemberLevel(long guild_id, long user_id, int clan_id, int level) {
		logger.trace("SQLUpdateClanMemberLevel launched. Passed params {}, {}, {}, {}", guild_id, user_id, clan_id, level);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLUpdateClanMemberLevel);
			stmt.setInt(1, level);
			stmt.setLong(2, guild_id);
			stmt.setLong(3, user_id);
			stmt.setInt(4, clan_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateClanMemberLevel Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Long> SQLgetClanManagement(long guild_id, int clan_id) {
		logger.trace("SQLgetClanManagement launched. Passed params {}, {}", guild_id, clan_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<Long> management = new ArrayList<Long>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetClanManagement);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, clan_id);
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
	public static int SQLInsertClanMember(long guild_id, long user_id, int clan_id) {
		logger.trace("SQLInsertClanMember launched. Passed params {}, {}", guild_id, user_id, clan_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLInsertClanMember);
			stmt.setLong(1, guild_id);
			stmt.setLong(2, user_id);
			stmt.setInt(3, clan_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLInsertClanMember2);
				stmt.setLong(1, guild_id);
				stmt.setInt(2, clan_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLInsertClanMember3);
				stmt.setInt(1, clan_id);
				stmt.setLong(2, guild_id);
				stmt.setLong(3, user_id);
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
	public static int SQLRemoveClanMember(long guild_id, long user_id, int clan_id) {
		logger.trace("SQLRemoveClanMember launched. Passed params {}, {}, {}", guild_id, user_id, clan_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLRemoveClanMember);
			stmt.setLong(1, guild_id);
			stmt.setLong(2, user_id);
			stmt.setInt(3, clan_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLRemoveClanMember2);
				stmt.setLong(1, guild_id);
				stmt.setInt(2, clan_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLRemoveClanMember3);
				stmt.setLong(1, guild_id);
				stmt.setLong(2, user_id);
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
	public static int SQLDelegateOwnership(long guild_id, long user_id_target, long user_id_self, int clan_id) {
		logger.trace("SQLDelegateOwnership launched. Passed params {}, {}, {}, {}", guild_id, user_id_target, user_id_self, clan_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLDelegateOwnership);
			stmt.setLong(1, guild_id);
			stmt.setLong(2, user_id_target);
			stmt.setInt(3, clan_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLDelegateOwnership2);
				stmt.setLong(1, guild_id);
				stmt.setLong(2, user_id_self);
				stmt.setInt(3, clan_id);
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
	public static int SQLDisbandClan(long guild_id, long user_id, int clan_id) {
		logger.trace("SQLDisbandClan launched. Passed params {}, {}, {}", guild_id, user_id, clan_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLDisbandClan);
			stmt.setLong(1, guild_id);
			stmt.setLong(2, user_id);
			stmt.setInt(3, clan_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLDisbandClan2);
				stmt.setLong(1, guild_id);
				stmt.setInt(2, clan_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLDisbandClan3);
				stmt.setLong(1, guild_id);
				stmt.setLong(2, user_id);
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
	public static int SQLCreateClan(long guild_id, long user_id, String name) {
		logger.trace("SQLCreateClan launched. Passed params {}, {}, {}", guild_id, user_id, name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLCreateClan);
			stmt.setLong(1, guild_id);
			stmt.setString(2, name);
			int result = stmt.executeUpdate();
			
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLCreateClan2);
			stmt.setLong(1, guild_id);
			stmt.setString(2, name);
			ResultSet rs = stmt.executeQuery();
			int clan_id = 0;
			if(rs.next()) {
				clan_id = rs.getInt(1);
			}
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLCreateClan3);
				stmt.setInt(1, clan_id);
				stmt.setLong(2, guild_id);
				stmt.setLong(3, user_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLCreateClan4);
				stmt.setInt(1, clan_id);
				stmt.setLong(2, guild_id);
				stmt.setLong(3, user_id);
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
	
	public static ArrayList<Clan> SQLgetClans(long guild_id) {
		logger.trace("SQLgetClans launched. Passed params {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<Clan> clans = new ArrayList<Clan>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetClans);
			stmt.setLong(1, guild_id);
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
	
	public static int SQLgetClanID(long guild_id, String name) {
		logger.trace("SQLgetClanID launched. Passed params {}, {}", guild_id, name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetClanID);
			stmt.setLong(1, guild_id);
			stmt.setString(2, name);
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
	
	public static int SQLgetClanMemberNumber(long guild_id, int clan_id) {
		logger.trace("SQLgetClanMemberNumber launched. Passed params {}, {}", guild_id, clan_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetClanMemberNumber);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, clan_id);
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
	
	public static String SQLgetClanName(long guild_id, int clan_id) {
		logger.trace("SQLgetClanName launched. Passed params {}, {}", guild_id, clan_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetClanName);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, clan_id);
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
	
	public static int SQLUpdateClanMark(long guild_id, int clan_id, String url) {
		logger.trace("SQLUpdateClanMark launched. Passed params {}, {}, {}", guild_id, clan_id, url);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLUpdateClanMark);
			stmt.setString(1, url);
			stmt.setLong(2, guild_id);
			stmt.setInt(3, clan_id);
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
	public static int SQLInsertClanReservation(long guild_id, long user_id, int clan_id, int type, long channel_id) {
		logger.trace("SQLInsertClanReservation launched. Passed params {}, {}, {}, {}, {}", guild_id, user_id, clan_id, type, channel_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLInsertClanReservation);
			stmt.setLong(1, guild_id);
			stmt.setLong(2, user_id);
			stmt.setInt(3, clan_id);
			stmt.setInt(4, type);
			stmt.setLong(5, channel_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertClanReservation Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetClanReservationType(long guild_id, long user_id, int clan_id, boolean done) {
		logger.trace("SQLgetClanReservationType launched. Passed params {}, {}, {}, {}", guild_id, user_id, clan_id, done);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetClanReservationType);
			stmt.setLong(1, guild_id);
			stmt.setLong(2, user_id);
			stmt.setInt(3, clan_id);
			stmt.setBoolean(4, done);
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
	
	public static int SQLgetClanReservationAction(long guild_id, long user_id, int clan_id, boolean done) {
		logger.trace("SQLgetClanReservationType launched. Passed params {}, {}, {}, {}", guild_id, user_id, clan_id, done);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetClanReservationAction);
			stmt.setLong(1, guild_id);
			stmt.setLong(2, user_id);
			stmt.setInt(3, clan_id);
			stmt.setBoolean(4, done);
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
	
	public static ClanReservation SQLgetClanReservation(long guild_id, int clan_id, int type, boolean done) {
		logger.trace("SQLgetClanReservation launched. Passed params {}, {}, {}, {}", guild_id, clan_id, type, done);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetClanReservation);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, clan_id);
			stmt.setInt(3, type);
			stmt.setBoolean(4, done);
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
	
	public static ClanReservation SQLgetClanReservation(long guild_id, long user_id, int clan_id) {
		logger.trace("SQLgetClanReservation launched. Passed params {}, {}, {}, {}, {}", guild_id, user_id, clan_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetClanReservation);
			stmt.setLong(1, guild_id);
			stmt.setLong(2, user_id);
			stmt.setInt(3, clan_id);
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
	
	public static int SQLUpdateClanReservationAction(long guild_id, long user_id, int clan_id, int type, int action) {
		logger.trace("SQLInsertClanReservation launched. Passed params {}, {}, {}, {}, {}", guild_id, user_id, clan_id, type, action);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLUpdateClanReservationAction);
			stmt.setInt(1, action);
			stmt.setLong(2, guild_id);
			stmt.setLong(3, user_id);
			stmt.setInt(4, clan_id);
			stmt.setInt(5, type);
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
	public static Room SQLgetMatchmakingRoom(long guild_id, int type, int status) {
		logger.trace("SQLgetMatchmakingRoom launched. Passed params {}, {}, {}", guild_id, type, status);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetMatchmakingRoom);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, type);
			stmt.setInt(3, status);
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
						rs.getInt(10),
						rs.getTimestamp(11),
						rs.getTimestamp(12),
						rs.getLong(13),
						rs.getLong(14)
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
	
	public static Room SQLgetMatchmakingRoom(long guild_id, int room_id) {
		logger.trace("SQLgetMatchmakingRoom launched. Passed params {}, {}", guild_id, room_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetMatchmakingRoom2);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, room_id);
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
						rs.getInt(10),
						rs.getTimestamp(11),
						rs.getTimestamp(12),
						rs.getLong(13),
						rs.getLong(14)
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
	
	public static Room SQLgetMatchmakingRoom(long guild_id, int clan_id, int type, int status) {
		logger.trace("SQLgetMatchmakingRoom launched. Passed params {}, {}, {}, {}", guild_id, clan_id, type, status);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetMatchmakingRoom3);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, clan_id);
			stmt.setInt(3, clan_id);
			stmt.setInt(4, type);
			stmt.setInt(5, status);
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
						rs.getInt(10),
						rs.getTimestamp(11),
						rs.getTimestamp(12),
						rs.getLong(13),
						rs.getLong(14)
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
	
	public static ArrayList<Room> SQLgetOngoingMatchmakingRooms(long guild_id) {
		logger.trace("SQLgetOngoingMatchmakingRooms launched. Passed params {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<Room> rooms = new ArrayList<Room>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetOngoingMatchmakingRooms);
			stmt.setLong(1, guild_id);
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
						rs.getInt(10),
						rs.getTimestamp(11),
						rs.getTimestamp(12),
						rs.getLong(13),
						rs.getLong(14)
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
	
	public static int SQLisClanMatchmakingRoomOngoing(long guild_id, int type, int clan_id) {
		logger.trace("SQLisClanMatchmakingRoomOngoing launched. Passed params {}, {}, {}", guild_id, type, clan_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLisClanMatchmakingRoomOngoing);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, type);
			stmt.setInt(3, clan_id);
			stmt.setInt(4, clan_id);
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
	public static int SQLCreateMatchmakingRoom(long guild_id, long user_id, int type, int map, int member_limit, long channel_id) {
		logger.trace("SQLCreateMatchmakingRoom launched. Passed params {}, {}, {}, {}, {}, {}", guild_id, user_id, type, map, member_limit, channel_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLCreateMatchmakingRoom);
			rs = stmt.executeQuery();
			int room_id = 0;
			if(rs.next()) {
				room_id = rs.getInt(1);
			}
			room_id++;
			
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLCreateMatchmakingRoom2);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, room_id);
			stmt.setInt(3, type);
			if(map > 0)
				stmt.setInt(4, map);
			else
				stmt.setNull(4, Types.INTEGER);
			stmt.setInt(5, member_limit);
			stmt.setLong(6, channel_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLCreateMatchmakingRoom3);
				stmt.setLong(1, guild_id);
				stmt.setLong(2, user_id);
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
	public static int SQLCreateClanMatchmakingRoom(long guild_id, int type, int map, int clan_id_1, int clan_id_2, int member_limit) {
		logger.trace("SQLCreateMatchmakingRoom launched. Passed params {}, {}, {}, {}, {}, {}", guild_id, type, map, clan_id_1, clan_id_2, member_limit);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLCreateClanMatchmakingRoom);
			rs = stmt.executeQuery();
			int room_id = 0;
			if(rs.next()) {
				room_id = rs.getInt(1);
			}
			room_id++;
			
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLCreateClanMatchmakingRoom2);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, room_id);
			stmt.setInt(3, type);
			stmt.setInt(4, map);
			stmt.setInt(5, clan_id_1);
			stmt.setInt(6, clan_id_2);
			stmt.setInt(7, member_limit);
			if(map > 0)
				stmt.setInt(4, map);
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
	public static int SQLDeleteMatchmakingRoom(long guild_id, int room_id) {
		logger.trace("SQLDeleteMatchmakingRoom launched. Passed params {}, {}", guild_id, room_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLDeleteMatchmakingRoom);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, room_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLDeleteMatchmakingRoom2);
				stmt.setLong(1, guild_id);
				stmt.setInt(2, room_id);
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
	public static int SQLJoinRoom(long guild_id, long user_id, int room_id) {
		logger.trace("SQLJoinRoom launched. Passed params {}, {}, {}", guild_id, user_id, room_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLJoinRoom);
			stmt.setLong(1, guild_id);
			stmt.setLong(2, user_id);
			stmt.setInt(3, room_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLJoinRoom2);
				stmt.setLong(1, guild_id);
				stmt.setInt(2, room_id);
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
	public static int SQLJoinRoom(long guild_id, long user_id, int room_id, int team) {
		logger.trace("SQLJoinRoom launched. Passed params {}, {}, {}, {}", guild_id, user_id, room_id, team);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLJoinRoom);
			stmt.setLong(1, guild_id);
			stmt.setLong(2, user_id);
			stmt.setInt(3, room_id);
			stmt.setInt(4, team);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLJoinRoom2);
				stmt.setLong(1, guild_id);
				stmt.setInt(2, room_id);
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
	public static int SQLUpdateTeams(long guild_id, int room_id, Member [] team1, Member [] team2) {
		logger.trace("SQLUpdateTeams launched. Passed params {}, {} and arrays", guild_id, room_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLUpdateTeams);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, room_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLUpdateTeams2);
				for(int i = 0; i < team1.length; i++) {
					if(team1[i] != null) {
						stmt.setLong(1, guild_id);
						stmt.setLong(2, team1[i].getUserID());
						stmt.setInt(3, room_id);
						stmt.addBatch();
					}
				}
				int [] batchResult = stmt.executeBatch();
				if(batchResult[0] <= 0) {
					myConn.rollback();
					return 0;
				}
				
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLUpdateTeams3);
				for(int i = 0; i < team2.length; i++) {
					if(team2[i] != null) {
						stmt.setLong(1, guild_id);
						stmt.setLong(2, team2[i].getUserID());
						stmt.setInt(3, room_id);
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
	public static int SQLUpdateTeams(long guild_id, int room_id, long user_id_1, long user_id_2) {
		logger.trace("SQLUpdateTeams launched. Passed params {}, {}, {}, {}", guild_id, room_id, user_id_1, user_id_2);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLUpdateTeams4);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, room_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLUpdateTeams5);
				stmt.setLong(1, guild_id);
				stmt.setLong(2, user_id_1);
				stmt.setInt(3, room_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLUpdateTeams6);
				stmt.setLong(1, guild_id);
				stmt.setLong(2, user_id_2);
				stmt.setInt(3, room_id);
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
	
	public static int SQLUpdateRoomMessageID(long guild_id, int room_id, long channel_id, long message_id) {
		logger.trace("SQLUpdateRoomMessageID launched. Passed params {}, {}, {}, {}", guild_id, room_id, channel_id, message_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLUpdateRoomMessageID);
			stmt.setLong(1, channel_id);
			stmt.setLong(2, message_id);
			stmt.setLong(3, guild_id);
			stmt.setInt(4, room_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateRoomMessageID Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateMatchmakingRoomMap(long guild_id, int room_id, int map_id) {
		logger.trace("SQLUpdateMatchmakingRoomMap launched. Passed params {}, {}, {}", guild_id, room_id, map_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLUpdateMatchmakingRoomMap);
			stmt.setLong(1, map_id);
			stmt.setLong(2, guild_id);
			stmt.setInt(3, room_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateMatchmakingRoomMap Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateMatchmakingRoomStatus(long guild_id, int room_id, int status) {
		logger.trace("SQLUpdateMatchmakingRoomStatus launched. Passed params {}, {}, {}", guild_id, room_id, status);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLUpdateMatchmakingRoomStatus);
			stmt.setInt(1, status);
			stmt.setLong(2, guild_id);
			stmt.setInt(3, room_id);
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
	public static int SQLsetWinner(long guild_id, int room_id, int team, boolean clans) {
		logger.trace("SQLsetWinner launched. Passed params {}, {}, {}, {}", guild_id, room_id, team, clans);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLsetWinner);
			stmt.setInt(1, team);
			stmt.setLong(2, guild_id);
			stmt.setInt(3, room_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLsetWinner2);
				stmt.setLong(1, guild_id);
				stmt.setInt(2, room_id);
				stmt.setInt(3, team);
				result = stmt.executeUpdate();
			}
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLsetWinner3);
				stmt.setLong(1, guild_id);
				stmt.setInt(2, room_id);
				stmt.setInt(3, team);
				result = stmt.executeUpdate();
			}
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLsetWinner4);
				stmt.setLong(1, guild_id);
				stmt.setInt(2, room_id);
				stmt.setInt(3, team);
				stmt.setLong(4, guild_id);
				result = stmt.executeUpdate();
			}
						
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLsetWinner5);
				stmt.setLong(1, guild_id);
				stmt.setInt(2, room_id);
				stmt.setInt(3, team);
				stmt.setLong(4, guild_id);
				result = stmt.executeUpdate();
			}
			
			if(team == 1 && clans) {
				if(result > 0) {
					stmt = myConn.prepareStatement(CompetitiveStatements.SQLsetWinner6);
					stmt.setLong(1, guild_id);
					stmt.setInt(2, room_id);
					stmt.setLong(3, guild_id);
					result = stmt.executeUpdate();
				}
				
				if(result > 0) {
					stmt = myConn.prepareStatement(CompetitiveStatements.SQLsetWinner7);
					stmt.setLong(1, guild_id);
					stmt.setInt(2, room_id);
					stmt.setLong(3, guild_id);
					result = stmt.executeUpdate();
				}
			}
			else if(team == 2 && clans) {
				if(result > 0) {
					stmt = myConn.prepareStatement(CompetitiveStatements.SQLsetWinner8);
					stmt.setLong(1, guild_id);
					stmt.setInt(2, room_id);
					stmt.setLong(3, guild_id);
					result = stmt.executeUpdate();
				}
				
				if(result > 0) {
					stmt = myConn.prepareStatement(CompetitiveStatements.SQLsetWinner9);
					stmt.setLong(1, guild_id);
					stmt.setInt(2, room_id);
					stmt.setLong(3, guild_id);
					result = stmt.executeUpdate();
				}
			}
			
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLsetWinner10);
			stmt.setLong(1, guild_id);
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
	public static int SQLrevertWinner(long guild_id, int room_id, int team, boolean clans) {
		logger.trace("SQLrevertWinner launched. Passed params {}, {}, {}, {}", guild_id, room_id, team, clans);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLrevertWinner);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, room_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLrevertWinner2);
				stmt.setInt(1, room_id);
				stmt.setLong(2, guild_id);
				stmt.setInt(3, room_id);
				stmt.setInt(4, team);
				stmt.setLong(5, guild_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLrevertWinner3);
				stmt.setInt(1, room_id);
				stmt.setLong(2, guild_id);
				stmt.setInt(3, room_id);
				stmt.setInt(4, team);
				stmt.setLong(5, guild_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLrevertWinner4);
				stmt.setLong(1, guild_id);
				stmt.setInt(2, room_id);
				result = stmt.executeUpdate();
			}
			
			if(team == 1 && clans) {
				if(result > 0) {
					stmt = myConn.prepareStatement(CompetitiveStatements.SQLrevertWinner5);
					stmt.setLong(1, guild_id);
					stmt.setInt(2, room_id);
					stmt.setLong(3, guild_id);
					result = stmt.executeUpdate();
				}
				
				if(result > 0) {
					stmt = myConn.prepareStatement(CompetitiveStatements.SQLrevertWinner6);
					stmt.setLong(1, guild_id);
					stmt.setInt(2, room_id);
					stmt.setLong(3, guild_id);
					result = stmt.executeUpdate();
				}
			}
			else if(team == 2 && clans) {
				if(result > 0) {
					stmt = myConn.prepareStatement(CompetitiveStatements.SQLrevertWinner7);
					stmt.setLong(1, guild_id);
					stmt.setInt(2, room_id);
					stmt.setLong(3, guild_id);
					result = stmt.executeUpdate();
				}
				
				if(result > 0) {
					stmt = myConn.prepareStatement(CompetitiveStatements.SQLrevertWinner8);
					stmt.setLong(1, guild_id);
					stmt.setInt(2, room_id);
					stmt.setLong(3, guild_id);
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
	public static int SQLPickMember(long guild_id, int room_id, long user_id, long leader_id, int team) {
		logger.trace("SQLPickMember launched. Passed params {}, {}, {}, {}, {}", guild_id, room_id, user_id, leader_id, team);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLPickMember);
			stmt.setInt(1, team);
			stmt.setLong(2, guild_id);
			stmt.setInt(3, room_id);
			stmt.setLong(4, user_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLPickMember2);
				stmt.setLong(1, guild_id);
				stmt.setLong(2, leader_id);
				stmt.setInt(3, room_id);
				result = stmt.executeUpdate();
			}
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLPickMember3);
				stmt.setLong(1, guild_id);
				stmt.setLong(2, leader_id);
				stmt.setInt(3, room_id);
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
	public static int SQLInsertMap(long guild_id, String mapName, String url) {
		logger.trace("SQLInsertMap launched. Passed params {}, {}, {}", guild_id, mapName, url);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLInsertMap);
			stmt.setLong(1, guild_id);
			stmt.setString(2, mapName);
			rs = stmt.executeQuery();
			boolean exists = false;
			if(rs.next()) {
				exists = true;
			}
			
			int result = 0;
			if(exists) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLInsertMap2);
				stmt.setString(1, mapName);
				if(url != null)
					stmt.setString(2, url);
				else
					stmt.setNull(2, Types.VARCHAR);
				stmt.setLong(3, guild_id);
				stmt.setString(4, mapName);
				result = stmt.executeUpdate();
			}
			else {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLInsertMap3);
				stmt.setLong(1, guild_id);
				stmt.setString(2, mapName);
				if(url != null)
					stmt.setString(3, url);
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
	
	public static CompMap SQLgetRandomMap(long guild_id) {
		logger.trace("SQLgetRandomMap launched. Passed params {}, {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetRandomMap);
			stmt.setLong(1, guild_id);
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
	
	public static CompMap SQLgetMap(int map_id) {
		logger.trace("SQLgetMap launched. Passed params {}", map_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetMap);
			stmt.setInt(1, map_id);
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
	
	public static CompMap SQLgetMap(long guild_id, String map) {
		logger.trace("SQLgetMap launched. Passed params {}, {}", guild_id, map);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetMap2);
			stmt.setLong(1, guild_id);
			stmt.setString(2, map);
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
	public static ClanMember SQLgetClanDetails(long user_id, long guild_id) {
		logger.trace("SQLgetClanDetails launched. Passed params {}, {}", user_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetClanDetails);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
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
	
	public static ClanMember SQLgetClanDetailsByName(String name, long guild_id) {
		logger.trace("SQLgetClanDetailsByName launched. Passed params {}, {}", name, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetClanDetailsByName);
			stmt.setString(1, name);
			stmt.setLong(2, guild_id);
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
	
	public static ArrayList<ClanMember> SQLgetClanMembers(long guild_id, int clan_id) {
		logger.trace("SQLgetClanMembers launched. Passed params {}, {}", guild_id, clan_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<ClanMember> clanMembers = new ArrayList<ClanMember>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetClanMembers);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, clan_id);
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
	
	public static ArrayList<ClanMember> SQLgetClanMembersStaff(long guild_id, int clan_id) {
		logger.trace("SQLgetClanMembersStaff launched. Passed params {}, {}", guild_id, clan_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ArrayList<ClanMember> clanMembers = new ArrayList<ClanMember>();
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLgetClanMembersStaff);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, clan_id);
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
	
	public static int SQLisUserInRoom(long guild_id, long user_id) {
		logger.trace("SQLisUserInRoom launched. Passed params {}, {}", guild_id, user_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLisUserInRoom);
			stmt.setLong(1, guild_id);
			stmt.setLong(2, user_id);
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
	public static int SQLLeaveRoom(long guild_id, long user_id, int room_id) {
		logger.trace("SQLLeaveRoom launched. Passed params {}, {}, {}", guild_id, user_id, room_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLLeaveRoom);
			stmt.setLong(1, guild_id);
			stmt.setLong(2, user_id);
			stmt.setInt(3, room_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLLeaveRoom2);
				stmt.setLong(1, guild_id);
				stmt.setInt(2, room_id);
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
	
	public static Member SQLRetrievePicker(long guild_id, int room_id, int status) {
		logger.trace("SQLRetrievePicker launched. Passed params {}, {}, {}", guild_id, room_id, status);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLRetrievePicker);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, room_id);
			stmt.setInt(3, status);
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
					rs.getBoolean(8),
					rs.getBoolean(9)
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
	
	public static Member SQLRetrieveMember(long guild_id, int room_id, int status, long user_id) {
		logger.trace("SQLRetrieveMember launched. Passed params {}, {}, {}, {}, {}", guild_id, room_id, status, user_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLRetrieveMember);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, room_id);
			stmt.setInt(3, status);
			stmt.setLong(4, user_id);
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
					rs.getBoolean(8),
					rs.getBoolean(9)
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
	
	public static Member SQLRetrieveMember(long guild_id, int room_id, int status, String username) {
		logger.trace("SQLRetrieveMember launched. Passed params {}, {}, {}, {}, {}", guild_id, room_id, status, username);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), Competitive.username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLRetrieveMember2);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, room_id);
			stmt.setInt(3, status);
			stmt.setString(4, username);
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
					rs.getBoolean(8),
					rs.getBoolean(9)
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
	
	@SuppressWarnings("resource")
	public static int SQLUpdateRoomMaster(long guild_id, long user_id1, long user_id2, int room_id) {
		logger.trace("SQLLeaveRoom launched. Passed params {}, {}, {}, {}", guild_id, user_id1, user_id2, room_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLUpdateRoomMaster);
			stmt.setLong(1, guild_id);
			stmt.setLong(2, user_id1);
			stmt.setInt(3, room_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				stmt = myConn.prepareStatement(CompetitiveStatements.SQLUpdateRoomMaster2);
				stmt.setLong(1, guild_id);
				stmt.setLong(2, user_id2);
				stmt.setInt(3, room_id);
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
	
	public static int SQLUpdateRoomMaster(long guild_id, long user_id, int room_id) {
		logger.trace("SQLLeaveRoom launched. Passed params {}, {}, {}", guild_id, user_id, room_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLUpdateRoomMaster3);
			stmt.setLong(1, guild_id);
			stmt.setLong(2, user_id);
			stmt.setInt(3, room_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLLeaveRoom Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateRoomMemberLimit(long guild_id, int room_id, int memberLimit) {
		logger.trace("SQLUpdateRoomMemberLimit launched. Passed params {}, {}, {}", guild_id, room_id, memberLimit);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			stmt = myConn.prepareStatement(CompetitiveStatements.SQLUpdateRoomMemberLimit);
			stmt.setInt(1, memberLimit);
			stmt.setLong(2, guild_id);
			stmt.setInt(3, room_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateRoomMemberLimit Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
}
