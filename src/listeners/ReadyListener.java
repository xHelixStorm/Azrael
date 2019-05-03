package listeners;

import java.awt.Color;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Channels;
import core.Guilds;
import core.Patchnote;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
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
import util.STATIC;

public class ReadyListener extends ListenerAdapter {
	
	@Override
	public void onReady(ReadyEvent e) {
		if(FileSetting.readFile("./files/running.azr").contains("1")) {
			FileSetting.createFile("./files/running.azr", "2");
			e.getJDA().shutdownNow();
			return;
		}
		FileSetting.createFile("./files/running.azr", "1");
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
		FileSetting.createTemp();

		var themesRetrieved = true;
		if(RankingSystem.SQLgetThemes() == false) {
			themesRetrieved = false;
			logger.error("Themes couldn't be retried from RankingSystem.themes");
		}
		for(Guild g : e.getJDA().getGuilds()){
			long guild_id = g.getIdLong();
			if(!new File("./ini/"+guild_id+".ini").exists()) {
				GuildIni.createIni(guild_id);
			}
			Channels log_channel = null;
			Channels bot_channel = null;
			var channels = Azrael.SQLgetChannels(guild_id);
			if(channels == null) {
				logger.error("Channel information from Azrael.channels couldn't be retrieved and cached");
			}
			else {
				log_channel = channels.parallelStream().filter(f -> f.getChannel_Type().equals("log")).findAny().orElse(null);
				bot_channel = channels.parallelStream().filter(f -> f.getChannel_Type().equals("bot")).findAny().orElse(null);
			}
			if(DiscordRoles.SQLgetRoles(guild_id).size() == 0) {
				logger.error("Roles information from DiscordRoles.roles couldn't be retrieved and cached");
				if(log_channel != null)e.getJDA().getGuildById(guild_id).getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. Roles information from DiscordRoles.roles couldn't be called and cached").queue();
			}
			Guilds guild_settings = RankingSystem.SQLgetGuild(guild_id);
			if(guild_settings == null) {
				logger.error("Guild information from RankingSystem.guilds couldn't be retrieved and cached");
				if(log_channel != null)e.getJDA().getGuildById(guild_id).getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. Guild information from RankingSystem.guilds couldn't be called and cached").queue();
			}
			if(guild_settings != null && guild_settings.getRankingState() && RankingSystem.SQLgetRoles(guild_id) == false) {
				logger.error("Roles from RankingSystem.roles couldn't be called and cached");
				if(log_channel != null)e.getJDA().getGuildById(guild_id).getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. Roles from RankingSystem.roles couldn't be called and cached").queue();
			}
			if(guild_settings != null && guild_settings.getRankingState() && RankingSystem.SQLgetLevels(guild_id, guild_settings.getThemeID()) == 0) {
				logger.error("Levels from RankingSystem.level_list couldn't be called and cached");
				if(log_channel != null)e.getJDA().getGuildById(guild_id).getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. Levels from RankingSystem.level_list couldn't be called and cached").queue();
			}
			if(themesRetrieved == false) {
				if(log_channel != null)e.getJDA().getGuildById(guild_id).getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. Themes from RankingSystem.themes couldn't be called and cached").queue();
			}
			Azrael.SQLgetRSSFeeds(guild_id);
			ParseRSS.runTask(e, guild_id);
			
			if(log_channel != null){e.getJDA().getGuildById(guild_id).getTextChannelById(log_channel.getChannel_ID()).sendMessage("Bot is now operational!").queue();}
			
			Patchnote priv_notes = null;
			Patchnote publ_notes = null;
			if(!Patchnotes.SQLcheckPublishedPatchnotes(guild_id)) {
				priv_notes = Patchnotes.SQLgetPrivatePatchnotes();
				publ_notes = Patchnotes.SQLgetPublicPatchnotes();
			}
			
			var published = false;
			if(priv_notes != null && allowPatchNotes) {
				if(log_channel != null) {
					e.getJDA().getGuildById(guild_id).getTextChannelById(log_channel.getChannel_ID()).sendMessage(
							messageBuild.setDescription("Bot patch notes version **"+STATIC.getVersion()+"** "+priv_notes.getDate()+"\n"+priv_notes.getMessage1())
							.build()).complete();
					if(priv_notes.getMessage2() != null && priv_notes.getMessage2().length() > 0) {
						e.getJDA().getGuildById(guild_id).getTextChannelById(log_channel.getChannel_ID()).sendMessage(
								messageBuild.setDescription(priv_notes.getMessage2())
								.build()).complete();
					}
					published = true;
				}
			}
			if(publ_notes != null && allowPublicPatchNotes) {
				if(bot_channel != null) {
					e.getJDA().getGuildById(guild_id).getTextChannelById(bot_channel.getChannel_ID()).sendMessage(
							messageBuild.setDescription("Bot patch notes version **"+STATIC.getVersion()+"** "+publ_notes.getDate()+"\n"+publ_notes.getMessage1())
							.build()).complete();
					if(publ_notes.getMessage2() != null && publ_notes.getMessage2().length() > 0) {
						e.getJDA().getGuildById(guild_id).getTextChannelById(bot_channel.getChannel_ID()).sendMessage(
								messageBuild.setDescription(publ_notes.getMessage2())
								.build()).complete();
					}
					published = true;
				}
			}
			if(published) {
				Patchnotes.SQLInsertPublishedPatchnotes(guild_id);
			}
		}
		Azrael.SQLInsertActionLog("BOT_BOOT", e.getJDA().getSelfUser().getIdLong(), 0, "Launched");
		
		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.execute(new BotStartAssign(e));
		for(Guild g : e.getJDA().getGuilds()){
			executor.execute(new RoleExtend(e, g.getIdLong()));
			for(TextChannel tc : g.getTextChannels()){
				if(Azrael.SQLInsertChannels(tc.getIdLong(), tc.getName()) == 0) {
					logger.error("channel {} couldn't be registered", tc.getId());
				}
			}
		}
		
		DoubleExperienceStart.runTask(e);
		DoubleExperienceOff.runTask();
		ClearHashes.runTask();
		
		var timeout = IniFileReader.getMessageTimeout();
		if(timeout != 0)
			ClearCommentedUser.runTask(timeout);
		
		executor.shutdown();
	}
}
