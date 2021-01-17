package commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import core.Hashes;
import enums.Translation;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import timerTask.MessageSchedule;
import util.STATIC;

public class ScheduleExecution {
	private static final Logger logger = LoggerFactory.getLogger(ScheduleExecution.class);
	
	public static void display(GuildMessageReceivedEvent e, Cache cache) {
		final var schedules = Azrael.SQLgetScheduledMessages(e.getGuild().getIdLong());
		if(schedules != null && schedules.size() > 0) {
			StringBuilder out = new StringBuilder();
			final String enabled = STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_ENABLED);
			final String disabled = STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_DISABLED);
			int count = 1;
			for(final var schedule : schedules) {
				String time = formatTime(schedule.getTime());
				TextChannel textChannel = e.getGuild().getTextChannelById(schedule.getChannel_id());
				out.append("**"+(count++)+"**: "+STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_TIME)+": "+time+" "+STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_CHANNEL)+": "+(textChannel != null ? textChannel.getName()+" ("+textChannel.getId()+")" : STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE))+"\n"
					+ "**MO**: "+(schedule.isMonday() ? enabled : disabled)
					+ " **TU**: "+(schedule.isTuesday() ? enabled : disabled)
					+ " **WE**: "+(schedule.isWednesday() ? enabled : disabled)
					+ " **TH**: "+(schedule.isThursday() ? enabled : disabled)
					+ " **FR**: "+(schedule.isFriday() ? enabled : disabled)
					+ " **SA**: "+(schedule.isSaturday() ? enabled : disabled)
					+ " **SU**: "+(schedule.isSunday() ? enabled : disabled)+"\n\n");
			}
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_DISPLAY)+out.toString()).build()).queue();
			Hashes.addTempCache("schedule_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000).updateDescription("display").setObject(schedules));
		}
		else if (schedules != null) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_ERR)).build()).queue();
			Hashes.clearTempCache("schedule_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Schedules messages couldn't be retrieved in guild {}", e.getGuild().getId());
			Hashes.clearTempCache("schedule_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		}
	}
	
	public static void displayMessage(GuildMessageReceivedEvent e, Cache cache, String message) {
		if(message.replaceAll("[0-9]*", "").length() == 0) {
			@SuppressWarnings("unchecked")
			final var schedules = (ArrayList<constructors.Schedule>)cache.getObject();
			final int index = Integer.parseInt(message)-1;
			final var schedule = schedules.get(index);
			e.getChannel().sendMessage(schedule.getMessage().replaceAll("(@here)", "`@here`").replaceAll("(@everyone)", "`@everyone`")).queue();
			Hashes.addTempCache("schedule_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
		}
		
	}
	
	public static void create(GuildMessageReceivedEvent e, Cache cache) {
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_CREATE)).build()).queue();
		Hashes.addTempCache("schedule_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000).updateDescription("create"));
	}
	
	public static void createMessage(GuildMessageReceivedEvent e, Cache cache, String message) {
		constructors.Schedule schedule = new constructors.Schedule();
		schedule.setMessage(message);
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_CREATE2)).build()).queue();
		Hashes.addTempCache("schedule_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000).updateDescription2("channel").setObject(schedule));
	}
	
	public static void createChannel(GuildMessageReceivedEvent e, Cache cache, String message) {
		constructors.Schedule schedule = (constructors.Schedule)cache.getObject();
		message = message.replaceAll("[<>#]", "");
		if(message.replaceAll("[0-9]*", "").length() == 0) {
			TextChannel textChannel = e.getGuild().getTextChannelById(message);
			if(textChannel != null) {
				if(e.getGuild().getSelfMember().hasPermission(textChannel, Permission.MESSAGE_WRITE) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.MESSAGE_WRITE))) {
					schedule.setChannelId(textChannel.getIdLong());
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_CREATE3).replace("{}", IniFileReader.getTimezone())).build()).queue();
					Hashes.addTempCache("schedule_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000).updateDescription2("time").setObject(schedule));
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_ERR3)).build()).queue();
					Hashes.addTempCache("schedule_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_ERR2)).build()).queue();
				Hashes.addTempCache("schedule_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			}
		}
	}
	
	public static void createTime(GuildMessageReceivedEvent e, Cache cache, String message) {
		constructors.Schedule schedule = (constructors.Schedule)cache.getObject();
		message = message.replaceAll("[:]", "");
		if(message.length() == 6 && message.replaceAll("[0-9]*", "").length() == 0) {
			schedule.setTime(Integer.parseInt(message));
			final String enabled = STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_ENABLED);
			final String disabled = STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_DISABLED);
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_CREATE4)+"\n\n"
				+ "**MO**: "+(schedule.isMonday() ? enabled : disabled)
				+ " **TU**: "+(schedule.isTuesday() ? enabled : disabled)
				+ " **WE**: "+(schedule.isWednesday() ? enabled : disabled)
				+ " **TH**: "+(schedule.isThursday() ? enabled : disabled)
				+ " **FR**: "+(schedule.isFriday() ? enabled : disabled)
				+ " **SA**: "+(schedule.isSaturday() ? enabled : disabled)
				+ " **SU**: "+(schedule.isSunday() ? enabled : disabled)).build()).queue();
			Hashes.addTempCache("schedule_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000).updateDescription2("days").setObject(schedule));
		}
	}
	
	public static void createDays(GuildMessageReceivedEvent e, Cache cache, String message) {
		constructors.Schedule schedule = (constructors.Schedule)cache.getObject();
		if(!message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_COMPLETE))) {
			switch(message.toLowerCase()) {
				case "mo" -> schedule.setMonday((schedule.isMonday() ? false : true));
				case "tu" -> schedule.setTuesday((schedule.isTuesday() ? false : true));
				case "we" -> schedule.setWednesday((schedule.isWednesday() ? false : true));
				case "th" -> schedule.setThursday((schedule.isThursday() ? false : true));
				case "fr" -> schedule.setFriday((schedule.isFriday() ? false : true));
				case "sa" -> schedule.setSaturday((schedule.isSaturday() ? false : true));
				case "su" -> schedule.setSunday((schedule.isSunday() ? false : true));
			}
			final String enabled = STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_ENABLED);
			final String disabled = STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_DISABLED);
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(
				  "**MO**: "+(schedule.isMonday() ? enabled : disabled)
				+ " **TU**: "+(schedule.isTuesday() ? enabled : disabled)
				+ " **WE**: "+(schedule.isWednesday() ? enabled : disabled)
				+ " **TH**: "+(schedule.isThursday() ? enabled : disabled)
				+ " **FR**: "+(schedule.isFriday() ? enabled : disabled)
				+ " **SA**: "+(schedule.isSaturday() ? enabled : disabled)
				+ " **SU**: "+(schedule.isSunday() ? enabled : disabled)
			).build()).queue();
			Hashes.addTempCache("schedule_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000).updateDescription2("days").setObject(schedule));
		}
		else {
			if(Azrael.SQLInsertScheduledMessage(e.getGuild().getIdLong(), schedule) > 0) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_CREATE5)).build()).queue();
				Hashes.clearTempCache("schedule_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Scheduled message couldn't be inserted in guild {}", e.getGuild().getId());
				Hashes.clearTempCache("schedule_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			}
		}
	}
	
	public static void remove(GuildMessageReceivedEvent e, Cache cache) {
		final var schedules = Azrael.SQLgetScheduledMessages(e.getGuild().getIdLong());
		if(schedules != null && schedules.size() > 0) {
			StringBuilder out = new StringBuilder();
			final String enabled = STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_ENABLED);
			final String disabled = STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_DISABLED);
			int count = 1;
			for(final var schedule : schedules) {
				String time = formatTime(schedule.getTime());
				TextChannel textChannel = e.getGuild().getTextChannelById(schedule.getChannel_id());
				out.append("**"+(count++)+"**: "+STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_TIME)+": "+time+" "+STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_CHANNEL)+": "+(textChannel != null ? textChannel.getName()+" ("+textChannel.getId()+")" : STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE))+"\n"
					+ "**MO**: "+(schedule.isMonday() ? enabled : disabled)
					+ " **TU**: "+(schedule.isTuesday() ? enabled : disabled)
					+ " **WE**: "+(schedule.isWednesday() ? enabled : disabled)
					+ " **TH**: "+(schedule.isThursday() ? enabled : disabled)
					+ " **FR**: "+(schedule.isFriday() ? enabled : disabled)
					+ " **SA**: "+(schedule.isSaturday() ? enabled : disabled)
					+ " **SU**: "+(schedule.isSunday() ? enabled : disabled)+"\n\n");
			}
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_REMOVE)+out.toString()).build()).queue();
			Hashes.addTempCache("schedule_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000).updateDescription("remove").setObject(schedules));
		}
		else if (schedules != null) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_ERR)).build()).queue();
			Hashes.clearTempCache("schedule_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Schedules messages couldn't be retrieved in guild {}", e.getGuild().getId());
			Hashes.clearTempCache("schedule_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		}
	}
	
	public static void removeMessage(GuildMessageReceivedEvent e, Cache cache, String message) {
		if(message.replaceAll("[0-9]*", "").length() == 0) {
			@SuppressWarnings("unchecked")
			final var schedules = (ArrayList<constructors.Schedule>)cache.getObject();
			final int index = Integer.parseInt(message)-1;
			final var schedule = schedules.get(index);
			if(Azrael.SQLDeleteScheduledMessageTask(e.getGuild().getIdLong(), schedule.getSchedule_id()) > 0) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_REMOVE2)).build()).queue();
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Scheduled message couldn't be deleted in guild {}", e.getGuild().getId());
			}
			Hashes.clearTempCache("schedule_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		}
	}
	
	private static String formatTime(int time) {
		String formattedTime = time+"";
		while(formattedTime.length() < 6) {
			formattedTime = "0"+formattedTime;
		}
		formattedTime = formattedTime.substring(0, 2)+":"+formattedTime.substring(2, 4)+":"+formattedTime.substring(4);
		return formattedTime;
	}
	
	public static void restartTimers(Guild guild) {
		final var timers = Hashes.getSchedules(guild.getIdLong());
		if(timers != null && timers.size() > 0) {
			for(final var timer : timers) {
				timer.cancel();
			}
			Hashes.removeSchedules(guild.getIdLong());
		}
		startTimers(guild);
	}
	
	public static void startTimers(Guild guild) {
		final var schedules = Azrael.SQLgetScheduledMessages(guild.getIdLong());
		if(schedules != null && schedules.size() > 0) {
			for(final var schedule : schedules) {
				MessageSchedule.runTask(guild, schedule);
			}
		}
		else if(schedules == null) {
			logger.error("Scheduled message couldn't be retrieved in guild {}", guild.getId());
		}
	}
}
