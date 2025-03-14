package de.azrael.commands;

import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.enums.Command;
import de.azrael.enums.Directory;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.listeners.ShutdownListener;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.util.FileHandler;
import de.azrael.util.Hashes;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Shutdown the Bot with this command
 * @author xHelixStorm
 *
 */

public class ShutDown implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(ShutDown.class);

	@Override
	public boolean called(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		return true;
	}

	@Override
	public boolean action(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		if(BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
			FileHandler.createFile(Directory.TEMP, System.getProperty("SESSION_NAME")+"running.azr", "0");
			e.getChannel().sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.SHUTDOWN_PREP)).queue();
			for(final Guild guild : e.getJDA().getGuilds()) {
				botConfig = BotConfiguration.SQLgetBotConfigs(guild.getIdLong());
				saveCache(guild, botConfig);
			}
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
			if(STATIC.getGoogleThreadCount() == 0) {
				e.getChannel().sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.SHUTDOWN)).queue(m -> {
					e.getJDA().shutdown();
				}, err -> {
					e.getJDA().shutdown();
				});
			}
			else {
				ShutdownListener.setShutdownChannel(e.getChannel().asTextChannel());
				STATIC.killGoogleThreads();
			}
			return true;
		}
		return false;
	}

	@Override
	public void executed(String[] args, boolean success, MessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Shutdown command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SHUTDOWN.getColumn(), out.toString().trim());
		}
	}
	
	public static void saveCache(Guild guild, BotConfigs botConfig) {
		if(botConfig.getCacheLog()) {
			JSONObject json = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			final var message_pool = Hashes.getWholeMessagePool(guild.getIdLong());
			message_pool.forEach((k, v) -> {
				JSONObject messageHeader = new JSONObject();
				JSONArray messageArray = new JSONArray();
				messageHeader.put("message_id", k);
				v.stream().forEach(message -> {
					JSONObject messageBody = new JSONObject();
					if(!messageHeader.has("channel_id")) {
						messageHeader.put("channel_id", message.getChannelID());
						messageHeader.put("channel_name", message.getChannelName());
						messageHeader.put("user_id", message.getUserID());
						messageHeader.put("username", message.getUserName());
						messageHeader.put("isUserBot", message.isUserBot());
					}
					messageBody.put("edit", message.isEdit());
					messageBody.put("date", message.getTime());
					messageBody.put("message", message.getMessage());
					messageArray.put(messageBody);
				});
				messageHeader.put("history", messageArray);
				jsonArray.put(messageHeader);
			});
			json.put("messages", jsonArray);
			
			FileHandler.createFile(Directory.CACHE, "message_pool"+guild.getId()+".azr", json.toString());
		}
	}
}
