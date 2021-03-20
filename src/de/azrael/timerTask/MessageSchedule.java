package de.azrael.timerTask;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import de.azrael.constructors.Schedule;
import de.azrael.core.Hashes;
import de.azrael.enums.Channel;
import de.azrael.enums.Translation;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class MessageSchedule extends TimerTask {
	private Guild guild;
	private Schedule schedule;
	
	public MessageSchedule(Guild _guild, Schedule _schedule) {
		this.guild = _guild;
		this.schedule = _schedule;
	}
	
	@Override
	public void run() {
		final int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		switch(today) {
			case 1 -> {
				if(!schedule.isSunday())
					return;
			}
			case 2 -> {
				if(!schedule.isMonday())
					return;
			}
			case 3 -> {
				if(!schedule.isTuesday())
					return;
			}
			case 4 -> {
				if(!schedule.isWednesday())
					return;
			}
			case 5 -> {
				if(!schedule.isThursday())
					return;
			}
			case 6 -> {
				if(!schedule.isFriday())
					return;
			}
			case 7 -> {
				if(!schedule.isSaturday())
					return;
			}
		}
		
		final TextChannel textChannel = guild.getTextChannelById(schedule.getChannel_id());
		if(textChannel != null) {
			if(guild.getSelfMember().hasPermission(textChannel, Permission.MESSAGE_WRITE) || STATIC.setPermissions(null, textChannel, EnumSet.of(Permission.MESSAGE_WRITE))) {
				textChannel.sendMessage(schedule.getMessage()).queue();
			}
			else {
				STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_ERROR)), STATIC.getTranslation2(guild, Translation.SCHEDULE_MESSAGE).replace("{}", ""+schedule.getChannel_id()), Channel.LOG.getType());
			}
		}
		else {
			STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_ERROR)), STATIC.getTranslation2(guild, Translation.SCHEDULE_MESSAGE2).replace("{}", ""+schedule.getChannel_id()), Channel.LOG.getType());
		}
	}

	public static void runTask(Guild guild, Schedule schedule) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, schedule.getTime()/10000);
		calendar.set(Calendar.MINUTE, (schedule.getTime()-((schedule.getTime()/10000)*10000))/100);
		calendar.set(Calendar.SECOND, schedule.getTime()-((schedule.getTime()/100)*100));
		if(calendar.getTimeInMillis() - System.currentTimeMillis() < 0) {
			calendar.add(Calendar.DATE, 1);
		}
		
		Timer time = new Timer();
		var timers = Hashes.getSchedules(guild.getIdLong());
		if(timers == null || timers.size() == 0) {
			timers = new ArrayList<Timer>();
		}
		timers.add(time);
		Hashes.addSchedule(guild.getIdLong(), timers);
		time.schedule(new MessageSchedule(guild, schedule), calendar.getTime(), TimeUnit.DAYS.toMillis(1));
	}
}
