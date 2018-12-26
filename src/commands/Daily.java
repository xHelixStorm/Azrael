package commands;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
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

import core.Hashes;
import fileManagement.IniFileReader;
import inventory.Dailies;
import inventory.DrawDaily;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingDB;
import sql.SqlConnect;
import threads.DelayDelete;

public class Daily implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getDailyCommand().equals("true")){
			String fileName = IniFileReader.getTempDirectory()+"CommandDelay/"+e.getMember().getUser().getId()+"_daily.azr";
			File file = new File(fileName);
			if(!file.exists()){
				try {
					file.createNewFile();
					new Thread(new DelayDelete(fileName, 3000)).start();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				
				ExecutorService executor = Executors.newSingleThreadExecutor();
				executor.execute(() -> {
					SqlConnect.SQLgetChannelID(e.getGuild().getIdLong(), "bot");
					if(SqlConnect.getChannelID() == e.getTextChannel().getIdLong() || SqlConnect.getChannelID() == 0){
						RankingDB.SQLgetDailiesUsage(e.getMember().getUser().getIdLong());
						long time_for_daily = 0;
						try {
							time_for_daily = RankingDB.getNextDaily().getTime()-System.currentTimeMillis();
						} catch(NullPointerException npe){
							time_for_daily = -1;
						}
						if(time_for_daily < 0){
							// confirm if there are any available rewards to send in private message
							String cod_reward = RankingDB.SQLRetrieveGiveawayReward();
							boolean exclude_cod = false;
							if(cod_reward.length() == 0)
								exclude_cod = true;
							//
							List<Dailies> daily_items = RankingDB.SQLgetDailiesAndType();
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
								rankingSystem.Rank user_details = Hashes.getRanking(e.getMember().getUser().getIdLong());
								user_details.setCurrency(user_details.getCurrency()+Long.parseLong(list.get(random).getDescription().replaceAll("[^0-9]*", "")));
								RankingDB.SQLUpdateCurrency(e.getMember().getUser().getIdLong(), user_details.getCurrency());
								Hashes.addRanking(e.getMember().getUser().getIdLong(), user_details);
							}
							else if(list.get(random).getType().equals("exp")){
								var item_id = RankingDB.SQLgetSkinshopContentAndType().parallelStream().filter(i -> i.getShopDescription().equals(list.get(random).getDescription())).findAny().orElse(null).getItemID();
								if(list.get(random).getAction().equals("keep")){
									RankingDB.SQLgetNumberAndExpirationFromInventory(e.getMember().getUser().getIdLong(), list.get(random).getDescription(), "perm");
									editedRows = RankingDB.SQLInsertInventory(e.getMember().getUser().getIdLong(), item_id, timestamp, RankingDB.getNumber()+1, "perm");
								}
								else if(list.get(random).getAction().equals("use")){
									RankingDB.SQLgetNumberAndExpirationFromInventory(e.getMember().getUser().getIdLong(), list.get(random).getDescription(), "limit");
									try {
										Timestamp timestamp3 = new Timestamp(RankingDB.getExpiration().getTime()+1000*60*60*24);
										editedRows = RankingDB.SQLInsertInventoryWithLimit(e.getMember().getUser().getIdLong(), item_id, timestamp, RankingDB.getNumber()+1, "limit", timestamp3);
									} catch(NullPointerException npe){
										Timestamp timestamp4 = new Timestamp(time+1000*60*60*24);
										editedRows = RankingDB.SQLInsertInventoryWithLimit(e.getMember().getUser().getIdLong(), item_id, timestamp, RankingDB.getNumber()+1, "limit", timestamp4);
									}
								}
							}
							else if(list.get(random).getType().equals("cod")) {
								//send a private message
								PrivateChannel pc = e.getMember().getUser().openPrivateChannel().complete();
								pc.sendMessage("Congratulations. You have unlocked the following reward:\n"+cod_reward).queue();
								pc.close();
								
								//log the reward in bot channel
								SqlConnect.SQLgetChannelID(e.getGuild().getIdLong(), "log");
								if(SqlConnect.getChannelID() != 0) {
									EmbedBuilder message = new EmbedBuilder().setColor(Color.getHSBColor(268, 81, 88)).setTitle("Reward was sent to user!");
									e.getGuild().getTextChannelById(SqlConnect.getChannelID()).sendMessage(message.setDescription("The user "+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+" has received a rare reward from the daily commands. This is the reward:\n"+cod_reward).build()).queue();
								}
								
								//mark the code as used
								RankingDB.SQLUpdateUsedOnReward(cod_reward);
							}
							if(editedRows > 0) {
								RankingDB.SQLInsertDailiesUsage(e.getMember().getUser().getIdLong(), timestamp, timestamp2);
								RankingDB.SQLInsertActionLog("low", e.getMember().getUser().getIdLong(), "Daily retrieved", list.get(random).getDescription());
							}
							else {
								e.getTextChannel().sendMessage("Internal error occurred! Daily item couldn't be inserted into your inventory. Please contact an administrator!").queue();
								RankingDB.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), "Daily item couldn't be inserted to inventory", "An insert error occurred for the following item "+list.get(random).getDescription());
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
						e.getTextChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in <#"+SqlConnect.getChannelID()+">").queue();
					}
				});
				executor.shutdown();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		RankingDB.clearAllVariables();
		SqlConnect.clearAllVariables();
	}

	@Override
	public String help() {
		return null;
	}

}
