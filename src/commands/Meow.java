package commands;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.MeowExecution;
import fileManagement.GuildIni;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.Azrael;
import util.STATIC;

public class Meow implements Command{
	
	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(GuildIni.getMeowCommand(e.getGuild().getIdLong())){
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(() -> {
				Logger logger = LoggerFactory.getLogger(Meow.class);
				logger.debug("{} has used Meow command", e.getMember().getUser().getId());
				long guild_id = e.getGuild().getIdLong();
				String path = "./files/Cat/";				
				
				var bot_channels = Azrael.SQLgetChannels(guild_id).parallelStream().filter(f -> f.getChannel_Type().equals("bot")).collect(Collectors.toList());
				var this_channel = bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getTextChannel().getIdLong()).findAny().orElse(null);
				
				var execution_id = Azrael.SQLgetExecutionID(guild_id);
				if(execution_id == 0){
					e.getTextChannel().sendMessage("This command is disabled on this server. Please ask an administrator or moderator to enable it!").queue();
				}
				else if(execution_id == 2 || execution_id == 1){
					if(execution_id != 1){
						try {
							MeowExecution.Execute(e, args, path, e.getTextChannel().getIdLong());
						} catch (IOException e1) {
							logger.error("Selected meow picture couldn't be found", e1);
						}
					}
					else{
						if(bot_channels.size() > 0 && this_channel != null){
							try {
								MeowExecution.Execute(e, args, path, this_channel.getChannel_ID());
							} catch (IOException e1) {
								logger.error("Selected meow picture couldn't be found", e1);
							}
						}
						else{
							e.getTextChannel().sendMessage("This command can be used only in "+STATIC.getChannels(bot_channels)).queue();
						}
					}
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
