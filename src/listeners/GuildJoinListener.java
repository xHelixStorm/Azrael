package listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.RankingSystem;
import sql.DiscordRoles;
import sql.Azrael;

public class GuildJoinListener extends ListenerAdapter{
	
	@Override
	public void onGuildJoin(GuildJoinEvent e){
		Logger logger = LoggerFactory.getLogger(GuildJoinListener.class);
		logger.debug("Bot joined a new guild: {}", e.getGuild().getName());
		
		long guild_id = e.getGuild().getIdLong();
		String guild_name = e.getGuild().getName();
		
		if(Azrael.SQLInsertGuild(guild_id, guild_name) == 0) {
			logger.error("guild information couldn't be inserted into Azrael.guilds table for the guild {}", e.getGuild().getName());
		}
		if(RankingSystem.SQLInsertGuild(guild_id, guild_name, 1, 1, 1, 1, 0, false) > 0) {
			if(DiscordRoles.SQLInsertGuild(guild_id, guild_name) == 0) {
				logger.error("guild information couldn't be inserted into DiscordRoles.guilds table for the guild {}", e.getGuild().getName());
			}
		}
		else {
			logger.error("guild settings couldn't be inserted into RankingSystem.guilds table for the guild {}", e.getGuild().getName());
		}
		Azrael.SQLInsertActionLog("GUILD_JOIN", e.getGuild().getIdLong(), e.getGuild().getIdLong(), e.getGuild().getName());
	}
}
