package commands;

import java.awt.Color;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import commandsContainer.UserExecution;
import core.UserPrivs;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class User implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");
		if(IniFileReader.getUserCommand()) {
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(() -> {
				if(UserPrivs.isUserAdmin(e.getMember().getUser(), e.getGuild().getIdLong()) || UserPrivs.isUserMod(e.getMember().getUser(), e.getGuild().getIdLong()) || e.getMember().getUser().getIdLong() == IniFileReader.getAdmin()) {
					if(e.getMessage().getContentRaw().equals(IniFileReader.getCommandPrefix()+"user")) {
						UserExecution.getHelp(e);
					}
					else if(e.getMessage().getContentRaw().contains(IniFileReader.getCommandPrefix()+"user ")) {
						UserExecution.runTask(e, e.getMessage().getContentRaw().replaceAll("[^0-9]", ""), e.getMessage().getContentDisplay().substring(IniFileReader.getCommandPrefix().length()+5));
					}
				}
				else {
					e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. This command can be used only from an Administrator or an Moderator. Here a cookie** :cookie:").build()).queue();
				}
			});
			executor.shutdown();
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		
	}

	@Override
	public String help() {
		return null;
	}
	
}
