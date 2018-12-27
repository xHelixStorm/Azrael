package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.FilterExecution;
import core.UserPrivs;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Filter implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getFilterCommand()) {			
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");
			if(UserPrivs.isUserAdmin(e.getMember().getUser(), e.getGuild().getIdLong()) || UserPrivs.isUserMod(e.getMember().getUser(), e.getGuild().getIdLong()) || e.getMember().getUser().getIdLong() == IniFileReader.getAdmin()) {
				if(e.getMessage().getContentRaw().equals(IniFileReader.getCommandPrefix()+"filter")) {
					FilterExecution.runHelp(e);
				}
				else if(e.getMessage().getContentRaw().contains(IniFileReader.getCommandPrefix()+"filter ")) {
					FilterExecution.runTask(e, e.getMessage().getContentRaw().substring(IniFileReader.getCommandPrefix().length()+7));
				}
			}
			else {
				e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() +" **My apologies young padawan. This command can be used only from an Administrator or an Moderator. Here a cookie** :cookie:").build()).queue();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		Logger logger = LoggerFactory.getLogger(Filter.class);
		logger.info("{} has used the Filter command", e.getMember().getUser().getId());
	}

	@Override
	public String help() {
		return null;
	}

}
