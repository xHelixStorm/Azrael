package de.azrael.enums;

public enum Directory {
	TEMP 			(null, false),
	LOG 			("./log/"),
	MESSAGE_LOG 	("./message_log/"),
	USER_LOG 		("./user_log/", true),
	CACHE 			("./cache/", true),
	CAT 			("./files/Cat/"),
	GOOGLE 			("./files/Google/"),
	LANGUAGES 		("./files/Languages/"),
	PUG 			("./files/Pug/"),
	BANNERS 		("./files/RankingSystem/Banners/"),
	DAILIES 		("./files/RankingSystem/Dailies/"),
	EXPERIENCEBAR 	("./files/RankingSystem/ExperienceBar/"),
	INVENTORY 		("./files/RankingSystem/Inventory/"),
	ITEMS 			("./files/RankingSystem/Items/"),
	RANDOMSHOP 		("./files/RankingSystem/Randomshop/"),
	RANK 			("./files/RankingSystem/Rank/"),
	SKILLS 			("./files/RankingSystem/Skills/"),
	SKINS 			("./files/RankingSystem/Skins/"),
	WEAPONS 		("./files/RankingSystem/Weapons/");
	
	private String path;
	private boolean encryptionEnabled = false;
	
	private Directory(String _path) {
		this.path = _path;
	}
	
	private Directory(String _path, boolean _encryptionEnabled) {
		this.path = _path;
		this.encryptionEnabled = _encryptionEnabled;
	}
	
	public String getPath() {
		if(!this.equals(TEMP))
			return path;
		else
			return System.getProperty("TEMP_DIRECTORY");
	}
	
	public boolean isEncryptionEnabled() {
		return encryptionEnabled;
	}
}
