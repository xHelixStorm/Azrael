package constructors;

/**
 * Constructor to get the event and starting point
 */

import enums.GoogleEvent;

public class GoogleSheet {
	private GoogleEvent event;
	private String sheetRowStart;
	
	/**
	 * Default constructor
	 * @param _event
	 * @param _sheetRowStart
	 */
	
	public GoogleSheet(GoogleEvent _event, String _sheetRowStart) {
		this.event = _event;
		this.sheetRowStart = _sheetRowStart;
	}
	
	/**
	 * Retrieve the saved event
	 * @return GoogleEvent
	 */
	
	public GoogleEvent getEvent() {
		return this.event;
	}
	
	/**
	 * Retrieve the starting point
	 * @return String
	 */
	
	public String getSheetRowStart() {
		return this.sheetRowStart;
	}
}
