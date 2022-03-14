package de.azrael.constructors;

import java.util.ArrayList;

public class Subscription {
	private String url; 
	private long guild_id; 
	private String format;
	private int type;
	private boolean videos;
	private boolean pictures;
	private boolean text;
	private long channel_id;
	private String name;
	private ArrayList<String> childTweets;
	
	public Subscription(String _url, long _guild_id, String _format, int _type, boolean _videos, boolean _pictures, boolean _text, long _channel_id, String _name, ArrayList<String> _childTweets) {
		this.url = _url;
		this.guild_id = _guild_id;
		this.format = _format;
		this.type = _type;
		this.videos = _videos;
		this.pictures = _pictures;
		this.text = _text;
		this.channel_id = _channel_id;
		this.name = _name;
		this.childTweets = _childTweets;
	}
	
	public String getURL() {
		return this.url;
	}
	public long getGuildId() {
		return this.guild_id;
	}
	public String getFormat() {
		return this.format;
	}
	public int getType() {
		return this.type;
	}
	public boolean getVideos() {
		return this.videos;
	}
	public boolean getPictures() {
		return this.pictures;
	}
	public boolean getText() {
		return this.text;
	}
	public long getChannelID() {
		return this.channel_id;
	}
	public String getName() {
		return this.name;
	}
	public ArrayList<String> getChildTweets() {
		return this.childTweets;
	}
}
