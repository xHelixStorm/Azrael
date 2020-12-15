package webserver;

import java.awt.Color;
import java.io.PrintWriter;
import java.sql.Timestamp;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;

import commands.ShutDown;
import enums.Channel;
import enums.GoogleEvent;
import enums.Translation;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import google.GoogleUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.ReadyEvent;
import sql.Azrael;
import sql.AzraelWeb;
import util.STATIC;

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
		if(!type.equals("shutdown") && !type.equals("google") && !type.equals("webLogin")) {
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
				if(!ip.matches("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}")) {
					WebserviceUtils.return502(out, "Source address invalid.", false);
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
						if(GoogleUtils.handleSpreadsheetRequest(Azrael.SQLgetGoogleFilesAndEvent(guild.getIdLong(), 2, GoogleEvent.EXPORT.id, ""), guild, "", e.getJDA().getSelfUser().getId(), new Timestamp(System.currentTimeMillis()), e.getJDA().getSelfUser().getName()+"#"+e.getJDA().getSelfUser().getDiscriminator(), "EXPORT", e.getJDA().getGatewayPing(), guild.getMemberCount(), e.getJDA().getGuilds().size(), GoogleEvent.EXPORT.id)) {
							WebserviceUtils.return201(out, "Data exported to google spreadsheet.", false);
						}
						else {
							WebserviceUtils.return501(out, "Data couldn't be exported into the spreadsheet due to an unknown error.", false);
						}
					}
					else {
						WebserviceUtils.return200(out, "Request accepted but spreadsheet settings are not set for this guild.", false);
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
						GoogleUtils.handleSpreadsheetRequest(Azrael.SQLgetGoogleFilesAndEvent(guild.getIdLong(), 2, GoogleEvent.EXPORT.id, ""), guild, "", e.getJDA().getSelfUser().getId(), new Timestamp(System.currentTimeMillis()), e.getJDA().getSelfUser().getName()+"#"+e.getJDA().getSelfUser().getDiscriminator(), "EXPORT", e.getJDA().getGatewayPing(), guild.getMemberCount(), guilds_count, GoogleEvent.EXPORT.id);
					}
				}
				WebserviceUtils.return200(out, "Request accepted for all guilds.", false);
			}
		}
	}
	
	private static void shutdown(ReadyEvent e, PrintWriter out, JSONObject json) {
		FileSetting.createFile(IniFileReader.getTempDirectory()+STATIC.getSessionName()+"running.azr", "0");
		WebserviceUtils.return200(out, "Bot shutdown", false);
		for(final var guild : e.getJDA().getGuilds()) {
			if(GuildIni.getNotifications(guild.getIdLong())) {
				if(json.has("message")) {
					STATIC.writeToRemoteChannel(guild, null, json.getString("message"), Channel.LOG.getType());
				}
				else {
					STATIC.writeToRemoteChannel(guild, null, STATIC.getTranslation2(guild, Translation.SHUTDOWN_SOON), Channel.LOG.getType());
				}
			}
			ShutDown.saveCache(guild);
		}
		e.getJDA().shutdown();
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
								WebserviceUtils.return200(out, "Login key sent!", true);
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
}
