package de.azrael.commandsContainer;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import de.azrael.constructors.Cache;
import de.azrael.core.Hashes;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.IniFileReader;
import de.azrael.preparedMessages.MeowUsage;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Extension of the meow command
 * @author xHelixStorm
 *
 */

public class MeowExecution {
	public static void Execute(GuildMessageReceivedEvent e, String [] _variable, String _path, long channel_id) throws IOException{
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.PINK).setThumbnail(IniFileReader.getMeowThumbnail());
		var variable = _variable;
		String path = _path;
		String pictureName = "";
		var cache = Hashes.getTempCache("meowDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
		
		if(cache == null || cache.getExpiration() - System.currentTimeMillis() <= 0) {
			if(variable.length == 0) {
				e.getChannel().sendMessage(messageBuild.setDescription(MeowUsage.getMeowInfos(e)).build()).queue();
			}
			else {
				Hashes.addTempCache("meowDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId(), new Cache(30000));
				File file;
				
				if(variable[0].equalsIgnoreCase("meow")) {
					file = new File(path+"meow01.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("art")) {
					file = new File(path+"meow02.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("attention")) {
					file = new File(path+"meow03.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("baker")) {
					file = new File(path+"meow04.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("bicycle")) {
					file = new File(path+"meow05.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("birthday")) {
					file = new File(path+"meow06.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("blonde")) {
					file = new File(path+"meow07.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("blue")) {
					file = new File(path+"meow08.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("box")) {
					file = new File(path+"meow09.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("broken")) {
					file = new File(path+"meow10.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("brunette")) {
					file = new File(path+"meow11.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("car")) {
					file = new File(path+"meow12.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("catrick")) {
					file = new File(path+"meow13.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("chef")) {
					file = new File(path+"meow14.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("chicks")) {
					file = new File(path+"meow15.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("christmas")) {
					file = new File(path+"meow16.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("cup")) {
					file = new File(path+"meow17.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("distracted")) {
					file = new File(path+"meow18.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("down")) {
					file = new File(path+"meow19.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("elvis")) {
					file = new File(path+"meow20.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("excited")) {
					file = new File(path+"meow21.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("family")) {
					file = new File(path+"meow22.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("gaming")) {
					file = new File(path+"meow23.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("happy")) {
					file = new File(path+"meow24.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("kidmeow")) {
					file = new File(path+"meow25.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("laundry")) {
					file = new File(path+"meow26.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("life")) {
					file = new File(path+"meow27.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("litter")) {
					file = new File(path+"meow28.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("love")) {
					file = new File(path+"meow29.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("loveyou")) {
					file = new File(path+"meow30.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("mess")) {
					file = new File(path+"meow31.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("mexican")) {
					file = new File(path+"meow32.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("moustache")) {
					file = new File(path+"meow33.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("munching")) {
					file = new File(path+"meow34.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("nudge")) {
					file = new File(path+"meow35.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("online")) {
					file = new File(path+"meow36.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("piano")) {
					file = new File(path+"meow37.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("pikachu")) {
					file = new File(path+"meow38.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("powerpuffmeow")) {
					file = new File(path+"meow39.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("present")) {
					file = new File(path+"meow40.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("relaxed")) {
					file = new File(path+"meow41.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("ripped")) {
					file = new File(path+"meow42.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("sad")) {
					file = new File(path+"meow43.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("satisfied")) {
					file = new File(path+"meow44.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("seal")) {
					file = new File(path+"meow45.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("sia")) {
					file = new File(path+"meow46.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("sir")) {
					file = new File(path+"meow47.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("sleeping")) {
					file = new File(path+"meow48.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("string")) {
					file = new File(path+"meow49.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("study")) {
					file = new File(path+"meow50.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("unicorn")) {
					file = new File(path+"meow51.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("tumblr")) {
					file = new File(path+"meow52.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("viking")) {
					file = new File(path+"meow53.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("winkyface")) {
					file = new File(path+"meow54.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("wool")) {
					file = new File(path+"meow55.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("workout")) {
					file = new File(path+"meow56.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("wrapped")) {
					file = new File(path+"meow57.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("cake")) {
					file = new File(path+"meow58.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("chickenwings")) {
					file = new File(path+"meow59.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("cookies")) {
					file = new File(path+"meow60.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("cupcake")) {
					file = new File(path+"meow61.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("diet")) {
					file = new File(path+"meow62.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("donut")) {
					file = new File(path+"meow63.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("fishy")) {
					file = new File(path+"meow64.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("hotchocolate")) {
					file = new File(path+"meow65.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("icecream")) {
					file = new File(path+"meow66.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("mcmeow")) {
					file = new File(path+"meow67.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("noodles")) {
					file = new File(path+"meow68.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("nutella")) {
					file = new File(path+"meow69.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("onigiri")) {
					file = new File(path+"meow70.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("pizza")) {
					file = new File(path+"meow71.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("sushy")) {
					file = new File(path+"meow72.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("sweetmountain")) {
					file = new File(path+"meow73.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("toast")) {
					file = new File(path+"meow74.png");
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("random-emoji")) {
					int randomNumber = (int)(Math.random()*57)+1;
					switch(randomNumber) {
						case 1 -> pictureName = "meow01.png";
						case 2 -> pictureName = "meow02.png";
						case 3 -> pictureName = "meow03.png";
						case 4 -> pictureName = "meow04.png";
						case 5 -> pictureName = "meow05.png";
						case 6 -> pictureName = "meow06.png";
						case 7 -> pictureName = "meow07.png";
						case 8 -> pictureName = "meow08.png";
						case 9 -> pictureName = "meow09.png";
						case 10 -> pictureName = "meow10.png";
						case 11 -> pictureName = "meow11.png";
						case 12 -> pictureName = "meow12.png";
						case 13 -> pictureName = "meow13.png";
						case 14 -> pictureName = "meow14.png";
						case 15 -> pictureName = "meow15.png";
						case 16 -> pictureName = "meow16.png";
						case 17 -> pictureName = "meow17.png";
						case 18 -> pictureName = "meow18.png";
						case 19 -> pictureName = "meow19.png";
						case 20 -> pictureName = "meow20.png";
						case 21 -> pictureName = "meow21.png";
						case 22 -> pictureName = "meow22.png";
						case 23 -> pictureName = "meow23.png";
						case 24 -> pictureName = "meow24.png";
						case 25 -> pictureName = "meow25.png";
						case 26 -> pictureName = "meow26.png";
						case 27 -> pictureName = "meow27.png";
						case 28 -> pictureName = "meow28.png";
						case 29 -> pictureName = "meow29.png";
						case 30 -> pictureName = "meow30.png";
						case 31 -> pictureName = "meow31.png";
						case 32 -> pictureName = "meow32.png";
						case 33 -> pictureName = "meow33.png";
						case 34 -> pictureName = "meow34.png";
						case 35 -> pictureName = "meow35.png";
						case 36 -> pictureName = "meow36.png";
						case 37 -> pictureName = "meow37.png";
						case 38 -> pictureName = "meow38.png";
						case 39 -> pictureName = "meow39.png";
						case 40 -> pictureName = "meow40.png";
						case 41 -> pictureName = "meow41.png";
						case 42 -> pictureName = "meow42.png";
						case 43 -> pictureName = "meow43.png";
						case 44 -> pictureName = "meow44.png";
						case 45 -> pictureName = "meow45.png";
						case 46 -> pictureName = "meow46.png";
						case 47 -> pictureName = "meow47.png";
						case 48 -> pictureName = "meow48.png";
						case 49 -> pictureName = "meow49.png";
						case 50 -> pictureName = "meow50.png";
						case 51 -> pictureName = "meow51.png";
						case 52 -> pictureName = "meow52.png";
						case 53 -> pictureName = "meow53.png";
						case 54 -> pictureName = "meow54.png";
						case 55 -> pictureName = "meow55.png";
						case 56 -> pictureName = "meow56.png";
						case 57 -> pictureName = "meow57.png";
					}
					file = new File(path+""+pictureName);
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("random-food")) {
					int randomNumber = (int)(Math.random()*18)+1;
					switch(randomNumber) {
						case 1 -> pictureName = "meow58.png";
						case 2 -> pictureName = "meow59.png";
						case 3 -> pictureName = "meow60.png";
						case 4 -> pictureName = "meow61.png";
						case 5 -> pictureName = "meow62.png";
						case 6 -> pictureName = "meow63.png";
						case 7 -> pictureName = "meow64.png";
						case 8 -> pictureName = "meow65.png";
						case 9 -> pictureName = "meow66.png";
						case 10 -> pictureName = "meow67.png";
						case 11 -> pictureName = "meow68.png";
						case 12 -> pictureName = "meow69.png";
						case 13 -> pictureName = "meow70.png";
						case 14 -> pictureName = "meow71.png";
						case 15 -> pictureName = "meow72.png";
						case 17 -> pictureName = "meow73.png";
						case 18 -> pictureName = "meow74.png";
					}
					file = new File(path+""+pictureName);
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("random-meow")) {
					int randomNumber = (int)(Math.random()*74)+1;
					switch(randomNumber) {
						case 1 -> pictureName = "meow01.png";
						case 2 -> pictureName = "meow02.png";
						case 3 -> pictureName = "meow03.png";
						case 4 -> pictureName = "meow04.png";
						case 5 -> pictureName = "meow05.png";
						case 6 -> pictureName = "meow06.png";
						case 7 -> pictureName = "meow07.png";
						case 8 -> pictureName = "meow08.png";
						case 9 -> pictureName = "meow09.png";
						case 10 -> pictureName = "meow10.png";
						case 11 -> pictureName = "meow11.png";
						case 12 -> pictureName = "meow12.png";
						case 13 -> pictureName = "meow13.png";
						case 14 -> pictureName = "meow14.png";
						case 15 -> pictureName = "meow15.png";
						case 16 -> pictureName = "meow16.png";
						case 17 -> pictureName = "meow17.png";
						case 18 -> pictureName = "meow18.png";
						case 19 -> pictureName = "meow19.png";
						case 20 -> pictureName = "meow20.png";
						case 21 -> pictureName = "meow21.png";
						case 22 -> pictureName = "meow22.png";
						case 23 -> pictureName = "meow23.png";
						case 24 -> pictureName = "meow24.png";
						case 25 -> pictureName = "meow25.png";
						case 26 -> pictureName = "meow26.png";
						case 27 -> pictureName = "meow27.png";
						case 28 -> pictureName = "meow28.png";
						case 29 -> pictureName = "meow29.png";
						case 30 -> pictureName = "meow30.png";
						case 31 -> pictureName = "meow31.png";
						case 32 -> pictureName = "meow32.png";
						case 33 -> pictureName = "meow33.png";
						case 34 -> pictureName = "meow34.png";
						case 35 -> pictureName = "meow35.png";
						case 36 -> pictureName = "meow36.png";
						case 37 -> pictureName = "meow37.png";
						case 38 -> pictureName = "meow38.png";
						case 39 -> pictureName = "meow39.png";
						case 40 -> pictureName = "meow40.png";
						case 41 -> pictureName = "meow41.png";
						case 42 -> pictureName = "meow42.png";
						case 43 -> pictureName = "meow43.png";
						case 44 -> pictureName = "meow44.png";
						case 45 -> pictureName = "meow45.png";
						case 46 -> pictureName = "meow46.png";
						case 47 -> pictureName = "meow47.png";
						case 48 -> pictureName = "meow48.png";
						case 49 -> pictureName = "meow49.png";
						case 50 -> pictureName = "meow50.png";
						case 51 -> pictureName = "meow51.png";
						case 52 -> pictureName = "meow52.png";
						case 53 -> pictureName = "meow53.png";
						case 54 -> pictureName = "meow54.png";
						case 55 -> pictureName = "meow55.png";
						case 56 -> pictureName = "meow56.png";
						case 57 -> pictureName = "meow57.png";
						case 58 -> pictureName = "meow58.png";
						case 59 -> pictureName = "meow59.png";
						case 60 -> pictureName = "meow60.png";
						case 61 -> pictureName = "meow61.png";
						case 62 -> pictureName = "meow62.png";
						case 63 -> pictureName = "meow63.png";
						case 64 -> pictureName = "meow64.png";
						case 65 -> pictureName = "meow65.png";
						case 66 -> pictureName = "meow66.png";
						case 67 -> pictureName = "meow67.png";
						case 68 -> pictureName = "meow68.png";
						case 69 -> pictureName = "meow69.png";
						case 70 -> pictureName = "meow70.png";
						case 71 -> pictureName = "meow71.png";
						case 72 -> pictureName = "meow72.png";
						case 73 -> pictureName = "meow73.png";
						case 74 -> pictureName = "meow74.png";
					}
					file = new File(path+""+pictureName);
					e.getChannel().sendFile(file, "meow.png").queue();
				}
				else {
					e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.IMAGE_ERR)).queue();
					Hashes.clearTempCache("meowDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
				}
			}
		}
		else {
			e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.COOLDOWN)).queue();
		}
	}
}
