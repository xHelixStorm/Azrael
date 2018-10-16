package listeners;

import java.sql.Timestamp;

import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.SqlConnect;

public class BanListener extends ListenerAdapter{
	
	@Override
	public void onGuildBan(GuildBanEvent e){
		
		long user_id = e.getUser().getIdLong();
		long guild_id = e.getGuild().getIdLong();
		
		SqlConnect.SQLgetData(user_id, guild_id);
		int ban_id = SqlConnect.getBanID();
		
		if(ban_id == 1){
			SqlConnect.SQLUpdateBan(user_id, guild_id, 2);
		}
		else{
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			SqlConnect.SQLInsertData(user_id, guild_id, 1, 2, timestamp, timestamp, true, false);
		}
		
		SqlConnect.SQLUpdateMuted(user_id, guild_id, true, false);
		SqlConnect.SQLInsertActionLog("MEMBER_BAN_ADD", user_id, guild_id, "User Banned");
		SqlConnect.clearAllVariables();
	}
}
