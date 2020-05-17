package commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import enums.Translation;
import fileManagement.IniFileReader;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import util.STATIC;

public class Reboot implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Reboot.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		return true;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		if(e.getMember().getUser().getIdLong() == IniFileReader.getAdmin()) {
			e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.REBOOT)).complete();
			e.getJDA().shutdown();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used Reboot command from guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
