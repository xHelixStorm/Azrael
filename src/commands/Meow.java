package commands;

import java.awt.Color;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.MeowExecution;
import core.UserPrivs;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
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
				final var commandLevel = GuildIni.getMeowLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
					long guild_id = e.getGuild().getIdLong();
					String path = "./files/Cat/";				
					
					var bot_channels = Azrael.SQLgetChannels(guild_id).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("bot")).collect(Collectors.toList());
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
				}
				else {
					EmbedBuilder message = new EmbedBuilder();
					e.getTextChannel().sendMessage(message.setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(commandLevel, e.getGuild())).build()).queue();
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
