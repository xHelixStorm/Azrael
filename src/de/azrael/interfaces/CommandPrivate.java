package de.azrael.interfaces;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface CommandPrivate {
	
	boolean called(String[] args, MessageReceivedEvent e);
	boolean action(String[] args, MessageReceivedEvent e);
	void executed(String[] args, boolean success, MessageReceivedEvent e);
}
