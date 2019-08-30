package constructors;

public class Channels {
	private long guild_id;
	private String guild_name;
	private long channel_id;
	private String channel_name;
	private String channel_type;
	private String channel_type_name;
	private String lang_filter;
	private boolean url_censoring;
	private boolean txt_removal;
	
	public void setGuild_ID(long _guild_id) {
		this.guild_id = _guild_id;
	}
	public void setGuild_Name(String _guild_name) {
		this.guild_name = _guild_name;
	}
	public void setChannel_ID(long _channel_id) {
		this.channel_id = _channel_id;
	}
	public void setChannel_Name(String _channel_name) {
		this.channel_name = _channel_name;
	}
	public void setChannel_Type(String _channel_type) {
		this.channel_type = _channel_type;
	}
	public void setChannel_Type_Name(String _channel_type_name) {
		this.channel_type_name = _channel_type_name;
	}
	public void setLang_Filter(String _lang_filter) {
		this.lang_filter = _lang_filter;
	}
	public void setURLCensoring(boolean _url_censoring) {
		this.url_censoring = _url_censoring;
	}
	public void setTextRemoval(boolean _txt_removal) {
		this.txt_removal = _txt_removal;
	}
	
	public Long getGuild_ID() {
		return this.guild_id;
	}
	public String getGuild_Name() {
		return this.guild_name;
	}
	public Long getChannel_ID() {
		return this.channel_id;
	}
	public String getChannel_Name() {
		return this.channel_name;
	}
	public String getChannel_Type() {
		return this.channel_type;
	}
	public String getChannel_Type_Name() {
		return this.channel_type_name;
	}
	public String getLang_Filter() {
		return this.lang_filter;
	}
	public boolean getURLCensoring() {
		return this.url_censoring;
	}
	public boolean getTxtRemoval() {
		return this.txt_removal;
	}
}
