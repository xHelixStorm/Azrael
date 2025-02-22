package de.azrael.interfaces;

import de.azrael.constructors.BotConfigs;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface CommandPublic {
	
	boolean called(String[] args, MessageReceivedEvent e, BotConfigs botConfig);
	boolean action(String[] args, MessageReceivedEvent e, BotConfigs botConfig);
	void executed(String[] args, boolean success, MessageReceivedEvent e, BotConfigs botConfig);
}
