package de.azrael.webserver;

import java.awt.Color;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;

import de.azrael.commands.Invites;
import de.azrael.commands.ShutDown;
import de.azrael.constructors.Cache;
import de.azrael.core.Hashes;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Channel;
import de.azrael.enums.GoogleEvent;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.FileSetting;
import de.azrael.fileManagement.GuildIni;
import de.azrael.fileManagement.IniFileReader;
import de.azrael.google.GoogleSheets;
import de.azrael.listeners.ShutdownListener;
import de.azrael.sql.Azrael;
import de.azrael.sql.AzraelWeb;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.ReadyEvent;

public class HandlerPOST {
	public static void handleRequest(ReadyEvent e, PrintWriter out, JSONObject json) {
		if(validateJSON(out, json)) {
			final String type = json.getString("type");
			switch(type) {
				case "shutdown" -> {
					shutdown(e, out, json);
				}
				case "google" -> {
					google(e, out, json);
				}
				case "webLogin" -> {
					webLogin(e, out, json);
				}
				case "confirm" -> {
					confirm(e, out, json);
				}
				case "webRecovery" -> {
					webRecovery(e, out, json);
				}
				case "accountOptions" -> {
					accountOptions(e, out, json);
				}
				case "optionUpdate" -> {
					optionUpdate(e, out, json);
				}
			}
		}
	}
	
