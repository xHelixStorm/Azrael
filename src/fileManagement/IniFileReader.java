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
	public static boolean getNameFilter(){
		readConfig();
		return ini.get("Bot", "NameFilter", boolean.class);
	}
	public static boolean getActionLog(){
		readConfig();
		return ini.get("Bot", "ActionLog", boolean.class);
	}
	public static boolean getFileLogger() {
		readConfig();
		return ini.get("Bot", "FileLogger", boolean.class);
	}
	public static String getCommandPrefix(){
		readConfig();
		return ini.get("Bot", "CommandPrefix");
	}
	public static String getTempDirectory(){
		readConfig();
		return ini.get("Bot", "TempDirectory");
	}
	public static boolean getCountMembers() {
		readConfig();
		return ini.get("Bot", "CountMembers", boolean.class);
	}
	public static String getGameMessage(){
		readConfig();
		return ini.get("Bot", "GameMessage");
	}
	public static boolean getAllowPatchNotes(){
		readConfig();
		return ini.get("Patch", "PrivatePatchNotes", boolean.class);
	}
	public static boolean getAllowPublicPatchNotes(){
		readConfig();
		return ini.get("Patch", "PublicPatchNotes", boolean.class);
	}
	public static boolean getJoinMessage(){
		readConfig();
		return ini.get("Messages", "JoinMessage", boolean.class);
	}
	public static boolean getLeaveMessage(){
		readConfig();
		return ini.get("Messages", "LeaveMessage", boolean.class);
	}
	public static boolean getChannelLog(){
		readConfig();
		return ini.get("Messages", "ChannelLog", boolean.class);
	}
	public static boolean getCacheLog(){
		readConfig();
		return ini.get("Messages", "CacheLog", boolean.class);
	}
	public static String getPastebinKey() {
		readConfig();
		return ini.get("Pastebin", "Key");
	}
	public static String getPastebinUsername() {
		readConfig();
		return ini.get("Pastebin", "Username");
	}
	public static String getPastebinPassword() {
		readConfig();
		return ini.get("Pastebin", "Password");
	}
	public static String getSQLUsername(){
		readConfig();
		return ini.get("Azrael", "Username");
	}
	public static String getSQLPassword(){
		readConfig();
		return ini.get("Azrael", "Password");
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
	public static String[] getReactions() {
		readConfig();
		String[] reaction = new String[10];
		reaction[0] = ini.get("Reactions", "Enabled");
		reaction[1] = ini.get("Reactions", "Emoji1");
		reaction[2] = ini.get("Reactions", "Emoji2");
		reaction[3] = ini.get("Reactions", "Emoji3");
		reaction[4] = ini.get("Reactions", "Emoji4");
		reaction[5] = ini.get("Reactions", "Emoji5");
		reaction[6] = ini.get("Reactions", "Emoji6");
		reaction[7] = ini.get("Reactions", "Emoji7");
		reaction[8] = ini.get("Reactions", "Emoji8");
		reaction[9] = ini.get("Reactions", "Emoji9");
		return reaction;
	}
	public static boolean getAboutCommand(){
		readConfig();
		return ini.get("Commands", "About", boolean.class);
	}
	public static boolean getCommandsCommand(){
		readConfig();
		return ini.get("Commands", "Commands", boolean.class);
	}
	public static boolean getDailyCommand(){
		readConfig();
		return ini.get("Commands", "Daily", boolean.class);
	}
	public static boolean getDisplayCommand(){
		readConfig();
		return ini.get("Commands", "Display", boolean.class);
	}
	public static boolean getHelpCommand(){
		readConfig();
		return ini.get("Commands", "Help", boolean.class);
	}
	public static boolean getInventoryCommand(){
		readConfig();
		return ini.get("Commands", "Inventory", boolean.class);
	}
	public static boolean getMeowCommand(){
		readConfig();
		return ini.get("Commands", "Meow", boolean.class);
	}
	public static boolean getProfileCommand(){
		readConfig();
		return ini.get("Commands", "Profile", boolean.class);
	}
	public static boolean getPugCommand(){
		readConfig();
		return ini.get("Commands", "Pug", boolean.class);
	}
	public static boolean getPurchaseCommand(){
		readConfig();
		return ini.get("Commands", "Purchase", boolean.class);
	}
	public static boolean getRankCommand(){
		readConfig();
		return ini.get("Commands", "Rank", boolean.class);
	}
	public static boolean getRebootCommand(){
		readConfig();
		return ini.get("Commands", "Reboot", boolean.class);
	}
	public static boolean getRegisterCommand(){
		readConfig();
		return ini.get("Commands", "Register", boolean.class);
	}
	public static boolean getSetCommand(){
		readConfig();
		return ini.get("Commands", "Set", boolean.class);
	}
	public static boolean getShopCommand(){
		readConfig();
		return ini.get("Commands", "Shop", boolean.class);
	}
	public static boolean getShutDownCommand(){
		readConfig();
		return ini.get("Commands", "ShutDown", boolean.class);
	}
	public static boolean getTopCommand(){
		readConfig();
		return ini.get("Commands", "Top", boolean.class);
	}
	public static boolean getUseCommand(){
		readConfig();
		return ini.get("Commands", "Use", boolean.class);
	}
	public static boolean getUserCommand(){
		readConfig();
		return ini.get("Commands", "User", boolean.class);
	}
	public static boolean getFilterCommand(){
		readConfig();
		return ini.get("Commands", "Filter", boolean.class);
	}
	public static boolean getQuizCommand(){
		readConfig();
		return ini.get("Commands", "Quiz", boolean.class);
	}
	public static boolean getRoleReactionCommand() {
		readConfig();
		return ini.get("Commands", "RoleReaction", boolean.class);
	}
	public static boolean getRssCommand() {
		readConfig();
		return ini.get("Commands", "Rss", boolean.class);
	}
}
