package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import constructors.Cache;
import constructors.CategoryConf;
import constructors.Channels;
import constructors.Dailies;
import constructors.Guilds;
import constructors.Level;
import constructors.Messages;
import constructors.NameFilter;
import constructors.Quizes;
import constructors.RSS;
import constructors.Ranking;
import constructors.Roles;
import constructors.Skills;
import constructors.Skins;
import constructors.SpamDetection;
import constructors.UserIcon;
import constructors.UserLevel;
import constructors.UserProfile;
import constructors.UserRank;
import constructors.Watchlist;
import constructors.WeaponAbbvs;
import constructors.WeaponStats;
import constructors.Weapons;
import net.dv8tion.jda.api.entities.Member;

public class Hashes {
    private final static ConcurrentMap<Long, LinkedHashMap<Long, Ranking>> guild_ranking = new ConcurrentHashMap<Long, LinkedHashMap<Long, Ranking>>();
    private final static ConcurrentMap<Long, LinkedHashMap<Long, ArrayList<Messages>>> guild_message_pool = new ConcurrentHashMap<Long, LinkedHashMap<Long, ArrayList<Messages>>>();
    private static final ConcurrentMap<String, ArrayList<String>> querry_result = new ConcurrentHashMap<String, ArrayList<String>>();
    private static final ConcurrentMap<Long, ArrayList<NameFilter>> name_filter = new ConcurrentHashMap<Long, ArrayList<NameFilter>>();
    private static final ConcurrentMap<Long, ArrayList<String>> filter_lang = new ConcurrentHashMap<Long, ArrayList<String>>();
    private static final Set<String> actionlog = new LinkedHashSet<String>();
    private static final ConcurrentMap<Long, Guilds> status = new ConcurrentHashMap<Long, Guilds>();
    private static final ConcurrentMap<Long, ArrayList<Roles>> ranking_roles = new ConcurrentHashMap<Long, ArrayList<Roles>>();
    private static final LinkedHashMap<Long, ArrayList<Level>> ranking_levels = new LinkedHashMap<Long, ArrayList<Level>>();
    private static final ConcurrentMap<Long, ArrayList<Roles>> reaction_roles = new ConcurrentHashMap<Long, ArrayList<Roles>>();
    private static final ConcurrentMap<Long, Long> reaction_message = new ConcurrentHashMap<Long, Long>();
    private static final ConcurrentMap<Long, ConcurrentMap<Integer, Quizes>> quizes = new ConcurrentHashMap<Long, ConcurrentMap<Integer, Quizes>>();
    private static final ConcurrentMap<Member, Integer> quiz_winners = new ConcurrentHashMap<Member, Integer>();
    private static final ConcurrentMap<Long, ArrayList<Skins>> shopContent = new ConcurrentHashMap<Long, ArrayList<Skins>>();
    private static final ConcurrentMap<Long, ArrayList<Dailies>> daily_items = new ConcurrentHashMap<Long, ArrayList<Dailies>>();
    private static final ConcurrentMap<Long, ArrayList<Roles>> discordRoles = new ConcurrentHashMap<Long, ArrayList<Roles>>();
    private static final ConcurrentMap<Long, ArrayList<RSS>> feeds = new ConcurrentHashMap<Long, ArrayList<RSS>>();
    private static final ConcurrentMap<Long, ArrayList<Weapons>> weaponShopContent = new ConcurrentHashMap<Long, ArrayList<Weapons>>();
    private static final ConcurrentMap<Long, ArrayList<String>> weaponCategories = new ConcurrentHashMap<Long, ArrayList<String>>();
    private static final ConcurrentMap<Long, ArrayList<WeaponAbbvs>> weaponAbbvs = new ConcurrentHashMap<Long, ArrayList<WeaponAbbvs>>();
    private static final LinkedHashMap<Long, ArrayList<WeaponStats>> weaponStats = new LinkedHashMap<Long, ArrayList<WeaponStats>>();
    private static final ConcurrentMap<Long, ArrayList<Channels>> channels = new ConcurrentHashMap<Long, ArrayList<Channels>>();
    private static final ConcurrentMap<String, String> commentedUsers = new ConcurrentHashMap<String, String>();
    private static final ConcurrentMap<String, Cache> tempCache = new ConcurrentHashMap<String, Cache>();
    private static final ConcurrentMap<Long, ArrayList<Skills>> skillShop = new ConcurrentHashMap<Long, ArrayList<Skills>>();
    private static final Set<String> globalURLBlacklist = new HashSet<String>();
    private static final ConcurrentMap<Long, ArrayList<String>> urlBlacklist = new ConcurrentHashMap<Long, ArrayList<String>>();
    private static final ConcurrentMap<Long, ArrayList<String>> urlWhitelist = new ConcurrentHashMap<Long, ArrayList<String>>();
    private static final ConcurrentMap<Long, ArrayList<String>> tweetBlacklist = new ConcurrentHashMap<Long, ArrayList<String>>();
    private static final ConcurrentMap<String, Watchlist> watchlist = new ConcurrentHashMap<String, Watchlist>();
    private static final ConcurrentMap<Long, Boolean> heavyCensoring = new ConcurrentHashMap<Long, Boolean>();
    private static final ConcurrentMap<Long, ArrayList<String>> censorMessage = new ConcurrentHashMap<Long, ArrayList<String>>();
    private static final ConcurrentMap<Long, Guilds> old_guild_settings = new ConcurrentHashMap<Long, Guilds>();
    private static final ConcurrentMap<Long, String> filter_threshold = new ConcurrentHashMap<Long, String>();
    private static final ConcurrentMap<Long, Thread> heavyCensoringThread = new ConcurrentHashMap<Long, Thread>();
    private static final ConcurrentMap<String, SpamDetection> spamDetection = new ConcurrentHashMap<String, SpamDetection>();
    private static final ConcurrentMap<Long, String> languages = new ConcurrentHashMap<Long, String>();
    private static final ConcurrentMap<Long, ConcurrentHashMap<Integer, UserLevel>> level_skins = new ConcurrentHashMap<Long, ConcurrentHashMap<Integer, UserLevel>>();
    private static final ConcurrentMap<Long, ConcurrentHashMap<Integer, UserRank>> rank_skins = new ConcurrentHashMap<Long, ConcurrentHashMap<Integer, UserRank>>();
    private static final ConcurrentMap<Long, ConcurrentHashMap<Integer, UserProfile>> profile_skins = new ConcurrentHashMap<Long, ConcurrentHashMap<Integer, UserProfile>>();
    private static final ConcurrentMap<Long, ConcurrentHashMap<Integer, UserIcon>> icon_skins = new ConcurrentHashMap<Long, ConcurrentHashMap<Integer, UserIcon>>();
    private static final ConcurrentMap<String, Integer> subscription_status = new ConcurrentHashMap<String, Integer>();
    private static final ConcurrentMap<Long, ArrayList<CategoryConf>> categories = new ConcurrentHashMap<Long, ArrayList<CategoryConf>>();
    private static final ConcurrentMap<Long, ArrayList<Timer>> schedules = new ConcurrentHashMap<Long, ArrayList<Timer>>();
    private static final ConcurrentMap<Long, HashMap<String, Long>> itemEffect = new ConcurrentHashMap<Long, HashMap<String, Long>>();
    
