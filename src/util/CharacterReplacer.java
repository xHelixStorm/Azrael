package util;

import java.util.ArrayList;

public class CharacterReplacer {
private static String parseMessage;
	
	public static String replace(String message, ArrayList<String> filter_lang) {
		if(message != null && filter_lang.size() == 1) {
			message = message.replaceAll("[\\.\\?!,\"+@#$%^&*\\(\\)\\{\\}\\]\\[\\/\\-\\_\\|\\=§‘’`„°•—–¿¡₩€¢¥£\\-​]", "");
			if(filter_lang.contains("eng")) {
				message = message.replaceAll("[ÀÁÂÃÅÆàáâãåæĀāĂăΑАаӒӓĄą]", "a");
				message = message.replaceAll("[ΒВЬ]", "b");
				message = message.replaceAll("[ĆćĈĉĊċČčϹСⅭϲсⅽçÇ]", "c");
				message = message.replaceAll("[ÐĎďĐđƉƊԁⅾ]", "d");
				message = message.replaceAll("[3ÈÉÊËèéêëĚěĒēĔĕĖėΕЕеĘę]", "e");
				message = message.replaceAll("[Ϝf​]", "f");
				message = message.replaceAll("[ĜĝĞğĠġģԌĢ]", "g");
				message = message.replaceAll("[ĤĥĦħΗНһ]", "h");
				message = message.replaceAll("[1ĨĩĪīĬĭĮįİıĲĳÌÍÎÏìíîïÌÍÎÏ¡!ΙІⅠіⅰ]", "i");
				message = message.replaceAll("[ĴĵЈј]", "j");
				message = message.replaceAll("[ĶķĸΚКK]", "k");
				message = message.replaceAll("[ĹĺĻļĽľĿŀŁł]", "l");
				message = message.replaceAll("[ΜМⅯⅿ]", "m");
				message = message.replaceAll("[ŃńŅņŇňŉŊŋñΝn​]", "n");
				message = message.replaceAll("[0ŌōŎŏŐőŒœòóôõΟОοоӦӧ]", "o");
				message = message.replaceAll("[ΡРр₽]", "p");
				message = message.replaceAll("[ŔŕŖŗŘř]", "r");
				message = message.replaceAll("[ŚśŜŝŠšЅѕŞş]", "s");
				message = message.replaceAll("[ŢţŤťŦŧΤТ]", "t");
				message = message.replaceAll("[ŨũŪūŬŭŮůŰűÙÚÛùúûµ]", "u");
				message = message.replaceAll("[ѴⅤνѵⅴ]", "v");
				message = message.replaceAll("[Ŵŵѡ]", "w");
				message = message.replaceAll("[ΧХⅩхⅹ]", "x");
				message = message.replaceAll("[ŶŷŸÝýÿΥҮу]", "y");
				message = message.replaceAll("[ŹźŻżŽžΖ]", "z");
			}
			parseMessage = message;
		}
		return parseMessage;
	}
	
	public static String simpleReplace(String message) {
		return message.replaceAll("[\\.\\?!,\"+@#$%^&*\\(\\)\\{\\}\\]\\[\\/\\-\\_\\|\\=§‘’`„°•—–¿¡₩€¢¥£\\-​]", "");
	}
	
	private static String [] exceptions = {
			"sex change"
	};
	public static String[] getExceptions(){
		return exceptions;
	}
}
