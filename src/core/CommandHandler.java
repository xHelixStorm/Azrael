package core;

import java.util.HashMap;

import interfaces.CommandPrivate;
import interfaces.CommandPublic;

public class CommandHandler {

	public static CommandParser parse = new CommandParser();
	public static HashMap<String, CommandPublic> commandsPublic = new HashMap<>();
	public static HashMap<String, CommandPrivate> commandsPrivate = new HashMap<>();
	
	public static boolean handleCommand(CommandParser.CommandContainer cmd) {
		
		if(cmd.e != null) {
			if(commandsPublic.containsKey(cmd.invoke)) {
				boolean safe = commandsPublic.get(cmd.invoke).called(cmd.args, cmd.e);
				
				if(safe){
					commandsPublic.get(cmd.invoke).action(cmd.args, cmd.e);
					commandsPublic.get(cmd.invoke).executed(safe, cmd.e);
				}
				else{
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
