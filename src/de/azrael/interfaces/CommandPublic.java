package de.azrael.interfaces;

import de.azrael.constructors.BotConfigs;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public interface CommandPublic {
	
	boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig);
	boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig);
	void executed(String[] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig);
}
