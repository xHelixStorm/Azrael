package util;

public class RankingSystemPreferences {
	
	private static int [] experienceForRankUp = {
			300,	//Rank 0
			300,	//Rank 1
			300,	//Rank 2
			300,	//Rank 3
			300,	//Rank 4
			700,	//Rank 5
			700,	//Rank 6
			700,	//Rank 7
			700,	//Rank 8
			700,	//Rank 9
			1200,	//Rank 10
			1200,	//Rank 11
			1200,	//Rank 12
			1200,	//Rank 13
			1200,	//Rank 14
			1700,	//Rank 15
			1700,	//Rank 16
			1700,	//Rank 17
			1700,	//Rank 18
			2300,	//Rank 19
			2300,	//Rank 20
			2300,	//Rank 21
			2300,	//Rank 22
			2300,	//Rank 23
			4300,	//Rank 24
			4300,	//Rank 25
			4300,	//Rank 26
			4300,	//Rank 27
			4300,	//Rank 28
			9800,	//Rank 29
			9800,	//Rank 30
			9800,	//Rank 31
			9800,	//Rank 32
			9800,	//Rank 33
 			26800,	//Rank 34
			26800,	//Rank 35
			26800,	//Rank 36
			26800,	//Rank 37
			26800,	//Rank 38
			30800,	//Rank 39
			30800,	//Rank 40
			30800,	//Rank 41
			30800,	//Rank 42
			30800,	//Rank 43
			31800,	//Rank 44
			31800,	//Rank 45
			31800,	//Rank 46
			31800,	//Rank 47
			31800,	//Rank 48
			32800,	//Rank 49
			32800,	//Rank 50
			33800,	//Rank 51
			34800,	//Rank 52
			35800,	//Rank 53
			36800,	//Rank 54
			36800,	//Rank 55
			36800,	//Rank 56
			36800,	//Rank 57
			36800,	//Rank 58
			37800,	//Rank 59
			37800,	//Rank 60
			37800,	//Rank 61
			37800,	//Rank 62
			40800,	//Rank 63
			41800,	//Rank 64
			41800,	//Rank 65
			41800,	//Rank 66
			41800,	//Rank 67
			41800,	//Rank 68
			42800,	//Rank 69
			42800,	//Rank 70
			42800,	//Rank 71
			42800,	//Rank 72
			42800,	//Rank 73
			43800,	//Rank 74
			50000,	//Rank 75
			60000,	//Rank 76
			70000,	//Rank 77
			80000,	//Rank 78
			100000,	//Rank 79
	}; 
	
	private static int [] currencyForRankUp = {
			30000,	//Rank 0
			20000,	//Rank 1
			1000,	//Rank 2
			1000,	//Rank 3
			1000,	//Rank 4
			1000,	//Rank 5
			1000,	//Rank 6
			1000,	//Rank 7
			1000,	//Rank 8
			1000,	//Rank 9
			10000,	//Rank 10
			2000,	//Rank 11
			2000,	//Rank 12
			2000,	//Rank 13
			2000,	//Rank 14
			10000,	//Rank 15
			2000,	//Rank 16
			2000,	//Rank 17
			2000,	//Rank 18
			2000,	//Rank 19
			10000,	//Rank 20
			3000,	//Rank 21
			3000,	//Rank 22
			3000,	//Rank 23
			3000,	//Rank 24
			3000,	//Rank 25
			3000,	//Rank 26
			3000,	//Rank 27
			3000,	//Rank 28
			3000,	//Rank 29
			10000,	//Rank 30
			3500,	//Rank 31
			3500,	//Rank 32
			3500,	//Rank 33
			3500,	//Rank 34
			3500,	//Rank 35
			3500,	//Rank 36
			3500,	//Rank 37
			3500,	//Rank 38
			3500,	//Rank 39
			10000,	//Rank 40
			4000,	//Rank 41
			4000,	//Rank 42
			4000,	//Rank 43
			4000,	//Rank 44
			4000,	//Rank 45
			4500,	//Rank 46
			4500,	//Rank 47
			4500,	//Rank 48
			4500,	//Rank 49
			10000,	//Rank 50
			5000,	//Rank 51
			5000,	//Rank 52
			5000,	//Rank 53
			5000,	//Rank 54
			5000,	//Rank 55
			5500,	//Rank 56
			5500,	//Rank 57
			5500,	//Rank 58
			5500,	//Rank 59
			10000,	//Rank 60
			6000,	//Rank 61
			6000,	//Rank 62
			6000,	//Rank 63
			6000,	//Rank 64
			6000,	//Rank 65
			6500,	//Rank 66
			6500,	//Rank 67
			6500,	//Rank 68
			6500,	//Rank 69
			10000,	//Rank 70
			7000,	//Rank 71
			7000,	//Rank 72
			7000,	//Rank 73
			7000,	//Rank 74
			7000,	//Rank 75
			7500,	//Rank 76
			7500,	//Rank 77
			7500,	//Rank 78
			7500,	//Rank 79
			100000	//Rank 80
	};
	
