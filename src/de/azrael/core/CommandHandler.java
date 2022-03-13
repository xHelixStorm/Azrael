package de.azrael.core;

import java.util.EnumSet;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.interfaces.CommandPrivate;
import de.azrael.interfaces.CommandPublic;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.Permission;

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
	
	public static boolean handleCommand(CommandParser.CommandContainer cmd, BotConfigs botConfig) {
		
		if(cmd.e != null) {
			final String cmdInvoker = cmd.invoke.toLowerCase();
			if(commandsPublic.containsKey(cmdInvoker)) {
				final CommandPublic command = commandsPublic.get(cmdInvoker);
				boolean safe = command.called(cmd.args, cmd.e, botConfig);
				
				if(safe) {
					if(cmd.e.getGuild().getSelfMember().hasPermission(cmd.e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(cmd.e.getGuild(), cmd.e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS))) {
						boolean executed = command.action(cmd.args, cmd.e, botConfig);
						command.executed(cmd.args, executed, cmd.e, botConfig);
					}
					else {
						logger.warn("Either MESSAGE_WRITE or MESSAGE_EMBED_LINKS is missing for channel {} in guild {}", cmd.e.getChannel().getId(), cmd.e.getGuild().getId());
					}
				}
				return true;
			}
			else {
				final String commandKey = cmd.e.getGuild().getId()+""+cmdInvoker;
				if(commandsPublic.containsKey(commandKey)) {
					final CommandPublic command = commandsPublic.get(commandKey);
					boolean safe = command.called(cmd.args, cmd.e, botConfig);
					
					if(safe) {
						if(cmd.e.getGuild().getSelfMember().hasPermission(cmd.e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(cmd.e.getGuild(), cmd.e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS))) {
							boolean executed = command.action(cmd.args, cmd.e, botConfig);
							command.executed(cmd.args, executed, cmd.e, botConfig);
						}
						else {
							logger.warn("Either MESSAGE_WRITE or MESSAGE_EMBED_LINKS is missing for channel {} in guild {}", cmd.e.getChannel().getId(), cmd.e.getGuild().getId());
						}
					}
					return true;
				}
				return false;
			}
		}
		else {
			final String cmdInvoker = cmd.invoke.toLowerCase();
			if(commandsPrivate.containsKey(cmdInvoker)) {
				final CommandPrivate command = commandsPrivate.get(cmdInvoker);
				boolean safe = command.called(cmd.args, cmd.e2);
				if(safe) {
					boolean executed = command.action(cmd.args, cmd.e2);
					command.executed(cmd.args, executed, cmd.e2);
				}
				return true;
			}
			else {
				return false;
			}
		}
	}
}
