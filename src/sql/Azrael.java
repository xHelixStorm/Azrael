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
import constructors.Channels;
import constructors.History;
import constructors.NameFilter;
import constructors.RSS;
import constructors.User;
import constructors.Warning;
import constructors.Watchlist;
import core.Hashes;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.entities.Member;

public class Azrael {
	private static final Logger logger = LoggerFactory.getLogger(Azrael.class);
	
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
			logger.debug("SQLInsertActionLog launched. Passed params {}, {}, {}, {}", _event, _target_id, _guild_id, _description);
			Connection myConn = null;
			PreparedStatement stmt = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
				String sql = ("INSERT INTO action_log (log_id, event, target_id, guild_id, description, timestamp) VALUES(null, ?, ?, ?, ?, ?)");
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
	
	public static void SQLInsertHistory(long _user_id, long _guild_id, String _type, String _reason, long _penalty) {
		logger.debug("SQLInsertHistory launched. Passed params {}, {}, {}, {}, {}", _user_id, _guild_id, _type, _reason, _penalty);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO history (fk_user_id, fk_guild_id, type, reason, time, penalty) VALUES(?, ?, ?, ?, ?, "+(_penalty != 0 ? "?" : "NULL")+")");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, _type);
			stmt.setString(4, _reason);
			stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
			if(_penalty != 0)
				stmt.setLong(6, _penalty);
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertActionLog Exception", e);
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<History> SQLgetHistory(long _user_id, long _guild_id) {
		logger.debug("SQLgetHistory launched. Passed params {}, {}", _user_id, _guild_id);
		ArrayList<History> history = new ArrayList<History>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT type, reason, time, penalty FROM history WHERE fk_user_id = ? && fk_guild_id = ?");
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
						rs.getLong(4)
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
		logger.debug("SQLgetSingleActionEventCount launched. Passed params {}, {}, {}", _event, _target_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT COUNT(*) FROM action_log WHERE target_id = ? && guild_id = ? && event LIKE ?");
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
		logger.debug("SQLgetDoubleActionEventDescriptions launched. Passed params {}, {}, {}, {}", _event, _event2, _target_id, _guild_id);
		ArrayList<String> descriptions = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT description FROM action_log WHERE target_id = ? && (guild_id = ? || guild_id = 0) && (event LIKE ? || event LIKE ?) GROUP BY description");
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
		logger.debug("SQLgetSingleActionEventDescriptions launched. Passed params {}, {}, {}", _event, _target_id, _guild_id);
		ArrayList<String> descriptions = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT description FROM action_log WHERE target_id = ? && guild_id = ? && event LIKE ? GROUP BY description");
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
		logger.debug("SQLgetCriticalActionEvents launched. Passed params {}, {}", _target_id, _guild_id);
		ArrayList<String> descriptions = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT description, timestamp FROM action_log WHERE target_id = ? && guild_id = ? && (event LIKE \"MEMBER_KICK\" || event LIKE \"MEMBER_BAN_ADD\" || event LIKE \"MEMBER_BAN_REMOVE\" || event LIKE \"MEMBER_MUTE_ADD\") ORDER BY timestamp desc LIMIT 30");
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
		logger.debug("SQLgetSingleActionEventDescriptions launched. Passed params {}, {}, {}", _event, _target_id, _guild_id);
		ArrayList<String> descriptions = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT timestamp, description FROM action_log WHERE target_id = ? && guild_id = ? && event LIKE ? ORDER BY timestamp desc LIMIT 30");
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
	
	public static int SQLInsertUser(long _user_id, String _name, String _avatar, String _join_date) {
		logger.debug("SQLInsertUser launched. Passed params {}, {}, {}, {}", _user_id, _name, _avatar, _join_date);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO users (user_id, name, avatar_url, join_date) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), avatar_url=VALUES(avatar_url)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setString(2, _name);
			stmt.setString(3, _avatar);
			stmt.setString(4, _join_date);
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
		logger.debug("SQLBulkInsertUsers launched. Passed member list params");
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			myConn.setAutoCommit(false); 
			String sql = ("INSERT INTO users (user_id, name, avatar_url, join_date) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), avatar_url=VALUES(avatar_url)");
			stmt = myConn.prepareStatement(sql);
			for(Member member : members) {
				stmt.setLong(1, member.getUser().getIdLong());
				stmt.setString(2, member.getUser().getName()+"#"+member.getUser().getDiscriminator());
				stmt.setString(3, member.getUser().getEffectiveAvatarUrl());
				stmt.setString(4, member.getTimeJoined().format(DateTimeFormatter.ISO_LOCAL_DATE));
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
		logger.debug("SQLUpdateAvatar launched. Passed params {}, {}", _user_id, _avatar);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
	
	public static User SQLgetUser(String _name) {
		logger.debug("SqlgetUser launched. Passed params {}", _name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT user_id, name FROM users WHERE name LIKE ?");
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
		logger.debug("SQLgetUserThroughID launched. Passed params {}");
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM users WHERE user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _user_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return new User(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4));
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
	
	public static int SQLUpdateUser(Long _user_id, String _name) {
		logger.debug("SQLUpdateUser launched. Passed params {}, {}", _user_id, _name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
	
	public static long SQLgetGuild(long _guild_id) {
		logger.debug("SQLgetGuild launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT guild_id FROM guild WHERE guild_id= ?");
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
	
	public static int SQLInsertGuild(Long _guild_id, String _guild_name) {
		logger.debug("SQLInsertGuild launched. Passed params {}, {}", _guild_id, _guild_name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO guild VALUES (?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)");
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
		logger.debug("SQLgetNickname launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLInsertNickname launched. Passed params {}, {}, {}", _user_id, _guild_id, _nickname);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO nickname VALUES (?, ?, ?)");
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
		logger.debug("SQLUpdateNickname launched. Passed params {}, {}", _user_id, _guild_id, _nickname);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLDeleteNickname launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLgetData launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLgetMuted launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLgetMuted launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLgetMuted launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLgetMuted launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLInsertData launched. Passed params {}, {}, {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _warning_id, _ban_id, _timestamp, _unmute, _muted, _custom_time);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLDeleteData launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM bancollect WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteData Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateMuted(long _user_id, long _guild_id, boolean _muted) {
		logger.debug("SQLUpdateMuted launched. Passed params {}, {}, {}", _user_id, _guild_id, _muted);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLUpdateMutedAndCustomMuted launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _muted, _custom_time);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLUpdateMutedOnEnd launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _muted, _custom_time);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE bancollect SET muted = ?, custom_time = ? WHERE fk_user_id = ? && fk_guild_id = ? && muted = 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setBoolean(1, _muted);
			stmt.setBoolean(2, _custom_time);
			stmt.setLong(3, _user_id);
			stmt.setLong(4, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateMuted Exception", e);
			return 999;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateGuildLeft(long _user_id, long _guild_id, boolean _guildLeft) {
		logger.debug("SQLUpdateGuildLeft launched. Passed params {}, {}, {}", _user_id, _guild_id, _guildLeft);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLgetWarning launched. Passed params {}, {}", _guild_id, _warning_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLgetMaxWarning launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
	
	@SuppressWarnings("preview")
	public static int SQLInsertWarning(long _guild_id, int _warning_id) {
		logger.debug("SQLInsertWarning launched. Passed params {}, {}", _guild_id, _warning_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLUpdateWarning launched. Passed params {}, {}", _user_id, _guild_id, _warning_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLUpdateTimeOfWarning launched. Passed params {}, {}, {}", _guild_id, _warning_id, _mute_time);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLUpdateUnmute launched. Passed params {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _timestamp, _unmute, _muted, _custom_time);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLUpdateUnmute launched. Passed params {}, {}, {}", _user_id, _guild_id, _unmute);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLUpdateban launched. Passed params {}, {}, {}", _user_id, _guild_id, _ban_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLInsertChannels launched. Passed params {}, {}", _channel_id, _channel_name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
	
	public static int SQLDeleteChannels(long _channel_id) {
		logger.debug("SQLDeleteChannels launched. Passed params {}", _channel_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLInsertChannel launched. Passed params {}, {}, {}, {}", _channel_id, _guild_id, _channel_type);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLDeleteChannelConf launched. Passed params {}, {}", _channel_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLDeleteAllChannelConfs launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM channel_conf WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteAllChannelConfs Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertChannel_ConfURLCensoring(long _channel_id, long _guild_id, boolean _url_censoring) {
		logger.debug("SQLInsertChannel_ConfURLCensoring launched. Passed params {}, {}, {}, {}", _channel_id, _guild_id, _url_censoring);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLInsertChannel_ConfTXTCensoring launched. Passed params {}, {}, {}, {}", _channel_id, _guild_id, _txt_removal);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
	
	public static void SQLDeleteChannelType(String _channel_type, long _guild_id) {
		logger.debug("SQLDeleteChannelType launched. Passed params {}, {}", _channel_type, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM channel_conf WHERE fk_channel_type = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _channel_type);
			stmt.setLong(2, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteChannelType Exception", e);
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static synchronized ArrayList<Channels> SQLgetChannels(long _guild_id) {
		logger.debug("SQLgetChannels launched. Passed params {}", _guild_id);
		ArrayList<Channels> channels = new ArrayList<Channels>();
		if(Hashes.getChannels(_guild_id) == null) {
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
				return channels;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return Hashes.getChannels(_guild_id);
	}
	
	public static ArrayList<Channels> SQLgetChannelTypes() {
		logger.debug("SQLgetChannelTypes launched. No params");
		ArrayList<Channels> channels = new ArrayList<Channels>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
			return channels;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetExecutionID(long _guild_id) {
		logger.debug("SQLgetExecutionID launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT execution_id FROM command WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetExecutionID Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLgetCommandExecutionReaction(long _guild_id) {
		logger.debug("SQLgetCommandExecutionReaction launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT reactions FROM command WHERE guild_id = ?");
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
	
	public static int SQLInsertCommand(long _guild_id, int _execution_id) {
		logger.debug("SQLInsertCommand launched. Passed params {}, {}", _guild_id, _execution_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO command (guild_id, execution_id, reactions) VALUES (?, ?, 0) ON DUPLICATE KEY UPDATE execution_id=VALUES(execution_id)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _execution_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertCommand Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertCommand(long _guild_id, int _execution_id, boolean _reactions) {
		logger.debug("SQLInsertCommand launched. Passed params {}, {}, {}", _guild_id, _execution_id, _reactions);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO command (guild_id, execution_id, reactions) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE reactions=VALUES(reactions)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _execution_id);
			stmt.setBoolean(3, _reactions);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertCommand Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateReaction(long _guild_id, boolean _reactions) {
		logger.debug("SQLUpdateReaction launched. Passed params {}, {}", _guild_id, _reactions);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE command SET reactions = ? WHERE guild_id = ?");
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
		logger.debug("SQLgetChannel_Filter launched. Passed params {}", _channel_id);
		if(Hashes.getFilterLang(_channel_id) == null) {
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		return Hashes.getFilterLang(_channel_id);
	}
	
	public synchronized static ArrayList<String> SQLgetFilter(String _filter_lang, long _guild_id) {
		logger.debug("SQLgetFilter launched. Passed params {}, {}", _filter_lang, _guild_id);
		if(Hashes.getQuerryResult(_filter_lang+"_"+_guild_id) == null) {
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
				ArrayList<String> filter_words = new ArrayList<String>();
				String sql;
				if(_filter_lang.equals("all")) {
					sql = ("SELECT word FROM filter WHERE fk_guild_id = ?");
					stmt = myConn.prepareStatement(sql);
					stmt.setLong(1, _guild_id);
				}
				else{
					sql = ("SELECT word FROM filter WHERE fk_lang_abbrv LIKE ? && fk_guild_id = ?");
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
		return Hashes.getQuerryResult(_filter_lang+"_"+_guild_id);
	}
	
	public static int SQLInsertWordFilter(String _lang, String _word, long _guild_id) {
		logger.debug("SQLInsertWordFilter launched. Passed params {}, {}, {}", _lang, _word, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			var sql = "";
			if(!_lang.equals("all"))
				sql = ("INSERT INTO filter (filter_id, word, fk_lang_abbrv, fk_guild_id) VALUES(NULL, ?, ?, ?)");
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
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLDeleteWordFilterAllLang(String _word, long _guild_id) {
		logger.debug("SQLDeleteWordFilter launched. Passed params {}, {}", _word, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM filter WHERE word LIKE ? && fk_guild_id = ?");
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
		logger.debug("SQLDeleteWordFilter launched. Passed params {}, {}, {}", _lang, _word, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM filter WHERE word LIKE ? "+(!_lang.equals("all") ? "&& fk_lang_abbrv LIKE ? " : "")+"&& fk_guild_id = ?");
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
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public synchronized static ArrayList<String> SQLgetStaffNames(long _guild_id) {
		if(Hashes.getQuerryResult("staff-names_"+_guild_id) == null) {
			logger.debug("SQLgetStaffNames launched. Passed params {}", _guild_id);
			ArrayList<String> staff_names = new ArrayList<String>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		return Hashes.getQuerryResult("staff-names_"+_guild_id);
	}
	
	public static int SQLInsertStaffName(String _word, long _guild_id) {
		logger.debug("SQLInsertStaffName launched. Passed params {}, {}", _word, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO staff_name_filter (name_id, name, fk_guild_id) VALUES(NULL, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word.toLowerCase());
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertStaffName Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteStaffNames(String _word, long _guild_id) {
		logger.debug("SQLDeleteStaffNames launched. Passed params {}, {}", _word, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM staff_name_filter WHERE name LIKE ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word.toLowerCase());
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteStaffNames Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static int SQLReplaceStaffNames(String [] _words, long _guild_id, boolean delete) {
		logger.debug("SQLBatchInsertStaffNames launched. Passed params array, {}, {}", _guild_id, delete);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			myConn.setAutoCommit(false);
			if(delete) {
				String sql = ("DELETE FROM staff_name_filter WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				stmt.executeUpdate();
			}
			
			String sql2 = ("INSERT INTO staff_name_filter (name_id, name, fk_guild_id) VALUES(NULL, ?, ?)");
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
	
	public static void SQLInsertChannel_Filter(long _channel_id, String _filter_lang) {
		logger.debug("SQLInsertChannel_Filter launched. Passed params {}, {}", _channel_id, _filter_lang);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO channel_filter (fk_channel_id, fk_lang_abbrv) VALUES (?,?) ON DUPLICATE KEY UPDATE fk_lang_abbrv = VALUES(fk_lang_abbrv)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			stmt.setString(2, _filter_lang);
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertChannel_Filter Exception", e);
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteChannel_Filter(long _channel_id) {
		logger.debug("SQLDeleteChannel_Filter launched. Passed params {}", _channel_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM channel_filter WHERE fk_channel_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteChannel_Filter Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetFunnyNames(long _guild_id) {
		if(Hashes.getQuerryResult("funny-names_"+_guild_id) == null) {
			ArrayList<String> names = new ArrayList<String>();
			logger.debug("SQLgetFunnyNames launched. Passed params {}", _guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		return Hashes.getQuerryResult("funny-names_"+_guild_id);
	}
	
	public static int SQLInsertFunnyNames(String _word, long _guild_id) {
		logger.debug("SQLInsertFunnnynames launched. Passed params {}, {}", _word, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO names (name_id, name, fk_guild_id) VALUES(NULL, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertFunnyNamesException", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteFunnyNames(String _word, long _guild_id) {
		logger.debug("SQLDeleteFunnyNames launched. Passed params {}, {}", _word, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM names WHERE name LIKE ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteFunnyNames Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<NameFilter> SQLgetNameFilter(long _guild_id) {
		logger.debug("SQLgetNameFilter launched. Passed params {}", _guild_id);
		ArrayList<NameFilter> names = new ArrayList<NameFilter>();
		if(Hashes.getNameFilter(_guild_id) == null) {
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT word, kick FROM name_filter");
				stmt = myConn.prepareStatement(sql);
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
		return Hashes.getNameFilter(_guild_id);
	}
	
	public static int SQLInsertNameFilter(String _word, boolean _kick, long _guild_id) {
		logger.debug("SQLInsertNameFilter launched. Passed params {}, {}, {}", _word, _kick, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO name_filter (word_id, word, kick, fk_guild_id) VALUES(NULL, ?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word);
			stmt.setBoolean(2, _kick);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertNameFilter Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteNameFilter(String _word, boolean _kick, long _guild_id) {
		logger.debug("SQLDeleteNameFilter launched. Passed params {}, {}, {}", _word, _kick, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM name_filter WHERE word LIKE ? && fk_guild_id = ? && kick = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word);
			stmt.setLong(2, _guild_id);
			stmt.setBoolean(3, _kick);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteNameFilter Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static String SQLgetRandomName(long _guild_id) {
		logger.debug("SQLgetRandomName launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLgetFilterLanguages launched. No params passed");
		ArrayList<String> filter_lang = new ArrayList<String>();
		if(Hashes.getFilterLang(0) == null) {
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT language FROM filter_languages WHERE lang_abbrv NOT LIKE \"all\"");
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
		return Hashes.getFilterLang(0);
	}
	
	public static int SQLInsertRSS(String _url, long _guild_id, int _type) {
		logger.debug("SQLInsertRSS launched. Params passed {}, {}, {}", _url, _guild_id, _type);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO rss (url, fk_guild_id, format, type) VALUES (?, ?, ?, ?)");
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
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<RSS> SQLgetRSSFeeds(long _guild_id) {
		logger.debug("SQLgetRSSFeeds launched. Params passed {}", _guild_id);
		ArrayList<RSS> feeds = new ArrayList<RSS>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT url, format, type FROM rss WHERE fk_guild_id = ? ORDER BY timestamp asc");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				feeds.add(new RSS(rs.getString(1), rs.getString(2), rs.getInt(3)));
			}
			Hashes.addFeeds(_guild_id, feeds);
			return feeds;
		} catch (SQLException e) {
			logger.error("SQLgetRSSFeeds Exception", e);
			return feeds;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteRSSFeed(String _url, long _guild_id) {
		logger.debug("SQLDeleteRSSFeed launched. Params passed {}, {}", _url, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLUpdateRSSFormat launched. Params passed {}, {}, {}", _url, _guild_id, _format);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
	
	public static synchronized ArrayList<String> SQLgetURLBlacklist(long _guild_id) {
		if(Hashes.getURLBlacklist(_guild_id) == null) {
			logger.debug("SQLgetURLBlacklist launched. Passed params {}", _guild_id);
			ArrayList<String> urls = new ArrayList<String>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		return Hashes.getURLBlacklist(_guild_id);
	}
	
	public static int SQLInsertURLBlacklist(String _url, long _guild_id) {
		logger.debug("SQLInsertURLBlacklist launched. Passed params {}, {}", _url, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO url_blacklist (url, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _url.replaceAll("(http:\\/\\/|https:\\/\\/)", "").replaceAll("www.", "").replace("\\b\\/[\\w\\d=?!&#\\[\\]().,+_*';:@$\\/-]*\\b", ""));
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertURLBlacklist Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteURLBlacklist(String _url, long _guild_id) {
		logger.debug("SQLDeleteURLBlacklist launched. Passed params {}, {}", _url, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM url_blacklist WHERE url LIKE ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _url.replaceAll("(http:\\/\\/|https:\\/\\/)", "").replaceAll("www.", "").replace("\\b\\/[\\w\\d=?!&#\\[\\]().,+_*';:@$\\/-]*\\b", ""));
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteURLBlacklist Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static synchronized ArrayList<String> SQLgetURLWhitelist(long _guild_id) {
		if(Hashes.getURLWhitelist(_guild_id) == null) {
			logger.debug("SQLgetURLWhitelist launched. Passed params {}", _guild_id);
			ArrayList<String> urls = new ArrayList<String>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		return Hashes.getURLWhitelist(_guild_id);
	}
	
	public static int SQLInsertURLWhitelist(String _url, long _guild_id) {
		logger.debug("SQLInsertURLWhitelist launched. Passed params {}, {}", _url, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO url_whitelist (url, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _url.replaceAll("(http:\\/\\/|https:\\/\\/)", "").replaceAll("www.", "").replaceAll("\\b\\/[\\w\\d=?!&#\\[\\]().,+_*';:@$\\/-]*\\b", ""));
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertURLWhitelist Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteURLWhitelist(String _url, long _guild_id) {
		logger.debug("SQLDeleteURLWhitelist launched. Passed params {}, {}", _url, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM url_whitelist WHERE url LIKE ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _url.replaceAll("(http:\\/\\/|https:\\/\\/)", "").replaceAll("www.", "").replace("\\b\\/[\\w\\d=?!&#\\[\\]().,+_*';:@$\\/-]*\\b", ""));
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteURLWhitelist Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static synchronized ArrayList<String> SQLgetTweetBlacklist(long _guild_id) {
		if(Hashes.getTweetBlacklist(_guild_id) == null) {
			logger.debug("SQLgetTweetBlacklist launched. Passed params {}", _guild_id);
			ArrayList<String> urls = new ArrayList<String>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		return Hashes.getTweetBlacklist(_guild_id);
	}
	
	public static int SQLInsertTweetBlacklist(String _username, long _guild_id) {
		logger.debug("SQLInsertTweetBlacklist launched. Passed params {}, {}", _username, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO tweet_blacklist (username, fk_guild_id) VALUES(?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, (_username.startsWith("@") ? _username : "@"+_username));
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInsertTweetBlacklist Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteTweetBlacklist(String _username, long _guild_id) {
		logger.debug("SQLDeleteTweetBlacklist launched. Passed params {}, {}", _username, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM tweet_blacklist WHERE username LIKE ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, (_username.startsWith("@") ? _username : "@"+_username));
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLDeleteTweetBlacklist Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static synchronized void SQLgetWholeWatchlist() {
		logger.debug("SQLgetWholeWatchlist launched. No params have been passed!");
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLgetWholeWatchlist launched. Params passed {}, {}", _guild_id, _highPrivileges);
		ArrayList<String> watchlist = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM watchlist WHERE guild_id = ? && higher_privileges = ?");
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
		logger.debug("SQLInsertWatchlist launched. Passed params {}, {}, {}, {}, {}", _user_id, _guild_id, _level, _watchChannel, _higherPrivileges);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLDeleteWatchlist launched. Passed params {}, {}, {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
	
	//Transactions
	@SuppressWarnings("resource")
	public static int SQLLowerTotalWarning(long _guild_id, int _warning_id) {
		logger.debug("SQLLowerTotalWarning launched. Passed params {}, {}", _guild_id, _warning_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLReplaceWordFilter launched. Passed params {}, array, {}, {}", _lang, _guild_id, delete);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			myConn.setAutoCommit(false);
			if(delete) {
				String sql = ("DELETE FROM filter WHERE fk_lang_abbrv LIKE ? && fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setString(1, _lang);
				stmt.setLong(2, _guild_id);
				stmt.executeUpdate();
			}
			
			String sql2 = ("INSERT INTO filter (filter_id, word, fk_lang_abbrv, fk_guild_id) VALUES(NULL, ?, ?, ?)");
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
		logger.debug("SQLReplaceNameFilter launched. Passed params array, {}, {}, {}", _kick, _guild_id, delete);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			myConn.setAutoCommit(false);
			if(delete) {
				String sql = ("DELETE FROM name_filter WHERE fk_guild_id = ? && kick = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				stmt.setBoolean(2, _kick);
				stmt.executeUpdate();
			}
			
			String sql2 = ("INSERT INTO name_filter (word_id, word, kick, fk_guild_id) VALUES(NULL, ?, ?, ?)");
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
		logger.debug("SQLReplaceFunnyNames launched. Passed params array, {}, {}", _guild_id, delete);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			myConn.setAutoCommit(false);
			if(delete) {
				String sql = ("DELETE FROM names WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
			}
			
			String sql2 = ("INSERT INTO names (name_id, name, fk_guild_id) VALUES(NULL, ?, ?)");
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
		logger.debug("SQLReplaceURLBlacklist launched. Passed params array, {}, {}", _guild_id, delete);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			myConn.setAutoCommit(false);
			if(delete) {
				String sql = ("DELETE FROM url_blacklist WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				stmt.executeUpdate();
			}
			
			String sql2 = ("INSERT INTO url_blacklist (url, fk_guild_id) VALUES(?, ?)");
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
		logger.debug("SQLReplaceURLWhitelist launched. Passed params array, {}, {}", _guild_id, delete);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			myConn.setAutoCommit(false);
			if(delete) {
				String sql = ("DELETE FROM url_whitelist WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				stmt.executeUpdate();
			}
			
			String sql2 = ("INSERT INTO url_whitelist (url, fk_guild_id) VALUES(?, ?)");
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
		logger.debug("SQLReplaceTweetBlacklist launched. Passed params array, {}, {}", _guild_id, delete);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			myConn.setAutoCommit(false);
			if(delete) {
				String sql = ("DELETE FROM tweet_blacklist WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				stmt.executeUpdate();
			}
			
			String sql2 = ("INSERT INTO tweet_blacklist (username, fk_guild_id) VALUES(?, ?)");
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
