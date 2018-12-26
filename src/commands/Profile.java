package commands;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import core.Hashes;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import rankingSystem.RankingMethods;
import sql.RankingDB;
import threads.DelayDelete;

public class Profile implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getProfileCommand().equals("true")){
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(() -> {
				long user_id = 0;
				if(e.getMessage().getContentRaw().contains(IniFileReader.getCommandPrefix()+"profile ")) {
					String id = e.getMessage().getContentRaw().substring(IniFileReader.getCommandPrefix().length()+7).replaceAll("[^0-9]", "");
					user_id = id.length() > 0 ? Long.parseLong(id) : 0;
				}
				else {
					user_id = e.getMember().getUser().getIdLong();
				}
				
				if(user_id != 0) {
					long guild_id = e.getGuild().getIdLong();
					int rank = 0;
					String fileName = IniFileReader.getTempDirectory()+"CommandDelay/"+e.getMember().getUser().getId()+"_profile.azr";
					File file = new File(fileName);
					
					RankingDB.SQLgetWholeRankView(user_id);
					rankingSystem.Rank user_details = Hashes.getRanking(user_id);
					
					if(Hashes.getStatus(guild_id).getRankingState() == true){				
						if(!file.exists()){
							try {
								file.createNewFile();
							} catch (IOException e2) {
								e2.printStackTrace();
							}
							
							new Thread(new DelayDelete(fileName, 30000)).start();
							
							float experienceCounter;
							int convertedExperience;
							
							String name = e.getGuild().getMemberById(user_id).getEffectiveName();
							String avatar = e.getGuild().getMemberById(user_id).getUser().getEffectiveAvatarUrl();
							int level = user_details.getLevel();
							float currentExperience = user_details.getCurrentExperience();
							float rankUpExperience = user_details.getRankUpExperience();
							long experience = user_details.getExperience();
							long currency = user_details.getCurrency();
							int max_level = Hashes.getStatus(guild_id).getMaxLevel();
							int profile_skin = user_details.getRankingProfile();
							int icon_skin = user_details.getRankingIcon();
							int bar_color = user_details.getBarColorProfile();
							boolean additional_text = user_details.getAdditionalTextProfile();
							int color_r = user_details.getColorRProfile();
							int color_g = user_details.getColorGProfile();
							int color_b = user_details.getColorBProfile();
							int rankx = user_details.getRankXProfile();
							int ranky = user_details.getRankYProfile();
							int rank_width = user_details.getRankWidthProfile();
							int rank_height = user_details.getRankHeightProfile();
							
							if(level == max_level){currentExperience = 999999; rankUpExperience = 999999;}
							
							experienceCounter = (currentExperience / rankUpExperience)*100;
							convertedExperience = (int) experienceCounter;
							if(convertedExperience > 100) {
								convertedExperience = 100;
							}
							
							ArrayList<rankingSystem.Rank> rankList = RankingDB.SQLRanking();
							if(rankList.size() > 0) {
								search: for(rankingSystem.Rank ranking : rankList){
									if(user_id == ranking.getUser_ID()){
										rank = ranking.getRank();
										break search;
									}
								}
							}
							if(currentExperience >= 0) {
								RankingMethods.getProfile(e, name, avatar, convertedExperience, level, currentExperience, rankUpExperience, experience, currency, rank, profile_skin, icon_skin, bar_color, additional_text, color_r, color_g, color_b, rankx, ranky, rank_width, rank_height);
							}
							else {
								EmbedBuilder error = new EmbedBuilder().setColor(Color.RED).setTitle("An error occured!");
								e.getTextChannel().sendMessage(error.setDescription("An error occured on use. Please contact an administrator or moderator!").build()).queue();
								RankingDB.SQLInsertActionLog("critical", user_id, "negative experience value", "The user has less experience points in proportion to his level: "+currentExperience);
							}
						}
						else{
							e.getTextChannel().sendMessage("This command is currently having a cooldown, please try again later").queue();
						}
					}
					else {
						e.getTextChannel().sendMessage("**The ranking system is disabled. Please contact an administrator to enable the feature!**").queue();
					}
				}
				else {
					e.getTextChannel().sendMessage("This user doesn't exist. Please try again!").queue();
				}
			});
			executor.shutdown();
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		RankingDB.clearAllVariables();
	}

	@Override
	public String help() {
		return null;
	}

}
