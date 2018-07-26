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
		
		if(IniFileReader.getShutDownCommand().equals("true") || IniFileReader.getRebootCommand().equals("true") || IniFileReader.getRegisterCommand().equals("true") || IniFileReader.getSetCommand().equals("true") || IniFileReader.getUserCommand().equals("true") || IniFileReader.getFilterCommand().equals("true")){
			administration = true;
		}
		
		if(IniFileReader.getPugCommand().equals("true") || IniFileReader.getMeowCommand().equals("true") || IniFileReader.getRankCommand().equals("true") || IniFileReader.getProfileCommand().equals("true") || IniFileReader.getTopCommand().equals("true") || IniFileReader.getUseCommand().equals("true") || IniFileReader.getShopCommand().equals("true") || IniFileReader.getInventoryCommand().equals("true") || IniFileReader.getPurchaseCommand().equals("true") || IniFileReader.getDailyCommand().equals("true")){
			entertainment = true;
		}
		
		if(IniFileReader.getAboutCommand().equals("true") || IniFileReader.getHelpCommand().equals("true") || IniFileReader.getDisplayCommand().equals("true")){
			other = true;
		}
		
		if(administration == true)textCollector.add("**_Administration:_**\n");
		if(IniFileReader.getShutDownCommand().equals("true"))textCollector.add("**-"+IniFileReader.getCommandPrefix()+"shutdown**\nturns the bot off (can be used only from an admin or mod)\n\n");
		if(IniFileReader.getRebootCommand().equals("true"))textCollector.add("**-"+IniFileReader.getCommandPrefix()+"reboot**\nrestarts the Bot(can be used only from an admin or mod)\n\n");
		if(IniFileReader.getRegisterCommand().equals("true"))textCollector.add("**-"+IniFileReader.getCommandPrefix()+"register**\nregister channel, role, ranking role or users with the database\n\n");
		if(IniFileReader.getSetCommand().equals("true"))textCollector.add("**-"+IniFileReader.getCommandPrefix()+"set**\nset set specific paramater to configure your bot and server\n\n");
		if(IniFileReader.getUserCommand().equals("true"))textCollector.add("**-"+IniFileReader.getCommandPrefix()+"user**\nchoose between various actions that you can take against a user\n\n");
		if(IniFileReader.getFilterCommand().equals("true"))textCollector.add("**-"+IniFileReader.getCommandPrefix()+"filter**\ndecide to view, add or remove words/names from various filters or funky names\n\n");
		if(entertainment == true)textCollector.add("**_Entertainment:_**\n");
		if(IniFileReader.getPugCommand().equals("true"))textCollector.add("**-"+IniFileReader.getCommandPrefix()+"pug**\nshows a pug picture. Use help as a parameter to get a list of all parameters\n\n");
		if(IniFileReader.getMeowCommand().equals("true"))textCollector.add("**-"+IniFileReader.getCommandPrefix()+"meow**\nshows a cat picture. Use help as a parameter to get a list of all parameters\n\n");
		if(IniFileReader.getRankCommand().equals("true"))textCollector.add("**-"+IniFileReader.getCommandPrefix()+"rank**\nshows the players actual rank\n\n");
		if(IniFileReader.getProfileCommand().equals("true"))textCollector.add("**-"+IniFileReader.getCommandPrefix()+"profile**\nshows the players actual rank with more informations\n\n");
		if(IniFileReader.getTopCommand().equals("true"))textCollector.add("**-"+IniFileReader.getCommandPrefix()+"top**\nshows the top 10 ranking (still in beta phase\n\n");
		if(IniFileReader.getUseCommand().equals("true"))textCollector.add("**-"+IniFileReader.getCommandPrefix()+"use**\nto use an item from your inventory\n\n");
		if(IniFileReader.getShopCommand().equals("true"))textCollector.add("**-"+IniFileReader.getCommandPrefix()+"shop**\ndisplay the content of the shop\n\n");
		if(IniFileReader.getInventoryCommand().equals("true"))textCollector.add("**-"+IniFileReader.getCommandPrefix()+"inventory**\ndisplays the content in your inventory\n\n");
		if(IniFileReader.getPurchaseCommand().equals("true"))textCollector.add("**-"+IniFileReader.getCommandPrefix()+"purchase**\nto purchase and item or skin from the shop\n\n");
		if(IniFileReader.getDailyCommand().equals("true"))textCollector.add("**-"+IniFileReader.getCommandPrefix()+"daily**\nto get a daily reward\n\n");
		if(other == true)textCollector.add("**_Other:_**\n");
		if(IniFileReader.getAboutCommand().equals("true"))textCollector.add("**-"+IniFileReader.getCommandPrefix()+"about**\nshows all informations regarding this Bot\n\n");
		if(IniFileReader.getHelpCommand().equals("true"))textCollector.add("**-"+IniFileReader.getCommandPrefix()+"help**\nshows a link to our tech thread on forum\n\n");
		if(IniFileReader.getDisplayCommand().equals("true"))textCollector.add("**-"+IniFileReader.getCommandPrefix()+"display**\nshows details of registered roles, rank level, channel filter, etc. (few parameters may be restricted)\n\n");
		
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
