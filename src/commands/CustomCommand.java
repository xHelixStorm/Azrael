package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.UserPrivs;
import enums.CommandAction;
import enums.Translation;
import fileManagement.GuildIni;
import google.GoogleYoutube;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import util.STATIC;

public class CustomCommand implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(CustomCommand.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		return true;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		//Retrieve the command name only without prefix
		final String cmd = e.getMessage().getContentRaw().split(" ")[0].substring(GuildIni.getCommandPrefix(e.getGuild().getIdLong()).length());
		final var command = Azrael.SQLgetCustomCommand(e.getGuild().getIdLong(), cmd);
		if(command != null && command.isEnabled()) {
			if(UserPrivs.comparePrivilege(e.getMember(), command.getLevel()) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
				if(command.getAction().inputRequired && args.length == 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Input is required!").build()).queue();
					return;
				}
				
				String out = command.getOutput();
				if(out == null) {
					out = "";
				}
				else {
					//TODO: format output message by replacing variables in message
				}
				
				if(command.getAction() == CommandAction.YOUTUBE) {
					String input = "";
					for(String arg : args) {
						input += arg+" ";
					}
					try {
						GoogleYoutube.searchYouTubeVideo(GoogleYoutube.getService(), input.trim(), 1);
					} catch (Exception e1) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("YouTube query couldn't be executed for guild {}", e.getGuild().getId(), e1);
					}
				}
				
				TextChannel textChannel = e.getChannel();
				if(command.getTargetChannel() > 0) {
					textChannel = e.getGuild().getTextChannelById(command.getTargetChannel());
					if(textChannel == null)
						textChannel = e.getChannel();
				}
				
				e.getGuild().getTextChannelById(textChannel.getIdLong()).sendMessage(out);
			}
			else {
				UserPrivs.throwNotEnoughPrivilegeError(e, command.getLevel());
			}
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used the custom command {} in guild {}", e.getMember().getUser().getId(), e.getMessage().getContentRaw().split(" ")[0].toUpperCase(), e.getGuild().getId());
	}

}
