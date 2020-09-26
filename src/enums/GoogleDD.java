package enums;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum for all google data dictionary items
 * @author xHelixStorm
 *
 */

public enum GoogleDD {
	TIMESTAMP			(1, "TIMESTAMP", 1),
	USER_ID				(2, "USER_ID", 0),
	NAME				(3, "NAME", 2),
	USERNAME			(4, "USERNAME", 2),
	REPORTER_NAME		(5, "REPORTER_NAME", 2),
	REPORTER_USERNAME	(6, "REPORTER_USERNAME", 2),
	REASON				(7, "REASON", 2),
	TIME				(8, "TIME", 2),
	ACTION				(9, "ACTION", 3),
	WARNING				(10, "WARNING", 0),
	UNMUTE_TIME			(11, "UNMUTE_TIME", 1),
	PLACEHOLDER			(12, "PLACEHOLDER", 0),
	GUILD_ID			(13, "GUILD_ID", 0),
	GUILD_NAME			(14, "GUILD_NAME", 2),
	ROLE_ID				(15, "ROLE_ID", 0),
	ROLE_NAME			(16, "ROLE_NAME", 2),
	OLD_NAME			(17, "OLD_NAME", 2),
	NEW_NAME			(18, "NEW_NAME", 2),
	MESSAGE_ID			(19, "MESSAGE_ID", 0),
	MESSAGE				(20, "MESSAGE", 0),
	UP_VOTE				(21, "UP_VOTE", 0),
	DOWN_VOTE			(22, "DOWN_VOTE", 0),
	PING				(23, "PING", 0),
	MEMBER_COUNT		(24, "MEMBER_COUNT", 0),
	GUILDS_COUNT		(25, "GUILDS_COUNT", 0),
	SCREEN_URL			(26, "SCREEN_URL", 0);
	
	/**
	 * Maps defined here to retrieve enum either by id or value
	 */
	
	private static final Map<Integer, GoogleDD> BY_ID = new HashMap<Integer, GoogleDD>();
	private static final Map<String, GoogleDD> BY_ITEM = new HashMap<String, GoogleDD>();
	
	/**
	 * Map setter
	 */
	
	static {
		for(GoogleDD e : values()) {
			BY_ID.put(e.id, e);
			BY_ITEM.put(e.item, e);
		}
	}
	
	public final int id;
	public final String item;
	public final int type;
	
	/**
	 * Default constructor
	 * @param _id
	 * @param _item
	 * @param _type
	 */
	
	private GoogleDD(int _id, String _item, int _type) {
		this.id = _id;
		this.item = _item;
		this.type = _type;
	}
	
	/**
	 * Retrieve enum by id
	 * @param _id
	 * @return
	 */
	
	public static GoogleDD valueOfId(int _id) {
		return BY_ID.get(_id);
	}
	
	/**
	 * Retrieve enum by value
	 * @param _value
	 * @return
	 */
	
	public static GoogleDD valueOfItem(String _item) {
		return BY_ITEM.get(_item);
	}
	
	/**
	 * Format received values based on the type of the enum
	 * @param _value
	 * @param _format
	 * @return
	 */
	
	public String valueFormatter(Object _value, String _format) {
		String formattedValue = null;
		
		switch(this.type) {
			//format the timestamp with a date formatter
			case 1 -> {
				Timestamp timestamp = (Timestamp)_value;
				if(_format != null && !_format.isBlank())
					formattedValue = new SimpleDateFormat(_format).format(timestamp);
				else
					formattedValue = timestamp.toString();
			}
			//format names, usernames and so on
			case 2 -> {
				String value = (String)_value;
				if(_format == null)
					formattedValue = value;
				if(_format.equalsIgnoreCase("UPPER_CASE"))
					formattedValue = value.toUpperCase();
				else if(_format.equalsIgnoreCase("LOWER_CASE"))
					formattedValue = value.toLowerCase();
				else
					formattedValue = value;
			}
			//overwrite these values with the formatter if not blank
			case 3 -> {
				if(_format == null || _format.isBlank())
					formattedValue = (String)_value;
				else
					formattedValue = _format;
			}
			//No formats available
			default -> {
				if(_value instanceof Long)
					formattedValue = ""+(long)_value;
				else if(_value instanceof Integer)
					formattedValue = ""+(int)_value;
				else if(_value instanceof Double)
					formattedValue = ""+(double)_value;
				else
					formattedValue = (String)_value;
			}
		}
		return formattedValue;
	}
}
