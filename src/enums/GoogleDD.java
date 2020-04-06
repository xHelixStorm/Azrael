package enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum for all google data dictionary items
 * @author xHelixStorm
 *
 */

public enum GoogleDD {
	USER_ID		(1, "user_id"),
	NAME		(2, "name"),
	USERNAME	(3, "username");
	
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
	
	/**
	 * Default constructor
	 * @param _id
	 * @param _item
	 */
	
	private GoogleDD(int _id, String _item) {
		this.id = _id;
		this.item = _item;
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
}
