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
	private String user_id;
	private long guild_id;
	
	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(GuildIni.getRegisterCommand(e.getGuild().getIdLong())){
			Logger logger = LoggerFactory.getLogger(Register.class);
			logger.debug("{} has used Register command", e.getMember().getUser().getId());
			
			ExecutorService executor = Executors.newSingleThreadExecutor();
			user_id = e.getMember().getUser().getId();
			guild_id = e.getGuild().getIdLong();
			
			final String prefix = GuildIni.getCommandPrefix(e.getGuild().getIdLong());
			if(DiscordRoles.SQLgetRole(guild_id, "adm") == 0){
				if(args.length == 0){
					e.getTextChannel().sendMessage(messageBuild.setDescription("Use this command to register either a channel, a role, a ranking role or all users in a guild. For the first time, an administrator role needs to be registered and afterwards all the other features for this command will be unlocked.\n\n"
							+ "Here how you can display more details on how to register a role:\n"
							+ "**"+prefix+"register -role**").build()).queue();
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase("-role")){
					RegisterRole.RegisterRoleHelper(e);
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase("-role")){
					RegisterRole.runCommandWithAdminFirst(e, guild_id, args);
				}
			}
			else if(UserPrivs.isUserAdmin(e.getMember().getUser(), guild_id) || UserPrivs.isUserMod(e.getMember().getUser(), guild_id) || Long.parseLong(user_id) == GuildIni.getAdmin(guild_id)){
				if(args.length == 0){
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
				else if(args.length == 1 && args[0].equalsIgnoreCase("-role")){
					RegisterRole.RegisterRoleHelper(e);
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase("-role")){
					RegisterRole.runCommand(e, guild_id, args);
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase("-text-channel")){
					RegisterChannel.RegisterChannelHelper(e);
				}
				else if(args[0].equalsIgnoreCase("-text-channels")){
					RegisterChannel.runChannelsRegistration(e, guild_id);
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase("-text-channel")){
					RegisterChannel.runCommand(e, guild_id, args);
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase("-ranking-role")){
					RegisterRankingRole.RegisterRankingRoleHelper(e);
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase("-ranking-role")){
					RegisterRankingRole.runCommand(e, guild_id, args);
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase("-users")){
					executor.execute(new CollectUsers(e));
					e.getTextChannel().sendMessage("All users in this server are being registered. Please wait...").queue();
					executor.shutdown();
				}
				else{
					e.getTextChannel().sendMessage("**"+e.getMember().getAsMention()+" Something went wrong. Please recheck the syntax and try again!**").queue();
				}
			}
			else{
				e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. This command can be used only from an Administrator or an Moderator. Here a cookie** :cookie:").build()).queue();
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
