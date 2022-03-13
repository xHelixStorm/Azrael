package de.azrael.commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.commandsContainer.RegisterCategory;
import de.azrael.commandsContainer.RegisterChannel;
import de.azrael.commandsContainer.RegisterRankingRole;
import de.azrael.commandsContainer.RegisterRole;
import de.azrael.constructors.BotConfigs;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.IniFileReader;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.sql.DiscordRoles;
import de.azrael.threads.CollectUsers;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Register implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Register.class);
	
	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(STATIC.getCommandEnabled(e.getGuild(), Command.REGISTER)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getSettingsThumbnail());
		var guild_id = e.getGuild().getIdLong();
		var adminPermission = BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
		if(DiscordRoles.SQLgetRoles(guild_id).parallelStream().filter(f -> f.getCategory_ABV().equals("adm")).findAny().orElse(null) == null) {
			if(args.length == 0) {
				if(adminPermission) {
					e.getChannel().sendMessage(messageBuild.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_HELP_1)).build()).queue();
					return true;
				}
				else {
					EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DENIED));
					e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_REQUIRED)).build()).queue();
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ROLE))) {
				if(adminPermission) {
					RegisterRole.RegisterRoleHelper(e);
					return true;
				}
				else {
					EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DENIED));
					e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_REQUIRED)).build()).queue();
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ROLE))) {
				return RegisterRole.runCommandWithAdminFirst(e, guild_id, args, adminPermission);
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
				return true;
			}
		}
		else if(UserPrivs.comparePrivilege(e.getMember(), STATIC.getCommandLevel(e.getGuild(), Command.REGISTER)) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
			if(args.length == 0) {
				e.getChannel().sendMessage(messageBuild.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_HELP_2)).build()).queue();
				return true;
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ROLE))) {
				final var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_ROLE);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					RegisterRole.RegisterRoleHelper(e);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ROLE))) {
				return RegisterRole.runCommand(e, guild_id, args, adminPermission);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CATEGORY))) {
				final var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_CATEGORY);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					RegisterCategory.runHelp(e);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CATEGORY))) {
				RegisterCategory.runCommand(e, args, adminPermission);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNEL))) {
				final var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_TEXT_CHANNEL);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					RegisterChannel.RegisterChannelHelper(e);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNEL))) {
				return RegisterChannel.runCommand(e, guild_id, args, adminPermission);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNEL_URL))) {
				final var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_TEXT_CHANNEL_URL);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					RegisterChannel.RegisterChannelHelperURL(e);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNEL_URL))) {
				return RegisterChannel.runCommandURL(e, guild_id, args, adminPermission);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNEL_TXT))) {
				final var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_TEXT_CHANNEL_TXT);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					RegisterChannel.RegisterChannelHelperTxt(e);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNEL_TXT))) {
				return RegisterChannel.runCommandTxt(e, guild_id, args, adminPermission);
			}
			else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNELS))) {
				return RegisterChannel.runChannelsRegistration(e, guild_id, adminPermission);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_RANKING_ROLE))) {
				final var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_RANKING_ROLE);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					RegisterRankingRole.RegisterRankingRoleHelper(e);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_RANKING_ROLE))) {
				return RegisterRankingRole.runCommand(e, guild_id, args, adminPermission);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_USERS))) {
				final var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_USERS);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					new Thread(new CollectUsers(e, false)).start();
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
		}
		else if(!botConfig.getIgnoreMissingPermissions()) {
			UserPrivs.throwNotEnoughPrivilegeError(e, STATIC.getCommandLevel(e.getGuild(), Command.REGISTER));
		}
		return false;
	}

	@Override
	public void executed(String[] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Register command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.REGISTER.getColumn(), out.toString().trim());
		}
	}
}
