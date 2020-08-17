package threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import enums.Channel;
import enums.Translation;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import util.STATIC;

public class LowerHeavyCensoring implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(LowerHeavyCensoring.class);
	private GuildMessageReceivedEvent e;
	
	public LowerHeavyCensoring(GuildMessageReceivedEvent _e) {
		this.e = _e;
	}

	@Override
	public void run() {
		logger.debug("LowerHeavyCensoring thread started in guild {}", e.getGuild().getId());
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
				}
				Thread.sleep(60000);
			}
		} catch (InterruptedException e1) {
			logger.debug("LowerHeavyCensoring thread interrupted in guild {}", e.getGuild().getId());
		}
	}
	
}
