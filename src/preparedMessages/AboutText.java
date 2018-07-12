package preparedMessages;

import java.util.ArrayList;

import util.STATIC;

public class AboutText {
	
	public static StringBuilder readMessage = new StringBuilder();
	public static ArrayList <String> textCollector = new ArrayList<>();

	public static String getAbout(){
		/*Text Layout
		  About me
		  Name: 			xxx
		  Version:			*Version Number*
		  Creator:			[GS]Heiliger
		  Functions: 		Automatically assigns the community role to new Members after a little delay
		  Commands: 		Use H!help for more informations
		  Hobbies: 			IT, pizza and Anime
		  Favourite phrase: May the force be with you
		 */
		//To review
		textCollector.add("About me\n");
		textCollector.add("Name:\t\t\t Azrael\n");
		textCollector.add("Version:\t\t  " + STATIC.getVersion_New() + "\n");
		textCollector.add("Creator:\t\t  [GS]Heiliger S4League Game Sage\n");
		textCollector.add("Functions:\t\tAutomatically assigns community roles to new\n\t\t\t\t  Members after a little delay\n");
		textCollector.add("          \t\tAutomatically removes users on the mute role\n\t\t\t\t  after 24 hours\n");
		textCollector.add("          \t\tIt's coming with a self designed ranking system\n");
		textCollector.add("Commands:\t\t Use H!commands for more informations\n");
		textCollector.add("Favourite phrase: May the force be with you\n");
		
		for(String text : textCollector){
			 readMessage.append(text);
		}
		String message = readMessage.toString();
		return message;
		
	}
}
