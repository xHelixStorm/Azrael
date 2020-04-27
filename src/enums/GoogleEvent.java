package enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum for all Google events
 * @author xHelixStorm
 *
 */

public enum GoogleEvent {
	/**
	 * Enums defined here 
	 */
	
	MUTE			(1, "MUTE"),
	MUTE_READD		(2, "MUTE_READD"),
	UNMUTE			(3, "UNMUTE"),
	UNMUTE_MANUAL	(4, "UNMUTE_MANUAL"),
	KICK			(5, "KICK"),
	BAN				(6, "BAN"),
	UNBAN			(7, "UNBAN");
	
	/**
	 * Maps defined here to retrieve enum either by id or value
	 */
	
	private static final Map<Integer, GoogleEvent> BY_ID = new HashMap<Integer, GoogleEvent>();
	private static final Map<String, GoogleEvent> BY_VALUE = new HashMap<String, GoogleEvent>();
	
	/**
	 * Map setter
	 */
	
	static {
		for(GoogleEvent e : values()) {
			BY_ID.put(e.id, e);
			BY_VALUE.put(e.value, e);
		}
	}
	
	public final int id;
	public final String value;
	
	/**
	 * Default constructor
	 * @param _id
	 * @param _value
	 */
	
	private GoogleEvent(int _id, String _value) {
		this.id = _id;
		this.value = _value;
	}
	
	/**
	 * Retrieve enum by id
	 * @param _id
	 * @return
	 */
	
	public static GoogleEvent valueOfId(int _id) {
		return BY_ID.get(_id);
	}
	
	/**
	 * Retrieve enum by value
	 * @param _value
	 * @return
	 */
	
	public static GoogleEvent valueOfEvent(String _value) {
		return BY_VALUE.get(_value);
	}
}
