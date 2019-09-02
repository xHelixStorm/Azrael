package commandsContainer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import core.Hashes;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;

public class SetMaxExperience {
	public static void runTask(GuildMessageReceivedEvent _e, String _input, Guilds guild_settings){
		Logger logger = LoggerFactory.getLogger(SetMaxExperience.class);
		
		Pattern pattern = Pattern.compile("(enable|disable)");
		Matcher matcher = pattern.matcher(_input);
		if(matcher.find()){
			var editedRows = 0;
			if(matcher.group().equals("enable")){
				guild_settings.setMaxExpEnabled(true);
				editedRows = RankingSystem.SQLInsertMaxExperience(guild_settings.getMaxExperience(), guild_settings.getMaxExpEnabled(), _e.getGuild().getIdLong());
				if(editedRows > 0)
					_e.getChannel().sendMessage("The max experience limitation has been enabled!").queue();
				else 
					_e.getChannel().sendMessage("An internal error occurred. Max experience couldn't be set to enable in table RankingSystem.max_exp").queue();
			}
			else if(matcher.group().equals("disable")){
				guild_settings.setMaxExpEnabled(false);
				editedRows = RankingSystem.SQLInsertMaxExperience(guild_settings.getMaxExperience(), guild_settings.getMaxExpEnabled(), _e.getGuild().getIdLong());
				if(editedRows > 0)
					_e.getChannel().sendMessage("The max experience limitation has been disabled!").queue();
				else
					_e.getChannel().sendMessage("An internal error occurred. Max experience couldn't be set to disable in table RankingSystem.max_exp").queue();
			}
			if(editedRows > 0) {
				logger.debug("{} has set the max experience limitation to {} in guild {}", _e.getMember().getUser().getId(), matcher.group(), _e.getGuild().getId());
				Hashes.addStatus(_e.getGuild().getIdLong(), guild_settings);
			}
			else {
				logger.error("RankingSystem.max_exp couldn't be updated with enable or disable information for guild {}", _e.getGuild().getName());
			}
		}
		else{
			try{
				guild_settings.setMaxExperience(Long.parseLong(_input.replaceAll("[^0-9]", "")));
				guild_settings.setMaxExpEnabled(true);
				if(RankingSystem.SQLInsertMaxExperience(guild_settings.getMaxExperience(), guild_settings.getMaxExpEnabled(), _e.getGuild().getIdLong()) > 0) {
					logger.debug("{} has set the max experience limitation to {} exp in guild {}", _e.getMember().getUser().getId(), guild_settings.getMaxExperience(), _e.getGuild().getId());
					Hashes.addStatus(_e.getGuild().getIdLong(), guild_settings);
					_e.getChannel().sendMessage("**The max experience per day is now "+guild_settings.getMaxExperience()+" and has been automatically enabled!**").queue();
				}
				else {
					logger.error("Max experience couldn't be updated in table RankingSystem.max_exp");
				}
			} catch(NullPointerException | NumberFormatException npe){
				_e.getChannel().sendMessage(_e.getMember().getAsMention()+" Something went wrong, please assure that you've written the parameters correctly!").queue();
			}
		}
	}
}
