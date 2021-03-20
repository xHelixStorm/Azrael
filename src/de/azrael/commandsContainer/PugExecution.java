package de.azrael.commandsContainer;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import de.azrael.constructors.Cache;
import de.azrael.core.Hashes;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.IniFileReader;
import de.azrael.preparedMessages.PugUsage;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Extension of the pug command
 * @author xHelixStorm
 *
 */

public class PugExecution {
	public static void Execute(GuildMessageReceivedEvent e, String [] _variable, String _path, long channel_id) throws IOException{
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.PINK).setThumbnail(IniFileReader.getPugThumbnail());
		var variable = _variable;
		String path = _path;
		String pictureName = "";
		var cache = Hashes.getTempCache("pugDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
		
		if(cache == null || cache.getExpiration() - System.currentTimeMillis() <= 0) {			
			if(variable.length == 0) {
				e.getChannel().sendMessage(messageBuild.setDescription(PugUsage.getPugInfos(e)).build()).queue();
			}
			else {
				Hashes.addTempCache("pugDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId(), new Cache(30000));
				File file;
				
				if(variable[0].equalsIgnoreCase("pug")) {
					file = new File(path+"pug01.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("greet")) {
					file = new File(path+"pug02.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("showoff")) {
					file = new File(path+"pug03.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("annoyed")) {
					file = new File(path+"pug04.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("wink")) {
					file = new File(path+"pug05.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("sleep")) {
					file = new File(path+"pug06.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("stress")) {
					file = new File(path+"pug07.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("stormtrooper")) {
					file = new File(path+"pug08.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("cry")) {
					file = new File(path+"pug09.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("unconscious")) {
					file = new File(path+"pug10.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("puzzled")) {
					file = new File(path+"pug11.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("rage")) {
					file = new File(path+"pug12.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("taco")) {
					file = new File(path+"pug13.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("burrito")) {
					file = new File(path+"pug14.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("bread")) {
					file = new File(path+"pug15.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("donut")) {
					file = new File(path+"pug16.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("pizza")) {
					file = new File(path+"pug17.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("fries")) {
					file = new File(path+"pug18.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("potato")) {
					file = new File(path+"pug19.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("lollipop")) {
					file = new File(path+"pug20.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("salmon")) {
					file = new File(path+"pug21.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("onigiri")) {
					file = new File(path+"pug22.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("chocolate")) {
					file = new File(path+"pug23.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("bacon")) {
					file = new File(path+"pug24.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("cottoncandy")) {
					file = new File(path+"pug25.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("hotdog")) {
					file = new File(path+"pug26.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("cookie")) {
					file = new File(path+"pug27.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("tart")) {
					file = new File(path+"pug28.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("ironman")) {
					file = new File(path+"pug29.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("captainamerica")) {
					file = new File(path+"pug30.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("thor")) {
					file = new File(path+"pug31.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("batman")) {
					file = new File(path+"pug32.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("catwoman")) {
					file = new File(path+"pug39.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("loki")) {
					file = new File(path+"pug33.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("storm")) {
					file = new File(path+"pug34.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("wolverine")) {
					file = new File(path+"pug35.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("wonderwoman")) {
					file = new File(path+"pug36.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("spiderman")) {
					file = new File(path+"pug37.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("deadpool")) {
					file = new File(path+"pug38.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("flash")) {
					file = new File(path+"pug40.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("superman")) {
					file = new File(path+"pug41.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("star-lord")) {
					file = new File(path+"pug42.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("bastion")) {
					file = new File(path+"pug43.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("dva")) {
					file = new File(path+"pug44.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("genji")) {
					file = new File(path+"pug45.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("hanzo")) {
					file = new File(path+"pug46.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("lucio")) {
					file = new File(path+"pug47.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("mccree")) {
					file = new File(path+"pug48.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("mei")) {
					file = new File(path+"pug49.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("mercy")) {
					file = new File(path+"pug50.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("reaper")) {
					file = new File(path+"pug51.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("soldier76")) {
					file = new File(path+"pug52.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("symmetra")) {
					file = new File(path+"pug53.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("tracer")) {
					file = new File(path+"pug54.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("widowmaker")) {
					file = new File(path+"pug55.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("winston")) {
					file = new File(path+"pug56.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("zarya")) {
					file = new File(path+"pug57.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("zenyatta")) {
					file = new File(path+"pug58.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("fashion")) {
					file = new File(path+"pug59.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("fat")) {
					file = new File(path+"pug60.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("holiday")) {
					file = new File(path+"pug61.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("loveletter")) {
					file = new File(path+"pug62.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("pride")) {
					file = new File(path+"pug63.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("pugmountain")) {
					file = new File(path+"pug64.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("rainbow")) {
					file = new File(path+"pug65.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("rein")) {
					file = new File(path+"pug66.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("santa")) {
					file = new File(path+"pug67.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("sleepy")) {
					file = new File(path+"pug68.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("smile")) {
					file = new File(path+"pug69.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("snowman")) {
					file = new File(path+"pug70.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("twitch")) {
					file = new File(path+"pug71.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("wub")) {
					file = new File(path+"pug72.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("bunny")) {
					file = new File(path+"pug73.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("bunnyherd")) {
					file = new File(path+"pug74.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("pockie")) {
					file = new File(path+"pug75.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("ramune")) {
					file = new File(path+"pug76.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("chocolatemilk")) {
					file = new File(path+"pug77.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("chocoswiss")) {
					file = new File(path+"pug78.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("coffee")) {
					file = new File(path+"pug79.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("coke")) {
					file = new File(path+"pug80.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("corn")) {
					file = new File(path+"pug81.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("cream")) {
					file = new File(path+"pug82.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("crisps")) {
					file = new File(path+"pug83.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("cupcake")) {
					file = new File(path+"pug84.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("fruittart")) {
					file = new File(path+"pug85.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("icecream")) {
					file = new File(path+"pug86.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("jelly")) {
					file = new File(path+"pug87.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("meatball")) {
					file = new File(path+"pug88.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("milk")) {
					file = new File(path+"pug89.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("nacho")) {
					file = new File(path+"pug90.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("pancake")) {
					file = new File(path+"pug91.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("pootloops")) {
					file = new File(path+"pug92.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("ramen")) {
					file = new File(path+"pug93.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("smores")) {
					file = new File(path+"pug94.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("starbucks")) {
					file = new File(path+"pug95.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("takiyaki")) {
					file = new File(path+"pug96.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("takoyaki")) {
					file = new File(path+"pug97.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("toastie")) {
					file = new File(path+"pug98.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("wizard")) {
					file = new File(path+"pug99.png");
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("random-emoji")) {
					int randomNumber = (int)(Math.random()*31)+1;
					switch(randomNumber) {
						case 1 -> pictureName = "pug01.png";
						case 2 -> pictureName = "pug02.png";
						case 3 -> pictureName = "pug03.png";
						case 4 -> pictureName = "pug04.png";
						case 5 -> pictureName = "pug05.png";
						case 6 -> pictureName = "pug06.png";
						case 7 -> pictureName = "pug07.png";
						case 8 -> pictureName = "pug08.png";
						case 9 -> pictureName = "pug09.png";
						case 10 -> pictureName = "pug10.png";
						case 11 -> pictureName = "pug11.png";
						case 12 -> pictureName = "pug12.png";
						case 13 -> pictureName = "pug59.png";
						case 14 -> pictureName = "pug60.png";
						case 15 -> pictureName = "pug61.png";
						case 16 -> pictureName = "pug62.png";
						case 17 -> pictureName = "pug63.png";
						case 18 -> pictureName = "pug64.png";
						case 19 -> pictureName = "pug65.png";
						case 20 -> pictureName = "pug66.png";
						case 21 -> pictureName = "pug67.png";
						case 22 -> pictureName = "pug68.png";
						case 23 -> pictureName = "pug69.png";
						case 24 -> pictureName = "pug70.png";
						case 25 -> pictureName = "pug71.png";
						case 26 -> pictureName = "pug72.png";
						case 27 -> pictureName = "pug73.png";
						case 28 -> pictureName = "pug74.png";
						case 29 -> pictureName = "pug75.png";
						case 30 -> pictureName = "pug76.png";
						case 31 -> pictureName = "pug99.png";
					}
					file = new File(path+""+pictureName);
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("random-food")) {
					int randomNumber = (int)(Math.random()*39)+1;
					switch(randomNumber) {
						case 1 -> pictureName = "pug13.png";
						case 2 -> pictureName = "pug14.png";
						case 3 -> pictureName = "pug15.png";
						case 4 -> pictureName = "pug16.png";
						case 5 -> pictureName = "pug17.png";
						case 6 -> pictureName = "pug18.png";
						case 7 -> pictureName = "pug19.png";
						case 8 -> pictureName = "pug20.png";
						case 9 -> pictureName = "pug21.png";
						case 10 -> pictureName = "pug22.png";
						case 11 -> pictureName = "pug23.png";
						case 12 -> pictureName = "pug24.png";
						case 13 -> pictureName = "pug25.png";
						case 14 -> pictureName = "pug26.png";
						case 15 -> pictureName = "pug27.png";
						case 17 -> pictureName = "pug28.png";
						case 18 -> pictureName = "pug77.png";
						case 19 -> pictureName = "pug78.png";
						case 20 -> pictureName = "pug79.png";
						case 21 -> pictureName = "pug80.png";
						case 22 -> pictureName = "pug81.png";
						case 23 -> pictureName = "pug82.png";
						case 24 -> pictureName = "pug83.png";
						case 25 -> pictureName = "pug84.png";
						case 26 -> pictureName = "pug85.png";
						case 27 -> pictureName = "pug86.png";
						case 28 -> pictureName = "pug87.png";
						case 29 -> pictureName = "pug88.png";
						case 30 -> pictureName = "pug89.png";
						case 31 -> pictureName = "pug90.png";
						case 32 -> pictureName = "pug91.png";
						case 33 -> pictureName = "pug92.png";
						case 34 -> pictureName = "pug93.png";
						case 35 -> pictureName = "pug94.png";
						case 36 -> pictureName = "pug95.png";
						case 37 -> pictureName = "pug96.png";
						case 38 -> pictureName = "pug97.png";
						case 39 -> pictureName = "pug98.png";
					}
					file = new File(path+""+pictureName);
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("random-marvel")) {
					int randomNumber = (int)(Math.random()*14)+1;
					switch(randomNumber) {
						case 1 -> pictureName = "pug29.png";
						case 2 -> pictureName = "pug30.png";
						case 3 -> pictureName = "pug31.png";
						case 4 -> pictureName = "pug32.png";
						case 5 -> pictureName = "pug33.png";
						case 6 -> pictureName = "pug34.png";
						case 7 -> pictureName = "pug35.png";
						case 8 -> pictureName = "pug36.png";
						case 9 -> pictureName = "pug37.png";
						case 10 -> pictureName = "pug38.png";
						case 11 -> pictureName = "pug39.png";
						case 12 -> pictureName = "pug40.png";
						case 13 -> pictureName = "pug41.png";
						case 14 -> pictureName = "pug42.png";
					}
					file = new File(path+""+pictureName);
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("random-overwatch")) {
					int randomNumber = (int)(Math.random()*16)+1;
					switch(randomNumber) {
						case 1 -> pictureName = "pug43.png";
						case 2 -> pictureName = "pug44.png";
						case 3 -> pictureName = "pug45.png";
						case 4 -> pictureName = "pug46.png";
						case 5 -> pictureName = "pug47.png";
						case 6 -> pictureName = "pug48.png";
						case 7 -> pictureName = "pug49.png";
						case 8 -> pictureName = "pug50.png";
						case 9 -> pictureName = "pug51.png";
						case 10 -> pictureName = "pug52.png";
						case 11 -> pictureName = "pug53.png";
						case 12 -> pictureName = "pug54.png";
						case 13 -> pictureName = "pug55.png";
						case 14 -> pictureName = "pug56.png";
						case 15 -> pictureName = "pug57.png";
						case 16 -> pictureName = "pug58.png";
					}
					file = new File(path+""+pictureName);
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				
				else if(variable[0].equalsIgnoreCase("random-pug")) {
					int randomNumber = (int)(Math.random()*99)+1;
					switch(randomNumber) {
						case 1 -> pictureName = "pug01.png";
						case 2 -> pictureName = "pug02.png";
						case 3 -> pictureName = "pug03.png";
						case 4 -> pictureName = "pug04.png";
						case 5 -> pictureName = "pug05.png";
						case 6 -> pictureName = "pug06.png";
						case 7 -> pictureName = "pug07.png";
						case 8 -> pictureName = "pug08.png";
						case 9 -> pictureName = "pug09.png";
						case 10 -> pictureName = "pug10.png";
						case 11 -> pictureName = "pug11.png";
						case 12 -> pictureName = "pug12.png";
						case 13 -> pictureName = "pug13.png";
						case 14 -> pictureName = "pug14.png";
						case 15 -> pictureName = "pug15.png";
						case 16 -> pictureName = "pug16.png";
						case 17 -> pictureName = "pug17.png";
						case 18 -> pictureName = "pug18.png";
						case 19 -> pictureName = "pug19.png";
						case 20 -> pictureName = "pug20.png";
						case 21 -> pictureName = "pug21.png";
						case 22 -> pictureName = "pug22.png";
						case 23 -> pictureName = "pug23.png";
						case 24 -> pictureName = "pug24.png";
						case 25 -> pictureName = "pug25.png";
						case 26 -> pictureName = "pug26.png";
						case 27 -> pictureName = "pug27.png";
						case 28 -> pictureName = "pug28.png";
						case 29 -> pictureName = "pug29.png";
						case 30 -> pictureName = "pug30.png";
						case 31 -> pictureName = "pug31.png";
						case 32 -> pictureName = "pug32.png";
						case 33 -> pictureName = "pug33.png";
						case 34 -> pictureName = "pug34.png";
						case 35 -> pictureName = "pug35.png";
						case 36 -> pictureName = "pug36.png";
						case 37 -> pictureName = "pug37.png";
						case 38 -> pictureName = "pug38.png";
						case 39 -> pictureName = "pug39.png";
						case 40 -> pictureName = "pug40.png";
						case 41 -> pictureName = "pug41.png";
						case 42 -> pictureName = "pug42.png";
						case 43 -> pictureName = "pug43.png";
						case 44 -> pictureName = "pug44.png";
						case 45 -> pictureName = "pug45.png";
						case 46 -> pictureName = "pug46.png";
						case 47 -> pictureName = "pug47.png";
						case 48 -> pictureName = "pug48.png";
						case 49 -> pictureName = "pug49.png";
						case 50 -> pictureName = "pug50.png";
						case 51 -> pictureName = "pug51.png";
						case 52 -> pictureName = "pug52.png";
						case 53 -> pictureName = "pug53.png";
						case 54 -> pictureName = "pug54.png";
						case 55 -> pictureName = "pug55.png";
						case 56 -> pictureName = "pug56.png";
						case 57 -> pictureName = "pug57.png";
						case 58 -> pictureName = "pug58.png";
						case 59 -> pictureName = "pug59.png";
						case 60 -> pictureName = "pug60.png";
						case 61 -> pictureName = "pug61.png";
						case 62 -> pictureName = "pug62.png";
						case 63 -> pictureName = "pug63.png";
						case 64 -> pictureName = "pug64.png";
						case 65 -> pictureName = "pug65.png";
						case 66 -> pictureName = "pug66.png";
						case 67 -> pictureName = "pug67.png";
						case 68 -> pictureName = "pug68.png";
						case 69 -> pictureName = "pug69.png";
						case 70 -> pictureName = "pug70.png";
						case 71 -> pictureName = "pug71.png";
						case 72 -> pictureName = "pug72.png";
						case 73 -> pictureName = "pug73.png";
						case 74 -> pictureName = "pug74.png";
						case 75 -> pictureName = "pug75.png";
						case 76 -> pictureName = "pug76.png";
						case 77 -> pictureName = "pug77.png";
						case 78 -> pictureName = "pug78.png";
						case 79 -> pictureName = "pug79.png";
						case 80 -> pictureName = "pug80.png";
						case 81 -> pictureName = "pug81.png";
						case 82 -> pictureName = "pug82.png";
						case 83 -> pictureName = "pug83.png";
						case 84 -> pictureName = "pug84.png";
						case 85 -> pictureName = "pug85.png";
						case 86 -> pictureName = "pug86.png";
						case 87 -> pictureName = "pug87.png";
						case 88 -> pictureName = "pug88.png";
						case 89 -> pictureName = "pug89.png";
						case 90 -> pictureName = "pug90.png";
						case 91 -> pictureName = "pug91.png";
						case 92 -> pictureName = "pug92.png";
						case 93 -> pictureName = "pug93.png";
						case 94 -> pictureName = "pug94.png";
						case 95 -> pictureName = "pug95.png";
						case 96 -> pictureName = "pug96.png";
						case 97 -> pictureName = "pug97.png";
						case 98 -> pictureName = "pug98.png";
						case 99 -> pictureName = "pug99.png";
					}
					file = new File(path+""+pictureName);
					e.getChannel().sendFile(file, "pug.png").queue();
				}
				else {
					e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.IMAGE_ERR)).queue();
					Hashes.clearTempCache("pugDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
				}
			}
		}
		else {
			e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.COOLDOWN)).queue();
		}
	}
}
