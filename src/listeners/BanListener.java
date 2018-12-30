package listeners;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.Azrael;

public class BanListener extends ListenerAdapter{
	
	@Override
	public void onGuildBan(GuildBanEvent e){
		
		long user_id = e.getUser().getIdLong();
		long guild_id = e.getGuild().getIdLong();
		
		Azrael.SQLgetData(user_id, guild_id);
		int ban_id = Azrael.getBanID();
		
		if(ban_id == 1){
			Azrael.SQLUpdateBan(user_id, guild_id, 2);
		}
		else{
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			Azrael.SQLInsertData(user_id, guild_id, 1, 2, timestamp, timestamp, true, false);
		}
		
		Azrael.SQLUpdateMuted(user_id, guild_id, true, false);
		Logger logger = LoggerFactory.getLogger(BanListener.class);
		logger.debug("{} has been banned from {}", e.getUser().getId(), e.getGuild().getName());
		Azrael.SQLInsertActionLog("MEMBER_BAN_ADD", user_id, guild_id, "User Banned");
		Azrael.clearAllVariables();
	}
}
