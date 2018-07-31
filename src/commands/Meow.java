package commands;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import commandsContainer.MeowExecution;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.SqlConnect;

public class Meow implements Command{
	
	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getMeowCommand().equals("true")){
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(() -> {
				long channel = e.getTextChannel().getIdLong();
				long guild_id = e.getGuild().getIdLong();
				String variable = e.getMessage().getContentRaw();
				String path = "./files/Cat/";
				
				SqlConnect.SQLgetChannelID(guild_id, "bot");
				long channel_id = SqlConnect.getChannelID();
				
				if(variable.equals("H!meow")){
					e.getTextChannel().sendMessage("Please, type H!meow help to check the usage").queue();
				}
				else{
					SqlConnect.SQLgetExecutionID(guild_id);
					if(SqlConnect.getExecutionID() == 0){
						e.getTextChannel().sendMessage("This type of command is disabled on this server. Please ask an administrator or moderator to enable it!").queue();
					}
					else if(SqlConnect.getExecutionID() == 2 || SqlConnect.getExecutionID() == 1){
						if(SqlConnect.getExecutionID() != 1){
							try {
								MeowExecution.Execute(e, variable, path, channel_id);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
						else{
							if(channel_id == channel){
								try {
									MeowExecution.Execute(e, variable, path, channel_id);
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
							else{
								e.getTextChannel().sendMessage("This command can be used only in <#"+channel_id+">").queue();
							}
						}
					}
				}
			});
			executor.shutdown();
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
