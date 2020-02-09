package constructors;

/**
 * Collect message and channel (used together with SpamDetection)
 * @author xHelixStorm
 *
 */

public class SpamMessages {
	private String message;
	private long channel_id;
	
	/**
	 * Constructor
	 * @param _message
	 * @param _channel_id
	 */
	public SpamMessages(String _message, long _channel_id) {
		this.message = _message;
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
	 * Retrieve the current channel id
	 * @return channel_id
	 */
	
	public long getChannelID() {
		return this.channel_id;
	}
}
