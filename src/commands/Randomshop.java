package commands;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.RandomshopExecution;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
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
		if(GuildIni.getRandomshopCommand(e.getGuild().getIdLong())) {
			Logger logger = LoggerFactory.getLogger(Randomshop.class);
			logger.debug("The user {} has executed the Randomshop command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			
			var bot_channel = Azrael.SQLgetChannelID(e.getGuild().getIdLong(), "bot");
			if(e.getTextChannel().getIdLong() == bot_channel || bot_channel == 0) {
				final String prefix = GuildIni.getCommandPrefix(e.getGuild().getIdLong());
				if(e.getMessage().getContentRaw().equals(prefix+"randomshop")) {
					//run help and collect all possible parameters
					RandomshopExecution.runHelp(e, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong()));
				}
				else if(e.getMessage().getContentRaw().contains(prefix+"randomshop -play ")) {
					//start a round
					RandomshopExecution.runRound(e, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong()), e.getMessage().getContentRaw().substring((e.getMessage().getContentRaw().indexOf("-play")+6)));
				}
				else if(e.getMessage().getContentRaw().equals(prefix+"randomshop -replay")) {
					//play another round if a match occurred within 10 minutes
					File file = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/randomshop_play_"+e.getMember().getUser().getId());
					if(file.exists() && System.currentTimeMillis() - file.lastModified() < 600000) {
						RandomshopExecution.runRound(e, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong()), FileSetting.readFile(file.getAbsolutePath()));
					}
					else {
						e.getTextChannel().sendMessage("You haven't played one round yet or the last time you played was over 10 minutes ago. Please rewrite the full command").queue();
						if(file.exists())
							file.delete();
					}
				}
				else if(e.getMessage().getContentRaw().contains(prefix+"randomshop ")) {
					//display the weapons that can be obtained.
					RandomshopExecution.inspectItems(e, null, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong()), e.getMessage().getContentRaw().substring(prefix.length()+11), 1);
				}
				else {
					//if typos occur, run help
					RandomshopExecution.runHelp(e, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong()));
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
