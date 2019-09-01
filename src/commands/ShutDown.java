package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class ShutDown implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(ShutDown.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		return true;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		if(e.getMember().getUser().getIdLong() == IniFileReader.getAdmin()) {
			FileSetting.createFile(IniFileReader.getTempDirectory()+"running.azr", "0");
			e.getChannel().sendMessage("**shutting down now. Cya later!**").queue();
			e.getJDA().shutdown();
		}
		else {
			EmbedBuilder message = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");
			e.getChannel().sendMessage(message.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. The highest instance privilege is required. Here a cookie** :cookie:").build()).queue();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used ShutDown command", e.getMember().getUser().getId());
	}

	@Override
	public String help() {
		return null;
	}

}
