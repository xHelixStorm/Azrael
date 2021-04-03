package de.azrael.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Available reddit methods to fetch user related content
 * @author xHelixStorm
 *
 */

public enum RedditMethod {
	OVERVIEW	("overview"),
	SUBMITTED	("submitted"),
	COMMENTS	("comments"),
	UPVOTED		("upvoted"),
	DOWNVOTED	("downvoted"),
	HIDDEN		("hidden"),
	SAVED		("saved"),
	GILDED		("gilded");
	
	/**
	 * Map defined to retrieve enum by type
	 */
	
	private static final Map<String, RedditMethod> BY_TYPE = new HashMap<String, RedditMethod>();
	
	static {
		for(RedditMethod e : values()) {
			BY_TYPE.put(e.type, e);
		}
	}
	
	public final String type;
	
	/**
	 * Default constructor
	 * @param _type
	 */
	
	private RedditMethod(String _type) {
		this.type = _type;
	}
	
	/**
	 * Return enum by type
	 * @param type reddit fetch method
	 * @return Enum of method
	 */
	
	public static RedditMethod valueOfType(String type) {
		return BY_TYPE.get(type.toLowerCase());
	}
}
