package threads;

/**
 * This class is meant to automatically remove the mute role after a defined amount of time
 * and gets directly called by the RoleReceivedListener class. 
 * 
 * handled are mutes with a custom time and general mutes based on the current warning
 * counter of the affected user. If a permanent mute will be applied, the timer will be 
 * set to 0 and it should just print the message.
 */

import java.awt.Color;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import constructors.Channels;
import core.Hashes;
import enums.GoogleEvent;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import google.GoogleUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.Azrael;
import util.STATIC;

public class RoleTimer extends ListenerAdapter implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(RoleTimer.class);
	private final static EmbedBuilder message = new EmbedBuilder().setColor(Color.RED).setTitle("User muted!");
	private final static EmbedBuilder message2 = new EmbedBuilder().setColor(Color.GREEN).setThumbnail(IniFileReader.getUnmuteThumbnail()).setTitle("User unmuted!");
	
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
		//collect current thread and assign a name to make interruptions possible, if required
		STATIC.addThread(Thread.currentThread(), "mute_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
		message.setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl());
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		long user_id = e.getUser().getIdLong();
		long guild_id = e.getGuild().getIdLong();
		String user_name = e.getUser().getName()+"#"+e.getUser().getDiscriminator();
		
		//execute when a timer bigger than 0 has been provided. If the timer is 0, a permanent mute without expiration has been given
		if(timer > 0) {
			try {
				//print a muted user message if a log channel has been registered
				if(channel != null) {
					if(warning_id == 0 && max_warning_id == 0) {
						e.getGuild().getTextChannelById(channel.getChannel_ID()).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+ "** with the ID Number **" + e.getMember().getUser().getId() + "** has been muted with a custom mute time and will be unmuted in **"+hour_add+and_add+minute_add+"**!\nMuted by: "+issuer+"\nReason: "+reason).build()).queue();
					}
					else {
						e.getGuild().getTextChannelById(channel.getChannel_ID()).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+ "** with the ID Number **" + e.getMember().getUser().getId() + "** has been muted and will be unmuted in **"+hour_add+and_add+minute_add+"**!\nMuted by: "+issuer+"\nReason: "+reason+"\nWarning **"+warning_id+"**/**"+max_warning_id+"**").build()).queue();
					}
				}
				//put the thread to wait for a determined time
				Thread.sleep(timer);
				//check if the user has been banned during the wait and if not, check if he's still muted. If muted, print message that the mute has been lifted
				if(!Azrael.SQLisBanned(user_id, guild_id)) {
					if(channel != null && Azrael.SQLgetMuted(user_id, guild_id) == true) {
						timestamp = new Timestamp(System.currentTimeMillis());
						e.getGuild().getTextChannelById(channel.getChannel_ID()).sendMessage(message2.setDescription("["+timestamp.toString()+"] **"+user_name+ "** with the ID Number **" + e.getMember().getUser().getId() + "** has been unmuted").build()).queue();
					}
					//if the user is still present on the server, remove the mute role and assign back a ranking role, if available
					if(e.getGuild().getMember(e.getMember().getUser()) != null) {
						//verify that the user has the manage roles permission before removing the mute role
						if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
							//write into cache for RoleRemovedListener to use for any google API operation
							if(assignedRole != 0)Hashes.addTempCache("unmute_gu"+guild_id+"us"+user_id, new Cache(60000, "", ""+assignedRole));
							e.getGuild().removeRoleFromMember(e.getMember(), e.getGuild().getRoleById(mute_id)).queue();
							if(assignedRole != 0)e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(assignedRole)).queue();
						}
						else {
							if(channel != null) e.getGuild().getTextChannelById(channel.getChannel_ID()).sendMessage(message.setTitle("Permission required!").setDescription("The mute role couldn't be removed from **"+user_name+"** with the id number **"+user_id+"** because the permission MANAGE ROLES is missing").build()).queue();
							logger.warn("MANAGE ROLES permission required to remove the mute role in guild {}", guild_id);
						}
					}
				}
			} catch (InterruptedException e1) {
				//executed when H!user unmute was used to interrupt the Thread.sleep
				logger.info("The mute of {} in guild {} has been interrupted!", e.getMember().getUser().getId(), e.getGuild().getId());
				//verify that the user is not banned and still labeled as muted before printing a message and before updating the unmute time
				if(!Azrael.SQLisBanned(user_id, guild_id)) {
					if(channel != null && (Azrael.SQLgetMuted(user_id, guild_id) == true || Azrael.SQLgetData(user_id, guild_id).getUserID() == 0)) {
						timestamp = new Timestamp(System.currentTimeMillis());
						Azrael.SQLUpdateUnmute(user_id, guild_id, timestamp);
						e.getGuild().getTextChannelById(channel.getChannel_ID()).sendMessage(message2.setDescription("["+timestamp.toString()+"] **"+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator() + "** with the ID Number **" + e.getMember().getUser().getId() + "** has been unmuted and the timer has been interrupted!").build()).queue();
					}
					//if the user is still present on the server, remove the mute role and assign back a ranking role, if available
					Role role = null;
					if(e.getGuild().getMember(e.getMember().getUser()) != null) {
						//verify that the user has the manage roles permission before removing the mute role
						if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
							if(assignedRole != 0)role = e.getGuild().getRoleById(assignedRole);
							e.getGuild().removeRoleFromMember(e.getMember(), e.getGuild().getRoleById(mute_id)).queue();
							if(role != null)e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(assignedRole)).queue();
						}
						else {
							if(channel != null) e.getGuild().getTextChannelById(channel.getChannel_ID()).sendMessage(message.setTitle("Permission required!").setDescription("The mute role couldn't be removed from **"+user_name+"** with the id number **"+user_id+"** because the permission MANAGE ROLES is missing").build()).queue();
							logger.warn("MANAGE ROLES permission required to remove the mute role in guild {}", guild_id);
						}
					}
					//Run google service, if enabled
					if(GuildIni.getGoogleFunctionalitiesEnabled(e.getGuild().getIdLong()) && GuildIni.getGoogleSpreadsheetsEnabled(e.getGuild().getIdLong())) {
						final var cache = Hashes.getTempCache("unmute_gu"+guild_id+"us"+user_id);
						String reporter_name = "NaN";
						String reporter_username = "Nan";
						String role_id = "NaN";
						String role_name = "NaN";
						if(cache != null) {
							final var reporter = e.getGuild().getMemberById(cache.getAdditionalInfo());
							if(reporter != null) {
								reporter_name = reporter.getUser().getName()+"#"+reporter.getUser().getDiscriminator();
								reporter_username = reporter.getEffectiveName();
							}
							Hashes.clearTempCache("unmute_gu"+guild_id+"us"+user_id);
						}
						if(role != null) {
							role_id = role.getId();
							role_name = role.getName();
						}
						GoogleUtils.handleSpreadsheetRequest(e.getGuild(), ""+user_id, timestamp, user_name, e.getMember().getEffectiveName(), reporter_name, reporter_username, "NaN", null, null, "UNMUTED", null, role_id, role_name, GoogleEvent.UNMUTE.id, channel);
					}
				}
			}
		}
		//print message of the user being permanently muted
		else if(channel != null) {
			e.getGuild().getTextChannelById(channel.getChannel_ID()).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+ "** with the ID Number **" + e.getMember().getUser().getId() + "** has been muted without any expiration until further actions!\n Muted by: "+issuer+"\nReason: "+reason).build()).queue();
		}
		//task completed! Remove this thread from the array of currently running threads
		STATIC.removeThread(Thread.currentThread());
	}
}
