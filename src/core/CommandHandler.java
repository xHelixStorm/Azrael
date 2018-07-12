package core;

import java.util.HashMap;

import commands.Command;

public class CommandHandler {

		public static CommandParser parse = new CommandParser();
		public static HashMap<String, Command> commands = new HashMap<>();
		
		public static void handleCommand(CommandParser.CommandContainer cmd){
			
			if(commands.containsKey(cmd.invoke)){
				
			}
			else {
				System.out.println("Command doesn't exist!");
			}
			
			boolean safe = commands.get(cmd.invoke).called(cmd.args, cmd.e);
			
			if(!safe){
				commands.get(cmd.invoke).action(cmd.args, cmd.e);
				commands.get(cmd.invoke).executed(safe, cmd.e);
			}
			else{
				commands.get(cmd.invoke).executed(safe, cmd.e);
			}
			
		}
}
