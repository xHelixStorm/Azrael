package commandsContainer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.SqlConnect;

public class SetMuteTimer {
	public static void runTask(MessageReceivedEvent _e, String _input, double _timer1, double _timer2){
		Pattern pattern = Pattern.compile("[0-9]");
		Matcher matcher = pattern.matcher(_input);
		if(matcher.find()){
			try{
				int separator = Integer.parseInt(matcher.group());
				long timer = Long.parseLong(_input.substring(3));
				timer = timer*60*1000;
				long updateTimer = timer;
				
				if(separator == 1){
					if(_timer1 == 0 && _e.getGuild().getIdLong() == 0){
						SqlConnect.SQLInsertMuteTimer1(_e.getGuild().getIdLong(), updateTimer);
					}
					else{
						SqlConnect.SQLUpdateMuteTimer1(_e.getGuild().getIdLong(), updateTimer);
					}
					_e.getTextChannel().sendMessage("The first warning timer has been updated").queue();
				}
				else if(separator == 2){
					if(_timer2 == 0 && _e.getGuild().getIdLong() == 0){
						SqlConnect.SQLInsertMuteTimer2(_e.getGuild().getIdLong(), updateTimer);
					}
					else{
						SqlConnect.SQLUpdateMuteTimer2(_e.getGuild().getIdLong(), updateTimer);
					}
					_e.getTextChannel().sendMessage("The second warning timer has been updated").queue();
				}
				else{
					_e.getTextChannel().sendMessage("Please select either timer 1 or timer 2").queue();
				}
			} catch(StringIndexOutOfBoundsException sioobe){
				_e.getTextChannel().sendMessage("Please insert a valid time in minutes").queue();
			}
		}
		else{
			_e.getTextChannel().sendMessage("Please select either timer 1 or timer 2").queue();
		}
	}
}
