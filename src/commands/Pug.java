package commands;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.PugExecution;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.Azrael;

public class Pug implements Command{
	
	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getPugCommand()){
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(() -> {
				long channel = e.getTextChannel().getIdLong();
				long guild_id = e.getGuild().getIdLong();
				String variable = e.getMessage().getContentRaw();
				String path = "./files/Pug/";
				
				Azrael.SQLgetChannelID(guild_id, "bot");
				long channel_id = Azrael.getChannelID();
				
				if(variable.equals("H!pug")){
					e.getTextChannel().sendMessage("Please, type H!pug help to check the usage").queue();
				}
				else{
					Azrael.SQLgetExecutionID(guild_id);
					if(Azrael.getExecutionID() == 0){
						e.getTextChannel().sendMessage("This type of command is disabled on this server. Please ask an administrator or moderator to activate it!").queue();
					}
					else if(Azrael.getExecutionID() == 2 || Azrael.getExecutionID() == 1){
						if(Azrael.getExecutionID() != 1){
							try {
								PugExecution.Execute(e, variable, path, channel_id);
							} catch (IOException e1) {
								System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
								e1.printStackTrace();
							}
						}
						else{
							if(channel_id == channel){
								try {
									PugExecution.Execute(e, variable, path, channel_id);
								} catch (IOException e1) {
									System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
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
		Logger logger = LoggerFactory.getLogger(Pug.class);
		logger.info("{} has used Pug command", e.getMember().getUser().getId());
		Azrael.clearAllVariables();
	}

	@Override
	public String help() {
		return null;
	}
	
}
