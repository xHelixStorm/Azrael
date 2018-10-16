package listeners;

import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import commandsContainer.FilterExecution;
import commandsContainer.SetWarning;
import commandsContainer.UserExecution;
import core.Guilds;
import core.Hashes;
import core.UserPrivs;
import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import filter.LanguageFilter;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import rankingSystem.Rank;
import rankingSystem.RankingThreadExecution;
import sql.RankingDB;
import sql.SqlConnect;

public class MessageListener extends ListenerAdapter{
	
	@Override
	public void onMessageReceived(MessageReceivedEvent e){
		ExecutorService executor = Executors.newFixedThreadPool(2);
		
		try {
			File warning = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/warnings_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId()+".azr");
			File user = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/user_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId()+"_0.azr");
			File filter = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/filter_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId()+"_0.azr");
			
			long user_id = e.getMember().getUser().getIdLong();
			long guild_id = e.getGuild().getIdLong();
			String message = e.getMessage().getContentRaw();
			long channel_id = e.getTextChannel().getIdLong();
			
			if(IniFileReader.getChannelLog().equals("true")){
				LocalDateTime time = LocalDateTime.now();
				String image_url = "";
				for(Attachment attch : e.getMessage().getAttachments()){
					image_url = (e.getMessage().getContentRaw().length() == 0 && image_url.length() == 0) ? image_url+"("+attch.getProxyUrl()+")" : image_url+"\n("+attch.getProxyUrl()+")";
				}
				FileSetting.appendFile("./message_log/"+e.getTextChannel().getName()+".txt", "["+time.toString()+" - "+e.getMember().getEffectiveName()+"]: "+e.getMessage().getContentRaw()+image_url+"\n");
				if(IniFileReader.getCacheLog().equals("true") && !UserPrivs.isUserBot(e.getMember().getUser(), guild_id)) {
					Hashes.addMessagePool(e.getMessageIdLong(), "["+time.toString()+" - "+e.getMember().getEffectiveName()+"]: "+e.getMessage().getContentRaw()+image_url);
				}
			}
			
			if(warning.exists()) {
				SetWarning.performUpdate(e, message);
			}
			
			if(filter.exists()) {
				String file_name = getFileName(new File(IniFileReader.getTempDirectory()+"AutoDelFiles/filter_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId()+"_1.azr"), 20, "filter", e);
				FilterExecution.performAction(e, message, file_name);
			}
			
			if(user.exists()){
				String file_name = getFileName(new File(IniFileReader.getTempDirectory()+"AutoDelFiles/user_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId()+"_1.azr"), 20, "user", e);
				UserExecution.performAction(e, message, file_name);
			}
			
			Guilds guild_settings = Hashes.getStatus(guild_id);
			if(guild_settings.getRankingState() == true){
				RankingDB.SQLgetWholeRankView(user_id);
				Rank user_details = Hashes.getRanking(user_id);
				if(user_details == null){
					RankingDB.SQLInsertUser(user_id, e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), guild_settings.getLevelID(), guild_settings.getRankID(), guild_settings.getProfileID(), guild_settings.getIconID());
					RankingDB.SQLInsertUserDetails(user_id, 0, 0, 50000, 0);
					RankingDB.SQLInsertUserGuild(user_id, guild_id);
				}
				else{
					SqlConnect.SQLgetChannelID(guild_id, "bot");
					long bot_channel = SqlConnect.getChannelID();
					if(!UserPrivs.isUserBot(e.getMember().getUser(), e.getGuild().getIdLong()) && e.getTextChannel().getIdLong() != bot_channel){
						int roleAssignLevel = 0;
						long role_id = 0;
						Rank ranking_levels = Hashes.getRankingRoles(guild_id+"_"+(user_details.getLevel()+1));
						if(ranking_levels != null){
							if(ranking_levels.getGuildID() == guild_id){
								roleAssignLevel = ranking_levels.getLevel_Requirement();
								role_id = ranking_levels.getRoleID();
							}
						}
						
						int percent_multiplier;
						try {
							RankingDB.SQLExpBoosterExistsInInventory();
							percent_multiplier = Integer.parseInt(RankingDB.getDescription().replaceAll("[^0-9]*", ""));
						} catch(NumberFormatException nfe){
							percent_multiplier = 0;
						}
						
						RankingThreadExecution.setProgress(e, user_id, guild_id, message, roleAssignLevel, role_id, percent_multiplier, user_details, guild_settings);
					}
				}
			}
			
			SqlConnect.SQLgetChannel_Filter(channel_id);
			if(Hashes.getFilterLang(channel_id).size() > 0){
				executor.execute(new LanguageFilter(e, Hashes.getFilterLang(channel_id)));
			}
		} catch(NullPointerException npe){
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			System.out.println("["+timestamp+"] The mute role has been assigned!");
		}
		RankingDB.clearAllVariables();
		SqlConnect.clearAllVariables();
		executor.shutdown();
	}
	
	private String getFileName(File file, int max_count, String name, MessageReceivedEvent e) {
		String file_name = IniFileReader.getTempDirectory()+"AutoDelFiles/"+name+"_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId()+"_0.azr";
		int counter = 1;
		boolean break_while = false;
		while(counter < max_count && break_while == false){
			if(file.exists()){
				file = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/"+name+"_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId()+"_"+(counter+1)+".azr");
				file_name = IniFileReader.getTempDirectory()+"AutoDelFiles/"+name+"_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId()+"_"+counter+".azr";
			}
			else{
				break_while = true;
			}
			counter++;
		}
		return file_name;
	}
}
