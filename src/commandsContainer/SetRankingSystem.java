package commandsContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import core.Hashes;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;
import threads.CollectUsers;

public class SetRankingSystem {
	private final static Logger logger = LoggerFactory.getLogger(SetRankingSystem.class);
	
	@SuppressWarnings("preview")
	public static void runTask(GuildMessageReceivedEvent _e, String _input){
		boolean ranking_state = false;
		boolean wrongInput = false;
		String message;
		
		switch(_input) {
			case "enable" -> {
				ranking_state = true;
				message = "**Ranking system has been succesfully enabled!**";
			}
			case "disable" -> {
				ranking_state = false;
				message = "**Ranking system has been succesfully disabled!**";
			}
			default -> {
				wrongInput = true;
				message = "**"+_e.getMember().getAsMention()+" Something went wrong. Please recheck the syntax and try again!**";
			}
		}
		
		if(wrongInput == false){
			if(RankingSystem.SQLUpdateRankingSystem(_e.getGuild().getIdLong(), _e.getGuild().getName(), ranking_state) > 0) {
				Guilds guild = RankingSystem.SQLgetGuild(_e.getGuild().getIdLong());
				guild.setRankingState(ranking_state);
				Hashes.addStatus(_e.getGuild().getIdLong(), guild);
				logger.debug("{} has set the ranking system to {} in guild {}", _e.getMember().getUser().getId(), _input, _e.getGuild().getId());
				_e.getChannel().sendMessage(message).queue();
				
				if(ranking_state == true) {
					if(RankingSystem.SQLgetRoles(_e.getGuild().getIdLong()) == null) {
						logger.error("Roles from RankingSystem.roles couldn't be called and cached");
						_e.getChannel().sendMessage("An internal error occurred. Roles from RankingSystem.roles couldn't be called and cached").queue();
					}
					if(RankingSystem.SQLgetLevels(guild.getThemeID()).size() == 0) {
						logger.error("Levels from RankingSystem.level_list couldn't be called and cached");
						_e.getChannel().sendMessage("An internal error occurred. Levels from RankingSystem.level_list couldn't be called and cached").queue();
					}
					new Thread(new CollectUsers(_e)).start();
				}
			}
			else {
				logger.error("An internal error occurred on editing the RankingSystem.guilds table to alter the ranking state for guild {}", _e.getGuild().getName());
				_e.getChannel().sendMessage("An internal error occurred. The ranking state couldn't be altered in the table RankingSystem.guilds").queue();
			}
		}
		else{
			_e.getChannel().sendMessage(message).queue();
		}
	}
}
