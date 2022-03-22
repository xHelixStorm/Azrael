package de.azrael.commands;

import java.awt.Color;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.commandsContainer.WriteEditExecution;
import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.core.Hashes;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * The Edit command allows a user to edit any Bot written message
 * and also to apply or remove reactions
 * @author xHelixStorm
 *
 */

public class Edit implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Edit.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.EDIT);
	}

	@Override
	public boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(args.length == 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_HELP)
					.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_REACTION))
					.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_CLEAR_REACTIONS))).build()).queue();
		}
		else if (args.length == 2) {
			String channel_id = args[0].replaceAll("[<>#]", "");
			String message_id = args[1].replaceAll("[^0-9]*", "");
			if(e.getGuild().getTextChannelById(channel_id) != null) {
				checkMessage(e, channel_id, message_id, "", false);
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_NO_TEXT_CHANNEL)).build()).queue();
			}
		}
		else if(args.length == 3) {
			if(args[2].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_REACTION)) || args[2].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CLEAR_REACTIONS))) {
				String channel_id = args[0].replaceAll("[<>#]", "");
				String message_id = args[1].replaceAll("[^0-9]*", "");
				String parameter = args[2].toLowerCase();
				if(e.getGuild().getTextChannelById(channel_id) != null) {
					checkMessage(e, channel_id, message_id, parameter, true);
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_NO_TEXT_CHANNEL)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_NOT_ENOUGH_PARAMS)).build()).queue();
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Edit command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.EDIT.getColumn(), out.toString().trim());
		}
	}

	private static void checkMessage(GuildMessageReceivedEvent e, String channel_id, String message_id, String parameter, final boolean reaction) {
		if(message_id.length() == 17 || message_id.length() == 18) {
			TextChannel textChannel = e.getGuild().getTextChannelById(channel_id);
			if((e.getGuild().getSelfMember().hasPermission(textChannel, Permission.MESSAGE_HISTORY) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.MESSAGE_HISTORY)))) {
				e.getGuild().getTextChannelById(channel_id).retrieveMessageById(message_id).queue(m -> {
					if(!reaction) {
						Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "E", channel_id, message_id));
						WriteEditExecution.editHelp(e, Hashes.getTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId()));
					}
					else {
						final String addReaction = STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_REACTION);
						Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, (parameter.equals(addReaction) ? "RA" : "RC"), channel_id, message_id));
						if(parameter.equals(addReaction)) WriteEditExecution.reactionAddHelp(e, Hashes.getTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId()));
						else WriteEditExecution.runClearReactions(e, Hashes.getTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId()));
					}
				}, err -> {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_NOT_EXISTS)).build()).queue();
				});
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_HISTORY.getName()).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_INVALID_MESSAGE)).build()).queue();
		}
	}
}
