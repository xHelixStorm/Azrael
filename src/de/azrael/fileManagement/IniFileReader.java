package de.azrael.fileManagement;

import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.enums.Weekday;
import de.azrael.util.STATIC;

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
	public static String getWebURL() {
		Ini ini = readConfig();
		return ini.get("Bot", "WebUrl");
	}
	public static String getAESSecret() {
		Ini ini = readConfig();
		return ini.get("Bot", "AESSecret");
	}
	public static int getDelayedGoogleRequestTime() {
		Ini ini = readConfig();
		return ini.get("Bot", "DelayedGoogleRequestTime", int.class);
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
