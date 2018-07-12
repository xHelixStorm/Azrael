package listeners;

import java.awt.Color;
import java.sql.Timestamp;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.SqlConnect;

public class GuildLeaveListener extends ListenerAdapter{
	
	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent e){
		EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE);
		
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String user_name = e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator();
		long guild_id = e.getGuild().getIdLong();
		String guild_name = e.getGuild().getName();
		
		SqlConnect.SQLgetChannelID(guild_id, "log");
		long channel_id = SqlConnect.getChannelID();
		
		SqlConnect.SQLgetData(e.getMember().getUser().getIdLong(), guild_id);
		
		if(channel_id != 0){
			if(SqlConnect.getBanID() == 2 && SqlConnect.getMuted()){
				System.out.println("["+timestamp.toString()+"] "+user_name+" with the id number "+e.getMember().getUser().getId()+" has been banned!");
			}
			else if(SqlConnect.getMuted()){
				e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+"** has left from "+guild_name+" while being muted!").build()).queue();
			}
			else if(IniFileReader.getLeaveMessage().equals("true")){
				e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+"** has left or has been kicked from **"+guild_name+"**").build()).queue();
			}
		}
		SqlConnect.clearAllVariables();
	}

}
