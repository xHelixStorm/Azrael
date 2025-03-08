package de.azrael.commands;

import java.awt.Color;
import java.util.EnumSet;

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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * The Edit command allows a user to edit any Bot written message
 * and also to apply or remove reactions
 * @author xHelixStorm
 *
 */

public class Edit implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Edit.class);

	@Override
	public boolean called(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.EDIT);
	}

	@Override
	public boolean action(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		if(args.length == 0) {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_HELP)
					.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_REACTION))
					.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_CLEAR_REACTIONS))).build()).queue();
		}
		else if (args.length == 2) {
			String channelId = args[0].replaceAll("[^0-9]*", "");
			String messageId = args[1].replaceAll("[^0-9]*", "");
			if(channelId.length() > 0 && messageId.length() > 0) {
				if(e.getGuild().getTextChannelById(channelId) != null) {
					checkMessage(e, channelId, messageId, "", false);
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_NO_TEXT_CHANNEL)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
		}
		else if(args.length == 3) {
			if(args[2].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_REACTION)) || args[2].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CLEAR_REACTIONS))) {
				String channelId = args[0].replaceAll("[^0-9]*", "");
				String messageId = args[1].replaceAll("[^0-9]*", "");
				String parameter = args[2].toLowerCase();
				if(e.getGuild().getTextChannelById(channelId) != null) {
					checkMessage(e, channelId, messageId, parameter, true);
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_NO_TEXT_CHANNEL)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_NOT_ENOUGH_PARAMS)).build()).queue();
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, MessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Edit command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.EDIT.getColumn(), out.toString().trim());
		}
	}

	private static void checkMessage(MessageReceivedEvent e, String channelId, String messageId, String parameter, final boolean reaction) {
		if(messageId.length() == 17 || messageId.length() == 18) {
			TextChannel textChannel = e.getGuild().getTextChannelById(channelId);
			if((e.getGuild().getSelfMember().hasPermission(textChannel, Permission.MESSAGE_HISTORY) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.MESSAGE_HISTORY)))) {
				textChannel.retrieveMessageById(messageId).queue(m -> {
					if(!reaction) {
						WriteEditExecution.editHelp(e, channelId, messageId);
					}
					else {
						final String addReaction = STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_REACTION);
						final String clearReactions = STATIC.getTranslation(e.getMember(), Translation.PARAM_CLEAR_REACTIONS);
						if(parameter.equalsIgnoreCase(addReaction)) 
							WriteEditExecution.reactionAddHelp(e, channelId, messageId);
						else if(parameter.equalsIgnoreCase(clearReactions)) 
							WriteEditExecution.runClearReactions(e, channelId, messageId);
						else
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();					
					}
				}, err -> {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_NOT_EXISTS)).build()).queue();
				});
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_HISTORY.getName()).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_INVALID_MESSAGE)).build()).queue();
		}
	}
}
