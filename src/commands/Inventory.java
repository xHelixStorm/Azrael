package commands;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import fileManagement.IniFileReader;
import inventory.InventoryBuilder;
import inventory.InventoryContent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingSystem;
import sql.Azrael;

public class Inventory implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getHelpCommand()){
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(() -> {
				Logger logger = LoggerFactory.getLogger(Inventory.class);
				logger.debug("{} has used Inventory command", e.getMember().getUser().getId());
				
				if(Hashes.getStatus(e.getGuild().getIdLong()).getRankingState() == true){
					Azrael.SQLgetChannelID(e.getGuild().getIdLong(), "bot");
					if(Azrael.getChannelID() == e.getTextChannel().getIdLong() || Azrael.getChannelID() == 0){
						//project will be enhanced with more options in the future (e.g distinction of displaying items, weapons, all or kind of weapons. for now only items)
						if(e.getMessage().getContentRaw().equals(IniFileReader.getCommandPrefix()+"inventory -list")){
							String out = "";
							for(InventoryContent inventory : RankingSystem.SQLgetInventoryAndDescriptionWithoutLimit(e.getMember().getUser().getIdLong())){
								out+= inventory.getDescription()+"\n";
							}
							e.getTextChannel().sendMessage("```"+out+"```").queue();
						}
						else{
							int limit = 0;
							int itemNumber = RankingSystem.SQLgetTotalItemNumber(e.getMember().getUser().getIdLong());
							if(e.getMessage().getContentRaw().contains(IniFileReader.getCommandPrefix()+"inventory -page ")){
								try {
									limit = Integer.parseInt(e.getMessage().getContentRaw().replaceAll("[^0-9]", ""))-1;
									if(limit <= itemNumber){
										limit*=12;
									}
								} catch(NumberFormatException nfe){
									limit = 0;
								}
							}
							e.getTextChannel().sendMessage("to have everything on one page, use the **-list** parameter together with the command!\nAdditionally, you can visualize the desired page with the **-page** paramenter.").queue();
							InventoryBuilder.DrawInventory(e, "total", "total", RankingSystem.SQLgetInventoryAndDescriptions(e.getMember().getUser().getIdLong(), limit), limit/12+1, itemNumber+1);
						}
					}
					else{
						e.getTextChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in <#"+Azrael.getChannelID()+">").queue();
						logger.warn("Inventory command used in a not bot channel");
					}
				}
				else{
					e.getTextChannel().sendMessage(e.getMember().getAsMention()+" you can't use any item or skin while the ranking system is disabled!").queue();
				}
			});
			executor.shutdown();
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		Azrael.clearAllVariables();
	}

	@Override
	public String help() {
		return null;
	}

}
