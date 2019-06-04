package preparedMessages;

import java.util.ArrayList;

import fileManagement.GuildIni;

public class CommandList {
	
	public static String getHelp(long guild_id, boolean permissionGranted){
		StringBuilder readMessage = new StringBuilder();
		ArrayList <String> textCollector = new ArrayList<>();
		boolean administration = false;
		boolean entertainment = false;
		boolean other = false;
		
		if(permissionGranted && (GuildIni.getRegisterCommand(guild_id) || GuildIni.getSetCommand(guild_id) || GuildIni.getUserCommand(guild_id) || GuildIni.getFilterCommand(guild_id) || GuildIni.getRoleReactionCommand(guild_id) || GuildIni.getRssCommand(guild_id))){
			administration = true;
		}
		
		if(GuildIni.getPugCommand(guild_id) || GuildIni.getMeowCommand(guild_id) || GuildIni.getRankCommand(guild_id) || GuildIni.getProfileCommand(guild_id) || GuildIni.getTopCommand(guild_id) || GuildIni.getUseCommand(guild_id) || GuildIni.getShopCommand(guild_id) || GuildIni.getInventoryCommand(guild_id) || GuildIni.getPurchaseCommand(guild_id) || GuildIni.getDailyCommand(guild_id) || GuildIni.getQuizCommand(guild_id) || GuildIni.getRandomshopCommand(guild_id)){
			entertainment = true;
		}
		
		if(GuildIni.getAboutCommand(guild_id) || GuildIni.getHelpCommand(guild_id) || GuildIni.getDisplayCommand(guild_id) || GuildIni.getPatchnotesCommand(guild_id)){
			other = true;
		}
		
		final String prefix = GuildIni.getCommandPrefix(guild_id);
		if(administration == true) {
			textCollector.add("**_Administration:_**\n");
			if(GuildIni.getRegisterCommand(guild_id))textCollector.add("**-"+prefix+"register**\nregister channel, role, ranking role or users with the database\n\n");
			if(GuildIni.getSetCommand(guild_id))textCollector.add("**-"+prefix+"set**\nset set specific paramater to configure your bot and server\n\n");
			if(GuildIni.getUserCommand(guild_id))textCollector.add("**-"+prefix+"user**\nchoose between various actions that you can take against a user\n\n");
			if(GuildIni.getFilterCommand(guild_id))textCollector.add("**-"+prefix+"filter**\ndecide to view, add or remove words/names from various filters or funky names\n\n");
			if(GuildIni.getRoleReactionCommand(guild_id))textCollector.add("**-"+prefix+"roleReaction**\nenable / disable the role reaction and remove roles on disable\n\n");
			if(GuildIni.getRssCommand(guild_id))textCollector.add("**-"+prefix+"rss**\ninsert rss feeds \n\n");
			if(GuildIni.getDoubleExperienceCommand(guild_id))textCollector.add("**-"+prefix+"doubleExperience**\nenable, disable or set the double experience mode to auto \n\n");
		}
		if(entertainment == true) {
			textCollector.add("**_Entertainment:_**\n");
			if(GuildIni.getPugCommand(guild_id))textCollector.add("**-"+prefix+"pug**\nshows a pug picture. Use help as a parameter to get a list of all parameters\n\n");
			if(GuildIni.getMeowCommand(guild_id))textCollector.add("**-"+prefix+"meow**\nshows a cat picture. Use help as a parameter to get a list of all parameters\n\n");
			if(GuildIni.getRankCommand(guild_id))textCollector.add("**-"+prefix+"rank**\nshows the players actual rank\n\n");
			if(GuildIni.getProfileCommand(guild_id))textCollector.add("**-"+prefix+"profile**\nshows the players actual rank with more informations\n\n");
			if(GuildIni.getTopCommand(guild_id))textCollector.add("**-"+prefix+"top**\nshows the top 10 ranking\n\n");
			if(GuildIni.getUseCommand(guild_id))textCollector.add("**-"+prefix+"use**\nto use an item from your inventory\n\n");
			if(GuildIni.getShopCommand(guild_id))textCollector.add("**-"+prefix+"shop**\ndisplay the content in the shop\n\n");
			if(GuildIni.getInventoryCommand(guild_id))textCollector.add("**-"+prefix+"inventory**\ndisplays the content inside your inventory\n\n");
			if(GuildIni.getPurchaseCommand(guild_id))textCollector.add("**-"+prefix+"purchase**\nto purchase and item or skin from the shop\n\n");
			if(GuildIni.getDailyCommand(guild_id))textCollector.add("**-"+prefix+"daily**\nto get a daily reward\n\n");
			if(GuildIni.getQuizCommand(guild_id))textCollector.add("**-"+prefix+"quiz**\nto initialize a question and answer session with rewards\n\n");
			if(GuildIni.getRandomshopCommand(guild_id))textCollector.add("**-"+prefix+"randomshop**\nto receive a random weapon from the randomshop\n\n");
		}
		if(other == true) {
			textCollector.add("**_Other:_**\n");
			if(GuildIni.getAboutCommand(guild_id))textCollector.add("**-"+prefix+"about**\nshows all information regarding this Bot\n\n");
			if(GuildIni.getHelpCommand(guild_id))textCollector.add("**-"+prefix+"help**\nshows a link to our tech thread on forum\n\n");
			if(GuildIni.getDisplayCommand(guild_id))textCollector.add("**-"+prefix+"display**\nshows details of registered roles, rank level, channel filter, etc. (few parameters may be restricted)\n\n");
			if(GuildIni.getPatchnotesCommand(guild_id))textCollector.add("**-"+prefix+"patchnotes**\nshows past published patchnotes of the game and game\n\n");
		}
		
		if(administration == false && entertainment == false && other == false){
			textCollector.add("All commands are disabled");
		}
		
		for(String text : textCollector){
			 readMessage.append(text);
		}
		String message = readMessage.toString();
		return message;
	}
}
