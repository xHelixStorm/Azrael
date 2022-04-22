package de.azrael.listeners;

import java.awt.Color;
import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.sheets.v4.model.ValueRange;

import de.azrael.constructors.Bancollect;
import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.constructors.Guilds;
import de.azrael.constructors.Ranking;
import de.azrael.core.Hashes;
import de.azrael.enums.Channel;
import de.azrael.enums.GoogleDD;
import de.azrael.enums.GoogleEvent;
import de.azrael.enums.Translation;
import de.azrael.google.GoogleSheets;
import de.azrael.google.GoogleUtils;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.sql.DiscordRoles;
import de.azrael.sql.RankingSystem;
import de.azrael.threads.DelayedGoogleUpdate;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

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
			final EmbedBuilder err = new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_ERROR));
			logger.trace("User {} has joined in guild {}", e.getUser().getId(), e.getGuild().getId());
			boolean excludeChannelCreation = false;
			long user_id = e.getMember().getUser().getIdLong();
			String user_name = e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator();
			long guild_id = e.getGuild().getIdLong();
			BotConfigs botConfig = BotConfiguration.SQLgetBotConfigs(guild_id);
			long currentTime = System.currentTimeMillis();
			boolean badName = false;
			long unmute = 0;
			boolean muted;
			
			//insert or update the name of the user into Azrael.users
			if(Azrael.SQLInsertUser(user_id, user_name, STATIC.getLanguage2(e.getGuild()), e.getMember().getUser().getEffectiveAvatarUrl()) == 0) {
				STATIC.writeToRemoteChannel(e.getGuild(), err, STATIC.getTranslation2(e.getGuild(), Translation.JOIN_ERR).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id), Channel.LOG.getType());
				logger.error("Information of user {} couldn't saved in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			}
			//insert or update the join_date
			final int insertJoinDateResult = Azrael.SQLInsertJoinDate(user_id, guild_id, e.getMember().getTimeJoined().format(DateTimeFormatter.ISO_LOCAL_DATE));
			if(insertJoinDateResult == 0) {
				//Update the join date, if no rows have been inserted with the insert statement and no error has been thrown
				if(Azrael.SQLUpdateJoinDate(user_id, guild_id, e.getMember().getTimeJoined().format(DateTimeFormatter.ISO_LOCAL_DATE)) == 0) {
					STATIC.writeToRemoteChannel(e.getGuild(), err, STATIC.getTranslation2(e.getGuild(), Translation.JOIN_ERR).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id), Channel.LOG.getType());
					logger.error("The join date of user {} couldn't be updated in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
				}
			}
			else if(insertJoinDateResult == -1) {
				STATIC.writeToRemoteChannel(e.getGuild(), err, STATIC.getTranslation2(e.getGuild(), Translation.JOIN_ERR).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id), Channel.LOG.getType());
				logger.error("The join date of user {} couldn't be saved in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
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
					logger.error("Ranking information of user {} couldn't be saved on join in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
					RankingSystem.SQLInsertActionLog("high", user_id, guild_id, "User wasn't inserted into user table", "This user couldn't be inserted into the user table. Please verify the name of this user and eventually insert it manually into the table!");
				}
			}
			
			//check if the currently joined user is marked as muted
			Bancollect warnedUser = Azrael.SQLgetData(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
			muted = warnedUser.getMuted();
			//print join message, if the user is not muted and if the printing of join messages is allowed
			if(botConfig.getJoinMessage() || botConfig.getNewAccountOnJoin()) {
				if(!botConfig.getNewAccountOnJoin())
					STATIC.writeToRemoteChannel(e.getGuild(), message.setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl()), STATIC.getTranslation2(e.getGuild(), Translation.JOIN_MESSAGE).replace("{}", user_name), Channel.LOG.getType());
				else {
					//TODO: check logic again during daylight saving 
					final long createdAgo = System.currentTimeMillis() - ((e.getMember().getTimeCreated().toEpochSecond()*1000) + (TimeZone.getDefault().useDaylightTime() ? Calendar.ZONE_OFFSET : 0));
					final long hours = TimeUnit.MILLISECONDS.toHours(createdAgo);
					final long minutes = (TimeUnit.MILLISECONDS.toMinutes(createdAgo)%60);
					//display accounts which are not older than a day only
					if(hours < 24) {
						STATIC.writeToRemoteChannel(e.getGuild(), message.setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl()).setFooter(STATIC.getTranslation2(e.getGuild(), Translation.JOIN_NEW).replaceFirst("\\{\\}", ""+hours).replace("{}", ""+minutes)), STATIC.getTranslation2(e.getGuild(), Translation.JOIN_MESSAGE).replace("{}", user_name), Channel.LOG.getType());
					}
					else if(botConfig.getJoinMessage()) {
						STATIC.writeToRemoteChannel(e.getGuild(), message.setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl()), STATIC.getTranslation2(e.getGuild(), Translation.JOIN_MESSAGE).replace("{}", user_name), Channel.LOG.getType());
					}
				}
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
					if(mute_role != null) {
						e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(mute_role.getRole_ID())).queue();
						excludeChannelCreation = true;
					}
					else
						STATIC.writeToRemoteChannel(e.getGuild(), err, STATIC.getTranslation2(e.getGuild(), Translation.JOIN_ERR_4).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id), Channel.LOG.getType());
				}
				else {
					//throw an error and set the user to not muted and joined, if the permission is missing but is still muted
					if(Azrael.SQLUpdateMutedOnEnd(user_id, guild_id, false, false) > 0) {
						if(Azrael.SQLUpdateGuildLeft(user_id, guild_id, false) == 0) {
							logger.error("The status of the muted user {} couldn't be updated to rejoined in guild {}", user_id, guild_id);
						}
					}
					else
						logger.error("Mute state of user {} couldn't be terminated on end in guild {}", user_id, guild_id);
					STATIC.writeToRemoteChannel(e.getGuild(), err, STATIC.getTranslation2(e.getGuild(), Translation.JOIN_PERMISSION_ERR).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id)+Permission.MANAGE_ROLES.getName(), Channel.LOG.getType());
					logger.warn("MANAGE ROLES permission required to mute a user in guild {}", e.getGuild().getId());
				}
			}
			else {
				//Remove the muted and guild left label from this user, if he joined the server after the mute time expired and is still marked as muted
				if(muted) {
					if(Azrael.SQLUpdateMutedOnEnd(user_id, guild_id, false, false) > 0) {
						if(Azrael.SQLUpdateGuildLeft(user_id, guild_id, false) == 0) {
							logger.error("The status of the muted user {} couldn't be updated to rejoined in guild {}", user_id, guild_id);
						}
					}
					else
						logger.error("Mute state of user {} couldn't be terminated on end in guild {}", user_id, guild_id);
				}
				//Assign an unlocked ranking role after verifying that the ranking system is enabled
				if(guild_settings.getRankingState()) {
					Ranking user_details = RankingSystem.SQLgetWholeRankView(user_id, guild_id);
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
									logger.error("User {} couldn't be labeled as permanently muted in guild {}", user_id, guild_id);
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
										logger.error("The unmute timer couldn't be updated for user {} in guild {}", user_id, guild_id);
									}
									else {
										Hashes.addTempCache("mute_time_gu"+guild_id+"us"+user_id, new Cache(""+mute_time, rejoinAction.getReporter(), rejoinAction.getReason()));
										e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(mute_role.getRole_ID())).queue();
									}
								}
								else {
									if(Azrael.SQLInsertData(user_id, guild_id, 1, 1, timestamp, unmute_timestamp, true, true) == 0) {
										STATIC.writeToRemoteChannel(e.getGuild(), err, STATIC.getTranslation2(e.getGuild(), Translation.JOIN_ERR_7).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id), Channel.LOG.getType());
										logger.error("User {} couldn't be labeled as muted in guild {}", user_id, guild_id);
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
						logger.error("Rejoin task mute couldn't be removed for user {} in guild {}", user_id, guild_id);
					}
				}
				//ban a joined user
				else if(rejoinAction.getType().equals("ban")) {
					//check if the bot has the ban permission before continuing
					if(e.getGuild().getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
						//send a private message before banning
						e.getUser().openPrivateChannel().queue(channel -> {
							channel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.USER_BAN_DM_2).replace("{}", e.getGuild().getName())
									+ (botConfig.getBanSendReason() ? STATIC.getTranslation2(e.getGuild(), Translation.USER_BAN_REASON)+rejoinAction.getReason() : "")).queue(success -> {
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
						excludeChannelCreation = true;
					}
					else {
						STATIC.writeToRemoteChannel(e.getGuild(), err, STATIC.getTranslation2(e.getGuild(), Translation.JOIN_PERMISSION_ERR_2).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id)+Permission.BAN_MEMBERS.getName(), Channel.LOG.getType());
					}
					if(Azrael.SQLDeleteRejoinTask(user_id, guild_id) == 0) {
						STATIC.writeToRemoteChannel(e.getGuild(), err, STATIC.getTranslation2(e.getGuild(), Translation.JOIN_ERR_8).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id), Channel.LOG.getType());
						logger.error("Rejoin task ban couldn't be removed for user {} in guild {}", user_id, guild_id);
					}
				}
			}
			else {
				//throw rejoin task error here, if it's null
				if(rejoinAction == null) {
					STATIC.writeToRemoteChannel(e.getGuild(), err, STATIC.getTranslation2(e.getGuild(), Translation.JOIN_ERR_9).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id), Channel.LOG.getType());
					logger.error("Rejoin tasks couldn't be verified for user {} in guild {}", user_id, guild_id);
				}
				
				//verify if additional verification actions are required
				if(!excludeChannelCreation) {
					putUserIntoWaitingRoom(e.getGuild(), e.getMember(), botConfig);
				}
				
				String nickname = null;
				String lc_user_name = user_name.toLowerCase();
				//lookup if the user is using the same name as a registered staff member name
				final var name = Azrael.SQLgetStaffNames(guild_id).parallelStream().filter(f -> lc_user_name.matches(f+"#[0-9]{4}")).findAny().orElse(null);
				if(name != null) {
					final EmbedBuilder nick_assign = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(BotConfiguration.SQLgetThumbnails(e.getGuild().getIdLong()).getCaught()).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.NAME_TITLE));
					nick_assign.setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.NAME_STAFF_TITLE)).setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl());
					//verify if the bot has the permission to manage nicknames
					if(e.getGuild().getSelfMember().hasPermission(Permission.NICKNAME_MANAGE)) {
						//retrieve a random nickname and assign to the user
						nickname = Azrael.SQLgetRandomName(e.getGuild().getIdLong());
						Hashes.addTempCache("nickname_add_gu"+guild_id+"us"+user_id, new Cache(60000));
						e.getGuild().modifyNickname(e.getMember(), nickname).queue();
						STATIC.writeToRemoteChannel(e.getGuild(), nick_assign, STATIC.getTranslation2(e.getGuild(), Translation.NAME_STAFF).replaceFirst("\\{\\}", user_name).replace("{}", nickname), Channel.LOG.getType());
						logger.info("Impersonation attempt found from user {} in guild {}", user_id, guild_id);
						Azrael.SQLInsertActionLog("MEMBER_NICKNAME_UPDATE", e.getUser().getIdLong(), guild_id, nickname);
						//Run google service, if enabled
						if(botConfig.getGoogleFunctionalities()) {
							GoogleSheets.spreadsheetRenameRequest(Azrael.SQLgetGoogleFilesAndEvent(guild_id, 2, GoogleEvent.RENAME.id, ""), e.getGuild(), "", ""+user_id, new Timestamp(System.currentTimeMillis()), e.getUser().getName()+"#"+e.getUser().getDiscriminator(), e.getGuild().getSelfMember().getUser().getName()+"#"+e.getGuild().getSelfMember().getUser().getDiscriminator(), e.getGuild().getSelfMember().getEffectiveName(), STATIC.getTranslation2(e.getGuild(), Translation.NAME_STAFF_IMPERSONATION), e.getMember().getEffectiveName(), nickname);
						}
					}
					else {
						STATIC.writeToRemoteChannel(e.getGuild(), nick_assign, STATIC.getTranslation2(e.getGuild(), Translation.NAME_STAFF_ERR).replaceFirst("\\{\\}", user_name)+Permission.NICKNAME_MANAGE.getName(), Channel.LOG.getType());
						logger.warn("MANAGE NICKNAME permission required to assign nicknames in guild {}", e.getGuild().getId());
					}
					//set flag to signal that the current name is not allowed
					badName = true;
				}
				//execute only if the staff name search failed
				if(badName == false) {
					//look up the name filter for not allowed words
					final var word = Azrael.SQLgetNameFilter(guild_id).parallelStream().filter(f -> lc_user_name.contains(f.getName())).findAny().orElse(null);
					if(word != null) {
						final EmbedBuilder nick_assign = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(BotConfiguration.SQLgetThumbnails(e.getGuild().getIdLong()).getCaught()).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.NAME_TITLE));
						if(!word.getKick()) {
							//verify if the bot has the permission to manage nicknames
							if(e.getGuild().getSelfMember().hasPermission(Permission.NICKNAME_MANAGE)) {
								//retrieve a random nickname and assign to the user
								nickname = Azrael.SQLgetRandomName(e.getGuild().getIdLong());
								Hashes.addTempCache("nickname_add_gu"+guild_id+"us"+user_id, new Cache(60000));
								e.getGuild().modifyNickname(e.getMember(), nickname).queue();
								STATIC.writeToRemoteChannel(e.getGuild(), nick_assign, STATIC.getTranslation2(e.getGuild(), Translation.NAME_ASSIGN).replaceFirst("\\{\\}", user_name).replace("{}", nickname), Channel.LOG.getType());
								logger.info("Improper name found from user {} in guild {}", user_id, guild_id);
								Azrael.SQLInsertActionLog("MEMBER_NICKNAME_UPDATE", e.getUser().getIdLong(), guild_id, nickname);
								//Run google service, if enabled
								if(botConfig.getGoogleFunctionalities()) {
									GoogleSheets.spreadsheetRenameRequest(Azrael.SQLgetGoogleFilesAndEvent(guild_id, 2, GoogleEvent.RENAME.id, ""), e.getGuild(), "", ""+user_id, new Timestamp(System.currentTimeMillis()), e.getUser().getName()+"#"+e.getUser().getDiscriminator(), e.getGuild().getSelfMember().getUser().getName()+"#"+e.getGuild().getSelfMember().getUser().getDiscriminator(), e.getGuild().getSelfMember().getEffectiveName(), STATIC.getTranslation2(e.getGuild(), Translation.NAME_REASON), e.getMember().getEffectiveName(), nickname);
								}
							}
							else {
								STATIC.writeToRemoteChannel(e.getGuild(), nick_assign, STATIC.getTranslation2(e.getGuild(), Translation.NAME_ASSIGN_ERR).replaceFirst("\\{\\}", user_name)+Permission.NICKNAME_MANAGE.getName(), Channel.LOG.getType());
								logger.warn("MANAGE NICKNAME permission required to assign nicknames in guild {}", e.getGuild().getId());
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
										nick_assign.setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.NAME_KICK_TITLE));
										STATIC.writeToRemoteChannel(e.getGuild(), nick_assign, STATIC.getTranslation2(e.getGuild(), Translation.NAME_KICK_MESSAGE_1).replaceFirst("\\{\\}", user_name).replaceFirst("\\{\\}", ""+user_id).replace("{}", word.getName().toUpperCase()), Channel.LOG.getType());
									}, error -> {
										e.getGuild().kick(e.getMember()).reason(STATIC.getTranslation2(e.getGuild(), Translation.NAME_KICK_REASON).replace("{}", word.getName().toUpperCase())).queue();
										nick_assign.setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.NAME_KICK_TITLE));
										STATIC.writeToRemoteChannel(e.getGuild(), nick_assign, STATIC.getTranslation2(e.getGuild(), Translation.NAME_KICK_MESSAGE_2).replaceFirst("\\{\\}", user_name).replaceFirst("\\{\\}", ""+user_id).replace("{}", word.getName().toUpperCase()), Channel.LOG.getType());
									});
								});
								Azrael.SQLInsertHistory(e.getUser().getIdLong(), guild_id, "kick", STATIC.getTranslation2(e.getGuild(), Translation.NAME_KICK_REASON).replace("{}", word.getName().toUpperCase()), 0, "");
								//Run google service, if enabled
								if(botConfig.getGoogleFunctionalities()) {
									GoogleSheets.spreadsheetKickRequest(Azrael.SQLgetGoogleFilesAndEvent(guild_id, 2, GoogleEvent.KICK.id, ""), e.getGuild(), "", ""+user_id, new Timestamp(System.currentTimeMillis()), e.getUser().getName()+"#"+e.getUser().getDiscriminator(), e.getMember().getEffectiveName(), e.getGuild().getSelfMember().getUser().getName()+"#"+e.getGuild().getSelfMember().getUser().getDiscriminator(), e.getGuild().getSelfMember().getEffectiveName(), STATIC.getTranslation2(e.getGuild(), Translation.NAME_KICK_REASON).replace("{}", word.getName().toUpperCase()));
								}
							}
							else {
								nick_assign.setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_ERROR));
								STATIC.writeToRemoteChannel(e.getGuild(), nick_assign, STATIC.getTranslation2(e.getGuild(), Translation.NAME_KICK_PERMISSION_ERR).replaceFirst("\\{\\}", user_name).replaceFirst("\\{\\}", ""+user_id).replace("{}", word.getName().toUpperCase())+Permission.KICK_MEMBERS.getName(), Channel.LOG.getType());
								logger.warn("KICK MEMBERS permission required to kick users {} in guild {}", e.getGuild().getId());
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
							logger.error("User nickname of {} couldn't be updated in guild {}", user_id, guild_id);
						}
						else {
							logger.info("User {} received the nickname {} in guild {}", user_id, nickname, guild_id);
							Azrael.SQLInsertActionLog("MEMBER_NICKNAME_UPDATE", user_id, guild_id, nickname);
						}
					}
					else if(nickname != null) {
						if(Azrael.SQLInsertNickname(user_id, guild_id, nickname) == 0) {
							logger.error("User nickname of {} couldn't be saved in guild {}", user_id, guild_id);
						}
						else {
							logger.info("User {} received the nickname {} in guild {}", user_id, nickname, guild_id);
							Azrael.SQLInsertActionLog("MEMBER_NICKNAME_UPDATE", user_id, guild_id, nickname);
						}
					}
				}
				
				//single use invite logic
				handleSingleUseInvites(botConfig, e.getGuild(), e.getMember());
			}
			
			Azrael.SQLInsertActionLog("GUILD_MEMBER_JOIN", user_id, guild_id, user_name);
		}).start();
	}
	
	public static void putUserIntoWaitingRoom(Guild guild, Member member, BotConfigs botConfig) {
		final var categories = Azrael.SQLgetCategories(guild.getIdLong());
		if(categories != null && categories.size() > 0) {
			final var verification = categories.parallelStream().filter(f -> f.getType().equals("ver")).findAny().orElse(null);
			if(verification != null) {
				Category category = guild.getCategoryById(verification.getCategoryID());
				if(category != null) {
					if(guild.getSelfMember().hasPermission(category, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS) || STATIC.setPermissions(guild, category, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS))) {
						//create a new text channel under the category and add the required permissions
						category.createTextChannel(""+member.getUser().getId())
							.addPermissionOverride(guild.getSelfMember(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE, Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS), null)
							.addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE))
							.addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EMBED_LINKS), EnumSet.of(Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS))
							.queue(channel -> {
								final var roles = DiscordRoles.SQLgetRoles(guild.getIdLong()).parallelStream().filter(f -> f.getCategory_ABV().equals("adm") || f.getCategory_ABV().equals("mod")).collect(Collectors.toList());
								for(final var role : roles) {
									Role serverRole = guild.getRoleById(role.getRole_ID());
									if(serverRole != null) {
										channel.getManager().putPermissionOverride(serverRole, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EMBED_LINKS), EnumSet.of(Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS)).queue();
									}
								}
								final String verificationMessage = botConfig.getCustomMessageVerification();
								channel.sendMessage(new EmbedBuilder().setColor(Color.BLUE).setThumbnail(guild.getIconUrl()).setDescription((verificationMessage != null && verificationMessage.length() > 0 ? verificationMessage : STATIC.getTranslation2(guild, Translation.JOIN_VERIFY).replaceFirst("\\{\\}", guild.getName()).replace("{}", member.getAsMention()))).build()).queue();
							}
						);
					}
					else {
						STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(guild, Translation.MISSING_PERMISSION_IN_2).replace("{}", Permission.MANAGE_CHANNEL.getName()+" and "+Permission.MANAGE_PERMISSIONS.getName())+category.getName(), Channel.LOG.getType());
						logger.warn("MANAGE_CHANNEL and MANAGE_PERMISSIONS for category {} required to create verification channels in guild {}", verification.getCategoryID(), guild.getId());
					}
				}
				else {
					logger.warn("Category {} doesn't exist anymore in guild {}", verification.getCategoryID(), guild.getId());
				}
			}
		}
	}
	
	private static void handleSingleUseInvites(BotConfigs botConfig, Guild guild, Member member) {
		final var invites = Azrael.SQLgetUnusedInvites(guild.getIdLong());
		if(invites != null && invites.size() > 0) {
			if(guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
				guild.retrieveInvites().queue(serverInvites -> {
					for(final var invite : invites) {
						final Invite serverInvite = serverInvites.parallelStream().filter(f -> f.getUrl().equals(invite) && f.getUses() == 1).findAny().orElse(null);
						if(serverInvite != null) {
							serverInvite.delete().queue(success -> {
								logger.info("Invite {} has been used by {} and as a result removed in guild {}", invite, member.getUser().getId(), guild.getId());
								final var role = DiscordRoles.SQLgetRoles(guild.getIdLong()).parallelStream().filter(f -> f.getCategory_ABV() != null && f.getCategory_ABV().equals("inv")).findAny().orElse(null);
								if(role != null) {
									final var serverRole = guild.getRoleById(role.getRole_ID());
									if(serverRole != null) {
										if(guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES) && guild.getSelfMember().canInteract(serverRole))
											guild.addRoleToMember(member, serverRole).queue();
										else {
											STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(guild, Translation.JOIN_ERR_14)+Permission.MANAGE_ROLES.getName(), Channel.LOG.getType());
											logger.error("Permission {} required to assign roles in guild {}", Permission.MANAGE_ROLES.getName(), guild.getId());
										}
									}
									else {
										logger.warn("Role {} doesn't exist anymore in guild {}", role.getRole_ID(), guild.getId());
									}
								}
							});
							
							STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.GREEN), STATIC.getTranslation2(guild, Translation.INVITES_USED_BY).replaceFirst("\\{\\}", serverInvite.getCode()).replaceFirst("\\{\\}", member.getUser().getName()+"#"+member.getUser().getDiscriminator()).replace("{}", member.getUser().getId()), Channel.LOG.getType());
							
							if(Azrael.SQLUpdateUsedinvite(guild.getIdLong(), invite, member.getUser().getIdLong()) == 0)
								logger.warn("Used invite {} from {} couldn't be labeled as used in guild {}", invite, member.getUser().getId(), guild.getId());
							
							//Google spreadsheet execution
							if(botConfig.getGoogleFunctionalities()) {
								handleGoogleInviteRequest(guild, member, invite);
							}
							break;
						}
					}
				});
			}
			else {
				STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(guild, Translation.JOIN_ERR_13).replace("{}", Permission.MANAGE_SERVER.getName()), Channel.LOG.getType());
				logger.error("Permission {} required to remove already used invites in guild {}", Permission.MANAGE_SERVER.getName(), guild.getId());
			}
		}
		else if(invites == null) {
			logger.error("Single use invites couldn't be retrieved in guild {}", guild.getId());
		}
	}
	
	private static void handleGoogleInviteRequest(Guild guild, Member member, String invite) {
		final String [] array = Azrael.SQLgetGoogleFilesAndEvent(guild.getIdLong(), 2, GoogleEvent.INVITES.id, "");
		if(array != null && !array[0].equals("empty")) {
			final String file_id = array[0];
			final String row_start = array[1].replaceAll("![A-Z0-9]*", "");
			try {
				ValueRange response = DelayedGoogleUpdate.getCachedValueRange("INVITES"+guild.getId());
				if(response == null) {
					final var service = GoogleSheets.getSheetsClientService();
					response = GoogleSheets.readWholeSpreadsheet(service, file_id, row_start);
					DelayedGoogleUpdate.cacheRetrievedSheetValueRange("INVITES"+guild.getId(), response);
				}
				int currentRow = 0;
				for(var row : response.getValues()) {
					currentRow ++;
					if(row.parallelStream().filter(f -> {
						String cell = (String)f;
						if(cell.equals(invite))
							return true;
						else
							return false;
						}).findAny().orElse(null) != null) {
						//retrieve the saved mapping for the comment event
						final var columns = Azrael.SQLgetGoogleSpreadsheetMapping(file_id, GoogleEvent.INVITES.id, guild.getIdLong());
						if(columns != null && columns.size() > 0) {
							int columnUserID = 0;
							int columnName = 0;
							String columnNameFormat = null;
							int columnUsername = 0;
							String columnUsernameFormat = null;
							int columnTimestampUpdated = 0;
							String columnTimestampUpdatedFormat = null;
							for(final var column : columns) {
								if(column.getItem() == GoogleDD.USER_ID)
									columnUserID = column.getColumn();
								else  if(column.getItem() == GoogleDD.NAME) {
									columnName = column.getColumn();
									columnNameFormat = column.getFormatter();
								}
								else if(column.getItem() == GoogleDD.USERNAME) {
									columnUsername = column.getColumn();
									columnUsernameFormat = column.getFormatter();
								}
								else if(column.getItem() == GoogleDD.TIMESTAMP_UPDATED) {
									columnTimestampUpdated = column.getColumn();
									columnTimestampUpdatedFormat = column.getFormatter();
								}
							}
							if(columnUserID != 0 || columnName != 0 || columnUsername != 0 || columnTimestampUpdated != 0) {
								ArrayList<List<Object>> values = new ArrayList<List<Object>>();
								List<Object> subValues = new ArrayList<Object>();
								//build update array
								int columnCount = 0;
								for(final var column : row) {
									columnCount ++;
									if(columnCount == columnUserID)
										subValues.add(member.getUser().getId());
									else if(columnCount == columnName) 
										subValues.add(GoogleDD.NAME.valueFormatter(member.getUser().getName()+"#"+member.getUser().getDiscriminator(), columnNameFormat));
									else if(columnCount == columnUsername) 
										subValues.add(GoogleDD.USERNAME.valueFormatter(member.getEffectiveName(), columnUsernameFormat));
									else if(columnCount == columnTimestampUpdated)
										subValues.add(GoogleDD.TIMESTAMP_UPDATED.valueFormatter(new Timestamp(System.currentTimeMillis()), columnTimestampUpdatedFormat));
									else
										subValues.add(column);
								}
								values.add(subValues);
								ValueRange valueRange = new ValueRange().setRange(row_start+"!A"+currentRow).setValues(values);
								//Execute Runnable
								if(!STATIC.threadExists("INVITES"+guild.getId())) {
									new Thread(new DelayedGoogleUpdate(guild, valueRange, 0, array[0], "", "update", GoogleEvent.INVITES)).start();
								}
								else {
									DelayedGoogleUpdate.handleAdditionalRequest(guild, "", valueRange, 0, "update");
								}
							}
						}
						//interrupt the row search
						break;
					}
				}
			} catch(SocketTimeoutException e) {
				if(GoogleUtils.timeoutHandler(guild, file_id, GoogleEvent.INVITES.name(), e)) {
					handleGoogleInviteRequest(guild, member, invite);
				}
			} catch (Exception e1) {
				STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED), STATIC.getTranslation2(guild, Translation.GOOGLE_WEBSERVICE)+e1.getMessage(), Channel.LOG.getType());
				logger.error("Google Spreadsheet webservice error for event INVITES in guild {}", guild.getIdLong(), e1);
			}
		}
	}
}
