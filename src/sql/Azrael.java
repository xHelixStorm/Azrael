package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import core.Channels;
import core.Hashes;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.entities.Member;

public class Azrael {
	
	private static long user_id = 0;
	private static long guild_id = 0;
	private static String name = null;
	private static String nickname = null;
	private static String guild_name = null; 
	private static int warning_id = 0;
	private static int ban_id = 0;
	private static long channel_id = 0;
	private static long channel_id2 = 0;
	private static String channel_name = null;
	private static long ch_guild_id = 0;
	private static String channel_type = null;
	private static String channel_type_name = null;
	private static int execution_id = 0;
	private static long guild = 0;
	private static boolean muted = false;
	private static boolean custom_time = false;
	private static double timer = 0;
	private static String description = "";
	private static int count = 0;
	private static String avatar = null;
	private static String join_date = null;
	private static boolean reactions = false;
	
	private static Date timestamp;
	private static Date unmute;
	private static ArrayList<Channels> channels = new ArrayList<Channels>();
	private static ArrayList<String> descriptions = new ArrayList<String>();
	
	private static String username = IniFileReader.getSQLUsername();
	private static String password = IniFileReader.getSQLPassword();
	
	
	public static void SQLconnection(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		}
	}
	
	public static synchronized void SQLInsertActionLog(String _event, long _target_id, long _guild_id, String _description) {
		if(IniFileReader.getActionLog()) {
			Connection myConn = null;
			PreparedStatement stmt = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
				System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
				e.printStackTrace();
			} finally {
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
	}
	
	public static void SQLgetSingleActionEventCount(String _event, long _target_id, long _guild_id) {
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
			if(rs.next()){
				setCount(rs.getInt(1));
			}
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetDoubleActionEventDescriptions(String _event, String _event2, long _target_id, long _guild_id) {
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
			while(rs.next()){
				descriptions.add(rs.getString(1));
			}
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetSingleActionEventDescriptions(String _event, long _target_id, long _guild_id) {
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
			while(rs.next()){
				descriptions.add(rs.getString(1));
			}
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	public static void SQLgetCriticalActionEvents(long _target_id, long _guild_id) {
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
			while(rs.next()){
				descriptions.add("`["+rs.getTimestamp(2).toString()+"] - "+rs.getString(1)+"`");
			}
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertUser(long _user_id, String _name, String _avatar, String _join_date){
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
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLBulkInsertUsers(List<Member> members){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateAvatar(long _user_id, String _avatar){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE users SET avatar_url = ? WHERE user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _avatar);
			stmt.setLong(2, _user_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetUser(String _name){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM users WHERE name LIKE ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _name);
			rs = stmt.executeQuery();
			if(rs.next()){
				setUser_id(rs.getLong(1));
				setName(rs.getString(2));
				setAvatar(rs.getString(3));
				setJoinDate(rs.getString(4));
			}
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetUserThroughID(String _user_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM users WHERE user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _user_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setUser_id(rs.getLong(1));
				setName(rs.getString(2));
				setAvatar(rs.getString(3));
				setJoinDate(rs.getString(4));
			}
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateUser(Long _user_id, String _name){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE users SET name = ? WHERE user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _name);
			stmt.setLong(2, _user_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertGuild(Long _guild_id, String _guild_name){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO guild VALUES (?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setString(2, _guild_name);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetNickname(Long _user_id, Long _guild_id){
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
			if(rs.next()){
				setNickname(rs.getString(1));
			}
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertNickname(Long _user_id, Long _guild_id, String _nickname){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO nickname VALUES (?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, _nickname);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateNickname(Long _user_id, Long _guild_id, String _nickname){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE nickname SET nickname = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _nickname);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLDeleteNickname(Long _user_id, Long _guild_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM nickname WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetData(Long _user_id, Long _guild_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT fk_user_id, fk_guild_id, fk_warning_id, fk_ban_id, timestamp, unmute, muted, custom_time FROM bancollect WHERE fk_user_id= ? && fk_guild_id= ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setUser_id(rs.getLong(1));
				setGuild_id(rs.getLong(2));
				setWarningID(rs.getInt(3));
				setBanID(rs.getInt(4));
				setTimestamp(rs.getTimestamp(5));
				setUnmute(rs.getTimestamp(6));
				setMuted(rs.getBoolean(7));
				setCustomTime(rs.getBoolean(8));
			}
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetMuted(long _user_id, long _guild_id){
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
			if(rs.next()){
				setMuted(rs.getBoolean(1));
			}
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertData(long _user_id, long _guild_id, int _warning_id, int _ban_id, Timestamp _timestamp, Timestamp _unmute, boolean _muted, boolean _custom_time){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLDeleteData(long _user_id, long _guild_id) {
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM bancollect WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateMuted(long _user_id, long _guild_id, boolean _muted, boolean _custom_time){
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
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetWarning(long _guild_id, int _warning_id) {
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
			if(rs.next()){
				setWarningID(rs.getInt(1));
				setTimer(rs.getDouble(2));
				setDescription(rs.getString(3));
			}
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetMaxWarning(long _guild_id) {
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT MAX(warning_id) FROM warnings WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setWarningID(rs.getInt(1));
			}
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertWarning(long _guild_id, int _warning_id) {
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateWarning(long _user_id, long _guild_id, int _warning_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE bancollect SET fk_warning_id = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _warning_id);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateMuteTimeOfWarning(long _guild_id, int _warning_id, long _mute_time) {
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE warnings SET mute_time = ? WHERE fk_guild_id = ? && warning_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _mute_time);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _warning_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateUnmute(Long _user_id, Long _guild_id, Timestamp _timestamp, Timestamp _unmute, boolean _muted, boolean _custom_time){
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
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateBan(Long _user_id, Long _guild_id, Integer _ban_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE bancollect SET fk_ban_id = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _ban_id);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertChannels(long _channel_id, String _channel_name){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO channels (channel_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			stmt.setString(2, _channel_name);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertChannel_Conf(long _channel_id, long _guild_id, String _channel_type){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO channel_conf (fk_channel_id, fk_channel_type, fk_guild_id) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE fk_channel_type = VALUES(fk_channel_type)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			stmt.setString(2, _channel_type);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLgetChannelID(long _guild_id, String _channel_type){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT fk_channel_id FROM channel_conf WHERE fk_channel_type = ? && fk_guild_id = ?");
			boolean success = false;
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _channel_type);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setChannelID(rs.getLong(1));
				success = true;
			}
			return success;
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
			return false;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetTwoChanneIDs(long _guild_id, String _channel_type, String _channel_type2){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT (SELECT fk_channel_id FROM channel_conf WHERE fk_channel_type LIKE ? && fk_guild_id = ?) AS channel1, (SELECT fk_channel_id FROM channel_conf WHERE fk_channel_type = ? && fk_guild_id = ?) AS channel2 FROM channel_conf LIMIT 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _channel_type);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, _channel_type2);
			stmt.setLong(4, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setChannelID(rs.getLong(1));
				setChannelID2(rs.getLong(2));
			}
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetChannelType(long _channel_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT fk_channel_type FROM channel_conf WHERE fk_channel_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setChannelType(rs.getString(1));
			}
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLDeleteChannelType(String _channel_type) {
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM channel_conf WHERE fk_channel_type = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _channel_type);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetChannels(long _guild_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetChannelTypes(){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM channeltypes");
			stmt = myConn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while(rs.next()){
				Channels channelProperties = new Channels();
				channelProperties.setChannel_Type(rs.getString(1));
				channelProperties.setChannel_Type_Name(rs.getString(2));
				channels.add(channelProperties);
			}
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLgetExecutionID(long _guild_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM command WHERE guild_id = ?");
			boolean success = false;
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setGuild(rs.getLong(2));
				setExecutionID(rs.getInt(3));
				setReactions(rs.getBoolean(4));
				success = true;
			}
			return success;
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
			return false;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertCommand(long _guild_id, int _execution_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO command (guild_id, execution_id, reactions) VALUES (?, ?, 0) ON DUPLICATE KEY UPDATE execution_id=VALUES(execution_id)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _execution_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertCommand(long _guild_id, int _execution_id, boolean _reactions){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO command (guild_id, execution_id, reactions) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE reactions=VALUES(reactions)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _execution_id);
			stmt.setBoolean(3, _reactions);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateReaction(long _guild_id, boolean _reactions) {
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE command SET reactions = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setBoolean(1, _reactions);
			stmt.setLong(2, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public synchronized static void SQLgetChannel_Filter(long _channel_id){
		if(Hashes.getFilterLang(_channel_id) == null){
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
				while(rs.next()){
					filter_lang.add(rs.getString(2));
				}
				Hashes.addFilterLang(_channel_id, filter_lang);
			} catch (SQLException e) {
				System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
				e.printStackTrace();
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
	}
	
	public synchronized static void SQLgetFilter(String _filter_lang, long _guild_id){
		if(Hashes.getQuerryResult(_filter_lang+"_"+_guild_id) == null) {
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
				System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
				e.printStackTrace();
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
	}
	
	public static void SQLInsertWordFilter(String _lang, String _word, long _guild_id) {
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO filter (filter_id, word, fk_lang_abbrv, fk_guild_id) VALUES(NULL, ?, ?, ?) ON DUPLICATE KEY UPDATE word=VALUES(word)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word.toLowerCase());
			stmt.setString(2, _lang);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLDeleteWordFilter(String _lang, String _word, long _guild_id) {
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM filter WHERE word LIKE ? && fk_lang_abbrv LIKE ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word);
			stmt.setString(2, _lang);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLDeleteLangWordFilter(String _lang, long _guild_id) {
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM filter WHERE fk_lang_abbrv LIKE ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _lang);
			stmt.setLong(2, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public synchronized static void SQLgetStaffNames(long _guild_id){
		if(Hashes.getQuerryResult("staff-names_"+_guild_id) == null) {
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
				ArrayList<String> staff_names = new ArrayList<String>();
				String sql = ("SELECT name FROM staff_name_filter WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				rs = stmt.executeQuery();
				while(rs.next()){
					staff_names.add(rs.getString(1));
				}
				Hashes.addQuerryResult("staff-names_"+_guild_id, staff_names);
			} catch (SQLException e) {
				System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
				e.printStackTrace();
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
	}
	
	public static void SQLInsertStaffName(String _word, long _guild_id) {
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO staff_name_filter (name_id, name, fk_guild_id) VALUES(NULL, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word.toLowerCase());
			stmt.setLong(2, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLDeleteStaffNames(String _word, long _guild_id) {
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM staff_name_filter WHERE name LIKE ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word.toLowerCase());
			stmt.setLong(2, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLDeleteWholeStaffNames(long _guild_id) {
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM staff_name_filter WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLBatchInsertStaffNames(ArrayList<String> _words, long _guild_id) {
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertChannel_Filter(long _channel_id, String _filter_lang){
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
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLDeleteChannel_Filter(long _channel_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM channel_filter WHERE fk_channel_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetFunnyNames(long _guild_id) {
		if(Hashes.getQuerryResult("funny-names_"+_guild_id) == null) {
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
				ArrayList<String> names = new ArrayList<String>();
				String sql = ("SELECT name FROM names WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				rs = stmt.executeQuery();
				while(rs.next()){
					names.add(rs.getString(1));
				}
				Hashes.addQuerryResult("funny-names_"+_guild_id, names);
			} catch (SQLException e) {
				System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
				e.printStackTrace();
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
	}
	
	public static void SQLInsertFunnyNames(String _word, long _guild_id) {
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO names (name_id, name, fk_guild_id) VALUES(NULL, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word);
			stmt.setLong(2, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLDeleteFunnyNames(String _word, long _guild_id) {
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM names WHERE name LIKE ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word);
			stmt.setLong(2, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLDeleteWholeFunnyNames(long _guild_id) {
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM names WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetNameFilter(long _guild_id) {
		if(Hashes.getQuerryResult("bad-names_"+_guild_id) == null) {
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
				ArrayList<String> names = new ArrayList<String>();
				String sql = ("SELECT word FROM name_filter");
				stmt = myConn.prepareStatement(sql);
				rs = stmt.executeQuery();
				while(rs.next()){
					names.add(rs.getString(1));
				}
				Hashes.addQuerryResult("bad-names_"+_guild_id, names);
			} catch (SQLException e) {
				System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
				e.printStackTrace();
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
	}
	
	public static void SQLInsertNameFilter(String _word, long _guild_id) {
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO name_filter (word_id, word, fk_guild_id) VALUES(NULL, ?, ?) ON DUPLICATE KEY UPDATE word=VALUES(word)");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word);
			stmt.setLong(2, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLDeleteNameFilter(String _word, long _guild_id) {
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM name_filter WHERE word LIKE ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _word);
			stmt.setLong(2, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLDeleteWholeNameFilter(long _guild_id) {
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM name_filter WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetRandomName(long _guild_id) {
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT name FROM names WHERE fk_guild_id = ? ORDER BY RAND() LIMIT 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setName(rs.getString(1));
			}
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetFilterLanguages() {
		if(Hashes.getFilterLang(0) == null){
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT language FROM filter_languages WHERE lang_abbrv NOT LIKE \"all\"");
				ArrayList<String> filter_lang = new ArrayList<String>();
				stmt = myConn.prepareStatement(sql);
				rs = stmt.executeQuery();
				while(rs.next()){
					filter_lang.add(rs.getString(1));
				}
				Hashes.addFilterLang(0, filter_lang);
			} catch (SQLException e) {
				System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
				e.printStackTrace();
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
	}
	
	//Transactions
	@SuppressWarnings("resource")
	public static void SQLLowerTotalWarning(long _guild_id, int _warning_id){
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
			stmt.executeUpdate();
			myConn.commit();	
		} catch (SQLException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
			try {
				myConn.rollback();
			} catch (SQLException e1) {
				System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
				e1.printStackTrace();
			}
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLReplaceWordFilter(String _lang, ArrayList<String> _words, long _guild_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
			try {
				myConn.rollback();
			} catch (SQLException e1) {
				System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
				e1.printStackTrace();
			}
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLReplaceNameFilter(ArrayList<String> _words, long _guild_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
			try {
				myConn.rollback();
			} catch (SQLException e1) {
				System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
				e1.printStackTrace();
			}
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLReplaceFunnyNames(ArrayList<String> _words, long _guild_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Azrael?autoReconnect=true&useSSL=false", username, password);
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
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
			try {
				myConn.rollback();
			} catch (SQLException e1) {
				System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
				e1.printStackTrace();
			}
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void setUser_id(long _user_id){
		user_id=_user_id;
	}
	public static void setGuild_id(long _guild_id){
		guild_id=_guild_id;
	}
	public static void setName(String _name){
		name=_name;
	}
	public static void setNickname(String _nickname){
		nickname = _nickname;
	}
	public static void setGuildName(String _guild_name){
		guild_name=_guild_name;
	}
	public static void setWarningID(int _warning_id){
		warning_id=_warning_id;
	}
	public static void setBanID(int _ban_id){
		ban_id=_ban_id;
	}
	public static void setChannelID(long _channel_id){
		channel_id = _channel_id;
	}
	public static void setChannelID2(long _channel_id2) {
		channel_id2 = _channel_id2;
	}
	public static void setChannelName(String _channel_name){
		channel_name = _channel_name;
	}
	public static void setCH_GuildID(long _ch_guild_id){
		ch_guild_id = _ch_guild_id;
	}
	public static void setChannelType(String _channel_type){
		channel_type = _channel_type;
	}
	public static void setChannelTypeName(String _channel_type_name){
		channel_type_name = _channel_type_name;
	}
	public static void setExecutionID(int _execution_id){
		execution_id = _execution_id;
	}
	public static void setGuild(long _guild){
		guild = _guild;
	}
	public static void setMuted(boolean _muted){
		muted = _muted;
	}
	public static void setCustomTime(boolean _custom_time) {
		custom_time = _custom_time;
	}
	public static void setTimer(double _timer){
		timer = _timer;
	}
	public static void setDescription(String _description){
		description = _description;
	}
	public static void setCount(int _count) {
		count = _count;
	}
	public static void setTimestamp(Date _timestamp){
		timestamp = _timestamp;
	}
	public static void setUnmute(Date _unmute){
		unmute = _unmute;
	}
	public static void setAvatar(String _avatar){
		avatar = _avatar;
	}
	public static void setJoinDate(String _join_date){
		join_date = _join_date;
	}
	public static void setReactions(boolean _reactions) {
		reactions = _reactions;
	}
	
	
	public static long getUser_id(){
		return user_id;
	}
	public static long getGuild_id(){
		return guild_id;
	}
	public static String getName(){
		return name;
	}
	public static String getNickname(){
		return nickname;
	}
	public static String getGuildName(){
		return guild_name;
	}
	public static int getWarningID(){
		return warning_id;
	}
	public static int getBanID(){
		return ban_id;
	}
	public static long getChannelID(){
		return channel_id;
	}
	public static long getChannelID2() {
		return channel_id2;
	}
	public static String getChannelName(){
		return channel_name;
	}
	public static long getCH_GuildID(){
		return ch_guild_id;
	}
	public static String getChannelType(){
		return channel_type;
	}
	public static String getChannelTypeName(){
		return channel_type_name;
	}
	public static int getExecutionID(){
		return execution_id;
	}
	public static long getGuild(){
		return guild;
	}
	public static boolean getMuted(){
		return muted;
	}
	public static boolean getCustomTime() {
		return custom_time;
	}
	public static double getTimer(){
		return timer;
	}
	public static String getDescription() {
		return description;
	}
	public static int getCount() {
		return count;
	}
	public static ArrayList<Channels> getChannels(){
		return channels;
	}
	public static ArrayList<String> getDescriptions(){
		return descriptions;
	}
	public static Date getTimestamp(){
		return timestamp;
	}
	public static Date getUnmute(){
		return unmute;
	}
	public static String getAvatar(){
		return avatar;
	}
	public static String getJoinDate(){
		return join_date;
	}
	public static boolean getReactions() {
		return reactions;
	}
	
	public static void clearTimestamp() {
		timestamp = null;
	}
	public static void clearUnmute(){
		unmute = null;
	}
	public static void clearChannelsArray(){
		channels.clear();
	}
	public static void clearDescriptions() {
		descriptions.clear();
	}
	
	public static void clearAllVariables(){
		setUser_id(0);
		setGuild_id(0);
		setName("");
		setNickname("");
		setGuildName(""); 
		setWarningID(0);
		setBanID (0);
		setChannelID(0);
		setChannelID2(0);
		setChannelName("");
		setCH_GuildID(0);
		setChannelType("");
		setChannelTypeName("");
		setExecutionID(0);
		setGuild(0);
		setTimer(0);
		setMuted(false);
		setCustomTime(false);
		setDescription("");
		setCount(0);
		setAvatar("");
		setJoinDate("");
	}
}
