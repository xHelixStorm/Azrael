package listeners;

import java.awt.Color;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Guilds;
import core.Hashes;
import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import preparedMessages.PatchNotes;
import preparedMessages.PublicPatchNotes;
import rankingSystem.DoubleExperienceOff;
import rankingSystem.DoubleExperienceStart;
import sql.RankingSystem;
import sql.Azrael;
import sql.DiscordRoles;
import threads.BotStartAssign;
import threads.RoleExtend;
import timerTask.ClearHashes;
import timerTask.ParseRSS;
import util.STATIC;

public class ReadyListener extends ListenerAdapter{
	private static String privatePatchNotes = PatchNotes.patchNotes();
	private static String publicPatchNotes = PublicPatchNotes.publicPatchNotes();
	
	@Override
	public void onReady(ReadyEvent e){
		Logger logger = LoggerFactory.getLogger(ReadyListener.class);
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.MAGENTA).setThumbnail(e.getJDA().getSelfUser().getAvatarUrl()).setTitle("Here the latest patch notes!");
		System.out.println();
		System.out.println("Azrael Version: "+STATIC.getVersion_New()+"\nAll credits to xHelixStorm");
		
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

		FileSetting.createFile("./files/reboot.azr", "0");
		for(Guild g : e.getJDA().getGuilds()){
			long guild_id = g.getIdLong();
			var log_channel = Azrael.SQLgetChannelID(guild_id, "log");
			if(DiscordRoles.SQLgetRoles(guild_id).size() == 0) {
				logger.error("Roles information from DiscordRoles.roles couldn't be retrieved and cached");
				if(log_channel != 0)e.getJDA().getGuildById(guild_id).getTextChannelById(log_channel).sendMessage("An internal error occurred. Roles information from DiscordRoles.roles couldn't be called and cached").queue();
			}
			if(RankingSystem.SQLgetGuild(guild_id) == null) {
				logger.error("Guild information from RankingSystem.guilds couldn't be retrieved and cached");
				if(log_channel != 0)e.getJDA().getGuildById(guild_id).getTextChannelById(log_channel).sendMessage("An internal error occurred. Guild information from RankingSystem.guilds couldn't be called and cached").queue();
			}
			Guilds guild_settings = Hashes.getStatus(guild_id);
			if(guild_settings != null && guild_settings.getRankingState() && RankingSystem.SQLgetRoles(guild_id) == false) {
				logger.error("Roles from RankingSystem.roles couldn't be called and cached");
				if(log_channel != 0)e.getJDA().getGuildById(guild_id).getTextChannelById(log_channel).sendMessage("An internal error occurred. Roles from RankingSystem.roles couldn't be called and cached").queue();
			}
			if(guild_settings != null && guild_settings.getRankingState() && RankingSystem.SQLgetLevels(guild_id) == 0) {
				logger.error("Levels from RankingSystem.level_list couldn't be called and cached");
				if(log_channel != 0)e.getJDA().getGuildById(guild_id).getTextChannelById(log_channel).sendMessage("An internal error occurred. Levels from RankingSystem.level_list couldn't be called and cached").queue();
			}
			Azrael.SQLgetRSSFeeds(guild_id);
			ParseRSS.runTask(e, guild_id, Azrael.SQLgetChannelID(guild_id, "rss"));
			if(log_channel != 0){e.getJDA().getGuildById(guild_id).getTextChannelById(log_channel).sendMessage("Bot is now operational!").queue();}
		}
		Azrael.SQLInsertActionLog("BOT_BOOT", e.getJDA().getSelfUser().getIdLong(), 0, "Launched");
		
		if(!(STATIC.getVersion_Old().contains(STATIC.getVersion_New())) && allowPatchNotes){
			for(Guild g : e.getJDA().getGuilds()){
				long guild_id = g.getIdLong();
				long channel_id = Azrael.SQLgetChannelID(guild_id, "log");
				
				if(channel_id != 0){
					FileSetting.createFile("./files/version.azr", STATIC.getVersion_New());
					e.getJDA().getGuildById(guild_id).getTextChannelById(channel_id).sendMessage(
							messageBuild.setDescription(privatePatchNotes).build()).queue();
					logger.debug("Private patch notes launched");
					
					long channel_id2 = Azrael.SQLgetChannelID(guild_id, "bot");
					
					if(allowPublicPatchNotes){
						if(channel_id2 != 0){
							e.getJDA().getGuildById(guild_id).getTextChannelById(channel_id2).sendMessage(
									messageBuild.setDescription(publicPatchNotes).build()).queue();
							logger.debug("Public patch notes launched");
						}
					}
				}
			}
		}
		
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
		
		executor.shutdown();
	}
}
