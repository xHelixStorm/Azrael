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
}
