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
import constructors.Channels;
import constructors.Dailies;
import constructors.Guilds;
import constructors.Level;
import constructors.Messages;
import constructors.NameFilter;
import constructors.Quizes;
import constructors.RSS;
import constructors.Rank;
import constructors.RejoinTask;
import constructors.Roles;
import constructors.Skills;
import constructors.Skins;
import constructors.Watchlist;
import constructors.WeaponAbbvs;
import constructors.WeaponStats;
import constructors.Weapons;
import net.dv8tion.jda.api.entities.Member;

public class Hashes {
	private static final int max_message_pool_size = 100000;
	private static final LinkedHashMap<Long, ArrayList<Messages>> message_pool = new LinkedHashMap<Long, ArrayList<Messages>>() {
		private static final long serialVersionUID = 7505333508062985903L;
		@Override
		@SuppressWarnings("rawtypes")
        protected boolean removeEldestEntry(final Map.Entry eldest) {
            return size() > max_message_pool_size;
        }
    };
    private static final int max_ranking_pool_size = 500;
    private static final LinkedHashMap<String, Rank> ranking = new LinkedHashMap<String, Rank>() {
		private static final long serialVersionUID = 7054847678737381845L;
		@Override
		@SuppressWarnings("rawtypes")
    	protected boolean removeEldestEntry(final Map.Entry eldest) {
    		return size() > max_ranking_pool_size;
    	}
    };
    
    private static final ConcurrentMap<String, ArrayList<String>> querry_result = new ConcurrentHashMap<String, ArrayList<String>>();
    private static final ConcurrentMap<Long, ArrayList<NameFilter>> name_filter = new ConcurrentHashMap<Long, ArrayList<NameFilter>>();
    private static final ConcurrentMap<Long, ArrayList<String>> filter_lang = new ConcurrentHashMap<Long, ArrayList<String>>();
    private static final Set<String> actionlog = new LinkedHashSet<String>();
    private static final ConcurrentHashMap<Long, Guilds> status = new ConcurrentHashMap<Long, Guilds>();
    private static final ConcurrentMap<Long, ArrayList<Roles>> ranking_roles = new ConcurrentHashMap<Long, ArrayList<Roles>>();
    private static final LinkedHashMap<String, ArrayList<Level>> ranking_levels = new LinkedHashMap<String, ArrayList<Level>>();
    private static final ConcurrentMap<Long, ArrayList<Roles>> reaction_roles = new ConcurrentHashMap<Long, ArrayList<Roles>>();
    private static final ConcurrentMap<Long, Long> reaction_message = new ConcurrentHashMap<Long, Long>();
    private static final ConcurrentMap<Integer, Quizes> quiz = new ConcurrentHashMap<Integer, Quizes>();
    private static final ConcurrentMap<Member, Integer> quiz_winners = new ConcurrentHashMap<Member, Integer>();
    private static final ConcurrentMap<Long, ArrayList<Rank>> rankListLevel = new ConcurrentHashMap<Long, ArrayList<Rank>>();
    private static final ConcurrentMap<Long, ArrayList<Rank>> rankListRank = new ConcurrentHashMap<Long, ArrayList<Rank>>();
    private static final ConcurrentMap<Long, ArrayList<Rank>> rankListProfile = new ConcurrentHashMap<Long, ArrayList<Rank>>();
    private static final ConcurrentMap<Long, ArrayList<Rank>> rankListIcons = new ConcurrentHashMap<Long, ArrayList<Rank>>();
    private static final ConcurrentMap<Long, ArrayList<Skins>> shopContent = new ConcurrentHashMap<Long, ArrayList<Skins>>();
    private static final ConcurrentMap<Long, ArrayList<Dailies>> daily_items = new ConcurrentHashMap<Long, ArrayList<Dailies>>();
    private static final ConcurrentMap<Long, ArrayList<Roles>> discordRoles = new ConcurrentHashMap<Long, ArrayList<Roles>>();
    private static final ConcurrentMap<Long, ArrayList<RSS>> feeds = new ConcurrentHashMap<Long, ArrayList<RSS>>();
    private static final ConcurrentMap<Long, ArrayList<Weapons>> weaponShopContent = new ConcurrentHashMap<Long, ArrayList<Weapons>>();
    private static final ConcurrentMap<Long, ArrayList<String>> weaponCategories = new ConcurrentHashMap<Long, ArrayList<String>>();
    private static final ConcurrentMap<Long, ArrayList<WeaponAbbvs>> weaponAbbvs = new ConcurrentHashMap<Long, ArrayList<WeaponAbbvs>>();
    private static final LinkedHashMap<Long, ArrayList<WeaponStats>> weaponStats = new LinkedHashMap<Long, ArrayList<WeaponStats>>();
    private static final ConcurrentMap<String, Integer> themes = new ConcurrentHashMap<String, Integer>();
    private static final ConcurrentMap<Long, ArrayList<Channels>> channels = new ConcurrentHashMap<Long, ArrayList<Channels>>();
    private static final ConcurrentHashMap<String, String> commentedUsers = new ConcurrentHashMap<String, String>();
    private static final ConcurrentHashMap<String, Cache> tempCache = new ConcurrentHashMap<String, Cache>();
    private static final ConcurrentMap<Long, ArrayList<Skills>> skillShop = new ConcurrentHashMap<Long, ArrayList<Skills>>();
    private static final ConcurrentMap<String, RejoinTask> rejoinTask = new ConcurrentHashMap<String, RejoinTask>();
    private static final Set<String> globalURLBlacklist = new HashSet<String>();
    private static final ConcurrentHashMap<Long, ArrayList<String>> urlBlacklist = new ConcurrentHashMap<Long, ArrayList<String>>();
    private static final ConcurrentHashMap<Long, ArrayList<String>> urlWhitelist = new ConcurrentHashMap<Long, ArrayList<String>>();
    private static final ConcurrentHashMap<Long, ArrayList<String>> tweetBlacklist = new ConcurrentHashMap<Long, ArrayList<String>>();
    private static final ConcurrentHashMap<String, Watchlist> watchlist = new ConcurrentHashMap<String, Watchlist>();
    private static final ConcurrentHashMap<Long, Boolean> heavyCensoring = new ConcurrentHashMap<Long, Boolean>();
    private static final ConcurrentHashMap<Long, ArrayList<String>> censorMessage = new ConcurrentHashMap<Long, ArrayList<String>>();
    private static final ConcurrentHashMap<Long, Guilds> old_guild_settings = new ConcurrentHashMap<Long, Guilds>();
    private static final ConcurrentHashMap<Long, String> filter_threshold = new ConcurrentHashMap<Long, String>();
    private static final ConcurrentHashMap<Long, Thread> heavyCensoringThread = new ConcurrentHashMap<Long, Thread>();
	
