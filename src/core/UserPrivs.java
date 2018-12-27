package core;

import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import sql.DiscordRoles;

public class UserPrivs {
	
	public static boolean isUserAdmin(User user, long _guild_id){
		for(Role r : user.getJDA().getGuildById(_guild_id).getMember(user).getRoles()){
			if(DiscordRoles.SQLgetCategory(r.getIdLong(), _guild_id)) {
				try {
					String category_name = DiscordRoles.getCategory_Name();
					if(category_name.equals("Administrator")){
						return true;
					}
				} catch(NullPointerException npe){
					//do nothing
				} finally {
					DiscordRoles.clearAllVariables();
				}
			}
		}
		return false;
	}
	
	public static boolean isUserMod(User user, long _guild_id){
		for(Role r : user.getJDA().getGuildById(_guild_id).getMember(user).getRoles()){
			if(DiscordRoles.SQLgetCategory(r.getIdLong(), _guild_id)) {
				try {
					String category_name = DiscordRoles.getCategory_Name();
					if(category_name.equals("Moderator")){
						return true;
					}
				} catch(NullPointerException npe){
					//do nothing
				} finally {
					DiscordRoles.clearAllVariables();
				}
			}
		}
		return false;
	}
	
	public static boolean isUserBot(User user, long _guild_id){
		for(Role r : user.getJDA().getGuildById(_guild_id).getMember(user).getRoles()){
			if(DiscordRoles.SQLgetCategory(r.getIdLong(), _guild_id)) {
				try {
					String category_name = DiscordRoles.getCategory_Name();
					if(category_name.equals("Bot")){
						return true;
					}
				} catch(NullPointerException npe){
					//do nothing
				} finally {
					DiscordRoles.clearAllVariables();
				}
			}
		}
		return false;
	}
	
	public static boolean isUserMuted(User user, long _guild_id){
		for(Role r : user.getJDA().getGuildById(_guild_id).getMember(user).getRoles()){
			if(DiscordRoles.SQLgetCategory(r.getIdLong(), _guild_id)) {
				try {
					String category_name = DiscordRoles.getCategory_Name();
					if(category_name.equals("Mute")){
						return true;
					}
				} catch(NullPointerException npe){
					//do nothing
				} finally {
					DiscordRoles.clearAllVariables();
				}
			}
		}
		return false;
	}
	
	public static boolean isUserCommunity(User user, long _guild_id){
		for(Role r : user.getJDA().getGuildById(_guild_id).getMember(user).getRoles()){
			if(DiscordRoles.SQLgetCategory(r.getIdLong(), _guild_id)) {
				try {
					String category_name = DiscordRoles.getCategory_Name();
					if(category_name.equals("Community")){
						return true;
					}
				} catch(NullPointerException npe){
					//do nothing
				} finally {
					DiscordRoles.clearAllVariables();
				}
			}
		}
		return false;
	}
}
