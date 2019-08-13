package filter;

import java.util.ArrayList;

import fileManagement.GuildIni;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class URLFilter implements Runnable {
	MessageReceivedEvent e;
	ArrayList<String> blacklist;
	ArrayList<String> whitelist;
	
	public URLFilter(MessageReceivedEvent _e, ArrayList<String> _blacklist, ArrayList<String> _whitelist) {
		this.e = _e;
		this.blacklist = _blacklist;
		this.whitelist = _whitelist;
	}

	@Override
	public void run() {
		if(GuildIni.getURLBlacklist(e.getGuild().getIdLong())) {
			if(e.getMessage().getContentRaw().matches("[-a-zA-Z0-9@:%._\\\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&\\/=]*)")) {
				//TO DO: confront link with the whitelist table and delete if nothing has been found
			}
		}
		else {
			//TO DO: confront link with the blacklist table and delete if found
		}
	}

}