    public static void initializeGuildMessagePool(Long key, final int max_message_pool_size) {
    	LinkedHashMap<Long, ArrayList<Messages>> message_pool = new LinkedHashMap<Long, ArrayList<Messages>>() {
			private static final long serialVersionUID = 1770564696361163460L;
			@SuppressWarnings("rawtypes")
            protected boolean removeEldestEntry(final Map.Entry eldest) {
                return size() > max_message_pool_size;
            }
    	};
    	guild_message_pool.put(key, message_pool);
    }
	public static void addMessagePool(final long key, long message_id, ArrayList<Messages> messages) {
		final var message_pool = guild_message_pool.get(key);
		message_pool.put(message_id, messages);
		guild_message_pool.put(key, message_pool);
	}
	public static void setWholeMessagePool(final long key, LinkedHashMap<Long, ArrayList<Messages>> messages) {
		guild_message_pool.put(key, messages);
	}
	public static void addFilterLang(long channel_id, ArrayList<String> filter_languages) {
		filter_lang.put(channel_id, filter_languages);
	}
	public static void addQuerryResult(String key, ArrayList<String> result) {
		querry_result.put(key, result);
	}
	public static void addNameFilter(Long key, ArrayList<NameFilter> names) {
		name_filter.putIfAbsent(key, names);
	}
	public static void addActionlog(String key) {
		actionlog.add(key);
	}
	public static void addStatus(Long key, Guilds options) {
		status.put(key, options);
	}
	public static void initializeGuildRanking(Long key) {
		final LinkedHashMap<Long, Ranking> ranking = new LinkedHashMap<Long, Ranking>() {
			private static final long serialVersionUID = 1770564696361163460L;
			@Override
			@SuppressWarnings("rawtypes")
	    	protected boolean removeEldestEntry(final Map.Entry eldest) {
	    		return size() > 100;
	    	}
	    };
	    guild_ranking.put(key, ranking);
	}
	public static void addRanking(Long key, Long key2, Ranking details) {
		final var ranking = guild_ranking.get(key);
		ranking.put(key2, details);
		guild_ranking.put(key, ranking);
	}
	public static void addRankingRoles(Long key, ArrayList<Roles> details) {
		ranking_roles.put(key, details);
	}
	public static void addRankingLevels(Long key, ArrayList<Level> levels) {
		ranking_levels.put(key, levels);
	}
	public static void addReactionRoles(Long key, ArrayList<Roles> roles) {
		reaction_roles.put(key, roles);
	}
	public static void addReactionMessage(Long key, Long message_id) {
		reaction_message.put(key, message_id);
	}
	public static void addQuiz(Long key, Integer key2, Quizes quiz) {
		if(!quizes.containsKey(key)) {
			ConcurrentMap<Integer, Quizes> map = new ConcurrentHashMap<Integer, Quizes>();
			map.put(key2, quiz);
			quizes.put(key, map);
		}
		else {
			final var map = quizes.get(key);
			map.put(key2, quiz);
			quizes.put(key, map);
		}
	}
	public static void addQuizWinners(Member key, Integer threshold) {
		quiz_winners.put(key, threshold);
	}
	public static void addShopContent(Long key, ArrayList<Skins> skin_content) {
		shopContent.put(key, skin_content);
	}
	public static void addDailyItems(Long key, ArrayList<Dailies> items) {
		daily_items.put(key, items);
	}
	public static void addDiscordRole(Long key, ArrayList<Roles> roles) {
		discordRoles.put(key, roles);
	}
	public static void addFeeds(Long key, ArrayList<RSS> subscriptions) {
		feeds.put(key, subscriptions);
	}
	public static void addWeaponShopContent(Long key, ArrayList<Weapons> weapons) {
		weaponShopContent.put(key, weapons);
	}
	public static void addWeaponCategories(Long key, ArrayList<String> categories) {
		weaponCategories.put(key, categories);
	}
	public static void addWeaponAbbreviation(Long key, ArrayList<WeaponAbbvs> abbreviations) {
		weaponAbbvs.put(key, abbreviations);
	}
	public static void addWeaponStat(Long key, ArrayList<WeaponStats> stats) {
		weaponStats.put(key, stats);
	}
	public static void addChannels(Long key, ArrayList<Channels> ch) {
		channels.put(key, ch);
	}
	public static void addCommentedUser(String key, String name) {
		commentedUsers.put(key, name);
	}
	public static void addTempCache(String key, Cache cache) {
		tempCache.put(key, cache);
	}
	public static void addSkillShop(Long key, ArrayList<Skills> skill) {
		skillShop.put(key, skill);
	}
	public static void addGlobalURLBlacklist(String url) {
		globalURLBlacklist.add(url);
	}
	public static void addURLBlacklist(Long key, ArrayList<String> value) {
		urlBlacklist.put(key, value);
	}
	public static void addURLWhitelist(Long key, ArrayList<String> value) {
		urlWhitelist.put(key, value);
	}
	public static void addTweetBlacklist(Long key, ArrayList<String> value) {
		tweetBlacklist.put(key, value);
	}
	public static void addWatchlist(String key, Watchlist value) {
		watchlist.put(key, value);
	}
	public static void addHeavyCensoring(Long key, Boolean value) {
		heavyCensoring.put(key, value);
	}
	public static void addCensorMessage(Long key, ArrayList<String> value) {
		censorMessage.put(key, value);
	}
	public static void addOldGuildSettings(Long key, Guilds guild) {
		old_guild_settings.put(key, guild);
	}
	public static void addFilterThreshold(Long key, String count) {
		filter_threshold.put(key, count);
	}
	public static void addHeavyCensoringThread(Long key, Thread thread) {
		heavyCensoringThread.put(key, thread);
	}
	public static void addSpamMessage(String key, SpamDetection message) {
		spamDetection.put(key, message);
	}
	public static void setLanguage(Long key, String lang) {
		languages.put(key, lang);
	}
	public static void addLevelSkin(Long key, Integer key2, UserLevel userLevel) {
		if(!level_skins.containsKey(key)) {
			ConcurrentHashMap<Integer, UserLevel> map = new ConcurrentHashMap<Integer, UserLevel>();
			map.put(key2, userLevel);
			level_skins.put(key, map);
		}
		else {
			final var map = level_skins.get(key);
			map.put(key2, userLevel);
			level_skins.put(key, map);
		}
	}
	public static void addRankSkin(Long key, Integer key2, UserRank userRank) {
		if(!rank_skins.containsKey(key)) {
			ConcurrentHashMap<Integer, UserRank> map = new ConcurrentHashMap<Integer, UserRank>();
			map.put(key2, userRank);
			rank_skins.put(key, map);
		}
		else {
			final var map = rank_skins.get(key);
			map.put(key2, userRank);
			rank_skins.put(key, map);
		}
	}
	public static void addProfileSkin(Long key, Integer key2, UserProfile userProfile) {
		if(!profile_skins.containsKey(key)) {
			ConcurrentHashMap<Integer, UserProfile> map = new ConcurrentHashMap<Integer, UserProfile>();
			map.put(key2, userProfile);
			profile_skins.put(key, map);
		}
		else {
			final var map = profile_skins.get(key);
			map.put(key2, userProfile);
			profile_skins.put(key, map);
		}
	}
	public static void addIconSkin(Long key, Integer key2, UserIcon userIcon) {
		if(!icon_skins.containsKey(key)) {
			ConcurrentHashMap<Integer, UserIcon> map = new ConcurrentHashMap<Integer, UserIcon>();
			map.put(key2, userIcon);
			icon_skins.put(key, map);
		}
		else {
			final var map = icon_skins.get(key);
			map.put(key2, userIcon);
			icon_skins.put(key, map);
		}
	}
	public static void addSubscriptionStatus(String key, Integer value) {
		subscription_status.put(key, value);
	}
	public static void addCategories(Long key, ArrayList<CategoryConf> cat) {
		categories.put(key, cat);
	}
	public static void addSchedule(Long key, ArrayList<Timer> timer) {
		schedules.put(key, timer);
	}
	public static void addItemEffects(Long key, HashMap<String, Long> items) {
		itemEffect.put(key, items);
	}
	
