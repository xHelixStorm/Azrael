package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Reboot implements Command{
	private static EmbedBuilder message = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(e.getMember().getUser().getIdLong() == IniFileReader.getAdmin()){
			e.getTextChannel().sendMessage("**Now rebooting!**").queue();
			e.getJDA().shutdown();
		}
		else {
			e.getTextChannel().sendMessage(message.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. The highest instance privilege is required. Here a cookie** :cookie:").build()).queue();
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		Logger logger = LoggerFactory.getLogger(Reboot.class);
		logger.debug("{} has used Reboot command", e.getMember().getUser().getId());
	}

	@Override
	public String help() {
		return null;
	}

}
