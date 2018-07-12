package preparedMessages;

import java.util.ArrayList;

public class MeowUsage {

	private static StringBuilder readMessage = new StringBuilder();
	private static ArrayList <String> textCollector = new ArrayList<>();

	public static String getMeowInfos(){
		textCollector.add("Write these paramenters together with the H!meow command to \ndisplay a cat picture\n\n");
		textCollector.add("meow emojis:\n");
		textCollector.add("meow          \tart           \tattention\n");
		textCollector.add("baker         \tbicycle       \tbirthday\n");
		textCollector.add("blonde        \tblue          \tbox\n");
		textCollector.add("broken        \tbrunette      \tcar\n");
		textCollector.add("catrick       \tchef          \tchicks\n");
		textCollector.add("christmas     \tcup           \tdistracted\n");
		textCollector.add("down          \telvis         \texcited\n");
		textCollector.add("family        \tgaming        \thappy\n");
		textCollector.add("kidmeow       \tlaundry       \tlife\n");
		textCollector.add("litter        \tlove          \tloveyou\n");
		textCollector.add("mess          \tmexican       \tmoustache\n");
		textCollector.add("munching      \tnudge         \tonline\n");
		textCollector.add("piano         \tpikachu       \tpowerpuffmeow\n");
		textCollector.add("present       \trelaxed       \tripped\n");
		textCollector.add("sad           \tsatisfied     \tseal\n");
		textCollector.add("sia           \tsir           \tsleeping\n");
		textCollector.add("string        \tstudy         \tunicorn\n");
		textCollector.add("tumblr        \tviking        \twinkyface\n");
		textCollector.add("wool          \tworkout       \twrapped\n\n");
		textCollector.add("Cat emojis with food:\n");
		textCollector.add("cake          \tchickenwings  \tcookies\n");
		textCollector.add("cupcake       \tdiet          \tdonut\n");
		textCollector.add("fishy         \thotchocolate  \ticecream\n");
		textCollector.add("mcmeow        \tnoodles       \tnutella\n");
		textCollector.add("onigiri       \tpizza         \tsushy\n");
		textCollector.add("sweetmountain \ttoast\n\n");
		textCollector.add("Parameters for a random cat:\n");
		textCollector.add("random-emoji  \trandom-food   \trandom-meow\n");
		
		for(String text : textCollector){
			 readMessage.append(text);
		}
		String message = readMessage.toString();
		return message;
	}
}
