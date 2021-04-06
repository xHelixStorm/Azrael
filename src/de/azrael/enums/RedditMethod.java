package de.azrael.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Available reddit methods to fetch user related content
 * @author xHelixStorm
 *
 */

public enum RedditMethod {
	USER_OVERVIEW		("user_overview", "user/{}/overview"),
	USER_SUBMITTED		("user_submitted", "user/{}/submitted"),
	USER_COMMENTS		("user_comments", "user/{}/comments"),
	USER_UPVOTED		("user_upvoted", "user/{}/upvoted"),
	USER_DOWNVOTED		("user_downvoted", "user/{}/downvoted"),
	USER_HIDDEN			("user_hidden", "user/{}/hidden"),
	USER_SAVED			("user_saved", "user/{}/saved"),
	USER_GILDED			("user_gilded", "user/{}/gilded"),
	SUBREDDIT_CONTENT	("subreddit_content", "r/{}");
	
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
	public final String url;
	
	/**
	 * Default constructor
	 * @param _type
	 */
	
	private RedditMethod(String _type, String _url) {
		this.type = _type;
		this.url = _url;
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
