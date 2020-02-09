package constructors;

import java.util.ArrayList;

/**
 * Constructor to collect spam messages
 * @author xHelixStorm
 *
 */

public class SpamDetection {
	private long expires;
	private long time;
	private int count;
	private ArrayList<SpamMessages> messages;
	
	/**
	 * Constructor
	 */
	
	public SpamDetection(long _expires) {
		this.expires = _expires;
		messages = new ArrayList<SpamMessages>();
		time = System.currentTimeMillis();
		count = 0;
	}
	
	/**
	 * check if the current messages are not valid anymore
	 * @return
	 */
	
	public boolean isExpired() {
		return (this.time - System.currentTimeMillis()) <= 0;
	}
	
	/**
	 * Retrieve the size of messages
	 * @return count
	 */
	
	public int size() {
		return this.count;
	}
	
	/**
	 * Remove all collected messages and set the count to 0
	 * @return
	 */
	
	public SpamDetection clear() {
		time = System.currentTimeMillis();
		messages.clear();
		count = 0;
		return this;
	}
	
	/**
	 * Add a message and count up the size
	 * @param _message
	 * @param _channel_id
	 */
	
	public void put(String _message, long _channel_id) {
		messages.add(new SpamMessages(_message, _channel_id));
		time = System.currentTimeMillis()+expires;
		count++;
	}
	
	/**
	 * Retrieve all messages
	 * @return messages
	 */
	
	public ArrayList<SpamMessages> getMessages() {
		return messages;
	}
}
