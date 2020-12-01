package listeners;

import java.awt.Color;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiManager;

import commandsContainer.ClanExecution;
import commandsContainer.FilterExecution;
import commandsContainer.GoogleSpreadsheetsExecution;
import commandsContainer.JoinExecution;
import commandsContainer.PurchaseExecution;
import commandsContainer.RegisterRole;
import commandsContainer.RoomExecution;
import commandsContainer.SubscribeExecution;
import commandsContainer.SetWarning;
import commandsContainer.ShopExecution;
import commandsContainer.UserExecution;
import commandsContainer.WriteEditExecution;
import constructors.Cache;
import constructors.Guilds;
import constructors.Messages;
import constructors.Ranking;
import core.CommandHandler;
import core.CommandParser;
import core.Hashes;
import core.UserPrivs;
import enums.Channel;
import enums.GoogleEvent;
import enums.Translation;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
import filter.LanguageFilter;
import filter.URLFilter;
import google.GoogleUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rankingSystem.RankingThreadExecution;
import sql.RankingSystem;
import sql.Azrael;
import sql.Competitive;
import threads.RunQuiz;
import util.STATIC;

/**
 * This class gets executed when a message is sent
 * 
 * This class contains various things such as the launcher for
 * regular, url and message censoring, applying reactions on 
 * images, the execution of commands and the following of commands
 * through various steps.
 * @author xHelixStorm
 * 
 */

