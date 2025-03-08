package de.azrael.commands;

import java.awt.Color;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.commands.util.RegisterCategory;
import de.azrael.commands.util.RegisterChannel;
import de.azrael.commands.util.RegisterRankingRole;
import de.azrael.commands.util.RegisterRole;
import de.azrael.constructors.BotConfigs;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.sql.DiscordRoles;
import de.azrael.threads.CollectUsers;
import de.azrael.util.STATIC;
import de.azrael.util.UserPrivs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Register implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Register.class);
	
	@Override
	public boolean called(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.getCommandEnabled(e.getGuild(), Command.REGISTER);
	}

	@Override
	public boolean action(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		final var thumbnails = BotConfiguration.SQLgetThumbnails(e.getGuild().getIdLong());
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(thumbnails.getSettings());
		var guild_id = e.getGuild().getIdLong();
		if(DiscordRoles.SQLgetRoles(guild_id).parallelStream().filter(f -> f.getCategory_ABV().equals("adm")).findAny().orElse(null) == null) {
			if(BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
				if(args.length == 0) {
					e.getChannel().sendMessageEmbeds(messageBuild.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_HELP_1)).build()).queue();
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ROLE))) {
					RegisterRole.RegisterRoleHelper(e, thumbnails, true);
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ROLE))) {
					RegisterRole.runCommandWithAdminFirst(e, args, thumbnails);
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
				}
				return true;
			}
			else {
				EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(thumbnails.getDenied()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DENIED));
				e.getChannel().sendMessageEmbeds(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_REQUIRED)).build()).queue();
			}
		}
		else if(UserPrivs.comparePrivilege(e.getMember(), STATIC.getCommandLevel(e.getGuild(), Command.REGISTER))) {
			if(args.length == 0) {
				//parameters are disabled by default in case of errors
				boolean registerRole = false;
				boolean registerCategory = false;
				boolean registerTextChannel = false;
				boolean registerTextChannelUrl = false;
				boolean registerTextChannelTxt = false;
				boolean registerTextChannels = false;
				boolean registerRankingRole = false;
				boolean registerUsers = false;
				
				final var commands = BotConfiguration.SQLgetCommand(e.getGuild().getIdLong(), 1, Command.REGISTER_ROLE, Command.REGISTER_CATEGORY, Command.REGISTER_TEXT_CHANNEL, Command.REGISTER_TEXT_CHANNEL_URL
						, Command.REGISTER_TEXT_CHANNEL_TXT, Command.REGISTER_TEXT_CHANNELS, Command.REGISTER_RANKING_ROLE, Command.REGISTER_USERS);
				
				for(final Object command : commands) {
					boolean enabled = false;
					String name = "";
					for(Object value : (ArrayList<?>)command) {
						if(value instanceof Boolean)
							enabled = (Boolean)value;
						else if(value instanceof String)
							name = ((String)value).split(":")[0];
					}
					
					if(name.equals(Command.REGISTER_ROLE.getColumn())) {
						registerRole = enabled;
					}
					else if(name.equals(Command.REGISTER_CATEGORY.getColumn())) {
						registerCategory = enabled;
					}
					else if(name.equals(Command.REGISTER_TEXT_CHANNEL.getColumn())) {
						registerTextChannel = enabled;
					}
					else if(name.equals(Command.REGISTER_TEXT_CHANNEL_URL.getColumn())) {
						registerTextChannelUrl = enabled;
					}
					else if(name.equals(Command.REGISTER_TEXT_CHANNEL_TXT.getColumn())) {
						registerTextChannelTxt = enabled;
					}
					else if(name.equals(Command.REGISTER_TEXT_CHANNELS.getColumn())) {
						registerTextChannels = enabled;
					}
					else if(name.equals(Command.REGISTER_RANKING_ROLE.getColumn())) {
						registerRankingRole = enabled;
					}
					else if(name.equals(Command.REGISTER_USERS.getColumn())) {
						registerUsers = enabled;
					}
				}
				
				StringBuilder sb = new StringBuilder();
				if(registerRole)			sb.append(STATIC.getTranslation(e.getMember(), Translation.REGISTER_PARAM_1).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ROLE)));
				if(registerCategory)		sb.append(STATIC.getTranslation(e.getMember(), Translation.REGISTER_PARAM_2).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_CATEGORY)));
				if(registerTextChannel)		sb.append(STATIC.getTranslation(e.getMember(), Translation.REGISTER_PARAM_3).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNEL)));
				if(registerTextChannelUrl)	sb.append(STATIC.getTranslation(e.getMember(), Translation.REGISTER_PARAM_4).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNEL_URL)));
				if(registerTextChannelTxt)	sb.append(STATIC.getTranslation(e.getMember(), Translation.REGISTER_PARAM_5).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNEL_TXT)));
				if(registerTextChannels)	sb.append(STATIC.getTranslation(e.getMember(), Translation.REGISTER_PARAM_6).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNELS)));
				if(registerRankingRole)		sb.append(STATIC.getTranslation(e.getMember(), Translation.REGISTER_PARAM_7).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_RANKING_ROLE)));
				if(registerUsers)			sb.append(STATIC.getTranslation(e.getMember(), Translation.REGISTER_PARAM_8).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_USERS)));
				
				if(sb.length() > 0)
					e.getChannel().sendMessageEmbeds(messageBuild.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_HELP_2)+sb.toString()).build()).queue();
				else
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_DISABLED)).build()).queue();
				return true;
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ROLE)) && STATIC.getCommandEnabled(e.getGuild(), Command.REGISTER_ROLE)) {
				final var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_ROLE);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel)) {
					RegisterRole.RegisterRoleHelper(e, thumbnails, false);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ROLE)) && STATIC.getCommandEnabled(e.getGuild(), Command.REGISTER_ROLE)) {
				return RegisterRole.runCommand(e, args, thumbnails);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CATEGORY)) && STATIC.getCommandEnabled(e.getGuild(), Command.REGISTER_CATEGORY)) {
				final var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_CATEGORY);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel)) {
					RegisterCategory.runHelp(e, thumbnails);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CATEGORY)) && STATIC.getCommandEnabled(e.getGuild(), Command.REGISTER_CATEGORY)) {
				RegisterCategory.runCommand(e, args, thumbnails);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNEL)) && STATIC.getCommandEnabled(e.getGuild(), Command.REGISTER_TEXT_CHANNEL)) {
				final var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_TEXT_CHANNEL);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel)) {
					RegisterChannel.RegisterChannelHelper(e, thumbnails);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNEL)) && STATIC.getCommandEnabled(e.getGuild(), Command.REGISTER_TEXT_CHANNEL)) {
				return RegisterChannel.runCommand(e, args, thumbnails, botConfig);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNEL_URL)) && STATIC.getCommandEnabled(e.getGuild(), Command.REGISTER_TEXT_CHANNEL_URL)) {
				final var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_TEXT_CHANNEL_URL);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel)) {
					RegisterChannel.RegisterChannelHelperURL(e);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNEL_URL)) && STATIC.getCommandEnabled(e.getGuild(), Command.REGISTER_TEXT_CHANNEL_URL)) {
				return RegisterChannel.runCommandURL(e, args, thumbnails);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNEL_TXT)) && STATIC.getCommandEnabled(e.getGuild(), Command.REGISTER_TEXT_CHANNEL_TXT)) {
				final var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_TEXT_CHANNEL_TXT);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel)) {
					RegisterChannel.RegisterChannelHelperTxt(e);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNEL_TXT)) && STATIC.getCommandEnabled(e.getGuild(), Command.REGISTER_TEXT_CHANNEL_TXT)) {
				return RegisterChannel.runCommandTxt(e, args, thumbnails);
			}
			else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEXT_CHANNELS)) && STATIC.getCommandEnabled(e.getGuild(), Command.REGISTER_TEXT_CHANNELS)) {
				return RegisterChannel.runChannelsRegistration(e, thumbnails);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_RANKING_ROLE)) && STATIC.getCommandEnabled(e.getGuild(), Command.REGISTER_RANKING_ROLE)) {
				final var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_RANKING_ROLE);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel)) {
					RegisterRankingRole.RegisterRankingRoleHelper(e, thumbnails);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_RANKING_ROLE)) && STATIC.getCommandEnabled(e.getGuild(), Command.REGISTER_RANKING_ROLE)) {
				return RegisterRankingRole.runCommand(e, args, thumbnails);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_USERS)) && STATIC.getCommandEnabled(e.getGuild(), Command.REGISTER_USERS)) {
				final var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_USERS);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel)) {
					new Thread(new CollectUsers(e, false)).start();
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
		}
		else if(!botConfig.getIgnoreMissingPermissions()) {
			UserPrivs.throwNotEnoughPrivilegeError(e, STATIC.getCommandLevel(e.getGuild(), Command.REGISTER));
		}
		return false;
	}

	@Override
	public void executed(String[] args, boolean success, MessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Register command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.REGISTER.getColumn(), out.toString().trim());
		}
	}
}
