package commandsContainer;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Dailies;
import core.Hashes;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;

public class SetDailyItem {

	public static void runTask(GuildMessageReceivedEvent _e, String _input, ArrayList<Dailies> _dailies, int _weight){
		Logger logger = LoggerFactory.getLogger(SetDailyItem.class);
		
		boolean duplicateFound = false;
		Pattern pattern = Pattern.compile("\"[\\s\\d\\w]*\"");
		Matcher matcher = pattern.matcher(_input);
		if(matcher.find()){
			String description = matcher.group();
			for(Dailies daily : _dailies){
				if(description.replaceAll("[\"]", "").equals(daily.getDescription())){
					duplicateFound = true;
				}
			}
			if(duplicateFound == true){
				_e.getChannel().sendMessage(_e.getMember().getAsMention()+ "this item has been already set into the daily item pool!").queue();
			}
			else{
				try {
					_input = _input.substring(description.length()+1);
					if(_input.contains("-weight ")){
						if(!_input.replaceAll("[^0-9]", "").equals("")){
							String weight = _input.replaceAll("[^0-9]", "");
							_input = _input.substring(weight.length()+9);
							if(_input.contains("-type ")){
								_input = _input.substring(6);
								String type = _input;
								if(type.equals("cur") || type.equals("exp") || type.equals("cod")){
									if(_weight+Integer.parseInt(weight) <= 100){
										if(RankingSystem.SQLInsertDailyItems(description.replaceAll("[\"]", ""), Integer.parseInt(weight), type, _e.getGuild().getIdLong(), RankingSystem.SQLgetGuild(_e.getGuild().getIdLong()).getThemeID()) > 0) {
											Hashes.removeDailyItems(_e.getGuild().getIdLong());
											logger.debug("{} has inserted the item {} into the daily items pool with the weight {}", _e.getMember().getUser().getId(), description.replaceAll("[\"]", ""), weight);
											_e.getChannel().sendMessage("New daily item has been set. Your current free weight is **"+(100-_weight-Integer.parseInt(weight))+"** now!").queue();
										}
										else {
											_e.getChannel().sendMessage("Internal error! Daily item couldn't be inserted!").queue();
											logger.error("Internal error! Daily item couldn't be inserted!");
											RankingSystem.SQLInsertActionLog("high", _e.getMember().getUser().getIdLong(), _e.getGuild().getIdLong(), "Daily item registration error", "daily item couldn't be inserted with weight "+weight+" and item "+description.replaceAll("[\"]", ""));
										}
									}
									else{
										_e.getChannel().sendMessage(_e.getMember().getAsMention()+" The total weight of 100 has been exceeded by **"+(_weight+Integer.parseInt(weight)-100)+"**! Your current free weight to distribute is **"+(100-_weight)+"**").queue();
										logger.warn("Daily item couldn't be inserted due to weight overload");
									}
								}
								else{
									_e.getChannel().sendMessage(_e.getMember().getAsMention()+" Something went wrong, please type either **cur** or **ite** after the -type parameter!").queue();
								}
							}
							else{
								_e.getChannel().sendMessage(_e.getMember().getAsMention()+" Something went wrong. The **-type** parameter is missing or no value followed!").queue();
							}
						}
						else{
							_e.getChannel().sendMessage(_e.getMember().getAsMention()+" Something went wrong, please type a proper digit value after the -weight parameter!").queue();
						}
					}
					else{
						_e.getChannel().sendMessage(_e.getMember().getAsMention()+" Something went wrong. The **-weight** parameter is missing or no value followed!").queue();
					}
				} catch(StringIndexOutOfBoundsException sioobe){
					_e.getChannel().sendMessage(_e.getMember().getAsMention()+" Something went wrong. Please check if the syntax is complete!").queue();
				}
			}
		}
		else{
			_e.getChannel().sendMessage(_e.getMember().getAsMention()+" Something went wrong, please recheck the syntax to put a proper description!").queue();
		}
	}
}
