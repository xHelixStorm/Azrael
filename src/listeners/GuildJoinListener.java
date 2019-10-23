package listeners;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fileManagement.GuildIni;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.RankingSystem;
import threads.CollectUsersGuilds;
import sql.DiscordRoles;
import sql.Patchnotes;
import sql.Azrael;

public class GuildJoinListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(GuildJoinListener.class);
	
	@Override
	public void onGuildJoin(GuildJoinEvent e){
		logger.debug("Bot joined a new guild: {}", e.getGuild().getName());
		
		long guild_id = e.getGuild().getIdLong();
		String guild_name = e.getGuild().getName();
		
		if(Azrael.SQLInsertGuild(guild_id, guild_name) == 0) {
			logger.error("guild information couldn't be inserted into Azrael.guilds table for the guild {}", guild_name);
		}
		if(RankingSystem.SQLInsertGuild(guild_id, guild_name, false) > 0) {
			if(DiscordRoles.SQLInsertGuild(guild_id, guild_name) == 0) {
				logger.error("guild information couldn't be inserted into DiscordRoles.guilds table for the guild {}", guild_name);
			}
		}
		else {
			logger.error("guild settings couldn't be inserted into RankingSystem.guilds table for the guild {}", guild_name);
		}
		if(Patchnotes.SQLInsertGuild(guild_id, guild_name) == 0) {
			logger.error("guild information couldn't be inserted into DiscordRoles.guilds table for the guild {}", guild_name);
		}
		if(!new File("./ini/"+guild_id+".ini").exists())
			GuildIni.createIni(e.getGuild().getIdLong());
		new Thread(new CollectUsersGuilds(null, e)).start();
		Azrael.SQLInsertActionLog("GUILD_JOIN", e.getGuild().getIdLong(), e.getGuild().getIdLong(), guild_name);
	}
}
