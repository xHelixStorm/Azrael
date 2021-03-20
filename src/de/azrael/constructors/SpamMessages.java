package de.azrael.constructors;

/**
 * Collect message and channel (used together with SpamDetection)
 * @author xHelixStorm
 *
 */

public class SpamMessages {
	private String message;
	private long message_id;
	private long channel_id;
	
	/**
	 * Constructor
	 * @param _message
	 * @param _message_id
	 * @param _channel_id
	 */
	public SpamMessages(String _message, long _message_id, long _channel_id) {
		this.message = _message;
		this.message_id = _message_id;
		this.channel_id = _channel_id;
	}
	
	/**
	 * Retrieve the current message
	 * @return message
	 */
	
	public String getMessage() {
		return this.message;
	}
	
	/**
	 * Retrieve the message id
	 * @return message_id
	 */
	
	public long getMessageID() {
		return this.message_id;
	}
	
	/**
	 * Retrieve the current channel id
	 * @return channel_id
	 */
	
	public long getChannelID() {
		return this.channel_id;
	}
}
