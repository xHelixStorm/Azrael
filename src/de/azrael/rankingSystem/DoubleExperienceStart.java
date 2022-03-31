package de.azrael.rankingSystem;

import java.io.File;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Cache;
import de.azrael.core.Hashes;
import de.azrael.enums.Channel;
import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.sql.RankingSystem;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.ResumedEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DoubleExperienceStart extends TimerTask {
	private final static Logger logger = LoggerFactory.getLogger(DoubleExperienceStart.class);

	private ReadyEvent e;
	private ReconnectedEvent e2;
	private ResumedEvent e3;
	private MessageReceivedEvent e4;
	
	public DoubleExperienceStart(ReadyEvent _e, ReconnectedEvent _e2, ResumedEvent _e3, MessageReceivedEvent _e4) {
		this.e = _e;
		this.e2 = _e2;
		this.e3 = _e3;
		this.e4 = _e4;
	}
	
	@Override
	public void run() {
		var event = (e != null ? e : (e2 != null ? e2 : (e3 != null ? e3 : e4)));
		for(Guild g : event.getJDA().getGuilds()) {
			final long guild_id = g.getIdLong();
			if(Hashes.getTempCache("doubleExp_gu"+guild_id) == null || Hashes.getTempCache("doubleExp_gu"+guild_id).getAdditionalInfo().equals("off")) {
				final var botConfig = BotConfiguration.SQLgetBotConfigs(guild_id);
				if(RankingSystem.SQLgetGuild(guild_id).getRankingState() && botConfig.getDoubleExperience().equals("auto")) {
					Calendar calendar = Calendar.getInstance();
					final int day = calendar.get(Calendar.DAY_OF_WEEK);
					if(day >= botConfig.getDoubleExperienceStart() && (day%7) <= botConfig.getDoubleExperienceEnd()) {
						var bot_channel = Azrael.SQLgetChannels(guild_id).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.BOT.getType())).findAny().orElse(null);
						if(bot_channel != null) {
							final TextChannel textChannel = g.getTextChannelById(bot_channel.getChannel_ID());
							if(textChannel != null && (g.getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES) || STATIC.setPermissions(g, textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES)))) {
								File doubleEvent = new File("./files/RankingSystem/Banners/doubleweekend.jpg");
								if(doubleEvent.exists())
									textChannel.sendFile(doubleEvent, "doubleweekend.jpg").queue();
								textChannel.sendMessage("```css\n"+STATIC.getTranslation2(g, Translation.DOUBLE_EXPERIENCE_AUTO)+"```").queue();
							}
							else
								logger.warn("MESSAGE_WRITE and MESSAGE_ATTACH_FILE permissions required to announce the double experience event on channel {} in guild {}", textChannel.getId(), g.getId());
							logger.info("Double experience event started in guild {}", guild_id);
						}
						Hashes.addTempCache("doubleExp_gu"+guild_id, new Cache("on"));
					}
					else {
						final var cache = Hashes.getTempCache("doubleExp_gu"+guild_id);
						if(cache != null && cache.getAdditionalInfo().equals("on"))
							logger.info("Double experience has been disabled in guild {}", guild_id);
						Hashes.addTempCache("doubleExp_gu"+guild_id, new Cache("off"));
					}
				}
			}
		}
	}
	
	public static void runTask(ReadyEvent _e, ReconnectedEvent _e2, ResumedEvent _e3, MessageReceivedEvent _e4) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		
		Timer time = new Timer("doubleExpStart");
		STATIC.addTimer(time);
		time.schedule(new DoubleExperienceStart(_e, _e2, _e3, _e4), calendar.getTime(), TimeUnit.DAYS.toMillis(1));
	}
}
