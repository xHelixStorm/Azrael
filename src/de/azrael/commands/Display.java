package de.azrael.commands;

import java.awt.Color;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.CategoryConf;
import de.azrael.constructors.Channels;
import de.azrael.constructors.Dailies;
import de.azrael.constructors.Roles;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.GuildIni;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
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
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getDisplayCommand(e.getGuild().getIdLong())) {
			var commandLevel = GuildIni.getDisplayLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || e.getMember().getUser().getIdLong() == GuildIni.getAdmin(e.getGuild().getIdLong()))
				return true;
			else if(!GuildIni.getIgnoreMissingPermissions(e.getGuild().getIdLong()))
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		var adminPermission = e.getMember().getUser().getIdLong() == GuildIni.getAdmin(e.getGuild().getIdLong());
		long guild_id = e.getGuild().getIdLong();
		StringBuilder out = new StringBuilder();
		
		//if no arguments have been added to the command, print a list of all available parameters
		if(args.length == 0) {
			out.append(STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP)
					+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayRolesLevel(e.getGuild().getIdLong())) || adminPermission 				? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_1) : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayRegisteredRolesLevel(e.getGuild().getIdLong())) || adminPermission 		? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_2) : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayRankingRolesLevel(e.getGuild().getIdLong())) || adminPermission 		? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_3) : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayCategoriesLevel(e.getGuild().getIdLong())) || adminPermission 			? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_4) : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayRegisteredCategoriesLevel(e.getGuild().getIdLong())) || adminPermission ? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_5) : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayTextChannelsLevel(e.getGuild().getIdLong())) || adminPermission 		? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_6) : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayVoiceChannelsLevel(e.getGuild().getIdLong())) || adminPermission 		? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_7) : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayRegisteredChannelsLevel(e.getGuild().getIdLong())) || adminPermission 	? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_8) : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayDailiesLevel(e.getGuild().getIdLong())) || adminPermission 				? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_9) : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayWatchedUsersLevel(e.getGuild().getIdLong())) || adminPermission 		? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_10) : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayCommandLevelsLevel(e.getGuild().getIdLong())) || adminPermission 		? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_HELP_11) : ""));
			e.getChannel().sendMessage(messageBuild.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(out.toString()).build()).queue();
		}
		//display all roles
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ROLES))) {
			//verify that the current user is allowed to use this parameter
			final var rolesLevel = GuildIni.getDisplayRolesLevel(e.getGuild().getIdLong());
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
			final var registeredRolesLevel = GuildIni.getDisplayRegisteredRolesLevel(e.getGuild().getIdLong());
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
			final var rankingRolesLevel = GuildIni.getDisplayRankingRolesLevel(e.getGuild().getIdLong());
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
			final var categoriesLevel = GuildIni.getDisplayCategoriesLevel(e.getGuild().getIdLong());
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
			final var registeredCategoriesLevel = GuildIni.getDisplayRegisteredCategoriesLevel(e.getGuild().getIdLong());
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
			final var textChannelsLevel = GuildIni.getDisplayTextChannelsLevel(e.getGuild().getIdLong());
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
			final var voiceChannelsLevel = GuildIni.getDisplayVoiceChannelsLevel(e.getGuild().getIdLong());
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
			final var registeredChannelsLevel = GuildIni.getDisplayRegisteredChannelsLevel(e.getGuild().getIdLong());
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
			final var dailiesLevel = GuildIni.getDisplayDailiesLevel(e.getGuild().getIdLong());
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
			final var watchedUsersLevel = GuildIni.getDisplayWatchedUsersLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), watchedUsersLevel) || adminPermission) {
				List<String> watchedUsers = null;
				//verify that the current user is allowed to use the watch channel, if yes, retrieve users that can get displayed in that channel
				if(!UserPrivs.comparePrivilege(e.getMember(), GuildIni.getUserUseWatchChannelLevel(e.getGuild().getIdLong()))) {
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
		//display all available commands with their permission level
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PERMISSIONS))) {
			//verify that the current user is allowed to use this parameter
			var commandsLevel = GuildIni.getDisplayCommandLevelsLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandsLevel) || adminPermission) {
				//collect commands and command levels
				out.append("About command: "+GuildIni.getAboutLevel(e.getGuild().getIdLong())+"\n");
				out.append("Daily command: "+GuildIni.getDailyLevel(e.getGuild().getIdLong())+"\n");
				out.append("Display command: "+GuildIni.getDisplayLevel(e.getGuild().getIdLong())+"\n");
				out.append("Display roles subcommand: "+GuildIni.getDisplayRolesLevel(e.getGuild().getIdLong())+"\n");
				out.append("Display registered roles subcommand: "+GuildIni.getDisplayRegisteredRolesLevel(e.getGuild().getIdLong())+"\n");
				out.append("Display ranking roles subcommand: "+GuildIni.getDisplayRankingRolesLevel(e.getGuild().getIdLong())+"\n");
				out.append("Display categories subcommand: "+GuildIni.getDisplayCategoriesLevel(e.getGuild().getIdLong())+"\n");
				out.append("Display registered categories subcommand: "+GuildIni.getDisplayRegisteredCategoriesLevel(e.getGuild().getIdLong())+"\n");
				out.append("Display text channels subcommand: "+GuildIni.getDisplayTextChannelsLevel(e.getGuild().getIdLong())+"\n");
				out.append("Display voice channels subcommand: "+GuildIni.getDisplayVoiceChannelsLevel(e.getGuild().getIdLong())+"\n");
				out.append("Display registered text channel subcommand: "+GuildIni.getDisplayRegisteredChannelsLevel(e.getGuild().getIdLong())+"\n");
				out.append("Display dailies subcommand: "+GuildIni.getDisplayDailiesLevel(e.getGuild().getIdLong())+"\n");
				out.append("Display watched users subcommand: "+GuildIni.getDisplayWatchedUsersLevel(e.getGuild().getIdLong())+"\n");
				out.append("Display command levels subcommand: "+GuildIni.getDisplayCommandLevelsLevel(e.getGuild().getIdLong())+"\n");
				out.append("Help command: "+GuildIni.getHelpLevel(e.getGuild().getIdLong())+"\n");
				out.append("Inventory command: "+GuildIni.getInventoryLevel(e.getGuild().getIdLong())+"\n");
				out.append("Meow command: "+GuildIni.getMeowLevel(e.getGuild().getIdLong())+"\n");
				out.append("Pug command: "+GuildIni.getPugLevel(e.getGuild().getIdLong())+"\n");
				out.append("Profile command: "+GuildIni.getProfileLevel(e.getGuild().getIdLong())+"\n");
				out.append("Rank command: "+GuildIni.getRankLevel(e.getGuild().getIdLong())+"\n");
				out.append("Register command: "+GuildIni.getRegisterLevel(e.getGuild().getIdLong())+"\n");
				out.append("Register role subcommand: "+GuildIni.getRegisterRoleLevel(e.getGuild().getIdLong())+"\n");
				out.append("Register category subcommand: "+GuildIni.getRegisterCategoryLevel(e.getGuild().getIdLong())+"\n");
				out.append("Register text channel subcommand: "+GuildIni.getRegisterTextChannelLevel(e.getGuild().getIdLong())+"\n");
				out.append("Register text channel url subcommand: "+GuildIni.getRegisterTextChannelURLLevel(e.getGuild().getIdLong())+"\n");
				out.append("Register text channel txt subcommand: "+GuildIni.getRegisterTextChannelTXTLevel(e.getGuild().getIdLong())+"\n");
				out.append("Register text channels subcommand: "+GuildIni.getRegisterTextChannelsLevel(e.getGuild().getIdLong())+"\n");
				out.append("Register ranking role subcommand: "+GuildIni.getRegisterRankingRoleLevel(e.getGuild().getIdLong())+"\n");
				out.append("Register users subcommand: "+GuildIni.getRegisterUsersLevel(e.getGuild().getIdLong())+"\n");
				out.append("Set command: "+GuildIni.getSetLevel(e.getGuild().getIdLong())+"\n");
				out.append("Set privilege level subcommand: "+GuildIni.getSetPrivilegeLevel(e.getGuild().getIdLong())+"\n");
				out.append("Set channel filter subcommand: "+GuildIni.getSetChannelFilterLevel(e.getGuild().getIdLong())+"\n");
				out.append("Set warnings subcommand: "+GuildIni.getSetWarningsLevel(e.getGuild().getIdLong())+"\n");
				out.append("Set ranking subcommand: "+GuildIni.getSetRankingLevel(e.getGuild().getIdLong())+"\n");
				out.append("Set max experience subcommand: "+GuildIni.getSetMaxExperienceLevel(e.getGuild().getIdLong())+"\n");
				out.append("Set main level skin subcommand: "+GuildIni.getSetDefaultLevelSkinLevel(e.getGuild().getIdLong())+"\n");
				out.append("Set main rank skin subcommand: "+GuildIni.getSetDefaultRankSkinLevel(e.getGuild().getIdLong())+"\n");
				out.append("Set main profile skin subcommand: "+GuildIni.getSetDefaultProfileSkinLevel(e.getGuild().getIdLong())+"\n");
				out.append("Set main icon skin subcommand: "+GuildIni.getSetDefaultIconSkinLevel(e.getGuild().getIdLong())+"\n");
				out.append("Set daily item sucommand: "+GuildIni.getSetDailyItemLevel(e.getGuild().getIdLong())+"\n");
				out.append("Set giveaway items subcommand: "+GuildIni.getSetGiveawayItemsLevel(e.getGuild().getIdLong())+"\n");
				out.append("Set privilege level sucommand: "+GuildIni.getSetPrivilegeLevel(e.getGuild().getIdLong())+"\n");
				out.append("Shop command: "+GuildIni.getShopLevel(e.getGuild().getIdLong())+"\n");
				out.append("Top command: "+GuildIni.getTopLevel(e.getGuild().getIdLong())+"\n");
				out.append("Use command: "+GuildIni.getUseLevel(e.getGuild().getIdLong())+"\n");
				out.append("User command: "+GuildIni.getUserLevel(e.getGuild().getIdLong())+"\n");
				out.append("User information subcommand: "+GuildIni.getUserInformationLevel(e.getGuild().getIdLong())+"\n");
				out.append("User delete messages subcommand: "+GuildIni.getUserDeleteMessagesLevel(e.getGuild().getIdLong())+"\n");
				out.append("User warning subcommand: "+GuildIni.getUserWarningLevel(e.getGuild().getIdLong())+"\n");
				out.append("User forced warning subcommand: "+GuildIni.getUserWarningForceLevel(e.getGuild().getIdLong())+"\n");
				out.append("User mute subcommand: "+GuildIni.getUserMuteLevel(e.getGuild().getIdLong())+"\n");
				out.append("User unmute subcommand: "+GuildIni.getUserUnmuteLevel(e.getGuild().getIdLong())+"\n");
				out.append("User ban subcommand: "+GuildIni.getUserBanLevel(e.getGuild().getIdLong())+"\n");
				//print the first half
				e.getChannel().sendMessage("```java\n"+out.toString()+"\n```").queue();
				out.setLength(0);
				//collect the second half
				out.append("User unban subcommand: "+GuildIni.getUserUnbanLevel(e.getGuild().getIdLong())+"\n");
				out.append("User kick subcommand: "+GuildIni.getUserKickLevel(e.getGuild().getIdLong())+"\n");
				out.append("User history subcommand: "+GuildIni.getUserHistoryLevel(e.getGuild().getIdLong())+"\n");
				out.append("User assign-role subcommand: "+GuildIni.getUserAssignRoleLevel(e.getGuild().getIdLong())+"\n");
				out.append("User remove-role subcommand: "+GuildIni.getUserAssignRoleLevel(e.getGuild().getIdLong())+"\n");
				out.append("User watch subcommand: "+GuildIni.getUserWatchLevel(e.getGuild().getIdLong())+"\n");
				out.append("User unwatch subcommand: "+GuildIni.getUserUnwatchLevel(e.getGuild().getIdLong())+"\n");
				out.append("User use watch channel subcommand: "+GuildIni.getUserUseWatchChannelLevel(e.getGuild().getIdLong())+"\n");
				out.append("User gift experience subcommand: "+GuildIni.getUserGiftExperienceLevel(e.getGuild().getIdLong())+"\n");
				out.append("User set experience subcommand: "+GuildIni.getUserSetExperienceLevel(e.getGuild().getIdLong())+"\n");
				out.append("User gift currency subcommand: "+GuildIni.getUserGiftCurrencyLevel(e.getGuild().getIdLong())+"\n");
				out.append("User set currency subcommand: "+GuildIni.getUserSetCurrencyLevel(e.getGuild().getIdLong())+"\n");
				out.append("Filter command: "+GuildIni.getFilterLevel(e.getGuild().getIdLong())+"\n");
				out.append("Filter word filter subcommand: "+GuildIni.getFilterWordFilterLevel(e.getGuild().getIdLong())+"\n");
				out.append("Filter name filter subcommand: "+GuildIni.getFilterNameFilterLevel(e.getGuild().getIdLong())+"\n");
				out.append("Filter name kick subcommand: "+GuildIni.getFilterNameKickLevel(e.getGuild().getIdLong())+"\n");
				out.append("Filter funny names subcommand: "+GuildIni.getFilterFunnyNamesLevel(e.getGuild().getIdLong())+"\n");
				out.append("Filter staff names subcommand: "+GuildIni.getFilterStaffNamesLevel(e.getGuild().getIdLong())+"\n");
				out.append("Filter url blacklist subcommand: "+GuildIni.getFilterURLBlacklistLevel(e.getGuild().getIdLong())+"\n");
				out.append("Filter url whitelist subcommand: "+GuildIni.getFilterURLBlacklistLevel(e.getGuild().getIdLong())+"\n");
				out.append("Filter tweet blacklist subcommand: "+GuildIni.getFilterTweetBlacklistLevel(e.getGuild().getIdLong())+"\n");
				out.append("Quiz command: "+GuildIni.getQuizLevel(e.getGuild().getIdLong())+"\n");
				out.append("Rolereaction command: "+GuildIni.getRoleReactionLevel(e.getGuild().getIdLong())+"\n");
				out.append("Rss command: "+GuildIni.getSubscribeLevel(e.getGuild().getIdLong())+"\n");
				out.append("Randomshop command: "+GuildIni.getRandomshopLevel(e.getGuild().getIdLong())+"\n");
				out.append("Patchnotes command: "+GuildIni.getPatchnotesLevel(e.getGuild().getIdLong())+"\n");
				out.append("Doubleexperience command: "+GuildIni.getDoubleExperienceLevel(e.getGuild().getIdLong())+"\n");
				out.append("Equip command: "+GuildIni.getEquipLevel(e.getGuild().getIdLong())+"\n");
				out.append("Remove command: "+GuildIni.getRemoveLevel(e.getGuild().getIdLong())+"\n");
				out.append("HeavyCensoring command: "+GuildIni.getHeavyCensoringLevel(e.getGuild().getIdLong())+"\n");
				out.append("Mute command: "+GuildIni.getMuteLevel(e.getGuild().getIdLong())+"\n");
				out.append("Google command: "+GuildIni.getGoogleLevel(e.getGuild().getIdLong())+"\n");
				out.append("Write command: "+GuildIni.getWriteLevel(e.getGuild().getIdLong())+"\n");
				out.append("Matchmaking command: "+GuildIni.getMatchmakingLevel(e.getGuild().getIdLong())+"\n");
				out.append("Join command: "+GuildIni.getJoinLevel(e.getGuild().getIdLong())+"\n");
				out.append("Leave command: "+GuildIni.getLeaveLevel(e.getGuild().getIdLong())+"\n");
				out.append("Clan command: "+GuildIni.getClanLevel(e.getGuild().getIdLong())+"\n");
				out.append("Queue command: "+GuildIni.getQueueLevel(e.getGuild().getIdLong())+"\n");
				out.append("Cw command: "+GuildIni.getCwLevel(e.getGuild().getIdLong())+"\n");
				out.append("Room command: "+GuildIni.getRoomLevel(e.getGuild().getIdLong())+"\n");
				out.append("Close rooms room subcommand: "+GuildIni.getRoomCloseLevel(e.getGuild().getIdLong())+"\n");
				out.append("Set winners room subcommand command: "+GuildIni.getRoomWinnerLevel(e.getGuild().getIdLong())+"\n");
				out.append("Reopen rooms room subcommand: "+GuildIni.getRoomReopenLevel(e.getGuild().getIdLong())+"\n");
				out.append("Stats command: "+GuildIni.getStatsLevel(e.getGuild().getIdLong())+"\n");
				out.append("Leaderboard command: "+GuildIni.getLeaderboardLevel(e.getGuild().getIdLong())+"\n");
				out.append("Accept command: "+GuildIni.getAcceptLevel(e.getGuild().getIdLong())+"\n");
				out.append("Deny command: "+GuildIni.getDenyLevel(e.getGuild().getIdLong())+"\n");
				out.append("Language command: "+GuildIni.getLanguageLevel(e.getGuild().getIdLong()));
				out.append("Schedule command: "+GuildIni.getScheduleLevel(e.getGuild().getIdLong()));
				out.append("Prune command: "+GuildIni.getPruneLevel(e.getGuild().getIdLong()));
				out.append("Warn command: "+GuildIni.getWarnLevel(e.getGuild().getIdLong()));
				out.append("Reddit command: "+GuildIni.getRedditLevel(e.getGuild().getIdLong()));
				out.append("Invites command: "+GuildIni.getInvitesLevel(e.getGuild().getIdLong()));
				//print second part
				e.getChannel().sendMessage("```java\n"+out.toString()+"\n```").queue();
				//print third part (custom commands)
				out.setLength(0);
				final var commands = Azrael.SQLgetCustomCommands2(guild_id);
				if(commands != null && commands.size() > 0) {
					for(final var command : commands) {
						out.append(command.getCommand().replaceFirst(command.getCommand().substring(0, 1), command.getCommand().substring(0, 1).toUpperCase())+" command: "+command.getLevel()+"\n");
					}
					e.getChannel().sendMessage("```java\n"+out.toString()+"\n```").queue();
				}
			}
			else{
				UserPrivs.throwNotEnoughPrivilegeError(e, commandsLevel);
			}
		}
		else {
			e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used Display command in guild {}", e.getMember().getUser().getIdLong(), e.getGuild().getId());
	}

}
