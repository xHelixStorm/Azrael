package commandsContainer;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import core.Roles;
import core.UserPrivs;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.DiscordRoles;

public class RegisterRole {
	private static final Logger logger = LoggerFactory.getLogger(RegisterRole.class);
	private static EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");
	
	public static void RegisterRoleHelper(MessageReceivedEvent _e){
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.WHITE).setThumbnail(IniFileReader.getSettingsThumbnail()).setTitle("Register various roles to create a an Administrator to User hierarchy for the bot!");
		StringBuilder strB = new StringBuilder();
		String parseMessage = null;
		
		final String prefix = GuildIni.getCommandPrefix(_e.getGuild().getIdLong());
		parseMessage = "Please write the command in this format:\n**"+prefix+"register -role <role_type> role-id**\n\nRole-ids can be displayed with the command **"+prefix+"display -roles**. Here are all available role_types:\n\n";
		for(Roles categories : DiscordRoles.SQLgetCategories()){
			strB.append("**"+categories.getCategory_ABV()+"** for the **"+categories.getCategory_Name()+"** role\n");
		}
		_e.getTextChannel().sendMessage(messageBuild.setDescription(parseMessage+strB.toString()).build()).queue();
	}
	
	public static void runCommandWithAdminFirst(MessageReceivedEvent _e, long _guild_id, String _message){
		String category_abv = null;
		String role;
		String role_name;
		long role_id;
		
		if(GuildIni.getAdmin(_guild_id) == _e.getMember().getUser().getIdLong()) {
			if(_message.contains(" adm ") || _message.matches("[!\"$%&�/()=?.@#^*+\\-={};':,<>\\w\\d\\s]*\\sadm(?!\\w\\d\\s)")){
				category_abv = "adm";
				role = _message.replaceAll("[^0-9]*", "");
				if(role.length() == 18){
					try {
						role_id = Long.parseLong(role);
						role_name = _e.getGuild().getRoleById(role_id).getName();
						if(DiscordRoles.SQLInsertRole(_guild_id, role_id, role_name, category_abv) > 0) {
							logger.debug("Administrator role registered {} for guild {}", role_id, _e.getGuild().getName());
							_e.getTextChannel().sendMessage("**The primary Administrator role has been registered!**").queue();
							DiscordRoles.SQLgetRoles(_e.getGuild().getIdLong());
						}
						else {
							logger.error("Role {} couldn't be registered into DiscordRoles.roles for the guild {}", role_id, _e.getGuild().getName());
							_e.getTextChannel().sendMessage("An internal error occurred. Role "+role_id+" couldn't be registered into DiscordRoles.roles table").queue();
						}
					} catch(NullPointerException npe){
						_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Please type a valid role id!").queue();
					}
				}
			}
			else{
				_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Please start with assigning an administrator role or recheck the syntax!").queue();
			}
		}
		else {
			_e.getTextChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention() + " **My apologies young padawan. This command can be used only from an Administrator. Here a cookie** :cookie:").build()).queue();
		}
	}

	public static void runCommand(MessageReceivedEvent _e, long _guild_id, String _message){
		String category_abv = null;
		String role;
		String role_name;
		long role_id;
		
		if(UserPrivs.isUserAdmin(_e.getMember().getUser(), _guild_id) || _e.getMember().getUser().getIdLong() == GuildIni.getAdmin(_guild_id)){
			Pattern pattern = Pattern.compile("(adm|mod|com|bot|mut|rea)");
			Matcher matcher = pattern.matcher(_message);
			if(matcher.find()){
				category_abv = matcher.group();
				role = _message.replaceAll("[^0-9]*", "");
				if(role.length() == 18){
					try {
						role_id = Long.parseLong(role);
						role_name = _e.getGuild().getRoleById(role_id).getName();
						if(DiscordRoles.SQLInsertRole(_guild_id, role_id, role_name, category_abv) > 0) {
							logger.debug("{} has registered the role {} with the category {} in guild {}", _e.getMember().getUser().getId(), role_name, category_abv, _e.getGuild().getName());
							_e.getTextChannel().sendMessage("**The role has been registered!**").queue();
							if(category_abv.equals("rea")) {
								Hashes.removeRoles();
							}
							DiscordRoles.SQLgetRoles(_e.getGuild().getIdLong());
						}
						else {
							logger.error("Role {} couldn't be registered into DiscordRoles.roles for the guild {}", role_id, _e.getGuild().getName());
							_e.getTextChannel().sendMessage("An internal error occurred. Role "+role_id+" couldn't be registered into DiscordRoles.roles table").queue();
						}
					} catch(NullPointerException npe){
						_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Please type a valid role id!").queue();
					}
				}
				else{
					_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" The role id has to be 18 digits long. Execution interrupted!").queue();
				}
			}
			else{
				_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Something went wrong. Please recheck the syntax!").queue();
			}
		}
		else {
			_e.getTextChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention() + " **My apologies young padawan. This command can be used only from an Administrator. Here a cookie** :cookie:").build()).queue();
		}
	}
}
