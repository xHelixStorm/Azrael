package fileManagement;

import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IniFileReader {
	
	private static Ini ini;
	
	private static void readConfig(){
		try {
			ini = new Ini(new File("./config.ini"));
		} catch (IOException e) {
			Logger logger = LoggerFactory.getLogger(IniFileReader.class);
			logger.error("Config file couldn't be found or couldn't be opened", e);
		}
	}
	
	public static String getToken(){
		readConfig();
		return ini.get("Bot", "Token");
	}
	public static long getAdmin(){
		readConfig();
		return ini.get("Bot", "Admin", long.class);
	}
	public static boolean getCountMembers() {
		readConfig();
		return ini.get("Bot", "CountMembers", boolean.class);
	}
	public static String getGameMessage(){
		readConfig();
		return ini.get("Bot", "GameMessage");
	}
	public static boolean getActionLog(){
		readConfig();
		return ini.get("Bot", "ActionLog", boolean.class);
	}
	public static boolean getFileLogger() {
		readConfig();
		return ini.get("Bot", "FileLogger", boolean.class);
	}
	public static String getTempDirectory(){
		readConfig();
		return ini.get("Bot", "TempDirectory");
	}
	public static boolean getAllowPatchNotes(){
		readConfig();
		return ini.get("Patch", "PrivatePatchNotes", boolean.class);
	}
	public static boolean getAllowPublicPatchNotes(){
		readConfig();
		return ini.get("Patch", "PublicPatchNotes", boolean.class);
	}
	public static String getSQLUsername(){
		readConfig();
		return ini.get("Azrael", "Username");
	}
	public static String getSQLPassword(){
		readConfig();
		return ini.get("Azrael", "Password");
	}
	public static long getMessageTimeout(){
		readConfig();
		return ini.get("RankingSystem", "MessageTimeout", long.class);
	}
	public static String getSQLUsername2(){
		readConfig();
		return ini.get("RankingSystem", "Username");
	}
	public static String getSQLPassword2(){
		readConfig();
		return ini.get("RankingSystem", "Password");
	}
	public static String getSQLUsername3(){
		readConfig();
		return ini.get("DiscordRoles", "Username");
	}
	public static String getSQLPassword3(){
		readConfig();
		return ini.get("DiscordRoles", "Password");
	}
	public static String getPugThumbnail() {
		readConfig();
		return ini.get("Thumbnails", "Pug");
	}
	public static String getMeowThumbnail() {
		readConfig();
		return ini.get("Thumbnails", "Meow");
	}
	public static String getBanThumbnail() {
		readConfig();
		return ini.get("Thumbnails", "Ban");
	}
	public static String getSettingsThumbnail() {
		readConfig();
		return ini.get("Thumbnails", "Settings");
	}
	public static String getShopThumbnail() {
		readConfig();
		return ini.get("Thumbnails", "Shop");
	}
	public static String getDeniedThumbnail() {
		readConfig();
		return ini.get("Thumbnails", "Denied");
	}
	public static String getLeaveThumbnail() {
		readConfig();
		return ini.get("Thumbnails", "Leave");
	}
	public static String getUnbanThumbnail() {
		readConfig();
		return ini.get("Thumbnails", "Unban");
	}
	public static String getUnmuteThumbnail() {
		readConfig();
		return ini.get("Thumbnails", "Unmute");
	}
	public static String getKickThumbnail() {
		readConfig();
		return ini.get("Thumbnails", "Kick");
	}
	public static String getCatchedThumbnail() {
		readConfig();
		return ini.get("Thumbnails", "Catched");
	}
	public static String getFalseAlarmThumbnail() {
		readConfig();
		return ini.get("Thumbnails", "FalseAlarm");
	}
}
