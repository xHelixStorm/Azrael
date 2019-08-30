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
import enums.Weekday;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rankingSystem.DoubleExperienceOff;
import rankingSystem.DoubleExperienceStart;
import sql.RankingSystem;
import sql.Azrael;
import sql.DiscordRoles;
import sql.Patchnotes;
import threads.BotStartAssign;
import threads.RoleExtend;
import timerTask.ClearCommentedUser;
import timerTask.ClearHashes;
import timerTask.ParseRSS;
import timerTask.VerifyMutedMembers;
import util.STATIC;

public class ReadyListener extends ListenerAdapter {
	
	@Override
	public void onReady(ReadyEvent e) {
		FileSetting.createTemp(e);
		if(new File(IniFileReader.getTempDirectory()+"running.azr").exists() && FileSetting.readFile(IniFileReader.getTempDirectory()+"running.azr").contains("1")) {
			FileSetting.createFile(IniFileReader.getTempDirectory()+"running.azr", "2");
			e.getJDA().shutdownNow();
			return;
		}
		FileSetting.createFile(IniFileReader.getTempDirectory()+"running.azr", "1");
		
		Logger logger = LoggerFactory.getLogger(ReadyListener.class);
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.MAGENTA).setThumbnail(e.getJDA().getSelfUser().getAvatarUrl()).setTitle("Here the latest patch notes!");
		System.out.println();
		System.out.println("Azrael Version: "+STATIC.getVersion()+"\nAll credits to xHelixStorm");
		
		boolean allowPatchNotes = IniFileReader.getAllowPatchNotes();
		boolean allowPublicPatchNotes = IniFileReader.getAllowPublicPatchNotes();
		
		System.out.println();
		
		if(allowPatchNotes){System.out.println("private patch notes: enabled");}
		else{System.out.println("private patch notes: disabled");}
		if(allowPublicPatchNotes){System.out.println("public patch notes:  enabled");}
		else{System.out.println("public patch notes:  disabled");}
		
		String out = "\nThis Bot is running on following servers: \n";
		for(Guild g : e.getJDA().getGuilds()){
			out += g.getName() + " (" + g.getId() + ") \n";
		}
		System.out.println(out);

		var themesRetrieved = true;
		if(RankingSystem.SQLgetThemes() == false) {
			themesRetrieved = false;
			logger.error("Themes couldn't be retried from RankingSystem.themes");
		}
		for(Guild guild : e.getJDA().getGuilds()) {
			if(!new File("ini/"+guild.getId()+".ini").exists()) {
				GuildIni.createIni(guild.getIdLong());
			}
			if(Azrael.SQLgetGuild(guild.getIdLong()) == 0) {
				if(Azrael.SQLInsertGuild(guild.getIdLong(), guild.getName()) == 0) {
					logger.error("Azrael.guild is empty and couldn't be filled!");
				}
			}
			if(DiscordRoles.SQLgetGuild(guild.getIdLong()) == 0) {
				if(DiscordRoles.SQLInsertGuild(guild.getIdLong(), guild.getName()) == 0) {
					logger.error("DiscordRoles.guilds is empty and couldn't be filled!");
				}
			}
			if(RankingSystem.SQLgetGuild(guild.getIdLong()) == null) {
				if(RankingSystem.SQLInsertGuild(guild.getIdLong(), guild.getName(), false) == 0) {
					logger.error("RankingSystem.guild is empty and couldn't be filled!");
				}
			}
			if(Patchnotes.SQLgetGuild(guild.getIdLong()) == 0) {
				if(Patchnotes.SQLInsertGuild(guild.getIdLong(), guild.getName()) == 0) {
					logger.error("Patchnotes.guilds is empty and couldn't be filled!");
				}
			}
			Channels log_channel = null;
			Channels bot_channel = null;
			var channels = Azrael.SQLgetChannels(guild.getIdLong());
			if(channels == null || channels.size() == 0) {
				logger.error("Channel information from Azrael.channels couldn't be retrieved and cached");
			}
			else {
				log_channel = channels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
				bot_channel = channels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("bot")).findAny().orElse(null);
			}
			if(DiscordRoles.SQLgetRoles(guild.getIdLong()).size() == 0) {
				var result = DiscordRoles.SQLInsertRoles(guild.getIdLong(), guild.getRoles());
				if(result != null && result.length > 0 && result[0] == 1) {
					logger.debug("Roles information from DiscordRoles.roles couldn't be retrieved and cached for guild {}. Hence all available roles have been inserted as default roles.", guild.getName());
					if(log_channel != null) guild.getTextChannelById(log_channel.getChannel_ID()).sendMessage("Roles information from DiscordRoles.roles couldn't be called and cached. Hence all roles have been inserted under the default role!").queue();
					DiscordRoles.SQLgetRoles(guild.getIdLong());
				}
				else {
					logger.error("Roles for DiscordRoles.roles couldn't be inserted for guild {}", guild.getName());
					if(log_channel != null) guild.getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occured. Roles for DiscordRoles.roles couldn't be inserted!").queue();
				}
			}
			Guilds guild_settings = RankingSystem.SQLgetGuild(guild.getIdLong());
			if(guild_settings == null) {
				logger.error("Guild information from RankingSystem.guilds couldn't be retrieved and cached");
				if(log_channel != null)e.getJDA().getGuildById(guild.getId()).getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. Guild information from RankingSystem.guilds couldn't be called and cached").queue();
			}
			if(guild_settings != null && guild_settings.getRankingState() && RankingSystem.SQLgetRoles(guild.getIdLong()) == null) {
				logger.error("Roles from RankingSystem.roles couldn't be called and cached");
				if(log_channel != null)e.getJDA().getGuildById(guild.getId()).getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. Roles from RankingSystem.roles couldn't be called and cached").queue();
			}
			if(guild_settings != null && guild_settings.getRankingState() && RankingSystem.SQLgetLevels(guild.getIdLong(), guild_settings.getThemeID()) == 0) {
				logger.error("Levels from RankingSystem.level_list couldn't be called and cached");
				if(log_channel != null)e.getJDA().getGuildById(guild.getId()).getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. Levels from RankingSystem.level_list couldn't be called and cached").queue();
			}
			if(themesRetrieved == false) {
				if(log_channel != null)e.getJDA().getGuildById(guild.getId()).getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. Themes from RankingSystem.themes couldn't be called and cached").queue();
			}
			Azrael.SQLgetRSSFeeds(guild.getIdLong());
			ParseRSS.runTask(e, guild.getIdLong());
			
