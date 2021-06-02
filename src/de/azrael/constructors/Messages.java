package de.azrael.constructors;

import java.time.ZonedDateTime;

public class Messages {
	private long user_id;
	private String user_name;
	private long guild_id;
	private long channel_id;
	private String channel_name;
	private String message;
	private long message_id;
	private ZonedDateTime time;
	private boolean edit;
	private boolean isUserBot;
	
	public void setUserID(long _user_id) {
		this.user_id = _user_id;
	}
	public void setUsername(String _user_name) {
		this.user_name = _user_name;
	}
	public void setGuildID(long _guild_id) {
		this.guild_id = _guild_id;
	}
	public void setChannelID(long _channel_id) {
		this.channel_id = _channel_id;
	}
	public void setChannelName(String _channel_name) {
		this.channel_name = _channel_name;
	}
	public void setMessage(String _message) {
		this.message = _message;
	}
	public void setMessageID(long _message_id) {
		this.message_id = _message_id;
	}
	public void setTime(ZonedDateTime _zonedDateTime) {
		this.time = _zonedDateTime;
	}
	public void setIsEdit(boolean _edit) {
		this.edit = _edit;
	}
	public void setIsUserBot(boolean _isUserBot) {
		this.isUserBot = _isUserBot;
	}
	
	public long getUserID() {
		return this.user_id;
	}
	public String getUserName() {
		return this.user_name;
	}
	public long getGuildID() {
		return this.guild_id;
	}
	public long getChannelID() {
		return this.channel_id;
	}
	public String getChannelName() {
		return this.channel_name;
	}
	public String getMessage() {
		return this.message;
	}
	public long getMessageID() {
		return this.message_id;
	}
	public ZonedDateTime getTime() {
		return this.time;
	}
	public boolean isEdit() {
		return this.edit;
	}
	public boolean isUserBot() {
		return this.isUserBot;
	}
}
