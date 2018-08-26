package listeners;

import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import commandsContainer.FilterExecution;
import commandsContainer.SetWarning;
import commandsContainer.UserExecution;
import core.Hashes;
import core.UserPrivs;
import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import filter.LanguageFilter;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import rankingSystem.RankingThreadExecution;
import sql.RankingDB;
import sql.SqlConnect;

public class MessageListener extends ListenerAdapter{
	
	@Override
	public void onMessageReceived(MessageReceivedEvent e){
		ExecutorService executor = Executors.newFixedThreadPool(2);
		
		try {
			File warning = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/warnings_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId()+".azr");
			File user = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/user_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId()+".azr");
			File filter = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/filter_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId()+"_0.azr");
			
			long user_id = e.getMember().getUser().getIdLong();
			long guild_id = e.getGuild().getIdLong();
			String message = e.getMessage().getContentRaw();
			
			RankingDB.SQLgetUserUserDetailsRanking(user_id);
			RankingDB.SQLgetGuild(guild_id);
			boolean ranking_state = RankingDB.getRankingState();
			long channel_id = e.getTextChannel().getIdLong();
			
			if(IniFileReader.getChannelLog().equals("true")){
				LocalDateTime time = LocalDateTime.now();
				FileSetting.appendFile("./message_log/"+e.getTextChannel().getName()+".txt", "["+time.toString()+" - "+e.getMember().getEffectiveName()+"]: "+e.getMessage().getContentRaw()+"\n");
				if(IniFileReader.getCacheLog().equals("true")) {
					Hashes.addMessagePool(e.getMessageIdLong(), "["+time.toString()+" - "+e.getMember().getEffectiveName()+"]: "+e.getMessage().getContentRaw());
				}
			}
			
			if(warning.exists()) {
				SetWarning.performUpdate(e, message);
			}
			
			if(filter.exists()) {
				filter = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/filter_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId()+"_1.azr");
				String file_name = IniFileReader.getTempDirectory()+"AutoDelFiles/filter_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId()+"_0.azr";
				int filter_counter = 1;
				boolean break_while = false;
				while(filter_counter < 20 && break_while == false){
					if(filter.exists()){
						filter = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/filter_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId()+"_"+(filter_counter+1)+".azr");
						file_name = IniFileReader.getTempDirectory()+"AutoDelFiles/filter_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId()+"_"+filter_counter+".azr";
					}
					else{
						break_while = true;
					}
					filter_counter++;
				}
				FilterExecution.performAction(e, message, file_name);
			}
			
			if(user.exists()){
				UserExecution.performAction(e, message);
			}
			
			if(ranking_state == true && !e.getMessage().getContentRaw().contains(IniFileReader.getCommandPrefix()+"profile") && !e.getMessage().getContentRaw().contains(IniFileReader.getCommandPrefix()+"rank")){
				try {
					Thread.sleep(100);
					SqlConnect.SQLgetChannelID(guild_id, "bot");
					long bot_channel = SqlConnect.getChannelID();
					if(RankingDB.getUserID() == 0 && RankingDB.getLevel() == 0 && RankingDB.getCurrentExperience() == 0 && RankingDB.getRankUpExperience() == 0 && RankingDB.getExperience() == 0 && RankingDB.getCurrency() == 0){
						RankingDB.SQLInsertUser(user_id, e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), RankingDB.getRankingLevel(), RankingDB.getRankingRank(), RankingDB.getRankingProfile(), RankingDB.getRankingIcon());
						RankingDB.SQLInsertUserDetails(user_id, 0, 0, 300, 0, 50000, 0);
						RankingDB.SQLInsertUserGuild(user_id, guild_id);
					}
					else{
						if(!UserPrivs.isUserBot(e.getMember().getUser(), e.getGuild().getIdLong()) && e.getTextChannel().getIdLong() != bot_channel){
							int rankUpExperience = RankingDB.getRankUpExperience();
							int max_level = RankingDB.getMaxLevel();
							int level = RankingDB.getLevel();
							int currentExperience = RankingDB.getCurrentExperience();
							long currency = RankingDB.getCurrency();
							int level_skin = RankingDB.getLevelSkin();
							int icon_skin = RankingDB.getIconSkin();
							int color_r = RankingDB.getTextColorRLevel();
							int color_g = RankingDB.getTextColorGLevel();
							int color_b = RankingDB.getTextColorBLevel();
							int rankx = RankingDB.getRankXLevel();
							int ranky = RankingDB.getRankYLevel();
							int rank_width = RankingDB.getRankWidthLevel();
							int rank_height = RankingDB.getRankHeightLevel();
							
							RankingDB.SQLgetMaxExperience(guild_id);
							long max_experience = RankingDB.getMaxExperience();
							boolean max_experience_enabled = RankingDB.getEnabled();
							
							RankingDB.SQLgetRole(level+1);
							int roleAssignLevel = RankingDB.getRoleLevelRequirement();
							long role_id = RankingDB.getRoleID();
							int percent_multiplier;
							
							try {
								RankingDB.SQLExpBoosterExistsInInventory();
								percent_multiplier = Integer.parseInt(RankingDB.getDescription().replaceAll("[^0-9]*", ""));
							} catch(NumberFormatException nfe){
								percent_multiplier = 0;
							}
							
							executor.execute(new RankingThreadExecution(e, user_id, guild_id, message, rankUpExperience, max_level, level, currentExperience, currency, max_experience, max_experience_enabled, roleAssignLevel, role_id, level_skin, icon_skin, percent_multiplier, color_r, color_g, color_b, rankx, ranky, rank_width, rank_height));
						}
					}
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			
			SqlConnect.SQLgetChannel_Filter(channel_id);;
			ArrayList<String> filter_lang = SqlConnect.getFilter_Lang();
			if(!filter_lang.isEmpty()){
				executor.execute(new LanguageFilter(e, filter_lang));
			}
		} catch(NullPointerException npe){
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			System.out.println("["+timestamp+"] The mute role has been assigned!");
		}
		RankingDB.clearAllVariables();
		SqlConnect.clearAllVariables();
		executor.shutdown();
	}
}
