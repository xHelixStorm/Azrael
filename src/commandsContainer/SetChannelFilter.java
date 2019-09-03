package commandsContainer;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;

public class SetChannelFilter {
	private final static Logger logger = LoggerFactory.getLogger(SetChannelFilter.class);
	
	@SuppressWarnings("preview")
	public static void runTask(GuildMessageReceivedEvent _e, String [] _input){
		int i = 0;
		ArrayList<String> filter_lang = new ArrayList<String>();
		boolean languageError = false;
		if(_input.length == 3) {
			String channel = _input[1].replaceAll("[^0-9]*", "");
			if(channel.length() == 18){
				long channel_id = Long.parseLong(channel);
				Azrael.SQLDeleteChannel_Filter(channel_id);
				Pattern pattern = Pattern.compile("(all|eng|fre|ger|tur|rus|spa|por|ita)");
				Matcher matcher = pattern.matcher(_input[2]);
				while(matcher.find()) {
					filter_lang.add(matcher.group());
					if(filter_lang.get(i).length() != 3){
						languageError = true;
					}
					else{
						switch(filter_lang.get(i)) {
						case "all", "eng", "ger", "fre", "tur", "rus", "spa", "por", "ita" ->
							languageError = false;
						default -> languageError = true;
						}
					}
					i++;
				}
				if(languageError ==  false){
					for(String language : filter_lang){
						Azrael.SQLInsertChannel_Filter(channel_id, language);
						logger.debug("{} has set the channel filter {} for channel {} in guild {}", _e.getMember().getUser().getId(), language, channel_id, _e.getGuild().getId());
						_e.getChannel().sendMessage("**Filter for <#"+channel_id+"> has been updated!**").queue();
					}
				}
				else{
					_e.getChannel().sendMessage(_e.getMember().getAsMention()+" please define one or more available/valid languages!").queue();
				}
			}
			else{
				_e.getChannel().sendMessage(_e.getMember().getAsMention()+" Please insert a valid channel id or channel name!").queue();
			}
		}
		else {
			_e.getChannel().sendMessage(_e.getMember().getAsMention()+" Command couldn't be executed. Please recheck the syntax!").queue();
		}
	}
}