	public static ArrayList<Messages> getMessagePool(long key, long message_id) {
		final var message_pool = guild_message_pool.get(key);
		if(message_pool == null)
			return null;
		return message_pool.get(message_id);
	}
	public static ArrayList<String> getFilterLang(long channel_id) {
		return filter_lang.get(channel_id);
	}
	public static LinkedHashMap<Long, ArrayList<Messages>> getWholeMessagePool(long key) {
		return guild_message_pool.get(key);
	}
	public static ArrayList<String> getQuerryResult(String key) {
		return querry_result.get(key);
	}
	public static ArrayList<NameFilter> getNameFilter(Long key) {
		return name_filter.get(key);
	}
	public static boolean containsActionlog(String key) {
		return actionlog.contains(key);
	}
	public static boolean actionlogIsEmpty() {
		return actionlog.isEmpty();
	}
	public static Guilds getStatus(long key) {
		return status.get(key);
	}
	public static Ranking getRanking(long key, long key2) {
		final var ranking = guild_ranking.get(key);
		if(ranking == null)
			return null;
		return ranking.get(key2);
	}
	public static ArrayList<Roles> getRankingRoles(Long key) {
		return ranking_roles.get(key);
	}
	public static ArrayList<Level> getRankingLevels(Long key) {
		return ranking_levels.get(key);
	}
	public static ArrayList<Roles> getReactionRoles(Long key) {
		return reaction_roles.get(key);
	}
	public static Long getReactionMessage(Long key) {
		return reaction_message.get(key);
	}
	public static Quizes getQuiz(Long key, Integer key2) {
		if(quizes.containsKey(key))
			return quizes.get(key).get(key2);
		else
			return null;
	}
	public static Map<Integer, Quizes> getWholeQuiz(Long key) {
		return quizes.get(key);
	}
	public static Integer getQuizWinners(Member key) {
		return quiz_winners.get(key);
	}
	public static ArrayList<Skins> getShopContent(Long key) {
		return shopContent.get(key);
	}
	public static ArrayList<Dailies> getDailyItems(Long key) {
		return daily_items.get(key);
	}
	public static ArrayList<Roles> getDiscordRole(Long key) {
		return discordRoles.get(key);
	}
	public static ArrayList<RSS> getFeed(Long key) {
		return feeds.get(key);
	}
	public static int getFeedsSize(Long key) {
		ArrayList<RSS> feedList = feeds.get(key);
		if(feedList == null)
			return 0;
		else
			return feedList.size();
	}
	public static ArrayList<Weapons> getWeaponShopContent(Long key) {
		return weaponShopContent.get(key);
	}
	public static ArrayList<String> getWeaponCategories(Long key) {
		return weaponCategories.get(key);
	}
	public static ArrayList<WeaponAbbvs> getWeaponAbbreviations(Long key) {
		return weaponAbbvs.get(key);
	}
	public static ArrayList<WeaponStats> getWeaponStats(Long key) {
		return weaponStats.get(key);
	}
	public static ArrayList<Channels> getChannels(Long key) {
		return channels.get(key);
	}
	public static String getCommentedUser(String key) {
		return commentedUsers.get(key);
	}
	public static Cache getTempCache(String key) {
		return tempCache.get(key);
	}
	public static ArrayList<Skills> getSkillShop(Long key) {
		return skillShop.get(key);
	}
	public static boolean findGlobalURLBlacklist(String url) {
		return globalURLBlacklist.contains(url);
	}
	public static boolean globalURLBlacklistEmpty() {
		return globalURLBlacklist.isEmpty();
	}
	public static ArrayList<String> getURLBlacklist(Long key) {
		return urlBlacklist.get(key);
	}
	public static ArrayList<String> getURLWhitelist(Long key) {
		return urlWhitelist.get(key);
	}
	public static ArrayList<String> getTweetBlacklist(Long key) {
		return tweetBlacklist.get(key);
	}
	public static Watchlist getWatchlist(String key) {
		return watchlist.get(key);
	}
	public static Boolean getHeavyCensoring(Long key) {
		return heavyCensoring.get(key);
	}
	public static ArrayList<String> getCensorMessage(Long key) {
		return censorMessage.get(key);
	}
	public static Guilds getOldGuildSettings(Long key) {
		return old_guild_settings.get(key);
	}
	public static String getFilterThreshold(Long key) {
		return filter_threshold.get(key);
	}
	public static Thread getHeavyCensoringThread(Long key) {
		return heavyCensoringThread.get(key);
	}
	public static SpamDetection getSpamDetection(String key) {
		return spamDetection.get(key);
	}
	public static String getLanguage(long key) {
		return languages.get(key);
	}
	public static UserLevel getLevelSkin(Long key, Integer key2) {
		if(level_skins.containsKey(key))
			return level_skins.get(key).get(key2);
		else
			return null;
	}
	public static ConcurrentHashMap<Integer, UserLevel> getLevelSkins(Long key) {
		return level_skins.get(key);
	}
	public static UserRank getRankSkin(Long key, Integer key2) {
		if(rank_skins.containsKey(key))
			return rank_skins.get(key).get(key2);
		else
			return null;
	}
	public static ConcurrentHashMap<Integer, UserRank> getRankSkins(Long key) {
		return rank_skins.get(key);
	}
	public static UserProfile getProfileSkin(Long key, Integer key2) {
		if(profile_skins.containsKey(key))
			return profile_skins.get(key).get(key2);
		else
			return null;
	}
	public static ConcurrentHashMap<Integer, UserProfile> getProfileSkins(Long key) {
		return profile_skins.get(key);
	}
	public static UserIcon getIconSkin(Long key, Integer key2) {
		if(icon_skins.containsKey(key))
			return icon_skins.get(key).get(key2);
		else
			return null;
	}
	public static ConcurrentHashMap<Integer, UserIcon> getIconSkins(Long key) {
		return icon_skins.get(key);
	}
	public static Integer getSubscriptionStatus(String key) {
		return subscription_status.get(key);
	}
	public static ArrayList<CategoryConf> getCategories(Long key) {
		return categories.get(key);
	}
	public static ArrayList<Timer> getSchedules(Long key) {
		return schedules.get(key);
	}
	public static HashMap<String, Long> getItemEffects(Long key) {
		return itemEffect.get(key);
	}
	
