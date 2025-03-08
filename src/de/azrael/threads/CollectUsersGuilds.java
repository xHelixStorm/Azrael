package de.azrael.threads;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Guilds;
import de.azrael.sql.Azrael;
import de.azrael.sql.RankingSystem;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class CollectUsersGuilds implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(CollectUsersGuilds.class);
	
	JDA jda;
	
	public CollectUsersGuilds(JDA _jda) {
		jda = _jda;
	}
	
	@Override
	public void run() {
		for(Guild g : jda.getGuilds()) {
			long guild_id = g.getIdLong();
			List<Member> users = g.loadMembers().get();
			if(users.size() > 0) {
				Azrael.SQLBulkInsertUsers(users);
				Azrael.SQLBulkInsertJoinDates(users);
				Guilds guild_settings = RankingSystem.SQLgetGuild(guild_id);
				if(guild_settings != null && guild_settings.getRankingState()) {
					RankingSystem.SQLBulkInsertUsers(users, guild_settings.getLevelID(), guild_settings.getRankID(), guild_settings.getProfileID(), guild_settings.getIconID());
					RankingSystem.SQLBulkInsertUserDetails(users, 0, 0, guild_settings.getStartCurrency(), 0);
				}
			}
			logger.info("User registration complete in guild {}", g.getId());
		}
	}
}
