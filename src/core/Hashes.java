package core;

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
	
	public static void addMessagePool(long _message_id, String _message) {
		message_pool.put(_message_id, _message);
	}
	public static String getMessagePool(long _message_id) {
		return message_pool.get(_message_id);
	}
	public static void removeMessagePool(long _message_id) {
		message_pool.remove(_message_id);
	}
}
