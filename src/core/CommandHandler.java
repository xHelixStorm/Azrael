package core;

import java.util.EnumSet;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import interfaces.CommandPrivate;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.Permission;
import util.STATIC;

public class CommandHandler {
	private final static Logger logger = LoggerFactory.getLogger(CommandHandler.class);

	public static CommandParser parse = new CommandParser();
	public static HashMap<String, CommandPublic> commandsPublic = new HashMap<>();
	public static HashMap<String, CommandPrivate> commandsPrivate = new HashMap<>();
	
	public static boolean handleCommand(CommandParser.CommandContainer cmd) {
		
		if(cmd.e != null) {
			if(commandsPublic.containsKey(cmd.invoke)) {
				boolean safe = commandsPublic.get(cmd.invoke).called(cmd.args, cmd.e);
				
				if(safe) {
					if(cmd.e.getGuild().getSelfMember().hasPermission(cmd.e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(cmd.e.getGuild(), cmd.e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS))) {
						commandsPublic.get(cmd.invoke).action(cmd.args, cmd.e);
						commandsPublic.get(cmd.invoke).executed(safe, cmd.e);
					}
					else {
						logger.warn("Either MESSAGE_WRITE or MESSAGE_EMBED_LINKS is missing for channel {} in guild {}");
					}
				}
				else {
					commandsPublic.get(cmd.invoke).executed(safe, cmd.e);
				}
				return true;
			}
			else {
				return false;
			}
		}
		else {
			if(commandsPrivate.containsKey(cmd.invoke)) {
				boolean safe = commandsPrivate.get(cmd.invoke).called(cmd.args, cmd.e2);
				if(safe) {
					commandsPrivate.get(cmd.invoke).action(cmd.args, cmd.e2);
					commandsPrivate.get(cmd.invoke).executed(safe, cmd.e2);
				}
				else {
					commandsPrivate.get(cmd.invoke).executed(safe, cmd.e2);
				}
				return true;
			}
			else {
				return false;
			}
		}
	}
}
