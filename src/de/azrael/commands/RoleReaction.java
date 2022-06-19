package de.azrael.commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.core.Hashes;
import de.azrael.enums.Channel;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.preparedMessages.ReactionMessage;
import de.azrael.sql.Azrael;
import de.azrael.sql.DiscordRoles;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Enable and disable reactions on the server
 */

public class RoleReaction implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(RoleReaction.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.ROLE_REACTION);
	}

	@Override
	public boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		//after a channel has been registered for self role assignment, it can be disabled and enabled with this command
		if(args.length > 0 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE))) {
			if(Azrael.SQLgetCommandExecutionReaction(e.getGuild().getIdLong()) == true) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROLE_REACTION_ENABLED)).build()).queue();
			}
			else {
				if(Azrael.SQLUpdateReaction(e.getGuild().getIdLong(), true) > 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROLE_REACTION_ENABLE)).build()).queue();
					logger.info("User {} has enabled role reactions in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
					var rea_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.REA.getType())).findAny().orElse(null);
					if(rea_channel != null) ReactionMessage.print(e, rea_channel.getChannel_ID(), botConfig);
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Role reactions couldn't be enabled in guild {}", e.getGuild().getId());
				}
			}
		}
		else if(args.length > 0 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE))) {
			if(Azrael.SQLgetCommandExecutionReaction(e.getGuild().getIdLong()) == false) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROLE_REACTION_DISABLED)).build()).queue();
			}
			else {
				if(Azrael.SQLUpdateReaction(e.getGuild().getIdLong(), false) > 0) {
					var rea_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.REA.getType())).findAny().orElse(null);
					if(rea_channel != null && Hashes.getReactionMessage(e.getGuild().getIdLong()) != null) {
						e.getGuild().getTextChannelById(rea_channel.getChannel_ID()).deleteMessageById(Hashes.getReactionMessage(e.getGuild().getIdLong())).queue();
					}
					var reactionRoles = DiscordRoles.SQLgetReactionRoles(e.getGuild().getIdLong());
					if(reactionRoles != null && reactionRoles.size() > 0) {
						if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
							for(int i = 0; i < reactionRoles.size(); i++) {
								if(!reactionRoles.get(i).isPersistent()) {
									long role_id = reactionRoles.get(i).getRole_ID();
									e.getGuild().getMembersWithRoles(e.getGuild().getRoleById(role_id)).parallelStream().forEach(m -> {
										e.getGuild().removeRoleFromMember(m, e.getGuild().getRoleById(role_id)).queue();
									});
								}
							}
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MANAGE_ROLES.getName()).build()).queue();
							logger.warn("MANAGE_ROLES permission required to remove roles from users in guild {}", e.getGuild().getId());
						}
					}
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROLE_REACTION_DISABLE)).build()).queue();
					logger.info("User {} has disabled role reactions in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Role reactions couldn't be disabled in guild {}", e.getGuild().getId());
				}
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROLE_REACTION_HELP)
					.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE))
					.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE))).build()).queue();
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used RoleReaction command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.ROLE_REACTION.getColumn(), out.toString().trim());
		}
	}
}
