package constructors;

public class Channels {
	private long guild_id;
	private String guild_name;
	private long channel_id;
	private String channel_name;
	private String channel_type;
	private String channel_type_name;
	private String lang_filter;
	
	public void setGuild_ID(long _guild_id){
		guild_id = _guild_id;
	}
	public void setGuild_Name(String _guild_name){
		guild_name = _guild_name;
	}
	public void setChannel_ID(long _channel_id){
		channel_id = _channel_id;
	}
	public void setChannel_Name(String _channel_name){
		channel_name = _channel_name;
	}
	public void setChannel_Type(String _channel_type){
		channel_type = _channel_type;
	}
	public void setChannel_Type_Name(String _channel_type_name){
		channel_type_name = _channel_type_name;
	}
	public void setLang_Filter(String _lang_filter){
		lang_filter = _lang_filter;
	}
	
	public Long getGuild_ID(){
		return guild_id;
	}
	public String getGuild_Name(){
		return guild_name;
	}
	public Long getChannel_ID(){
		return channel_id;
	}
	public String getChannel_Name(){
		return channel_name;
	}
	public String getChannel_Type(){
		return channel_type;
	}
	public String getChannel_Type_Name(){
		return channel_type_name;
	}
	public String getLang_Filter(){
		return lang_filter;
	}
}
