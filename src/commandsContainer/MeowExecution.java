package commandsContainer;

import java.io.File;
import java.io.IOException;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import preparedMessages.MeowUsage;
import threads.CommandDelay;

public class MeowExecution {
	private static String commandInfo = MeowUsage.getMeowInfos();

	public static void Execute(MessageReceivedEvent e, String _variable, String _path, long channel_id) throws IOException{
		String variable = _variable;
		String path = _path;
		String pictureName = "";
		String fileName = IniFileReader.getTempDirectory()+"CommandDelay/"+e.getMember().getUser().getId()+"_meow.azr";
		File file = new File(fileName);
		
		if(!file.exists()){
			try {
				file.createNewFile();
				new Thread(new CommandDelay(fileName, 20000)).start();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			
			if(variable.equals("H!meow meow")){
				file = new File(path+"meow01.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow art")){
				file = new File(path+"meow02.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow attention")){
				file = new File(path+"meow03.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow baker")){
				file = new File(path+"meow04.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow bicycle")){
				file = new File(path+"meow05.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow birthday")){
				file = new File(path+"meow06.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow blonde")){
				file = new File(path+"meow07.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow blue")){
				file = new File(path+"meow08.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow box")){
				file = new File(path+"meow09.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow broken")){
				file = new File(path+"meow10.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow brunette")){
				file = new File(path+"meow11.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow car")){
				file = new File(path+"meow12.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow catrick")){
				file = new File(path+"meow13.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow chef")){
				file = new File(path+"meow14.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow chicks")){
				file = new File(path+"meow15.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow christmas")){
				file = new File(path+"meow16.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow cup")){
				file = new File(path+"meow17.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow distracted")){
				file = new File(path+"meow18.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow down")){
				file = new File(path+"meow19.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow elvis")){
				file = new File(path+"meow20.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow excited")){
				file = new File(path+"meow21.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow family")){
				file = new File(path+"meow22.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow gaming")){
				file = new File(path+"meow23.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow happy")){
				file = new File(path+"meow24.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow kidmeow")){
				file = new File(path+"meow25.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow laundry")){
				file = new File(path+"meow26.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow life")){
				file = new File(path+"meow27.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow litter")){
				file = new File(path+"meow28.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow love")){
				file = new File(path+"meow29.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow loveyou")){
				file = new File(path+"meow30.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow mess")){
				file = new File(path+"meow31.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow mexican")){
				file = new File(path+"meow32.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow moustache")){
				file = new File(path+"meow33.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow munching")){
				file = new File(path+"meow34.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow nudge")){
				file = new File(path+"meow35.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow online")){
				file = new File(path+"meow36.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow piano")){
				file = new File(path+"meow37.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow pikachu")){
				file = new File(path+"meow38.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow powerpuffmeow")){
				file = new File(path+"meow39.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow present")){
				file = new File(path+"meow40.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow relaxed")){
				file = new File(path+"meow41.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow ripped")){
				file = new File(path+"meow42.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow sad")){
				file = new File(path+"meow43.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow satisfied")){
				file = new File(path+"meow44.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow seal")){
				file = new File(path+"meow45.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow sia")){
				file = new File(path+"meow46.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow sir")){
				file = new File(path+"meow47.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow sleeping")){
				file = new File(path+"meow48.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow string")){
				file = new File(path+"meow49.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow study")){
				file = new File(path+"meow50.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow unicorn")){
				file = new File(path+"meow51.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow tumblr")){
				file = new File(path+"meow52.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow viking")){
				file = new File(path+"meow53.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow winkyface")){
				file = new File(path+"meow54.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow wool")){
				file = new File(path+"meow55.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow workout")){
				file = new File(path+"meow56.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow wrapped")){
				file = new File(path+"meow57.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow cake")){
				file = new File(path+"meow58.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow chickenwings")){
				file = new File(path+"meow59.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow cookies")){
				file = new File(path+"meow60.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow cupcake")){
				file = new File(path+"meow61.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow diet")){
				file = new File(path+"meow62.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow donut")){
				file = new File(path+"meow63.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow fishy")){
				file = new File(path+"meow64.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow hotchocolate")){
				file = new File(path+"meow65.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow icecream")){
				file = new File(path+"meow66.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow mcmeow")){
				file = new File(path+"meow67.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow noodles")){
				file = new File(path+"meow68.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow nutella")){
				file = new File(path+"meow69.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow onigiri")){
				file = new File(path+"meow70.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow pizza")){
				file = new File(path+"meow71.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow sushy")){
				file = new File(path+"meow72.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow sweetmountain")){
				file = new File(path+"meow73.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow toast")){
				file = new File(path+"meow74.png");
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow random-emoji")){
				int randomNumber = (int)(Math.random()*57)+1;
				switch(randomNumber){
				case 1: pictureName = "meow01.png";
						break;
				case 2: pictureName = "meow02.png";
						break;
				case 3: pictureName = "meow03.png";
						break;
				case 4: pictureName = "meow04.png";
						break;
				case 5: pictureName = "meow05.png";
						break;
				case 6: pictureName = "meow06.png";
						break;
				case 7: pictureName = "meow07.png";
						break;
				case 8: pictureName = "meow08.png";
						break;
				case 9: pictureName = "meow09.png";
						break;
				case 10: pictureName = "meow10.png";
						break;
				case 11: pictureName = "meow11.png";
						break;
				case 12: pictureName = "meow12.png";
						break;
				case 13: pictureName = "meow13.png";
						break;
				case 14: pictureName = "meow14.png";
						break;
				case 15: pictureName = "meow15.png";
						break;
				case 16: pictureName = "meow16.png";
						break;
				case 17: pictureName = "meow17.png";
						break;
				case 18: pictureName = "meow18.png";
						break;
				case 19: pictureName = "meow19.png";
						break;
				case 20: pictureName = "meow20.png";
						break;
				case 21: pictureName = "meow21.png";
						break;
				case 22: pictureName = "meow22.png";
						break;
				case 23: pictureName = "meow23.png";
						break;
				case 24: pictureName = "meow24.png";
						break;
				case 25: pictureName = "meow25.png";
						break;
				case 26: pictureName = "meow26.png";
						break;
				case 27: pictureName = "meow27.png";
						break;
				case 28: pictureName = "meow28.png";
						break;
				case 29: pictureName = "meow29.png";
						break;
				case 30: pictureName = "meow30.png";
						break;
				case 31: pictureName = "meow31.png";
						break;
				case 32: pictureName = "meow32.png";
						break;
				case 33: pictureName = "meow33.png";
						break;
				case 34: pictureName = "meow34.png";
						break;
				case 35: pictureName = "meow35.png";
						break;
				case 36: pictureName = "meow36.png";
						break;
				case 37: pictureName = "meow37.png";
						break;
				case 38: pictureName = "meow38.png";
						break;
				case 39: pictureName = "meow39.png";
						break;
				case 40: pictureName = "meow40.png";
						break;
				case 41: pictureName = "meow41.png";
						break;
				case 42: pictureName = "meow42.png";
						break;
				case 43: pictureName = "meow43.png";
						break;
				case 44: pictureName = "meow44.png";
						break;
				case 45: pictureName = "meow45.png";
						break;
				case 46: pictureName = "meow46.png";
						break;
				case 47: pictureName = "meow47.png";
						break;
				case 48: pictureName = "meow48.png";
						break;
				case 49: pictureName = "meow49.png";
						break;
				case 50: pictureName = "meow50.png";
						break;
				case 51: pictureName = "meow51.png";
						break;
				case 52: pictureName = "meow52.png";
						break;
				case 53: pictureName = "meow53.png";
						break;
				case 54: pictureName = "meow54.png";
						break;
				case 55: pictureName = "meow55.png";
						break;
				case 56: pictureName = "meow56.png";
						break;
				case 57: pictureName = "meow57.png";
						break;
				}
				file = new File(path+""+pictureName);
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow random-food")){
				int randomNumber = (int)(Math.random()*18)+1;
				switch(randomNumber){
				case 1: pictureName = "meow58.png";
						break;
				case 2: pictureName = "meow59.png";
						break;
				case 3: pictureName = "meow60.png";
						break;
				case 4: pictureName = "meow61.png";
						break;
				case 5: pictureName = "meow62.png";
						break;
				case 6: pictureName = "meow63.png";
						break;
				case 7: pictureName = "meow64.png";
						break;
				case 8: pictureName = "meow65.png";
						break;
				case 9: pictureName = "meow66.png";
						break;
				case 10: pictureName = "meow67.png";
						break;
				case 11: pictureName = "meow68.png";
						break;
				case 12: pictureName = "meow69.png";
						break;
				case 13: pictureName = "meow70.png";
						break;
				case 14: pictureName = "meow71.png";
						break;
				case 15: pictureName = "meow72.png";
						break;
				case 17: pictureName = "meow73.png";
						break;
				case 18: pictureName = "meow74.png";
				}
				file = new File(path+""+pictureName);
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow random-meow")){
				int randomNumber = (int)(Math.random()*74)+1;
				switch(randomNumber){
				case 1: pictureName = "meow01.png";
						break;
				case 2: pictureName = "meow02.png";
						break;
				case 3: pictureName = "meow03.png";
						break;
				case 4: pictureName = "meow04.png";
						break;
				case 5: pictureName = "meow05.png";
						break;
				case 6: pictureName = "meow06.png";
						break;
				case 7: pictureName = "meow07.png";
						break;
				case 8: pictureName = "meow08.png";
						break;
				case 9: pictureName = "meow09.png";
						break;
				case 10: pictureName = "meow10.png";
						break;
				case 11: pictureName = "meow11.png";
						break;
				case 12: pictureName = "meow12.png";
						break;
				case 13: pictureName = "meow13.png";
						break;
				case 14: pictureName = "meow14.png";
						break;
				case 15: pictureName = "meow15.png";
						break;
				case 16: pictureName = "meow16.png";
						break;
				case 17: pictureName = "meow17.png";
						break;
				case 18: pictureName = "meow18.png";
						break;
				case 19: pictureName = "meow19.png";
						break;
				case 20: pictureName = "meow20.png";
						break;
				case 21: pictureName = "meow21.png";
						break;
				case 22: pictureName = "meow22.png";
						break;
				case 23: pictureName = "meow23.png";
						break;
				case 24: pictureName = "meow24.png";
						break;
				case 25: pictureName = "meow25.png";
						break;
				case 26: pictureName = "meow26.png";
						break;
				case 27: pictureName = "meow27.png";
						break;
				case 28: pictureName = "meow28.png";
						break;
				case 29: pictureName = "meow29.png";
						break;
				case 30: pictureName = "meow30.png";
						break;
				case 31: pictureName = "meow31.png";
						break;
				case 32: pictureName = "meow32.png";
						break;
				case 33: pictureName = "meow33.png";
						break;
				case 34: pictureName = "meow34.png";
						break;
				case 35: pictureName = "meow35.png";
						break;
				case 36: pictureName = "meow36.png";
						break;
				case 37: pictureName = "meow37.png";
						break;
				case 38: pictureName = "meow38.png";
						break;
				case 39: pictureName = "meow39.png";
						break;
				case 40: pictureName = "meow40.png";
						break;
				case 41: pictureName = "meow41.png";
						break;
				case 42: pictureName = "meow42.png";
						break;
				case 43: pictureName = "meow43.png";
						break;
				case 44: pictureName = "meow44.png";
						break;
				case 45: pictureName = "meow45.png";
						break;
				case 46: pictureName = "meow46.png";
						break;
				case 47: pictureName = "meow47.png";
						break;
				case 48: pictureName = "meow48.png";
						break;
				case 49: pictureName = "meow49.png";
						break;
				case 50: pictureName = "meow50.png";
						break;
				case 51: pictureName = "meow51.png";
						break;
				case 52: pictureName = "meow52.png";
						break;
				case 53: pictureName = "meow53.png";
						break;
				case 54: pictureName = "meow54.png";
						break;
				case 55: pictureName = "meow55.png";
						break;
				case 56: pictureName = "meow56.png";
						break;
				case 57: pictureName = "meow57.png";
						break;
				case 58: pictureName = "meow58.png";
						break;
				case 59: pictureName = "meow59.png";
						break;
				case 60: pictureName = "meow60.png";
						break;
				case 61: pictureName = "meow61.png";
						break;
				case 62: pictureName = "meow62.png";
						break;
				case 63: pictureName = "meow63.png";
						break;
				case 64: pictureName = "meow64.png";
						break;
				case 65: pictureName = "meow65.png";
						break;
				case 66: pictureName = "meow66.png";
						break;
				case 67: pictureName = "meow67.png";
						break;
				case 68: pictureName = "meow68.png";
						break;
				case 69: pictureName = "meow69.png";
						break;
				case 70: pictureName = "meow70.png";
						break;
				case 71: pictureName = "meow71.png";
						break;
				case 72: pictureName = "meow72.png";
						break;
				case 73: pictureName = "meow73.png";
						break;
				case 74: pictureName = "meow74.png";
						break;
				}
				file = new File(path+""+pictureName);
				e.getTextChannel().sendFile(file, "meow.png", null).complete();
			}
			
			else if(variable.equals("H!meow help")){
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