	public static void addMessagePool(long _message_id, ArrayList<Messages> _messages) {
		message_pool.put(_message_id, _messages);
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
	public static void addRanking(String _key, Rank _details) {
		ranking.put(_key, _details);
	}
	public static void addRankingRoles(Long _key, ArrayList<Roles> _details) {
		ranking_roles.put(_key, _details);
	}
	public static void addRankingLevels(String _key, ArrayList<Level> _levels) {
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
	public static void addRankListLevel(Long _key, ArrayList<Rank> _rankList) {
		rankListLevel.put(_key, _rankList);
	}
	public static void addRankListRank(Long _key, ArrayList<Rank> _rankList) {
		rankListRank.put(_key, _rankList);
	}
	public static void addRankListProfile(Long _key, ArrayList<Rank> _rankList) {
		rankListProfile.put(_key, _rankList);
	}
	public static void addRankListIcons(Long _key, ArrayList<Rank> _rankList) {
		rankListIcons.put(_key, _rankList);
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
	public static void addTheme(String _key, Integer _id) {
		themes.put(_key, _id);
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
	public static void addRejoinTask(String _key, RejoinTask task) {
		rejoinTask.put(_key, task);
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
	
	
	public static ArrayList<Messages> getMessagePool(long _message_id) {
		return message_pool.get(_message_id);
	}
	public static ArrayList<String> getFilterLang(long _channel_id) {
		return filter_lang.get(_channel_id);
	}
	public static LinkedHashMap<Long, ArrayList<Messages>> getWholeMessagePool() {
		return message_pool;
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
	public static Rank getRanking(String _key) {
		return ranking.get(_key);
	}
	public static ArrayList<Roles> getRankingRoles(Long _key) {
		return ranking_roles.get(_key);
	}
	public static ArrayList<Level> getRankingLevels(String _key) {
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
	public static ArrayList<Rank> getRankListLevel(Long _key) {
		return rankListLevel.get(_key);
	}
	public static ArrayList<Rank> getRankListRank(Long _key) {
		return rankListRank.get(_key);
	}
	public static ArrayList<Rank> getRankListProfile(Long _key) {
		return rankListProfile.get(_key);
	}
	public static ArrayList<Rank> getRankListIcons(Long _key) {
		return rankListIcons.get(_key);
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
	public static Integer getTheme(String _key) {
		return themes.get(_key);
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
	public static RejoinTask getRejoinTask(String _key) {
		return rejoinTask.get(_key);
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
	
	public static void removeMessagePool(long _message_id) {
		message_pool.remove(_message_id);
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
	public static void removeRanking(String _key) {
		ranking.remove(_key);
	}
	public static void removeReactionRoles(Long _key) {
		reaction_roles.remove(_key);
	}
	public static void removeRankingRoles(long _key) {
		ranking_roles.remove(_key);
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
	public static void removeRankListLevel(long _key) {
		rankListLevel.remove(_key);
	}
	public static void removeRankListRank(long _key) {
		rankListRank.remove(_key);
	}
	public static void removeRankListProfile(long _key) {
		rankListProfile.remove(_key);
	}
	public static void removeRankListIcons(long _key) {
		rankListIcons.remove(_key);
	}
	public static void clearRankListLevel() {
		rankListLevel.clear();
	}
	public static void clearRankListRank() {
		rankListRank.clear();
	}
	public static void clearRankListProfile() {
		rankListProfile.clear();
	}
	public static void clearRankListIcons() {
		rankListIcons.clear();
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
	public static void clearThemes() {
		themes.clear();
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
	public static void removeRejoinTask(String _key) {
		rejoinTask.remove(_key);
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
}
