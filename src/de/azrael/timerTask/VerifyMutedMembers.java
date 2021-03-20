package de.azrael.timerTask;

import java.awt.Color;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.enums.Channel;
import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.sql.DiscordRoles;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.ResumedEvent;

public class VerifyMutedMembers extends TimerTask {
	private final static Logger logger = LoggerFactory.getLogger(VerifyMutedMembers.class);
	
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
			if(delay) {
				Thread.sleep(600000);
				delay = false;
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		var e = (event != null ? event : (event2 != null ? event2 : event3));
		for(var guild : e.getJDA().getGuilds()) {
			final boolean ROLE_MANAGEMENT = guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES);
			var mute_role = DiscordRoles.SQLgetRoles(guild.getIdLong()).parallelStream().filter(f -> f.getCategory_ABV().equals("mute")).findAny().orElse(null);
			if(mute_role != null) {
				List<Member> mutedMembers = guild.getMembersWithRoles(guild.getRoleById(mute_role.getRole_ID()));
				var count = 0;
				for(var member : mutedMembers) {
					var data = Azrael.SQLgetData(member.getUser().getIdLong(), guild.getIdLong());
					if(data.getUnmute() != null && System.currentTimeMillis() > data.getUnmute().getTime()) {
						if(ROLE_MANAGEMENT) {
							guild.removeRoleFromMember(member, guild.getRoleById(mute_role.getRole_ID())).queue();
							count++;
						}
						else {
							STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(guild, Translation.MISSING_PERMISSION)+Permission.MANAGE_ROLES.getName(), Channel.LOG.getType());
							logger.error("MANAGE_ROLES permission required to remove the mute role from a user in guild {}", guild.getId());
						}
					}
				}
				var channels = Azrael.SQLgetChannels(guild.getIdLong());
				if(channels != null && channels.size() > 0 && count > 0) {
					STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_UNMUTED)), count+STATIC.getTranslation2(guild, Translation.UNMUTE_LIMBO), Channel.LOG.getType());
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
