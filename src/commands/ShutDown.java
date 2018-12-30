package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.UserPrivs;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ShutDown implements Command{
	
	private static EmbedBuilder message = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getShutDownCommand()){
			Logger logger = LoggerFactory.getLogger(ShutDown.class);
			logger.debug("{} has used ShutDown command", e.getMember().getUser().getId());
			
			if(e.getMember().getUser().getIdLong() == IniFileReader.getAdmin() || UserPrivs.isUserAdmin(e.getMember().getUser(), e.getGuild().getIdLong())){
				e.getTextChannel().sendMessage("**I'm going to shut down shortly**").queue();
				
				try{
					Thread.sleep(20000);
				}catch(InterruptedException ev){
					logger.error("Exception of thread sleep while performing shut down. Bot couldn't shut down", ev);
				}
				e.getTextChannel().sendMessage("**shutting down now. Cya later!**").queue();
				e.getJDA().shutdown();
			}
			else {
				e.getTextChannel().sendMessage(message.setDescription("**" + e.getMember().getAsMention() + ", you don't have the force to command me this. Join the dark side first!**").build()).queue();
			}
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
