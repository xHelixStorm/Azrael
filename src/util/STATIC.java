package util;

import fileManagement.FileSetting;

public class STATIC {
	
	private static final String VERSION_OLD = FileSetting.readFile("./files/version.azr");
	private static final String VERSION_NEW = "12.0.1";
	
	public static String getVersion_Old(){
		return VERSION_OLD;
	}
	public static String getVersion_New(){
		return VERSION_NEW;
	}
}
