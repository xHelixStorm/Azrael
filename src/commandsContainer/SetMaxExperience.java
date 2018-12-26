package commandsContainer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.Guilds;
import core.Hashes;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingDB;

public class SetMaxExperience {
	public static void runTask(MessageReceivedEvent _e, String _input, Guilds guild_settings){
		Pattern pattern = Pattern.compile("(enable|disable)");
		Matcher matcher = pattern.matcher(_input);
		if(matcher.find()){
			if(matcher.group().equals("enable")){
				guild_settings.setMaxExpEnabled(true);
				RankingDB.SQLInsertMaxExperience(guild_settings.getMaxExperience(), guild_settings.getMaxExpEnabled(), _e.getGuild().getIdLong());
				_e.getTextChannel().sendMessage("The max experience limitation has been enabled!").queue();
			}
			else if(matcher.group().equals("disable")){
				guild_settings.setMaxExpEnabled(false);
				RankingDB.SQLInsertMaxExperience(guild_settings.getMaxExperience(), guild_settings.getMaxExpEnabled(), _e.getGuild().getIdLong());
				_e.getTextChannel().sendMessage("The max experience limitation has been disabled!").queue();
			}
			Hashes.addStatus(_e.getGuild().getIdLong(), guild_settings);
		}
		else{
			try{
				guild_settings.setMaxExperience(Long.parseLong(_input.replaceAll("[^0-9]", "")));
				guild_settings.setMaxExpEnabled(true);
				RankingDB.SQLInsertMaxExperience(guild_settings.getMaxExperience(), guild_settings.getMaxExpEnabled(), _e.getGuild().getIdLong());
				Hashes.addStatus(_e.getGuild().getIdLong(), guild_settings);
				_e.getTextChannel().sendMessage("**The max experience per day is now "+guild_settings.getMaxExperience()+" and has been automatically enabled!**").queue();
			} catch(NullPointerException | NumberFormatException npe){
				_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Something went wrong, please assure that you've written the parameters correctly!").queue();
			}
		}
	}
}
