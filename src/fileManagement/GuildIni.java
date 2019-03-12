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
			ini.add("General", "Theme", "");
			ini.add("General", "Administrator", "");
			ini.add("General", "CommandPrefix", "");
			ini.add("General", "CountMembers", "false");
			ini.add("General", "GameMessage", "");
			ini.add("General", "JoinMessage", "false");
			ini.add("General", "LeaveMessage", "false");
			ini.add("General", "ChannelLog", "false");
			ini.add("General", "CacheLog", "false");
			
			//Pastebin
			ini.add("Pastebin", "Key", "");
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
			ini.add("Commands", "About", "true");
			ini.add("Commands", "Commands", "true");
			ini.add("Commands", "Daily", "true");
			ini.add("Commands", "Display", "true");
			ini.add("Commands", "Help", "true");
			ini.add("Commands", "Inventory", "true");
			ini.add("Commands", "Meow", "true");
			ini.add("Commands", "Profile", "true");
			ini.add("Commands", "Pug", "true");
			ini.add("Commands", "Purchase", "true");
			ini.add("Commands", "Rank", "true");
			ini.add("Commands", "Register", "true");
			ini.add("Commands", "Set", "true");
			ini.add("Commands", "Shop", "true");
			ini.add("Commands", "Top", "true");
			ini.add("Commands", "Use", "true");
			ini.add("Commands", "User", "true");
			ini.add("Commands", "Filter", "true");
			ini.add("Commands", "Quiz", "true");
			ini.add("Commands", "RoleReaction", "true");
			ini.add("Commands", "Rss", "true");
			ini.add("Commands", "Randomshop", "true");
			
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
			ini.add("RandomshopReward", "itemSizeY", 0);
			
			ini.store(new File("./ini/"+guild_id+".ini"));
		} catch (IOException e) {
			logger.error("Error while creating guild ini file {}.ini", guild_id);
		}
	}
	
	private static Ini readIni(long guild_id) {
		try {
			return new Ini(new File("./ini/"+guild_id+".ini"));
		} catch (IOException e) {
			logger.error("Error while reading guild ini file {}.ini", guild_id);
			return null;
		}
	}
	
	public static String getTheme(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("General", "Theme");
	}
	
	public static long getAdmin(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("General", "Administrator", long.class);
	}
	
	public static String getCommandPrefix(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("General", "CommandPrefix");
	}
	
	public static boolean getCountMembers(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("General", "CountMembers", boolean.class);
	}

	public static String getGameMessage(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("General", "GameMessage");
	}
	
	public static boolean getJoinMessage(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("General", "JoinMessage", boolean.class);
	}
	
	public static boolean getLeaveMessage(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("General", "LeaveMessage", boolean.class);
	}
	
	public static boolean getChannelLog(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("General", "ChannelLog", boolean.class);
	}
	
	public static boolean getCacheLog(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("General", "CacheLog", boolean.class);
	}
	
	public static String[] getWholePastebin(long guild_id) {
		Ini ini = readIni(guild_id);
		Ini.Section pastebin = ini.get("Pastebin");
		String[] credentials = new String[3];
		credentials[0] = pastebin.get("Key");
		credentials[1] = pastebin.get("Username");
		credentials[2] = pastebin.get("Password");
		return credentials;
	}
	
	public static String getPastebinKey(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Pastebin", "Key");
	}
	
	public static String[] getReactions(long guild_id) {
		Ini ini = readIni(guild_id);
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
	
	public static boolean getAboutCommand(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "About", boolean.class);
	}
	public static boolean getCommandsCommand(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Commands", boolean.class);
	}
	public static boolean getDailyCommand(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Daily", boolean.class);
	}
	public static boolean getDisplayCommand(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Display", boolean.class);
	}
	public static boolean getHelpCommand(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Help", boolean.class);
	}
	public static boolean getInventoryCommand(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Inventory", boolean.class);
	}
	public static boolean getMeowCommand(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Meow", boolean.class);
	}
	public static boolean getProfileCommand(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Profile", boolean.class);
	}
	public static boolean getPugCommand(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Pug", boolean.class);
	}
	public static boolean getPurchaseCommand(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Purchase", boolean.class);
	}
	public static boolean getRankCommand(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Rank", boolean.class);
	}
	public static boolean getRebootCommand(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Reboot", boolean.class);
	}
	public static boolean getRegisterCommand(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Register", boolean.class);
	}
	public static boolean getSetCommand(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Set", boolean.class);
	}
	public static boolean getShopCommand(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Shop", boolean.class);
	}
	public static boolean getShutDownCommand(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "ShutDown", boolean.class);
	}
	public static boolean getTopCommand(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Top", boolean.class);
	}
	public static boolean getUseCommand(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Use", boolean.class);
	}
	public static boolean getUserCommand(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "User", boolean.class);
	}
	public static boolean getFilterCommand(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Filter", boolean.class);
	}
	public static boolean getQuizCommand(long guild_id){
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Quiz", boolean.class);
	}
	public static boolean getRoleReactionCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "RoleReaction", boolean.class);
	}
	public static boolean getRssCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Rss", boolean.class);
	}
	public static boolean getRandomshopCommand(long guild_id) {
		Ini ini = readIni(guild_id);
		return ini.get("Commands", "Randomshop", boolean.class);
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
}
