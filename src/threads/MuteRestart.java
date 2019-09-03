package threads;

import java.awt.Color;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Channels;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.ReadyEvent;
import sql.Azrael;
import util.STATIC;

public class MuteRestart implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(MuteRestart.class);
	private EmbedBuilder message = new EmbedBuilder().setColor(Color.GREEN).setThumbnail(IniFileReader.getUnmuteThumbnail()).setTitle("User unmuted!");
	
	private ReadyEvent e;
	private Member member;
	private long guild_id;
	private Channels channel;
	private Role mute_role;
	private long unmute;
	private long assignedRole;
	private boolean ranking_state;
	
	public MuteRestart(ReadyEvent _e, Member _member, long _guild_id, Channels _channel, Role _mute_role, long _unmute, long _assignedRole, boolean _ranking_state){
		this.e = _e;
		this.member = _member;
		this.guild_id = _guild_id;
		this.channel = _channel;
		this.mute_role = _mute_role;
		this.unmute = _unmute;
		this.assignedRole = _assignedRole;
		this.ranking_state = _ranking_state;
	}

	@Override
	public void run() {
		STATIC.addThread(Thread.currentThread(), "mute_gu"+guild_id+"us"+member.getUser().getId());
		try {
			Thread.sleep(unmute);
			if(!Azrael.SQLisBanned(member.getUser().getIdLong(), guild_id)) {
				if(channel != null && Azrael.SQLgetMuted(member.getUser().getIdLong(), guild_id) == true) {
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					e.getJDA().getGuildById(guild_id).getTextChannelById(channel.getChannel_ID()).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+member.getUser().getName()+"#"+member.getUser().getDiscriminator() + "** with the ID Number **" + member.getUser().getId() + "** has been unmuted").build()).queue();
				}
				e.getJDA().getGuildById(guild_id).removeRoleFromMember(member, mute_role).queue();
				if(assignedRole != 0 && ranking_state == true){e.getJDA().getGuildById(guild_id).addRoleToMember(member, e.getJDA().getGuildById(guild_id).getRoleById(assignedRole)).queue();}
			}			
		} catch (InterruptedException e2) {
			//launch when the sleep has been interrupted
			if(!Azrael.SQLisBanned(member.getUser().getIdLong(), guild_id)) {
				logger.info("The mute of {} ing guild {} has been interrupted!", member.getUser().getId(), e.getJDA().getGuildById(guild_id).getName());
				if(channel != null && (Azrael.SQLgetMuted(member.getUser().getIdLong(), guild_id) == true || Azrael.SQLgetData(member.getUser().getIdLong(), guild_id).getUserID() == 0)) {
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					Azrael.SQLUpdateUnmute(member.getUser().getIdLong(), guild_id, timestamp);
					e.getJDA().getGuildById(guild_id).getTextChannelById(channel.getChannel_ID()).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+member.getUser().getName()+"#"+member.getUser().getDiscriminator() + "** with the ID Number **" + member.getUser().getId() + "** has been unmuted and the timer has been interrupted!").build()).queue();
				}
				if(e.getJDA().getGuildById(guild_id).getMembers().parallelStream().filter(f -> f.getUser().getIdLong() == member.getUser().getIdLong()).findAny().orElse(null) != null) {
					e.getJDA().getGuildById(guild_id).removeRoleFromMember(member, mute_role).queue();
					if(assignedRole != 0 && ranking_state == true){e.getJDA().getGuildById(guild_id).addRoleToMember(member, e.getJDA().getGuildById(guild_id).getRoleById(assignedRole)).queue();}
				}
			}
		}
		STATIC.removeThread(Thread.currentThread());
	}
}
