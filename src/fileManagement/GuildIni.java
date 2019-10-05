package fileManagement;

import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuildIni {
	private static Logger logger = LoggerFactory.getLogger(GuildIni.class);
	
	public static void createIni(long guild_id) {
		try {
			Ini ini = new Ini();
			
			//General
			ini.add("General", "Administrator", "");
			ini.add("General", "CommandPrefix", "");
			ini.add("General", "JoinMessage", "false");
			ini.add("General", "LeaveMessage", "false");
			ini.add("General", "ChannelLog", "false");
			ini.add("General", "CacheLog", "false");
			ini.add("General", "DoubleExperience", "auto");
			ini.add("General", "ForceReason", "true");
			ini.add("General", "OverrideBan", "false");
			ini.add("General", "URLBlacklist", "false");
			ini.add("General", "SelfDeletedMessage", "false");
			ini.add("General", "EditedMessage" , "false");
			
			//Patch
			ini.add("Patch", "PrivatePatchNotes", "true");
			ini.add("Patch", "PublicPatchNotes", "true");
			
			//Mute
			ini.add("Mute", "MessageDeleteEnabled", "false");
			ini.add("Mute", "ForceMessageDeletion", "false");
			ini.add("Mute", "AutoDeleteMessages", "0");
			ini.add("Mute", "SendReason", "false");
			
			//Kick
			ini.add("Kick", "MessageDeleteEnabled", "false");
			ini.add("Kick", "ForceMessageDeletion", "false");
			ini.add("Kick", "AutoDeleteMessages", "0");
			ini.add("Kick", "SendReason", "false");
			
			//Ban
			ini.add("Ban", "MessageDeleteEnabled", "false");
			ini.add("Ban", "ForceMessageDeletion", "false");
			ini.add("Ban", "AutoDeleteMessages", "0");
			ini.add("Ban", "SendReason", "false");
			
			//Pastebin
			ini.add("Pastebin", "Username", "");
			ini.add("Pastebin", "Password", "");
			
			//Reactions
			ini.add("Reactions", "Enabled", "false");
			ini.add("Reactions", "Emoji1", "");
			ini.add("Reactions", "Emoji2", "");
			ini.add("Reactions", "Emoji3", "");
			ini.add("Reactions", "Emoji4", "");
			ini.add("Reactions", "Emoji5", "");
			ini.add("Reactions", "Emoji6", "");
			ini.add("Reactions", "Emoji7", "");
			ini.add("Reactions", "Emoji8", "");
			ini.add("Reactions", "Emoji9", "");
			
			//Commands
			ini.add("Commands", "About", "false");
			ini.add("Commands", "AboutLevel", "1");
			ini.add("Commands", "Commands", "false");
			ini.add("Commands", "CommandsLevel", "1");
			ini.add("Commands", "CommandsAdminLevel", "20");
			ini.add("Commands", "Daily", "false");
			ini.add("Commands", "DailyLevel", "1");
			ini.add("Commands", "Display", "false");
			ini.add("Commands", "DisplayLevel", "1");
			ini.add("Commands", "DisplayRolesLevel", "1");
			ini.add("Commands", "DisplayRegisteredRolesLevel", "1");
			ini.add("Commands", "DisplayRankingRolesLevel", "1");
			ini.add("Commands", "DisplayTextChannelsLevel", "1");
			ini.add("Commands", "DisplayVoiceChannelsLevel", "1");
			ini.add("Commands", "DisplayRegisteredChannelsLevel", "1");
			ini.add("Commands", "DisplayDailiesLevel", "1");
			ini.add("Commands", "DisplayWatchedUsersLevel", "1");
			ini.add("Commands", "DisplayCommandLevelsLevel", "1");
			ini.add("Commands", "Help", "false");
			ini.add("Commands", "HelpLevel", "1");
			ini.add("Commands", "Inventory", "false");
			ini.add("Commands", "InventoryLevel", "1");
			ini.add("Commands", "Meow", "false");
			ini.add("Commands", "MeowLevel", "1");
			ini.add("Commands", "Profile", "false");
			ini.add("Commands", "ProfileLevel", "1");
			ini.add("Commands", "Pug", "false");
			ini.add("Commands", "PugLevel", "1");
			ini.add("Commands", "Rank", "false");
			ini.add("Commands", "RankLevel", "1");
			ini.add("Commands", "Register", "false");
			ini.add("Commands", "RegisterLevel", "20");
			ini.add("Commands", "RegisterRoleLevel", "20");
			ini.add("Commands", "RegisterTextChannelLevel", "20");
			ini.add("Commands", "RegisterTextChannelURLLevel", "20");
			ini.add("Commands", "RegisterTextChannelTXTLevel", "20");
			ini.add("Commands", "RegisterRankingRoleLevel", "20");
			ini.add("Commands", "RegisterTextChannelsLevel", "20");
			ini.add("Commands", "RegisterUsersLevel", "20");
			ini.add("Commands", "Set", "false");
			ini.add("Commands", "SetLevel", "20");
			ini.add("Commands", "SetPrivilegeLevel", "100");
			ini.add("Commands", "SetChannelFilterLevel", "20");
			ini.add("Commands", "SetWarningsLevel", "20");
			ini.add("Commands", "SetCommandsLevel", "20");
			ini.add("Commands", "SetRankingLevel", "20");
			ini.add("Commands", "SetMaxExperienceLevel", "20");
			ini.add("Commands", "SetDefaultLevelSkinLevel", "20");
			ini.add("Commands", "SetDefaultRankSkinLevel", "20");
			ini.add("Commands", "SetDefaultProfileSkinLevel", "20");
			ini.add("Commands", "SetDefaultIconSkinLevel", "20");
			ini.add("Commands", "SetDailyItemLevel", "20");
			ini.add("Commands", "SetGiveawayItemsLevel", "20");
			ini.add("Commands", "Shop", "false");
			ini.add("Commands", "ShopLevel", "1");
			ini.add("Commands", "Top", "false");
			ini.add("Commands", "TopLevel", "1");
			ini.add("Commands", "Use", "false");
			ini.add("Commands", "UseLevel", "1");
			ini.add("Commands", "User", "false");
			ini.add("Commands", "UserLevel", "20");
			ini.add("Commands", "UserInformationLevel", "20");
			ini.add("Commands", "UserDeleteMessagesLevel", "20");
			ini.add("Commands", "UserWarningLevel", "20");
			ini.add("Commands", "UserWarningForceLevel", "20");
			ini.add("Commands", "UserMuteLevel", "20");
			ini.add("Commands", "UserUnmuteLevel", "20");
			ini.add("Commands", "UserBanLevel", "20");
			ini.add("Commands", "UserKickLevel", "20");
			ini.add("Commands", "UserHistoryLevel", "20");
			ini.add("Commands", "UserWatchLevel", "20");
			ini.add("Commands", "UserUnwatchLevel", "20");
			ini.add("Commands", "UserUseWatchChannelLevel", "20");
			ini.add("Commands", "UserGiftExperienceLevel", "20");
			ini.add("Commands", "UserSetExperienceLevel", "20");
			ini.add("Commands", "UserSetLevelLevel", "20");
			ini.add("Commands", "UserGiftCurrencyLevel", "20");
			ini.add("Commands", "UserSetCurrencyLevel", "20");
			ini.add("Commands", "Filter", "false");
			ini.add("Commands", "FilterLevel", "20");
			ini.add("Commands", "FilterWordFilterLevel", "20");
			ini.add("Commands", "FilterNameFilterLevel", "20");
			ini.add("Commands", "FilterNameKickLevel", "20");
			ini.add("Commands", "FilterFunnyNamesLevel", "20");
			ini.add("Commands", "FilterStaffNamesLevel", "20");
			ini.add("Commands", "FilterURLBlacklistLevel", "20");
			ini.add("Commands", "FilterURLWhitelistLevel", "20");
			ini.add("Commands", "FilterTweetBlacklistLevel", "20");
			ini.add("Commands", "Quiz", "false");
			ini.add("Commands", "QuizLevel", "20");
			ini.add("Commands", "RoleReaction", "false");
			ini.add("Commands", "RoleReactionLevel", "20");
			ini.add("Commands", "Rss", "false");
			ini.add("Commands", "RssLevel", "20");
			ini.add("Commands", "Randomshop", "false");
			ini.add("Commands", "RandomshopLevel", "1");
			ini.add("Commands", "Patchnotes", "false");
			ini.add("Commands", "PatchnotesLevel", "1");
			ini.add("Commands", "DoubleExperience", "false");
			ini.add("Commands", "DoubleExperienceLevel", "20");
			ini.add("Commands", "Equip", "false");
			ini.add("Commands", "EquipLevel", "1");
			ini.add("Commands", "Remove", "false");
			ini.add("Commands", "RemoveLevel", "20");
			ini.add("Commands", "HeavyCensoring", "false");
			ini.add("Commands", "HeavyCensoringLevel", "20");
			
			//Inventory
			ini.add("Inventory", "startX", "0");
			ini.add("Inventory", "startY", "0");
			ini.add("Inventory", "tabX", "0");
			ini.add("Inventory", "tabY", "0");
			ini.add("Inventory", "pageFontSize", "12");
			ini.add("Inventory", "pageX", "0");
			ini.add("Inventory", "pageY", "0");
			ini.add("Inventory", "generalTextFontSize", "0");
			ini.add("Inventory", "boxSizeX", "0");
			ini.add("Inventory", "boxSizeY", "0");
			ini.add("Inventory", "itemSizeX", "0");
			ini.add("Inventory", "itemSizeY", "0");
			ini.add("Inventory", "nextBoxX", "0");
			ini.add("Inventory", "nextBoxY", "0");
			ini.add("Inventory", "expiration-positionY", "0");
			ini.add("Inventory", "rowLimit", "0");
			ini.add("Inventory", "maxItems", "0");
			
			//RandomshopItems
			ini.add("RandomshopItems", "startX", "0");
			ini.add("RandomshopItems", "startY", "0");
			ini.add("RandomshopItems", "pageX", "0");
			ini.add("RandomshopItems", "pageY", "0");
			ini.add("RandomshopItems", "generalFontSize", "12");
			ini.add("RandomshopItems", "boxSizeX", "0");
			ini.add("RandomshopItems", "boxSizeY", "0");
			ini.add("RandomshopItems", "nextBoxX", "0");
			ini.add("RandomshopItems", "nextBoxY", "0");
			ini.add("RandomshopItems", "itemSizeX", "0");
			ini.add("RandomshopItems", "itemSizeY", "0");
			ini.add("RandomshopItems", "rowLimit", "0");
			ini.add("RandomshopItems", "maxItems", "0");
			
			//RandomshopReward
			ini.add("RandomshopReward", "itemSizeX", "0");
			ini.add("RandomshopReward", "itemSizeY", "0");
			
			//Daily
			ini.add("Daily", "rewardX", "0");
			ini.add("Daily", "rewardY", "0");
			ini.add("Daily", "generalTextFontSize", "12");
			ini.add("Daily", "descriptionMode", "0");
			ini.add("Daily", "descriptionX", "0");
			ini.add("Daily", "descriptionY", "0");
			ini.add("Daily", "descriptionStartX", "0");
			ini.add("Daily", "fieldSizeX", "0");
			
			//Profile
			ini.add("Profile", "nameLengthLimit", "0");
			ini.add("Profile", "generalTextFontSize", "12");
			ini.add("Profile", "nameTextFontSize", "12");
			ini.add("Profile", "descriptionMode", "0");
			
			//Rank
			ini.add("Rank", "nameLengthLimit", "0");
			ini.add("Rank", "generalTextFontSize", "12");
			ini.add("Rank", "nameTextFontSize", "12");
			
			//Level
			ini.add("Level", "nameLengthLimit", "0");
			ini.add("Level", "generalTextFontSize", "12");
			ini.add("Level", "nameTextFontSize", "12");
			
			ini.store(new File("ini/"+guild_id+".ini"));
		} catch (IOException e) {
			logger.error("Error while creating guild ini file {}.ini", guild_id, e);
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
		return ini.get("Commands", "AboutLevel", int.class);
	}
	public static boolean getCommandsCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Commands", boolean.class);
	}
	public static int getCommandsLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "CommandsLevel", int.class);
	}
	public static int getCommandsAdminLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "CommandsAdminLevel", int.class);
	}
	public static boolean getDailyCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Daily", boolean.class);
	}
	public static int getDailyLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "DailyLevel", int.class);
	}
	public static boolean getDisplayCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Display", boolean.class);
	}
	public static int getDisplayLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "DisplayLevel", int.class);
	}
	public static int getDisplayRolesLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "DisplayRolesLevel", int.class);
	}
	public static int getDisplayRegisteredRolesLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "DisplayRegisteredRolesLevel", int.class);
	}
	public static int getDisplayRankingRolesLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "DisplayRankingRolesLevel", int.class);
	}
	public static int getDisplayTextChannelsLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "DisplayTextChannelsLevel", int.class);
	}
	public static int getDisplayVoiceChannelsLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "DisplayVoiceChannelsLevel", int.class);
	}
	public static int getDisplayRegisteredChannelsLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "DisplayRegisteredChannelsLevel", int.class);
	}
	public static int getDisplayDailiesLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "DisplayDailiesLevel", int.class);
	}
	public static int getDisplayWatchedUsersLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "DisplayWatchedUsersLevel", int.class);
	}
	public static int getDisplayCommandLevelsLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "DisplayCommandLevelsLevel", int.class);
	}
	public static boolean getHelpCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Help", boolean.class);
	}
	public static int getHelpLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "HelpLevel", int.class);
	}
	public static boolean getInventoryCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Inventory", boolean.class);
	}
	public static int getInventoryLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "InventoryLevel", int.class);
	}
	public static boolean getMeowCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Meow", boolean.class);
	}
	public static int getMeowLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "MeowLevel", int.class);
	}
	public static boolean getProfileCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Profile", boolean.class);
	}
	public static int getProfileLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "ProfileLevel", int.class);
	}
	public static boolean getPugCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Pug", boolean.class);
	}
	public static int getPugLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "PugLevel", int.class);
	}
	public static boolean getRankCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Rank", boolean.class);
	}
	public static int getRankLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "RankLevel", int.class);
	}
	public static boolean getRegisterCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Register", boolean.class);
	}
	public static int getRegisterLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "RegisterLevel", int.class);
	}
	public static int getRegisterRoleLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "RegisterRoleLevel", int.class);
	}
	public static int getRegisterTextChannelLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "RegisterTextChannelLevel", int.class);
	}
	public static int getRegisterTextChannelURLLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "RegisterTextChannelURLLevel", int.class);
	}
	public static int getRegisterTextChannelTXTLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "RegisterTextChannelTXTLevel", int.class);
	}
	public static int getRegisterRankingRoleLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "RegisterRankingRoleLevel", int.class);
	}
	public static int getRegisterTextChannelsLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "RegisterTextChannelsLevel", int.class);
	}
	public static int getRegisterUsersLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "RegisterUsersLevel", int.class);
	}
	public static boolean getSetCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Set", boolean.class);
	}
	public static int getSetLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "SetLevel", int.class);
	}
	public static int getSetPrivilegeLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "SetPrivilegeLevel", int.class);
	}
	public static int getSetChannelFilterLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "SetChannelFilterLevel", int.class);
	}
	public static int getSetWarningsLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "SetWarningsLevel", int.class);
	}
	public static int getSetCommandsLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "SetCommandsLevel", int.class);
	}
	public static int getSetRankingLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "SetRankingLevel", int.class);
	}
	public static int getSetMaxExperienceLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "SetMaxExperienceLevel", int.class);
	}
	public static int getSetDefaultLevelSkinLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "SetDefaultLevelSkinLevel", int.class);
	}
	public static int getSetDefaultRankSkinLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "SetDefaultRankSkinLevel", int.class);
	}
	public static int getSetDefaultProfileSkinLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "SetDefaultProfileSkinLevel", int.class);
	}
	public static int getSetDefaultIconSkinLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "SetDefaultIconSkinLevel", int.class);
	}
	public static int getSetDailyItemLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "SetDailyItemLevel", int.class);
	}
	public static int getSetGiveawayItemsLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "SetGiveawayItemsLevel", int.class);
	}
	public static boolean getShopCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Shop", boolean.class);
	}
	public static int getShopLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "ShopLevel", int.class);
	}
	public static boolean getTopCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Top", boolean.class);
	}
	public static int getTopLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "TopLevel", int.class);
	}
	public static boolean getUseCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Use", boolean.class);
	}
	public static int getUseLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "UseLevel", int.class);
	}
	public static boolean getUserCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "User", boolean.class);
	}
	public static int getUserLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "UserLevel", int.class);
	}
	public static int getUserInformationLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "UserInformationLevel", int.class);
	}
	public static int getUserDeleteMessagesLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "UserDeleteMessagesLevel", int.class);
	}
	public static int getUserWarningLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "UserWarningLevel", int.class);
	}
	public static int getUserWarningForceLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "UserWarningForceLevel", int.class);
	}
	public static int getUserMuteLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "UserMuteLevel", int.class);
	}
	public static int getUserUnmuteLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "UserUnmuteLevel", int.class);
	}
	public static int getUserBanLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "UserBanLevel", int.class);
	}
	public static int getUserKickLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "UserKickLevel", int.class);
	}
	public static int getUserHistoryLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "UserHistoryLevel", int.class);
	}
	public static int getUserWatchLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "UserWatchLevel", int.class);
	}
	public static int getUserUnwatchLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "UserUnwatchLevel", int.class);
	}
	public static int getUserUseWatchChannelLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "UserUseWatchChannelLevel", int.class);
	}
	public static int getUserGiftExperienceLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "UserGiftExperienceLevel", int.class);
	}
	public static int getUserSetExperienceLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "UserSetExperienceLevel", int.class);
	}
	public static int getUserSetLevelLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "UserSetLevelLevel", int.class);
	}
	public static int getUserGiftCurrencyLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "UserGiftCurrencyLevel", int.class);
	}
	public static int getUserSetCurrencyLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "UserSetCurrencyLevel", int.class);
	}
	public static boolean getFilterCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Filter", boolean.class);
	}
	public static int getFilterLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "FilterLevel", int.class);
	}
	public static int getFilterWordFilterLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "FilterWordFilterLevel", int.class);
	}
	public static int getFilterNameFilterLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "FilterNameFilterLevel", int.class);
	}
	public static int getFilterNameKickLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "FilterNameKickLevel", int.class);
	}
	public static int getFilterFunnyNamesLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "FilterFunnyNamesLevel", int.class);
	}
	public static int getFilterStaffNamesLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "FilterStaffNamesLevel", int.class);
	}
	public static int getFilterURLBlacklistLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "FilterURLBlacklistLevel", int.class);
	}
	public static int getFilterURLWhitelistLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "FilterURLWhitelistLevel", int.class);
	}
	public static int getFilterTweetBlacklistLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "FilterTweetBlacklistLevel", int.class);
	}
	public static boolean getQuizCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Quiz", boolean.class);
	}
	public static int getQuizLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "QuizLevel", int.class);
	}
	public static boolean getRoleReactionCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "RoleReaction", boolean.class);
	}
	public static int getRoleReactionLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "RoleReactionLevel", int.class);
	}
	public static boolean getRssCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Rss", boolean.class);
	}
	public static int getRssLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "RssLevel", int.class);
	}
	public static boolean getRandomshopCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Randomshop", boolean.class);
	}
	public static int getRandomshopLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "RandomshopLevel", int.class);
	}
	public static boolean getPatchnotesCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Patchnotes", boolean.class);
	}
	public static int getPatchnotesLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "PatchnotesLevel", int.class);
	}
	public static boolean getDoubleExperienceCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "DoubleExperience", boolean.class);
	}
	public static int getDoubleExperienceLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "DoubleExperienceLevel", int.class);
	}
	public static boolean getEquipCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Equip", boolean.class);
	}
	public static int getEquipLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "EquipLevel", int.class);
	}
	public static boolean getRemoveCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Remove", boolean.class);
	}
	public static int getRemoveLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "RemoveLevel", int.class);
	}
	public static boolean getHeavyCensoring(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "HeavyCensoring", boolean.class);
	}
	public static int getHeavyCensoringLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "HeavyCensoringLevel", int.class);
	}
	
	public static int[] getWholeInventory(long guild_id) {
		Ini ini = readIni(guild_id);
		Ini.Section inventory = ini.get("Inventory");
		int[] inven = new int[17];
		inven[0] = inventory.get("startX", int.class);
		inven[1] = inventory.get("startY", int.class);
		inven[2] = inventory.get("tabX", int.class);
		inven[3] = inventory.get("tabY", int.class);
		inven[4] = inventory.get("pageFontSize", int.class);
		inven[5] = inventory.get("pageX", int.class);
		inven[6] = inventory.get("pageY", int.class);
		inven[7] = inventory.get("generalTextFontSize", int.class);
		inven[8] = inventory.get("boxSizeX", int.class);
		inven[9] = inventory.get("boxSizeY", int.class);
		inven[10] = inventory.get("descriptionY", int.class);
		inven[11] = inventory.get("itemSizeX", int.class);
		inven[12] = inventory.get("itemSizeY", int.class);
		inven[13] = inventory.get("nextBoxX", int.class);
		inven[14] = inventory.get("nextBoxY", int.class);
		inven[15] = inventory.get("expiration-positionY", int.class);
		inven[16] = inventory.get("rowLimit", int.class);
		return inven;
	}
	
	public static int getInventoryMaxItems(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Inventory", "maxItems", int.class);
	}
	
	public static int[] getWholeRandomshopItems(long guild_id) {
		Ini ini = readIni(guild_id);
		Ini.Section randomshop = ini.get("RandomshopItems");
		int[] rand = new int[12];
		rand[0] = randomshop.get("startX", int.class);
		rand[1] = randomshop.get("startY", int.class);
		rand[2] = randomshop.get("pageX", int.class);
		rand[3] = randomshop.get("pageY", int.class);
		rand[4] = randomshop.get("generalTextFontSize", int.class);
		rand[5] = randomshop.get("boxSizeX", int.class);
		rand[6] = randomshop.get("boxSizeY", int.class);
		rand[7] = randomshop.get("itemSizeX", int.class);
		rand[8] = randomshop.get("itemSizeY", int.class);
		rand[9] = randomshop.get("nextBoxX", int.class);
		rand[10] = randomshop.get("nextBoxY", int.class);
		rand[11] = randomshop.get("rowLimit", int.class);
		return rand;
	}
	
	public static int getRandomshopItemsMaxItems(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("RandomshopItems", "maxItems", int.class);
	}
	
	public static int[] getWholeRandomshopReward(long guild_id) {
		Ini ini = readIni(guild_id);
		Ini.Section randomshop = ini.get("RandomshopReward");
		int[] rand = new int[2];
		rand[0] = randomshop.get("itemSizeX", int.class);
		rand[1] = randomshop.get("itemSizeY", int.class);
		return rand;
	}
	
	public static int[] getWholeDaily(long guild_id) {
		Ini ini = readIni(guild_id);
		Ini.Section daily = ini.get("Daily");
		int [] dail = new int[8];
		dail[0] = daily.get("rewardX", int.class);
		dail[1] = daily.get("rewardY", int.class);
		dail[2] = daily.get("generalTextFontSize", int.class);
		dail[3] = daily.get("descriptionMode", int.class);
		dail[4] = daily.get("descriptionX", int.class);
		dail[5] = daily.get("descriptionY", int.class);
		dail[6] = daily.get("descriptionStartX", int.class);
		dail[7] = daily.get("fieldSizeX", int.class);
		return dail;
	}
	
	public static int[] getWholeProfile(long guild_id) {
		Ini ini = readIni(guild_id);
		Ini.Section profile = ini.get("Profile");
		int [] prof = new int[4];
		prof[0] = profile.get("nameLengthLimit", int.class);
		prof[1] = profile.get("generalTextFontSize", int.class);
		prof[2] = profile.get("nameTextFontSize", int.class);
		prof[3] = profile.get("descriptionMode", int.class);
		return prof;
	}
	
	public static int[] getWholeRank(long guild_id) {
		Ini ini = readIni(guild_id);
		Ini.Section rank = ini.get("Rank");
		int [] ran = new int[3];
		ran[0] = rank.get("nameLengthLimit", int.class);
		ran[1] = rank.get("generalTextFontSize", int.class);
		ran[2] = rank.get("nameTextFontSize", int.class);
		return ran;
	}
	
	public static int[] getWholeLevel(long guild_id) {
		Ini ini = readIni(guild_id);
		Ini.Section level = ini.get("Level");
		int [] lev = new int[3];
		lev[0] = level.get("nameLengthLimit", int.class);
		lev[1] = level.get("generalTextFontSize", int.class);
		lev[2] = level.get("nameTextFontSize", int.class);
		return lev;
	}
}
