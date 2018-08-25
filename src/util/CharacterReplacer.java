package util;

public class CharacterReplacer {
private static String parseMessage;
	
	public static String replace(String message){
		if(message != null){
			message = message.replaceAll("[\\.\\?!,\"'+@#$%^&*\\(\\)\\{\\}\\]\\[/\\-\\_\\|\\=§‘’`„°•—–¿¡₩€¢¥£]", "");
			message = message.replaceAll("[ÀÁÂÃÅÆàáâãåæĀāĂăΑАаӒӓ]", "a");
			message = message.replaceAll("[Ąą]", "ah");
			message = message.replaceAll("[ΒВЬ]", "b");
			message = message.replaceAll("[ĆćĈĉĊċČčϹСⅭϲсⅽ]", "c");
			message = message.replaceAll("[çÇ]", "ch");
			message = message.replaceAll("[ÐĎďĐđƉƊԁⅾ]", "d");
			message = message.replaceAll("[ÈÉÊËèéêëĚěĒēĔĕĖėΕЕе]", "e");
			message = message.replaceAll("[Ęę]", "eh");
			message = message.replaceAll("[Ϝ]", "f");
			message = message.replaceAll("[ĜĝĞğĠġģԌ]", "g");
			message = message.replaceAll("[Ģ]", "gh");
			message = message.replaceAll("[ĤĥĦħΗНһ]", "h");
			message = message.replaceAll("[1ĨĩĪīĬĭĮįİıĲĳÌÍÎÏìíîïÌÍÎÏ¡!ΙІⅠіⅰ]", "i");
			message = message.replaceAll("[ĴĵЈј]", "j");
			message = message.replaceAll("[ĶķĸΚКK]", "k");
			message = message.replaceAll("[ĹĺĻļĽľĿŀŁł]", "l");
			message = message.replaceAll("[ΜМⅯⅿ]", "m");
			message = message.replaceAll("[ŃńŅņŇňŉŊŋñΝ]", "n");
			message = message.replaceAll("[0ŌōŎŏŐőŒœòóôõΟОοоӦӧ]", "o");
			message = message.replaceAll("[ΡРр₽]", "p");
			message = message.replaceAll("[ŔŕŖŗŘř]", "r");
			message = message.replaceAll("[ŚśŜŝŠšЅѕ]", "s");
			message = message.replaceAll("[Şş]", "sh");
			message = message.replaceAll("[ŢţŤťŦŧΤТ]", "t");
			message = message.replaceAll("[ŨũŪūŬŭŮůŰűÙÚÛùúûµ]", "u");
			message = message.replaceAll("[Ųų]", "uh");
			message = message.replaceAll("[ѴⅤνѵⅴ]", "v");
			message = message.replaceAll("[Ŵŵѡ]", "w");
			message = message.replaceAll("[ΧХⅩхⅹ]", "x");
			message = message.replaceAll("[ŶŷŸÝýÿΥҮу]", "y");
			message = message.replaceAll("[ŹźŻżŽžΖ]", "z");
			parseMessage = message;
		}
		return parseMessage;
	}
	
	private static String [] exceptions = {
			"sex change"
	};
	public static String[] getExceptions(){
		return exceptions;
	}
}
