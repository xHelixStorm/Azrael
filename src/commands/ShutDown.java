package commands;

import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import enums.Translation;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import util.STATIC;

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
			saveCache(e);
			e.getChannel().sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.SHUTDOWN)).queue(m -> {
				e.getJDA().shutdown();
			}, err -> {
				e.getJDA().shutdown();
			});
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used ShutDown command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
	
	public static void saveCache(GuildMessageReceivedEvent e) {
		for(final var guild : e.getJDA().getGuilds()) {
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
}
