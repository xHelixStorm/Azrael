package fileManagement;

import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;

public class IniFileReader {
	
	private static Ini ini;
	
	private static void readConfig(){
		try {
			ini = new Ini(new File("./config.ini"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getToken(){
		readConfig();
		return ini.get("Bot", "Token");
	}
	public static String getAdmin(){
		readConfig();
		return ini.get("Bot", "Admin");
	}
	public static String getCommandPrefix(){
		readConfig();
		return ini.get("Bot", "CommandPrefix");
	}
	public static String getTempDirectory(){
		readConfig();
		return ini.get("Bot", "TempDirectory");
	}
	public static String getGameMessage(){
		readConfig();
		return ini.get("Bot", "GameMessage");
	}
	public static String getAllowPatchNotes(){
		readConfig();
		return ini.get("Patch", "PrivatePatchNotes");
	}
	public static String getAllowPublicPatchNotes(){
		readConfig();
		return ini.get("Patch", "PublicPatchNotes");
	}
	public static String getJoinMessage(){
		readConfig();
		return ini.get("Messages", "JoinMessage");
	}
	public static String getLeaveMessage(){
		readConfig();
		return ini.get("Messages", "LeaveMessage");
	}
	public static String getChannelLog(){
		readConfig();
		return ini.get("Messages", "ChannelLog");
	}
	public static String getCacheLog(){
		readConfig();
		return ini.get("Messages", "CacheLog");
	}
	public static String getSQLUsername(){
		readConfig();
		return ini.get("SqlConnect", "username");
	}
	public static String getSQLPassword(){
		readConfig();
		return ini.get("SqlConnect", "password");
	}
	public static String getSQLUsername2(){
		readConfig();
		return ini.get("RankingDB", "username");
	}
	public static String getSQLPassword2(){
		readConfig();
		return ini.get("RankingDB", "password");
	}
	public static String getSQLUsername3(){
		readConfig();
		return ini.get("DiscordRoles", "username");
	}
	public static String getSQLPassword3(){
		readConfig();
		return ini.get("DiscordRoles", "password");
	}
	public static String getPugThumbnail() {
		readConfig();
		return ini.get("Thumbnails", "Pug");
	}
	public static String getMeowThumbnail() {
		readConfig();
		return ini.get("Thumbnails", "Meow");
	}
	public static String getMuteThumbnail() {
		readConfig();
		return ini.get("Thumbnails", "Mute");
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
	public static String getAboutCommand(){
		readConfig();
		return ini.get("Commands", "About");
	}
	public static String getCommandsCommand(){
		readConfig();
		return ini.get("Commands", "Commands");
	}
	public static String getDailyCommand(){
		readConfig();
		return ini.get("Commands", "Daily");
	}
	public static String getDisplayCommand(){
		readConfig();
		return ini.get("Commands", "Display");
	}
	public static String getHelpCommand(){
		readConfig();
		return ini.get("Commands", "Help");
	}
	public static String getInventoryCommand(){
		readConfig();
		return ini.get("Commands", "Inventory");
	}
	public static String getMeowCommand(){
		readConfig();
		return ini.get("Commands", "Meow");
	}
	public static String getProfileCommand(){
		readConfig();
		return ini.get("Commands", "Profile");
	}
	public static String getPugCommand(){
		readConfig();
		return ini.get("Commands", "Pug");
	}
	public static String getPurchaseCommand(){
		readConfig();
		return ini.get("Commands", "Purchase");
	}
	public static String getRankCommand(){
		readConfig();
		return ini.get("Commands", "Rank");
	}
	public static String getRebootCommand(){
		readConfig();
		return ini.get("Commands", "Reboot");
	}
	public static String getRegisterCommand(){
		readConfig();
		return ini.get("Commands", "Register");
	}
	public static String getSetCommand(){
		readConfig();
		return ini.get("Commands", "Set");
	}
	public static String getShopCommand(){
		readConfig();
		return ini.get("Commands", "Shop");
	}
	public static String getShutDownCommand(){
		readConfig();
		return ini.get("Commands", "ShutDown");
	}
	public static String getTopCommand(){
		readConfig();
		return ini.get("Commands", "Top");
	}
	public static String getUseCommand(){
		readConfig();
		return ini.get("Commands", "Use");
	}
	public static String getUserCommand(){
		readConfig();
		return ini.get("Commands", "User");
	}
}
