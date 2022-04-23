package de.azrael.commandsContainer;

import java.awt.Color;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jpastebin.exceptions.PasteException;
import org.jpastebin.pastebin.exceptions.LoginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Bancollect;
import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.constructors.Channels;
import de.azrael.constructors.Guilds;
import de.azrael.constructors.Messages;
import de.azrael.constructors.Ranking;
import de.azrael.constructors.User;
import de.azrael.constructors.Watchlist;
import de.azrael.core.Hashes;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Channel;
import de.azrael.enums.Command;
import de.azrael.enums.Directory;
import de.azrael.enums.GoogleEvent;
import de.azrael.enums.Translation;
import de.azrael.google.GoogleSheets;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.sql.DiscordRoles;
import de.azrael.sql.RankingSystem;
import de.azrael.util.FileHandler;
import de.azrael.util.Pastebin;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Addition to the user command
 */ 

public class UserExecution {
	private static final Logger logger = LoggerFactory.getLogger(UserExecution.class);
	
	public static void getHelp(GuildMessageReceivedEvent e) {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS));
		e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_HELP)).build()).queue();
	}
	
	public static void runTask(GuildMessageReceivedEvent e, String _input, String _displayed_input, BotConfigs botConfig) {
		var key = "user_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId();
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
		
		String name = _displayed_input.replaceAll("[@<>!]", "");
		String raw_input = _input;
		String user_name = null;
		
		if(name.replaceAll("[0-9]*", "").length() > 0) {
			final User user = Azrael.SQLgetUser(name, e.getGuild().getIdLong());
			if(user != null) {
				raw_input = ""+user.getUserID();
				user_name = user.getUserName();
			}
			else {
				final var users = Azrael.SQLgetPossibleUsers(name, e.getGuild().getIdLong());
				if(users != null && users.size() > 0) {
					StringBuilder out = new StringBuilder();
					out.append(STATIC.getTranslation(e.getMember(), Translation.USER_EXAMPLE));
					for(final var curUser : users) {
						if(out.length() > 2000)
							break;
						out.append(curUser.getUserName()+" ("+curUser.getUserID()+")\n");
					}
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(out.toString()).build()).queue();
					return;
				}
			}
		}
		else {
			User user = Azrael.SQLgetUserThroughID(raw_input, e.getGuild().getIdLong());
			if(user != null) {
				user = Azrael.SQLgetJoinDatesFromUser(Long.parseLong(raw_input), e.getGuild().getIdLong(), user);
				user_name = user.getUserName();
			}
			else if(raw_input.matches("[0-9]{17,18}") && botConfig.getCacheLog()) {
				final var messages = Hashes.getMessagePool(e.getGuild().getIdLong(), Long.parseLong(raw_input));
				if(messages != null) {
					final var cachedMessage = messages.get(0);
					raw_input = ""+cachedMessage.getUserID();
					user_name = cachedMessage.getUserName();
				}
			}
		}
		
		if(raw_input != null && (raw_input.length() == 18 || raw_input.length() == 17) && user_name != null && user_name.length() > 0) {
			final var guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
			
			//parameters are disabled by default in case of errors
			boolean userInformation = false;
			boolean userDeleteMessages = false;
			boolean userWarning = false;
			boolean userMute = false;
			boolean userUnmute = false;
			boolean userBan = false;
			boolean userUnban = false;
			boolean userKick = false;
			boolean userAssignRole = false;
			boolean userRemoveRole = false;
			boolean userHistory = false;
			boolean userWatch = false;
			boolean userUnwatch = false;
			boolean userGiftExperience = false;
			boolean userSetExperience = false;
			boolean userSetLevel = false;
			boolean userGiftCurrency = false;
			boolean userSetCurrency = false;
			
			final var subCommands = BotConfiguration.SQLgetCommand(e.getGuild().getIdLong(), 1, Command.USER_INFORMATION, Command.USER_DELETE_MESSAGES
					, Command.USER_WARNING, Command.USER_MUTE, Command.USER_UNMUTE, Command.USER_BAN, Command.USER_UNBAN, Command.USER_KICK
					, Command.USER_ASSIGN_ROLE, Command.USER_REMOVE_ROLE, Command.USER_HISTORY, Command.USER_WATCH, Command.USER_UNWATCH
					, Command.USER_GIFT_EXPERIENCE, Command.USER_SET_EXPERIENCE, Command.USER_SET_LEVEL, Command.USER_GIFT_EXPERIENCE
					, Command.USER_SET_CURRENCY);
			
			for(final var values : subCommands) {
				boolean enabled = false;
				name = "";
				if(values instanceof Boolean)
					enabled = (Boolean)values;
				else if(values instanceof String)
					name = ((String)values).split(":")[0];
				
				if(name.equals(Command.USER_INFORMATION.getColumn()))
					userInformation = enabled;
				else if(name.equals(Command.USER_DELETE_MESSAGES.getColumn()))
					userDeleteMessages = enabled;
				else if(name.equals(Command.USER_WARNING.getColumn()))
					userWarning = enabled;
				else if(name.equals(Command.USER_MUTE.getColumn()))
					userMute = enabled;
				else if(name.equals(Command.USER_UNMUTE.getColumn()))
					userUnmute = enabled;
				else if(name.equals(Command.USER_BAN.getColumn()))
					userBan = enabled;
				else if(name.equals(Command.USER_UNBAN.getColumn()))
					userUnban = enabled;
				else if(name.equals(Command.USER_KICK.getColumn()))
					userKick = enabled;
				else if(name.equals(Command.USER_ASSIGN_ROLE.getColumn()))
					userAssignRole = enabled;
				else if(name.equals(Command.USER_REMOVE_ROLE.getColumn()))
					userRemoveRole = enabled;
				else if(name.equals(Command.USER_HISTORY.getColumn()))
					userHistory = enabled;
				else if(name.equals(Command.USER_WATCH.getColumn()))
					userWatch = enabled;
				else if(name.equals(Command.USER_UNWATCH.getColumn()))
					userUnwatch = enabled;
				else if(name.equals(Command.USER_GIFT_EXPERIENCE.getColumn()))
					userGiftExperience = enabled;
				else if(name.equals(Command.USER_SET_EXPERIENCE.getColumn()))
					userSetExperience = enabled;
				else if(name.equals(Command.USER_SET_LEVEL.getColumn()))
					userSetLevel = enabled;
				else if(name.equals(Command.USER_GIFT_CURRENCY.getColumn()))
					userGiftCurrency = enabled;
				else if(name.equals(Command.USER_SET_CURRENCY.getColumn()))
					userSetCurrency = enabled;
			}
			
			StringBuilder out = new StringBuilder();
			if(userInformation)			out.append(STATIC.getTranslation(e.getMember(), Translation.USER_FOUND_2).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_INFORMATION)));
			if(userDeleteMessages)		out.append(STATIC.getTranslation(e.getMember(), Translation.USER_FOUND_3).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_DELETE_MESSAGES)));
			if(userWarning)				out.append(STATIC.getTranslation(e.getMember(), Translation.USER_FOUND_4).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_WARNING)));
			if(userMute)				out.append(STATIC.getTranslation(e.getMember(), Translation.USER_FOUND_5).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_MUTE)));
			if(userUnmute)				out.append(STATIC.getTranslation(e.getMember(), Translation.USER_FOUND_6).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_UNMUTE)));
			if(userBan)					out.append(STATIC.getTranslation(e.getMember(), Translation.USER_FOUND_7).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_BAN)));
			if(userUnban)				out.append(STATIC.getTranslation(e.getMember(), Translation.USER_FOUND_8).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_UNBAN)));
			if(userKick)				out.append(STATIC.getTranslation(e.getMember(), Translation.USER_FOUND_9).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_KICK)));
			if(userAssignRole)			out.append(STATIC.getTranslation(e.getMember(), Translation.USER_FOUND_10).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ASSIGN_ROLE)));
			if(userRemoveRole)			out.append(STATIC.getTranslation(e.getMember(), Translation.USER_FOUND_11).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE_ROLE)));
			if(userHistory)				out.append(STATIC.getTranslation(e.getMember(), Translation.USER_FOUND_12).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_HISTORY)));
			if(userWatch)				out.append(STATIC.getTranslation(e.getMember(), Translation.USER_FOUND_13).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_WATCH)));
			if(userUnwatch)				out.append(STATIC.getTranslation(e.getMember(), Translation.USER_FOUND_14).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_UNWATCH)));
			if(guild_settings != null && guild_settings.getRankingState()) {
				if(userGiftExperience)	out.append(STATIC.getTranslation(e.getMember(), Translation.USER_FOUND_15).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_GIFT_EXPERIENCE)));
				if(userSetExperience)	out.append(STATIC.getTranslation(e.getMember(), Translation.USER_FOUND_16).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_SET_EXPERIENCE)));
				if(userSetLevel)		out.append(STATIC.getTranslation(e.getMember(), Translation.USER_FOUND_17).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_SET_LEVEL)));
				if(userGiftCurrency)	out.append(STATIC.getTranslation(e.getMember(), Translation.USER_FOUND_18).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_GIFT_CURRENCY)));
				if(userSetCurrency)		out.append(STATIC.getTranslation(e.getMember(), Translation.USER_FOUND_19).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_SET_CURRENCY)));
			}
			
			if(out.length() > 0) {
				message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_FOUND_1)
						.replaceFirst("\\{\\}", user_name).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_EXIT))+out.toString());
				e.getChannel().sendMessage(message.build()).queue();
				Hashes.addTempCache(key, new Cache(180000, raw_input));
			}
			else {
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_DISABLED).replace("{}", user_name)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_NOT_FOUND)).build()).queue();
		}
	}
	
	public static void performAction(GuildMessageReceivedEvent e, String _message, Cache cache, ArrayList<Channels> _allChannels, BotConfigs botConfig) {
		var key = "user_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId();
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
		if(cache != null && cache.getExpiration() - System.currentTimeMillis() > 0) {
			Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
			var comment = _message.toLowerCase();
			if(comment.equals("exit")) {
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_EXIT)).build()).queue();
				Hashes.clearTempCache(key);
				Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER.getColumn(), e.getMessage().getContentRaw());
				return;
			}
			var user_id = Long.parseLong(cache.getAdditionalInfo().replaceAll("[^0-9]*", ""));
			if(!cache.getAdditionalInfo().matches("[a-zA-Z\\-]{1,}[\\d]*")) {
				if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_INFORMATION)) && STATIC.getCommandEnabled(e.getGuild(), Command.USER_INFORMATION)) {
					final var command = BotConfiguration.SQLgetCommand(e.getGuild().getIdLong(), 2, Command.USER_INFORMATION, Command.USER_USE_WATCH_CHANNEL);
					final var informationLevel = (Integer)command.get(0);
					if(UserPrivs.comparePrivilege(e.getMember(), informationLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
						User user = Azrael.SQLgetUserThroughID(cache.getAdditionalInfo(), e.getGuild().getIdLong());
						if(user != null) {
							user = Azrael.SQLgetJoinDatesFromUser(user_id, e.getGuild().getIdLong(), user);
							message.setTitle(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_TITLE));
							if(user.getAvatar() != null && user.getAvatar().length() > 0 && user.getAvatar().startsWith("http"))
								message.setThumbnail(user.getAvatar());
							message.setAuthor(user.getUserName()+ " "+STATIC.getTranslation(e.getMember(), Translation.USER_INFO_ID)+": "+cache.getAdditionalInfo());
							Bancollect warnedUser = Azrael.SQLgetData(user_id, e.getGuild().getIdLong());
							message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_CUR_WARNING), "**"+warnedUser.getWarningID()+"**/**"+Azrael.SQLgetMaxWarning(e.getGuild().getIdLong())+"**", true);
							message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_TOT_WARNING), "**"+Azrael.SQLgetSingleActionEventCount("MEMBER_MUTE_ADD", user_id, e.getGuild().getIdLong())+"**", true);
							message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_TOT_BANS), "**"+Azrael.SQLgetSingleActionEventCount("MEMBER_BAN_ADD", user_id, e.getGuild().getIdLong())+"**", true);
							message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_BANNED), warnedUser.getBanID() == 2 ? STATIC.getTranslation(e.getMember(), Translation.USER_INFO_YES) : STATIC.getTranslation(e.getMember(), Translation.USER_INFO_NO), true);
							message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_JOIN_DATE), "**"+user.getOriginalJoinDate()+"**", true);
							message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_NEW_JOIN_DATE), "**"+user.getNewestJoinDate()+"**", true);
							var watchedUser = Azrael.SQLgetWatchlist(user_id, e.getGuild().getIdLong());
							if(watchedUser == null || (watchedUser.hasHigherPrivileges() && !UserPrivs.comparePrivilege(e.getMember(), (Integer)command.get(1))))
								message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_WATCH_LEVEL), "**0**", true);
							else
								message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_WATCH_LEVEL), "**"+watchedUser.getLevel()+"**", true);
							message.addBlankField(false);
							Ranking user_details = RankingSystem.SQLgetWholeRankView(user_id, e.getGuild().getIdLong());
							if(guild_settings != null && guild_settings.getRankingState() == true && user_details != null) {
								message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_LEVEL), "**"+user_details.getLevel()+"**/**"+guild_settings.getMaxLevel()+"**", true);
								message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_EXPERIENCE), "**"+user_details.getCurrentExperience()+"**/**"+user_details.getRankUpExperience()+"**", true);
								if(user_details.getCurrentRole() != 0) {
									message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_UNLOCKED_ROLE), e.getGuild().getRoleById(user_details.getCurrentRole()).getAsMention(), true);
								}
								else {
									message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_UNLOCKED_ROLE), "**"+STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE)+"**", true);
								}
								message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_TOT_EXPERIENCE), "**"+user_details.getExperience()+"**", true);
								message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_BALANCE), "**"+user_details.getCurrency()+"**", true);
								message.addBlankField(true);
							}
							StringBuilder out = new StringBuilder();
							try {
								for(String description : Azrael.SQLgetDoubleActionEventDescriptions("MEMBER_NAME_UPDATE", "GUILD_MEMBER_JOIN", user_id, e.getGuild().getIdLong())) {
									out.append("[`"+description+"`] ");
								}
								out = out.toString().replaceAll("[\\s]*", "").length() == 0 ? out.append("**"+STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE)+"**") : out;
								message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_NAMES), out.toString(), false);
								out.setLength(0);
								for(String description : Azrael.SQLgetSingleActionEventDescriptions("MEMBER_NICKNAME_UPDATE", user_id, e.getGuild().getIdLong())) {
									out.append("[`"+description+"`] ");
								}
								out = out.toString().replaceAll("[\\s]*", "").length() == 0 ? out.append("**"+STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE)+"**") : out;
								message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_NICKNAMES), out.toString(), false);
								out.setLength(0);
								e.getChannel().sendMessage(message.build()).queue();
							} catch(IllegalArgumentException iae) {
								e.getChannel().sendMessage(message.build()).queue();
								message.clear();
								out.setLength(0);
								for(String description : Azrael.SQLgetDoubleActionEventDescriptions("MEMBER_NAME_UPDATE", "GUILD_MEMBER_JOIN", user_id, e.getGuild().getIdLong())) {
									out.append("[`"+description+"`] ");
								}
								out = out.toString().replaceAll("[\\s]*", "").length() == 0 ? out.append("**"+STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE)+"**") : out;
								try {
									message.setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_TITLE_NAMES)).setDescription(out.toString());
								} catch(IllegalArgumentException iae2) {
									try {
										String pastebin_link = Pastebin.unlistedPaste(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_TITLE_NAMES), out.toString());
										message.setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_TITLE_NAMES)).setDescription(pastebin_link);
									} catch (IllegalStateException | LoginException | PasteException e2) {
										logger.error("Error on creating a pastebin page in guild {}", e.getGuild().getId(), e2);
										message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_TITLE_NAMES)).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_NAMES_ERR));
									}
								}
								e.getChannel().sendMessage(message.build()).queue();
								message.clear();
								out.setLength(0);
								for(String description : Azrael.SQLgetSingleActionEventDescriptions("MEMBER_NICKNAME_UPDATE", user_id, e.getGuild().getIdLong())) {
									out.append("[`"+description+"`] ");
								}
								out = out.toString().replaceAll("[\\s]*", "").length() == 0 ? out.append("**"+STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE)+"**") : out;
								try {
									message.setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_TITLE_NICKNAMES)).setDescription(out.toString());
								} catch(IllegalArgumentException iae2) {
									try {
										String pastebin_link = Pastebin.unlistedPaste(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_TITLE_NICKNAMES), out.toString());
										message.setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_TITLE_NICKNAMES)).setDescription(pastebin_link);
									} catch (IllegalStateException | LoginException | PasteException e2) {
										message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_TITLE_NICKNAMES)).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_NICKNAMES_ERR));
										logger.error("Error on creating a pastebin page in guild {}!", e.getGuild().getId(), e2);
									}
								}
								e.getChannel().sendMessage(message.build()).queue();
							}
							if(System.getProperty("ACTION_LOG").equals("true")) {
								message.clear();
								message.setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_DELETED_MESSAGES));
								out.setLength(0);
								for(String description : Azrael.SQLgetSingleActionEventDescriptionsOrdered("MESSAGES_DELETED", user_id, e.getGuild().getIdLong())) {
									out.append(description+"\n");
								}
								message.setDescription(out);
								if(out.length() > 0)e.getChannel().sendMessage(message.build()).queue();
								
								message.clear();
								message.setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_EVENTS));
								out.setLength(0);
								for(String description : Azrael.SQLgetCriticalActionEvents(user_id, e.getGuild().getIdLong())) {
									out.append(description+"\n");
								}
								message.setDescription(out);
								if(out.length() > 0)e.getChannel().sendMessage(message.build()).queue();
							}
							logger.info("User {} has displayed server information of user {}", e.getMember().getUser().getId(), cache.getAdditionalInfo());
						}
						else {
							e.getChannel().sendMessage(message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_ERR)).build()).queue();
							logger.error("Information of user {} couldn't be retrieved in guild {}", cache.getAdditionalInfo(), e.getGuild().getId());
						}
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER.getColumn(), _message);
					}
					else {
						UserPrivs.throwNotEnoughPrivilegeError(e, informationLevel);
					}
					Hashes.clearTempCache(key);
				}
				else if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_DELETE_MESSAGES)) && STATIC.getCommandEnabled(e.getGuild(), Command.USER_DELETE_MESSAGES)) {
					if(e.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
						if(e.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_HISTORY)) {
							final var deleteMessagesLevel = STATIC.getCommandLevel(e.getGuild(), Command.USER_DELETE_MESSAGES);
							if(UserPrivs.comparePrivilege(e.getMember(), deleteMessagesLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_DELETE_HELP)).build()).queue();
								cache.updateDescription("delete-messages"+user_id).setExpiration(180000);
								Hashes.addTempCache(key, cache);
								Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER.getColumn(), _message);
							}
							else {
								UserPrivs.throwNotEnoughPrivilegeError(e, deleteMessagesLevel);
								Hashes.clearTempCache(key);
							}
						}
						else {
							message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setColor(Color.RED);
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_HISTORY.getName()).build()).queue();
							logger.warn("MESSAGE HISTORY permission required for deleting messages in guild {}", e.getGuild().getId());
							Hashes.clearTempCache(key);
						}
					}
					else {
						message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_MANAGE.getName()).build()).queue();
						logger.warn("MANAGE MESSAGES permission required for deleting messages in guild {}", e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
				}
				else if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_WARNING)) && STATIC.getCommandEnabled(e.getGuild(), Command.USER_WARNING)) {
					final var warningLevel = STATIC.getCommandLevel(e.getGuild(), Command.USER_WARNING);
					if(UserPrivs.comparePrivilege(e.getMember(), warningLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_WARNING_HELP)).build()).queue();
						cache.updateDescription("warning"+user_id).setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					else {
						UserPrivs.throwNotEnoughPrivilegeError(e, warningLevel);
						Hashes.clearTempCache(key);
					}
				}
				else if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_MUTE)) && STATIC.getCommandEnabled(e.getGuild(), Command.USER_MUTE)) {
					Member member = e.getGuild().getMemberById(user_id);
					if(member == null)
						e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_WARNING)).setColor(Color.ORANGE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_LEFT)).build()).queue();
					if(member != null && !e.getGuild().getSelfMember().canInteract(e.getGuild().getMemberById(user_id))) {
						message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_HIGHER_PERMISSION)).build()).queue();
						Hashes.clearTempCache(key);
						return;
					}
					//abort if the user is already muted and warn to unmute before muting again
					if(!UserPrivs.isUserMuted(member)) {
						if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
							final var muteLevel = STATIC.getCommandLevel(e.getGuild(), Command.USER_MUTE);
							if(UserPrivs.comparePrivilege(e.getMember(), muteLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
								message.setColor(Color.BLUE);
								if(botConfig.getForceReason()) {
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_REASON)).build()).queue();
									cache.updateDescription("mute-reason"+cache.getAdditionalInfo().replaceAll("[^0-9]*", "")).setExpiration(180000);
									Hashes.addTempCache(key, cache);
								}
								else {
									message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_YES), STATIC.getTranslation(e.getMember(), Translation.USER_REASON_YES_DESC), true);
									message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_NO), STATIC.getTranslation(e.getMember(), Translation.USER_REASON_NO_DESC), true);
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_QUESTION)).build()).queue();
									cache.updateDescription("mute"+user_id).setExpiration(180000);
									Hashes.addTempCache(key, cache);
								}
								Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER.getColumn(), _message);
							}
							else {
								UserPrivs.throwNotEnoughPrivilegeError(e, muteLevel);
								Hashes.clearTempCache(key);
							}
						}
						else {
							message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setColor(Color.RED);
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MANAGE_ROLES.getName()).build()).queue();
							logger.warn("MANAGE ROLES permission required to mute a user in guild {}", e.getGuild().getId());
							Hashes.clearTempCache(key);
						}
					}
					else {
						message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_MUTE_ALREADY_MUTED)
								.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_UNMUTE))).build()).queue();
						Hashes.clearTempCache(key);
					}
				}
				else if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_UNMUTE)) && STATIC.getCommandEnabled(e.getGuild(), Command.USER_UNMUTE)) {
					if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
						final var unmuteLevel = STATIC.getCommandLevel(e.getGuild(), Command.USER_UNMUTE);
						if(UserPrivs.comparePrivilege(e.getMember(), unmuteLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
							if(!Azrael.SQLisBanned(user_id, e.getGuild().getIdLong())) {
								if(Azrael.SQLgetCustomMuted(user_id, e.getGuild().getIdLong())) {
									//write into cache for RoleTimer and RoleRemovedListener to use for any google API operation
									Hashes.addTempCache("unmute_gu"+e.getGuild().getId()+"us"+user_id, new Cache(60000, ""+e.getMember().getUser().getId()));
									if(STATIC.killThread("mute_gu"+e.getGuild().getId()+"us"+user_id)) {
										e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_UNMUTE_RUN)).build()).queue();
									}
									else {
										message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
										e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_UNMUTE_NOT_MUTED)).build()).queue();
									}
								}
								else if(Azrael.SQLgetMuted(user_id, e.getGuild().getIdLong())) {
									//write into cache for RoleTimer to use for any google API operation
									Hashes.addTempCache("unmute_gu"+e.getGuild().getId()+"us"+user_id, new Cache(60000, ""+e.getMember().getUser().getId()));
									if(STATIC.killThread("mute_gu"+e.getGuild().getId()+"us"+user_id)) {
										e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_UNMUTE_RUN_2)).build()).queue();
										var warning = Azrael.SQLgetWarning(user_id, e.getGuild().getIdLong());
										if(warning == 1) {
											Azrael.SQLDeleteData(user_id, e.getGuild().getIdLong());
										}
										else if(warning > 1) {
											Timestamp timestamp = new Timestamp(System.currentTimeMillis());
											Azrael.SQLInsertData(user_id, e.getGuild().getIdLong(), warning-1, 1, timestamp, timestamp, false, false);
										}
										else {
											message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
											e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_UNMUTE_NOT_MUTED)).build()).queue();
										}
									}
									else if(botConfig.getOverrideBan() && Azrael.SQLgetWarning(user_id, e.getGuild().getIdLong()) == Azrael.SQLgetMaxWarning(e.getGuild().getIdLong()) && Azrael.SQLgetData(user_id, e.getGuild().getIdLong()).getUnmute() == null) {
										Member member = e.getGuild().getMemberById(user_id);
										if(member != null) {
											Azrael.SQLDeleteData(user_id, e.getGuild().getIdLong());
											var mute_role = DiscordRoles.SQLgetRoles(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null);
											if(mute_role != null) {
												long assignedRole = 0;
												Ranking user_details = RankingSystem.SQLgetWholeRankView(user_id, e.getGuild().getIdLong());
												if(user_details != null) {
													assignedRole = user_details.getCurrentRole();
												}
												//write into cache for RoleTimer and RoleRemovedListener to use for any google API operation
												if(assignedRole != 0)Hashes.addTempCache("unmute_gu"+e.getGuild().getId()+"us"+user_id, new Cache(60000, ""+e.getMember().getUser().getId(), ""+assignedRole));
												e.getGuild().removeRoleFromMember(e.getGuild().getMemberById(user_id), e.getGuild().getRoleById(mute_role.getRole_ID())).queue();
												Role role = null;
												if(assignedRole != 0) {
													role = e.getGuild().getRoleById(assignedRole);
													if(role != null)
														e.getGuild().addRoleToMember(e.getGuild().getMemberById(user_id), e.getGuild().getRoleById(assignedRole)).queue();
												}
												Timestamp timestamp = new Timestamp(System.currentTimeMillis());
												e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.GREEN).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_UNMUTED)).setThumbnail(BotConfiguration.SQLgetThumbnails(e.getGuild().getIdLong()).getUnmute()).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_UNMUTE_INFINITE).replaceFirst("\\{\\}", e.getGuild().getMemberById(user_id).getUser().getName()+"#"+e.getGuild().getMemberById(user_id).getUser().getDiscriminator()).replace("{}", ""+user_id)).build()).queue();
												Azrael.SQLInsertActionLog("MEMBER_MUTE_REMOVE", user_id, e.getGuild().getIdLong(), "Permanent mute terminated");
												//Run google service, if enabled
												if(botConfig.getGoogleFunctionalities()) {
													final String [] array = Azrael.SQLgetGoogleFilesAndEvent(e.getGuild().getIdLong(), 2, GoogleEvent.UNMUTE.id, "");
													if(array != null && !array[0].equals("empty")) {
														final String NA = STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE);
														String role_id = NA;
														String role_name = NA;
														if(role != null) {
															role_id = role.getId();
															role_name = role.getName();
														}
														GoogleSheets.spreadsheetUnmuteRequest(array, e.getGuild(), "", ""+user_id, timestamp, member.getUser().getName()+"#"+member.getUser().getDiscriminator(), member.getEffectiveName(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), e.getMember().getEffectiveName(), NA, role_id, role_name);
													}
												}
											}
											else {
												e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setThumbnail(BotConfiguration.SQLgetThumbnails(e.getGuild().getIdLong()).getUnmute()).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_MUTE_ROLE)).build()).queue();
												Hashes.clearTempCache("unmute_gu"+e.getGuild().getId()+"us"+user_id);
											}
										}
										else {
											Azrael.SQLDeleteData(user_id, e.getGuild().getIdLong());
											e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.GREEN).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_UNMUTED)).setThumbnail(BotConfiguration.SQLgetThumbnails(e.getGuild().getIdLong()).getUnmute()).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_UNMUTE_INFINITE).replaceFirst("\\{\\}", e.getGuild().getMemberById(user_id).getUser().getName()+"#"+e.getGuild().getMemberById(user_id).getUser().getDiscriminator()).replace("{}", ""+user_id)).build()).queue();
											Azrael.SQLInsertActionLog("MEMBER_MUTE_REMOVE", user_id, e.getGuild().getIdLong(), "Permanent mute terminated");
											//Run google service, if enabled
											if(botConfig.getGoogleFunctionalities()) {
												final String [] array = Azrael.SQLgetGoogleFilesAndEvent(e.getGuild().getIdLong(), 2, GoogleEvent.UNMUTE.id, "");
												if(array != null && !array[0].equals("empty")) {
													final String NA = STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE);
													var user = Azrael.SQLgetUserThroughID(""+user_id, e.getGuild().getIdLong());
													String username = (user != null ? user.getUserName() : NA);
													GoogleSheets.spreadsheetUnmuteRequest(array, e.getGuild(), "", ""+user_id, new Timestamp(System.currentTimeMillis()), username, username.replaceAll("#[0-9]{4}$", ""), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), e.getMember().getEffectiveName(), NA, "", "");
												}
											}
										}
									}
									else {
										message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
										e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_UNMUTE_NOT_MUTED)).build()).queue();
									}
								}
								else {
									message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_UNMUTE_NOT_MUTED)).build()).queue();
								}
							}
							else {
								EmbedBuilder error = new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
								e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_UNMUTE_IS_BANNED)).build()).queue();
							}
							logger.info("User {} has used the unmute action on user {} in guild {}", e.getMember().getUser().getId(), cache.getAdditionalInfo(), e.getGuild().getId());
							Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER.getColumn(), _message);
						}
						else {
							UserPrivs.throwNotEnoughPrivilegeError(e, unmuteLevel);
						}
					}
					else {
						message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MANAGE_ROLES.getName()).build()).queue();
						logger.warn("MANAGE ROLES permission required to unmute a user in guild {}!", e.getGuild().getId());
					}
					Hashes.clearTempCache(key);
				}
				else if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_BAN)) && STATIC.getCommandEnabled(e.getGuild(), Command.USER_BAN)) {
					Member member = e.getGuild().getMemberById(user_id);
					if(member == null)
						e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_WARNING)).setColor(Color.ORANGE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_LEFT)).build()).queue();
					if(member != null && !e.getGuild().getSelfMember().canInteract(e.getGuild().getMemberById(user_id))) {
						message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_HIGHER_PERMISSION)).build()).queue();
						Hashes.clearTempCache(key);
						return;
					}
					if(e.getGuild().getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
						final var banLevel = STATIC.getCommandLevel(e.getGuild(), Command.USER_BAN);
						if(UserPrivs.comparePrivilege(e.getMember(), banLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
							message.setColor(Color.BLUE);
							if(botConfig.getForceReason()) {
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_REASON)).build()).queue();
								cache.updateDescription("ban-reason"+cache.getAdditionalInfo().replaceAll("[^0-9]*", "")).setExpiration(180000);
								Hashes.addTempCache(key, cache);
							}
							else {
								message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_YES), STATIC.getTranslation(e.getMember(), Translation.USER_REASON_YES_DESC), true);
								message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_NO), STATIC.getTranslation(e.getMember(), Translation.USER_REASON_NO_DESC), true);
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_QUESTION)).build()).queue();
								cache.updateDescription("ban"+user_id).setExpiration(180000);
								Hashes.addTempCache(key, cache);
							}
							Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER.getColumn(), _message);
						}
						else {
							UserPrivs.throwNotEnoughPrivilegeError(e, banLevel);
							Hashes.clearTempCache(key);
						}
					}
					else {
						message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.BAN_MEMBERS.getName()).build()).queue();
						logger.warn("BAN MEMBERS permission required to ban a user in guild {}", e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
				}
				else if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_UNBAN)) && STATIC.getCommandEnabled(e.getGuild(), Command.USER_UNBAN)) {
					if(e.getGuild().getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
						final var unbanLevel = STATIC.getCommandLevel(e.getGuild(), Command.USER_UNBAN);
						if(UserPrivs.comparePrivilege(e.getMember(), unbanLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
							e.getGuild().retrieveBanById(user_id).queue(success -> {
								if(botConfig.getForceReason()) {
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_REASON)).build()).queue();
									cache.updateDescription("unban-reason"+cache.getAdditionalInfo().replaceAll("[^0-9]*", "")).setExpiration(180000);
									Hashes.addTempCache(key, cache);
								}
								else {
									message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_YES), STATIC.getTranslation(e.getMember(), Translation.USER_REASON_YES_DESC), true);
									message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_NO), STATIC.getTranslation(e.getMember(), Translation.USER_REASON_NO_DESC), true);
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_QUESTION)).build()).queue();
									cache.updateDescription("unban"+user_id).setExpiration(180000);
									Hashes.addTempCache(key, cache);
								}
							}, error -> {
								message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_UNBAN_NOT_BANNED)).build()).queue();
								Hashes.clearTempCache(key);
							});
							Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER.getColumn(), _message);
						}
						else {
							UserPrivs.throwNotEnoughPrivilegeError(e, unbanLevel);
							Hashes.clearTempCache(key);
						}
					}
					else {
						message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.BAN_MEMBERS.getName()).build()).queue();
						logger.warn("BAN MEMBERS permission required to unban a user in guild {}", e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
				}
				else if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_KICK)) && STATIC.getCommandEnabled(e.getGuild(), Command.USER_KICK)) {
					Member member = e.getGuild().getMemberById(user_id);
					if(member == null) {
						e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_WARNING)).setColor(Color.ORANGE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_LEFT)).build()).queue();
						Hashes.clearTempCache(key);
						return;
					}
					if(!e.getGuild().getSelfMember().canInteract(e.getGuild().getMemberById(user_id))) {
						message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_HIGHER_PERMISSION)).build()).queue();
						Hashes.clearTempCache(key);
						return;
					}
					if(e.getGuild().getSelfMember().hasPermission(Permission.KICK_MEMBERS)) {
						final var kickLevel = STATIC.getCommandLevel(e.getGuild(), Command.USER_KICK);
						if(UserPrivs.comparePrivilege(e.getMember(), kickLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
							message.setColor(Color.BLUE);
							if(botConfig.getForceReason()) {
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_REASON)).build()).queue();
								cache.updateDescription("kick-reason"+cache.getAdditionalInfo().replaceAll("[^0-9]*", "")).setExpiration(180000);
								Hashes.addTempCache(key, cache);
							}
							else {
								message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_YES), STATIC.getTranslation(e.getMember(), Translation.USER_REASON_YES_DESC), true);
								message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_NO), STATIC.getTranslation(e.getMember(), Translation.USER_REASON_NO_DESC), true);
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_QUESTION)).build()).queue();
								cache.updateDescription("kick"+user_id).setExpiration(180000);
								Hashes.addTempCache(key, cache);
							}
							Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER.getColumn(), _message);
						}
						else {
							UserPrivs.throwNotEnoughPrivilegeError(e, kickLevel);
							Hashes.clearTempCache(key);
						}
					}
					else {
						message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.KICK_MEMBERS.getName()).build()).queue();
						logger.warn("KICK MEMBERS permission required to kick a user in guild {}", e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
				}
				else if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_ASSIGN_ROLE)) && STATIC.getCommandEnabled(e.getGuild(), Command.USER_ASSIGN_ROLE)) {
					final var assignRoleLevel = STATIC.getCommandLevel(e.getGuild(), Command.USER_ASSIGN_ROLE);
					if(UserPrivs.comparePrivilege(e.getMember(), assignRoleLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
						int count = 0;
						ArrayList<Long> roles = new ArrayList<Long>();
						StringBuilder out = new StringBuilder();
						for(final var role : e.getGuild().getRoles()) {
							if(e.getGuild().getSelfMember().canInteract(role) && !role.getName().equals("@everyone")) {
								out.append(++count+": "+role.getName()+" ("+role.getId()+")\n");
								roles.add(role.getIdLong());
							}
						}
						if(roles.size() > 0) {
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_ASSIGN_HELP)+out.toString()).build()).queue();
							cache.updateDescription("assign-role"+user_id).setExpiration(180000).setObject(roles);
							Hashes.addTempCache(key, cache);
						}
						else {
							message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_ASSIGN_NO_ROLES)).build()).queue();
							Hashes.clearTempCache(key);
						}
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER.getColumn(), _message);
					}
					else {
						UserPrivs.throwNotEnoughPrivilegeError(e, assignRoleLevel);
						Hashes.clearTempCache(key);
					}
				}
				else if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE_ROLE)) && STATIC.getCommandEnabled(e.getGuild(), Command.USER_REMOVE_ROLE)) {
					final var removeRoleLevel = STATIC.getCommandLevel(e.getGuild(), Command.USER_REMOVE_ROLE);
					if(UserPrivs.comparePrivilege(e.getMember(), removeRoleLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
						int count = 0;
						ArrayList<Long> roles = new ArrayList<Long>();
						StringBuilder out = new StringBuilder();
						for(final var role : e.getGuild().getRoles()) {
							if(e.getGuild().getSelfMember().canInteract(role) && !role.getName().equals("@everyone")) {
								out.append(++count+": "+role.getName()+" ("+role.getId()+")\n");
								roles.add(role.getIdLong());
							}
						}
						if(roles.size() > 0) {
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_REMOVE_HELP)+out.toString()).build()).queue();
							cache.updateDescription("remove-role"+user_id).setExpiration(180000).setObject(roles);
							Hashes.addTempCache(key, cache);
						}
						else {
							message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_REMOVE_NO_ROLES)).build()).queue();
							Hashes.clearTempCache(key);
						}
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER.getColumn(), _message);
					}
					else {
						UserPrivs.throwNotEnoughPrivilegeError(e, removeRoleLevel);
						Hashes.clearTempCache(key);
					}
				}
				else if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_HISTORY)) && STATIC.getCommandEnabled(e.getGuild(), Command.USER_HISTORY)) {
					final var historyLevel = STATIC.getCommandLevel(e.getGuild(), Command.USER_HISTORY);
					if(UserPrivs.comparePrivilege(e.getMember(), historyLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
						StringBuilder out = new StringBuilder();
						for(var history : Azrael.SQLgetHistory(user_id, e.getGuild().getIdLong())) {
							if(history.getType().equals("roleAdd"))
								out.append("["+history.getTime()+"] **"+history.getType().toUpperCase()+"**\n"+STATIC.getTranslation(e.getMember(), Translation.USER_HISTORY_ROLE)+"**"+history.getInfo()+"**\n"+STATIC.getTranslation(e.getMember(), Translation.USER_HISTORY_BY)+"**"+(history.getReason().length() > 0 ? history.getReason() : STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE))+"**\n\n");
							else if(history.getType().equals("roleRemove"))
								out.append("["+history.getTime()+"] **"+history.getType().toUpperCase()+"**\n"+STATIC.getTranslation(e.getMember(), Translation.USER_HISTORY_ROLE)+"**"+history.getInfo()+"**\n"+STATIC.getTranslation(e.getMember(), Translation.USER_HISTORY_BY)+"**"+(history.getReason().length() > 0 ? history.getReason() : STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE))+"**\n\n");
							else
								out.append("["+history.getTime()+"] **"+history.getType().toUpperCase()+"** "+(history.getPenalty() != 0 ? STATIC.getTranslation(e.getMember(), Translation.USER_HISTORY_TIME)+"**"+history.getPenalty()+STATIC.getTranslation(e.getMember(), Translation.USER_HISTORY_MINUTES)+"**" : "")+"\n"+STATIC.getTranslation(e.getMember(), Translation.USER_HISTORY_BY)+"**"+(history.getInfo().length() > 0 ? history.getInfo() : STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE))+"**\n"+STATIC.getTranslation(e.getMember(), Translation.USER_HISTORY_REASON)+"**"+history.getReason()+"**\n\n");
						}
						if(out.length() > 0) {
							e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.USER_HISTORY_DISPLAY)).setDescription((out.length() <= 2048 ? out.toString() : out.toString().substring(0, 2040)+"...")).build()).queue();
						}
						else
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.USER_HISTORY_TITLE_EMPTY)).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_HISTORY_EMPTY)).build()).queue();
						Hashes.clearTempCache(key);
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER.getColumn(), _message);
					}
					else {
						UserPrivs.throwNotEnoughPrivilegeError(e, historyLevel);
						Hashes.clearTempCache(key);
					}
				}
				else if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_WATCH)) && STATIC.getCommandEnabled(e.getGuild(), Command.USER_WATCH)) {
					final var watchLevel = STATIC.getCommandLevel(e.getGuild(), Command.USER_WATCH);
					if(UserPrivs.comparePrivilege(e.getMember(), watchLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
						if(!botConfig.getCacheLog()) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_WARNING)).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_WATCH_CACHE)).build()).queue();
						}
						message.setColor(Color.BLUE);
						message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_WATCH_HELP));
						e.getChannel().sendMessage(message.build()).queue();
						cache.updateDescription("watch"+user_id);
						Hashes.addTempCache(key, cache);
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER.getColumn(), _message);
					}
					else {
						UserPrivs.throwNotEnoughPrivilegeError(e, watchLevel);
						Hashes.clearTempCache(key);
					}
				}
				else if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_UNWATCH)) && STATIC.getCommandEnabled(e.getGuild(), Command.USER_UNWATCH)) {
					final var command = BotConfiguration.SQLgetCommand(e.getGuild().getIdLong(), 2, Command.USER_UNWATCH, Command.USER_USE_WATCH_CHANNEL);
					final var unwatchLevel = (Integer)command.get(0);
					if(UserPrivs.comparePrivilege(e.getMember(), unwatchLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
						var watchedMember = Azrael.SQLgetWatchlist(user_id, e.getGuild().getIdLong());
						if(watchedMember != null) {
							if(!watchedMember.hasHigherPrivileges()) {
								//No higher privileges required, if it's set to log in the trash channel
								if(Azrael.SQLDeleteWatchlist(user_id, e.getGuild().getIdLong()) > 0) {
									//Successful unwatch operation
									Hashes.removeWatchlist(e.getGuild().getId()+"-"+user_id);
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_UNWATCH)).build()).queue();
									logger.info("User {} has removed the user {} from the watchlist in guild {}", e.getMember().getUser().getId(), user_id, e.getGuild().getId());
								}
								else {
									//Error DB update
									message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
									e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR));
									logger.error("User {} couldn't be removed from the watchlist in guild {}", user_id, e.getGuild().getId());
								}
							}
							else if(UserPrivs.comparePrivilege(e.getMember(), (Integer)command.get(1))) {
								//Higher privileges required to unwatch a member that is assigned to a separate watchlist channel
								if(Azrael.SQLDeleteWatchlist(user_id, e.getGuild().getIdLong()) > 0) {
									//Successful unwatch operation
									Hashes.removeWatchlist(e.getGuild().getId()+"-"+user_id);
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_UNWATCH)).build()).queue();
									logger.info("User {} has removed the user {} from the watchlist in guild {}", e.getMember().getUser().getId(), user_id, e.getGuild().getId());
								}
								else {
									//Error DB update
									message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
									e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR));
									logger.error("User {} couldn't be removed from the watchlist in guild {}", user_id, e.getGuild().getId());
								}
							}
							else {
								//error: the user isn't being watched
								message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_UNWATCH_NOT_WATCHED)).build()).queue();
							}
						}
						else {
							//error: the user isn't being watched
							message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_UNWATCH_NOT_WATCHED)).build()).queue();
						}
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER.getColumn(), _message);
					}
					else {
						UserPrivs.throwNotEnoughPrivilegeError(e, unwatchLevel);
					}
					Hashes.clearTempCache(key);
				}
				else if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_GIFT_EXPERIENCE)) && STATIC.getCommandEnabled(e.getGuild(), Command.USER_GIFT_EXPERIENCE) && guild_settings != null && guild_settings.getRankingState()) {
					final var giftExperienceLevel = STATIC.getCommandLevel(e.getGuild(), Command.USER_GIFT_EXPERIENCE);
					if(UserPrivs.comparePrivilege(e.getMember(), giftExperienceLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_GIFT_EXP)).build()).queue();
						cache.updateDescription("gift-experience"+user_id).setExpiration(180000);
						Hashes.addTempCache(key, cache);
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER.getColumn(), _message);
					}
					else {
						UserPrivs.throwNotEnoughPrivilegeError(e, giftExperienceLevel);
						Hashes.clearTempCache(key);
					}
				}
				else if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_SET_EXPERIENCE)) && STATIC.getCommandEnabled(e.getGuild(), Command.USER_SET_EXPERIENCE) && guild_settings != null && guild_settings.getRankingState()) {
					final var setExperienceLevel = STATIC.getCommandLevel(e.getGuild(), Command.USER_SET_EXPERIENCE);
					if(UserPrivs.comparePrivilege(e.getMember(), setExperienceLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_SET_EXP)).build()).queue();
						cache.updateDescription("set-experience"+user_id).setExpiration(180000);
						Hashes.addTempCache(key, cache);
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER.getColumn(), _message);
					}
					else {
						UserPrivs.throwNotEnoughPrivilegeError(e, setExperienceLevel);
						Hashes.clearTempCache(key);
					}
				}
				else if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_SET_LEVEL)) && STATIC.getCommandEnabled(e.getGuild(), Command.USER_SET_LEVEL) && guild_settings != null && guild_settings.getRankingState()) {
					final var setLevelLevel = STATIC.getCommandLevel(e.getGuild(), Command.USER_SET_LEVEL);
					if(UserPrivs.comparePrivilege(e.getMember(), setLevelLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_LEVEL)).build()).queue();
						cache.updateDescription("set-level"+user_id).setExpiration(180000);
						Hashes.addTempCache(key, cache);
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER.getColumn(), _message);
					}
					else {
						UserPrivs.throwNotEnoughPrivilegeError(e, setLevelLevel);
						Hashes.clearTempCache(key);
					}
				}
				else if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_GIFT_CURRENCY)) && STATIC.getCommandEnabled(e.getGuild(), Command.USER_GIFT_CURRENCY) && guild_settings != null && guild_settings.getRankingState()) {
					final var giftCurrencyLevel = STATIC.getCommandLevel(e.getGuild(), Command.USER_GIFT_CURRENCY);
					if(UserPrivs.comparePrivilege(e.getMember(), giftCurrencyLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_GIFT_CUR)).build()).queue();
						cache.updateDescription("gift-currency"+user_id).setExpiration(180000);
						Hashes.addTempCache(key, cache);
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER.getColumn(), _message);
					}
					else {
						UserPrivs.throwNotEnoughPrivilegeError(e, giftCurrencyLevel);
						Hashes.clearTempCache(key);
					}
				}
				else if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_SET_CURRENCY)) && STATIC.getCommandEnabled(e.getGuild(), Command.USER_SET_CURRENCY) && guild_settings != null && guild_settings.getRankingState()) {
					final var setCurrencyLevel = STATIC.getCommandLevel(e.getGuild(), Command.USER_SET_CURRENCY);
					if(UserPrivs.comparePrivilege(e.getMember(), setCurrencyLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_SET_CUR)).build()).queue();
						cache.updateDescription("set-currency"+user_id).setExpiration(180000);
						Hashes.addTempCache(key, cache);
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER.getColumn(), _message);
					}
					else {
						UserPrivs.throwNotEnoughPrivilegeError(e, setCurrencyLevel);
						Hashes.clearTempCache(key);
					}
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("delete-messages-question")) {
				if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_YES))) {
					if(cache.getAdditionalInfo3().length() == 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_DELETE_HELP)).build()).queue();
						cache.updateDescription("delete-messages"+user_id).setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					else {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_DELETE_DELETING)).build()).queue();
						deleteMessages(e, user_id, _message, Integer.parseInt(cache.getAdditionalInfo3()), message, key, true, botConfig);
					}
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER.getColumn(), _message);
				}
				else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_NO))) {
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_DELETE_ABORT)).build()).queue();
					Hashes.clearTempCache(key);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_DELETE_MESSAGES.getColumn(), _message);
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("delete-messages")) {
				deleteMessages(e, user_id, _message, 0, message, key, false, botConfig);
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("warning")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					if(!Azrael.SQLisBanned(user_id, e.getGuild().getIdLong()) && ( e.getGuild().getMemberById(user_id) == null || !UserPrivs.isUserMuted(e.getGuild().getMemberById(user_id)))) {
						int db_warning = Azrael.SQLgetData(user_id, e.getGuild().getIdLong()).getWarningID();
						if(db_warning != 0) {
							int warning_id = Integer.parseInt(_message.replaceAll("[^0-9]*", ""));
							int max_warning_id = Azrael.SQLgetMaxWarning(e.getGuild().getIdLong());
							if(warning_id == 0) {
								if(Azrael.SQLDeleteData(user_id, e.getGuild().getIdLong()) > 0) {
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_WARNING_CLEAR)).build()).queue();
									logger.info("User {} has cleared the warnings of user {} in guild {}", e.getMember().getUser().getId(), user_id, e.getGuild().getId());
								}
								else {
									e.getChannel().sendMessage(message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("The warnings of user {} couldn't be cleared in guild {}", user_id, e.getGuild().getId());
								}
							}
							else if(warning_id <= max_warning_id) {
								if(Azrael.SQLUpdateWarning(user_id, e.getGuild().getIdLong(), warning_id) > 0) {
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_WARNING_UPDATED)).build()).queue();
									logger.info("User {} has set the warning value to {} for user {} in guild {}", e.getMember().getUser().getId(), warning_id, user_id, e.getGuild().getId());
								}
								else {
									e.getChannel().sendMessage(message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("Warning value of user {} couldn't be updated to {} in guild {}", user_id, warning_id, e.getGuild().getId());
								}
							}
							else {
								if(Azrael.SQLUpdateWarning(user_id, e.getGuild().getIdLong(), max_warning_id) > 0) {
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_WARNING_UPDATED_LIMIT)).build()).queue();
									logger.info("User {} has set the warning value to {} for user {} in guild {}", e.getMember().getUser().getId(), max_warning_id, user_id, e.getGuild().getId());
								}
								else {
									e.getChannel().sendMessage(message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("Warning value of user {} couldn't be updated to {} in guild {}", user_id, max_warning_id, e.getGuild().getId());
								}
							}
						}
						else {
							if(UserPrivs.comparePrivilege(e.getMember(), STATIC.getCommandLevel(e.getGuild(), Command.USER_WARNING_FORCE)) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
								int warning_id = Integer.parseInt(_message.replaceAll("[^0-9]*", ""));
								int max_warning_id = Azrael.SQLgetMaxWarning(e.getGuild().getIdLong());
								if(warning_id <= max_warning_id) {
									if(Azrael.SQLInsertData(user_id, e.getGuild().getIdLong(), warning_id, 1, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), false, false) > 0) {
										e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_WARNING_UPDATED)).build()).queue();
										logger.info("User {} has set the warning value to {} for user {} in guild {}", e.getMember().getUser().getId(), warning_id, user_id, e.getGuild().getId());
									}
									else {
										e.getChannel().sendMessage(message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
										logger.error("Warning value of user {} couldn't be updated to {} in guild {}", user_id, warning_id, e.getGuild().getId());
									}
								}
								else {
									if(Azrael.SQLInsertData(user_id, e.getGuild().getIdLong(), max_warning_id, 1, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), false, false) > 0) {
										e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_WARNING_UPDATED_LIMIT)).build()).queue();
										logger.info("User {} has set the warning value to {} for user {} in guild {}", e.getMember().getUser().getId(), max_warning_id, user_id, e.getGuild().getId());
									}
									else {
										e.getChannel().sendMessage(message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
										logger.error("Warning value of user {} couldn't be updated to {} in guild {}", user_id, max_warning_id, e.getGuild().getId());
									}
								}
							}
							else {
								e.getChannel().sendMessage(message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_WARNING_NEVER_WARNED)).build()).queue();
							}
						}
					}
					else {
						e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_WARNING_IS_MUTED)).build()).queue();
					}
					Hashes.clearTempCache(key);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_WARNING.getColumn(), _message);
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("mute")) {
				if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_YES))) {
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_REASON)).build()).queue();
					cache.updateDescription("mute-reason"+user_id).setExpiration(180000);
					Hashes.addTempCache(key, cache);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_MUTE.getColumn(), _message);
				}
				else if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_NO))) {
					message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_YES), STATIC.getTranslation(e.getMember(), Translation.USER_MUTE_TIME), true);
					message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_NO), STATIC.getTranslation(e.getMember(), Translation.USER_MUTE_NO_TIME), true);
					if(botConfig.getOverrideBan())
						message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_MUTE_PERM), STATIC.getTranslation(e.getMember(), Translation.USER_MUTE_PERM_DESC), true);
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_MUTE_CHOICE)).build()).queue();
					cache.updateDescription("mute-action"+user_id).setExpiration(180000).updateDescription2(STATIC.getTranslation2(e.getGuild(), Translation.DEFAULT_REASON));
					Hashes.addTempCache(key, cache);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_MUTE.getColumn(), _message);
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("mute-reason")) {
				message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_YES), STATIC.getTranslation(e.getMember(), Translation.USER_MUTE_TIME), true);
				message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_NO), STATIC.getTranslation(e.getMember(), Translation.USER_MUTE_NO_TIME), true);
				if(botConfig.getOverrideBan())
					message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_MUTE_PERM), STATIC.getTranslation(e.getMember(), Translation.USER_MUTE_PERM_DESC), true);
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_MUTE_CHOICE)).build()).queue();
				cache.updateDescription("mute-action"+user_id).updateDescription2(_message).setExpiration(180000);
				Hashes.addTempCache(key, cache);
				Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_MUTE.getColumn(), _message);
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("mute-action")) {
				if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_YES))) {
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_MUTE_FORMAT)).build()).queue();
					cache.updateDescription("mute-time"+user_id).setExpiration(180000);
					Hashes.addTempCache(key, cache);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_MUTE.getColumn(), _message);
				}
				else if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_NO)) || (botConfig.getOverrideBan() && comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_PERM)))) {
					boolean permMute = false;
					if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_PERM)))
						permMute = true;
					Member member = e.getGuild().getMemberById(user_id);
					if(member == null) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_MUTE_LEFT)).addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_YES), "", true).addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_NO), "", true).build()).queue();
						cache.updateDescription("mute-delay"+user_id).setExpiration(180000);
						if(permMute)
							cache.updateDescription3("perm");
						Hashes.addTempCache(key, cache);
						return;
					}
					if(!e.getGuild().getSelfMember().canInteract(e.getGuild().getMemberById(user_id))) {
						message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_HIGHER_PERMISSION)).build()).queue();
						Hashes.clearTempCache(key);
						return;
					}
					if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
						var mute_role_id = DiscordRoles.SQLgetRoles(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null);
						if(mute_role_id != null) {
							Hashes.addTempCache("mute_time_gu"+e.getGuild().getId()+"us"+user_id, new Cache(e.getMember().getUser().getId(), (cache.getAdditionalInfo2().length() > 0 ? cache.getAdditionalInfo2() : STATIC.getTranslation2(e.getGuild(), Translation.DEFAULT_REASON))));
							if(permMute) {
								var timestamp = new Timestamp(System.currentTimeMillis());
								if(Azrael.SQLInsertData(member.getUser().getIdLong(), e.getGuild().getIdLong(), Azrael.SQLgetMaxWarning(e.getGuild().getIdLong()), 1, timestamp, timestamp, false, false) == 0) {
									e.getChannel().sendMessage(message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("The perm mute flag couldn't be set for user {} in guild {}", user_id, e.getGuild().getId());
								}
							}
							e.getGuild().addRoleToMember(e.getGuild().getMemberById(user_id), e.getGuild().getRoleById(mute_role_id.getRole_ID())).queue();
							var mute_time = (long)Azrael.SQLgetWarning(e.getGuild().getIdLong(), Azrael.SQLgetData(user_id, e.getGuild().getIdLong()).getWarningID()+1).getTimer();
							Azrael.SQLInsertHistory(user_id, e.getGuild().getIdLong(), "mute", cache.getAdditionalInfo2(), (mute_time/1000/60), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator());
							e.getChannel().sendMessage(message.setDescription((permMute ? STATIC.getTranslation(e.getMember(), Translation.USER_MUTE_ORDER_2) : STATIC.getTranslation(e.getMember(), Translation.USER_MUTE_ORDER_1))).build()).queue();
							checkIfDeleteMessagesAfterAction(e, cache, user_id, _message, message, key, botConfig);
						}
						else {
							e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_MUTE_ROLE)).build()).queue();
							Hashes.clearTempCache(key);
						}
					}
					else {
						message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MANAGE_ROLES.getName()).build()).queue();
						logger.warn("MANAGE ROLES permission required to mute a user in guild {}", e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_MUTE.getColumn(), _message);
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("mute-time")) {
				if(_message.replaceAll("[0-9]*", "").length() == 1 && (comment.endsWith("m") || comment.endsWith("h") || comment.endsWith("d"))) {
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
					
					Member member = e.getGuild().getMemberById(user_id);
					if(member == null) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_MUTE_LEFT)).addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_YES), "", true).addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_NO), "", true).build()).queue();
						cache.updateDescription("mute-delay"+user_id).setExpiration(180000).updateDescription3(""+mute_time);
						Hashes.addTempCache(key, cache);
						return;
					}
					if(!e.getGuild().getSelfMember().canInteract(member)) {
						message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_HIGHER_PERMISSION)).build()).queue();
						Hashes.clearTempCache(key);
						return;
					}
					if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
						Timestamp timestamp = new Timestamp(System.currentTimeMillis());
						Timestamp unmute_timestamp = new Timestamp(System.currentTimeMillis()+mute_time);
						var mute_role_id = DiscordRoles.SQLgetRoles(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null);
						if(mute_role_id != null) {
							Hashes.addTempCache("mute_time_gu"+e.getGuild().getId()+"us"+user_id, new Cache(""+mute_time, e.getMember().getUser().getId(), (cache.getAdditionalInfo2().length() > 0 ? cache.getAdditionalInfo2() : STATIC.getTranslation2(e.getGuild(), Translation.DEFAULT_REASON))));
							if(Azrael.SQLgetData(user_id, e.getGuild().getIdLong()).getWarningID() != 0) {
								if(Azrael.SQLUpdateUnmute(user_id, e.getGuild().getIdLong(), timestamp, unmute_timestamp, true, true) == 0) {
									e.getChannel().sendMessage(message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("The unmute timer couldn't be updated to {} for user {} in guild {}", unmute_timestamp, user_id, e.getGuild().getId());
								}
							}
							else {
								if(Azrael.SQLInsertData(user_id, e.getGuild().getIdLong(), 1, 1, timestamp, unmute_timestamp, true, true) == 0) {
									e.getChannel().sendMessage(message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("The muted user {} couldn't be labeled as muted in guild {}", user_id, e.getGuild().getId());
								}
							}
							e.getGuild().addRoleToMember(e.getGuild().getMemberById(user_id), e.getGuild().getRoleById(mute_role_id.getRole_ID())).reason(cache.getAdditionalInfo2()).queue();
							Azrael.SQLInsertHistory(e.getGuild().getMemberById(user_id).getUser().getIdLong(), e.getGuild().getIdLong(), "mute", cache.getAdditionalInfo2(), (mute_time/1000/60), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator());
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_MUTE_ORDER_1)).build()).queue();
							logger.info("User {} has muted user {} in guild {}", e.getMember().getUser().getId(), user_id, e.getGuild().getId());
							checkIfDeleteMessagesAfterAction(e, cache, user_id, _message, message, key, botConfig);
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_MUTE_ROLE)).build()).queue();
							Hashes.clearTempCache(key);
						}
					}
					else {
						message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MANAGE_ROLES.getName()).build()).queue();
						logger.warn("MANAGE ROLES permission required to mute a user in guild {}!", e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_MUTE.getColumn(), _message);
				}
				else {
					e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_MUTE_NUMERIC)).build()).queue();
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("mute-delay")) {
				if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_YES))) {
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_REMINDER_SET)).build()).queue();
					Azrael.SQLInsertReminder(user_id, e.getGuild().getIdLong(), "mute", cache.getAdditionalInfo2(), e.getMember().getUser().getId(), cache.getAdditionalInfo3());
					Hashes.clearTempCache(key);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_MUTE.getColumn(), _message);
				}
				else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_NO))) {
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_REMINDER_NOT_SET)).build()).queue();
					Hashes.clearTempCache(key);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_MUTE.getColumn(), _message);
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("ban")) {
				if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_YES))) {
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_REASON)).build()).queue();
					cache.updateDescription("ban-reason"+user_id).setExpiration(180000);
					Hashes.addTempCache(key, cache);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_BAN.getColumn(), _message);
				}
				else if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_NO))) {
					Member member = e.getGuild().getMemberById(user_id);
					if(member != null && !e.getGuild().getSelfMember().canInteract(member)) {
						message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_HIGHER_PERMISSION)).build()).queue();
						Hashes.clearTempCache(key);
						return;
					}
					if(e.getGuild().getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
						if(member != null) {
							final String reason = STATIC.getTranslation2(e.getGuild(), Translation.DEFAULT_REASON);
							Hashes.addTempCache("ban_gu"+e.getGuild().getId()+"us"+user_id, new Cache(e.getMember().getId(), reason));
							int warning_id = Azrael.SQLgetData(user_id, e.getGuild().getIdLong()).getWarningID();
							int max_warning_id = Azrael.SQLgetMaxWarning(e.getGuild().getIdLong());
							e.getGuild().getMemberById(user_id).getUser().openPrivateChannel().queue(channel -> {
								if(warning_id == max_warning_id) {
									channel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.USER_BAN_DM).replace("{}", e.getGuild().getName())
										+ (botConfig.getBanSendReason() ? STATIC.getTranslation2(e.getGuild(), Translation.USER_BAN_REASON)+reason : "")).queue(m -> {
											e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.USER_BAN_ORDER)).build()).queue();
											e.getGuild().ban(e.getGuild().getMemberById(user_id), 0).reason(reason).queue();
										}, err -> {
											e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.USER_BAN_ORDER)).build()).queue();
											e.getGuild().ban(e.getGuild().getMemberById(user_id), 0).reason(reason).queue();
										});
								}
								else {
									channel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.USER_BAN_DM_2).replace("{}", e.getGuild().getName())
										+ (botConfig.getBanSendReason() ? STATIC.getTranslation2(e.getGuild(), Translation.USER_BAN_REASON)+reason : "")).queue(m -> {
											e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.USER_BAN_ORDER)).build()).queue();
											e.getGuild().ban(e.getGuild().getMemberById(user_id), 0).reason(reason).queue();
										}, err -> {
											e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.USER_BAN_ORDER)).build()).queue();
											e.getGuild().ban(e.getGuild().getMemberById(user_id), 0).reason(reason).queue();
										});
								}
							});
							Azrael.SQLInsertHistory(user_id, e.getGuild().getIdLong(), "ban", reason, 0, e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator());
							logger.info("User {} has banned user {} in guild {}", e.getMember().getUser().getId(), user_id, e.getGuild().getId());
							checkIfDeleteMessagesAfterAction(e, cache, user_id, _message, message, key, botConfig);
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_BAN_LEFT)).addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_YES), "", true).addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_NO), "", true).build()).queue();
							cache.updateDescription("ban-delay"+user_id).updateDescription2(STATIC.getTranslation2(e.getGuild(), Translation.DEFAULT_REASON)).setExpiration(180000);
							Hashes.addTempCache(key, cache);
						}
					}
					else {
						message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.BAN_MEMBERS.getName()).build()).queue();
						logger.warn("BAN MEMBERS permission required to ban a user in guild {}", e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_BAN.getColumn(), _message);
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("ban-reason")) {
				Member member = e.getGuild().getMemberById(user_id);
				if(member != null && !e.getGuild().getSelfMember().canInteract(member)) {
					message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_HIGHER_PERMISSION)).build()).queue();
					Hashes.clearTempCache(key);
					return;
				}
				if(e.getGuild().getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
					if(member != null) {
						Hashes.addTempCache("ban_gu"+e.getGuild().getId()+"us"+user_id, new Cache(e.getMember().getId(), _message));
						int warning_id = Azrael.SQLgetData(user_id, e.getGuild().getIdLong()).getWarningID();
						int max_warning_id = Azrael.SQLgetMaxWarning(e.getGuild().getIdLong());
						e.getGuild().getMemberById(user_id).getUser().openPrivateChannel().queue(channel -> {
							if(warning_id == max_warning_id) {
								channel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.USER_BAN_DM).replace("{}", e.getGuild().getName())
									+ (botConfig.getBanSendReason() ? STATIC.getTranslation2(e.getGuild(), Translation.USER_BAN_REASON)+_message : "")).queue(m -> {
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_BAN_ORDER)).build()).queue();
										e.getGuild().ban(e.getGuild().getMemberById(user_id), 0).reason(_message).queue();
									}, err -> {
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_BAN_ORDER)).build()).queue();
										e.getGuild().ban(e.getGuild().getMemberById(user_id), 0).reason(_message).queue();
									});
							}
							else {
								channel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.USER_BAN_DM_2).replace("{}", e.getGuild().getName())
									+ (botConfig.getBanSendReason() ? STATIC.getTranslation2(e.getGuild(), Translation.USER_BAN_REASON)+_message : "")).queue(m -> {
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_BAN_ORDER)).build()).queue();
										e.getGuild().ban(e.getGuild().getMemberById(user_id), 0).reason(_message).queue();
									}, err -> {
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_BAN_ORDER)).build()).queue();
										e.getGuild().ban(e.getGuild().getMemberById(user_id), 0).reason(_message).queue();
									});
							}
						});
						Azrael.SQLInsertHistory(e.getGuild().getMemberById(user_id).getUser().getIdLong(), e.getGuild().getIdLong(), "ban", _message, 0, e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator());
						logger.info("User {} has banned user {} in guild {}", e.getMember().getUser().getId(), cache.getAdditionalInfo().replaceAll("[^0-9]",  ""), e.getGuild().getId());
						checkIfDeleteMessagesAfterAction(e, cache, user_id, _message, message, key, botConfig);
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_BAN_LEFT)).addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_YES), "", true).addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_NO), "", true).build()).queue();
						cache.updateDescription("ban-delay"+user_id).setExpiration(180000).updateDescription2(_message);
						Hashes.addTempCache(key, cache);
					}
				}
				else {
					message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setColor(Color.RED);
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.BAN_MEMBERS).build()).queue();
					logger.warn("BAN MEMBERS permission required to ban a user in guild {}", e.getGuild().getId());
					Hashes.clearTempCache(key);
				}
				Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_BAN.getColumn(), _message);
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("ban-delay")) {
				if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_YES))) {
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_REMINDER_SET)).build()).queue();
					Azrael.SQLInsertReminder(user_id, e.getGuild().getIdLong(), "ban", cache.getAdditionalInfo2(), e.getMember().getUser().getId(), "");
					Hashes.clearTempCache(key);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_BAN.getColumn(), _message);
				}
				else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_NO))) {
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_REMINDER_NOT_SET)).build()).queue();
					Hashes.clearTempCache(key);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_BAN.getColumn(), _message);
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("unban")) {
				if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_YES))) {
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_REASON)).build()).queue();
					cache.updateDescription("unban-reason"+user_id).setExpiration(180000);
					Hashes.addTempCache(key, cache);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_UNBAN.getColumn(), _message);
				}
				else if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_NO))) {
					if(e.getGuild().getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
						String reason = STATIC.getTranslation2(e.getGuild(), Translation.DEFAULT_REASON);
						Hashes.addTempCache("unban_gu"+e.getGuild().getId()+"us"+user_id, new Cache(e.getMember().getId(), reason));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_UNBAN_ORDER)).build()).queue();
						e.getGuild().retrieveBanById(user_id).queue(ban -> {
							e.getGuild().unban(ban.getUser()).reason(reason).queue();
						});
						Azrael.SQLInsertHistory(user_id, e.getGuild().getIdLong(), "unban", reason, 0, e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator());
						logger.info("User {} has unbanned user {} in guild {}", e.getMember().getUser().getId(), user_id, e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
					else {
						message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.BAN_MEMBERS).build()).queue();
						logger.warn("BAN MEMBERS permission required to unban a user in guild {}", e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_UNBAN.getColumn(), _message);
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("unban-reason")) {
				if(e.getGuild().getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
					Hashes.addTempCache("unban_gu"+e.getGuild().getId()+"us"+user_id, new Cache(e.getMember().getId(), _message));
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_UNBAN_ORDER)).build()).queue();
					e.getGuild().retrieveBanById(user_id).queue(ban -> {
						e.getGuild().unban(ban.getUser()).reason(_message).queue();
					});
					Azrael.SQLInsertHistory(user_id, e.getGuild().getIdLong(), "unban", _message, 0, e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator());
					logger.info("User {} has unbanned user {} in guild {}", e.getMember().getUser().getId(), user_id, e.getGuild().getId());
					Hashes.clearTempCache(key);
				}
				else {
					message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setColor(Color.RED);
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.BAN_MEMBERS.getName()).build()).queue();
					logger.warn("BAN MEMBERS permission required to unban a user in guild {}", e.getGuild().getId());
					Hashes.clearTempCache(key);
				}
				Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_UNBAN.getColumn(), _message);
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("kick")) {
				if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_YES))) {
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_REASON)).build()).queue();
					cache.updateDescription("kick-reason"+user_id).setExpiration(180000);
					Hashes.addTempCache(key, cache);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_KICK.getColumn(), _message);
				}
				else if(comment.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_NO))) {
					Member member = e.getGuild().getMemberById(user_id);
					if(member != null && !e.getGuild().getSelfMember().canInteract(member)) {
						message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_HIGHER_PERMISSION)).build()).queue();
						Hashes.clearTempCache(key);
						return;
					}
					if(e.getGuild().getSelfMember().hasPermission(Permission.KICK_MEMBERS)) {
						if(member != null) {
							final String reason = STATIC.getTranslation2(e.getGuild(), Translation.DEFAULT_REASON);
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_KICK_ORDER)).build()).queue();
							Hashes.addTempCache("kick_gu"+e.getGuild().getId()+"us"+user_id, new Cache(e.getMember().getUser().getId(), reason));
							member.getUser().openPrivateChannel().queue(channel -> {
								channel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.USER_KICK_DM)
									+ (botConfig.getKickSendReason() ? STATIC.getTranslation2(e.getGuild(), Translation.USER_BAN_REASON)+reason : "")).queue(m -> {
										e.getGuild().kick(member).reason(reason).queue();
									}, err -> {
										e.getGuild().kick(member).reason(reason).queue();
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.ORANGE).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.KICK_DM_LOCKED).replaceFirst("\\{\\}", member.getUser().getName()+"#"+member.getUser().getDiscriminator()).replace("{}", member.getUser().getId())).build()).queue();
									});
							});
							Azrael.SQLInsertHistory(e.getGuild().getMemberById(user_id).getUser().getIdLong(), e.getGuild().getIdLong(), "kick", reason, 0, e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator());
							logger.info("User {} has kicked user {} in guild {}", e.getMember().getUser().getId(), user_id, e.getGuild().getId());
							checkIfDeleteMessagesAfterAction(e, cache, user_id, _message, message, key, botConfig);
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_KICK_LEFT)).build()).queue();
							Hashes.clearTempCache(key);
						}
					}
					else {
						message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.KICK_MEMBERS.getName()).build()).queue();
						logger.warn("KICK MEMBERS permission required to kick a user in guild {}!", e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_KICK.getColumn(), _message);
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("kick-reason")) {
				Member member = e.getGuild().getMemberById(user_id);
				if(member != null && !e.getGuild().getSelfMember().canInteract(member)) {
					message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_HIGHER_PERMISSION)).build()).queue();
					Hashes.clearTempCache(key);
					return;
				}
				if(e.getGuild().getSelfMember().hasPermission(Permission.KICK_MEMBERS)) {
					if(member != null) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_KICK_ORDER)).build()).queue();
						Hashes.addTempCache("kick_gu"+e.getGuild().getId()+"us"+user_id, new Cache(e.getMember().getId(), _message));
						member.getUser().openPrivateChannel().queue(channel -> {
							channel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.USER_KICK_DM)
								+ (botConfig.getKickSendReason() ? STATIC.getTranslation2(e.getGuild(), Translation.USER_BAN_REASON)+_message : "")).queue(m -> {
									e.getGuild().kick(member).reason(_message).queue();
								}, err -> {
									e.getGuild().kick(member).reason(_message).queue();
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.ORANGE).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.KICK_DM_LOCKED).replaceFirst("\\{\\}", member.getUser().getName()+"#"+member.getUser().getDiscriminator()).replace("{}", member.getUser().getId())).build()).queue();
								});
						});
						Azrael.SQLInsertHistory(e.getGuild().getMemberById(user_id).getUser().getIdLong(), e.getGuild().getIdLong(), "kick", _message, 0, e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator());
						logger.info("User {} has kicked user {} in guild {}", e.getMember().getUser().getId(), user_id, e.getGuild().getName());
						checkIfDeleteMessagesAfterAction(e, cache, user_id, _message, message, key, botConfig);
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_KICK_LEFT)).build()).queue();
						Hashes.clearTempCache(key);
					}
				}
				else {
					message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setColor(Color.RED);
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.KICK_MEMBERS).build()).queue();
					logger.warn("KICK MEMBERS permission required to kick a user in guild {}", e.getGuild().getId());
					Hashes.clearTempCache(key);
				}
				Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_KICK.getColumn(), _message);
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("assign-role")) {
				@SuppressWarnings("unchecked")
				ArrayList<Long> roles = (ArrayList<Long>)cache.getObject();
				if(_message.replaceAll("[0-9]*", "").isBlank()) {
					int number = Integer.parseInt(_message);
					if(roles.size() >= number) {
						final long role_id = roles.get(number-1);
						final var member = e.getGuild().getMemberById(user_id);
						if(member != null) {
							if(member.getRoles().parallelStream().filter(f -> f.getIdLong() == role_id).findAny().orElse(null) == null) {
								final var role = e.getGuild().getRoleById(role_id);
								if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
									e.getGuild().addRoleToMember(member, role).queue(success -> {
										Azrael.SQLInsertHistory(user_id, e.getGuild().getIdLong(), "roleAdd", e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), 0, role.getName());
										Azrael.SQLInsertActionLog("MEMBER_ROLE_ADD", user_id, e.getGuild().getIdLong(), "Role add "+role.getName());
										e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_ASSIGN_ADD).replaceFirst("\\{\\}", member.getUser().getName()+"#"+member.getUser().getDiscriminator()).replaceFirst("\\{\\}", member.getUser().getId()).replaceFirst("\\{\\}", role.getName()).replace("{}", e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator())).build()).queue();
										logger.info("User {} has assigned the role {} to user {} in guild {}", e.getMember().getUser().getId(), role_id, user_id, e.getGuild().getId());
										Hashes.clearTempCache(key);
									});
								}
								else {
									message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setColor(Color.RED);
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MANAGE_ROLES.getName()).build()).queue();
									logger.warn("MANAGE_ROLES permission required to assign roles in guild {}", e.getGuild().getId());
									Hashes.clearTempCache(key);
								}
							}
							else {
								message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_ASSIGN_ALREADY)).build()).queue();
								Hashes.clearTempCache(key);
							}
						}
						else {
							message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_LEFT)).build()).queue();
							Hashes.clearTempCache(key);
						}
					}
					else {
						message.setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_NUMBER)).build()).queue();
						cache.setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_ASSIGN_ROLE.getColumn(), _message);
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("remove-role")) {
				@SuppressWarnings("unchecked")
				ArrayList<Long> roles = (ArrayList<Long>)cache.getObject();
				if(_message.replaceAll("[0-9]*", "").isBlank()) {
					int number = Integer.parseInt(_message);
					if(roles.size() >= number) {
						final long role_id = roles.get(number-1);
						final var member = e.getGuild().getMemberById(user_id);
						if(member != null) {
							if(member.getRoles().parallelStream().filter(f -> f.getIdLong() == role_id).findAny().orElse(null) != null) {
								final var role = e.getGuild().getRoleById(role_id);
								if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
									e.getGuild().removeRoleFromMember(member, role).queue(success -> {
										Azrael.SQLInsertHistory(user_id, e.getGuild().getIdLong(), "roleRemove", e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), 0, role.getName());
										Azrael.SQLInsertActionLog("MEMBER_ROLE_REMOVE", user_id, e.getGuild().getIdLong(), "Role Remove "+role.getName());
										e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_REMOVE_RETRACT).replaceFirst("\\{\\}", member.getUser().getName()+"#"+member.getUser().getDiscriminator()).replaceFirst("\\{\\}", member.getUser().getId()).replaceFirst("\\{\\}", role.getName()).replace("{}", e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator())).build()).queue();
										logger.info("User {} has removed the role {} from user {} in guild {}", e.getMember().getUser().getId(), role_id, user_id, e.getGuild().getId());
										Hashes.clearTempCache(key);
									});
								}
								else {
									message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setColor(Color.RED);
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MANAGE_ROLES.getName()).build()).queue();
									logger.warn("MANAGE_ROLES permission required to remove roles in guild {}", e.getGuild().getId());
									Hashes.clearTempCache(key);
								}
							}
							else {
								message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_REMOVE_ALREADY)).build()).queue();
								Hashes.clearTempCache(key);
							}
						}
						else {
							message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_LEFT)).build()).queue();
							Hashes.clearTempCache(key);
						}
					}
					else {
						message.setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_NUMBER)).build()).queue();
						cache.setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_REMOVE_ROLE.getColumn(), _message);
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("watch")) {
				if(UserPrivs.comparePrivilege(e.getMember(), STATIC.getCommandLevel(e.getGuild(), Command.USER_USE_WATCH_CHANNEL)) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
					var trash_channel = _allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.TRA.getType())).findAny().orElse(null);
					var watch_channel = _allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.WAT.getType())).findAny().orElse(null);
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
								if(Azrael.SQLInsertWatchlist(user_id, e.getGuild().getIdLong(), 1, watchChannel, higherPrivileges) == 0) {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("User {} couldn't be added to the watch list with level 1 in guild {}", user_id, e.getGuild().getId());
									return;
								}
								else {
									Hashes.addWatchlist(e.getGuild().getId()+"-"+user_id, new Watchlist(1, watchChannel, higherPrivileges));
									logger.info("User {} has added user {} to the watch list with level 1 in guild {}", e.getMember().getUser().getId(), user_id, e.getGuild().getId());
								}
							}
							case "2" -> {
								if(Azrael.SQLInsertWatchlist(user_id, e.getGuild().getIdLong(), 2, watchChannel, higherPrivileges) == 0) {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("User {} couldn't be added to the watch list with level 2 in guild {}", user_id, e.getGuild().getId());
									return;
								}
								else {
									Hashes.addWatchlist(e.getGuild().getId()+"-"+user_id, new Watchlist(2, watchChannel, higherPrivileges));
									logger.info("User {} has added user {} to the watch list with level 2 in guild {}", user_id, e.getGuild().getId());
								}
							}
							default  -> { return; }
						}
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_WATCH_ADDED)+_message).build()).queue();
						Hashes.clearTempCache(key);
					}
					else {
						//throw error if no trash or watch channel has been registered
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_TRA_CHANNEL)).build()).queue();
						Hashes.clearTempCache(key);
					}
				}
				else {
					var trash_channel = _allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.TRA.getType())).findAny().orElse(null);
					if(trash_channel != null) {
						switch(_message) {
							case "1" -> {
								if(Azrael.SQLInsertWatchlist(user_id, e.getGuild().getIdLong(), 1, trash_channel.getChannel_ID(), false) == 0) {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("User {} couldn't be added to the watch list with level 1 in guild {}", user_id, e.getGuild().getId());
									return;
								}
								else {
									Hashes.addWatchlist(e.getGuild().getId()+"-"+user_id, new Watchlist(1, trash_channel.getChannel_ID(), false));
									logger.info("User {} has added user {} to the watch list with level 1 in guild {}", e.getMember().getUser().getId(), user_id, e.getGuild().getId());
								}
							}
							case "2" -> {
								if(Azrael.SQLInsertWatchlist(user_id, e.getGuild().getIdLong(), 2, trash_channel.getChannel_ID(), false) == 0) {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("User {} couldn't be added to the watch list with level 2 in guild {}", user_id, e.getGuild().getId());
									return;
								}
								else {
									Hashes.addWatchlist(e.getGuild().getId()+"-"+user_id, new Watchlist(2, trash_channel.getChannel_ID(), false));
									logger.info("User {} has added user {} to the watch list with level 2 in guild {}", user_id, e.getGuild().getId());
								}
							}
							default  -> { return; }
						}
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_WATCH_ADDED)+_message).build()).queue();
						Hashes.clearTempCache(key);
					}
					else {
						//throw error if no trash channel has been registered
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_TRA_CHANNEL)).build()).queue();
						Hashes.clearTempCache(key);
					}
				}
				Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_WATCH.getColumn(), _message);
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*",	"").equals("gift-experience")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					Ranking user_details = RankingSystem.SQLgetWholeRankView(user_id, e.getGuild().getIdLong());
					long experience = Integer.parseInt(_message);
					long totExperience = 0;
					long currentExperience = 0;
					long rankUpExperience = 0;
					int level = 0;
					long assign_role = 0;
					boolean toBreak = false;
					var roles = RankingSystem.SQLgetRoles(e.getGuild().getIdLong());
					for(final var ranks : RankingSystem.SQLgetLevels(e.getGuild().getIdLong())) {
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
					user_details.setLastUpdate(new Timestamp(System.currentTimeMillis()));
					if(RankingSystem.SQLsetLevelUp(user_details.getUser_ID(), e.getGuild().getIdLong(), user_details.getLevel(), user_details.getExperience(), user_details.getCurrency(), user_details.getCurrentRole(), user_details.getLastUpdate()) > 0) {
						RankingSystem.SQLInsertActionLog("low", user_details.getUser_ID(), e.getGuild().getIdLong(), "Experience points gifted", "User received "+experience+" experience points");
						Hashes.addRanking(e.getGuild().getIdLong(), user_details.getUser_ID(), user_details);
						if(roles.size() > 0) {
							if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
								Member member = e.getGuild().getMemberById(user_id);
								if(member != null) {
									if(!botConfig.getCollectRankingRoles()) {
										member.getRoles().parallelStream().forEach(r -> {
											roles.parallelStream().forEach(role -> {
												if(r.getIdLong() == role.getRole_ID())
													e.getGuild().removeRoleFromMember(member, e.getGuild().getRoleById(r.getIdLong())).queue();
											});
										});
									}
									if(assign_role != 0) {
										e.getGuild().addRoleToMember(member, e.getGuild().getRoleById(assign_role)).queue();
									}
								}
								else {
									e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_WARNING)).setColor(Color.ORANGE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_LEFT)).build()).queue();
								}
							}
							else {
								e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MANAGE_ROLES.getName()).build()).queue();
								logger.warn("MANAGE ROLES permission required to assign a ranking role in guild {}", e.getGuild().getId());
							}
						}
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_GIFT_EXP_ADDED)).build()).queue();
						logger.info("User {} has gifted {} experience points to user {} in guild {}", e.getMember().getUser().getId(), _message, user_id, e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
					else {
						e.getChannel().sendMessage(message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("The amount of {} experience points couldn't be gifted to user {} in guild {}", _message, user_id, e.getGuild().getId());
					}
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_GIFT_EXPERIENCE.getColumn(), _message);
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("set-experience")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					Ranking user_details = RankingSystem.SQLgetWholeRankView(user_id, e.getGuild().getIdLong());
					long experience = Long.parseLong(_message);
					long totExperience = 0;
					long currentExperience = 0;
					long rankUpExperience = 0;
					int level = 0;
					long assign_role = 0;
					boolean toBreak = false;
					Set<Long> rankingRoles = new HashSet<Long>();
					var roles = RankingSystem.SQLgetRoles(e.getGuild().getIdLong());
					for(final var ranks : RankingSystem.SQLgetLevels(e.getGuild().getIdLong())) {
						if(experience >= ranks.getExperience()) {
							totExperience = ranks.getExperience();
							level = ranks.getLevel();
							currentExperience = experience - ranks.getExperience();
							var role = roles.parallelStream().filter(f -> f.getLevel() == ranks.getLevel()).findAny().orElse(null);
							if(role != null) {
								assign_role = role.getRole_ID();
								if(botConfig.getCollectRankingRoles())
									rankingRoles.add(assign_role);
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
					user_details.setLastUpdate(new Timestamp(System.currentTimeMillis()));
					if(RankingSystem.SQLsetLevelUp(user_details.getUser_ID(), e.getGuild().getIdLong(), user_details.getLevel(), user_details.getExperience(), user_details.getCurrency(), user_details.getCurrentRole(), user_details.getLastUpdate()) > 0) {
						RankingSystem.SQLInsertActionLog("low", user_details.getUser_ID(), e.getGuild().getIdLong(), "Experience points edited", "User has been set to "+experience+" experience points");
						Hashes.addRanking(e.getGuild().getIdLong(), user_details.getUser_ID(), user_details);
						if(roles.size() > 0) {
							if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
								Member member = e.getGuild().getMemberById(user_id);
								if(member != null) {
									if(!botConfig.getCollectRankingRoles()) {
										member.getRoles().parallelStream().forEach(r -> {
											roles.parallelStream().forEach(role -> {
												if(r.getIdLong() == role.getRole_ID())
													e.getGuild().removeRoleFromMember(member, e.getGuild().getRoleById(r.getIdLong())).queue();
											});
										});
									}
									if(!botConfig.getCollectRankingRoles() && assign_role != 0) {
										e.getGuild().addRoleToMember(member, e.getGuild().getRoleById(assign_role)).queue();
									}
									else {
										rankingRoles.forEach(r -> {
											e.getGuild().addRoleToMember(member, e.getGuild().getRoleById(r)).queue();
										});
									}
								}
								else {
									e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_WARNING)).setColor(Color.ORANGE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_LEFT)).build()).queue();
								}
							}
							else {
								e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MANAGE_ROLES.getName()).build()).queue();
								logger.warn("MANAGE ROLES permission required to assign a ranking role in guild {}", e.getGuild().getId());
							}
						}
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_SET_EXP_UPDATED)).build()).queue();
						logger.info("User {} has set {} experience points to user {} in guild {}", e.getMember().getUser().getId(), _message, user_id, e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
					else {
						e.getChannel().sendMessage(message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("The amount of {} experience points couldn't be set for user {} in guild {}", _message, user_id, e.getGuild().getId());
					}
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_SET_EXPERIENCE.getColumn(), _message);
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("set-level")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					int level = Integer.parseInt(_message);
					if(level <= guild_settings.getMaxLevel()) {
						Ranking user_details = RankingSystem.SQLgetWholeRankView(user_id, e.getGuild().getIdLong());
						long experience = 0;
						long rankUpExperience = 0;
						long assign_role = 0;
						boolean toBreak = false;
						Set<Long> rankingRoles = new HashSet<Long>();
						var roles = RankingSystem.SQLgetRoles(e.getGuild().getIdLong());
						for(final var ranks : RankingSystem.SQLgetLevels(e.getGuild().getIdLong())) {
							var role = roles.parallelStream().filter(f -> f.getLevel() == ranks.getLevel()).findAny().orElse(null);
							if(role != null && toBreak == false) {
								assign_role = role.getRole_ID();
								if(botConfig.getCollectRankingRoles())
									rankingRoles.add(assign_role);
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
						user_details.setLastUpdate(new Timestamp(System.currentTimeMillis()));
						if(RankingSystem.SQLsetLevelUp(user_details.getUser_ID(), e.getGuild().getIdLong(), user_details.getLevel(), user_details.getExperience(), user_details.getCurrency(), user_details.getCurrentRole(), user_details.getLastUpdate()) > 0) {
							RankingSystem.SQLInsertActionLog("low", user_details.getUser_ID(), e.getGuild().getIdLong(), "Level changed", "User is now level "+user_details.getLevel());
							Hashes.addRanking(e.getGuild().getIdLong(), user_details.getUser_ID(), user_details);
							if(roles.size() > 0) {
								if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
									Member member = e.getGuild().getMemberById(user_id);
									if(member != null) {
										if(!botConfig.getCollectRankingRoles()) {
											member.getRoles().parallelStream().forEach(r -> {
												roles.parallelStream().forEach(role -> {
													if(r.getIdLong() == role.getRole_ID())
														e.getGuild().removeRoleFromMember(member, e.getGuild().getRoleById(r.getIdLong())).queue();
												});
											});
										}
										if(!botConfig.getCollectRankingRoles() && assign_role != 0) {
											e.getGuild().addRoleToMember(member, e.getGuild().getRoleById(assign_role)).queue();
										}
										else {
											rankingRoles.forEach(r -> {
												e.getGuild().addRoleToMember(member, e.getGuild().getRoleById(r)).queue();
											});
										}
									}
									else {
										e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_WARNING)).setColor(Color.ORANGE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_LEFT)).build()).queue();
									}
								}
								else {
									e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MANAGE_ROLES.getName()).build()).queue();
									logger.warn("MANAGE ROLES permission required to assign a ranking role in guild {}", e.getGuild().getId());
								}
							}
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_LEVEL_UPDATED)).build()).queue();
							logger.info("User {} has set the level {} to user {} in guild {}", e.getMember().getUser().getId(), _message, user_id, e.getGuild().getId());
							Hashes.clearTempCache(key);
						}
						else {
							e.getChannel().sendMessage(message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("The user {} couldn't receive the level {} in guild {}", user_id, _message, e.getGuild().getId());
						}
					}
					else {
						e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_LEVEL_ERR).replace("{}", ""+guild_settings.getMaxLevel())).build()).queue();
					}
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_SET_LEVEL.getColumn(), _message);
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("gift-currency")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					Ranking user_details = RankingSystem.SQLgetWholeRankView(user_id, e.getGuild().getIdLong());
					long currency = Long.parseLong(_message);
					user_details.setCurrency(user_details.getCurrency()+currency);
					user_details.setLastUpdate(new Timestamp(System.currentTimeMillis()));
					if(RankingSystem.SQLUpdateCurrency(user_details.getUser_ID(), e.getGuild().getIdLong(), user_details.getCurrency(), user_details.getLastUpdate()) > 0) {
						RankingSystem.SQLInsertActionLog("low", user_details.getUser_ID(), e.getGuild().getIdLong(), "Money gifted", "User received money in value of "+currency+" "+guild_settings.getCurrency());
						Hashes.addRanking(e.getGuild().getIdLong(), user_details.getUser_ID(), user_details);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_GIFT_CUR_ADDED)).build()).queue();
						logger.info("User {} has gifted {} money to user {} in guild {}", e.getMember().getUser().getId(), _message, user_id, e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
					else {
						e.getChannel().sendMessage(message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("The amount of {} money couldn't be gifted to user {} in guild {}", _message, user_id, e.getGuild().getId());
					}
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_GIFT_CURRENCY.getColumn(), _message);
				}
			}
			else if(cache.getAdditionalInfo().replaceAll("[0-9]*", "").equals("set-currency")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					Ranking user_details = RankingSystem.SQLgetWholeRankView(user_id, e.getGuild().getIdLong());
					long currency = Long.parseLong(_message);
					user_details.setCurrency(currency);
					user_details.setLastUpdate(new Timestamp(System.currentTimeMillis()));
					if(RankingSystem.SQLUpdateCurrency(user_details.getUser_ID(), e.getGuild().getIdLong(), user_details.getCurrency(), user_details.getLastUpdate()) > 0) {
						RankingSystem.SQLInsertActionLog("low", user_details.getUser_ID(), e.getGuild().getIdLong(), "Money set", "Currency value for the user has been changed to "+currency+" "+guild_settings.getCurrency());
						Hashes.addRanking(e.getGuild().getIdLong(), user_details.getUser_ID(), user_details);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_SET_CUR_UPDATED)).build()).queue();
						logger.info("User {} has set {} money to user {} in guild {}", e.getMember().getUser().getId(), _message, user_id, e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
					else {
						e.getChannel().sendMessage(message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Currency of user {} couldn't be set to {} in guild {}", user_id, _message, e.getGuild().getId());
					}
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_SET_CURRENCY.getColumn(), _message);
				}
			}
		}
		else {
			Hashes.clearTempCache(key);
		}
	}
	
	private static void checkIfDeleteMessagesAfterAction(GuildMessageReceivedEvent e, Cache cache, long user_id, String _message, EmbedBuilder message, String key, BotConfigs botConfig) {
		if(!botConfig.isMuteMessageDeleteEnabled())
			Hashes.clearTempCache(key);
		else {
			var removeMessages = botConfig.getMuteAutoDeleteMessages();
			if(botConfig.getMuteForceMessageDeletion()) {
				if(removeMessages > 0) {
					deleteMessages(e, user_id, _message, removeMessages, message, key, true, botConfig);
				}
				else {
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_DELETE_HELP)).build()).queueAfter(1, TimeUnit.SECONDS);
					cache.updateDescription("delete-messages"+user_id).setExpiration(180000);
					Hashes.addTempCache(key, cache);
				}
			}
			else {
				message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_DELETE_QUESTION));
				message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_YES), "", true);
				message.addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_NO), "", true);
				cache.updateDescription("delete-messages-question"+user_id).updateDescription3((removeMessages > 0 ? ""+removeMessages : "")).setExpiration(180000);
				Hashes.addTempCache(key, cache);
				e.getChannel().sendMessage(message.build()).queueAfter(1, TimeUnit.SECONDS);
			}
		}
	}
	
	private static void deleteMessages(GuildMessageReceivedEvent e, long user_id, String _message, int messagesCount, EmbedBuilder message, String key, boolean passValue, BotConfigs botConfig) {
		if(_message.replaceAll("[0-9]*", "").length() == 0 || passValue) {
			EmbedBuilder error = new EmbedBuilder().setColor(Color.RED);
			int value = (passValue ? messagesCount : Integer.parseInt(_message));
			if(value == 0) {
				e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.USER_DELETE_ABORT)).queue();
			}
			else if(value > 100) {
				e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.USER_DELETE_HELP)).queue();
				return;
			}
			else {
				List<ArrayList<Messages>> messages = Hashes.getWholeMessagePool(e.getGuild().getIdLong()).values().parallelStream().filter(f -> f.get(0).getUserID() == user_id && f.get(0).getGuildID() == e.getGuild().getIdLong()).collect(Collectors.toList());
				if(messages.size() > 0) {
					if(e.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
						int hash_counter = 0;
						StringBuilder collected_messages = new StringBuilder();
						ArrayList<Long> channelErr = new ArrayList<Long>();
						for(int i = messages.size()-1; i >= 0; i--) {
							hash_counter++;
							final var currentMessage = messages.get(i).get(0);
							TextChannel channel = e.getGuild().getTextChannelById(currentMessage.getChannelID());
							if(channel != null) {
								if(e.getGuild().getSelfMember().hasPermission(channel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY) || STATIC.setPermissions(e.getGuild(), channel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY))) {
									Message m = e.getGuild().getTextChannelById(currentMessage.getChannelID()).retrieveMessageById(currentMessage.getMessageID()).complete();
									for(final var cachedMessage: messages.get(i)) {
										collected_messages.append((cachedMessage.isEdit() ? "EDIT" : "MESSAGE")+" ["+cachedMessage.getTime().toString()+" - "+cachedMessage.getUserName()+" ("+cachedMessage.getUserID()+")]: "+cachedMessage.getMessage());
									}
									Hashes.removeMessagePool(e.getGuild().getIdLong(), currentMessage.getMessageID());
									m.delete().queue();
									if(i == 0 || hash_counter == value) {
										break;
									}
								}
								else {
									if(channelErr.contains(currentMessage.getChannelID())) {
										error.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS));
										e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_MANAGE+" and "+Permission.MESSAGE_HISTORY.getName())+channel.getAsMention()).build()).queue();
										logger.warn("MESSAGE_MANAGE and MESSAGE_HISTORY permissions required to retrieve and delete messages in channel {} in guild {}", channel.getId(), e.getGuild().getId());
										channelErr.add(currentMessage.getChannelID());
									}
									hash_counter--;
								}
							}
							else {
								Hashes.removeMessagePool(e.getGuild().getIdLong(), currentMessage.getMessageID());
								hash_counter--;
							}
						}
						
						if(collected_messages.length() > 0) {
							final var userMessage = messages.get(0).get(0);
							//TODO: remember to translate required translations
							final String content = STATIC.getTranslation2(e.getGuild(), Translation.USER_DELETE_REMOVED_2).replaceFirst("\\{\\}", ""+hash_counter).replaceFirst("\\{\\}", userMessage.getUserName()).replace("{}", ""+userMessage.getUserID())+collected_messages.toString();
							long nextNumber = Azrael.SQLgetNextNumberDeletedMessages();
							if(nextNumber > 0) {
								String nextNumberKey = ""+nextNumber;
								while(nextNumberKey.length() < 5)
									nextNumberKey = "0"+nextNumberKey;
								nextNumberKey = "#"+nextNumberKey+".azr";
								
								final String fileName = nextNumberKey;
								if(FileHandler.createFile(Directory.TEMP, fileName, content) && FileHandler.createFile(Directory.USER_LOG, fileName, content)) {
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_DELETE_REMOVED)+fileName).build()).queue();
									e.getChannel().sendFile(new File(Directory.TEMP+fileName), fileName).queue(m -> {
										FileHandler.deleteFile(Directory.TEMP, fileName);
									});
									Azrael.SQLInsertActionLog("MESSAGES_DELETED", user_id, e.getGuild().getIdLong(), fileName);
									logger.info("User {} has deleted {} messages of user {} in guild {}", e.getMember().getUser().getId(), hash_counter, userMessage.getUserID(), e.getGuild().getId());
								}
								else {
									error.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
									e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_DELETE_ERR)).build()).queue();
								}
							}
							else {
								error.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
								e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_DELETE_ERR)).build()).queue();
							}
						}
						else {
							if(botConfig.getCacheLog())
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_DELETE_NOTHING)).build()).queue();
							else
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_DELETE_NOTHING_2)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessage(message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_MANAGE.getName()).build()).queue();
						logger.warn("MANAGE MESSAGES permission for message deletions is missing in guild {}", e.getGuild().getId());
					}
				}
				else {
					if(botConfig.getCacheLog())
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_DELETE_NOTHING)).build()).queue();
					else
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_DELETE_NOTHING_2)).build()).queue();
				}
			}
			Hashes.clearTempCache(key);
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER_DELETE_MESSAGES.getColumn(), _message);
		}
	}
}