	private static boolean validateJSON(PrintWriter out, JSONObject json) {
		if(!json.has("token")) {
			WebserviceUtils.return502(out, "Item token required.", false);
			return false;
		}
		final String token = (String)json.get("token"); 
		if(!token.equals(STATIC.getToken())) {
			WebserviceUtils.return502(out, "Invalid Token", false);
			return false;
		}
		if(!json.has("type")) {
			WebserviceUtils.return502(out, "Item type required.", false);
			return false;
		}
		final String type = (String)json.get("type");
		if(!type.equals("shutdown") && !type.equals("google") && !type.equals("webLogin") && !type.equals("confirm") && !type.equals("webRecovery") && !type.equals("accountOptions") && !type.equals("optionUpdate")) {
			WebserviceUtils.return502(out, "Invalid Type.", false);
			return false;
		}
		if(type.equals("shutdown")) {
			if(json.has("message") && !(json.get("message") instanceof String)) {
				WebserviceUtils.return502(out, "Shutdown message needs to be a string.", false);
				return false;
			}
		}
		if(type.equals("google")) {
			if(!json.has("action")) {
				WebserviceUtils.return502(out, "Item action required.", false);
				return false;
			}
			final String action = (String)json.get("action");
			if(!action.equals("export")) {
				WebserviceUtils.return502(out, "Invalid action for type google.", false);
				return false;
			}
			if(json.has("guild_id")) {
				final String guild_id = (String)json.get("guild_id");
				if(guild_id.replaceAll("[0-9]*", "").length() != 0) {
					WebserviceUtils.return502(out, "Invalid guild_id. Not numeric.", false);
					return false;
				}
				else if(guild_id.length() != 18) {
					WebserviceUtils.return502(out, "Invalid guild_id. Not 18 digits long.", false);
					return false;
				}
			}
		}
		if(type.equals("webLogin")) {
			if(!json.has("user_id")) {
				WebserviceUtils.return502(out, "User ID required to request the web login.", false);
				return false;
			}
			else {
				final String user_id = (String) json.get("user_id");
				if(user_id.replaceAll("[0-9]*", "").length() != 0) {
					WebserviceUtils.return502(out, "User id is not numeric.", false);
					return false;
				}
				else if(user_id.length() != 17 && user_id.length() != 18) {
					WebserviceUtils.return502(out, "The user id needs to be either 17 or 18 digits long.", false);
					return false;
				}
			}
			if(!json.has("ip")) {
				WebserviceUtils.return502(out, "Source address not found.", false);
				return false;
			}
			else {
				final String ip = json.getString("ip");
				if(!ip.matches("([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}|::1)")) {
					WebserviceUtils.return502(out, "Source address invalid.", false);
					return false;
				}
			}
		}
		if(type.equals("confirm")) {
			if(!json.has("user_id")) {
				WebserviceUtils.return502(out, "User ID required to request the web login.", false);
				return false;
			}
			else {
				final String user_id = (String) json.get("user_id");
				if(user_id.replaceAll("[0-9]*", "").length() != 0) {
					WebserviceUtils.return502(out, "User id is not numeric.", false);
					return false;
				}
				else if(user_id.length() != 17 && user_id.length() != 18) {
					WebserviceUtils.return502(out, "The user id needs to be either 17 or 18 digits long.", false);
					return false;
				}
			}
			if(!json.has("body") && json.get("body") instanceof JSONObject) {
				WebserviceUtils.return502(out, "Body with confirmation token required.", false);
				return false;
			}
			else {
				final JSONObject body = (JSONObject) json.get("body");
				if(!body.has("confirmToken")) {
					WebserviceUtils.return502(out, "Confirmation token required inside body.", false);
					return false;
				}
			}
			if(!json.has("ip")) {
				WebserviceUtils.return502(out, "Source address not found.", false);
				return false;
			}
			else {
				final String ip = json.getString("ip");
				if(!ip.matches("([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}|::1)")) {
					WebserviceUtils.return502(out, "Source address invalid.", false);
					return false;
				}
			}
		}
		if(type.equals("webRecovery")) {
			if(!json.has("user_id")) {
				WebserviceUtils.return502(out, "User ID required to request an account recovery.", false);
				return false;
			}
			else {
				final String user_id = (String) json.get("user_id");
				if(user_id.replaceAll("[0-9]*", "").length() != 0) {
					WebserviceUtils.return502(out, "User id is not numeric.", false);
					return false;
				}
				else if(user_id.length() != 17 && user_id.length() != 18) {
					WebserviceUtils.return502(out, "The user id needs to be either 17 or 18 digits long.", false);
					return false;
				}
			}
			if(!json.has("ip")) {
				WebserviceUtils.return502(out, "Source address not found.", false);
				return false;
			}
			else {
				final String ip = json.getString("ip");
				if(!ip.matches("([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}|::1)")) {
					WebserviceUtils.return502(out, "Source address invalid.", false);
					return false;
				}
			}
		}
		if(type.equals("accountOptions")) {
			if(!json.has("user_id")) {
				WebserviceUtils.return502(out, "User ID required to request a key to change account options.", false);
				return false;
			}
			else {
				final String user_id = (String) json.get("user_id");
				if(user_id.replaceAll("[0-9]*", "").length() != 0) {
					WebserviceUtils.return502(out, "User id is not numeric.", false);
					return false;
				}
				else if(user_id.length() != 17 && user_id.length() != 18) {
					WebserviceUtils.return502(out, "The user id needs to be either 17 or 18 digits long.", false);
					return false;
				}
			}
			if(!json.has("ip")) {
				WebserviceUtils.return502(out, "Source address not found.", false);
				return false;
			}
			else {
				final String ip = json.getString("ip");
				if(!ip.matches("([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}|::1)")) {
					WebserviceUtils.return502(out, "Source address invalid.", false);
					return false;
				}
			}
		}
		if(type.equals("optionUpdate")) {
			if(!json.has("user_id")) {
				WebserviceUtils.return502(out, "User ID required to request a key to change account options.", false);
				return false;
			}
			else {
				final String user_id = (String) json.get("user_id");
				if(user_id.replaceAll("[0-9]*", "").length() != 0) {
					WebserviceUtils.return502(out, "User id is not numeric.", false);
					return false;
				}
				else if(user_id.length() != 17 && user_id.length() != 18) {
					WebserviceUtils.return502(out, "The user id needs to be either 17 or 18 digits long.", false);
					return false;
				}
			}
			if(!json.has("ip")) {
				WebserviceUtils.return502(out, "Source address not found.", false);
				return false;
			}
			else {
				final String ip = json.getString("ip");
				if(!ip.matches("([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}|::1)")) {
					WebserviceUtils.return502(out, "Source address invalid.", false);
					return false;
				}
			}
			if(!json.has("body") && json.get("body") instanceof JSONObject) {
				WebserviceUtils.return502(out, "Body message required.", false);
				return false;
			}
			else {
				final JSONObject body = (JSONObject) json.get("body");
				if(!body.has("field") || !body.has("value")) {
					WebserviceUtils.return502(out, "Field reference with value required.", false);
					return false;
				}
				else if(!body.has("guild_id")) {
					WebserviceUtils.return502(out, "Referenced guild is required.", false);
					return false;
				}
			}
		}
		
		return true;
	}
	
