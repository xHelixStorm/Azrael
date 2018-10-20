package core;

import java.time.LocalDateTime;

public class Messages {
	private long user_id;
	private String user_name;
	private long guild_id;
	private long channel_id;
	private String channel_name;
	private String message;
	private long message_id;
	private LocalDateTime time;
	
	public void setUserID(long _user_id) {
		user_id = _user_id;
	}
	public void setUsername(String _user_name) {
		user_name = _user_name;
	}
	public void setGuildID(long _guild_id) {
		guild_id = _guild_id;
	}
	public void setChannelID(long _channel_id) {
		channel_id = _channel_id;
	}
	public void setChannelName(String _channel_name) {
		channel_name = _channel_name;
	}
	public void setMessage(String _message) {
		message = _message;
	}
	public void setMessageID(long _message_id) {
		message_id = _message_id;
	}
	public void setTime(LocalDateTime _time) {
		time = _time;
	}
	
	public long getUserID() {
		return user_id;
	}
	public String getUserName() {
		return user_name;
	}
	public long getGuildID() {
		return guild_id;
	}
	public long getChannelID() {
		return channel_id;
	}
	public String getChannelName() {
		return channel_name;
	}
	public String getMessage() {
		return message;
	}
	public long getMessageID() {
		return message_id;
	}
	public LocalDateTime getTime() {
		return time;
	}
}
