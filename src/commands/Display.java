package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Channels;
import constructors.Dailies;
import constructors.Roles;
import core.UserPrivs;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sql.RankingSystem;
import sql.DiscordRoles;
import sql.Azrael;

public class Display implements Command{
	private static EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.MAGENTA);
	private static EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(GuildIni.getDisplayCommand(e.getGuild().getIdLong())) {
			var adminPermission = e.getMember().getUser().getIdLong() == GuildIni.getAdmin(e.getGuild().getIdLong());
			var commandLevel = GuildIni.getDisplayLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
				long guild_id = e.getGuild().getIdLong();
				String out = "";
				
				final String prefix = GuildIni.getCommandPrefix(guild_id);
				if(args.length == 0) {
					out = "Use these parameters after the display command like **"+prefix+"display -roles** for further information on what to display:\n\n"
							+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayRolesLevel(e.getGuild().getIdLong())) || adminPermission 				? "**-roles**: Display all roles from this guild.\n" : "")
							+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayRegisteredRolesLevel(e.getGuild().getIdLong())) || adminPermission 		? "**-registered-roles**: Display all registered roles with their privileges.\n" : "")
							+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayRankingRolesLevel(e.getGuild().getIdLong())) || adminPermission 		? "**-ranking-roles**: Display all roles that can be unlocked with which level.\n" : "")
							+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayTextChannelsLevel(e.getGuild().getIdLong())) || adminPermission 		? "**-textchannels**: Display all textchannels from this guild.\n" : "")
							+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayVoiceChannelsLevel(e.getGuild().getIdLong())) || adminPermission 		? "**-voicechannels**: Display all voicechannels from this guild.\n" : "")
							+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayRegisteredChannelsLevel(e.getGuild().getIdLong())) || adminPermission 	? "**-registered-channels**: Display all registered textchannels with configured filter options.\n" : "")
							+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayDailiesLevel(e.getGuild().getIdLong())) || adminPermission 				? "**-dailies**: Display all items that the "+prefix+"daily command contains.\n" : "")
							+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayCommandLevelsLevel(e.getGuild().getIdLong())) || adminPermission 		? "**-command-levels**: Display the privilege level of each command and subcommand." : "");
					e.getTextChannel().sendMessage(messageBuild.setDescription(out).build()).queue();
				}
				else if(args[0].equalsIgnoreCase("-roles")) {
					final var rolesLevel = GuildIni.getDisplayRolesLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), rolesLevel) || adminPermission) {
						for(Role r : e.getGuild().getRoles()) {
							out += r.getName() + " (" + r.getId() + ") \n";
						}
						e.getTextChannel().sendMessage(messageBuild.setDescription(out).build()).queue();
					}
					else {
						e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(rolesLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args[0].equalsIgnoreCase("-registered-roles")) {
					final var registeredRolesLevel = GuildIni.getDisplayRegisteredRolesLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), registeredRolesLevel) || adminPermission) {
						for(Roles r : DiscordRoles.SQLgetRoles(guild_id)) {
							out += r.getRole_Name() + " (" + r.getRole_ID() + ") \nrole type: "+r.getCategory_Name()+"\nPrivilege level: "+r.getLevel()+"\n\n";
						}
						e.getTextChannel().sendMessage(messageBuild.setDescription(out).build()).queue();
					}
					else {
						e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(registeredRolesLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args[0].equalsIgnoreCase("-ranking-roles")) {
					final var rankingRolesLevel = GuildIni.getDisplayRankingRolesLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), rankingRolesLevel) || adminPermission) {
						if(RankingSystem.SQLgetGuild(guild_id).getRankingState()) {
							for(constructors.Rank r : RankingSystem.SQLgetRoles(guild_id)) {
								if(r.getGuildID() == guild_id) {
									out += r.getRole_Name() + " (" + r.getRoleID() + ") \nlevel to unlock: " + r.getLevel_Requirement() + "\n";
								}
							}
						}
						else {
							out = "The ranking system isn't enabled and hence no role can be displayed!";
						}
						e.getTextChannel().sendMessage(messageBuild.setDescription((out.length() > 0) ? out : "No ranking role has been registered!").build()).queue();
					}
					else {
						e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(rankingRolesLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args[0].equalsIgnoreCase("-textchannels")) {
					final var textChannelsLevel = GuildIni.getDisplayTextChannelsLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), textChannelsLevel) || adminPermission) {
						for(TextChannel tc : e.getGuild().getTextChannels()) {
							out += tc.getName() + " (" + tc.getId() + ") \n";
						}
						e.getTextChannel().sendMessage(messageBuild.setDescription((out.length() > 0) ? out : "Textchannels don't exist in this server!").build()).queue();
					}
					else {
						e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(textChannelsLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args[0].equalsIgnoreCase("-voicechannels")) {
					final var voiceChannelsLevel = GuildIni.getDisplayVoiceChannelsLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), voiceChannelsLevel) || adminPermission) {
						for(VoiceChannel vc : e.getGuild().getVoiceChannels()) {
							out += vc.getName() + " (" + vc.getId() + ") \n";
						}
						e.getTextChannel().sendMessage(messageBuild.setDescription((out.length() > 0) ? out : "Voicechannels don't exist in this server!").build()).queue();
					}
					else {
						e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(voiceChannelsLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args[0].equalsIgnoreCase("-registered-channels")) {
					final var registeredChannelsLevel = GuildIni.getDisplayRegisteredChannelsLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), registeredChannelsLevel) || adminPermission) {
						for(Channels ch : Azrael.SQLgetChannels(guild_id)) {
							if(!out.contains(""+ch.getChannel_ID())) {
								out += "\n\n"+ch.getChannel_Name() + " (" + ch.getChannel_ID() + ") \nChannel type: "+ch.getChannel_Type_Name()+" Channel\nFilter(s) in use: "+ch.getLang_Filter();
							}
							else if(out.contains(""+ch.getChannel_ID())) {
								out += ", "+ch.getLang_Filter();
							}
						}
						e.getTextChannel().sendMessage(messageBuild.setDescription((out.length() > 0) ? out : "No channel has been registered!").build()).queue();
					}
					else{
						e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(registeredChannelsLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args[0].equalsIgnoreCase("-dailies")) {
					final var dailiesLevel = GuildIni.getDisplayDailiesLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), dailiesLevel) || adminPermission) {
						for(Dailies daily : RankingSystem.SQLgetDailiesAndType(guild_id, RankingSystem.SQLgetGuild(guild_id).getThemeID())) {
							out+= daily.getDescription()+"\nWeight: "+daily.getWeight()+"\n\n";
						}
						e.getTextChannel().sendMessage(messageBuild.setDescription((out.length() > 0) ? "You can receive the following items through dailies:\n\n"+out : "No daily item has been registered!").build()).queue();
					}
					else{
						e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:").build()).queue();
					}
				}
				else if(args[0].equalsIgnoreCase("-command-levels")) {
					var commandsLevel = GuildIni.getDisplayCommandLevelsLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), commandsLevel) || adminPermission) {
						out += "About command: "+GuildIni.getAboutLevel(e.getGuild().getIdLong())+"\n";
						out += "Commands command: "+GuildIni.getCommandsLevel(e.getGuild().getIdLong())+"\n";
						out += "Hidden commands: "+GuildIni.getCommandsAdminLevel(e.getGuild().getIdLong())+"\n";
						out += "Daily command: "+GuildIni.getDailyLevel(e.getGuild().getIdLong())+"\n";
						out += "Display command: "+GuildIni.getDisplayLevel(e.getGuild().getIdLong())+"\n";
						out += "Display roles subcommand: "+GuildIni.getDisplayRolesLevel(e.getGuild().getIdLong())+"\n";
						out += "Display registered roles subcommand: "+GuildIni.getDisplayRegisteredRolesLevel(e.getGuild().getIdLong())+"\n";
						out += "Display ranking roles subcommand: "+GuildIni.getDisplayRankingRolesLevel(e.getGuild().getIdLong())+"\n";
						out += "Display text channels subcommand: "+GuildIni.getDisplayTextChannelsLevel(e.getGuild().getIdLong())+"\n";
						out += "Display voice channels subcommand: "+GuildIni.getDisplayVoiceChannelsLevel(e.getGuild().getIdLong())+"\n";
						out += "Display registered text channel subcommand: "+GuildIni.getDisplayRegisteredChannelsLevel(e.getGuild().getIdLong())+"\n";
						out += "Display dailies subcommand: "+GuildIni.getDisplayDailiesLevel(e.getGuild().getIdLong())+"\n";
						out += "Display command levels subcommand: "+GuildIni.getDisplayCommandLevelsLevel(e.getGuild().getIdLong())+"\n";
						out += "Help command: "+GuildIni.getHelpLevel(e.getGuild().getIdLong())+"\n";
						out += "Inventory command: "+GuildIni.getInventoryLevel(e.getGuild().getIdLong())+"\n";
						out += "Meow command: "+GuildIni.getMeowLevel(e.getGuild().getIdLong())+"\n";
						out += "Pug command: "+GuildIni.getPugLevel(e.getGuild().getIdLong())+"\n";
						out += "Profile command: "+GuildIni.getProfileLevel(e.getGuild().getIdLong())+"\n";
						out += "Rank command: "+GuildIni.getRankLevel(e.getGuild().getIdLong())+"\n";
						out += "Register command: "+GuildIni.getRegisterLevel(e.getGuild().getIdLong())+"\n";
						out += "Register role subcommand: "+GuildIni.getRegisterRoleLevel(e.getGuild().getIdLong())+"\n";
						out += "Register text channel subcommand: "+GuildIni.getRegisterTextChannelLevel(e.getGuild().getIdLong())+"\n";
						out += "Register text channels subcommand: "+GuildIni.getRegisterTextChannelsLevel(e.getGuild().getIdLong())+"\n";
						out += "Register ranking role subcommand: "+GuildIni.getRegisterRankingRoleLevel(e.getGuild().getIdLong())+"\n";
						out += "Register users subcommand: "+GuildIni.getRegisterUsersLevel(e.getGuild().getIdLong())+"\n";
						out += "Set command: "+GuildIni.getSetLevel(e.getGuild().getIdLong())+"\n";
						out += "Set privilege level subcommand: "+GuildIni.getSetPrivilegeLevel(e.getGuild().getIdLong())+"\n";
						out += "Set channel filter subcommand: "+GuildIni.getSetChannelFilterLevel(e.getGuild().getIdLong())+"\n";
						out += "Set warnings subcommand: "+GuildIni.getSetWarningsLevel(e.getGuild().getIdLong())+"\n";
						out += "Set commands subcommand: "+GuildIni.getSetCommandsLevel(e.getGuild().getIdLong())+"\n";
						out += "Set ranking sucommand: "+GuildIni.getSetRankingLevel(e.getGuild().getIdLong())+"\n";
						out += "Set max experience subcommand: "+GuildIni.getSetMaxExperienceLevel(e.getGuild().getIdLong())+"\n";
						out += "Set default level skin subcommand: "+GuildIni.getSetDefaultLevelSkinLevel(e.getGuild().getIdLong())+"\n";
						out += "Set default rank skin subcommand: "+GuildIni.getSetDefaultRankSkinLevel(e.getGuild().getIdLong())+"\n";
						out += "Set default profile skin subcommand: "+GuildIni.getSetDefaultProfileSkinLevel(e.getGuild().getIdLong())+"\n";
						out += "Set default icon skin subcommand: "+GuildIni.getSetDefaultIconSkinLevel(e.getGuild().getIdLong())+"\n";
						out += "Set daily item sucommand: "+GuildIni.getSetDailyItemLevel(e.getGuild().getIdLong())+"\n";
						out += "Set giveaway items sucommand: "+GuildIni.getSetGiveawayItemsLevel(e.getGuild().getIdLong())+"\n";
						out += "Set privilege level sucommand: "+GuildIni.getSetPrivilegeLevel(e.getGuild().getIdLong())+"\n";
						out += "Shop command: "+GuildIni.getShopLevel(e.getGuild().getIdLong())+"\n";
						out += "Top command: "+GuildIni.getTopLevel(e.getGuild().getIdLong())+"\n";
						out += "Use command: "+GuildIni.getUseLevel(e.getGuild().getIdLong())+"\n";
						out += "User command: "+GuildIni.getUserLevel(e.getGuild().getIdLong())+"\n";
						out += "User information subcommand: "+GuildIni.getUserInformationLevel(e.getGuild().getIdLong())+"\n";
						out += "User delete messages subcommand: "+GuildIni.getUserDeleteMessagesLevel(e.getGuild().getIdLong())+"\n";
						out += "User warning subcommand: "+GuildIni.getUserWarningLevel(e.getGuild().getIdLong())+"\n";
						out += "User forced warning subcommand: "+GuildIni.getUserWarningForceLevel(e.getGuild().getIdLong())+"\n";
						out += "User mute subcommand: "+GuildIni.getUserMuteLevel(e.getGuild().getIdLong())+"\n";
						out += "User unmute subcommand: "+GuildIni.getUserUnmuteLevel(e.getGuild().getIdLong())+"\n";
						out += "User ban subcommand: "+GuildIni.getUserBanLevel(e.getGuild().getIdLong())+"\n";
						out += "User kick subcommand: "+GuildIni.getUserKickLevel(e.getGuild().getIdLong())+"\n";
						out += "User history subcommand: "+GuildIni.getUserHistoryLevel(e.getGuild().getIdLong())+"\n";
						out += "User gift experience subcommand: "+GuildIni.getUserGiftExperienceLevel(e.getGuild().getIdLong())+"\n";
						out += "User set experience subcommand: "+GuildIni.getUserSetExperienceLevel(e.getGuild().getIdLong())+"\n";
						out += "User gift currency subcommand: "+GuildIni.getUserGiftCurrencyLevel(e.getGuild().getIdLong())+"\n";
						out += "User set currency subcommand: "+GuildIni.getUserSetCurrencyLevel(e.getGuild().getIdLong())+"\n";
						out += "Filter command: "+GuildIni.getFilterLevel(e.getGuild().getIdLong())+"\n";
						out += "Filter word filter subcommand: "+GuildIni.getFilterWordFilterLevel(e.getGuild().getIdLong())+"\n";
						out += "Filter name filter subcommand: "+GuildIni.getFilterNameFilterLevel(e.getGuild().getIdLong())+"\n";
						out += "Filter name kick subcommand: "+GuildIni.getFilterNameKickLevel(e.getGuild().getIdLong())+"\n";
						out += "Filter funny names subcommand: "+GuildIni.getFilterFunnyNamesLevel(e.getGuild().getIdLong())+"\n";
						out += "Filter staff names subcommand: "+GuildIni.getFilterStaffNamesLevel(e.getGuild().getIdLong())+"\n";
						out += "Quiz command: "+GuildIni.getQuizLevel(e.getGuild().getIdLong())+"\n";
						out += "Rolereaction command: "+GuildIni.getRoleReactionLevel(e.getGuild().getIdLong())+"\n";
						out += "Rss command: "+GuildIni.getRssLevel(e.getGuild().getIdLong())+"\n";
						out += "Randomshop command: "+GuildIni.getRandomshopLevel(e.getGuild().getIdLong())+"\n";
						out += "Patchnotes command: "+GuildIni.getPatchnotesLevel(e.getGuild().getIdLong())+"\n";
						out += "Doubleexperience command: "+GuildIni.getDoubleExperienceLevel(e.getGuild().getIdLong())+"\n";
						out += "Equip command: "+GuildIni.getEquipLevel(e.getGuild().getIdLong());
						
						e.getTextChannel().sendMessage("`"+out+"`").queue();
					}
					else{
						e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(commandsLevel, e.getGuild())).build()).queue();
					}
				}
				else {
					e.getTextChannel().sendMessage(denied.setDescription("**"+e.getMember().getAsMention()+" Something went wrong. Please recheck the syntax and try again!**").build()).queue();
				}
			}
			else {
				e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(commandLevel, e.getGuild())).build()).queue();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		Logger logger = LoggerFactory.getLogger(Display.class);
		logger.debug("{} has used Display command", e.getMember().getUser().getIdLong());
	}

	@Override
	public String help() {
		return null;
	}

}
