package interfaces;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public interface CommandPublic {
	
	boolean called(String[] args, GuildMessageReceivedEvent e);
	void action(String[] args, GuildMessageReceivedEvent e);
	void executed(boolean success, GuildMessageReceivedEvent e);
	String help();
}
