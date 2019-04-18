package listeners;

import java.awt.Color;
import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiManager;

import commandsContainer.FilterExecution;
import commandsContainer.RssExecution;
import commandsContainer.SetWarning;
import commandsContainer.UserExecution;
import core.CommandHandler;
import core.CommandParser;
import core.Guilds;
import core.Hashes;
import core.Messages;
import core.UserPrivs;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
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
	
	@SuppressWarnings("null")
	@Override
	public void onMessageReceived(MessageReceivedEvent e){
		ExecutorService executor = Executors.newFixedThreadPool(2);
		
		try {
			//execute commands first
			if(e.getMessage().getContentRaw().startsWith(GuildIni.getCommandPrefix(e.getGuild().getIdLong())) && e.getMessage().getAuthor().getId() != e.getJDA().getSelfUser().getId()){
				var prefixLength = GuildIni.getCommandPrefix(e.getGuild().getIdLong()).length();
				if(!CommandHandler.handleCommand(CommandParser.parser(e.getMessage().getContentRaw().substring(0, prefixLength)+e.getMessage().getContentRaw().substring(prefixLength).toLowerCase(), e))) {
					Logger logger = LoggerFactory.getLogger(MessageListener.class);
					logger.debug("Command {} doesn't exist!", e.getMessage().getContentRaw());
				}
			}
			
			File warning = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/warnings_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId()+".azr");
			File user = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/user_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId()+"_0.azr");
			File filter = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/filter_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId()+"_0.azr");
			File inventory_bot = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+".azr");
			File randomshop_bot = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/randomshop_bot_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+".azr");
			File reaction = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/reaction_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+".azr");
			File rss = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/rss_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+".azr");
			File quiz = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/quizstarter.azr");
			File runquiz = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/quiztime.azr");
			
			long user_id = e.getMember().getUser().getIdLong();
			long guild_id = e.getGuild().getIdLong();
			String message = e.getMessage().getContentRaw();
			long channel_id = e.getTextChannel().getIdLong();
			
			final boolean channelLog = GuildIni.getChannelLog(guild_id);
			if(channelLog){
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
				if(channelLog && !UserPrivs.isUserBot(e.getMember().getUser(), guild_id)) {
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
			
			if(inventory_bot.exists() && UserPrivs.isUserBot(e.getMember().getUser(), guild_id)) {
				String file_content = FileSetting.readFile(IniFileReader.getTempDirectory()+"AutoDelFiles/inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+".azr");
				String [] array = file_content.split("_");
				final long member_id = Long.parseLong(array[0]);
				final int current_page = Integer.parseInt(array[1]);
				final int last_page = Integer.parseInt(array[2]);
				final String inventory_tab = array[3];
				final String sub_tab = array[4];
				
				boolean createFile = false;
				if(current_page > 1) {
					e.getMessage().addReaction(EmojiManager.getForAlias(":arrow_left:").getUnicode()).complete();
					createFile = true;
				}
				if(current_page < last_page) {
					e.getMessage().addReaction(EmojiManager.getForAlias(":arrow_right:").getUnicode()).complete();
					createFile = true;
				}
				if(createFile == true) {
					FileSetting.createFile(IniFileReader.getTempDirectory()+"AutoDelFiles/inventory_gu"+e.getGuild().getId()+"me"+e.getMessageId()+"us"+member_id+".azr", current_page+"_"+last_page+"_"+inventory_tab+"_"+sub_tab);
				}
				inventory_bot.delete();
			}
			
			if(randomshop_bot.exists() && UserPrivs.isUserBot(e.getMember().getUser(), guild_id)) {
				String file_content = FileSetting.readFile(IniFileReader.getTempDirectory()+"AutoDelFiles/randomshop_bot_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+".azr");
				String [] array = file_content.split("_");
				final long member_id = Long.parseLong(array[0]);
				final int current_page = Integer.parseInt(array[1]);
				final String input = array[2];
				final int last_page = Integer.parseInt(array[3]);
				
				boolean createFile = false;
				if(current_page > 1) {
					e.getMessage().addReaction(EmojiManager.getForAlias(":arrow_left:").getUnicode()).complete();
					createFile = true;
				}
				if(current_page < last_page) {
					e.getMessage().addReaction(EmojiManager.getForAlias(":arrow_right:").getUnicode()).complete();
					createFile = true;
				}
				if(createFile == true) {
					FileSetting.createFile(IniFileReader.getTempDirectory()+"AutoDelFiles/randomshop_gu"+e.getGuild().getId()+"me"+e.getMessageId()+"us"+member_id+".azr", current_page+"_"+last_page+"_"+input);
				}
				randomshop_bot.delete();
				
			}
			
			if(reaction.exists() && UserPrivs.isUserBot(e.getMember().getUser(), guild_id)) {
				int counter = Integer.parseInt(FileSetting.readFile(IniFileReader.getTempDirectory()+"AutoDelFiles/reaction_gu"+e.getGuild().getIdLong()+"ch"+e.getTextChannel().getId()+".azr"));
				Message m = e.getMessage();
				String [] reactions = GuildIni.getReactions(guild_id);
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
			
			if(rss.exists() && !UserPrivs.isUserBot(e.getMember().getUser(), guild_id)) {
				String task = FileSetting.readFile(rss.getAbsolutePath());
				if(!message.equalsIgnoreCase("exit")) {
					if(task.equals("remove") && message.replaceAll("[0-9]", "").length() == 0) {
						if(RssExecution.removeFeed(e, Integer.parseInt(message)-1))
							rss.delete();
					}
					else if(task.equals("format") && message.replaceAll("[0-9]", "").length() == 0) {
						RssExecution.currentFormat(e, Integer.parseInt(message)-1, rss.getAbsolutePath());
					}
					else if(task.contains("updateformat")) {
						if(RssExecution.updateFormat(e, Integer.parseInt(task.replaceAll("[^0-9]", "")), message))
							rss.delete();
					}
					else if(task.equals("test") && message.replaceAll("[0-9]", "").length() == 0) {
						if(RssExecution.runTest(e, Integer.parseInt(message)-1))
							rss.delete();
					}
				}
				else {
					EmbedBuilder embed = new EmbedBuilder().setColor(Color.BLUE);
					e.getTextChannel().sendMessage(embed.setDescription("Rss command cancelled").build()).queue();
				}
			}
			
			if(quiz.exists()) {
				String content = FileSetting.readFile(IniFileReader.getTempDirectory()+"AutoDelFiles/quizstarter.azr");
				if(e.getMember().getUser().getId().equals(content) && (message.equals("1") || message.equals("2") || message.equals("3"))) {
					//run the quiz in a thread. // retrieve the log channel and quiz channel at the same time and pass them over to the new Thread
					var channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type().equals("log") || f.getChannel_Type().equals("qui")).collect(Collectors.toList());
					var log_channel = channels.parallelStream().filter(f -> f.getChannel_Type().equals("log")).findAny().orElse(null);
					var qui_channel = channels.parallelStream().filter(f -> f.getChannel_Type().equals("qui")).findAny().orElse(null);
					if(log_channel == null)
						log_channel.setChannel_ID(e.getTextChannel().getIdLong());
					if(qui_channel == null)
						qui_channel.setChannel_ID(e.getTextChannel().getIdLong());
					e.getTextChannel().sendMessage("The quiz will run shortly in <#"+qui_channel.getChannel_ID()+">!").queue();
					//execute independent Quiz Thread
					new Thread(new RunQuiz(e, qui_channel.getChannel_ID(), log_channel.getChannel_ID(), Integer.parseInt(message))).start();
					//remove the file after starting 
					quiz.delete();
				}
			}
			
			if(runquiz.exists()) {
				String content = FileSetting.readFile(IniFileReader.getTempDirectory()+"AutoDelFiles/quiztime.azr");
				if(!content.equals("skip-question") || !content.equals("interrupt-questions")) {
					var qui_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type().equals("qui")).findAny().orElse(null);
					if(qui_channel != null && qui_channel.getChannel_ID() == e.getTextChannel().getIdLong() && !UserPrivs.isUserBot(e.getMember().getUser(), guild_id)) {
						if(UserPrivs.isUserAdmin(e.getMember().getUser(), guild_id) || e.getMember().getUser().getIdLong() == GuildIni.getAdmin(guild_id)) {
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
			}
			
			Guilds guild_settings = RankingSystem.SQLgetGuild(guild_id);
			if(guild_settings.getRankingState() == true && (Hashes.getCommentedUser(e.getMember().getUser().getId()+"_"+e.getGuild().getId()) == null || guild_settings.getMessageTimeout() == 0)){
				Rank user_details = RankingSystem.SQLgetWholeRankView(user_id, guild_id, guild_settings.getThemeID());
				if(user_details == null){
					if(RankingSystem.SQLInsertUser(user_id, guild_id, e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), guild_settings.getLevelID(), guild_settings.getRankID(), guild_settings.getProfileID(), guild_settings.getIconID()) > 0) {
						if(RankingSystem.SQLInsertUserDetails(user_id, guild_id, 0, 0, 50000, 0) > 0) {
							var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type().equals("log")).findAny().orElse(null);
							if(log_channel != null) {
								EmbedBuilder success = new EmbedBuilder().setColor(Color.GREEN).setTitle("Table insertion successful!");
								e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(success.setDescription("The user **"+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+"** with the ID number **"+user_id+"** has been successfully inserted into all required ranking system table!").build()).queue();
							}
						}
					}
				}
				else{
					var channels = Azrael.SQLgetChannels(guild_id).parallelStream().filter(f -> f.getChannel_Type().equals("bot") || f.getChannel_Type().equals("qui")).collect(Collectors.toList());
					if(!UserPrivs.isUserBot(e.getMember().getUser(), e.getGuild().getIdLong()) && channels.parallelStream().filter(f -> f.getChannel_ID() == e.getTextChannel().getIdLong()).findAny().orElse(null) == null){
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
							percent_multiplier = Integer.parseInt(RankingSystem.SQLExpBoosterExistsInInventory(user_id, guild_id, guild_settings.getThemeID()).replaceAll("[^0-9]*", ""));
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
