package listeners;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.Azrael;

public class BanListener extends ListenerAdapter{
	
	@Override
	public void onGuildBan(GuildBanEvent e){
		Logger logger = LoggerFactory.getLogger(BanListener.class);
		
		long user_id = e.getUser().getIdLong();
		long guild_id = e.getGuild().getIdLong();
		
		var user = Azrael.SQLgetData(user_id, guild_id);
		var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type().equals("log")).findAny().orElse(null);
		
		if(user.getBanID() == 1 && user.getWarningID() > 0) {
			if(Azrael.SQLUpdateBan(user_id, guild_id, 2) == 0) {
				logger.error("banned user {} couldn't be marked as banned on Azrael.bancollect for guild {}", e.getUser().getId(), e.getGuild().getName());
				if(log_channel != null)e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. User couldn't be marked as banned in Azrael.bancollect").queue();
			}
		}
		else{
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			if(Azrael.SQLInsertData(user_id, guild_id, 0, 2, timestamp, timestamp, true, false) == 0) {
				logger.error("muted user {} couldn't be inserted into Azrael.bancollect for guild {}", e.getUser().getId(), e.getGuild().getName());
				if(log_channel != null)e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. Muted user couldn't be inserted into Azrael.bancollect").queue();
			}
		}
		logger.debug("{} has been banned from {}", e.getUser().getId(), e.getGuild().getName());
		Azrael.SQLInsertActionLog("MEMBER_BAN_ADD", user_id, guild_id, "User Banned");
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {}
	}
}
