package de.azrael.preparedMessages;

import java.util.ArrayList;

import de.azrael.enums.Translation;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PugUsage {
	
	private static StringBuilder readMessage = new StringBuilder();
	private static ArrayList <String> textCollector = new ArrayList<>();

	public static String getPugInfos(MessageReceivedEvent e) {
		if(readMessage.length() == 0) {
			textCollector.add(STATIC.getTranslation(e.getMember(), Translation.PUG_HELP));
			textCollector.add(STATIC.getTranslation(e.getMember(), Translation.PUG_HELP_1));
			textCollector.add("`pug`, `greet`, `showoff`, ");
			textCollector.add("`annoyed`, `wink`, `sleep`, ");
			textCollector.add("`stress`, `stormtrooper`, `cry`, ");
			textCollector.add("`unconscious`, `puzzled`, `rage`, ");
			textCollector.add("`bunny`, `bunnyherd`, `fashion`, ");
			textCollector.add("`fat`, `holiday`, `loveletter`, ");
			textCollector.add("`pride`, `pugmountain`, `rainbow`, ");
			textCollector.add("`rein`, `santa`, `sleepy`, ");
			textCollector.add("`smile`, `snowman`, `twitch`, ");
			textCollector.add("`wub`, `pockie`, `ramune`, ");
			textCollector.add("`wizard`\n\n");
			textCollector.add(STATIC.getTranslation(e.getMember(), Translation.PUG_HELP_2));
			textCollector.add("`taco`, `burrito`, `bread`, ");
			textCollector.add("`donut`, `pizza`, `fries`, ");
			textCollector.add("`potato`, `lollipop`, `salmon`, ");
			textCollector.add("`onigiri`, `chocolate`, `bacon`, ");
			textCollector.add("`cottoncandy`, `hotdog`, `cookie`, ");
			textCollector.add("`tart`, `chocolatemilk`, `chocoswiss`, ");
			textCollector.add("`coffee`, `coke`, `corn`, ");
			textCollector.add("`cream`, `crisps`, `cupcake`\n");
			textCollector.add("`fruittart`, `icecream`, `jelly`, ");
			textCollector.add("`meatball`, `milk`, `nacho`, ");
			textCollector.add("`pancake`, `pootloops`, `ramen`, ");
			textCollector.add("`smores`, `starbucks`, `takiyaki`, ");
			textCollector.add("`takoyaki`, `toastie`\n\n");
			textCollector.add(STATIC.getTranslation(e.getMember(), Translation.PUG_HELP_3));
			textCollector.add("`ironman`, `captainamerica`, `thor`, ");
			textCollector.add("`batman`, `catwoman`, `loki`, ");
			textCollector.add("`storm`, `wolverine`, `wonderwoman`, ");
			textCollector.add("`spiderman`, `deadpool`, `flash`, ");
			textCollector.add("`superman`, `star-lord`\n\n");
			textCollector.add(STATIC.getTranslation(e.getMember(), Translation.PUG_HELP_4));
			textCollector.add("`bastion`, `dva`, `genji`, ");
			textCollector.add("`hanzo`, `lucio`, `mccree`, ");
			textCollector.add("`mei`, `mercy`, `reaper`, ");
			textCollector.add("`soldier76`, `symmetra`, `tracer`, ");
			textCollector.add("`widowmaker`, `winston`, `zarya`, ");
			textCollector.add("`zenyatta`\n\n");
			textCollector.add(STATIC.getTranslation(e.getMember(), Translation.PUG_HELP_5));
			textCollector.add("`random-emoji`, `random-food`, `random-marvel`, ");
			textCollector.add("`random-pug`, `random-overwatch`");
			
			for(String text : textCollector){
				 readMessage.append(text);
			}
		}
		String message = readMessage.toString();
		return message;
	}
}
