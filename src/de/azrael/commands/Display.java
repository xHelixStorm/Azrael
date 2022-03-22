package de.azrael.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.CategoryConf;
import de.azrael.constructors.Channels;
import de.azrael.constructors.Dailies;
import de.azrael.constructors.Roles;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.sql.DiscordRoles;
import de.azrael.sql.RankingSystem;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Category;
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
		
		//if no arguments have been added to the command, print a list of all available parameters
		if(args.length == 0) {
			//default values at 100 to avoid command abuse in case of errors
			int displayRolesLevel = 100;
			int displayRegisteredRolesLevel = 100;
			int displayRankingRolesLevel = 100;
			int displayCategoriesLevel = 100;
			int displayRegisteredCategoriesLevel = 100;
			int displayTextChannelsLevels = 100;
			int displayVoiceChannelsLevels = 100;
			int displayRegisteredChannelsLevel = 100;
			int displayDailiesLevel = 100;
			int displayWatchedUsersLevel = 100;
			
			final var commands = (ArrayList<?>)BotConfiguration.SQLgetCommand(guild_id, 1, Command.DISPLAY_ROLES, Command.DISPLAY_REGISTERED_ROLES, Command.DISPLAY_RANKING_ROLES
					, Command.DISPLAY_CATEGORIES, Command.DISPLAY_REGISTERED_CATEGORIES, Command.DISPLAY_TEXT_CHANNELS, Command.DISPLAY_VOICE_CHANNELS
					, Command.DISPLAY_REGISTERED_CHANNELS, Command.DISPLAY_DAILIES, Command.DISPLAY_WATCHED_USERS);
			
			for(final Object command : commands) {
				int permissionLevel = 0;
				String name = "";
				for(Object values : (ArrayList<?>)command) {
					if(values instanceof Integer)
						permissionLevel = (Integer)values;
					else if(values instanceof String)
						name = ((String)values).split(":")[0];
				}
				
				if(name.equals(Command.DISPLAY_ROLES.getColumn()))
					displayRolesLevel = permissionLevel;
				else if(name.equals(Command.DISPLAY_ROLES.getColumn()))
					displayRolesLevel = permissionLevel;
				else if(name.equals(Command.DISPLAY_REGISTERED_ROLES.getColumn()))
					displayRolesLevel = permissionLevel;
				else if(name.equals(Command.DISPLAY_RANKING_ROLES.getColumn()))
					displayRolesLevel = permissionLevel;
				else if(name.equals(Command.DISPLAY_CATEGORIES.getColumn()))
					displayRolesLevel = permissionLevel;
				else if(name.equals(Command.DISPLAY_REGISTERED_CATEGORIES.getColumn()))
					displayRolesLevel = permissionLevel;
				else if(name.equals(Command.DISPLAY_TEXT_CHANNELS.getColumn()))
					displayRolesLevel = permissionLevel;
				else if(name.equals(Command.DISPLAY_VOICE_CHANNELS.getColumn()))
					displayRolesLevel = permissionLevel;
				else if(name.equals(Command.DISPLAY_REGISTERED_CHANNELS.getColumn()))
					displayRolesLevel = permissionLevel;
				else if(name.equals(Command.DISPLAY_WATCHED_USERS.getColumn()))
					displayRolesLevel = permissionLevel;
			}
			
			out.append(STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP)
					+ (UserPrivs.comparePrivilege(e.getMember(), displayRolesLevel) || adminPermission 					? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_1).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ROLES)) : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), displayRegisteredRolesLevel) || adminPermission 		? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_2).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTERED_ROLES)) : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), displayRankingRolesLevel) || adminPermission 			? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_3).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_RANKING_ROLES)) : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), displayCategoriesLevel) || adminPermission 			? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_4).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_CATEGORIES)) : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), displayRegisteredCategoriesLevel) || adminPermission 	? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_5).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTERED_CATEGORIES)) : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), displayTextChannelsLevels) || adminPermission 			? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_6).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNELS)) : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), displayVoiceChannelsLevels) || adminPermission 		? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_7).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_VOICE_CHANNELS)) : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), displayRegisteredChannelsLevel) || adminPermission 	? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_8).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTERED_CHANNELS)) : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), displayDailiesLevel) || adminPermission 				? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_9).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_DAILIES)) : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), displayWatchedUsersLevel) || adminPermission 			? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_10).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_WATCHED_USERS)) : ""));
			e.getChannel().sendMessage(messageBuild.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(out.toString()).build()).queue();
		}
		//display all roles
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ROLES))) {
			//verify that the current user is allowed to use this parameter
			final var rolesLevel = STATIC.getCommandLevel(e.getGuild(), Command.DISPLAY_ROLES);
			if(UserPrivs.comparePrivilege(e.getMember(), rolesLevel) || adminPermission) {
				//retrieve roles from the server
				for(Role r : e.getGuild().getRoles()) {
					out.append("**"+r.getName() + "** (" + r.getId() + ") \n");
				}
				e.getChannel().sendMessage(messageBuild.setDescription(out.toString()).build()).queue();
			}
			else {
				UserPrivs.throwNotEnoughPrivilegeError(e, rolesLevel);
			}
		}
		//display all registered roles
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTERED_ROLES))) {
			//verify that the current user is allowed to use this parameter
			final var registeredRolesLevel = STATIC.getCommandLevel(e.getGuild(), Command.DISPLAY_REGISTERED_ROLES);
			if(UserPrivs.comparePrivilege(e.getMember(), registeredRolesLevel) || adminPermission) {
				//retrieve roles from table
				for(Roles r : DiscordRoles.SQLgetRoles(guild_id)) {
					if(!r.getCategory_ABV().equals("def"))
						out.append("**"+r.getRole_Name() + "** (" + r.getRole_ID() + ") \n"
							+ STATIC.getTranslation(e.getMember(), Translation.DISPLAY_ROLE_TYPE)+r.getCategory_Name()+"\n"
							+ STATIC.getTranslation(e.getMember(), Translation.DISPLAY_PERMISSION_LEVEL)+r.getLevel()+"\n"
							+ STATIC.getTranslation(e.getMember(), Translation.DISPLAY_PERSISTANT)+(r.isPersistent() ? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_PERSISTANT) : STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_NOT_PERSISTANT))+"\n\n");
				}
				e.getChannel().sendMessage((out.length() > 0 ? messageBuild.setDescription(out.toString()).build() : error.setDescription(STATIC.getTranslation(e.getMember(), Translation.DISPLAY_INPUT_NOT_FOUND)).build())).queue();
			}
			else {
				UserPrivs.throwNotEnoughPrivilegeError(e, registeredRolesLevel);
			}
		}
		//display all registered ranking roles
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_RANKING_ROLES))) {
			//verify that the current user is allowed to use this parameter
			final var rankingRolesLevel = STATIC.getCommandLevel(e.getGuild(), Command.DISPLAY_RANKING_ROLES);
			if(UserPrivs.comparePrivilege(e.getMember(), rankingRolesLevel) || adminPermission) {
				//confirm that the ranking system is enabled
				if(RankingSystem.SQLgetGuild(guild_id).getRankingState()) {
					//retrieve all ranking roles from table
					for(final var r : RankingSystem.SQLgetRoles(guild_id)) {
						out.append("**"+r.getRole_Name() + "** (" + r.getRole_ID() + ") \n"+STATIC.getTranslation(e.getMember(), Translation.DISPLAY_UNLOCK_LEVEL) + r.getLevel() + "\n");
					}
					e.getChannel().sendMessage((out.length() > 0 ? messageBuild.setDescription(out.toString()).build() : error.setDescription(STATIC.getTranslation(e.getMember(), Translation.DISPLAY_INPUT_NOT_FOUND)).build())).queue();
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
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CATEGORIES))) {
			//verify that the current user is allowed to use this parameter
			final var categoriesLevel = STATIC.getCommandLevel(e.getGuild(), Command.DISPLAY_CATEGORIES);
			if(UserPrivs.comparePrivilege(e.getMember(), categoriesLevel) || adminPermission) {
				//retrieve all text channels from the server
				for(Category ct : e.getGuild().getCategories()) {
					out.append("**"+ct.getName() + "** (" + ct.getId() + ") \n");
				}
				e.getChannel().sendMessage((out.length() > 0 ? messageBuild.setDescription(out.toString()).build() : error.setDescription(STATIC.getTranslation(e.getMember(), Translation.DISPLAY_INPUT_NOT_FOUND)).build())).queue();
			}
			else {
				UserPrivs.throwNotEnoughPrivilegeError(e, categoriesLevel);
			}
		}
		//display all registered categories
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTERED_CATEGORIES))) {
			//verify that the current user is allowed to use this parameter
			final var registeredCategoriesLevel = STATIC.getCommandLevel(e.getGuild(), Command.DISPLAY_REGISTERED_CATEGORIES);
			if(UserPrivs.comparePrivilege(e.getMember(), registeredCategoriesLevel) || adminPermission) {
				//retrieve all registered text channels from table
				for(final CategoryConf ct : Azrael.SQLgetCategories(guild_id)) {
					Category category = e.getGuild().getCategoryById(ct.getCategoryID());
					if(category != null) {
						out.append("**"+category.getName()+"** ("+category.getId()+")\n"+STATIC.getTranslation(e.getMember(), Translation.DISPLAY_CATEGORY_TYPE)+ct.getType()+"\n\n");
					}
				}
				e.getChannel().sendMessage((out.length() > 0 ? messageBuild.setDescription(out.toString()).build() : error.setDescription(STATIC.getTranslation(e.getMember(), Translation.DISPLAY_INPUT_NOT_FOUND)).build())).queue();
			}
			else{
				UserPrivs.throwNotEnoughPrivilegeError(e, registeredCategoriesLevel);
			}
		}
		//display all text channels
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNELS))) {
			//verify that the current user is allowed to use this parameter
			final var textChannelsLevel = STATIC.getCommandLevel(e.getGuild(), Command.DISPLAY_TEXT_CHANNELS);
			if(UserPrivs.comparePrivilege(e.getMember(), textChannelsLevel) || adminPermission) {
				//retrieve all text channels from the server
				for(TextChannel tc : e.getGuild().getTextChannels()) {
					out.append("**"+tc.getName() + "** (" + tc.getId() + ") \n");
				}
				e.getChannel().sendMessage((out.length() > 0 ? messageBuild.setDescription(out.toString()).build() : error.setDescription(STATIC.getTranslation(e.getMember(), Translation.DISPLAY_INPUT_NOT_FOUND)).build())).queue();
			}
			else {
				UserPrivs.throwNotEnoughPrivilegeError(e, textChannelsLevel);
			}
		}
		//display all voice channels
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_VOICE_CHANNELS))) {
			//verify that the current user is allowed to use this parameter
			final var voiceChannelsLevel = STATIC.getCommandLevel(e.getGuild(), Command.DISPLAY_VOICE_CHANNELS);
			if(UserPrivs.comparePrivilege(e.getMember(), voiceChannelsLevel) || adminPermission) {
				//retrieve all voice channels from the server
				for(VoiceChannel vc : e.getGuild().getVoiceChannels()) {
					out.append("**"+vc.getName() + "** (" + vc.getId() + ") \n");
				}
				e.getChannel().sendMessage((out.length() > 0 ? messageBuild.setDescription(out.toString()).build() : error.setDescription(STATIC.getTranslation(e.getMember(), Translation.DISPLAY_INPUT_NOT_FOUND)).build())).queue();
			}
			else {
				UserPrivs.throwNotEnoughPrivilegeError(e, voiceChannelsLevel);
			}
		}
		//display all registered text channels
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTERED_CHANNELS))) {
			//verify that the current user is allowed to use this parameter
			final var registeredChannelsLevel = STATIC.getCommandLevel(e.getGuild(), Command.DISPLAY_REGISTERED_CHANNELS);
			if(UserPrivs.comparePrivilege(e.getMember(), registeredChannelsLevel) || adminPermission) {
				long prevChannelID = 0;
				//retrieve all registered text channels from table
				for(Channels ch : Azrael.SQLgetChannels(guild_id)) {
					if(prevChannelID != ch.getChannel_ID()) {
						prevChannelID = ch.getChannel_ID();
						if(out.length() > 0)
							out.append("\n\n");
						out.append("**"+ch.getChannel_Name() + "** (" + ch.getChannel_ID() + ") \n"
							+ STATIC.getTranslation(e.getMember(), Translation.DISPLAY_CHANNEL_TYPE)+(ch.getChannel_Type_Name() != null ? ch.getChannel_Type_Name() : STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE))+"\n"
							+ STATIC.getTranslation(e.getMember(), Translation.DISPLAY_URL_CENSORING)+(ch.getURLCensoring() ? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_ENABLED) : STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_NOT_ENABLED))+"\n"
							+ STATIC.getTranslation(e.getMember(), Translation.DISPLAY_TEXT_CENSORING)+(ch.getTxtRemoval() ? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_ENABLED) : STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_NOT_ENABLED))+"\n"
							+ STATIC.getTranslation(e.getMember(), Translation.DISPLAY_LANG_CENSORING)+(ch.getLang_Filter() != null ? ch.getLang_Filter() : STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE)));
					}
					else {
						out.append(", "+ch.getLang_Filter());
					}
				}
				e.getChannel().sendMessage((out.length() > 0 ? messageBuild.setDescription(out.toString()).build() : error.setDescription(STATIC.getTranslation(e.getMember(), Translation.DISPLAY_INPUT_NOT_FOUND)).build())).queue();
			}
			else{
				UserPrivs.throwNotEnoughPrivilegeError(e, registeredChannelsLevel);
			}
		}
		//display all registered daily rewards
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DAILIES))) {
			//verify that the current user is allowed to use this parameter
			final var dailiesLevel = STATIC.getCommandLevel(e.getGuild(), Command.DISPLAY_DAILIES);
			if(UserPrivs.comparePrivilege(e.getMember(), dailiesLevel) || adminPermission) {
				//retrieve all daily rewards from table
				for(Dailies daily : RankingSystem.SQLgetDailiesAndType(guild_id)) {
					out.append("**"+daily.getDescription()+"**\n"+STATIC.getTranslation(e.getMember(), Translation.DISPLAY_PROBABILITY)+daily.getWeight()+"%\n\n");
				}
				e.getChannel().sendMessage((out.length() > 0) ? messageBuild.setDescription(out.toString()).build() : error.setDescription(STATIC.getTranslation(e.getMember(), Translation.DISPLAY_INPUT_NOT_FOUND)).build()).queue();
			}
			else{
				UserPrivs.throwNotEnoughPrivilegeError(e, dailiesLevel);
			}
		}
		//display all users that are being watched
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_WATCHED_USERS))) {
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
				//list the watched users
				for(final var watchedUser : watchedUsers) {
					out.append("**"+watchedUser+"**\n");
				}
				e.getChannel().sendMessage((out.length() > 0 ? messageBuild.setDescription(out.toString()).build() : error.setDescription(STATIC.getTranslation(e.getMember(), Translation.DISPLAY_INPUT_NOT_FOUND)).build())).queue();
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

}
