package preparedMessages;

import java.util.ArrayList;

import fileManagement.IniFileReader;

public class HelpText {
	public static StringBuilder readMessage = new StringBuilder();
	public static ArrayList <String> textCollector = new ArrayList<>();
	
	public static String getHelp(){
		boolean administration = false;
		boolean entertainment = false;
		boolean other = false;
		
		if(IniFileReader.getShutDownCommand() || IniFileReader.getRebootCommand() || IniFileReader.getRegisterCommand() || IniFileReader.getSetCommand() || IniFileReader.getUserCommand() || IniFileReader.getFilterCommand() || IniFileReader.getRoleReactionCommand() || IniFileReader.getRssCommand()){
			administration = true;
		}
		
		if(IniFileReader.getPugCommand() || IniFileReader.getMeowCommand() || IniFileReader.getRankCommand() || IniFileReader.getProfileCommand() || IniFileReader.getTopCommand() || IniFileReader.getUseCommand() || IniFileReader.getShopCommand() || IniFileReader.getInventoryCommand() || IniFileReader.getPurchaseCommand() || IniFileReader.getDailyCommand() || IniFileReader.getQuizCommand() || IniFileReader.getRandomshopCommand()){
			entertainment = true;
		}
		
		if(IniFileReader.getAboutCommand() || IniFileReader.getHelpCommand() || IniFileReader.getDisplayCommand()){
			other = true;
		}
		
		if(administration == true)textCollector.add("**_Administration:_**\n");
		if(IniFileReader.getShutDownCommand())textCollector.add("**-"+IniFileReader.getCommandPrefix()+"shutdown**\nturns the bot off (can be used only from an admin or mod)\n\n");
		if(IniFileReader.getRebootCommand())textCollector.add("**-"+IniFileReader.getCommandPrefix()+"reboot**\nrestarts the Bot(can be used only from an admin or mod)\n\n");
		if(IniFileReader.getRegisterCommand())textCollector.add("**-"+IniFileReader.getCommandPrefix()+"register**\nregister channel, role, ranking role or users with the database\n\n");
		if(IniFileReader.getSetCommand())textCollector.add("**-"+IniFileReader.getCommandPrefix()+"set**\nset set specific paramater to configure your bot and server\n\n");
		if(IniFileReader.getUserCommand())textCollector.add("**-"+IniFileReader.getCommandPrefix()+"user**\nchoose between various actions that you can take against a user\n\n");
		if(IniFileReader.getFilterCommand())textCollector.add("**-"+IniFileReader.getCommandPrefix()+"filter**\ndecide to view, add or remove words/names from various filters or funky names\n\n");
		if(IniFileReader.getRoleReactionCommand())textCollector.add("**-"+IniFileReader.getCommandPrefix()+"roleReaction**\nenable / disable the role reaction and remove roles on disable\n\n");
		if(IniFileReader.getRssCommand())textCollector.add("**-"+IniFileReader.getCommandPrefix()+"rss**\ninsert rss feeds \n\n");
		if(entertainment == true)textCollector.add("**_Entertainment:_**\n");
		if(IniFileReader.getPugCommand())textCollector.add("**-"+IniFileReader.getCommandPrefix()+"pug**\nshows a pug picture. Use help as a parameter to get a list of all parameters\n\n");
		if(IniFileReader.getMeowCommand())textCollector.add("**-"+IniFileReader.getCommandPrefix()+"meow**\nshows a cat picture. Use help as a parameter to get a list of all parameters\n\n");
		if(IniFileReader.getRankCommand())textCollector.add("**-"+IniFileReader.getCommandPrefix()+"rank**\nshows the players actual rank\n\n");
		if(IniFileReader.getProfileCommand())textCollector.add("**-"+IniFileReader.getCommandPrefix()+"profile**\nshows the players actual rank with more informations\n\n");
		if(IniFileReader.getTopCommand())textCollector.add("**-"+IniFileReader.getCommandPrefix()+"top**\nshows the top 10 ranking\n\n");
		if(IniFileReader.getUseCommand())textCollector.add("**-"+IniFileReader.getCommandPrefix()+"use**\nto use an item from your inventory\n\n");
		if(IniFileReader.getShopCommand())textCollector.add("**-"+IniFileReader.getCommandPrefix()+"shop**\ndisplay the content in the shop\n\n");
		if(IniFileReader.getInventoryCommand())textCollector.add("**-"+IniFileReader.getCommandPrefix()+"inventory**\ndisplays the content inside your inventory\n\n");
		if(IniFileReader.getPurchaseCommand())textCollector.add("**-"+IniFileReader.getCommandPrefix()+"purchase**\nto purchase and item or skin from the shop\n\n");
		if(IniFileReader.getDailyCommand())textCollector.add("**-"+IniFileReader.getCommandPrefix()+"daily**\nto get a daily reward\n\n");
		if(IniFileReader.getQuizCommand())textCollector.add("**-"+IniFileReader.getCommandPrefix()+"quiz**\nto initialize a question and answer session with rewards\n\n");
		if(IniFileReader.getRandomshopCommand())textCollector.add("**-"+IniFileReader.getCommandPrefix()+"randomshop**\nto receive a random weapon from the randomshop\n\n");
		if(other == true)textCollector.add("**_Other:_**\n");
		if(IniFileReader.getAboutCommand())textCollector.add("**-"+IniFileReader.getCommandPrefix()+"about**\nshows all information regarding this Bot\n\n");
		if(IniFileReader.getHelpCommand())textCollector.add("**-"+IniFileReader.getCommandPrefix()+"help**\nshows a link to our tech thread on forum\n\n");
		if(IniFileReader.getDisplayCommand())textCollector.add("**-"+IniFileReader.getCommandPrefix()+"display**\nshows details of registered roles, rank level, channel filter, etc. (few parameters may be restricted)\n\n");
		
		if(administration == false && entertainment == false && other == false){
			textCollector.add("No command has been enabled to display");
		}
		
		for(String text : textCollector){
			 readMessage.append(text);
		}
		String message = readMessage.toString();
		return message;
	}
}
