package core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
    private static final ConcurrentMap<Integer, Quizes> quiz = new ConcurrentHashMap<Integer, Quizes>();
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
    
    public static void initializeGuildMessagePool(Long _key, final int max_message_pool_size) {
    	LinkedHashMap<Long, ArrayList<Messages>> message_pool = new LinkedHashMap<Long, ArrayList<Messages>>() {
			private static final long serialVersionUID = 1770564696361163460L;
			@SuppressWarnings("rawtypes")
            protected boolean removeEldestEntry(final Map.Entry eldest) {
                return size() > max_message_pool_size;
            }
    	};
    	guild_message_pool.put(_key, message_pool);
    }
	public static void addMessagePool(final long _key, long _message_id, ArrayList<Messages> _messages) {
		final var message_pool = guild_message_pool.get(_key);
		message_pool.put(_message_id, _messages);
		guild_message_pool.put(_key, message_pool);
	}
	public static void setWholeMessagePool(final long _key, LinkedHashMap<Long, ArrayList<Messages>> messages) {
		guild_message_pool.put(_key, messages);
	}
	public static void addFilterLang(long _channel_id, ArrayList<String> _filter_lang) {
		filter_lang.put(_channel_id, _filter_lang);
	}
	public static void addQuerryResult(String _key, ArrayList<String> _result) {
		querry_result.put(_key, _result);
	}
	public static void addNameFilter(Long _key, ArrayList<NameFilter> _names) {
		name_filter.putIfAbsent(_key, _names);
	}
	public static void addActionlog(String _key) {
		actionlog.add(_key);
	}
	public static void addStatus(Long _key, Guilds _status) {
		status.put(_key, _status);
	}
	public static void initializeGuildRanking(Long _key) {
		final LinkedHashMap<Long, Ranking> ranking = new LinkedHashMap<Long, Ranking>() {
			private static final long serialVersionUID = 1770564696361163460L;
			@Override
			@SuppressWarnings("rawtypes")
	    	protected boolean removeEldestEntry(final Map.Entry eldest) {
	    		return size() > 100;
	    	}
	    };
	    guild_ranking.put(_key, ranking);
	}
	public static void addRanking(Long _key, Long _key2, Ranking _details) {
		final var ranking = guild_ranking.get(_key);
		ranking.put(_key2, _details);
		guild_ranking.put(_key, ranking);
	}
	public static void addRankingRoles(Long _key, ArrayList<Roles> _details) {
		ranking_roles.put(_key, _details);
	}
	public static void addRankingLevels(Long _key, ArrayList<Level> _levels) {
		ranking_levels.put(_key, _levels);
	}
	public static void addReactionRoles(Long _key, ArrayList<Roles> _roles) {
		reaction_roles.put(_key, _roles);
	}
	public static void addReactionMessage(Long _key, Long _message_id) {
		reaction_message.put(_key, _message_id);
	}
	public static void addQuiz(Integer _key, Quizes _quiz) {
		quiz.put(_key, _quiz);
	}
	public static void addQuizWinners(Member _key, Integer _threshold) {
		quiz_winners.put(_key, _threshold);
	}
	public static void addShopContent(Long _key, ArrayList<Skins> _skin_content) {
		shopContent.put(_key, _skin_content);
	}
	public static void addDailyItems(Long _key, ArrayList<Dailies> _daily_items) {
		daily_items.put(_key, _daily_items);
	}
	public static void addDiscordRole(Long _key, ArrayList<Roles> _roles) {
		discordRoles.put(_key, _roles);
	}
	public static void addFeeds(Long _key, ArrayList<RSS> _feeds) {
		feeds.put(_key, _feeds);
	}
	public static void addWeaponShopContent(Long _key, ArrayList<Weapons> _weapons) {
		weaponShopContent.put(_key, _weapons);
	}
	public static void addWeaponCategories(Long _key, ArrayList<String> _categories) {
		weaponCategories.put(_key, _categories);
	}
	public static void addWeaponAbbreviation(Long _key, ArrayList<WeaponAbbvs> _abbreviations) {
		weaponAbbvs.put(_key, _abbreviations);
	}
	public static void addWeaponStat(Long _key, ArrayList<WeaponStats> _stats) {
		weaponStats.put(_key, _stats);
	}
	public static void addChannels(Long _key, ArrayList<Channels> _channels) {
		channels.put(_key, _channels);
	}
	public static void addCommentedUser(String _key, String _name) {
		commentedUsers.put(_key, _name);
	}
	public static void addTempCache(String _key, Cache _cache) {
		tempCache.put(_key, _cache);
	}
	public static void addSkillShop(Long _key, ArrayList<Skills> skill) {
		skillShop.put(_key, skill);
	}
	public static void addGlobalURLBlacklist(String _url) {
		globalURLBlacklist.add(_url);
	}
	public static void addURLBlacklist(Long _key, ArrayList<String> _value) {
		urlBlacklist.put(_key, _value);
	}
	public static void addURLWhitelist(Long _key, ArrayList<String> _value) {
		urlWhitelist.put(_key, _value);
	}
	public static void addTweetBlacklist(Long _key, ArrayList<String> _value) {
		tweetBlacklist.put(_key, _value);
	}
	public static void addWatchlist(String _key, Watchlist _value) {
		watchlist.put(_key, _value);
	}
	public static void addHeavyCensoring(Long _key, Boolean _value) {
		heavyCensoring.put(_key, _value);
	}
	public static void addCensorMessage(Long _key, ArrayList<String> _value) {
		censorMessage.put(_key, _value);
	}
	public static void addOldGuildSettings(Long _key, Guilds _guild) {
		old_guild_settings.put(_key, _guild);
	}
	public static void addFilterThreshold(Long _key, String _count) {
		filter_threshold.put(_key, _count);
	}
	public static void addHeavyCensoringThread(Long _key, Thread _thread) {
		heavyCensoringThread.put(_key, _thread);
	}
	public static void addSpamMessage(String _key, SpamDetection _message) {
		spamDetection.put(_key, _message);
	}
	public static void setLanguage(Long _key, String _lang) {
		languages.put(_key, _lang);
	}
	public static void addLevelSkin(Long _key, Integer _key2, UserLevel _userLevel) {
		if(!level_skins.containsKey(_key)) {
			ConcurrentHashMap<Integer, UserLevel> map = new ConcurrentHashMap<Integer, UserLevel>();
			map.put(_key2, _userLevel);
			level_skins.put(_key, map);
		}
		else {
			final var map = level_skins.get(_key);
			map.put(_key2, _userLevel);
			level_skins.put(_key, map);
		}
	}
	public static void addRankSkin(Long _key, Integer _key2, UserRank _userRank) {
		if(!rank_skins.containsKey(_key)) {
			ConcurrentHashMap<Integer, UserRank> map = new ConcurrentHashMap<Integer, UserRank>();
			map.put(_key2, _userRank);
			rank_skins.put(_key, map);
		}
		else {
			final var map = rank_skins.get(_key);
			map.put(_key2, _userRank);
			rank_skins.put(_key, map);
		}
	}
	public static void addProfileSkin(Long _key, Integer _key2, UserProfile _userProfile) {
		if(!profile_skins.containsKey(_key)) {
			ConcurrentHashMap<Integer, UserProfile> map = new ConcurrentHashMap<Integer, UserProfile>();
			map.put(_key2, _userProfile);
			profile_skins.put(_key, map);
		}
		else {
			final var map = profile_skins.get(_key);
			map.put(_key2, _userProfile);
			profile_skins.put(_key, map);
		}
	}
	public static void addIconSkin(Long _key, Integer _key2, UserIcon _userIcon) {
		if(!icon_skins.containsKey(_key)) {
			ConcurrentHashMap<Integer, UserIcon> map = new ConcurrentHashMap<Integer, UserIcon>();
			map.put(_key2, _userIcon);
			icon_skins.put(_key, map);
		}
		else {
			final var map = icon_skins.get(_key);
			map.put(_key2, _userIcon);
			icon_skins.put(_key, map);
		}
	}
	public static void addSubscriptionStatus(String _key, Integer _value) {
		subscription_status.put(_key, _value);
	}
	public static void addCategories(Long _key, ArrayList<CategoryConf> _categories) {
		categories.put(_key, _categories);
	}
	
	public static ArrayList<Messages> getMessagePool(long _key, long _message_id) {
		final var message_pool = guild_message_pool.get(_key);
		if(message_pool == null)
			return null;
		return message_pool.get(_message_id);
	}
	public static ArrayList<String> getFilterLang(long _channel_id) {
		return filter_lang.get(_channel_id);
	}
	public static LinkedHashMap<Long, ArrayList<Messages>> getWholeMessagePool(long _key) {
		return guild_message_pool.get(_key);
	}
	public static ArrayList<String> getQuerryResult(String _key) {
		return querry_result.get(_key);
	}
	public static ArrayList<NameFilter> getNameFilter(Long _key) {
		return name_filter.get(_key);
	}
	public static boolean containsActionlog(String _key) {
		return actionlog.contains(_key);
	}
	public static boolean actionlogIsEmpty() {
		return actionlog.isEmpty();
	}
	public static Guilds getStatus(long _key) {
		return status.get(_key);
	}
	public static Ranking getRanking(long _key, long _key2) {
		final var ranking = guild_ranking.get(_key);
		if(ranking == null)
			return null;
		return ranking.get(_key2);
	}
	public static ArrayList<Roles> getRankingRoles(Long _key) {
		return ranking_roles.get(_key);
	}
	public static ArrayList<Level> getRankingLevels(Long _key) {
		return ranking_levels.get(_key);
	}
	public static ArrayList<Roles> getReactionRoles(Long _key) {
		return reaction_roles.get(_key);
	}
	public static Long getReactionMessage(Long _key) {
		return reaction_message.get(_key);
	}
	public static Quizes getQuiz(int _key) {
		return quiz.get(_key);
	}
	public static Map<Integer, Quizes> getWholeQuiz() {
		return quiz;
	}
	public static Integer getQuizWinners(Member _key) {
		return quiz_winners.get(_key);
	}
	public static ArrayList<Skins> getShopContent(Long _key) {
		return shopContent.get(_key);
	}
	public static ArrayList<Dailies> getDailyItems(Long _key) {
		return daily_items.get(_key);
	}
	public static ArrayList<Roles> getDiscordRole(Long _key) {
		return discordRoles.get(_key);
	}
	public static ArrayList<RSS> getFeed(Long _key) {
		return feeds.get(_key);
	}
	public static int getFeedsSize(Long _key) {
		ArrayList<RSS> feedList = feeds.get(_key);
		if(feedList == null)
			return 0;
		else
			return feedList.size();
	}
	public static ArrayList<Weapons> getWeaponShopContent(Long _key) {
		return weaponShopContent.get(_key);
	}
	public static ArrayList<String> getWeaponCategories(Long _key) {
		return weaponCategories.get(_key);
	}
	public static ArrayList<WeaponAbbvs> getWeaponAbbreviations(Long _key) {
		return weaponAbbvs.get(_key);
	}
	public static ArrayList<WeaponStats> getWeaponStats(Long _key) {
		return weaponStats.get(_key);
	}
	public static ArrayList<Channels> getChannels(Long _key) {
		return channels.get(_key);
	}
	public static String getCommentedUser(String _key) {
		return commentedUsers.get(_key);
	}
	public static Cache getTempCache(String _key) {
		return tempCache.get(_key);
	}
	public static ArrayList<Skills> getSkillShop(Long _key) {
		return skillShop.get(_key);
	}
	public static boolean findGlobalURLBlacklist(String _url) {
		return globalURLBlacklist.contains(_url);
	}
	public static boolean globalURLBlacklistEmpty() {
		return globalURLBlacklist.isEmpty();
	}
	public static ArrayList<String> getURLBlacklist(Long _key) {
		return urlBlacklist.get(_key);
	}
	public static ArrayList<String> getURLWhitelist(Long _key) {
		return urlWhitelist.get(_key);
	}
	public static ArrayList<String> getTweetBlacklist(Long _key) {
		return tweetBlacklist.get(_key);
	}
	public static Watchlist getWatchlist(String _key) {
		return watchlist.get(_key);
	}
	public static Boolean getHeavyCensoring(Long _key) {
		return heavyCensoring.get(_key);
	}
	public static ArrayList<String> getCensorMessage(Long _key) {
		return censorMessage.get(_key);
	}
	public static Guilds getOldGuildSettings(Long _key) {
		return old_guild_settings.get(_key);
	}
	public static String getFilterThreshold(Long _key) {
		return filter_threshold.get(_key);
	}
	public static Thread getHeavyCensoringThread(Long _key) {
		return heavyCensoringThread.get(_key);
	}
	public static SpamDetection getSpamDetection(String _key) {
		return spamDetection.get(_key);
	}
	public static String getLanguage(long _key) {
		return languages.get(_key);
	}
	public static UserLevel getLevelSkin(Long _key, Integer _key2) {
		if(level_skins.containsKey(_key))
			return level_skins.get(_key).get(_key2);
		else
			return null;
	}
	public static ConcurrentHashMap<Integer, UserLevel> getLevelSkins(Long _key) {
		return level_skins.get(_key);
	}
	public static UserRank getRankSkin(Long _key, Integer _key2) {
		if(rank_skins.containsKey(_key))
			return rank_skins.get(_key).get(_key2);
		else
			return null;
	}
	public static ConcurrentHashMap<Integer, UserRank> getRankSkins(Long _key) {
		return rank_skins.get(_key);
	}
	public static UserProfile getProfileSkin(Long _key, Integer _key2) {
		if(profile_skins.containsKey(_key))
			return profile_skins.get(_key).get(_key2);
		else
			return null;
	}
	public static ConcurrentHashMap<Integer, UserProfile> getProfileSkins(Long _key) {
		return profile_skins.get(_key);
	}
	public static UserIcon getIconSkin(Long _key, Integer _key2) {
		if(icon_skins.containsKey(_key))
			return icon_skins.get(_key).get(_key2);
		else
			return null;
	}
	public static ConcurrentHashMap<Integer, UserIcon> getIconSkins(Long _key) {
		return icon_skins.get(_key);
	}
	public static Integer getSubscriptionStatus(String _key) {
		return subscription_status.get(_key);
	}
	public static ArrayList<CategoryConf> getCategories(Long _key) {
		return categories.get(_key);
	}
	
	public static void removeMessagePool(final long _key, long _message_id) {
		final var message_pool = guild_message_pool.get(_key);
		message_pool.remove(_message_id);
		guild_message_pool.put(_key, message_pool);
	}
	public static void removeFilterLang(long _key) {
		filter_lang.remove(_key);
	}
	public static void removeQuerryResult(String _key) {
		querry_result.remove(_key);
	}
	public static void removeNameFilter(Long _key) {
		name_filter.remove(_key);
	}
	public static void removeGuildRanking(long _key) {
		guild_ranking.remove(_key);
	}
	public static void removeReactionRoles(Long _key) {
		reaction_roles.remove(_key);
	}
	public static void removeRankingRoles(long _key) {
		ranking_roles.remove(_key);
	}
	public static void clearRankingLevels() {
		ranking_levels.clear();
	}
	public static void removeQuiz(int _key) {
		quiz.remove(_key);
	}
	public static void clearQuiz() {
		quiz.clear();
	}
	public static void removeQuizWinners() {
		if(quiz_winners.size() > 0) {
			for(Member member : quiz_winners.keySet()) {
				if(quiz_winners.get(member) == 1) {
					quiz_winners.remove(member);
				}
				else {
					quiz_winners.put(member, (quiz_winners.get(member)-1));
				}
			}
		}
	}
	public static void clearQuizWinners() {
		quiz_winners.clear();
	}
	public static void clearShopContent() {
		shopContent.clear();
	}
	public static void removeDailyItems(long _key) {
		daily_items.remove(_key);
	}
	public static void clearDailyItems() {
		daily_items.clear();
	}
	public static void removeDiscordRoles(long _key) {
		discordRoles.remove(_key);
	}
	public static void clearDiscordRoles() {
		discordRoles.clear();
	}
	public static void removeFeeds(long _key) {
		feeds.remove(_key);
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
	public static void removeChannels(Long _key) {
		channels.remove(_key);
	}
	public static void clearCommentedUsers() {
		commentedUsers.clear();
	}
	public static void clearTempCache(String _key) {
		tempCache.remove(_key);
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
	public static void removeURLBlacklist(Long _key) {
		urlBlacklist.remove(_key);
	}
	public static void clearURLBlacklist() {
		urlBlacklist.clear();
	}
	public static void removeURLWhitelist(Long _key) {
		urlWhitelist.remove(_key);
	}
	public static void clearURLWhitelist() {
		urlWhitelist.clear();
	}
	public static void removeTweetBlacklist(Long _key) {
		tweetBlacklist.remove(_key);
	}
	public static void clearTweetBlacklist() {
		tweetBlacklist.clear();
	}
	public static void clearActionlog() {
		actionlog.clear();
	}
	public static void removeWatchlist(String _key) {
		watchlist.remove(_key);
	}
	public static void removeStatus(long _key) {
		status.remove(_key);
	}
	public static void removeCensoreMessage(long _key) {
		censorMessage.remove(_key);
	}
	public static void removeFilterThreshold(long _key) {
		filter_threshold.remove(_key);
	}
	public static void removeHeavyCensoringThread(long _key) {
		heavyCensoringThread.remove(_key);
	}
	public static void removeSpamDetection(String _key) {
		spamDetection.remove(_key);
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
	public static void clearLevelSkins(Long _key) {
		level_skins.remove(_key);
	}
	public static void clearRankSkins() {
		rank_skins.clear();
	}
	public static void clearRankSkins(Long _key) {
		rank_skins.remove(_key);
	}
	public static void clearProfileSkins() {
		profile_skins.clear();
	}
	public static void clearProfileSkins(Long _key) {
		profile_skins.remove(_key);
	}
	public static void clearIconSkins() {
		icon_skins.clear();
	}
	public static void clearIconSkins(Long _key) {
		icon_skins.remove(_key);
	}
	public static void removeSubscriptionStatus(String _key) {
		subscription_status.remove(_key);
	}
	public static void removeCategories(Long _key) {
		categories.remove(_key);
	}
	public static void clearCategories() {
		categories.clear();
	}
}
