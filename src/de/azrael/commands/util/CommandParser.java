package de.azrael.commands.util;

import java.util.ArrayList;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandParser {
	
	public static CommandContainer parser(String prefix, String raw, MessageReceivedEvent e) {
		
		String beheaded = (e != null ? raw.replaceFirst(prefix, "") : raw);
		String[] splitBeheaded = beheaded.split(" ");
		String invoke = splitBeheaded[0];
		ArrayList <String> split = new ArrayList <String>();
		
		for(String s : splitBeheaded){
			split.add(s);
		}
		String[] args = new String[split.size() -1];
		split.subList(1, split.size()).toArray(args);
		
		return new CommandContainer(raw, beheaded, splitBeheaded, invoke, args, e);
	}
	
	public static class CommandContainer{
		
		public final String raw;
		public final String beheaded;
		public final String[] splitBeheaded;
		public final String invoke;
		public final String[] args;
		public final MessageReceivedEvent e;
		
		public CommandContainer(String _raw, String _beheaded, String[] _splitBeheaded, String _invoke, String[] _args, MessageReceivedEvent _e) {
			this.raw=_raw;
			this.beheaded=_beheaded;
			this.splitBeheaded=_splitBeheaded;
			this.invoke=_invoke;
			this.args=_args;
			this.e=_e;
		}
	}

}
