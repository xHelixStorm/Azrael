package threads;

import java.awt.Color;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.UserPrivs;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.Azrael;

public class RoleTimer extends ListenerAdapter implements Runnable{
	private GuildMemberRoleAddEvent e;
	private long guild_id;
	private String name_id;
	private String user_name;
	private long timer;
	private long channel_id;
	private long mute_id;
	private long assignedRole;
	private String hour_add;
	private String and_add;
	private String minute_add;
	private int warning_id;
	private int max_warning_id;
	
	public RoleTimer(GuildMemberRoleAddEvent event, long _guild_id, String _name_id, String _user_name, long _timer, long _channel_id, long _mute_id, long _assignedRole, String _hour_add, String _and_add, String _minute_add, int _warning_id, int _max_warning_id){
		this.e = event;
		this.guild_id = _guild_id;
		this.name_id = _name_id;
		this.user_name = _user_name;
		this.timer = _timer;
		this.channel_id = _channel_id;
		this.mute_id = _mute_id;
		this.assignedRole = _assignedRole;
		this.hour_add = _hour_add;
		this.minute_add = _minute_add;
		this.warning_id = _warning_id;
		this.max_warning_id = _max_warning_id;
		this.and_add = _and_add;
	}
	
	@Override
	public void run() {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.RED).setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl()).setTitle("User muted!");
		EmbedBuilder message2 = new EmbedBuilder().setColor(Color.GREEN).setThumbnail(IniFileReader.getUnmuteThumbnail()).setTitle("User unmuted!");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		
		try {
			if(UserPrivs.isUserMuted(e.getMember().getUser(), e.getGuild().getIdLong()) == true){
				if(channel_id != 0){
					if(warning_id == 0 && max_warning_id == 0) {
						e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+ "** with the ID Number **" + e.getMember().getUser().getId() + "** has been muted with a custom mute time and will be unmuted in **"+hour_add+and_add+minute_add+"**!").build()).queue();
					}
					else {
						e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+ "** with the ID Number **" + e.getMember().getUser().getId() + "** has been muted and will be unmuted in **"+hour_add+and_add+minute_add+"**!\nWarning **"+warning_id+"**/**"+max_warning_id+"**").build()).queue();
					}
				}
				Thread.sleep(timer);
				
				Azrael.clearUnmute();
				Azrael.SQLgetMuted(Long.parseLong(name_id), guild_id);
				if(channel_id != 0 && Azrael.getMuted() == true){
					timestamp = new Timestamp(System.currentTimeMillis());
					e.getGuild().getTextChannelById(channel_id).sendMessage(message2.setDescription("["+timestamp.toString()+"] **"+user_name+ "** with the ID Number **" + e.getMember().getUser().getId() + "** has been unmuted").build()).queue();
				}
				e.getJDA().getGuildById(e.getGuild().getIdLong()).getController().removeSingleRoleFromMember(e.getMember(), e.getGuild().getRoleById(mute_id)).complete();
				if(assignedRole != 0){e.getJDA().getGuildById(e.getGuild().getId()).getController().addSingleRoleToMember(e.getMember(), e.getGuild().getRoleById(assignedRole)).queue();}
				Azrael.clearAllVariables();
			}
		} catch (InterruptedException e1) {
			Logger logger = LoggerFactory.getLogger(RoleTimer.class);
			logger.error("Exception on separate thread for muted members", e1);
		}
	}
}
