package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import core.Channels;
import fileManagement.IniFileReader;

public class SqlConnect {
	
	private static long user_id = 0;
	private static long guild_id = 0;
	private static String name = null;
	private static String nickname = null;
	private static String guild_name = null; 
	private static int warning_id = 0;
	private static int ban_id = 0;
	private static long channel_id = 0;
	private static String channel_name = null;
	private static long ch_guild_id = 0;
	private static String channel_type = null;
	private static String channel_type_name = null;
	private static int execution_id = 0;
	private static long guild = 0;
	private static boolean muted = false;
	private static double timer1 = 0;
	private static double timer2 = 0;
	
	private static Date unmute;
	private static ArrayList<Channels> channels = new ArrayList<Channels>();
	private static ArrayList<String> filter_lang = new ArrayList<String>();
	private static ArrayList<String> filter_words = new ArrayList<String>();
	
	private static String username = IniFileReader.getSQLUsername();
	private static String password = IniFileReader.getSQLPassword();
	
	
	public static void SQLconnection(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void SQLInsertUser(Long _user_id, String _name){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO users (user_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setString(2, _name);
			stmt.executeUpdate();
		} catch (SQLException e) {
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
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE users SET name = ? WHERE user_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _name);
			stmt.setLong(2, _user_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
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
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO guild VALUES (?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setString(2, _guild_name);
			stmt.executeUpdate();
		} catch (SQLException e) {
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
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT nickname FROM nickname WHERE fk_user_id= ? && fk_guild_id= ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setNickname(rs.getString(1));
			}
		} catch (SQLException e) {
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
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO nickname VALUES (?, ?, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, _nickname);
			stmt.executeUpdate();
		} catch (SQLException e) {
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
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE nickname SET nickname = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _nickname);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
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
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM nickname WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
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
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT fk_user_id, fk_guild_id, fk_warning_id, fk_ban_id, unmute, muted FROM bancollect WHERE fk_user_id= ? && fk_guild_id= ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setUser_id(rs.getLong(1));
				setGuild_id(rs.getLong(2));
				setWarningID(rs.getInt(3));
				setBanID(rs.getInt(4));
				setUnmute(rs.getTimestamp(5));
				setMuted(rs.getBoolean(6));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertData(Long _user_id, Long _guild_id, int _warning_id, int _ban_id, Timestamp _timestamp, Timestamp _unmute, boolean _muted){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO bancollect VALUES (NULL, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE fk_warning_id=VALUES(fk_warning_id), fk_ban_id=VALUES(fk_ban_id), timestamp=VALUES(timestamp), unmute=VALUES(unmute), muted=VALUES(muted)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setInt(3, _warning_id);
			stmt.setInt(4, _ban_id);
			stmt.setTimestamp(5, _timestamp);
			stmt.setTimestamp(6, _unmute);
			stmt.setBoolean(7, _muted);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateMuted(long _user_id, long _guild_id, boolean _muted){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE bancollect SET muted = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setBoolean(1, _muted);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateWarning(Long _user_id, Long _guild_id, Integer _warning_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE bancollect SET fk_warning_id = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _warning_id);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateUnmute(Long _user_id, Long _guild_id, Timestamp _unmute){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE bancollect SET unmute = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setTimestamp(1, _unmute);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
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
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE bancollect SET fk_ban_id = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _ban_id);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateWarningAndBan(long _user_id, long _guild_id, Timestamp _unmute, int _warning_id, int _ban_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE bancollect SET unmute = ?, fk_warning_id = ?, fk_ban_id = ? WHERE fk_user_id = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setTimestamp(1, _unmute);
			stmt.setInt(2, _warning_id);
			stmt.setInt(3, _ban_id);
			stmt.setLong(4, _user_id);
			stmt.setLong(5, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetChannel(long _channel_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT channels.channel_id, channels.name, channel_conf.fk_channel_type, channel_conf.fk_guild_id FROM channels INNER JOIN channel_conf ON channels.channel_id = channel_conf.fk_channel_id WHERE channel_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setChannelID(rs.getLong(1));
				setChannelName(rs.getString(2));
				setChannelType(rs.getString(3));
				setCH_GuildID(rs.getLong(4));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertChannels(long _channel_id, String _channel_name){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO channels (channel_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			stmt.setString(2, _channel_name);
			stmt.executeUpdate();
		} catch (SQLException e) {
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
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO channel_conf (fk_channel_id, fk_channel_type, fk_guild_id) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE fk_channel_type = VALUES(fk_channel_type)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			stmt.setString(2, _channel_type);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetChannelID(long _guild_id, String _channel_type){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT fk_channel_id FROM channel_conf WHERE fk_channel_type = ? && fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setString(1, _channel_type);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setChannelID(rs.getLong(1));
			}
		} catch (SQLException e) {
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
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT fk_channel_type FROM channel_conf WHERE fk_channel_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setChannelType(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetChannels(long _guild_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT channels.channel_id, channels.name, channeltypes.channel_type, channeltypes.channel, guild.guild_id, guild.name, filter_languages.language FROM channels INNER JOIN channel_conf ON channels.channel_id = channel_conf.fk_channel_id INNER JOIN channeltypes ON channel_conf.fk_channel_type = channeltypes.channel_type INNER JOIN guild ON channel_conf.fk_guild_id = guild.guild_id LEFT JOIN channel_filter ON channels.channel_id = channel_filter.fk_channel_id LEFT JOIN filter_languages ON channel_filter.fk_lang_abbrv = filter_languages.lang_abbrv WHERE guild.guild_id = ?");
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
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
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
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetExecutionID(long _guild_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM command WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setGuild(rs.getLong(2));
				setExecutionID(rs.getInt(3));
			}
		} catch (SQLException e) {
			e.printStackTrace();
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
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO command (guild_id, execution_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE execution_id=VALUES(execution_id)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _execution_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLgetMuteTimer(long _ch_guild_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM mutetimer WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _ch_guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				setCH_GuildID(rs.getLong(1));
				setTimer1(rs.getDouble(2));
				setTimer2(rs.getDouble(3));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertMuteTimer1(long _ch_guild_id, long _timer1){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO mutetimer (guild_id, timer1, timer2) VALUES (?, ?, NULL)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _ch_guild_id);
			stmt.setLong(2, _timer1);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertMuteTimer2(long _ch_guild_id, long _timer2){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO mutetimer (guild_id, timer1, timer2) VALUES (?, NULL, ?)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _ch_guild_id);
			stmt.setLong(2, _timer2);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateMuteTimer1(long _ch_guild_id, long _timer1){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE mutetimer SET timer1 = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _timer1);
			stmt.setLong(2, _ch_guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLUpdateMuteTimer2(long _ch_guild_id, long _timer2){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("UPDATE mutetimer SET timer2 = ? WHERE guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _timer2);
			stmt.setLong(2, _ch_guild_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public synchronized static void SQLgetChannel_Filter(long _channel_id){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT * FROM channel_filter WHERE fk_channel_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			rs = stmt.executeQuery();
			while(rs.next()){
				filter_lang.add(rs.getString(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public synchronized static void SQLgetFilter(String _filter_lang){
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql;
			if(_filter_lang.equals("all")){
				sql = ("SELECT word FROM filter");
				stmt = myConn.prepareStatement(sql);
			}
			else{
				sql = ("SELECT word FROM filter WHERE fk_lang_abbrv LIKE ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setString(1, _filter_lang);
			}
			rs = stmt.executeQuery();
			while(rs.next()){
				filter_words.add(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static void SQLInsertChannel_Filter(long _channel_id, String _filter_lang){
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("INSERT INTO channel_filter (fk_channel_id, fk_lang_abbrv) VALUES (?,?) ON DUPLICATE KEY UPDATE fk_lang_abbrv = VALUES(fk_lang_abbrv)");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			stmt.setString(2, _filter_lang);
			stmt.executeUpdate();
		} catch (SQLException e) {
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
			myConn = DriverManager.getConnection("jdbc:mysql://192.168.0.2:3306/Test?autoReconnect=true&useSSL=false", username, password);
			String sql = ("DELETE FROM channel_filter WHERE fk_channel_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _channel_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
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
	public static void setTimer1(double _timer1){
		timer1 = _timer1;
	}
	public static void setTimer2(double _timer2){
		timer2 = _timer2;
	}
	public static void setUnmute(Date _unmute){
		unmute = _unmute;
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
	public static double getTimer1(){
		return timer1;
	}
	public static double getTimer2(){
		return timer2;
	}
	public static ArrayList<Channels> getChannels(){
		return channels;
	}
	public synchronized static ArrayList<String> getFilter_Lang(){
		return filter_lang;
	}
	public static ArrayList<String> getFilter_Words(){
		return filter_words;
	}
	public static Date getUnmute(){
		return unmute;
	}
	
	public static void clearUnmute(){
		unmute = null;
	}
	public static void clearChannelsArray(){
		channels.clear();
	}
	public synchronized static void clearFilter_Lang(){
		filter_lang.clear();
	}
	public synchronized static void clearFilter_Words(){
		filter_words.clear();
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
		setChannelName("");
		setCH_GuildID(0);
		setChannelType("");
		setChannelTypeName("");
		setExecutionID(0);
		setGuild(0);
		setTimer1(0);
		setTimer2(0);
		setMuted(false);
	}
}
