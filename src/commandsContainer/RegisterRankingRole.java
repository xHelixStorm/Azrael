package commandsContainer;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.UserPrivs;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingDB;

public class RegisterRankingRole {
	
	public static void RegisterRankingRoleHelper(MessageReceivedEvent _e){
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.WHITE).setThumbnail(IniFileReader.getSettingsThumbnail()).setTitle("Register roles for the ranking system!");
		_e.getTextChannel().sendMessage(messageBuild.setDescription("To use this command, write the role_id right after the command and add the required level to unlock this role in this format:\n"
				+ "**"+IniFileReader.getCommandPrefix()+"register -ranking-role <role_id> -level <level>**\n\n To display all roles, type the command **"+IniFileReader.getCommandPrefix()+"display -roles**. To remove all registered roles, type **"+IniFileReader.getCommandPrefix()+"register -ranking-role -clear**").build()).queue();
	}
	
	public static void runCommand(MessageReceivedEvent _e, long _guild_id, String _message){
		EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied");
		long guild_id = _e.getGuild().getIdLong();
		String message = _e.getMessage().getContentRaw();
		long role_id = 0;
		String role_name = "";
		String level = "";
		int level_requirement = 0;
		
		if(UserPrivs.isUserAdmin(_e.getMember().getUser(), _guild_id) || _e.getMember().getUser().getId().equals(IniFileReader.getAdmin())){
			if(message.equals(IniFileReader.getCommandPrefix()+"register -ranking-role -clear")) {
				RankingDB.SQLclearRoles(guild_id);
				_e.getTextChannel().sendMessage("All registered ranking roles have been cleared from the database!").queue();
			}
			else {
				Pattern pattern = Pattern.compile("[0-9]{18,18}");
				Pattern pattern2 = Pattern.compile("-level [0-9]{1,4}");
				Matcher matcher = pattern.matcher(message);
				try{
					if(matcher.find()){
						role_id = Long.parseLong(matcher.group());
						role_name = _e.getGuild().getRoleById(role_id).getName();
					}
					matcher = pattern2.matcher(message);
					if(matcher.find()){
						level = matcher.group().substring(7);
						level_requirement = Integer.parseInt(level);
					}
					if(level.length() < 1 || level.length() > 4){
						_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Please type a level between 1 and 9999 and don't forget the -level parameter!").queue();
					}
					else{
						RankingDB.SQLInsertRoles(role_id, role_name, level_requirement, guild_id);
						_e.getTextChannel().sendMessage("**The role named "+role_name+" can now be unlocked by reaching level "+level_requirement+"**").queue();
					}
				} catch(NullPointerException npe){
					_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Please type a valid role id!").queue();
				}
			}
		}
		else {
			_e.getTextChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention() + " **My apologies young padawan. This command can be used only from an Administrator. Here a cookie** :cookie:").build()).queue();
		}
	}
}
