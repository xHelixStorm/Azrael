package util;

public class CharacterReplacer {
private static String parseMessage;
	
	public static String replace(String message){
		if(message != null){
			message = message.replaceAll("[\\.\\?!,\"'+@#$%^&*\\(\\)\\{\\}\\]\\[/\\-\\_\\|\\=Â§â€˜â€™`â€žÂ°â€¢â€”â€“Â¿Â¡â‚©â‚¬Â¢Â¥Â£]", "");
			message = message.replaceAll("[Ã€Ã�Ã‚ÃƒÃ…Ã†Ã Ã¡Ã¢Ã£Ã¥Ã¦Ä€Ä�Ä‚ÄƒÎ‘Ð�Ð°Ó’Ó“]", "a");
			message = message.replaceAll("[Ä„Ä…]", "ah");
			message = message.replaceAll("[Î’Ð’Ð¬]", "b");
			message = message.replaceAll("[Ä†Ä‡ÄˆÄ‰ÄŠÄ‹ÄŒÄ�Ï¹Ð¡â…­Ï²Ñ�â…½]", "c");
			message = message.replaceAll("[Ã§Ã‡]", "ch");
			message = message.replaceAll("[Ã�ÄŽÄ�Ä�Ä‘Æ‰ÆŠÔ�â…¾]", "d");
			message = message.replaceAll("[ÃˆÃ‰ÃŠÃ‹Ã¨Ã©ÃªÃ«ÄšÄ›Ä’Ä“Ä”Ä•Ä–Ä—Î•Ð•Ðµ]", "e");
			message = message.replaceAll("[Ä˜Ä™]", "eh");
			message = message.replaceAll("[Ïœ]", "f");
			message = message.replaceAll("[ÄœÄ�ÄžÄŸÄ Ä¡Ä£ÔŒ]", "g");
			message = message.replaceAll("[Ä¢]", "gh");
			message = message.replaceAll("[Ä¤Ä¥Ä¦Ä§Î—Ð�Ò»]", "h");
			message = message.replaceAll("[1Ä¨Ä©ÄªÄ«Ä¬Ä­Ä®Ä¯Ä°Ä±Ä²Ä³ÃŒÃ�ÃŽÃ�Ã¬Ã­Ã®Ã¯ÃŒÃ�ÃŽÃ�Â¡!Î™Ð†â… Ñ–â…°]", "i");
			message = message.replaceAll("[Ä´ÄµÐˆÑ˜]", "j");
			message = message.replaceAll("[Ä¶Ä·Ä¸ÎšÐšâ„ª]", "k");
			message = message.replaceAll("[Ä¹ÄºÄ»Ä¼Ä½Ä¾Ä¿Å€Å�Å‚]", "l");
			message = message.replaceAll("[ÎœÐœâ…¯â…¿]", "m");
			message = message.replaceAll("[ÅƒÅ„Å…Å†Å‡ÅˆÅ‰ÅŠÅ‹Ã±Î�]", "n");
			message = message.replaceAll("[0ÅŒÅ�ÅŽÅ�Å�Å‘Å’Å“Ã²Ã³Ã´ÃµÎŸÐžÎ¿Ð¾Ó¦Ó§]", "o");
			message = message.replaceAll("[Î¡Ð Ñ€â‚½]", "p");
			message = message.replaceAll("[Å”Å•Å–Å—Å˜Å™]", "r");
			message = message.replaceAll("[ÅšÅ›ÅœÅ�Å Å¡Ð…Ñ•]", "s");
			message = message.replaceAll("[ÅžÅŸ]", "sh");
			message = message.replaceAll("[Å¢Å£Å¤Å¥Å¦Å§Î¤Ð¢]", "t");
			message = message.replaceAll("[Å¨Å©ÅªÅ«Å¬Å­Å®Å¯Å°Å±Ã™ÃšÃ›Ã¹ÃºÃ»Âµ]", "u");
			message = message.replaceAll("[Å²Å³]", "uh");
			message = message.replaceAll("[Ñ´â…¤Î½Ñµâ…´]", "v");
			message = message.replaceAll("[Å´ÅµÑ¡]", "w");
			message = message.replaceAll("[Î§Ð¥â…©Ñ…â…¹]", "x");
			message = message.replaceAll("[Å¶Å·Å¸Ã�Ã½Ã¿Î¥Ò®Ñƒ]", "y");
			message = message.replaceAll("[Å¹ÅºÅ»Å¼Å½Å¾Î–]", "z");
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
