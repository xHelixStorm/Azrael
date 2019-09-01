package core;

import java.util.ArrayList;

import fileManagement.GuildIni;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public class CommandParser {
	
	public static CommandContainer parser(String raw, GuildMessageReceivedEvent e, PrivateMessageReceivedEvent e2) {
		
		String beheaded = (e != null ? raw.replaceFirst(GuildIni.getCommandPrefix(e.getGuild().getIdLong()), "") : raw);
		String[] splitBeheaded = beheaded.split(" ");
		String invoke = splitBeheaded[0];
		ArrayList <String> split = new ArrayList <String>();
		
		for(String s : splitBeheaded){
			split.add(s);
		}
		String[] args = new String[split.size() -1];
		split.subList(1, split.size()).toArray(args);
		
		return new CommandContainer(raw, beheaded, splitBeheaded, invoke, args, e, e2);
	}
	
	public static class CommandContainer{
		
		public final String raw;
		public final String beheaded;
		public final String[] splitBeheaded;
		public final String invoke;
		public final String[] args;
		public final GuildMessageReceivedEvent e;
		public final PrivateMessageReceivedEvent e2;
		
		public CommandContainer(String _raw, String _beheaded, String[] _splitBeheaded, String _invoke, String[] _args, GuildMessageReceivedEvent _e, PrivateMessageReceivedEvent _e2) {
			this.raw=_raw;
			this.beheaded=_beheaded;
			this.splitBeheaded=_splitBeheaded;
			this.invoke=_invoke;
			this.args=_args;
			this.e=_e;
			this.e2=_e2;
		}
	}

}
