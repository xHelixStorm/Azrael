package listeners;

import java.awt.Color;
import java.io.File;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import constructors.Channels;
import constructors.Guilds;
import constructors.Patchnote;
import core.Hashes;
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
import timerTask.ClearCommentedUser;
import timerTask.ClearHashes;
import timerTask.ParseRSS;
import timerTask.VerifyMutedMembers;
import util.STATIC;

/**
 * First event that gets executed on start up and on successful login to Discord
 * 
 * collect themes, roles, settings, etc before the Bot is fully operational for taking
 * commands, restart timers for muted users and start timers for regular check ups and 
 * clean ups
 * @author xHelixStorm
 *
 */

public class ReadyListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(ReadyListener.class);
	
	@Override
	public void onReady(ReadyEvent e) {
		//create the temp directory and verify if multiple sessions are running. If yes, terminate this session
		FileSetting.createTemp(e);
		if(new File(IniFileReader.getTempDirectory()+STATIC.getSessionName()+"running.azr").exists() && FileSetting.readFile(IniFileReader.getTempDirectory()+STATIC.getSessionName()+"running.azr").contains("1")) {
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

		//retrieve all available ranking themes
		if(RankingSystem.SQLgetThemes() == false) {
			logger.error("Themes couldn't be retried from RankingSystem.themes");
		}
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
					logger.error("Azrael.guild is empty and couldn't be filled! for guild {}", guild.getId());
				}
			}
			//verify that the guild is registered in the discord roles database and do the same step
			if(DiscordRoles.SQLgetGuild(guild.getIdLong()) == 0) {
				if(DiscordRoles.SQLInsertGuild(guild.getIdLong(), guild.getName()) == 0) {
					logger.error("DiscordRoles.guilds is empty and couldn't be filled! for guild {}", guild.getId());
				}
			}
			//verify that the guild is registered in the ranking system database and do the same step
			if(RankingSystem.SQLgetGuild(guild.getIdLong()) == null) {
				if(RankingSystem.SQLInsertGuild(guild.getIdLong(), guild.getName(), false) == 0) {
					logger.error("RankingSystem.guild is empty and couldn't be filled! for guild {}", guild.getId());
				}
			}
			//verify that the guild is registered in the patch notes database and do the same step
			if(Patchnotes.SQLgetGuild(guild.getIdLong()) == 0) {
				if(Patchnotes.SQLInsertGuild(guild.getIdLong(), guild.getName()) == 0) {
					logger.error("Patchnotes.guilds is empty and couldn't be filled! for guild {}", guild.getId());
				}
			}
			Channels log_channel = null;
			Channels bot_channel = null;
			//Retrieve all registered channels and throw warning, if no channel has been registered. If found, check for the log and bot channel
			var channels = Azrael.SQLgetChannels(guild.getIdLong());
			if(channels == null) {
				logger.warn("Channel information from Azrael.channels couldn't be retrieved and cached for guild {}", guild.getId());
			}
			else if(channels.size() > 0) {
				log_channel = channels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
				bot_channel = channels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("bot")).findAny().orElse(null);
			}
			//retrieve all registered discord roles, if empty insert the roles into the database and throw an error if this fails as well
			if(DiscordRoles.SQLgetRoles(guild.getIdLong()).size() == 0) {
				var result = DiscordRoles.SQLInsertRoles(guild.getIdLong(), guild.getRoles());
				if(result != null && result.length > 0 && result[0] == 1) {
					logger.debug("Roles information from DiscordRoles.roles couldn't be retrieved and cached for guild {}. Hence all available roles have been inserted as default roles.", guild.getId());
					DiscordRoles.SQLgetRoles(guild.getIdLong());
				}
				else {
					logger.error("Roles for DiscordRoles.roles couldn't be inserted for guild {}", guild.getId());
				}
			}
			//retrieve all settings for the guild
			Guilds guild_settings = RankingSystem.SQLgetGuild(guild.getIdLong());
			if(guild_settings == null) {
				logger.error("Guild information from RankingSystem.guilds couldn't be retrieved and cached in guild {}", guild.getId());
			}
			//if the ranking system is enabled, retrieve all registered ranking roles
			if(guild_settings != null && guild_settings.getRankingState() && RankingSystem.SQLgetRoles(guild.getIdLong()).size() == 0) {
				logger.warn("Roles from RankingSystem.roles couldn't be called and cached in guild {}", guild.getId());
			}
			//retrieve all registered rss feeds and start the timer to make these display on the server
			Azrael.SQLgetRSSFeeds(guild.getIdLong());
			ParseRSS.runTask(e, guild.getIdLong());
			
			//print bot is now operational message for the current server
			if(log_channel != null){e.getJDA().getGuildById(guild.getId()).getTextChannelById(log_channel.getChannel_ID()).sendMessage("Bot is now operational!").queue();}
			
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
			if(priv_notes != null && GuildIni.getPrivatePatchNotes(guild.getIdLong())) {
				EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.MAGENTA).setThumbnail(e.getJDA().getSelfUser().getAvatarUrl()).setTitle(STATIC.getTranslation2(guild, Translation.PATCHNOTES_LATEST_TITLE));
				if(log_channel != null) {
					guild.getTextChannelById(log_channel.getChannel_ID()).sendMessage(
							messageBuild.setDescription("Bot patch notes version **"+STATIC.getVersion()+"** "+priv_notes.getDate()+"\n"+priv_notes.getMessage1())
							.build()).queue();
					if(priv_notes.getMessage2() != null && priv_notes.getMessage2().length() > 0) {
						guild.getTextChannelById(log_channel.getChannel_ID()).sendMessage(
								messageBuild.setDescription(priv_notes.getMessage2())
								.build()).queue();
					}
					published = true;
				}
			}
			//retrieve public patch notes
			if(publ_notes != null && GuildIni.getPublicPatchNotes(guild.getIdLong())) {
				EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.MAGENTA).setThumbnail(e.getJDA().getSelfUser().getAvatarUrl()).setTitle(STATIC.getTranslation2(guild, Translation.PATCHNOTES_LATEST_TITLE));
				if(bot_channel != null) {
					guild.getTextChannelById(bot_channel.getChannel_ID()).sendMessage(
							messageBuild.setDescription("Bot patch notes version **"+STATIC.getVersion()+"** "+publ_notes.getDate()+"\n"+publ_notes.getMessage1())
							.build()).queue();
					if(publ_notes.getMessage2() != null && publ_notes.getMessage2().length() > 0) {
						guild.getTextChannelById(bot_channel.getChannel_ID()).sendMessage(
								messageBuild.setDescription(publ_notes.getMessage2())
								.build()).queue();
					}
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
		}
		Azrael.SQLInsertActionLog("BOT_BOOT", e.getJDA().getSelfUser().getIdLong(), 0, "Launched");
		
		//execute background threads to collect current users, users under watch, text channels and muted users 
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(() -> { Azrael.SQLgetWholeWatchlist(); });
		executor.execute(new CollectUsersGuilds(e, null));
		e.getJDA().getGuilds().parallelStream().forEach(g -> {
			executor.execute(new RoleExtend(g));
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
	}
}
