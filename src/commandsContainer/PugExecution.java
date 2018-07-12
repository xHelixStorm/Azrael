package commandsContainer;

import java.io.File;
import java.io.IOException;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import preparedMessages.PugUsage;
import threads.CommandDelay;

public class PugExecution {
	private static String commandInfo = PugUsage.getPugInfos();

	public static void Execute(MessageReceivedEvent e, String _variable, String _path, long channel_id) throws IOException{
		String variable = _variable;
		String path = _path;
		String pictureName = "";
		String fileName = IniFileReader.getTempDirectory()+"CommandDelay/"+e.getMember().getUser()+"_pug.azr";
		File file = new File(fileName);
		
		if(!file.exists()){
			try {
				file.createNewFile();
				new Thread(new CommandDelay(fileName, 20000)).start();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			
			if(variable.equals("H!pug pug")){
				file = new File(path+"pug01.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug greet")){
				file = new File(path+"pug02.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug showoff")){
				file = new File(path+"pug03.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug annoyed")){
				file = new File(path+"pug04.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug wink")){
				file = new File(path+"pug05.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug sleep")){
				file = new File(path+"pug06.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug stress")){
				file = new File(path+"pug07.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug stormtrooper")){
				file = new File(path+"pug08.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug cry")){
				file = new File(path+"pug09.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug unconscious")){
				file = new File(path+"pug10.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug puzzled")){
				file = new File(path+"pug11.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug rage")){
				file = new File(path+"pug12.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug taco")){
				file = new File(path+"pug13.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug burrito")){
				file = new File(path+"pug14.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug bread")){
				file = new File(path+"pug15.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug donut")){
				file = new File(path+"pug16.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug pizza")){
				file = new File(path+"pug17.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug fries")){
				file = new File(path+"pug18.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug potato")){
				file = new File(path+"pug19.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug lollipop")){
				file = new File(path+"pug20.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug salmon")){
				file = new File(path+"pug21.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug onigiri")){
				file = new File(path+"pug22.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug chocolate")){
				file = new File(path+"pug23.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug bacon")){
				file = new File(path+"pug24.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug cottoncandy")){
				file = new File(path+"pug25.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug hotdog")){
				file = new File(path+"pug26.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug cookie")){
				file = new File(path+"pug27.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug tart")){
				file = new File(path+"pug28.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug ironman")){
				file = new File(path+"pug29.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug captainamerica")){
				file = new File(path+"pug30.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug thor")){
				file = new File(path+"pug31.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug batman")){
				file = new File(path+"pug32.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug catwoman")){
				file = new File(path+"pug39.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug loki")){
				file = new File(path+"pug33.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug storm")){
				file = new File(path+"pug34.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug wolverine")){
				file = new File(path+"pug35.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug wonderwoman")){
				file = new File(path+"pug36.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug spiderman")){
				file = new File(path+"pug37.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug deadpool")){
				file = new File(path+"pug38.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug flash")){
				file = new File(path+"pug40.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug superman")){
				file = new File(path+"pug41.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug star-lord")){
				file = new File(path+"pug42.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug bastion")){
				file = new File(path+"pug43.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug dva")){
				file = new File(path+"pug44.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug genji")){
				file = new File(path+"pug45.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug hanzo")){
				file = new File(path+"pug46.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug lucio")){
				file = new File(path+"pug47.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug mccree")){
				file = new File(path+"pug48.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug mei")){
				file = new File(path+"pug49.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug mercy")){
				file = new File(path+"pug50.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug reaper")){
				file = new File(path+"pug51.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug soldier76")){
				file = new File(path+"pug52.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug symmetra")){
				file = new File(path+"pug53.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug tracer")){
				file = new File(path+"pug54.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug widowmaker")){
				file = new File(path+"pug55.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug winston")){
				file = new File(path+"pug56.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug zarya")){
				file = new File(path+"pug57.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug zenyatta")){
				file = new File(path+"pug58.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug fashion")){
				file = new File(path+"pug59.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug fat")){
				file = new File(path+"pug60.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug holiday")){
				file = new File(path+"pug61.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug loveletter")){
				file = new File(path+"pug62.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug pride")){
				file = new File(path+"pug63.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug pugmountain")){
				file = new File(path+"pug64.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug rainbow")){
				file = new File(path+"pug65.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug rein")){
				file = new File(path+"pug66.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug santa")){
				file = new File(path+"pug67.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug sleepy")){
				file = new File(path+"pug68.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug smile")){
				file = new File(path+"pug69.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug snowman")){
				file = new File(path+"pug70.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug twitch")){
				file = new File(path+"pug71.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug wub")){
				file = new File(path+"pug72.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug bunny")){
				file = new File(path+"pug73.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug bunnyherd")){
				file = new File(path+"pug74.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug pockie")){
				file = new File(path+"pug75.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug ramune")){
				file = new File(path+"pug76.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug chocolatemilk")){
				file = new File(path+"pug77.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug chocoswiss")){
				file = new File(path+"pug78.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug coffee")){
				file = new File(path+"pug79.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug coke")){
				file = new File(path+"pug80.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug corn")){
				file = new File(path+"pug81.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug cream")){
				file = new File(path+"pug82.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug crisps")){
				file = new File(path+"pug83.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug cupcake")){
				file = new File(path+"pug84.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug fruittart")){
				file = new File(path+"pug85.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug icecream")){
				file = new File(path+"pug86.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug jelly")){
				file = new File(path+"pug87.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug meatball")){
				file = new File(path+"pug88.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug milk")){
				file = new File(path+"pug89.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug nacho")){
				file = new File(path+"pug90.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug pancake")){
				file = new File(path+"pug91.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug pootloops")){
				file = new File(path+"pug92.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug ramen")){
				file = new File(path+"pug93.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug smores")){
				file = new File(path+"pug94.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug starbucks")){
				file = new File(path+"pug95.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug takiyaki")){
				file = new File(path+"pug96.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug takoyaki")){
				file = new File(path+"pug97.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug toastie")){
				file = new File(path+"pug98.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug wizard")){
				file = new File(path+"pug99.png");
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug random-emoji")){
				int randomNumber = (int)(Math.random()*31)+1;
				switch(randomNumber){
				case 1: pictureName = "pug01.png";
						break;
				case 2: pictureName = "pug02.png";
						break;
				case 3: pictureName = "pug03.png";
						break;
				case 4: pictureName = "pug04.png";
						break;
				case 5: pictureName = "pug05.png";
						break;
				case 6: pictureName = "pug06.png";
						break;
				case 7: pictureName = "pug07.png";
						break;
				case 8: pictureName = "pug08.png";
						break;
				case 9: pictureName = "pug09.png";
						break;
				case 10: pictureName = "pug10.png";
						break;
				case 11: pictureName = "pug11.png";
						break;
				case 12: pictureName = "pug12.png";
						break;
				case 13: pictureName = "pug59.png";
						break;
				case 14: pictureName = "pug60.png";
						break;
				case 15: pictureName = "pug61.png";
						break;
				case 16: pictureName = "pug62.png";
						break;
				case 17: pictureName = "pug63.png";
						break;
				case 18: pictureName = "pug64.png";
						break;
				case 19: pictureName = "pug65.png";
						break;
				case 20: pictureName = "pug66.png";
						break;
				case 21: pictureName = "pug67.png";
						break;
				case 22: pictureName = "pug68.png";
						break;
				case 23: pictureName = "pug69.png";
						break;
				case 24: pictureName = "pug70.png";
						break;
				case 25: pictureName = "pug71.png";
						break;
				case 26: pictureName = "pug72.png";
						break;
				case 27: pictureName = "pug73.png";
						break;
				case 28: pictureName = "pug74.png";
						break;
				case 29: pictureName = "pug75.png";
						break;
				case 30: pictureName = "pug76.png";
						break;
				case 31: pictureName = "pug99.png";
						break;
				}
				file = new File(path+""+pictureName);
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug random-food")){
				int randomNumber = (int)(Math.random()*39)+1;
				switch(randomNumber){
				case 1: pictureName = "pug13.png";
						break;
				case 2: pictureName = "pug14.png";
						break;
				case 3: pictureName = "pug15.png";
						break;
				case 4: pictureName = "pug16.png";
						break;
				case 5: pictureName = "pug17.png";
						break;
				case 6: pictureName = "pug18.png";
						break;
				case 7: pictureName = "pug19.png";
						break;
				case 8: pictureName = "pug20.png";
						break;
				case 9: pictureName = "pug21.png";
						break;
				case 10: pictureName = "pug22.png";
						break;
				case 11: pictureName = "pug23.png";
						break;
				case 12: pictureName = "pug24.png";
						break;
				case 13: pictureName = "pug25.png";
						break;
				case 14: pictureName = "pug26.png";
						break;
				case 15: pictureName = "pug27.png";
						break;
				case 17: pictureName = "pug28.png";
						break;
				case 18: pictureName = "pug77.png";
						break;
				case 19: pictureName = "pug78.png";
						break;
				case 20: pictureName = "pug79.png";
						break;
				case 21: pictureName = "pug80.png";
						break;
				case 22: pictureName = "pug81.png";
						break;
				case 23: pictureName = "pug82.png";
						break;
				case 24: pictureName = "pug83.png";
						break;
				case 25: pictureName = "pug84.png";
						break;
				case 26: pictureName = "pug85.png";
						break;
				case 27: pictureName = "pug86.png";
						break;
				case 28: pictureName = "pug87.png";
						break;
				case 29: pictureName = "pug88.png";
						break;
				case 30: pictureName = "pug89.png";
						break;
				case 31: pictureName = "pug90.png";
						break;
				case 32: pictureName = "pug91.png";
						break;
				case 33: pictureName = "pug92.png";
						break;
				case 34: pictureName = "pug93.png";
						break;
				case 35: pictureName = "pug94.png";
						break;
				case 36: pictureName = "pug95.png";
						break;
				case 37: pictureName = "pug96.png";
						break;
				case 38: pictureName = "pug97.png";
						break;
				case 39: pictureName = "pug98.png";
						break;
				}
				file = new File(path+""+pictureName);
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug random-marvel")){
				int randomNumber = (int)(Math.random()*14)+1;
				switch(randomNumber){
				case 1: pictureName = "pug29.png";
						break;
				case 2: pictureName = "pug30.png";
						break;
				case 3: pictureName = "pug31.png";
						break;
				case 4: pictureName = "pug32.png";
						break;
				case 5: pictureName = "pug33.png";
						break;
				case 6: pictureName = "pug34.png";
						break;
				case 7: pictureName = "pug35.png";
						break;
				case 8: pictureName = "pug36.png";
						break;
				case 9: pictureName = "pug37.png";
						break;
				case 10: pictureName = "pug38.png";
						break;
				case 11: pictureName = "pug39.png";
						break;
				case 12: pictureName = "pug40.png";
						break;
				case 13: pictureName = "pug41.png";
						break;
				case 14: pictureName = "pug42.png";
						break;
				}
				file = new File(path+""+pictureName);
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug random-overwatch")){
				int randomNumber = (int)(Math.random()*16)+1;
				switch(randomNumber){
				case 1: pictureName = "pug43.png";
						break;
				case 2: pictureName = "pug44.png";
						break;
				case 3: pictureName = "pug45.png";
						break;
				case 4: pictureName = "pug46.png";
						break;
				case 5: pictureName = "pug47.png";
						break;
				case 6: pictureName = "pug48.png";
						break;
				case 7: pictureName = "pug49.png";
						break;
				case 8: pictureName = "pug50.png";
						break;
				case 9: pictureName = "pug51.png";
						break;
				case 10: pictureName = "pug52.png";
						break;
				case 11: pictureName = "pug53.png";
						break;
				case 12: pictureName = "pug54.png";
						break;
				case 13: pictureName = "pug55.png";
						break;
				case 14: pictureName = "pug56.png";
						break;
				case 15: pictureName = "pug57.png";
						break;
				case 16: pictureName = "pug58.png";
				}
				file = new File(path+""+pictureName);
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug random-pug")){
				int randomNumber = (int)(Math.random()*99)+1;
				switch(randomNumber){
				case 1: pictureName = "pug01.png";
						break;
				case 2: pictureName = "pug02.png";
						break;
				case 3: pictureName = "pug03.png";
						break;
				case 4: pictureName = "pug04.png";
						break;
				case 5: pictureName = "pug05.png";
						break;
				case 6: pictureName = "pug06.png";
						break;
				case 7: pictureName = "pug07.png";
						break;
				case 8: pictureName = "pug08.png";
						break;
				case 9: pictureName = "pug09.png";
						break;
				case 10: pictureName = "pug10.png";
						break;
				case 11: pictureName = "pug11.png";
						break;
				case 12: pictureName = "pug12.png";
						break;
				case 13: pictureName = "pug13.png";
						break;
				case 14: pictureName = "pug14.png";
						break;
				case 15: pictureName = "pug15.png";
						break;
				case 16: pictureName = "pug16.png";
						break;
				case 17: pictureName = "pug17.png";
						break;
				case 18: pictureName = "pug18.png";
						break;
				case 19: pictureName = "pug19.png";
						break;
				case 20: pictureName = "pug20.png";
						break;
				case 21: pictureName = "pug21.png";
						break;
				case 22: pictureName = "pug22.png";
						break;
				case 23: pictureName = "pug23.png";
						break;
				case 24: pictureName = "pug24.png";
						break;
				case 25: pictureName = "pug25.png";
						break;
				case 26: pictureName = "pug26.png";
						break;
				case 27: pictureName = "pug27.png";
						break;
				case 28: pictureName = "pug28.png";
						break;
				case 29: pictureName = "pug29.png";
						break;
				case 30: pictureName = "pug30.png";
						break;
				case 31: pictureName = "pug31.png";
						break;
				case 32: pictureName = "pug32.png";
						break;
				case 33: pictureName = "pug33.png";
						break;
				case 34: pictureName = "pug34.png";
						break;
				case 35: pictureName = "pug35.png";
						break;
				case 36: pictureName = "pug36.png";
						break;
				case 37: pictureName = "pug37.png";
						break;
				case 38: pictureName = "pug38.png";
						break;
				case 39: pictureName = "pug39.png";
						break;
				case 40: pictureName = "pug40.png";
						break;
				case 41: pictureName = "pug41.png";
						break;
				case 42: pictureName = "pug42.png";
						break;
				case 43: pictureName = "pug43.png";
						break;
				case 44: pictureName = "pug44.png";
						break;
				case 45: pictureName = "pug45.png";
						break;
				case 46: pictureName = "pug46.png";
						break;
				case 47: pictureName = "pug47.png";
						break;
				case 48: pictureName = "pug48.png";
						break;
				case 49: pictureName = "pug49.png";
						break;
				case 50: pictureName = "pug50.png";
						break;
				case 51: pictureName = "pug51.png";
						break;
				case 52: pictureName = "pug52.png";
						break;
				case 53: pictureName = "pug53.png";
						break;
				case 54: pictureName = "pug54.png";
						break;
				case 55: pictureName = "pug55.png";
						break;
				case 56: pictureName = "pug56.png";
						break;
				case 57: pictureName = "pug57.png";
						break;
				case 58: pictureName = "pug58.png";
						break;
				case 59: pictureName = "pug59.png";
						break;
				case 60: pictureName = "pug60.png";
						break;
				case 61: pictureName = "pug61.png";
						break;
				case 62: pictureName = "pug62.png";
						break;
				case 63: pictureName = "pug63.png";
						break;
				case 64: pictureName = "pug64.png";
						break;
				case 65: pictureName = "pug65.png";
						break;
				case 66: pictureName = "pug66.png";
						break;
				case 67: pictureName = "pug67.png";
						break;
				case 68: pictureName = "pug68.png";
						break;
				case 69: pictureName = "pug69.png";
						break;
				case 70: pictureName = "pug70.png";
						break;
				case 71: pictureName = "pug71.png";
						break;
				case 72: pictureName = "pug72.png";
						break;
				case 73: pictureName = "pug73.png";
						break;
				case 74: pictureName = "pug74.png";
						break;
				case 75: pictureName = "pug75.png";
						break;
				case 76: pictureName = "pug76.png";
						break;
				case 77: pictureName = "pug77.png";
						break;
				case 78: pictureName = "pug78.png";
						break;
				case 79: pictureName = "pug79.png";
						break;
				case 80: pictureName = "pug80.png";
						break;
				case 81: pictureName = "pug81.png";
						break;
				case 82: pictureName = "pug82.png";
						break;
				case 83: pictureName = "pug83.png";
						break;
				case 84: pictureName = "pug84.png";
						break;
				case 85: pictureName = "pug85.png";
						break;
				case 86: pictureName = "pug86.png";
						break;
				case 87: pictureName = "pug87.png";
						break;
				case 88: pictureName = "pug88.png";
						break;
				case 89: pictureName = "pug89.png";
						break;
				case 90: pictureName = "pug90.png";
						break;
				case 91: pictureName = "pug91.png";
						break;
				case 92: pictureName = "pug92.png";
						break;
				case 93: pictureName = "pug93.png";
						break;
				case 94: pictureName = "pug94.png";
						break;
				case 95: pictureName = "pug95.png";
						break;
				case 96: pictureName = "pug96.png";
						break;
				case 97: pictureName = "pug97.png";
						break;
				case 98: pictureName = "pug98.png";
						break;
				case 99: pictureName = "pug99.png";
						break;
				}
				file = new File(path+""+pictureName);
				e.getTextChannel().sendFile(file, "pug.png", null).complete();
			}
			
			else if(variable.equals("H!pug help")){
				long channel = e.getTextChannel().getIdLong();
				if(channel == channel_id || channel_id == 0){
					e.getTextChannel().sendMessage("```"+commandInfo+"```").queue();
				}
				else{
					e.getTextChannel().sendMessage("Please type this command in <#"+channel_id+"> to see its usage").queue();
				}
			}
		}
		else {
			e.getTextChannel().sendMessage("This command is currently having a cooldown. Please try again later.").queue();
		}
	}
}
