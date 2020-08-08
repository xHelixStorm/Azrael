package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import core.UserPrivs;
import enums.Translation;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import sql.DiscordRoles;
import sql.RankingSystem;
import util.STATIC;

/**
 * The remove command allows a user to remove registered 
 * roles, ranking roles, channels and channel filters
 * @author xHelixStorm
 *
 */

public class Remove implements CommandPublic {
	Logger logger = LoggerFactory.getLogger(Remove.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getRemoveCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getRemoveLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		if(args.length == 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS))
				.setDescription(STATIC.getTranslation(e.getMember(), Translation.REMOVE_HELP)).build()).queue();
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ROLE))) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REMOVE_ROLE_HELP)).build()).queue();
		}
		else if(args.length == 2 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ROLE))) {
			if(args[1].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ALL))) {
				final int result = DiscordRoles.SQLUpdateAllRoles(e.getGuild().getIdLong());
				if(result > 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REMOVE_ROLES)).build()).queue();
					Hashes.removeDiscordRoles(e.getGuild().getIdLong());
					Hashes.removeReactionRoles(e.getGuild().getIdLong());
					logger.debug("User {} has removed all registered roles from guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
				}
				else if(result == 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REMOVE_ROLES_ERR)).build()).queue();
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Roles couldn't be removed from DiscordRoles.roles table in guild {}", e.getGuild().getId());
				}
			}
			else {
				if(!args[1].matches("[^\\d]*")) {
					long role = Long.parseLong(args[1]);
					if(e.getGuild().getRoleById(role) != null) {
						if(DiscordRoles.SQLUpdateRole(role, e.getGuild().getIdLong()) > 0) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REMOVE_ROLE)).build()).queue();
							Hashes.removeDiscordRoles(e.getGuild().getIdLong());
							Hashes.removeReactionRoles(e.getGuild().getIdLong());
							logger.debug("User {} has removed the registered role {} from guild {}", e.getMember().getUser().getId(), role, e.getGuild().getId());
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REMOVE_ROLE_ERR)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROLE_NOT_EXISTS)).build()).queue();
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_ROLE_ID)).build()).queue();
				}
			}
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_RANKING_ROLE))) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REMOVE_RANKING_HELP)).build()).queue();
		}
		else if(args.length == 2 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_RANKING_ROLE))) {
			if(args[1].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ALL))) {
				final int result = RankingSystem.SQLclearRoles(e.getGuild().getIdLong());
				if(result > 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REMOVE_RANKING_ROLES)).build()).queue();
					Hashes.removeRankingRoles(e.getGuild().getIdLong());
					logger.debug("User {} has removed all registered ranking roles from guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
				}
				else if(result == 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REMOVE_RANKING_ROLES_ERR)).build()).queue();
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Roles couldn't be removed from RankingSystem.roles table for guild {}", e.getGuild().getId());
				}
			}
			else {
				if(!args[1].matches("[^\\d]*")) {
					long role = Long.parseLong(args[1]);
					if(e.getGuild().getRoleById(role) != null) {
						if(RankingSystem.SQLDeleteRole(role, e.getGuild().getIdLong()) > 0) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REMOVE_RANKING_ROLE)).build()).queue();
							Hashes.removeRankingRoles(e.getGuild().getIdLong());
							logger.debug("User {} has removed the registered ranking role {} from guild {}", e.getMember().getUser().getId(), role, e.getGuild().getId());
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REMOVE_RANKING_ROLE_ERR)).build()).queue();
							logger.warn("Role couldn't be removed from guild {} of DiscordRoles.roles", e.getGuild().getId());
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROLE_NOT_EXISTS)).build()).queue();
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_ROLE_ID)).build()).queue();
				}
			}
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNEL))) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REMOVE_TXT_CHANNEL_HELP)).build()).queue();
		}
		else if(args.length == 2 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNEL))) {
			if(args[1].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ALL))) {
				final int result = Azrael.SQLDeleteAllChannelConfs(e.getGuild().getIdLong());
				if(result > 0) {
					for(var tc : e.getGuild().getTextChannels()) {
						Azrael.SQLDeleteChannel_Filter(tc.getIdLong());
						Hashes.removeFilterLang(tc.getIdLong());
					}
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REMOVE_TXT_CHANNELS)).build()).queue();
					Hashes.removeChannels(e.getGuild().getIdLong());
					logger.debug("User {} has removed all registered text channels from guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
				}
				else if(result == 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REMOVE_TXT_CHANNELS_ERR)).build()).queue();
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Text channels couldn't be removed from Azrael.channel_conf table in guild {}", e.getGuild().getId());
				}
			}
			else {
				var channel_string = args[1].replaceAll("[<>#]*", "");
				if(!channel_string.matches("[^\\d]*")) {
					var channel_id = Long.parseLong(channel_string);
					if(e.getGuild().getTextChannelById(channel_id) != null) {
						if(Azrael.SQLDeleteChannelConf(channel_id, e.getGuild().getIdLong()) > 0) {
							Azrael.SQLDeleteChannel_Filter(channel_id);
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REMOVE_TXT_CHANNEL)).build()).queue();
							Hashes.removeChannels(e.getGuild().getIdLong());
							Hashes.removeFilterLang(channel_id);
							logger.debug("User {} has removed the text channel {} from guild {}", e.getMember().getUser().getId(), channel_id, e.getGuild().getId());
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REMOVE_TXT_CHANNEL_ERR)).build()).queue();
							logger.warn("Text channel couldn't be removed from Azrael.channel_conf table in guild {}", e.getGuild().getId());
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TEXT_CHANNEL_NOT_EXISTS)).build()).queue();
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_TEXT_CHANNEL)).build()).queue();
				}
			}
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CHANNEL_CENSOR))) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REMOVE_CENSOR_HELP)).build()).queue();
		}
		else if(args.length == 2 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CHANNEL_CENSOR))) {
			if(args[1].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ALL))) {
				boolean deleted = false;
				boolean error = false;
				for(final var tc : e.getGuild().getTextChannels()) {
					logger.debug("Removing censor languages for text channel {} in guild {}", tc.getId(), e.getGuild().getId());
					var result = Azrael.SQLDeleteChannel_Filter(tc.getIdLong());
					Hashes.removeFilterLang(tc.getIdLong());
					if(result > 0)   deleted = true;
					if(result == -1) error = true;
				}
				if(deleted) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REMOVE_CENSORS)).build()).queue();
					logger.debug("User {} has removed all registered text channel filters from guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
				}
				else if(!error) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REMOVE_CENSORS_ERR)).build()).queue();
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Censor languages couldn't be removed for all text channels from Azrael.channel_filter table in guild {}", e.getGuild().getId());
				}
			}
			else {
				var channel_string = args[1].replaceAll("[<>#]*", "");
				if(!channel_string.matches("[^\\d]*")) {
					var channel_id = Long.parseLong(channel_string);
					if(e.getGuild().getTextChannelById(channel_id) != null) {
						final int result = Azrael.SQLDeleteChannel_Filter(channel_id); 
						if(result > 0) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REMOVE_CENSOR)).build()).queue();
							Hashes.removeFilterLang(channel_id);
							logger.debug("User {} has removed all registered text channel censor languages for channel {} from guild {}", e.getMember().getUser().getId(), channel_id, e.getGuild().getId());
						}
						else if(result == 0) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REMOVE_CENSOR_ERR)).build()).queue();
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("Censor languages for text channel {} couldn't be removed from Azrael.channel_filter table in guild {}", channel_string, e.getGuild().getId());
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TEXT_CHANNEL_NOT_EXISTS)).build()).queue();
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_TEXT_CHANNEL)).build()).queue();
				}
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used Remove command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}

}
