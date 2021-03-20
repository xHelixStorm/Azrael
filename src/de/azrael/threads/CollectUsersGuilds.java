package de.azrael.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Guilds;
import de.azrael.core.UserPrivs;
import de.azrael.sql.Azrael;
import de.azrael.sql.RankingSystem;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;

public class CollectUsersGuilds implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(CollectUsersGuilds.class);
	private static final List<Long> checkedGuilds = new ArrayList<Long>();
	
	ReadyEvent e;
	GuildJoinEvent e2;
	
	public CollectUsersGuilds(ReadyEvent _e, GuildJoinEvent _e2) {
		e = _e;
		e2 = _e2;
	}
	
	@Override
	public void run() {
		for(Guild g : (e != null ? e.getJDA().getGuilds() : e2.getJDA().getGuilds())) {
			long guild_id = g.getIdLong();
			if(!checkedGuilds.contains(guild_id)) {
				List<Member> users = g.loadMembers().get().parallelStream().filter(m -> !UserPrivs.isUserCommunity(m) && !UserPrivs.isUserMuted(m)).collect(Collectors.toList());
				if(users.size() > 0) {
					Azrael.SQLBulkInsertUsers(users);
					Azrael.SQLBulkInsertJoinDates(users);
					Guilds guild_settings = RankingSystem.SQLgetGuild(guild_id);
					if(guild_settings != null && guild_settings.getRankingState()) {
						RankingSystem.SQLBulkInsertUsers(users, guild_settings.getLevelID(), guild_settings.getRankID(), guild_settings.getProfileID(), guild_settings.getIconID());
						RankingSystem.SQLBulkInsertUserDetails(users, 0, 0, guild_settings.getStartCurrency(), 0);
					}
				}
				checkedGuilds.add(guild_id);
				logger.info("Start up user registration complete in guild {}", g.getId());
			}
		}
	}
}
