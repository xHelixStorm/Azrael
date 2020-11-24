package core;

import java.util.EnumSet;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import interfaces.CommandPrivate;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.Permission;
import util.STATIC;

/**
 * Command existance control and execution
 * @author xHelixstorm
 *
 */

public class CommandHandler {
	private final static Logger logger = LoggerFactory.getLogger(CommandHandler.class);

	public static CommandParser parse = new CommandParser();
	public static HashMap<String, CommandPublic> commandsPublic = new HashMap<>();
	public static HashMap<String, CommandPrivate> commandsPrivate = new HashMap<>();
	
	public static boolean handleCommand(CommandParser.CommandContainer cmd) {
		
		if(cmd.e != null) {
			if(commandsPublic.containsKey(cmd.invoke)) {
				final CommandPublic command = commandsPublic.get(cmd.invoke);
				boolean safe = command.called(cmd.args, cmd.e);
				
				if(safe) {
					if(cmd.e.getGuild().getSelfMember().hasPermission(cmd.e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(cmd.e.getGuild(), cmd.e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS))) {
						command.action(cmd.args, cmd.e);
						command.executed(safe, cmd.e);
					}
					else {
						logger.warn("Either MESSAGE_WRITE or MESSAGE_EMBED_LINKS is missing for channel {} in guild {}", cmd.e.getChannel().getId(), cmd.e.getGuild().getId());
					}
				}
				else {
					command.executed(safe, cmd.e);
				}
				return true;
			}
			else {
				final String commandKey = cmd.e.getGuild().getId()+""+cmd.invoke;
				if(commandsPublic.containsKey(commandKey)) {
					final CommandPublic command = commandsPublic.get(commandKey);
					boolean safe = command.called(cmd.args, cmd.e);
					
					if(safe) {
						if(cmd.e.getGuild().getSelfMember().hasPermission(cmd.e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(cmd.e.getGuild(), cmd.e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS))) {
							command.action(cmd.args, cmd.e);
							command.executed(safe, cmd.e);
						}
						else {
							logger.warn("Either MESSAGE_WRITE or MESSAGE_EMBED_LINKS is missing for channel {} in guild {}", cmd.e.getChannel().getId(), cmd.e.getGuild().getId());
						}
					}
					else {
						command.executed(safe, cmd.e);
					}
					return true;
				}
				return false;
			}
		}
		else {
			if(commandsPrivate.containsKey(cmd.invoke)) {
				final CommandPrivate command = commandsPrivate.get(cmd.invoke);
				boolean safe = command.called(cmd.args, cmd.e2);
				if(safe) {
					command.action(cmd.args, cmd.e2);
					command.executed(safe, cmd.e2);
				}
				else {
					command.executed(safe, cmd.e2);
				}
				return true;
			}
			else {
				return false;
			}
		}
	}
}
