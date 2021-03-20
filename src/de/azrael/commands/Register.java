package de.azrael.commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.commandsContainer.RegisterCategory;
import de.azrael.commandsContainer.RegisterChannel;
import de.azrael.commandsContainer.RegisterRankingRole;
import de.azrael.commandsContainer.RegisterRole;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.GuildIni;
import de.azrael.fileManagement.IniFileReader;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.DiscordRoles;
import de.azrael.threads.CollectUsers;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Register implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Register.class);
	
	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getRegisterCommand(e.getGuild().getIdLong())) {
			return true;
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getSettingsThumbnail());
		var guild_id = e.getGuild().getIdLong();
		var adminPermission = (GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong());
		if(DiscordRoles.SQLgetRoles(guild_id).parallelStream().filter(f -> f.getCategory_ABV().equals("adm")).findAny().orElse(null) == null) {
			if(args.length == 0) {
				if(adminPermission) {
					e.getChannel().sendMessage(messageBuild.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_HELP_1)).build()).queue();
				}
				else {
					EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DENIED));
					e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_REQUIRED)).build()).queue();
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ROLE))) {
				if(adminPermission) {
					RegisterRole.RegisterRoleHelper(e);
				}
				else {
					EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DENIED));
					e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_REQUIRED)).build()).queue();
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ROLE))) {
				RegisterRole.runCommandWithAdminFirst(e, guild_id, args, adminPermission);
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
		}
		else if(UserPrivs.comparePrivilege(e.getMember(), GuildIni.getRegisterLevel(e.getGuild().getIdLong())) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
			if(args.length == 0) {
				e.getChannel().sendMessage(messageBuild.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_HELP_2)).build()).queue();
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ROLE))) {
				final var commandLevel = GuildIni.getRegisterRoleLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					RegisterRole.RegisterRoleHelper(e);
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ROLE))) {
				RegisterRole.runCommand(e, guild_id, args, adminPermission);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CATEGORY))) {
				final var commandLevel = GuildIni.getRegisterCategoryLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					RegisterCategory.runHelp(e);
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CATEGORY))) {
				RegisterCategory.runCommand(e, args, adminPermission);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNEL))) {
				final var commandLevel = GuildIni.getRegisterTextChannelLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					RegisterChannel.RegisterChannelHelper(e);
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNEL))) {
				RegisterChannel.runCommand(e, guild_id, args, adminPermission);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNEL_URL))) {
				final var commandLevel = GuildIni.getRegisterTextChannelURLLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					RegisterChannel.RegisterChannelHelperURL(e);
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNEL_URL))) {
				RegisterChannel.runCommandURL(e, guild_id, args, adminPermission);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNEL_TXT))) {
				final var commandLevel = GuildIni.getRegisterTextChannelTXTLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					RegisterChannel.RegisterChannelHelperTxt(e);
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNEL_TXT))) {
				RegisterChannel.runCommandTxt(e, guild_id, args, adminPermission);
			}
			else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNELS))) {
				RegisterChannel.runChannelsRegistration(e, guild_id, adminPermission);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_RANKING_ROLE))) {
				final var commandLevel = GuildIni.getRegisterRankingRoleLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					RegisterRankingRole.RegisterRankingRoleHelper(e);
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_RANKING_ROLE))) {
				RegisterRankingRole.runCommand(e, guild_id, args, adminPermission);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_USERS))) {
				final var commandLevel = GuildIni.getRegisterUsersLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					new Thread(new CollectUsers(e, false)).start();
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
		}
		else {
			UserPrivs.throwNotEnoughPrivilegeError(e, GuildIni.getRegisterLevel(e.getGuild().getIdLong()));
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used Register command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
