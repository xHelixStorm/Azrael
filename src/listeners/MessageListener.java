package listeners;

import java.awt.Color;
import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.vdurmont.emoji.EmojiManager;

import commandsContainer.FilterExecution;
import commandsContainer.SetWarning;
import commandsContainer.UserExecution;
import core.Guilds;
import core.Hashes;
import core.Messages;
import core.UserPrivs;
import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import filter.LanguageFilter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import preparedMessages.ReactionMessage;
import rankingSystem.Rank;
import rankingSystem.RankingThreadExecution;
import sql.RankingSystem;
import sql.Azrael;
import threads.RunQuiz;

public class MessageListener extends ListenerAdapter{
	
	@Override
	public void onMessageReceived(MessageReceivedEvent e){
		ExecutorService executor = Executors.newFixedThreadPool(2);
		
		try {
			File warning = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/warnings_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId()+".azr");
			File user = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/user_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId()+"_0.azr");
			File filter = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/filter_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId()+"_0.azr");
			File reaction = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/reaction_gu"+e.getGuild().getIdLong()+"ch"+e.getTextChannel().getId()+".azr");
			File quiz = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/quizstarter.azr");
			File runquiz = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/quiztime.azr");
			
			long user_id = e.getMember().getUser().getIdLong();
			long guild_id = e.getGuild().getIdLong();
			String message = e.getMessage().getContentRaw();
			long channel_id = e.getTextChannel().getIdLong();
			
			if(IniFileReader.getChannelLog()){
				LocalDateTime time = LocalDateTime.now();
				String image_url = "";
				for(Attachment attch : e.getMessage().getAttachments()){
					image_url = (e.getMessage().getContentRaw().length() == 0 && image_url.length() == 0) ? image_url+"("+attch.getProxyUrl()+")" : image_url+"\n("+attch.getProxyUrl()+")";
				}
				Messages collectedMessage = new Messages();
				collectedMessage.setUserID(user_id);
				collectedMessage.setUsername(e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator());
				collectedMessage.setGuildID(guild_id);
				collectedMessage.setChannelID(channel_id);
				collectedMessage.setChannelName(e.getTextChannel().getName());
				collectedMessage.setMessage(message+image_url+"\n");
				collectedMessage.setMessageID(e.getMessageIdLong());
				collectedMessage.setTime(time);
				FileSetting.appendFile("./message_log/"+e.getTextChannel().getName()+".txt", "["+collectedMessage.getTime().toString()+" - "+collectedMessage.getUserName()+"]: "+collectedMessage.getMessage());
				if(IniFileReader.getCacheLog() && !UserPrivs.isUserBot(e.getMember().getUser(), guild_id)) {
					Hashes.addMessagePool(e.getMessageIdLong(), collectedMessage);
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
			if(reaction.exists() && UserPrivs.isUserBot(e.getMember().getUser(), guild_id)) {
				int counter = Integer.parseInt(FileSetting.readFile(IniFileReader.getTempDirectory()+"AutoDelFiles/reaction_gu"+e.getGuild().getIdLong()+"ch"+e.getTextChannel().getId()+".azr"));
				Message m = e.getMessage();
				String [] reactions = IniFileReader.getReactions();
				for(int i = 1; i <= counter; i++) {
					if(!reactions[0].equals("true")) {
						m.addReaction(EmojiManager.getForAlias(ReactionMessage.getReaction(i)).getUnicode()).complete();
					}
					else {
						if(reactions[i].length() > 0) {
							try {
								m.addReaction(e.getGuild().getEmotesByName(reactions[i], false).get(0)).complete();
							} catch(Exception exc) {
								m.addReaction(EmojiManager.getForAlias(":"+reactions[i]+":").getUnicode()).complete();
							}
						}
						else {
							m.addReaction(EmojiManager.getForAlias(ReactionMessage.getReaction(i)).getUnicode()).complete();
						}
					}
				}
				Hashes.addReactionMessage(e.getGuild().getIdLong(), e.getMessage().getIdLong());
				reaction.delete();
			}
			
			if(quiz.exists()) {
				String content = FileSetting.readFile(IniFileReader.getTempDirectory()+"AutoDelFiles/quizstarter.azr");
				if(e.getMember().getUser().getId().equals(content) && (message.equals("1") || message.equals("2") || message.equals("3"))) {
					//run the quiz in a thread. // retrieve the log channel and quiz channel at the same time and pass them over to the new Thread
					long log_channel_id;
					Azrael.SQLgetTwoChanneIDs(e.getGuild().getIdLong(), "qui", "log");
					if(Azrael.getChannelID2() != 0) {
						log_channel_id = Azrael.getChannelID();
					}
					else {
						log_channel_id = e.getTextChannel().getIdLong();
					}
					e.getTextChannel().sendMessage("The quiz will run shortly in <#"+Azrael.getChannelID()+">!").queue();
					//execute independent Quiz Thread
					new Thread(new RunQuiz(e, Azrael.getChannelID(), log_channel_id, Integer.parseInt(message))).start();
					//remove the file after starting 
					quiz.delete();
				}
			}
			
			if(runquiz.exists()) {
				String content = FileSetting.readFile(IniFileReader.getTempDirectory()+"AutoDelFiles/quiztime.azr");
				if(!content.equals("skip-question") || !content.equals("interrupt-questions")) {
					Azrael.SQLgetChannelID(guild_id, "qui");
					if(Azrael.getChannelID() == e.getTextChannel().getIdLong() && !UserPrivs.isUserBot(e.getMember().getUser(), guild_id)) {
						if(UserPrivs.isUserAdmin(e.getMember().getUser(), guild_id) || e.getMember().getUser().getIdLong() == IniFileReader.getAdmin()) {
							if(message.equals("skip-question") || message.equals("interrupt-questions")) {
								FileSetting.createFile(IniFileReader.getTempDirectory()+"AutoDelFiles/quiztime.azr", message);
							}
						}
						if(!(content.length() == 7) || !(content.length() == 8)) {
							if(Hashes.getQuiz(Integer.parseInt(content)).getAnswer1().trim().equalsIgnoreCase(message) ||
							   Hashes.getQuiz(Integer.parseInt(content)).getAnswer2().trim().equalsIgnoreCase(message) ||
							   Hashes.getQuiz(Integer.parseInt(content)).getAnswer3().trim().equalsIgnoreCase(message)) {
								Integer hash = Hashes.getQuizWinners(e.getMember());
								if(hash == null) {
									FileSetting.createFile(IniFileReader.getTempDirectory()+"AutoDelFiles/quiztime.azr", e.getMember().getUser().getId());
								}
							}
						}
					}
				}
				Azrael.setChannelID(0);
			}
			
			Guilds guild_settings = Hashes.getStatus(guild_id);
			if(guild_settings.getRankingState() == true){
				RankingSystem.SQLgetWholeRankView(user_id);
				Rank user_details = Hashes.getRanking(user_id);
				if(user_details == null){
					var editedRows = RankingSystem.SQLInsertUser(user_id, e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), guild_settings.getLevelID(), guild_settings.getRankID(), guild_settings.getProfileID(), guild_settings.getIconID());
					if(editedRows > 0) {
						var editedRows2 = RankingSystem.SQLInsertUserDetails(user_id, 0, 0, 50000, 0);
						if(editedRows2 > 0) {
							var editedRows3 = RankingSystem.SQLInsertUserGuild(user_id, guild_id);
							if(editedRows3 > 0) {
								if(channel_id != 0) {
									EmbedBuilder success = new EmbedBuilder().setColor(Color.GREEN).setTitle("Table insertion successful!");
									e.getGuild().getTextChannelById(channel_id).sendMessage(success.setDescription("The user **"+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+"** with the ID number **"+user_id+"** has been successfully inserted into all required ranking system table!").build()).queue();
								}
							}
						}
					}
				}
				else{
					Azrael.SQLgetTwoChanneIDs(guild_id, "bot", "qui");
					if(!UserPrivs.isUserBot(e.getMember().getUser(), e.getGuild().getIdLong()) && e.getTextChannel().getIdLong() != Azrael.getChannelID() && e.getTextChannel().getIdLong() != Azrael.getChannelID2()){
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
							percent_multiplier = Integer.parseInt(RankingSystem.SQLExpBoosterExistsInInventory(user_id).replaceAll("[^0-9]*", ""));
						} catch(NumberFormatException nfe){
							percent_multiplier = 0;
						}
						
						RankingThreadExecution.setProgress(e, user_id, guild_id, message, roleAssignLevel, role_id, percent_multiplier, user_details, guild_settings);
					}
				}
			}
			
			Azrael.SQLgetChannel_Filter(channel_id);
			if(Hashes.getFilterLang(channel_id).size() > 0){
				executor.execute(new LanguageFilter(e, Hashes.getFilterLang(channel_id)));
			}
		} catch(NullPointerException npe){
			//play with your thumbs 
		}
		RankingSystem.clearAllVariables();
		Azrael.clearAllVariables();
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
