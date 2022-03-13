package de.azrael.interfaces;

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public interface CommandPrivate {
	
	boolean called(String[] args, PrivateMessageReceivedEvent e);
	boolean action(String[] args, PrivateMessageReceivedEvent e);
	void executed(String[] args, boolean success, PrivateMessageReceivedEvent e);
}
