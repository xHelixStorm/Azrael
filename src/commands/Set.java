package commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.SetChannelFilter;
import commandsContainer.SetCommandLevel;
import commandsContainer.SetDailyItem;
import commandsContainer.SetGiveawayItems;
import commandsContainer.SetIconDefaultSkin;
import commandsContainer.SetLevelDefaultSkin;
import commandsContainer.SetMaxExperience;
import commandsContainer.SetPrivilegeLevel;
import commandsContainer.SetProfileDefaultSkin;
import commandsContainer.SetRankDefaultSkin;
import commandsContainer.SetRankingSystem;
import commandsContainer.SetWarning;
import constructors.Dailies;
import core.UserPrivs;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;

public class Set implements CommandPublic{

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getSetCommand(e.getGuild().getIdLong())) {
			Logger logger = LoggerFactory.getLogger(RoleReaction.class);
			logger.debug("{} has used Set command", e.getMember().getUser().getId());
			
			EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.WHITE).setThumbnail(IniFileReader.getSettingsThumbnail()).setTitle("Set up your server to use the capacities of this bot to the fullest!");
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");
			
			var adminPermission = (GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong());
			final var commandLevel = GuildIni.getSetLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), GuildIni.getSetLevel(e.getGuild().getIdLong())) || adminPermission) {
				final String prefix = GuildIni.getCommandPrefix(e.getGuild().getIdLong());
				if(args.length == 0) {
					e.getChannel().sendMessage(messageBuild.setDescription("Use the set command to set specific parameters for the bot:\n\n"
							+ "**-privilege-level**: To change the right level of a role\n\n"
							+ "**-channel-filter**: To set one or few language filters for one channel\n\n"
							+ "**-warnings**: To set a max allowed number of warnings before the affected users gets banned together with the mute time for each warning\n\n"
							+ "**-commands**: To disable, enable or limit specific commands to a bot channel or for the whole server\n\n"
							+ "**-ranking**: To enable or disable the ranking system\n\n"
							+ "**-max-experience**: To enable/disable the max experience limiter and to set the limit in experience\n\n"
							+ "**-default-level-skin**: To define the default skin for level ups\n\n"
							+ "**-default-rank-skin**: To define the default skin for rank commands like "+prefix+"rank\n\n"
							+ "**-default-profile-skin**: To define the default skin for profile commands like "+prefix+"profile\n\n"
							+ "**-default-icon-skin**: To define the default skin for level up icons that get displayed on rank and profile commands\n\n"
							+ "**-daily-item**: To add a item to the list to win every 24 hours\n\n"
							+ "**-giveaway-items**: To add giveaway rewards in private message when H!daily gets called\n\n\n"
							+ "Write the full command with one parameter to return more information. E.g **"+prefix+"set -channel-filter**").build()).queue();
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase("-privilege-level")) {
					final var thisLevel = GuildIni.getSetPrivilegeLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						e.getChannel().sendMessage(messageBuild.setDescription("Use this command to change the right level from a role between 0 (lowest) to 100 (highest). Write the command as the following example:\n\n**"+prefix+"set -privilege-level <role-id> 50**").build()).queue();
					}
					else {
						e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(thisLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase("-privilege-level")) {
					final var thisLevel = GuildIni.getSetPrivilegeLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						SetPrivilegeLevel.runTask(e, args);
					}
					else {
						e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(thisLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase("-channel-filter")) {
					final var thisLevel = GuildIni.getSetChannelFilterLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						e.getChannel().sendMessage(messageBuild.setDescription("Use the command with this parameter to set one or few self chosen filters on a channel. Write the command as the following example:\n\n**"+prefix+"set -channel-filter #yourchannel eng,ger,fre**\nThese languages can be added to the filter:\n**eng** for English\n**ger** for German\n**fre** for French\n**tur** for Turkish\n**rus** for Russian").build()).queue();
					}
					else {
						e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(thisLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase("-channel-filter")) {
					final var thisLevel = GuildIni.getSetChannelFilterLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						SetChannelFilter.runTask(e, args);
					}
					else {
						e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(thisLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase("-warnings")) {
					final var thisLevel = GuildIni.getSetWarningsLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						SetWarning.runHelp(e);
					}
					else {
						e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(thisLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase("-warnings")) {
					final var thisLevel = GuildIni.getSetWarningsLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						SetWarning.runTask(e, args[1]);
					}
					else {
						e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(thisLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase("-commands")) {
					final var thisLevel = GuildIni.getSetCommandsLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						e.getChannel().sendMessage(messageBuild.setDescription("To change the level on how the commands are allowed to be used, try to use the following:\n\n**"+prefix+"set -commands disable** to disable specific commands in all channels\n**"+prefix+"set -commands bot** to enable specific commands only in bot channel\n**"+prefix+"set -commands enable** to enable specific commands in all channels").build()).queue();
					}
					else {
						e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(thisLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase("-commands")) {
					final var thisLevel = GuildIni.getSetCommandsLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						SetCommandLevel.runTask(e, args[1]);
					}
					else {
						e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(thisLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase("-ranking")) {
					final var thisLevel = GuildIni.getSetRankingLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						e.getChannel().sendMessage(messageBuild.setDescription("Enable or disable the ranking system with the following command:\n\n**"+prefix+"set -ranking enable**: To enable the ranking system\n**"+prefix+"set -ranking disable**: To disable the ranking system").build()).queue();
					}
					else {
						e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(thisLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase("-ranking")) {
					final var thisLevel = GuildIni.getSetRankingLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						if(RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getMaxLevel() != 0) {
							SetRankingSystem.runTask(e, args[1]);
						}
						else {
							e.getChannel().sendMessage(e.getMember().getAsMention() + " **Before using this command, set the max level for this guild!**").queue();
						}
					}
					else {
						e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(thisLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase("-max-experience")) {
					final var thisLevel = GuildIni.getSetMaxExperienceLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						e.getChannel().sendMessage(messageBuild.setDescription("To use this command, type one of the following parameters after the syntax:\n\n**"+prefix+"set -max-experience <experience>**: To set an experience limit per day\n"
								+ "**"+prefix+"set -max-experience enable**: To enable the experience limit\n"
								+ "**"+prefix+"set -max-experience disable**: To disable the experience limit").build()).queue();
					}
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase("-max-experience")) {
					final var thisLevel = GuildIni.getSetMaxExperienceLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						SetMaxExperience.runTask(e, args[1], RankingSystem.SQLgetGuild(e.getGuild().getIdLong()));
					}
					else {
						e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(thisLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase("-default-level-skin")) {
					final var thisLevel = GuildIni.getSetDefaultLevelSkinLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						String out = "";
						final var theme = RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getThemeID();
						for(constructors.Rank rankingSystem : RankingSystem.SQLgetRankingLevel().parallelStream().filter(t -> t.getThemeID() == theme).collect(Collectors.toList())) {
							out+= "Theme "+rankingSystem.getRankingLevel()+":\t"+rankingSystem.getLevelDescription()+"\n";
						}
						if(out.length() > 0)
							e.getChannel().sendMessage(messageBuild.setDescription("Type "+prefix+"set -default-level-skin and then the digit for the desired skin. These skins are available for the level up pop ups:\n\n"+out).build()).queue();
						else
							e.getChannel().sendMessage(messageBuild.setDescription("An internal error occurred. Themes from table RankingSystem.ranking_level couldn't be loaded").build()).queue();
					}
					else {
						e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(thisLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase("-default-level-skin")) {
					final var thisLevel = GuildIni.getSetDefaultLevelSkinLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						final var theme = RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getThemeID();
						int last_theme = RankingSystem.SQLgetRankingLevel().parallelStream().filter(t -> t.getThemeID() == theme).collect(Collectors.toList()).size();
						if(last_theme > 0) {
							if(args[1].replaceAll("[0-9]", "").length() == 0)
								SetLevelDefaultSkin.runTask(e, Integer.parseInt(args[1]), last_theme);
							else
								e.getChannel().sendMessage(e.getMember().getAsMention()+" Please insert a theme digit to set a default level skin!").queue();
						}
						else {
							e.getChannel().sendMessage("An internal error occurred. Themes from table RankingSystem.ranking_level couldn't be loaded").queue();
						}
					}
					else {
						e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(thisLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase("-default-rank-skin")) {
					final var thisLevel = GuildIni.getSetDefaultRankSkinLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						String out = "";
						final var theme = RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getThemeID();
						for(constructors.Rank rankingSystem : RankingSystem.SQLgetRankingRank().parallelStream().filter(t -> t.getThemeID() == theme).collect(Collectors.toList())) {
							out+= "Theme "+rankingSystem.getRankingRank()+":\t"+rankingSystem.getRankDescription()+"\n";
						}
						if(out.length() > 0)
							e.getChannel().sendMessage(messageBuild.setDescription("Type "+prefix+"set -default-rank-skin and then the digit for the desired skin. These skins are available for the rank command:\n\n"+out).build()).queue();
						else
							e.getChannel().sendMessage(messageBuild.setDescription("An internal error occurred. Themes from table RankingSystem.ranking_rank couldn't be loaded").build()).queue();
					}
					else {
						e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(thisLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase("-default-rank-skin")) {
					final var thisLevel = GuildIni.getSetDefaultRankSkinLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						final var theme = RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getThemeID();
						int last_theme = RankingSystem.SQLgetRankingRank().parallelStream().filter(t -> t.getThemeID() == theme).collect(Collectors.toList()).size();
						if(last_theme > 0) {
							if(args[1].replaceAll("[0-9]", "").length() == 0)
								SetRankDefaultSkin.runTask(e, Integer.parseInt(args[1]), last_theme);
							else
								e.getChannel().sendMessage(e.getMember().getAsMention()+" Please insert a theme digit to set a default rank skin!").queue();
						}
						else {
							e.getChannel().sendMessage("An internal error occurred. Themes from table RankingSystem.ranking_rank couldn't be loaded").queue();
						}
					}
					else {
						e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(thisLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase("-default-profile-skin")) {
					final var thisLevel = GuildIni.getSetDefaultProfileSkinLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						String out = "";
						final var theme = RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getThemeID();
						for(constructors.Rank rankingSystem : RankingSystem.SQLgetRankingProfile().parallelStream().filter(t -> t.getThemeID() == theme).collect(Collectors.toList())) {
							out+= "Theme "+rankingSystem.getRankingProfile()+":\t"+rankingSystem.getProfileDescription()+"\n";
						}
						if(out.length() > 0)
							e.getChannel().sendMessage(messageBuild.setDescription("Type "+prefix+"set -default-profile-skin and then the digit for the desired skin. These skins are available for the profile command:\n\n"+out).build()).queue();
						else
							e.getChannel().sendMessage(messageBuild.setDescription("An internal error occurred. Themes from table RankingSystem.ranking_profile couldn't be loaded").build()).queue();
					}
					else {
						e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(thisLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase("-default-profile-skin")) {
					final var thisLevel = GuildIni.getSetDefaultProfileSkinLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						final var theme = RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getThemeID();
						int last_theme = RankingSystem.SQLgetRankingProfile().parallelStream().filter(t -> t.getThemeID() == theme).collect(Collectors.toList()).size();
						if(last_theme > 0) {
							if(args[1].replaceAll("[0-9]", "").length() == 0)
								SetProfileDefaultSkin.runTask(e, Integer.parseInt(args[1]), last_theme);
							else
								e.getChannel().sendMessage(e.getMember().getAsMention()+" Please insert a theme digit to set a default profile skin!").queue();
						}
						else {
							e.getChannel().sendMessage("An internal error occurred. Themes from table RankingSystem.ranking_profile couldn't be loaded").queue();
						}
					}
					else {
						e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(thisLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase("-default-icon-skin")) {
					final var thisLevel = GuildIni.getSetDefaultIconSkinLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						String out = "";
						final var theme = RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getThemeID();
						for(constructors.Rank rankingSystem : RankingSystem.SQLgetRankingIcons().parallelStream().filter(t -> t.getThemeID() == theme).collect(Collectors.toList())) {
							out+= "Theme "+rankingSystem.getRankingIcon()+":\t"+rankingSystem.getIconDescription()+"\n";
						}
						if(out.length() > 0)
							e.getChannel().sendMessage(messageBuild.setDescription("Type "+prefix+"set -default-icon-skin and then the digit for the desired level up icons. These level up icons are available:\n\n"+out).build()).queue();
						else
							e.getChannel().sendMessage(messageBuild.setDescription("An internal error occurred. Themes from table RankingSystem.ranking_icons couldn't be loaded").build()).queue();
					}
					else {
						e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(thisLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase("-default-icon-skin")) {
					final var thisLevel = GuildIni.getSetDefaultIconSkinLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						final var theme = RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getThemeID();
						int last_theme = RankingSystem.SQLgetRankingIcons().parallelStream().filter(t -> t.getThemeID() == theme).collect(Collectors.toList()).size();
						if(last_theme > 0) {
							if(args[1].replaceAll("[0-9]", "").length() == 0)
								SetIconDefaultSkin.runTask(e, Integer.parseInt(args[1]), last_theme);
							else
								e.getChannel().sendMessage(e.getMember().getAsMention()+" Please insert a theme digit to set a default icons skin!").queue();
						}
						else
							e.getChannel().sendMessage("An internal error occurred. Themes from table RankingSystem.ranking_icons couldn't be loaded").queue();
					}
					else {
						e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(thisLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase("-daily-item")) {
					final var thisLevel = GuildIni.getSetDailyItemLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						e.getChannel().sendMessage(messageBuild.setDescription("Write the name of the daily reward you want to make available for dailies together with the weight and type of the item. For example:\n**"+prefix+"set -daily-item \"5000 "+RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getCurrency()+"\" -weight 70 -type cur**\nNote that the total weight can't exceed 100 and that the currently available types are **cur** for currency , **exp** for experience enhancement items and **cod** for code giveaways.").build()).queue();
					}
					else {
						e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(thisLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase("-daily-item")) {
					final var thisLevel = GuildIni.getSetDailyItemLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						ArrayList<Dailies> daily_items = RankingSystem.SQLgetDailiesAndType(e.getGuild().getIdLong(), RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getThemeID());
						var tot_weight = daily_items.parallelStream().mapToInt(i -> i.getWeight()).sum();
						SetDailyItem.runTask(e, e.getMessage().getContentRaw().substring(16+prefix.length()), daily_items, tot_weight);
					}
					else {
						e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(thisLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase("-giveaway-items")) {
					final var thisLevel = GuildIni.getSetGiveawayItemsLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						e.getChannel().sendMessage(messageBuild.setDescription("Please past a pastebin link, together with the command, that contains all giveaway codes for the current month").build()).queue();
					}
					else {
						e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(thisLevel, e.getGuild())).build()).queue();
					}
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase("-giveaway-items")) {
					final var thisLevel = GuildIni.getSetGiveawayItemsLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), thisLevel) || adminPermission) {
						SetGiveawayItems.runTask(e, args[1]);
					}
					else {
						e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(thisLevel, e.getGuild())).build()).queue();
					}
				}
				else {
					e.getChannel().sendMessage("**"+e.getMember().getAsMention()+" Something went wrong. Please recheck the syntax and try again!**").queue();
				}
			}
			else {
				e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(commandLevel, e.getGuild())).build()).queue();
			}
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		
	}

	@Override
	public String help() {
		return null;
	}

}
