package preparedMessages;

import java.util.ArrayList;

public class PugUsage {
	
	private static StringBuilder readMessage = new StringBuilder();
	private static ArrayList <String> textCollector = new ArrayList<>();

	public static String getPugInfos(){
		textCollector.add("Write these paramenters together with the H!pug command to \ndisplay a pug picture\n\n");
		textCollector.add("Pug emojis:\n");
		textCollector.add("pug           \tgreet         \tshowoff\n");
		textCollector.add("annoyed       \twink          \tsleep\n");
		textCollector.add("stress        \tstormtrooper  \tcry\n");
		textCollector.add("unconscious   \tpuzzled       \trage\n");
		textCollector.add("bunny         \tbunnyherd     \tfashion\n");
		textCollector.add("fat           \tholiday       \tloveletter\n");
		textCollector.add("pride         \tpugmountain   \trainbow\n");
		textCollector.add("rein          \tsanta         \tsleepy\n");
		textCollector.add("smile         \tsnowman       \ttwitch\n");
		textCollector.add("wub           \tpockie        \tramune\n");
		textCollector.add("wizard\n\n");
		textCollector.add("Pug emojis dressed as food and drinks:\n");
		textCollector.add("taco          \tburrito       \tbread\n");
		textCollector.add("donut         \tpizza         \tfries\n");
		textCollector.add("potato        \tlollipop      \tsalmon\n");
		textCollector.add("onigiri       \tchocolate     \tbacon\n");
		textCollector.add("cottoncandy   \thotdog        \tcookie\n");
		textCollector.add("tart          \tchocolatemilk \tchocoswiss\n");
		textCollector.add("coffee        \tcoke          \tcorn\n");
		textCollector.add("cream         \tcrisps        \tcupcake\n");
		textCollector.add("fruittart     \ticecream      \tjelly\n");
		textCollector.add("meatball      \tmilk          \tnacho\n");
		textCollector.add("pancake       \tpootloops     \tramen\n");
		textCollector.add("smores        \tstarbucks     \ttakiyaki\n");
		textCollector.add("takoyaki      \ttoastie\n\n");
		textCollector.add("Pug emojis dressed as marvel characters:\n");
		textCollector.add("ironman       \tcaptainamerica\tthor\n");
		textCollector.add("batman        \tcatwoman      \tloki\n");
		textCollector.add("storm         \twolverine     \twonderwoman\n");
		textCollector.add("spiderman     \tdeadpool      \tflash\n");
		textCollector.add("superman      \tstar-lord\n\n");
		textCollector.add("Overwatch pugs:\n");
		textCollector.add("bastion       \tdva           \tgenji\n");
		textCollector.add("hanzo         \tlucio         \tmccree\n");
		textCollector.add("mei           \tmercy         \treaper\n");
		textCollector.add("soldier76     \tsymmetra      \ttracer\n");
		textCollector.add("widowmaker    \twinston       \tzarya\n");
		textCollector.add("zenyatta\n\n");
		textCollector.add("Parameters for a random pug:\n");
		textCollector.add("random-emoji  \trandom-food   \trandom-marvel\n");
		textCollector.add("random-pug    \trandom-overwatch\n");
		
		for(String text : textCollector){
			 readMessage.append(text);
		}
		String message = readMessage.toString();
		return message;
	}
}