			if(log_channel != null){e.getJDA().getGuildById(guild.getId()).getTextChannelById(log_channel.getChannel_ID()).sendMessage("Bot is now operational!").queue();}
			
			Patchnote priv_notes = null;
			Patchnote publ_notes = null;
			if(!Patchnotes.SQLcheckPublishedPatchnotes(guild.getIdLong())) {
				priv_notes = Patchnotes.SQLgetPrivatePatchnotes();
				publ_notes = Patchnotes.SQLgetPublicPatchnotes();
			}
			
			var published = false;
			if(priv_notes != null && allowPatchNotes) {
				if(log_channel != null) {
					e.getJDA().getGuildById(guild.getId()).getTextChannelById(log_channel.getChannel_ID()).sendMessage(
							messageBuild.setDescription("Bot patch notes version **"+STATIC.getVersion()+"** "+priv_notes.getDate()+"\n"+priv_notes.getMessage1())
							.build()).complete();
					if(priv_notes.getMessage2() != null && priv_notes.getMessage2().length() > 0) {
						e.getJDA().getGuildById(guild.getId()).getTextChannelById(log_channel.getChannel_ID()).sendMessage(
								messageBuild.setDescription(priv_notes.getMessage2())
								.build()).complete();
					}
					published = true;
				}
			}
			if(publ_notes != null && allowPublicPatchNotes) {
				if(bot_channel != null) {
					e.getJDA().getGuildById(guild.getId()).getTextChannelById(bot_channel.getChannel_ID()).sendMessage(
							messageBuild.setDescription("Bot patch notes version **"+STATIC.getVersion()+"** "+publ_notes.getDate()+"\n"+publ_notes.getMessage1())
							.build()).complete();
					if(publ_notes.getMessage2() != null && publ_notes.getMessage2().length() > 0) {
						e.getJDA().getGuildById(guild.getId()).getTextChannelById(bot_channel.getChannel_ID()).sendMessage(
								messageBuild.setDescription(publ_notes.getMessage2())
								.build()).complete();
					}
					published = true;
				}
			}
			if(published) {
				Patchnotes.SQLInsertPublishedPatchnotes(guild.getIdLong());
			}
			
			//check if the double exp should be enabled or disabled for the current guild
			var doubleExp = GuildIni.getDoubleExperienceMode(guild.getIdLong());
			if(!doubleExp.equals("auto"))
				Hashes.addTempCache("doubleExp_gu"+guild.getId(), new Cache(0, doubleExp));
		}
		Azrael.SQLInsertActionLog("BOT_BOOT", e.getJDA().getSelfUser().getIdLong(), 0, "Launched");
		
		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.execute(new BotStartAssign(e, null));
		for(Guild g : e.getJDA().getGuilds()){
			executor.execute(new RoleExtend(e, g.getIdLong()));
			for(TextChannel tc : g.getTextChannels()){
				if(Azrael.SQLInsertChannels(tc.getIdLong(), tc.getName()) == 0) {
					logger.error("channel {} couldn't be registered", tc.getId());
				}
			}
		}
		
		if(IniFileReader.getDoubleExpEnabled()) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.DAY_OF_WEEK, Weekday.getDay(IniFileReader.getDoubleExpStart()));
			Calendar calendar2 = Calendar.getInstance();
			calendar2.set(Calendar.DAY_OF_WEEK, Weekday.getDay(IniFileReader.getDoubleExpEnd()));
			calendar2.set(Calendar.HOUR_OF_DAY, 23);
			calendar2.set(Calendar.MINUTE, 59);
			var currentTime = System.currentTimeMillis();
			if(calendar.getTime().getTime() < currentTime && calendar2.getTime().getTime() > currentTime)
				Hashes.addTempCache("doubleExp", new Cache("on"));
			DoubleExperienceStart.runTask(e, null, null, null);
			DoubleExperienceOff.runTask();
		}
		ClearHashes.runTask();
		VerifyMutedMembers.delayFirstStart(e);
		
		var timeout = IniFileReader.getMessageTimeout();
		if(timeout != 0)
			ClearCommentedUser.runTask(timeout);
		
		executor.shutdown();
	}
}
