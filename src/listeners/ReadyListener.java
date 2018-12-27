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
import sql.RankingSystem;
import sql.Azrael;
import threads.BotStartAssign;
import threads.RoleExtend;
import timerTask.ClearHashes;
import util.STATIC;

public class ReadyListener extends ListenerAdapter{
	private static String privatePatchNotes = PatchNotes.patchNotes();
	private static String publicPatchNotes = PublicPatchNotes.publicPatchNotes();
	
	@Override
	public void onReady(ReadyEvent e){
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.MAGENTA).setThumbnail(e.getJDA().getSelfUser().getAvatarUrl()).setTitle("Here the latest patch notes!");
		System.out.println();
		System.out.println("Azrael Version: "+STATIC.getVersion_New()+"\nAll credits to xHelixStorm");
		
		boolean allowPatchNotes = IniFileReader.getAllowPatchNotes();
		boolean allowPublicPatchNotes = IniFileReader.getAllowPublicPatchNotes();
		
		System.out.println();
		
		if(allowPatchNotes){System.out.println("private patch notes: enabled");}
		else{System.out.println("private patch notes: disabled");}
		if(allowPublicPatchNotes){System.out.println("public patch notes:  enabled");}
		else{System.out.println("public patch notes:  disabled");}
		
		String out = "\nThis Bot is running on following servers: \n";
		for(Guild g : e.getJDA().getGuilds()){
			out += g.getName() + " (" + g.getId() + ") \n";
		}
		System.out.println(out);
		FileSetting.createTemp();

		FileSetting.createFile("./files/reboot.azr", "0");
		RankingSystem.SQLgetLevels();
		for(Guild g : e.getJDA().getGuilds()){
			long guild_id = g.getIdLong();
			RankingSystem.SQLgetGuild(guild_id);
			RankingSystem.SQLgetRoles(guild_id);
			Azrael.SQLgetChannelID(guild_id, "log");
			if(Azrael.getChannelID() != 0){e.getJDA().getGuildById(guild_id).getTextChannelById(Azrael.getChannelID()).sendMessage("Bot is now operational!").queue();}
		}
		Azrael.SQLInsertActionLog("BOT_BOOT", e.getJDA().getSelfUser().getIdLong(), 0, "Launched");
		
		if(!(STATIC.getVersion_Old().contains(STATIC.getVersion_New())) && allowPatchNotes){
			for(Guild g : e.getJDA().getGuilds()){
				long guild_id = g.getIdLong();
				Azrael.SQLgetChannelID(guild_id, "log");
				long channel_id = Azrael.getChannelID();
				
				if(channel_id != 0){
					FileSetting.createFile("./files/version.azr", STATIC.getVersion_New());
					e.getJDA().getGuildById(guild_id).getTextChannelById(channel_id).sendMessage(
							messageBuild.setDescription(privatePatchNotes).build()).queue();
					
					Azrael.SQLgetChannelID(guild_id, "bot");
					long channel_id2 = Azrael.getChannelID();
					
					if(allowPublicPatchNotes){
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
				Azrael.SQLInsertChannels(tc.getIdLong(), tc.getName());
			}
		}
		
		DoubleExperienceStart.runTask(e);
		DoubleExperienceOff.runTask();
		ClearHashes.runTask();
		
		Azrael.clearAllVariables();
		executor.shutdown();
	}
}
