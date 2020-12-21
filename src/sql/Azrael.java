package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Bancollect;
import constructors.CategoryConf;
import constructors.Channels;
import constructors.CustomCommand;
import constructors.GoogleAPISetup;
import constructors.GoogleEvents;
import constructors.GoogleSheet;
import constructors.GoogleSheetColumn;
import constructors.History;
import constructors.NameFilter;
import constructors.RSS;
import constructors.RejoinTask;
import constructors.User;
import constructors.Warning;
import constructors.Watchlist;
import core.Hashes;
import enums.GoogleDD;
import enums.GoogleEvent;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import util.CharacterReplacer;
import util.STATIC;

public class Azrael {
	private static final Logger logger = LoggerFactory.getLogger(Azrael.class);
	
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
	
	public static synchronized void SQLInsertActionLog(String event, long target_id, long guild_id, String description) {
		if(IniFileReader.getActionLog()) {
			logger.trace("SQLInsertActionLog launched. Passed params {}, {}, {}, {}", event, target_id, guild_id, description);
			Connection myConn = null;
			PreparedStatement stmt = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("INSERT INTO action_log (event, target_id, guild_id, description, timestamp) VALUES(?, ?, ?, ?, ?)");
				stmt = myConn.prepareStatement(sql);
				stmt.setString(1, event);
				stmt.setLong(2, target_id);
				stmt.setLong(3, guild_id);
				stmt.setString(4, description);
				stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
				stmt.executeUpdate();
			} catch (SQLException e) {
				logger.error("SQLInsertActionLog Exception", e);
			} finally {
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
	}
	
	public static void SQLInsertHistory(long user_id, long guild_id, String type, String reason, long penalty, String info) {
		logger.trace("SQLInsertHistory launched. Passed params {}, {}, {}, {}, {}, {}", user_id, guild_id, type, reason, penalty, info);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO history (fk_user_id, fk_guild_id, type, reason, time, penalty, info) VALUES(?, ?, ?, ?, ?, "+(penalty != 0 ? "?" : "NULL")+", ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			stmt.setString(3, type);
			stmt.setString(4, reason);
			stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
			if(penalty != 0) {
				stmt.setLong(6, penalty);
				stmt.setString(7, info);
			}
			else {
				stmt.setString(6, info);
			}
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertHistory Exception", e);
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<History> SQLgetHistory(long user_id, long guild_id) {
		logger.trace("SQLgetHistory launched. Passed params {}, {}", user_id, guild_id);
		ArrayList<History> history = new ArrayList<History>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT type, reason, time, penalty, info FROM history WHERE fk_user_id = ? && fk_guild_id = ? ORDER BY time desc");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				history.add(
					new History(
						rs.getString(1),
						rs.getString(2),
						rs.getTimestamp(3),
						rs.getLong(4),
						rs.getString(5)
					)	
				);
			}
			return history;
		} catch (SQLException e) {
			logger.error("SQLgetHistory Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetSingleActionEventCount(String event, long target_id, long guild_id) {
		logger.trace("SQLgetSingleActionEventCount launched. Passed params {}, {}, {}", event, target_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT COUNT(*) FROM action_log WHERE target_id = ? && guild_id = ? && event = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, target_id);
			stmt.setLong(2, guild_id);
			stmt.setString(3, event);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetSingleActionEventCount Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetDoubleActionEventDescriptions(String event, String event2, long target_id, long guild_id) {
		logger.trace("SQLgetDoubleActionEventDescriptions launched. Passed params {}, {}, {}, {}", event, event2, target_id, guild_id);
		ArrayList<String> descriptions = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT description FROM action_log WHERE target_id = ? && (guild_id = ? || guild_id = 0) && (event = ? || event = ?) GROUP BY description LIMIT 50");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, target_id);
			stmt.setLong(2, guild_id);
			stmt.setString(3, event);
			stmt.setString(4, event2);
			rs = stmt.executeQuery();
			while(rs.next()) {
				descriptions.add(rs.getString(1));
			}
			return descriptions;
		} catch (SQLException e) {
			logger.error("SQLgetDoubleActionEventDescription Exception", e);
			return descriptions;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetSingleActionEventDescriptions(String event, long target_id, long guild_id) {
		logger.trace("SQLgetSingleActionEventDescriptions launched. Passed params {}, {}, {}", event, target_id, guild_id);
		ArrayList<String> descriptions = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT description FROM action_log WHERE target_id = ? && guild_id = ? && event = ? GROUP BY description LIMIT 50");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, target_id);
			stmt.setLong(2, guild_id);
			stmt.setString(3, event);
			rs = stmt.executeQuery();
			while(rs.next()) {
				descriptions.add(rs.getString(1));
			}
			return descriptions;
		} catch (SQLException e) {
			logger.error("SQLgetSingleActionEventDescriptions Exception", e);
			return descriptions;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	public static ArrayList<String> SQLgetCriticalActionEvents(long target_id, long guild_id) {
		logger.trace("SQLgetCriticalActionEvents launched. Passed params {}, {}", target_id, guild_id);
		ArrayList<String> descriptions = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT description, timestamp FROM action_log WHERE target_id = ? && guild_id = ? && (event = \"MEMBER_KICK\" || event = \"MEMBER_BAN_ADD\" || event = \"MEMBER_BAN_REMOVE\" || event = \"MEMBER_MUTE_ADD\" || event = \"MEMBER_ROLE_ADD\" || event = \"MEMBER_ROLE_REMOVE\") ORDER BY timestamp desc LIMIT 30");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, target_id);
			stmt.setLong(2, guild_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				descriptions.add("`["+rs.getTimestamp(2).toString()+"] - "+rs.getString(1)+"`");
			}
			return descriptions;
		} catch (SQLException e) {
			logger.error("SQLgetCriticalActionEvents Exception", e);
			return descriptions;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetSingleActionEventDescriptionsOrdered(String event, long target_id, long guild_id) {
		logger.trace("SQLgetSingleActionEventDescriptions launched. Passed params {}, {}, {}", event, target_id, guild_id);
		ArrayList<String> descriptions = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT timestamp, description FROM action_log WHERE target_id = ? && guild_id = ? && event = ? ORDER BY timestamp desc LIMIT 30");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, target_id);
			stmt.setLong(2, guild_id);
			stmt.setString(3, event);
			rs = stmt.executeQuery();
			while(rs.next()) {
				descriptions.add("`["+rs.getString(1)+"] -` "+rs.getString(2));
			}
			return descriptions;
		} catch (SQLException e) {
			logger.error("SQLgetSingleActionEventDescriptions Exception", e);
			return descriptions;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertUser(long user_id, String name, String lang, String avatar) {
		logger.trace("SQLInsertUser launched. Passed params {}, {}, {}, {}", user_id, name, lang, avatar);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO users (user_id, name, lang, avatar_url) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), avatar_url=VALUES(avatar_url)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setString(2, name);
			stmt.setString(3, lang);
			stmt.setString(4, avatar);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertUser Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLBulkInsertUsers(List<Member> members) {
		logger.trace("SQLBulkInsertUsers launched. Passed member list params");
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip, "&rewriteBatchedStatements=true"), username, password);
			myConn.setAutoCommit(false); 
			String sql = ("INSERT INTO users (user_id, name, lang, avatar_url) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), avatar_url=VALUES(avatar_url)");
			stmt = myConn.prepareStatement(sql);
			for(Member member : members) {
				stmt.setLong(1, member.getUser().getIdLong());
				stmt.setString(2, member.getUser().getName()+"#"+member.getUser().getDiscriminator());
				stmt.setString(3, STATIC.getLanguage2(member.getGuild()));
				stmt.setString(4, member.getUser().getEffectiveAvatarUrl());
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
	
	public static int SQLUpdateAvatar(long user_id, String avatar) {
		logger.trace("SQLUpdateAvatar launched. Passed params {}, {}", user_id, avatar);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE users SET avatar_url = ? WHERE user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, avatar);
			stmt.setLong(2, user_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateAvatar Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertJoinDate(long user_id, long guild_id, String join_date) {
		logger.trace("SQLInsertJoinDate launched. Passed params {}, {}, {}", user_id, guild_id, join_date);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT IGNORE INTO join_dates (fk_user_id, fk_guild_id, first_join, join_date) VALUES(?,?,1,?), (?,?,0,?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			stmt.setString(3, join_date);
			stmt.setLong(4, user_id);
			stmt.setLong(5, guild_id);
			stmt.setString(6, join_date);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertJoinDate Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLBulkInsertJoinDates(List<Member> members) {
		logger.trace("SQLBulkInsertJoinDates launched. Passed member list params");
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip, "&rewriteBatchedStatements=true"), username, password);
			myConn.setAutoCommit(false); 
			String sql = ("INSERT IGNORE INTO join_dates (fk_user_id, fk_guild_id, first_join, join_date) VALUES (?, ?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			for(int i = 0; i <= 1; i++) {
				for(Member member : members) {
					stmt.setLong(1, member.getUser().getIdLong());
					stmt.setLong(2, member.getGuild().getIdLong());
					stmt.setInt(3, i);
					stmt.setString(4, member.getTimeJoined().format(DateTimeFormatter.ISO_LOCAL_DATE));
					stmt.addBatch();
				}
			}
			stmt.executeBatch();
			myConn.commit();
		} catch (SQLException e) {
			logger.error("SQLBulkInsertJoinDates Exception", e);
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateJoinDate(long user_id, long guild_id, String join_date) {
		logger.trace("SQLUpdateJoinDate launched. Passed params {}, {}, {}", user_id, guild_id, join_date);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE join_dates SET join_date = ? WHERE fk_user_id = ? AND fk_guild_id = ? AND first_join = 0");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, join_date);
			stmt.setLong(2, user_id);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateJoinDate Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static User SQLgetUser(String name) {
		logger.trace("SqlgetUser launched. Passed params {}", name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT user_id, name FROM users WHERE name = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, name);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return new User(rs.getLong(1), rs.getString(2));
			}
			return null;
		} catch (SQLException e) {
			logger.error("SQLgetUser Exception", e);
			return null;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static User SQLgetUserThroughID(String user_id) {
		logger.trace("SQLgetUserThroughID launched. Passed params {}", user_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM users WHERE user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, user_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				return new User(rs.getLong(1), rs.getString(2), rs.getString(4));
			}
			return null;
		} catch (SQLException e) {
			logger.error("SQLgetUserThroughID Exception", e);
			return null;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static User SQLgetJoinDatesFromUser(long user_id, long guild_id, User user) {
		logger.trace("SQLgetJoinDatesFromUser launched. Passed params {}, {}, User object", user_id, guild_id);
		String originalJoinDate = "N/A";
		String newestJoinDate = "N/A";
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT join_date FROM join_dates WHERE fk_user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			rs = stmt.executeQuery();
			int count = 0;
			while(rs.next()) {
				if(count == 0)
					originalJoinDate = rs.getString(1);
				else
					newestJoinDate = rs.getString(1);
				count++;
			}
			return user.setJoinDates(originalJoinDate, newestJoinDate);
		} catch (SQLException e) {
			logger.error("SQLgetJoinDatesFromUser Exception", e);
			return user.setJoinDates(originalJoinDate, newestJoinDate);
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUser(Long user_id, String name) {
		logger.trace("SQLUpdateUser launched. Passed params {}, {}", user_id, name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE users SET name = ? WHERE user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, name);
			stmt.setLong(2, user_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUser Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static String SQLgetUserLang(long user_id) {
		final var language = Hashes.getLanguage(user_id);
		if(language == null) {
			logger.trace("SQLgetUserLang launched. Passed params {}", user_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT lang FROM users WHERE user_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, user_id);
				rs = stmt.executeQuery();
				if(rs.next()) {
					String lang = rs.getString(1);
					Hashes.setLanguage(user_id, lang);
					return rs.getString(1);
				}
				return null;
			} catch (SQLException e) {
				logger.error("SQLgetUserLang Exception", e);
				return null;
			} finally {
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return language;
	}
	
	public static int SQLUpdateUserLanguage(long user_id, String language) {
		logger.trace("SQLUpdateUserLanguage launched. Passed params {}, {}", user_id, language);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE users SET lang = ? WHERE user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, language);
			stmt.setLong(2, user_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUserLanguage Exception", e);
			return -1;
		} finally {
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
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT guild_id FROM guild WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
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
	
	public static String SQLgetLanguage(long guild_id) {
		logger.trace("SQLgetLanguage launched. Passed params {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT language FROM guild WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getString(1);
			}
			return null;
		} catch (SQLException e) {
			logger.error("SQLgetLanguage Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateLanguage(Long guild_id, String language) {
		logger.trace("SQLUpdateLanguage launched. Passed params {}, {}", guild_id, language);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE guild SET language = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, language);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateLanguage Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertGuild(Long guild_id, String guild_name) {
		logger.trace("SQLInsertGuild launched. Passed params {}, {}", guild_id, guild_name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO guild (guild_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)");
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
	
	public static String SQLgetNickname(Long user_id, Long guild_id) {
		logger.trace("SQLgetNickname launched. Passed params {}, {}", user_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT nickname FROM nickname WHERE fk_user_id= ? && fk_guild_id= ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getString(1);
			}
			return "";
		} catch (SQLException e) {
			logger.error("SQLgetNickname Exception", e);
			return "";
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertNickname(Long user_id, Long guild_id, String nickname) {
		logger.trace("SQLInsertNickname launched. Passed params {}, {}, {}", user_id, guild_id, nickname);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO nickname VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE nickname=VALUES(nickname)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			stmt.setString(3, nickname);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertNickname Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateNickname(Long user_id, Long guild_id, String nickname) {
		logger.trace("SQLUpdateNickname launched. Passed params {}, {}", user_id, guild_id, nickname);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE nickname SET nickname = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, nickname);
			stmt.setLong(2, user_id);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateNickname Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteNickname(Long user_id, Long guild_id) {
		logger.trace("SQLDeleteNickname launched. Passed params {}, {}", user_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM nickname WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteNickname Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static Bancollect SQLgetData(Long user_id, Long guild_id) {
		logger.trace("SQLgetData launched. Passed params {}, {}", user_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM bancollect WHERE fk_user_id= ? && fk_guild_id= ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return new Bancollect(rs.getLong(1), rs.getLong(2), rs.getInt(3), rs.getInt(4), rs.getTimestamp(5), rs.getTimestamp(6), rs.getBoolean(7), rs.getBoolean(8), rs.getBoolean(9));
			}
			return new Bancollect();
		} catch (SQLException e) {
			logger.error("SQLgetData Exception", e);
			return new Bancollect();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLgetMuted(long user_id, long guild_id) {
		logger.trace("SQLgetMuted launched. Passed params {}, {}", user_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT muted FROM bancollect WHERE fk_user_id= ? && fk_guild_id= ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getBoolean(1);
			}
			return false;
		} catch (SQLException e) {
			logger.error("SQLgetMuted Exception", e);
			return false;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLgetCustomMuted(long user_id, long guild_id) {
		logger.trace("SQLgetMuted launched. Passed params {}, {}", user_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT custom_time FROM bancollect WHERE fk_user_id= ? && fk_guild_id= ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getBoolean(1);
			}
			return false;
		} catch (SQLException e) {
			logger.error("SQLgetMuted Exception", e);
			return false;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLisBanned(long user_id, long guild_id) {
		logger.trace("SQLgetMuted launched. Passed params {}, {}", user_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT fk_ban_id FROM bancollect WHERE fk_user_id= ? && fk_guild_id= ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				if(rs.getInt(1) == 1)
					return false;
				else
					return true;
			}
			return false;
		} catch (SQLException e) {
			logger.error("SQLgetMuted Exception", e);
			return false;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetWarning(long user_id, long guild_id) {
		logger.trace("SQLgetMuted launched. Passed params {}, {}", user_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT fk_warning_id FROM bancollect WHERE fk_user_id= ? && fk_guild_id= ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetMuted Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertData(long user_id, long guild_id, int warning_id, int ban_id, Timestamp timestamp, Timestamp unmute, boolean muted, boolean custom_time) {
		logger.trace("SQLInsertData launched. Passed params {}, {}, {}, {}, {}, {}, {}, {}", user_id, guild_id, warning_id, ban_id, timestamp, unmute, muted, custom_time);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO bancollect VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0) ON DUPLICATE KEY UPDATE fk_warning_id=VALUES(fk_warning_id), fk_ban_id=VALUES(fk_ban_id), timestamp=VALUES(timestamp), unmute=VALUES(unmute), muted=VALUES(muted), custom_time=VALUES(custom_time), guild_left=VALUES(guild_left)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			stmt.setInt(3, warning_id);
			stmt.setInt(4, ban_id);
			stmt.setTimestamp(5, timestamp);
			stmt.setTimestamp(6, unmute);
			stmt.setBoolean(7, muted);
			stmt.setBoolean(8, custom_time);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertData Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteData(long user_id, long guild_id) {
		logger.trace("SQLDeleteData launched. Passed params {}, {}", user_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM bancollect WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteData Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateMuted(long user_id, long guild_id, boolean muted) {
		logger.trace("SQLUpdateMuted launched. Passed params {}, {}, {}", user_id, guild_id, muted);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE bancollect SET muted = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setBoolean(1, muted);
			stmt.setLong(2, user_id);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateMuted Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateMutedAndCustomMuted(long user_id, long guild_id, boolean muted, boolean custom_time) {
		logger.trace("SQLUpdateMutedAndCustomMuted launched. Passed params {}, {}, {}, {}", user_id, guild_id, muted, custom_time);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE bancollect SET muted = ?, custom_time = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setBoolean(1, muted);
			stmt.setBoolean(2, custom_time);
			stmt.setLong(3, user_id);
			stmt.setLong(4, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateMutedAndCustomMuted Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateMutedOnEnd(long user_id, long guild_id, boolean muted, boolean custom_time) {
		logger.trace("SQLUpdateMutedOnEnd launched. Passed params {}, {}, {}, {}", user_id, guild_id, muted, custom_time);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE bancollect SET muted = ?, custom_time = ? WHERE fk_user_id = ? && fk_guild_id = ? && muted = 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setBoolean(1, muted);
			stmt.setBoolean(2, custom_time);
			stmt.setLong(3, user_id);
			stmt.setLong(4, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateMuted Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateGuildLeft(long user_id, long guild_id, boolean guildLeft) {
		logger.trace("SQLUpdateGuildLeft launched. Passed params {}, {}, {}", user_id, guild_id, guildLeft);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE bancollect SET guild_left = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setBoolean(1, guildLeft);
			stmt.setLong(2, user_id);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateGuildLeft Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static Warning SQLgetWarning(long guild_id, int warning_id) {
		logger.trace("SQLgetWarning launched. Passed params {}, {}", guild_id, warning_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT warning_id, mute_time, description FROM warnings WHERE fk_guild_id = ? && warning_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, warning_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return new Warning(rs.getInt(1), rs.getDouble(2), rs.getString(3));
			}
			return new Warning();
		} catch (SQLException e) {
			logger.error("SQLgetWarning Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetMaxWarning(long guild_id) {
		logger.trace("SQLgetMaxWarning launched. Passed params {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT MAX(warning_id) FROM warnings WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetMaxWarning Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertWarning(long guild_id, int warning_id) {
		logger.trace("SQLInsertWarning launched. Passed params {}, {}", guild_id, warning_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = "";
			switch(warning_id) {
				case 1 -> {
					sql = ("INSERT INTO warnings (fk_guild_id, warning_id, mute_time, description) VALUES"
							+ "(?, 0, 0, \"no warning\"),"
							+ "(?, 1, 0, \"first warning\") ON DUPLICATE KEY UPDATE description=VALUES(description)");
					stmt = myConn.prepareStatement(sql);
					stmt.setLong(1, guild_id);
					stmt.setLong(2, guild_id);
				}
				case 2 -> {
					sql = ("INSERT INTO warnings (fk_guild_id, warning_id, mute_time, description) VALUES"
							+ "(?, 0, 0, \"no warning\"),"
							+ "(?, 1, 0, \"first warning\"),"
							+ "(?, 2, 0, \"second warning\") ON DUPLICATE KEY UPDATE description=VALUES(description)");
					stmt = myConn.prepareStatement(sql);
					stmt.setLong(1, guild_id);
					stmt.setLong(2, guild_id);
					stmt.setLong(3, guild_id);
				}
				case 3 -> {
					sql = ("INSERT INTO warnings (fk_guild_id, warning_id, mute_time, description) VALUES"
							+ "(?, 0, 0, \"no warning\"),"
							+ "(?, 1, 0, \"first warning\"),"
							+ "(?, 2, 0, \"second warning\"),"
							+ "(?, 3, 0, \"third warning\") ON DUPLICATE KEY UPDATE description=VALUES(description)");
					stmt = myConn.prepareStatement(sql);
					stmt.setLong(1, guild_id);
					stmt.setLong(2, guild_id);
					stmt.setLong(3, guild_id);
					stmt.setLong(4, guild_id);
				}
				case 4 -> {
					sql = ("INSERT INTO warnings (fk_guild_id, warning_id, mute_time, description) VALUES"
							+ "(?, 0, 0, \"no warning\"),"
							+ "(?, 1, 0, \"first warning\"),"
							+ "(?, 2, 0, \"second warning\"),"
							+ "(?, 3, 0, \"third warning\"),"
							+ "(?, 4, 0, \"fourth warning\") ON DUPLICATE KEY UPDATE description=VALUES(description)");
					stmt = myConn.prepareStatement(sql);
					stmt.setLong(1, guild_id);
					stmt.setLong(2, guild_id);
					stmt.setLong(3, guild_id);
					stmt.setLong(4, guild_id);
					stmt.setLong(5, guild_id);
				}
				case 5 -> {
					sql = ("INSERT INTO warnings (fk_guild_id, warning_id, mute_time, description) VALUES"
							+ "(?, 0, 0, \"no warning\"),"
							+ "(?, 1, 0, \"first warning\"),"
							+ "(?, 2, 0, \"second warning\"),"
							+ "(?, 3, 0, \"third warning\"),"
							+ "(?, 4, 0, \"fourth warning\"),"
							+ "(?, 5, 0, \"fifth warning\") ON DUPLICATE KEY UPDATE description=VALUES(description)");
					stmt = myConn.prepareStatement(sql);
					stmt.setLong(1, guild_id);
					stmt.setLong(2, guild_id);
					stmt.setLong(3, guild_id);
					stmt.setLong(4, guild_id);
					stmt.setLong(5, guild_id);
					stmt.setLong(6, guild_id);
				}
			}
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertWarning Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateWarning(long user_id, long guild_id, int warning_id) {
		logger.trace("SQLUpdateWarning launched. Passed params {}, {}", user_id, guild_id, warning_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE bancollect SET fk_warning_id = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, warning_id);
			stmt.setLong(2, user_id);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateWarning Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateMuteTimeOfWarning(long guild_id, int warning_id, long mute_time) {
		logger.trace("SQLUpdateTimeOfWarning launched. Passed params {}, {}, {}", guild_id, warning_id, mute_time);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE warnings SET mute_time = ? WHERE fk_guild_id = ? && warning_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, mute_time);
			stmt.setLong(2, guild_id);
			stmt.setInt(3, warning_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateMuteTimeOfWarning Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUnmute(Long user_id, Long guild_id, Timestamp timestamp, Timestamp unmute, boolean muted, boolean custom_time) {
		logger.trace("SQLUpdateUnmute launched. Passed params {}, {}, {}, {}, {}, {}", user_id, guild_id, timestamp, unmute, muted, custom_time);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE bancollect SET timestamp = ?, unmute = ?, muted = ?, custom_time = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setTimestamp(1, timestamp);
			stmt.setTimestamp(2, unmute);
			stmt.setBoolean(3, muted);
			stmt.setBoolean(4, custom_time);
			stmt.setLong(5, user_id);
			stmt.setLong(6, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUnmute Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUnmute(Long user_id, Long guild_id, Timestamp unmute) {
		logger.trace("SQLUpdateUnmute launched. Passed params {}, {}, {}", user_id, guild_id, unmute);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE bancollect SET unmute = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setTimestamp(1, unmute);
			stmt.setLong(2, user_id);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUnmute Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateBan(Long user_id, Long guild_id, Integer ban_id) {
		logger.trace("SQLUpdateban launched. Passed params {}, {}, {}", user_id, guild_id, ban_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE bancollect SET fk_ban_id = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, ban_id);
			stmt.setLong(2, user_id);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateBan Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertChannels(long channel_id, String channel_name) {
		logger.trace("SQLInsertChannels launched. Passed params {}, {}", channel_id, channel_name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO channels (channel_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, channel_id);
			stmt.setString(2, channel_name);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertChannels Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLBulkInsertChannels(List<TextChannel> textChannels) {
		logger.trace("SQLBulkInsertChannels launched. Array param passed");
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false); 
			String sql = ("INSERT INTO channels (channel_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name)");
			stmt = myConn.prepareStatement(sql);
			for(final var channel : textChannels) {
				stmt.setLong(1, channel.getIdLong());
				stmt.setString(2, channel.getName());
				stmt.addBatch();
			}
			stmt.executeBatch();
			myConn.commit();
		} catch (SQLException e) {
			logger.error("SQLInsertChannels Exception", e);
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteChannels(long channel_id) {
		logger.trace("SQLDeleteChannels launched. Passed params {}", channel_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM channels WHERE channel_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, channel_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteChannels Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertChannel_Conf(long channel_id, long guild_id, String channel_type) {
		logger.trace("SQLInsertChannel launched. Passed params {}, {}, {}", channel_id, guild_id, channel_type);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO channel_conf (fk_channel_id, fk_channel_type, fk_guild_id, url_censoring, txt_removal) VALUES (?, ?, ?, 0, 0) ON DUPLICATE KEY UPDATE fk_channel_type = VALUES(fk_channel_type)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, channel_id);
			stmt.setString(2, channel_type);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertChannel_Conf Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteChannelConf(long channel_id, long guild_id) {
		logger.trace("SQLDeleteChannelConf launched. Passed params {}, {}", channel_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM channel_conf WHERE fk_channel_id = ? and fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, channel_id);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteChannelConf Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteAllChannelConfs(long guild_id) {
		logger.trace("SQLDeleteAllChannelConfs launched. Passed params {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM channel_conf WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteAllChannelConfs Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertChannel_ConfURLCensoring(long channel_id, long guild_id, boolean url_censoring) {
		logger.trace("SQLInsertChannel_ConfURLCensoring launched. Passed params {}, {}, {}, {}", channel_id, guild_id, url_censoring);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO channel_conf (fk_channel_id, fk_guild_id, url_censoring, txt_removal) VALUES (?, ?, ?, 0) ON DUPLICATE KEY UPDATE url_censoring = VALUES(url_censoring)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, channel_id);
			stmt.setLong(2, guild_id);
			stmt.setBoolean(3, url_censoring);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertChannel_ConfURLCensoring Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertChannel_ConfTXTCensoring(long channel_id, long guild_id, boolean txt_removal) {
		logger.trace("SQLInsertChannel_ConfTXTCensoring launched. Passed params {}, {}, {}, {}", channel_id, guild_id, txt_removal);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO channel_conf (fk_channel_id, fk_guild_id, url_censoring, txt_removal) VALUES (?, ?, 0, ?) ON DUPLICATE KEY UPDATE txt_removal = VALUES(txt_removal)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, channel_id);
			stmt.setLong(2, guild_id);
			stmt.setBoolean(3, txt_removal);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertChannel_ConfTXTCensoring Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteChannelType(String channel_type, long guild_id) {
		logger.trace("SQLDeleteChannelType launched. Passed params {}, {}", channel_type, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM channel_conf WHERE fk_channel_type = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, channel_type);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteChannelType Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static synchronized ArrayList<Channels> SQLgetChannels(long guild_id) {
		final var cachedChannels = Hashes.getChannels(guild_id);
		if(cachedChannels == null) {
			logger.trace("SQLgetChannels launched. Passed params {}", guild_id);
			ArrayList<Channels> channels = new ArrayList<Channels>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT * FROM all_channels WHERE guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					Channels channelProperties = new Channels();
					channelProperties.setChannel_ID(rs.getLong(1));
					channelProperties.setChannel_Name(rs.getString(2));
					channelProperties.setChannel_Type(rs.getString(3));
					channelProperties.setChannel_Type_Name(rs.getString(4));
					channelProperties.setGuild_ID(rs.getLong(5));
					channelProperties.setGuild_Name(rs.getString(6));
					channelProperties.setLang_Filter(rs.getString(7));
					channelProperties.setURLCensoring(rs.getBoolean(8));
					channelProperties.setTextRemoval(rs.getBoolean(9));
					channels.add(channelProperties);
				}
				Hashes.addChannels(guild_id, channels);
				return channels;
			} catch (SQLException e) {
				logger.error("SQLgetChannels Exception", e);
				return null;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return cachedChannels;
	}
	
	public static ArrayList<Channels> SQLgetChannelTypes() {
		logger.trace("SQLgetChannelTypes launched. No params");
		ArrayList<Channels> channels = new ArrayList<Channels>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM channeltypes");
			stmt = myConn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while(rs.next()) {
				Channels channelProperties = new Channels();
				channelProperties.setChannel_Type(rs.getString(1));
				channelProperties.setChannel_Type_Name(rs.getString(2));
				channels.add(channelProperties);
			}
			return channels;
		} catch (SQLException e) {
			logger.error("SQLgetChannelTypes Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLgetCommandExecutionReaction(long guild_id) {
		logger.trace("SQLgetCommandExecutionReaction launched. Passed params {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT reactions FROM guild WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getBoolean(1);
			}
			return false;
		} catch (SQLException e) {
			logger.error("SQLgetCommandExecutionReaction Exception", e);
			return false;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateReaction(long guild_id, boolean reactions) {
		logger.trace("SQLUpdateReaction launched. Passed params {}, {}", guild_id, reactions);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE guild SET reactions = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setBoolean(1, reactions);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateReaction Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public synchronized static ArrayList<String> SQLgetChannel_Filter(long channel_id) {
		final var censor = Hashes.getFilterLang(channel_id);
		if(censor == null) {
			logger.trace("SQLgetChannel_Filter launched. Passed params {}", channel_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT * FROM channel_filter WHERE fk_channel_id = ?");
				ArrayList<String> filter_lang = new ArrayList<String>();
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, channel_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					filter_lang.add(rs.getString(2));
				}
				Hashes.addFilterLang(channel_id, filter_lang);
				return filter_lang;
			} catch (SQLException e) {
				logger.error("SQLgetChannel_Filter Exception", e);
				return null;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return censor;
	}
	
	public synchronized static ArrayList<String> SQLgetFilter(String filter_lang, long guild_id) {
		final var query = Hashes.getQuerryResult(filter_lang+"_"+guild_id);
		if(query == null) {
			logger.trace("SQLgetFilter launched. Passed params {}, {}", filter_lang, guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				ArrayList<String> filter_words = new ArrayList<String>();
				String sql;
				if(filter_lang.equals("all")) {
					sql = ("SELECT word FROM filter WHERE fk_guild_id = ?");
					stmt = myConn.prepareStatement(sql);
					stmt.setLong(1, guild_id);
				}
				else{
					sql = ("SELECT word FROM filter WHERE fk_lang_abbrv = ? && fk_guild_id = ?");
					stmt = myConn.prepareStatement(sql);
					stmt.setString(1, filter_lang);
					stmt.setLong(2, guild_id);
				}
				rs = stmt.executeQuery();
				while(rs.next()) {
					filter_words.add(rs.getString(1));
				}
				Hashes.addQuerryResult(filter_lang+"_"+guild_id, filter_words);
				return filter_words;
			} catch (SQLException e) {
				logger.error("SQLgetFilter Exception", e);
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return query;
	}
	
	public static int SQLInsertWordFilter(String lang, String word, long guild_id) {
		logger.trace("SQLInsertWordFilter launched. Passed params {}, {}, {}", lang, word, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			var sql = "";
			if(!lang.equals("all"))
				sql = ("INSERT IGNORE INTO filter (word, fk_lang_abbrv, fk_guild_id) VALUES(?, ?, ?)");
			else {
				sql = ("INSERT IGNORE INTO filter (word, fk_lang_abbrv, fk_guild_id) VALUES"
						+ "(?, \"eng\", ?),"
						+ "(?, \"ger\", ?),"
						+ "(?, \"fre\", ?),"
						+ "(?, \"tur\", ?),"
						+ "(?, \"rus\", ?),"
						+ "(?, \"spa\", ?),"
						+ "(?, \"por\", ?),"
						+ "(?, \"ita\", ?)");
			}
			stmt = myConn.prepareStatement(sql);
			if(!lang.equals("all")) {
				stmt.setString(1, word.toLowerCase());
				stmt.setString(2, lang);
				stmt.setLong(3, guild_id);
			}
			else {
				word = word.toLowerCase();
				stmt.setString(1, word);
				stmt.setLong(2, guild_id);
				stmt.setString(3, word);
				stmt.setLong(4, guild_id);
				stmt.setString(5, word);
				stmt.setLong(6, guild_id);
				stmt.setString(7, word);
				stmt.setLong(8, guild_id);
				stmt.setString(9, word);
				stmt.setLong(10, guild_id);
				stmt.setString(11, word);
				stmt.setLong(12, guild_id);
				stmt.setString(13, word);
				stmt.setLong(14, guild_id);
				stmt.setString(15,word);
				stmt.setLong(16, guild_id);
			}
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertWordFilter Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLDeleteWordFilterAllLang(String word, long guild_id) {
		logger.trace("SQLDeleteWordFilter launched. Passed params {}, {}", word, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM filter WHERE word = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, word);
			stmt.setLong(2, guild_id);
			stmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			logger.error("SQLDeleteWordFilter Exception", e);
			return false;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteWordFilter(String lang, String word, long guild_id) {
		logger.trace("SQLDeleteWordFilter launched. Passed params {}, {}, {}", lang, word, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM filter WHERE word = ? "+(!lang.equals("all") ? "&& fk_lang_abbrv = ? " : "")+"&& fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, word);
			if(!lang.equals("all")) {
				stmt.setString(2, lang);
				stmt.setLong(3, guild_id);
			}
			else
				stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteWordFilter Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public synchronized static ArrayList<String> SQLgetStaffNames(long guild_id) {
		final var query = Hashes.getQuerryResult("staff-names_"+guild_id);
		if(query == null) {
			logger.trace("SQLgetStaffNames launched. Passed params {}", guild_id);
			ArrayList<String> staff_names = new ArrayList<String>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT name FROM staff_name_filter WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					staff_names.add(rs.getString(1));
				}
				Hashes.addQuerryResult("staff-names_"+guild_id, staff_names);
				return staff_names;
			} catch (SQLException e) {
				logger.error("SQLgetStaffNames Exception", e);
				return staff_names;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return query;
	}
	
	public static int SQLInsertStaffName(String word, long guild_id) {
		logger.trace("SQLInsertStaffName launched. Passed params {}, {}", word, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO staff_name_filter (name, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, word.toLowerCase());
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertStaffName Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteStaffNames(String word, long guild_id) {
		logger.trace("SQLDeleteStaffNames launched. Passed params {}, {}", word, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM staff_name_filter WHERE name = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, word.toLowerCase());
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteStaffNames Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLReplaceStaffNames(String [] words, long guild_id, boolean delete) {
		logger.trace("SQLBatchInsertStaffNames launched. Passed params array, {}, {}", guild_id, delete);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			if(delete) {
				String sql = ("DELETE FROM staff_name_filter WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, guild_id);
				stmt.executeUpdate();
			}
			
			String sql2 = ("INSERT IGNORE INTO staff_name_filter (name, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql2);
			for(String word : words) {
				stmt.setString(1, word.toLowerCase());
				stmt.setLong(2, guild_id);
				stmt.addBatch();
			}
			stmt.executeBatch();
			myConn.commit();
			return 0;
		} catch (SQLException e) {
			try {
				logger.error("SQLBatchInsertStaffNames Exception", e);
				myConn.rollback();
				return 1;
			} catch (SQLException e1) {
				logger.error("SQLBatchInsertStaffNames roll back Exception", e1);
				return 2;
			}
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertChannel_Filter(long channel_id, String filter_lang) {
		logger.trace("SQLInsertChannel_Filter launched. Passed params {}, {}", channel_id, filter_lang);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO channel_filter (fk_channel_id, fk_lang_abbrv) VALUES (?,?) ON DUPLICATE KEY UPDATE fk_lang_abbrv = VALUES(fk_lang_abbrv)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, channel_id);
			stmt.setString(2, filter_lang);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertChannel_Filter Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteChannel_Filter(long channel_id) {
		logger.trace("SQLDeleteChannel_Filter launched. Passed params {}", channel_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM channel_filter WHERE fk_channel_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, channel_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteChannel_Filter Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetFunnyNames(long guild_id) {
		final var query = Hashes.getQuerryResult("funny-names_"+guild_id);
		if(query == null) {
			ArrayList<String> names = new ArrayList<String>();
			logger.trace("SQLgetFunnyNames launched. Passed params {}", guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT name FROM names WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					names.add(rs.getString(1));
				}
				Hashes.addQuerryResult("funny-names_"+guild_id, names);
				return names;
			} catch (SQLException e) {
				logger.error("SQLgetFunnyNames Exception", e);
				return names;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return query;
	}
	
	public static int SQLInsertFunnyNames(String word, long guild_id) {
		logger.trace("SQLInsertFunnnynames launched. Passed params {}, {}", word, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO names (name, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, word);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertFunnyNamesException", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteFunnyNames(String word, long guild_id) {
		logger.trace("SQLDeleteFunnyNames launched. Passed params {}, {}", word, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM names WHERE name = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, word);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteFunnyNames Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<NameFilter> SQLgetNameFilter(long guild_id) {
		final var namesFilter = Hashes.getNameFilter(guild_id); 
		if(namesFilter == null) {
			logger.trace("SQLgetNameFilter launched. Passed params {}", guild_id);
			ArrayList<NameFilter> names = new ArrayList<NameFilter>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT word, kick FROM name_filter WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					names.add(new NameFilter(rs.getString(1), rs.getBoolean(2)));
				}
				Hashes.addNameFilter(guild_id, names);
				return names;
			} catch (SQLException e) {
				logger.error("SQLgetNameFilter Exception", e);
				return names;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return namesFilter;
	}
	
	public static int SQLInsertNameFilter(String word, boolean kick, long guild_id) {
		logger.trace("SQLInsertNameFilter launched. Passed params {}, {}, {}", word, kick, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO name_filter (word, kick, fk_guild_id) VALUES(?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, word.toLowerCase());
			stmt.setBoolean(2, kick);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertNameFilter Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteNameFilter(String word, boolean kick, long guild_id) {
		logger.trace("SQLDeleteNameFilter launched. Passed params {}, {}, {}", word, kick, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM name_filter WHERE word = ? && fk_guild_id = ? && kick = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, word);
			stmt.setLong(2, guild_id);
			stmt.setBoolean(3, kick);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteNameFilter Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static String SQLgetRandomName(long guild_id) {
		logger.trace("SQLgetRandomName launched. Passed params {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT name FROM names WHERE fk_guild_id = ? ORDER BY RAND() LIMIT 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getString(1);
			}
			return "Nickname";
		} catch (SQLException e) {
			logger.error("SQLgetRandomName Exception", e);
			return "Nickname";
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetFilterLanguages(String lang) {
		final var languages = Hashes.getFilterLang(0);
		if(languages == null) {
			logger.trace("SQLgetFilterLanguages launched. No params passed");
			ArrayList<String> filter_lang = new ArrayList<String>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT translation FROM languages_translation WHERE lang2 = ? AND lang != \"all\"");
				stmt = myConn.prepareStatement(sql);
				stmt.setString(1, lang);
				rs = stmt.executeQuery();
				while(rs.next()) {
					filter_lang.add(rs.getString(1));
				}
				Hashes.addFilterLang(0, filter_lang);
				return filter_lang;
			} catch (SQLException e) {
				logger.error("SQLgetFilterLanguages Exception", e);
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return languages;
	}
	
	public static int SQLInsertRSS(String url, long guild_id, int type) {
		logger.trace("SQLInsertRSS launched. Params passed {}, {}, {}", url, guild_id, type);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT IGNORE INTO rss (url, fk_guild_id, format, type, videos, pictures, text) VALUES (?, ?, ?, ?, 1, 1, 1)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, url);
			stmt.setLong(2, guild_id);
			if(type == 1)
				stmt.setString(3, "{pubDate} | {title}\n{description}\n{link}");
			else if(type == 2)
				stmt.setString(3, "From: **{fullName} {username}**\n{description}");
			stmt.setInt(4, type);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertRSS Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetSubTweets(long guild_id, String tweet) {
		logger.trace("SQLgetSubTweets launched. Params passed {}, {}", guild_id, tweet);
		ArrayList<String> tweets = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT child_tweet FROM sub_tweets WHERE fk_guild_id = ? && parent_tweet = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, guild_id);
			stmt.setString(2, tweet);
			rs = stmt.executeQuery();
			while(rs.next()) {
				tweets.add(rs.getString(1));
			}
			return tweets;
		} catch (SQLException e) {
			logger.error("SQLgetSubTweets Exception", e);
			return tweets;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<RSS> SQLgetSubscriptions(long guild_id) {
		final var feed = Hashes.getFeed(guild_id);
		if(feed == null) {
			logger.trace("SQLgetSubscriptions launched. Params passed {}", guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				ArrayList<RSS> feeds = new ArrayList<RSS>();
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT url, format, type, videos, pictures, text, channel_id FROM rss WHERE fk_guild_id = ? ORDER BY timestamp asc");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					feeds.add(
						new RSS(
							rs.getString(1),
							rs.getString(2),
							rs.getInt(3),
							rs.getBoolean(4),
							rs.getBoolean(5),
							rs.getBoolean(6),
							rs.getLong(7),
							SQLgetSubTweets(guild_id, rs.getString(1))
						)
					);
				}
				Hashes.addFeeds(guild_id, feeds);
				return feeds;
			} catch (SQLException e) {
				logger.error("SQLgetSubscriptions Exception", e);
				return null;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return feed;
	}
	
	public static ArrayList<RSS> SQLgetSubscriptions(long guild_id, int type) {
		logger.trace("SQLgetSubscriptions launched. Params passed {}, {}", guild_id, type);
		ArrayList<RSS> feeds = new ArrayList<RSS>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT url, format, type, videos, pictures, text, channel_id FROM rss WHERE fk_guild_id = ? AND type = ? ORDER BY timestamp asc");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, type);
			rs = stmt.executeQuery();
			while(rs.next()) {
				feeds.add(
					new RSS(
						rs.getString(1),
						rs.getString(2),
						rs.getInt(3),
						rs.getBoolean(4),
						rs.getBoolean(5),
						rs.getBoolean(6),
						rs.getLong(7),
						SQLgetSubTweets(guild_id, rs.getString(1))
					)
				);
			}
			Hashes.addFeeds(guild_id, feeds);
			return feeds;
		} catch (SQLException e) {
			logger.error("SQLgetSubscriptions Exception", e);
			return feeds;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateRSSPictures(String url, long guild_id, boolean option) {
		logger.trace("SQLUpdateRSSPictures launched. Params passed {}, {}, {}", url, guild_id, option);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE rss SET pictures = ? WHERE url = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setBoolean(1, option);
			stmt.setString(2, url);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateRSSPictures Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateRSSVideos(String url, long guild_id, boolean option) {
		logger.trace("SQLUpdateRSSVideos launched. Params passed {}, {}, {}", url, guild_id, option);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE rss SET videos = ? WHERE url = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setBoolean(1, option);
			stmt.setString(2, url);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateRSSVideos Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateRSSText(String url, long guild_id, boolean option) {
		logger.trace("SQLUpdateRSSText launched. Params passed {}, {}, {}", url, guild_id, option);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE rss SET text = ? WHERE url = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setBoolean(1, option);
			stmt.setString(2, url);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateRSSText Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertChildTweet(String urlParent, String urlChild, long guild_id) {
		logger.trace("SQLInsertChildTweet launched. Params passed {}, {}, {}", urlParent, urlChild, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT IGNORE INTO sub_tweets (parent_tweet, child_tweet, fk_guild_id) VALUES(?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, urlParent);
			stmt.setString(2, urlChild);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertChildTweet Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteChildTweet(String urlParent, String urlChild, long guild_id) {
		logger.trace("SQLDeleteChildTweet launched. Params passed {}, {}, {}", urlParent, urlChild, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM sub_tweets WHERE parent_tweet = ? AND child_tweet = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, urlParent);
			stmt.setString(2, urlChild);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteChildTweet Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteRSSFeed(String url, long guild_id) {
		logger.trace("SQLDeleteRSSFeed launched. Params passed {}, {}", url, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM rss WHERE url = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, url);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteRSSFeed Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateRSSFormat(String url, long guild_id, String format) {
		logger.trace("SQLUpdateRSSFormat launched. Params passed {}, {}, {}", url, guild_id, format);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE rss SET format = ? WHERE url = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, format);
			stmt.setString(2, url);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateRSSFormat Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateRSSChannel(String url, long guild_id, long channel_id) {
		logger.trace("SQLUpdateRSSChannel launched. Params passed {}, {}, {}", url, guild_id, channel_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE rss SET channel_id = ? WHERE url = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, channel_id);
			stmt.setString(2, url);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateRSSChannel Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static synchronized ArrayList<String> SQLgetURLBlacklist(long guild_id) {
		final var blacklist = Hashes.getURLBlacklist(guild_id); 
		if(blacklist == null) {
			logger.trace("SQLgetURLBlacklist launched. Passed params {}", guild_id);
			ArrayList<String> urls = new ArrayList<String>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT url FROM url_blacklist WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					urls.add(rs.getString(1));
				}
				Hashes.addURLBlacklist(guild_id, urls);
				return urls;
			} catch (SQLException e) {
				logger.error("SQLgetURLBlacklist Exception", e);
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return blacklist;
	}
	
	public static int SQLInsertURLBlacklist(String url, long guild_id) {
		logger.trace("SQLInsertURLBlacklist launched. Passed params {}, {}", url, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO url_blacklist (url, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, url.replaceAll("(http:\\/\\/|https:\\/\\/)", "").replaceAll("www.", "").replace("\\b\\/[\\w\\d=?!&#\\[\\]().,+_*';:@$\\/-]*\\b", ""));
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertURLBlacklist Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteURLBlacklist(String url, long guild_id) {
		logger.trace("SQLDeleteURLBlacklist launched. Passed params {}, {}", url, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM url_blacklist WHERE url = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, url.replaceAll("(http:\\/\\/|https:\\/\\/)", "").replaceAll("www.", "").replace("\\b\\/[\\w\\d=?!&#\\[\\]().,+_*';:@$\\/-]*\\b", ""));
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteURLBlacklist Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static synchronized ArrayList<String> SQLgetURLWhitelist(long guild_id) {
		final var whitelist = Hashes.getURLWhitelist(guild_id);
		if(whitelist == null) {
			logger.trace("SQLgetURLWhitelist launched. Passed params {}", guild_id);
			ArrayList<String> urls = new ArrayList<String>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT url FROM url_whitelist WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					urls.add(rs.getString(1));
				}
				Hashes.addURLWhitelist(guild_id, urls);
				return urls;
			} catch (SQLException e) {
				logger.error("SQLgetURLWhitelist Exception", e);
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return whitelist;
	}
	
	public static int SQLInsertURLWhitelist(String url, long guild_id) {
		logger.trace("SQLInsertURLWhitelist launched. Passed params {}, {}", url, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO url_whitelist (url, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, url.replaceAll("(http:\\/\\/|https:\\/\\/)", "").replaceAll("www.", "").replaceAll("\\b\\/[\\w\\d=?!&#\\[\\]().,+_*';:@$\\/-]*\\b", ""));
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertURLWhitelist Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteURLWhitelist(String url, long guild_id) {
		logger.trace("SQLDeleteURLWhitelist launched. Passed params {}, {}", url, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM url_whitelist WHERE url = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, url.replaceAll("(http:\\/\\/|https:\\/\\/)", "").replaceAll("www.", "").replace("\\b\\/[\\w\\d=?!&#\\[\\]().,+_*';:@$\\/-]*\\b", ""));
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteURLWhitelist Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static synchronized ArrayList<String> SQLgetTweetBlacklist(long guild_id) {
		final var blacklist = Hashes.getTweetBlacklist(guild_id);
		if(blacklist == null) {
			logger.trace("SQLgetTweetBlacklist launched. Passed params {}", guild_id);
			ArrayList<String> urls = new ArrayList<String>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT username FROM tweet_blacklist WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					urls.add(rs.getString(1));
				}
				Hashes.addTweetBlacklist(guild_id, urls);
				return urls;
			} catch (SQLException e) {
				logger.error("SQLgetTweetBlacklist Exception", e);
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return blacklist;
	}
	
	public static int SQLInsertTweetBlacklist(String username, long guild_id) {
		logger.trace("SQLInsertTweetBlacklist launched. Passed params {}, {}", username, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO tweet_blacklist (username, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, (username.startsWith("@") ? username : "@"+username));
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertTweetBlacklist Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteTweetBlacklist(String username, long guild_id) {
		logger.trace("SQLDeleteTweetBlacklist launched. Passed params {}, {}", username, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM tweet_blacklist WHERE username = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, (username.startsWith("@") ? username : "@"+username));
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteTweetBlacklist Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static synchronized Watchlist SQLgetWatchlist(long user_id, long guild_id) {
		final var cachedWatchlist = Hashes.getWatchlist(guild_id+"-"+user_id);
		if(cachedWatchlist == null) {
			logger.trace("SQLgetWatchlist launched. Params passed {}, {}", user_id, guild_id);
			Watchlist watchlist = null;
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT * FROM watchlist WHERE fk_user_id = ? AND fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, user_id);
				stmt.setLong(2, guild_id);
				rs = stmt.executeQuery();
				if(rs.next()) {
					watchlist = new Watchlist(rs.getInt(3), rs.getLong(4), rs.getBoolean(5));
					Hashes.addWatchlist(rs.getString(2)+"-"+rs.getString(1), watchlist);
				}
				return watchlist;
			} catch (SQLException e) {
				logger.error("SQLgetWatchlist Exception", e);
				return watchlist;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return cachedWatchlist;
	}
	
	public static synchronized void SQLgetWholeWatchlist() {
		logger.trace("SQLgetWholeWatchlist launched. No params have been passed!");
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM watchlist");
			stmt = myConn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while(rs.next()) {
				Hashes.addWatchlist(rs.getString(2)+"-"+rs.getString(1), new Watchlist(rs.getInt(3), rs.getLong(4), rs.getBoolean(5)));
			}
		} catch (SQLException e) {
			logger.error("SQLgetWholeWatchlist Exception", e);
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetWholeWatchlist(long guild_id, boolean highPrivileges) {
		logger.trace("SQLgetWholeWatchlist launched. Params passed {}, {}", guild_id, highPrivileges);
		ArrayList<String> watchlist = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM watchlist WHERE fk_guild_id = ? && higher_privileges = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, guild_id);
			stmt.setBoolean(2, highPrivileges);
			rs = stmt.executeQuery();
			while(rs.next()) {
				watchlist.add(rs.getString(1)+" ("+rs.getString(2)+") Watch Level "+rs.getString(3));
			}
			return watchlist;
		} catch (SQLException e) {
			logger.error("SQLgetWholeWatchlist Exception", e);
			return watchlist;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertWatchlist(long user_id, long guild_id, int level, long watchChannel, boolean higherPrivileges) {
		logger.trace("SQLInsertWatchlist launched. Passed params {}, {}, {}, {}, {}", user_id, guild_id, level, watchChannel, higherPrivileges);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO watchlist (fk_user_id, fk_guild_id, level, watch_channel, higher_privileges) VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE level=VALUES(level), watch_channel=VALUES(watch_channel), higher_privileges=VALUES(higher_privileges)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			stmt.setInt(3, level);
			stmt.setLong(4, watchChannel);
			stmt.setBoolean(5, higherPrivileges);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertWatchlist Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteWatchlist(long user_id, long guild_id) {
		logger.trace("SQLDeleteWatchlist launched. Passed params {}, {}", user_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM watchlist WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteWatchlist Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static List<GoogleAPISetup> SQLgetGoogleAPISetupOnGuildAndAPI(long guild_id, int api_id) {
		logger.trace("SQLgetGoogleAPISetupOnGuild launched. Params passed {}, {}", guild_id, api_id);
		ArrayList<GoogleAPISetup> setup = new ArrayList<GoogleAPISetup>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM google_apis_setup_view WHERE fk_guild_id = ? AND fk_api_id = ? ORDER BY timestamp asc");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, api_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				setup.add(new GoogleAPISetup(rs.getString(1), rs.getString(3), rs.getInt(4), rs.getString(5)));
			}
			return setup;
		} catch (SQLException e) {
			logger.error("SQLgetGoogleAPISetupOnGuild Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertGoogleAPISetup(String file_id, long guild_id, String title, int api_id) {
		logger.trace("SQLInsertGoogleAPISetup launched. Passed params {}, {}, {}, {}", file_id, guild_id, title, api_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO google_apis_setup (file_id, fk_guild_id, title, fk_api_id) VALUES(?, ?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, file_id);
			stmt.setLong(2, guild_id);
			stmt.setString(3, title);
			stmt.setInt(4, api_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertGoogleAPISetup Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteGoogleAPISetup(String file_id, long guild_id) {
		logger.trace("SQLDeleteGoogleAPISetup launched. Passed params {}, {}, {}, {}", file_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM google_apis_setup WHERE file_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, file_id);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteGoogleAPISetup Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Integer> SQLgetGoogleLinkedEvents(String file_id, long guild_id) {
		logger.trace("SQLgetGoogleLinkedEvents launched. Params passed {}, {}", file_id, guild_id);
		ArrayList<Integer> events = new ArrayList<Integer>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT fk_event_id FROM google_file_to_event WHERE fk_file_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, file_id);
			stmt.setLong(2, guild_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				events.add(rs.getInt(1));
			}
			return events;
		} catch (SQLException e) {
			logger.error("SQLgetGoogleLinkedEvents Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Integer> SQLgetGoogleLinkedEventsRestrictions(String file_id, long guild_id) {
		logger.trace("SQLgetGoogleLinkedEvents launched. Params passed {}, {}", file_id, guild_id);
		ArrayList<Integer> events = new ArrayList<Integer>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT fk_event_id FROM google_file_to_event INNER JOIN google_event_types ON fk_event_id = event_id WHERE fk_file_id = ? AND fk_guild_id = ? AND restrictable = 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, file_id);
			stmt.setLong(2, guild_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				events.add(rs.getInt(1));
			}
			return events;
		} catch (SQLException e) {
			logger.error("SQLgetGoogleLinkedEvents Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<GoogleEvents> SQLgetGoogleEventsSupportSpreadsheet() {
		logger.trace("SQLgetGoogleEventsSupportSpreadsheet launched. No params passed");
		ArrayList<GoogleEvents> events = new ArrayList<GoogleEvents>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM google_event_types WHERE support_spreadsheets = 1");
			stmt = myConn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while(rs.next()) {
				events.add(new GoogleEvents(rs.getInt(1), rs.getString(2), rs.getBoolean(3), rs.getBoolean(4), rs.getBoolean(5), rs.getBoolean(6), rs.getBoolean(7)));
			}
			return events;
		} catch (SQLException e) {
			logger.error("SQLgetGoogleEventsSupportSpreadsheet Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLBatchInsertGoogleFileToEventLink(String file_id, long guild_id, List<Integer> events) {
		logger.trace("SQLBatchInsertGoogleFileToEventLink launched. Passed params {}, {}, array", file_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO google_file_to_event (fk_guild_id, fk_file_id, fk_event_id) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE fk_event_id=VALUES(fk_event_id)");
			stmt = myConn.prepareStatement(sql);
			for(final int event: events) {
				stmt.setLong(1, guild_id);
				stmt.setString(2, file_id);
				stmt.setInt(3, event);
				stmt.addBatch();
			}
			stmt.executeBatch();
			return true;
		} catch (SQLException e) {
			logger.error("SQLBatchInsertGoogleFileToEventLink Exception", e);
			return false;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateGoogleChannelRestriction(long guild_id, String file_id, int event_id, long channel_id) {
		logger.trace("SQLUpdateGoogleChannelRestriction launched. Passed params {}, {}, {}, {}", guild_id, file_id, event_id, channel_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE google_file_to_event SET channel_id = ? WHERE fk_guild_id = ? AND fk_file_id = ? AND fk_event_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, channel_id);
			stmt.setLong(2, guild_id);
			stmt.setString(3, file_id);
			stmt.setInt(4, event_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateGoogleChannelRestriction Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateGoogleRemoveChannelRestriction(long guild_id, String file_id, int event_id) {
		logger.trace("SQLUpdateGoogleRemoveChannelRestriction launched. Passed params {}, {}, {}", guild_id, file_id, event_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE google_file_to_event SET channel_id = ? WHERE fk_guild_id = ? AND fk_file_id = ? AND fk_event_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setNull(1, Types.BIGINT);
			stmt.setLong(2, guild_id);
			stmt.setString(3, file_id);
			stmt.setInt(4, event_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateGoogleRemoveChannelRestriction Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLBatchDeleteGoogleSpreadsheetSheet(String file_id, long guild_id, List<Integer> events) {
		logger.trace("SQLBatchDeleteGoogleSpreadsheetSheet launched. Passed params {}, {}, array", file_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM google_spreadsheet_sheet WHERE fk_file_id = ? AND fk_event_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			for(final int event: events) {
				stmt.setString(1, file_id);
				stmt.setInt(2, event);
				stmt.setLong(3, guild_id);
				stmt.addBatch();
			}
			stmt.executeBatch();
			return true;
		} catch (SQLException e) {
			logger.error("SQLBatchDeleteGoogleSpreadsheetSheet Exception", e);
			return false;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertGoogleSpreadsheetSheet(String file_id, int event_id, String row_start, long guild_id) {
		logger.trace("SQLInsertGoogleSpreadsheetSheet launched. Passed params {}, {}, {}, {}", file_id, event_id, row_start, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO google_spreadsheet_sheet (fk_guild_id, fk_file_id, fk_event_id, sheet_row_start) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE sheet_row_start=VALUES(sheet_row_start)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, guild_id);
			stmt.setString(2, file_id);
			stmt.setInt(3, event_id);
			stmt.setString(4, row_start);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertGoogleSpreadsheetSheet Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<GoogleSheet> SQLgetGoogleSpreadsheetSheets(String file_id, long guild_id) {
		logger.trace("SQLgetGoogleSpreadsheetSheets launched. Params passed {}, {}", file_id, guild_id);
		ArrayList<GoogleSheet> sheets = new ArrayList<GoogleSheet>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT fk_event_id, sheet_row_start FROM google_spreadsheet_sheet WHERE fk_file_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, file_id);
			stmt.setLong(2, guild_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				sheets.add(new GoogleSheet(GoogleEvent.valueOfId(rs.getInt(1)), rs.getString(2)));
			}
			return sheets;
		} catch (SQLException e) {
			logger.error("SQLgetGoogleSpreadsheetSheets Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteGoogleSpreadsheetSheet(String file_id, long guild_id) {
		logger.trace("SQLDeleteGoogleSpreadsheetSheet launched. Passed params {}, {}", file_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM google_spreadsheet_sheet WHERE fk_file_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, file_id);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteGoogleSpreadsheetSheet Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLBatchDeleteGoogleSpreadsheetMapping(String file_id, long guild_id, List<Integer> events) {
		logger.trace("SQLBatchDeleteGoogleSpreadsheetMapping launched. Passed params {}, {} array", file_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM google_spreadsheet_mapping WHERE fk_file_id = ? AND fk_event_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			for(final int event: events) {
				stmt.setString(1, file_id);
				stmt.setInt(2, event);
				stmt.setLong(3, guild_id);
				stmt.addBatch();
			}
			stmt.executeBatch();
			return true;
		} catch (SQLException e) {
			logger.error("SQLBatchDeleteGoogleSpreadsheetMapping Exception", e);
			return false;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteGoogleSpreadsheetMapping(String file_id, long guild_id) {
		logger.trace("SQLDeleteGoogleSpreadsheetMapping launched. Passed params {}, {}", file_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM google_spreadsheet_mapping WHERE fk_file_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, file_id);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteGoogleSpreadsheetMapping Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLBatchDeleteGoogleFileToEvent(String file_id, long guild_id, List<Integer> events) {
		logger.trace("SQLBatchDeleteGoogleFileToEvent launched. Passed params {}, {}, array", file_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM google_file_to_event WHERE fk_file_id = ? AND fk_event_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			for(final int event: events) {
				stmt.setString(1, file_id);
				stmt.setInt(2, event);
				stmt.setLong(3, guild_id);
				stmt.addBatch();
			}
			stmt.executeBatch();
			return true;
		} catch (SQLException e) {
			logger.error("SQLBatchDeleteGoogleFileToEvent Exception", e);
			return false;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteGoogleFileToEvent(String file_id, long guild_id) {
		logger.trace("SQLDeleteGoogleFileToEvent launched. Passed params {}, {}", file_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM google_file_to_event WHERE fk_file_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, file_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteGoogleFileToEvent Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetGoogleEventsToDD(int api_id, int event_id) {
		logger.trace("SQLgetGoogleEventsToDD launched. Params passed {}, {}", api_id, event_id);
		ArrayList<String> items = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM google_events_to_dd WHERE fk_api_id = ? AND fk_event_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, api_id);
			stmt.setInt(2, event_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				items.add(GoogleDD.valueOfId(rs.getInt(3)).item);
			}
			return items;
		} catch (SQLException e) {
			logger.error("SQLgetGoogleEventsToDD Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteGoogleSpreadsheetMapping(String file_id, int event_id, long guild_id) {
		logger.trace("SQLDeleteGoogleSpreadsheetMapping launched. Passed params {}, {}, {}", file_id, event_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM google_spreadsheet_mapping WHERE fk_file_id = ? AND fk_event_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, file_id);
			stmt.setInt(2, event_id);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteGoogleSpreadsheetMapping Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int [] SQLBatchInsertGoogleSpreadsheetMapping(String file_id, int event_id, long guild_id, List<Integer> dd_items, List<String> dd_formats) {
		logger.trace("SQLBatchInsertGoogleSpreadsheetMapping launched. Passed params {}, {}, {}, array1, array2, array3", file_id, event_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO google_spreadsheet_mapping (fk_guild_id, fk_file_id, fk_event_id, column_number, fk_dd_id, format) VALUES(?, ?, ?, ?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			for(int columnNumber = 0; columnNumber < dd_items.size(); columnNumber++) {
				stmt.setLong(1, guild_id);
				stmt.setString(2, file_id);
				stmt.setInt(3, event_id);
				stmt.setInt(4, columnNumber+1);
				stmt.setInt(5, dd_items.get(columnNumber));
				stmt.setString(6, dd_formats.get(columnNumber));
				stmt.addBatch();
			}
			return stmt.executeBatch();
		} catch (SQLException e) {
			logger.error("SQLBatchInsertGoogleSpreadsheetMapping Exception", e);
			int[] val = { -1 };
			return val;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<GoogleSheetColumn> SQLgetGoogleSpreadsheetMapping(String file_id, int event_id, long guild_id) {
		logger.trace("SQLgetGoogleSpreadsheetMapping launched. Params passed {}, {}, {}", file_id, event_id, guild_id);
		ArrayList<GoogleSheetColumn> items = new ArrayList<GoogleSheetColumn>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT fk_dd_id, format, column_number FROM google_spreadsheet_mapping WHERE fk_file_id = ? AND fk_event_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, file_id);
			stmt.setInt(2, event_id);
			stmt.setLong(3, guild_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				items.add(new GoogleSheetColumn(rs.getInt(1), rs.getString(2), rs.getInt(3)));
			}
			return items;
		} catch (SQLException e) {
			logger.error("SQLgetGoogleSpreadsheetMapping Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static String [] SQLgetGoogleFilesAndEvent(long guild_id, int api_id, int event_id, String channel_id) {
		logger.trace("SQLgetGoogleFilesAndEvent launched. Params passed {}, {}, {}, {}", guild_id, api_id, event_id, channel_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT file_id, sheet_row_start, channel_id, force_restriction FROM google_files_and_events WHERE guild_id = ? AND api_id = ? AND event_id = ? AND (channel_id = ? OR channel_id IS NULL OR channel_id = 0) ORDER BY channel_id desc LIMIT 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, guild_id);
			stmt.setInt(2, api_id);
			stmt.setInt(3, event_id);
			stmt.setString(4, channel_id);
			rs = stmt.executeQuery();
			String [] array = new String [4];
			if(rs.next()) {
				array[0] = rs.getString(1);
				array[1] = rs.getString(2);
				array[2] = rs.getString(3);
				array[3] = rs.getString(4);
				return array;
			}
			array[0] = "empty";
			return array;
		} catch (SQLException e) {
			logger.error("SQLgetGoogleFilesAndEvent Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertReminder(long user_id, long guild_id, String type, String reason, String reporter, String time) {
		logger.trace("SQLInsertReminder launched. Passed params {}, {}, {}, {}, {}, {}", user_id, guild_id, type, reason, reporter, time);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO reminder (fk_user_id, fk_guild_id, type, reason, reporter, time) VALUES(?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE type=VALUES(type), reason=VALUES(reason), reporter=VALUES(reporter), time=VALUES(time)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			stmt.setString(3, type);
			stmt.setString(4, reason);
			stmt.setString(5, reporter);
			stmt.setString(6, time);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertReminder Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static RejoinTask SQLgetRejoinTask(long user_id, long guild_id) {
		logger.trace("SQLgetRejoinTask launched. Params passed {}, {}", user_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM reminder WHERE fk_user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return new RejoinTask(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6));
			}
			return new RejoinTask(0, 0, "", "", "", "");
		} catch (SQLException e) {
			logger.error("SQLgetRejoinTask Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteRejoinTask(long user_id, long guild_id) {
		logger.trace("SQLDeleteRejoinTask launched. Passed params {}, {}", user_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM reminder WHERE fk_user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteRejoinTask Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetLanguages(String lang) {
		logger.trace("SQLgetLanguages launched. Params passed {}", lang);
		ArrayList<String> langs = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT lang, translation FROM languages_translation WHERE lang2 = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, lang);
			rs = stmt.executeQuery();
			while(rs.next()) {
				langs.add(rs.getString(1)+"-"+rs.getString(2));
			}
			return langs;
		} catch (SQLException e) {
			logger.error("SQLgetLanguages Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetTranslatedLanguages(String lang) {
		logger.trace("SQLgetTranslatedLanguages launched. Params passed {}", lang);
		ArrayList<String> langs = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT lang, translation FROM languages_translation WHERE lang2 = ? AND translated = 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, lang);
			rs = stmt.executeQuery();
			while(rs.next()) {
				langs.add(rs.getString(1)+"-"+rs.getString(2));
			}
			return langs;
		} catch (SQLException e) {
			logger.error("SQLgetTranslatedLanguages Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLisLanguageTranslated(String lang, String lang2) {
		logger.trace("SQLisLanguageTranslated launched. Params passed {}, {}", lang, lang2);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM languages_translation WHERE lang = ? AND lang2 = ? AND translated = 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, lang);
			stmt.setString(2, lang2);
			rs = stmt.executeQuery();
			while(rs.next()) {
				return true;
			}
			return false;
		} catch (SQLException e) {
			logger.error("SQLisLanguageTranslated Exception", e);
			return false;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertTweetLog(long message_id, long tweet_id) {
		logger.trace("SQLInsertTweetLog launched. Passed params {}, {}", message_id, tweet_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO tweet_log (message_id, tweet_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, message_id);
			stmt.setLong(2, tweet_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertTweetLog Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateTweetLogDeleted(long message_id) {
		logger.trace("SQLUpdateTweetLogDeleted launched. Passed params {}", message_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE tweet_log SET deleted = 1 WHERE message_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, message_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateTweetLogDeleted Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLIsTweetDeleted(long tweet_id) {
		logger.trace("SQLIsTweetDeleted launched. Params passed {}", tweet_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT deleted FROM tweet_log WHERE tweet_id = ? AND deleted = 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, tweet_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				return true;
			}
			return false;
		} catch (SQLException e) {
			logger.error("SQLIsTweetDeleted Exception", e);
			return false;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateTweetTimestamp(long tweet_id) {
		logger.trace("SQLUpdateTweetLogDeleted launched. Passed params {}", tweet_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE tweet_log SET timestamp = SYSDATE() WHERE tweet_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, tweet_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateTweetLogDeleted Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteTweetLog() {
		logger.trace("SQLDeleteTweetLog launched. No params passed");
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM tweet_log WHERE DATE_ADD(timestamp, INTERVAL 7 DAY) <= SYSDATE()");
			stmt = myConn.prepareStatement(sql);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteTweetLog Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLBulkInsertCategories(List<Category> categories) {
		logger.trace("SQLBulkInsertCategories launched. Array param passed");
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false); 
			String sql = ("INSERT INTO categories (category_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name)");
			stmt = myConn.prepareStatement(sql);
			for(final var category : categories) {
				stmt.setLong(1, category.getIdLong());
				stmt.setString(2, category.getName());
				stmt.addBatch();
			}
			stmt.executeBatch();
			myConn.commit();
		} catch (SQLException e) {
			logger.error("SQLBulkInsertCategories Exception", e);
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<CategoryConf> SQLgetCategories(long guild_id) {
		final var cachedCategories = Hashes.getCategories(guild_id);
		if(cachedCategories == null) {
			logger.trace("SQLgetCategories launched. Params passed {}", guild_id);
			ArrayList<CategoryConf> categories = new ArrayList<CategoryConf>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT fk_category_id, fk_category_type FROM category_conf WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					categories.add(new CategoryConf(
						rs.getLong(1),
						rs.getString(2)
					));
				}
				Hashes.addCategories(guild_id, categories);
				return categories;
			} catch (SQLException e) {
				logger.error("SQLgetCategories Exception", e);
				return null;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return cachedCategories;
	}
	
	public static int SQLInsertCategory(long category_id, String name) {
		logger.trace("SQLInsertCategory launched. Params passed {}, {}", category_id, name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO categories (category_id, name) VALUES(?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, category_id);
			stmt.setString(2, name);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertCategory Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteCategory(long category_id) {
		logger.trace("SQLDeleteCategory launched. Params passed {}", category_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM categories WHERE category_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, category_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteCategory Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateCategoryName(long category_id, String name) {
		logger.trace("SQLUpdateCategoryName launched. Params passed {}, {}", category_id, name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE categories SET name = ? WHERE category_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, name);
			stmt.setLong(2, category_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateCategoryName Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertCategoryConf(long category_id, String categoryType, long guild_id) {
		logger.trace("SQLInsertCategoryConf launched. Params passed {}, {}, {}", category_id, categoryType, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO category_conf(fk_category_id, fk_category_type, fk_guild_id) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE fk_category_type=VALUES(fk_category_type)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, category_id);
			stmt.setString(2, categoryType);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertCategoryConf Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteCategoryConf(long category_id) {
		logger.trace("SQLDeleteCategoryConf launched. Params passed {}", category_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM category_conf WHERE fk_category_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, category_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteCategoryConf Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteAllCategoryConfs(long guild_id) {
		logger.trace("SQLDeleteAllCategoryConf launched. Params passed {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM category_conf WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteAllCategoryConf Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<constructors.Category> SQLgetCategoryTypes() {
		logger.trace("SQLgetCategoryTypes launched. No params");
		ArrayList<constructors.Category> categories = new ArrayList<constructors.Category>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM category_types");
			stmt = myConn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while(rs.next()) {
				categories.add(new constructors.Category(
					rs.getString(1),
					rs.getString(2)
				));
			}
			return categories;
		} catch (SQLException e) {
			logger.error("SQLgetCategoryTypes Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static CustomCommand SQLgetCustomCommand(long guild_id, String command) {
		logger.trace("SQLgetCustomCommand launched. Params passed {}, {}", guild_id, command);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM custom_commands WHERE fk_guild_id = ? AND command = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, guild_id);
			stmt.setString(2, command);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return new CustomCommand(
					rs.getString(2),
					rs.getString(3),
					rs.getInt(4),
					rs.getString(5),
					rs.getInt(6),
					rs.getLong(7),
					rs.getBoolean(8)
				);
			}
			return null;
		} catch (SQLException e) {
			logger.error("SQLgetCustomCommand Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetCustomCommands(long guild_id) {
		logger.trace("SQLgetCustomCommands launched. Params passed {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			ArrayList<String> commands = new ArrayList<String>();
			String sql = ("SELECT command FROM custom_commands WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, guild_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				commands.add(guild_id+""+rs.getString(1));
			}
			return commands;
		} catch (SQLException e) {
			logger.error("SQLgetCustomCommands Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<CustomCommand> SQLgetCustomCommands2(long guild_id) {
		logger.trace("SQLgetCustomCommands2 launched. Params passed {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			ArrayList<CustomCommand> commands = new ArrayList<CustomCommand>();
			String sql = ("SELECT * FROM custom_commands WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, guild_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				commands.add(
					new CustomCommand(
						rs.getString(2),
						rs.getString(3),
						rs.getInt(4),
						rs.getString(5),
						rs.getInt(6),
						rs.getLong(7),
						rs.getBoolean(8)
					)
				);
			}
			return commands;
		} catch (SQLException e) {
			logger.error("SQLgetCustomCommands2 Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static HashSet<String> SQLgetCustomCommandRestrictions(long guild_id, String command) {
		logger.trace("SQLgetCustomCommandRestrictions launched. Params passed {}, {}", guild_id, command);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			HashSet<String> channels = new HashSet<String>();
			String sql = ("SELECT * FROM custom_commands_restrictions WHERE fk_guild_id = ? AND command = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, guild_id);
			stmt.setString(2, command);
			rs = stmt.executeQuery();
			while(rs.next()) {
				final String channelType = rs.getString(3);
				final String channelId = rs.getString(4);
				String channel = null;
				if(channelType != null)
					channel = channelType;
				else if(channelId != null)
					channel = channelId;
				if(channel != null && !channels.contains(channel))
					channels.add(channel);
			}
			return channels;
		} catch (SQLException e) {
			logger.error("SQLgetCustomCommandRestrictions Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Long> SQLgetCustomCommandRoles(long guild_id, String command) {
		logger.trace("SQLgetCustomCommandRoles launched. Params passed {}, {}", guild_id, command);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			ArrayList<Long> roles = new ArrayList<Long>();
			String sql = ("SELECT role_id FROM custom_commands_roles WHERE fk_guild_id = ? AND command = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, guild_id);
			stmt.setString(2, command);
			rs = stmt.executeQuery();
			while(rs.next()) {
				roles.add(rs.getLong(1));
			}
			return roles;
		} catch (SQLException e) {
			logger.error("SQLgetCustomCommandRoles Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//Transactions
	@SuppressWarnings("resource")
	public static int SQLLowerTotalWarning(long guild_id, int warning_id) {
		logger.trace("SQLLowerTotalWarning launched. Passed params {}, {}", guild_id, warning_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			String sql = ("UPDATE bancollect SET fk_warning_id = ? WHERE fk_guild_id = ? && fk_warning_id > ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, warning_id);
			stmt.setLong(2, guild_id);
			stmt.setInt(3, warning_id);
			stmt.executeUpdate();

			String sql2 = ("DELETE FROM warnings WHERE warning_id > ?");
			stmt = myConn.prepareStatement(sql2);
			stmt.setInt(1, warning_id);
			var editedRows = stmt.executeUpdate();
			myConn.commit();
			return editedRows;
		} catch (SQLException e) {
			logger.error("SQLLowerTotalWarning Exception", e);
			try {
				myConn.rollback();
			} catch (SQLException e1) {
				logger.error("SQLLowerTotalWarning  rollback Exception", e);
			}
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLReplaceWordFilter(String lang, String [] words, long guild_id, boolean delete) {
		logger.trace("SQLReplaceWordFilter launched. Passed params {}, array, {}, {}", lang, guild_id, delete);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			if(delete) {
				String sql = ("DELETE FROM filter WHERE fk_lang_abbrv = ? && fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setString(1, lang);
				stmt.setLong(2, guild_id);
				stmt.executeUpdate();
			}
			
			String sql2 = ("INSERT IGNORE INTO filter (word, fk_lang_abbrv, fk_guild_id) VALUES(?, ?, ?)");
			stmt = myConn.prepareStatement(sql2);
			
			for(String word : words) {
				stmt.setString(1, CharacterReplacer.simpleReplace(word.toLowerCase()));
				stmt.setString(2, lang);
				stmt.setLong(3, guild_id);
				stmt.addBatch();
			}
			stmt.executeBatch();
			myConn.commit();
			return 0;
		} catch (SQLException e) {
			logger.error("SQLReplaceWordFilter Exception", e);
			try {
				myConn.rollback();
				return 1;
			} catch (SQLException e1) {
				logger.error("SQLReplaceWordFilter rollback Exception", e);
				return 2;
			}
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLReplaceNameFilter(String [] words, boolean kick, long guild_id, boolean delete) {
		logger.trace("SQLReplaceNameFilter launched. Passed params array, {}, {}, {}", kick, guild_id, delete);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			if(delete) {
				String sql = ("DELETE FROM name_filter WHERE fk_guild_id = ? && kick = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, guild_id);
				stmt.setBoolean(2, kick);
				stmt.executeUpdate();
			}
			
			String sql2 = ("INSERT IGNORE INTO name_filter (word, kick, fk_guild_id) VALUES(?, ?, ?)");
			stmt = myConn.prepareStatement(sql2);
			
			for(String word : words) {
				stmt.setString(1, word);
				stmt.setBoolean(2, kick);
				stmt.setLong(3, guild_id);
				stmt.addBatch();
			}
			stmt.executeBatch();
			myConn.commit();
			return 0;
		} catch (SQLException e) {
			logger.error("SQLReplaceNameFilter Exception", e);
			try {
				myConn.rollback();
				return 1;
			} catch (SQLException e1) {
				logger.error("SQLReplaceNameFilter rollback Exception", e);
				return 2;
			}
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLReplaceFunnyNames(String [] words, long guild_id, boolean delete) {
		logger.trace("SQLReplaceFunnyNames launched. Passed params array, {}, {}", guild_id, delete);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			if(delete) {
				String sql = ("DELETE FROM names WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, guild_id);
			}
			
			String sql2 = ("INSERT IGNORE INTO names (name, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql2);;
			
			for(String word : words) {
				stmt.setString(1, word);
				stmt.setLong(2, guild_id);
				stmt.addBatch();
			}
			stmt.executeBatch();
			myConn.commit();
			return 0;
		} catch (SQLException e) {
			logger.error("SQLReplaceFunnyNames Exception", e);
			try {
				myConn.rollback();
				return 1;
			} catch (SQLException e1) {
				logger.error("SQLReplaceFunnyNames rollback Exception", e);
				return 2;
			}
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLReplaceURLBlacklist(String [] urls, long guild_id, boolean delete) {
		logger.trace("SQLReplaceURLBlacklist launched. Passed params array, {}, {}", guild_id, delete);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			if(delete) {
				String sql = ("DELETE FROM url_blacklist WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, guild_id);
				stmt.executeUpdate();
			}
			
			String sql2 = ("INSERT IGNORE INTO url_blacklist (url, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql2);
			for(String url : urls) {
				stmt.setString(1, url.replaceAll("(http:\\/\\/|https:\\/\\/)", "").replaceAll("www.", "").replace("\\b\\/[\\w\\d=?!&#\\[\\]().,+_*';:@$\\/-]*\\b", ""));
				stmt.setLong(2, guild_id);
				stmt.addBatch();
			}
			stmt.executeBatch();
			myConn.commit();
			return 0;
		} catch (SQLException e) {
			try {
				logger.error("SQLReplaceURLBlacklist Exception", e);
				myConn.rollback();
				return 1;
			} catch (SQLException e1) {
				logger.error("SQLReplaceURLBlacklist roll back Exception", e1);
				return 2;
			}
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLReplaceURLWhitelist(String [] urls, long guild_id, boolean delete) {
		logger.trace("SQLReplaceURLWhitelist launched. Passed params array, {}, {}", guild_id, delete);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			if(delete) {
				String sql = ("DELETE FROM url_whitelist WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, guild_id);
				stmt.executeUpdate();
			}
			
			String sql2 = ("INSERT IGNORE INTO url_whitelist (url, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql2);
			for(String url : urls) {
				stmt.setString(1, url.replaceAll("(http:\\/\\/|https:\\/\\/)", "").replaceAll("www.", "").replace("\\b\\/[\\w\\d=?!&#\\[\\]().,+_*';:@$\\/-]*\\b", ""));
				stmt.setLong(2, guild_id);
				stmt.addBatch();
			}
			stmt.executeBatch();
			myConn.commit();
			return 0;
		} catch (SQLException e) {
			try {
				logger.error("SQLReplaceURLWhitelist Exception", e);
				myConn.rollback();
				return 1;
			} catch (SQLException e1) {
				logger.error("SQLReplaceURLWhitelist roll back Exception", e1);
				return 2;
			}
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLReplaceTweetBlacklist(String [] usernames, long guild_id, boolean delete) {
		logger.trace("SQLReplaceTweetBlacklist launched. Passed params array, {}, {}", guild_id, delete);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			if(delete) {
				String sql = ("DELETE FROM tweet_blacklist WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, guild_id);
				stmt.executeUpdate();
			}
			
			String sql2 = ("INSERT IGNORE INTO tweet_blacklist (username, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql2);
			for(String username : usernames) {
				stmt.setString(1, username);
				stmt.setLong(2, guild_id);
				stmt.addBatch();
			}
			stmt.executeBatch();
			myConn.commit();
			return 0;
		} catch (SQLException e) {
			try {
				logger.error("SQLReplaceTweetBlacklist Exception", e);
				myConn.rollback();
				return 1;
			} catch (SQLException e1) {
				logger.error("SQLReplaceTweetBlacklist roll back Exception", e1);
				return 2;
			}
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLRegisterLanguageChannel(long guild_id, long channel_id, String channel_type) {
		logger.trace("SQLRegisterLanguageChannel launched. Passed params {}, {}, {}", guild_id, channel_id, channel_type);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			String sql = ("INSERT INTO channel_conf (fk_channel_id, fk_channel_type, fk_guild_id, url_censoring, txt_removal) VALUES (?, ?, ?, 0, 0) ON DUPLICATE KEY UPDATE fk_channel_type = VALUES(fk_channel_type)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, channel_id);
			stmt.setString(2, channel_type);
			stmt.setLong(3, guild_id);
			int result = stmt.executeUpdate();
			
			if(result > 0) {
				sql = ("DELETE FROM channel_filter WHERE fk_channel_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, channel_id);
				stmt.executeUpdate();
				
				sql = ("INSERT INTO channel_filter (fk_channel_id, fk_lang_abbrv) VALUES (?,?) ON DUPLICATE KEY UPDATE fk_lang_abbrv = VALUES(fk_lang_abbrv)");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, channel_id);
				stmt.setString(2, channel_type);
				result = stmt.executeUpdate();
				
				if(result > 0)
					myConn.commit();
				else
					myConn.rollback();
			}
			return result;
		} catch (SQLException e) {
			logger.error("SQLRegisterLanguageChannel Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLRegisterSpecialChannel(long guild_id, long channel_id, String channel_type) {
		logger.trace("SQLRegisterSpecialChannel launched. Passed params {}, {}, {}", guild_id, channel_id, channel_type);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			String sql = ("DELETE FROM channel_filter WHERE fk_channel_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, channel_id);
			stmt.executeUpdate();
			
			sql = ("INSERT INTO channel_conf (fk_channel_id, fk_channel_type, fk_guild_id, url_censoring, txt_removal) VALUES (?, ?, ?, 0, 0) ON DUPLICATE KEY UPDATE fk_channel_type = VALUES(fk_channel_type)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, channel_id);
			stmt.setString(2, channel_type);
			stmt.setLong(3, guild_id);
			final int result = stmt.executeUpdate();
			
			if(result > 0)
				myConn.commit();
			else
				myConn.rollback();
			
			return result;
		} catch (SQLException e) {
			logger.error("SQLRegisterSpecialChannel Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLRegisterUniqueChannel(long guild_id, long channel_id, String channel_type) {
		logger.trace("SQLRegisterUniqueChannel launched. Passed params {}, {}, {}", guild_id, channel_id, channel_type);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			String sql = ("DELETE FROM channel_conf WHERE fk_channel_type = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, channel_type);
			stmt.setLong(2, guild_id);
			stmt.executeUpdate();
			
			sql = ("INSERT INTO channel_conf (fk_channel_id, fk_channel_type, fk_guild_id, url_censoring, txt_removal) VALUES (?, ?, ?, 0, 0) ON DUPLICATE KEY UPDATE fk_channel_type = VALUES(fk_channel_type)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, channel_id);
			stmt.setString(2, channel_type);
			stmt.setLong(3, guild_id);
			final int result = stmt.executeUpdate();
			
			if(result > 0)
				myConn.commit();
			else
				myConn.rollback();
			
			return result;
		} catch (SQLException e) {
			logger.error("SQLRegisterUniqueChannel Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
}
