package commandsContainer;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.Roles;
import core.UserPrivs;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.ServerRoles;

public class RegisterRole {
	private static EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");
	
	public static void RegisterRoleHelper(MessageReceivedEvent _e){
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.WHITE).setThumbnail(IniFileReader.getSettingsThumbnail()).setTitle("Register various roles to create a an Administrator to User hierarchy for the bot!");
		StringBuilder strB = new StringBuilder();
		String parseMessage = null;
		
		parseMessage = "Please write the command in this format:\n**"+IniFileReader.getCommandPrefix()+"register -role <role_type> role-id**\n\nRole-ids can be displayed with the command **"+IniFileReader.getCommandPrefix()+"display -roles**. Here are all available role_types:\n\n";
		ServerRoles.SQLgetCategories();
		for(Roles categories : ServerRoles.getRoles_ID()){
			strB.append("**"+categories.getCategory_ABV()+"** for the **"+categories.getCategory_Name()+"** role\n");
		}
		ServerRoles.clearRolesArray();
		_e.getTextChannel().sendMessage(messageBuild.setDescription(parseMessage+strB.toString()).build()).queue();
	}
	
	public static void runCommandWithAdminFirst(MessageReceivedEvent _e, long _guild_id, String _message){
		String category_abv = null;
		String role;
		String role_name;
		long role_id;
		
		if(_message.contains(" adm ") || _message.matches("[!\"$%&�/()=?.@#^*+\\-={};':,<>\\w\\d\\s]*\\sadm(?!\\w\\d\\s)")){
			category_abv = "adm";
			role = _message.replaceAll("[^0-9]*", "");
			if(role.length() == 18){
				try {
					role_id = Long.parseLong(role);
					role_name = _e.getGuild().getRoleById(role_id).getName();
					ServerRoles.SQLInsertRole(_guild_id, role_id, role_name, category_abv);
					_e.getTextChannel().sendMessage("**The primary Administrator role has been registered!**").queue();
				} catch(NullPointerException npe){
					_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Please type a valid role id!").queue();
				}
			}
		}
		else{
			_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Please start with assigning an administrator role or recheck the syntax!").queue();
		}
	}

	public static void runCommand(MessageReceivedEvent _e, long _guild_id, String _message){
		String category_abv = null;
		String role;
		String role_name;
		long role_id;
		
		if(UserPrivs.isUserAdmin(_e.getMember().getUser(), _guild_id) || _e.getMember().getUser().getId().equals(IniFileReader.getAdmin())){
			Pattern pattern = Pattern.compile("(adm|mod|com|bot|mut)");
			Matcher matcher = pattern.matcher(_message);
			if(matcher.find()){
				category_abv = matcher.group();
				role = _message.replaceAll("[^0-9]*", "");
				if(role.length() == 18){
					try {
						role_id = Long.parseLong(role);
						role_name = _e.getGuild().getRoleById(role_id).getName();
						ServerRoles.SQLInsertRole(_guild_id, role_id, role_name, category_abv);
						_e.getTextChannel().sendMessage("**The role has been registered!**").queue();
					} catch(NullPointerException npe){
						_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Please type a valid role id!").queue();
					}
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
