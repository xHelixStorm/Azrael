package commands;

import java.awt.Color;

import commandsContainer.SetChannelFilter;
import commandsContainer.SetCommandLevel;
import commandsContainer.SetDailyItem;
import commandsContainer.SetLevelDefaultSkin;
import commandsContainer.SetMaxExperience;
import commandsContainer.SetMaxLevel;
import commandsContainer.SetProfileDefaultSkin;
import commandsContainer.SetRankDefaultSkin;
import commandsContainer.SetRankingSystem;
import commandsContainer.SetWarning;
import core.UserPrivs;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingDB;
import sql.SqlConnect;

public class Set implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getSetCommand().equals("true")){
			EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.WHITE);
			String input = e.getMessage().getContentRaw();
			
			if(UserPrivs.isUserAdmin(e.getMember().getUser(), e.getGuild().getIdLong()) || e.getMember().getUser().getId().equals(IniFileReader.getAdmin())){
				if(input.equals(IniFileReader.getCommandPrefix()+"set")){
					e.getTextChannel().sendMessage(messageBuild.setDescription("Use the set command to set specific parameters for the bot:\n\n"
							+ "**-channel-filter**: To set one or few language filters for one channel\n\n"
							+ "**-warnings**: To set a max allowed number of warnings before the affected users gets banned together with the mute time for each warning\n\n"
							+ "**-commands**: To disable, enable or limit specific commands to a bot channel or for the whole server\n\n"
							+ "**-ranking**: To enable or disable the rankiing system\n\n"
							+ "**-max-experience**: To enable/disable the max experience limiter and to set the limit in experience\n\n"
							+ "**-max_level**: To define the max level that can be reached with the ranking system\n\n"
							+ "**-default-level-skin**: To define the default skin for level ups\n\n"
							+ "**-default-rank-skin**: To define the default skin for rank commands like "+IniFileReader.getCommandPrefix()+"rank\n\n"
							+ "**-default-profile-skin**: To define the default skin for profile commands like "+IniFileReader.getCommandPrefix()+"profile\n\n"
							+ "**-default-icon-skin**: To define the default skin for level up icons that get displayed on rank and profile commands\n\n"
							+ "**-daily-item**: To add a item to the list to win every 24 hours\n\n\n"
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
				else if(input.contains(IniFileReader.getCommandPrefix()+"set -warnings")){
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
					RankingDB.SQLgetGuild(e.getGuild().getIdLong());
					if(RankingDB.getMaxLevel() != 0){
						input = input.substring(13+IniFileReader.getCommandPrefix().length());
						SetRankingSystem.runTask(e, input);
					}
					else{
						e.getTextChannel().sendMessage(e.getMember().getAsMention() + " **Before using this command, set the max level for this guild!**").queue();
					}
				}
				else if(input.equals(IniFileReader.getCommandPrefix()+"set -max-experience")){
					e.getTextChannel().sendMessage(messageBuild.setDescription("To use this command, type one of the following parameters after the syntax:\n\n**"+IniFileReader.getCommandPrefix()+"set max-experience <experience>**: To set an experience limit per day\n"
							+ "**"+IniFileReader.getCommandPrefix()+"set max-experience enable**: To enable the experience limit\n"
							+ "**"+IniFileReader.getCommandPrefix()+"set max-experience disable**: To disable the experience limit").build()).queue();
				}
				else if(input.contains(IniFileReader.getCommandPrefix()+"set -max-experience")){
					input = input.substring(19+IniFileReader.getCommandPrefix().length());
					RankingDB.SQLgetMaxExperience(e.getGuild().getIdLong());
					long experience = RankingDB.getMaxExperience();
					SetMaxExperience.runTask(e, input, experience);
				}
				else if(input.equals(IniFileReader.getCommandPrefix()+"set -max-level")){
					e.getTextChannel().sendMessage(messageBuild.setDescription("To use this command, type **"+IniFileReader.getCommandPrefix()+"set -max-level <level in number>** for defining the max level that can be achieved in this guild").build()).queue();
				}
				else if(input.contains(IniFileReader.getCommandPrefix()+"set -max-level ")){
					input = input.substring(15+IniFileReader.getCommandPrefix().length());
					SetMaxLevel.runTask(e, Integer.parseInt(input));
				}
				else if(input.equals(IniFileReader.getCommandPrefix()+"set -default-level-skin")){
					RankingDB.SQLgetRankingLevel();
					String out = "";
					for(rankingSystem.Rank rankingSystem : RankingDB.getRankList()){
						out+= "Setup: "+rankingSystem.getRankingLevel()+"\tTheme: "+rankingSystem.getDescription()+"\n";
					}
					e.getTextChannel().sendMessage(messageBuild.setDescription("Type "+IniFileReader.getCommandPrefix()+"set -default-level-skin and then the digit for the desired skin. These skins are available for the level up pop ups:\n\n"+out).build()).queue();
					RankingDB.clearArrayList();
				}
				else if(input.contains(IniFileReader.getCommandPrefix()+"set -default-level-skin ")){
					RankingDB.SQLgetRankingLevel();
					int last_theme = RankingDB.getRankList().size();
					input = input.substring(24+IniFileReader.getCommandPrefix().length());
					SetLevelDefaultSkin.runTask(e, Integer.parseInt(input), last_theme);
					RankingDB.clearArrayList();
				}
				else if(input.equals(IniFileReader.getCommandPrefix()+"set -default-rank-skin")){
					RankingDB.SQLgetRankingRank();
					String out = "";
					for(rankingSystem.Rank rankingSystem : RankingDB.getRankList()){
						out+= "Setup: "+rankingSystem.getRankingRank()+"\tTheme: "+rankingSystem.getDescription()+"\n";
					}
					e.getTextChannel().sendMessage(messageBuild.setDescription("Type "+IniFileReader.getCommandPrefix()+"set -default-rank-skin and then the digit for the desired skin. These skins are available for the rank command:\n\n"+out).build()).queue();
					RankingDB.clearArrayList();
				}
				else if(input.contains(IniFileReader.getCommandPrefix()+"set -default-rank-skin ")){
					RankingDB.SQLgetRankingRank();
					int last_theme = RankingDB.getRankList().size();
					input = input.substring(23+IniFileReader.getCommandPrefix().length());
					SetRankDefaultSkin.runTask(e, Integer.parseInt(input), last_theme);
					RankingDB.clearArrayList();
				}
				else if(input.equals(IniFileReader.getCommandPrefix()+"set -default-profile-skin")){
					RankingDB.SQLgetRankingProfile();
					String out = "";
					for(rankingSystem.Rank rankingSystem : RankingDB.getRankList()){
						out+= "Setup: "+rankingSystem.getRankingProfile()+"\tTheme: "+rankingSystem.getDescription()+"\n";
					}
					e.getTextChannel().sendMessage(messageBuild.setDescription("Type "+IniFileReader.getCommandPrefix()+"set -default-profile-skin and then the digit for the desired skin. These skins are available for the profile command:\n\n"+out).build()).queue();
					RankingDB.clearArrayList();
				}
				else if(input.contains(IniFileReader.getCommandPrefix()+"set -default-profile-skin ")){
					RankingDB.SQLgetRankingProfile();
					int last_theme = RankingDB.getRankList().size();
					input = input.substring(26+IniFileReader.getCommandPrefix().length());
					SetProfileDefaultSkin.runTask(e, Integer.parseInt(input), last_theme);
					RankingDB.clearArrayList();
				}
				else if(input.equals(IniFileReader.getCommandPrefix()+"set -default-icon-skin")){
					RankingDB.SQLgetRankingIcons();
					String out = "";
					for(rankingSystem.Rank rankingSystem : RankingDB.getRankList()){
						out+= "Setup: "+rankingSystem.getRankingIcon()+"\tTheme: "+rankingSystem.getDescription()+"\n";
					}
					e.getTextChannel().sendMessage(messageBuild.setDescription("Type "+IniFileReader.getCommandPrefix()+"set -default-icon-skin and then the digit for the desired level up icons. These level up icons are available:\n\n"+out).build()).queue();
					RankingDB.clearArrayList();
				}
				else if(input.contains(IniFileReader.getCommandPrefix()+"set -default-icon-skin ")){
					RankingDB.SQLgetRankingIcons();
					int last_theme = RankingDB.getRankList().size();
					input = input.substring(23+IniFileReader.getCommandPrefix().length());
					SetRankDefaultSkin.runTask(e, Integer.parseInt(input), last_theme);
					RankingDB.clearArrayList();
				}
				else if(input.equals(IniFileReader.getCommandPrefix()+"set -daily-item")){
					e.getTextChannel().sendMessage(messageBuild.setDescription("Write the name of the daily reward you want to make available for dailies together with the weight and type of the item. For example:\n**"+IniFileReader.getCommandPrefix()+"set -daily-item \"5000 PEN\" -weight 70 -type cur**\nNote that the total weight can't exceed 100 and that the currently available types are **cur** for currency and **exp** for experience enhancement items.").build()).queue();
				}
				else if(input.contains(IniFileReader.getCommandPrefix()+"set -daily-item ")){
					input = input.substring(16+IniFileReader.getCommandPrefix().length());
					RankingDB.SQLgetDailiesAndType();
					RankingDB.SQLgetSumWeightFromDailyItems();
					SetDailyItem.runTask(e, input, RankingDB.getDailies(), RankingDB.getWeight());
					RankingDB.clearDailiesArray();
				}
				else{
					e.getTextChannel().sendMessage("**"+e.getMember().getAsMention()+" Something went wrong. Please recheck the syntax and try again!**").queue();
				}
			}
			else{
				e.getTextChannel().sendMessage(":warning: " + e.getMember().getAsMention() + " **My apologies young padawan. This command can be used only from [GS]Heiliger or from an Administrator. Here a cookie** :cookie:").queue();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		RankingDB.clearAllVariables();
		SqlConnect.clearAllVariables();
	}

	@Override
	public String help() {
		return null;
	}

}
