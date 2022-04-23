package de.azrael.listeners;

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

import de.azrael.commands.CustomCmd;
import de.azrael.commandsContainer.ScheduleExecution;
import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.constructors.Guilds;
import de.azrael.constructors.Messages;
import de.azrael.constructors.Patchnote;
import de.azrael.core.CommandHandler;
import de.azrael.core.Hashes;
import de.azrael.enums.Channel;
import de.azrael.enums.Directory;
import de.azrael.enums.Translation;
import de.azrael.rankingSystem.DoubleExperienceStart;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.sql.DiscordRoles;
import de.azrael.sql.Patchnotes;
import de.azrael.sql.RankingSystem;
import de.azrael.threads.CollectUsersGuilds;
import de.azrael.threads.RoleExtend;
import de.azrael.threads.Webserver;
import de.azrael.timerTask.ClearHashes;
import de.azrael.timerTask.ParseSubscription;
import de.azrael.timerTask.VerifyMutedMembers;
import de.azrael.util.FileHandler;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

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
		
		//create the temp directory and verify if multiple sessions are running. If yes, terminate this session
		FileHandler.createTemp(e);
		final String fileName = System.getProperty("SESSION_NAME")+"running.azr";
		if(new File(Directory.TEMP.getPath()+fileName).exists()) {
			final String fileContent = FileHandler.readFile(Directory.TEMP, fileName).trim();
			if(!fileContent.isBlank() && fileContent.matches("[0-9]*")) {
				final var processHandler = ProcessHandle.of(Long.parseLong(fileContent));
				if(processHandler != null && processHandler.isPresent()) {
					e.getJDA().shutdownNow();
					return;
				}
			}
		}
		FileHandler.createFile(Directory.TEMP, fileName, ""+ProcessHandle.current().pid());
		
		//print default message + version
		System.out.println();
		System.out.println("Azrael Version: "+STATIC.getVersion()+"\nAll credits to xHelixStorm");
		System.out.println();
		
		//initialize Twitter login, if keys are available
		STATIC.loginTwitter();

		//Iterate through all joined guilds
		for(Guild guild : e.getJDA().getGuilds()) {
			//verify that the guild is registered in the database, if not insert the current guild into the database
			if(Azrael.SQLgetGuild(guild.getIdLong()) == 0) {
				if(Azrael.SQLInsertGuild(guild.getIdLong(), guild.getName()) == 0) {
					logger.error("General guild information couldn't be saved in guild {}", guild.getId());
				}
			}
			//verify that the guild is registered in the ranking system database and do the same step
			if(RankingSystem.SQLgetGuild(guild.getIdLong()) == null) {
				if(RankingSystem.SQLInsertGuild(guild.getIdLong(), guild.getName(), false) == 0) {
					logger.error("Guild ranking information couldn't be saved in guild {}", guild.getId());
				}
			}
			//verify that bot configurations exist in the bot configuration database
			BotConfigs botConfig = BotConfiguration.SQLgetBotConfigs(guild.getIdLong());
			if(botConfig.isDefault()) {
				if(!BotConfiguration.SQLInsertBotConfigs(guild.getIdLong())) {
					logger.error("Guild configuration couldn't be generated in guild {}", guild.getId());
				}
			}
			//verify that commands exist in the bot configurations, else create them
			if(!BotConfiguration.SQLCommandsAvailable(guild.getIdLong())) {
				if(!BotConfiguration.SQLInsertBotConfigs(guild.getIdLong())) {
					logger.error("Guild commands configuration couldn't be generated in guild {}", guild.getId());
				}
			}
			//verify that sub commands exist in the bot configurations, else create them
			if(!BotConfiguration.SQLSubCommandsAvailable(guild.getIdLong())) {
				if(!BotConfiguration.SQLInsertBotConfigs(guild.getIdLong())) {
					logger.error("Guild sub commands configuration couldn't be generated in guild {}", guild.getId());
				}
			}
			//verify that commands permissions exist in the bot configurations, else create them
			if(!BotConfiguration.SQLCommandsLevelAvailable(guild.getIdLong())) {
				if(!BotConfiguration.SQLInsertBotConfigs(guild.getIdLong())) {
					logger.error("Guild command permissions configuration couldn't be generated in guild {}", guild.getId());
				}
			}
			//verify that commands permissions exist in the bot configurations, else create them
			if(BotConfiguration.SQLgetThumbnails(guild.getIdLong()).isDefault()) {
				if(!BotConfiguration.SQLInsertBotConfigs(guild.getIdLong())) {
					logger.error("Default thumbnails couldn't be generated in guild {}", guild.getId());
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
			//retrieve all registered subscriptions and start the timer to make these display on the server
			ParseSubscription.runTask(e.getJDA());
			
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
			if(priv_notes != null && botConfig.getPrivatePatchNotes()) {
				EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.MAGENTA).setThumbnail(e.getJDA().getSelfUser().getAvatarUrl()).setTitle(STATIC.getTranslation2(guild, Translation.PATCHNOTES_LATEST_TITLE));
				final var result = STATIC.writeToRemoteChannel(guild, messageBuild, STATIC.getTranslation2(guild, Translation.PATCHNOTES_VERSION)+"**"+STATIC.getVersion()+"** "+priv_notes.getDate()+"\n"+priv_notes.getMessage1(), Channel.LOG.getType());
				if(result) {
					if(priv_notes.getMessage2() != null && priv_notes.getMessage2().length() > 0)
						STATIC.writeToRemoteChannel(guild, messageBuild, priv_notes.getMessage2(), Channel.LOG.getType());
					published = true;
				}
			}
			//retrieve public patch notes
			if(publ_notes != null && botConfig.getPublicPatchnotes()) {
				EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.MAGENTA).setThumbnail(e.getJDA().getSelfUser().getAvatarUrl()).setTitle(STATIC.getTranslation2(guild, Translation.PATCHNOTES_LATEST_TITLE));
				final var result = STATIC.writeToRemoteChannel(guild, messageBuild, STATIC.getTranslation2(guild, Translation.PATCHNOTES_VERSION)+"**"+STATIC.getVersion()+"** "+publ_notes.getDate()+"\n"+publ_notes.getMessage1(), Channel.BOT.getType());
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
			var doubleExp = botConfig.getDoubleExperience();
			if(!doubleExp.equals("auto"))
				Hashes.addTempCache("doubleExp_gu"+guild.getId(), new Cache(0, doubleExp));
			
			//run scheduled messages timers
			ScheduleExecution.startTimers(guild);
			
			//initialize Message pool cache and load saved messages, if available
			Hashes.initializeGuildMessagePool(guild.getIdLong(), 10000);
			if(botConfig.getCacheLog()) {
				final String messagePoolFile = "message_pool"+guild.getId()+".azr";
				if(new File(Directory.CACHE.getPath()+messagePoolFile).exists()) {
					JSONObject json = null;
					try {
						json = new JSONObject(FileHandler.readFile(Directory.CACHE, "message_pool"+guild.getId()+".azr"));
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
										final boolean isUserBot = ((JSONObject) item).getBoolean("isUserBot");
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
												currentMessage.setIsUserBot(isUserBot);
												savedMessages.add(currentMessage);
											}
										});
										message_pool.put(message_id, savedMessages);
									}
								});
							}
						});
						Hashes.setWholeMessagePool(guild.getIdLong(), message_pool);
						FileHandler.deleteFile(Directory.CACHE, "message_pool"+guild.getId()+".azr");
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
			BotConfigs botConfig = BotConfiguration.SQLgetBotConfigs(g.getIdLong());
			//TODO: translate message
			if(botConfig.getNotifications())
				STATIC.writeToRemoteChannel(g, null, "Bot is now operational!", Channel.LOG.getType());
			executor.execute(new RoleExtend(g));
			Azrael.SQLBulkInsertCategories(g.getCategories());
			Azrael.SQLBulkInsertChannels(g.getTextChannels());
			
			//turn on double experience, if it's enabled and is within the day range
			if(botConfig.getDoubleExperience().equals("auto")) {
				Calendar calendar = Calendar.getInstance();
				final int day = calendar.get(Calendar.DAY_OF_WEEK);
				if(day >= botConfig.getDoubleExperienceStart() && (day%7) <= botConfig.getDoubleExperienceEnd())
					Hashes.addTempCache("doubleExp_gu"+g.getId(), new Cache("on"));
			}
		});
		
		//if double experience is enabled, run 2 tasks for the start time and end time
		DoubleExperienceStart.runTask(e, null, null, null);
		
		ClearHashes.runTask();
		//check later on if there are any residual muted users which were not unmuted
		VerifyMutedMembers.runTask(e, null, null, true);
		
		executor.shutdown();
		
		new Thread(new Webserver(e)).start();
	}
}
