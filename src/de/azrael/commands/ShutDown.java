package de.azrael.commands;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.core.Hashes;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.FileSetting;
import de.azrael.fileManagement.GuildIni;
import de.azrael.fileManagement.IniFileReader;
import de.azrael.interfaces.CommandPublic;
import de.azrael.listeners.ShutdownListener;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Shutdown the Bot with this command
 * @author xHelixStorm
 *
 */

public class ShutDown implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(ShutDown.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		return true;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		if(e.getMember().getUser().getIdLong() == IniFileReader.getAdmin()) {
			FileSetting.createFile(IniFileReader.getTempDirectory()+STATIC.getSessionName()+"running.azr", "0");
			e.getChannel().sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.SHUTDOWN_PREP)).queue();
			for(final Guild guild : e.getJDA().getGuilds()) {
				saveCache(guild);
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
				ShutdownListener.setShutdownChannel(e.getChannel());
				STATIC.killGoogleThreads();
			}
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.info("{} has used ShutDown command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
	
	public static void saveCache(Guild guild) {
		if(GuildIni.getCacheLog(guild.getIdLong())) {
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
			
			FileWriter file = null;
			try {
				file = new FileWriter(IniFileReader.getTempDirectory()+"message_pool"+guild.getId()+".json");
				file.write(json.toString());
			} catch (IOException e1) {
				logger.error("Error creating json file of message pool cache for guild {}", guild.getId(), e1);
			} finally {
				try {
					file.flush();
					file.close();
				} catch (IOException e1) {
					logger.error("Error creating json file of message pool cache for guild {}", guild.getId(), e1);
				}
			}
		}
	}
}
