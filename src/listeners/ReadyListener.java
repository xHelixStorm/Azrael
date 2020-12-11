package listeners;

import java.awt.Color;
import java.io.File;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commands.CustomCmd;
import constructors.Cache;
import constructors.Guilds;
import constructors.Messages;
import constructors.Patchnote;
import core.CommandHandler;
import core.Hashes;
import enums.Channel;
import enums.Translation;
import enums.Weekday;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rankingSystem.DoubleExperienceOff;
import rankingSystem.DoubleExperienceStart;
import sql.RankingSystem;
import sql.Azrael;
import sql.DiscordRoles;
import sql.Patchnotes;
import threads.CollectUsersGuilds;
import threads.RoleExtend;
import threads.Webserver;
import timerTask.ClearCommentedUser;
import timerTask.ClearHashes;
import timerTask.ParseSubscription;
import timerTask.VerifyMutedMembers;
import util.STATIC;

/**
 * First event that gets executed on start up and on successful login to Discord
 * 
 * collect roles, settings, etc before the Bot is fully operational for taking
 * commands, restart timers for muted users and start timers for regular check ups and 
 * clean ups
 * @author xHelixStorm
 *
 */

public class ReadyListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(ReadyListener.class);
	
	@Override
	public void onReady(ReadyEvent e) {
		//Save the time when the Bot successfully booted up
		STATIC.initializeBootTime();
		
		final String tempDirectory = IniFileReader.getTempDirectory();
		//create the temp directory and verify if multiple sessions are running. If yes, terminate this session
		FileSetting.createTemp(e);
		if(new File(tempDirectory+STATIC.getSessionName()+"running.azr").exists() && FileSetting.readFile(tempDirectory+STATIC.getSessionName()+"running.azr").contains("1")) {
			FileSetting.createFile(IniFileReader.getTempDirectory()+STATIC.getSessionName()+"running.azr", "2");
			e.getJDA().shutdownNow();
			return;
		}
		FileSetting.createFile(IniFileReader.getTempDirectory()+STATIC.getSessionName()+"running.azr", "1");
		
		//print default message + version
		System.out.println();
		System.out.println("Azrael Version: "+STATIC.getVersion()+"\nAll credits to xHelixStorm");
		System.out.println();
		
		//initialize variables of ini files and login into twitter, if the keys have been provided
		GuildIni.initialize();
		STATIC.loginTwitter();

		//Iterate through all joined guilds
		for(Guild guild : e.getJDA().getGuilds()) {
			//create a guild ini file for new servers or verify if there are any old or missing variables that need to be added or removed
			FileSetting.createGuildDirectory(guild);
			if(!new File("ini/"+guild.getId()+".ini").exists())
				GuildIni.createIni(guild.getIdLong());
			else
				GuildIni.verifyIni(guild.getIdLong());
			
			//verify that the guild is registered in the database, if not insert the current guild into the database
			if(Azrael.SQLgetGuild(guild.getIdLong()) == 0) {
				if(Azrael.SQLInsertGuild(guild.getIdLong(), guild.getName()) == 0) {
					logger.error("General guild information couldn't be saved in guild {}", guild.getId());
				}
			}
			//verify that the guild is registered in the discord roles database and do the same step
			if(DiscordRoles.SQLgetGuild(guild.getIdLong()) == 0) {
				if(DiscordRoles.SQLInsertGuild(guild.getIdLong(), guild.getName()) == 0) {
					logger.error("Guild roles information couldn't be saved in guild {}", guild.getId());
				}
			}
			//verify that the guild is registered in the ranking system database and do the same step
			if(RankingSystem.SQLgetGuild(guild.getIdLong()) == null) {
				if(RankingSystem.SQLInsertGuild(guild.getIdLong(), guild.getName(), false) == 0) {
					logger.error("Guild ranking information couldn't be saved in guild {}", guild.getId());
				}
			}
			//verify that the guild is registered in the patch notes database and do the same step
			if(Patchnotes.SQLgetGuild(guild.getIdLong()) == 0) {
				if(Patchnotes.SQLInsertGuild(guild.getIdLong(), guild.getName()) == 0) {
					logger.error("Guild patchnotes information couldn't be saved in guild {}", guild.getId());
				}
			}
			//Retrieve all registered channels and throw warning, if no channel has been registered. If found, check for the log and bot channel
			var channels = Azrael.SQLgetChannels(guild.getIdLong());
			if(channels == null) {
				logger.warn("Channel information couldn't be retrieved and cached in guild {}", guild.getId());
			}
			//retrieve all registered discord roles, if empty insert the roles into the database and throw an error if this fails as well
			if(DiscordRoles.SQLgetRoles(guild.getIdLong()).size() == 0) {
				var result = DiscordRoles.SQLInsertRoles(guild.getIdLong(), guild.getRoles());
				if(result != null && result.length > 0 && result[0] == 1) {
					logger.info("Not registered information of all guild roles have been saved in guild {}", guild.getId());
					DiscordRoles.SQLgetRoles(guild.getIdLong());
				}
				else {
					logger.error("Roles couldn't be saved in guild {}", guild.getId());
				}
			}
			//retrieve all settings for the guild
			Guilds guild_settings = RankingSystem.SQLgetGuild(guild.getIdLong());
			if(guild_settings == null) {
				logger.error("Guild ranking information couldn't be retrieved and cached in guild {}", guild.getId());
			}
			//if the ranking system is enabled, retrieve all registered ranking roles and initialize guild ranking
			if(guild_settings != null && guild_settings.getRankingState()) {
				if(RankingSystem.SQLgetRoles(guild.getIdLong()).size() == 0) {
					logger.warn("Ranking roles couldn't be called and cached in guild {}", guild.getId());
				}
				Hashes.initializeGuildRanking(guild.getIdLong());
			}
			//retrieve all custom commands
			final var customCommands = Azrael.SQLgetCustomCommands(guild.getIdLong());
			if(customCommands != null && customCommands.size() > 0) {
				for(final String command : customCommands) {
					CommandHandler.commandsPublic.put(command, new CustomCmd());
				}
			}
			else if(customCommands == null) {
				logger.error("Custom commands couldn't be retrieved in guild {}", guild.getId());
			}
			//retrieve all registered rss feeds and start the timer to make these display on the server
			ParseSubscription.runTask(e.getJDA(), guild.getIdLong());
			
			//print public and private patch notes, if available for the current version of the bot
			Patchnote priv_notes = null;
			Patchnote publ_notes = null;
			//check if the newest patch notes have been already printed, if not, make it possible to print
			if(!Patchnotes.SQLcheckPublishedPatchnotes(guild.getIdLong())) {
				priv_notes = Patchnotes.SQLgetPrivatePatchnotes();
				publ_notes = Patchnotes.SQLgetPublicPatchnotes();
			}
			
			//retrieve private patch notes
			var published = false;
			//TODO: Text should be translated
			if(priv_notes != null && GuildIni.getPrivatePatchNotes(guild.getIdLong())) {
				EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.MAGENTA).setThumbnail(e.getJDA().getSelfUser().getAvatarUrl()).setTitle(STATIC.getTranslation2(guild, Translation.PATCHNOTES_LATEST_TITLE));
				final var result = STATIC.writeToRemoteChannel(guild, messageBuild, "Bot patch notes version **"+STATIC.getVersion()+"** "+priv_notes.getDate()+"\n"+priv_notes.getMessage1(), Channel.LOG.getType());
				if(result) {
					if(priv_notes.getMessage2() != null && priv_notes.getMessage2().length() > 0)
						STATIC.writeToRemoteChannel(guild, messageBuild, priv_notes.getMessage2(), Channel.LOG.getType());
					published = true;
				}
			}
			//retrieve public patch notes
			if(publ_notes != null && GuildIni.getPublicPatchNotes(guild.getIdLong())) {
				EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.MAGENTA).setThumbnail(e.getJDA().getSelfUser().getAvatarUrl()).setTitle(STATIC.getTranslation2(guild, Translation.PATCHNOTES_LATEST_TITLE));
				final var result = STATIC.writeToRemoteChannel(guild, messageBuild, "Bot patch notes version **"+STATIC.getVersion()+"** "+publ_notes.getDate()+"\n"+publ_notes.getMessage1(), Channel.BOT.getType());
				if(result) {
					STATIC.writeToRemoteChannel(guild, messageBuild, publ_notes.getMessage2(), Channel.BOT.getType());
					published = true;
				}
			}
			//if patch notes have been printed, mark it as printed for the current guild in the database
			if(published) {
				Patchnotes.SQLInsertPublishedPatchnotes(guild.getIdLong());
			}
			
			//check if double exp should be enabled or disabled for the current guild
			var doubleExp = GuildIni.getDoubleExperienceMode(guild.getIdLong());
			if(!doubleExp.equals("auto"))
				Hashes.addTempCache("doubleExp_gu"+guild.getId(), new Cache(0, doubleExp));
			
			//initialize Message pool cache and load saved messages, if available
			Hashes.initializeGuildMessagePool(guild.getIdLong(), 10000);
			if(GuildIni.getCacheLog(guild.getIdLong())) {
				if(new File(tempDirectory+"message_pool"+guild.getId()+".json").exists()) {
					JSONObject json = null;
					try {
						json = new JSONObject(FileSetting.readFile(tempDirectory+"message_pool"+guild.getId()+".json"));
					} catch(JSONException e1) {
						logger.error("Error in retrieving message pool of past session in guild {}", guild.getId(), e1);
					}
					if(json != null) {
						final JSONObject messages = json;
						final var message_pool = Hashes.getWholeMessagePool(guild.getIdLong());
						json.keySet().forEach(k -> {
							Object el = messages.get(k);
							if(el instanceof JSONArray) {
								((JSONArray) el).forEach(item -> {
									if(item instanceof JSONObject) {
										final long message_id = ((JSONObject) item).getLong("message_id");
										final long channel_id = ((JSONObject) item).getLong("channel_id");
										final String channel_name = ((JSONObject) item).getString("channel_name");
										final long user_id = ((JSONObject) item).getLong("user_id");
										final String username = ((JSONObject) item).getString("username");
										final JSONArray history = ((JSONObject) item).getJSONArray("history");
										ArrayList<Messages> savedMessages = new ArrayList<Messages>();
										history.forEach(h -> {
											if(h instanceof JSONObject) {
												final boolean edit = ((JSONObject) h).getBoolean("edit");
												final ZonedDateTime date = ZonedDateTime.parse(((JSONObject) h).getString("date"));
												final String message = ((JSONObject) h).getString("message");
												
												Messages currentMessage = new Messages();
												currentMessage.setMessageID(message_id);
												currentMessage.setChannelID(channel_id);
												currentMessage.setChannelName(channel_name);
												currentMessage.setUserID(user_id);
												currentMessage.setUsername(username);
												currentMessage.setIsEdit(edit);
												currentMessage.setTime(date);
												currentMessage.setMessage(message);
												savedMessages.add(currentMessage);
											}
										});
										message_pool.put(message_id, savedMessages);
									}
								});
							}
						});
						Hashes.setWholeMessagePool(guild.getIdLong(), message_pool);
						FileSetting.deleteFile(tempDirectory+"message_pool"+guild.getId()+".json");
					}
				}
			}
		}
		Azrael.SQLInsertActionLog("BOT_BOOT", e.getJDA().getSelfUser().getIdLong(), 0, "Launched");
		
		//execute background threads to collect current users, users under watch, text channels and muted users 
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(() -> { Azrael.SQLgetWholeWatchlist(); });
		executor.execute(new CollectUsersGuilds(e, null));
		e.getJDA().getGuilds().parallelStream().forEach(g -> {
			//print bot is now operational message in all servers
			//TODO: translate message
			if(GuildIni.getNotifications(g.getIdLong()))
				STATIC.writeToRemoteChannel(g, null, "Bot is now operational!", Channel.LOG.getType());
			executor.execute(new RoleExtend(g));
			Azrael.SQLBulkInsertCategories(g.getCategories());
			Azrael.SQLBulkInsertChannels(g.getTextChannels());
		});
		
		//if double experience is enabled, run 2 tasks for the start time and end time
		if(IniFileReader.getDoubleExpEnabled()) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.DAY_OF_WEEK, Weekday.getDay(IniFileReader.getDoubleExpStart()));
			Calendar calendar2 = Calendar.getInstance();
			calendar2.set(Calendar.DAY_OF_WEEK, Weekday.getDay(IniFileReader.getDoubleExpEnd()));
			calendar2.set(Calendar.HOUR_OF_DAY, 23);
			calendar2.set(Calendar.MINUTE, 59);
			var currentTime = System.currentTimeMillis();
			//if the bot has started right between the double experience time
			if(calendar.getTime().getTime() < currentTime && calendar2.getTime().getTime() > currentTime)
				Hashes.addTempCache("doubleExp", new Cache("on"));
			DoubleExperienceStart.runTask(e, null, null, null);
			DoubleExperienceOff.runTask();
		}
		ClearHashes.runTask();
		//check later on if there are any residual muted users which were not unmuted
		VerifyMutedMembers.runTask(e, null, null, true);
		
		//check if the ranking system should have a timeout for gaining experience points
		var timeout = IniFileReader.getMessageTimeout();
		if(timeout != 0)
			ClearCommentedUser.runTask(timeout);
		
		executor.shutdown();
		
		new Thread(new Webserver(e)).start();
	}
}
