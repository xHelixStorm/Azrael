package core;

import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

public class UserPrivs {
	public static boolean isUserAdmin(User user, long _guild_id){
		for(Role r : user.getJDA().getGuildById(_guild_id).getMember(user).getRoles()){
			Roles category = Hashes.getDiscordRole(r.getIdLong());
			if(category != null && category.getCategory_Name().length() > 0) {
				if(category.getCategory_Name().equals("Administrator")){
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isUserMod(User user, long _guild_id){
		for(Role r : user.getJDA().getGuildById(_guild_id).getMember(user).getRoles()){
			Roles category = Hashes.getDiscordRole(r.getIdLong());
			if(category != null && category.getCategory_Name().length() > 0) {
				if(category.getCategory_Name().equals("Moderator")){
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isUserBot(User user, long _guild_id){
		for(Role r : user.getJDA().getGuildById(_guild_id).getMember(user).getRoles()){
			Roles category = Hashes.getDiscordRole(r.getIdLong());
			if(category != null && category.getCategory_Name().length() > 0) {
				if(category.getCategory_Name().equals("Bot")){
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isUserMuted(User user, long _guild_id){
		for(Role r : user.getJDA().getGuildById(_guild_id).getMember(user).getRoles()){
			Roles category = Hashes.getDiscordRole(r.getIdLong());
			if(category != null && category.getCategory_Name().length() > 0) {
				if(category.getCategory_Name().equals("Mute")){
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isUserCommunity(User user, long _guild_id){
		for(Role r : user.getJDA().getGuildById(_guild_id).getMember(user).getRoles()){
			Roles category = Hashes.getDiscordRole(r.getIdLong());
			if(category != null && category.getCategory_ABV().length() > 0) {
				if(category.getCategory_Name().equals("Community")){
					return true;
				}
			}
		}
		return false;
	}
}
