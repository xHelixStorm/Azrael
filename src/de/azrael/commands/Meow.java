package de.azrael.commands;

import java.awt.Color;
import java.io.IOException;
import java.util.EnumSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.commandsContainer.MeowExecution;
import de.azrael.constructors.BotConfigs;
import de.azrael.enums.Channel;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * The Meow command allows a user to print a cat image of choice.
 * @author xHelixStorm
 *
 */

public class Meow implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Meow.class);
	
	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.MEOW);
	}

	@Override
	public boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		long guild_id = e.getGuild().getIdLong();
		final String path = "./files/Cat/";				
		
		//retrieve all bot channels
		var bot_channels = Azrael.SQLgetChannels(guild_id).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.BOT.getType())).collect(Collectors.toList());
		var this_channel = bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null);
		
		//check if any bot channel is registered, else print the image anyway
		if((bot_channels.size() > 0 && this_channel != null) || bot_channels.size() == 0) {
			if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.MESSAGE_ATTACH_FILES) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.MESSAGE_ATTACH_FILES))) {
				try {
					MeowExecution.Execute(e, args, path, (bot_channels.size() > 0 ? this_channel.getChannel_ID() : e.getChannel().getIdLong()));
				} catch (IOException e1) {
					logger.error("Selected cat picture {} couldn't be found", args[1], e1);
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_ATTACH_FILES.getName()).build()).queue();
				logger.error("Permission MESSAGE_ATTACH_FILES required to display a cat picture in channel {} for guild {}", e.getChannel().getId(), e.getGuild().getId());
			}
		}
		else {
			e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.NOT_BOT_CHANNEL)+STATIC.getChannels(bot_channels)).queue();
		}
		return true;
	}
	@Override
	public void executed(String[] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Meow command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.MEOW.getColumn(), out.toString().trim());
		}
	}
}
