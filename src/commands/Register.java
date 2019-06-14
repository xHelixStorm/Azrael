package commands;

import java.awt.Color;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.RegisterChannel;
import commandsContainer.RegisterRankingRole;
import commandsContainer.RegisterRole;
import core.UserPrivs;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.DiscordRoles;
import threads.CollectUsers;

public class Register implements Command{
	private EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.WHITE).setThumbnail(IniFileReader.getSettingsThumbnail()).setTitle("Register various stuff from your server to enable all features!");
	private EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");
	private long guild_id;
	
	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(GuildIni.getRegisterCommand(e.getGuild().getIdLong())) {
			Logger logger = LoggerFactory.getLogger(Register.class);
			logger.debug("{} has used Register command", e.getMember().getUser().getId());
			
			ExecutorService executor = Executors.newSingleThreadExecutor();
			guild_id = e.getGuild().getIdLong();
			var adminPermission = (GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong());
			final String prefix = GuildIni.getCommandPrefix(e.getGuild().getIdLong());
			if(DiscordRoles.SQLgetRole(guild_id, "adm") == 0) {
				if(args.length == 0) {
					if(adminPermission) {
						e.getTextChannel().sendMessage(messageBuild.setDescription("Use this command to register either a channel, a role, a ranking role or all users in a guild. For the first time, an administrator role needs to be registered and afterwards all the other features for this command will be unlocked.\n\n"
								+ "Here how you can display more details on how to register a role:\n"
								+ "**"+prefix+"register -role**").build()).queue();
					}
					else {
						e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:").build()).queue();
					}
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase("-role")) {
					if(adminPermission) {
						RegisterRole.RegisterRoleHelper(e);
					}
					else {
						e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:").build()).queue();
					}
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase("-role")) {
					RegisterRole.runCommandWithAdminFirst(e, guild_id, args, adminPermission);
				}
				else {
					e.getTextChannel().sendMessage("**"+e.getMember().getAsMention()+" Something went wrong. Please recheck the syntax and try again!**").queue();
				}
			}
			else if(UserPrivs.comparePrivilege(e.getMember(), GuildIni.getRegisterLevel(e.getGuild().getIdLong())) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
				if(args.length == 0) {
					e.getTextChannel().sendMessage(messageBuild.setDescription("Use this command to register either a channel, a role, a ranking role or all users in a guild. Use the following commands to get more details:\n\n"
							+ "Description to register a role:\n"
								+ "**"+prefix+"register -role**\n\n"
							+ "Description to register a channel:\n"
								+ "**"+prefix+"register -text-channel**\n\n"
							+ "Syntax to register all channels:\n"
								+ "**"+prefix+"register -text-channels**\n\n"
							+ "Description to register a ranking role:\n"
								+ "**"+prefix+"register -ranking-role**\n\n"
							+ "How to register all users into the database:\n"
								+ "**"+prefix+"register -users**").build()).queue();
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase("-role")) {
					final var commandLevel = GuildIni.getRegisterRoleLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
						RegisterRole.RegisterRoleHelper(e);
					}
					else {
						e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(commandLevel, e.getGuild().getRoles())).build()).queue();
					}
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase("-role")) {
					RegisterRole.runCommand(e, guild_id, args, adminPermission);
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase("-text-channel")) {
					final var commandLevel = GuildIni.getRegisterTextChannelLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
						RegisterChannel.RegisterChannelHelper(e);
					}
					else {
						e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(commandLevel, e.getGuild().getRoles())).build()).queue();
					}
				}
				else if(args[0].equalsIgnoreCase("-text-channels")) {
					RegisterChannel.runChannelsRegistration(e, guild_id, adminPermission);
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase("-text-channel")) {
					RegisterChannel.runCommand(e, guild_id, args, adminPermission);
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase("-ranking-role")) {
					final var commandLevel = GuildIni.getRegisterRankingRoleLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
						RegisterRankingRole.RegisterRankingRoleHelper(e);
					}
					else {
						e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(commandLevel, e.getGuild().getRoles())).build()).queue();
					}
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase("-ranking-role")) {
					RegisterRankingRole.runCommand(e, guild_id, args, adminPermission);
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase("-users")) {
					final var commandLevel = GuildIni.getRegisterUsersLevel(e.getGuild().getIdLong());
					if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
						executor.execute(new CollectUsers(e));
						e.getTextChannel().sendMessage("All users in this server are being registered. Please wait...").queue();
						executor.shutdown();
					}
					else {
						e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(commandLevel, e.getGuild().getRoles())).build()).queue();
					}
				}
				else {
					e.getTextChannel().sendMessage("**"+e.getMember().getAsMention()+" Something went wrong. Please recheck the syntax and try again!**").queue();
				}
			}
			else {
				e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(GuildIni.getRegisterLevel(e.getGuild().getIdLong()), e.getGuild().getRoles())).build()).queue();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		
	}

	@Override
	public String help() {
		return null;
	}

}
