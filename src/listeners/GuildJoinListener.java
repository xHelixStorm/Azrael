package listeners;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.RankingSystem;
import threads.BotStartAssign;
import sql.DiscordRoles;
import sql.Patchnotes;
import sql.Azrael;

public class GuildJoinListener extends ListenerAdapter{
	
	@Override
	public void onGuildJoin(GuildJoinEvent e){
		Logger logger = LoggerFactory.getLogger(GuildJoinListener.class);
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
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(new BotStartAssign(null, e));
		executor.shutdown();
		Azrael.SQLInsertActionLog("GUILD_JOIN", e.getGuild().getIdLong(), e.getGuild().getIdLong(), guild_name);
	}
}
