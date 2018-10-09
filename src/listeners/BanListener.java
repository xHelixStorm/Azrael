package listeners;

import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.SqlConnect;

public class BanListener extends ListenerAdapter{
	
	@Override
	public void onGuildBan(GuildBanEvent e){
		
		long user_id = e.getUser().getIdLong();
		long guild_id = e.getGuild().getIdLong();
		
		SqlConnect.SQLUpdateMuted(user_id, guild_id, true, false);
		SqlConnect.SQLInsertActionLog("MEMBER_BAN_ADD", user_id, guild_id, "User Banned");
		SqlConnect.clearAllVariables();
	}
}
