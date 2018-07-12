package listeners;

import java.awt.Color;
import java.sql.Timestamp;

import core.UserPrivs;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.SqlConnect;

public class RoleRemovedListener extends ListenerAdapter{
	
	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent e){
		EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE);
		String member_name = e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator();
		long user_id = e.getMember().getUser().getIdLong();
		long guild_id = e.getGuild().getIdLong();
		long log_channel_id;
		
		SqlConnect.SQLgetData(user_id, guild_id);
		
		if(!UserPrivs.isUserMuted(e.getUser(), guild_id) &&(SqlConnect.getUnmute().getTime() - System.currentTimeMillis()) > 0){
			if(SqlConnect.getUser_id() != 0){SqlConnect.SQLUpdateMuted(user_id, guild_id, false);}
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			
			SqlConnect.SQLgetChannelID(guild_id, "log");
			log_channel_id = SqlConnect.getChannelID();
			SqlConnect.clearUnmute();
			
			if(log_channel_id != 0){e.getGuild().getTextChannelById(log_channel_id).sendMessage(message.setDescription("["+timestamp.toString()+"] The mute role has been manually removed from **"+member_name+"** with the ID number **"+user_id+"**!").build()).queue();}
		}
		else if(!UserPrivs.isUserMuted(e.getUser(), guild_id) && SqlConnect.getUser_id() != 0){
			SqlConnect.SQLUpdateMuted(user_id, guild_id, false);
		}
		SqlConnect.clearAllVariables();
	}
}
