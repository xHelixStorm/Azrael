package commands;

import java.awt.Color;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import preparedMessages.HelpText;
import sql.SqlConnect;

public class Commands implements Command{
	private static String message = HelpText.getHelp();

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.MAGENTA).setThumbnail(e.getJDA().getSelfUser().getAvatarUrl()).setTitle("Here are all available commands!");
		if(IniFileReader.getCommandsCommand().equals("true")){
			long guild_id = e.getGuild().getIdLong();
			long channel = e.getTextChannel().getIdLong();
			SqlConnect.SQLgetChannelID(guild_id, "bot");
			long channel_id = SqlConnect.getChannelID();
			
			if(channel != channel_id && channel_id != 0){
				e.getTextChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in <#"+channel_id+">").queue();
			}
			else{
				e.getTextChannel().sendMessage(messageBuild.setDescription(message).build()).queue();		
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
