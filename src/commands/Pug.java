package commands;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.PugExecution;
import fileManagement.GuildIni;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.Azrael;
import util.STATIC;

public class Pug implements Command{
	
	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(GuildIni.getPugCommand(e.getGuild().getIdLong())){
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(() -> {
				Logger logger = LoggerFactory.getLogger(Pug.class);
				logger.debug("{} has used Pug command", e.getMember().getUser().getId());
				
				long guild_id = e.getGuild().getIdLong();
				String variable = e.getMessage().getContentRaw();
				String path = "./files/Pug/";
				
				var bot_channels = Azrael.SQLgetChannels(guild_id).parallelStream().filter(f -> f.getChannel_Type().equals("bot")).collect(Collectors.toList());
				var this_channel = bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getTextChannel().getIdLong()).findAny().orElse(null);
				
				var execution_id = Azrael.SQLgetExecutionID(guild_id);
				if(execution_id == 0){
					e.getTextChannel().sendMessage("This command is disabled on this server. Please ask an administrator or moderator to activate it!").queue();
				}
				else if(execution_id == 2 || execution_id == 1){
					if(execution_id != 1){
						try {
							PugExecution.Execute(e, variable, path, e.getTextChannel().getIdLong());
						} catch (IOException e1) {
							logger.error("Selected pug picture couldn't be found", e1);
						}
					}
					else{
						if(bot_channels.size() > 0 && this_channel != null){
							try {
								PugExecution.Execute(e, variable, path, this_channel.getChannel_ID());
							} catch (IOException e1) {
								logger.error("Selected pug picture couldn't be found", e1);
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
