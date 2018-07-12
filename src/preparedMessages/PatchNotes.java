package preparedMessages;

import java.util.ArrayList;

import fileManagement.IniFileReader;
import util.STATIC;

public class PatchNotes {
	public static StringBuilder readMessage = new StringBuilder();
	public static ArrayList <String> textCollector = new ArrayList<>();
	
	public static String patchNotes(){
		
		textCollector.add("Bot Patchnotes version "+ STATIC.getVersion_New() + " 14.03.2018\n");
		textCollector.add("Today's patch will bring quite some changes. Hold on tight.\n\n");
		textCollector.add("**Discord roles management**\n");
		textCollector.add("-reworked command to register and to clear roles\n");
		textCollector.add("-rebuild the databse into the third normal form\n");
		textCollector.add("-role registrations work now with abbreviations instead of numbers (adm, mod, mut, etc..)\n");
		textCollector.add("-more options have been added to the command list\n\n");
		textCollector.add("**Mute and Unmute system**\n");
		textCollector.add("-simplified the code structure to get rid of bugs \n");
		textCollector.add("-the mute effect kicks in now faster (almost instantly, you'll be surprised)\n");
		textCollector.add("-solved the bug where players get muted again once they rejoin and the ranking system is enabled on unmute\n");
		textCollector.add("-solved a bug where the user bypasses the mute by rejoining\n\n");
		textCollector.add("-solved the bug where the user doesn't get unmuted\n");
		textCollector.add("coming next: command to display the warning status of a user\n\n");
		textCollector.add("**Ranking System**\n");
		textCollector.add("-reworked code and database regarding the ranking system\n");
		textCollector.add("-reworked command to register roles with unlock level in one command while not having it hard coded\n");
		textCollector.add("coming next: command to assign level and experience and to see the profile from other users\n\n");
		textCollector.add("**Filter System**\n");
		textCollector.add("-reworked channel registration command to assign filters to a channel with abbreviations instead of numbers\n");
		textCollector.add("coming next: From registering channels aside, soon it should be possible to assign one or more filters on a channel\n\n");
		textCollector.add("**General changes**\n");
		textCollector.add("-**"+IniFileReader.getCommandPrefix()+"commands** is now displaying all available commands\n");
		textCollector.add("-overall performance due to code and database simplification\n");
		textCollector.add("-put multiple commands together in one single command. Check **"+IniFileReader.getCommandPrefix()+"commands**\n");
		textCollector.add("-renamed **"+IniFileReader.getCommandPrefix()+"displayRoles** to **"+IniFileReader.getCommandPrefix()+"display with parameters to use**\n");
		textCollector.add("-coming next: random shop mini game with inventory in S4 style and channel logs combined with pastebin\n");
		
		for(String text : textCollector){
			 readMessage.append(text);
		}
		String message = readMessage.toString();
		return message;
	}
}
