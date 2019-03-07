package preparedMessages;

import java.util.ArrayList;

import fileManagement.GuildIni;
import util.STATIC;

public class PatchNotes {
	public static StringBuilder readMessage = new StringBuilder();
	public static ArrayList <String> textCollector = new ArrayList<>();
	
	public static String patchNotes(long guild_id){
		final String prefix = GuildIni.getCommandPrefix(guild_id);
		
		textCollector.add("Bot Patchnotes version "+ STATIC.getVersion_New() + " 26.07.2018\n");
		textCollector.add("Hello and welcome to today's changes. There will be a lot about moderation.\n\n");
		textCollector.add("**Administration**:\n");
		textCollector.add("-Added AuditLog support. Now the bot can add a name and reason to the one being kicked or banned\n");
		textCollector.add("-New command called "+prefix+"user added. Display a full information list about a user or directly decide to ban or kick with a reason and more\n");
		textCollector.add("-Added Thumbnails almost everywhere that can be changed anytime. For example during the mute, kick, ban and unmute\n");
		textCollector.add("-New command called "+prefix+"filter added. Display the current filter, add new words or delete them anytime while the bot is running\n");
		textCollector.add("-Added Pastebin support with the bot. All pastes will be uploaded unlisted and get automatically deleted within 24 hours\n");
		textCollector.add("-All written messages get logged into a file named by the text-channel. It can be disabled on the ini file.\n");
		textCollector.add("-All written messages gets saved into the bot cache to display if a mod or admin removes certain messages into the trash channel. It can be disabled and works only if the message log is enabled\n");
		textCollector.add("-You can now mute a player with the user command with a self chosen timer. Doing so won't increase the total warning counter, except if it's the first time the user gets muted\n");
		textCollector.add("-You can now decide how many warnings are allowed on the server before a user gets banned upon receiving the mute role. Available under the set -warnings command\n\n");
		textCollector.add("**Entertainment**:\n");
		textCollector.add("-The profile and rank command can now display the details from other users\n\n");
		textCollector.add("**Other**\n");
		textCollector.add("-The source code of this bot is now on GitHub. check it out! https://github.com/xHelixStorm/Azrael\n");
		
		
		for(String text : textCollector){
			 readMessage.append(text);
		}
		String message = readMessage.toString();
		return message;
	}
}
