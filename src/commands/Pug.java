package commands;

/**
 * The Pug command allows a user to print a pug image of choice.
 */

import java.io.IOException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.PugExecution;
import core.UserPrivs;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import util.STATIC;

public class Pug implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Pug.class);
	
	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getPugCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getPugLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		long guild_id = e.getGuild().getIdLong();
		String path = "./files/Pug/";
		
		//retrieve all bot channels
		var bot_channels = Azrael.SQLgetChannels(guild_id).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("bot")).collect(Collectors.toList());
		var this_channel = bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null);
		
		//verify that this command is allowed to be used on the current server through the execution_id
		var execution_id = Azrael.SQLgetExecutionID(guild_id);
		//command can't be used on this server
		if(execution_id == 0) {
			e.getChannel().sendMessage("This command is disabled on this server. Please ask an administrator or moderator to activate it!").queue();
		}
		//execute this block, if this command is allowed to be used in all channels or only bot channels
		else if(execution_id == 2 || execution_id == 1) {
			//execute if it's allowed to be used on all channels
			if(execution_id == 2) {
				try {
					PugExecution.Execute(e, args, path, e.getChannel().getIdLong());
				} catch (IOException e1) {
					logger.error("Selected pug picture couldn't be found", e1);
				}
			}
			//execute if it's allowed to be used only in bot channels
			else {
				//check if any bot channel is registered, else print the image anyway
				if(bot_channels.size() > 0 && this_channel != null) {
					try {
						PugExecution.Execute(e, args, path, this_channel.getChannel_ID());
					} catch (IOException e1) {
						logger.error("Selected pug picture couldn't be found", e1);
					}
				}
				else {
					e.getChannel().sendMessage("This command can be used only in "+STATIC.getChannels(bot_channels)).queue();
				}
			}
		}
	}
	
	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used Pug command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}	
}
