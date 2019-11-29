package fileManagement;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuildIni {
	private static Logger logger = LoggerFactory.getLogger(GuildIni.class);
	private final static LinkedHashMap<String, LinkedHashMap<String, String>> fileContent = new LinkedHashMap<String, LinkedHashMap<String, String>>();
	
	public static void initialize() {
		LinkedHashMap<String, String> general = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> patch = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> muteKickBan = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> pastebin = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> reactions = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> commands = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> commandLevels = new LinkedHashMap<String, String>();
		
		//collect all General variables
		general.put("Administrator", "");
		general.put("CommandPrefix", "H!");
		general.put("JoinMessage", "false");
		general.put("LeaveMessage", "false");
		general.put("ChannelLog", "false");
		general.put("CacheLog", "false");
		general.put("DoubleExperience", "auto");
		general.put("ForceReason", "false");
		general.put("OverrideBan", "false");
		general.put("URLBlacklist", "false");
		general.put("SelfDeletedMessage", "false");
		general.put("EditedMessage", "false");
		general.put("EditedMessageHistory", "false");
		fileContent.put("General", general);
		
		//collect all Patch variables
		patch.put("PrivatePatchNotes", "true");
		patch.put("PublicPatchNotes", "true");
		fileContent.put("Patch", patch);
		
		//collect all Mute, Kick and Ban variables
		muteKickBan.put("MessageDeleteEnabled", "false");
		muteKickBan.put("ForceMessageDeletion", "false");
		muteKickBan.put("AutoDeleteMessages", "0");
		muteKickBan.put("SendReason", "false");
		fileContent.put("Mute", muteKickBan);
		fileContent.put("Kick", muteKickBan);
		fileContent.put("Ban", muteKickBan);
		
		//collect all Pastebin variables
		pastebin.put("Username", "");
		pastebin.put("Password", "");
		fileContent.put("Pastebin", pastebin);
		
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
		fileContent.put("Reactions", reactions);
		
		//collect all Commands variables
		commands.put("About", "false");
		commands.put("Commands", "false");
		commands.put("Daily", "false");
		commands.put("Display", "false");
		commands.put("Help", "false");
		commands.put("Inventory", "false");
		commands.put("Meow", "false");
		commands.put("Profile", "false");
		commands.put("Pug", "false");
		commands.put("Purchase", "false");
		commands.put("Rank", "false");
		commands.put("Register", "false");
		commands.put("Set", "false");
		commands.put("Shop", "false");
		commands.put("Top", "false");
		commands.put("Use", "false");
		commands.put("User", "false");
		commands.put("Filter", "false");
		commands.put("Quiz", "false");
		commands.put("RoleReaction", "false");
		commands.put("Rss", "false");
		commands.put("Randomshop", "false");
		commands.put("Patchnotes", "false");
		commands.put("DoubleExperience", "false");
		commands.put("Equip", "false");
		commands.put("Remove", "false");
		commands.put("HeavyCensoring", "false");
		commands.put("Mute", "false");
		fileContent.put("Commands", commands);
		
		//collect all CommandLevels variables
		commandLevels.put("About", "1");
		commandLevels.put("Commands", "1");
		commandLevels.put("CommandsAdmin", "20");
		commandLevels.put("Daily", "1");
		commandLevels.put("Display", "1");
		commandLevels.put("DisplayRoles", "20");
		commandLevels.put("DisplayRegisteredRoles", "20");
		commandLevels.put("DisplayRankingRoles", "1");
		commandLevels.put("DisplayTextChannels", "20");
		commandLevels.put("DisplayVoiceChannels", "20");
		commandLevels.put("DisplayRegisteredChannels", "20");
		commandLevels.put("DisplayDailies", "1");
		commandLevels.put("WatchedUsers", "20");
		commandLevels.put("DisplayCommandLevels", "20");
		commandLevels.put("Help", "1");
		commandLevels.put("Inventory", "1");
		commandLevels.put("Meow", "1");
		commandLevels.put("Profile", "1");
		commandLevels.put("Pug", "1");
		commandLevels.put("Purchase", "1");
		commandLevels.put("Rank", "1");
		commandLevels.put("Register", "20");
		commandLevels.put("RegisterRole", "20");
		commandLevels.put("RegisterTextChannel", "20");
		commandLevels.put("RegisterTextChannelURL", "20");
		commandLevels.put("RegisterTextChannelTXT", "20");
		commandLevels.put("RegisterRankingRole", "20");
		commandLevels.put("RegisterTextChannels", "20");
		commandLevels.put("RegisterUsers", "20");
		commandLevels.put("Set", "20");
		commandLevels.put("SetPrivilege", "20");
		commandLevels.put("SetChannelFilter", "20");
		commandLevels.put("SetWarnings", "20");
		commandLevels.put("SetCommands", "20");
		commandLevels.put("SetRanking", "20");
		commandLevels.put("SetMaxExperience", "20");
		commandLevels.put("SetDefaultLevelSkin", "20");
		commandLevels.put("SetDefaultRankSkin", "20");
		commandLevels.put("SetDefaultProfileSkin", "20");
		commandLevels.put("SetDefaultIconSkin", "20");
		commandLevels.put("SetDailyItem", "20");
		commandLevels.put("SetGiveawayItems", "20");
		commandLevels.put("Shop", "1");
		commandLevels.put("Top", "1");
		commandLevels.put("Use", "1");
		commandLevels.put("User", "20");
		commandLevels.put("UserInformation", "20");
		commandLevels.put("UserDeleteMessages", "20");
		commandLevels.put("UserWarning", "20");
		commandLevels.put("UserWarningForce", "20");
		commandLevels.put("UserMute", "20");
		commandLevels.put("UserUnmute", "20");
		commandLevels.put("UserBan", "20");
		commandLevels.put("UserKick", "20");
		commandLevels.put("UserHistory", "20");
		commandLevels.put("UserWatch", "20");
		commandLevels.put("UserUnwatch", "20");
		commandLevels.put("UserUseWatchChannel", "20");
		commandLevels.put("UserGiftExperience", "20");
		commandLevels.put("UserSetExperience", "20");
		commandLevels.put("UserSetLevel", "20");
		commandLevels.put("UserGiftCurrency", "20");
		commandLevels.put("UserSetCurrency", "20");
		commandLevels.put("Filter", "20");
		commandLevels.put("FilterWordFilter", "20");
		commandLevels.put("FilterNameFilter", "20");
		commandLevels.put("FilterNameKick", "20");
		commandLevels.put("FilterFunnyNames", "20");
		commandLevels.put("FilterStaffNames", "20");
		commandLevels.put("FilterURLBlacklist", "20");
		commandLevels.put("FilterURLWhitelist", "20");
		commandLevels.put("FilterTweetBlacklist", "20");
		commandLevels.put("Quiz", "1");
		commandLevels.put("RoleReaction", "1");
		commandLevels.put("Rss", "1");
		commandLevels.put("Randomshop", "1");
		commandLevels.put("Patchnotes", "1");
		commandLevels.put("DoubleExperience", "1");
		commandLevels.put("Equip", "1");
		commandLevels.put("Remove", "20");
		commandLevels.put("HeavyCensoring", "20");
		commandLevels.put("Mute", "20");
		fileContent.put("CommandLevels", commandLevels);
	}
	
	public static void createIni(long guild_id) {
		try {
			Ini ini = new Ini();
			
			//General
			fileContent.get("General").forEach((key, value) -> {
				ini.add("General", key, value);
			});
			
			//Patch
			fileContent.get("Patch").forEach((key, value) -> {
				ini.add("Patch", key, value);
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
			
			//Pastebin
			fileContent.get("Pastebin").forEach((key, value) -> {
				ini.add("Pastebin", key, value);
			});
			
			//Reactions
			fileContent.get("Reactions").forEach((key, value) -> {
				ini.add("Reactions", key, value);
			});
			
			//Commands
			fileContent.get("Commands").forEach((key, value) -> {
				ini.add("Commands", key, value);
			});
			
			//CommandLevels
			fileContent.get("CommandLevels").forEach((key, value) -> {
				ini.add("CommandLevels", key, value);
			});
			
			ini.store(new File("ini/"+guild_id+".ini"));
		} catch (IOException e) {
			logger.error("Error while creating guild ini file {}.ini", guild_id, e);
		}
	}
	
	public static void verifyIni(long guild_id) {
		Ini ini = readIni(guild_id);
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
	
	private static Ini readIni(long guild_id) {
		try {
			return new Ini(new File("./ini/"+guild_id+".ini"));
		} catch (IOException e) {
			logger.error("Error while reading guild ini file {}.ini", guild_id, e);
			return null;
		}
	}
	
	public static long getAdmin(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("General", "Administrator", long.class);
	}
	
	public static String getCommandPrefix(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("General", "CommandPrefix");
	}
	
	public static boolean getJoinMessage(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("General", "JoinMessage", boolean.class);
	}
	
	public static boolean getLeaveMessage(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("General", "LeaveMessage", boolean.class);
	}
	
	public static boolean getCacheLog(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("General", "CacheLog", boolean.class);
	}
	
	public static boolean [] getChannelAndCacheLog(long guild_id) {
		Ini ini = readIni(guild_id);
		boolean [] log = new boolean[2];
		log[0] = ini.get("General", "ChannelLog", boolean.class);
		log[1] = ini.get("General", "CacheLog", boolean.class);
		return log;
	}
	
	public static boolean getURLBlacklist(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("General", "URLBlacklist", boolean.class);
	}
	
	public static void setDoubleExperienceMode(long guild_id, final String mode) {
		try {
			Ini ini = readIni(guild_id);
			ini.put("General", "DoubleExperience", mode);
			ini.store(new File("./ini/"+guild_id+".ini"));
		} catch (IOException e) {
			logger.error("Error while overwriting guild ini file {}.ini", guild_id, e);
		}
	}
	
	public static String getDoubleExperienceMode(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("General", "DoubleExperience");
	}
	
	public static boolean getForceReason(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("General", "ForceReason", boolean.class);
	}
	
	public static boolean getOverrideBan(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("General", "OverrideBan", boolean.class);
	}
	public static boolean getSelfDeletedMessage(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("General", "SelfDeletedMessage", boolean.class);
	}
	public static boolean getEditedMessage(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("General", "EditedMessage", boolean.class);
	}
	public static boolean getEditedMessageHistory(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("General", "EditedMessageHistory", boolean.class);
	}
	
	public static boolean getPrivatePatchNotes(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Patch", "PrivatePatchNotes", boolean.class);
	}
	public static boolean getPublicPatchNotes(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Patch", "PublicPatchNotes", boolean.class);
	}
	
	public static boolean getMuteMessageDeleteEnabled(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Mute", "MessageDeleteEnabled", boolean.class);
	}
	
	public static boolean getMuteForceMessageDeletion(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Mute", "ForceMessageDeletion", boolean.class);
	}
	
	public static int getMuteAutoDeleteMessages(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Mute", "AutoDeleteMessages", int.class);
	}
	
	public static boolean getMuteSendReason(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Mute", "SendReason", boolean.class);
	}
	
	public static boolean getKickMessageDeleteEnabled(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Kick", "MessageDeleteEnabled", boolean.class);
	}
	
	public static boolean getKickForceMessageDeletion(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Kick", "ForceMessageDeletion", boolean.class);
	}
	
	public static int getKickAutoDeleteMessages(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Kick", "AutoDeleteMessages", int.class);
	}
	
	public static boolean getKickSendReason(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Kick", "SendReason", boolean.class);
	}
	
	public static boolean getBanMessageDeleteEnabled(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Ban", "MessageDeleteEnabled", boolean.class);
	}
	
	public static boolean getBanForceMessageDeletion(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Ban", "ForceMessageDeletion", boolean.class);
	}
	
	public static int getBanAutoDeleteMessages(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Ban", "AutoDeleteMessages", int.class);
	}
	
	public static boolean getBanSendReason(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Ban", "SendReason", boolean.class);
	}
	
	public static String[] getPastebinCredentials(long guild_id) {
		Ini ini = readIni(guild_id);
		Ini.Section pastebin = ini.get("Pastebin");
		String[] credentials = new String[2];
		credentials[0] = pastebin.get("Username");
		credentials[1] = pastebin.get("Password");
		return credentials;
	}
	
	public static String getPastebinKey(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Pastebin", "Key");
	}
	
	public static boolean getReactionEnabled(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Reactions", "Enabled", boolean.class);
	}
	
	public static String[] getReactions(long guild_id) {
		Ini ini = readIni(guild_id);
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
	
	public static boolean getAboutCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "About", boolean.class);
	}
	public static int getAboutLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "About", int.class);
	}
	public static boolean getCommandsCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Commands", boolean.class);
	}
	public static int getCommandsLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "Commands", int.class);
	}
	public static int getCommandsAdminLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "CommandsAdmin", int.class);
	}
	public static boolean getDailyCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Daily", boolean.class);
	}
	public static int getDailyLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "Daily", int.class);
	}
	public static boolean getDisplayCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Display", boolean.class);
	}
	public static int getDisplayLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "Display", int.class);
	}
	public static int getDisplayRolesLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "DisplayRoles", int.class);
	}
	public static int getDisplayRegisteredRolesLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "DisplayRegisteredRoles", int.class);
	}
	public static int getDisplayRankingRolesLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "DisplayRankingRoles", int.class);
	}
	public static int getDisplayTextChannelsLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "DisplayTextChannels", int.class);
	}
	public static int getDisplayVoiceChannelsLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "DisplayVoiceChannels", int.class);
	}
	public static int getDisplayRegisteredChannelsLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "DisplayRegisteredChannels", int.class);
	}
	public static int getDisplayDailiesLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "DisplayDailies", int.class);
	}
	public static int getDisplayWatchedUsersLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "DisplayWatchedUsers", int.class);
	}
	public static int getDisplayCommandLevelsLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "DisplayCommandLevels", int.class);
	}
	public static boolean getHelpCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Help", boolean.class);
	}
	public static int getHelpLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "Help", int.class);
	}
	public static boolean getInventoryCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Inventory", boolean.class);
	}
	public static int getInventoryLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "Inventory", int.class);
	}
	public static boolean getMeowCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Meow", boolean.class);
	}
	public static int getMeowLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "Meow", int.class);
	}
	public static boolean getProfileCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Profile", boolean.class);
	}
	public static int getProfileLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "Profile", int.class);
	}
	public static boolean getPugCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Pug", boolean.class);
	}
	public static int getPugLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "Pug", int.class);
	}
	public static boolean getRankCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Rank", boolean.class);
	}
	public static int getRankLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "Rank", int.class);
	}
	public static boolean getRegisterCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Register", boolean.class);
	}
	public static int getRegisterLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "Register", int.class);
	}
	public static int getRegisterRoleLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "RegisterRole", int.class);
	}
	public static int getRegisterTextChannelLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "RegisterTextChannel", int.class);
	}
	public static int getRegisterTextChannelURLLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "RegisterTextChannelURL", int.class);
	}
	public static int getRegisterTextChannelTXTLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "RegisterTextChannelTXT", int.class);
	}
	public static int getRegisterRankingRoleLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "RegisterRankingRole", int.class);
	}
	public static int getRegisterTextChannelsLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "RegisterTextChannels", int.class);
	}
	public static int getRegisterUsersLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "RegisterUsers", int.class);
	}
	public static boolean getSetCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Set", boolean.class);
	}
	public static int getSetLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "Set", int.class);
	}
	public static int getSetPrivilegeLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "SetPrivilege", int.class);
	}
	public static int getSetChannelFilterLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "SetChannelFilter", int.class);
	}
	public static int getSetWarningsLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "SetWarnings", int.class);
	}
	public static int getSetCommandsLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "SetCommands", int.class);
	}
	public static int getSetRankingLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "SetRanking", int.class);
	}
	public static int getSetMaxExperienceLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "SetMaxExperience", int.class);
	}
	public static int getSetDefaultLevelSkinLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "SetDefaultLevelSkin", int.class);
	}
	public static int getSetDefaultRankSkinLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "SetDefaultRankSkin", int.class);
	}
	public static int getSetDefaultProfileSkinLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "SetDefaultProfileSkin", int.class);
	}
	public static int getSetDefaultIconSkinLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "SetDefaultIconSkin", int.class);
	}
	public static int getSetDailyItemLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "SetDailyItem", int.class);
	}
	public static int getSetGiveawayItemsLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "SetGiveawayItems", int.class);
	}
	public static boolean getShopCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Shop", boolean.class);
	}
	public static int getShopLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "Shop", int.class);
	}
	public static boolean getTopCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Top", boolean.class);
	}
	public static int getTopLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "Top", int.class);
	}
	public static boolean getUseCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Use", boolean.class);
	}
	public static int getUseLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "Use", int.class);
	}
	public static boolean getUserCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "User", boolean.class);
	}
	public static int getUserLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "User", int.class);
	}
	public static int getUserInformationLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "UserInformation", int.class);
	}
	public static int getUserDeleteMessagesLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "UserDeleteMessages", int.class);
	}
	public static int getUserWarningLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "UserWarning", int.class);
	}
	public static int getUserWarningForceLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "UserWarningForce", int.class);
	}
	public static int getUserMuteLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "UserMute", int.class);
	}
	public static int getUserUnmuteLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "UserUnmute", int.class);
	}
	public static int getUserBanLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "UserBan", int.class);
	}
	public static int getUserKickLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "UserKick", int.class);
	}
	public static int getUserHistoryLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "UserHistory", int.class);
	}
	public static int getUserWatchLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "UserWatch", int.class);
	}
	public static int getUserUnwatchLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "UserUnwatch", int.class);
	}
	public static int getUserUseWatchChannelLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "UserUseWatchChannel", int.class);
	}
	public static int getUserGiftExperienceLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "UserGiftExperience", int.class);
	}
	public static int getUserSetExperienceLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "UserSetExperience", int.class);
	}
	public static int getUserSetLevelLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "UserSetLevel", int.class);
	}
	public static int getUserGiftCurrencyLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "UserGiftCurrency", int.class);
	}
	public static int getUserSetCurrencyLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "UserSetCurrency", int.class);
	}
	public static boolean getFilterCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Filter", boolean.class);
	}
	public static int getFilterLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "Filter", int.class);
	}
	public static int getFilterWordFilterLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "FilterWordFilter", int.class);
	}
	public static int getFilterNameFilterLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "FilterNameFilter", int.class);
	}
	public static int getFilterNameKickLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "FilterNameKick", int.class);
	}
	public static int getFilterFunnyNamesLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "FilterFunnyNames", int.class);
	}
	public static int getFilterStaffNamesLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "FilterStaffNames", int.class);
	}
	public static int getFilterURLBlacklistLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "FilterURLBlacklist", int.class);
	}
	public static int getFilterURLWhitelistLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "FilterURLWhitelist", int.class);
	}
	public static int getFilterTweetBlacklistLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "FilterTweetBlacklist", int.class);
	}
	public static boolean getQuizCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Quiz", boolean.class);
	}
	public static int getQuizLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "Quiz", int.class);
	}
	public static boolean getRoleReactionCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "RoleReaction", boolean.class);
	}
	public static int getRoleReactionLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "RoleReaction", int.class);
	}
	public static boolean getRssCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Rss", boolean.class);
	}
	public static int getRssLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "Rss", int.class);
	}
	public static boolean getRandomshopCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Randomshop", boolean.class);
	}
	public static int getRandomshopLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "Randomshop", int.class);
	}
	public static boolean getPatchnotesCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Patchnotes", boolean.class);
	}
	public static int getPatchnotesLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "Patchnotes", int.class);
	}
	public static boolean getDoubleExperienceCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "DoubleExperience", boolean.class);
	}
	public static int getDoubleExperienceLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "DoubleExperience", int.class);
	}
	public static boolean getEquipCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Equip", boolean.class);
	}
	public static int getEquipLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "Equip", int.class);
	}
	public static boolean getRemoveCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Remove", boolean.class);
	}
	public static int getRemoveLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "Remove", int.class);
	}
	public static boolean getHeavyCensoringCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "HeavyCensoring", boolean.class);
	}
	public static int getHeavyCensoringLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "HeavyCensoring", int.class);
	}
	public static boolean getMuteCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Mute", boolean.class);
	}
	public static int getMuteLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("CommandLevels", "Mute", int.class);
	}
}
