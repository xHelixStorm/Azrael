package commands;

/**
 * The Daily command sends the user a random defined reward
 * which can be retrieved once a day as long the ranking 
 * system is enabled
 */

import java.awt.Color;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import constructors.Dailies;
import constructors.InventoryContent;
import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import inventory.DrawDaily;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;
import sql.Azrael;
import util.STATIC;

public class Daily implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Daily.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getDailyCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getDailyLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		//check if the user is spamming the command before it's completed (attempt to retrieve multiple rewards)
		var cache = Hashes.getTempCache("dailyDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
		if(cache == null || cache.getExpiration() - System.currentTimeMillis() <= 0) {
			//set timeout
			Hashes.addTempCache("dailyDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId(), new Cache(3000));
			//retrieve all bot channels
			var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("bot")).collect(Collectors.toList());
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
				if(time_for_daily < 0){
					//confirm that there are any available rewards to send in private message (e.g. giveaways)
					String cod_reward = RankingSystem.SQLRetrieveGiveawayReward(e.getGuild().getIdLong());
					boolean exclude_cod = false;
					if(cod_reward.length() == 0)
						exclude_cod = true;
					//retrieve guild settings and all registered daily items which a user can win
					constructors.Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
					List<Dailies> daily_items = RankingSystem.SQLgetDailiesAndType(e.getGuild().getIdLong(), guild_settings.getThemeID());
					var tot_weight = 0;
					//exclude rewards which get sent in private message, if there is no available reward
					if(exclude_cod == true) {
						//collect the total probability of all items together without special rewards (e.g. currency reward = 5% + special item = 1% = tot_weight = 6%)
						tot_weight = daily_items.parallelStream().filter(i -> !i.getType().equals("cod")).mapToInt(i -> i.getWeight()).sum();
						//remove all special items from the daily_items
						daily_items = daily_items.parallelStream().filter(i -> !i.getType().equals("cod")).collect(Collectors.toList());
					}
					//get the weight of everything if special rewards are included
					else
						tot_weight = daily_items.parallelStream().mapToInt(i -> i.getWeight()).sum();
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
					//get the index of an array depending on the random number and draw the reward into the screen
					DrawDaily.draw(e, list.get(random).getDescription(), guild_settings);
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
						constructors.Rank user_details = RankingSystem.SQLgetWholeRankView(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
						user_details.setCurrency(user_details.getCurrency()+Long.parseLong(list.get(random).getDescription().replaceAll("[^0-9]*", "")));
						editedRows = RankingSystem.SQLUpdateCurrency(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), user_details.getCurrency());
						if(editedRows > 0)
							Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getIdLong(), user_details);
					}
					//if it's a experience boost reward, add it to inventory and activate it, if the action is set to 'use'
					else if(list.get(random).getType().equals("exp")) {
						var item_id = RankingSystem.SQLgetSkinshopContentAndType(e.getGuild().getIdLong(), guild_settings.getThemeID()).parallelStream().filter(i -> i.getShopDescription().equals(list.get(random).getDescription())).findAny().orElse(null).getItemID();
						if(list.get(random).getAction().equals("keep")) {
							InventoryContent inventory = RankingSystem.SQLgetNumberAndExpirationFromInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), list.get(random).getDescription(), "perm", guild_settings.getThemeID());
							if(inventory != null)
								editedRows = RankingSystem.SQLInsertInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), item_id, timestamp, inventory.getNumber()+1, "perm", guild_settings.getThemeID());
							else
								editedRows = RankingSystem.SQLInsertInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), item_id, timestamp, 1, "perm", guild_settings.getThemeID());
						}
						else if(list.get(random).getAction().equals("use")) {
							InventoryContent inventory = RankingSystem.SQLgetNumberAndExpirationFromInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), list.get(random).getDescription(), "limit", guild_settings.getThemeID());
							try {
								Timestamp timestamp3 = new Timestamp(inventory.getExpiration().getTime()+1000*60*60*24);
								editedRows = RankingSystem.SQLInsertInventoryWithLimit(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), item_id, timestamp, inventory.getNumber()+1, "limit", timestamp3, guild_settings.getThemeID());
							} catch(NullPointerException npe){
								Timestamp timestamp4 = new Timestamp(time+1000*60*60*24);
								editedRows = RankingSystem.SQLInsertInventoryWithLimit(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), item_id, timestamp, 1, "limit", timestamp4, guild_settings.getThemeID());
							}
						}
					}
					//if it's a special reward, send it in private message to the user and print it in the log channel at the same time
					else if(list.get(random).getType().equals("cod")) {
						//send a private message
						e.getMember().getUser().openPrivateChannel().queue(channel -> {
							channel.sendMessage("Congratulations. You have unlocked the following reward:\n"+cod_reward).queue();
						});
						
						//log the reward in bot channel
						var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
						if(log_channel != null) {
							EmbedBuilder message = new EmbedBuilder().setColor(Color.getHSBColor(268, 81, 88)).setTitle("Reward was sent to user!");
							e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription("The user "+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+" has received a rare reward from the daily commands. This is the reward:\n"+cod_reward).build()).queue();
						}
						
						//mark the code as used
						editedRows = RankingSystem.SQLUpdateUsedOnReward(cod_reward, e.getGuild().getIdLong());
					}
					//if the inventory has been updated, set the daily for this user and the current day as opened
					if(editedRows > 0) {
						if(RankingSystem.SQLInsertDailiesUsage(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), timestamp, timestamp2) > 0) {
							logger.debug("{} received {} out of the Daily command", e.getMember().getUser().getId(), list.get(random).getDescription());
							RankingSystem.SQLInsertActionLog("low", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Daily retrieved", list.get(random).getDescription());
						}
						else {
							logger.error("used dailies from {} couldn't be marked in RankingSystem.dailies_usage table", e.getMember().getUser().getId());
							RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Daily retrieval not marked", list.get(random).getDescription());
						}
					}
					else {
						e.getChannel().sendMessage("Internal error occurred! Daily item couldn't be inserted into your inventory. Please contact an administrator!").queue();
						logger.warn("{} couldn't be inserted into inventory", list.get(random).getDescription());
						RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Daily item couldn't be inserted to inventory", "An insert error occurred for the following item "+list.get(random).getDescription());
					}
					list.clear();
				}
				//notify the user that he can't use the daily command yet
				else {
					long hours = time_for_daily/1000/60/60;
					long minutes = time_for_daily/1000/60%60;
					e.getChannel().sendMessage("Wait, slow down! You can open your next daily in **"+hours+" hours and "+minutes+" minutes**!").queue();
				}
			}
			else {
				e.getChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in "+STATIC.getChannels(bot_channels)).queue();
			}
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used Daily command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}

}