public class GuildMessageListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(GuildMessageListener.class);
	
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
						if(!CommandHandler.handleCommand(CommandParser.parser(e.getMessage().getContentRaw().substring(0, prefixLength)+e.getMessage().getContentRaw().substring(prefixLength), e, null))) {
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
					
					//for some cases do additional actions after registering a role
					final var roleRegistered = Hashes.getTempCache("register_role_gu"+guild_id+"ch"+channel_id+"us"+user_id);
					if(roleRegistered != null && roleRegistered.getExpiration() - System.currentTimeMillis() > 0 && !e.getMember().getUser().isBot()) {
						if(roleRegistered.getAdditionalInfo().equals("ver")) {
							RegisterRole.assignVerifiedRoleToMembers(e, roleRegistered);
						}
					}
					
					//if the shop command has been used, help the user to navigate through the shop easily
					final var shop = Hashes.getTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					if(shop != null && shop.getExpiration() - System.currentTimeMillis() > 0) {
						//verify if the user whishes to close the shop
						if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_EXIT))) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SHOP_TITLE_EXIT)).build()).queue();
							Hashes.clearTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
						}
						//check if the user has decided for a specific category
						else if(shop.getAdditionalInfo().length() == 0) {
							if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LEVEL_UPS))) {
								ShopExecution.displayShop(e, "lev", guild_settings.getLevelDescription());
							}
							else if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_RANKS))) {
								ShopExecution.displayShop(e, "ran", guild_settings.getRankDescription());
							}
							else if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PROFILES))) {
								ShopExecution.displayShop(e, "pro", guild_settings.getProfileDescription());
							}
							else if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ICONS))) {
								ShopExecution.displayShop(e, "ico", guild_settings.getIconDescription());
							}
							else if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ITEMS))) {
								ShopExecution.displayShop(e, "ite", "");
							}
							else if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_WEAPONS))) {
								ShopExecution.displayWeaponCategories(e, guild_settings.getThemeID());
							}
							else if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_SKILLS))) {
								ShopExecution.displaySkills(e, guild_settings);
							}
						}
						//display details of the currently selected item or skin
						else if(shop.getAdditionalInfo().matches("(lev|ran|pro|ico|ite)") && !shop.getAdditionalInfo2().contains("%") && !shop.getAdditionalInfo2().contains("$") && !shop.getAdditionalInfo2().contains("#")) {
							if(message.replaceAll("[0-9]*", "").trim().length() == 0 && message.length() <= 9) {
								ShopExecution.displaySingleItem(e, shop.getAdditionalInfo(), shop.getAdditionalInfo2().split("-"), guild_settings, Integer.parseInt(message)-1);
							}
						}
						//verify if the user whishes to purchase, sell or return on the currently selected item
						else if(shop.getAdditionalInfo().matches("(lev|ran|pro|ico|ite)") && (shop.getAdditionalInfo2().contains("%") || shop.getAdditionalInfo2().contains("$") || shop.getAdditionalInfo2().contains("#"))) {
							if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PURCHASE)) && shop.getAdditionalInfo2().contains("%")) {
								PurchaseExecution.purchase(e, shop.getAdditionalInfo(), shop.getAdditionalInfo2().replaceAll("%", ""), guild_settings);
							}
							else if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_SELL)) && shop.getAdditionalInfo2().contains("#")) {
								PurchaseExecution.sell(e, shop.getAdditionalInfo(), shop.getAdditionalInfo2().replaceAll("#", ""), guild_settings);
							}
							else if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_RETURN))) {
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
							if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PURCHASE))) {
								PurchaseExecution.purchase(e, "wep", shop.getAdditionalInfo2().replaceAll("%", ""), guild_settings);
							}
							else if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_RETURN))) {
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
							if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PURCHASE))) {
								PurchaseExecution.purchase(e, "ski", shop.getAdditionalInfo2().replaceAll("%", ""), guild_settings);
							}
							else if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_RETURN))) {
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
					if(inventory_bot != null && e.getGuild().getSelfMember().getIdLong() == e.getMember().getUser().getIdLong() && inventory_bot.getExpiration() - System.currentTimeMillis() > 0) {
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
					
					//include vote up and vote down reactions, if it's a vote channel
					if(currentChannel != null && currentChannel.getChannel_Type() != null && currentChannel.getChannel_Type().equals(Channel.VOT.getType()) && e.getGuild().getSelfMember().getIdLong() != e.getMember().getUser().getIdLong()) {
						if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_ADD_REACTION) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_ADD_REACTION))) {
							if(e.getMessage().getType() != MessageType.CHANNEL_PINNED_ADD) {
								e.getMessage().addReaction(EmojiManager.getForAlias(":thumbsup:").getUnicode()).queue();
								e.getMessage().addReaction(EmojiManager.getForAlias(":thumbsdown:").getUnicode()).queue();
								
								//Run google service, if enabled
								if(GuildIni.getGoogleFunctionalitiesEnabled(guild_id) && GuildIni.getGoogleSpreadsheetsEnabled(guild_id)) {
									GoogleUtils.handleSpreadsheetRequest(Azrael.SQLgetGoogleFilesAndEvent(guild_id, 2, GoogleEvent.VOTE.id, e.getChannel().getId()), e.getGuild(), e.getChannel().getId(), ""+user_id, new Timestamp(System.currentTimeMillis()), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), e.getMember().getEffectiveName(), null, null, null, null, null, "VOTE", null, null, null, null, null, e.getMessageIdLong(), e.getMessage().getContentRaw(), null, 0, 0, GoogleEvent.VOTE.id);
								}
							}
						}
						else {
							logger.error("MESSAGE_ADD_REACTION permission missing for votes in guild {}", e.getGuild().getId());
							STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_ADD_REACTION.getName())+e.getChannel().getName(), Channel.LOG.getType());
						}
					}
					
					//check if the randomshop command has been used
					final var randomshop_bot = Hashes.getTempCache("randomshop_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
					if(randomshop_bot != null && e.getGuild().getSelfMember().getIdLong() == e.getMember().getUser().getIdLong() && randomshop_bot.getExpiration() - System.currentTimeMillis() > 0) {
						if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_ADD_REACTION) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_ADD_REACTION))) {
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
						}
						else {
							logger.warn("MESSAGE_ADD_REACTION permission required to to browse through pages by reacting for channel {} in guild {}", e.getChannel().getId(), e.getGuild().getId());
						}
						Hashes.clearTempCache("randomshop_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
					}
					
					//check if the rss command has been used
					final var rss = Hashes.getTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
					if(rss != null && !e.getMember().getUser().isBot() && !UserPrivs.isUserBot(e.getMember()) && rss.getExpiration() - System.currentTimeMillis() > 0) {
						String task = rss.getAdditionalInfo();
						//check if the user wishes to close the rss window
						if(!message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_EXIT))) {
							//register a rss feed if selected
							if(task.equals("register") && ((message.startsWith("http") && rss.getAdditionalInfo2().equals("1")) || (message.startsWith("#") && rss.getAdditionalInfo2().equals("2")))) {
								Hashes.clearTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
								SubscribeExecution.registerFeed(e, message, Integer.parseInt(rss.getAdditionalInfo2()));
							}
							//remove a rss feed if selected
							if(task.equals("remove") && message.replaceAll("[0-9]*", "").length() == 0) {
								SubscribeExecution.removeFeed(e, Integer.parseInt(message)-1);
							}
							//format a rss feed if selected
							else if(task.equals("format") && message.replaceAll("[0-9]*", "").length() == 0) {
								SubscribeExecution.currentFormat(e, Integer.parseInt(message)-1, "rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
							}
							//update the format of an rss feed if format has been selected the step before
							else if(task.contains("updateformat")) {
								Hashes.clearTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
								SubscribeExecution.updateFormat(e, Integer.parseInt(task.replaceAll("[^0-9]", "")), message);
							}
							//do a test print of one selected feed
							else if(task.equals("test") && message.replaceAll("[0-9]*", "").length() == 0) {
								SubscribeExecution.runTest(e, Integer.parseInt(message)-1);
							}
							//change the options of a tweet
							else if(task.equals("options") && message.replaceAll("[0-9]*", "").length() == 0) {
								SubscribeExecution.changeOptions(e, Integer.parseInt(message)-1, "rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
							}
							//update the hashtag options
							else if(task.equals("options-page")) {
								final String lowCaseMessage = message.toLowerCase();
								if(lowCaseMessage.startsWith(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE)) || lowCaseMessage.startsWith(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE)) || lowCaseMessage.startsWith(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_CHILD)) || lowCaseMessage.startsWith(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE_CHILD)))
									SubscribeExecution.updateOptions(e, Integer.parseInt(rss.getAdditionalInfo2()), "rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
							}
							//set a text channel besides the default one
							else if(task.equals("channel") && message.replaceAll("[0-9]*", "").length() == 0) {
								SubscribeExecution.setChannel(e, Integer.parseInt(message)-1, "rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
							}
							//register the alternative text channel
							else if(task.equals("set-channel")) {
								SubscribeExecution.updateAlternativeChannel(e, Integer.parseInt(rss.getAdditionalInfo2()), "rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
							}
						}
						else {
							EmbedBuilder embed = new EmbedBuilder().setColor(Color.BLUE);
							e.getChannel().sendMessage(embed.setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_EXIT)).build()).queue();
						}
					}
					
					//check if the quiz command has been used
					final var quiz = Hashes.getTempCache("quizstarter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
					if(quiz != null && quiz.getExpiration() - System.currentTimeMillis() > 0) {
						String content = quiz.getAdditionalInfo();
						if(e.getMember().getUser().getId().equals(content) && (message.equals("1") || message.equals("2") || message.equals("3"))) {
							//run the quiz in a thread. // retrieve the log channel and quiz channel at the same time and pass them over to the new Thread
							var channels = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.LOG.getType()) || f.getChannel_Type().equals(Channel.QUI.getType())).collect(Collectors.toList());
							var log_channel = channels.parallelStream().filter(f -> f.getChannel_Type().equals(Channel.LOG.getType())).findAny().orElse(null);
							var qui_channel = channels.parallelStream().filter(f -> f.getChannel_Type().equals(Channel.QUI.getType())).findAny().orElse(null);
							long logChannel = 0;
							long quiChannel = 0;
							if(log_channel != null)
								logChannel = log_channel.getChannel_ID();
							else
								logChannel = e.getChannel().getIdLong();
							if(qui_channel != null)
								quiChannel = qui_channel.getChannel_ID();
							else
								quiChannel = e.getChannel().getIdLong();
							e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.QUIZ_START_SHORTLY).replace("{}", ""+quiChannel)).queue();
							//execute independent Quiz Thread
							new Thread(new RunQuiz(e, quiChannel, logChannel, Integer.parseInt(message))).start();
							//remove the entry from cache after starting 
							Hashes.clearTempCache("quizstarter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
						}
					}
					
					//check if a quiz session is running
					final var runquiz = Hashes.getTempCache("quiztime"+e.getGuild().getId());
					if(runquiz != null) {
						String content = runquiz.getAdditionalInfo();
						//continue as long a question shouldn't be skipped or the quiz shouldn't be interrupted
						if(!content.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_SKIP_QUESTION)) || !content.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_INTERRUPT_QUESTIONS))) {
							//verify that there is a registered quiz channel
							var qui_channel = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.QUI.getType())).findAny().orElse(null);
							if(qui_channel != null && qui_channel.getChannel_ID() == e.getChannel().getIdLong() && !e.getMember().getUser().isBot() && !UserPrivs.isUserBot(e.getMember())) {
								//check if an administrator or moderator wishes to skip a question or interrupt all questions
								if(UserPrivs.isUserAdmin(e.getMember()) || UserPrivs.isUserMod(e.getMember()) || e.getMember().getUser().getIdLong() == GuildIni.getAdmin(guild_id)) {
									if(message.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_SKIP_QUESTION)) || message.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_INTERRUPT_QUESTIONS))) {
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
					
					//check if the google command has been used
					final var google = Hashes.getTempCache("google_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					if(google != null && google.getExpiration() - System.currentTimeMillis() > 0) {
						final String key = "google_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId();
						final String lcMessage = message.toLowerCase();
						if(!lcMessage.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_EXIT))) {
							//actions for google docs
							if(google.getAdditionalInfo().equals("docs")) {
								//TODO: add google docs logic
							}
							
							//actions for google spreadsheets
							else if(google.getAdditionalInfo().equals("spreadsheets")) {
								GoogleSpreadsheetsExecution.runTask(e, key);
							}
							else if(google.getAdditionalInfo().equals("spreadsheets-selection")) {
								if(lcMessage.startsWith(STATIC.getTranslation(e.getMember(), Translation.PARAM_CREATE)))
									GoogleSpreadsheetsExecution.create(e, (lcMessage.length() > 7 ? message.substring(STATIC.getTranslation(e.getMember(), Translation.PARAM_CREATE).length()+1) : null), key);
								else if(lcMessage.startsWith(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD)))
									GoogleSpreadsheetsExecution.add(e, (lcMessage.length() > 4 ? message.substring(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD).length()+1) : null), key);
								else if(lcMessage.startsWith(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE)))
									GoogleSpreadsheetsExecution.remove(e, (lcMessage.length() > 7 ? message.substring(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE).length()+1) : null), key);
								else if(lcMessage.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_EVENTS)))
									GoogleSpreadsheetsExecution.events(e, key);
								else if(lcMessage.startsWith(STATIC.getTranslation(e.getMember(), Translation.PARAM_SHEET)))
									GoogleSpreadsheetsExecution.sheet(e, key);
								else if(lcMessage.startsWith(STATIC.getTranslation(e.getMember(), Translation.PARAM_MAP)))
									GoogleSpreadsheetsExecution.map(e, key);
								else if(lcMessage.startsWith(STATIC.getTranslation(e.getMember(), Translation.PARAM_RESTRICT)))
									GoogleSpreadsheetsExecution.restrict(e, key);
							}
							else if(google.getAdditionalInfo().equals("spreadsheets-events") && lcMessage.matches("[\\d]*")) {
								GoogleSpreadsheetsExecution.eventsFileSelection(e, Integer.parseInt(message)-1, key);
							}
							else if(google.getAdditionalInfo().equals("spreadsheets-events-update") && (lcMessage.startsWith(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD)) || lcMessage.startsWith(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE)))) {
								GoogleSpreadsheetsExecution.eventsFileHandler(e, lcMessage, google.getAdditionalInfo2(), key);
							}
							else if(google.getAdditionalInfo().equals("spreadsheets-sheet") && lcMessage.matches("[\\d]*")) {
								GoogleSpreadsheetsExecution.sheetSelection(e, Integer.parseInt(message)-1, key);
							}
							else if(google.getAdditionalInfo().equals("spreadsheets-sheet-events")) {
								GoogleSpreadsheetsExecution.sheetEvents(e, google.getAdditionalInfo2(), message.toUpperCase(), key);
							}
							else if(google.getAdditionalInfo().equals("spreadsheets-sheet-update")) {
								GoogleSpreadsheetsExecution.sheetUpdate(e, google.getAdditionalInfo2(), google.getAdditionalInfo3(), message, key);
							}
							else if(google.getAdditionalInfo().equals("spreadsheets-map") && lcMessage.matches("[\\d]*")) {
								GoogleSpreadsheetsExecution.mapSelection(e, Integer.parseInt(message)-1, key);
							}
							else if(google.getAdditionalInfo().equals("spreadsheets-map-events")) {
								GoogleSpreadsheetsExecution.mapEvents(e, google.getAdditionalInfo2(), message.toUpperCase(), key);
							}
							else if(google.getAdditionalInfo().equals("spreadsheets-map-update")) {
								GoogleSpreadsheetsExecution.mapUpdate(e, google.getAdditionalInfo2(), Integer.parseInt(google.getAdditionalInfo3()), message, key);
							}
							else if(google.getAdditionalInfo().equals("spreadsheets-restrict") && lcMessage.matches("[\\d]*")) {
								GoogleSpreadsheetsExecution.restrictSelection(e, Integer.parseInt(message)-1, key);
							}
							else if(google.getAdditionalInfo().equals("spreadsheets-restrict-events")) {
								GoogleSpreadsheetsExecution.restrictEvents(e, google.getAdditionalInfo2(), message.toUpperCase(), key);
							}
							else if(google.getAdditionalInfo().equals("spreadsheets-restrict-update")) {
								GoogleSpreadsheetsExecution.restrictUpdate(e, google.getAdditionalInfo2(), google.getAdditionalInfo3(), message, key);
							}
							
							//actions for google drive
							else if(google.getAdditionalInfo().equals("drive")) {
								//TODO: add google drive logic
							}
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_EXIT)).build()).queue();
							//remove the google command from cache
							Hashes.clearTempCache(key);
						}
					}
					
					final var writeEdit = Hashes.getTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					if(writeEdit != null && writeEdit.getExpiration() - System.currentTimeMillis() > 0) {
						if(writeEdit.getAdditionalInfo().equals("W")) {
							WriteEditExecution.writeHelp(e, writeEdit);
						}
						else if(writeEdit.getAdditionalInfo().equals("WE")) {
							WriteEditExecution.runWrite(e, writeEdit, message);
						}
						else if(writeEdit.getAdditionalInfo().equals("EE")) {
							WriteEditExecution.runEdit(e, writeEdit, message);
						}
						else if(writeEdit.getAdditionalInfo().equals("RA")) {
							WriteEditExecution.reactionAddHelp(e, writeEdit);
						}
						else if(writeEdit.getAdditionalInfo().equals("RA1")) {
							WriteEditExecution.reactionAnswer(e, writeEdit, message.toLowerCase());
						}
						else if(writeEdit.getAdditionalInfo().equals("RA2")) {
							WriteEditExecution.reactionBindRole(e, writeEdit, message);
						}
						else if(writeEdit.getAdditionalInfo().equals("RC")) {
							WriteEditExecution.runClearReactions(e, writeEdit);
						}
					}
					
					final var userProfile = Hashes.getTempCache("userProfile_gu"+guild_id+"ch"+channel_id+"us"+user_id);
					if(userProfile != null && !message.startsWith(GuildIni.getCommandPrefix(guild_id)) && userProfile.getExpiration() - System.currentTimeMillis() > 0) {
						if(userProfile.getAdditionalInfo().equals("name")) {
							JoinExecution.registerName(e, userProfile);
						}
						else if(userProfile.getAdditionalInfo().equals("server")) {
							JoinExecution.registerServer(e, userProfile);
						}
					}
					
					final var clan = Hashes.getTempCache("clan_gu"+guild_id+"ch"+channel_id+"us"+user_id);
					if(clan != null && !e.getMember().getUser().isBot() && clan.getExpiration() - System.currentTimeMillis() > 0) {
						String [] args = message.split(" ");
						if(clan.getAdditionalInfo().length() == 0) {
							//user isn't inside a clan
							if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_SEARCH))) {
								ClanExecution.search(e, args, clan);
							}
							else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_APPLY))) {
								ClanExecution.apply(e, args, clan);
							}
							else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CREATE))) {
								ClanExecution.create(e, args, clan);
							}
						}
						else {
							//user is inside a clan
							final int memberLevel = Competitive.SQLgetClanMemberLevel(user_id, guild_id);
							if(memberLevel == 1) {
								//commands for a regular member
								if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_SEARCH))) {
									ClanExecution.search(e, args, clan);
								}
								if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_MEMBERS))) {
									ClanExecution.members(e, clan);
								}
								else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LEAVE))) {
									ClanExecution.leave(e, args, clan);
								}
							}
							else if(memberLevel == 2) {
								//commands for a staff member
								if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_SEARCH))) {
									ClanExecution.search(e, args, clan);
								}
								if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_MEMBERS))) {
									ClanExecution.members(e, clan);
								}
								else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LEAVE))) {
									ClanExecution.leave(e, args, clan);
								}
								else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_KICK))) {
									ClanExecution.kick(e, args, memberLevel, clan);
								}
								else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_INVITE))) {
									ClanExecution.invite(e, args, clan);
								}
							}
							else if(memberLevel == 3) {
								//commands for the owner
								if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_SEARCH))) {
									ClanExecution.search(e, args, clan);
								}
								if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_MEMBERS))) {
									ClanExecution.members(e, clan);
								}
								else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_KICK))) {
									ClanExecution.kick(e, args, memberLevel, clan);
								}
								else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_INVITE))) {
									ClanExecution.invite(e, args, clan);
								}
								else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PROMOTE))) {
									ClanExecution.promote(e, args, clan);
								}
								else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ICON))) {
									ClanExecution.icon(e, args, clan);
								}
								else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DELEGATE))) {
									ClanExecution.delegate(e, args, clan);
								}
								else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISBAND))) {
									ClanExecution.disband(e, clan);
								}
							}
						}
					}
					else if(clan != null && userProfile.getExpiration() - System.currentTimeMillis() < 0) {
						Hashes.clearTempCache("clan_gu"+guild_id+"ch"+channel_id+"us"+user_id);
					}
					
					final var room = Hashes.getTempCache("room_gu"+guild_id+"ch"+channel_id+"us"+user_id);
					if(room != null && room.getExpiration()-System.currentTimeMillis() > 0) {
						String [] args = e.getMessage().getContentRaw().split(" ");
						switch(room.getAdditionalInfo()) {
							case "1" -> {
								if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CLOSE))) {
									RoomExecution.runClose(e, Integer.parseInt(room.getAdditionalInfo2()));
								}
							}
							case "2" -> {
								if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CLOSE))) {
									RoomExecution.runClose(e, Integer.parseInt(room.getAdditionalInfo2()));
								}
								else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_WINNER))) {
									RoomExecution.runWinnerHelp(e, room);
								}
								else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_WINNER))) {
									RoomExecution.runWinner(e, args, room, (room.getAdditionalInfo3().equals("1") ? true : false));
								}
							}
							case "3" -> {
								if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REOPEN))) {
									RoomExecution.runReopen(e, Integer.parseInt(room.getAdditionalInfo2()), room, (room.getAdditionalInfo3().equals("1") ? true : false));
								}
							}
						}
					}
					else {
						Hashes.clearTempCache("room_gu"+guild_id+"ch"+channel_id+"us"+user_id);
					}
				});
				
				//run a separate thread for the ranking system
				executor.execute(() -> {
					//check if the ranking system is enabled and that there's currently no message timeout
					if(guild_settings != null && guild_settings.getRankingState() == true && (Hashes.getCommentedUser(e.getMember().getUser().getId()+"_"+e.getGuild().getId()) == null || guild_settings.getMessageTimeout() == 0)) {
						//retrieve all details from the user
						Ranking user_details = RankingSystem.SQLgetWholeRankView(user_id, guild_id);
						if(user_details == null) {
							//if no user details have been found, insert the user into the users table and into the user details table
							if(RankingSystem.SQLInsertUser(user_id, guild_id, e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), guild_settings.getLevelID(), guild_settings.getRankID(), guild_settings.getProfileID(), guild_settings.getIconID()) > 0) {
								RankingSystem.SQLInsertUserDetails(user_id, guild_id, 0, 0, guild_settings.getStartCurrency(), 0);
							}
							else {
								RankingSystem.SQLInsertUserDetails(user_id, guild_id, 0, 0, guild_settings.getStartCurrency(), 0);
							}
						}
						else {
							//exclude bot and quiz channels for experience gain
							if(!e.getMember().getUser().isBot() && (currentChannel == null || currentChannel.getChannel_Type() == null || (currentChannel.getChannel_Type() != null && !currentChannel.getChannel_Type().equals(Channel.BOT.getType()) && !currentChannel.getChannel_Type().equals(Channel.QUI.getType())))) {
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
					if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_MANAGE) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_MANAGE))) {
						new Thread(new LanguageFilter(e, filter_lang, allChannels)).start();
						//if url censoring is enabled, also run the url censoring thread
						if(currentChannel != null && currentChannel.getURLCensoring())
							new Thread(new URLFilter(e, null, filter_lang, allChannels)).start();
					}
					else {
						STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_WRITE.getName()+" and "+Permission.MESSAGE_MANAGE.getName())+e.getChannel().getName(), Channel.LOG.getType());
						logger.error("MESSAGE_WRITE and MESSAGE_MANAGE permissions required for text channel {} in guild {} to censor messages", e.getChannel().getId(), e.getGuild().getId());
					}
				}
				//if url censoring is enabled but no language has been applied, use english as default and run the url censoring thread
				else if(currentChannel != null && currentChannel.getURLCensoring()) {
					if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_MANAGE) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_MANAGE))) {
						ArrayList<String> lang = new ArrayList<String>();
						lang.add("eng");
						new Thread(new URLFilter(e, null, filter_lang, allChannels)).start();
					}
					else {
						STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_WRITE.getName()+" and "+Permission.MESSAGE_MANAGE.getName())+e.getChannel().getName(), Channel.LOG.getType());
						logger.error("MESSAGE_WRITE and MESSAGE_MANAGE permissions required for text channel {} in guild {} to censor messages", e.getChannel().getId(), e.getGuild().getId());
					}
				}
				//check if the channel log and cache log is enabled and if one of the two or bot is/are enabled then write message to file or/and log to system cache
				var log = GuildIni.getChannelAndCacheLog(guild_id);
				if((log[0] || log[1]) && !e.getMember().getUser().isBot() && !UserPrivs.isUserBot(e.getMember())) {
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
					collectedMessage.setTime(ZonedDateTime.now());
					collectedMessage.setIsEdit(false);
					
					if(log[0]) 	FileSetting.appendFile("./message_log/"+e.getChannel().getId()+".txt", "MESSAGE ["+collectedMessage.getTime().toString()+" - "+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+" ("+e.getMember().getUser().getId()+")]: "+collectedMessage.getMessage());
					if(log[1]) {
						ArrayList<Messages> cacheMessage = new ArrayList<Messages>();
						cacheMessage.add(collectedMessage);
						Hashes.addMessagePool(e.getGuild().getIdLong(), e.getMessageIdLong(), cacheMessage);
					}
				}
				//check if the current user is being watched and that the cache log is enabled
				var watchedMember = Azrael.SQLgetWatchlist(user_id, guild_id);
				var sentMessage = Hashes.getMessagePool(e.getGuild().getIdLong(), e.getMessageIdLong());
				//if the watched member level equals 2, then print all written messages from that user in a separate channel
				if(watchedMember != null && watchedMember.getLevel() == 2 && sentMessage != null) {
					TextChannel textChannel = e.getGuild().getTextChannelById(watchedMember.getWatchChannel());
					if(e.getGuild().getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS))) {
						var cachedMessage = sentMessage.get(0);
						var printMessage = cachedMessage.getMessage();
						textChannel.sendMessage(new EmbedBuilder()
							.setAuthor(cachedMessage.getUserName()+" ("+cachedMessage.getUserID()+")")
							.setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_WATCH)).setColor(Color.WHITE)
							.setDescription((printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"...")).build()).queue();
					}
					else {
						STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_WRITE.getName()+" and "+Permission.MESSAGE_EMBED_LINKS.getName())+e.getChannel().getAsMention(), Channel.LOG.getType());
						logger.error("MESSAGE_WRITE and MESSAGE_EMBED_LINKS permissions required for text channel {} in guild {} to log messages of watched users", e.getChannel().getId(), e.getGuild().getId());
					}
				}
				//print an error if the cache log is not enabled
				else if(watchedMember != null && watchedMember.getLevel() == 2 && sentMessage == null) {
					TextChannel textChannel = e.getGuild().getTextChannelById(watchedMember.getWatchChannel());
					if(e.getGuild().getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS))) {
						textChannel.sendMessage(new EmbedBuilder()
								.setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED)
								.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.MESSAGE_WATCH_ERR).replace("{}", e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator())).build()).queue();
					}
					else {
						STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_WRITE.getName()+" and "+Permission.MESSAGE_EMBED_LINKS.getName())+e.getChannel().getAsMention(), Channel.LOG.getType());
						logger.error("MESSAGE_WRITE and MESSAGE_EMBED_LINKS permissions required for text channel {} in guild {} to log messages of watched users", e.getChannel().getId(), e.getGuild().getId());
					}
				}
				
				//Run google service, if enabled
				if(!e.getMember().getUser().isBot() && GuildIni.getGoogleFunctionalitiesEnabled(guild_id) && GuildIni.getGoogleSpreadsheetsEnabled(guild_id)) {
					final String [] array = Azrael.SQLgetGoogleFilesAndEvent(guild_id, 2, GoogleEvent.COMMENT.id, e.getChannel().getId());
					if(array != null && !array[0].equals("empty")) {
						//log low priority messages to google spreadsheets
						if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY))) {
							e.getChannel().retrieveMessageById(e.getMessageId()).queueAfter(10, TimeUnit.SECONDS, m -> {
								StringBuilder urls = new StringBuilder();
								for(final var attachment : e.getMessage().getAttachments()) {
									urls.append(attachment.getProxyUrl()+"\n");
								}
								GoogleUtils.handleSpreadsheetRequest(array, e.getGuild(), e.getChannel().getId(), ""+user_id, new Timestamp(System.currentTimeMillis()), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), e.getMember().getEffectiveName(), null, null, null, null, null, "COMMENT", null, null, null, null, null, e.getMessageIdLong(), e.getMessage().getContentRaw(), urls.toString().trim(), 0, 0, GoogleEvent.COMMENT.id);
							}, err -> {
								//message was removed
							});
						}
					}
				}
			}).start();
		}
	}
}