	public static void removeMessagePool(final long key, long message_id) {
		final var message_pool = guild_message_pool.get(key);
		message_pool.remove(message_id);
		guild_message_pool.put(key, message_pool);
	}
	public static void removeFilterLang(long key) {
		filter_lang.remove(key);
	}
	public static void removeQuerryResult(String key) {
		querry_result.remove(key);
	}
	public static void removeNameFilter(Long key) {
		name_filter.remove(key);
	}
	public static void removeGuildRanking(long key) {
		guild_ranking.remove(key);
	}
	public static void removeReactionRoles(Long key) {
		reaction_roles.remove(key);
	}
	public static void removeRankingRoles(long key) {
		ranking_roles.remove(key);
	}
	public static void clearRankingLevels() {
		ranking_levels.clear();
	}
	public static void removeQuiz(Long key, Integer key2) {
		if(quizes.containsKey(key)) {
			final var map = quizes.get(key);
			map.remove(key2);
			quizes.put(key, map);
		}
	}
	public static void clearQuiz(Long key) {
		quizes.remove(key);
	}
	public static void removeQuizWinners(long guild_id) {
		if(quiz_winners.size() > 0) {
			for(Member member : quiz_winners.keySet()) {
				if(member.getGuild().getIdLong() == guild_id) {
					if(quiz_winners.get(member) == 1) {
						quiz_winners.remove(member);
					}
					else {
						quiz_winners.put(member, (quiz_winners.get(member)-1));
					}
				}
			}
		}
	}
	public static void clearQuizWinners(long guild_id) {
		final var members = quiz_winners.keySet();
		for(final var member : members.parallelStream().filter(f -> f.getGuild().getIdLong() == guild_id).collect(Collectors.toList())) {
			quiz_winners.remove(member);
		}
	}
	public static void clearShopContent() {
		shopContent.clear();
	}
	public static void removeDailyItems(long key) {
		daily_items.remove(key);
	}
	public static void clearDailyItems() {
		daily_items.clear();
	}
	public static void removeDiscordRoles(long key) {
		discordRoles.remove(key);
	}
	public static void clearDiscordRoles() {
		discordRoles.clear();
	}
	public static void removeFeeds(long key) {
		feeds.remove(key);
	}
	public static void clearFeeds() {
		feeds.clear();
	}
	public static void clearWeaponShopContent() {
		weaponShopContent.clear();
	}
	public static void clearWeaponCategories() {
		weaponCategories.clear();
	}
	public static void clearWeaponAbbreviations() {
		weaponAbbvs.clear();
	}
	public static void clearWeaponStats() {
		weaponStats.clear();
	}
	public static void clearChannels() {
		channels.clear();
	}
	public static void removeChannels(Long key) {
		channels.remove(key);
	}
	public static void clearCommentedUsers() {
		commentedUsers.clear();
	}
	public static void clearTempCache(String key) {
		tempCache.remove(key);
	}
	public static void clearExpiredTempCache() {
		var now = System.currentTimeMillis();
		for(final var key : tempCache.keySet()) {
			if(tempCache.get(key).getExpiration() - now <= 0 && tempCache.get(key).getExpire()) {
				tempCache.remove(key);
			}
		}
	}
	public static void clearSkillShop() {
		skillShop.clear();
	}
	public static void removeURLBlacklist(Long key) {
		urlBlacklist.remove(key);
	}
	public static void clearURLBlacklist() {
		urlBlacklist.clear();
	}
	public static void removeURLWhitelist(Long key) {
		urlWhitelist.remove(key);
	}
	public static void clearURLWhitelist() {
		urlWhitelist.clear();
	}
	public static void removeTweetBlacklist(Long key) {
		tweetBlacklist.remove(key);
	}
	public static void clearTweetBlacklist() {
		tweetBlacklist.clear();
	}
	public static void clearActionlog() {
		actionlog.clear();
	}
	public static void removeWatchlist(String key) {
		watchlist.remove(key);
	}
	public static void removeStatus(long key) {
		status.remove(key);
	}
	public static void removeCensoreMessage(long key) {
		censorMessage.remove(key);
	}
	public static void removeFilterThreshold(long key) {
		filter_threshold.remove(key);
	}
	public static void removeHeavyCensoringThread(long key) {
		heavyCensoringThread.remove(key);
	}
	public static void removeSpamDetection(String key) {
		spamDetection.remove(key);
	}
	public static void clearExpiredSpamDetection() {
		spamDetection.keySet().parallelStream().forEach(key -> {
			if(spamDetection.get(key).isExpired()) {
				spamDetection.remove(key);
			}
		});
	}
	public static void clearLevelSkins() {
		level_skins.clear();
	}
	public static void clearLevelSkins(Long key) {
		level_skins.remove(key);
	}
	public static void clearRankSkins() {
		rank_skins.clear();
	}
	public static void clearRankSkins(Long key) {
		rank_skins.remove(key);
	}
	public static void clearProfileSkins() {
		profile_skins.clear();
	}
	public static void clearProfileSkins(Long key) {
		profile_skins.remove(key);
	}
	public static void clearIconSkins() {
		icon_skins.clear();
	}
	public static void clearIconSkins(Long key) {
		icon_skins.remove(key);
	}
	public static void removeSubscriptionStatus(String key) {
		subscription_status.remove(key);
	}
	public static void removeCategories(Long key) {
		categories.remove(key);
	}
	public static void clearCategories() {
		categories.clear();
	}
	public static void removeSchedules(Long key) {
		schedules.remove(key);
	}
	public static void clearItemEffects() {
		itemEffect.clear();
	}
}
