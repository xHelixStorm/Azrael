package listeners;

import java.awt.Color;
import java.sql.Timestamp;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.SqlConnect;

public class UnbanListener extends ListenerAdapter{
	
	@Override
	public void onGuildUnban(GuildUnbanEvent e){
		EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(IniFileReader.getUnbanThumbnail()).setTitle("User unbanned!");
		long guild = e.getGuild().getIdLong();
		SqlConnect.SQLgetChannelID(guild, "log");
		long channel_id = SqlConnect.getChannelID();
		long user_id = e.getUser().getIdLong();
		long guild_id = e.getGuild().getIdLong();
		String user_name = e.getUser().getName()+"#"+e.getUser().getDiscriminator();
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		
		if(channel_id != 0){e.getJDA().getGuildById(e.getGuild().getIdLong()).getTextChannelById(channel_id).sendMessage(message.setDescription("**["+timestamp+"] " + user_name + " with the ID number " + user_id + " has been unbanned!**").build()).queue();}
		SqlConnect.SQLUpdateWarningAndBan(user_id, guild_id, timestamp, 5, 1);
		SqlConnect.SQLUpdateMuted(user_id, guild_id, false);
		SqlConnect.clearAllVariables();
	}
}
