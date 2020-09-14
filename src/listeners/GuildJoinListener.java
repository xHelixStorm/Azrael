package listeners;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.RankingSystem;
import threads.CollectUsersGuilds;
import timerTask.ParseSubscription;
import sql.DiscordRoles;
import sql.Patchnotes;
import sql.Azrael;

/**
 * This class gets executed when the bot joins a new server.
 * 
 * On execution, the guild details will be inserted into all
 * tables and a guild ini file will be created if not available.
 * @author xHelixStorm
 *
 */

public class GuildJoinListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(GuildJoinListener.class);
	
	@Override
	public void onGuildJoin(GuildJoinEvent e){
		logger.debug("Bot joined a new guild: {}", e.getGuild().getName());
		
		long guild_id = e.getGuild().getIdLong();
		String guild_name = e.getGuild().getName();
		
		//insert into Azrael table
		if(Azrael.SQLInsertGuild(guild_id, guild_name) == 0) {
			logger.error("guild information couldn't be inserted into Azrael.guilds table for the guild {}", guild_id);
		}
		//insert into DiscordRoles table
		if(DiscordRoles.SQLInsertGuild(guild_id, guild_name) == 0) {
			logger.error("guild information couldn't be inserted into DiscordRoles.guilds table for the guild {}", guild_id);
		}
		//insert into RankingSystem table
		if(RankingSystem.SQLInsertGuild(guild_id, guild_name, false) == 0) {
			logger.error("guild settings couldn't be inserted into RankingSystem.guilds table for the guild {}", guild_id);
		}
		//insert into Patchnotes table
		if(Patchnotes.SQLInsertGuild(guild_id, guild_name) == 0) {
			logger.error("guild information couldn't be inserted into DiscordRoles.guilds table for the guild {}", guild_id);
		}
		//insert all categories into table
		Azrael.SQLBulkInsertCategories(e.getGuild().getCategories());
		//insert all channels into table
		Azrael.SQLBulkInsertChannels(e.getGuild().getTextChannels());
		
		FileSetting.createGuildDirectory(e.getGuild());
		//check if guild ini file exists, else create a new one or verify content
		if(!new File("./ini/"+guild_id+".ini").exists())
			GuildIni.createIni(guild_id);
		else
			GuildIni.verifyIni(guild_id);
		
		//run server specific timers
		ParseSubscription.runTask(e.getJDA(), guild_id);
		
		//set the default language for this server
		Hashes.setLanguage(guild_id, "en");
		
		//initialize message pool
		Hashes.initializeGuildMessagePool(guild_id, 1000);
		
		//collect all users in the server
		new Thread(new CollectUsersGuilds(null, e)).start();
		Azrael.SQLInsertActionLog("GUILD_JOIN", e.getGuild().getIdLong(), e.getGuild().getIdLong(), guild_name);
	}
}
