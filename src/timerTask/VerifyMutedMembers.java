package timerTask;

import java.awt.Color;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import enums.Translation;
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
	private boolean delay;
	
	public VerifyMutedMembers(ReadyEvent _e, ReconnectedEvent _e2, ResumedEvent _e3, boolean _delay) {
		this.event = _e;
		this.event2 = _e2;
		this.event3 = _e3;
		this.delay = _delay;
	}
	
	@Override
	public void run() {
		try {
			if(delay)
				Thread.sleep(600000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		var e = (event != null ? event : (event2 != null ? event2 : event3));
		for(var guild : e.getJDA().getGuilds()) {
			var mute_role = DiscordRoles.SQLgetRoles(guild.getIdLong()).parallelStream().filter(f -> f.getCategory_ABV().equals("mute")).findAny().orElse(null);
			if(mute_role != null) {
				List<Member> mutedMembers = guild.getMembersWithRoles(guild.getRoleById(mute_role.getRole_ID()));
				var count = 0;
				for(var member : mutedMembers) {
					var data = Azrael.SQLgetData(member.getUser().getIdLong(), guild.getIdLong());
					if(data.getUnmute() != null && System.currentTimeMillis() > data.getUnmute().getTime()) {
						guild.removeRoleFromMember(member, guild.getRoleById(mute_role.getRole_ID())).queue();
						count++;
					}
				}
				var channels = Azrael.SQLgetChannels(guild.getIdLong());
				if(channels != null && channels.size() > 0 && count > 0) {
					var log_channel = channels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
					if(log_channel != null) guild.getTextChannelById(log_channel.getChannel_ID()).sendMessage(
							new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_UNMUTED))
							.setDescription(count+STATIC.getTranslation2(guild, Translation.UNMUTE_LIMBO)).build()
					).queue();
				}
			}
		}
	}
	
	public static void runTask(ReadyEvent e, ReconnectedEvent e2, ResumedEvent e3, boolean delay) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		Timer time = new Timer("VerifyMutedMembers");
		STATIC.addTimer(time);
		time.schedule(new VerifyMutedMembers(e, e2, e3, delay), calendar.getTime(), TimeUnit.HOURS.toMillis(1));
	}
}
