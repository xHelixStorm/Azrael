package listeners;

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
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.RankingSystem;
import sql.DiscordRoles;
import sql.Azrael;
import threads.RoleTimer;

public class RoleListener extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(RoleListener.class);
	
	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent e) {
		new Thread(() -> {
			EmbedBuilder message = new EmbedBuilder().setColor(Color.RED).setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl()).setTitle("A user has been muted!");
			EmbedBuilder message2 = new EmbedBuilder().setColor(Color.GREEN).setTitle("Mute Retracted!");
			
			long user_id = e.getMember().getUser().getIdLong();
			long guild_id = e.getMember().getGuild().getIdLong();
			String user_name = e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator();
			long mute_time;
			double unmute;
			boolean customTimeMute = false;
			
			if(UserPrivs.isUserMuted(e.getMember().getUser(), e.getGuild().getIdLong())) {
				var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
				var mute_id = DiscordRoles.SQLgetRoles(guild_id).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null).getRole_ID();
				Bancollect warnedUser = Azrael.SQLgetData(user_id, guild_id);
				var permMute = false;
				long unmute_time = 0;
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
				if(permMute) {
					if(Azrael.SQLUpdateMuted(user_id, guild_id, true) == 0) {
						logger.error("Mute information of {} couldn't be updated in Azrael.bancollect in guild {}", user_id, e.getGuild().getId());
						if(log_channel != null)e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. The mute state couldn't be updated in table Azrael.bancollect").queue();
					}
					if(log_channel != null) {
						Timestamp timestamp = new Timestamp(System.currentTimeMillis());
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+ "** with the ID number **"+e.getMember().getUser().getId()+"** got his mute role reassigned and reactivated the unlimited mute. Reason may be due to manually reassigning the mute role or rejoining the server!").build()).queue();
					}
					removeRoles(e, mute_id);
				}
				else if(unmute_time - System.currentTimeMillis() > 0 && !warnedUser.getMuted()) {
					if(Azrael.SQLUpdateMuted(user_id, guild_id, true) == 0) {
						logger.error("Mute information of {} couldn't be updated in Azrael.bancollect in guild {}", user_id, e.getGuild().getId());
						if(log_channel != null)e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. The mute state couldn't be updated in table Azrael.bancollect").queue();
					}
					if(log_channel != null) {
						Timestamp timestamp = new Timestamp(System.currentTimeMillis());
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+ "** with the ID number **"+e.getMember().getUser().getId()+"** got his mute role reassigned before the mute time elapsed! Reason may be due to manually reassigning the mute role!").build()).queue();
					}
					removeRoles(e, mute_id);
				}
				else if(unmute_time - System.currentTimeMillis() > 0 && warnedUser.getMuted() && warnedUser.getGuildLeft()) {
					Azrael.SQLUpdateGuildLeft(user_id, guild_id, false);
					if(log_channel != null) {
						Timestamp timestamp = new Timestamp(System.currentTimeMillis());
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+ "** with the ID number **"+e.getMember().getUser().getId()+"** got his mute role reassigned before the mute time elapsed! Reason may be due to leaving and rejoining the server!").build()).queue();
					}
				}
				else {
					long from_user = 0;
					if(e.getGuild().getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
						var roleLog = e.getGuild().retrieveAuditLogs();
						for(final var entry : roleLog) {
							if(entry.getType() == ActionType.MEMBER_ROLE_UPDATE) {
								from_user = entry.getUser().getIdLong();
							}
							break;
						}
					}
					
					if(warnedUser.getCustomTime())
						customTimeMute = true;
					try {
						for(Role r : e.getMember().getRoles()) {
							if(r.getIdLong() != mute_id) {
								e.getGuild().removeRoleFromMember(e.getMember(), r).queue();
							}
						}
						
						long time = System.currentTimeMillis();
						int warning_id = warnedUser.getWarningID();
						long assignedRole = 0;
						Rank user_details = RankingSystem.SQLgetWholeRankView(user_id, guild_id);
						if(user_details != null) {
							assignedRole = user_details.getCurrentRole();
						}
						
						if(warnedUser.getCustomTime()) {
							var cache = Hashes.getTempCache("mute_time_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
							mute_time = Long.parseLong(cache.getAdditionalInfo());
							var issuer = cache.getAdditionalInfo2();
							var reason = cache.getAdditionalInfo3();
							Hashes.clearTempCache("mute_time_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
							long hours = (mute_time/1000/60/60);
							long minutes = (mute_time/1000/60%60);
							String hour_add = hours != 0 ? hours+" hours" : "";
							String minute_add = minutes != 0 ? minutes+" minutes" : "";
							String and_add = minutes != 0 && hours != 0 ? " and " : "";
							
							PrivateChannel pc = e.getUser().openPrivateChannel().complete();
							pc.sendMessage("You have been muted on "+e.getGuild().getName()+". Your current mute will last for **"+hour_add+and_add+minute_add+"** . Except for the first mute, your warning counter won't increase.\nPlease, refrain from rejoining the server, since it will result in consequences.\n"
									+ "On an important note, this is an automated reply. You'll receive no reply in any way.\n"
									+ (GuildIni.getMuteSendReason(guild_id) ? "Provided reason: **"+reason+"**" : "")).queue();
							pc.close();
							new Thread(new RoleTimer(e, mute_time, log_channel, mute_id, assignedRole, hour_add, and_add, minute_add, 0, 0, issuer, reason)).start();
							logger.debug("{} got muted in guild {}", e.getUser().getId(), e.getGuild().getId());
						}
						else {
							var cache = Hashes.getTempCache("mute_time_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
							var issuer = (cache != null ? cache.getAdditionalInfo() : (from_user != 0 ? e.getGuild().getMemberById(from_user).getAsMention() : "NaN"));
							var reason = (cache != null ? cache.getAdditionalInfo2() : "No reason has been provided!");
							Warning warn = Azrael.SQLgetWarning(e.getGuild().getIdLong(), (warning_id+1));
							mute_time = (long) warn.getTimer();
							unmute = warn.getTimer();
							long hours = (long) (unmute/1000/60/60);
							long minutes = (long) (unmute/1000/60%60);
							String hour_add = hours != 0 ? hours+" hours" : "";
							String minute_add = minutes != 0 ? minutes+" minutes" : "";
							String and_add = minutes != 0 && hours != 0 ? " and " : "";
							Timestamp timestamp = new Timestamp(time);
							Timestamp unmute_timestamp = new Timestamp(time+mute_time);
							
							int max_warning = Azrael.SQLgetMaxWarning(e.getGuild().getIdLong());
							if((warning_id+1) <= max_warning) {
								if(Azrael.SQLInsertData(user_id, guild_id, (warning_id+1), 1, timestamp, unmute_timestamp, true, false) == 0) {
									logger.error("muted user {} couldn't be inserted into Azrael.bancollect for guild {}", e.getUser().getId(), e.getGuild().getName());
									if(log_channel != null)e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. Muted user couldn't be inserted into Azrael.bancollect").queue();
								}
								PrivateChannel pc = e.getUser().openPrivateChannel().complete();
								pc.sendMessage("You have been muted on "+e.getGuild().getName()+". Your current mute will last for **"+hour_add+and_add+minute_add+"** for being your "+warn.getDescription()+". Warning **"+(warning_id+1)+"**/**"+max_warning+"**\nPlease, refrain from rejoining the server, since it will result in consequences.\n"
										+ "On an important note, this is an automated reply. You'll receive no reply in any way.\n"
										+ (GuildIni.getMuteSendReason(guild_id) ? "Provided reason: **"+reason+"**" : "")).queue();
								pc.close();
								new Thread(new RoleTimer(e, mute_time, log_channel, mute_id, assignedRole, hour_add, and_add, minute_add, (warning_id+1), max_warning, issuer, reason)).start();
								logger.debug("{} got muted in guild {}", e.getUser().getId(), e.getGuild().getId());
							}
							else if((warning_id+1) > max_warning) {
								if(!GuildIni.getOverrideBan(guild_id)) {
									PrivateChannel pc = e.getUser().openPrivateChannel().complete();
									pc.sendMessage("You have been banned from "+e.getGuild().getName()+", since you have exceeded the max amount of allowed mutes on this server. Thank you for your understanding.\n"
											+ "On an important note, this is an automated reply. You'll receive no reply in any way.\n"
											+ (GuildIni.getBanSendReason(guild_id) ? "Provided reason: **"+reason+"**" : "")).complete();
									pc.close();
									logger.debug("{} got banned in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
									e.getJDA().getGuildById(guild_id).ban(e.getMember(), 0).reason("User has been muted after reaching the limit of max allowed mutes!").complete();
								}
								else {
									if(Azrael.SQLInsertData(user_id, guild_id, warning_id, 1, timestamp, null, true, false) == 0) {
										logger.error("muted user {} couldn't be inserted into Azrael.bancollect for guild {}", e.getUser().getId(), e.getGuild().getName());
										if(log_channel != null)e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. Muted user couldn't be inserted into Azrael.bancollect").queue();
									}
									e.getUser().openPrivateChannel().complete().sendMessage("You have been muted without expiration from "+e.getGuild().getName()+". Rejoining the server will reapply the mute.\n"
												+ "On an important note, this is an automated reply. You'll receive no reply in any way.\n"
												+ (GuildIni.getMuteSendReason(guild_id) ? "Provided reason: **"+reason+"**" : "")).queue();
									new Thread(new RoleTimer(e, log_channel, mute_id, issuer, reason)).start();
								}
							}
						}
					} catch (HierarchyException hye) {
						if(customTimeMute)
							Azrael.SQLUpdateUnmute(user_id, guild_id, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), false, false);
						e.getJDA().getGuildById(guild_id).removeRoleFromMember(e.getMember(), e.getGuild().getRoleById(mute_id)).queue();
						logger.warn("{} received a mute role that has been instantly removed", e.getMember().getUser().getId(), hye);
						if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message2.setDescription("The mute role has been set on someone with higher privileges. Mute role removed!").build()).queue();
						
					}
				}
				if(unmute_time - System.currentTimeMillis() < 0) {
					Azrael.SQLInsertActionLog("MEMBER_MUTE_ADD", user_id, guild_id, "User Muted");
				}
			}
		}).start();
	}
	
	private void removeRoles(GuildMemberRoleAddEvent e, long mute_id) {
		//remove all roles on mute role reassign
		for(Role role : e.getMember().getRoles()) {
			if(role.getIdLong() != mute_id) {
				e.getGuild().removeRoleFromMember(e.getMember(), role).queue();
			}
		}
	}
}