package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import inventory.Dailies;
import net.dv8tion.jda.core.entities.Member;
import rankingSystem.Rank;
import rankingSystem.Ranks;
import rankingSystem.Skins;

public class Hashes {
	private static final int max_message_pool_size = 1500;
	private static final LinkedHashMap<Long, Messages> message_pool = new LinkedHashMap<Long, Messages>(){
		private static final long serialVersionUID = 7505333508062985903L;
		@Override
		@SuppressWarnings("rawtypes")
        protected boolean removeEldestEntry(final Map.Entry eldest) {
            return size() > max_message_pool_size;
        }
    };
    private static final int max_ranking_pool_size = 500;
    private static final LinkedHashMap<String, Rank> ranking = new LinkedHashMap<String, Rank>(){
		private static final long serialVersionUID = 7054847678737381845L;
		@Override
		@SuppressWarnings("rawtypes")
    	protected boolean removeEldestEntry(final Map.Entry eldest) {
    		return size() > max_ranking_pool_size;
    	}
    };
    
    private static final ConcurrentMap<String, ArrayList<String>> querry_result = new ConcurrentHashMap<String, ArrayList<String>>();
    private static final Map<Long, ArrayList<String>> filter_lang = new HashMap<Long, ArrayList<String>>();
    private static final Map<Long, Integer> message_removed = new HashMap<Long, Integer>();
    private static final Map<Long, Guilds> status = new HashMap<Long, Guilds>();
    private static final Map<String, Rank> ranking_roles = new HashMap<String, Rank>();
    private static final Map<String, Ranks> ranking_levels = new HashMap<String, Ranks>();
    private static final Map<String, Roles> roles = new HashMap<String, Roles>();
    private static final Map<Long, Long> reaction_message = new HashMap<Long, Long>();
    private static final Map<Integer, Quizes> quiz = new HashMap<Integer, Quizes>();
    private static final Map<Member, Integer> quiz_winners = new HashMap<Member, Integer>();
    private static final Map<String, ArrayList<Rank>> rankList = new HashMap<String, ArrayList<Rank>>();
    private static final Map<String, ArrayList<Skins>> shopContent = new HashMap<String, ArrayList<Skins>>();
    private static final Map<String, ArrayList<Dailies>> daily_items = new HashMap<String, ArrayList<Dailies>>();
    private static final Map<Long, Roles> discordRoles = new HashMap<Long, Roles>();
    private static final Map<Long, ArrayList<RSS>> feeds = new HashMap<Long, ArrayList<RSS>>();
	
	public static void addMessagePool(long _message_id, Messages _message) {
		message_pool.put(_message_id, _message);
	}
	public static void addFilterLang(long _channel_id, ArrayList<String> _filter_lang){
		filter_lang.put(_channel_id, _filter_lang);
	}
	public static void addQuerryResult(String _key, ArrayList<String> _result) {
		querry_result.put(_key, _result);
	}
	public static void addMessageRemoved(Long _key, Integer _count){
		message_removed.put(_key, _count);
	}
	public static void addStatus(Long _key, Guilds _status){
		status.put(_key, _status);
	}
	public static void addRanking(String _key, Rank _details){
		ranking.put(_key, _details);
	}
	public static void addRankingRoles(String _key, Rank _details){
		ranking_roles.put(_key, _details);
	}
	public static void addRankingLevels(String _key, Ranks _levels){
		ranking_levels.put(_key, _levels);
	}
	public static void addRoles(String _key, Roles _roles) {
		roles.put(_key, _roles);
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
	public static void addRankList(String _key, ArrayList<Rank> _rankList) {
		rankList.put(_key, _rankList);
	}
	public static void addShopContent(String _key, ArrayList<Skins> _skin_content) {
		shopContent.put(_key, _skin_content);
	}
	public static void addDailyItems(String _key, ArrayList<Dailies> _daily_items) {
		daily_items.put(_key, _daily_items);
	}
	public static void addDiscordRole(Long _key, Roles _role) {
		discordRoles.put(_key, _role);
	}
	public static void addFeeds(Long _key, ArrayList<RSS> _feeds) {
		feeds.put(_key, _feeds);
	}
	
	public static Messages getMessagePool(long _message_id) {
		return message_pool.get(_message_id);
	}
	public static ArrayList<String> getFilterLang(long _channel_id){
		return filter_lang.get(_channel_id);
	}
	public static LinkedHashMap<Long, Messages> getWholeMessagePool(){
		return message_pool;
	}
	public static ArrayList<String> getQuerryResult(String _key) {
		return querry_result.get(_key);
	}
	public static Integer getMessageRemoved(long _key){
		return message_removed.get(_key);
	}
	public static Guilds getStatus(long _key){
		return status.get(_key);
	}
	public static Rank getRanking(String _key){
		return ranking.get(_key);
	}
	public static Rank getRankingRoles(String _key){
		return ranking_roles.get(_key);
	}
	public static Map<String, Rank> getMapOfRankingRoles(){
		return ranking_roles;
	}
	public static Map<String, Ranks> getMapOfRankingLevels(){
		return ranking_levels;
	}
	public static Ranks getRankingLevels(String _key){
		return ranking_levels.get(_key);
	}
	public static Roles getRoles(String _key) {
		return roles.get(_key);
	}
	public static Long getReactionMessage(Long _key) {
		return reaction_message.get(_key);
	}
	public static Quizes getQuiz(int _key) {
		return quiz.get(_key);
	}
	public static Map<Integer, Quizes> getWholeQuiz(){
		return quiz;
	}
	public static Integer getQuizWinners(Member _key) {
		return quiz_winners.get(_key);
	}
	public static ArrayList<Rank> getRankList(String _key){
		return rankList.get(_key);
	}
	public static ArrayList<Skins> getShopContent(String _key){
		return shopContent.get(_key);
	}
	public static ArrayList<Dailies> getDailyItems(String _key){
		return daily_items.get(_key);
	}
	public static Roles getDiscordRole(Long _key) {
		return discordRoles.get(_key);
	}
	public static ArrayList<RSS> getFeed(Long _key) {
		return feeds.get(_key);
	}
	
	public static void removeMessagePool(long _message_id) {
		message_pool.remove(_message_id);
	}
	public static void removeQuerryResult(String _key) {
		querry_result.remove(_key);
	}
	public static void removeMessageRemoved(long _key){
		message_removed.remove(_key);
	}
	public static void removeRanking(String _key){
		ranking.remove(_key);
	}
	public static void removeRoles() {
		roles.clear();
	}
	public static void removeRankingRoles() {
		ranking_roles.clear();
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
	public static void clearRankList() {
		rankList.clear();
	}
	public static void clearShopContent() {
		shopContent.clear();
	}
	public static void clearDailyItems() {
		daily_items.clear();
	}
	public static void clearDiscordRoles() {
		discordRoles.clear();
	}
	public static void clearFeeds() {
		feeds.clear();
	}
}
