package util;

public class BannedNames {
	
	private static String [] listOfBadNames = new String [] {
			"fuck",			//eng
			"retard",
			"bitch",
			"vagina",
			"pussy",
			"blowjob",
			"boob",
			"cum",
			"clitoris",
			"cunt",
			"dick",
			"ejaculate",
			"gangbang",
			"hack",
			"h4ck",
			"masturbat",
			"nigger",
			"nigga",
			"pernis",
			"porn",
			"prostitute",
			"rape",
			"rapist",
			"slut",
			"sperm",
			"suck my",
			"sex",
			"tits",
			"whore",
			"arsch",		//ger
			"abspritz",
			"anal",
			"anus",
			"fick",
			"schlampe",
			"hure",
			"fotze",
			"orgasmus",
			"vagina",
			"hoden",
			"hitler",
			"trump",
			"gs",
			"gm",
			"cgm",
			"pm",
			"cm"
	};
	
	private static String [] nameToReplaceWith = new String [] {
			"SugarPrincess",
			"xXGirlXx",
			"CrazyCatLady",
			"TotallyAppropriate",
			"PugLife",
			"EmotionlessKoala",
			"StarLord",
			"Mademoiselle",
			"Enchantress",
			"KawaiiGirl",
			"KawaiiKitsune",
			"Wanderland",
			"StarMaiden",
			"EmpressOfSnow",
			"Sweetheart",
			"Blossom",
			"MoonMaiden"
	};
	
	private static int randomNameGenerator;
	
	public static String selectRandomName(){
		randomNameGenerator = (int) (Math.random()*16)+1;
		return nameToReplaceWith [randomNameGenerator];
	}
	
	public static String [] listOfBadnames(){
		return listOfBadNames;
	}
	
}
