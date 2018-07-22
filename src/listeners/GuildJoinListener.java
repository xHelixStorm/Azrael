package listeners;

import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.RankingDB;
import sql.ServerRoles;
import sql.SqlConnect;

public class GuildJoinListener extends ListenerAdapter{
	
	@Override
	public void onGuildJoin(GuildJoinEvent e){
		long guild_id = e.getGuild().getIdLong();
		String guild_name = e.getGuild().getName();
		
		SqlConnect.SQLInsertGuild(guild_id, guild_name);
		RankingDB.SQLInsertGuild(guild_id, guild_name, 1, 1, 1, 1, 0, false);
		ServerRoles.SQLInsertGuild(guild_id, guild_name);
		SqlConnect.SQLInsertActionLog("GUILD_JOIN", e.getGuild().getIdLong(), e.getGuild().getName());
	}
}
