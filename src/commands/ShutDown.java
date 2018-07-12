package commands;

import java.awt.Color;
import java.io.File;

import core.UserPrivs;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ShutDown implements Command{
	
	private static EmbedBuilder message = new EmbedBuilder().setColor(Color.RED);
	File fDenied = new File("./gifs/provocation.gif");

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getShutDownCommand().equals("true")){
			if(e.getMember().getUser().getId().equals(IniFileReader.getAdmin()) || UserPrivs.isUserAdmin(e.getMember().getUser(), e.getGuild().getIdLong())){
				e.getTextChannel().sendMessage("**I'm going to shut down shortly**").queue();
				
				try{
					Thread.sleep(20000);
				}catch(InterruptedException ev){
					ev.printStackTrace();
				}
				e.getTextChannel().sendMessage("**shutting down now. Cya later!**").queue();
				e.getJDA().shutdown();
			}
			else {
				e.getTextChannel().sendMessage(message.setDescription(":warning: " + e.getMember().getEffectiveName() + "**, you don't have the force to command me this. Join the dark side first!**").build()).queue();
				e.getTextChannel().sendFile(fDenied, "provocation.gif", null).complete();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		System.out.println("Bot shutdown commenced!");
	}

	@Override
	public String help() {
		return null;
	}

}
