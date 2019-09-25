package commands;

import java.awt.Color;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Channels;
import constructors.Dailies;
import constructors.Roles;
import core.UserPrivs;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;
import sql.DiscordRoles;
import sql.Azrael;

public class Display implements CommandPublic{
	private final static Logger logger = LoggerFactory.getLogger(Display.class);
	private static EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.MAGENTA);
	private static EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getDisplayCommand(e.getGuild().getIdLong())) {
			var commandLevel = GuildIni.getDisplayLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || e.getMember().getUser().getIdLong() == GuildIni.getAdmin(e.getGuild().getIdLong()))
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		var adminPermission = e.getMember().getUser().getIdLong() == GuildIni.getAdmin(e.getGuild().getIdLong());
		long guild_id = e.getGuild().getIdLong();
		StringBuilder out = new StringBuilder();
		
		final String prefix = GuildIni.getCommandPrefix(guild_id);
		if(args.length == 0) {
			out.append("Use these parameters after the display command like **"+prefix+"display -roles** for further information on what to display:\n\n"
					+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayRolesLevel(e.getGuild().getIdLong())) || adminPermission 				? "**-roles**: Display all roles from this guild.\n" : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayRegisteredRolesLevel(e.getGuild().getIdLong())) || adminPermission 		? "**-registered-roles**: Display all registered roles with their privileges.\n" : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayRankingRolesLevel(e.getGuild().getIdLong())) || adminPermission 		? "**-ranking-roles**: Display all roles that can be unlocked with which level.\n" : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayTextChannelsLevel(e.getGuild().getIdLong())) || adminPermission 		? "**-textchannels**: Display all textchannels from this guild.\n" : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayVoiceChannelsLevel(e.getGuild().getIdLong())) || adminPermission 		? "**-voicechannels**: Display all voicechannels from this guild.\n" : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayRegisteredChannelsLevel(e.getGuild().getIdLong())) || adminPermission 	? "**-registered-channels**: Display all registered textchannels with configured filter options.\n" : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayDailiesLevel(e.getGuild().getIdLong())) || adminPermission 				? "**-dailies**: Display all items that the "+prefix+"daily command contains.\n" : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayWatchedUsersLevel(e.getGuild().getIdLong())) || adminPermission 		? "**-watched-users**: Display all users that are currently being watched with their watch level.\n" : "")
					+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayCommandLevelsLevel(e.getGuild().getIdLong())) || adminPermission 		? "**-command-levels**: Display the privilege level of each command and subcommand." : ""));
			e.getChannel().sendMessage(messageBuild.setDescription(out.toString()).build()).queue();
		}
		else if(args[0].equalsIgnoreCase("-roles")) {
			final var rolesLevel = GuildIni.getDisplayRolesLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), rolesLevel) || adminPermission) {
				for(Role r : e.getGuild().getRoles()) {
					out.append(r.getName() + " (" + r.getId() + ") \n");
				}
				e.getChannel().sendMessage(messageBuild.setDescription(out.toString()).build()).queue();
			}
			else {
				UserPrivs.throwNotEnoughPrivilegeError(e, rolesLevel);
			}
		}
		else if(args[0].equalsIgnoreCase("-registered-roles")) {
			final var registeredRolesLevel = GuildIni.getDisplayRegisteredRolesLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), registeredRolesLevel) || adminPermission) {
				for(Roles r : DiscordRoles.SQLgetRoles(guild_id)) {
					out.append(r.getRole_Name() + " (" + r.getRole_ID() + ") \nrole type: "+r.getCategory_Name()+"\nPrivilege level: "+r.getLevel()+"\n\n");
				}
				e.getChannel().sendMessage(messageBuild.setDescription(out.toString()).build()).queue();
			}
			else {
				UserPrivs.throwNotEnoughPrivilegeError(e, registeredRolesLevel);
			}
		}
		else if(args[0].equalsIgnoreCase("-ranking-roles")) {
			final var rankingRolesLevel = GuildIni.getDisplayRankingRolesLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), rankingRolesLevel) || adminPermission) {
				if(RankingSystem.SQLgetGuild(guild_id).getRankingState()) {
					for(constructors.Rank r : RankingSystem.SQLgetRoles(guild_id)) {
						if(r.getGuildID() == guild_id) {
							out.append(r.getRole_Name() + " (" + r.getRoleID() + ") \nlevel to unlock: " + r.getLevel_Requirement() + "\n");
						}
					}
				}
				else {
					out.append("The ranking system isn't enabled and hence no role can be displayed!");
				}
				e.getChannel().sendMessage(messageBuild.setDescription((out.length() > 0) ? out.toString() : "No ranking role has been registered!").build()).queue();
			}
			else {
				UserPrivs.throwNotEnoughPrivilegeError(e, rankingRolesLevel);
			}
		}
		else if(args[0].equalsIgnoreCase("-textchannels")) {
			final var textChannelsLevel = GuildIni.getDisplayTextChannelsLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), textChannelsLevel) || adminPermission) {
				for(TextChannel tc : e.getGuild().getTextChannels()) {
					out.append(tc.getName() + " (" + tc.getId() + ") \n");
				}
				e.getChannel().sendMessage(messageBuild.setDescription((out.length() > 0) ? out.toString() : "Textchannels don't exist in this server!").build()).queue();
			}
			else {
				UserPrivs.throwNotEnoughPrivilegeError(e, textChannelsLevel);
			}
		}
		else if(args[0].equalsIgnoreCase("-voicechannels")) {
			final var voiceChannelsLevel = GuildIni.getDisplayVoiceChannelsLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), voiceChannelsLevel) || adminPermission) {
				for(VoiceChannel vc : e.getGuild().getVoiceChannels()) {
					out.append(vc.getName() + " (" + vc.getId() + ") \n");
				}
				e.getChannel().sendMessage(messageBuild.setDescription((out.length() > 0) ? out.toString() : "Voicechannels don't exist in this server!").build()).queue();
			}
			else {
				UserPrivs.throwNotEnoughPrivilegeError(e, voiceChannelsLevel);
			}
		}
		else if(args[0].equalsIgnoreCase("-registered-channels")) {
			final var registeredChannelsLevel = GuildIni.getDisplayRegisteredChannelsLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), registeredChannelsLevel) || adminPermission) {
				for(Channels ch : Azrael.SQLgetChannels(guild_id)) {
					if(!out.toString().contains(""+ch.getChannel_ID())) {
						out.append("\n\n"+ch.getChannel_Name() + " (" + ch.getChannel_ID() + ") \nChannel type: "+(ch.getChannel_Type_Name() != null ? ch.getChannel_Type_Name() : "none")+" Channel\nFilter(s) in use: "+ch.getLang_Filter()+"\nURL censoring: "+(ch.getURLCensoring() ? "enabled" : "disabled")) ;
					}
					else if(out.toString().contains(""+ch.getChannel_ID())) {
						out.append(", "+ch.getLang_Filter());
					}
				}
				e.getChannel().sendMessage(messageBuild.setDescription((out.length() > 0) ? out.toString() : "No channel has been registered!").build()).queue();
			}
			else{
				UserPrivs.throwNotEnoughPrivilegeError(e, registeredChannelsLevel);
			}
		}
		else if(args[0].equalsIgnoreCase("-dailies")) {
			final var dailiesLevel = GuildIni.getDisplayDailiesLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), dailiesLevel) || adminPermission) {
				for(Dailies daily : RankingSystem.SQLgetDailiesAndType(guild_id, RankingSystem.SQLgetGuild(guild_id).getThemeID())) {
					out.append(daily.getDescription()+"\nWeight: "+daily.getWeight()+"\n\n");
				}
				e.getChannel().sendMessage(messageBuild.setDescription((out.length() > 0) ? "You can receive the following items through dailies:\n\n"+out.toString() : "No daily item has been registered!").build()).queue();
			}
			else{
				UserPrivs.throwNotEnoughPrivilegeError(e, dailiesLevel);
			}
		}
		else if(args[0].equalsIgnoreCase("-watched-users")) {
			final var watchedUsersLevel = GuildIni.getDisplayWatchedUsersLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), watchedUsersLevel) || adminPermission) {
				List<String> watchedUsers = null;
				if(!UserPrivs.comparePrivilege(e.getMember(), GuildIni.getUserUseWatchChannelLevel(e.getGuild().getIdLong()))) {
					watchedUsers = Azrael.SQLgetWholeWatchlist(e.getGuild().getIdLong(), false);
				}
				else {
					watchedUsers = Azrael.SQLgetWholeWatchlist(e.getGuild().getIdLong(), true);
				}
				for(final var watchedUser : watchedUsers) {
					out.append(watchedUser+"\n");
				}
				e.getChannel().sendMessage(messageBuild.setDescription((out.length() > 0 ? "Here all users that are being watched:\n\n"+out.toString() : "Currently no user is being watched!")).build()).queue();
			}
			else {
				UserPrivs.throwNotEnoughPrivilegeError(e, watchedUsersLevel);
			}
		}
		else if(args[0].equalsIgnoreCase("-command-levels")) {
			var commandsLevel = GuildIni.getDisplayCommandLevelsLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandsLevel) || adminPermission) {
				out.append("About command: "+GuildIni.getAboutLevel(e.getGuild().getIdLong())+"\n");
				out.append("Commands command: "+GuildIni.getCommandsLevel(e.getGuild().getIdLong())+"\n");
				out.append("Hidden commands: "+GuildIni.getCommandsAdminLevel(e.getGuild().getIdLong())+"\n");
				out.append("Daily command: "+GuildIni.getDailyLevel(e.getGuild().getIdLong())+"\n");
				out.append("Display command: "+GuildIni.getDisplayLevel(e.getGuild().getIdLong())+"\n");
				out.append("Display roles subcommand: "+GuildIni.getDisplayRolesLevel(e.getGuild().getIdLong())+"\n");
				out.append("Display registered roles subcommand: "+GuildIni.getDisplayRegisteredRolesLevel(e.getGuild().getIdLong())+"\n");
				out.append("Display ranking roles subcommand: "+GuildIni.getDisplayRankingRolesLevel(e.getGuild().getIdLong())+"\n");
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
				out.append("Set commands subcommand: "+GuildIni.getSetCommandsLevel(e.getGuild().getIdLong())+"\n");
				out.append("Set ranking subcommand: "+GuildIni.getSetRankingLevel(e.getGuild().getIdLong())+"\n");
				out.append("Set max experience subcommand: "+GuildIni.getSetMaxExperienceLevel(e.getGuild().getIdLong())+"\n");
				out.append("Set default level skin subcommand: "+GuildIni.getSetDefaultLevelSkinLevel(e.getGuild().getIdLong())+"\n");
				out.append("Set default rank skin subcommand: "+GuildIni.getSetDefaultRankSkinLevel(e.getGuild().getIdLong())+"\n");
				out.append("Set default profile skin subcommand: "+GuildIni.getSetDefaultProfileSkinLevel(e.getGuild().getIdLong())+"\n");
				out.append("Set default icon skin subcommand: "+GuildIni.getSetDefaultIconSkinLevel(e.getGuild().getIdLong())+"\n");
				out.append("Set daily item sucommand: "+GuildIni.getSetDailyItemLevel(e.getGuild().getIdLong())+"\n");
				out.append("Set giveaway items subcommand: "+GuildIni.getSetGiveawayItemsLevel(e.getGuild().getIdLong())+"\n");
				
				e.getChannel().sendMessage("`"+out.toString()+"`").queue();
				out.setLength(0);
				
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
				out.append("User kick subcommand: "+GuildIni.getUserKickLevel(e.getGuild().getIdLong())+"\n");
				out.append("User history subcommand: "+GuildIni.getUserHistoryLevel(e.getGuild().getIdLong())+"\n");
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
				out.append("Rss command: "+GuildIni.getRssLevel(e.getGuild().getIdLong())+"\n");
				out.append("Randomshop command: "+GuildIni.getRandomshopLevel(e.getGuild().getIdLong())+"\n");
				out.append("Patchnotes command: "+GuildIni.getPatchnotesLevel(e.getGuild().getIdLong())+"\n");
				out.append("Doubleexperience command: "+GuildIni.getDoubleExperienceLevel(e.getGuild().getIdLong())+"\n");
				out.append("Equip command: "+GuildIni.getEquipLevel(e.getGuild().getIdLong())+"\n");
				out.append("Remove command: "+GuildIni.getRemoveLevel(e.getGuild().getIdLong())+"\n");
				
				e.getChannel().sendMessage("`"+out.toString()+"`").queue();
			}
			else{
				UserPrivs.throwNotEnoughPrivilegeError(e, commandsLevel);
			}
		}
		else {
			e.getChannel().sendMessage(denied.setDescription("**"+e.getMember().getAsMention()+" Something went wrong. Please recheck the syntax and try again!**").build()).queue();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used Display command in guild {}", e.getMember().getUser().getIdLong(), e.getGuild().getId());
	}

}
