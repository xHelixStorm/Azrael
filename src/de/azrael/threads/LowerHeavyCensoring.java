package de.azrael.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.core.Hashes;
import de.azrael.enums.Channel;
import de.azrael.enums.Translation;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class LowerHeavyCensoring implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(LowerHeavyCensoring.class);
	private GuildMessageReceivedEvent e;
	
	public LowerHeavyCensoring(GuildMessageReceivedEvent _e) {
		this.e = _e;
	}

	@Override
	public void run() {
		logger.trace("LowerHeavyCensoring thread started in guild {}", e.getGuild().getId());
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
							STATIC.writeToRemoteChannel(e.getGuild(), null, STATIC.getTranslation2(e.getGuild(), Translation.HEAVY_CENSORING_SOFT), Channel.LOG.getType());
						}
					}
					logger.info("Heavy censoring threshold is now at {} in guild {}", count, e.getGuild().getId());
				}
				Thread.sleep(60000);
			}
		} catch (InterruptedException e1) {
			logger.trace("LowerHeavyCensoring thread interrupted in guild {}", e.getGuild().getId());
		}
	}
	
}
