package interfaces;

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public interface CommandPrivate {
	
	boolean called(String[] args, PrivateMessageReceivedEvent e);
	void action(String[] args, PrivateMessageReceivedEvent e);
	void executed(boolean success, PrivateMessageReceivedEvent e);
}
