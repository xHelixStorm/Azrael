package de.azrael.fileManagement;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Guild;

public class GuildIni {
	private static Logger logger = LoggerFactory.getLogger(GuildIni.class);
	private final static LinkedHashMap<String, LinkedHashMap<String, String>> fileContent = new LinkedHashMap<String, LinkedHashMap<String, String>>();
	
	public static void initialize() {
		LinkedHashMap<String, String> messages = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> muteKickBan = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> competitive = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> reactions = new LinkedHashMap<String, String>();
		
		//collect all Messages variables
		messages.put("SpamDetection", "false");
		messages.put("MessagesLimit", "5");
		messages.put("MessagesOverChannelsLimit", "3");
		messages.put("Expires", "5");
		fileContent.put("Messages", messages);
		
		//collect all Mute, Kick and Ban variables
		muteKickBan.put("MessageDeleteEnabled", "false");
		muteKickBan.put("ForceMessageDeletion", "false");
		muteKickBan.put("AutoDeleteMessages", "0");
		muteKickBan.put("SendReason", "false");
		fileContent.put("Mute", muteKickBan);
		fileContent.put("Kick", muteKickBan);
		fileContent.put("Ban", muteKickBan);
		
		//collect all Competitive variables
		competitive.put("Team1", "");
		competitive.put("Team2", "");
		fileContent.put("Competitive", competitive);
		
		//collect all Reactions variables
		reactions.put("Enabled", "false");
		reactions.put("Emoji1", "");
		reactions.put("Emoji2", "");
		reactions.put("Emoji3", "");
		reactions.put("Emoji4", "");
		reactions.put("Emoji5", "");
		reactions.put("Emoji6", "");
		reactions.put("Emoji7", "");
		reactions.put("Emoji8", "");
		reactions.put("Emoji9", "");
		reactions.put("VoteThumbsUp", "");
		reactions.put("VoteThumbsDown", "");
		reactions.put("VoteShrug", "");
		fileContent.put("Reactions", reactions);
	}
	
	private static String buildFileName(Guild guild) {
		return guild.getJDA().getSelfUser().getName()+"_"+guild.getId()+".ini";
	}
	
	public static void createIni(Guild guild) {
		try {
			Ini ini = new Ini();
			
			//General
			fileContent.get("Messages").forEach((key, value) -> {
				ini.add("Messages", key, value);
			});
			
			//Mute
			fileContent.get("Mute").forEach((key, value) -> {
				ini.add("Mute", key, value);
			});
			
			//Kick
			fileContent.get("Kick").forEach((key, value) -> {
				ini.add("Kick", key, value);
			});
			
			//Ban
			fileContent.get("Ban").forEach((key, value) -> {
				ini.add("Ban", key, value);
			});
			
			//Reactions
			fileContent.get("Reactions").forEach((key, value) -> {
				ini.add("Reactions", key, value);
			});
			
			ini.store(new File("ini/"+buildFileName(guild)));
		} catch (IOException e) {
			logger.error("Error while creating guild ini file {}", buildFileName(guild), e);
		}
	}
	
