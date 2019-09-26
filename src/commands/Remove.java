package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import sql.DiscordRoles;
import sql.RankingSystem;

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
		//var adminPermission = (GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong());
		if(args.length == 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.WHITE)
				.setDescription("Use this command to remove registered settings! Write the parameter together with the command for more details!\n"
						+ "**-role**\n"
						+ "**-ranking-role**\n"
						+ "**-channel**\n"
						+ "**-channel-filter**").build()).queue();
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase("-role")) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.WHITE).setDescription("Insert the role id together with the full command to unregister that role or type **all** to remove all registerd roles!").build()).queue();
		}
		else if(args.length == 2 && args[0].equalsIgnoreCase("-role")) {
			if(args[1].equalsIgnoreCase("all")) {
				if(DiscordRoles.SQLDeleteAllRoles(e.getGuild().getIdLong()) > 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.WHITE).setDescription("All regular registered roles have been removed from the database!").build()).queue();
					Hashes.removeDiscordRoles(e.getGuild().getIdLong());
					Hashes.removeReactionRoles(e.getGuild().getIdLong());
					logger.debug("User {} has removed all registered roles from guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Roles couldn't be removed! Either there are no roles to remove or an internal error has occurred!").build()).queue();
					logger.warn("Roles couldn't be removed from guild {} of DiscordRoles.roles", e.getGuild().getId());
				}
			}
			else {
				if(!args[1].matches("[^\\d]*")) {
					long role = Long.parseLong(args[1]);
					if(e.getGuild().getRoleById(role) != null) {
						if(DiscordRoles.SQLDeleteRole(role, e.getGuild().getIdLong()) > 0) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.WHITE).setDescription("Role has been successfully removed from the database!").build()).queue();
							Hashes.removeDiscordRoles(e.getGuild().getIdLong());
							Hashes.removeReactionRoles(e.getGuild().getIdLong());
							logger.debug("User {} has removed the registered role {} from guild {}", e.getMember().getUser().getId(), role, e.getGuild().getId());
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Role couldn't be removed! Either this role has never been registered or an internal error has occurred!").build()).queue();
							logger.warn("Role couldn't be removed from guild {} of DiscordRoles.roles", e.getGuild().getId());
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Role doesn't exist on the server!").build()).queue();
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Please submit a valid role id!").build()).queue();
				}
			}
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase("-ranking-role")) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.WHITE).setDescription("Insert a role id together with the full command to remove a registered ranking role or type **all** to remove all registerd ranking role!").build()).queue();
		}
		else if(args.length == 2 && args[0].equalsIgnoreCase("-ranking-role")) {
			if(args[1].equalsIgnoreCase("all")) {
				if(RankingSystem.SQLclearRoles(e.getGuild().getIdLong()) > 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.WHITE).setDescription("All regular registered ranking roles have been removed from the database!").build()).queue();
					Hashes.removeRankingRoles(e.getGuild().getIdLong());
					logger.debug("User {} has removed all registered ranking roles from guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
				}
				else {
					e.getChannel().sendMessage("An internal error occurred. Roles couldn't be cleared from the RankingSystem.roles table").queue();
					logger.error("Roles couldn't be cleared from RankingSystem.roles table");
				}
			}
			else {
				if(!args[1].matches("[^\\d]*")) {
					long role = Long.parseLong(args[1]);
					if(e.getGuild().getRoleById(role) != null) {
						if(RankingSystem.SQLDeleteRole(role, e.getGuild().getIdLong()) > 0) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.WHITE).setDescription("Ranking role has been successfully removed from the database!").build()).queue();
							Hashes.removeRankingRoles(e.getGuild().getIdLong());
							logger.debug("User {} has removed the registered ranking role {} from guild {}", e.getMember().getUser().getId(), role, e.getGuild().getId());
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Ranking Role couldn't be removed! Either this ranking role is not registered or an internal error has occurred!").build()).queue();
							logger.warn("Role couldn't be removed from guild {} of DiscordRoles.roles", e.getGuild().getId());
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Role doesn't exist on the server!").build()).queue();
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Please submit a valid role id!").build()).queue();
				}
			}
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase("-channel")) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.WHITE).setDescription("Insert a channel together with the full command to remove a registered text channel together will all filters or type **all** to remove all registerd text channels!").build()).queue();
		}
		else if(args.length == 2 && args[0].equalsIgnoreCase("-channel")) {
			if(args[1].equalsIgnoreCase("all")) {
				if(Azrael.SQLDeleteAllChannelConfs(e.getGuild().getIdLong()) > 0) {
					for(var tc : e.getGuild().getTextChannels()) {
						Azrael.SQLDeleteChannel_Filter(tc.getIdLong());
						Hashes.removeFilterLang(tc.getIdLong());
					}
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.WHITE).setDescription("All regular registered text channels have been removed from the database!").build()).queue();
					Hashes.removeChannels(e.getGuild().getIdLong());
					logger.debug("User {} has removed all registered text channels from guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Text channels couldn't be removed! Either there are no registered text channels to remove or an internal error has occurred!").build()).queue();
					logger.warn("Text channel couldn't be removed from guild {} of Azrael.channel_conf", e.getGuild().getId());
				}
			}
			else {
				var channel_string = args[1].replaceAll("[<>#]*", "");
				if(!channel_string.matches("[^\\d]*")) {
					var channel_id = Long.parseLong(channel_string);
					if(e.getGuild().getTextChannelById(channel_id) != null) {
						if(Azrael.SQLDeleteChannelConf(channel_id, e.getGuild().getIdLong()) > 0) {
							Azrael.SQLDeleteChannel_Filter(channel_id);
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.WHITE).setDescription("This registered text channel has been removed!").build()).queue();
							Hashes.removeChannels(e.getGuild().getIdLong());
							Hashes.removeFilterLang(channel_id);
							logger.debug("User {} has removed the text channel {} from guild {}", e.getMember().getUser().getId(), channel_id, e.getGuild().getId());
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Text channel couldn't be removed! Either this text channel is not registered or an internal error has occurred!").build()).queue();
							logger.warn("Text channel couldn't be removed from guild {} of Azrael.channel_conf", e.getGuild().getId());
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("The provided text channel doesn't exist in this server!").build()).queue();
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Please submit a valid text channel tag or text channel id!").build()).queue();
				}
			}
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase("-channel-filter")) {
			if(args[1].equalsIgnoreCase("all")) {
				var deleted = false;
				for(final var tc : e.getGuild().getTextChannels()) {
					var result = Azrael.SQLDeleteChannel_Filter(tc.getIdLong());
					Hashes.removeFilterLang(tc.getIdLong());
					if(result > 0) deleted = true; 
				}
				if(deleted) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.WHITE).setDescription("All regular registered text channel filters have been removed from the database for this server!").build()).queue();
					logger.debug("User {} has removed all registered text channel filters from guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Registered text channel filters couldn't be removed! Either no filter has been set or an internal error has occurred!").build()).queue();
					logger.warn("Text channel filters couldn't be removed from guild {} of Azrael.channel_filter", e.getGuild().getId());
				}
			}
			else {
				var channel_string = args[1].replaceAll("[<>#]*", "");
				if(!channel_string.matches("[^\\d]*")) {
					var channel_id = Long.parseLong(channel_string);
					if(e.getGuild().getTextChannelById(channel_id) != null) {
						if(Azrael.SQLDeleteChannel_Filter(channel_id) > 0) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.WHITE).setDescription("All registered text channel filters for this text channel have been removed from the database for this server!").build()).queue();
							Hashes.removeFilterLang(channel_id);
							logger.debug("User {} has removed all registered text channel filters for channel {} from guild {}", e.getMember().getUser().getId(), channel_id, e.getGuild().getId());
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("This text channel filters couldn't be removed! Either no filter has been set for this text channel or an internal error has occurred!").build()).queue();
							logger.warn("Text channel filters couldn't be removed from guild {} of Azrael.channel_filter", e.getGuild().getId());
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("The provided text channel doesn't exist in this server!").build()).queue();
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Please submit a valid text channel tag or text channel id!").build()).queue();
				}
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("The wrong syntax has been used! Please retry.").build()).queue();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used Remove command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}

}
