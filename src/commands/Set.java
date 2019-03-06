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
import commandsContainer.SetMaxLevel;
import commandsContainer.SetProfileDefaultSkin;
import commandsContainer.SetRankDefaultSkin;
import commandsContainer.SetRankingSystem;
import commandsContainer.SetWarning;
import core.Hashes;
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
			
			if(UserPrivs.isUserAdmin(e.getMember().getUser(), e.getGuild().getIdLong()) || e.getMember().getUser().getIdLong() == IniFileReader.getAdmin()){
				if(input.equals(IniFileReader.getCommandPrefix()+"set")){
					e.getTextChannel().sendMessage(messageBuild.setDescription("Use the set command to set specific parameters for the bot:\n\n"
							+ "**-channel-filter**: To set one or few language filters for one channel\n\n"
							+ "**-warnings**: To set a max allowed number of warnings before the affected users gets banned together with the mute time for each warning\n\n"
							+ "**-commands**: To disable, enable or limit specific commands to a bot channel or for the whole server\n\n"
							+ "**-ranking**: To enable or disable the rankiing system\n\n"
							+ "**-max-experience**: To enable/disable the max experience limiter and to set the limit in experience\n\n"
							+ "**-max-level**: To define the max level that can be reached with the ranking system\n\n"
							+ "**-default-level-skin**: To define the default skin for level ups\n\n"
							+ "**-default-rank-skin**: To define the default skin for rank commands like "+IniFileReader.getCommandPrefix()+"rank\n\n"
							+ "**-default-profile-skin**: To define the default skin for profile commands like "+IniFileReader.getCommandPrefix()+"profile\n\n"
							+ "**-default-icon-skin**: To define the default skin for level up icons that get displayed on rank and profile commands\n\n"
							+ "**-daily-item**: To add a item to the list to win every 24 hours\n\n"
							+ "**-giveaway-items**: To add giveaway rewards in private message when H!daily gets called\n\n\n"
							+ "Write the full command with one parameter to return more information. E.g **"+IniFileReader.getCommandPrefix()+"set -channel-filter**").build()).queue();
				}
				else if(input.equals(IniFileReader.getCommandPrefix()+"set -channel-filter")){
					e.getTextChannel().sendMessage(messageBuild.setDescription("Use the command with this parameter to set one or few self chosen filters on a channel. Write the command in the following way as an example:\n\n**"+IniFileReader.getCommandPrefix()+"set -channel-filter #yourchannel eng,ger,fre**\nThese languages can be added to the filter:\n**eng** for English\n**ger** for German\n**fre** for French\n**tur** for Turkish\n**rus** for Russian").build()).queue();
				}
				else if(input.contains(IniFileReader.getCommandPrefix()+"set -channel-filter ")){
					input = input.substring(20+IniFileReader.getCommandPrefix().length());
					SetChannelFilter.runTask(e, input);
				}
				else if(input.equals(IniFileReader.getCommandPrefix()+"set -warnings")) {
					SetWarning.runHelp(e);
				}
				else if(input.contains(IniFileReader.getCommandPrefix()+"set -warnings ")){
					SetWarning.runTask(e, e.getMessage().getContentRaw().substring(IniFileReader.getCommandPrefix().length()+14));
				}
				else if(input.equals(IniFileReader.getCommandPrefix()+"set -commands")){
					e.getTextChannel().sendMessage(messageBuild.setDescription("To change the level on how the commands are allowed to be used, try to use the following:\n\n**"+IniFileReader.getCommandPrefix()+"set -commands disable** to disable specific commands in all channels\n**"+IniFileReader.getCommandPrefix()+"set -commands bot** to enable specific commands only in bot channel\n**"+IniFileReader.getCommandPrefix()+"set -commands enable** to enable specific commands in all channels").build()).queue();
				}
				else if(input.contains(IniFileReader.getCommandPrefix()+"set -commands ")){
					input = input.substring(14+IniFileReader.getCommandPrefix().length());
					SetCommandLevel.runTask(e, input);
				}
				else if(input.equals(IniFileReader.getCommandPrefix()+"set -ranking")){
					e.getTextChannel().sendMessage(messageBuild.setDescription("Enable or disable the ranking system with the following command:\n\n**"+IniFileReader.getCommandPrefix()+"set -ranking enable**: To enable the ranking system\n**"+IniFileReader.getCommandPrefix()+"set -ranking disable**: To disable the ranking system").build()).queue();
				}
				else if(input.contains(IniFileReader.getCommandPrefix()+"set -ranking ")){
					if(RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getMaxLevel() != 0){
						input = input.substring(13+IniFileReader.getCommandPrefix().length());
						SetRankingSystem.runTask(e, input);
					}
					else{
						e.getTextChannel().sendMessage(e.getMember().getAsMention() + " **Before using this command, set the max level for this guild!**").queue();
					}
				}
				else if(input.equals(IniFileReader.getCommandPrefix()+"set -max-experience")){
					e.getTextChannel().sendMessage(messageBuild.setDescription("To use this command, type one of the following parameters after the syntax:\n\n**"+IniFileReader.getCommandPrefix()+"set -max-experience <experience>**: To set an experience limit per day\n"
							+ "**"+IniFileReader.getCommandPrefix()+"set -max-experience enable**: To enable the experience limit\n"
							+ "**"+IniFileReader.getCommandPrefix()+"set -max-experience disable**: To disable the experience limit").build()).queue();
				}
				else if(input.contains(IniFileReader.getCommandPrefix()+"set -max-experience ")){
					input = input.substring(19+IniFileReader.getCommandPrefix().length());
					SetMaxExperience.runTask(e, input, Hashes.getStatus(e.getGuild().getIdLong()));
				}
				else if(input.equals(IniFileReader.getCommandPrefix()+"set -max-level")){
					e.getTextChannel().sendMessage(messageBuild.setDescription("To use this command, type **"+IniFileReader.getCommandPrefix()+"set -max-level <level in number>** for defining the max level that can be achieved in this guild").build()).queue();
				}
				else if(input.contains(IniFileReader.getCommandPrefix()+"set -max-level ")){
					input = input.substring(15+IniFileReader.getCommandPrefix().length());
					SetMaxLevel.runTask(e, Integer.parseInt(input));
				}
				else if(input.equals(IniFileReader.getCommandPrefix()+"set -default-level-skin")){
					String out = "";
					for(rankingSystem.Rank rankingSystem : RankingSystem.SQLgetRankingLevel()){
						out+= "Theme "+rankingSystem.getRankingLevel()+":\t"+rankingSystem.getLevelDescription()+"\n";
					}
					if(out.length() > 0)
						e.getTextChannel().sendMessage(messageBuild.setDescription("Type "+IniFileReader.getCommandPrefix()+"set -default-level-skin and then the digit for the desired skin. These skins are available for the level up pop ups:\n\n"+out).build()).queue();
					else
						e.getTextChannel().sendMessage(messageBuild.setDescription("An internal error occurred. Themes from table RankingSystem.ranking_level couldn't be loaded").build()).queue();
				}
				else if(input.contains(IniFileReader.getCommandPrefix()+"set -default-level-skin ")){
					int last_theme = RankingSystem.SQLgetRankingLevel().size();
					if(last_theme > 0) {
						input = input.substring(24+IniFileReader.getCommandPrefix().length());
						if(input.replaceAll("[0-9]", "").length() == 0)
							SetLevelDefaultSkin.runTask(e, Integer.parseInt(input), last_theme);
						else
							e.getTextChannel().sendMessage(e.getMember().getAsMention()+" Please insert a theme digit to set a default level skin!").queue();
					}
					else {
						e.getTextChannel().sendMessage("An internal error occurred. Themes from table RankingSystem.ranking_level couldn't be loaded").queue();
					}
				}
				else if(input.equals(IniFileReader.getCommandPrefix()+"set -default-rank-skin")){
					String out = "";
					for(rankingSystem.Rank rankingSystem : RankingSystem.SQLgetRankingRank()){
						out+= "Theme "+rankingSystem.getRankingRank()+":\t"+rankingSystem.getRankDescription()+"\n";
					}
					if(out.length() > 0)
						e.getTextChannel().sendMessage(messageBuild.setDescription("Type "+IniFileReader.getCommandPrefix()+"set -default-rank-skin and then the digit for the desired skin. These skins are available for the rank command:\n\n"+out).build()).queue();
					else
						e.getTextChannel().sendMessage(messageBuild.setDescription("An internal error occurred. Themes from table RankingSystem.ranking_rank couldn't be loaded").build()).queue();
				}
				else if(input.contains(IniFileReader.getCommandPrefix()+"set -default-rank-skin ")){
					int last_theme = RankingSystem.SQLgetRankingRank().size();
					if(last_theme > 0) {
						input = input.substring(23+IniFileReader.getCommandPrefix().length());
						if(input.replaceAll("[0-9]", "").length() == 0)
							SetRankDefaultSkin.runTask(e, Integer.parseInt(input), last_theme);
						else
							e.getTextChannel().sendMessage(e.getMember().getAsMention()+" Please insert a theme digit to set a default rank skin!").queue();
					}
					else {
						e.getTextChannel().sendMessage("An internal error occurred. Themes from table RankingSystem.ranking_rank couldn't be loaded").queue();
					}
				}
				else if(input.equals(IniFileReader.getCommandPrefix()+"set -default-profile-skin")){
					String out = "";
					for(rankingSystem.Rank rankingSystem : RankingSystem.SQLgetRankingProfile()){
						out+= "Theme "+rankingSystem.getRankingProfile()+":\t"+rankingSystem.getProfileDescription()+"\n";
					}
					if(out.length() > 0)
						e.getTextChannel().sendMessage(messageBuild.setDescription("Type "+IniFileReader.getCommandPrefix()+"set -default-profile-skin and then the digit for the desired skin. These skins are available for the profile command:\n\n"+out).build()).queue();
					else
						e.getTextChannel().sendMessage(messageBuild.setDescription("An internal error occurred. Themes from table RankingSystem.ranking_profile couldn't be loaded").build()).queue();
				}
				else if(input.contains(IniFileReader.getCommandPrefix()+"set -default-profile-skin ")){
					int last_theme = RankingSystem.SQLgetRankingProfile().size();
					if(last_theme > 0) {
						input = input.substring(26+IniFileReader.getCommandPrefix().length());
						if(input.replaceAll("[0-9]", "").length() == 0)
							SetProfileDefaultSkin.runTask(e, Integer.parseInt(input), last_theme);
						else
							e.getTextChannel().sendMessage(e.getMember().getAsMention()+" Please insert a theme digit to set a default profile skin!").queue();
					}
					else {
						e.getTextChannel().sendMessage("An internal error occurred. Themes from table RankingSystem.ranking_profile couldn't be loaded").queue();
					}
				}
				else if(input.equals(IniFileReader.getCommandPrefix()+"set -default-icon-skin")){
					String out = "";
					for(rankingSystem.Rank rankingSystem : RankingSystem.SQLgetRankingIcons()){
						out+= "Theme "+rankingSystem.getRankingIcon()+":\t"+rankingSystem.getIconDescription()+"\n";
					}
					if(out.length() > 0)
						e.getTextChannel().sendMessage(messageBuild.setDescription("Type "+IniFileReader.getCommandPrefix()+"set -default-icon-skin and then the digit for the desired level up icons. These level up icons are available:\n\n"+out).build()).queue();
					else
						e.getTextChannel().sendMessage(messageBuild.setDescription("An internal error occurred. Themes from table RankingSystem.ranking_icons couldn't be loaded").build()).queue();
				}
				else if(input.contains(IniFileReader.getCommandPrefix()+"set -default-icon-skin ")){
					int last_theme = RankingSystem.SQLgetRankingIcons().size();
					if(last_theme > 0) {
						input = input.substring(23+IniFileReader.getCommandPrefix().length());
						if(input.replaceAll("[0-9]", "").length() == 0)
							SetIconDefaultSkin.runTask(e, Integer.parseInt(input), last_theme);
						else
							e.getTextChannel().sendMessage(e.getMember().getAsMention()+" Please insert a theme digit to set a default icons skin!").queue();
					}
					else
						e.getTextChannel().sendMessage("An internal error occurred. Themes from table RankingSystem.ranking_icons couldn't be loaded").queue();
				}
				else if(input.equals(IniFileReader.getCommandPrefix()+"set -daily-item")){
					e.getTextChannel().sendMessage(messageBuild.setDescription("Write the name of the daily reward you want to make available for dailies together with the weight and type of the item. For example:\n**"+IniFileReader.getCommandPrefix()+"set -daily-item \"5000 "+RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getCurrency()+"\" -weight 70 -type cur**\nNote that the total weight can't exceed 100 and that the currently available types are **cur** for currency , **exp** for experience enhancement items and **cod** for code giveaways.").build()).queue();
				}
				else if(input.contains(IniFileReader.getCommandPrefix()+"set -daily-item ")){
					input = input.substring(16+IniFileReader.getCommandPrefix().length());
					ArrayList<Dailies> daily_items = RankingSystem.SQLgetDailiesAndType(e.getGuild().getIdLong());
					var tot_weight = daily_items.parallelStream().mapToInt(i -> i.getWeight()).sum();
					SetDailyItem.runTask(e, input, daily_items, tot_weight);
				}
				else if(input.equals(IniFileReader.getCommandPrefix()+"set -giveaway-items")) {
					e.getTextChannel().sendMessage(messageBuild.setDescription("Please past a pastebin link, together with the command, that contains all giveaway codes for the current month").build()).queue();
				}
				else if(input.contains(IniFileReader.getCommandPrefix()+"set -giveaway-items ")) {
					input = input.substring(20+IniFileReader.getCommandPrefix().length());
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
