package commands;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fileManagement.IniFileReader;
import inventory.InventoryBuilder;
import inventory.InventoryContent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingDB;
import sql.SqlConnect;

public class Inventory implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getHelpCommand().equals("true")){
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(() -> {
				RankingDB.SQLgetGuild(e.getGuild().getIdLong());{
					if(RankingDB.getRankingState() == true){
						SqlConnect.SQLgetChannelID(e.getGuild().getIdLong(), "bot");
						if(SqlConnect.getChannelID() == e.getTextChannel().getIdLong()){
							//project will be enhanced with more options in the future (e.g distinction of displaying items, weapons, all or kind of weapons. for now only items)
							if(e.getMessage().getContentRaw().equals(IniFileReader.getCommandPrefix()+"inventory -list")){
								String out = "";
								RankingDB.SQLgetInventoryAndDescriptionWithoutLimit(e.getMember().getUser().getIdLong());
								for(InventoryContent inventory : RankingDB.getInventory()){
									out+= inventory.getDescription()+"\n";
								}
								e.getTextChannel().sendMessage("```"+out+"```").queue();
							}
							else{
								int limit = 0;
								if(e.getMessage().getContentRaw().equals(IniFileReader.getCommandPrefix()+"inventory -page ")){
									try {
										limit = Integer.parseInt(e.getMessage().getContentRaw().replaceAll("[^0-9]", ""))-1;
										RankingDB.SQLgetTotalItemNumber(e.getMember().getUser().getIdLong());
										if(limit <= RankingDB.getItemNumber()){
											limit*=12;
										}
									} catch(NumberFormatException nfe){
										limit = 0;
									}
								}
								e.getTextChannel().sendMessage("to have everything on one page, use the **-list** parameter together with the command!\nAdditionally, you can visualize the desired page with the **-page** paramenter.").queue();
								RankingDB.SQLgetInventoryAndDescriptions(e.getMember().getUser().getIdLong(), limit);
								InventoryBuilder.DrawInventory(e, "total", "total", RankingDB.getInventory(), limit/12+1, RankingDB.getItemNumber()+1);
							}
						}
						else{
							e.getTextChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in <#"+SqlConnect.getChannelID()+">").queue();
						}
					}
					else{
						e.getTextChannel().sendMessage(e.getMember().getAsMention()+" you can't use any item or skin while the ranking system is disabled!").queue();
					}
				}
			});
			executor.shutdown();
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		RankingDB.clearAllVariables();
		SqlConnect.clearAllVariables();
		RankingDB.clearInventoryArray();
	}

	@Override
	public String help() {
		return null;
	}

}
