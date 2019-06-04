package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.UserPrivs;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Help implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(GuildIni.getHelpCommand(e.getGuild().getIdLong())) {
			if(UserPrivs.comparePrivilege(e.getMember(), GuildIni.getHelpLevel(e.getGuild().getIdLong())) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
				e.getTextChannel().sendMessage("Here all listed issues about S4. If you got something to add or to ask about a specific point, poke a GM\nhttps://s4league.aeriagames.com/forum/index.php?thread/52-guide-general-technical-issues/").queue();
			}
			else {
				EmbedBuilder message = new EmbedBuilder();
				e.getTextChannel().sendMessage(message.setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:").build()).queue();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		Logger logger = LoggerFactory.getLogger(Help.class);
		logger.debug("{} has used Help command", e.getMember().getUser().getId());
	}

	@Override
	public String help() {
		return null;
	}

}
