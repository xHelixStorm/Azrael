package listeners;

import java.awt.Color;
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
import constructors.Cache;
import constructors.Guilds;
import constructors.Messages;
import constructors.Rank;
import core.CommandHandler;
import core.CommandParser;
import core.Hashes;
import core.UserPrivs;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
import filter.LanguageFilter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import preparedMessages.ReactionMessage;
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
			
			var warning = Hashes.getTempCache("warnings_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId());
			var user = Hashes.getTempCache("user_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId());
			var filter = Hashes.getTempCache("filter_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId());
			var inventory_bot = Hashes.getTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId());
			var randomshop_bot = Hashes.getTempCache("randomshop_bot_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId());
			var reaction = Hashes.getTempCache("reaction_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId());
			var rss = Hashes.getTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId());
			var quiz = Hashes.getTempCache("quizstarter_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId());
			var runquiz = Hashes.getTempCache("quiztime"+e.getGuild().getId());
			
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
			
			if(warning != null) {
				SetWarning.performUpdate(e, message, warning, "warnings_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId());
			}
			
			if(filter != null) {
				FilterExecution.performAction(e, message, filter);
			}
			
			if(user != null){
				UserExecution.performAction(e, message, user);
			}
			
			if(inventory_bot != null && UserPrivs.isUserBot(e.getMember().getUser(), guild_id) && inventory_bot.getExpiration() - System.currentTimeMillis() > 0) {
				String cache_content = inventory_bot.getAdditionalInfo();
				String [] array = cache_content.split("_");
				final long member_id = Long.parseLong(array[0]);
				final int current_page = Integer.parseInt(array[1]);
				final int last_page = Integer.parseInt(array[2]);
				final String inventory_tab = array[3];
				final String sub_tab = array[4];
				
				boolean createTemp = false;
				if(current_page > 1) {
					e.getMessage().addReaction(EmojiManager.getForAlias(":arrow_left:").getUnicode()).complete();
					createTemp = true;
				}
				if(current_page < last_page) {
					e.getMessage().addReaction(EmojiManager.getForAlias(":arrow_right:").getUnicode()).complete();
					createTemp = true;
				}
				if(createTemp == true) {
					Hashes.addTempCache("inventory_gu"+e.getGuild().getId()+"me"+e.getMessageId()+"us"+member_id, new Cache(0, current_page+"_"+last_page+"_"+inventory_tab+"_"+sub_tab));
				}
				Hashes.clearTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId());
			}
			
			if(randomshop_bot != null && UserPrivs.isUserBot(e.getMember().getUser(), guild_id) && randomshop_bot.getExpiration() - System.currentTimeMillis() > 0) {
				String cache_content = randomshop_bot.getAdditionalInfo();
				String [] array = cache_content.split("_");
				final long member_id = Long.parseLong(array[0]);
				final int current_page = Integer.parseInt(array[1]);
				final String input = array[2];
				final int last_page = Integer.parseInt(array[3]);
				
				boolean createCache = false;
				if(current_page > 1) {
					e.getMessage().addReaction(EmojiManager.getForAlias(":arrow_left:").getUnicode()).complete();
					createCache = true;
				}
				if(current_page < last_page) {
					e.getMessage().addReaction(EmojiManager.getForAlias(":arrow_right:").getUnicode()).complete();
					createCache = true;
				}
				if(createCache == true) {
					Hashes.addTempCache("randomshop_gu"+e.getGuild().getId()+"me"+e.getMessageId()+"us"+member_id, new Cache(0, current_page+"_"+last_page+"_"+input));
				}
				Hashes.clearTempCache("randomshop_bot_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId());
			}
			
			if(reaction != null && UserPrivs.isUserBot(e.getMember().getUser(), guild_id)) {
				int counter = Integer.parseInt(reaction.getAdditionalInfo());
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
				Hashes.clearTempCache("reaction_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId());
			}
			
			if(rss != null && !UserPrivs.isUserBot(e.getMember().getUser(), guild_id) && rss.getExpiration() - System.currentTimeMillis() > 0) {
				var key = "rss_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId();
				String task = rss.getAdditionalInfo();
				if(!message.equalsIgnoreCase("exit")) {
					if(task.equals("remove") && message.replaceAll("[0-9]", "").length() == 0) {
						if(RssExecution.removeFeed(e, Integer.parseInt(message)-1))
							Hashes.clearTempCache(key);
					}
					else if(task.equals("format") && message.replaceAll("[0-9]", "").length() == 0) {
						RssExecution.currentFormat(e, Integer.parseInt(message)-1, key);
					}
					else if(task.contains("updateformat")) {
						if(RssExecution.updateFormat(e, Integer.parseInt(task.replaceAll("[^0-9]", "")), message))
							Hashes.clearTempCache(key);
					}
					else if(task.equals("test") && message.replaceAll("[0-9]", "").length() == 0) {
						if(RssExecution.runTest(e, Integer.parseInt(message)-1))
							Hashes.clearTempCache(key);
					}
				}
				else {
					EmbedBuilder embed = new EmbedBuilder().setColor(Color.BLUE);
					e.getTextChannel().sendMessage(embed.setDescription("Rss command cancelled").build()).queue();
				}
			}
			
			if(quiz != null && quiz.getExpiration() - System.currentTimeMillis() > 0) {
				String content = quiz.getAdditionalInfo();
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
					Hashes.clearTempCache("quizstarter_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId());
				}
			}
			
			if(runquiz != null) {
				String content = runquiz.getAdditionalInfo();
				if(!content.equals("skip-question") || !content.equals("interrupt-questions")) {
					var qui_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type().equals("qui")).findAny().orElse(null);
					if(qui_channel != null && qui_channel.getChannel_ID() == e.getTextChannel().getIdLong() && !UserPrivs.isUserBot(e.getMember().getUser(), guild_id)) {
						if(UserPrivs.isUserAdmin(e.getMember().getUser(), guild_id) || e.getMember().getUser().getIdLong() == GuildIni.getAdmin(guild_id)) {
							if(message.equals("skip-question") || message.equals("interrupt-questions")) {
								Hashes.addTempCache("quiztime"+e.getGuild().getId(), new Cache(0, message));
							}
						}
						if(!(content.length() == 7) || !(content.length() == 8)) {
							if(Hashes.getQuiz(Integer.parseInt(content)).getAnswer1().trim().equalsIgnoreCase(message) ||
							   Hashes.getQuiz(Integer.parseInt(content)).getAnswer2().trim().equalsIgnoreCase(message) ||
							   Hashes.getQuiz(Integer.parseInt(content)).getAnswer3().trim().equalsIgnoreCase(message)) {
								Integer hash = Hashes.getQuizWinners(e.getMember());
								if(hash == null) {
									Hashes.addTempCache("quiztime"+e.getGuild().getId(), new Cache(0, e.getMember().getUser().getId()));
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
						Rank ranking_levels = RankingSystem.SQLgetRoles(guild_id).parallelStream().filter(f -> f.getLevel_Requirement() == (user_details.getLevel()+1)).findAny().orElse(null);
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
}
