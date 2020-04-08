package preparedMessages;

import fileManagement.GuildIni;

public class CommandList {
	
	public static String getHelp(long guild_id, boolean permissionGranted) {
		StringBuilder textCollector = new StringBuilder();
		boolean administration = false;
		boolean entertainment = false;
		boolean other = false;
		
		if(permissionGranted && (GuildIni.getRegisterCommand(guild_id) || GuildIni.getSetCommand(guild_id) || GuildIni.getUserCommand(guild_id) || GuildIni.getFilterCommand(guild_id) || GuildIni.getRoleReactionCommand(guild_id) || GuildIni.getRssCommand(guild_id) || GuildIni.getRemoveCommand(guild_id) || GuildIni.getHeavyCensoringCommand(guild_id) || GuildIni.getMuteCommand(guild_id) || GuildIni.getGoogleCommand(guild_id) || GuildIni.getWriteCommand(guild_id) || GuildIni.getEditCommand(guild_id))) {
			administration = true;
		}
		
		if(GuildIni.getEquipCommand(guild_id) || GuildIni.getPugCommand(guild_id) || GuildIni.getMeowCommand(guild_id) || GuildIni.getRankCommand(guild_id) || GuildIni.getProfileCommand(guild_id) || GuildIni.getTopCommand(guild_id) || GuildIni.getUseCommand(guild_id) || GuildIni.getShopCommand(guild_id) || GuildIni.getInventoryCommand(guild_id) || GuildIni.getDailyCommand(guild_id) || GuildIni.getQuizCommand(guild_id) || GuildIni.getRandomshopCommand(guild_id) || GuildIni.getEquipCommand(guild_id)) {
			entertainment = true;
		}
		
		if(GuildIni.getAboutCommand(guild_id) || GuildIni.getHelpCommand(guild_id) || GuildIni.getDisplayCommand(guild_id) || GuildIni.getPatchnotesCommand(guild_id)) {
			other = true;
		}
		
		final String prefix = GuildIni.getCommandPrefix(guild_id);
		if(administration == true) {
			textCollector.append("**_Administration:_**\n");
			if(GuildIni.getRegisterCommand(guild_id))textCollector.append("**-"+prefix+"register**\nregister channel, role, ranking role or users with the database\n\n");
			if(GuildIni.getSetCommand(guild_id))textCollector.append("**-"+prefix+"set**\nset set specific paramater to configure your bot and server\n\n");
			if(GuildIni.getRemoveCommand(guild_id)) textCollector.append("**-"+prefix+"remove**\n remove a registered text channel, filter, role or ranking role from the database\n\n");
			if(GuildIni.getUserCommand(guild_id))textCollector.append("**-"+prefix+"user**\nchoose between various actions that you can take against a user\n\n");
			if(GuildIni.getFilterCommand(guild_id))textCollector.append("**-"+prefix+"filter**\ndecide to view, append or remove words/names from various filters or funky names\n\n");
			if(GuildIni.getRoleReactionCommand(guild_id))textCollector.append("**-"+prefix+"roleReaction**\nenable / disable the role reaction and remove roles on disable\n\n");
			if(GuildIni.getRssCommand(guild_id))textCollector.append("**-"+prefix+"rss**\ninsert rss feeds\n\n");
			if(GuildIni.getDoubleExperienceCommand(guild_id))textCollector.append("**-"+prefix+"doubleExperience**\nenable, disable or set the double experience mode to auto\n\n");
			if(GuildIni.getHeavyCensoringCommand(guild_id))textCollector.append("**-"+prefix+"heavycensoring**\nenable/disable the heavycensoring for cases when it will get hard to moderate\n\n");
			if(GuildIni.getMuteCommand(guild_id))textCollector.append("**-"+prefix+"mute**\nto mute one or multiple users on the server. Optional with reason\n\n");
			if(GuildIni.getGoogleCommand(guild_id))textCollector.append("**-"+prefix+"google**\nto configure integrations for specific actions with google services\n\n");
			if(GuildIni.getGoogleCommand(guild_id))textCollector.append("**-"+prefix+"write**\nto write a message as the bot\n\n");
			if(GuildIni.getGoogleCommand(guild_id))textCollector.append("**-"+prefix+"edit**\nto update a message of the bot\n\n");
		}
		if(entertainment == true) {
			textCollector.append("**_Entertainment:_**\n");
			if(GuildIni.getPugCommand(guild_id))textCollector.append("**-"+prefix+"pug**\nshows a pug picture. Use help as a parameter to get a list of all parameters\n\n");
			if(GuildIni.getMeowCommand(guild_id))textCollector.append("**-"+prefix+"meow**\nshows a cat picture. Use help as a parameter to get a list of all parameters\n\n");
			if(GuildIni.getRankCommand(guild_id))textCollector.append("**-"+prefix+"rank**\nshows the players actual rank\n\n");
			if(GuildIni.getProfileCommand(guild_id))textCollector.append("**-"+prefix+"profile**\nshows the players actual rank with more informations\n\n");
			if(GuildIni.getTopCommand(guild_id))textCollector.append("**-"+prefix+"top**\nshows the top 10 ranking\n\n");
			if(GuildIni.getUseCommand(guild_id))textCollector.append("**-"+prefix+"use**\nto use an item from your inventory\n\n");
			if(GuildIni.getShopCommand(guild_id))textCollector.append("**-"+prefix+"shop**\ndisplay the content in the shop and purchase\n\n");
			if(GuildIni.getInventoryCommand(guild_id))textCollector.append("**-"+prefix+"inventory**\ndisplays the content inside your inventory\n\n");
			if(GuildIni.getDailyCommand(guild_id))textCollector.append("**-"+prefix+"daily**\nto get a daily reward\n\n");
			if(GuildIni.getQuizCommand(guild_id))textCollector.append("**-"+prefix+"quiz**\nto initialize a question and answer session with rewards\n\n");
			if(GuildIni.getRandomshopCommand(guild_id))textCollector.append("**-"+prefix+"randomshop**\nto receive a random weapon from the randomshop\n\n");
			if(GuildIni.getEquipCommand(guild_id))textCollector.append("**-"+prefix+"equip**\nto equip purchased weapons and skills. To use only in private message with the ! prefix\n\n");
		}
		if(other == true) {
			textCollector.append("**_Other:_**\n");
			if(GuildIni.getAboutCommand(guild_id))textCollector.append("**-"+prefix+"about**\nshows all information regarding this Bot\n\n");
			if(GuildIni.getHelpCommand(guild_id))textCollector.append("**-"+prefix+"help**\nshows a link to our tech thread on forum\n\n");
			if(GuildIni.getDisplayCommand(guild_id))textCollector.append("**-"+prefix+"display**\nshows details of registered roles, rank level, channel filter, etc. (few parameters may be restricted)\n\n");
			if(GuildIni.getPatchnotesCommand(guild_id))textCollector.append("**-"+prefix+"patchnotes**\nshows past published patchnotes of the game and game\n\n");
		}
		
		if(administration == false && entertainment == false && other == false){
			textCollector.append("All commands are disabled");
		}
		
		
		return textCollector.toString();
	}
}
