package commands;

import java.awt.Color;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.UserExecution;
import core.UserPrivs;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class User implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");
		if(GuildIni.getUserCommand(e.getGuild().getIdLong())) {
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(() -> {
				final var commandLevel = GuildIni.getUserLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
					final String prefix = GuildIni.getCommandPrefix(e.getGuild().getIdLong());
					if(args.length == 0) {
						UserExecution.getHelp(e);
					}
					else if(args.length > 0) {
						UserExecution.runTask(e, e.getMessage().getContentRaw().replaceAll("[^0-9]", ""), e.getMessage().getContentDisplay().substring(prefix.length()+5));
					}
				}
				else {
					e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(commandLevel, e.getGuild())).build()).queue();
				}
			});
			executor.shutdown();
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		Logger logger = LoggerFactory.getLogger(User.class);
		logger.debug("{} has used User command", e.getMember().getUser().getId());
	}

	@Override
	public String help() {
		return null;
	}
	
}
