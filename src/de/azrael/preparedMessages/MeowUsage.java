package de.azrael.preparedMessages;

import java.util.ArrayList;

import de.azrael.enums.Translation;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class MeowUsage {

	private static StringBuilder readMessage = new StringBuilder();
	private static ArrayList <String> textCollector = new ArrayList<>();

	public static String getMeowInfos(GuildMessageReceivedEvent e) {
		if(readMessage.length() == 0) {
			textCollector.add(STATIC.getTranslation(e.getMember(), Translation.MEOW_HELP));
			textCollector.add(STATIC.getTranslation(e.getMember(), Translation.MEOW_HELP_1));
			textCollector.add("`meow`, `art`, `attention`, ");
			textCollector.add("`baker`, `bicycle`, `birthday`, ");
			textCollector.add("`blonde`, `blue`, `box`, ");
			textCollector.add("`broken`, `brunette`, `car`, ");
			textCollector.add("`catrick`, `chef`, `chicks`, ");
			textCollector.add("`christmas`, `cup`, `distracted`, ");
			textCollector.add("`down`, `elvis`, `excited`, ");
			textCollector.add("`family`, `gaming`, `happy`, ");
			textCollector.add("`kidmeow`, `laundry`, `life`, ");
			textCollector.add("`litter`, `love`, `loveyou`, ");
			textCollector.add("`mess`, `mexican`, `moustache`, ");
			textCollector.add("`munching`, `nudge`, `online`, ");
			textCollector.add("`piano`, `pikachu`, `powerpuffmeow`, ");
			textCollector.add("`present`, `relaxed`, `ripped`, ");
			textCollector.add("`sad`, `satisfied`, `seal`, ");
			textCollector.add("`sia`, `sir`, `sleeping`, ");
			textCollector.add("`string`, `study`, `unicorn`, ");
			textCollector.add("`tumblr`, `viking`, `winkyface`, ");
			textCollector.add("`wool`, `workout`, `wrapped`\n\n");
			textCollector.add(STATIC.getTranslation(e.getMember(), Translation.MEOW_HELP_2));
			textCollector.add("`cake`, `chickenwings`, `cookies`, ");
			textCollector.add("`cupcake`, `diet`, `donut`, ");
			textCollector.add("`fishy`, `hotchocolate`, `icecream`, ");
			textCollector.add("`mcmeow`, `noodles`, `nutella`, ");
			textCollector.add("`onigiri`, `pizza`, `sushy`, ");
			textCollector.add("`sweetmountain`, `toast`\n\n");
			textCollector.add(STATIC.getTranslation(e.getMember(), Translation.MEOW_HELP_3));
			textCollector.add("`random-emoji`, `random-food`, `random-meow`");
			
			for(String text : textCollector){
				 readMessage.append(text);
			}
		}
		String message = readMessage.toString();
		return message;
	}
}
