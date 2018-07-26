package commands;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fileManagement.IniFileReader;
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
					
					RankingDB.SQLgetUserUserDetailsGuildRanking(user_id, guild_id);
					boolean ranking_state = RankingDB.getRankingState();
					
					if(ranking_state == true){				
						if(!file.exists()){
							try {
								file.createNewFile();
							} catch (IOException e2) {
								e2.printStackTrace();
							}
							
							new Thread(new DelayDelete(fileName, 30000, false)).start();
							
							float experienceCounter;
							int convertedExperience;
							
							String name = e.getGuild().getMemberById(user_id).getEffectiveName();
							String avatar = e.getGuild().getMemberById(user_id).getUser().getEffectiveAvatarUrl();
							int level = RankingDB.getLevel();
							float currentExperience = RankingDB.getCurrentExperience();
							float rankUpExperience = RankingDB.getRankUpExperience();
							long experience = RankingDB.getExperience();
							long currency = RankingDB.getCurrency();
							int max_level = RankingDB.getMaxLevel();
							int profile_skin = RankingDB.getProfileSkin();
							int icon_skin = RankingDB.getIconSkin();
							int bar_color = RankingDB.getColorProfile();
							boolean additional_text = RankingDB.getExpAndPercentAllowedProfile();
							int color_r = RankingDB.getTextColorRProfile();
							int color_g = RankingDB.getTextColorGProfile();
							int color_b = RankingDB.getTextColorBProfile();
							int rankx = RankingDB.getRankXProfile();
							int ranky = RankingDB.getRankYProfile();
							int rank_width = RankingDB.getRankWidthProfile();
							int rank_height = RankingDB.getRankHeightProfile();
							
							if(level == max_level){currentExperience = 999999; rankUpExperience = 999999;}
							
							experienceCounter = (currentExperience / rankUpExperience)*100;
							convertedExperience = (int) experienceCounter;
							if(convertedExperience > 100) {
								convertedExperience = 100;
							}
							
							RankingDB.SQLRanking(guild_id);
							search: for(rankingSystem.Rank ranking : RankingDB.getRankList()){
								if(user_id == ranking.getUser_id()){
									rank = ranking.getRank();
									RankingDB.clearArrayList();
									break search;
								}
							}
							
							RankingMethods.getProfile(e, name, avatar, convertedExperience, level, currentExperience, rankUpExperience, experience, currency, rank, profile_skin, icon_skin, bar_color, additional_text, color_r, color_g, color_b, rankx, ranky, rank_width, rank_height);
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
