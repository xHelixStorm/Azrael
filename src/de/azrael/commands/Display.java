package de.azrael.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiManager;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.constructors.CategoryConf;
import de.azrael.constructors.Channels;
import de.azrael.constructors.Dailies;
import de.azrael.constructors.Roles;
import de.azrael.core.Hashes;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Channel;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.sql.DiscordRoles;
import de.azrael.sql.RankingSystem;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * The Display command can print details of various things
 * like roles, registered roles, text channels, registered
 * text channels and so on. 
 * @author xHelixStorm
 *
 */

public class Display implements CommandPublic{
	private final static Logger logger = LoggerFactory.getLogger(Display.class);
	private static EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.MAGENTA);
	private static EmbedBuilder error = new EmbedBuilder().setColor(Color.RED);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.DISPLAY);
	}

	@Override
	public boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		var adminPermission = BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
		long guild_id = e.getGuild().getIdLong();
		StringBuilder out = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		
		//if no arguments have been added to the command, print a list of all available parameters
		if(args.length == 0) {
			//sub commands are disabled by default
			boolean displayRoles = false;
			boolean displayRegisteredRoles = false;
			boolean displayRankingRoles = false;
			boolean displayCategories = false;
			boolean displayRegisteredCategories = false;
			boolean displayTextChannels = false;
			boolean displayVoiceChannels = false;
			boolean displayRegisteredChannels = false;
			boolean displayDailies = false;
			boolean displayWatchedUsers = false;
			
			
			//default values at 100 to avoid command abuse in case of errors
			int displayRolesLevel = 100;
			int displayRegisteredRolesLevel = 100;
			int displayRankingRolesLevel = 100;
			int displayCategoriesLevel = 100;
			int displayRegisteredCategoriesLevel = 100;
			int displayTextChannelsLevel = 100;
			int displayVoiceChannelsLevel = 100;
			int displayRegisteredChannelsLevel = 100;
			int displayDailiesLevel = 100;
			int displayWatchedUsersLevel = 100;
			
			final var commands = (ArrayList<?>)BotConfiguration.SQLgetCommand(guild_id, 1, Command.DISPLAY_ROLES, Command.DISPLAY_REGISTERED_ROLES, Command.DISPLAY_RANKING_ROLES
					, Command.DISPLAY_CATEGORIES, Command.DISPLAY_REGISTERED_CATEGORIES, Command.DISPLAY_TEXT_CHANNELS, Command.DISPLAY_VOICE_CHANNELS
					, Command.DISPLAY_REGISTERED_CHANNELS, Command.DISPLAY_DAILIES, Command.DISPLAY_WATCHED_USERS);
			
			for(final Object command : commands) {
				int permissionLevel = 0;
				boolean enabled = false;
				String name = "";
				for(Object values : (ArrayList<?>)command) {
					if(values instanceof Integer)
						permissionLevel = (Integer)values;
					else if(values instanceof Boolean)
						enabled = (Boolean)values;
					else if(values instanceof String)
						name = ((String)values).split(":")[0];
				}
				
				if(name.equals(Command.DISPLAY_ROLES.getColumn())) {
					displayRoles = enabled;
					displayRolesLevel = permissionLevel;
				}
				else if(name.equals(Command.DISPLAY_REGISTERED_ROLES.getColumn())) {
					displayRegisteredRoles = enabled;
					displayRegisteredRolesLevel = permissionLevel;
				}
				else if(name.equals(Command.DISPLAY_RANKING_ROLES.getColumn())) {
					displayRankingRoles = enabled;
					displayRankingRolesLevel = permissionLevel;
				}
				else if(name.equals(Command.DISPLAY_CATEGORIES.getColumn())) {
					displayCategories = enabled;
					displayCategoriesLevel = permissionLevel;
				}
				else if(name.equals(Command.DISPLAY_REGISTERED_CATEGORIES.getColumn())) {
					displayRegisteredCategories = enabled;
					displayRegisteredCategoriesLevel = permissionLevel;
				}
				else if(name.equals(Command.DISPLAY_TEXT_CHANNELS.getColumn())) {
					displayTextChannels = enabled;
					displayTextChannelsLevel = permissionLevel;
				}
				else if(name.equals(Command.DISPLAY_VOICE_CHANNELS.getColumn())) {
					displayVoiceChannels = enabled;
					displayVoiceChannelsLevel = permissionLevel;
				}
				else if(name.equals(Command.DISPLAY_REGISTERED_CHANNELS.getColumn())) {
					displayRegisteredChannels = enabled;
					displayRegisteredChannelsLevel = permissionLevel;
				}
				else if(name.equals(Command.DISPLAY_DAILIES.getColumn())) {
					displayRegisteredChannels = enabled;
					displayRegisteredChannelsLevel = permissionLevel;
				}
				else if(name.equals(Command.DISPLAY_WATCHED_USERS.getColumn())) {
					displayWatchedUsers = enabled;
					displayWatchedUsersLevel = permissionLevel;
				}
			}
			
			sb.append((displayRoles && (UserPrivs.comparePrivilege(e.getMember(), displayRolesLevel) || adminPermission) 								? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_1).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ROLES)) : "")
					+ (displayRegisteredRoles && (UserPrivs.comparePrivilege(e.getMember(), displayRegisteredRolesLevel) || adminPermission) 			? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_2).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTERED_ROLES)) : "")
					+ (displayRankingRoles && (UserPrivs.comparePrivilege(e.getMember(), displayRankingRolesLevel) || adminPermission) 					? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_3).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_RANKING_ROLES)) : "")
					+ (displayCategories && (UserPrivs.comparePrivilege(e.getMember(), displayCategoriesLevel) || adminPermission) 						? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_4).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_CATEGORIES)) : "")
					+ (displayRegisteredCategories && (UserPrivs.comparePrivilege(e.getMember(), displayRegisteredCategoriesLevel) || adminPermission) 	? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_5).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTERED_CATEGORIES)) : "")
					+ (displayTextChannels && (UserPrivs.comparePrivilege(e.getMember(), displayTextChannelsLevel) || adminPermission) 					? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_6).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNELS)) : "")
					+ (displayVoiceChannels && (UserPrivs.comparePrivilege(e.getMember(), displayVoiceChannelsLevel) || adminPermission) 				? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_7).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_VOICE_CHANNELS)) : "")
					+ (displayRegisteredChannels && (UserPrivs.comparePrivilege(e.getMember(), displayRegisteredChannelsLevel) || adminPermission) 		? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_8).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTERED_CHANNELS)) : "")
					+ (displayDailies && (UserPrivs.comparePrivilege(e.getMember(), displayDailiesLevel) || adminPermission) 							? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_9).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_DAILIES)) : "")
					+ (displayWatchedUsers && (UserPrivs.comparePrivilege(e.getMember(), displayWatchedUsersLevel) || adminPermission) 					? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_10).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_WATCHED_USERS)) : ""));
			
			if(sb.length() > 0) {
				out.append(STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP)+sb.toString());
				e.getChannel().sendMessage(messageBuild.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(out.toString()).build()).queue();
			}
			else {
				e.getChannel().sendMessage(messageBuild.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.DISPLAY_DISABLED)).build()).queue();
			}
		}
		//display all roles
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ROLES)) && STATIC.getCommandEnabled(e.getGuild(), Command.DISPLAY_ROLES)) {
			//verify that the current user is allowed to use this parameter
			final var rolesLevel = STATIC.getCommandLevel(e.getGuild(), Command.DISPLAY_ROLES);
			if(UserPrivs.comparePrivilege(e.getMember(), rolesLevel) || adminPermission) {
				//retrieve roles from the server
				final var roles = e.getGuild().getRoles();
				int count = 1;
				for(Role r : roles) {
					if(count == 10) break;
					out.append("**"+r.getName() + "** (" + r.getId() + ") \n");
					count++;
				}
				final int maxPage = (roles.size()/10)+(roles.size()%10 > 0 ? 1 : 0);
				e.getChannel().sendMessage(messageBuild.setFooter("1/"+maxPage).setDescription(out.toString()).build()).queue(m -> {
					addReactions(e, m, maxPage, "1", roles);
				});
			}
			else {
				UserPrivs.throwNotEnoughPrivilegeError(e, rolesLevel);
			}
		}
		//display all registered roles
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTERED_ROLES)) && STATIC.getCommandEnabled(e.getGuild(), Command.DISPLAY_REGISTERED_ROLES)) {
			//verify that the current user is allowed to use this parameter
			final var registeredRolesLevel = STATIC.getCommandLevel(e.getGuild(), Command.DISPLAY_REGISTERED_ROLES);
			if(UserPrivs.comparePrivilege(e.getMember(), registeredRolesLevel) || adminPermission) {
				//retrieve roles from table
				final var roles = DiscordRoles.SQLgetRoles(guild_id);
				int count = 1;
				for(Roles r : roles) {
					if(count == 10) break;
					if(!r.getCategory_ABV().equals("def"))
						out.append("**"+r.getRole_Name() + "** (" + r.getRole_ID() + ") \n"
							+ STATIC.getTranslation(e.getMember(), Translation.DISPLAY_ROLE_TYPE)+r.getCategory_Name()+"\n"
							+ STATIC.getTranslation(e.getMember(), Translation.DISPLAY_PERMISSION_LEVEL)+r.getLevel()+"\n"
							+ STATIC.getTranslation(e.getMember(), Translation.DISPLAY_PERSISTANT)+(r.isPersistent() ? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_PERSISTANT) : STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_NOT_PERSISTANT))+"\n\n");
					count++;
				}
				if(out.length() > 0) {
					final int maxPage = (roles.size()/10)+(roles.size()%10 > 0 ? 1 : 0);
					e.getChannel().sendMessage(messageBuild.setDescription(out.toString()).build()).queue(m -> {
						addReactions(e, m, maxPage, "1", roles);
					});
				}
				else {
					e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.DISPLAY_INPUT_NOT_FOUND)).build()).queue();
				}
			}
			else {
				UserPrivs.throwNotEnoughPrivilegeError(e, registeredRolesLevel);
			}
		}
		//display all registered ranking roles
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_RANKING_ROLES)) && STATIC.getCommandEnabled(e.getGuild(), Command.DISPLAY_RANKING_ROLES)) {
			//verify that the current user is allowed to use this parameter
			final var rankingRolesLevel = STATIC.getCommandLevel(e.getGuild(), Command.DISPLAY_RANKING_ROLES);
			if(UserPrivs.comparePrivilege(e.getMember(), rankingRolesLevel) || adminPermission) {
				//confirm that the ranking system is enabled
				if(RankingSystem.SQLgetGuild(guild_id).getRankingState()) {
					//retrieve all ranking roles from table
					final var roles = RankingSystem.SQLgetRoles(guild_id);
					int count = 1;
					for(final var r : roles) {
						if(count == 10) break;
						out.append("**"+r.getRole_Name() + "** (" + r.getRole_ID() + ") \n"+STATIC.getTranslation(e.getMember(), Translation.DISPLAY_UNLOCK_LEVEL) + r.getLevel() + "\n");
						count++;
					}
					if(out.length() > 0) {
						final int maxPage = (roles.size()/10)+(roles.size()%10 > 0 ? 1 : 0);
						e.getChannel().sendMessage(messageBuild.setFooter("1/"+maxPage).setDescription(out.toString()).build()).queue(m -> {
							addReactions(e, m, maxPage, "2", roles);
						});
					}
					else {
						e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.DISPLAY_INPUT_NOT_FOUND)).build()).queue();
					}
				}
				else {
					e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.LEVEL_SYSTEM_NOT_ENABLED)).build()).queue();
				}
			}
			else {
				UserPrivs.throwNotEnoughPrivilegeError(e, rankingRolesLevel);
			}
		}
		//display all categories
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CATEGORIES)) && STATIC.getCommandEnabled(e.getGuild(), Command.DISPLAY_CATEGORIES)) {
			//verify that the current user is allowed to use this parameter
			final var categoriesLevel = STATIC.getCommandLevel(e.getGuild(), Command.DISPLAY_CATEGORIES);
			if(UserPrivs.comparePrivilege(e.getMember(), categoriesLevel) || adminPermission) {
				//retrieve all categories from the server
				final var categories = e.getGuild().getCategories();
				int count = 1;
				for(Category ct : categories) {
					if(count == 10) break;
					out.append("**"+ct.getName()+ "** (" + ct.getId() + ") \n");
					count++;
				}
				if(out.length() > 0) {
					final int maxPage = (categories.size()/10)+(categories.size()%10 > 0 ? 1 : 0);
					e.getChannel().sendMessage(messageBuild.setFooter("1/"+maxPage).setDescription(out.toString()).build()).queue(m -> {
						addReactions(e, m, maxPage, "1", categories);
					});
				}
				else {
					e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.DISPLAY_INPUT_NOT_FOUND)).build()).queue();
				}
			}
			else {
				UserPrivs.throwNotEnoughPrivilegeError(e, categoriesLevel);
			}
		}
		//display all registered categories
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTERED_CATEGORIES)) && STATIC.getCommandEnabled(e.getGuild(), Command.DISPLAY_REGISTERED_CATEGORIES)) {
			//verify that the current user is allowed to use this parameter
			final var registeredCategoriesLevel = STATIC.getCommandLevel(e.getGuild(), Command.DISPLAY_REGISTERED_CATEGORIES);
			if(UserPrivs.comparePrivilege(e.getMember(), registeredCategoriesLevel) || adminPermission) {
				//retrieve all registered text channels from table
				final var categories =  Azrael.SQLgetCategories(guild_id);
				int count = 1;
				for(final CategoryConf ct : categories) {
					Category category = e.getGuild().getCategoryById(ct.getCategoryID());
					if(category != null) {
						if(count <= 10)
							out.append("**"+category.getName()+"** ("+category.getId()+")\n"+STATIC.getTranslation(e.getMember(), Translation.DISPLAY_CATEGORY_TYPE)+ct.getType()+"\n\n");
					}
					else {
						Azrael.SQLDeleteCategoryConf(ct.getCategoryID());
						Azrael.SQLDeleteCategory(ct.getCategoryID());
					}
					count++;
					
				}
				if(out.length() > 0) {
					final int maxPage = (categories.size()/10)+(categories.size()%10 > 0 ? 1 : 0);
					e.getChannel().sendMessage(messageBuild.setFooter("1/"+maxPage).setDescription(out.toString()).build()).queue(m -> {
						addReactions(e, m, maxPage, "1", categories);
					});
				}
				else {
					e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.DISPLAY_INPUT_NOT_FOUND)).build()).queue();
				}
			}
			else{
				UserPrivs.throwNotEnoughPrivilegeError(e, registeredCategoriesLevel);
			}
		}
		//display all text channels
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNELS)) && STATIC.getCommandEnabled(e.getGuild(), Command.DISPLAY_TEXT_CHANNELS)) {
			//verify that the current user is allowed to use this parameter
			final var textChannelsLevel = STATIC.getCommandLevel(e.getGuild(), Command.DISPLAY_TEXT_CHANNELS);
			if(UserPrivs.comparePrivilege(e.getMember(), textChannelsLevel) || adminPermission) {
				//retrieve all text channels from the server
				final var textChannels = e.getGuild().getTextChannels();
				int count = 1;
				for(TextChannel tc : textChannels) {
					if(count == 10) break;
					out.append("**"+tc.getName() + "** (" + tc.getId() + ") \n");
					count++;
				}
				if(out.length() > 0) {
					final int maxPage = (textChannels.size()/10)+(textChannels.size()%10 > 0 ? 1 : 0);
					e.getChannel().sendMessage(messageBuild.setFooter("1/"+maxPage).setDescription(out.toString()).build()).queue(m -> {
						addReactions(e, m, maxPage, "1", textChannels);
					});
				}
				else {
					e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.DISPLAY_INPUT_NOT_FOUND)).build()).queue();
				}
			}
			else {
				UserPrivs.throwNotEnoughPrivilegeError(e, textChannelsLevel);
			}
		}
		//display all voice channels
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_VOICE_CHANNELS)) && STATIC.getCommandEnabled(e.getGuild(), Command.DISPLAY_VOICE_CHANNELS)) {
			//verify that the current user is allowed to use this parameter
			final var voiceChannelsLevel = STATIC.getCommandLevel(e.getGuild(), Command.DISPLAY_VOICE_CHANNELS);
			if(UserPrivs.comparePrivilege(e.getMember(), voiceChannelsLevel) || adminPermission) {
				//retrieve all voice channels from the server
				final var voiceChannels = e.getGuild().getVoiceChannels();
				int count = 1;
				for(VoiceChannel vc : voiceChannels) {
					if(count == 10) break;
					out.append("**"+vc.getName() + "** (" + vc.getId() + ") \n");
					count++;
				}
				if(out.length() > 0) {
					final int maxPage = (voiceChannels.size()/10)+(voiceChannels.size()%10 > 0 ? 1 : 0);
					e.getChannel().sendMessage(messageBuild.setFooter("1/"+maxPage).setDescription(out.toString()).build()).queue(m -> {
						addReactions(e, m, maxPage, "1", voiceChannels);
					});
				}
				else {
					e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.DISPLAY_INPUT_NOT_FOUND)).build()).queue();
				}
			}
			else {
				UserPrivs.throwNotEnoughPrivilegeError(e, voiceChannelsLevel);
			}
		}
		//display all registered text channels
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTERED_CHANNELS)) && STATIC.getCommandEnabled(e.getGuild(), Command.DISPLAY_REGISTERED_CHANNELS)) {
			//verify that the current user is allowed to use this parameter
			final var registeredChannelsLevel = STATIC.getCommandLevel(e.getGuild(), Command.DISPLAY_REGISTERED_CHANNELS);
			if(UserPrivs.comparePrivilege(e.getMember(), registeredChannelsLevel) || adminPermission) {
				long prevChannelID = 0;
				//retrieve all registered text channels from table
				final var channels = Azrael.SQLgetChannels(guild_id);
				int count = 1;
				for(Channels ch : channels) {
					if(count == 10) break;
					if(prevChannelID != ch.getChannel_ID()) {
						prevChannelID = ch.getChannel_ID();
						if(out.length() > 0)
							out.append("\n\n");
						out.append("**"+ch.getChannel_Name() + "** (" + ch.getChannel_ID() + ") \n"
							+ STATIC.getTranslation(e.getMember(), Translation.DISPLAY_CHANNEL_TYPE)+(ch.getChannel_Type_Name() != null ? ch.getChannel_Type_Name() : STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE))+"\n"
							+ STATIC.getTranslation(e.getMember(), Translation.DISPLAY_URL_CENSORING)+(ch.getURLCensoring() ? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_ENABLED) : STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_NOT_ENABLED))+"\n"
							+ STATIC.getTranslation(e.getMember(), Translation.DISPLAY_TEXT_CENSORING)+(ch.getTxtRemoval() ? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_ENABLED) : STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_NOT_ENABLED))+"\n"
							+ STATIC.getTranslation(e.getMember(), Translation.DISPLAY_LANG_CENSORING)+(ch.getLang_Filter() != null ? ch.getLang_Filter() : STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE)));
						count++;
					}
					else {
						out.append(", "+ch.getLang_Filter());
					}
				}
				if(out.length() > 0) {
					final int maxPage = (channels.size()/10)+(channels.size()%10 > 0 ? 1 : 0);
					e.getChannel().sendMessage(messageBuild.setFooter("1/"+maxPage).setDescription(out.toString()).build()).queue(m -> {
						addReactions(e, m, maxPage, "1", channels);
					});
				}
				else {
					e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.DISPLAY_INPUT_NOT_FOUND)).build()).queue();
				}
			}
			else{
				UserPrivs.throwNotEnoughPrivilegeError(e, registeredChannelsLevel);
			}
		}
		//display all registered daily rewards
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DAILIES)) && STATIC.getCommandEnabled(e.getGuild(), Command.DISPLAY_DAILIES)) {
			//verify that the current user is allowed to use this parameter
			final var dailiesLevel = STATIC.getCommandLevel(e.getGuild(), Command.DISPLAY_DAILIES);
			if(UserPrivs.comparePrivilege(e.getMember(), dailiesLevel) || adminPermission) {
				//retrieve all daily rewards from table
				final var dailies = RankingSystem.SQLgetDailiesAndType(guild_id);
				int count = 1;
				for(Dailies daily : dailies) {
					if(count == 10) break;
					out.append("**"+daily.getDescription()+"**\n"+STATIC.getTranslation(e.getMember(), Translation.DISPLAY_PROBABILITY)+daily.getWeight()+"%\n\n");
					count++;
				}
				if(out.length() > 0) {
					final int maxPage = (dailies.size()/10)+(dailies.size()%10 > 0 ? 1 : 0);
					e.getChannel().sendMessage(messageBuild.setFooter("1/"+maxPage).setDescription(out.toString()).build()).queue(m -> {
						addReactions(e, m, maxPage, "1", dailies);
					});
				}
				else {
					e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.DISPLAY_INPUT_NOT_FOUND)).build()).queue();
				}
			}
			else{
				UserPrivs.throwNotEnoughPrivilegeError(e, dailiesLevel);
			}
		}
		//display all users that are being watched
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_WATCHED_USERS)) && STATIC.getCommandEnabled(e.getGuild(), Command.DISPLAY_WATCHED_USERS)) {
			//verify that the current user is allowed to use this parameter
			final var watchedUsersLevel = STATIC.getCommandLevel(e.getGuild(), Command.DISPLAY_WATCHED_USERS);
			if(UserPrivs.comparePrivilege(e.getMember(), watchedUsersLevel) || adminPermission) {
				List<String> watchedUsers = null;
				//verify that the current user is allowed to use the watch channel, if yes, retrieve users that can get displayed in that channel
				if(!UserPrivs.comparePrivilege(e.getMember(), STATIC.getCommandLevel(e.getGuild(), Command.USER_USE_WATCH_CHANNEL))) {
					watchedUsers = Azrael.SQLgetWholeWatchlist(e.getGuild().getIdLong(), false);
				}
				else {
					watchedUsers = Azrael.SQLgetWholeWatchlist(e.getGuild().getIdLong(), true);
				}
				int count = 1;
				//list the watched users
				for(final var watchedUser : watchedUsers) {
					if(count == 10) break;
					out.append("**"+watchedUser+"**\n");
					count++;
				}
				if(out.length() > 0) {
					final int maxPage = (watchedUsers.size()/10)+(watchedUsers.size()%10 > 0 ? 1 : 0);
					final var users = watchedUsers;
					e.getChannel().sendMessage(messageBuild.setDescription(out.toString()).build()).queue(m -> {
						addReactions(e, m, maxPage, "1", users);
					});
				}
				else {
					e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.DISPLAY_INPUT_NOT_FOUND)).build()).queue();
				}
			}
			else {
				UserPrivs.throwNotEnoughPrivilegeError(e, watchedUsersLevel);
			}
		}
		else {
			e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Display command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.DISPLAY.getColumn(), out.toString().trim());
		}
	}

	private static void addReactions(GuildMessageReceivedEvent e, Message m, int maxPage, String method, Object object) {
		if(maxPage > 1) {
			if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.MESSAGE_ADD_REACTION) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.MESSAGE_ADD_REACTION))) {
				m.addReaction(EmojiManager.getForAlias(":arrow_left:").getUnicode()).queue();
				m.addReaction(EmojiManager.getForAlias(":arrow_right:").getUnicode()).queue();
				Hashes.addTempCache("pagination_gu"+e.getGuild().getId()+"me"+m.getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "1", method).setObject(object));
			}
			else {
				STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_ADD_REACTION.getName())+"<#"+e.getChannel().getId()+">", Channel.LOG.getType());
				logger.error("MESSAGE_ADD_REACTION permission required to display reactions on text channel {} in guild {}", e.getChannel().getId(), e.getGuild().getId());
			}
		}
	}
}
