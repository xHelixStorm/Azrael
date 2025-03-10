package de.azrael.commands;

import java.awt.Color;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.constructors.Dailies;
import de.azrael.constructors.InventoryContent;
import de.azrael.constructors.Ranking;
import de.azrael.enums.Channel;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.inventory.DrawDaily;
import de.azrael.sql.Azrael;
import de.azrael.sql.RankingSystem;
import de.azrael.util.Hashes;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * The Daily command sends the user a random defined reward
 * which can be retrieved once a day as long the ranking 
 * system is enabled
 * @author xHelixStorm
 *
 */

public class Daily implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Daily.class);

	@Override
	public boolean called(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.DAILY);
	}

	@Override
	public boolean action(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		//check if the user is spamming the command before it's completed (attempt to retrieve multiple rewards)
		var cache = Hashes.getTempCache("dailyDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
		if(cache == null || cache.getExpiration() - System.currentTimeMillis() <= 0) {
			//set timeout
			Hashes.addTempCache("dailyDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId(), new Cache(3000));
			//retrieve all bot channels
			var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.BOT.getType())).collect(Collectors.toList());
			//execute block only if no bot channel is registered or if the current channel is a bot channel
			if(bot_channels.size() == 0 || bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null) != null) {
				long time_for_daily = 0;
				//check if the daily command can be used again today as long the current time is bigger than the saved time
				var dailiesUsage = RankingSystem.SQLgetDailiesUsage(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
				if(dailiesUsage != null)
					time_for_daily = dailiesUsage.getTime()-System.currentTimeMillis();
				else
					time_for_daily = -1;
				//enter this block when the user is allowed to retrieve a daily again
				if(time_for_daily < 0) {
					//confirm that there are any available rewards to send in private message (e.g. giveaways)
					String cod_reward = RankingSystem.SQLRetrieveGiveawayReward(e.getGuild().getIdLong());
					boolean exclude_cod = false;
					if(cod_reward.length() == 0)
						exclude_cod = true;
					//retrieve guild settings and all registered daily items which a user can win
					de.azrael.constructors.Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
					List<Dailies> daily_items = RankingSystem.SQLgetDailiesAndType(e.getGuild().getIdLong());
					if(daily_items != null) {
						//check that any items are registered for the daily
						if(daily_items.size() > 0) {
							var tot_weight = 0;
							//exclude rewards which get sent in private message, if there is no available reward
							if(exclude_cod == true) {
								if(guild_settings.getRankingState()) {
									//collect the total probability of all items together without special rewards (e.g. currency reward = 5% + special item = 1% = tot_weight = 6%)
									tot_weight = daily_items.parallelStream().filter(i -> !i.getType().equals("cod")).mapToInt(i -> i.getWeight()).sum();
									//remove all special items from the daily_items
									daily_items = daily_items.parallelStream().filter(i -> !i.getType().equals("cod")).collect(Collectors.toList());
								}
							}
							//get the weight of everything if special rewards are included
							else if(guild_settings.getRankingState())
								tot_weight = daily_items.parallelStream().mapToInt(i -> i.getWeight()).sum();
							//eclude exp and cur items, if the ranking system is disabled
							else
								tot_weight = daily_items.parallelStream().filter(i -> !i.getType().equals("cur") && !i.getType().equals("exp")).mapToInt(i -> i.getWeight()).sum();
							if(tot_weight > 0) {
								//get a random number between 1 and the total weight (probability)
								int random = ThreadLocalRandom.current().nextInt(1, tot_weight);
								
								//iterate through the daily items and insert all items into the daily array depending by how big the probability is
								//(e.g. if a currency reward equals 5% then it will be inserted 5 times into the array)
								ArrayList<Dailies> list = new ArrayList<Dailies>();
								for(Dailies daily : daily_items){
									for(int i = 1; i <= daily.getWeight(); i++){
										Dailies this_daily = new Dailies();
										this_daily.setDescription(daily.getDescription());
										this_daily.SetType(daily.getType());
										this_daily.setAction(daily.getAction());
										list.add(this_daily);
									}
								}
								
								if(guild_settings.getDailyId() > 0) {
									//get the index of an array depending on the random number and draw the reward into the screen
									if(e.getGuild().getSelfMember().hasPermission(e.getGuildChannel(), Permission.MESSAGE_ATTACH_FILES) || STATIC.setPermissions(e.getGuild(), e.getChannel().asTextChannel(), EnumSet.of(Permission.MESSAGE_ATTACH_FILES)))
										DrawDaily.draw(e, list.get(random).getDescription(), guild_settings);
									else {
										e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_ATTACH_FILES.getName()).build()).queue();
										logger.error("Permission MESSAGE_ATTACH_FILES required to display the dailies for channel {} in guild {}", e.getChannel().getId(), e.getGuild().getId());
									}
								}
								else {
									e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.DAILY_ERR)).build()).queue();
									return true;
								}
								
								//set the daily reset to next midnight time
								long time = System.currentTimeMillis();
								Timestamp timestamp = new Timestamp(time);
								LocalTime midnight = LocalTime.MIDNIGHT;
								LocalDate today = LocalDate.now();
								LocalDateTime tomorrowMidnight = LocalDateTime.of(today, midnight).plusDays(1);
								Timestamp timestamp2 = Timestamp.valueOf(tomorrowMidnight);
								var editedRows = 0;
								//if it's a currency reward, add it directly to the total currency of the user and update the db
								if(list.get(random).getType().equals("cur")) {
									Ranking user_details = RankingSystem.SQLgetWholeRankView(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
									//look up the amount to update the currency
									final var reward = list.get(random);
									final var itemEffects = RankingSystem.SQLgetItemEffects(e.getGuild().getIdLong());
									if(!itemEffects.isEmpty() && itemEffects.containsKey(reward.getDescription())) {
										user_details.setCurrency(user_details.getCurrency()+itemEffects.get(reward.getDescription()));
										user_details.setLastUpdate(timestamp);
										editedRows = RankingSystem.SQLUpdateCurrency(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), user_details.getCurrency(), user_details.getLastUpdate());
										if(editedRows > 0)
											Hashes.addRanking(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), user_details);
									}
									else {
										e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
										logger.error("Currency couldn't be updated for user {} with the daily reward {} in guild {}", e.getMember().getUser().getId(), reward.getDescription(), e.getGuild().getId());
										RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Currency from daily not updated", reward.getDescription());
									}
								}
								//if it's a experience boost reward, add it to inventory and activate it, if the action is set to 'use'
								else if(list.get(random).getType().equals("exp")) {
									var item_id = RankingSystem.SQLgetSkinshopContentAndType(e.getGuild().getIdLong()).parallelStream().filter(i -> i.getShopDescription().equals(list.get(random).getDescription())).findAny().orElse(null).getItemID();
									if(list.get(random).getAction().equals("keep")) {
										InventoryContent inventory = RankingSystem.SQLgetNumberAndExpirationFromInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), list.get(random).getDescription(), "perm");
										if(inventory != null)
											editedRows = RankingSystem.SQLInsertInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), item_id, timestamp, inventory.getNumber()+1, "perm");
										else
											editedRows = RankingSystem.SQLInsertInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), item_id, timestamp, 1, "perm");
									}
									else if(list.get(random).getAction().equals("use")) {
										InventoryContent inventory = RankingSystem.SQLgetNumberAndExpirationFromInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), list.get(random).getDescription(), "limit");
										try {
											Timestamp timestamp3 = new Timestamp(inventory.getExpiration().getTime()+1000*60*60*24);
											editedRows = RankingSystem.SQLInsertInventoryWithLimit(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), item_id, timestamp, inventory.getNumber()+1, "limit", timestamp3);
										} catch(NullPointerException npe){
											Timestamp timestamp4 = new Timestamp(time+1000*60*60*24);
											editedRows = RankingSystem.SQLInsertInventoryWithLimit(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), item_id, timestamp, 1, "limit", timestamp4);
										}
									}
								}
								//if it's a special reward, send it in private message to the user and print it in the log channel at the same time
								else if(list.get(random).getType().equals("cod")) {
									//log the reward in bot channel and send a private message
									e.getMember().getUser().openPrivateChannel().queue(channel -> {
										channel.sendMessage(STATIC.getTranslation(e.getMember(), Translation.DAILY_REWARD)+cod_reward).queue(success -> {
											EmbedBuilder message = new EmbedBuilder().setColor(Color.getHSBColor(268, 81, 88)).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DAILY));
											STATIC.writeToRemoteChannel(e.getGuild(), message, STATIC.getTranslation(e.getMember(), Translation.DAILY_REWARD_SENT).replaceFirst("\\{\\}", e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()).replace("{}", e.getMember().getUser().getId())+cod_reward, Channel.LOG.getType());
										}, error -> {
											STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED), STATIC.getTranslation(e.getMember(), Translation.DAILY_REWARD_NOT_SENT).replaceFirst("\\{\\}", e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()).replace("{}", e.getMember().getUser().getId())+cod_reward, Channel.LOG.getType());
										});
									});
									
									//mark the code as used
									editedRows = RankingSystem.SQLUpdateUsedOnReward(cod_reward, e.getGuild().getIdLong());
								}
								//If the player was unlucky, send a message that nothing was won
								else if(list.get(random).getType().equals("riv")) {
									EmbedBuilder message = new EmbedBuilder().setColor(Color.getHSBColor(268, 81, 88)).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DAILY));
									e.getChannel().sendMessageEmbeds(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.DAILY_RIVET)).build()).queue();
									//mark as successful
									editedRows = 1;
								}
								//if the inventory has been updated, set the daily for this user and the current day as opened
								if(editedRows > 0) {
									if(RankingSystem.SQLInsertDailiesUsage(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), timestamp, timestamp2) > 0) {
										logger.info("{} received {} out of the Daily command in guild {}", e.getMember().getUser().getId(), list.get(random).getDescription(), e.getGuild().getId());
										RankingSystem.SQLInsertActionLog("low", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Daily retrieved", list.get(random).getDescription());
									}
									else {
										STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)), STATIC.getTranslation(e.getMember(), Translation.DAILY_ERROR_2)+e.getMember().getAsMention(), Channel.LOG.getType());
										logger.error("used dailies from {} couldn't be marked as used in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
										RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Daily retrieval not marked", list.get(random).getDescription());
									}
								}
								else {
									e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.DAILY_ERROR_3)).build()).queue();
									logger.error("{} couldn't be inserted into inventory in guild {}", list.get(random).getDescription(), e.getGuild().getId());
									RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Daily item couldn't be inserted to inventory", "An insert error occurred for the following item "+list.get(random).getDescription());
								}
								list.clear();
							}
							else {
								e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.DAILY_EMPTY)).build()).queue();
							}
						}
						else {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.DAILY_EMPTY)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					}
				}
				//notify the user that he can't use the daily command yet
				else {
					long hours = time_for_daily/1000/60/60;
					long minutes = time_for_daily/1000/60%60;
					e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.DAILY_COOLDOWN).replaceFirst("\\{\\}", ""+hours).replace("{}", ""+minutes)).queue();
				}
			}
			else {
				e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.NOT_BOT_CHANNEL)+STATIC.getChannels(bot_channels)).queue();
			}
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, MessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Daily command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.DAILY.getColumn(), out.toString().trim());
		}
	}

}
