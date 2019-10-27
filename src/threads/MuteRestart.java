package threads;

/**
 * Abbreviation of the RoleTimer class. Works the same way basing on 
 * removing the mute role after a determined time.
 */

import java.awt.Color;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Channels;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import sql.Azrael;
import util.STATIC;

public class MuteRestart implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(MuteRestart.class);
	private EmbedBuilder message = new EmbedBuilder().setColor(Color.GREEN).setThumbnail(IniFileReader.getUnmuteThumbnail()).setTitle("User unmuted!");
	
	private long user_id;
	private Guild guild;
	private String user_name;
	private Channels channel;
	private Role mute_role;
	private long unmute;
	private long assignedRole;
	private boolean ranking_state;
	
	public MuteRestart(long _user_id, Guild _guild, String _user_name, Channels _channel, Role _mute_role, long _unmute, long _assignedRole, boolean _ranking_state){
		this.user_id = _user_id;
		this.guild = _guild;
		this.user_name = _user_name;
		this.channel = _channel;
		this.mute_role = _mute_role;
		this.unmute = _unmute;
		this.assignedRole = _assignedRole;
		this.ranking_state = _ranking_state;
	}

	@Override
	public void run() {
		STATIC.addThread(Thread.currentThread(), "mute_gu"+guild.getId()+"us"+user_id);
		try {
			//put the thread to wait for a determined time
			Thread.sleep(unmute);
			//check if the user has been banned during the wait and if not, check if he's still muted. If muted, print message that the mute has been lifted
			if(!Azrael.SQLisBanned(user_id, guild.getIdLong())) {
				if(channel != null && Azrael.SQLgetMuted(user_id, guild.getIdLong()) == true) {
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					guild.getTextChannelById(channel.getChannel_ID()).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+ "** with the ID Number **" +user_id+ "** has been unmuted").build()).queue();
				}
				//if the user is still present on the server, remove the mute role and assign back a ranking role, if available
				Member member = guild.getMemberById(user_id);
				if(member != null) {
					//verify that the user has the manage roles permission before removing the mute role
					if(guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
						guild.removeRoleFromMember(member, mute_role).queue();
						if(assignedRole != 0 && ranking_state == true){guild.addRoleToMember(member, guild.getRoleById(assignedRole)).queue();}
					}
					else {
						if(channel != null) guild.getTextChannelById(channel.getChannel_ID()).sendMessage(message.setTitle("Permission required!").setDescription("The mute role couldn't be removed from **"+user_name+"** with the id number **"+user_id+"** because the permission MANAGE ROLES is missing").build()).queue();
						logger.warn("MANAGE ROLES permission required to remove the mute role in guild {}", guild.getId());
					}
				}
			}			
		} catch (InterruptedException e2) {
			//executed when H!user unmute was used to interrupt the Thread.sleep
			logger.info("The mute of {} ing guild {} has been interrupted!", user_id, guild.getId());
			//verify that the user is not banned and still labeled as muted before printing a message and before updating the unmute time
			if(!Azrael.SQLisBanned(user_id, guild.getIdLong())) {
				if(channel != null && (Azrael.SQLgetMuted(user_id, guild.getIdLong()) == true || Azrael.SQLgetData(user_id, guild.getIdLong()).getUserID() == 0)) {
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					Azrael.SQLUpdateUnmute(user_id, guild.getIdLong(), timestamp);
					guild.getTextChannelById(channel.getChannel_ID()).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+ "** with the ID Number **" +user_id+ "** has been unmuted and the timer has been interrupted!").build()).queue();
				}
				//if the user is still present on the server, remove the mute role and assign back a ranking role, if available
				Member member = guild.getMemberById(user_id);
				if(member != null) {
					//verify that the user has the manage roles permission before removing the mute role
					if(guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
						guild.removeRoleFromMember(member, mute_role).queue();
						if(assignedRole != 0 && ranking_state == true){guild.addRoleToMember(member, guild.getRoleById(assignedRole)).queue();}
					}
					else {
						if(channel != null) guild.getTextChannelById(channel.getChannel_ID()).sendMessage(message.setTitle("Permission required!").setDescription("The mute role couldn't be removed from **"+user_name+"** with the id number **"+user_id+"** because the permission MANAGE ROLES is missing").build()).queue();
						logger.warn("MANAGE ROLES permission required to remove the mute role in guild {}", guild.getId());
					}
				}
			}
		}
		STATIC.removeThread(Thread.currentThread());
	}
}
