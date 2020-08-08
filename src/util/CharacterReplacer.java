package util;

public class CharacterReplacer {
private static String parseMessage;
	
	public static String replace(String message){
		if(message != null){
			message = message.replaceAll("[\\.\\?!,\"+@#$%^&*\\(\\)\\{\\}\\]\\[/\\-\\_\\|\\=§‘’`„°•—–¿¡₩€¢¥£¡!-​]", "");
			message = message.replaceAll("[ÆæΑАа]", "a");
			message = message.replaceAll("[ΒВЬ]", "b");
			message = message.replaceAll("[ϹСⅭϲсⅽ]", "c");
			message = message.replaceAll("[ďԁⅾ]", "d");
			message = message.replaceAll("[3ΕЕе]", "e");
			message = message.replaceAll("[Ϝf​]", "f");
			message = message.replaceAll("[Ԍ]", "g");
			message = message.replaceAll("[ΗНһ]", "h");
			message = message.replaceAll("[1ĳΙІіⅰ]", "i");
			message = message.replaceAll("[Јј]", "j");
			message = message.replaceAll("[ĸΚКK]", "k");
			message = message.replaceAll("[ΜМⅯⅿ]", "m");
			message = message.replaceAll("[Νn​]", "n");
			message = message.replaceAll("[0ŒœΟОοо]", "o");
			message = message.replaceAll("[ΡРр₽]", "p");
			message = message.replaceAll("[Ѕѕ]", "s");
			message = message.replaceAll("[ŢŦŧΤТ]", "t");
			message = message.replaceAll("[µ]", "u");
			message = message.replaceAll("[ѴⅤνѵⅴ]", "v");
			message = message.replaceAll("[ѡ]", "w");
			message = message.replaceAll("[ΧХⅩхⅹ]", "x");
			message = message.replaceAll("[ΥҮу]", "y");
			message = message.replaceAll("[Ζ]", "z");
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
