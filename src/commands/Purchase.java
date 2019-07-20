package commands;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Purchase implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		e.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("This command is deprecated! Please use the shop command for any kind of purchase!").build()).queue();
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		
	}

	@Override
	public String help() { 
		return null;
	}

}
