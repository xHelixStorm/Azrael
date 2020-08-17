package webserver;

import java.io.PrintWriter;
import java.sql.Timestamp;

import org.json.JSONObject;

import enums.GoogleEvent;
import enums.Translation;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import google.GoogleUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import sql.Azrael;
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
		if(!type.equals("shutdown") && !type.equals("google")) {
			WebserviceUtils.return502(out, "Invalid Type.");
			return false;
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
		for(final var guild : e.getJDA().getGuilds()) {
			final var log_channel = Azrael.SQLgetChannels(guild.getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
			if(log_channel != null) {
				guild.getTextChannelById(log_channel.getChannel_ID()).sendMessage(STATIC.getTranslation2(guild, Translation.SHUTDOWN)).queue();
			}
		}
		WebserviceUtils.return200(out, "Bot shutdown");
		e.getJDA().shutdown();
	}
}
