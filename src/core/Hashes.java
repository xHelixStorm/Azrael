package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import rankingSystem.Rank;
import rankingSystem.Ranks;

public class Hashes {
	private static final int max_message_pool_size = 1000;
	private static final LinkedHashMap<Long, String> message_pool = new LinkedHashMap<Long, String>(){
		private static final long serialVersionUID = 7505333508062985903L;
		@Override
		@SuppressWarnings("rawtypes")
        protected boolean removeEldestEntry(final Map.Entry eldest) {
            return size() > max_message_pool_size;
        }
    };
    
    private static final Map<String, ArrayList<String>> querry_result = new HashMap<String, ArrayList<String>>();
    private static final Map<Long, Integer> message_removed = new HashMap<Long, Integer>();
    private static final Map<Long, Guilds> status = new HashMap<Long, Guilds>();
    private static final Map<Long, Rank> ranking = new HashMap<Long, Rank>();
    private static final Map<String, Rank> ranking_roles = new HashMap<String, Rank>();
    private static final Map<Integer, Ranks> ranking_levels = new HashMap<Integer, Ranks>();
	
	public static void addMessagePool(long _message_id, String _message) {
		message_pool.put(_message_id, _message);
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
	public static void addRanking(Long _key, Rank _details){
		ranking.put(_key, _details);
	}
	public static void addRankingRoles(String _key, Rank _details){
		ranking_roles.put(_key, _details);
	}
	public static void addRankingLevels(Integer _key, Ranks _levels){
		ranking_levels.put(_key, _levels);
	}
	public static String getMessagePool(long _message_id) {
		return message_pool.get(_message_id);
	}
	public static LinkedHashMap<Long, String> getWholeMessagePool(){
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
	public static Rank getRanking(long _key){
		return ranking.get(_key);
	}
	public static Rank getRankingRoles(String _key){
		return ranking_roles.get(_key);
	}
	public static Map<String, Rank> getMapOfRankingRoles(){
		return ranking_roles;
	}
	public static Map<Integer, Ranks> getMapOfRankingLevels(){
		return ranking_levels;
	}
	public static Ranks getRankingLevels(int _key){
		return ranking_levels.get(_key);
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
	public static void removeRanking(long _key){
		ranking.remove(_key);
	}
}
