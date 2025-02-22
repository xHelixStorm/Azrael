package de.azrael.commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.commands.util.WriteEditExecution;
import de.azrael.constructors.BotConfigs;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * This command allows a user to submit a message as the Bot.
 * To use by mentioning the text channel together with the command.
 * @author xHelixStorm
 *
 */

public class Write implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Write.class);

	@Override
	public boolean called(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.WRITE);
	}

	@Override
	public boolean action(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		if(args.length == 0) {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.WRITE_HELP)).build()).queue();
		}
		else if (args.length >= 1 && args.length <= 2) {
			String channelId = args[0].replaceAll("[^0-9]*", "");
			if(channelId.length() > 0) {
				if(e.getGuild().getTextChannelById(channelId) != null) {
					String delay = null;
					if(args.length == 2) {
						if(args[1].replaceAll("[0-9]*", "").length() == 0) {
							delay = args[1];
						}
						else {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
							return true;
						}
					}
					WriteEditExecution.writeHelp(e, channelId, delay);
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TEXT_CHANNEL_NOT_EXISTS)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, MessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Write command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.WRITE.getColumn(), out.toString().trim());
		}
	}
	
}
