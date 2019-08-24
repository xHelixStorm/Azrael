package listeners;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.ResumedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rankingSystem.DoubleExperienceOff;
import rankingSystem.DoubleExperienceStart;
import sql.Azrael;
import sql.DiscordRoles;
import util.STATIC;

public class ResumedListener extends ListenerAdapter{
	
	@Override
	public void onResume(ResumedEvent e) {
		if(IniFileReader.getDoubleExpEnabled()) {
			STATIC.killAllTimers();
			DoubleExperienceStart.runTask(null, null, e, null);
			DoubleExperienceOff.runTask();
		}
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(() -> {
			for(var guild : e.getJDA().getGuilds()) {
				var mute_role = DiscordRoles.SQLgetRole(guild.getIdLong(), "mut");
				if(mute_role != 0) {
					List<Member> mutedMembers = guild.getMembersWithRoles(guild.getRoleById(mute_role));
					var count = 0;
					for(var member : mutedMembers) {
						var data = Azrael.SQLgetData(member.getUser().getIdLong(), guild.getIdLong());
						if(data.getUnmute() != null && System.currentTimeMillis() > data.getUnmute().getTime()) {
							guild.removeRoleFromMember(member, guild.getRoleById(mute_role)).queue();
							count++;
						}
					}
					var channels = Azrael.SQLgetChannels(guild.getIdLong());
					if(channels != null && channels.size() > 0) {
						var log_channel = channels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
						if(log_channel != null) guild.getTextChannelById(log_channel.getChannel_ID()).sendMessage(
								new EmbedBuilder().setColor(Color.RED).setTitle("Users unmuted!")
								.setDescription(count+" users were stuck in a muted limbo on reconnect! Users unmuted!").build()
						).queue();
					}
				}
			}
		});
	}
}
