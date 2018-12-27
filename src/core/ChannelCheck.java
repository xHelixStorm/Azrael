package core;

import net.dv8tion.jda.core.entities.Channel;
import sql.Azrael;

public class ChannelCheck {
	
	public static boolean isChannelLog(Channel channel){
		try {
			Azrael.SQLgetChannelType(channel.getIdLong());
			String channel_type_name = Azrael.getChannelTypeName();
			if(channel_type_name.equals("Log")){
				return true;
			}
		} catch(NullPointerException npe){
			//do nothing
		} finally {
			Azrael.clearAllVariables();
		}
		return false;
	}
	
	public static boolean isChannelTrash(Channel channel){
		try {
			Azrael.SQLgetChannelType(channel.getIdLong());
			String channel_type_name = Azrael.getChannelTypeName();
			if(channel_type_name.equals("Trash")){
				return true;
			}
		} catch(NullPointerException npe){
			//do nothing
		} finally {
			Azrael.clearAllVariables();
		}
		return false;
	}
	
	public static boolean isChannelStatus(Channel channel){
		try {
			Azrael.SQLgetChannelType(channel.getIdLong());
			String channel_type_name = Azrael.getChannelTypeName();
			if(channel_type_name.equals("Status")){
				return true;
			}
		} catch(NullPointerException npe){
			//do nothing
		} finally {
			Azrael.clearAllVariables();
		}
		return false;
	}
	
	public static boolean isChannelMaintenance(Channel channel){
		try {
			Azrael.SQLgetChannelType(channel.getIdLong());
			String channel_type_name = Azrael.getChannelTypeName();
			if(channel_type_name.equals("Maintenance")){
				return true;
			}
		} catch(NullPointerException npe){
			//do nothing
		} finally {
			Azrael.clearAllVariables();
		}
		return false;
	}
	
	public static boolean isChannelBot(Channel channel){
		try {
			Azrael.SQLgetChannelType(channel.getIdLong());
			String channel_type_name = Azrael.getChannelTypeName();
			if(channel_type_name.equals("Bot")){
				return true;
			}
		} catch(NullPointerException npe){
			//do nothing
		} finally {
			Azrael.clearAllVariables();
		}
		return false;
	}
	
	public static boolean isChannelMusic(Channel channel){
		try {
			Azrael.SQLgetChannelType(channel.getIdLong());
			String channel_type_name = Azrael.getChannelTypeName();
			if(channel_type_name.equals("Music")){
				return true;
			}
		} catch(NullPointerException npe){
			//do nothing
		} finally {
			Azrael.clearAllVariables();
		}
		return false;
	}
	
	public static boolean isChannelEnglish(Channel channel){
		try {
			Azrael.SQLgetChannelType(channel.getIdLong());
			String channel_type_name = Azrael.getChannelTypeName();
			if(channel_type_name.equals("English")){
				return true;
			}
		} catch(NullPointerException npe){
			//do nothing
		} finally {
			Azrael.clearAllVariables();
		}
		return false;
	}
	
	public static boolean isChannelGerman(Channel channel){
		try {
			Azrael.SQLgetChannelType(channel.getIdLong());
			String channel_type_name = Azrael.getChannelTypeName();
			if(channel_type_name.equals("German")){
				return true;
			}
		} catch(NullPointerException npe){
			//do nothing
		} finally {
			Azrael.clearAllVariables();
		}
		return false;
	}
	
	public static boolean isChannelFrench(Channel channel){
		try {
			Azrael.SQLgetChannelType(channel.getIdLong());
			String channel_type_name = Azrael.getChannelTypeName();
			if(channel_type_name.equals("Log")){
				return true;
			}
		} catch(NullPointerException npe){
			//do nothing
		}
		return false;
	}
	
	public static boolean isChannelTurkish(Channel channel){
		try {
			Azrael.SQLgetChannelType(channel.getIdLong());
			String channel_type_name = Azrael.getChannelTypeName();
			if(channel_type_name.equals("Log")){
				return true;
			}
		} catch(NullPointerException npe){
			//do nothing
		}
		return false;
	}
	
	public static boolean isChannelAllLanguages(Channel channel){
		try {
			Azrael.SQLgetChannelType(channel.getIdLong());
			String channel_type_name = Azrael.getChannelTypeName();
			if(channel_type_name.equals("All Languages")){
				return true;
			}
		} catch(NullPointerException npe){
			//do nothing
		}
		return false;
	}
}
