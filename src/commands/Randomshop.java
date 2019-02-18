package commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.RandomshopExecution;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.Azrael;
import sql.RankingSystemItems;

public class Randomshop implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getRandomshopCommand()) {
			Logger logger = LoggerFactory.getLogger(Randomshop.class);
			logger.debug("The user {} has executed the Randomshop command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			
			var bot_channel = Azrael.SQLgetChannelID(e.getGuild().getIdLong(), "bot");
			if(e.getTextChannel().getIdLong() == bot_channel || bot_channel == 0) {
				if(e.getMessage().getContentRaw().equals(IniFileReader.getCommandPrefix()+"randomshop")) {
					//run help and collect all possible parameters
					RandomshopExecution.runHelp(e, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong()));
				}
				else if(e.getMessage().getContentRaw().contains("play")) {
					//start a round
				}
				else if(e.getMessage().getContentRaw().contains("replay")) {
					//play another round if a match occurred within 5 minutes
				}
			}
			else {
				e.getTextChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in <#"+bot_channel+">").queue();
				logger.warn("Daily command has been used in a not bot channel");
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