	private static int [] totalExperience = {
			0,		//Rank 0
			300,	//Rank 1
			600,	//Rank 2
			900,	//Rank 3
			1200,	//Rank 4
			1500,	//Rank 5
			2200,	//Rank 6
			2900,	//Rank 7
			3600,	//Rank 8
			4300,	//Rank 9
			5000,	//Rank 10
			6200,	//Rank 11
			7400,	//Rank 12
			8600,	//Rank 13
			9800,	//Rank 14
			11000,	//Rank 15
			12700,	//Rank 16
			14400,	//Rank 17
			16100,  //Rank 18
			17800,	//Rank 19
			20100,	//Rank 20
			22400,	//Rank 21
			24700,	//Rank 22
			27000,	//Rank 23
			29300,	//Rank 24
			33600,	//Rank 25
			37900,	//Rank 26
			42200,	//Rank 27
			46500,	//Rank 28
			50800,	//Rank 29
			60600,	//Rank 30
			70400,	//Rank 31
			80200,	//Rank 32
			90000,	//Rank 33
			99800,	//Rank 34
			126600,	//Rank 35
			153400,	//Rank 36
			180200,	//Rank 37
			207000,	//Rank 38
			233800,	//Rank 39
			264600,	//Rank 40
			295400,	//Rank 41
			326200,	//Rank 42
			357000,	//Rank 43
			387800,	//Rank 44
			419600,	//Rank 45
			451400,	//Rank 46
			483200,	//Rank 47
			515000,	//Rank 48
			546800,	//Rank 49
			578600,	//Rank 50
			611400,	//Rank 51
			644200,	//Rank 52
			678000,	//Rank 53
			712800,	//Rank 54
			748600,	//Rank 55
			785400,	//Rank 56
			822200,	//Rank 57
			859000,	//Rank 58
			895800,	//Rank 59
			933600,	//Rank 60
			971400,	//Rank 61
			1009200,//Rank 62
			1047000,//Rank 63
			1087800,//Rank 64
			1129600,//Rank 65
			1171400,//Rank 66
			1213200,//Rank 67
			1255000,//Rank 68
			1296800,//Rank 69
			1339600,//Rank 70
			1382400,//Rank 71
			1425200,//Rank 72
			1468000,//Rank 73
			1510800,//Rank 74
			1554600,//Rank 75
			1604600,//Rank 76
			1664600,//Rank 77
			1734600,//Rank 78
			1814600,//Rank 79
			1914600,//Rank 80
	};
	
	
	public static int getExperienceForRankUp(int index){
		return experienceForRankUp[index];
	}
	public static int getCurrencyForRankUp(int index){
		return currencyForRankUp[index];
	}
	public static int getTotalExperience(int index){
		return totalExperience[index];
	}
}
