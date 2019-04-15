package core;

import java.util.HashMap;

import commands.Command;

public class CommandHandler {

		public static CommandParser parse = new CommandParser();
		public static HashMap<String, Command> commands = new HashMap<>();
		
		public static boolean handleCommand(CommandParser.CommandContainer cmd){
			
			if(commands.containsKey(cmd.invoke)){
				boolean safe = commands.get(cmd.invoke).called(cmd.args, cmd.e);
				
				if(!safe){
					commands.get(cmd.invoke).action(cmd.args, cmd.e);
					commands.get(cmd.invoke).executed(safe, cmd.e);
				}
				else{
					commands.get(cmd.invoke).executed(safe, cmd.e);
				}
				return true;
			}
			else {
				return false;
			}
		}
}
