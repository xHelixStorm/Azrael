package threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;

public class LowerHeavyCensoring implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(LowerHeavyCensoring.class);
	private GuildMessageReceivedEvent e;
	
	public LowerHeavyCensoring(GuildMessageReceivedEvent _e) {
		this.e = _e;
	}

	@Override
	public void run() {
		logger.debug("lowerHeavyCensoring thread started in guild {}", e.getGuild().getId());
		Hashes.addHeavyCensoringThread(e.getGuild().getIdLong(), Thread.currentThread());
		try {
			Thread.sleep(60000);
			while(true) {
				var threshold = Hashes.getFilterThreshold(e.getGuild().getIdLong());
				if(threshold != null) {
					var count = Integer.parseInt(threshold);
					if(count > 0) {
						Hashes.addFilterThreshold(e.getGuild().getIdLong(), ""+(--count));
						if(count == 29) {
							var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
							if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage("The heavy censoring has terminated the mute everyone level!").queue();
						}
					}
				}
				Thread.sleep(60000);
			}
		} catch (InterruptedException e1) {
			logger.debug("LowerHeavyCensoring thread interrupted in guild {}", e.getGuild().getId());
		}
	}
	
}
