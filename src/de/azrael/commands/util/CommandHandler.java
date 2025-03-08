package de.azrael.commands.util;

import java.util.EnumSet;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.interfaces.CommandPrivate;
import de.azrael.interfaces.CommandPublic;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;

/**
 * Command existence control and execution
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
			if(cmd.e.isFromType(ChannelType.TEXT) && cmd.e.isFromGuild()) {
				final String cmdInvoker = cmd.invoke.toLowerCase();
				if(commandsPublic.containsKey(cmdInvoker)) {
					final CommandPublic command = commandsPublic.get(cmdInvoker);
					boolean safe = command.called(cmd.args, cmd.e, botConfig);
					
					if(safe) {
						if(cmd.e.getGuild().getSelfMember().hasPermission(cmd.e.getGuildChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(cmd.e.getGuild(), cmd.e.getChannel().asTextChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS))) {
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
							if(cmd.e.getGuild().getSelfMember().hasPermission(cmd.e.getGuildChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(cmd.e.getGuild(), cmd.e.getChannel().asTextChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS))) {
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
			else if(cmd.e.isFromType(ChannelType.PRIVATE)) {
				final String cmdInvoker = cmd.invoke.toLowerCase();
				if(commandsPrivate.containsKey(cmdInvoker)) {
					final CommandPrivate command = commandsPrivate.get(cmdInvoker);
					boolean safe = command.called(cmd.args, cmd.e);
					if(safe) {
						boolean executed = command.action(cmd.args, cmd.e);
						command.executed(cmd.args, executed, cmd.e);
					}
					return true;
				}
				else {
					return false;
				}
			}
		}
		return false;
	}
}
