package de.azrael.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Thumbnails;
import de.azrael.core.Hashes;
import de.azrael.enums.Command;
import de.azrael.util.STATIC;

public class BotConfiguration {
private static final Logger logger = LoggerFactory.getLogger(BotConfiguration.class);
	
	public static void SQLconnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			logger.error("JDBC driver couldn't be loaded", e);
		}
	}
	
	public static boolean SQLisAdministrator(long user_id, long guild_id) {
		logger.trace("SQLisAdministrator launched. Passed params {}, {}", user_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = STATIC.getDatabaseURL(3);
			stmt = myConn.prepareStatement(BotConfigurationStatements.SQLisAdministrator);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return true;
			}
			return false;
		} catch (SQLException e) {
			logger.error("SQLisAdministrator Exception", e);
			return false;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Long> SQLgetAdministrators(long guild_id) {
		logger.trace("SQLgetAdministrators launched. Passed params {}", guild_id);
		ArrayList<Long> administrators = new ArrayList<Long>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = STATIC.getDatabaseURL(3);
			stmt = myConn.prepareStatement(BotConfigurationStatements.SQLgetAdministrators);
			stmt.setLong(1, guild_id);
			rs = stmt.executeQuery();
			while(rs.next()) {
				administrators.add(rs.getLong(1));
			}
			return administrators;
		} catch (SQLException e) {
			logger.error("SQLgetAdministrators Exception", e);
			return administrators;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	@SuppressWarnings("resource")
	public static boolean SQLInsertBotConfigs(long guild_id) {
		logger.trace("SQLInsertBotConfigs launched. Passed params {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = STATIC.getDatabaseURL(3);
			myConn.setAutoCommit(false);
			stmt = myConn.prepareStatement(BotConfigurationStatements.SQLInsertBotConfigs);
			stmt.setLong(1, guild_id);
			stmt.executeUpdate();
			
			stmt = myConn.prepareStatement(BotConfigurationStatements.SQLInsertBotConfigs2);
			stmt.setLong(1, guild_id);
			stmt.executeUpdate();
			
			stmt = myConn.prepareStatement(BotConfigurationStatements.SQLInsertBotConfigs3);
			stmt.setLong(1, guild_id);
			stmt.executeUpdate();
			
			stmt = myConn.prepareStatement(BotConfigurationStatements.SQLInsertBotConfigs4);
			stmt.setLong(1, guild_id);
			stmt.executeUpdate();
			
			stmt = myConn.prepareStatement(BotConfigurationStatements.SQLInsertBotConfigs5);
			stmt.setLong(1, guild_id);
			stmt.executeUpdate();
			
			myConn.commit();
			return true;
		} catch (SQLException e) {
			logger.error("SQLInsertBotConfigs Exception", e);
			return false;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static BotConfigs SQLgetBotConfigs(long guild_id) {
		if(Hashes.getBotConfiguration(guild_id) == null) {
			logger.trace("SQLgetBotConfigs launched. Passed params {}", guild_id);
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = STATIC.getDatabaseURL(3);
				stmt = myConn.prepareStatement(BotConfigurationStatements.SQLgetBotConfigs);
				stmt.setLong(1, guild_id);
				rs = stmt.executeQuery();
				BotConfigs config = null;
				if(rs.next()) {
					config = new BotConfigs(
							rs.getString("command_prefix"),
							rs.getBoolean("join_message"),
							rs.getBoolean("leave_message"),
							rs.getBoolean("channel_log"),
							rs.getBoolean("cache_log"),
							rs.getString("double_experience"),
							rs.getInt("double_experience_start"),
							rs.getInt("double_experience_end"),
							rs.getBoolean("force_reason"),
							rs.getBoolean("override_ban"),
							rs.getBoolean("prohibit_urls_mode"),
							rs.getBoolean("self_deleted_messages"),
							rs.getBoolean("edited_messages"),
							rs.getBoolean("edited_messages_history"),
							rs.getBoolean("notifications"),
							rs.getBoolean("new_account_on_join"),
							rs.getBoolean("reassign_roles"),
							rs.getBoolean("collect_ranking_roles"),
							rs.getInt("exp_rate_limit"),
							rs.getBoolean("ignore_missing_permissions"),
							rs.getBoolean("private_patch_notes"),
							rs.getBoolean("public_patch_notes"),
							rs.getBoolean("google_functionalities"),
							rs.getString("google_main_email"),
							rs.getBoolean("spam_detection_enabled"),
							rs.getInt("spam_detection_channel_limit"),
							rs.getInt("spam_detection_all_channels_limit"),
							rs.getInt("spam_detection_expires"),
							rs.getBoolean("mute_message_delete_enabled"),
							rs.getBoolean("mute_force_message_deletion"),
							rs.getInt("mute_auto_delete_messages"),
							rs.getBoolean("mute_send_reason"),
							rs.getBoolean("kick_message_delete_enabled"),
							rs.getBoolean("kick_force_message_deletion"),
							rs.getInt("kick_auto_delete_messages"),
							rs.getBoolean("kick_send_reason"),
							rs.getBoolean("ban_message_delete_enabled"),
							rs.getBoolean("ban_force_message_deletion"),
							rs.getInt("ban_auto_delete_messages"),
							rs.getBoolean("ban_send_reason"),
							rs.getString("competitive_team_1_name"),
							rs.getString("competitive_team_2_name"),
							rs.getBoolean("reactions_enabled"),
							rs.getString("reactions_emoji1"),
							rs.getString("reactions_emoji2"),
							rs.getString("reactions_emoji3"),
							rs.getString("reactions_emoji4"),
							rs.getString("reactions_emoji5"),
							rs.getString("reactions_emoji6"),
							rs.getString("reactions_emoji7"),
							rs.getString("reactions_emoji8"),
							rs.getString("reactions_emoji9"),
							rs.getString("vote_reaction_thumbs_up"),
							rs.getString("vote_reaction_thumbs_down"),
							rs.getString("vote_reaction_shrug")
					);
				}
				if(config == null)
					config = new BotConfigs();
				Hashes.addBotConfiguration(guild_id, config);
				return config;
			} catch (SQLException e) {
				logger.error("SQLgetBotConfigs Exception", e);
				return new BotConfigs();
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return Hashes.getBotConfiguration(guild_id);
	}
	
	public static int SQLUpdateBotConfigsDoubleExperience(long guild_id, String value) {
		logger.trace("SQLUpdateBotConfigsDoubleExperience launched. Passed params {}", guild_id, value);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = STATIC.getDatabaseURL(3);
			stmt = myConn.prepareStatement(BotConfigurationStatements.SQLUpdateBotConfigsDoubleExperience);
			stmt.setString(1, value);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUpdateBotConfigsDoubleExperience Exception", e);
			return -1;
		} finally {
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLCommandsAvailable(long guild_id) {
		logger.trace("SQLCommandsAvailable launched. Passed params {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = STATIC.getDatabaseURL(3);
			stmt = myConn.prepareStatement(BotConfigurationStatements.SQLCommandsAvailable);
			stmt.setLong(1, guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return true;
			}
			return false;
		} catch (SQLException e) {
			logger.error("SQLCommandsAvailable Exception", e);
			return false;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLSubCommandsAvailable(long guild_id) {
		logger.trace("SQLSubCommandsAvailable launched. Passed params {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = STATIC.getDatabaseURL(3);
			stmt = myConn.prepareStatement(BotConfigurationStatements.SQLSubCommandsAvailable);
			stmt.setLong(1, guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return true;
			}
			return false;
		} catch (SQLException e) {
			logger.error("SQLSubCommandsAvailable Exception", e);
			return false;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static boolean SQLCommandsLevelAvailable(long guild_id) {
		logger.trace("SQLCommandsLevelAvailable launched. Passed params {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = STATIC.getDatabaseURL(3);
			stmt = myConn.prepareStatement(BotConfigurationStatements.SQLCommandsLevelAvailable);
			stmt.setLong(1, guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return true;
			}
			return false;
		} catch (SQLException e) {
			logger.error("SQLCommandsLevelAvailable Exception", e);
			return false;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static Object SQLgetCommand(long guild_id, int mode, Command command) {
		logger.trace("SQLgetCommand launched. Passed params {}, {}, {}", guild_id, mode, command.getColumn());
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = STATIC.getDatabaseURL(3);
			stmt = myConn.prepareStatement(BotConfigurationStatements.SQLgetCommand);
			stmt.setLong(1, guild_id);
			rs = stmt.executeQuery();
			if(rs.next() && !command.isHidden()) {
				if(mode == 1)
					return command.resolveResultSet(rs);
				else if(mode == 2 && !command.isStateOnly())
					return command.resolveResultSetLv(rs);
				else if(mode == 3 && !command.isLevelOnly())
					return command.resolveResultSetCm(rs);
				else if(mode == 4 && !command.isLevelOnly())
					return command.resolveResultSetScm(rs);
				else if(mode == 5 && !command.isLevelOnly())
					return command.resolveResultSetType(rs);
			}
			ArrayList<Object> values = new ArrayList<Object>();
			if(mode == 1) {
				values.add(false);
				values.add(100);
			}
			else if(mode == 2)
				values.add(100);
			else if(mode == 3 || mode == 4)
				values.add(false);
			return values;
		} catch (SQLException e) {
			logger.error("SQLgetCommand Exception", e);
			ArrayList<Object> values = new ArrayList<Object>();
			if(mode == 1) {
				values.add(false);
				values.add(100);
			}
			else if(mode == 3)
				values.add(false);
			return values;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<Object> SQLgetCommand(long guild_id, int mode, Command... commands) {
		logger.trace("SQLgetCommand launched. Passed params {}, list of commands", guild_id);
		ArrayList<Object> values = new ArrayList<Object>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = STATIC.getDatabaseURL(3);
			stmt = myConn.prepareStatement(BotConfigurationStatements.SQLgetCommand);
			stmt.setLong(1, guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				for(Command command : commands) {
					if(!command.isHidden()) {
						if(mode == 1)
							values.add(command.resolveResultSet(rs));
						else if(mode == 2 && !command.isStateOnly())
							values.add(command.resolveResultSetLv(rs));
						else if(mode == 3 && !command.isLevelOnly())
							values.add(command.resolveResultSetCm(rs));
						else if(mode == 4 && !command.isLevelOnly())
							values.add(command.resolveResultSetScm(rs));
						else if(mode == 5 && !command.isLevelOnly())
							values.add(command.resolveResultSetType(rs));
					}
				}
				return values;
			}
			return values;
		} catch (SQLException e) {
			logger.error("SQLgetCommand Exception", e);
			return values;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static Thumbnails SQLgetThumbnails(long guild_id) {
		logger.trace("SQLgetThumbnails launched. Passed params {}", guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = STATIC.getDatabaseURL(3);
			stmt = myConn.prepareStatement(BotConfigurationStatements.SQLgetThumbnails);
			stmt.setLong(1, guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				return new Thumbnails(
						rs.getString("pug"),
						rs.getString("meow"),
						rs.getString("ban"),
						rs.getString("settings"),
						rs.getString("denied"),
						rs.getString("shop"),
						rs.getString("unban"),
						rs.getString("unmute"),
						rs.getString("kick"),
						rs.getString("caught"),
						rs.getString("false_alarm"),
						rs.getString("heavy"),
						rs.getString("heavy_end")
				);
			}
			return new Thumbnails();
		} catch (SQLException e) {
			logger.error("SQLgetThumbnails Exception", e);
			return new Thumbnails();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
}
