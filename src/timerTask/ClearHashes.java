package timerTask;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import sql.Azrael;
import util.STATIC;

/**
 * Cache clearing for every 4 hours
 * @author xHelixStorm
 *
 */

public class ClearHashes extends TimerTask {
	private final static Logger logger = LoggerFactory.getLogger(ClearHashes.class);

	@Override
	public void run() {
		//Admire the wonderful backward stairs!! Anyway, clearing cache in set interval
		Hashes.clearExpiredSpamDetection();
		Hashes.clearWeaponAbbreviations();
		Hashes.clearWeaponShopContent();
		Hashes.clearWeaponCategories();
		Hashes.clearExpiredTempCache();
		Hashes.clearTweetBlacklist();
		Hashes.clearRankingLevels();
		Hashes.clearURLBlacklist();
		Hashes.clearURLWhitelist();
		Hashes.clearProfileSkins();
		Hashes.clearWeaponStats();
		Hashes.clearShopContent();
		Hashes.clearLevelSkins();
		Hashes.clearDailyItems();
		Hashes.clearCategories();
		Hashes.clearSkillShop();
		Hashes.clearActionlog();
		Hashes.clearRankSkins();
		Hashes.clearChannels();
		
		logger.info("Temporary Hashes have been cleared!");
		
		//clear any outdated tweet logs
		Azrael.SQLDeleteTweetLog();
	}
	
	public static void runTask(){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		Timer time = new Timer("ClearHashes");
		STATIC.addTimer(time);
		time.schedule(new ClearHashes(), calendar.getTime(), TimeUnit.HOURS.toMillis(4));
	}
}
