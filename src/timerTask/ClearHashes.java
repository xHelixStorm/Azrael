package timerTask;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;

public class ClearHashes extends TimerTask{
	//this class is meant to clear the cache of ranks, shop and dailies every 12h

	@Override
	public void run() {
		Hashes.clearWeaponAbbreviations();
		Hashes.clearWeaponShopContent();
		Hashes.clearWeaponCategories();
		Hashes.clearExpiredTempCache();
		Hashes.clearURLBlacklist();
		Hashes.clearURLWhitelist();
		Hashes.clearWeaponStats();
		Hashes.clearShopContent();
		Hashes.clearDailyItems();
		Hashes.clearSkillShop();
		Hashes.clearRankList();
		Hashes.clearThemes();
		
		Logger logger = LoggerFactory.getLogger(ClearHashes.class);
		logger.debug("Temporary Hashes have been cleared!");
	}
	
	public static void runTask(){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		Timer time = new Timer("ClearHashes");
		time.schedule(new ClearHashes(), calendar.getTime(), TimeUnit.HOURS.toMillis(4));
	}
}
