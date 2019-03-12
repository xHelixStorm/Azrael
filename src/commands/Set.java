package commands;

import java.awt.Color;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.SetChannelFilter;
import commandsContainer.SetCommandLevel;
import commandsContainer.SetDailyItem;
import commandsContainer.SetGiveawayItems;
import commandsContainer.SetIconDefaultSkin;
import commandsContainer.SetLevelDefaultSkin;
import commandsContainer.SetMaxExperience;
import commandsContainer.SetProfileDefaultSkin;
import commandsContainer.SetRankDefaultSkin;
import commandsContainer.SetRankingSystem;
import commandsContainer.SetWarning;
import core.UserPrivs;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import inventory.Dailies;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingSystem;

public class Set implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(GuildIni.getSetCommand(e.getGuild().getIdLong())){
			Logger logger = LoggerFactory.getLogger(RoleReaction.class);
			logger.debug("{} has used Set command", e.getMember().getUser().getId());
			
			EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.WHITE).setThumbnail(IniFileReader.getSettingsThumbnail()).setTitle("Set up your server to use the capacities of this bot to the fullest!");
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");
			String input = e.getMessage().getContentRaw();
			
			if(UserPrivs.isUserAdmin(e.getMember().getUser(), e.getGuild().getIdLong()) || e.getMember().getUser().getIdLong() == GuildIni.getAdmin(e.getGuild().getIdLong())){
				final String prefix = GuildIni.getCommandPrefix(e.getGuild().getIdLong());
				if(input.equals(prefix+"set")){
					e.getTextChannel().sendMessage(messageBuild.setDescription("Use the set command to set specific parameters for the bot:\n\n"
							+ "**-channel-filter**: To set one or few language filters for one channel\n\n"
							+ "**-warnings**: To set a max allowed number of warnings before the affected users gets banned together with the mute time for each warning\n\n"
							+ "**-commands**: To disable, enable or limit specific commands to a bot channel or for the whole server\n\n"
							+ "**-ranking**: To enable or disable the rankiing system\n\n"
							+ "**-max-experience**: To enable/disable the max experience limiter and to set the limit in experience\n\n"
							+ "**-default-level-skin**: To define the default skin for level ups\n\n"
							+ "**-default-rank-skin**: To define the default skin for rank commands like "+prefix+"rank\n\n"
							+ "**-default-profile-skin**: To define the default skin for profile commands like "+prefix+"profile\n\n"
							+ "**-default-icon-skin**: To define the default skin for level up icons that get displayed on rank and profile commands\n\n"
							+ "**-daily-item**: To add a item to the list to win every 24 hours\n\n"
							+ "**-giveaway-items**: To add giveaway rewards in private message when H!daily gets called\n\n\n"
							+ "Write the full command with one parameter to return more information. E.g **"+prefix+"set -channel-filter**").build()).queue();
				}
				else if(input.equals(prefix+"set -channel-filter")){
					e.getTextChannel().sendMessage(messageBuild.setDescription("Use the command with this parameter to set one or few self chosen filters on a channel. Write the command in the following way as an example:\n\n**"+prefix+"set -channel-filter #yourchannel eng,ger,fre**\nThese languages can be added to the filter:\n**eng** for English\n**ger** for German\n**fre** for French\n**tur** for Turkish\n**rus** for Russian").build()).queue();
				}
				else if(input.contains(prefix+"set -channel-filter ")){
					input = input.substring(20+prefix.length());
					SetChannelFilter.runTask(e, input);
				}
				else if(input.equals(prefix+"set -warnings")) {
					SetWarning.runHelp(e);
				}
				else if(input.contains(prefix+"set -warnings ")){
					SetWarning.runTask(e, e.getMessage().getContentRaw().substring(prefix.length()+14));
				}
				else if(input.equals(prefix+"set -commands")){
					e.getTextChannel().sendMessage(messageBuild.setDescription("To change the level on how the commands are allowed to be used, try to use the following:\n\n**"+prefix+"set -commands disable** to disable specific commands in all channels\n**"+prefix+"set -commands bot** to enable specific commands only in bot channel\n**"+prefix+"set -commands enable** to enable specific commands in all channels").build()).queue();
				}
				else if(input.contains(prefix+"set -commands ")){
					input = input.substring(14+prefix.length());
					SetCommandLevel.runTask(e, input);
				}
				else if(input.equals(prefix+"set -ranking")){
					e.getTextChannel().sendMessage(messageBuild.setDescription("Enable or disable the ranking system with the following command:\n\n**"+prefix+"set -ranking enable**: To enable the ranking system\n**"+prefix+"set -ranking disable**: To disable the ranking system").build()).queue();
				}
				else if(input.contains(prefix+"set -ranking ")){
					if(RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getMaxLevel() != 0){
						input = input.substring(13+prefix.length());
						SetRankingSystem.runTask(e, input);
					}
					else{
						e.getTextChannel().sendMessage(e.getMember().getAsMention() + " **Before using this command, set the max level for this guild!**").queue();
					}
				}
				else if(input.equals(prefix+"set -max-experience")){
					e.getTextChannel().sendMessage(messageBuild.setDescription("To use this command, type one of the following parameters after the syntax:\n\n**"+prefix+"set -max-experience <experience>**: To set an experience limit per day\n"
							+ "**"+prefix+"set -max-experience enable**: To enable the experience limit\n"
							+ "**"+prefix+"set -max-experience disable**: To disable the experience limit").build()).queue();
				}
				else if(input.contains(prefix+"set -max-experience ")){
					input = input.substring(19+prefix.length());
					SetMaxExperience.runTask(e, input, RankingSystem.SQLgetGuild(e.getGuild().getIdLong()));
				}
				else if(input.equals(prefix+"set -default-level-skin")){
					String out = "";
					for(rankingSystem.Rank rankingSystem : RankingSystem.SQLgetRankingLevel()){
						out+= "Theme "+rankingSystem.getRankingLevel()+":\t"+rankingSystem.getLevelDescription()+"\n";
					}
					if(out.length() > 0)
						e.getTextChannel().sendMessage(messageBuild.setDescription("Type "+prefix+"set -default-level-skin and then the digit for the desired skin. These skins are available for the level up pop ups:\n\n"+out).build()).queue();
					else
						e.getTextChannel().sendMessage(messageBuild.setDescription("An internal error occurred. Themes from table RankingSystem.ranking_level couldn't be loaded").build()).queue();
				}
				else if(input.contains(prefix+"set -default-level-skin ")){
					int last_theme = RankingSystem.SQLgetRankingLevel().size();
					if(last_theme > 0) {
						input = input.substring(24+prefix.length());
						if(input.replaceAll("[0-9]", "").length() == 0)
							SetLevelDefaultSkin.runTask(e, Integer.parseInt(input), last_theme);
						else
							e.getTextChannel().sendMessage(e.getMember().getAsMention()+" Please insert a theme digit to set a default level skin!").queue();
					}
					else {
						e.getTextChannel().sendMessage("An internal error occurred. Themes from table RankingSystem.ranking_level couldn't be loaded").queue();
					}
				}
				else if(input.equals(prefix+"set -default-rank-skin")){
					String out = "";
					for(rankingSystem.Rank rankingSystem : RankingSystem.SQLgetRankingRank()){
						out+= "Theme "+rankingSystem.getRankingRank()+":\t"+rankingSystem.getRankDescription()+"\n";
					}
					if(out.length() > 0)
						e.getTextChannel().sendMessage(messageBuild.setDescription("Type "+prefix+"set -default-rank-skin and then the digit for the desired skin. These skins are available for the rank command:\n\n"+out).build()).queue();
					else
						e.getTextChannel().sendMessage(messageBuild.setDescription("An internal error occurred. Themes from table RankingSystem.ranking_rank couldn't be loaded").build()).queue();
				}
				else if(input.contains(prefix+"set -default-rank-skin ")){
					int last_theme = RankingSystem.SQLgetRankingRank().size();
					if(last_theme > 0) {
						input = input.substring(23+prefix.length());
						if(input.replaceAll("[0-9]", "").length() == 0)
							SetRankDefaultSkin.runTask(e, Integer.parseInt(input), last_theme);
						else
							e.getTextChannel().sendMessage(e.getMember().getAsMention()+" Please insert a theme digit to set a default rank skin!").queue();
					}
					else {
						e.getTextChannel().sendMessage("An internal error occurred. Themes from table RankingSystem.ranking_rank couldn't be loaded").queue();
					}
				}
				else if(input.equals(prefix+"set -default-profile-skin")){
					String out = "";
					for(rankingSystem.Rank rankingSystem : RankingSystem.SQLgetRankingProfile()){
						out+= "Theme "+rankingSystem.getRankingProfile()+":\t"+rankingSystem.getProfileDescription()+"\n";
					}
					if(out.length() > 0)
						e.getTextChannel().sendMessage(messageBuild.setDescription("Type "+prefix+"set -default-profile-skin and then the digit for the desired skin. These skins are available for the profile command:\n\n"+out).build()).queue();
					else
						e.getTextChannel().sendMessage(messageBuild.setDescription("An internal error occurred. Themes from table RankingSystem.ranking_profile couldn't be loaded").build()).queue();
				}
				else if(input.contains(prefix+"set -default-profile-skin ")){
					int last_theme = RankingSystem.SQLgetRankingProfile().size();
					if(last_theme > 0) {
						input = input.substring(26+prefix.length());
						if(input.replaceAll("[0-9]", "").length() == 0)
							SetProfileDefaultSkin.runTask(e, Integer.parseInt(input), last_theme);
						else
							e.getTextChannel().sendMessage(e.getMember().getAsMention()+" Please insert a theme digit to set a default profile skin!").queue();
					}
					else {
						e.getTextChannel().sendMessage("An internal error occurred. Themes from table RankingSystem.ranking_profile couldn't be loaded").queue();
					}
				}
				else if(input.equals(prefix+"set -default-icon-skin")){
					String out = "";
					for(rankingSystem.Rank rankingSystem : RankingSystem.SQLgetRankingIcons()){
						out+= "Theme "+rankingSystem.getRankingIcon()+":\t"+rankingSystem.getIconDescription()+"\n";
					}
					if(out.length() > 0)
						e.getTextChannel().sendMessage(messageBuild.setDescription("Type "+prefix+"set -default-icon-skin and then the digit for the desired level up icons. These level up icons are available:\n\n"+out).build()).queue();
					else
						e.getTextChannel().sendMessage(messageBuild.setDescription("An internal error occurred. Themes from table RankingSystem.ranking_icons couldn't be loaded").build()).queue();
				}
				else if(input.contains(prefix+"set -default-icon-skin ")){
					int last_theme = RankingSystem.SQLgetRankingIcons().size();
					if(last_theme > 0) {
						input = input.substring(23+prefix.length());
						if(input.replaceAll("[0-9]", "").length() == 0)
							SetIconDefaultSkin.runTask(e, Integer.parseInt(input), last_theme);
						else
							e.getTextChannel().sendMessage(e.getMember().getAsMention()+" Please insert a theme digit to set a default icons skin!").queue();
					}
					else
						e.getTextChannel().sendMessage("An internal error occurred. Themes from table RankingSystem.ranking_icons couldn't be loaded").queue();
				}
				else if(input.equals(prefix+"set -daily-item")){
					e.getTextChannel().sendMessage(messageBuild.setDescription("Write the name of the daily reward you want to make available for dailies together with the weight and type of the item. For example:\n**"+prefix+"set -daily-item \"5000 "+RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getCurrency()+"\" -weight 70 -type cur**\nNote that the total weight can't exceed 100 and that the currently available types are **cur** for currency , **exp** for experience enhancement items and **cod** for code giveaways.").build()).queue();
				}
				else if(input.contains(prefix+"set -daily-item ")){
					input = input.substring(16+prefix.length());
					ArrayList<Dailies> daily_items = RankingSystem.SQLgetDailiesAndType(e.getGuild().getIdLong(), RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getThemeID());
					var tot_weight = daily_items.parallelStream().mapToInt(i -> i.getWeight()).sum();
					SetDailyItem.runTask(e, input, daily_items, tot_weight);
				}
				else if(input.equals(prefix+"set -giveaway-items")) {
					e.getTextChannel().sendMessage(messageBuild.setDescription("Please past a pastebin link, together with the command, that contains all giveaway codes for the current month").build()).queue();
				}
				else if(input.contains(prefix+"set -giveaway-items ")) {
					input = input.substring(20+prefix.length());
					SetGiveawayItems.runTask(e, input);
				}
				else{
					e.getTextChannel().sendMessage("**"+e.getMember().getAsMention()+" Something went wrong. Please recheck the syntax and try again!**").queue();
				}
			}
			else{
				e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. This command can be used only from an Administrator. Here a cookie** :cookie:").build()).queue();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		
	}

	@Override
	public String help() {
		return null;
	}

}
