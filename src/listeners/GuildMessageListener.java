package listeners;

/**
 * This class gets executed when a message is sent
 * 
 * This class contains various things such as the launcher for
 * regular, url and message censoring, applying reactions on 
 * images, the execution of commands and the following of commands
 * through various steps.
 */

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiManager;

import commandsContainer.FilterExecution;
import commandsContainer.PurchaseExecution;
import commandsContainer.RssExecution;
import commandsContainer.SetWarning;
import commandsContainer.ShopExecution;
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
import filter.URLFilter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rankingSystem.RankingThreadExecution;
import sql.RankingSystem;
import sql.Azrael;
import threads.RunQuiz;
import util.STATIC;

public class GuildMessageListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(GuildMessageListener.class);
	
	@SuppressWarnings({ "preview", "null" })
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
		if(e.getMember() != null) {
			new Thread(() -> {
				long user_id = e.getMember().getUser().getIdLong();
				long guild_id = e.getGuild().getIdLong();
				String message = e.getMessage().getContentRaw();
				long channel_id = e.getChannel().getIdLong();
				Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
				var allChannels = Azrael.SQLgetChannels(e.getGuild().getIdLong());
				var currentChannel = allChannels.parallelStream().filter(f -> f.getChannel_ID() == channel_id).findAny().orElse(null);
				
				//allow to run only one thread at the same time
				ExecutorService executor = Executors.newSingleThreadExecutor();
				executor.execute(() -> {
					if(STATIC.spamDetected(e))
						return;
					
					//execute commands
					if(e.getMessage().getContentRaw().startsWith(GuildIni.getCommandPrefix(e.getGuild().getIdLong())) && e.getMessage().getAuthor().getId() != e.getJDA().getSelfUser().getId()) {
						var prefixLength = GuildIni.getCommandPrefix(e.getGuild().getIdLong()).length();
						if(!CommandHandler.handleCommand(CommandParser.parser(e.getMessage().getContentRaw().substring(0, prefixLength)+e.getMessage().getContentRaw().substring(prefixLength).toLowerCase(), e, null))) {
							logger.warn("Command {} doesn't exist!", e.getMessage().getContentRaw());
						}
					}
					
					//If the channel doesn't allow any text input but only screenshots, then delete
					if(currentChannel != null && currentChannel.getTxtRemoval() && e.getMessage().getAttachments().size() == 0) {
						Hashes.addTempCache("message-removed_gu"+guild_id+"ch"+channel_id+"us"+user_id, new Cache(10000));
						e.getMessage().delete().reason("Text comment without screenshot not allowed!").queue(success -> {}, error -> {
							logger.warn("Message already removed!");
						});
					}
					
					//if the warning parameter has been used under the set command, then forward the user for the next step
					final var warning = Hashes.getTempCache("warnings_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					if(warning != null) {
						SetWarning.performUpdate(e, message, warning, "warnings_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					}
					
					//if the filter command has been used, forward the user for the next step
					final var filter = Hashes.getTempCache("filter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					if(filter != null) {
						FilterExecution.performAction(e, message, filter);
					}
					
					//if the shop command has been used, help the user to navigate through the shop easily
					final var shop = Hashes.getTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					if(shop != null && shop.getExpiration() - System.currentTimeMillis() > 0) {
						//verify if the user whishes to close the shop
						if(message.equalsIgnoreCase("exit")) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("Thanks for the visit! See you again!").build()).queue();
							Hashes.clearTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
						}
						//check if the user has decided for a specific category
						else if(shop.getAdditionalInfo().length() == 0) {
							if(message.equalsIgnoreCase("level ups")) {
								ShopExecution.displayShop(e, "lev", guild_settings.getLevelDescription());
							}
							else if(message.equalsIgnoreCase("ranks")) {
								ShopExecution.displayShop(e, "ran", guild_settings.getRankDescription());
							}
							else if(message.equalsIgnoreCase("profiles")) {
								ShopExecution.displayShop(e, "pro", guild_settings.getProfileDescription());
							}
							else if(message.equalsIgnoreCase("icons")) {
								ShopExecution.displayShop(e, "ico", guild_settings.getIconDescription());
							}
							else if(message.equalsIgnoreCase("items")) {
								ShopExecution.displayShop(e, "ite", "");
							}
							else if(message.equalsIgnoreCase("weapons")) {
								ShopExecution.displayWeaponCategories(e, guild_settings.getThemeID());
							}
							else if(message.equalsIgnoreCase("skills")) {
								ShopExecution.displaySkills(e, guild_settings);
							}
						}
						//display details of the currently selected item or skin
						else if(shop.getAdditionalInfo().matches("(lev|ran|pro|ico|ite)") && !shop.getAdditionalInfo2().contains("%") && !shop.getAdditionalInfo2().contains("$") && !shop.getAdditionalInfo2().contains("#")) {
							if(!message.matches("[^\\d]*") && message.length() <= 9) {
								ShopExecution.displaySingleItem(e, shop.getAdditionalInfo(), shop.getAdditionalInfo2().split("-"), guild_settings, Integer.parseInt(message)-1);
							}
						}
						//verify if the user whishes to purchase, sell or return on the currently selected item
						else if(shop.getAdditionalInfo().matches("(lev|ran|pro|ico|ite)") && (shop.getAdditionalInfo2().contains("%") || shop.getAdditionalInfo2().contains("$") || shop.getAdditionalInfo2().contains("#"))) {
							if(message.equalsIgnoreCase("purchase") && shop.getAdditionalInfo2().contains("%")) {
								PurchaseExecution.purchase(e, shop.getAdditionalInfo(), shop.getAdditionalInfo2().replaceAll("%", ""), guild_settings);
							}
							else if(message.equalsIgnoreCase("sell") && shop.getAdditionalInfo2().contains("#")) {
								PurchaseExecution.sell(e, shop.getAdditionalInfo(), shop.getAdditionalInfo2().replaceAll("#", ""), guild_settings);
							}
							else if(message.equalsIgnoreCase("return")) {
								switch(shop.getAdditionalInfo()) {
									case "lev" -> ShopExecution.displayShop(e, "lev", guild_settings.getLevelDescription());
									case "ran" -> ShopExecution.displayShop(e, "ran", guild_settings.getRankDescription());
									case "pro" -> ShopExecution.displayShop(e, "pro", guild_settings.getProfileDescription());
									case "ico" -> ShopExecution.displayShop(e, "ico", guild_settings.getIconDescription());
									case "ite" -> ShopExecution.displayShop(e, "ite", "");
								}
							}
						}
						//if the weapon category has been selected, check if the user has chosen for a sub category 
						else if(shop.getAdditionalInfo().equals("wea") && shop.getAdditionalInfo2().contains("-") && !shop.getAdditionalInfo2().matches("[\\d]")) {
							var categories = shop.getAdditionalInfo2().split("-");
							var categoryFound = false;
							for(final var category : categories) {
								if(category.equalsIgnoreCase(message)) {
									categoryFound = true;
									break;
								}
							}
							if(categoryFound) {
								ShopExecution.displayShopWeapons(e, message);
							}
						}
						//display details of the currently selected weapon
						else if(shop.getAdditionalInfo().contains("wea-") && !shop.getAdditionalInfo2().contains("%")) {
							if(!message.matches("[^\\d]*") && message.length() <= 9) {
								ShopExecution.displaySingleWeapon(e, shop.getAdditionalInfo(), shop.getAdditionalInfo2().split("-"), guild_settings, Integer.parseInt(message)-1);
							}
						}
						//check if a user wishes to purchase a weapon or return from the currently focused weapon
						else if(shop.getAdditionalInfo().contains("wea-") && shop.getAdditionalInfo2().contains("%")) {
							if(message.equalsIgnoreCase("purchase")) {
								PurchaseExecution.purchase(e, "wep", shop.getAdditionalInfo2().replaceAll("%", ""), guild_settings);
							}
							else if(message.equalsIgnoreCase("return")) {
								ShopExecution.displayShopWeapons(e, shop.getAdditionalInfo().split("-")[1]);
							}
						}
						
						//display details of the currently selected skill
						else if(shop.getAdditionalInfo().equals("ski") && !shop.getAdditionalInfo2().contains("%")) {
							if(!message.matches("[^\\d]*") && message.length() <= 9) {
								ShopExecution.displaySingleSkill(e, guild_settings, shop.getAdditionalInfo2().split("-"), Integer.parseInt(message)-1);
							}
						}
						//check if a user wishes to purchase a skill or return from the currently focused skill
						else if(shop.getAdditionalInfo().equals("ski") && shop.getAdditionalInfo2().contains("%")) {
							if(message.equalsIgnoreCase("purchase")) {
								PurchaseExecution.purchase(e, "ski", shop.getAdditionalInfo2().replaceAll("%", ""), guild_settings);
							}
							else if(message.equalsIgnoreCase("return")) {
								ShopExecution.displaySkills(e, guild_settings);
							}
						}
					}
					
					//check if the user command has been used and forward the user for the next steps
					final var user = Hashes.getTempCache("user_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					if(user != null) {
						UserExecution.performAction(e, message, user, allChannels);
					}
					
					//check if the inventory command has been used and if yes, append reactions when there are multiple pages to jump into
					final var inventory_bot = Hashes.getTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
					if(inventory_bot != null && UserPrivs.isUserBot(e.getMember()) && inventory_bot.getExpiration() - System.currentTimeMillis() > 0) {
						//get details of the inventory
						String cache_content = inventory_bot.getAdditionalInfo();
						String [] array = cache_content.split("_");
						final long member_id = Long.parseLong(array[0]);
						final int current_page = Integer.parseInt(array[1]);
						final int last_page = Integer.parseInt(array[2]);
						final String inventory_tab = array[3];
						final String sub_tab = array[4];
						
						boolean createTemp = false;
						//add reactions
						if(current_page > 1) {
							e.getMessage().addReaction(EmojiManager.getForAlias(":arrow_left:").getUnicode()).queue();
							createTemp = true;
						}
						if(current_page < last_page) {
							e.getMessage().addReaction(EmojiManager.getForAlias(":arrow_right:").getUnicode()).queue();
							createTemp = true;
						}
						//enable reaction events for the current user and drawn inventory
						if(createTemp == true) {
							Hashes.addTempCache("inventory_gu"+e.getGuild().getId()+"me"+e.getMessageId()+"us"+member_id, new Cache(0, current_page+"_"+last_page+"_"+inventory_tab+"_"+sub_tab));
						}
						Hashes.clearTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
					}
					
					//check if the randomshop command has been used
					final var randomshop_bot = Hashes.getTempCache("randomshop_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
					if(randomshop_bot != null && UserPrivs.isUserBot(e.getMember()) && randomshop_bot.getExpiration() - System.currentTimeMillis() > 0) {
						//get details of the inventory
						String cache_content = randomshop_bot.getAdditionalInfo();
						String [] array = cache_content.split("_");
						final long member_id = Long.parseLong(array[0]);
						final int current_page = Integer.parseInt(array[1]);
						final String input = array[2];
						final int last_page = Integer.parseInt(array[3]);
						
						boolean createCache = false;
						//add reactions
						if(current_page > 1) {
							e.getMessage().addReaction(EmojiManager.getForAlias(":arrow_left:").getUnicode()).queue();
							createCache = true;
						}
						if(current_page < last_page) {
							e.getMessage().addReaction(EmojiManager.getForAlias(":arrow_right:").getUnicode()).queue();
							createCache = true;
						}
						//enable reaction events for the current user and drawn inventory
						if(createCache == true) {
							Hashes.addTempCache("randomshop_gu"+e.getGuild().getId()+"me"+e.getMessageId()+"us"+member_id, new Cache(0, current_page+"_"+last_page+"_"+input));
						}
						Hashes.clearTempCache("randomshop_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
					}
					
					//check if the rss command has been used
					final var rss = Hashes.getTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
					if(rss != null && !UserPrivs.isUserBot(e.getMember()) && rss.getExpiration() - System.currentTimeMillis() > 0) {
						String task = rss.getAdditionalInfo();
						//check if the user wishes to close the rss window
						if(!message.equalsIgnoreCase("exit")) {
							//register a rss feed if selected
							if(task.equals("register") && ((message.startsWith("http") && rss.getAdditionalInfo2().equals("1")) || (message.startsWith("#") && rss.getAdditionalInfo2().equals("2")))) {
								Hashes.clearTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
								RssExecution.registerFeed(e, message, Integer.parseInt(rss.getAdditionalInfo2()));
							}
							//remove a rss feed if selected
							if(task.equals("remove") && message.replaceAll("[0-9]", "").length() == 0) {
								RssExecution.removeFeed(e, Integer.parseInt(message)-1);
							}
							//format a rss feed if selected
							else if(task.equals("format") && message.replaceAll("[0-9]", "").length() == 0) {
								RssExecution.currentFormat(e, Integer.parseInt(message)-1, "rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
							}
							//update the format of an rss feed if format has been selected the step before
							else if(task.contains("updateformat")) {
								Hashes.clearTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
								RssExecution.updateFormat(e, Integer.parseInt(task.replaceAll("[^0-9]", "")), message);
							}
							//do a test print of one selected feed
							else if(task.equals("test") && message.replaceAll("[0-9]", "").length() == 0) {
								RssExecution.runTest(e, Integer.parseInt(message)-1);
							}
							//change the options of a tweet
							else if(task.equals("options") && message.replaceAll("[0-9]", "").length() == 0) {
								RssExecution.changeOptions(e, Integer.parseInt(message)-1, "rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
							}
							//update the hashtag options
							else if(task.equals("options-page")) {
								final String lowCaseMessage = message.toLowerCase();
								if(lowCaseMessage.startsWith("enable") || lowCaseMessage.startsWith("disable") || lowCaseMessage.startsWith("add-child") || lowCaseMessage.startsWith("remove-child"))
									RssExecution.updateOptions(e, Integer.parseInt(rss.getAdditionalInfo2()), "rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
							}
						}
						else {
							EmbedBuilder embed = new EmbedBuilder().setColor(Color.BLUE);
							e.getChannel().sendMessage(embed.setDescription("Rss command cancelled").build()).queue();
						}
					}
					
					//check if the quiz command has been used
					final var quiz = Hashes.getTempCache("quizstarter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
					if(quiz != null && quiz.getExpiration() - System.currentTimeMillis() > 0) {
						String content = quiz.getAdditionalInfo();
						if(e.getMember().getUser().getId().equals(content) && (message.equals("1") || message.equals("2") || message.equals("3"))) {
							//run the quiz in a thread. // retrieve the log channel and quiz channel at the same time and pass them over to the new Thread
							var channels = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log") || f.getChannel_Type().equals("qui")).collect(Collectors.toList());
							var log_channel = channels.parallelStream().filter(f -> f.getChannel_Type().equals("log")).findAny().orElse(null);
							var qui_channel = channels.parallelStream().filter(f -> f.getChannel_Type().equals("qui")).findAny().orElse(null);
							if(log_channel == null)
								log_channel.setChannel_ID(e.getChannel().getIdLong());
							if(qui_channel == null)
								qui_channel.setChannel_ID(e.getChannel().getIdLong());
							e.getChannel().sendMessage("The quiz will run shortly in <#"+qui_channel.getChannel_ID()+">!").queue();
							//execute independent Quiz Thread
							new Thread(new RunQuiz(e, qui_channel.getChannel_ID(), log_channel.getChannel_ID(), Integer.parseInt(message))).start();
							//remove the entry from cache after starting 
							Hashes.clearTempCache("quizstarter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
						}
					}
					
					//check if a quiz session is running
					final var runquiz = Hashes.getTempCache("quiztime"+e.getGuild().getId());
					if(runquiz != null) {
						String content = runquiz.getAdditionalInfo();
						//continue as long a question shouldn't be skipped or the quiz shouldn't be interrupted
						if(!content.equals("skip-question") || !content.equals("interrupt-questions")) {
							//verify that there is a registered quiz channel
							var qui_channel = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("qui")).findAny().orElse(null);
							if(qui_channel != null && qui_channel.getChannel_ID() == e.getChannel().getIdLong() && !UserPrivs.isUserBot(e.getMember())) {
								//check if an administrator or moderator wishes to skip a question or interrupt all questions
								if(UserPrivs.isUserAdmin(e.getMember()) || UserPrivs.isUserMod(e.getMember()) || e.getMember().getUser().getIdLong() == GuildIni.getAdmin(guild_id)) {
									if(message.equals("skip-question") || message.equals("interrupt-questions")) {
										Hashes.addTempCache("quiztime"+e.getGuild().getId(), new Cache(0, message));
									}
								}
								if(!(content.length() == 7) || !(content.length() == 8)) {
									//check if the given answer is the same of one of three provided possible answers
									if(Hashes.getQuiz(Integer.parseInt(content)).getAnswer1().trim().equalsIgnoreCase(message) ||
									   Hashes.getQuiz(Integer.parseInt(content)).getAnswer2().trim().equalsIgnoreCase(message) ||
									   Hashes.getQuiz(Integer.parseInt(content)).getAnswer3().trim().equalsIgnoreCase(message)) {
										//check if this user had won something before during the same quiz session
										Integer hash = Hashes.getQuizWinners(e.getMember());
										if(hash == null) {
											Hashes.addTempCache("quiztime"+e.getGuild().getId(), new Cache(0, e.getMember().getUser().getId()));
										}
									}
								}
							}
						}
					}
				});
				
				//run a separate thread for the ranking system
				executor.execute(() -> {
					//check if the ranking system is enabled and that there's currently no message timeout
					if(guild_settings != null && guild_settings.getRankingState() == true && (Hashes.getCommentedUser(e.getMember().getUser().getId()+"_"+e.getGuild().getId()) == null || guild_settings.getMessageTimeout() == 0)) {
						//retrieve all details from the user
						Rank user_details = RankingSystem.SQLgetWholeRankView(user_id, guild_id);
						if(user_details == null) {
							//if no user details have been found, insert the user into the users table and into the user details table
							if(RankingSystem.SQLInsertUser(user_id, guild_id, e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), guild_settings.getLevelID(), guild_settings.getRankID(), guild_settings.getProfileID(), guild_settings.getIconID()) > 0) {
								if(RankingSystem.SQLInsertUserDetails(user_id, guild_id, 0, 0, guild_settings.getStartCurrency(), 0) > 0) {
									var log_channel = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
									if(log_channel != null) {
										EmbedBuilder success = new EmbedBuilder().setColor(Color.GREEN).setTitle("Table insertion successful!");
										e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(success.setDescription("The user **"+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+"** with the ID number **"+user_id+"** has been successfully inserted into all required ranking system table!").build()).queue();
									}
								}
							}
							else if(RankingSystem.SQLInsertUserDetails(user_id, guild_id, 0, 0, guild_settings.getStartCurrency(), 0) > 0) {
								var log_channel = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
								if(log_channel != null) {
									EmbedBuilder success = new EmbedBuilder().setColor(Color.GREEN).setTitle("Table insertion successful!");
									e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(success.setDescription("The user **"+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+"** with the ID number **"+user_id+"** has been successfully inserted into all required ranking system table!").build()).queue();
								}
							}
						}
						else {
							//retrieve all bots and quiz channels to exclude users from gaining experience points in these channels. also keep bots away from gaining experience points
							var channels = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && (f.getChannel_Type().equals("bot") || f.getChannel_Type().equals("qui"))).collect(Collectors.toList());
							if(!e.getMember().getUser().isBot() && channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null) == null) {
								int roleAssignLevel = 0;
								long role_id = 0;
								//check if there's a ranking role to unlock when the user reaches the next level
								final var ranking_levels = RankingSystem.SQLgetRoles(guild_id).parallelStream().filter(f -> f.getLevel() == (user_details.getLevel()+1)).findAny().orElse(null);
								if(ranking_levels != null) {
									roleAssignLevel = ranking_levels.getLevel();
									role_id = ranking_levels.getRole_ID();
								}
								
								//check if the user has an item to boost the experience points
								int percent_multiplier;
								try {
									percent_multiplier = Integer.parseInt(RankingSystem.SQLExpBoosterExistsInInventory(user_id, guild_id, guild_settings.getThemeID()).replaceAll("[^0-9]*", ""));
								} catch(NumberFormatException nfe) {
									percent_multiplier = 0;
								}
								
								//run method to gain experience points or level up
								RankingThreadExecution.setProgress(e, user_id, guild_id, message, roleAssignLevel, role_id, percent_multiplier, user_details, guild_settings);
							}
						}
					}
				});
				executor.shutdown();
				
				//check if the language filter is enabled for this channel and retrieve all languages
				var filter_lang = Azrael.SQLgetChannel_Filter(channel_id);
				if(filter_lang != null && filter_lang.size() > 0) {
					new Thread(new LanguageFilter(e, filter_lang, allChannels)).start();
					//if url censoring is enabled, also run the url censoring thread
					if(currentChannel != null && currentChannel.getURLCensoring())
						new Thread(new URLFilter(e, null, filter_lang, allChannels)).start();
				}
				//if url censoring is enabled but no language has been applied, use english as default and run the url censoring thread
				else if(currentChannel != null && currentChannel.getURLCensoring()) {
					ArrayList<String> lang = new ArrayList<String>();
					lang.add("eng");
					new Thread(new URLFilter(e, null, filter_lang, allChannels)).start();
				}
				//check if the channel log and cache log is enabled and if one of the two or bot is/are enabled then write message to file or/and log to system cache
				var log = GuildIni.getChannelAndCacheLog(guild_id);
				if((log[0] || log[1]) && !UserPrivs.isUserBot(e.getMember())) {
					StringBuilder image_url = new StringBuilder();
					for(Attachment attch : e.getMessage().getAttachments()) {
						image_url.append((e.getMessage().getContentRaw().length() == 0 && image_url.length() == 0) ? "("+attch.getProxyUrl()+")" : "\n("+attch.getProxyUrl()+")");
					}
					Messages collectedMessage = new Messages();
					collectedMessage.setUserID(user_id);
					collectedMessage.setUsername(e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator());
					collectedMessage.setGuildID(guild_id);
					collectedMessage.setChannelID(channel_id);
					collectedMessage.setChannelName(e.getChannel().getName());
					collectedMessage.setMessage(message+image_url.toString()+"\n");
					collectedMessage.setMessageID(e.getMessageIdLong());
					collectedMessage.setTime(LocalDateTime.now());
					collectedMessage.setIsEdit(false);
					
					if(log[0]) 	FileSetting.appendFile("./message_log/"+e.getChannel().getId()+".txt", "MESSAGE ["+collectedMessage.getTime().toString()+" - "+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+" ("+e.getMember().getUser().getId()+")]: "+collectedMessage.getMessage());
					if(log[1]) {
						ArrayList<Messages> cacheMessage = new ArrayList<Messages>();
						cacheMessage.add(collectedMessage);
						Hashes.addMessagePool(e.getMessageIdLong(), cacheMessage);
					}
				}
				//check if the current user is being watched and that the cache log is enabled
				var watchedMember = Azrael.SQLgetWatchlist(user_id, guild_id);
				var sentMessage = Hashes.getMessagePool(e.getMessageIdLong());
				//if the watched member level equals 2, then print all written messages from that user in a separate channel
				if(watchedMember != null && watchedMember.getLevel() == 2 && sentMessage != null) {
					var cachedMessage = sentMessage.get(0);
					var printMessage = "["+cachedMessage.getTime().toString()+" - "+cachedMessage.getUserName()+"]: "+cachedMessage.getMessage();
					e.getGuild().getTextChannelById(watchedMember.getWatchChannel()).sendMessage(new EmbedBuilder()
						.setTitle("Logged written message due to watching!").setColor(Color.YELLOW)
						.setDescription((printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"...")).build()).queue();
				}
				//print an error if the cache log is not enabled
				else if(watchedMember != null && watchedMember.getLevel() == 2 && sentMessage == null) {
					e.getGuild().getTextChannelById(watchedMember.getWatchChannel()).sendMessage(new EmbedBuilder()
						.setTitle("CacheLog disabled!").setColor(Color.RED)
						.setDescription("Please enable the CacheLog to display messages! Message from "+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+" couldn't be displayed!").build()).queue();
				}
			}).start();
		}
	}
}
