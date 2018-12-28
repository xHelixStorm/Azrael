package commands;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.MeowExecution;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.Azrael;

public class Meow implements Command{
	
	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getMeowCommand()){
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(() -> {
				Logger logger = LoggerFactory.getLogger(Meow.class);
				logger.info("{} has used Meow command", e.getMember().getUser().getId());
				long channel = e.getTextChannel().getIdLong();
				long guild_id = e.getGuild().getIdLong();
				String variable = e.getMessage().getContentRaw();
				String path = "./files/Cat/";
				
				Azrael.SQLgetChannelID(guild_id, "bot");
				long channel_id = Azrael.getChannelID();
				
				if(variable.equals("H!meow")){
					e.getTextChannel().sendMessage("Please, type H!meow help to check the usage").queue();
				}
				else{
					Azrael.SQLgetExecutionID(guild_id);
					if(Azrael.getExecutionID() == 0){
						e.getTextChannel().sendMessage("This type of command is disabled on this server. Please ask an administrator or moderator to enable it!").queue();
					}
					else if(Azrael.getExecutionID() == 2 || Azrael.getExecutionID() == 1){
						if(Azrael.getExecutionID() != 1){
							try {
								MeowExecution.Execute(e, variable, path, channel_id);
							} catch (IOException e1) {
								logger.warn("Selected meow picture couldn't be found", e1);
							}
						}
						else{
							if(channel_id == channel){
								try {
									MeowExecution.Execute(e, variable, path, channel_id);
								} catch (IOException e1) {
									logger.warn("Selected meow picture couldn't be found", e1);
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
		Azrael.clearAllVariables();
	}

	@Override
	public String help() {
		return null;
	}
	
}
