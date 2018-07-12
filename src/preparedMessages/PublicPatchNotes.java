package preparedMessages;

import java.util.ArrayList;

import util.STATIC;

public class PublicPatchNotes {
	public static StringBuilder readMessage = new StringBuilder();
	public static ArrayList <String> textCollector = new ArrayList<>();
	public static String publicPatchNotes(){
		/*Text Layout
		  Bot Patchnotes version *** 06.05.2017
		  
		  Added word filter for English and German
		    
		 */
		textCollector.add("Bot Patchnotes version "+ STATIC.getVersion_New() + " 31.10.2017\n\n");
		textCollector.add("-H!meow is out. With the similarities of H!pug, you can now display cat pictures! Additionally the H!top command is also available but still in beta phase. Gonna improve the design in these next days\n");
		
		for(String text : textCollector){
			 readMessage.append(text);
		}
		String message = readMessage.toString();
		return message;
	}
}