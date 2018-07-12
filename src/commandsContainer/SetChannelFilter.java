package commandsContainer;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.SqlConnect;

public class SetChannelFilter {
	public static void runTask(MessageReceivedEvent _e, String _input){
		int i = 0;
		ArrayList<String> filter_lang = new ArrayList<String>();
		boolean languageError = false;
		String channel = _input.replaceAll("[^0-9]*", "");
		if(channel.length() == 18){
			long channel_id = Long.parseLong(channel);
			SqlConnect.SQLDeleteChannel_Filter(channel_id);
			Pattern pattern = Pattern.compile("(all|eng|fre|ger|tur|rus)");
			Matcher matcher = pattern.matcher(_input);
			while(matcher.find()){
				filter_lang.add(matcher.group());
				if(filter_lang.get(i).length() != 3){
					languageError = true;
				}
				else{
					switch(filter_lang.get(i)){
					case "all":
					case "eng":
					case "ger":
					case "fre":
					case "tur":
					case "rus":
						languageError = false;
						break;
					default:
						languageError = true;
					}
				}
				i++;
			}
			if(languageError ==  false){
				for(String language : filter_lang){
					SqlConnect.SQLInsertChannel_Filter(channel_id, language);
					_e.getTextChannel().sendMessage("**Filter for <#"+channel_id+"> has been updated!**").queue();
				}
			}
			else{
				_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" please define one or more available languages!").queue();
			}
		}
		else{
			_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Please insert a valid channel id or channel name!").queue();
		}
	}
}
