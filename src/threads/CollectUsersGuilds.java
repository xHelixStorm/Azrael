package threads;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import core.UserPrivs;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import sql.RankingSystem;
import sql.Azrael;

public class CollectUsersGuilds implements Runnable{
	private final Logger logger = LoggerFactory.getLogger(CollectUsersGuilds.class);
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
			List<Member> users = g.getMembers().parallelStream().filter(m -> !UserPrivs.isUserCommunity(m) && !UserPrivs.isUserMuted(m) && !UserPrivs.isUserBot(m)).collect(Collectors.toList());
			if(users.size() > 0) {
				Azrael.SQLBulkInsertUsers(users);
				Guilds guild_settings = RankingSystem.SQLgetGuild(guild_id);
				if(guild_settings != null && guild_settings.getRankingState()) {
					RankingSystem.SQLBulkInsertUsers(users, guild_settings.getLevelID(), guild_settings.getRankID(), guild_settings.getProfileID(), guild_settings.getIconID());
					RankingSystem.SQLBulkInsertUserDetails(users, 0, 0, 50000, 0);
				}
			}
			logger.debug("Start up user registration complete in {}", g.getId());
		}
	}
}