	private static void google(ReadyEvent e, PrintWriter out, JSONObject json) {
		final String action = json.getString("action");
		if(action.equals("export")) {
			if(json.has("guild_id")) {
				Guild guild = e.getJDA().getGuildById(json.getLong("guild_id"));
				if(guild != null) {
					if(GuildIni.getGoogleFunctionalitiesEnabled(guild.getIdLong()) && GuildIni.getGoogleSpreadsheetsEnabled(guild.getIdLong())) {
						if(GoogleSheets.spreadsheetExportRequest(Azrael.SQLgetGoogleFilesAndEvent(guild.getIdLong(), 2, GoogleEvent.EXPORT.id, ""), guild, "", e.getJDA().getSelfUser().getId(), new Timestamp(System.currentTimeMillis()), e.getJDA().getGatewayPing(), guild.getMemberCount(), e.getJDA().getGuilds().size())) {
							WebserviceUtils.return201(out, "Data exported to google spreadsheet.", false);
						}
						else {
							WebserviceUtils.return501(out, "Data couldn't be exported into the spreadsheet due to an unknown error.", false);
						}
					}
					else {
						WebserviceUtils.return200(out, "Request accepted but spreadsheet settings are not set for this guild.", false, false);
					}
				}
				else {
					WebserviceUtils.return502(out, "Invalid guild_id. Guild not found.", false);
				}
			}
			else {
				long guilds_count = e.getJDA().getGuilds().size();
				for(final var guild : e.getJDA().getGuilds()) {
					if(GuildIni.getGoogleFunctionalitiesEnabled(guild.getIdLong()) && GuildIni.getGoogleSpreadsheetsEnabled(guild.getIdLong())) {
						GoogleSheets.spreadsheetExportRequest(Azrael.SQLgetGoogleFilesAndEvent(guild.getIdLong(), 2, GoogleEvent.EXPORT.id, ""), guild, "", e.getJDA().getSelfUser().getId(), new Timestamp(System.currentTimeMillis()), e.getJDA().getGatewayPing(), guild.getMemberCount(), guilds_count);
					}
				}
				WebserviceUtils.return200(out, "Request accepted for all guilds.", false, false);
			}
		}
	}
	
