package commands;

import java.io.IOException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.PugExecution;
import core.UserPrivs;
import enums.Channel;
import enums.Translation;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import util.STATIC;

/**
 * The Pug command allows a user to print a pug image of choice.
 * @author xHelixStorm
 *
 */

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
		var bot_channels = Azrael.SQLgetChannels(guild_id).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.BOT)).collect(Collectors.toList());
		var this_channel = bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null);
		
		//check if any bot channel is registered, else print the image anyway
		if((bot_channels.size() > 0 && this_channel != null) || bot_channels.size() == 0) {
			try {
				PugExecution.Execute(e, args, path, (bot_channels.size() > 0 ? this_channel.getChannel_ID() : e.getChannel().getIdLong()));
			} catch (IOException e1) {
				logger.error("Selected pug picture couldn't be found", e1);
			}
		}
		else {
			e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.NOT_BOT_CHANNEL)+STATIC.getChannels(bot_channels)).queue();
		}
	}
	
	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used Pug command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}	
}
