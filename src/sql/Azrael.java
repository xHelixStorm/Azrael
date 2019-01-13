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

import core.Bancollect;
import core.Channels;
import core.Hashes;
import core.RSS;
import core.User;
import core.Warning;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.entities.Member;

public class Azrael {
	private static final Logger logger = LoggerFactory.getLogger(Azrael.class);
	
	private static String username = IniFileReader.getSQLUsername();
	private static String password = IniFileReader.getSQLPassword();
	
	
	public static void SQLconnection(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			logger.error("JDBC driver couldn't be loaded", e);
		}
	}
	
	public static synchronized void SQLInsertActionLog(String _event, long _target_id, long _guild_id, String _description) {
		if(IniFileReader.getActionLog()) {
			logger.debug("Azrael.SQLInsertActionLog launched. Passed params {}, {}, {}, {}", _event, _target_id, _guild_id, _description);
			Connection myConn = null;
			PreparedStatement stmt = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
				String sql = ("INSERT INTO action_log (log_id, event, target_id, guild_id, description, timestamp) VALUES(null, ?, ?, ?, ?, ?)");
				Timestamp action_time = new Timestamp(System.currentTimeMillis());
				stmt = myConn.prepareStatement(sql);
				stmt.setString(1, _event);
				stmt.setLong(2, _target_id);
				stmt.setLong(3, _guild_id);
				stmt.setString(4, _description);
				stmt.setTimestamp(5, action_time);
				stmt.executeUpdate();
			} catch (SQLException e) {
				logger.error("Azrael.SQLInsertActionLog Exception", e);
			} finally {
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
	}
	
	public static int SQLgetSingleActionEventCount(String _event, long _target_id, long _guild_id) {
		logger.debug("Azrael.SQLgetSingleActionEventCount launched. Passed params {}, {}, {}", _event, _target_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT COUNT(*) FROM action_log WHERE target_id = ? && guild_id = ? && event LIKE ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _target_id);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, _event);
			rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("Azrael.SQLgetSingleActionEventCount Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetDoubleActionEventDescriptions(String _event, String _event2, long _target_id, long _guild_id) {
		logger.debug("Azrael.SQLgetDoubleActionEventDescriptions launched. Passed params {}, {}, {}, {}", _event, _event2, _target_id, _guild_id);
		ArrayList<String> descriptions = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT description FROM action_log WHERE target_id = ? && (guild_id = ? || guild_id = 0) && (event LIKE ? || event LIKE ?) GROUP BY description");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _target_id);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, _event);
			stmt.setString(4, _event2);
			rs = stmt.executeQuery();
			while(rs.next()){
				descriptions.add(rs.getString(1));
			}
			return descriptions;
		} catch (SQLException e) {
			logger.error("Azrael.SQLgetDoubleActionEventDescription Exception", e);
			return descriptions;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetSingleActionEventDescriptions(String _event, long _target_id, long _guild_id) {
		logger.debug("Azrael.SQLgetSingleActionEventDescriptions launched. Passed params {}, {}, {}", _event, _target_id, _guild_id);
		ArrayList<String> descriptions = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT description FROM action_log WHERE target_id = ? && guild_id = ? && event LIKE ? GROUP BY description");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _target_id);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, _event);
			rs = stmt.executeQuery();
			while(rs.next()){
				descriptions.add(rs.getString(1));
			}
			return descriptions;
		} catch (SQLException e) {
			logger.error("Azrael.SQLgetSingleActionEventDescriptions Exception", e);
			return descriptions;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	public static ArrayList<String> SQLgetCriticalActionEvents(long _target_id, long _guild_id) {
		logger.debug("Azrael.SQLgetCriticalActionEvents launched. Passed params {}, {}", _target_id, _guild_id);
		ArrayList<String> descriptions = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT description, timestamp FROM action_log WHERE target_id = ? && guild_id = ? && (event LIKE \"MEMBER_KICK\" || event LIKE \"MEMBER_BAN_ADD\" || event LIKE \"MEMBER_BAN_REMOVE\" || event LIKE \"MEMBER_MUTE_ADD\") ORDER BY timestamp desc LIMIT 30");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _target_id);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			while(rs.next()){
				descriptions.add("`["+rs.getTimestamp(2).toString()+"] - "+rs.getString(1)+"`");
			}
			return descriptions;
		} catch (SQLException e) {
			logger.error("Azrael.SQLgetCriticalActionEvents Exception", e);
			return descriptions;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertUser(long _user_id, String _name, String _avatar, String _join_date){
		logger.debug("Azrael.SQLInsertUser launched. Passed params {}, {}, {}, {}", _user_id, _name, _avatar, _join_date);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO users (user_id, name, avatar_url, join_date) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), avatar_url=VALUES(avatar_url)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setString(2, _name);
			stmt.setString(3, _avatar);
			stmt.setString(4, _join_date);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLInsertUser Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLBulkInsertUsers(List<Member> members){
		logger.debug("Azrael.SQLBulkInsertUsers launched. Passed member list params");
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			myConn.setAutoCommit(false); 
			String sql = ("INSERT INTO users (user_id, name, avatar_url, join_date) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), avatar_url=VALUES(avatar_url)");
			stmt = myConn.prepareStatement(sql);
			for(Member member : members){
				stmt.setLong(1, member.getUser().getIdLong());
				stmt.setString(2, member.getUser().getName()+"#"+member.getUser().getDiscriminator());
				stmt.setString(3, member.getUser().getEffectiveAvatarUrl());
				stmt.setString(4, member.getJoinDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
				stmt.addBatch();
			}
			stmt.executeBatch();
			myConn.commit();
		} catch (SQLException e) {
			logger.error("Azrael.SQLBulkInsertUsers Exception", e);
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateAvatar(long _user_id, String _avatar){
		logger.debug("Azrael.SQLUpdateAvatar launched. Passed params {}, {}", _user_id, _avatar);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE users SET avatar_url = ? WHERE user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _avatar);
			stmt.setLong(2, _user_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLUpdateAvatar Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static User SQLgetUser(String _name){
		logger.debug("Azrael.SqlgetUser launched. Passed params {}", _name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT user_id, name FROM users WHERE name LIKE ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _name);
			rs = stmt.executeQuery();
			if(rs.next()){
				return new User(rs.getLong(1), rs.getString(2));
			}
			return null;
		} catch (SQLException e) {
			logger.error("Azrael.SQLgetUser Exception", e);
			return null;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static User SQLgetUserThroughID(String _user_id){
		logger.debug("Azrael.SQLgetUserThroughID launched. Passed params {}");
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM users WHERE user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _user_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				return new User(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4));
			}
			return null;
		} catch (SQLException e) {
			logger.error("Azrael.SQLgetUserThroughID Exception", e);
			return null;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUser(Long _user_id, String _name){
		logger.debug("Azrael.SQLUpdateUser launched. Passed params {}, {}", _user_id, _name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE users SET name = ? WHERE user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _name);
			stmt.setLong(2, _user_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLUpdateUser Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertGuild(Long _guild_id, String _guild_name){
		logger.debug("Azrael.SQLInsertGuild launched. Passed params {}, {}", _guild_id, _guild_name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO guild VALUES (?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setString(2, _guild_name);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLInsertGuild Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static String SQLgetNickname(Long _user_id, Long _guild_id){
		logger.debug("Azrael.SQLgetNickname launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT nickname FROM nickname WHERE fk_user_id= ? && fk_guild_id= ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getString(1);
			}
			return "";
		} catch (SQLException e) {
			logger.error("Azrael.SQLgetNickname Exception", e);
			return "";
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertNickname(Long _user_id, Long _guild_id, String _nickname){
		logger.debug("Azrael.SQLInsertNickname launched. Passed params {}, {}, {}", _user_id, _guild_id, _nickname);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO nickname VALUES (?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, _nickname);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLInsertNickname Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateNickname(Long _user_id, Long _guild_id, String _nickname){
		logger.debug("Azrael.SQLUpdateNickname launched. Passed params {}, {}", _user_id, _guild_id, _nickname);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE nickname SET nickname = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _nickname);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLUpdateNickname Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteNickname(Long _user_id, Long _guild_id){
		logger.debug("Azrael.SQLDeleteNickname launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM nickname WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLDeleteNickname Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static Bancollect SQLgetData(Long _user_id, Long _guild_id){
		logger.debug("Azrael.SQLgetData launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT fk_user_id, fk_guild_id, fk_warning_id, fk_ban_id, timestamp, unmute, muted, custom_time FROM bancollect WHERE fk_user_id= ? && fk_guild_id= ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				return new Bancollect(rs.getLong(1), rs.getLong(2), rs.getInt(3), rs.getInt(4), rs.getTimestamp(5), rs.getTimestamp(6), rs.getBoolean(7), rs.getBoolean(8));
			}
			return new Bancollect();
		} catch (SQLException e) {
			logger.error("Azrael.SQLgetData Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLgetMuted(long _user_id, long _guild_id){
		logger.debug("Azrael.SQLgetMuted launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT muted FROM bancollect WHERE fk_user_id= ? && fk_guild_id= ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getBoolean(1);
			}
			return false;
		} catch (SQLException e) {
			logger.error("Azrael.SQLgetMuted Exception", e);
			return false;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertData(long _user_id, long _guild_id, int _warning_id, int _ban_id, Timestamp _timestamp, Timestamp _unmute, boolean _muted, boolean _custom_time){
		logger.debug("Azrael.SQLInsertData launched. Passed params {}, {}, {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _warning_id, _ban_id, _timestamp, _unmute, _muted, _custom_time);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO bancollect VALUES (NULL, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE fk_warning_id=VALUES(fk_warning_id), fk_ban_id=VALUES(fk_ban_id), timestamp=VALUES(timestamp), unmute=VALUES(unmute), muted=VALUES(muted), custom_time=VALUES(custom_time)");
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
			logger.error("Azrael.SQLInsertData Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteData(long _user_id, long _guild_id) {
		logger.debug("Azrael.SQLDeleteData launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM bancollect WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLDeleteData Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateMuted(long _user_id, long _guild_id, boolean _muted, boolean _custom_time){
		logger.debug("Azrael.SQLUpdateMuted launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _muted, _custom_time);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE bancollect SET muted = ?, custom_time = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setBoolean(1, _muted);
			stmt.setBoolean(2, _custom_time);
			stmt.setLong(3, _user_id);
			stmt.setLong(4, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLUpdateMuted Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateMutedOnEnd(long _user_id, long _guild_id, boolean _muted, boolean _custom_time){
		logger.debug("Azrael.SQLUpdateMutedOnEnd launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _muted, _custom_time);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE bancollect SET muted = ?, custom_time = ? WHERE fk_user_id = ? && fk_guild_id = ? && muted = 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setBoolean(1, _muted);
			stmt.setBoolean(2, _custom_time);
			stmt.setLong(3, _user_id);
			stmt.setLong(4, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLUpdateMuted Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static Warning SQLgetWarning(long _guild_id, int _warning_id) {
		logger.debug("Azrael.SQLgetWarning launched. Passed params {}, {}", _guild_id, _warning_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT warning_id, mute_time, description FROM warnings WHERE fk_guild_id = ? && warning_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _warning_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				return new Warning(rs.getInt(1), rs.getDouble(2), rs.getString(3));
			}
			return new Warning();
		} catch (SQLException e) {
			logger.error("Azrael.SQLgetWarning Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetMaxWarning(long _guild_id) {
		logger.debug("Azrael.SQLgetMaxWarning launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT MAX(warning_id) FROM warnings WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("Azrael.SQLgetMaxWarning Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertWarning(long _guild_id, int _warning_id) {
		logger.debug("Azrael.SQLInsertWarning launched. Passed params {}, {}", _guild_id, _warning_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = "";
			switch(_warning_id) {
				case 1: 
					sql = ("INSERT INTO warnings (fk_guild_id, warning_id, mute_time, description) VALUES"
							+ "(?, 1, 0, \"first warning\") ON DUPLICATE KEY UPDATE description=VALUES(description)");
					stmt = myConn.prepareStatement(sql);
					stmt.setLong(1, _guild_id);
					break;
				case 2:
					sql = ("INSERT INTO warnings (fk_guild_id, warning_id, mute_time, description) VALUES"
							+ "(?, 1, 0, \"first warning\"),"
							+ "(?, 2, 0, \"second warning\") ON DUPLICATE KEY UPDATE description=VALUES(description)");
					stmt = myConn.prepareStatement(sql);
					stmt.setLong(1, _guild_id);
					stmt.setLong(2, _guild_id);
					break;
				case 3:
					sql = ("INSERT INTO warnings (fk_guild_id, warning_id, mute_time, description) VALUES"
							+ "(?, 1, 0, \"first warning\"),"
							+ "(?, 2, 0, \"second warning\"),"
							+ "(?, 3, 0, \"third warning\") ON DUPLICATE KEY UPDATE description=VALUES(description)");
					stmt = myConn.prepareStatement(sql);
					stmt.setLong(1, _guild_id);
					stmt.setLong(2, _guild_id);
					stmt.setLong(3, _guild_id);
					break;
				case 4:
					sql = ("INSERT INTO warnings (fk_guild_id, warning_id, mute_time, description) VALUES"
							+ "(?, 1, 0, \"first warning\"),"
							+ "(?, 2, 0, \"second warning\"),"
							+ "(?, 3, 0, \"third warning\"),"
							+ "(?, 4, 0, \"fourth warning\") ON DUPLICATE KEY UPDATE description=VALUES(description)");
					stmt = myConn.prepareStatement(sql);
					stmt.setLong(1, _guild_id);
					stmt.setLong(2, _guild_id);
					stmt.setLong(3, _guild_id);
					stmt.setLong(4, _guild_id);
					break;
				case 5:
					sql = ("INSERT INTO warnings (fk_guild_id, warning_id, mute_time, description) VALUES"
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
			}
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLInsertWarning Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateWarning(long _user_id, long _guild_id, int _warning_id){
		logger.debug("Azrael.SQLUpdateWarning launched. Passed params {}, {}", _user_id, _guild_id, _warning_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE bancollect SET fk_warning_id = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _warning_id);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLUpdateWarning Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateMuteTimeOfWarning(long _guild_id, int _warning_id, long _mute_time) {
		logger.debug("Azrael.SQLUpdateTimeOfWarning launched. Passed params {}, {}, {}", _guild_id, _warning_id, _mute_time);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE warnings SET mute_time = ? WHERE fk_guild_id = ? && warning_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _mute_time);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _warning_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLUpdateMuteTimeOfWarning Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateUnmute(Long _user_id, Long _guild_id, Timestamp _timestamp, Timestamp _unmute, boolean _muted, boolean _custom_time){
		logger.debug("Azrael.SQLUpdateUnmute launched. Passed params {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _timestamp, _unmute, _muted, _custom_time);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
			logger.error("Azrael.SQLUpdateUnmute Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateBan(Long _user_id, Long _guild_id, Integer _ban_id){
		logger.debug("Azrael.SQLUpdateban launched. Passed params {}, {}, {}", _user_id, _guild_id, _ban_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE bancollect SET fk_ban_id = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _ban_id);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLUpdateBan Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertChannels(long _channel_id, String _channel_name){
		logger.debug("Azrael.SQLInsertChannels launched. Passed params {}, {}", _channel_id, _channel_name);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO channels (channel_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			stmt.setString(2, _channel_name);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLInsertChannels Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertChannel_Conf(long _channel_id, long _guild_id, String _channel_type){
		logger.debug("Azrael.SQLInsertChannel launched. Passed params {}, {}", _channel_id, _guild_id, _channel_type);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO channel_conf (fk_channel_id, fk_channel_type, fk_guild_id) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE fk_channel_type = VALUES(fk_channel_type)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			stmt.setString(2, _channel_type);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLInsertChannel_Conf Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static long SQLgetChannelID(long _guild_id, String _channel_type){
		logger.debug("Azrael.SQLgetChannelID launched. Passed params {}, {}", _guild_id, _channel_type);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT fk_channel_id FROM channel_conf WHERE fk_channel_type = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _channel_type);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getLong(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("Azrael.SQLgetChannelID Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static String SQLgetTwoChanneIDs(long _guild_id, String _channel_type, String _channel_type2){
		logger.debug("Azrael.SQLgetTwoChannelIDs launched. Passed params {}, {}, {}", _guild_id, _channel_type, _channel_type2);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT (SELECT fk_channel_id FROM channel_conf WHERE fk_channel_type LIKE ? && fk_guild_id = ?) AS channel1, (SELECT fk_channel_id FROM channel_conf WHERE fk_channel_type = ? && fk_guild_id = ?) AS channel2 FROM channel_conf LIMIT 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _channel_type);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, _channel_type2);
			stmt.setLong(4, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getString(1)+"_"+rs.getString(2);
			}
			return "0_0";
		} catch (SQLException e) {
			logger.error("Azrael.SQLgetTwoChannelIDs Exception", e);
			return "0_0";
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLDeleteChannelType(String _channel_type, long _guild_id) {
		logger.debug("Azrael.SQLDeleteChannelType launched. Passed params {}, {}", _channel_type, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM channel_conf WHERE fk_channel_type = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _channel_type);
			stmt.setLong(2, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLDeleteChannelType Exception", e);
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Channels> SQLgetChannels(long _guild_id){
		logger.debug("Azrael.SQLgetChannels launched. Passed params {}", _guild_id);
		ArrayList<Channels> channels = new ArrayList<Channels>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM all_channels WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			while(rs.next()){
				Channels channelProperties = new Channels();
				channelProperties.setChannel_ID(rs.getLong(1));
				channelProperties.setChannel_Name(rs.getString(2));
				channelProperties.setChannel_Type(rs.getString(3));
				channelProperties.setChannel_Type_Name(rs.getString(4));
				channelProperties.setGuild_ID(rs.getLong(5));
				channelProperties.setGuild_Name(rs.getString(6));
				channelProperties.setLang_Filter(rs.getString(7));
				channels.add(channelProperties);
			}
			return channels;
		} catch (SQLException e) {
			logger.error("Azrael.SQLgetChannels Exception", e);
			return channels;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Channels> SQLgetChannelTypes(){
		logger.debug("Azrael.SQLgetChannelTypes launched. No params");
		ArrayList<Channels> channels = new ArrayList<Channels>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM channeltypes");
			stmt = myConn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while(rs.next()){
				Channels channelProperties = new Channels();
				channelProperties.setChannel_Type(rs.getString(1));
				channelProperties.setChannel_Type_Name(rs.getString(2));
				channels.add(channelProperties);
			}
			return channels;
		} catch (SQLException e) {
			logger.error("Azrael.SQLgetChannelTypes Exception", e);
			return channels;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetExecutionID(long _guild_id){
		logger.debug("Azrael.SQLgetExecutionID launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT execution_id FROM command WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("Azrael.SQLgetExecutionID Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLgetCommandExecutionReaction(long _guild_id) {
		logger.debug("Azrael.SQLgetCommandExecutionReaction launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT reactions FROM command WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getBoolean(1);
			}
			return false;
		} catch (SQLException e) {
			logger.error("Azrael.SQLgetCommandExecutionReaction Exception", e);
			return false;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertCommand(long _guild_id, int _execution_id){
		logger.debug("Azrael.SQLInsertCommand launched. Passed params {}, {}", _guild_id, _execution_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO command (guild_id, execution_id, reactions) VALUES (?, ?, 0) ON DUPLICATE KEY UPDATE execution_id=VALUES(execution_id)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _execution_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLInsertCommand Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLInsertCommand(long _guild_id, int _execution_id, boolean _reactions){
		logger.debug("Azrael.SQLInsertCommand launched. Passed params {}, {}, {}", _guild_id, _execution_id, _reactions);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO command (guild_id, execution_id, reactions) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE reactions=VALUES(reactions)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _execution_id);
			stmt.setBoolean(3, _reactions);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLInsertCommand Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUpdateReaction(long _guild_id, boolean _reactions) {
		logger.debug("Azrael.SQLUpdateReaction launched. Passed params {}, {}", _guild_id, _reactions);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE command SET reactions = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setBoolean(1, _reactions);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLUpdateReaction Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public synchronized static void SQLgetChannel_Filter(long _channel_id){
		logger.debug("Azrael.SQLgetChannel_Filter launched. Passed params {}", _channel_id);
		if(Hashes.getFilterLang(_channel_id) == null){
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT * FROM channel_filter WHERE fk_channel_id = ?");
				ArrayList<String> filter_lang = new ArrayList<String>();
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _channel_id);
				rs = stmt.executeQuery();
				while(rs.next()){
					filter_lang.add(rs.getString(2));
				}
				Hashes.addFilterLang(_channel_id, filter_lang);
			} catch (SQLException e) {
				logger.error("Azrael.SQLgetChannel_Filter Exception", e);
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
	}
	
	public synchronized static void SQLgetFilter(String _filter_lang, long _guild_id){
		logger.debug("Azrael.SQLgetFilter launched. Passed params {}, {}", _filter_lang, _guild_id);
		if(Hashes.getQuerryResult(_filter_lang+"_"+_guild_id) == null) {
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
				ArrayList<String> filter_words = new ArrayList<String>();
				String sql;
				if(_filter_lang.equals("all")){
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
				while(rs.next()){
					filter_words.add(rs.getString(1));
				}
				Hashes.addQuerryResult(_filter_lang+"_"+_guild_id, filter_words);
			} catch (SQLException e) {
				logger.error("Azrael.SQLgetFilter Exception", e);
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
	}
	
	public static int SQLInsertWordFilter(String _lang, String _word, long _guild_id) {
		logger.debug("Azrael.SQLInsertWordFilter launched. Passed params {}, {}, {}", _lang, _word, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO filter (filter_id, word, fk_lang_abbrv, fk_guild_id) VALUES(NULL, ?, ?, ?) ON DUPLICATE KEY UPDATE word=VALUES(word)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word.toLowerCase());
			stmt.setString(2, _lang);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLInsertWordFilter Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteWordFilter(String _lang, String _word, long _guild_id) {
		logger.debug("Azrael.SQLDeleteWordFilter launched. Passed params {}, {}. {}", _lang, _word, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM filter WHERE word LIKE ? && fk_lang_abbrv LIKE ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word);
			stmt.setString(2, _lang);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLDeleteWordFilter Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLDeleteLangWordFilter(String _lang, long _guild_id) {
		logger.debug("Azrael.SQLDeleteLangWordFilter launched. Passed params {}, {}", _lang, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM filter WHERE fk_lang_abbrv LIKE ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _lang);
			stmt.setLong(2, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLDeleteLangWordFilter Exception", e);
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public synchronized static ArrayList<String> SQLgetStaffNames(long _guild_id){
		if(Hashes.getQuerryResult("staff-names_"+_guild_id) == null) {
			logger.debug("Azrael.SQLgetStaffNames launched. Passed params {}", _guild_id);
			ArrayList<String> staff_names = new ArrayList<String>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT name FROM staff_name_filter WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				rs = stmt.executeQuery();
				while(rs.next()){
					staff_names.add(rs.getString(1));
				}
				Hashes.addQuerryResult("staff-names_"+_guild_id, staff_names);
				return staff_names;
			} catch (SQLException e) {
				logger.error("Azrael.SQLgetStaffNames Exception", e);
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
		logger.debug("Azrael.SQLInsertStaffName launched. Passed params {}, {}", _word, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO staff_name_filter (name_id, name, fk_guild_id) VALUES(NULL, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word.toLowerCase());
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLInsertStaffName Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteStaffNames(String _word, long _guild_id) {
		logger.debug("Azrael.SQLDeleteStaffNames launched. Passed params {}, {}", _word, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM staff_name_filter WHERE name LIKE ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word.toLowerCase());
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLDeleteStaffNames Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLDeleteWholeStaffNames(long _guild_id) {
		logger.debug("Azrael.SQLDeleteWholeStaffNames launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM staff_name_filter WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLDeleteWholeStaffNames Exception", e);
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLBatchInsertStaffNames(ArrayList<String> _words, long _guild_id) {
		logger.debug("Azrael.SQLBatchInsertStaffNames launched. Passed params array, {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			myConn.setAutoCommit(false);
			String sql = ("INSERT INTO staff_name_filter (name_id, name, fk_guild_id) VALUES(NULL, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)");
			stmt = myConn.prepareStatement(sql);
			for(String word : _words){
				stmt.setString(1, word.toLowerCase());
				stmt.setLong(2, _guild_id);
				stmt.addBatch();
			}
			stmt.executeBatch();
			myConn.commit();
		} catch (SQLException e) {
			logger.error("Azrael.SQLBatchInsertStaffNames Exception", e);
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertChannel_Filter(long _channel_id, String _filter_lang){
		logger.debug("Azrael.SQLInsertChannel_Filter launched. Passed params {}, {}", _channel_id, _filter_lang);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO channel_filter (fk_channel_id, fk_lang_abbrv) VALUES (?,?) ON DUPLICATE KEY UPDATE fk_lang_abbrv = VALUES(fk_lang_abbrv)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			stmt.setString(2, _filter_lang);
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLInsertChannel_Filter Exception", e);
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLDeleteChannel_Filter(long _channel_id){
		logger.debug("Azrael.SQLDeleteChannel_Filter launched. Passed params {}", _channel_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM channel_filter WHERE fk_channel_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLDeleteChannel_Filter Exception", e);
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetFunnyNames(long _guild_id) {
		if(Hashes.getQuerryResult("funny-names_"+_guild_id) == null) {
			ArrayList<String> names = new ArrayList<String>();
			logger.debug("Azrael.SQLgetFunnyNames launched. Passed params {}", _guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT name FROM names WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				rs = stmt.executeQuery();
				while(rs.next()){
					names.add(rs.getString(1));
				}
				Hashes.addQuerryResult("funny-names_"+_guild_id, names);
				return names;
			} catch (SQLException e) {
				logger.error("Azrael.SQLgetFunnyNames Exception", e);
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
		logger.debug("Azrael.SQLInsertFunnnynames launched. Passed params {}, {}", _word, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO names (name_id, name, fk_guild_id) VALUES(NULL, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLInsertFunnyNamesException", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteFunnyNames(String _word, long _guild_id) {
		logger.debug("Azrael.SQLDeleteFunnyNames launched. Passed params {}, {}", _word, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM names WHERE name LIKE ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLDeleteFunnyNames Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLDeleteWholeFunnyNames(long _guild_id) {
		logger.debug("Azrael.SQLDeleteWholeFunnyNames launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM names WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLDeleteWholeFunnyNames Exception", e);
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetNameFilter(long _guild_id) {
		logger.debug("Azrael.SQLgetNameFilter launched. Passed params {}", _guild_id);
		ArrayList<String> names = new ArrayList<String>();
		if(Hashes.getQuerryResult("bad-names_"+_guild_id) == null) {
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT word FROM name_filter");
				stmt = myConn.prepareStatement(sql);
				rs = stmt.executeQuery();
				while(rs.next()){
					names.add(rs.getString(1));
				}
				Hashes.addQuerryResult("bad-names_"+_guild_id, names);
				return names;
			} catch (SQLException e) {
				logger.error("Azrael.SQLgetNameFilter Exception", e);
				return names;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return Hashes.getQuerryResult("bad-names_"+_guild_id);
	}
	
	public static int SQLInsertNameFilter(String _word, long _guild_id) {
		logger.debug("Azrael.SQLInsertNameFilter launched. Passed params {}, {}", _word, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO name_filter (word_id, word, fk_guild_id) VALUES(NULL, ?, ?) ON DUPLICATE KEY UPDATE word=VALUES(word)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLInsertNameFilter Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLDeleteNameFilter(String _word, long _guild_id) {
		logger.debug("Azrael.SQLDeleteNameFilter launched. Passed params {}, {}", _word, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM name_filter WHERE word LIKE ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLDeleteNameFilter Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLDeleteWholeNameFilter(long _guild_id) {
		logger.debug("Azrael.SQLDeleteWholeNameFilter launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM name_filter WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Azrael.SQLDeleteWholeNameFilter Exception", e);
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static String SQLgetRandomName(long _guild_id) {
		logger.debug("Azrael.SQLgetRandomName launched. Passed params {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT name FROM names WHERE fk_guild_id = ? ORDER BY RAND() LIMIT 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getString(1);
			}
			return "Nickname";
		} catch (SQLException e) {
			logger.error("Azrael.SQLgetRandomName Exception", e);
			return "Nickname";
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<String> SQLgetFilterLanguages() {
		logger.debug("Azrael.SQLgetFilterLanguages launched. No params passed");
		ArrayList<String> filter_lang = new ArrayList<String>();
		if(Hashes.getFilterLang(0) == null){
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT language FROM filter_languages WHERE lang_abbrv NOT LIKE \"all\"");
				stmt = myConn.prepareStatement(sql);
				rs = stmt.executeQuery();
				while(rs.next()){
					filter_lang.add(rs.getString(1));
				}
				Hashes.addFilterLang(0, filter_lang);
				return filter_lang;
			} catch (SQLException e) {
				logger.error("Azrael.SQLgetFilterLanguages Exception", e);
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return Hashes.getFilterLang(0);
	}
	
	public static int SQLInsertRSS(String _url, long _guild_id) {
		logger.debug("SQLInsertRSS launched. Params passed {}, {}", _url, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO rss (url, fk_guild_id, format) VALUES (?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _url);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, "{pubDate} | {title}\n{description}\n{link}");
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLInserRSS Exception", e);
			return 0;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<RSS> SQLgetRSSFeeds(long _guild_id){
		logger.debug("SQLgetRSSFeeds launched. Params passed {}", _guild_id);
		ArrayList<RSS> feeds = new ArrayList<RSS>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT url, format FROM rss WHERE fk_guild_id = ? ORDER BY timestamp asc");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			while(rs.next()){
				feeds.add(new RSS(rs.getString(1), rs.getString(2)));
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
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
	
	//Transactions
	@SuppressWarnings("resource")
	public static int SQLLowerTotalWarning(long _guild_id, int _warning_id){
		logger.debug("Azrael.SQLLowerTotalWarning launched. Passed params {}, {}", _guild_id, _warning_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
			logger.error("Azrael.SQLLowerTotalWarning Exception", e);
			try {
				myConn.rollback();
			} catch (SQLException e1) {
				logger.error("Azrael.SQLLowerTotalWarning  rollback Exception", e);
			}
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLReplaceWordFilter(String _lang, ArrayList<String> _words, long _guild_id){
		logger.debug("Azrael.SQLReplaceWordFilter launched. Passed params {}, array, {}", _lang, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			myConn.setAutoCommit(false);
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
		} catch (SQLException e) {
			logger.error("Azrael.SQLReplaceWordFilter Exception", e);
			try {
				myConn.rollback();
			} catch (SQLException e1) {
				logger.error("Azrael.SQLReplaceWordFilter rollback Exception", e);
			}
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLReplaceNameFilter(ArrayList<String> _words, long _guild_id){
		logger.debug("Azrael.SQLReplaceNameFilter launched. Passed params array, {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			myConn.setAutoCommit(false);
			String sql2 = ("INSERT INTO name_filter (word_id, word, fk_guild_id) VALUES(NULL, ?, ?) ON DUPLICATE KEY UPDATE word=VALUES(word)");
			stmt = myConn.prepareStatement(sql2);
			
			for(String word : _words) {
				stmt.setString(1, word);
				stmt.setLong(2, _guild_id);
				stmt.addBatch();
			}
			stmt.executeBatch();
			myConn.commit();	
		} catch (SQLException e) {
			logger.error("Azrael.SQLReplaceNameFilter Exception", e);
			try {
				myConn.rollback();
			} catch (SQLException e1) {
				logger.error("Azrael.SQLReplaceNameFilter rollback Exception", e);
			}
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLReplaceFunnyNames(ArrayList<String> _words, long _guild_id){
		logger.debug("Azrael.SQLReplaceFunnyNames launched. Passed params array, {}", _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.178.2:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			myConn.setAutoCommit(false);
			String sql2 = ("INSERT INTO names (name_id, name, fk_guild_id) VALUES(NULL, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)");
			stmt = myConn.prepareStatement(sql2);;
			
			for(String word : _words) {
				stmt.setString(1, word);
				stmt.setLong(2, _guild_id);
				stmt.addBatch();
			}
			stmt.executeBatch();
			myConn.commit();	
		} catch (SQLException e) {
			logger.error("Azrael.SQLReplaceFunnyNames Exception", e);
			try {
				myConn.rollback();
			} catch (SQLException e1) {
				logger.error("Azrael.SQLReplaceFunnyNames rollback Exception", e);
			}
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
}