	private static void shutdown(ReadyEvent e, PrintWriter out, JSONObject json) {
		FileSetting.createFile(IniFileReader.getTempDirectory()+STATIC.getSessionName()+"running.azr", "0");
		WebserviceUtils.return200(out, "Bot shutdown", false, false);
		e.getJDA().getGuilds().parallelStream().forEach(guild -> {
			if(GuildIni.getNotifications(guild.getIdLong())) {
				if(json.has("message")) {
					STATIC.writeToRemoteChannel(guild, null, json.getString("message"), Channel.LOG.getType());
				}
				else {
					STATIC.writeToRemoteChannel(guild, null, STATIC.getTranslation2(guild, Translation.SHUTDOWN_SOON), Channel.LOG.getType());
				}
			}
			ShutDown.saveCache(guild);
		});
		Invites.enableShutdownMode();
		while(true) {
			if(Invites.inviteStatus.isEmpty())
				break;
			try {
				Thread.sleep(TimeUnit.SECONDS.toMillis(10));
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		if(STATIC.getGoogleThreadCount() == 0)
			e.getJDA().shutdown();
		else {
			ShutdownListener.setShutdownJDA(e.getJDA());
			STATIC.killGoogleThreads();
		}
	}
	
	private static void webLogin(ReadyEvent e, PrintWriter out, JSONObject json) {
		final long user_id = json.getLong("user_id");
		final String address = json.getString("ip");
		if(e.getJDA().getSelfUser().getIdLong() != user_id) {
			Guild guild = e.getJDA().getGuilds().parallelStream().filter(g -> g.getMemberById(user_id) != null).findAny().orElse(null);
			if(guild != null) {
				Member member = guild.getMemberById(user_id);
				if(member != null) {
					if(!member.getUser().isBot()) {
						final String key = RandomStringUtils.random(6, true, true).toUpperCase();
						final String displayCode = key.substring(0, 2)+"-"+key.substring(2, 4)+"-"+key.substring(4);
						final var channel = member.getUser().openPrivateChannel().complete();
						try {
							channel.sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(member, Translation.WEB_CODE).replace("{}", displayCode)).build()).queue();
							if(AzraelWeb.SQLInsertLoginInfo(user_id, 2, key) > 0) {
								WebserviceUtils.return200(out, "Login key sent!", true, false);
								AzraelWeb.SQLCodeUsageLog(user_id, address);
								AzraelWeb.SQLInsertActionLog(user_id, address, "CODE_GENERATED", key);
							}
							else {
								WebserviceUtils.return500(out, "An unnexpected error occurred! Please try again later!", true);
							}
						} catch(Exception exc) {
							WebserviceUtils.return500(out, "Direct messages are locked for this user!", true);
							AzraelWeb.SQLCodeUsageLog(user_id, address);
							AzraelWeb.SQLInsertActionLog(user_id, address, "CODE_GENERATION_ATTEMPT", "Destination user has locked direct messages.");
						}
					}
					else {
						WebserviceUtils.return502(out, "Authentication through other Bots not possible!", true);
						AzraelWeb.SQLCodeUsageLog(user_id, address);
						AzraelWeb.SQLInsertActionLog(user_id, address, "CODE_GENERATION_ATTEMPT", "Attempted to message a Bot.");
					}
				}
				else {
					WebserviceUtils.return404(out, "User not found!", true);
					AzraelWeb.SQLInsertActionLog(user_id, address, "CODE_GENERATION_ATTEMPT", "User not found.");
				}
			}
			else {
				WebserviceUtils.return404(out, "User not found!", true);
				AzraelWeb.SQLInsertActionLog(user_id, address, "CODE_GENERATION_ATTEMPT", "User not found.");
			}
		}
		else {
			WebserviceUtils.return502(out, "Please insert your discord name and not the one of the Bot!", true);
			AzraelWeb.SQLCodeUsageLog(user_id, address);
			AzraelWeb.SQLInsertActionLog(user_id, address, "CODE_GENERATION_ATTEMPT", "Attempted to message the main Bot.");
		}
	}
	
