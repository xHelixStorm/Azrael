package commandsContainer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingDB;

public class SetMaxExperience {
	public static void runTask(MessageReceivedEvent _e, String _input, long _experience){
		Pattern pattern = Pattern.compile("(enable|disable)");
		Matcher matcher = pattern.matcher(_input);
		if(matcher.find()){
			if(matcher.group().equals("enable")){
				RankingDB.SQLInsertMaxExperience(_experience, true, _e.getGuild().getIdLong());
				_e.getTextChannel().sendMessage("The max experience limitation has been enabled!").queue();
			}
			else if(matcher.group().equals("disable")){
				RankingDB.SQLInsertMaxExperience(_experience, false, _e.getGuild().getIdLong());
				_e.getTextChannel().sendMessage("The max experience limitation has been disabled!").queue();
			}
		}
		else{
			try{
				_experience = Long.parseLong(_input.replaceAll("[^0-9]", ""));
				RankingDB.SQLInsertMaxExperience(_experience, true, _e.getGuild().getIdLong());
				_e.getTextChannel().sendMessage("**The max experience per day is now "+_experience+" and has been automatically enabled!**").queue();
			} catch(NullPointerException | NumberFormatException npe){
				_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Something went wrong, please assure that you've written the parameters correctly!").queue();
			}
		}
	}
}
