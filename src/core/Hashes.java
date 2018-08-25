package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Hashes {
	private static final int max_message_pool_size = 10000;
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
	
	public static void addMessagePool(long _message_id, String _message) {
		message_pool.put(_message_id, _message);
	}
	public static void addQuerryResult(String _key, ArrayList<String> _result) {
		querry_result.put(_key, _result);
	}
	public static void addMessageRemoved(Long _key, Integer _count){
		message_removed.put(_key, _count);
	}
	public static String getMessagePool(long _message_id) {
		return message_pool.get(_message_id);
	}
	public static ArrayList<String> getQuerryResult(String _key) {
		return querry_result.get(_key);
	}
	public static Integer getMessageRemoved(Long _key){
		return message_removed.get(_key);
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
}
