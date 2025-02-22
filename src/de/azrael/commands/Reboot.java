package de.azrael.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Reboot implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Reboot.class);

	@Override
	public boolean called(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		return true;
	}

	@Override
	public boolean action(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		if(BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
			e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.REBOOT)).complete();
			e.getJDA().shutdown();
			return true;
		}
		return false;
	}

	@Override
	public void executed(String[] args, boolean success, MessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Reboot command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.REBOOT.getColumn(), out.toString().trim());
		}
	}
}
