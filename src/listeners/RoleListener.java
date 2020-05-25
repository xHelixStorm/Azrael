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
import enums.GoogleEvent;
import enums.Translation;
import fileManagement.GuildIni;
import google.GoogleUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.RankingSystem;
import sql.DiscordRoles;
import sql.Azrael;
import threads.RoleTimer;
import util.STATIC;

public class RoleListener extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(RoleListener.class);
	
	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent e) {
		new Thread(() -> {
			long user_id = e.getMember().getUser().getIdLong();
			long guild_id = e.getMember().getGuild().getIdLong();
			String user_name = e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator();
			long mute_time;
			double unmute;
			
			//verify that the user is muted currently
			if(UserPrivs.isUserMuted(e.getMember())) {
				EmbedBuilder message = new EmbedBuilder().setColor(Color.RED).setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl()).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_MUTED_TITLE));
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
					Hashes.clearTempCache("mute_time_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
					//set mute status to true
					if(Azrael.SQLUpdateMuted(user_id, guild_id, true) == 0) {
						logger.error("Mute information of {} couldn't be updated in Azrael.bancollect in guild {}", user_id, e.getGuild().getId());
						if(log_channel != null)e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.JOIN_ERR_7).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id)).build()).queue();
					}
					//remove all roles, except the mute role when the muted user has more than 1 assigned role
					if(e.getMember().getRoles().size() > 1 && !removeRoles(e, mute_id) && log_channel != null)
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_ERROR)).setDescription(Permission.MANAGE_ROLES.getName()+STATIC.getTranslation2(e.getGuild(), Translation.ROLE_ROLES_REMOVE_ERR)).build()).queue();
					if(!warnedUser.getGuildLeft()) {
						Timestamp timestamp = new Timestamp(System.currentTimeMillis());
						if(log_channel != null) {
							e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_AGAIN_MUTED_1).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id)).build()).queue();
						}
						Azrael.SQLInsertActionLog("MEMBER_PERM_MUTE_READD", user_id, guild_id, "Permanent Mute role reassigned");
						if(GuildIni.getGoogleFunctionalitiesEnabled(guild_id) && GuildIni.getGoogleSpreadsheetsEnabled(guild_id)) {
							Object [] object = getReporterFromAuditLog(e);
							var reporter = e.getGuild().getMemberById((long)object[0]);
							String reason = (String)object[1];
							final String NA = STATIC.getTranslation2(e.getGuild(), Translation.NOT_AVAILABLE);
							String reporter_name = NA;
							String reporter_username = NA;
							if(reporter != null) {
								reporter_name = reporter.getUser().getName()+"#"+reporter.getUser().getDiscriminator();
								reporter_username = reporter.getEffectiveName();
							}
							GoogleUtils.handleSpreadsheetRequest(e.getGuild(), e.getMember().getUser().getId(), timestamp, user_name, e.getMember().getEffectiveName(), reporter_name, reporter_username, reason, "", ""+warnedUser.getWarningID(), "MUTE_READDED", warnedUser.getUnmute(), null, null, null, null, GoogleEvent.MUTE_READD.id, log_channel);
						}
					}
					else {
						Azrael.SQLUpdateGuildLeft(user_id, guild_id, false);
						if(log_channel != null) {
							e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_AGAIN_MUTED_3).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id)).build()).queue();
						}
					}
				}
				//enter if a mute timer has been defined but the user itself is not yet marked as muted on the table (e.g. manually removing and adding the mute role)
				else if(unmute_time - System.currentTimeMillis() > 0 && !warnedUser.getMuted()) {
					Hashes.clearTempCache("mute_time_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
					//set mute status to true
					if(Azrael.SQLUpdateMuted(user_id, guild_id, true) == 0) {
						logger.error("Mute information of {} couldn't be updated in Azrael.bancollect in guild {}", user_id, e.getGuild().getId());
						if(log_channel != null)e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.JOIN_ERR_7)).build()).queue();
					}
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					if(log_channel != null) {
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_AGAIN_MUTED_2).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id)).build()).queue();
					}
					//remove all roles, except the mute role when the muted user has more than 1 assigned role
					if(e.getMember().getRoles().size() > 1 && !removeRoles(e, mute_id) && log_channel != null)
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(Permission.MANAGE_ROLES.getName()+STATIC.getTranslation2(e.getGuild(), Translation.ROLE_ROLES_REMOVE_ERR)).build()).queue();
					Azrael.SQLInsertActionLog("MEMBER_MUTE_READD", user_id, guild_id, "Mute role reassigned");
					//Run google service, if enabled
					if(GuildIni.getGoogleFunctionalitiesEnabled(guild_id) && GuildIni.getGoogleSpreadsheetsEnabled(guild_id)) {
						Object [] object = getReporterFromAuditLog(e);
						var reporter = e.getGuild().getMemberById((long)object[0]);
						String reason = (String)object[1];
						final String NA = STATIC.getTranslation2(e.getGuild(), Translation.NOT_AVAILABLE);
						String reporter_name = NA;
						String reporter_username = NA;
						if(reporter != null) {
							reporter_name = reporter.getUser().getName()+"#"+reporter.getUser().getDiscriminator();
							reporter_username = reporter.getEffectiveName();
						}
						GoogleUtils.handleSpreadsheetRequest(e.getGuild(), e.getMember().getUser().getId(), timestamp, user_name, e.getMember().getEffectiveName(), reporter_name, reporter_username, reason, "", ""+warnedUser.getWarningID(), "MUTE_READDED", warnedUser.getUnmute(), null, null, null, null, GoogleEvent.MUTE_READD.id, log_channel);
					}
				}
				//enter in this block, if the user has been already muted but rejoined the server before the time elapsed
				else if(unmute_time - System.currentTimeMillis() > 0 && warnedUser.getMuted() && warnedUser.getGuildLeft()) {
					//mark the user as has rejoined the server
					Azrael.SQLUpdateGuildLeft(user_id, guild_id, false);
					if(log_channel != null) {
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_AGAIN_MUTED_4).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id)).build()).queue();
					}
				}
				//for manual mutes without command and which isn't permanent and for users that can be interacted with
				else if(e.getGuild().getSelfMember().canInteract(e.getMember())) {
					Object [] object = getReporterFromAuditLog(e);
					long from_user = (long)object[0];
					
					//remove all roles, except the mute role when the muted user has more than 1 assigned role
					if(e.getMember().getRoles().size() > 1 && !removeRoles(e, mute_id) && log_channel != null)
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(Permission.MANAGE_ROLES.getName()+STATIC.getTranslation2(e.getGuild(), Translation.ROLE_ROLES_REMOVE_ERR)).build()).queue();
					
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
						var reporter = e.getGuild().getMemberById(cache.getAdditionalInfo2());
						var issuer = reporter.getAsMention();
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
							channel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_MUTE_DM).replaceFirst("\\{\\}", e.getGuild().getName().replace("{}", hour_add+and_add+minute_add))
									+ (GuildIni.getMuteSendReason(guild_id) ? STATIC.getTranslation2(e.getGuild(), Translation.USER_BAN_REASON)+reason : "")).queue(success -> {
										//success callback not required
										channel.close().queue();
									}, error -> {
										if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.ORANGE).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_DM_LOCKED)).build()).queue();
										channel.close().queue();
									});
						});
						//unmute after a specific amount of time
						new Thread(new RoleTimer(e, mute_time, log_channel, mute_id, assignedRole, hour_add, and_add, minute_add, 0, 0, issuer, reason)).start();
						logger.debug("{} got muted in guild {}", e.getUser().getId(), e.getGuild().getId());
						
						//Run google service, if enabled
						if(GuildIni.getGoogleFunctionalitiesEnabled(guild_id) && GuildIni.getGoogleSpreadsheetsEnabled(guild_id)) {
							GoogleUtils.handleSpreadsheetRequest(e.getGuild(), e.getMember().getUser().getId(), new Timestamp(time), user_name, e.getMember().getEffectiveName(), reporter.getUser().getName()+"#"+reporter.getUser().getDiscriminator(), reporter.getEffectiveName(), reason, hour_add+minute_add+and_add, ""+(warning_id+1), "MUTED", new Timestamp(time+mute_time), null, null, null, null, GoogleEvent.MUTE.id, log_channel);
						}
					}
					//execute this block if a regular mute has been applied
					else {
						//check if the cache contains a reason and a name of who applied the mute
						var cache = Hashes.getTempCache("mute_time_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
						var reporter = (cache != null ? e.getGuild().getMemberById(cache.getAdditionalInfo()) : (from_user != 0 ? e.getGuild().getMemberById(from_user) : null));
						var issuer = (reporter != null ? reporter.getAsMention() : STATIC.getTranslation2(e.getGuild(), Translation.NOT_AVAILABLE));
						var reason = (cache != null ? cache.getAdditionalInfo2() : (String)object[1]);
						Hashes.clearTempCache("mute_time_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
						//retrieve current warning and the time to mute
						Warning warn = Azrael.SQLgetWarning(e.getGuild().getIdLong(), (warning_id+1));
						mute_time = (long) warn.getTimer();
						unmute = warn.getTimer();
						//calculate the mute time in a user friendly, visible form
						long hours = (long) (unmute/1000/60/60);
						long minutes = (long) (unmute/1000/60%60);
						String hour_add = hours != 0 ? hours+STATIC.getTranslation2(e.getGuild(), Translation.ROLE_HOURS) : "";
						String minute_add = minutes != 0 ? minutes+STATIC.getTranslation2(e.getGuild(), Translation.ROLE_MINUTES) : "";
						String and_add = minutes != 0 && hours != 0 ? STATIC.getTranslation2(e.getGuild(), Translation.ROLE_AND) : "";
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
								channel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_MUTE_DM_2).replaceFirst("\\{\\}", e.getGuild().getName()).replaceFirst("\\{\\}", hour_add+and_add+minute_add).replace("{}", "**"+(warning_id+1)+"**/**"+max_warning+"**")
										+ (GuildIni.getMuteSendReason(guild_id) ? STATIC.getTranslation2(e.getGuild(), Translation.USER_BAN_REASON)+reason : "")).queue(success -> {
											//success callback not required
											channel.close().queue();
										}, error -> {
											if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.ORANGE).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_DM_LOCKED)).build()).queue();
											channel.close().queue();
										});
							});
							//run RoleTimer for automatic unmute
							new Thread(new RoleTimer(e, mute_time, log_channel, mute_id, assignedRole, hour_add, and_add, minute_add, (warning_id+1), max_warning, issuer, reason)).start();
							logger.debug("{} got muted in guild {}", e.getUser().getId(), e.getGuild().getId());
							
							//Run google service, if enabled
							if(GuildIni.getGoogleFunctionalitiesEnabled(guild_id) && GuildIni.getGoogleSpreadsheetsEnabled(guild_id)) {
								GoogleUtils.handleSpreadsheetRequest(e.getGuild(), e.getMember().getUser().getId(), timestamp, user_name, e.getMember().getEffectiveName(), reporter.getUser().getName()+"#"+reporter.getUser().getDiscriminator(), reporter.getEffectiveName(), reason, hour_add+minute_add+and_add, ""+(warning_id+1), "MUTED", unmute_timestamp, null, null, null, null, GoogleEvent.MUTE.id, log_channel);
							}
						}
						//ban or perm mute if the current warning exceeded the max allowed warning
						else if((warning_id+1) > max_warning) {
							//execute this block if perm mute is disabled
							if(!GuildIni.getOverrideBan(guild_id)) {
								if(e.getGuild().getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
									//send a private message to the user
									e.getUser().openPrivateChannel().queue(channel -> {
										channel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_BAN_DM)
												+ (GuildIni.getBanSendReason(guild_id) ? STATIC.getTranslation2(e.getGuild(), Translation.USER_BAN_REASON)+reason : "")).queue(success -> {
													//ban the user
													e.getGuild().ban(e.getMember(), 0).reason(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_BAN_REASON)).queue();
													logger.debug("{} got banned in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
													channel.close().queue();
												}, error -> {
													if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.ORANGE).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_BAN_DM_LOCKED)).build()).queue();
													//ban the user
													e.getGuild().ban(e.getMember(), 0).reason(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_BAN_REASON)).queue();
													logger.debug("{} got banned in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
													channel.close().queue();
												});
									});
								}
								else {
									if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_BAN_PERMISSION_ERR).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id)+Permission.BAN_MEMBERS.getName()).build()).queue();
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
									channel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_MUTE_DM_3).replace("{}", e.getGuild().getName())
											+ (GuildIni.getMuteSendReason(guild_id) ? STATIC.getTranslation2(e.getGuild(), Translation.USER_BAN_REASON)+reason : "")).queue(success -> {
												//no callback required
												channel.close().queue();
											}, error -> {
												if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.ORANGE).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_DM_LOCKED)).build()).queue();
												channel.close().queue();
											});
								});
								//execute RoleTimer
								new Thread(new RoleTimer(e, log_channel, mute_id, issuer, reason)).start();
								
								//Run google service, if enabled
								if(GuildIni.getGoogleFunctionalitiesEnabled(guild_id) && GuildIni.getGoogleSpreadsheetsEnabled(guild_id)) {
									GoogleUtils.handleSpreadsheetRequest(e.getGuild(), e.getMember().getUser().getId(), timestamp, user_name, e.getMember().getEffectiveName(), reporter.getUser().getName()+"#"+reporter.getUser().getDiscriminator(), reporter.getEffectiveName(), reason, "PERMANENT", ""+(warning_id+1), "MUTED", unmute_timestamp, null, null, null, null, GoogleEvent.MUTE.id, log_channel);
								}
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
						if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.GREEN).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_RETRACTED_TITLE)).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_MUTE_REMOVED)).build()).queue();
						logger.warn("{} received a mute role that has been instantly removed in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
					}
					else {
						if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_MUTE_REMOVE_ERR)+Permission.MANAGE_ROLES.getName()).build()).queue();
						logger.warn("MANAGE ROLES permission missing to retract the mute role in guild {}!", e.getGuild().getId());
					}
				}
				if(unmute_time - System.currentTimeMillis() < 0) {
					Azrael.SQLInsertActionLog("MEMBER_MUTE_ADD", user_id, guild_id, "User Muted");
				}
			}
		}).start();
	}
	
	private Object [] getReporterFromAuditLog(GuildMemberRoleAddEvent e) {
		Object [] object = {0, STATIC.getTranslation2(e.getGuild(), Translation.DEFAULT_REASON)};
		//check if the bot has able to view the audit logs
		if(e.getGuild().getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
			var roleLog = e.getGuild().retrieveAuditLogs();
			//iterate through the log
			for(final var entry : roleLog) {
				//retrieve the first log about a role update
				if(entry.getType() == ActionType.MEMBER_ROLE_UPDATE) {
					//retrieve the user who applied the mute role and reason
					object[0] = entry.getUser().getIdLong();
					if(entry.getReason() != null) object[1] = entry.getReason();
					return object;
				}
			}
		}
		return object;
	}
	
	private boolean removeRoles(GuildMemberRoleAddEvent e, long mute_id) {
		//check that the bot has the manage roles permission before removing roles
		if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
			//remove all roles on mute role reassign except for the mute role itself
			for(Role role : e.getMember().getRoles()) {
				if(role.getIdLong() != mute_id) {
					e.getGuild().removeRoleFromMember(e.getMember(), role).queue(
						//don't remove the role, if it can't be removed. Like booster role
						success -> logger.info("{} role got removed from {} in guild {}", role.getId(), e.getMember().getUser().getId(), e.getGuild().getId()),
						error -> logger.info("{} role could not be removed from {} in guild {}. It's a persistant role!", role.getId(), e.getMember().getUser().getId(), e.getGuild().getId())
					);
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