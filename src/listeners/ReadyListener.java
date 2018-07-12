package listeners;

import java.awt.Color;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import preparedMessages.PatchNotes;
import preparedMessages.PublicPatchNotes;
import rankingSystem.DoubleExperienceOff;
import rankingSystem.DoubleExperienceStart;
import sql.SqlConnect;
import threads.BotStartAssign;
import threads.RoleExtend;
import util.STATIC;

public class ReadyListener extends ListenerAdapter{
	private static String privatePatchNotes = PatchNotes.patchNotes();
	private static String publicPatchNotes = PublicPatchNotes.publicPatchNotes();
	
	@Override
	public void onReady(ReadyEvent e){
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.MAGENTA);
		System.out.println();
		System.out.println("Azrael Version: "+STATIC.getVersion_New()+"\nAll credits to [GM]Heiliger");
		
		String allowPatchNotes = IniFileReader.getAllowPatchNotes();
		String allowPublicPatchNotes = IniFileReader.getAllowPublicPatchNotes();
		
		System.out.println();
		
		if(allowPatchNotes.equals("true")){System.out.println("private patch notes: enabled");}
		else{System.out.println("private patch notes: disabled");}
		if(allowPublicPatchNotes.equals("true")){System.out.println("public patch notes:  enabled");}
		else{System.out.println("public patch notes:  disabled");}
		
		String out = "\nThis Bot is running on following servers: \n";
		for(Guild g : e.getJDA().getGuilds()){
			out += g.getName() + " (" + g.getId() + ") \n";
		}
		System.out.println(out);
		FileSetting.createTemp();

		FileSetting.createFile("./files/reboot", "0");
		for(Guild g : e.getJDA().getGuilds()){
			long guild_id = g.getIdLong();
			SqlConnect.SQLgetChannelID(guild_id, "log");
			long channel_id = SqlConnect.getChannelID();
			if(channel_id != 0){e.getJDA().getGuildById(guild_id).getTextChannelById(channel_id).sendMessage("Bot is now operational!").queue();}
		}
		
		if(!(STATIC.getVersion_Old().contains(STATIC.getVersion_New())) && allowPatchNotes.equals("true")){
			for(Guild g : e.getJDA().getGuilds()){
				long guild_id = g.getIdLong();
				SqlConnect.SQLgetChannelID(guild_id, "log");
				long channel_id = SqlConnect.getChannelID();
				
				if(channel_id != 0){
					FileSetting.createFile("./files/version", STATIC.getVersion_New());
					e.getJDA().getGuildById(guild_id).getTextChannelById(channel_id).sendMessage(
							messageBuild.setDescription(privatePatchNotes).build()).queue();
					
					SqlConnect.SQLgetChannelID(guild_id, "bot");
					long channel_id2 = SqlConnect.getChannelID();
					
					if(allowPublicPatchNotes.equals("true")){
						if(channel_id2 != 0){
							e.getJDA().getGuildById(guild_id).getTextChannelById(channel_id2).sendMessage(
									messageBuild.setDescription(publicPatchNotes).build()).queue();
						}
					}
				}
			}
		}
		
		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.execute(new BotStartAssign(e));
		for(Guild g : e.getJDA().getGuilds()){
			executor.execute(new RoleExtend(e, g.getIdLong()));
			for(TextChannel tc : g.getTextChannels()){
				SqlConnect.SQLInsertChannels(tc.getIdLong(), tc.getName());
			}
		}
		
		DoubleExperienceStart.runTask(e);
		DoubleExperienceOff.runTask();
		
		SqlConnect.clearAllVariables();
		executor.shutdown();
		
		/*for(Guild g : e.getJDA().getGuilds()){
			SqlConnect.SQLgetChannelID(g.getIdLong(), "sta");
			long channel_id = SqlConnect.getChannelID();
			
			if(channel_id != 0){e.getJDA().getGuildById(g.getIdLong()).getTextChannelById(channel_id).sendMessage("H!gameStatus").queue();}
		}*/
	}
}
