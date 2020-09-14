package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Bancollect;
import constructors.CategoryConf;
import constructors.Channels;
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
	
	public static synchronized void SQLInsertActionLog(String _event, long _target_id, long _guild_id, String _description) {
		if(IniFileReader.getActionLog()) {
			logger.trace("SQLInsertActionLog launched. Passed params {}, {}, {}, {}", _event, _target_id, _guild_id, _description);
			Connection myConn = null;
			PreparedStatement stmt = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("INSERT INTO action_log (event, target_id, guild_id, description, timestamp) VALUES(?, ?, ?, ?, ?)");
				stmt = myConn.prepareStatement(sql);
				stmt.setString(1, _event);
				stmt.setLong(2, _target_id);
				stmt.setLong(3, _guild_id);
				stmt.setString(4, _description);
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
	
	public static void SQLInsertHistory(long _user_id, long _guild_id, String _type, String _reason, long _penalty, String _info) {
		logger.trace("SQLInsertHistory launched. Passed params {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _type, _reason, _penalty, _info);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO history (fk_user_id, fk_guild_id, type, reason, time, penalty, info) VALUES(?, ?, ?, ?, ?, "+(_penalty != 0 ? "?" : "NULL")+", ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, _type);
			stmt.setString(4, _reason);
			stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
			if(_penalty != 0) {
				stmt.setLong(6, _penalty);
				stmt.setString(7, _info);
			}
			else {
				stmt.setString(6, _info);
			}
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertHistory Exception", e);
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<History> SQLgetHistory(long _user_id, long _guild_id) {
		logger.trace("SQLgetHistory launched. Passed params {}, {}", _user_id, _guild_id);
		ArrayList<History> history = new ArrayList<History>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT type, reason, time, penalty, info FROM history WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
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
	
	public static int SQLgetSingleActionEventCount(String _event, long _target_id, long _guild_id) {
		logger.trace("SQLgetSingleActionEventCount launched. Passed params {}, {}, {}", _event, _target_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT COUNT(*) FROM action_log WHERE target_id = ? && guild_id = ? && event = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _target_id);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, _event);
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
	
	public static ArrayList<String> SQLgetDoubleActionEventDescriptions(String _event, String _event2, long _target_id, long _guild_id) {
		logger.trace("SQLgetDoubleActionEventDescriptions launched. Passed params {}, {}, {}, {}", _event, _event2, _target_id, _guild_id);
		ArrayList<String> descriptions = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT description FROM action_log WHERE target_id = ? && (guild_id = ? || guild_id = 0) && (event = ? || event = ?) GROUP BY description");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _target_id);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, _event);
			stmt.setString(4, _event2);
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
	
	public static ArrayList<String> SQLgetSingleActionEventDescriptions(String _event, long _target_id, long _guild_id) {
		logger.trace("SQLgetSingleActionEventDescriptions launched. Passed params {}, {}, {}", _event, _target_id, _guild_id);
		ArrayList<String> descriptions = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT description FROM action_log WHERE target_id = ? && guild_id = ? && event = ? GROUP BY description");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _target_id);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, _event);
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
	public static ArrayList<String> SQLgetCriticalActionEvents(long _target_id, long _guild_id) {
		logger.trace("SQLgetCriticalActionEvents launched. Passed params {}, {}", _target_id, _guild_id);
		ArrayList<String> descriptions = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT description, timestamp FROM action_log WHERE target_id = ? && guild_id = ? && (event = \"MEMBER_KICK\" || event = \"MEMBER_BAN_ADD\" || event = \"MEMBER_BAN_REMOVE\" || event = \"MEMBER_MUTE_ADD\" || event = \"MEMBER_ROLE_ADD\" || event = \"MEMBER_ROLE_REMOVE\") ORDER BY timestamp desc LIMIT 30");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _target_id);
			stmt.setLong(2, _guild_id);
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
	
	public static ArrayList<String> SQLgetSingleActionEventDescriptionsOrdered(String _event, long _target_id, long _guild_id) {
		logger.trace("SQLgetSingleActionEventDescriptions launched. Passed params {}, {}, {}", _event, _target_id, _guild_id);
		ArrayList<String> descriptions = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT timestamp, description FROM action_log WHERE target_id = ? && guild_id = ? && event = ? ORDER BY timestamp desc LIMIT 30");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _target_id);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, _event);
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
	
	public static int SQLInsertUser(long _user_id, String _name, String _lang, String _avatar) {
		logger.trace("SQLInsertUser launched. Passed params {}, {}, {}, {}", _user_id, _name, _lang, _avatar);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO users (user_id, name, lang, avatar_url) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), avatar_url=VALUES(avatar_url)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setString(2, _name);
			stmt.setString(3, _lang);
			stmt.setString(4, _avatar);
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
	
	public static int SQLUpdateAvatar(long _user_id, String _avatar) {
		logger.trace("SQLUpdateAvatar launched. Passed params {}, {}", _user_id, _avatar);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE users SET avatar_url = ? WHERE user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _avatar);
			stmt.setLong(2, _user_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateAvatar Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertJoinDate(long _user_id, long _guild_id, String _join_date) {
		logger.trace("SQLInsertJoinDate launched. Passed params {}, {}, {}", _user_id, _guild_id, _join_date);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT IGNORE INTO join_dates (fk_user_id, fk_guild_id, first_join, join_date) VALUES(?,?,1,?), (?,?,0,?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, _join_date);
			stmt.setLong(4, _user_id);
			stmt.setLong(5, _guild_id);
			stmt.setString(6, _join_date);
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
	
	public static int SQLUpdateJoinDate(long _user_id, long _guild_id, String _join_date) {
		logger.trace("SQLUpdateJoinDate launched. Passed params {}, {}, {}", _user_id, _guild_id, _join_date);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE join_dates SET join_date = ? WHERE fk_user_id = ? AND fk_guild_id = ? AND first_join = 0");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _join_date);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateJoinDate Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static User SQLgetUser(String _name) {
		logger.trace("SqlgetUser launched. Passed params {}", _name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT user_id, name FROM users WHERE name = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _name);
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
	
	public static User SQLgetUserThroughID(String _user_id) {
		logger.trace("SQLgetUserThroughID launched. Passed params {}", _user_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM users WHERE user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _user_id);
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
	
	public static User SQLgetJoinDatesFromUser(long _user_id, long _guild_id, User _user) {
		logger.trace("SQLgetJoinDatesFromUser launched. Passed params {}, {}, User object", _user_id, _guild_id);
		String originalJoinDate = "N/A";
		String newestJoinDate = "N/A";
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT join_date FROM join_dates WHERE fk_user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			int count = 0;
			while(rs.next()) {
				if(count == 0)
					originalJoinDate = rs.getString(1);
				else
					newestJoinDate = rs.getString(1);
				count++;
			}
			return _user.setJoinDates(originalJoinDate, newestJoinDate);
		} catch (SQLException e) {
			logger.error("SQLgetJoinDatesFromUser Exception", e);
			return _user.setJoinDates(originalJoinDate, newestJoinDate);
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUser(Long _user_id, String _name) {
		logger.trace("SQLUpdateUser launched. Passed params {}, {}", _user_id, _name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE users SET name = ? WHERE user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _name);
			stmt.setLong(2, _user_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUser Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static String SQLgetUserLang(long _user_id) {
		final var language = Hashes.getLanguage(_user_id);
		if(language == null) {
			logger.trace("SQLgetUserLang launched. Passed params {}", _user_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT lang FROM users WHERE user_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _user_id);
				rs = stmt.executeQuery();
				if(rs.next()) {
					String lang = rs.getString(1);
					Hashes.setLanguage(_user_id, lang);
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
	
	public static long SQLgetGuild(long _guild_id) {
		logger.trace("SQLgetGuild launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT guild_id FROM guild WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
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
	
	public static String SQLgetLanguage(long _guild_id) {
		logger.trace("SQLgetLanguage launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT language FROM guild WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
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
	
	public static int SQLUpdateLanguage(Long _guild_id, String _language) {
		logger.trace("SQLUpdateLanguage launched. Passed params {}, {}", _guild_id, _language);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE guild SET language = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _language);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateLanguage Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertGuild(Long _guild_id, String _guild_name) {
		logger.trace("SQLInsertGuild launched. Passed params {}, {}", _guild_id, _guild_name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO guild (guild_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)");
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
	
	public static String SQLgetNickname(Long _user_id, Long _guild_id) {
		logger.trace("SQLgetNickname launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT nickname FROM nickname WHERE fk_user_id= ? && fk_guild_id= ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
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
	
	public static int SQLInsertNickname(Long _user_id, Long _guild_id, String _nickname) {
		logger.trace("SQLInsertNickname launched. Passed params {}, {}, {}", _user_id, _guild_id, _nickname);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO nickname VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE nickname=VALUES(nickname)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, _nickname);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertNickname Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateNickname(Long _user_id, Long _guild_id, String _nickname) {
		logger.trace("SQLUpdateNickname launched. Passed params {}, {}", _user_id, _guild_id, _nickname);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE nickname SET nickname = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _nickname);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateNickname Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteNickname(Long _user_id, Long _guild_id) {
		logger.trace("SQLDeleteNickname launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM nickname WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteNickname Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static Bancollect SQLgetData(Long _user_id, Long _guild_id) {
		logger.trace("SQLgetData launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM bancollect WHERE fk_user_id= ? && fk_guild_id= ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
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
	
	public static boolean SQLgetMuted(long _user_id, long _guild_id) {
		logger.trace("SQLgetMuted launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT muted FROM bancollect WHERE fk_user_id= ? && fk_guild_id= ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
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
	
	public static boolean SQLgetCustomMuted(long _user_id, long _guild_id) {
		logger.trace("SQLgetMuted launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT custom_time FROM bancollect WHERE fk_user_id= ? && fk_guild_id= ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
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
	
	public static boolean SQLisBanned(long _user_id, long _guild_id) {
		logger.trace("SQLgetMuted launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT fk_ban_id FROM bancollect WHERE fk_user_id= ? && fk_guild_id= ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
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
	
	public static int SQLgetWarning(long _user_id, long _guild_id) {
		logger.trace("SQLgetMuted launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT fk_warning_id FROM bancollect WHERE fk_user_id= ? && fk_guild_id= ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
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
	
	public static int SQLInsertData(long _user_id, long _guild_id, int _warning_id, int _ban_id, Timestamp _timestamp, Timestamp _unmute, boolean _muted, boolean _custom_time) {
		logger.trace("SQLInsertData launched. Passed params {}, {}, {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _warning_id, _ban_id, _timestamp, _unmute, _muted, _custom_time);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO bancollect VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0) ON DUPLICATE KEY UPDATE fk_warning_id=VALUES(fk_warning_id), fk_ban_id=VALUES(fk_ban_id), timestamp=VALUES(timestamp), unmute=VALUES(unmute), muted=VALUES(muted), custom_time=VALUES(custom_time), guild_left=VALUES(guild_left)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _warning_id);
			stmt.setInt(4, _ban_id);
			stmt.setTimestamp(5, _timestamp);
			stmt.setTimestamp(6, _unmute);
			stmt.setBoolean(7, _muted);
			stmt.setBoolean(8, _custom_time);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertData Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteData(long _user_id, long _guild_id) {
		logger.trace("SQLDeleteData launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM bancollect WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteData Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateMuted(long _user_id, long _guild_id, boolean _muted) {
		logger.trace("SQLUpdateMuted launched. Passed params {}, {}, {}", _user_id, _guild_id, _muted);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE bancollect SET muted = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setBoolean(1, _muted);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateMuted Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateMutedAndCustomMuted(long _user_id, long _guild_id, boolean _muted, boolean _custom_time) {
		logger.trace("SQLUpdateMutedAndCustomMuted launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _muted, _custom_time);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE bancollect SET muted = ?, custom_time = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setBoolean(1, _muted);
			stmt.setBoolean(2, _custom_time);
			stmt.setLong(3, _user_id);
			stmt.setLong(4, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateMutedAndCustomMuted Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateMutedOnEnd(long _user_id, long _guild_id, boolean _muted, boolean _custom_time) {
		logger.trace("SQLUpdateMutedOnEnd launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _muted, _custom_time);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE bancollect SET muted = ?, custom_time = ? WHERE fk_user_id = ? && fk_guild_id = ? && muted = 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setBoolean(1, _muted);
			stmt.setBoolean(2, _custom_time);
			stmt.setLong(3, _user_id);
			stmt.setLong(4, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateMuted Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateGuildLeft(long _user_id, long _guild_id, boolean _guildLeft) {
		logger.trace("SQLUpdateGuildLeft launched. Passed params {}, {}, {}", _user_id, _guild_id, _guildLeft);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE bancollect SET guild_left = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setBoolean(1, _guildLeft);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateGuildLeft Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static Warning SQLgetWarning(long _guild_id, int _warning_id) {
		logger.trace("SQLgetWarning launched. Passed params {}, {}", _guild_id, _warning_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT warning_id, mute_time, description FROM warnings WHERE fk_guild_id = ? && warning_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _warning_id);
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
	
	public static int SQLgetMaxWarning(long _guild_id) {
		logger.trace("SQLgetMaxWarning launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT MAX(warning_id) FROM warnings WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
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
	
	public static int SQLInsertWarning(long _guild_id, int _warning_id) {
		logger.trace("SQLInsertWarning launched. Passed params {}, {}", _guild_id, _warning_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = "";
			switch(_warning_id) {
				case 1 -> {
					sql = ("INSERT INTO warnings (fk_guild_id, warning_id, mute_time, description) VALUES"
							+ "(?, 0, 0, \"no warning\"),"
							+ "(?, 1, 0, \"first warning\") ON DUPLICATE KEY UPDATE description=VALUES(description)");
					stmt = myConn.prepareStatement(sql);
					stmt.setLong(1, _guild_id);
					stmt.setLong(2, _guild_id);
				}
				case 2 -> {
					sql = ("INSERT INTO warnings (fk_guild_id, warning_id, mute_time, description) VALUES"
							+ "(?, 0, 0, \"no warning\"),"
							+ "(?, 1, 0, \"first warning\"),"
							+ "(?, 2, 0, \"second warning\") ON DUPLICATE KEY UPDATE description=VALUES(description)");
					stmt = myConn.prepareStatement(sql);
					stmt.setLong(1, _guild_id);
					stmt.setLong(2, _guild_id);
					stmt.setLong(3, _guild_id);
				}
				case 3 -> {
					sql = ("INSERT INTO warnings (fk_guild_id, warning_id, mute_time, description) VALUES"
							+ "(?, 0, 0, \"no warning\"),"
							+ "(?, 1, 0, \"first warning\"),"
							+ "(?, 2, 0, \"second warning\"),"
							+ "(?, 3, 0, \"third warning\") ON DUPLICATE KEY UPDATE description=VALUES(description)");
					stmt = myConn.prepareStatement(sql);
					stmt.setLong(1, _guild_id);
					stmt.setLong(2, _guild_id);
					stmt.setLong(3, _guild_id);
					stmt.setLong(4, _guild_id);
				}
				case 4 -> {
					sql = ("INSERT INTO warnings (fk_guild_id, warning_id, mute_time, description) VALUES"
							+ "(?, 0, 0, \"no warning\"),"
							+ "(?, 1, 0, \"first warning\"),"
							+ "(?, 2, 0, \"second warning\"),"
							+ "(?, 3, 0, \"third warning\"),"
							+ "(?, 4, 0, \"fourth warning\") ON DUPLICATE KEY UPDATE description=VALUES(description)");
					stmt = myConn.prepareStatement(sql);
					stmt.setLong(1, _guild_id);
					stmt.setLong(2, _guild_id);
					stmt.setLong(3, _guild_id);
					stmt.setLong(4, _guild_id);
					stmt.setLong(5, _guild_id);
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
					stmt.setLong(1, _guild_id);
					stmt.setLong(2, _guild_id);
					stmt.setLong(3, _guild_id);
					stmt.setLong(4, _guild_id);
					stmt.setLong(5, _guild_id);
					stmt.setLong(6, _guild_id);
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
	
	public static int SQLUpdateWarning(long _user_id, long _guild_id, int _warning_id) {
		logger.trace("SQLUpdateWarning launched. Passed params {}, {}", _user_id, _guild_id, _warning_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE bancollect SET fk_warning_id = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _warning_id);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateWarning Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateMuteTimeOfWarning(long _guild_id, int _warning_id, long _mute_time) {
		logger.trace("SQLUpdateTimeOfWarning launched. Passed params {}, {}, {}", _guild_id, _warning_id, _mute_time);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE warnings SET mute_time = ? WHERE fk_guild_id = ? && warning_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _mute_time);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _warning_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateMuteTimeOfWarning Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUnmute(Long _user_id, Long _guild_id, Timestamp _timestamp, Timestamp _unmute, boolean _muted, boolean _custom_time) {
		logger.trace("SQLUpdateUnmute launched. Passed params {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _timestamp, _unmute, _muted, _custom_time);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE bancollect SET timestamp = ?, unmute = ?, muted = ?, custom_time = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setTimestamp(1, _timestamp);
			stmt.setTimestamp(2, _unmute);
			stmt.setBoolean(3, _muted);
			stmt.setBoolean(4, _custom_time);
			stmt.setLong(5, _user_id);
			stmt.setLong(6, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUnmute Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUnmute(Long _user_id, Long _guild_id, Timestamp _unmute) {
		logger.trace("SQLUpdateUnmute launched. Passed params {}, {}, {}", _user_id, _guild_id, _unmute);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE bancollect SET unmute = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setTimestamp(1, _unmute);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateUnmute Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateBan(Long _user_id, Long _guild_id, Integer _ban_id) {
		logger.trace("SQLUpdateban launched. Passed params {}, {}, {}", _user_id, _guild_id, _ban_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE bancollect SET fk_ban_id = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _ban_id);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateBan Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertChannels(long _channel_id, String _channel_name) {
		logger.trace("SQLInsertChannels launched. Passed params {}, {}", _channel_id, _channel_name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO channels (channel_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			stmt.setString(2, _channel_name);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertChannels Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLBulkInsertChannels(List<TextChannel> _textChannels) {
		logger.trace("SQLBulkInsertChannels launched. Array param passed");
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false); 
			String sql = ("INSERT INTO channels (channel_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name)");
			stmt = myConn.prepareStatement(sql);
			for(final var channel : _textChannels) {
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
	
	public static int SQLDeleteChannels(long _channel_id) {
		logger.trace("SQLDeleteChannels launched. Passed params {}", _channel_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM channels WHERE channel_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteChannels Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertChannel_Conf(long _channel_id, long _guild_id, String _channel_type) {
		logger.trace("SQLInsertChannel launched. Passed params {}, {}, {}, {}", _channel_id, _guild_id, _channel_type);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO channel_conf (fk_channel_id, fk_channel_type, fk_guild_id, url_censoring, txt_removal) VALUES (?, ?, ?, 0, 0) ON DUPLICATE KEY UPDATE fk_channel_type = VALUES(fk_channel_type)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			stmt.setString(2, _channel_type);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertChannel_Conf Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteChannelConf(long _channel_id, long _guild_id) {
		logger.trace("SQLDeleteChannelConf launched. Passed params {}, {}", _channel_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM channel_conf WHERE fk_channel_id = ? and fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteChannelConf Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteAllChannelConfs(long _guild_id) {
		logger.trace("SQLDeleteAllChannelConfs launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM channel_conf WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteAllChannelConfs Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertChannel_ConfURLCensoring(long _channel_id, long _guild_id, boolean _url_censoring) {
		logger.trace("SQLInsertChannel_ConfURLCensoring launched. Passed params {}, {}, {}, {}", _channel_id, _guild_id, _url_censoring);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO channel_conf (fk_channel_id, fk_guild_id, url_censoring, txt_removal) VALUES (?, ?, ?, 0) ON DUPLICATE KEY UPDATE url_censoring = VALUES(url_censoring)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			stmt.setLong(2, _guild_id);
			stmt.setBoolean(3, _url_censoring);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertChannel_ConfURLCensoring Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertChannel_ConfTXTCensoring(long _channel_id, long _guild_id, boolean _txt_removal) {
		logger.trace("SQLInsertChannel_ConfTXTCensoring launched. Passed params {}, {}, {}, {}", _channel_id, _guild_id, _txt_removal);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO channel_conf (fk_channel_id, fk_guild_id, url_censoring, txt_removal) VALUES (?, ?, 0, ?) ON DUPLICATE KEY UPDATE txt_removal = VALUES(txt_removal)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			stmt.setLong(2, _guild_id);
			stmt.setBoolean(3, _txt_removal);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertChannel_ConfTXTCensoring Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteChannelType(String _channel_type, long _guild_id) {
		logger.trace("SQLDeleteChannelType launched. Passed params {}, {}", _channel_type, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM channel_conf WHERE fk_channel_type = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _channel_type);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteChannelType Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static synchronized ArrayList<Channels> SQLgetChannels(long _guild_id) {
		final var cachedChannels = Hashes.getChannels(_guild_id);
		if(cachedChannels == null) {
			logger.trace("SQLgetChannels launched. Passed params {}", _guild_id);
			ArrayList<Channels> channels = new ArrayList<Channels>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT * FROM all_channels WHERE guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
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
				Hashes.addChannels(_guild_id, channels);
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
	
	public static boolean SQLgetCommandExecutionReaction(long _guild_id) {
		logger.trace("SQLgetCommandExecutionReaction launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT reactions FROM guild WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
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
	
	public static int SQLUpdateReaction(long _guild_id, boolean _reactions) {
		logger.trace("SQLUpdateReaction launched. Passed params {}, {}", _guild_id, _reactions);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE guild SET reactions = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setBoolean(1, _reactions);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateReaction Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public synchronized static ArrayList<String> SQLgetChannel_Filter(long _channel_id) {
		final var censor = Hashes.getFilterLang(_channel_id);
		if(censor == null) {
			logger.trace("SQLgetChannel_Filter launched. Passed params {}", _channel_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT * FROM channel_filter WHERE fk_channel_id = ?");
				ArrayList<String> filter_lang = new ArrayList<String>();
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _channel_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					filter_lang.add(rs.getString(2));
				}
				Hashes.addFilterLang(_channel_id, filter_lang);
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
	
	public synchronized static ArrayList<String> SQLgetFilter(String _filter_lang, long _guild_id) {
		final var query = Hashes.getQuerryResult(_filter_lang+"_"+_guild_id);
		if(query == null) {
			logger.trace("SQLgetFilter launched. Passed params {}, {}", _filter_lang, _guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				ArrayList<String> filter_words = new ArrayList<String>();
				String sql;
				if(_filter_lang.equals("all")) {
					sql = ("SELECT word FROM filter WHERE fk_guild_id = ?");
					stmt = myConn.prepareStatement(sql);
					stmt.setLong(1, _guild_id);
				}
				else{
					sql = ("SELECT word FROM filter WHERE fk_lang_abbrv = ? && fk_guild_id = ?");
					stmt = myConn.prepareStatement(sql);
					stmt.setString(1, _filter_lang);
					stmt.setLong(2, _guild_id);
				}
				rs = stmt.executeQuery();
				while(rs.next()) {
					filter_words.add(rs.getString(1));
				}
				Hashes.addQuerryResult(_filter_lang+"_"+_guild_id, filter_words);
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
	
	public static int SQLInsertWordFilter(String _lang, String _word, long _guild_id) {
		logger.trace("SQLInsertWordFilter launched. Passed params {}, {}, {}", _lang, _word, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			var sql = "";
			if(!_lang.equals("all"))
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
			if(!_lang.equals("all")) {
				stmt.setString(1, _word.toLowerCase());
				stmt.setString(2, _lang);
				stmt.setLong(3, _guild_id);
			}
			else {
				var word = _word.toLowerCase();
				stmt.setString(1, word);
				stmt.setLong(2, _guild_id);
				stmt.setString(3, word);
				stmt.setLong(4, _guild_id);
				stmt.setString(5, word);
				stmt.setLong(6, _guild_id);
				stmt.setString(7, word);
				stmt.setLong(8, _guild_id);
				stmt.setString(9, word);
				stmt.setLong(10, _guild_id);
				stmt.setString(11, word);
				stmt.setLong(12, _guild_id);
				stmt.setString(13, word);
				stmt.setLong(14, _guild_id);
				stmt.setString(15,word);
				stmt.setLong(16, _guild_id);
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
	
	public static boolean SQLDeleteWordFilterAllLang(String _word, long _guild_id) {
		logger.trace("SQLDeleteWordFilter launched. Passed params {}, {}", _word, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM filter WHERE word = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word);
			stmt.setLong(2, _guild_id);
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
	
	public static int SQLDeleteWordFilter(String _lang, String _word, long _guild_id) {
		logger.trace("SQLDeleteWordFilter launched. Passed params {}, {}, {}", _lang, _word, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM filter WHERE word = ? "+(!_lang.equals("all") ? "&& fk_lang_abbrv = ? " : "")+"&& fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word);
			if(!_lang.equals("all")) {
				stmt.setString(2, _lang);
				stmt.setLong(3, _guild_id);
			}
			else
				stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteWordFilter Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public synchronized static ArrayList<String> SQLgetStaffNames(long _guild_id) {
		final var query = Hashes.getQuerryResult("staff-names_"+_guild_id);
		if(query == null) {
			logger.trace("SQLgetStaffNames launched. Passed params {}", _guild_id);
			ArrayList<String> staff_names = new ArrayList<String>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT name FROM staff_name_filter WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					staff_names.add(rs.getString(1));
				}
				Hashes.addQuerryResult("staff-names_"+_guild_id, staff_names);
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
	
	public static int SQLInsertStaffName(String _word, long _guild_id) {
		logger.trace("SQLInsertStaffName launched. Passed params {}, {}", _word, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO staff_name_filter (name, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word.toLowerCase());
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertStaffName Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteStaffNames(String _word, long _guild_id) {
		logger.trace("SQLDeleteStaffNames launched. Passed params {}, {}", _word, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM staff_name_filter WHERE name = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word.toLowerCase());
			stmt.setLong(2, _guild_id);
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
	public static int SQLReplaceStaffNames(String [] _words, long _guild_id, boolean delete) {
		logger.trace("SQLBatchInsertStaffNames launched. Passed params array, {}, {}", _guild_id, delete);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			if(delete) {
				String sql = ("DELETE FROM staff_name_filter WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				stmt.executeUpdate();
			}
			
			String sql2 = ("INSERT IGNORE INTO staff_name_filter (name, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql2);
			for(String word : _words) {
				stmt.setString(1, word.toLowerCase());
				stmt.setLong(2, _guild_id);
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
	
	public static int SQLInsertChannel_Filter(long _channel_id, String _filter_lang) {
		logger.trace("SQLInsertChannel_Filter launched. Passed params {}, {}", _channel_id, _filter_lang);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO channel_filter (fk_channel_id, fk_lang_abbrv) VALUES (?,?) ON DUPLICATE KEY UPDATE fk_lang_abbrv = VALUES(fk_lang_abbrv)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			stmt.setString(2, _filter_lang);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertChannel_Filter Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteChannel_Filter(long _channel_id) {
		logger.trace("SQLDeleteChannel_Filter launched. Passed params {}", _channel_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM channel_filter WHERE fk_channel_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteChannel_Filter Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetFunnyNames(long _guild_id) {
		final var query = Hashes.getQuerryResult("funny-names_"+_guild_id);
		if(query == null) {
			ArrayList<String> names = new ArrayList<String>();
			logger.trace("SQLgetFunnyNames launched. Passed params {}", _guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT name FROM names WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					names.add(rs.getString(1));
				}
				Hashes.addQuerryResult("funny-names_"+_guild_id, names);
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
	
	public static int SQLInsertFunnyNames(String _word, long _guild_id) {
		logger.trace("SQLInsertFunnnynames launched. Passed params {}, {}", _word, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO names (name, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertFunnyNamesException", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteFunnyNames(String _word, long _guild_id) {
		logger.trace("SQLDeleteFunnyNames launched. Passed params {}, {}", _word, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM names WHERE name = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteFunnyNames Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<NameFilter> SQLgetNameFilter(long _guild_id) {
		final var namesFilter = Hashes.getNameFilter(_guild_id); 
		if(namesFilter == null) {
			logger.trace("SQLgetNameFilter launched. Passed params {}", _guild_id);
			ArrayList<NameFilter> names = new ArrayList<NameFilter>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT word, kick FROM name_filter WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					names.add(new NameFilter(rs.getString(1), rs.getBoolean(2)));
				}
				Hashes.addNameFilter(_guild_id, names);
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
	
	public static int SQLInsertNameFilter(String _word, boolean _kick, long _guild_id) {
		logger.trace("SQLInsertNameFilter launched. Passed params {}, {}, {}", _word, _kick, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO name_filter (word, kick, fk_guild_id) VALUES(?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word.toLowerCase());
			stmt.setBoolean(2, _kick);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertNameFilter Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteNameFilter(String _word, boolean _kick, long _guild_id) {
		logger.trace("SQLDeleteNameFilter launched. Passed params {}, {}, {}", _word, _kick, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM name_filter WHERE word = ? && fk_guild_id = ? && kick = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word);
			stmt.setLong(2, _guild_id);
			stmt.setBoolean(3, _kick);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteNameFilter Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static String SQLgetRandomName(long _guild_id) {
		logger.trace("SQLgetRandomName launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT name FROM names WHERE fk_guild_id = ? ORDER BY RAND() LIMIT 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
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
	
	public static ArrayList<String> SQLgetFilterLanguages() {
		final var languages = Hashes.getFilterLang(0);
		if(languages == null) {
			logger.trace("SQLgetFilterLanguages launched. No params passed");
			ArrayList<String> filter_lang = new ArrayList<String>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT language FROM filter_languages WHERE lang_abbrv  != \"all\"");
				stmt = myConn.prepareStatement(sql);
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
	
	public static int SQLInsertRSS(String _url, long _guild_id, int _type) {
		logger.trace("SQLInsertRSS launched. Params passed {}, {}, {}", _url, _guild_id, _type);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT IGNORE INTO rss (url, fk_guild_id, format, type, videos, pictures, text) VALUES (?, ?, ?, ?, 1, 1, 1)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _url);
			stmt.setLong(2, _guild_id);
			if(_type == 1)
				stmt.setString(3, "{pubDate} | {title}\n{description}\n{link}");
			else if(_type == 2)
				stmt.setString(3, "From: **{fullName} {username}**\n{description}");
			stmt.setInt(4, _type);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertRSS Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetSubTweets(long _guild_id, String _tweet) {
		logger.trace("SQLgetSubTweets launched. Params passed {}, {}", _guild_id, _tweet);
		ArrayList<String> tweets = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT child_tweet FROM sub_tweets WHERE fk_guild_id = ? && parent_tweet = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setString(2, _tweet);
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
	
	public static ArrayList<RSS> SQLgetSubscriptions(long _guild_id) {
		final var feed = Hashes.getFeed(_guild_id);
		if(feed == null) {
			logger.trace("SQLgetSubscriptions launched. Params passed {}", _guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				ArrayList<RSS> feeds = new ArrayList<RSS>();
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT url, format, type, videos, pictures, text, channel_id FROM rss WHERE fk_guild_id = ? ORDER BY timestamp asc");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
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
							SQLgetSubTweets(_guild_id, rs.getString(1))
						)
					);
				}
				Hashes.addFeeds(_guild_id, feeds);
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
	
	public static ArrayList<RSS> SQLgetSubscriptions(long _guild_id, int _type) {
		logger.trace("SQLgetSubscriptions launched. Params passed {}, {}", _guild_id, _type);
		ArrayList<RSS> feeds = new ArrayList<RSS>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT url, format, type, videos, pictures, text, channel_id FROM rss WHERE fk_guild_id = ? AND type = ? ORDER BY timestamp asc");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _type);
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
						SQLgetSubTweets(_guild_id, rs.getString(1))
					)
				);
			}
			Hashes.addFeeds(_guild_id, feeds);
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
	
	public static int SQLUpdateRSSPictures(String _url, long _guild_id, boolean _option) {
		logger.trace("SQLUpdateRSSPictures launched. Params passed {}, {}, {}", _url, _guild_id, _option);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE rss SET pictures = ? WHERE url = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setBoolean(1, _option);
			stmt.setString(2, _url);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateRSSPictures Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateRSSVideos(String _url, long _guild_id, boolean _option) {
		logger.trace("SQLUpdateRSSVideos launched. Params passed {}, {}, {}", _url, _guild_id, _option);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE rss SET videos = ? WHERE url = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setBoolean(1, _option);
			stmt.setString(2, _url);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateRSSVideos Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateRSSText(String _url, long _guild_id, boolean _option) {
		logger.trace("SQLUpdateRSSText launched. Params passed {}, {}, {}", _url, _guild_id, _option);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE rss SET text = ? WHERE url = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setBoolean(1, _option);
			stmt.setString(2, _url);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateRSSText Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertChildTweet(String _urlParent, String _urlChild, long _guild_id) {
		logger.trace("SQLInsertChildTweet launched. Params passed {}, {}, {}", _urlParent, _urlChild, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT IGNORE INTO sub_tweets (parent_tweet, child_tweet, fk_guild_id) VALUES(?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _urlParent);
			stmt.setString(2, _urlChild);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertChildTweet Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteChildTweet(String _urlParent, String _urlChild, long _guild_id) {
		logger.trace("SQLDeleteChildTweet launched. Params passed {}, {}, {}", _urlParent, _urlChild, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM sub_tweets WHERE parent_tweet = ? AND child_tweet = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _urlParent);
			stmt.setString(2, _urlChild);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteChildTweet Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteRSSFeed(String _url, long _guild_id) {
		logger.trace("SQLDeleteRSSFeed launched. Params passed {}, {}", _url, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM rss WHERE url = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _url);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteRSSFeed Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateRSSFormat(String _url, long _guild_id, String _format) {
		logger.trace("SQLUpdateRSSFormat launched. Params passed {}, {}, {}", _url, _guild_id, _format);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE rss SET format = ? WHERE url = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _format);
			stmt.setString(2, _url);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateRSSFormat Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateRSSChannel(String _url, long _guild_id, long _channel_id) {
		logger.trace("SQLUpdateRSSChannel launched. Params passed {}, {}, {}", _url, _guild_id, _channel_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE rss SET channel_id = ? WHERE url = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			stmt.setString(2, _url);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateRSSChannel Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static synchronized ArrayList<String> SQLgetURLBlacklist(long _guild_id) {
		final var blacklist = Hashes.getURLBlacklist(_guild_id); 
		if(blacklist == null) {
			logger.trace("SQLgetURLBlacklist launched. Passed params {}", _guild_id);
			ArrayList<String> urls = new ArrayList<String>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT url FROM url_blacklist WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					urls.add(rs.getString(1));
				}
				Hashes.addURLBlacklist(_guild_id, urls);
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
	
	public static int SQLInsertURLBlacklist(String _url, long _guild_id) {
		logger.trace("SQLInsertURLBlacklist launched. Passed params {}, {}", _url, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO url_blacklist (url, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _url.replaceAll("(http:\\/\\/|https:\\/\\/)", "").replaceAll("www.", "").replace("\\b\\/[\\w\\d=?!&#\\[\\]().,+_*';:@$\\/-]*\\b", ""));
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertURLBlacklist Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteURLBlacklist(String _url, long _guild_id) {
		logger.trace("SQLDeleteURLBlacklist launched. Passed params {}, {}", _url, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM url_blacklist WHERE url = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _url.replaceAll("(http:\\/\\/|https:\\/\\/)", "").replaceAll("www.", "").replace("\\b\\/[\\w\\d=?!&#\\[\\]().,+_*';:@$\\/-]*\\b", ""));
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteURLBlacklist Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static synchronized ArrayList<String> SQLgetURLWhitelist(long _guild_id) {
		final var whitelist = Hashes.getURLWhitelist(_guild_id);
		if(whitelist == null) {
			logger.trace("SQLgetURLWhitelist launched. Passed params {}", _guild_id);
			ArrayList<String> urls = new ArrayList<String>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT url FROM url_whitelist WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					urls.add(rs.getString(1));
				}
				Hashes.addURLWhitelist(_guild_id, urls);
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
	
	public static int SQLInsertURLWhitelist(String _url, long _guild_id) {
		logger.trace("SQLInsertURLWhitelist launched. Passed params {}, {}", _url, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO url_whitelist (url, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _url.replaceAll("(http:\\/\\/|https:\\/\\/)", "").replaceAll("www.", "").replaceAll("\\b\\/[\\w\\d=?!&#\\[\\]().,+_*';:@$\\/-]*\\b", ""));
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertURLWhitelist Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteURLWhitelist(String _url, long _guild_id) {
		logger.trace("SQLDeleteURLWhitelist launched. Passed params {}, {}", _url, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM url_whitelist WHERE url = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _url.replaceAll("(http:\\/\\/|https:\\/\\/)", "").replaceAll("www.", "").replace("\\b\\/[\\w\\d=?!&#\\[\\]().,+_*';:@$\\/-]*\\b", ""));
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteURLWhitelist Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static synchronized ArrayList<String> SQLgetTweetBlacklist(long _guild_id) {
		final var blacklist = Hashes.getTweetBlacklist(_guild_id);
		if(blacklist == null) {
			logger.trace("SQLgetTweetBlacklist launched. Passed params {}", _guild_id);
			ArrayList<String> urls = new ArrayList<String>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT username FROM tweet_blacklist WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					urls.add(rs.getString(1));
				}
				Hashes.addTweetBlacklist(_guild_id, urls);
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
	
	public static int SQLInsertTweetBlacklist(String _username, long _guild_id) {
		logger.trace("SQLInsertTweetBlacklist launched. Passed params {}, {}", _username, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO tweet_blacklist (username, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, (_username.startsWith("@") ? _username : "@"+_username));
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertTweetBlacklist Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteTweetBlacklist(String _username, long _guild_id) {
		logger.trace("SQLDeleteTweetBlacklist launched. Passed params {}, {}", _username, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM tweet_blacklist WHERE username = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, (_username.startsWith("@") ? _username : "@"+_username));
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteTweetBlacklist Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static synchronized Watchlist SQLgetWatchlist(long _user_id, long _guild_id) {
		final var cachedWatchlist = Hashes.getWatchlist(_guild_id+"-"+_user_id);
		if(cachedWatchlist == null) {
			logger.trace("SQLgetWatchlist launched. Params passed {}, {}", _user_id, _guild_id);
			Watchlist watchlist = null;
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT * FROM watchlist WHERE fk_user_id = ? AND fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _user_id);
				stmt.setLong(2, _guild_id);
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
	
	public static ArrayList<String> SQLgetWholeWatchlist(long _guild_id, boolean _highPrivileges) {
		logger.trace("SQLgetWholeWatchlist launched. Params passed {}, {}", _guild_id, _highPrivileges);
		ArrayList<String> watchlist = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM watchlist WHERE fk_guild_id = ? && higher_privileges = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setBoolean(2, _highPrivileges);
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
	
	public static int SQLInsertWatchlist(long _user_id, long _guild_id, int _level, long _watchChannel, boolean _higherPrivileges) {
		logger.trace("SQLInsertWatchlist launched. Passed params {}, {}, {}, {}, {}", _user_id, _guild_id, _level, _watchChannel, _higherPrivileges);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO watchlist (fk_user_id, fk_guild_id, level, watch_channel, higher_privileges) VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE level=VALUES(level), watch_channel=VALUES(watch_channel), higher_privileges=VALUES(higher_privileges)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _level);
			stmt.setLong(4, _watchChannel);
			stmt.setBoolean(5, _higherPrivileges);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertWatchlist Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteWatchlist(long _user_id, long _guild_id) {
		logger.trace("SQLDeleteWatchlist launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM watchlist WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteWatchlist Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static List<GoogleAPISetup> SQLgetGoogleAPISetupOnGuildAndAPI(long _guild_id, int _api_id) {
		logger.trace("SQLgetGoogleAPISetupOnGuild launched. Params passed {}, {}", _guild_id, _api_id);
		ArrayList<GoogleAPISetup> setup = new ArrayList<GoogleAPISetup>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM google_apis_setup_view WHERE fk_guild_id = ? AND fk_api_id = ? ORDER BY timestamp asc");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _api_id);
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
	
	public static int SQLInsertGoogleAPISetup(String _file_id, long _guild_id, String _title, int _api_id) {
		logger.trace("SQLInsertGoogleAPISetup launched. Passed params {}, {}, {}, {}", _file_id, _guild_id, _title, _api_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO google_apis_setup (file_id, fk_guild_id, title, fk_api_id) VALUES(?, ?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _file_id);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, _title);
			stmt.setInt(4, _api_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertGoogleAPISetup Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteGoogleAPISetup(String _file_id, long _guild_id) {
		logger.trace("SQLDeleteGoogleAPISetup launched. Passed params {}, {}, {}, {}", _file_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM google_apis_setup WHERE file_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _file_id);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteGoogleAPISetup Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Integer> SQLgetGoogleLinkedEvents(String _file_id, long _guild_id) {
		logger.trace("SQLgetGoogleLinkedEvents launched. Params passed {}, {}", _file_id, _guild_id);
		ArrayList<Integer> events = new ArrayList<Integer>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT fk_event_id FROM google_file_to_event WHERE fk_file_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _file_id);
			stmt.setLong(2, _guild_id);
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
				events.add(new GoogleEvents(rs.getInt(1), rs.getString(2), rs.getBoolean(3), rs.getBoolean(4), rs.getBoolean(5)));
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
	
	public static boolean SQLBatchInsertGoogleFileToEventLink(String _file_id, long _guild_id, List<Integer> _events) {
		logger.trace("SQLBatchInsertGoogleFileToEventLink launched. Passed params {}, {}, array", _file_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO google_file_to_event (fk_guild_id, fk_file_id, fk_event_id) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE fk_event_id=VALUES(fk_event_id)");
			stmt = myConn.prepareStatement(sql);
			for(final int event: _events) {
				stmt.setLong(1, _guild_id);
				stmt.setString(2, _file_id);
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
	
	public static boolean SQLBatchDeleteGoogleSpreadsheetSheet(String _file_id, long _guild_id, List<Integer> _events) {
		logger.trace("SQLBatchDeleteGoogleSpreadsheetSheet launched. Passed params {}, {}, array", _file_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM google_spreadsheet_sheet WHERE fk_file_id = ? AND fk_event_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			for(final int event: _events) {
				stmt.setString(1, _file_id);
				stmt.setInt(2, event);
				stmt.setLong(3, _guild_id);
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
	
	public static int SQLInsertGoogleSpreadsheetSheet(String _file_id, int _event_id, String _row_start, long _guild_id) {
		logger.trace("SQLInsertGoogleSpreadsheetSheet launched. Passed params {}, {}, {}, {}", _file_id, _event_id, _row_start, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO google_spreadsheet_sheet (fk_guild_id, fk_file_id, fk_event_id, sheet_row_start) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE sheet_row_start=VALUES(sheet_row_start)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setString(2, _file_id);
			stmt.setInt(3, _event_id);
			stmt.setString(4, _row_start);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertGoogleSpreadsheetSheet Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<GoogleSheet> SQLgetGoogleSpreadsheetSheets(String _file_id, long _guild_id) {
		logger.trace("SQLgetGoogleSpreadsheetSheets launched. Params passed {}, {}", _file_id, _guild_id);
		ArrayList<GoogleSheet> sheets = new ArrayList<GoogleSheet>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT fk_event_id, sheet_row_start FROM google_spreadsheet_sheet WHERE fk_file_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _file_id);
			stmt.setLong(2, _guild_id);
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
	
	public static int SQLDeleteGoogleSpreadsheetSheet(String _file_id, long _guild_id) {
		logger.trace("SQLDeleteGoogleSpreadsheetSheet launched. Passed params {}, {}", _file_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM google_spreadsheet_sheet WHERE fk_file_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _file_id);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteGoogleSpreadsheetSheet Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLBatchDeleteGoogleSpreadsheetMapping(String _file_id, long _guild_id, List<Integer> _events) {
		logger.trace("SQLBatchDeleteGoogleSpreadsheetMapping launched. Passed params {}, {} array", _file_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM google_spreadsheet_mapping WHERE fk_file_id = ? AND fk_event_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			for(final int event: _events) {
				stmt.setString(1, _file_id);
				stmt.setInt(2, event);
				stmt.setLong(3, _guild_id);
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
	
	public static int SQLDeleteGoogleSpreadsheetMapping(String _file_id, long _guild_id) {
		logger.trace("SQLDeleteGoogleSpreadsheetMapping launched. Passed params {}, {}", _file_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM google_spreadsheet_mapping WHERE fk_file_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _file_id);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteGoogleSpreadsheetMapping Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLBatchDeleteGoogleFileToEvent(String _file_id, long _guild_id, List<Integer> _events) {
		logger.trace("SQLBatchDeleteGoogleFileToEvent launched. Passed params {}, {}, array", _file_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM google_file_to_event WHERE fk_file_id = ? AND fk_event_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			for(final int event: _events) {
				stmt.setString(1, _file_id);
				stmt.setInt(2, event);
				stmt.setLong(3, _guild_id);
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
	
	public static int SQLDeleteGoogleFileToEvent(String _file_id, long _guild_id) {
		logger.trace("SQLDeleteGoogleFileToEvent launched. Passed params {}, {}", _file_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM google_file_to_event WHERE fk_file_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _file_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteGoogleFileToEvent Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetGoogleEventsToDD(int _api_id, int _event_id) {
		logger.trace("SQLgetGoogleEventsToDD launched. Params passed {}, {}", _api_id, _event_id);
		ArrayList<String> items = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM google_events_to_dd WHERE fk_api_id = ? AND fk_event_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _api_id);
			stmt.setInt(2, _event_id);
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
	
	public static int SQLDeleteGoogleSpreadsheetMapping(String _file_id, int _event_id, long _guild_id) {
		logger.trace("SQLDeleteGoogleSpreadsheetMapping launched. Passed params {}, {}, {}", _file_id, _event_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM google_spreadsheet_mapping WHERE fk_file_id = ? AND fk_event_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _file_id);
			stmt.setInt(2, _event_id);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteGoogleSpreadsheetMapping Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int [] SQLBatchInsertGoogleSpreadsheetMapping(String _file_id, int _event_id, long _guild_id, List<Integer> _dd_items, List<String> _dd_formats) {
		logger.trace("SQLBatchInsertGoogleSpreadsheetMapping launched. Passed params {}, {}, {}, array1, array2, array3", _file_id, _event_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO google_spreadsheet_mapping (fk_guild_id, fk_file_id, fk_event_id, column_number, fk_dd_id, format) VALUES(?, ?, ?, ?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			for(int columnNumber = 0; columnNumber < _dd_items.size(); columnNumber++) {
				stmt.setLong(1, _guild_id);
				stmt.setString(2, _file_id);
				stmt.setInt(3, _event_id);
				stmt.setInt(4, columnNumber+1);
				stmt.setInt(5, _dd_items.get(columnNumber));
				stmt.setString(6, _dd_formats.get(columnNumber));
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
	
	public static ArrayList<GoogleSheetColumn> SQLgetGoogleSpreadsheetMapping(String _file_id, int _event_id, long _guild_id) {
		logger.trace("SQLgetGoogleSpreadsheetMapping launched. Params passed {}, {}, {}", _file_id, _event_id, _guild_id);
		ArrayList<GoogleSheetColumn> items = new ArrayList<GoogleSheetColumn>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT fk_dd_id, format, column_number FROM google_spreadsheet_mapping WHERE fk_file_id = ? AND fk_event_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _file_id);
			stmt.setInt(2, _event_id);
			stmt.setLong(3, _guild_id);
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
	
	public static String [] SQLgetGoogleFilesAndEvent(long _guild_id, int _api_id, int _event_id) {
		logger.trace("SQLgetGoogleFilesAndEvent launched. Params passed {}, {}, {}", _guild_id, _api_id, _event_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT file_id, sheet_row_start FROM google_files_and_events WHERE guild_id = ? AND api_id = ? AND event_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _api_id);
			stmt.setInt(3, _event_id);
			rs = stmt.executeQuery();
			String [] array = new String [2];
			if(rs.next()) {
				array[0] = rs.getString(1);
				array[1] = rs.getString(2);
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
	
	public static int SQLInsertReminder(long _user_id, long _guild_id, String _type, String _reason, String _reporter, String _time) {
		logger.trace("SQLInsertReminder launched. Passed params {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _type, _reason, _reporter, _time);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO reminder (fk_user_id, fk_guild_id, type, reason, reporter, time) VALUES(?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE type=VALUES(type), reason=VALUES(reason), reporter=VALUES(reporter), time=VALUES(time)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, _type);
			stmt.setString(4, _reason);
			stmt.setString(5, _reporter);
			stmt.setString(6, _time);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertReminder Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static RejoinTask SQLgetRejoinTask(long _user_id, long _guild_id) {
		logger.trace("SQLgetRejoinTask launched. Params passed {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT * FROM reminder WHERE fk_user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
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
	
	public static int SQLDeleteRejoinTask(long _user_id, long _guild_id) {
		logger.trace("SQLDeleteRejoinTask launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM reminder WHERE fk_user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteRejoinTask Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetLanguages(String _lang) {
		logger.trace("SQLgetLanguages launched. Params passed {}", _lang);
		ArrayList<String> langs = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT lang, translation FROM languages_translation WHERE lang2 = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _lang);
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
	
	public static int SQLInsertTweetLog(long _message_id, long _tweet_id) {
		logger.trace("SQLInsertTweetLog launched. Passed params {}, {}", _message_id, _tweet_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO tweet_log (message_id, tweet_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _message_id);
			stmt.setLong(2, _tweet_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertTweetLog Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateTweetLogDeleted(long _message_id) {
		logger.trace("SQLUpdateTweetLogDeleted launched. Passed params {}", _message_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE tweet_log SET deleted = 1 WHERE message_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _message_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateTweetLogDeleted Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLIsTweetDeleted(long _tweet_id) {
		logger.trace("SQLIsTweetDeleted launched. Params passed {}", _tweet_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("SELECT deleted FROM tweet_log WHERE tweet_id = ? AND deleted = 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _tweet_id);
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
	
	public static int SQLUpdateTweetTimestamp(long _tweet_id) {
		logger.trace("SQLUpdateTweetLogDeleted launched. Passed params {}", _tweet_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE tweet_log SET timestamp = SYSDATE() WHERE tweet_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _tweet_id);
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
	
	public static void SQLBulkInsertCategories(List<Category> _categories) {
		logger.trace("SQLBulkInsertCategories launched. Array param passed");
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false); 
			String sql = ("INSERT INTO categories (category_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name)");
			stmt = myConn.prepareStatement(sql);
			for(final var category : _categories) {
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
	
	public static ArrayList<CategoryConf> SQLgetCategories(long _guild_id) {
		final var cachedCategories = Hashes.getCategories(_guild_id);
		if(cachedCategories == null) {
			logger.trace("SQLgetCategories launched. Params passed {}", _guild_id);
			ArrayList<CategoryConf> categories = new ArrayList<CategoryConf>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
				String sql = ("SELECT fk_category_id, fk_category_type FROM category_conf WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					categories.add(new CategoryConf(
						rs.getLong(1),
						rs.getString(2)
					));
				}
				Hashes.addCategories(_guild_id, categories);
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
	
	public static int SQLInsertCategory(long _category_id, String _name) {
		logger.trace("SQLInsertCategory launched. Params passed {}, {}", _category_id, _name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO categories (category_id, name) VALUES(?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _category_id);
			stmt.setString(2, _name);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertCategory Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteCategory(long _category_id) {
		logger.trace("SQLDeleteCategory launched. Params passed {}", _category_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM categories WHERE category_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _category_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteCategory Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateCategoryName(long _category_id, String _name) {
		logger.trace("SQLUpdateCategoryName launched. Params passed {}, {}", _category_id, _name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("UPDATE categories SET name = ? WHERE category_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _name);
			stmt.setLong(2, _category_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateCategoryName Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertCategoryConf(long _category_id, String _categoryType, long _guild_id) {
		logger.trace("SQLInsertCategoryConf launched. Params passed {}, {}, {}", _category_id, _categoryType, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("INSERT INTO category_conf(fk_category_id, fk_category_type, fk_guild_id) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE fk_category_type=VALUES(fk_category_type)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _category_id);
			stmt.setString(2, _categoryType);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertCategoryConf Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteCategoryConf(long _category_id) {
		logger.trace("SQLDeleteCategoryConf launched. Params passed {}", _category_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM category_conf WHERE fk_category_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _category_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteCategoryConf Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteAllCategoryConfs(long _guild_id) {
		logger.trace("SQLDeleteAllCategoryConf launched. Params passed {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			String sql = ("DELETE FROM category_conf WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
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
	
	//Transactions
	@SuppressWarnings("resource")
	public static int SQLLowerTotalWarning(long _guild_id, int _warning_id) {
		logger.trace("SQLLowerTotalWarning launched. Passed params {}, {}", _guild_id, _warning_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			String sql = ("UPDATE bancollect SET fk_warning_id = ? WHERE fk_guild_id = ? && fk_warning_id > ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _warning_id);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _warning_id);
			stmt.executeUpdate();

			String sql2 = ("DELETE FROM warnings WHERE warning_id > ?");
			stmt = myConn.prepareStatement(sql2);
			stmt.setInt(1, _warning_id);
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
	public static int SQLReplaceWordFilter(String _lang, String [] _words, long _guild_id, boolean delete) {
		logger.trace("SQLReplaceWordFilter launched. Passed params {}, array, {}, {}", _lang, _guild_id, delete);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			if(delete) {
				String sql = ("DELETE FROM filter WHERE fk_lang_abbrv = ? && fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setString(1, _lang);
				stmt.setLong(2, _guild_id);
				stmt.executeUpdate();
			}
			
			String sql2 = ("INSERT IGNORE INTO filter (word, fk_lang_abbrv, fk_guild_id) VALUES(?, ?, ?)");
			stmt = myConn.prepareStatement(sql2);
			
			for(String word : _words) {
				stmt.setString(1, word.toLowerCase());
				stmt.setString(2, _lang);
				stmt.setLong(3, _guild_id);
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
	public static int SQLReplaceNameFilter(String [] _words, boolean _kick, long _guild_id, boolean delete) {
		logger.trace("SQLReplaceNameFilter launched. Passed params array, {}, {}, {}", _kick, _guild_id, delete);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			if(delete) {
				String sql = ("DELETE FROM name_filter WHERE fk_guild_id = ? && kick = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				stmt.setBoolean(2, _kick);
				stmt.executeUpdate();
			}
			
			String sql2 = ("INSERT IGNORE INTO name_filter (word, kick, fk_guild_id) VALUES(?, ?, ?)");
			stmt = myConn.prepareStatement(sql2);
			
			for(String word : _words) {
				stmt.setString(1, word);
				stmt.setBoolean(2, _kick);
				stmt.setLong(3, _guild_id);
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
	public static int SQLReplaceFunnyNames(String [] _words, long _guild_id, boolean delete) {
		logger.trace("SQLReplaceFunnyNames launched. Passed params array, {}, {}", _guild_id, delete);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			if(delete) {
				String sql = ("DELETE FROM names WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
			}
			
			String sql2 = ("INSERT IGNORE INTO names (name, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql2);;
			
			for(String word : _words) {
				stmt.setString(1, word);
				stmt.setLong(2, _guild_id);
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
	public static int SQLReplaceURLBlacklist(String [] _urls, long _guild_id, boolean delete) {
		logger.trace("SQLReplaceURLBlacklist launched. Passed params array, {}, {}", _guild_id, delete);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			if(delete) {
				String sql = ("DELETE FROM url_blacklist WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				stmt.executeUpdate();
			}
			
			String sql2 = ("INSERT IGNORE INTO url_blacklist (url, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql2);
			for(String url : _urls) {
				stmt.setString(1, url.replaceAll("(http:\\/\\/|https:\\/\\/)", "").replaceAll("www.", "").replace("\\b\\/[\\w\\d=?!&#\\[\\]().,+_*';:@$\\/-]*\\b", ""));
				stmt.setLong(2, _guild_id);
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
	public static int SQLReplaceURLWhitelist(String [] _urls, long _guild_id, boolean delete) {
		logger.trace("SQLReplaceURLWhitelist launched. Passed params array, {}, {}", _guild_id, delete);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			if(delete) {
				String sql = ("DELETE FROM url_whitelist WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				stmt.executeUpdate();
			}
			
			String sql2 = ("INSERT IGNORE INTO url_whitelist (url, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql2);
			for(String url : _urls) {
				stmt.setString(1, url.replaceAll("(http:\\/\\/|https:\\/\\/)", "").replaceAll("www.", "").replace("\\b\\/[\\w\\d=?!&#\\[\\]().,+_*';:@$\\/-]*\\b", ""));
				stmt.setLong(2, _guild_id);
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
	public static int SQLReplaceTweetBlacklist(String [] _usernames, long _guild_id, boolean delete) {
		logger.trace("SQLReplaceTweetBlacklist launched. Passed params array, {}, {}", _guild_id, delete);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("Azrael", ip), username, password);
			myConn.setAutoCommit(false);
			if(delete) {
				String sql = ("DELETE FROM tweet_blacklist WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				stmt.executeUpdate();
			}
			
			String sql2 = ("INSERT IGNORE INTO tweet_blacklist (username, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql2);
			for(String username : _usernames) {
				stmt.setString(1, username);
				stmt.setLong(2, _guild_id);
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
}