	public static void verifyIni(Guild guild) {
		Ini ini = readIni(guild);
		Set<String> keyFound = new HashSet<String>();
		ini.forEach((key, values) -> {
			keyFound.add(key);
			LinkedHashMap<String, String> currentSection = fileContent.get(key);
			if(currentSection != null) {
				Set<String> subKeyFound = new HashSet<String>();
				values.forEach((subKey, value) -> {
					if(currentSection.get(subKey) == null)
						ini.remove(key, subKey);
					else
						subKeyFound.add(subKey);
				});
				currentSection.forEach((subKey, value) -> {
					if(!subKeyFound.contains(subKey))
						ini.add(key, subKey, value);
				});
			}
			else {
				ini.remove(key);
			}
		});
		fileContent.forEach((key, values) -> {
			if(!keyFound.contains(key)) {
				ini.add(key);
				values.forEach((subKey, value) -> {
					ini.add(key, subKey, value);
				});
			}
		});
		
		try {
			ini.store();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Ini readIni(Guild guild) {
		try {
			return new Ini(new File("./ini/"+buildFileName(guild)));
		} catch (IOException e) {
			logger.error("Error while reading guild ini file {}", buildFileName(guild), e);
			return null;
		}
	}
	
	public static boolean saveIniOption(Guild guild, String field, String value) {
		try {
			Ini ini = readIni(guild);
			String [] selectedField = field.split("_");
			ini.put(selectedField[0], selectedField[1], value);
			ini.store(new File("./ini/"+buildFileName(guild)));
			return true;
		} catch (IOException e) {
			logger.error("Error while overwriting guild ini file {}", buildFileName(guild), e);
		}
		return false;
	}
	
	public static boolean getSpamDetection(Guild guild) {
		Ini ini = readIni(guild);
		return ini.get("Messages", "SpamDetection", boolean.class);
	}
	public static int getMessagesLimit(Guild guild) {
		Ini ini = readIni(guild);
		return ini.get("Messages", "MessagesLimit", int.class);
	}
	public static int getMessageOverChannelsLimit(Guild guild) {
		Ini ini = readIni(guild);
		return ini.get("Messages", "MessagesOverChannelsLimit", int.class);
	}
	public static long getMessageExpires(Guild guild) {
		Ini ini = readIni(guild);
		return ini.get("Messages", "Expires", long.class)*60*1000;
	}
	
	public static boolean getMuteMessageDeleteEnabled(Guild guild) {
		Ini ini = readIni(guild);
		return ini.get("Mute", "MessageDeleteEnabled", boolean.class);
	}
	
	public static boolean getMuteForceMessageDeletion(Guild guild) {
		Ini ini = readIni(guild);
		return ini.get("Mute", "ForceMessageDeletion", boolean.class);
	}
	
	public static int getMuteAutoDeleteMessages(Guild guild) {
		Ini ini = readIni(guild);
		return ini.get("Mute", "AutoDeleteMessages", int.class);
	}
	
	public static boolean getMuteSendReason(Guild guild) {
		Ini ini = readIni(guild);
		return ini.get("Mute", "SendReason", boolean.class);
	}
	
	public static boolean getKickMessageDeleteEnabled(Guild guild) {
		Ini ini = readIni(guild);
		return ini.get("Kick", "MessageDeleteEnabled", boolean.class);
	}
	
	public static boolean getKickForceMessageDeletion(Guild guild) {
		Ini ini = readIni(guild);
		return ini.get("Kick", "ForceMessageDeletion", boolean.class);
	}
	
	public static int getKickAutoDeleteMessages(Guild guild) {
		Ini ini = readIni(guild);
		return ini.get("Kick", "AutoDeleteMessages", int.class);
	}
	
	public static boolean getKickSendReason(Guild guild) {
		Ini ini = readIni(guild);
		return ini.get("Kick", "SendReason", boolean.class);
	}
	
	public static boolean getBanMessageDeleteEnabled(Guild guild) {
		Ini ini = readIni(guild);
		return ini.get("Ban", "MessageDeleteEnabled", boolean.class);
	}
	
	public static boolean getBanForceMessageDeletion(Guild guild) {
		Ini ini = readIni(guild);
		return ini.get("Ban", "ForceMessageDeletion", boolean.class);
	}
	
	public static int getBanAutoDeleteMessages(Guild guild) {
		Ini ini = readIni(guild);
		return ini.get("Ban", "AutoDeleteMessages", int.class);
	}
	
	public static boolean getBanSendReason(Guild guild) {
		Ini ini = readIni(guild);
		return ini.get("Ban", "SendReason", boolean.class);
	}
	
	public static String getCompetitiveTeam1(Guild guild) {
		Ini ini = readIni(guild);
		return ini.get("Competitive", "Team1");
	}
	public static String getCompetitiveTeam2(Guild guild) {
		Ini ini = readIni(guild);
		return ini.get("Competitive", "Team2");
	}
	
	public static boolean getReactionEnabled(Guild guild) {
		Ini ini = readIni(guild);
		return ini.get("Reactions", "Enabled", boolean.class);
	}
	
	public static String[] getReactions(Guild guild) {
		Ini ini = readIni(guild);
		String[] reaction = new String[9];
		reaction[0] = ini.get("Reactions", "Emoji1");
		reaction[1] = ini.get("Reactions", "Emoji2");
		reaction[2] = ini.get("Reactions", "Emoji3");
		reaction[3] = ini.get("Reactions", "Emoji4");
		reaction[4] = ini.get("Reactions", "Emoji5");
		reaction[5] = ini.get("Reactions", "Emoji6");
		reaction[6] = ini.get("Reactions", "Emoji7");
		reaction[7] = ini.get("Reactions", "Emoji8");
		reaction[8] = ini.get("Reactions", "Emoji9");
		return reaction;
	}
	public static String[] getVoteReactions(Guild guild) {
		Ini ini = readIni(guild);
		String[] reaction = new String[3];
		reaction[0] = ini.get("Reactions", "VoteThumbsUp");
		reaction[1] = ini.get("Reactions", "VoteThumbsDown");
		reaction[2] = ini.get("Reactions", "VoteShrug");
		return reaction;
	}
}
