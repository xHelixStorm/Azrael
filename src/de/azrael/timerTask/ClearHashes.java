package de.azrael.timerTask;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.core.Hashes;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;

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
		Hashes.clearBotConfiguration();
		Hashes.clearTweetBlacklist();
		Hashes.clearRankingLevels();
		Hashes.clearReactionRoles();
		Hashes.clearDiscordRoles();
		Hashes.clearURLBlacklist();
		Hashes.clearURLWhitelist();
		Hashes.clearProfileSkins();
		Hashes.clearQueryResults();
		Hashes.clearRankingRoles();
		Hashes.clearShopContent();
		Hashes.clearLevelSkins();
		Hashes.clearDailyItems();
		Hashes.clearCategories();
		Hashes.clearFilterLang();
		Hashes.clearNameFilter();
		Hashes.clearSkillShop();
		Hashes.clearActionlog();
		Hashes.clearRankSkins();
		Hashes.clearChannels();
		Hashes.clearStatus();
		Hashes.clearFeeds();
		
		logger.info("Temporary cache has been cleared");
		
		//clear any expired subscription log
		Azrael.SQLDeleteSubscriptionLog();
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
