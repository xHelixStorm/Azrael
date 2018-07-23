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

public class Rank implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getPurchaseCommand().equals("true")){
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(() -> {
				long user_id = e.getMember().getUser().getIdLong();
				long guild_id = e.getGuild().getIdLong();
				String fileName = IniFileReader.getTempDirectory()+"CommandDelay/"+user_id+"_rank.azr";
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
						
						int level = RankingDB.getLevel();
						float currentExperience = RankingDB.getCurrentExperience();
						float rankUpExperience = RankingDB.getRankUpExperience();
						int max_level = RankingDB.getMaxLevel();
						int rank_skin = RankingDB.getRankSkin();
						int icon_skin = RankingDB.getIconSkin();
						int bar_color = RankingDB.getColorRank();
						boolean additional_text = RankingDB.getExpAndPercentAllowedRank();
						int color_r = RankingDB.getTextColorRRank();
						int color_g = RankingDB.getTextColorGRank();
						int color_b = RankingDB.getTextColorBRank();
						int rankx = RankingDB.getRankXRank();
						int ranky = RankingDB.getRankYRank();
						int rank_width = RankingDB.getRankWidthRank();
						int rank_height = RankingDB.getRankHeightRank();
						
						if(level == max_level){currentExperience = 999999; rankUpExperience = 999999;}
						
						experienceCounter = (currentExperience / rankUpExperience)*100;
						convertedExperience = (int) experienceCounter;
						if(convertedExperience > 100) {
							convertedExperience = 100;
						}
						
						RankingMethods.getRank(e, convertedExperience, level, rank_skin, icon_skin, bar_color, additional_text, color_r, color_g, color_b, rankx, ranky, rank_width, rank_height);
					}
					else{
						e.getTextChannel().sendMessage("This command is currently having a cooldown, please try again later").queue();
					}
				}
				else{
					e.getTextChannel().sendMessage("**The ranking system is disabled. Please contact an administrator to enable the feature!**").queue();
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
