package commandsContainer;

import java.awt.Color;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jpaste.exceptions.PasteException;
import org.jpaste.pastebin.exceptions.LoginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Bancollect;
import constructors.Cache;
import constructors.Channels;
import constructors.Guilds;
import constructors.Messages;
import constructors.Rank;
import constructors.RejoinTask;
import constructors.User;
import constructors.Watchlist;
import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;
import sql.DiscordRoles;
import sql.Azrael;
import util.Pastebin;
import util.STATIC;

public class UserExecution {
	private static final Logger logger = LoggerFactory.getLogger(UserExecution.class);
	
	public static void getHelp(GuildMessageReceivedEvent _e) {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setTitle("Help for the user command!");
		_e.getChannel().sendMessage(message.setDescription("Mention a user right after the command and then choose an action to take. For example to display information, to mute, to ban, to set a warning value, to set a level or to gift experience points for the ranking system").build()).queue();
	}
	
	public static void runTask(GuildMessageReceivedEvent _e, String _input, String _displayed_input) {
		var key = "user_gu"+_e.getGuild().getId()+"ch"+_e.getChannel().getId()+"us"+_e.getMember().getUser().getId();
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setTitle("Choose the desired action");
		
		String name = _displayed_input.replaceAll("@", "");
		String raw_input = _input;
		String user_name = null;
		
		if(raw_input.length() != 18 && raw_input.length() != 17) {
			final User user = Azrael.SQLgetUser(name);
			if(user != null) {
				raw_input = ""+user.getUserID();
				user_name = user.getUserName();
			}
		}
		else {
			final User user = Azrael.SQLgetUserThroughID(raw_input, _e.getGuild().getIdLong());
			if(user != null)
				user_name = user.getUserName();
		}
		
		if(raw_input != null && (raw_input.length() == 18 || raw_input.length() == 17) && user_name != null && user_name.length() > 0) {
			_e.getChannel().sendMessage(message.setDescription("User **"+user_name+"** has been found in this guild! Now type one of the following words within 3 minutes to execute an action! In case you want to abort the current action, type **exit**!\n\n"
				+ "**information**: To display all details of the selected user\n"
				+ "**delete-messages**: To remove up to 100 messages from the selected user\n"
				+ "**warning**: To change the current warning value\n"
				+ "**mute**: To assign the mute role\n"
				+ "**unmute** To unmute the member and to terminate the running task\n"
				+ "**ban**: To ban the user\n"
				+ "**kick**: To kick the user\n"
				+ "**history**: To display the whole kick/ban/mute history with reasons\n"
				+ "**watch**: To either log all messages or only deleted messages from this user\n"
				+ "**unwatch**: To remove this user from the watchlist\n"
				+ "**gift-experience**: To gift experience points\n"
				+ "**set-experience**: To set an experience value\n"
				+ "**set-level**: To assign a level\n"
				+ "**gift-currency**: To gift money\n"
				+ "**set-currency**: To se a money value").build()).queue();
			Hashes.addTempCache(key, new Cache(180000, raw_input));
		}
		else {
			_e.getChannel().sendMessage(_e.getMember().getAsMention()+" Error, user doesn't exist. Please try again!").queue();
		}
	}
	
