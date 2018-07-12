package threads;

import java.awt.Color;
import java.sql.Timestamp;
import core.UserPrivs;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.SqlConnect;

public class RoleTimer extends ListenerAdapter implements Runnable{
	private GuildMemberRoleAddEvent e;
	private long guild_id;
	private String name_id;
	private String user_name;
	private long timer;
	private long channel_id;
	private long mute_id;
	private double unmute;
	private long assignedRole;
	
	public RoleTimer(GuildMemberRoleAddEvent event, long _guild_id, String _name_id, String _user_name, double _unmute, long _timer, long _channel_id, long _mute_id, long _assignedRole){
		this.e = event;
		this.guild_id = _guild_id;
		this.name_id = _name_id;
		this.user_name = _user_name;
		this.unmute = _unmute;
		this.timer = _timer;
		this.channel_id = _channel_id;
		this.mute_id = _mute_id;
		this.assignedRole = _assignedRole;
	}
	
	@Override
	public void run() {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.RED);
		EmbedBuilder message2 = new EmbedBuilder().setColor(Color.GREEN);
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		
		try {
			if(UserPrivs.isUserMuted(e.getMember().getUser(), e.getGuild().getIdLong()) == true){
				if(channel_id != 0){
					e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+ "** with the ID Number **" + e.getMember().getUser().getId() + "** has been muted and will be unmuted in **"+unmute+"** hour(s)!").build()).queue();
				}
				Thread.sleep(timer);
				
				e.getJDA().getGuildById(e.getGuild().getIdLong()).getController().removeSingleRoleFromMember(e.getMember(), e.getGuild().getRoleById(mute_id)).queue();
				Thread.sleep(1000);
				
				timestamp = new Timestamp(System.currentTimeMillis());
				if(assignedRole != 0){e.getJDA().getGuildById(e.getGuild().getId()).getController().addSingleRoleToMember(e.getMember(), e.getGuild().getRoleById(assignedRole)).queue();}
				SqlConnect.SQLgetData(Long.parseLong(name_id), guild_id);
				long unmutedTime = SqlConnect.getUnmute().getTime();
				SqlConnect.clearUnmute();
				if(channel_id != 0 && System.currentTimeMillis() > unmutedTime){
					e.getGuild().getTextChannelById(channel_id).sendMessage(message2.setDescription("["+timestamp.toString()+"] **"+user_name+ "** with the ID Number **" + e.getMember().getUser().getId() + "** has been unmuted").build()).queue();
				}
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
}
