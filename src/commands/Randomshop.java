package commands;

import java.io.File;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.RandomshopExecution;
import core.Guilds;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.Azrael;
import sql.RankingSystem;
import sql.RankingSystemItems;
import util.STATIC;

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
			
			var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type().equals("bot")).collect(Collectors.toList());
			if(bot_channels.size() == 0 || bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getTextChannel().getIdLong()).findAny().orElse(null) != null) {
				Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
				if(args.length == 0) {
					//run help and collect all possible parameters
					RandomshopExecution.runHelp(e, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong(), guild_settings.getThemeID()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), guild_settings.getThemeID()));
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase("-play")) {
					//start a round
					RandomshopExecution.runRound(e, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong(), guild_settings.getThemeID()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), guild_settings.getThemeID()), bundleArguments(args, 1));
				}
				else if(args[0].equalsIgnoreCase("-replay")) {
					//play another round if a match occurred within 10 minutes
					File file = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/randomshop_play_"+e.getMember().getUser().getId());
					if(file.exists() && System.currentTimeMillis() - file.lastModified() < 600000) {
						RandomshopExecution.runRound(e, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong(), guild_settings.getThemeID()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), guild_settings.getThemeID()), FileSetting.readFile(file.getAbsolutePath()));
					}
					else {
						e.getTextChannel().sendMessage("You haven't played one round yet or the last time you played was over 10 minutes ago. Please rewrite the full command").queue();
						if(file.exists())
							file.delete();
					}
				}
				else if(args.length > 0) {
					//display the weapons that can be obtained.
					RandomshopExecution.inspectItems(e, null, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong(), guild_settings.getThemeID()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), guild_settings.getThemeID()), bundleArguments(args, 0), 1);
				}
				else {
					//if typos occur, run help
					RandomshopExecution.runHelp(e, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong(), guild_settings.getThemeID()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), guild_settings.getThemeID()));
				}
			}
			else {
				e.getTextChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in "+STATIC.getChannels(bot_channels)).queue();
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
	
	private String bundleArguments(String [] args, int start) {
		var weapon = "";
		for(int i = start; i < args.length; i++) {
			weapon += args[i]+" ";
		}
		return weapon.trim();
	}

}
