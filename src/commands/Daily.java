package commands;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import core.Hashes;
import fileManagement.IniFileReader;
import inventory.Dailies;
import inventory.DrawDaily;
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
							RankingDB.SQLgetSumWeightFromDailyItems();
							int random = ThreadLocalRandom.current().nextInt(1, RankingDB.getWeight());
							ArrayList<Dailies> list = new ArrayList<Dailies>();
							
							RankingDB.SQLgetDailiesAndType();
							for(Dailies daily : RankingDB.getDailies()){
								for(int i = 1; i <= daily.getWeight(); i++){
									Dailies this_daily = new Dailies();
									this_daily.setDescription(daily.getDescription());
									this_daily.SetType(daily.getType());
									this_daily.setAction(daily.getAction());
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
							if(list.get(random).getType().equals("cur")){
								rankingSystem.Rank user_details = Hashes.getRanking(e.getMember().getUser().getIdLong());
								user_details.setCurrency(user_details.getCurrency()+Long.parseLong(list.get(random).getDescription().replaceAll("[^0-9]*", "")));
								RankingDB.SQLUpdateCurrency(e.getMember().getUser().getIdLong(), user_details.getCurrency());
								Hashes.addRanking(e.getMember().getUser().getIdLong(), user_details);
							}
							else if(list.get(random).getType().equals("exp")){
								RankingDB.SQLgetItemIDFromShopContent(list.get(random).getDescription());
								if(list.get(random).getAction().equals("keep")){
									RankingDB.SQLgetNumberAndExpirationFromInventory(e.getMember().getUser().getIdLong(), list.get(random).getDescription(), "perm");
									RankingDB.SQLInsertInventory(e.getMember().getUser().getIdLong(), RankingDB.getItemID(), timestamp, RankingDB.getNumber()+1, "perm");
								}
								else if(list.get(random).getAction().equals("use")){
									RankingDB.SQLgetNumberAndExpirationFromInventory(e.getMember().getUser().getIdLong(), list.get(random).getDescription(), "limit");
									try {
										Timestamp timestamp3 = new Timestamp(RankingDB.getExpiration().getTime()+1000*60*60*24);
										RankingDB.SQLInsertInventoryWithLimit(e.getMember().getUser().getIdLong(), RankingDB.getItemID(), timestamp, RankingDB.getNumber()+1, "limit", timestamp3);
									} catch(NullPointerException npe){
										Timestamp timestamp4 = new Timestamp(time+1000*60*60*24);
										RankingDB.SQLInsertInventoryWithLimit(e.getMember().getUser().getIdLong(), RankingDB.getItemID(), timestamp, RankingDB.getNumber()+1, "limit", timestamp4);
									}
								}
							}
							RankingDB.SQLInsertDailiesUsage(e.getMember().getUser().getIdLong(), timestamp, timestamp2);
							RankingDB.SQLInsertActionLog("low", e.getMember().getUser().getIdLong(), "Daily retrieved", list.get(random).getDescription());
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
		RankingDB.clearDailiesArray();
	}

	@Override
	public String help() {
		return null;
	}

}
