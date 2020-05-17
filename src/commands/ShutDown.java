package commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import enums.Translation;
import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import util.STATIC;

/**
 * Shutdown the Bot with this command
 * @author xHelixStorm
 *
 */

public class ShutDown implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(ShutDown.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		return true;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		if(e.getMember().getUser().getIdLong() == IniFileReader.getAdmin()) {
			FileSetting.createFile(IniFileReader.getTempDirectory()+STATIC.getSessionName()+"running.azr", "0");
			e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.SHUTDOWN)).complete();
			e.getJDA().shutdown();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used ShutDown command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