	@SuppressWarnings("preview")
	public static void performAction(GuildMessageReceivedEvent _e, String _message, Cache cache, ArrayList<Channels> _allChannels) {
		var key = "user_gu"+_e.getGuild().getId()+"ch"+_e.getChannel().getId()+"us"+_e.getMember().getUser().getId();
		EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Session Expired!");
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
		if(cache != null && cache.getExpiration() - System.currentTimeMillis() > 0) {
			Guilds guild_settings = RankingSystem.SQLgetGuild(_e.getGuild().getIdLong());
			var comment = _message.toLowerCase();
			if(comment.equals("exit")) {
				_e.getChannel().sendMessage(message.setDescription("User command aborted!").build()).queue();
				Hashes.clearTempCache(key);
				return;
			}
			var user_id = Long.parseLong(cache.getAdditionalInfo().replaceAll("[^0-9]*", ""));
			if(!cache.getAdditionalInfo().matches("[a-zA-Z\\-]{1,}[\\d]*") && (comment.equals("information") || comment.equals("delete-messages") || comment.equals("warning") || comment.equals("mute") || comment.equals("unmute") || comment.equals("ban") || comment.equals("kick") || comment.equals("history") || comment.equals("watch") || comment.equals("unwatch") || comment.equals("gift-experience") || comment.equals("set-experience") || comment.equals("set-level") || comment.equals("gift-currency") || comment.equals("set-currency"))) {
				switch(comment) {
					case "information" -> {
						final var informationLevel = GuildIni.getUserInformationLevel(_e.getGuild().getIdLong());
						if(UserPrivs.comparePrivilege(_e.getMember(), informationLevel) || GuildIni.getAdmin(_e.getGuild().getIdLong()) == _e.getMember().getUser().getIdLong()) {
							User user = Azrael.SQLgetUserThroughID(cache.getAdditionalInfo(), _e.getGuild().getIdLong());
							message.setTitle("Here the requested information!");
							if(user.getAvatar() != null)
								message.setThumbnail(user.getAvatar());
							message.setAuthor(user.getUserName());
							message.setDescription("Here you can inspect all current information for this user!");
							message.addBlankField(false);
							Bancollect warnedUser = Azrael.SQLgetData(user_id, _e.getGuild().getIdLong());
							message.addField("CURRENT WARNING", "**"+warnedUser.getWarningID()+"**/**"+Azrael.SQLgetMaxWarning(_e.getGuild().getIdLong())+"**", true);
							message.addField("TOTAL WARNINGS", "**"+Azrael.SQLgetSingleActionEventCount("MEMBER_MUTE_ADD", user_id, _e.getGuild().getIdLong())+"**", true);
							message.addField("TOTAL BANS", "**"+Azrael.SQLgetSingleActionEventCount("MEMBER_BAN_ADD", user_id, _e.getGuild().getIdLong())+"**", true);
							message.addField("BANNED", warnedUser.getBanID() == 2 ? "**YES**" : "**NO**", true);
							message.addField("ORIGINAL JOIN DATE", "**"+user.getOriginalJoinDate()+"**", true);
							message.addField("NEWEST JOIN DATE", "**"+user.getNewestJoinDate()+"**", true);
							message.addField("USER ID", "**"+cache.getAdditionalInfo()+"**", true);
							var watchedUser = Azrael.SQLgetWatchlist(user_id, _e.getGuild().getIdLong());
							if(watchedUser == null || (watchedUser.hasHigherPrivileges() && !UserPrivs.comparePrivilege(_e.getMember(), GuildIni.getUserUseWatchChannelLevel(_e.getGuild().getIdLong()))))
								message.addField("WATCH LEVEL", "**0**", true);
							else
								message.addField("WATCH LEVEL", "**"+watchedUser.getLevel()+"**", true);
							message.addBlankField(false);
							Rank user_details = RankingSystem.SQLgetWholeRankView(user_id, _e.getGuild().getIdLong());
							if(guild_settings.getRankingState() == true) {
								message.addField("LEVEL", "**"+user_details.getLevel()+"**/**"+guild_settings.getMaxLevel()+"**", true);
								message.addField("EXPERIENCE", "**"+user_details.getCurrentExperience()+"**/**"+user_details.getRankUpExperience()+"**", true);
								if(user_details.getCurrentRole() != 0) {
									message.addField("UNLOCKED ROLE", _e.getGuild().getRoleById(user_details.getCurrentRole()).getAsMention(), true);
								}
								else {
									message.addField("UNLOCKED ROLE", "**N/A**", true);
								}
								message.addField("TOTAL EXPERIENCE", "**"+user_details.getExperience()+"**", true);
							}
							StringBuilder out = new StringBuilder();
							try {
								for(String description : Azrael.SQLgetDoubleActionEventDescriptions("MEMBER_NAME_UPDATE", "GUILD_MEMBER_JOIN", user_id, _e.getGuild().getIdLong())) {
									out.append("[`"+description+"`] ");
								}
								out = out.toString().replaceAll("[\\s]*", "").length() == 0 ? out.append("**N/A**") : out;
								message.addField("USED NAMES", out.toString(), false);
								out.setLength(0);
								for(String description : Azrael.SQLgetSingleActionEventDescriptions("MEMBER_NICKNAME_UPDATE", user_id, _e.getGuild().getIdLong())) {
									out.append("[`"+description+"`] ");
								}
								out = out.toString().replaceAll("[\\s]*", "").length() == 0 ? out.append("**N/A**") : out;
								message.addField("USED NICKNAMES", out.toString(), false);
								out.setLength(0);
								_e.getChannel().sendMessage(message.build()).queue();
							} catch(IllegalArgumentException iae) {
								_e.getChannel().sendMessage(message.build()).queue();
								message.clear();
								out.setLength(0);
								for(String description : Azrael.SQLgetDoubleActionEventDescriptions("MEMBER_NAME_UPDATE", "GUILD_MEMBER_JOIN", user_id, _e.getGuild().getIdLong())) {
									out.append("[`"+description+"`] ");
								}
								out = out.toString().replaceAll("[\\s]*", "").length() == 0 ? out.append("**N/A**") : out;
								try {
									message.setColor(Color.BLUE).setTitle("USED NAMES").setDescription(out.toString());
								} catch(IllegalArgumentException iae2) {
									try {
										String pastebin_link = Pastebin.unlistedPaste("USED NAMES", out.toString(), _e.getGuild().getIdLong());
										message.setColor(Color.BLUE).setTitle("USED NAMES").setDescription("Names posted on Pastebin as unlisted: "+pastebin_link);
									} catch (IllegalStateException | LoginException | PasteException e) {
										logger.warn("Error on creating paste!", e);
										message.setColor(Color.RED).setTitle("USED NAMES").setDescription("Names couldn't be displayed because it exceeded the limit of characters. Please bind the bot with a Pastebin account to display the names on Pastebin!");
									}
								}
								_e.getChannel().sendMessage(message.build()).queue();
								message.clear();
								for(String description : Azrael.SQLgetSingleActionEventDescriptions("MEMBER_NICKNAME_UPDATE", user_id, _e.getGuild().getIdLong())) {
									out.append("[`"+description+"`] ");
								}
								out = out.toString().replaceAll("[\\s]*", "").length() == 0 ? out.append("**N/A**") : out;
								try {
									message.setColor(Color.BLUE).setTitle("USED NICKNAMES").setDescription(out.toString());
								} catch(IllegalArgumentException iae2) {
									try {
										String pastebin_link = Pastebin.unlistedPaste("USED NICKNAMES", out.toString(), _e.getGuild().getIdLong());
										message.setColor(Color.BLUE).setTitle("USED NICKNAMES").setDescription("Nicknames posted on Pastebin as unlisted: "+pastebin_link);
									} catch (IllegalStateException | LoginException | PasteException e) {
										logger.warn("Error on creating paste!", e);
										message.setColor(Color.RED).setTitle("USED NICKNAMES").setDescription("Nicknames couldn't be displayed because it exceeded the limit of characters. Please bind the bot with a Pastebin account to display the names on Pastebin!");
									}
								}
								_e.getChannel().sendMessage(message.build()).queue();
							}
							if(IniFileReader.getActionLog()) {
								message.clear();
								message.setColor(Color.BLUE).setTitle("DELETED MESSAGES ON PASTEBIN");
								out.setLength(0);
								for(String description : Azrael.SQLgetSingleActionEventDescriptionsOrdered("MESSAGES_DELETED", user_id, _e.getGuild().getIdLong())) {
									out.append(description+"\n");
								}
								message.setDescription(out);
								if(out.length() > 0)_e.getChannel().sendMessage(message.build()).queue();
								
								message.clear();
								message.setColor(Color.BLUE).setTitle("EVENTS");
								out.setLength(0);
								for(String description : Azrael.SQLgetCriticalActionEvents(user_id, _e.getGuild().getIdLong())) {
									out.append(description+"\n");
								}
								message.setDescription(out);
								if(out.length() > 0)_e.getChannel().sendMessage(message.build()).queue();
							}
							logger.debug("{} has displayed information of the user {}", _e.getMember().getUser().getId(), cache.getAdditionalInfo());
						}
						else {
							UserPrivs.throwNotEnoughPrivilegeError(_e, informationLevel);
						}
						Hashes.clearTempCache(key);
					}
					case "delete-messages" -> {
						if(_e.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
							if(_e.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_HISTORY)) {
								final var deleteMessagesLevel = GuildIni.getUserDeleteMessagesLevel(_e.getGuild().getIdLong());
								if(UserPrivs.comparePrivilege(_e.getMember(), deleteMessagesLevel) || GuildIni.getAdmin(_e.getGuild().getIdLong()) == _e.getMember().getUser().getIdLong()) {
									message.setTitle("You chose to delete a number of messages!");
									_e.getChannel().sendMessage(message.setDescription("Please choose how many messages should be removed between 1 and 100!").build()).queue();
									cache.updateDescription("delete-messages"+user_id).setExpiration(180000);
									Hashes.addTempCache(key, cache);
								}
								else {
									UserPrivs.throwNotEnoughPrivilegeError(_e, deleteMessagesLevel);
									Hashes.clearTempCache(key);
								}
							}
							else {
								message.setTitle("Messages can't be deleted!").setColor(Color.RED);
								_e.getChannel().sendMessage(message.setDescription("The delete-messages parameter can't be used because the MESSAGE HISTORY permission is missing!").build()).queue();
								logger.warn("MESSAGE HISTORY permission required for deleting messages in guild {}!", _e.getGuild().getId());
								Hashes.clearTempCache(key);
							}
						}
						else {
							message.setTitle("Messages can't be deleted!").setColor(Color.RED);
							_e.getChannel().sendMessage(message.setDescription("The delete-messages parameter can't be used because the MANAGE MESSAGES permission is missing!").build()).queue();
							logger.warn("MANAGE MESSAGES permission required for deleting messages in guild {}!", _e.getGuild().getId());
							Hashes.clearTempCache(key);
						}
					}
					case "warning" -> {
						final var warningLevel = GuildIni.getUserWarningLevel(_e.getGuild().getIdLong());
						if(UserPrivs.comparePrivilege(_e.getMember(), warningLevel) || GuildIni.getAdmin(_e.getGuild().getIdLong()) == _e.getMember().getUser().getIdLong()) {
							message.setTitle("You chose to set a warning value!");
							_e.getChannel().sendMessage(message.setDescription("Please choose a numerical warning value that is lower or identical to the max warning value!").build()).queue();
							cache.updateDescription("warning"+user_id).setExpiration(180000);
							Hashes.addTempCache(key, cache);
						}
						else {
							UserPrivs.throwNotEnoughPrivilegeError(_e, warningLevel);
							Hashes.clearTempCache(key);
						}
					}
					case "mute" -> {
						Member member = _e.getGuild().getMemberById(user_id);
						if(member == null)
							_e.getChannel().sendMessage(message.setTitle("Warning!").setColor(Color.ORANGE).setDescription("This user has left the server!").build()).queue();
						if(member != null && !_e.getGuild().getSelfMember().canInteract(_e.getGuild().getMemberById(user_id))) {
							message.setTitle("User can't get muted").setColor(Color.RED);
							_e.getChannel().sendMessage(message.setDescription("The mute parameter can't be used because this user has higher privileges!").build()).queue();
							Hashes.clearTempCache(key);
							return;
						}
						if(_e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
							final var muteLevel = GuildIni.getUserMuteLevel(_e.getGuild().getIdLong());
							if(UserPrivs.comparePrivilege(_e.getMember(), muteLevel) || GuildIni.getAdmin(_e.getGuild().getIdLong()) == _e.getMember().getUser().getIdLong()) {
								message.setTitle("You chose to mute!");
								if(GuildIni.getForceReason(_e.getGuild().getIdLong())) {
									_e.getChannel().sendMessage(message.setDescription("Please provide a reason!").build()).queue();
									cache.updateDescription("mute-reason"+cache.getAdditionalInfo().replaceAll("[^0-9]*", "")).setExpiration(180000);
									Hashes.addTempCache(key, cache);
								}
								else {
									message.addField("YES", "Provide a reason", true);
									message.addField("NO", "Don't provide a reason", true);
									_e.getChannel().sendMessage(message.setDescription("Do you wish to provide a reason to the mute?").build()).queue();
									cache.updateDescription("mute"+user_id).setExpiration(180000);
									Hashes.addTempCache(key, cache);
								}
							}
							else {
								UserPrivs.throwNotEnoughPrivilegeError(_e, muteLevel);
								Hashes.clearTempCache(key);
							}
						}
						else {
							message.setTitle("User can't get muted").setColor(Color.RED);
							_e.getChannel().sendMessage(message.setDescription("The mute parameter can't be used because the MANAGE ROLES permission is missing!").build()).queue();
							logger.warn("MANAGE ROLES permission required to mute a user in guild {}!", _e.getGuild().getId());
							Hashes.clearTempCache(key);
						}
					}
					case "unmute" -> {
						if(_e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
							final var unmuteLevel = GuildIni.getUserUnmuteLevel(_e.getGuild().getIdLong());
							if(UserPrivs.comparePrivilege(_e.getMember(), unmuteLevel) || GuildIni.getAdmin(_e.getGuild().getIdLong()) == _e.getMember().getUser().getIdLong()) {
								if(!Azrael.SQLisBanned(user_id, _e.getGuild().getIdLong())) {
									EmbedBuilder notice = new EmbedBuilder().setColor(Color.BLUE);
									if(Azrael.SQLgetCustomMuted(user_id, _e.getGuild().getIdLong())) {
										if(STATIC.killThread("mute_gu"+_e.getGuild().getId()+"us"+user_id)) {
											notice.setTitle("Unmute action issued!");
											_e.getChannel().sendMessage(notice.setDescription("Action issued to interrupt the mute! Please note that warnings don't get reverted while applying a custom mute time!").build()).queue();
										}
										else {
											notice.setColor(Color.RED).setTitle("Member is not muted!");
											_e.getChannel().sendMessage(notice.setDescription("The member is not muted! Please apply this action, if you want to unmute a muted user!").build()).queue();
										}
									}
									else if(Azrael.SQLgetMuted(user_id, _e.getGuild().getIdLong())) {
										if(STATIC.killThread("mute_gu"+_e.getGuild().getId()+"us"+user_id)) {
											notice.setTitle("Unmute action issued!");
											_e.getChannel().sendMessage(notice.setDescription("Action issued to interrupt the mute! Undoing the previous warning!").build()).queue();
											var warning = Azrael.SQLgetWarning(user_id, _e.getGuild().getIdLong());
											if(warning == 1) {
												Azrael.SQLDeleteData(user_id, _e.getGuild().getIdLong());
											}
											else if(warning > 1) {
												Timestamp timestamp = new Timestamp(System.currentTimeMillis());
												Azrael.SQLInsertData(user_id, _e.getGuild().getIdLong(), warning-1, 1, timestamp, timestamp, false, false);
											}
											else {
												notice.setColor(Color.RED).setTitle("Member is not muted!");
												_e.getChannel().sendMessage(notice.setDescription("The member is not muted! Please apply this action, if you want to unmute a muted user!").build()).queue();
											}
										}
										else if(GuildIni.getOverrideBan(_e.getGuild().getIdLong()) && Azrael.SQLgetWarning(user_id, _e.getGuild().getIdLong()) == Azrael.SQLgetMaxWarning(_e.getGuild().getIdLong()) && Azrael.SQLgetData(user_id, _e.getGuild().getIdLong()).getUnmute() == null) {
											Azrael.SQLDeleteData(user_id, _e.getGuild().getIdLong());
											var mute_role = DiscordRoles.SQLgetRoles(_e.getGuild().getIdLong()).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null);
											if(mute_role != null) {
												_e.getGuild().removeRoleFromMember(_e.getGuild().getMemberById(user_id), _e.getGuild().getRoleById(mute_role.getRole_ID())).queue();
												long assignedRole = 0;
												Rank user_details = RankingSystem.SQLgetWholeRankView(user_id, _e.getGuild().getIdLong());
												if(user_details != null) {
													assignedRole = user_details.getCurrentRole();
												}
												if(assignedRole != 0) {
													Role role = _e.getGuild().getRoleById(assignedRole);
													if(role != null)
														_e.getGuild().addRoleToMember(_e.getGuild().getMemberById(user_id), _e.getGuild().getRoleById(assignedRole)).queue();
												}
												_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.GREEN).setTitle("User unmuted!").setThumbnail(IniFileReader.getUnmuteThumbnail()).setDescription("["+new Timestamp(System.currentTimeMillis()).toString()+"] **"+_e.getGuild().getMemberById(user_id).getUser().getName()+"#"+_e.getGuild().getMemberById(user_id).getUser().getDiscriminator() + "** with the ID Number **" +user_id+ "** has been unmuted from his/her infinite mute!").build()).queue();
											}
											else {
												_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Error!").setThumbnail(IniFileReader.getUnmuteThumbnail()).setDescription("Mute role doesn't exist! Please verify that the mute role is still registered! Action aborted!").build()).queue();
											}
										}
										else {
											notice.setColor(Color.RED).setTitle("Member is not muted!");
											_e.getChannel().sendMessage(notice.setDescription("The member is not muted! Please apply this action, if you want to unmute a muted user!").build()).queue();
										}
									}
									else {
										notice.setColor(Color.RED).setTitle("Member is not muted!");
										_e.getChannel().sendMessage(notice.setDescription("The member is not muted! Please apply this action, if you want to unmute a muted user!").build()).queue();
									}
								}
								else {
									EmbedBuilder error = new EmbedBuilder().setColor(Color.RED);
									_e.getChannel().sendMessage(error.setDescription("Action can't be executed! The user is currently banned").build()).queue();
								}
								logger.debug("{} has used the unmute action on {}", _e.getMember().getUser().getId(), cache.getAdditionalInfo());
							}
							else {
								UserPrivs.throwNotEnoughPrivilegeError(_e, unmuteLevel);
							}
						}
						else {
							message.setTitle("User can't get unmuted").setColor(Color.RED);
							_e.getChannel().sendMessage(message.setDescription("The unmute parameter can't be used because the MANAGE ROLES permission is missing!").build()).queue();
							logger.warn("MANAGE ROLES permission required to unmute a user in guild {}!", _e.getGuild().getId());
						}
						Hashes.clearTempCache(key);
					}
					case "ban" -> {
						Member member = _e.getGuild().getMemberById(user_id);
						if(member == null)
							_e.getChannel().sendMessage(message.setTitle("Warning!").setColor(Color.ORANGE).setDescription("This user has left the server!").build()).queue();
						if(member != null && !_e.getGuild().getSelfMember().canInteract(_e.getGuild().getMemberById(user_id))) {
							message.setTitle("User can't get banned!").setColor(Color.RED);
							_e.getChannel().sendMessage(message.setDescription("The ban parameter can't be used because this user has higher permissions!").build()).queue();
							Hashes.clearTempCache(key);
							return;
						}
						if(_e.getGuild().getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
							final var banLevel = GuildIni.getUserBanLevel(_e.getGuild().getIdLong());
							if(UserPrivs.comparePrivilege(_e.getMember(), banLevel) || GuildIni.getAdmin(_e.getGuild().getIdLong()) == _e.getMember().getUser().getIdLong()) {
								message.setTitle("You chose to ban!");
								if(GuildIni.getForceReason(_e.getGuild().getIdLong())) {
									_e.getChannel().sendMessage(message.setDescription("Please provide a reason!").build()).queue();
									cache.updateDescription("ban-reason"+cache.getAdditionalInfo().replaceAll("[^0-9]*", "")).setExpiration(180000);
									Hashes.addTempCache(key, cache);
								}
								else {
									message.addField("YES", "Provide a reason", true);
									message.addField("NO", "Don't provide a reason", true);
									_e.getChannel().sendMessage(message.setDescription("Do you wish to provide a reson to the ban?").build()).queue();
									cache.updateDescription("ban"+user_id).setExpiration(180000);
									Hashes.addTempCache(key, cache);
								}
							}
							else {
								UserPrivs.throwNotEnoughPrivilegeError(_e, banLevel);
								Hashes.clearTempCache(key);
							}
						}
						else {
							message.setTitle("User can't get banned!").setColor(Color.RED);
							_e.getChannel().sendMessage(message.setDescription("The ban parameter can't be used because the BAN MEMBERS permission is missing!").build()).queue();
							logger.warn("BAN MEMBERS permission required to ban a user in guild {}!", _e.getGuild().getId());
							Hashes.clearTempCache(key);
						}
					}
					case "kick" -> {
						Member member = _e.getGuild().getMemberById(user_id);
						if(member == null)
							_e.getChannel().sendMessage(message.setTitle("Warning!").setColor(Color.ORANGE).setDescription("This user has left the server!").build()).queue();
						if(member != null && !_e.getGuild().getSelfMember().canInteract(_e.getGuild().getMemberById(user_id))) {
							message.setTitle("User can't be kicked!").setColor(Color.RED);
							_e.getChannel().sendMessage(message.setDescription("The kick parameter can't be used because this user has higher permissions!").build()).queue();
							Hashes.clearTempCache(key);
							return;
						}
						if(_e.getGuild().getSelfMember().hasPermission(Permission.KICK_MEMBERS)) {
							final var kickLevel = GuildIni.getUserKickLevel(_e.getGuild().getIdLong());
							if(UserPrivs.comparePrivilege(_e.getMember(), kickLevel) || GuildIni.getAdmin(_e.getGuild().getIdLong()) == _e.getMember().getUser().getIdLong()) {
								message.setTitle("You chose to kick!");
								if(GuildIni.getForceReason(_e.getGuild().getIdLong())) {
									_e.getChannel().sendMessage(message.setDescription("Please provide a reason!").build()).queue();
									cache.updateDescription("kick-reason"+cache.getAdditionalInfo().replaceAll("[^0-9]*", "")).setExpiration(180000);
									Hashes.addTempCache(key, cache);
								}
								else {
									message.addField("YES", "Provide a reason", true);
									message.addField("NO", "Don't provide a reason", true);
									_e.getChannel().sendMessage(message.setDescription("Do you wish to provide a reson to the kick?").build()).queue();
									cache.updateDescription("kick"+user_id).setExpiration(180000);
									Hashes.addTempCache(key, cache);
								}
							}
							else {
								UserPrivs.throwNotEnoughPrivilegeError(_e, kickLevel);
								Hashes.clearTempCache(key);
							}
						}
						else {
							message.setTitle("User can't be kicked!").setColor(Color.RED);
							_e.getChannel().sendMessage(message.setDescription("The kick parameter can't be used because the KICK MEMBERS permission is missing!").build()).queue();
							logger.warn("KICK MEMBERS permission required to kick a user in guild {}!", _e.getGuild().getId());
							Hashes.clearTempCache(key);
						}
					}
					case "history" -> {
						final var historyLevel = GuildIni.getUserHistoryLevel(_e.getGuild().getIdLong());
						if(UserPrivs.comparePrivilege(_e.getMember(), historyLevel) || GuildIni.getAdmin(_e.getGuild().getIdLong()) == _e.getMember().getUser().getIdLong()) {
							message.setTitle("You chose to display the history!");
							StringBuilder out = new StringBuilder();
							for(var history : Azrael.SQLgetHistory(user_id, _e.getGuild().getIdLong())) {
								out.append(history.getTime()+": **"+history.getType()+(history.getPenalty() != 0 ? " for "+history.getPenalty()+" minutes" : "")+"**\nReason: **"+history.getReason()+"**\n\n");
							}
							if(out.length() > 0)
								_e.getChannel().sendMessage(message.setDescription("Here the requested history of this user\n\n"+out.toString()).build()).queue();
							else
								_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("History is empty!").setDescription("The history of this user is empty! Nothing to display!").build()).queue();
							Hashes.clearTempCache(key);
						}
						else {
							UserPrivs.throwNotEnoughPrivilegeError(_e, historyLevel);
							Hashes.clearTempCache(key);
						}
					}
					case "watch" -> {
						final var watchLevel = GuildIni.getUserWatchLevel(_e.getGuild().getIdLong());
						if(UserPrivs.comparePrivilege(_e.getMember(), watchLevel) || GuildIni.getAdmin(_e.getGuild().getIdLong()) == _e.getMember().getUser().getIdLong()) {
							if(!GuildIni.getCacheLog(_e.getGuild().getIdLong())) {
								_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.YELLOW).setTitle("Warning!").setDescription("The CacheLog is disabled. Please ensure to have it enabled or logged messages won't get displayed!").build()).queue();
							}
							message.setTitle("You chose to watch this user!");
							message.setDescription("Now please select a log level.\n1: Log only deleted messages from this user\n2: log all written messages from a user!");
							_e.getChannel().sendMessage(message.build()).queue();
							cache.updateDescription("watch"+user_id);
							Hashes.addTempCache(key, cache);
						}
						else {
							UserPrivs.throwNotEnoughPrivilegeError(_e, watchLevel);
							Hashes.clearTempCache(key);
						}
					}
					case "unwatch" -> {
						final var unwatchLevel = GuildIni.getUserUnwatchLevel(_e.getGuild().getIdLong());
						if(UserPrivs.comparePrivilege(_e.getMember(), unwatchLevel) || GuildIni.getAdmin(_e.getGuild().getIdLong()) == _e.getMember().getUser().getIdLong()) {
							var watchedMember = Azrael.SQLgetWatchlist(user_id, _e.getGuild().getIdLong());
							if(watchedMember != null) {
								if(!watchedMember.hasHigherPrivileges()) {
									//No higher privileges required, if it's set to log in the trash channel
									if(Azrael.SQLDeleteWatchlist(user_id, _e.getGuild().getIdLong()) > 0) {
										//Successful unwatch operation
										Hashes.removeWatchlist(_e.getGuild().getId()+"-"+user_id);
										message.setTitle("Success!");
										_e.getChannel().sendMessage(message.setDescription("This user is not being watched anymore!").build()).queue();
										logger.debug("The user {} has been removed from the watchlist for the guild {}", user_id, _e.getGuild().getId());
									}
									else {
										//Error DB update
										message.setTitle("Error!").setColor(Color.RED);
										_e.getChannel().sendMessage("An internal error occurred! The Azrael.watchlist table couldn't be updated!");
										logger.error("An internal error occurred! User {} in guild {} couldn't be inserted into Azrael.watchlist", user_id, _e.getGuild().getId());
									}
								}
								else if(UserPrivs.comparePrivilege(_e.getMember(), GuildIni.getUserUseWatchChannelLevel(_e.getGuild().getIdLong()))) {
									//Higher privileges required to unwatch a member that is assigned to a separate watchlist channel
									if(Azrael.SQLDeleteWatchlist(user_id, _e.getGuild().getIdLong()) > 0) {
										//Successful unwatch operation
										Hashes.removeWatchlist(_e.getGuild().getId()+"-"+user_id);
										message.setTitle("Success!");
										_e.getChannel().sendMessage(message.setDescription("This user is not being watched anymore!").build()).queue();
										logger.debug("The user {} has been removed from the watchlist for the guild {}", user_id, _e.getGuild().getId());
									}
									else {
										//Error DB update
										message.setTitle("Error!").setColor(Color.RED);
										_e.getChannel().sendMessage("An internal error occurred! The Azrael.watchlist table couldn't be updated!");
										logger.error("An internal error occurred! User {} in guild {} couldn't be inserted into Azrael.watchlist", user_id, _e.getGuild().getId());
									}
								}
								else {
									//error: the user isn't being watched
									message.setTitle("User is not being watched!").setColor(Color.RED);
									_e.getChannel().sendMessage(message.setDescription("The user is currently not being watched! No actions required!").build()).queue();
								}
							}
							else {
								//error: the user isn't being watched
								message.setTitle("User is not being watched!").setColor(Color.RED);
								_e.getChannel().sendMessage(message.setDescription("The user is currently not being watched! Not actions required!").build()).queue();
							}
						}
						else {
							UserPrivs.throwNotEnoughPrivilegeError(_e, unwatchLevel);
						}
						Hashes.clearTempCache(key);
					}
					case "gift-experience" -> {
						final var giftExperienceLevel = GuildIni.getUserGiftExperienceLevel(_e.getGuild().getIdLong());
						if(UserPrivs.comparePrivilege(_e.getMember(), giftExperienceLevel) || GuildIni.getAdmin(_e.getGuild().getIdLong()) == _e.getMember().getUser().getIdLong()) {
							if(guild_settings.getRankingState()) {
								message.setTitle("You chose to gift experience points!");
								_e.getChannel().sendMessage(message.setDescription("Choose a number of experience points to add to the total number of experience points").build()).queue();
								cache.updateDescription("gift-experience"+user_id).setExpiration(180000);
								Hashes.addTempCache(key, cache);
							}
							else {
								denied.setTitle("Access Denied!");
								_e.getChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention()+" This action can't be used because the ranking system is disabled! Please choose a different action!").build()).queue();
								Hashes.clearTempCache(key);
							}
						}
						else {
							UserPrivs.throwNotEnoughPrivilegeError(_e, giftExperienceLevel);
							Hashes.clearTempCache(key);
						}
					}
					case "set-experience" -> {
						final var setExperienceLevel = GuildIni.getUserSetExperienceLevel(_e.getGuild().getIdLong());
						if(UserPrivs.comparePrivilege(_e.getMember(), setExperienceLevel) || GuildIni.getAdmin(_e.getGuild().getIdLong()) == _e.getMember().getUser().getIdLong()) {
							if(guild_settings.getRankingState()) {
								message.setTitle("You chose to set experience points!");
								_e.getChannel().sendMessage(message.setDescription("Choose a number of experience points to set for the user").build()).queue();
								cache.updateDescription("set-experience"+user_id).setExpiration(180000);
								Hashes.addTempCache(key, cache);
							}
							else {
								denied.setTitle("Access Denied!");
								_e.getChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention()+" This action can't be used because the ranking system is disabled! Please choose a different action!").build()).queue();
								Hashes.clearTempCache(key);
							}
						}
						else {
							UserPrivs.throwNotEnoughPrivilegeError(_e, setExperienceLevel);
							Hashes.clearTempCache(key);
						}
					}
					case "set-level" -> {
						final var setLevelLevel = GuildIni.getUserSetLevelLevel(_e.getGuild().getIdLong());
						if(UserPrivs.comparePrivilege(_e.getMember(), setLevelLevel) || GuildIni.getAdmin(_e.getGuild().getIdLong()) == _e.getMember().getUser().getIdLong()) {
							if(guild_settings.getRankingState()) {
								message.setTitle("You chose to set a level!");
								_e.getChannel().sendMessage(message.setDescription("Choose a level to assign the user").build()).queue();
								cache.updateDescription("set-level"+user_id).setExpiration(180000);
								Hashes.addTempCache(key, cache);
							}
							else {
								denied.setTitle("Access Denied!");
								_e.getChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention()+" This action can't be used because the ranking system is disabled! Please choose a different action!").build()).queue();
								Hashes.clearTempCache(key);
							}
						}
						else {
							UserPrivs.throwNotEnoughPrivilegeError(_e, setLevelLevel);
							Hashes.clearTempCache(key);
						}
					}
					case "gift-currency" -> {
						final var giftCurrencyLevel = GuildIni.getUserGiftCurrencyLevel(_e.getGuild().getIdLong());
						if(UserPrivs.comparePrivilege(_e.getMember(), giftCurrencyLevel) || GuildIni.getAdmin(_e.getGuild().getIdLong()) == _e.getMember().getUser().getIdLong()) {
							if(guild_settings.getRankingState()) {
								message.setTitle("You chose to gift money!");
								_e.getChannel().sendMessage(message.setDescription("Choose the amount of money to gift the user").build()).queue();
								cache.updateDescription("gift-currency"+user_id).setExpiration(180000);
								Hashes.addTempCache(key, cache);
							}
							else {
								denied.setTitle("Access Denied!");
								_e.getChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention()+" This action can't be used because the ranking system is disabled! Please choose a different action!").build()).queue();
								Hashes.clearTempCache(key);
							}
						}
						else {
							UserPrivs.throwNotEnoughPrivilegeError(_e, giftCurrencyLevel);
							Hashes.clearTempCache(key);
						}
					}
					case "set-currency" -> {
						final var setCurrencyLevel = GuildIni.getUserSetCurrencyLevel(_e.getGuild().getIdLong());
						if(UserPrivs.comparePrivilege(_e.getMember(), setCurrencyLevel) || GuildIni.getAdmin(_e.getGuild().getIdLong()) == _e.getMember().getUser().getIdLong()) {
							if(guild_settings.getRankingState()) {
								message.setTitle("You chose to set money!");
								_e.getChannel().sendMessage(message.setDescription("Choose the amount of money to set for the user").build()).queue();
								cache.updateDescription("set-currency"+user_id).setExpiration(180000);
								Hashes.addTempCache(key, cache);
							}
							else {
								denied.setTitle("Access Denied!");
								_e.getChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(setCurrencyLevel, _e.getGuild())).build()).queue();
								Hashes.clearTempCache(key);
							}
						}
						else {
							UserPrivs.throwNotEnoughPrivilegeError(_e, setCurrencyLevel);
							Hashes.clearTempCache(key);
						}
					}
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("delete-messages-question")) {
				if(_message.equalsIgnoreCase("yes")) {
					if(cache.getAdditionalInfo3().length() == 0) {
						message.setTitle("You chose to delete a number of messages!");
						_e.getChannel().sendMessage(message.setDescription("Please choose how many messages should be removed between 1 and 100!").build()).queue();
						cache.updateDescription("delete-messages"+user_id).setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					else {
						message.setTitle("Delete Messages!");
						_e.getChannel().sendMessage(message.setDescription("Messages are being deleted...").build()).queue();
						deleteMessages(_e, user_id, _message, Integer.parseInt(cache.getAdditionalInfo3()), message, key, true);
					}
				}
				else if(_message.equalsIgnoreCase("no")) {
					_e.getChannel().sendMessage(message.setDescription("Delete messages action aborted!").build()).queue();
					Hashes.clearTempCache(key);
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("delete-messages")) {
				deleteMessages(_e, user_id, _message, 0, message, key, false);
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("warning")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					if(!Azrael.SQLisBanned(user_id, _e.getGuild().getIdLong()) && ( _e.getGuild().getMemberById(user_id) == null || !UserPrivs.isUserMuted(_e.getGuild().getMemberById(user_id)))) {
						int db_warning = Azrael.SQLgetData(user_id, _e.getGuild().getIdLong()).getWarningID();
						if(db_warning != 0) {
							int warning_id = Integer.parseInt(_message.replaceAll("[^0-9]*", ""));
							int max_warning_id = Azrael.SQLgetMaxWarning(_e.getGuild().getIdLong());
							if(warning_id == 0) {
								if(Azrael.SQLDeleteData(user_id, _e.getGuild().getIdLong()) > 0) {
									_e.getChannel().sendMessage("The warnings of this user has been cleared!").queue();
									logger.debug("{} has cleared the warnings from {} in guild {}", _e.getMember().getUser().getId(), user_id, _e.getGuild().getId());
								}
								else {
									_e.getChannel().sendMessage("An internal error occurred. The warnings of this user couldn't be cleared from Azrael.bancollect").queue();
									logger.error("The warnings of the user {} in guild {} couldn't be cleared", user_id, _e.getGuild().getId());
								}
							}
							else if(warning_id <= max_warning_id) {
								if(Azrael.SQLUpdateWarning(user_id, _e.getGuild().getIdLong(), warning_id) > 0) {
									_e.getChannel().sendMessage("Warning value "+warning_id+" has been set!").queue();
									logger.debug("{} has set the warning level to {} from {} in guild {}", _e.getMember().getUser().getId(), warning_id, user_id, _e.getGuild().getId());
								}
								else {
									_e.getChannel().sendMessage("An internal error occurred. The warning level of the selected user couldn't be updated on Azrael.bancollect").queue();
									logger.error("Warning on user {} couldn't be updated on Azrael.bancollect in guild {}", user_id, _e.getGuild().getId());
								}
							}
							else {
								if(Azrael.SQLUpdateWarning(user_id, _e.getGuild().getIdLong(), max_warning_id) > 0) {
									_e.getChannel().sendMessage("The max possible value "+max_warning_id+" has been set because your input exceeded the max possible warning!").queue();
									logger.debug("{} has set the warning level to {} from {} in guild {}", _e.getMember().getUser().getId(), max_warning_id, user_id, _e.getGuild().getId());
								}
								else {
									_e.getChannel().sendMessage("An internal error occurred. The warning level of the selected user couldn't be updated on Azrael.bancollect").queue();
									logger.error("Warning on user {} couldn't be updated on Azrael.bancollect in guild {}", user_id, _e.getGuild().getId());
								}
							}
						}
						else {
							if(UserPrivs.comparePrivilege(_e.getMember(), GuildIni.getUserWarningForceLevel(_e.getGuild().getIdLong())) || _e.getMember().getUser().getIdLong() == GuildIni.getAdmin(_e.getGuild().getIdLong())) {
								int warning_id = Integer.parseInt(_message.replaceAll("[^0-9]*", ""));
								int max_warning_id = Azrael.SQLgetMaxWarning(_e.getGuild().getIdLong());
								if(warning_id <= max_warning_id) {
									if(Azrael.SQLInsertData(user_id, _e.getGuild().getIdLong(), warning_id, 1, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), false, false) > 0) {
										_e.getChannel().sendMessage("Warning value "+warning_id+" has been set!").queue();
										logger.debug("{} has set the warning level to {} from {} in guild {}", _e.getMember().getUser().getId(), warning_id, user_id, _e.getGuild().getId());
									}
									else {
										_e.getChannel().sendMessage("An internal error occurred. The warning level of the selected user couldn't be inserted in Azrael.bancollect").queue();
										logger.error("Warning on user {} couldn't be inserted in Azrael.bancollect in guild {}", user_id, _e.getGuild().getId());
									}
								}
								else {
									if(Azrael.SQLInsertData(user_id, _e.getGuild().getIdLong(), max_warning_id, 1, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), false, false) > 0) {
										_e.getChannel().sendMessage("The max possible value "+max_warning_id+" has been set because your input exceeded the max possible warning!").queue();
										logger.debug("{} has set the warning level to {} from {} in guild {}", _e.getMember().getUser().getId(), max_warning_id, user_id, _e.getGuild().getId());
									}
									else {
										_e.getChannel().sendMessage("An internal error occurred. The warning level of the selected user couldn't be inserted in Azrael.bancollect").queue();
										logger.error("Warning on user {} couldn't be inserted in Azrael.bancollect in guild {}", user_id, _e.getGuild().getId());
									}
								}
							}
							else {
								_e.getChannel().sendMessage("A custom warning can't be set because the player was never muted or was freshly unbanned!").queue();
								logger.warn("{} got no available warnings to be edit", user_id);
							}
						}
					}
					else {
						_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Warning couldn't be edited for this user because the affected user is currently muted or banned!").build()).queue();
					}
					Hashes.clearTempCache(key);
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("mute")) {
				if(comment.equals("yes")) {
					message.setTitle("You chose to provide a reason!");
					_e.getChannel().sendMessage(message.setDescription("Please provide a reason!").build()).queue();
					cache.updateDescription("mute-reason"+user_id).setExpiration(180000);
					Hashes.addTempCache(key, cache);
				}
				else if(comment.equals("no")) {
					message.setTitle("Mute action!");
					message.addField("YES", "Provide a mute time", true);
					message.addField("NO", "Don't provide a mute time", true);
					_e.getChannel().sendMessage(message.setDescription("Do you wish to provide a self chosen mute timer? By providing a self chosen mute timer, the warning value won't increment unless the user has been never warned before!").build()).queue();
					cache.updateDescription("mute-action"+user_id).setExpiration(180000);
					Hashes.addTempCache(key, cache);
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("mute-reason")) {
				message.setTitle("Mute action!");
				message.addField("YES", "Provide a mute time", true);
				message.addField("NO", "Don't provide a mute time", true);
				if(GuildIni.getOverrideBan(_e.getGuild().getIdLong()))
					message.addField("PERM", "Mute permanently", true);
				_e.getChannel().sendMessage(message.setDescription("Do you wish to provide a self chosen mute timer? By providing a self chosen mute timer, the warning value won't increment unless the user has been never warned before!").build()).queue();
				cache.updateDescription("mute-action"+user_id).updateDescription2(_message).setExpiration(180000);
				Hashes.addTempCache(key, cache);
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("mute-action")) {
				if(comment.equals("yes")) {
					message.setTitle("You chose to provide a mute time!");
					_e.getChannel().sendMessage(message.setDescription("Please provide a mute time in the following format:\n\n"
							+ "to set the time in minutes: eg. **1m**\n"
							+ "to set the time in hours: eg.**1h**\n"
							+ "to set the time in days: eg. **1d**").build()).queue();
					cache.updateDescription("mute-time"+user_id).setExpiration(180000);
					Hashes.addTempCache(key, cache);
				}
				else if(comment.equals("no") || (GuildIni.getOverrideBan(_e.getGuild().getIdLong()) && comment.equals("perm"))) {
					Member member = _e.getGuild().getMemberById(user_id);
					if(member != null && !_e.getGuild().getSelfMember().canInteract(_e.getGuild().getMemberById(user_id))) {
						message.setTitle("User can't get muted").setColor(Color.RED);
						_e.getChannel().sendMessage(message.setDescription("The mute role couldn't be assigned because this user has higher privileges!").build()).queue();
						Hashes.clearTempCache(key);
						return;
					}
					if(_e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
						var permMute = false;
						if(!comment.equals("no")) {
							var timestamp = new Timestamp(System.currentTimeMillis());
							if(Azrael.SQLInsertData(member.getUser().getIdLong(), _e.getGuild().getIdLong(), Azrael.SQLgetMaxWarning(_e.getGuild().getIdLong()), 1, timestamp, timestamp, false, false) > 0) {
								permMute = true;
							}
							else {
								
							}
						}
						var mute_role_id = DiscordRoles.SQLgetRoles(_e.getGuild().getIdLong()).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null);
						if(mute_role_id != null) {
							if(member != null) {
								Hashes.addTempCache("mute_time_gu"+_e.getGuild().getId()+"us"+user_id, new Cache(_e.getMember().getAsMention(), (cache.getAdditionalInfo2().length() > 0 ? cache.getAdditionalInfo2() : "No reason has been provided!")));
								_e.getGuild().addRoleToMember(_e.getGuild().getMemberById(user_id), _e.getGuild().getRoleById(mute_role_id.getRole_ID())).queue();
								var mute_time = (long)Azrael.SQLgetWarning(_e.getGuild().getIdLong(), Azrael.SQLgetData(user_id, _e.getGuild().getIdLong()).getWarningID()+1).getTimer();
								Azrael.SQLInsertHistory(user_id, _e.getGuild().getIdLong(), "mute", (cache.getAdditionalInfo2().length() > 0 ? cache.getAdditionalInfo2() : "No reason has been provided!"), (mute_time/1000/60));
								_e.getChannel().sendMessage(message.setDescription((permMute ? "Perm mute" : "Mute")+" order has been issued!").build()).queue();
								checkIfDeleteMessagesAfterAction(_e, cache, user_id, _message, message, key);
							}
							else {
								_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Action cannot be executed. Do you wish to apply the mute role after the user has rejoined the server?").addField("YES", "", true).addField("NO", "", true).build()).queue();
								cache.updateDescription("mute-delay"+user_id).setExpiration(180000);
								Hashes.addTempCache(key, cache);
							}
						}
						else {
							_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("No mute role has been registered! Please register a mute role before applying a mute on a user!").build()).queue();
							Hashes.clearTempCache(key);
						}
					}
					else {
						message.setTitle("User can't get muted").setColor(Color.RED);
						_e.getChannel().sendMessage(message.setDescription("The user couldn't be muted because the MANAGE ROLES permission is missing!").build()).queue();
						logger.warn("MANAGE ROLES permission required to mute a user in guild {}!", _e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("mute-time")) {
				if(_message.replaceAll("[0-9]*", "").length() == 1 && (comment.endsWith("m") || comment.endsWith("h") || comment.endsWith("d"))) {
					Member member = _e.getGuild().getMemberById(user_id);
					if(member != null && !_e.getGuild().getSelfMember().canInteract(member)) {
						message.setTitle("User can't get muted").setColor(Color.RED);
						_e.getChannel().sendMessage(message.setDescription("The mute role couldn't be assigned because this user has higher privileges!").build()).queue();
						Hashes.clearTempCache(key);
						return;
					}
					if(_e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
						long mute_time = (Long.parseLong(_message.replaceAll("[^0-9]*", ""))*1000);
						if(comment.endsWith("m")) {
							mute_time *= 60;
						}
						else if(comment.endsWith("h")) {
							mute_time = mute_time*60*60;
						}
						else if(comment.endsWith("d")) {
							mute_time = mute_time*60*60*24;
						}
						Timestamp timestamp = new Timestamp(System.currentTimeMillis());
						Timestamp unmute_timestamp = new Timestamp(System.currentTimeMillis()+mute_time);
						var mute_role_id = DiscordRoles.SQLgetRoles(_e.getGuild().getIdLong()).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null);
						if(mute_role_id != null) {
							if(_e.getGuild().getMemberById(user_id) != null) {
								Hashes.addTempCache("mute_time_gu"+_e.getGuild().getId()+"us"+user_id, new Cache(""+mute_time, _e.getMember().getAsMention(), (cache.getAdditionalInfo2().length() > 0 ? cache.getAdditionalInfo2() : "No reason has been provided!")));
								if(Azrael.SQLgetData(user_id, _e.getGuild().getIdLong()).getWarningID() != 0) {
									if(Azrael.SQLUpdateUnmute(user_id, _e.getGuild().getIdLong(), timestamp, unmute_timestamp, true, true) == 0) {
										logger.error("The unmute timer couldn't be updated from user {} in guild {} for the table Azrael.bancollect", user_id, _e.getGuild().getId());
										_e.getChannel().sendMessage("An internal error occurred. The unmute time couldn't be updated on Azrael.bancollect").queue();
									}
								}
								else {
									if(Azrael.SQLInsertData(user_id, _e.getGuild().getIdLong(), 1, 1, timestamp, unmute_timestamp, true, true) == 0) {
										logger.error("muted user {} couldn't be inserted into Azrael.bancollect for guild {}", user_id, _e.getGuild().getName());
										_e.getChannel().sendMessage("An internal error occurred. Muted user couldn't be inserted into Azrael.bancollect").queue();
									}
								}
								if(cache.getAdditionalInfo2().length() > 0) {
									_e.getGuild().addRoleToMember(_e.getGuild().getMemberById(user_id), _e.getGuild().getRoleById(mute_role_id.getRole_ID())).reason(cache.getAdditionalInfo2()).queue();
									Azrael.SQLInsertHistory(_e.getGuild().getMemberById(user_id).getUser().getIdLong(), _e.getGuild().getIdLong(), "mute", cache.getAdditionalInfo2(), (mute_time/1000/60));
								}
								else {
									_e.getGuild().addRoleToMember(_e.getGuild().getMemberById(user_id), _e.getGuild().getRoleById(mute_role_id.getRole_ID())).reason("No reason has been provided!").queue();
									Azrael.SQLInsertHistory(_e.getGuild().getMemberById(user_id).getUser().getIdLong(), _e.getGuild().getIdLong(), "mute", "No reason has been provided!", (mute_time/1000/60));
								}
								_e.getChannel().sendMessage(message.setDescription("Mute order has been issued!").build()).queue();
								logger.debug("{} has muted {} in guild {}", _e.getMember().getUser().getId(), user_id, _e.getGuild().getId());
								checkIfDeleteMessagesAfterAction(_e, cache, user_id, _message, message, key);
							}
							else {
								_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Action cannot be executed. Do you wish to apply the mute role after the user has rejoined the server?").addField("YES", "", true).addField("NO", "", true).build()).queue();
								cache.updateDescription("mute-delay"+user_id).setExpiration(180000).updateDescription3(""+mute_time);
								Hashes.addTempCache(key, cache);
							}
						}
						else {
							_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("No mute role has been registered! Please register a mute role before applying a mute on a user!").build()).queue();
							Hashes.clearTempCache(key);
						}
					}
					else {
						message.setTitle("User can't get muted").setColor(Color.RED);
						_e.getChannel().sendMessage(message.setDescription("The user couldn't be muted because the MANAGE ROLES permission is missing!").build()).queue();
						logger.warn("MANAGE ROLES permission required to mute a user in guild {}!", _e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
				}
				else {
					_e.getChannel().sendMessage(_e.getMember().getAsMention()+" Please type a numerical value together with a time unit m, h or d!").queue();
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("mute-delay")) {
				if(_message.equalsIgnoreCase("yes")) {
					_e.getChannel().sendMessage(message.setDescription("Mute reminder has been set!").build()).queue();
					Hashes.addRejoinTask(_e.getGuild().getId()+"_"+user_id, new RejoinTask(user_id, _e.getGuild().getIdLong(), cache.getAdditionalInfo3(), _e.getMember().getAsMention(), "mute", cache.getAdditionalInfo2()));
					Hashes.clearTempCache(key);
				}
				else if(_message.equalsIgnoreCase("no")) {
					_e.getChannel().sendMessage(message.setDescription("Action aborted!").build()).queue();
					Hashes.clearTempCache(key);
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("ban")) {
				if(comment.equals("yes")) {
					message.setTitle("You chose to provide a reason!");
					_e.getChannel().sendMessage(message.setDescription("Please provide a reason!").build()).queue();
					cache.updateDescription("ban-reason"+user_id).setExpiration(180000);
					Hashes.addTempCache(key, cache);
				}
				else if(comment.equals("no")) {
					Member member = _e.getGuild().getMemberById(user_id);
					if(member != null && !_e.getGuild().getSelfMember().canInteract(member)) {
						message.setTitle("User can't get banned").setColor(Color.RED);
						_e.getChannel().sendMessage(message.setDescription("The ban can't be applied because this user has higher privileges!").build()).queue();
						Hashes.clearTempCache(key);
						return;
					}
					if(_e.getGuild().getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
						if(member != null) {
							Hashes.addTempCache("ban_gu"+_e.getGuild().getId()+"us"+user_id, new Cache(_e.getMember().getAsMention(), "No reason has been provided!"));
							PrivateChannel pc = _e.getGuild().getMemberById(user_id).getUser().openPrivateChannel().complete();
							int warning_id = Azrael.SQLgetData(user_id, _e.getGuild().getIdLong()).getWarningID();
							int max_warning_id = Azrael.SQLgetMaxWarning(_e.getGuild().getIdLong());
							if(warning_id == max_warning_id) {
								pc.sendMessage("You have been banned from "+_e.getGuild().getName()+", since you have exceeded the max amount of allowed mutes on this server. Thank you for your understanding.\n"
										+ "On a important note, this is an automatic reply. You'll receive no reply in any way.\n"
										+ (GuildIni.getBanSendReason(_e.getGuild().getIdLong()) ? "Provided reason: No reason has been provided!" : "")).complete();
							}
							else {
								pc.sendMessage("You have been banned from "+_e.getGuild().getName()+". Thank you for your understanding.\n"
										+ "On a important note, this is an automatic reply. You'll receive no reply in any way.\n"
										+ (GuildIni.getBanSendReason(_e.getGuild().getIdLong()) ? "Provided reason: No reason has been provided!" : "")).complete();
							}
							_e.getChannel().sendMessage(message.setDescription("Ban order has been issued!").build()).queue();
							_e.getGuild().ban(_e.getGuild().getMemberById(user_id), 0).reason("User has been banned with the bot command!").queue();
							Azrael.SQLInsertHistory(user_id, _e.getGuild().getIdLong(), "ban", "No reason has been provided!", 0);
							logger.debug("{} has banned {} from guild {}", _e.getMember().getUser().getId(), user_id, _e.getGuild().getId());
							checkIfDeleteMessagesAfterAction(_e, cache, user_id, _message, message, key);
						}
						else {
							_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Action cannot be executed. Do you wish to apply the ban after the user has rejoined the server?").addField("YES", "", true).addField("NO", "", true).build()).queue();
							cache.updateDescription("ban-delay"+user_id).updateDescription2("No reason has been provided!").setExpiration(180000);
							Hashes.addTempCache(key, cache);
						}
					}
					else {
						message.setTitle("User can't get banned").setColor(Color.RED);
						_e.getChannel().sendMessage(message.setDescription("The ban can't be applied because the BAN MEMBERS permission is missing!").build()).queue();
						logger.warn("BAN MEMBERS permission required to ban a user in guild {}!", _e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("ban-reason")) {
				Member member = _e.getGuild().getMemberById(user_id);
				if(member != null && !_e.getGuild().getSelfMember().canInteract(member)) {
					message.setTitle("User can't get banned").setColor(Color.RED);
					_e.getChannel().sendMessage(message.setDescription("The ban can't be applied because this user has higher privileges!").build()).queue();
					Hashes.clearTempCache(key);
					return;
				}
				if(_e.getGuild().getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
					if(member != null) {
						Hashes.addTempCache("ban_gu"+_e.getGuild().getId()+"us"+user_id, new Cache(_e.getMember().getAsMention(), _message));
						PrivateChannel pc = _e.getGuild().getMemberById(user_id).getUser().openPrivateChannel().complete();
						int warning_id = Azrael.SQLgetData(user_id, _e.getGuild().getIdLong()).getWarningID();
						int max_warning_id = Azrael.SQLgetMaxWarning(_e.getGuild().getIdLong());
						if(warning_id == max_warning_id) {
							pc.sendMessage("You have been banned from "+_e.getGuild().getName()+", since you have exceeded the max amount of allowed mutes on this server. Thank you for your understanding.\n"
									+ "On an important note, this is an automatic reply. You'll receive no reply in any way.\n"
									+ (GuildIni.getBanSendReason(_e.getGuild().getIdLong()) ? "Provided reason: "+_message : "")).complete();
						}
						else {
							pc.sendMessage("You have been banned from "+_e.getGuild().getName()+". Thank you for your understanding.\n"
									+ "On an important note, this is an automatic reply. You'll receive no reply in any way.\n"
									+ (GuildIni.getBanSendReason(_e.getGuild().getIdLong()) ? "Provided reason: "+_message : "")).complete();
						}
						_e.getChannel().sendMessage(message.setDescription("Ban order has been issued!").build()).queue();
						_e.getGuild().ban(_e.getGuild().getMemberById(user_id), 0).reason(_message).queue();
						Azrael.SQLInsertHistory(_e.getGuild().getMemberById(user_id).getUser().getIdLong(), _e.getGuild().getIdLong(), "ban", _message, 0);
						logger.debug("{} has banned {} in guild {}", _e.getMember().getUser().getId(), cache.getAdditionalInfo().replaceAll("[^0-9]",  ""), _e.getGuild().getId());
						checkIfDeleteMessagesAfterAction(_e, cache, user_id, _message, message, key);
					}
					else {
						_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Action cannot be executed. Do you wish to apply the ban after the user has rejoined the server?").addField("YES", "", true).addField("NO", "", true).build()).queue();
						cache.updateDescription("ban-delay"+user_id).setExpiration(180000).updateDescription2(_message);
						Hashes.addTempCache(key, cache);
					}
				}
				else {
					message.setTitle("User can't get banned").setColor(Color.RED);
					_e.getChannel().sendMessage(message.setDescription("The ban can't be applied because the BAN MEMBERS permission is missing!").build()).queue();
					logger.warn("BAN MEMBERS permission required to ban a user in guild {}!", _e.getGuild().getId());
					Hashes.clearTempCache(key);
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("ban-delay")) {
				if(_message.equalsIgnoreCase("yes")) {
					_e.getChannel().sendMessage(message.setDescription("Ban reminder has been set!").build()).queue();
					Hashes.addRejoinTask(_e.getGuild().getId()+"_"+user_id, new RejoinTask(user_id, _e.getGuild().getIdLong(), "", _e.getMember().getAsMention(), "ban", cache.getAdditionalInfo2()));
					Hashes.clearTempCache(key);
				}
				else if(_message.equalsIgnoreCase("no")) {
					_e.getChannel().sendMessage(message.setDescription("Action aborted!").build()).queue();
					Hashes.clearTempCache(key);
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("kick")) {
				if(comment.equals("yes")) {
					message.setTitle("You chose to provide a reason!");
					_e.getChannel().sendMessage(message.setDescription("Please provide a reason!").build()).queue();
					cache.updateDescription("kick-reason"+user_id).setExpiration(180000);
					Hashes.addTempCache(key, cache);
				}
				else if(comment.equals("no")) {
					Member member = _e.getGuild().getMemberById(user_id);
					if(member != null && !_e.getGuild().getSelfMember().canInteract(member)) {
						message.setTitle("User can't be kicked!").setColor(Color.RED);
						_e.getChannel().sendMessage(message.setDescription("Kick is not possible because this user has higher privileges!").build()).queue();
						Hashes.clearTempCache(key);
						return;
					}
					if(_e.getGuild().getSelfMember().hasPermission(Permission.KICK_MEMBERS)) {
						if(member != null) {
							_e.getChannel().sendMessage(message.setDescription("Kick order has been issued!").build()).queue();
							Hashes.addTempCache("kick_gu"+_e.getGuild().getId()+"us"+user_id, new Cache(_e.getMember().getAsMention(), "No reason has been provided!"));
							member.getUser().openPrivateChannel().complete()
							.sendMessage("You have been kicked from **"+_e.getGuild().getName()+"**.Thank you for your understanding.\n" 
									+ "On an important note, this is an automatic reply. You'll receive no reply in any way.\n"
									+ (GuildIni.getKickSendReason(_e.getGuild().getIdLong()) ? "Provided reason: No reason has been provided!" : "")).complete();
							_e.getGuild().kick(_e.getGuild().getMemberById(user_id)).reason("No reason has been provided!").queue();
							Azrael.SQLInsertHistory(_e.getGuild().getMemberById(user_id).getUser().getIdLong(), _e.getGuild().getIdLong(), "kick", "No reason has been provided!", 0);
							logger.debug("{} has kicked {} from guild {}", _e.getMember().getUser().getId(), user_id, _e.getGuild().getName());
							checkIfDeleteMessagesAfterAction(_e, cache, user_id, _message, message, key);
						}
						else {
							_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Action cannot be executed because the user can't be found on the server!").build()).queue();
							Hashes.clearTempCache(key);
						}
					}
					else {
						message.setTitle("User can't be kicked").setColor(Color.RED);
						_e.getChannel().sendMessage(message.setDescription("Kick is not possible because the KICK MEMBERS permission is missing!").build()).queue();
						logger.warn("KICK MEMBERS permission required to kick a user in guild {}!", _e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("kick-reason")) {
				Member member = _e.getGuild().getMemberById(user_id);
				if(member != null && !_e.getGuild().getSelfMember().canInteract(member)) {
					message.setTitle("User can't be kicked!").setColor(Color.RED);
					_e.getChannel().sendMessage(message.setDescription("Kick is not possible because this user has higher privileges!").build()).queue();
					Hashes.clearTempCache(key);
					return;
				}
				if(_e.getGuild().getSelfMember().hasPermission(Permission.KICK_MEMBERS)) {
					if(member != null) {
						_e.getChannel().sendMessage(message.setDescription("Kick order has been issued!").build()).queue();
						Hashes.addTempCache("kick_gu"+_e.getGuild().getId()+"us"+user_id, new Cache(_e.getMember().getAsMention(), _message));
						member.getUser().openPrivateChannel().complete()
						.sendMessage("You have been kicked from **"+_e.getGuild().getName()+"**.Thank you for your understanding.\n" 
								+ "On an important note, this is an automatic reply. You'll receive no reply in any way.\n"
								+ (GuildIni.getKickSendReason(_e.getGuild().getIdLong()) ? "Provided reason: "+_message : "")).complete();
						_e.getGuild().kick(_e.getGuild().getMemberById(user_id)).reason(_message).queue();
						Azrael.SQLInsertHistory(_e.getGuild().getMemberById(user_id).getUser().getIdLong(), _e.getGuild().getIdLong(), "kick", _message, 0);
						logger.debug("{} has kicked {} from guild {}", _e.getMember().getUser().getId(), user_id, _e.getGuild().getName());
						checkIfDeleteMessagesAfterAction(_e, cache, user_id, _message, message, key);
					}
					else {
						_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Action cannot be executed because the user can't be found on the server!").build()).queue();
						Hashes.clearTempCache(key);
					}
				}
				else {
					message.setTitle("User can't be kicked").setColor(Color.RED);
					_e.getChannel().sendMessage(message.setDescription("Kick is not possible because the KICK MEMBERS permission is missing!").build()).queue();
					logger.warn("KICK MEMBERS permission required to kick a user in guild {}!", _e.getGuild().getId());
					Hashes.clearTempCache(key);
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("watch")) {
				if(UserPrivs.comparePrivilege(_e.getMember(), GuildIni.getUserUseWatchChannelLevel(_e.getGuild().getIdLong())) || _e.getMember().getUser().getIdLong() == GuildIni.getAdmin(_e.getGuild().getIdLong())) {
					var trash_channel = _allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("tra")).findAny().orElse(null);
					var watch_channel = _allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("wat")).findAny().orElse(null);
					if(trash_channel != null || watch_channel != null) {
						long watchChannel = 0;
						var higherPrivileges = false;
						if(watch_channel != null) {
							watchChannel = watch_channel.getChannel_ID();
							higherPrivileges = true;
						}
						else {
							watchChannel = trash_channel.getChannel_ID();
							higherPrivileges = false;
						}
						switch(_message) {
							case "1" -> {
								if(Azrael.SQLInsertWatchlist(user_id, _e.getGuild().getIdLong(), 1, watchChannel, higherPrivileges) == 0) {
									_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Error!").setDescription("An internal error occurred. User couldn't be inserted into Azrael.watchlist table!").build()).queue();
									logger.error("{} couldn't be inserted into Azrael.watchlist for guild {}", user_id, _e.getGuild().getId());
									return;
								}
								else {
									Hashes.addWatchlist(_e.getGuild().getId()+"-"+user_id, new Watchlist(1, watchChannel, higherPrivileges));
									logger.debug("User {} has been added to the watchlist for guild {}", user_id, _e.getGuild().getId());
								}
							}
							case "2" -> {
								if(Azrael.SQLInsertWatchlist(user_id, _e.getGuild().getIdLong(), 2, watchChannel, higherPrivileges) == 0) {
									_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Error!").setDescription("An internal error occurred. User couldn't be inserted into Azrael.watchlist table!").build()).queue();
									logger.error("{} couldn't be inserted into Azrael.watchlist for guild {}", user_id, _e.getGuild().getId());
									return;
								}
								else {
									Hashes.addWatchlist(_e.getGuild().getId()+"-"+user_id, new Watchlist(2, watchChannel, higherPrivileges));
									logger.debug("User {} has been added to the watchlist for guild {}", user_id, _e.getGuild().getId());
								}
							}
							default  -> { return; }
						}
						_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle("Success!").setDescription("This user is now being watched with level "+_message).build()).queue();
						Hashes.clearTempCache(key);
					}
					else {
						//throw error if no trash or watch channel has been registered
						_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Trash channel not found!").setDescription("Before watching a discord user, please register a trash channel!").build()).queue();
						Hashes.clearTempCache(key);
					}
				}
				else {
					var trash_channel = _allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("tra")).findAny().orElse(null);
					if(trash_channel != null) {
						switch(_message) {
							case "1" -> {
								if(Azrael.SQLInsertWatchlist(user_id, _e.getGuild().getIdLong(), 1, trash_channel.getChannel_ID(), false) == 0) {
									_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Error!").setDescription("An internal error occurred. User couldn't be inserted into Azrael.watchlist table!").build()).queue();
									logger.error("{} couldn't be inserted into Azrael.watchlist for guild {}", user_id, _e.getGuild().getId());
									return;
								}
								else {
									Hashes.addWatchlist(_e.getGuild().getId()+"-"+user_id, new Watchlist(1, trash_channel.getChannel_ID(), false));
									logger.debug("User {} has been added to the watchlist for guild {}", user_id, _e.getGuild().getId());
								}
							}
							case "2" -> {
								if(Azrael.SQLInsertWatchlist(user_id, _e.getGuild().getIdLong(), 2, trash_channel.getChannel_ID(), false) == 0) {
									_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Error!").setDescription("An internal error occurred. User couldn't be inserted into Azrael.watchlist table!").build()).queue();
									logger.error("{} couldn't be inserted into Azrael.watchlist for guild {}", user_id, _e.getGuild().getId());
									return;
								}
								else {
									Hashes.addWatchlist(_e.getGuild().getId()+"-"+user_id, new Watchlist(2, trash_channel.getChannel_ID(), false));
									logger.debug("User {} has been added to the watchlist for guild {}", user_id, _e.getGuild().getId());
								}
							}
							default  -> { return; }
						}
						_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle("Success!").setDescription("This user is now being watched with level "+_message).build()).queue();
						Hashes.clearTempCache(key);
					}
					else {
						//throw error if no trash channel has been registered
						_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Trash channel not found!").setDescription("Before watching a discord user, please register a trash channel!").build()).queue();
						Hashes.clearTempCache(key);
					}
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*",	"").equals("gift-experience")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					Rank user_details = RankingSystem.SQLgetWholeRankView(user_id, _e.getGuild().getIdLong());
					long experience = Integer.parseInt(_message);
					long totExperience = 0;
					long currentExperience = 0;
					long rankUpExperience = 0;
					int level = 0;
					long assign_role = 0;
					boolean toBreak = false;
					var roles = RankingSystem.SQLgetRoles(_e.getGuild().getIdLong());
					for(final var ranks : RankingSystem.SQLgetLevels(guild_settings.getThemeID())) {
						if((user_details.getExperience() + experience) >= ranks.getExperience()) {
							totExperience = ranks.getExperience();
							level = ranks.getLevel();
							currentExperience = (user_details.getExperience() + experience) - ranks.getExperience();
							var role = roles.parallelStream().filter(f -> f.getLevel() == ranks.getLevel()).findAny().orElse(null);
							if(role != null) {
								assign_role = role.getRole_ID();
							}
						}
						else {
							if(toBreak == false) {
								rankUpExperience = ranks.getExperience() - totExperience;
								toBreak = true;
							}
							else
								break;
						}
					}
					user_details.setExperience(user_details.getExperience()+experience);
					user_details.setCurrentExperience((int) currentExperience);
					user_details.setRankUpExperience((int) rankUpExperience);
					user_details.setLevel(level);
					user_details.setCurrentRole(assign_role);
					if(RankingSystem.SQLsetLevelUp(user_details.getUser_ID(), _e.getGuild().getIdLong(), user_details.getLevel(), user_details.getExperience(), user_details.getCurrency(), user_details.getCurrentRole()) > 0) {
						RankingSystem.SQLInsertActionLog("low", user_details.getUser_ID(), _e.getGuild().getIdLong(), "Experience points gifted", "User received "+experience+" experience points");
						Hashes.addRanking(_e.getGuild().getId()+"_"+user_details.getUser_ID(), user_details);
						if(roles.size() > 0) {
							if(_e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
								Member member = _e.getGuild().getMemberById(user_id);
								if(member != null) {
									member.getRoles().parallelStream().forEach(r -> {
										roles.parallelStream().forEach(role -> {
											if(r.getIdLong() == role.getRole_ID())
												_e.getGuild().removeRoleFromMember(member, _e.getGuild().getRoleById(r.getIdLong())).queue();
										});
									});
									if(assign_role != 0) {
										_e.getGuild().addRoleToMember(member, _e.getGuild().getRoleById(assign_role)).queue();
									}
								}
								else {
									_e.getChannel().sendMessage(message.setTitle("Warning!").setColor(Color.ORANGE).setDescription("Roles won't be updated because user has left the guild!").build()).queue();
								}
							}
							else {
								_e.getChannel().sendMessage(message.setTitle("Permission missing!").setColor(Color.RED).setDescription("Ranking role can't be assigned because the MANAGE ROLES permission is missing!").build()).queue();
								logger.warn("MANAGE ROLES permission missing to assign a ranking role in guild {}!", _e.getGuild().getId());
							}
						}
						_e.getChannel().sendMessage("Experience points have been updated!").queue();
						logger.debug("{} has gifted {} experience points to {} in guild {}", _e.getMember().getUser().getId(), _message, user_id, _e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
					else {
						_e.getChannel().sendMessage("An internal error occurred in updating the experience and level information in the table RankingSystem.user_details").queue();
						logger.error("RankingSystem.user_details table couldn't be updated with the latest experience and level information");
					}
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("set-experience")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					Rank user_details = RankingSystem.SQLgetWholeRankView(user_id, _e.getGuild().getIdLong());
					long experience = Long.parseLong(_message);
					long totExperience = 0;
					long currentExperience = 0;
					long rankUpExperience = 0;
					int level = 0;
					long assign_role = 0;
					boolean toBreak = false;
					var roles = RankingSystem.SQLgetRoles(_e.getGuild().getIdLong());
					for(final var ranks : RankingSystem.SQLgetLevels(guild_settings.getThemeID())) {
						if(experience >= ranks.getExperience()) {
							totExperience = ranks.getExperience();
							level = ranks.getLevel();
							currentExperience = experience - ranks.getExperience();
							var role = roles.parallelStream().filter(f -> f.getLevel() == ranks.getLevel()).findAny().orElse(null);
							if(role != null) {
								assign_role = role.getRole_ID();
							}
						}
						else {
							if(toBreak == false) {
								rankUpExperience = ranks.getExperience() - totExperience;
								toBreak = true;
							}
							else
								break;
						}
					}
					user_details.setExperience(experience);
					user_details.setCurrentExperience((int) currentExperience);
					user_details.setRankUpExperience((int) rankUpExperience);
					user_details.setLevel(level);
					user_details.setCurrentRole(assign_role);
					if(RankingSystem.SQLsetLevelUp(user_details.getUser_ID(), _e.getGuild().getIdLong(), user_details.getLevel(), user_details.getExperience(), user_details.getCurrency(), user_details.getCurrentRole()) > 0) {
						RankingSystem.SQLInsertActionLog("low", user_details.getUser_ID(), _e.getGuild().getIdLong(), "Experience points edited", "User has been set to "+experience+" experience points");
						Hashes.addRanking(_e.getGuild().getId()+"_"+user_details.getUser_ID(), user_details);
						if(roles.size() > 0) {
							if(_e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
								Member member = _e.getGuild().getMemberById(user_id);
								if(member != null) {
									member.getRoles().parallelStream().forEach(r -> {
										roles.parallelStream().forEach(role -> {
											if(r.getIdLong() == role.getRole_ID())
												_e.getGuild().removeRoleFromMember(member, _e.getGuild().getRoleById(r.getIdLong())).queue();
										});
									});
									if(assign_role != 0) {
										_e.getGuild().addRoleToMember(member, _e.getGuild().getRoleById(assign_role)).queue();
									}
								}
								else {
									_e.getChannel().sendMessage(message.setTitle("Warning!").setColor(Color.ORANGE).setDescription("Roles won't be updated because user has left the guild!").build()).queue();
								}
							}
							else {
								_e.getChannel().sendMessage(message.setTitle("Permission missing!").setColor(Color.RED).setDescription("Ranking role can't be assigned because the MANAGE ROLES permission is missing!").build()).queue();
								logger.warn("MANAGE ROLES permission missing to assign a ranking role in guild {}!", _e.getGuild().getId());
							}
						}
						_e.getChannel().sendMessage("Experience points have been updated!").queue();
						logger.debug("{} has set {} experience points to {} in guild {}", _e.getMember().getUser().getId(), _message, user_id, _e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
					else {
						_e.getChannel().sendMessage("An internal error occurred in updating the experience and level information in the table RankingSystem.user_details").queue();
						logger.error("RankingSystem.user_details table couldn't be updated with the latest experience and level information");
					}
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("set-level")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					int level = Integer.parseInt(_message);
					if(level <= guild_settings.getMaxLevel()) {
						Rank user_details = RankingSystem.SQLgetWholeRankView(user_id, _e.getGuild().getIdLong());
						long experience = 0;
						long rankUpExperience = 0;
						long assign_role = 0;
						boolean toBreak = false;
						var roles = RankingSystem.SQLgetRoles(_e.getGuild().getIdLong());
						for(final var ranks : RankingSystem.SQLgetLevels(guild_settings.getThemeID())) {
							var role = roles.parallelStream().filter(f -> f.getLevel() == ranks.getLevel()).findAny().orElse(null);
							if(role != null && toBreak == false) {
								assign_role = role.getRole_ID();
							}
							if(level == ranks.getLevel()) {
								experience = ranks.getExperience();
								toBreak = true;
							}
							else if(toBreak == true) {
								rankUpExperience = ranks.getExperience() - experience;
								break;
							}
						}
						user_details.setLevel(level);
						user_details.setExperience(experience);
						user_details.setCurrentExperience(0);
						user_details.setRankUpExperience((int) rankUpExperience);
						user_details.setCurrentRole(assign_role);
						if(RankingSystem.SQLsetLevelUp(user_details.getUser_ID(), _e.getGuild().getIdLong(), user_details.getLevel(), user_details.getExperience(), user_details.getCurrency(), user_details.getCurrentRole()) > 0) {
							RankingSystem.SQLInsertActionLog("low", user_details.getUser_ID(), _e.getGuild().getIdLong(), "Level changed", "User is now level "+user_details.getLevel());
							Hashes.addRanking(_e.getGuild().getId()+"_"+user_details.getUser_ID(), user_details);
							if(roles.size() > 0) {
								if(_e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
									Member member = _e.getGuild().getMemberById(user_id);
									if(member != null) {
										member.getRoles().parallelStream().forEach(r -> {
											roles.parallelStream().forEach(role -> {
												if(r.getIdLong() == role.getRole_ID())
													_e.getGuild().removeRoleFromMember(member, _e.getGuild().getRoleById(r.getIdLong())).queue();
											});
										});
										if(assign_role != 0) {
											_e.getGuild().addRoleToMember(member, _e.getGuild().getRoleById(assign_role)).queue();
										}
									}
									else {
										_e.getChannel().sendMessage(message.setTitle("Warning!").setColor(Color.ORANGE).setDescription("Roles won't be updated because user has left the guild!").build()).queue();
									}
								}
								else {
									_e.getChannel().sendMessage(message.setTitle("Permission missing!").setColor(Color.RED).setDescription("Ranking role can't be assigned because the MANAGE ROLES permission is missing!").build()).queue();
									logger.warn("MANAGE ROLES permission missing to assign a ranking role in guild {}!", _e.getGuild().getId());
								}
							}
							_e.getChannel().sendMessage("The level has been updated!").queue();
							logger.debug("{} has set the level {} to {} in guild {}", _e.getMember().getUser().getId(), _message, user_id, _e.getGuild().getId());
							Hashes.clearTempCache(key);
						}
						else {
							_e.getChannel().sendMessage("An internal error occurred in updating the experience and level information in the table RankingSystem.user_details").queue();
							logger.error("RankingSystem.user_details table couldn't be updated with the latest experience and level information");
						}
					}
					else {
						_e.getChannel().sendMessage(_e.getMember().getAsMention()+" Please choose a level that is lower or equal to "+guild_settings.getMaxLevel()).queue();
					}
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("gift-currency")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					Rank user_details = RankingSystem.SQLgetWholeRankView(user_id, _e.getGuild().getIdLong());
					long currency = Long.parseLong(_message);
					user_details.setCurrency(user_details.getCurrency()+currency);
					if(RankingSystem.SQLUpdateCurrency(user_details.getUser_ID(), _e.getGuild().getIdLong(), user_details.getCurrency()) > 0) {
						RankingSystem.SQLInsertActionLog("low", user_details.getUser_ID(), _e.getGuild().getIdLong(), "Money gifted", "User received money in value of "+currency+" "+guild_settings.getCurrency());
						Hashes.addRanking(_e.getGuild().getId()+"_"+user_details.getUser_ID(), user_details);
						_e.getChannel().sendMessage("Currency has been updated!").queue();
						logger.debug("{} has gifted {} currency value to {} in guild {}", _e.getMember().getUser().getId(), _message, user_id, _e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
					else {
						_e.getChannel().sendMessage("An internal error occurred in updating the currency information in the table RankingSystem.user_details").queue();
						logger.error("RankingSystem.user_details table couldn't be updated with the latest currency information");
					}
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("set-currency")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					Rank user_details = RankingSystem.SQLgetWholeRankView(user_id, _e.getGuild().getIdLong());
					long currency = Long.parseLong(_message);
					user_details.setCurrency(currency);
					if(RankingSystem.SQLUpdateCurrency(user_details.getUser_ID(), _e.getGuild().getIdLong(), user_details.getCurrency()) > 0) {
						RankingSystem.SQLInsertActionLog("low", user_details.getUser_ID(), _e.getGuild().getIdLong(), "Money set", "Currency value for the user has been changed to "+currency+" "+guild_settings.getCurrency());
						Hashes.addRanking(_e.getGuild().getId()+"_"+user_details.getUser_ID(), user_details);
						_e.getChannel().sendMessage("Currency has been updated!").queue();
						logger.debug("{} has set {} currency value to {} in guild {}", _e.getMember().getUser().getId(), _message, user_id, _e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
					else {
						_e.getChannel().sendMessage("An internal error occurred in updating the currency information in the table RankingSystem.user_details").queue();
						logger.error("RankingSystem.user_details table couldn't be updated with the latest currency information");
					}
				}
			}
		}
		else {
			Hashes.clearTempCache(key);
		}
	}
	
	private static void checkIfDeleteMessagesAfterAction(GuildMessageReceivedEvent _e, Cache cache, long user_id, String _message, EmbedBuilder message, String key) {
		if(!GuildIni.getMuteMessageDeleteEnabled(_e.getGuild().getIdLong()))
			Hashes.clearTempCache(key);
		else {
			var removeMessages = GuildIni.getMuteAutoDeleteMessages(_e.getGuild().getIdLong());
			if(GuildIni.getMuteForceMessageDeletion(_e.getGuild().getIdLong())) {
				if(removeMessages > 0) {
					deleteMessages(_e, user_id, _message, removeMessages, message, key, true);
				}
				else {
					message.setTitle("Delete messages!");
					_e.getChannel().sendMessage(message.setDescription("Please choose how many messages should be removed between 1 and 100!").build()).queueAfter(1, TimeUnit.SECONDS);
					cache.updateDescription("delete-messages"+user_id).setExpiration(180000);
					Hashes.addTempCache(key, cache);
				}
			}
			else {
				message.setTitle("Delete messages!").setDescription("Do you wish to delete messages?");
				message.addField("YES", "", true);
				message.addField("NO", "", true);
				cache.updateDescription("delete-messages-question"+user_id).updateDescription3((removeMessages > 0 ? ""+removeMessages : "")).setExpiration(180000);
				Hashes.addTempCache(key, cache);
				_e.getChannel().sendMessage(message.build()).queueAfter(1, TimeUnit.SECONDS);
			}
		}
	}
	
	private static void deleteMessages(GuildMessageReceivedEvent _e, long user_id, String _message, int messagesCount, EmbedBuilder message, String key, boolean passValue) {
		if(_message.replaceAll("[0-9]*", "").length() == 0 || passValue) {
			EmbedBuilder error = new EmbedBuilder().setColor(Color.RED);
			int value = (passValue ? messagesCount : Integer.parseInt(_message));
			if(value == 0) {
				_e.getChannel().sendMessage("You chose to not remove any messages!").queue();
			}
			else if(value > 100) {
				_e.getChannel().sendMessage("Please choose a number between 1 and 100!").queue();
				return;
			}
			else {
				List<ArrayList<Messages>> messages = Hashes.getWholeMessagePool().values().parallelStream().filter(f -> f.get(0).getUserID() == user_id && f.get(0).getGuildID() == _e.getGuild().getIdLong()).collect(Collectors.toList());
				if(messages.size() > 0) {
					if(_e.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
						int hash_counter = 0;
						StringBuilder collected_messages = new StringBuilder();
						ArrayList<Long> channelErr = new ArrayList<Long>();
						for(int i = messages.size()-1; i >= 0; i--) {
							hash_counter++;
							final var currentMessage = messages.get(i).get(0);
							TextChannel channel = _e.getGuild().getTextChannelById(currentMessage.getChannelID());
							if(channel != null) {
								if(_e.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_HISTORY)) {
									Message m = _e.getGuild().getTextChannelById(currentMessage.getChannelID()).retrieveMessageById(currentMessage.getMessageID()).complete();
									for(final var cachedMessage: messages.get(i)) {
										collected_messages.append((cachedMessage.isEdit() ? "EDIT" : "MESSAGE")+" ["+cachedMessage.getTime().toString()+" - "+cachedMessage.getUserName()+" ("+cachedMessage.getUserID()+")]: "+cachedMessage.getMessage());
									}
									Hashes.removeMessagePool(currentMessage.getMessageID());
									m.delete().queue();
									if(i == 0 || hash_counter == value) {
										break;
									}
								}
								else {
									if(channelErr.contains(currentMessage.getChannelID())) {
										error.setTitle("Message couldn't be removed");
										_e.getChannel().sendMessage(error.setDescription("Messages couldn't be removed from <#"+currentMessage.getChannelID()+"> due to lack of permissions: **MESSAGE HISTORY**").build()).queue();
										logger.warn("MESSAGE HISTORY permission to retrieve messages is missing in guild {}!", _e.getGuild().getId());
										channelErr.add(currentMessage.getChannelID());
									}
									hash_counter--;
								}
							}
							else {
								Hashes.removeMessagePool(currentMessage.getMessageID());
								hash_counter--;
							}
						}
						
						if(collected_messages.length() > 0) {
							try {
								final var userMessage = messages.get(0).get(0);
								String paste_link = Pastebin.unlistedPermanentPaste("Messages from "+userMessage.getUserName()+" ("+userMessage.getUserID()+") in guild"+_e.getGuild().getId(), hash_counter+" messages from "+userMessage.getUserName()+" ("+userMessage.getUserID()+") have been removed:\n\n"+collected_messages.toString(), _e.getGuild().getIdLong());
								_e.getChannel().sendMessage(message.setDescription("The comments of the selected user have been succesfully removed: "+paste_link).build()).queue();
								Azrael.SQLInsertActionLog("MESSAGES_DELETED", user_id, _e.getGuild().getIdLong(), paste_link);
								logger.debug("{} has bulk deleted messages from {}", _e.getMember().getUser().getId(), userMessage.getUserID());
							} catch(IllegalStateException | LoginException | PasteException e) {
								logger.warn("Error on creating paste", e);
								error.setTitle("New Paste couldn't be created!");
								_e.getChannel().sendMessage(error.setDescription("A new Paste couldn't be created. Please ensure that valid login credentials and a valid Pastebing API key has been inserted into the config.ini file!").build()).queue();
							} catch(RuntimeException e) {
								logger.warn("New Paste couldn't be created!", e);
								error.setTitle("New Paste couldn't be created!");
								_e.getChannel().sendMessage(error.setDescription("A new Paste couldn't be created. Pastebin login page couldn't be reached!").build()).queue();
							}
						}
						else {
							_e.getChannel().sendMessage(message.setDescription("Nothing has been found to delete....\nPlease check the config file if the Bot is allowed to cache messages").build()).queue();
						}
					}
					else {
						_e.getChannel().sendMessage(message.setDescription("Messages can't be deleted because the MANAGE MESSAGES permission is missing!").build()).queue();
						logger.warn("MANAGE MESSAGES permission for message deletions is missing in guild {}!", _e.getGuild().getId());
					}
				}
				else {
					_e.getChannel().sendMessage(message.setDescription("Nothing has been found to delete....\nPlease check the config file if the Bot is allowed to cache messages").build()).queue();
				}
			}
			Hashes.clearTempCache(key);
		}
	}
}
