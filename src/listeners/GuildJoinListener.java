package listeners;

import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.RankingSystem;
import sql.DiscordRoles;
import sql.Azrael;

public class GuildJoinListener extends ListenerAdapter{
	
	@Override
	public void onGuildJoin(GuildJoinEvent e){
		long guild_id = e.getGuild().getIdLong();
		String guild_name = e.getGuild().getName();
		
		Azrael.SQLInsertGuild(guild_id, guild_name);
		RankingSystem.SQLInsertGuild(guild_id, guild_name, 1, 1, 1, 1, 0, false);
		DiscordRoles.SQLInsertGuild(guild_id, guild_name);
		Azrael.SQLInsertActionLog("GUILD_JOIN", e.getGuild().getIdLong(), e.getGuild().getIdLong(), e.getGuild().getName());
	}
}