	private static void confirm(ReadyEvent e, PrintWriter  out, JSONObject json) {
		final long user_id = json.getLong("user_id");
		final JSONObject body = (JSONObject)json.get("body");
		final String confirmToken = body.getString("confirmToken");
		final String address = json.getString("ip");
		Guild guild = e.getJDA().getGuilds().parallelStream().filter(g -> g.getMemberById(user_id) != null).findAny().orElse(null);
		if(guild != null) {
			Member member = guild.getMemberById(user_id);
			if(member != null) {
				if(!member.getUser().isBot()) {
					final var channel = member.getUser().openPrivateChannel().complete();
					try {
						channel.sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(member, Translation.WEB_CONFIRM)+IniFileReader.getWebURL()+"/account/confirm.php?key="+confirmToken).build()).queue();
						AzraelWeb.SQLInsertActionLog(user_id, address, "ACCOUNT_CONFIRMATION_SENT", "Confirmation sent.");
						WebserviceUtils.return200(out, "Success", true, false);
					} catch(Exception exc) {
						WebserviceUtils.return500(out, "Direct messages are locked for this user!", true);
						AzraelWeb.SQLInsertActionLog(user_id, address, "ACCOUNT_CONFIRMATION_ATTEMPT", "Destination user has locked direct messages.");
					}
				}
				else {
					WebserviceUtils.return404(out, "User not found!", true);
					AzraelWeb.SQLInsertActionLog(user_id, address, "ACCOUNT_CONFIRMATION_ATTEMPT", "User not found.");
				}
			}
			else {
				WebserviceUtils.return404(out, "User not found!", true);
				AzraelWeb.SQLInsertActionLog(user_id, address, "ACCOUNT_CONFIRMATION_ATTEMPT", "User not found.");
			}
		}
		else {
			WebserviceUtils.return404(out, "User not found!", true);
			AzraelWeb.SQLInsertActionLog(user_id, address, "ACCOUNT_CONFIRMATION_ATTEMPT", "User not found.");
		}
	}
	
	private static void webRecovery(ReadyEvent e, PrintWriter out, JSONObject json) {
		final long user_id = json.getLong("user_id");
		final String address = json.getString("ip");
		Guild guild = e.getJDA().getGuilds().parallelStream().filter(g -> g.getMemberById(user_id) != null).findAny().orElse(null);
		if(guild != null) {
			Member member = guild.getMemberById(user_id);
			if(member != null) {
				final String key = RandomStringUtils.random(6, true, true).toUpperCase();
				final String displayCode = key.substring(0, 2)+"-"+key.substring(2, 4)+"-"+key.substring(4);
				final var channel = member.getUser().openPrivateChannel().complete();
				try {
					channel.sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(member, Translation.WEB_RECOVERY).replace("{}", displayCode)).build()).queue();
					if(AzraelWeb.SQLInsertLoginInfo(user_id, 3, key) > 0) {
						WebserviceUtils.return200(out, "Recovery key sent!", true, false);
						AzraelWeb.SQLInsertActionLog(user_id, address, "RECOVERY_CODE_GENERATED", key);
					}
					else {
						WebserviceUtils.return500(out, "An unnexpected error occurred! Please try again later!", true);
					}
				} catch(Exception exc) {
					WebserviceUtils.return500(out, "Direct messages are locked for this user!", true);
					AzraelWeb.SQLInsertActionLog(user_id, address, "RECOVERY_CODE_GENERATION_ATTEMPT", "Destination user has locked direct messages.");
				}
			}
			else {
				WebserviceUtils.return404(out, "User not found!", true);
				AzraelWeb.SQLInsertActionLog(user_id, address, "RECOVERY_CODE_GENERATION_ATTEMPT", "User not found.");
			}
		}
		else {
			WebserviceUtils.return404(out, "User not found!", true);
			AzraelWeb.SQLInsertActionLog(user_id, address, "RECOVERY_CODE_GENERATION_ATTEMPT", "User not found.");
		}
	}
	
	private static void accountOptions(ReadyEvent e, PrintWriter out, JSONObject json) {
		final long user_id = json.getLong("user_id");
		final String address = json.getString("ip");
		Guild guild = e.getJDA().getGuilds().parallelStream().filter(g -> g.getMemberById(user_id) != null).findAny().orElse(null);
		if(guild != null) {
			Member member = guild.getMemberById(user_id);
			if(member != null) {
				final String key = RandomStringUtils.random(6, true, true).toUpperCase();
				final var channel = member.getUser().openPrivateChannel().complete();
				try {
					channel.sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(member, Translation.WEB_OPTIONS).replace("{}", key)).build()).queue();
					if(AzraelWeb.SQLInsertLoginInfo(user_id, 4, key) > 0) {
						WebserviceUtils.return200(out, "Options change key sent!", true, false);
						AzraelWeb.SQLInsertActionLog(user_id, address, "OPTIONS_CODE_GENERATED", key);
					}
					else {
						WebserviceUtils.return500(out, "An unnexpected error occurred! Please try again later!", true);
					}
				} catch(Exception exc) {
					WebserviceUtils.return500(out, "Direct messages are locked for this user!", true);
					AzraelWeb.SQLInsertActionLog(user_id, address, "RECOVERY_CODE_GENERATION_ATTEMPT", "Destination user has locked direct messages.");
				}
			}
			else {
				WebserviceUtils.return404(out, "User not found!", true);
				AzraelWeb.SQLInsertActionLog(user_id, address, "RECOVERY_CODE_GENERATION_ATTEMPT", "User not found.");
			}
		}
		else {
			WebserviceUtils.return404(out, "User not found!", true);
			AzraelWeb.SQLInsertActionLog(user_id, address, "RECOVERY_CODE_GENERATION_ATTEMPT", "User not found.");
		}
	}
	
	private static void optionUpdate(ReadyEvent e, PrintWriter out, JSONObject json) {
		final long user_id = json.getLong("user_id");
		final String address = json.getString("ip");
		final JSONObject body = (JSONObject)json.get("body");
		final long guild_id = body.getLong("guild_id");
		final String field = body.getString("field");
		final String value = body.getString("value");
		
		final Guild guild = e.getJDA().getGuildById(guild_id);
		if(guild != null) {
			Member serverMember = guild.getMemberById(user_id);
			if(serverMember != null) {
				if(UserPrivs.isUserAdmin(serverMember) || serverMember.getUser().getIdLong() == GuildIni.getAdmin(guild.getIdLong())) {
					boolean valid = false;
					boolean textFileEdit = false;
					switch(field) {
						case "General_JoinMessage", "General_LeaveMessage", "General_CacheLog", "General_URLBlacklist", "General_ForceReason", "General_OverrideBan",
						"General_SelfDeletedMessage", "General_EditedMessage", "General_EditedMessageHistory", "General_Notifications", "General_NewAccountOnJoin",
						"General_ReassignRolesAfterMute", "General_CollectRankingRoles", "Google_FunctionalitiesEnabled", "Google_SpreadsheetsEnabled", 
						"Messages_SpamDetection", "Patch_PrivatePatchNotes", "Patch_PublicPatchNotes", "Mute_MessageDeleteEnabled", "Mute_ForceMessageDeletion",
						"Mute_SendReason", "Kick_MessageDeleteEnabled", "Kick_ForceMessageDeletion", "Kick_SendReason", "Ban_MessageDeleteEnabled", 
						"Ban_ForceMessageDeletion", "Ban_SendReason", "Reactions_Enabled", "Commands_About", "Commands_Daily", "Commands_Display", "Commands_Help",
						"Commands_Inventory", "Commands_Meow", "Commands_Pug", "Commands_Profile", "Commands_Rank", "Commands_Register", "Commands_Set", "Commands_Shop",
						"Commands_Top", "Commands_Use", "Commands_User", "Commands_Filter", "Commands_Quiz", "Commands_RoleReaction", "Commands_Subscribe",
						"Commands_Randomshop", "Commands_Patchnotes", "Commands_DoubleExperience", "Commands_Equip", "Commands_Remove", "Commands_HeavyCensoring",
						"Commands_Mute", "Commands_Google", "Commands_Write", "Commands_Edit", "Commands_Matchmaking", "Commands_Join", "Commands_Clan", "Commands_Leave",
						"Commands_Queue", "Commands_Cw", "Commands_Room", "Commands_Stats", "Commands_Leaderboard", "Commands_Accept", "Commands_Deny", "Commands_Language",
						"Commands_Schedule", "Commands_Prune", "Commands_Warn", "Commands_Reddit", "Commands_Invites" -> {
							if(value.equals("true") || value.equals("false"))
								valid = true;
						}
						case "CommandLevels_About", "CommandLevels_Daily", "CommandLevels_Display", "CommandLevels_DisplayRoles", "CommandLevels_DisplayRegisteredRoles",
						"CommandLevels_DisplayRankingRoles", "CommandLevels_DisplayCategories", "CommandLevels_DisplayRegisteredCategories", "CommandLevels_DisplayTextChannels",
						"CommandLevels_DisplayVoiceChannels", "CommandLevels_DisplayRegisteredChannels", "CommandLevels_DisplayDailies", "CommandLevels_DisplayWatchedUsers",
						"CommandLevels_DisplayCommandLevels", "CommandLevels_Help", "CommandLevels_Inventory", "CommandLevels_Meow", "CommandLevels_Pug", "CommandLevels_Profile",
						"CommandLevels_Rank", "CommandLevels_Register", "CommandLevels_RegisterRole", "CommandLevels_RegisterCategory", "CommandLevels_RegisterTextChannel",
						"CommandLevels_RegisterTextChannelURL", "CommandLevels_RegisterTextChannelTXT", "CommandLevels_RegisterRankingRole", "CommandLevels_RegisterTextChannels",
						"CommandLevels_RegisterUsers", "CommandLevels_Set", "CommandLevels_SetPrivilege", "CommandLevels_SetChannelFilter", "CommandLevels_SetWarnings",
						"CommandLevels_SetRanking", "CommandLevels_SetMaxExperience", "CommandLevels_SetDefaultLevelSkin", "CommandLevels_SetDefaultRankSkin",
						"CommandLevels_SetDefaultProfileSkin", "CommandLevels_SetDefaultIconSkin", "CommandLevels_SetDailyItem", "CommandLevels_SetGiveawayItems",
						"CommandLevels_SetCompServer", "CommandLevels_SetMaxClanMembers", "CommandLevels_SetMatchmakingMembers", "CommandLevels_SetMaps",
						"CommandLevels_SetLanguage", "CommandLevels_Shop", "CommandLevels_Top", "CommandLevels_Use", "CommandLevels_User", "CommandLevels_UserInformation",
						"CommandLevels_UserDeleteMessages", "CommandLevels_UserWarning", "CommandLevels_UserWarningForce", "CommandLevels_UserMute", "CommandLevels_UserUnmute",
						"CommandLevels_UserBan", "CommandLevels_UserUnban", "CommandLevels_UserKick", "CommandLevels_UserAssignRole", "CommandLevels_UserRemoveRole",
						"CommandLevels_UserHistory", "CommandLevels_UserWatch", "CommandLevels_UserUnwatch", "CommandLevels_UserUseWatchChannel", "CommandLevels_UserGiftExperience",
						"CommandLevels_UserSetExperience", "CommandLevels_UserSetLevel", "CommandLevels_UserGiftCurrency", "CommandLevels_UserSetCurrency", "CommandLevels_Filter",
						"CommandLevels_FilterWordFilter", "CommandLevels_FilterNameFilter", "CommandLevels_FilterNameKick", "CommandLevels_FilterFunnyNames",
						"CommandLevels_FilterStaffNames", "CommandLevels_FilterURLBlacklist", "CommandLevels_FilterURLWhitelist", "CommandLevels_FilterTweetBlacklist",
						"CommandLevels_Quiz", "CommandLevels_RoleReaction", "CommandLevels_Subscribe", "CommandLevels_Randomshop", "CommandLevels_Patchnotes",
						"CommandLevels_DoubleExperience", "CommandLevels_Equip", "CommandLevels_Remove", "CommandLevels_HeavyCensoring", "CommandLevels_Mute", 
						"CommandLevels_Google", "CommandLevels_Write", "CommandLevels_Edit", "CommandLevels_Matchmaking", "CommandLevels_Join", "CommandLevels_Clan",
						"CommandLevels_Leave", "CommandLevels_Queue", "CommandLevels_Cw", "CommandLevels_Room", "CommandLevels_RoomClose", "CommandLevels_RoomWinner",
						"CommandLevels_RoomReopen", "CommandLevels_Stats", "CommandLevels_Leaderboard", "CommandLevels_Accept", "CommandLevels_Deny", "CommandLevels_Language",
						"CommandLevels_Schedule", "CommandLevels_Prune", "CommandLevels_Warn", "CommandLevels_Reddit", "CommandLevels_Invites" -> {
							if(value.matches("[0-9]*")) {
								final int convertedValue = Integer.parseInt(value);
								if(convertedValue >= 0 && convertedValue <= 100)
									valid = true;
							}
						}
						case "General_Administrator" -> {
							if(value.matches("[0-9]*") && guild.getMemberById(value) != null && user_id == GuildIni.getAdmin(guild.getIdLong()))
								valid = true;
						}
						case "General_CommandPrefix" -> {
							if(value.length() > 0 && value.length() <= 5)
								valid = true;
						}
						case "Competitive_Team1", "Competitive_Team2", "Reactions_Emoji1", "Reactions_Emoji2", "Reactions_Emoji3", "Reactions_Emoji4", "Reactions_Emoji5",
						"Reactions_Emoji6", "Reactions_Emoji7", "Reactions_Emoji8", "Reactions_Emoji9", "Reactions_VoteThumbsUp", "Reactions_VoteThumbsDown", "Reactions_VoteShrug" -> {
							if(value.length() >= 0 && value.length() <= 30)
								valid = true;
						}
						case "General_DoubleExperience" -> {
							if(value.equals("false") || value.equals("true") || value.equals("auto"))
								valid = true;
						}
						case "Google_MainEmail" -> {
							if((value.matches("^[^.][\\w\\d!#$%&'*+\\-\\/=?^_`{|}~.]*@[a-zA-Z0-9.]*\\.[a-z0-9]{2,5}$") && value.length() <= 30) || value.length() == 0)
								valid = true;
						}
						case "Messages_MessagesLimit", "Messages_MessagesOverChannelsLimit" -> {
							if(value.matches("[0-9]*")) {
								final int convertedValue = Integer.parseInt(value);
								if(convertedValue >= 0 && convertedValue <= 10)
									valid = true;
							}
						}
						case "Messages_Expires" -> {
							if(value.matches("[0-9]*")) {
								final int convertedValue = Integer.parseInt(value);
								if(convertedValue > 0 && convertedValue <= 1440)
									valid = true;
							}
						}
						case "Mute_AutoDeleteMessages", "Kick_AutoDeleteMessages", "Ban_AutoDeleteMessages" -> {
							if(value.matches("[0-9]*")) {
								final int convertedValue = Integer.parseInt(value);
								if(convertedValue > 0 && convertedValue <= 100)
									valid = true;
							}
						}
						case "CustomMessages_reactionmessage", "CustomMessages_verificationmessage", "CustomMessages_assignmessage" -> {
							valid = true;
							textFileEdit = true;
						}
						default -> {
							WebserviceUtils.return404(out, "Field "+field+" not found!", true);
							return;
						}
					}
					
					if(valid) {
						if(!textFileEdit) {
							if(GuildIni.saveIniOption(guild.getIdLong(), field, value)) {
								WebserviceUtils.return200(out, "Option updated!", true, false);
								AzraelWeb.SQLInsertActionLog(user_id, address, "BOT_OPTION_UPDATED", field+" for guild "+guild.getIdLong());
								
								if(field.equals("General_DoubleExperience")) {
									GuildIni.setDoubleExperienceMode(guild.getIdLong(), value);
									if(!value.equals("auto"))
										Hashes.addTempCache("doubleExp_gu"+guild.getId(), new Cache(value));
									else 
										Hashes.clearTempCache("doubleExp_gu"+guild.getId());
								}
							}
							else {
								WebserviceUtils.return500(out, "An internal error occurred! Value couldn't be saved!", true);
							}
						}
						else {
							FileSetting.createFile("./files/Guilds/"+guild.getId()+"/"+field.split("_")[1]+".txt", value);
							WebserviceUtils.return200(out, "Option updated!", true, false);
							AzraelWeb.SQLInsertActionLog(user_id, address, "BOT_OPTION_UPDATED", field+" for guild "+guild.getIdLong());
						}
					}
					else {
						WebserviceUtils.return500(out, "Unkown value found!", true);
					}
				}
				else {
					WebserviceUtils.return401(out, "Permissions missing to change options!", true);
				}
			}
			else {
				WebserviceUtils.return404(out, "Guild member of action executioner not found!", true);
			}
		}
		else {
			WebserviceUtils.return404(out, "Guild not found!", true);
		}
	}
}
