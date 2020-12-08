package rankingSystem;

import java.io.File;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import core.Hashes;
import enums.Channel;
import enums.Translation;
import enums.Weekday;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.ResumedEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sql.Azrael;
import sql.RankingSystem;
import util.STATIC;

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
		long guild_id;
		var event = (e != null ? e : (e2 != null ? e2 : (e3 != null ? e3 : e4)));
		if(Hashes.getTempCache("doubleExp") == null || Hashes.getTempCache("doubleExp").getAdditionalInfo().equals("off")) {
			Hashes.addTempCache("doubleExp", new Cache("on"));
			for(Guild g : event.getJDA().getGuilds()) {
				guild_id = g.getIdLong();
				if(RankingSystem.SQLgetGuild(guild_id).getRankingState() || GuildIni.getDoubleExperienceMode(guild_id).equals("auto")) {
					var bot_channel = Azrael.SQLgetChannels(guild_id).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.BOT.getType())).findAny().orElse(null);
					if(bot_channel != null) {
						final TextChannel textChannel = g.getTextChannelById(bot_channel.getChannel_ID());
						if(textChannel != null && (g.getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES) || STATIC.setPermissions(g, textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES)))) {
							File doubleEvent = new File("./files/RankingSystem/"+RankingSystem.SQLgetGuild(guild_id).getThemeID()+"/doubleweekend.jpg");
							event.getJDA().getGuildById(guild_id).getTextChannelById(bot_channel.getChannel_ID()).sendFile(doubleEvent, "doubleweekend.jpg").queue();
							event.getJDA().getGuildById(guild_id).getTextChannelById(bot_channel.getChannel_ID()).sendMessage("```css\n"+STATIC.getTranslation2(g, Translation.DOUBLE_EXPERIENCE_AUTO)+"```").queue();
						}
						else
							logger.warn("MESSAGE_WRITE and MESSAGE_ATTACH_FILE permissions required to announce the double experience event on channel {} in guild {}", textChannel.getId(), g.getId());
						logger.info("Double experience event started in guild {}", guild_id);
					}
				}
			}
		}
	}
	
	public static void runTask(ReadyEvent _e, ReconnectedEvent _e2, ResumedEvent _e3, MessageReceivedEvent _e4) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, Weekday.getDay(IniFileReader.getDoubleExpStart()));
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		
		Timer time = new Timer("doubleExpStart");
		STATIC.addTimer(time);
		time.schedule(new DoubleExperienceStart(_e, _e2, _e3, _e4), calendar.getTime(), TimeUnit.DAYS.toMillis(7));
	}
}
