package commands;

import java.awt.Color;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Cache;
import core.Hashes;
import fileManagement.GuildIni;
import inventory.Dailies;
import inventory.DrawDaily;
import inventory.InventoryContent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingSystem;
import sql.Azrael;
import util.STATIC;

public class Daily implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(GuildIni.getDailyCommand(e.getGuild().getIdLong())){
			Logger logger = LoggerFactory.getLogger(Daily.class);
			logger.debug("{} has used Daily command", e.getMember().getUser().getId());
			
			var cache = Hashes.getTempCache("dailyDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
			if(cache == null || cache.getExpiration() - System.currentTimeMillis() <= 0) {
				Hashes.addTempCache("dailyDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId(), new Cache(3000));
				
				ExecutorService executor = Executors.newSingleThreadExecutor();
				executor.execute(() -> {
					var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type().equals("bot")).collect(Collectors.toList());
					if(bot_channels.size() == 0 || bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getTextChannel().getIdLong()).findAny().orElse(null) != null) {
						long time_for_daily = 0;
						try {
							time_for_daily = RankingSystem.SQLgetDailiesUsage(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong()).getTime()-System.currentTimeMillis();
						} catch(NullPointerException npe){
							time_for_daily = -1;
						}
						if(time_for_daily < 0){
							// confirm if there are any available rewards to send in private message
							String cod_reward = RankingSystem.SQLRetrieveGiveawayReward(e.getGuild().getIdLong());
							boolean exclude_cod = false;
							if(cod_reward.length() == 0)
								exclude_cod = true;
							//
							core.Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
							List<Dailies> daily_items = RankingSystem.SQLgetDailiesAndType(e.getGuild().getIdLong(), guild_settings.getThemeID());
							var tot_weight = 0;
							if(exclude_cod == true) {
								tot_weight = daily_items.parallelStream().filter(i -> !i.getType().equals("cod")).mapToInt(i -> i.getWeight()).sum();
								daily_items = daily_items.parallelStream().filter(i -> !i.getType().equals("cod")).collect(Collectors.toList());
							}
							else
								tot_weight = daily_items.parallelStream().mapToInt(i -> i.getWeight()).sum();
							int random = ThreadLocalRandom.current().nextInt(1, tot_weight);
							
							ArrayList<Dailies> list = new ArrayList<Dailies>();
							for(Dailies daily : daily_items){
								for(int i = 1; i <= daily.getWeight(); i++){
									Dailies this_daily = new Dailies();
									this_daily.setDescription(daily.getDescription());
									this_daily.SetType(daily.getType());
									this_daily.setAction(daily.getAction());
									if(daily.getType().equals("cod") && cod_reward.length() > 0) 
										list.add(this_daily);
									
									else if(daily.getType().equals("cur") || daily.getType().equals("exp"))
										list.add(this_daily);
								}
							}
							DrawDaily.draw(e, list.get(random).getDescription());
							long time = System.currentTimeMillis();
							Timestamp timestamp = new Timestamp(time);
							LocalTime midnight = LocalTime.MIDNIGHT;
							LocalDate today = LocalDate.now();
							LocalDateTime tomorrowMidnight = LocalDateTime.of(today, midnight).plusDays(1);
							Timestamp timestamp2 = Timestamp.valueOf(tomorrowMidnight);
							var editedRows = 0;
							if(list.get(random).getType().equals("cur")){
								rankingSystem.Rank user_details = RankingSystem.SQLgetWholeRankView(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), guild_settings.getThemeID());
								user_details.setCurrency(user_details.getCurrency()+Long.parseLong(list.get(random).getDescription().replaceAll("[^0-9]*", "")));
								editedRows = RankingSystem.SQLUpdateCurrency(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), user_details.getCurrency());
								if(editedRows > 0)
									Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getIdLong(), user_details);
							}
							else if(list.get(random).getType().equals("exp")){
								var item_id = RankingSystem.SQLgetSkinshopContentAndType(e.getGuild().getIdLong(), guild_settings.getThemeID()).parallelStream().filter(i -> i.getShopDescription().equals(list.get(random).getDescription())).findAny().orElse(null).getItemID();
								if(list.get(random).getAction().equals("keep")){
									InventoryContent inventory = RankingSystem.SQLgetNumberAndExpirationFromInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), list.get(random).getDescription(), "perm", guild_settings.getThemeID());
									if(inventory != null)
										editedRows = RankingSystem.SQLInsertInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), item_id, timestamp, inventory.getNumber()+1, "perm", guild_settings.getThemeID());
									else
										editedRows = RankingSystem.SQLInsertInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), item_id, timestamp, 1, "perm", guild_settings.getThemeID());
								}
								else if(list.get(random).getAction().equals("use")){
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
							else if(list.get(random).getType().equals("cod")) {
								//send a private message
								PrivateChannel pc = e.getMember().getUser().openPrivateChannel().complete();
								pc.sendMessage("Congratulations. You have unlocked the following reward:\n"+cod_reward).queue();
								pc.close();
								
								//log the reward in bot channel
								var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type().equals("log")).findAny().orElse(null);
								if(log_channel != null) {
									EmbedBuilder message = new EmbedBuilder().setColor(Color.getHSBColor(268, 81, 88)).setTitle("Reward was sent to user!");
									e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription("The user "+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+" has received a rare reward from the daily commands. This is the reward:\n"+cod_reward).build()).queue();
								}
								
								//mark the code as used
								editedRows = RankingSystem.SQLUpdateUsedOnReward(cod_reward, e.getGuild().getIdLong());
							}
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
								e.getTextChannel().sendMessage("Internal error occurred! Daily item couldn't be inserted into your inventory. Please contact an administrator!").queue();
								logger.warn("{} couldn't be inserted into inventory", list.get(random).getDescription());
								RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Daily item couldn't be inserted to inventory", "An insert error occurred for the following item "+list.get(random).getDescription());
							}
							list.clear();
						}
						else{
							long hours = time_for_daily/1000/60/60;
							long minutes = time_for_daily/1000/60%60;
							e.getTextChannel().sendMessage("Wait, slow down! You can open your next daily in **"+hours+" hours and "+minutes+" minutes**!").queue();
						}
					}
					else{
						e.getTextChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in "+STATIC.getChannels(bot_channels)).queue();
						logger.warn("Daily command has been used in a not bot channel");
					}
				});
				executor.shutdown();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		
	}

	@Override
	public String help() {
		return null;
	}

}
