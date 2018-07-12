package commands;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import preparedMessages.AboutText;
import sql.SqlConnect;

public class About implements Command {
	
	private static String message = AboutText.getAbout();

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getAboutCommand().equals("true")){
			long channel = e.getTextChannel().getIdLong();
			long guild_id = e.getGuild().getIdLong();
			SqlConnect.SQLgetChannelID(guild_id, "bot");
			long channel_id = SqlConnect.getChannelID();
			
			if(channel != channel_id && channel_id != 0){
				e.getTextChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in <#"+channel_id+">").queue();
			}
			else{
				e.getTextChannel().sendMessage("```" + message + "```").queue();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		SqlConnect.clearAllVariables();
	}

	@Override
	public String help() {
		return null;
	}
	
	

}
