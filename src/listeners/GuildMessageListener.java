package listeners;

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
				
				ExecutorService executor = Executors.newSingleThreadExecutor();
				executor.execute(() -> {
					//execute commands first
					if(e.getMessage().getContentRaw().startsWith(GuildIni.getCommandPrefix(e.getGuild().getIdLong())) && e.getMessage().getAuthor().getId() != e.getJDA().getSelfUser().getId()) {
						var prefixLength = GuildIni.getCommandPrefix(e.getGuild().getIdLong()).length();
						if(!CommandHandler.handleCommand(CommandParser.parser(e.getMessage().getContentRaw().substring(0, prefixLength)+e.getMessage().getContentRaw().substring(prefixLength).toLowerCase(), e, null))) {
							logger.warn("Command {} doesn't exist!", e.getMessage().getContentRaw());
						}
					}
					
					//If the channel doesn't allow any text input but only screenshots, then delete
					if(currentChannel != null && currentChannel.getTxtRemoval() && e.getMessage().getAttachments().size() == 0) {
						Hashes.addTempCache("message-removed_gu"+guild_id+"ch"+channel_id+"us"+user_id, new Cache(10000));
						e.getMessage().delete().reason("Messages not allowed!").queue();
					}
					
					final var warning = Hashes.getTempCache("warnings_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					if(warning != null) {
						SetWarning.performUpdate(e, message, warning, "warnings_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					}
					
					final var filter = Hashes.getTempCache("filter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					if(filter != null) {
						FilterExecution.performAction(e, message, filter);
					}
					
					final var shop = Hashes.getTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					if(shop != null && shop.getExpiration() - System.currentTimeMillis() > 0) {
						if(message.equalsIgnoreCase("exit")) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("Thanks for the visit! See you again!").build()).queue();
							Hashes.clearTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
						}
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
						else if(shop.getAdditionalInfo().matches("(lev|ran|pro|ico|ite)") && !shop.getAdditionalInfo2().contains("%") && !shop.getAdditionalInfo2().contains("$") && !shop.getAdditionalInfo2().contains("#")) {
							if(!message.matches("[^\\d]*") && message.length() <= 9) {
								ShopExecution.displaySingleItem(e, shop.getAdditionalInfo(), shop.getAdditionalInfo2().split("-"), guild_settings, Integer.parseInt(message)-1);
							}
						}
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
						else if(shop.getAdditionalInfo().contains("wea-") && !shop.getAdditionalInfo2().contains("%")) {
							if(!message.matches("[^\\d]") && message.length() <= 9) {
								ShopExecution.displaySingleWeapon(e, shop.getAdditionalInfo(), shop.getAdditionalInfo2().split("-"), guild_settings, Integer.parseInt(message)-1);
							}
						}
						else if(shop.getAdditionalInfo().contains("wea-") && shop.getAdditionalInfo2().contains("%")) {
							if(message.equalsIgnoreCase("purchase")) {
								PurchaseExecution.purchase(e, "wep", shop.getAdditionalInfo2().replaceAll("%", ""), guild_settings);
							}
							else if(message.equalsIgnoreCase("return")) {
								ShopExecution.displayShopWeapons(e, shop.getAdditionalInfo().split("-")[1]);
							}
						}
						else if(shop.getAdditionalInfo().equals("ski") && !shop.getAdditionalInfo2().contains("%")) {
							if(!message.matches("[^\\d]") && message.length() <= 9) {
								ShopExecution.displaySingleSkill(e, guild_settings, shop.getAdditionalInfo2().split("-"), Integer.parseInt(message)-1);
							}
						}
						else if(shop.getAdditionalInfo().equals("ski") && shop.getAdditionalInfo2().contains("%")) {
							if(message.equalsIgnoreCase("purchase")) {
								PurchaseExecution.purchase(e, "ski", shop.getAdditionalInfo2().replaceAll("%", ""), guild_settings);
							}
							else if(message.equalsIgnoreCase("return")) {
								ShopExecution.displaySkills(e, guild_settings);
							}
						}
					}
					
					final var user = Hashes.getTempCache("user_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					if(user != null) {
						UserExecution.performAction(e, message, user, allChannels);
					}
					
					final var inventory_bot = Hashes.getTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
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
						Hashes.clearTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
					}
					
					final var randomshop_bot = Hashes.getTempCache("randomshop_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
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
						Hashes.clearTempCache("randomshop_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
					}
					
					final var rss = Hashes.getTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
					if(rss != null && !UserPrivs.isUserBot(e.getMember().getUser(), guild_id) && rss.getExpiration() - System.currentTimeMillis() > 0) {
						String task = rss.getAdditionalInfo();
						if(!message.equalsIgnoreCase("exit")) {
							if(task.equals("register") && message.startsWith("http")) {
								Hashes.clearTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
								RssExecution.registerFeed(e, message, Integer.parseInt(rss.getAdditionalInfo2()));
							}
							if(task.equals("remove") && message.replaceAll("[0-9]", "").length() == 0) {
								RssExecution.removeFeed(e, Integer.parseInt(message)-1);
							}
							else if(task.equals("format") && message.replaceAll("[0-9]", "").length() == 0) {
								RssExecution.currentFormat(e, Integer.parseInt(message)-1, "rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
							}
							else if(task.contains("updateformat")) {
								Hashes.clearTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
								RssExecution.updateFormat(e, Integer.parseInt(task.replaceAll("[^0-9]", "")), message);
							}
							else if(task.equals("test") && message.replaceAll("[0-9]", "").length() == 0) {
								RssExecution.runTest(e, Integer.parseInt(message)-1);
							}
						}
						else {
							EmbedBuilder embed = new EmbedBuilder().setColor(Color.BLUE);
							e.getChannel().sendMessage(embed.setDescription("Rss command cancelled").build()).queue();
						}
					}
					
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
							//remove the file after starting 
							Hashes.clearTempCache("quizstarter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
						}
					}
					
					final var runquiz = Hashes.getTempCache("quiztime"+e.getGuild().getId());
					if(runquiz != null) {
						String content = runquiz.getAdditionalInfo();
						if(!content.equals("skip-question") || !content.equals("interrupt-questions")) {
							var qui_channel = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("qui")).findAny().orElse(null);
							if(qui_channel != null && qui_channel.getChannel_ID() == e.getChannel().getIdLong() && !UserPrivs.isUserBot(e.getMember().getUser(), guild_id)) {
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
				});
				
				executor.execute(() -> {
					if(guild_settings != null && guild_settings.getRankingState() == true && (Hashes.getCommentedUser(e.getMember().getUser().getId()+"_"+e.getGuild().getId()) == null || guild_settings.getMessageTimeout() == 0)) {
						Rank user_details = RankingSystem.SQLgetWholeRankView(user_id, guild_id);
						if(user_details == null) {
							if(RankingSystem.SQLInsertUser(user_id, guild_id, e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), guild_settings.getLevelID(), guild_settings.getRankID(), guild_settings.getProfileID(), guild_settings.getIconID()) > 0) {
								if(RankingSystem.SQLInsertUserDetails(user_id, guild_id, 0, 0, 50000, 0) > 0) {
									var log_channel = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
									if(log_channel != null) {
										EmbedBuilder success = new EmbedBuilder().setColor(Color.GREEN).setTitle("Table insertion successful!");
										e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(success.setDescription("The user **"+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+"** with the ID number **"+user_id+"** has been successfully inserted into all required ranking system table!").build()).queue();
									}
								}
							}
							else if(RankingSystem.SQLInsertUserDetails(user_id, guild_id, 0, 0, 50000, 0) > 0) {
								var log_channel = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
								if(log_channel != null) {
									EmbedBuilder success = new EmbedBuilder().setColor(Color.GREEN).setTitle("Table insertion successful!");
									e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(success.setDescription("The user **"+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+"** with the ID number **"+user_id+"** has been successfully inserted into all required ranking system table!").build()).queue();
								}
							}
						}
						else {
							var channels = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && (f.getChannel_Type().equals("bot") || f.getChannel_Type().equals("qui"))).collect(Collectors.toList());
							if(!UserPrivs.isUserBot(e.getMember().getUser(), e.getGuild().getIdLong()) && channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null) == null) {
								int roleAssignLevel = 0;
								long role_id = 0;
								final var ranking_levels = RankingSystem.SQLgetRoles(guild_id).parallelStream().filter(f -> f.getLevel() == (user_details.getLevel()+1)).findAny().orElse(null);
								if(ranking_levels != null) {
									roleAssignLevel = ranking_levels.getLevel();
									role_id = ranking_levels.getRole_ID();
								}
								
								int percent_multiplier;
								try {
									percent_multiplier = Integer.parseInt(RankingSystem.SQLExpBoosterExistsInInventory(user_id, guild_id, guild_settings.getThemeID()).replaceAll("[^0-9]*", ""));
								} catch(NumberFormatException nfe) {
									percent_multiplier = 0;
								}
								
								RankingThreadExecution.setProgress(e, user_id, guild_id, message, roleAssignLevel, role_id, percent_multiplier, user_details, guild_settings);
							}
						}
					}
				});
				executor.shutdown();
				
				var filter_lang = Azrael.SQLgetChannel_Filter(channel_id);
				if(filter_lang != null && filter_lang.size() > 0) {
					new Thread(new LanguageFilter(e, filter_lang, allChannels)).start();
					if(currentChannel != null && currentChannel.getURLCensoring())
						new Thread(new URLFilter(e, null, filter_lang, allChannels)).start();
				}
				else if(currentChannel != null && currentChannel.getURLCensoring()) {
					ArrayList<String> lang = new ArrayList<String>();
					lang.add("eng");
					new Thread(new URLFilter(e, null, filter_lang, allChannels)).start();
				}
				var log = GuildIni.getChannelAndCacheLog(guild_id);
				if((log[0] || log[1]) && !UserPrivs.isUserBot(e.getMember().getUser(), guild_id)) {
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
				var watchedMember = Hashes.getWatchlist(guild_id+"-"+user_id);
				var sentMessage = Hashes.getMessagePool(e.getMessageIdLong());
				if(watchedMember != null && watchedMember.getLevel() == 2 && sentMessage != null) {
					var cachedMessage = sentMessage.get(0);
					e.getGuild().getTextChannelById(watchedMember.getWatchChannel()).sendMessage(new EmbedBuilder()
						.setTitle("Logged written message due to watching!").setColor(Color.YELLOW)
						.setDescription("["+cachedMessage.getTime().toString()+" - "+cachedMessage.getUserName()+"]: "+cachedMessage.getMessage()).build()).queue();
				}
				else if(watchedMember != null && watchedMember.getLevel() == 3 && sentMessage == null) {
					e.getGuild().getTextChannelById(watchedMember.getWatchChannel()).sendMessage(new EmbedBuilder()
						.setTitle("CacheLog disabled!").setColor(Color.RED)
						.setDescription("Please enable the CacheLog to display messages! Message from "+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+" couldn't be displayed!").build()).queue();
				}
			}).start();
		}
	}
}
