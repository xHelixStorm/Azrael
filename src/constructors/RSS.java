package constructors;

import java.util.ArrayList;

public class RSS {
	private String url; 
	private String format;
	private int type;
	private boolean videos;
	private boolean pictures;
	private boolean text;
	private long channel_id;
	private ArrayList<String> childTweets;
	
	public RSS(String _url, String _format, int _type, boolean _videos, boolean _pictures, boolean _text, long _channel_id, ArrayList<String> _childTweets) {
		this.url = _url;
		this.format = _format;
		this.type = _type;
		this.videos = _videos;
		this.pictures = _pictures;
		this.text = _text;
		this.channel_id = _channel_id;
		this.childTweets = _childTweets;
	}
	
	public String getURL() {
		return this.url;
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
	public ArrayList<String> getChildTweets() {
		return this.childTweets;
	}
}
