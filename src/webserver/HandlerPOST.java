package webserver;

import java.io.PrintWriter;
import java.sql.Timestamp;

import org.json.JSONObject;

import commands.ShutDown;
import enums.Channel;
import enums.GoogleEvent;
import enums.Translation;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import google.GoogleUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
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
			WebserviceUtils.return502(out, "Item token required.");
			return false;
		}
		final String token = (String)json.get("token"); 
		if(!token.equals(STATIC.getToken())) {
			WebserviceUtils.return502(out, "Invalid Token");
			return false;
		}
		if(!json.has("type")) {
			WebserviceUtils.return502(out, "Item type required.");
			return false;
		}
		final String type = (String)json.get("type");
		if(!type.equals("shutdown") && !type.equals("google") && !type.equals("webLogin")) {
			WebserviceUtils.return502(out, "Invalid Type.");
			return false;
		}
		if(type.equals("shutdown")) {
			if(json.has("message") && !(json.get("message") instanceof String)) {
				WebserviceUtils.return502(out, "Shutdown message needs to be a string.");
				return false;
			}
		}
		if(type.equals("google")) {
			if(!json.has("action")) {
				WebserviceUtils.return502(out, "Item action required.");
				return false;
			}
			final String action = (String)json.get("action");
			if(!action.equals("export")) {
				WebserviceUtils.return502(out, "Invalid action for type google.");
				return false;
			}
			if(json.has("guild_id")) {
				final String guild_id = (String)json.get("guild_id");
				if(guild_id.replaceAll("[0-9]*", "").length() != 0) {
					WebserviceUtils.return502(out, "Invalid guild_id. Not numeric.");
					return false;
				}
				else if(guild_id.length() != 18) {
					WebserviceUtils.return502(out, "Invalid guild_id. Not 18 digits long.");
					return false;
				}
			}
		}
		if(type.equals("webLogin")) {
			if(!json.has("user_id")) {
				WebserviceUtils.return502(out, "User ID required to request the web login.");
				return false;
			}
			final String user_id = (String) json.get("user_id");
			if(user_id.replaceAll("[0-9]*", "").length() != 0) {
				WebserviceUtils.return502(out, "User id is not numeric.");
				return false;
			}
			else if(user_id.length() != 17 && user_id.length() != 18) {
				WebserviceUtils.return502(out, "The user id needs to be either 17 or 18 digits long.");
				return false;
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
						if(GoogleUtils.handleSpreadsheetRequest(guild, e.getJDA().getSelfUser().getId(), new Timestamp(System.currentTimeMillis()), e.getJDA().getSelfUser().getName()+"#"+e.getJDA().getSelfUser().getDiscriminator(), "EXPORT", e.getJDA().getGatewayPing(), guild.getMemberCount(), e.getJDA().getGuilds().size(), GoogleEvent.EXPORT.id)) {
							WebserviceUtils.return201(out, "Data exported to google spreadsheet.");
						}
						else {
							WebserviceUtils.return501(out, "Data couldn't be exported into the spreadsheet due to an unknown error.");
						}
					}
					else {
						WebserviceUtils.return200(out, "Request accepted but spreadsheet settings are not set for this guild.");
					}
				}
				else {
					WebserviceUtils.return502(out, "Invalid guild_id. Guild not found.");
				}
			}
			else {
				long guilds_count = e.getJDA().getGuilds().size();
				for(final var guild : e.getJDA().getGuilds()) {
					if(GuildIni.getGoogleFunctionalitiesEnabled(guild.getIdLong()) && GuildIni.getGoogleSpreadsheetsEnabled(guild.getIdLong())) {
						GoogleUtils.handleSpreadsheetRequest(guild, e.getJDA().getSelfUser().getId(), new Timestamp(System.currentTimeMillis()), e.getJDA().getSelfUser().getName()+"#"+e.getJDA().getSelfUser().getDiscriminator(), "EXPORT", e.getJDA().getGatewayPing(), guild.getMemberCount(), guilds_count, GoogleEvent.EXPORT.id);
					}
				}
				WebserviceUtils.return200(out, "Request accepted for all guilds.");
			}
		}
	}
	
	private static void shutdown(ReadyEvent e, PrintWriter out, JSONObject json) {
		FileSetting.createFile(IniFileReader.getTempDirectory()+STATIC.getSessionName()+"running.azr", "0");
		WebserviceUtils.return200(out, "Bot shutdown");
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
		WebserviceUtils.return404(out, "User not found!", true);
	}
}
