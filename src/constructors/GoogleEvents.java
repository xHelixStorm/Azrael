package constructors;

/**
 * Event detail with the api support
 * @author xHelixStorm
 *
 */

public class GoogleEvents {
	private int event_id;
	private String event;
	private boolean docsSupport;
	private boolean spreadsheetsSupport;
	private boolean driveSupport;
	private boolean restrictable;
	private boolean forceRestriction;
	
	/**
	 * Main constructor
	 * @param _event_id
	 * @param _event
	 * @param _docsSupport
	 * @param _spreadsheetsSupport
	 * @param _driveSupport
	 * @param _restrictable
	 * @param _forceRestriction
	 */
	
	public GoogleEvents(int _event_id, String _event, boolean _docsSupport, boolean _spreadsheetsSupport, boolean _driveSupport, boolean _restrictable, boolean _forceRestriction) {
		this.event_id = _event_id;
		this.event = _event;
		this.docsSupport = _docsSupport;
		this.spreadsheetsSupport = _spreadsheetsSupport;
		this.driveSupport = _driveSupport;
		this.restrictable = _restrictable;
		this.forceRestriction = _forceRestriction;
	}
	
	/**
	 * Retrieve the event id
	 * @return
	 */
	
	public int getEventID() {
		return this.event_id;
	}
	
	/**
	 * Retrieve the description of the event
	 * @return
	 */
	
	public String getEvent() {
		return this.event;
	}
	
	/**
	 * Check if this event is supported for documents
	 * @return
	 */
	
	public boolean areDocsSupported() {
		return this.docsSupport;
	}
	
	/**
	 * Check if this event is supported for spreadsheets
	 * @return
	 */
	
	public boolean areSpreadsheetsSupported() {
		return this.spreadsheetsSupport;
	}
	
	/**
	 * Check if this event is supported for drive
	 * @return
	 */
	
	public boolean isDriveSupported() {
		return this.driveSupport;
	}
	
	/**
	 * Check if this event can be restricted to a text channel
	 * @return
	 */
	
	public boolean getRestrictable() {
		return this.restrictable;
	}
	
	/**
	 * Check if the restriction has to be forced
	 * @return
	 */
	
	public boolean getForceRestriction() {
		return this.forceRestriction;
	}
}
