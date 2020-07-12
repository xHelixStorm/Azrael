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
		var admin = STATIC.getAdmin();
		if(admin > 0) {
			return admin;
		}
		else {
			Ini ini = readConfig();
			admin = ini.get("Bot", "Admin", long.class);
			STATIC.setAdmin(admin);
			return admin;
		}
	}
	public static String getTimezone() {
		var timezone = STATIC.getTimezone();
		if(timezone.length() > 0) {
			return timezone;
		}
		else {
			Ini ini = readConfig();
			timezone = ini.get("Bot", "Timezone");
			if(timezone.length() == 0)
				timezone = "UTC";
			STATIC.setTimezone(timezone);
			return timezone;
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
		var fileLogger = STATIC.getFileLogger();
		if(fileLogger.equals("true"))
			return true;
		else if(fileLogger.equals("false"))
			return false;
		else {
			Ini ini = readConfig();
			return ini.get("Bot", "FileLogger", boolean.class);
		}
	}
	public static String getTempDirectory() {
		var temp = STATIC.getTemp();
		if(temp.length() > 0) 
			return temp;
		else {
			Ini ini = readConfig();
			temp = ini.get("Bot", "TempDirectory");
			STATIC.setTemp(temp);
			return temp;
		}
	}
	
	public static boolean getLinuxScreen() {
		Ini ini = readConfig();
		return ini.get("Bot", "LinuxScreen", boolean.class);
	}
	public static int getWebserverPort() {
		Ini ini = readConfig();
		return ini.get("Bot", "WebserverPort", int.class);
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
		if(STATIC.getDoubleExperienceStart().length() > 0)
			return Enum.valueOf(Weekday.class, STATIC.getDoubleExperienceStart());
		else {
			Ini ini = readConfig();
			return ini.get("DoubleExperience", "Start", Weekday.class);
		}
	}
	
	public static Weekday getDoubleExpEnd() {
		if(STATIC.getDoubleExperienceEnd().length() > 0)
			return Enum.valueOf(Weekday.class, STATIC.getDoubleExperienceEnd());
		else {
			Ini ini = readConfig();
			return ini.get("DoubleExperience", "End", Weekday.class);
		}
	}
	public static String getSQLIP() {
		Ini ini = readConfig();
		return ini.get("Azrael", "IP");
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
	public static String getSQLIP2() {
		Ini ini = readConfig();
		return ini.get("RankingSystem", "IP");
	}
	public static String getSQLUsername2() {
		Ini ini = readConfig();
		return ini.get("RankingSystem", "Username");
	}
	public static String getSQLPassword2() {
		Ini ini = readConfig();
		return ini.get("RankingSystem", "Password");
	}
	public static String getSQLIP3() {
		Ini ini = readConfig();
		return ini.get("DiscordRoles", "IP");
	}
	public static String getSQLUsername3() {
		Ini ini = readConfig();
		return ini.get("DiscordRoles", "Username");
	}
	public static String getSQLPassword3() {
		Ini ini = readConfig();
		return ini.get("DiscordRoles", "Password");
	}
	public static String getSQLIP4() {
		Ini ini = readConfig();
		return ini.get("Patchnotes", "IP");
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
	public static String getCaughtThumbnail() {
		Ini ini = readConfig();
		return ini.get("Thumbnails", "Caught");
	}
	public static String getFalseAlarmThumbnail() {
		Ini ini = readConfig();
		return ini.get("Thumbnails", "FalseAlarm");
	}
	public static String getHeavyThumbnail() {
		Ini ini = readConfig();
		return ini.get("Thumbnails", "Heavy");
	}
	public static String getHeavyEndThumbnail() {
		Ini ini = readConfig();
		return ini.get("Thumbnails", "HeavyEnd");
	}
}
