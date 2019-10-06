package fileManagement;

import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import enums.Weekday;
import util.STATIC;

public class IniFileReader {
	private final static Logger logger = LoggerFactory.getLogger(IniFileReader.class);
	
	private static Ini readConfig() {
		try {
			return new Ini(new File("config.ini"));
		} catch (IOException e) {
			logger.error("Config file couldn't be found or couldn't be opened", e);
			return null;
		}
	}
	
	public static String getToken() {
		Ini ini = readConfig();
		return ini.get("Bot", "Token");
	}
	public static long getAdmin() {
		if(STATIC.getAdmin() != 0) {
			return STATIC.getAdmin();
		}
		else {
			Ini ini = readConfig();
			return ini.get("Bot", "Admin", long.class);
		}
	}
	public static boolean getCountMembers() {
		var countMembers = STATIC.getCountMembers();
		if(countMembers.equals("true"))
			return true;
		else if(countMembers.equals("false"))
			return false;
		else {
			Ini ini = readConfig();
			return ini.get("Bot", "CountMembers", boolean.class);
		}
	}
	public static String getGameMessage() {
		var gameMessage = STATIC.getGameMessage();
		if(gameMessage.length() == 1 && gameMessage.equals("%"))
			return "";
		else if(gameMessage.length() > 0)
			return gameMessage;
		else {
			Ini ini = readConfig();
			return ini.get("Bot", "GameMessage");
		}
	}
	public static boolean getActionLog() {
		var actionLog = STATIC.getActionLog();
		if(actionLog.equals("true"))
			return true;
		else if(actionLog.equals("false"))
			return false;
		else {
			Ini ini = readConfig();
			return ini.get("Bot", "ActionLog", boolean.class);
		}
	}
	public static boolean getFileLogger() {
		Ini ini = readConfig();
		return ini.get("Bot", "FileLogger", boolean.class);
	}
	public static String getTempDirectory() {
		Ini ini = readConfig();
		return ini.get("Bot", "TempDirectory");
	}
	
	public static String getPastebinDeveloperKey() {
		Ini ini = readConfig();
		return ini.get("Pastebin", "DeveloperKey");
	}
	
	public static String[] getTwitterKeys() {
		Ini ini = readConfig();
		Ini.Section tokens = ini.get("Twitter");
		String []twitter = new String[4];
		twitter[0] = tokens.get("ConsumerKey");
		twitter[1] = tokens.get("ConsumerKeySecret");
		twitter[2] = tokens.get("AccessToken");
		twitter[3] = tokens.get("AccessTokenSecret");
		return twitter;
	}
	
	public static boolean getDoubleExpEnabled() {
		var doubleExperience = STATIC.getDoubleExperience();
		if(doubleExperience.equals("true"))
			return true;
		else if(doubleExperience.equals("false"))
			return false;
		else {
			Ini ini = readConfig();
			return ini.get("DoubleExperience", "Enabled", boolean.class);
		}
	}
	
	public static Weekday getDoubleExpStart() {
		Ini ini = readConfig();
		return ini.get("DoubleExperience", "Start", Weekday.class);
	}
	
	public static Weekday getDoubleExpEnd() {
		Ini ini = readConfig();
		return ini.get("DoubleExperience", "End", Weekday.class);
	}
	public static String getSQLUsername() {
		Ini ini = readConfig();
		return ini.get("Azrael", "Username");
	}
	public static String getSQLPassword() {
		Ini ini = readConfig();
		return ini.get("Azrael", "Password");
	}
	public static long getMessageTimeout() {
		Ini ini = readConfig();
		return ini.get("RankingSystem", "MessageTimeout", long.class);
	}
	public static String getSQLUsername2() {
		Ini ini = readConfig();
		return ini.get("RankingSystem", "Username");
	}
	public static String getSQLPassword2() {
		Ini ini = readConfig();
		return ini.get("RankingSystem", "Password");
	}
	public static String getSQLUsername3() {
		Ini ini = readConfig();
		return ini.get("DiscordRoles", "Username");
	}
	public static String getSQLPassword3() {
		Ini ini = readConfig();
		return ini.get("DiscordRoles", "Password");
	}
	public static String getSQLUsername4() {
		Ini ini = readConfig();
		return ini.get("Patchnotes", "Username");
	}
	public static String getSQLPassword4() {
		Ini ini = readConfig();
		return ini.get("Patchnotes", "Password");
	}
	public static String getPugThumbnail() {
		Ini ini = readConfig();
		return ini.get("Thumbnails", "Pug");
	}
	public static String getMeowThumbnail() {
		Ini ini = readConfig();
		return ini.get("Thumbnails", "Meow");
	}
	public static String getBanThumbnail() {
		Ini ini = readConfig();
		return ini.get("Thumbnails", "Ban");
	}
	public static String getSettingsThumbnail() {
		Ini ini = readConfig();
		return ini.get("Thumbnails", "Settings");
	}
	public static String getShopThumbnail() {
		Ini ini = readConfig();
		return ini.get("Thumbnails", "Shop");
	}
	public static String getDeniedThumbnail() {
		Ini ini = readConfig();
		return ini.get("Thumbnails", "Denied");
	}
	public static String getLeaveThumbnail() {
		Ini ini = readConfig();
		return ini.get("Thumbnails", "Leave");
	}
	public static String getUnbanThumbnail() {
		Ini ini = readConfig();
		return ini.get("Thumbnails", "Unban");
	}
	public static String getUnmuteThumbnail() {
		Ini ini = readConfig();
		return ini.get("Thumbnails", "Unmute");
	}
	public static String getKickThumbnail() {
		Ini ini = readConfig();
		return ini.get("Thumbnails", "Kick");
	}
	public static String getCatchedThumbnail() {
		Ini ini = readConfig();
		return ini.get("Thumbnails", "Catched");
	}
	public static String getFalseAlarmThumbnail() {
		Ini ini = readConfig();
		return ini.get("Thumbnails", "FalseAlarm");
	}
}
