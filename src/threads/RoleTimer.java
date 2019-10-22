package threads;

import java.awt.Color;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Channels;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.Azrael;
import util.STATIC;

public class RoleTimer extends ListenerAdapter implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(RoleTimer.class);
	
	private GuildMemberRoleAddEvent e;
	private long timer;
	private Channels channel;
	private long mute_id;
	private long assignedRole;
	private String hour_add;
	private String and_add;
	private String minute_add;
	private int warning_id;
	private int max_warning_id;
	private String issuer;
	private String reason;
	
	public RoleTimer(GuildMemberRoleAddEvent event, long _timer, Channels _channel, long _mute_id, long _assignedRole, String _hour_add, String _and_add, String _minute_add, int _warning_id, int _max_warning_id, String _issuer, String _reason) {
		this.e = event;
		this.timer = _timer;
		this.channel = _channel;
		this.mute_id = _mute_id;
		this.assignedRole = _assignedRole;
		this.hour_add = _hour_add;
		this.minute_add = _minute_add;
		this.warning_id = _warning_id;
		this.max_warning_id = _max_warning_id;
		this.and_add = _and_add;
		this.issuer = _issuer;
		this.reason = _reason;
	}
	public RoleTimer(GuildMemberRoleAddEvent event, Channels _channel, long _mute_id, String _issuer, String _reason) {
		this.e = event;
		this.timer = 0;
		this.channel = _channel;
		this.mute_id = _mute_id;
		this.assignedRole = 0;
		this.hour_add = null;
		this.minute_add = null;
		this.warning_id = 0;
		this.max_warning_id = 0;
		this.and_add = null;
		this.issuer = _issuer;
		this.reason = _reason;
	}
	
	@Override
	public void run() {
		STATIC.addThread(Thread.currentThread(), "mute_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
		EmbedBuilder message = new EmbedBuilder().setColor(Color.RED).setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl()).setTitle("User muted!");
		EmbedBuilder message2 = new EmbedBuilder().setColor(Color.GREEN).setThumbnail(IniFileReader.getUnmuteThumbnail()).setTitle("User unmuted!");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		long user_id = e.getUser().getIdLong();
		long guild_id = e.getGuild().getIdLong();
		String user_name = e.getUser().getName()+"#"+e.getUser().getDiscriminator();
		
		if(timer > 0) {
			try {
				if(channel != null) {
					if(warning_id == 0 && max_warning_id == 0) {
						e.getGuild().getTextChannelById(channel.getChannel_ID()).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+ "** with the ID Number **" + e.getMember().getUser().getId() + "** has been muted with a custom mute time and will be unmuted in **"+hour_add+and_add+minute_add+"**!\nMuted by: "+issuer+"\nReason: "+reason).build()).queue();
					}
					else {
						e.getGuild().getTextChannelById(channel.getChannel_ID()).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+ "** with the ID Number **" + e.getMember().getUser().getId() + "** has been muted and will be unmuted in **"+hour_add+and_add+minute_add+"**!\nMuted by: "+issuer+"\nReason: "+reason+"\nWarning **"+warning_id+"**/**"+max_warning_id+"**").build()).queue();
					}
				}
				Thread.sleep(timer);
				if(!Azrael.SQLisBanned(user_id, guild_id)) {
					if(channel != null && Azrael.SQLgetMuted(user_id, guild_id) == true) {
						timestamp = new Timestamp(System.currentTimeMillis());
						e.getGuild().getTextChannelById(channel.getChannel_ID()).sendMessage(message2.setDescription("["+timestamp.toString()+"] **"+user_name+ "** with the ID Number **" + e.getMember().getUser().getId() + "** has been unmuted").build()).queue();
					}
					if(e.getJDA().getGuildById(guild_id).getMembers().parallelStream().filter(f -> f.getUser().getIdLong() == e.getGuild().getMemberById(user_id).getUser().getIdLong()).findAny().orElse(null) != null) {
						e.getJDA().getGuildById(e.getGuild().getIdLong()).removeRoleFromMember(e.getMember(), e.getGuild().getRoleById(mute_id)).queue();
						if(assignedRole != 0){e.getJDA().getGuildById(e.getGuild().getId()).addRoleToMember(e.getMember(), e.getGuild().getRoleById(assignedRole)).queue();}
					}
				}
			} catch (InterruptedException e1) {
				logger.info("The mute of {} in guild {} has been interrupted!", e.getMember().getUser().getId(), e.getGuild().getId());
				if(!Azrael.SQLisBanned(user_id, guild_id)) {
					if(channel != null && (Azrael.SQLgetMuted(user_id, guild_id) == true || Azrael.SQLgetData(user_id, guild_id).getUserID() == 0)) {
						timestamp = new Timestamp(System.currentTimeMillis());
						Azrael.SQLUpdateUnmute(user_id, guild_id, timestamp);
						e.getGuild().getTextChannelById(channel.getChannel_ID()).sendMessage(message2.setDescription("["+timestamp.toString()+"] **"+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator() + "** with the ID Number **" + e.getMember().getUser().getId() + "** has been unmuted and the timer has been interrupted!").build()).queue();
					}
					if(e.getJDA().getGuildById(guild_id).getMembers().parallelStream().filter(f -> f.getUser().getIdLong() == e.getMember().getUser().getIdLong()).findAny().orElse(null) != null) {
						e.getJDA().getGuildById(e.getGuild().getIdLong()).removeRoleFromMember(e.getMember(), e.getGuild().getRoleById(mute_id)).queue();
						if(assignedRole != 0){e.getJDA().getGuildById(e.getGuild().getId()).addRoleToMember(e.getMember(), e.getGuild().getRoleById(assignedRole)).queue();}
					}
				}
			}
		}
		else {
			if(channel != null) {
				e.getGuild().getTextChannelById(channel.getChannel_ID()).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+ "** with the ID Number **" + e.getMember().getUser().getId() + "** has been muted without any expiration until further actions!\n Muted by: "+issuer+"\nReason: "+reason).build()).queue();
			}
		}
		STATIC.removeThread(Thread.currentThread());
	}
}
