package listeners;

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
import enums.Channel;
import enums.GoogleEvent;
import enums.Translation;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import google.GoogleUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.RankingSystem;
import util.STATIC;
import sql.DiscordRoles;
import sql.Azrael;

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
 * @author xHelixStorm
 * 
 */

public class GuildListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(GuildListener.class);
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent e) {
		new Thread(() -> {
			final EmbedBuilder message = new EmbedBuilder().setColor(Color.GREEN).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.JOIN_TITLE));
			final EmbedBuilder nick_assign = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(IniFileReader.getCaughtThumbnail()).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.NAME_TITLE));
			final EmbedBuilder err = new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_ERROR));
			logger.debug("{} has joined the guild {}", e.getUser().getId(), e.getGuild().getId());
			long user_id = e.getMember().getUser().getIdLong();
			String user_name = e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator();
			long guild_id = e.getGuild().getIdLong();
			long currentTime = System.currentTimeMillis();;
			boolean badName = false;
			long unmute = 0;
			boolean muted;
			
			//insert or update the name of the user into Azrael.users
			if(Azrael.SQLInsertUser(user_id, user_name, STATIC.getLanguage2(e.getGuild()), e.getMember().getUser().getEffectiveAvatarUrl()) == 0) {
				STATIC.writeToRemoteChannel(e.getGuild(), err, STATIC.getTranslation2(e.getGuild(), Translation.JOIN_ERR).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id), Channel.LOG.getType());
				logger.error("User {} couldn't be inserted into the table Azrael.users for guild {}", e.getMember().getUser().getId(), e.getGuild().getName());
			}
			//insert or update the join_date
			final int insertJoinDateResult = Azrael.SQLInsertJoinDate(user_id, guild_id, e.getMember().getTimeJoined().format(DateTimeFormatter.ISO_LOCAL_DATE));
			if(insertJoinDateResult == 0) {
				//Update the join date, if no rows have been inserted with the insert statement and no error has been thrown
				if(Azrael.SQLUpdateJoinDate(user_id, guild_id, e.getMember().getTimeJoined().format(DateTimeFormatter.ISO_LOCAL_DATE)) == 0) {
					STATIC.writeToRemoteChannel(e.getGuild(), err, STATIC.getTranslation2(e.getGuild(), Translation.JOIN_ERR).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id), Channel.LOG.getType());
					logger.error("The join date of user {} couldn't be updated into the table Azrael.join_dates for guild {}", e.getMember().getUser().getId(), e.getGuild().getName());
				}
			}
			else if(insertJoinDateResult == -1) {
				STATIC.writeToRemoteChannel(e.getGuild(), err, STATIC.getTranslation2(e.getGuild(), Translation.JOIN_ERR).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id), Channel.LOG.getType());
				logger.error("The join date of user {} couldn't be inserted into the table Azrael.join_dates for guild {}", e.getMember().getUser().getId(), e.getGuild().getName());
			}
			//retrieve current guild settings
			Guilds guild_settings = RankingSystem.SQLgetGuild(guild_id);
			//if the ranking system is enabled, insert the joined user into RankingSystem.users and RankingSystem.user_details
			if(guild_settings.getRankingState() == true) {
				if(RankingSystem.SQLInsertUser(user_id, guild_id, e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), guild_settings.getLevelID(), guild_settings.getRankID(), guild_settings.getProfileID(), guild_settings.getIconID()) > 0) {
					RankingSystem.SQLInsertUserDetails(user_id, guild_id, 0, 0, guild_settings.getStartCurrency(), 0);
				}
				else {
					STATIC.writeToRemoteChannel(e.getGuild(), err, STATIC.getTranslation2(e.getGuild(), Translation.JOIN_ERR_3).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id), Channel.LOG.getType());
					logger.error("Failed to insert joined user into RankingSystem.users in guild {}", e.getGuild().getId());
					RankingSystem.SQLInsertActionLog("high", user_id, guild_id, "User wasn't inserted into user table", "This user couldn't be inserted into the user table. Please verify the name of this user and eventually insert it manually into the table!");
				}
			}
			
			//check if the currently joined user is marked as muted
			Bancollect warnedUser = Azrael.SQLgetData(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
			muted = warnedUser.getMuted();
			//print join message, if the user is not muted and if the printing of join messages is allowed
			if(GuildIni.getJoinMessage(guild_id)) {
				STATIC.writeToRemoteChannel(e.getGuild(), message.setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl()), STATIC.getTranslation2(e.getGuild(), Translation.JOIN_MESSAGE).replace("{}", user_name), Channel.LOG.getType());
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
					else
						STATIC.writeToRemoteChannel(e.getGuild(), err, STATIC.getTranslation2(e.getGuild(), Translation.JOIN_ERR_4).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id), Channel.LOG.getType());
				}
				else {
					//throw an error and set the user to not muted and joined, if the permission is missing but is still muted
					if(Azrael.SQLUpdateMutedOnEnd(user_id, guild_id, false, false) > 0) {
						if(Azrael.SQLUpdateGuildLeft(user_id, guild_id, false) == 0) {
							logger.error("Guild left state couldn't be update in Azrael.bancollect for user {} in guild {}", user_id, guild_id);
						}
					}
					else
						logger.error("Mute end state couldn't be update in Azrael.bancollect for user {} in guild {}", user_id, guild_id);
					STATIC.writeToRemoteChannel(e.getGuild(), err, STATIC.getTranslation2(e.getGuild(), Translation.JOIN_PERMISSION_ERR).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id)+Permission.MANAGE_ROLES.getName(), Channel.LOG.getType());
					logger.warn("MANAGE ROLES permission missing to mute a user in guild {}!", e.getGuild().getId());
				}
			}
			else {
				//Remove the muted and guild left label from this user, if he joined the server after the mute time expired and is still marked as muted
				if(muted) {
					if(Azrael.SQLUpdateMutedOnEnd(user_id, guild_id, false, false) > 0) {
						if(Azrael.SQLUpdateGuildLeft(user_id, guild_id, false) == 0) {
							logger.error("Guild left state couldn't be update in Azrael.bancollect for user {} in guild {}", user_id, guild_id);
						}
					}
					else
						logger.error("Mute end state couldn't be update in Azrael.bancollect for user {} in guild {}", user_id, guild_id);
				}
				//Assign an unlocked ranking role after verifying that the ranking system is enabled
				if(guild_settings.getRankingState()) {
					Rank user_details = RankingSystem.SQLgetWholeRankView(user_id, guild_id);
					if(user_details.getCurrentRole() != 0) {e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(user_details.getCurrentRole())).queue();}
				}
			}
			
			//check for residual tasks like mute or ban on server join
			var rejoinAction = Azrael.SQLgetRejoinTask(user_id, guild_id);
			if(rejoinAction != null && rejoinAction.getUserID() != 0) {
				//mute the newly joined user depending if it was a regular or time defined mute
				if(rejoinAction.getType().equals("mute")) {
					//look up for a mute role
					var mute_role = DiscordRoles.SQLgetRoles(guild_id).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null);
					if(mute_role != null) {
						//check if the bot has the manage roles permission before continuing
						if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
							//this is a regular mute
							if(rejoinAction.getTime().length() == 0) {
								Hashes.addTempCache("mute_time_gu"+guild_id+"us"+user_id, new Cache(rejoinAction.getReporter(), rejoinAction.getReason()));
								e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(mute_role.getRole_ID())).queue();
								var mute_time = (long)Azrael.SQLgetWarning(guild_id, Azrael.SQLgetData(user_id, guild_id).getWarningID()+1).getTimer();
								Azrael.SQLInsertHistory(user_id, guild_id, "mute", (rejoinAction.getReason().length() > 0 ? rejoinAction.getReason() : STATIC.getTranslation2(e.getGuild(), Translation.DEFAULT_REASON)), (mute_time/1000/60), "");
							}
							//this is a perm mute
							else if(rejoinAction.getTime().equals("perm")) {
								Hashes.addTempCache("mute_time_gu"+guild_id+"us"+user_id, new Cache(rejoinAction.getReporter(), rejoinAction.getReason()));
								var timestamp = new Timestamp(System.currentTimeMillis());
								if(Azrael.SQLInsertData(e.getUser().getIdLong(), e.getGuild().getIdLong(), Azrael.SQLgetMaxWarning(e.getGuild().getIdLong()), 1, timestamp, timestamp, false, false) == 0) {
									STATIC.writeToRemoteChannel(e.getGuild(), err, STATIC.getTranslation2(e.getGuild(), Translation.JOIN_ERR_5).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id), Channel.LOG.getType());
									logger.error("The perm mute flag for user {} in guild {} couldn't be inserted into Azrael.bancollect", user_id, guild_id);
								}
								e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(mute_role.getRole_ID())).queue();
								var mute_time = (long)Azrael.SQLgetWarning(guild_id, Azrael.SQLgetData(user_id, guild_id).getWarningID()+1).getTimer();
								Azrael.SQLInsertHistory(user_id, guild_id, "mute", (rejoinAction.getReason().length() > 0 ? rejoinAction.getReason() : STATIC.getTranslation2(e.getGuild(), Translation.DEFAULT_REASON)), (mute_time/1000/60), "");
							}
							//this is a time defined mute
							else {
								var mute_time = Long.parseLong(rejoinAction.getTime());
								Timestamp timestamp = new Timestamp(System.currentTimeMillis());
								Timestamp unmute_timestamp = new Timestamp(System.currentTimeMillis()+mute_time);
								Azrael.SQLInsertHistory(user_id, guild_id, "mute", (rejoinAction.getReason().length() > 0 ? rejoinAction.getReason() : STATIC.getTranslation2(e.getGuild(), Translation.DEFAULT_REASON)), (mute_time/1000/60), "");
								if(Azrael.SQLgetData(user_id, guild_id).getWarningID() != 0) {
									if(Azrael.SQLUpdateUnmute(user_id, guild_id, timestamp, unmute_timestamp, true, true) == 0) {
										STATIC.writeToRemoteChannel(e.getGuild(), err, STATIC.getTranslation2(e.getGuild(), Translation.JOIN_ERR_6).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id), Channel.LOG.getType());
										logger.error("The unmute timer couldn't be updated from user {} in guild {} for the table Azrael.bancollect", user_id, guild_id);
									}
									else {
										Hashes.addTempCache("mute_time_gu"+guild_id+"us"+user_id, new Cache(""+mute_time, rejoinAction.getReporter(), rejoinAction.getReason()));
										e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(mute_role.getRole_ID())).queue();
									}
								}
								else {
									if(Azrael.SQLInsertData(user_id, guild_id, 1, 1, timestamp, unmute_timestamp, true, true) == 0) {
										STATIC.writeToRemoteChannel(e.getGuild(), err, STATIC.getTranslation2(e.getGuild(), Translation.JOIN_ERR_7).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id), Channel.LOG.getType());
										logger.error("muted user {} couldn't be inserted into Azrael.bancollect for guild {}", user_id, guild_id);
									}
									else {
										Hashes.addTempCache("mute_time_gu"+guild_id+"us"+user_id, new Cache(""+mute_time, rejoinAction.getReporter(), rejoinAction.getReason()));
										e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(mute_role.getRole_ID())).queue();
									}
								}
							}
						}
						else {
							STATIC.writeToRemoteChannel(e.getGuild(), err, STATIC.getTranslation2(e.getGuild(), Translation.JOIN_PERMISSION_ERR).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id)+Permission.MANAGE_ROLES.getName(), Channel.LOG.getType());
						}
					}
					else {
						STATIC.writeToRemoteChannel(e.getGuild(), err, STATIC.getTranslation2(e.getGuild(), Translation.JOIN_ERR_4).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id)+Permission.MANAGE_ROLES.getName(), Channel.LOG.getType());
					}
					//remove the completed join task
					if(Azrael.SQLDeleteRejoinTask(user_id, guild_id) == 0) {
						STATIC.writeToRemoteChannel(e.getGuild(), err, STATIC.getTranslation2(e.getGuild(), Translation.JOIN_ERR_4).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id)+Permission.MANAGE_ROLES.getName(), Channel.LOG.getType());
						logger.error("Rejoin task couldn't be removed from table Azrael.reminder for user {} in guild {}", user_id, guild_id);
					}
				}
				//ban a joined user
				else if(rejoinAction.getType().equals("ban")) {
					//send a private message before banning
					e.getUser().openPrivateChannel().queue(channel -> {
						channel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.USER_BAN_DM_2).replace("{}", e.getGuild().getName())
								+ (GuildIni.getBanSendReason(e.getGuild().getIdLong()) ? STATIC.getTranslation2(e.getGuild(), Translation.USER_BAN_REASON)+rejoinAction.getReason() : "")).queue(success -> {
									Hashes.addTempCache("ban_gu"+e.getGuild().getId()+"us"+user_id, new Cache(rejoinAction.getReporter(), rejoinAction.getReason()));
									e.getGuild().ban(e.getMember(), 0).reason(rejoinAction.getReason()).queue();
									Azrael.SQLInsertHistory(user_id, guild_id, "ban", rejoinAction.getReason(), 0, "");
									channel.close().queue();
								}, error -> {
									STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE), STATIC.getTranslation2(e.getGuild(), Translation.BAN_DM_LOCKED).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id), Channel.LOG.getType());
									Hashes.addTempCache("ban_gu"+e.getGuild().getId()+"us"+user_id, new Cache(rejoinAction.getReporter(), rejoinAction.getReason()));
									e.getGuild().ban(e.getMember(), 0).reason(rejoinAction.getReason()).queue();
									Azrael.SQLInsertHistory(user_id, guild_id, "ban", rejoinAction.getReason(), 0, "");
									channel.close().queue();
								});
					});
					if(Azrael.SQLDeleteRejoinTask(user_id, guild_id) == 0) {
						STATIC.writeToRemoteChannel(e.getGuild(), err, STATIC.getTranslation2(e.getGuild(), Translation.JOIN_ERR_8).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id), Channel.LOG.getType());
						logger.error("Rejoin task couldn't be removed from table Azrael.reminder for user {} in guild {}", user_id, guild_id);
					}
				}
			}
			else {
				//throw rejoin task error here, if it's null
				if(rejoinAction == null) {
					STATIC.writeToRemoteChannel(e.getGuild(), err, STATIC.getTranslation2(e.getGuild(), Translation.JOIN_ERR_9).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id), Channel.LOG.getType());
					logger.error("Rejoin tasks couldn't be verified for the user {} in guild {} from table Azrael.reminder", user_id, guild_id);
				}
				
				String nickname = null;
				String lc_user_name = user_name.toLowerCase();
				//lookup if the user is using the same name as a registered staff member name
				final var name = Azrael.SQLgetStaffNames(guild_id).parallelStream().filter(f -> lc_user_name.matches(f+"#[0-9]{4}")).findAny().orElse(null);
				if(name != null) {
					nick_assign.setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.NAME_STAFF_TITLE)).setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl());
					//verify if the bot has the permission to manage nicknames
					if(e.getGuild().getSelfMember().hasPermission(Permission.NICKNAME_MANAGE)) {
						//retrieve a random nickname and assign to the user
						nickname = Azrael.SQLgetRandomName(e.getGuild().getIdLong());
						Hashes.addTempCache("nickname_add_gu"+guild_id+"us"+user_id, new Cache(60000));
						e.getGuild().modifyNickname(e.getMember(), nickname).queue();
						STATIC.writeToRemoteChannel(e.getGuild(), nick_assign, STATIC.getTranslation2(e.getGuild(), Translation.NAME_STAFF).replaceFirst("\\{\\}", user_name).replace("{}", nickname), Channel.LOG.getType());
						logger.info("Impersonation attempt found from {} in guild {}", user_id, guild_id);
						Azrael.SQLInsertActionLog("MEMBER_NICKNAME_UPDATE", e.getUser().getIdLong(), guild_id, nickname);
						//Run google service, if enabled
						if(GuildIni.getGoogleFunctionalitiesEnabled(guild_id) && GuildIni.getGoogleSpreadsheetsEnabled(guild_id)) {
							GoogleUtils.handleSpreadsheetRequest(e.getGuild(), ""+user_id, new Timestamp(System.currentTimeMillis()), e.getUser().getName()+"#"+e.getUser().getDiscriminator(), null, e.getGuild().getSelfMember().getUser().getName()+"#"+e.getGuild().getSelfMember().getUser().getDiscriminator(), e.getGuild().getSelfMember().getEffectiveName(), "Impersonating a staff member!", null, null, "RENAMED", null, null, null, e.getMember().getEffectiveName(), nickname, 0, null, 0, 0, GoogleEvent.RENAME.id);
						}
					}
					else {
						STATIC.writeToRemoteChannel(e.getGuild(), nick_assign, STATIC.getTranslation2(e.getGuild(), Translation.NAME_STAFF_ERR).replaceFirst("\\{\\}", user_name)+Permission.NICKNAME_MANAGE.getName(), Channel.LOG.getType());
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
								Hashes.addTempCache("nickname_add_gu"+guild_id+"us"+user_id, new Cache(60000));
								e.getGuild().modifyNickname(e.getMember(), nickname).queue();
								STATIC.writeToRemoteChannel(e.getGuild(), nick_assign, STATIC.getTranslation2(e.getGuild(), Translation.NAME_ASSIGN).replaceFirst("\\{\\}", user_name).replace("{}", nickname), Channel.LOG.getType());
								logger.info("Improper name found from {} in guild {}", user_id, guild_id);
								Azrael.SQLInsertActionLog("MEMBER_NICKNAME_UPDATE", e.getUser().getIdLong(), guild_id, nickname);
								//Run google service, if enabled
								if(GuildIni.getGoogleFunctionalitiesEnabled(guild_id) && GuildIni.getGoogleSpreadsheetsEnabled(guild_id)) {
									GoogleUtils.handleSpreadsheetRequest(e.getGuild(), ""+user_id, new Timestamp(System.currentTimeMillis()), e.getUser().getName()+"#"+e.getUser().getDiscriminator(), null, e.getGuild().getSelfMember().getUser().getName()+"#"+e.getGuild().getSelfMember().getUser().getDiscriminator(), e.getGuild().getSelfMember().getEffectiveName(), STATIC.getTranslation2(e.getGuild(), Translation.NAME_REASON), null, null, "RENAMED", null, null, null, e.getMember().getEffectiveName(), nickname, 0, null, 0, 0, GoogleEvent.RENAME.id);
								}
							}
							else {
								STATIC.writeToRemoteChannel(e.getGuild(), nick_assign, STATIC.getTranslation2(e.getGuild(), Translation.NAME_ASSIGN_ERR).replaceFirst("\\{\\}", user_name)+Permission.NICKNAME_MANAGE.getName(), Channel.LOG.getType());
								logger.warn("Lacking MANAGE NICKNAME permission in guild {}", e.getGuild().getId());
							}
							//set flag to signal that the current name is not allowed
							badName = true;
						}
						else {
							//verify if the bot has the permission to kick users
							if(e.getGuild().getSelfMember().hasPermission(Permission.KICK_MEMBERS)) {
								//send a private message and then kick the user
								e.getMember().getUser().openPrivateChannel().queue(channel -> {
									channel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.NAME_KICK_DM).replaceFirst("\\{\\}", e.getGuild().getName()).replace("{}", word.getName().toUpperCase())).queue(success -> {
										e.getGuild().kick(e.getMember()).reason(STATIC.getTranslation2(e.getGuild(), Translation.NAME_KICK_REASON).replace("{}", word.getName().toUpperCase())).queue();
										nick_assign.setColor(Color.RED).setThumbnail(IniFileReader.getCaughtThumbnail()).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.NAME_KICK_TITLE));
										STATIC.writeToRemoteChannel(e.getGuild(), nick_assign, STATIC.getTranslation2(e.getGuild(), Translation.NAME_KICK_MESSAGE_1).replaceFirst("\\{\\}", user_name).replaceFirst("\\{\\}", ""+user_id).replace("{}", word.getName().toUpperCase()), Channel.LOG.getType());
									}, error -> {
										e.getGuild().kick(e.getMember()).reason(STATIC.getTranslation2(e.getGuild(), Translation.NAME_KICK_REASON).replace("{}", word.getName().toUpperCase())).queue();
										nick_assign.setColor(Color.RED).setThumbnail(IniFileReader.getCaughtThumbnail()).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.NAME_KICK_TITLE));
										STATIC.writeToRemoteChannel(e.getGuild(), nick_assign, STATIC.getTranslation2(e.getGuild(), Translation.NAME_KICK_MESSAGE_2).replaceFirst("\\{\\}", user_name).replaceFirst("\\{\\}", ""+user_id).replace("{}", word.getName().toUpperCase()), Channel.LOG.getType());
									});
								});
								Azrael.SQLInsertHistory(e.getUser().getIdLong(), guild_id, "kick", STATIC.getTranslation2(e.getGuild(), Translation.NAME_KICK_REASON).replace("{}", word.getName().toUpperCase()), 0, "");
								//Run google service, if enabled
								if(GuildIni.getGoogleFunctionalitiesEnabled(guild_id) && GuildIni.getGoogleSpreadsheetsEnabled(guild_id)) {
									GoogleUtils.handleSpreadsheetRequest(e.getGuild(), ""+user_id, new Timestamp(System.currentTimeMillis()), e.getUser().getName()+"#"+e.getUser().getDiscriminator(), e.getMember().getEffectiveName(), e.getGuild().getSelfMember().getUser().getName()+"#"+e.getGuild().getSelfMember().getUser().getDiscriminator(), e.getGuild().getSelfMember().getEffectiveName(), STATIC.getTranslation2(e.getGuild(), Translation.NAME_KICK_REASON).replace("{}", word.getName().toUpperCase()), null, null, "KICK", null, null, null, null, null, 0, null, 0, 0, GoogleEvent.KICK.id);
								}
							}
							else {
								nick_assign.setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_ERROR));
								STATIC.writeToRemoteChannel(e.getGuild(), nick_assign, STATIC.getTranslation2(e.getGuild(), Translation.NAME_KICK_PERMISSION_ERR).replaceFirst("\\{\\}", user_name).replaceFirst("\\{\\}", ""+user_id).replace("{}", word.getName().toUpperCase())+Permission.KICK_MEMBERS.getName(), Channel.LOG.getType());
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
