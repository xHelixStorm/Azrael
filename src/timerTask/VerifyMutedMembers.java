package timerTask;

import java.awt.Color;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.ResumedEvent;
import sql.Azrael;
import sql.DiscordRoles;
import util.STATIC;

public class VerifyMutedMembers extends TimerTask {
	private ReadyEvent event;
	private ReconnectedEvent event2;
	private ResumedEvent event3;
	
	public VerifyMutedMembers(ReadyEvent _e, ReconnectedEvent _e2, ResumedEvent _e3) {
		this.event = _e;
		this.event2 = _e2;
		this.event3 = _e3;
	}
	
	@Override
	public void run() {
		var e = (event != null ? event : (event2 != null ? event2 : event3));
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
				if(channels != null && channels.size() > 0 && count > 0) {
					var log_channel = channels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
					if(log_channel != null) guild.getTextChannelById(log_channel.getChannel_ID()).sendMessage(
							new EmbedBuilder().setColor(Color.RED).setTitle("Users unmuted!")
							.setDescription(count+" users were stuck in a muted limbo on reconnect! Users unmuted!").build()
					).queue();
				}
			}
		}
	}
	
	public static void delayFirstStart(ReadyEvent e) {
		try {
			Thread.sleep(3600000);
			runTask(e, null, null);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void runTask(ReadyEvent e, ReconnectedEvent e2, ResumedEvent e3) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		Timer time = new Timer("VerifyMutedMembers");
		STATIC.addTimer(time);
		time.schedule(new VerifyMutedMembers(e, e2, e3), calendar.getTime(), TimeUnit.HOURS.toMillis(1));
	}
}
