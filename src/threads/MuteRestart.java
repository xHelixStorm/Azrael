package threads;

import java.awt.Color;
import java.sql.Timestamp;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.ReadyEvent;

public class MuteRestart implements Runnable{
	private EmbedBuilder message = new EmbedBuilder().setColor(Color.GREEN).setThumbnail(IniFileReader.getUnmuteThumbnail()).setTitle("User unmuted!");
	
	private ReadyEvent e;
	private Member member;
	private long guild_id;
	private long channel_id;
	private Role mute_role;
	private long unmute;
	private long assignedRole;
	private boolean ranking_state;
	
	public MuteRestart(ReadyEvent _e, Member _member, long _guild_id, long _channel_id, Role _mute_role, long _unmute, long _assignedRole, boolean _ranking_state){
		this.e = _e;
		this.member = _member;
		this.guild_id = _guild_id;
		this.channel_id = _channel_id;
		this.mute_role = _mute_role;
		this.unmute = _unmute;
		this.assignedRole = _assignedRole;
		this.ranking_state = _ranking_state;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(unmute);
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			
			e.getJDA().getGuildById(guild_id).getController().removeSingleRoleFromMember(member, mute_role).queue();
			Thread.sleep(1000);
			
			if(assignedRole != 0 && ranking_state == true){e.getJDA().getGuildById(guild_id).getController().addSingleRoleToMember(member, e.getJDA().getGuildById(guild_id).getRoleById(assignedRole)).queue();}
			if(channel_id != 0){
				e.getJDA().getGuildById(guild_id).getTextChannelById(channel_id).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+member.getUser().getName()+"#"+member.getUser().getDiscriminator() + "** with the ID Number **" + member.getUser().getId() + "** has been unmuted").build()).queue();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
