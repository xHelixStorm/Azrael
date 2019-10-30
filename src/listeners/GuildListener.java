package listeners;

/**
 * This class gets executed for all users that are joining the guild where the bot is present!
 * 
 * All joined users will get inserted into the main users table and into the ranking
 * system users table, if the ranking system is enabled. Additionally, all joined users
 * will be checked, if they were previously muted and then left the server in question.
 * 
 * If there are any residual actions that need to be done such as mute or ban on join,
 * it will be handled here as well.
 * 
 * For last, this class also includes a name check by comparing all registered staff names
 * first and then by comparing registered words which are not allowed for the current server.
 */

import java.awt.Color;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Bancollect;
import constructors.Cache;
import constructors.Guilds;
import constructors.Rank;
import core.Hashes;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.RankingSystem;
import sql.DiscordRoles;
import sql.Azrael;

public class GuildListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(GuildListener.class);
	private final static EmbedBuilder message = new EmbedBuilder().setColor(Color.GREEN).setTitle("User joined!");
	private final static EmbedBuilder nick_assign = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(IniFileReader.getCaughtThumbnail()).setTitle("Not allowed name found!");
	private final static EmbedBuilder err = new EmbedBuilder().setColor(Color.RED).setTitle("An error occurred!");
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent e) {
		new Thread(() -> {
			logger.debug("{} has joined the guild {}", e.getUser().getId(), e.getGuild().getId());
			long user_id = e.getMember().getUser().getIdLong();
			String user_name = e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator();
			long guild_id = e.getGuild().getIdLong();
			long currentTime = System.currentTimeMillis();;
			boolean badName = false;
			long unmute = 0;
			boolean muted;
			
			//retrieve the log channel
			var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
			//insert or update the name of the user into Azrael.users
			if(Azrael.SQLInsertUser(user_id, user_name, e.getMember().getUser().getEffectiveAvatarUrl(), e.getMember().getTimeJoined().format(DateTimeFormatter.ISO_LOCAL_DATE)) == 0) {
				if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(err.setDescription("The user **"+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+"** with the ID number **"+user_id+"** couldn't be inserted into **Azrael.users** table").build()).queue();
				logger.error("User {} couldn't be inserted into the table Azrael.users for guild {}", e.getMember().getUser().getId(), e.getGuild().getName());
			}
			//retrieve current guild settings
			Guilds guild_settings = RankingSystem.SQLgetGuild(guild_id);
			//if the ranking system is enabled, insert the joined user into RankingSystem.users and RankingSystem.user_details
			if(guild_settings.getRankingState() == true) {
				if(RankingSystem.SQLInsertUser(user_id, guild_id, e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), guild_settings.getLevelID(), guild_settings.getRankID(), guild_settings.getProfileID(), guild_settings.getIconID()) > 0) {
					RankingSystem.SQLInsertUserDetails(user_id, guild_id, 0, 0, 50000, 0);
				}
				else {
					if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(err.setDescription("The user **"+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+"** with the ID number **"+user_id+"** couldn't be inserted into **RankingSystem.users** table").build()).queue();
					logger.error("Failed to insert joined user into RankingSystem.users");
					RankingSystem.SQLInsertActionLog("high", user_id, guild_id, "User wasn't inserted into user table", "This user couldn't be inserted into the user table. Please verify the name of this user and eventually insert it manually into the table!");
				}
			}
			
			//check if the currently joined user is marked as muted
			Bancollect warnedUser = Azrael.SQLgetData(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
			muted = warnedUser.getMuted();
			//print join message, if the user is not muted and if the printing of join messages is allowed
			if(GuildIni.getJoinMessage(guild_id)) {
				if(log_channel != null && muted == false) {e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription(":warning: The user **" + user_name + "** with the ID Number **" + user_id + "** joined **" + e.getGuild().getName() + "**").build()).queue();}
			}
			
			//retrieve the unmute time if available and if not, check if the user is marked as muted and if muted, reassign the mute role regardless
			boolean permMute = false;
			if(warnedUser.getUnmute() != null) {
				unmute = warnedUser.getUnmute().getTime();
			}
			else if(warnedUser.getWarningID() != 0 && warnedUser.getMuted()) {
				permMute = true;
			}
			
			//mute the user if the time isn't elapsed and if the user is still marked as muted or if the user should receive back the perm mute
			if(permMute || ((unmute - currentTime) > 0 && muted)) {
				//verify that the bot has the manage roles permission before muting again
				if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
					var mute_role = DiscordRoles.SQLgetRoles(guild_id).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null);
					if(mute_role != null)
						e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(mute_role.getRole_ID())).queue();
					else if(log_channel != null)
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(err.setDescription("Muted user couldn't receive the mute role on server join! Is a mute role registered?").build()).queue();
				}
				else {
					//throw an error and set the user to not muted and joined, if the permission is missing but is still muted
					Azrael.SQLUpdateMutedOnEnd(user_id, guild_id, false, false);
					Azrael.SQLUpdateGuildLeft(user_id, guild_id, false);
					if(log_channel != null)
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(err.setDescription("The joined user **"+user_name+"** with the id number **"+user_id+"** couldn't get muted because the MANAGE ROLES permission is mssing!").build()).queue();
					logger.warn("MANAGE ROLES permission missing to mute a user in guild {}!", e.getGuild().getId());
				}
			}
			else {
				//Remove the muted and guild left label from this user, if he joined the server after the mute time expired and is still marked as muted
				if(muted) {
					Azrael.SQLUpdateMutedOnEnd(user_id, guild_id, false, false);
					Azrael.SQLUpdateGuildLeft(user_id, guild_id, false);
				}
				//Assign an unlocked ranking role after verifying that the ranking system is enabled
				if(guild_settings.getRankingState()) {
					Rank user_details = RankingSystem.SQLgetWholeRankView(user_id, guild_id);
					if(user_details.getCurrentRole() != 0) {e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(user_details.getCurrentRole())).queue();}
				}
			}
			
			//check for residual tasks like mute or ban on server join
			var rejoinAction = Hashes.getRejoinTask(e.getGuild().getId()+"_"+e.getMember().getUser().getId());
			if(rejoinAction != null) {
				//mute the newly joined user depending if it was a regular or time defined mute
				if(rejoinAction.getType().equals("mute")) {
					//look up for a mute role
					var mute_role = DiscordRoles.SQLgetRoles(guild_id).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null);
					if(mute_role != null) {
						//check if the bot has the manage roles permission before continuing
						if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
							//this is a regular mute
							if(rejoinAction.getInfo().length() == 0) {
								Hashes.addTempCache("mute_time_gu"+guild_id+"us"+user_id, new Cache(rejoinAction.getInfo2(), rejoinAction.getReason()));
								e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(mute_role.getRole_ID())).queue();
								var mute_time = (long)Azrael.SQLgetWarning(guild_id, Azrael.SQLgetData(user_id, guild_id).getWarningID()+1).getTimer();
								Azrael.SQLInsertHistory(user_id, guild_id, "mute", (rejoinAction.getReason().length() > 0 ? rejoinAction.getReason() : "No reason has been provided!"), (mute_time/1000/60));
							}
							//this is a time defined mute
							else {
								var mute_time = Long.parseLong(rejoinAction.getInfo());
								Timestamp timestamp = new Timestamp(System.currentTimeMillis());
								Timestamp unmute_timestamp = new Timestamp(System.currentTimeMillis()+mute_time);
								Azrael.SQLInsertHistory(user_id, guild_id, "mute", (rejoinAction.getReason().length() > 0 ? rejoinAction.getReason() : "No reason has been provided!"), (mute_time/1000/60));
								if(Azrael.SQLgetData(user_id, guild_id).getWarningID() != 0) {
									if(Azrael.SQLUpdateUnmute(user_id, guild_id, timestamp, unmute_timestamp, true, true) == 0) {
										logger.error("The unmute timer couldn't be updated from user {} in guild {} for the table Azrael.bancollect", user_id, guild_id);
										if(log_channel != null)e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. The unmute time couldn't be updated on Azrael.bancollect").queue();
									}
									else {
										Hashes.addTempCache("mute_time_gu"+guild_id+"us"+user_id, new Cache(""+mute_time, rejoinAction.getInfo2(), rejoinAction.getReason()));
										e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(mute_role.getRole_ID())).queue();
									}
								}
								else {
									if(Azrael.SQLInsertData(user_id, guild_id, 1, 1, timestamp, unmute_timestamp, true, true) == 0) {
										logger.error("muted user {} couldn't be inserted into Azrael.bancollect for guild {}", user_id, guild_id);
										if(log_channel != null)e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. Muted user couldn't be inserted into Azrael.bancollect").queue();
									}
									else {
										Hashes.addTempCache("mute_time_gu"+guild_id+"us"+user_id, new Cache(""+mute_time, rejoinAction.getInfo2(), rejoinAction.getReason()));
										e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(mute_role.getRole_ID())).queue();
									}
								}
							}
						}
						else if(log_channel != null) {
							 e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(err.setDescription("The joined user **"+user_name+"** with the id number **"+user_id+"** couldn't be muted for missing the MANAGE ROLES permission!").build()).queue();
						}
					}
					else if(log_channel != null) {
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(err.setDescription("Mute reminder couldn't be applied on server join! Is a mute role registered?").build()).queue();
					}
					//remove the completed join task
					Hashes.removeRejoinTask(e.getGuild().getId()+"_"+e.getMember().getUser().getId());
				}
				//ban a joined user
				else if(rejoinAction.getType().equals("ban")) {
					//send a private message before banning
					e.getUser().openPrivateChannel().complete().sendMessage("You have been banned from "+e.getGuild().getName()+". Thank you for your understanding.\n"
							+ "On an important note, this is an automatic reply. You'll receive no reply in any way.\n"
							+ (GuildIni.getBanSendReason(e.getGuild().getIdLong()) ? "Provided reason: "+rejoinAction.getReason() : "")).complete();
					Hashes.addTempCache("ban_gu"+e.getGuild().getId()+"us"+user_id, new Cache(rejoinAction.getInfo2(), rejoinAction.getReason()));
					e.getGuild().ban(e.getMember(), 0).reason(rejoinAction.getReason()).queue();
					Azrael.SQLInsertHistory(user_id, guild_id, "ban", rejoinAction.getReason(), 0);
					Hashes.removeRejoinTask(e.getGuild().getId()+"_"+e.getMember().getUser().getId());
				}
			}
			else {
				String nickname = null;
				String lc_user_name = user_name.toLowerCase();
				//lookup if the user is using the same name as a registered staff member name
				final var name = Azrael.SQLgetStaffNames(guild_id).parallelStream().filter(f -> lc_user_name.matches(f+"#[0-9]{4}")).findAny().orElse(null);
				if(name != null) {
					nick_assign.setColor(Color.RED).setTitle("Impersonation attempt found!").setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl());
					//verify if the bot has the permission to manage nicknames
					if(e.getGuild().getSelfMember().hasPermission(Permission.NICKNAME_MANAGE)) {
						//retrieve a random nickname and assign to the user
						nickname = Azrael.SQLgetRandomName(e.getGuild().getIdLong());
						Hashes.addTempCache("nickname_add_gu"+guild_id+"us"+user_id, new Cache(600000));
						e.getGuild().modifyNickname(e.getMember(), nickname).queue();
						if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(nick_assign.setDescription("**"+user_name+"** joined this server and tried to impersonate a staff member. This nickname had been assigned to him/her: **"+nickname+"**").build()).queue();
						logger.info("Impersonation attempt found from {} in guild {}", user_id, guild_id);
					}
					else {
						if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(nick_assign.setDescription("**"+user_name+"** joined this server and tried to impersonate a staff member but no nickname could have been assigned becuase the MANAGE NICKNAMES permission is missing!").build()).queue();
						logger.warn("Lacking MANAGE NICKNAME permission in guild {}", e.getGuild().getId());
					}
					//set flag to signal that the current name is not allowed
					badName = true;
				}
				//execute only if the staff name search failed
				if(badName == false) {
					//look up the name filter for not allowed words
					final var word = Azrael.SQLgetNameFilter(guild_id).parallelStream().filter(f -> lc_user_name.contains(f.getName())).findAny().orElse(null);
					if(word != null) {
						if(!word.getKick()) {
							//verify if the bot has the permission to manage nicknames
							if(e.getGuild().getSelfMember().hasPermission(Permission.NICKNAME_MANAGE)) {
								//retrieve a random nickname and assign to the user
								nickname = Azrael.SQLgetRandomName(e.getGuild().getIdLong());
								Hashes.addTempCache("nickname_add_gu"+guild_id+"us"+user_id, new Cache(600000));
								e.getGuild().modifyNickname(e.getMember(), nickname).queue();
								if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(nick_assign.setDescription("**"+user_name+"** joined this server with an unproper name. This nickname had been assigned to him/her: **"+nickname+"**").build()).queue();
								logger.info("Improper name found from {} in guild {}", user_id, guild_id);
							}
							else {
								if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(nick_assign.setColor(Color.RED).setDescription("**"+user_name+"** joined this server with an unproper name but no nickname could have been assigned because the MANAGE NICKNAMES permission is missing!").build()).queue();
								logger.warn("Lacking MANAGE NICKNAME permission in guild {}", e.getGuild().getId());
							}
							//set flag to signal that the current name is not allowed
							badName = true;
						}
						else {
							//verify if the bot has the permission to kick users
							if(e.getGuild().getSelfMember().hasPermission(Permission.KICK_MEMBERS)) {
								//send a private message and then kick the user
								e.getMember().getUser().openPrivateChannel().complete().sendMessage("You have been automatically kicked from "+e.getJDA().getGuildById(guild_id).getName()+" for having the word **"+word.getName().toUpperCase()+"** in your name!").complete();
								e.getGuild().kick(e.getMember()).reason("User kicked for having "+word.getName().toUpperCase()+" inside his name").queue();
								Azrael.SQLInsertHistory(e.getUser().getIdLong(), guild_id, "kick", "Kicked for having an invalid word inside his name!", 0);
								nick_assign.setColor(Color.RED).setThumbnail(IniFileReader.getCaughtThumbnail()).setTitle("User kicked for having a not allowed name!");
								if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(nick_assign.setDescription("**"+user_name+"** joined this server with an unproper name. The user has been kicked automatically from the server due to this word: **"+word.getName().toUpperCase()+"**").build()).queue();
							}
							else {
								nick_assign.setColor(Color.RED).setTitle("User couldn't be kicked!");
								if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(nick_assign.setDescription("**"+user_name+"** joined this server with an unproper name that contains **"+word.getName().toUpperCase()+"** but couldn't get kicked because the permission KICK MEMBERS is missing!").build()).queue();
								logger.warn("Lacking KICK MEMBERS permission in guild {}", e.getGuild().getId());
							}
						}
					}
				}
				//update Azrael.nickname table with a new nickname or remove existent one, if nothing was found
				if(badName == false) {
					Azrael.SQLDeleteNickname(user_id, guild_id);
				}
				else {
					if(nickname != null && Azrael.SQLgetNickname(user_id, guild_id).length() > 0) {
						if(Azrael.SQLUpdateNickname(user_id, guild_id, nickname) == 0) {
							logger.error("User nickname of {} couldn't be updated in Azrael.nickname", user_id);
						}
						else {
							logger.debug("{} received the nickname {} in guild {}", user_id, nickname, guild_id);
							Azrael.SQLInsertActionLog("MEMBER_NICKNAME_UPDATE", user_id, guild_id, nickname);
						}
					}
					else if(nickname != null) {
						if(Azrael.SQLInsertNickname(user_id, guild_id, nickname) == 0) {
							logger.error("User nickname of {} couldn't be inserted into Azrael.nickname", user_id);
						}
						else {
							logger.debug("{} received the nickname {} in guild {}", user_id, nickname, guild_id);
							Azrael.SQLInsertActionLog("MEMBER_NICKNAME_UPDATE", user_id, guild_id, nickname);
						}
					}
				}
			}
			
			Azrael.SQLInsertActionLog("GUILD_MEMBER_JOIN", user_id, guild_id, user_name);
		}).start();
	}
}
