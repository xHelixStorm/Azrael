package listeners;

/**
 * this class gets executed when a user receives a role.
 * 
 * Handled is when a user receives the mute role. After
 * receiving the mute role, it will be checked what
 * kind and level of punishment the user will receive.
 */ 

import java.awt.Color;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Bancollect;
import constructors.Rank;
import constructors.Warning;
import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.RankingSystem;
import sql.DiscordRoles;
import sql.Azrael;
import threads.RoleTimer;

public class RoleListener extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(RoleListener.class);
	private static final EmbedBuilder message2 = new EmbedBuilder().setColor(Color.GREEN).setTitle("Mute Retracted!");
	
	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent e) {
		new Thread(() -> {
			EmbedBuilder message = new EmbedBuilder().setColor(Color.RED).setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl()).setTitle("A user has been muted!");
			
			long user_id = e.getMember().getUser().getIdLong();
			long guild_id = e.getMember().getGuild().getIdLong();
			String user_name = e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator();
			long mute_time;
			double unmute;
			
			//verify that the user is muted currently
			if(UserPrivs.isUserMuted(e.getMember())) {
				//retrieve log channel, mute role and current warnings
				var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
				var mute_id = DiscordRoles.SQLgetRoles(guild_id).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null).getRole_ID();
				Bancollect warnedUser = Azrael.SQLgetData(user_id, guild_id);
				var permMute = false;
				long unmute_time = 0;
				//verify if it's a permanent mute or if the user should be muted for a specific time
				if(warnedUser.getUnmute() == null && warnedUser.getWarningID() != 0) {
					permMute = true;
				}
				else {
					try {
						if(warnedUser.getUnmute().getTime() != 0) {
							unmute_time = warnedUser.getUnmute().getTime();
						}
					} catch(NullPointerException npe) {
						unmute_time = -1;
					}
				}
				//enter this block if the user is already permanently muted
				if(permMute) {
					//set mute status to true
					if(Azrael.SQLUpdateMuted(user_id, guild_id, true) == 0) {
						logger.error("Mute information of {} couldn't be updated in Azrael.bancollect in guild {}", user_id, e.getGuild().getId());
						if(log_channel != null)e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. The mute state couldn't be updated in table Azrael.bancollect").queue();
					}
					if(log_channel != null) {
						Timestamp timestamp = new Timestamp(System.currentTimeMillis());
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+ "** with the ID number **"+e.getMember().getUser().getId()+"** got his mute role reassigned and is again permanently muted! Reason may be due to manually reassigning the mute role or due to rejoining the server!").build()).queue();
					}
					//remove all roles, except the mute role when the muted user has more than 1 assigned role
					if(e.getMember().getRoles().size() > 1 && !removeRoles(e, mute_id) && log_channel != null)
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Permission error!").setDescription("MANAGE ROLES permission required to remove all roles! Roles removal interrupted!").build()).queue();
				}
				//enter if a mute timer has been defined but the user itself is not yet marked as muted on the table (e.g. manually removing and adding the mute role)
				else if(unmute_time - System.currentTimeMillis() > 0 && !warnedUser.getMuted()) {
					//set mute status to true
					if(Azrael.SQLUpdateMuted(user_id, guild_id, true) == 0) {
						logger.error("Mute information of {} couldn't be updated in Azrael.bancollect in guild {}", user_id, e.getGuild().getId());
						if(log_channel != null)e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. The mute state couldn't be updated in table Azrael.bancollect").queue();
					}
					if(log_channel != null) {
						Timestamp timestamp = new Timestamp(System.currentTimeMillis());
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+ "** with the ID number **"+e.getMember().getUser().getId()+"** got his mute role reassigned before the mute time elapsed! Reason may be due to manually reassigning the mute role!").build()).queue();
					}
					//remove all roles, except the mute role when the muted user has more than 1 assigned role
					if(e.getMember().getRoles().size() > 1 && !removeRoles(e, mute_id) && log_channel != null)
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Permission error!").setDescription("MANAGE ROLES permission required to remove all roles! Roles removal interrupted!").build()).queue();
				}
				//enter in this block, if the user has been already muted but rejoined the server before the time elapsed
				else if(unmute_time - System.currentTimeMillis() > 0 && warnedUser.getMuted() && warnedUser.getGuildLeft()) {
					//mark the user as has rejoined the server
					Azrael.SQLUpdateGuildLeft(user_id, guild_id, false);
					if(log_channel != null) {
						Timestamp timestamp = new Timestamp(System.currentTimeMillis());
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+ "** with the ID number **"+e.getMember().getUser().getId()+"** got his mute role reassigned before the mute time elapsed! Reason may be due to leaving and rejoining the server!").build()).queue();
					}
				}
				//for manual mutes without command and which isn't permanent and for users that can be interacted with
				else if(e.getGuild().getSelfMember().canInteract(e.getMember())) {
					long from_user = 0;
					//check if the bot has able to view the audit logs
					if(e.getGuild().getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
						var roleLog = e.getGuild().retrieveAuditLogs();
						//iterate through the log
						for(final var entry : roleLog) {
							//retrieve the first log about a role update
							if(entry.getType() == ActionType.MEMBER_ROLE_UPDATE) {
								//retrieve the user who applied the mute role
								from_user = entry.getUser().getIdLong();
							}
							break;
						}
					}
					
					//remove all roles, except the mute role when the muted user has more than 1 assigned role
					if(e.getMember().getRoles().size() > 1 && !removeRoles(e, mute_id) && log_channel != null)
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Permission error!").setDescription("MANAGE ROLES permission required to remove all roles! Roles removal interrupted!").build()).queue();
					
					long time = System.currentTimeMillis();
					int warning_id = warnedUser.getWarningID();
					long assignedRole = 0;
					//check if this user has a ranking role registered and if yes, save it for when the mute elapsed
					Rank user_details = RankingSystem.SQLgetWholeRankView(user_id, guild_id);
					if(user_details != null) {
						assignedRole = user_details.getCurrentRole();
					}
					
					//execute this block if a custom mute time has been applied with a command
					if(warnedUser.getCustomTime()) {
						//get cache with reason and user who applied the mute
						var cache = Hashes.getTempCache("mute_time_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
						mute_time = Long.parseLong(cache.getAdditionalInfo());
						var issuer = cache.getAdditionalInfo2();
						var reason = cache.getAdditionalInfo3();
						Hashes.clearTempCache("mute_time_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
						//calculate the mute time in a user friendly, visible form
						long hours = (mute_time/1000/60/60);
						long minutes = (mute_time/1000/60%60);
						String hour_add = hours != 0 ? hours+" hours" : "";
						String minute_add = minutes != 0 ? minutes+" minutes" : "";
						String and_add = minutes != 0 && hours != 0 ? " and " : "";
						
						//send a private message to the user
						e.getUser().openPrivateChannel().queue(channel -> {
							channel.sendMessage("You have been muted on "+e.getGuild().getName()+". Your current mute will last for **"+hour_add+and_add+minute_add+"** . Except for the first mute, your warning counter won't increase.\nPlease, refrain from rejoining the server, since it will result in consequences.\n"
									+ "On an important note, this is an automated reply. You'll receive no reply in any way.\n"
									+ (GuildIni.getMuteSendReason(guild_id) ? "Provided reason: **"+reason+"**" : "")).queue();
						});
						//unmute after a specific amount of time
						new Thread(new RoleTimer(e, mute_time, log_channel, mute_id, assignedRole, hour_add, and_add, minute_add, 0, 0, issuer, reason)).start();
						logger.debug("{} got muted in guild {}", e.getUser().getId(), e.getGuild().getId());
					}
					//execute this block if a regular mute has been applied
					else {
						//check if the cache contains a reason and a name of who applied the mute
						var cache = Hashes.getTempCache("mute_time_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
						var issuer = (cache != null ? cache.getAdditionalInfo() : (from_user != 0 ? e.getGuild().getMemberById(from_user).getAsMention() : "NaN"));
						var reason = (cache != null ? cache.getAdditionalInfo2() : "No reason has been provided!");
						Hashes.clearTempCache("mute_time_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
						//retrieve current warning and the time to mute
						Warning warn = Azrael.SQLgetWarning(e.getGuild().getIdLong(), (warning_id+1));
						mute_time = (long) warn.getTimer();
						unmute = warn.getTimer();
						//calculate the mute time in a user friendly, visible form
						long hours = (long) (unmute/1000/60/60);
						long minutes = (long) (unmute/1000/60%60);
						String hour_add = hours != 0 ? hours+" hours" : "";
						String minute_add = minutes != 0 ? minutes+" minutes" : "";
						String and_add = minutes != 0 && hours != 0 ? " and " : "";
						Timestamp timestamp = new Timestamp(time);
						Timestamp unmute_timestamp = new Timestamp(time+mute_time);
						
						//mute if the current warning is lower than the max warning
						int max_warning = Azrael.SQLgetMaxWarning(e.getGuild().getIdLong());
						if((warning_id+1) <= max_warning) {
							//mark the user as muted with a set time
							if(Azrael.SQLInsertData(user_id, guild_id, (warning_id+1), 1, timestamp, unmute_timestamp, true, false) == 0) {
								logger.error("muted user {} couldn't be inserted into Azrael.bancollect for guild {}", e.getUser().getId(), e.getGuild().getName());
								if(log_channel != null)e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. Muted user couldn't be inserted into Azrael.bancollect").queue();
							}
							//send a private message
							e.getUser().openPrivateChannel().queue(channel -> {
								channel.sendMessage("You have been muted on "+e.getGuild().getName()+". Your current mute will last for **"+hour_add+and_add+minute_add+"** for being your "+warn.getDescription()+". Warning **"+(warning_id+1)+"**/**"+max_warning+"**\nPlease, refrain from rejoining the server, since it will result in consequences.\n"
										+ "On an important note, this is an automated reply. You'll receive no reply in any way.\n"
										+ (GuildIni.getMuteSendReason(guild_id) ? "Provided reason: **"+reason+"**" : "")).queue();
							});
							//run RoleTimer for automatic unmute
							new Thread(new RoleTimer(e, mute_time, log_channel, mute_id, assignedRole, hour_add, and_add, minute_add, (warning_id+1), max_warning, issuer, reason)).start();
							logger.debug("{} got muted in guild {}", e.getUser().getId(), e.getGuild().getId());
						}
						//ban or perm mute if the current warning exceeded the max allowed warning
						else if((warning_id+1) > max_warning) {
							//execute this block if perm mute is disabled
							if(!GuildIni.getOverrideBan(guild_id)) {
								if(e.getGuild().getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
									//send a private message to the user
									PrivateChannel pc = e.getUser().openPrivateChannel().complete();
									pc.sendMessage("You have been banned from "+e.getGuild().getName()+", since you have exceeded the max amount of allowed mutes on this server. Thank you for your understanding.\n"
											+ "On an important note, this is an automated reply. You'll receive no reply in any way.\n"
											+ (GuildIni.getBanSendReason(guild_id) ? "Provided reason: **"+reason+"**" : "")).complete();
									pc.close();
									//ban the user
									e.getGuild().ban(e.getMember(), 0).reason("User has been muted after reaching the limit of max allowed mutes!").queue();
									logger.debug("{} got banned in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
								}
								else {
									if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Permission missing!").setDescription("This user couldn't be banned after reaching the limit of allowed mutes because the BAN MEMBERS permission is missing!").build()).queue();
									logger.warn("BAN MEMBERS permission missing to ban a user in guild {}!", e.getGuild().getId());
								}
							}
							else {
								//mark the user as permanently muted
								if(Azrael.SQLInsertData(user_id, guild_id, warning_id, 1, timestamp, null, true, false) == 0) {
									logger.error("muted user {} couldn't be inserted into Azrael.bancollect for guild {}", e.getUser().getId(), e.getGuild().getName());
									if(log_channel != null)e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. Muted user couldn't be inserted into Azrael.bancollect").queue();
								}
								//send a private message
								e.getUser().openPrivateChannel().queue(channel -> {
									channel.sendMessage("You have been muted without expiration from "+e.getGuild().getName()+". Rejoining the server will reapply the mute.\n"
											+ "On an important note, this is an automated reply. You'll receive no reply in any way.\n"
											+ (GuildIni.getMuteSendReason(guild_id) ? "Provided reason: **"+reason+"**" : "")).queue();
								});
								//execute RoleTimer
								new Thread(new RoleTimer(e, log_channel, mute_id, issuer, reason)).start();
							}
						}
					}
				}
				//enter the lowest block if the bot is not allowed to interact with this user
				else {
					var timestamp = new Timestamp(System.currentTimeMillis());
					//clear mute from the db in case it got updated with a command
					Azrael.SQLUpdateUnmute(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), timestamp, timestamp, false, false);
					//check if the bot has the manage roles permission before removing the mute role again
					if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
						//remove the mute role and send a message
						e.getGuild().removeRoleFromMember(e.getMember(), e.getGuild().getRoleById(mute_id)).queue();
						if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message2.setDescription("The mute role has been set on someone with higher privileges. Mute role removed!").build()).queue();
						logger.warn("{} received a mute role that has been instantly removed in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
					}
					else {
						if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Permission missing!").setDescription("Applied mute role couldn't be removed because the MANAGE ROLES permission is missing!").build()).queue();
						logger.warn("MANAGE ROLES permission missing to retract the mute role in guild {}!", e.getGuild().getId());
					}
				}
				if(unmute_time - System.currentTimeMillis() < 0) {
					Azrael.SQLInsertActionLog("MEMBER_MUTE_ADD", user_id, guild_id, "User Muted");
				}
			}
		}).start();
	}
	
	private boolean removeRoles(GuildMemberRoleAddEvent e, long mute_id) {
		//check that the bot has the manage roles permission before removing roles
		if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
			//remove all roles on mute role reassign except for the mute role itself
			for(Role role : e.getMember().getRoles()) {
				if(role.getIdLong() != mute_id) {
					e.getGuild().removeRoleFromMember(e.getMember(), role).queue();
				}
			}
		}
		else {
			logger.warn("MANAGE ROLES permission required to remove all roles from a user in guild {}!", e.getGuild().getId());
			return false;
		}
		return true;
	}
}