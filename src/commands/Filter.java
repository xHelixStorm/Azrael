package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.FilterExecution;
import core.UserPrivs;
import fileManagement.GuildIni;
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
		if(GuildIni.getFilterCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getFilterLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
				if(args.length == 0) {
					FilterExecution.runHelp(e);
				}
				else if(args.length > 0) {
					FilterExecution.runTask(e, args[0]);
				}
			}
			else {
				EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");
				e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(commandLevel, e.getGuild().getRoles())).build()).queue();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		Logger logger = LoggerFactory.getLogger(Filter.class);
		logger.debug("{} has used the Filter command", e.getMember().getUser().getId());
	}

	@Override
	public String help() {
		return null;
	}

}
