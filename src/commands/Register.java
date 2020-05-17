package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.RegisterChannel;
import commandsContainer.RegisterRankingRole;
import commandsContainer.RegisterRole;
import core.UserPrivs;
import enums.Translation;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.DiscordRoles;
import threads.CollectUsers;
import util.STATIC;

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
			else if(args.length == 1 && args[0].equalsIgnoreCase("role")) {
				if(adminPermission) {
					RegisterRole.RegisterRoleHelper(e);
				}
				else {
					EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DENIED));
					e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_REQUIRED)).build()).queue();
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase("role")) {
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
			else if(args.length == 1 && args[0].equalsIgnoreCase("role")) {
				final var commandLevel = GuildIni.getRegisterRoleLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					RegisterRole.RegisterRoleHelper(e);
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase("role")) {
				RegisterRole.runCommand(e, guild_id, args, adminPermission);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase("text-channel")) {
				final var commandLevel = GuildIni.getRegisterTextChannelLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					RegisterChannel.RegisterChannelHelper(e);
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase("text-channel")) {
				RegisterChannel.runCommand(e, guild_id, args, adminPermission);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase("text-channel-url")) {
				final var commandLevel = GuildIni.getRegisterTextChannelURLLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					RegisterChannel.RegisterChannelHelperURL(e);
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase("text-channel-url")) {
				RegisterChannel.runCommandURL(e, guild_id, args, adminPermission);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase("text-channel-txt")) {
				final var commandLevel = GuildIni.getRegisterTextChannelTXTLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					RegisterChannel.RegisterChannelHelperTxt(e);
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase("text-channel-txt")) {
				RegisterChannel.runCommandTxt(e, guild_id, args, adminPermission);
			}
			else if(args[0].equalsIgnoreCase("text-channels")) {
				RegisterChannel.runChannelsRegistration(e, guild_id, adminPermission);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase("ranking-role")) {
				final var commandLevel = GuildIni.getRegisterRankingRoleLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					RegisterRankingRole.RegisterRankingRoleHelper(e);
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase("ranking-role")) {
				RegisterRankingRole.runCommand(e, guild_id, args, adminPermission);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase("users")) {
				final var commandLevel = GuildIni.getRegisterUsersLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					new Thread(new CollectUsers(e)).start();
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
		logger.debug("{} has used Register command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
