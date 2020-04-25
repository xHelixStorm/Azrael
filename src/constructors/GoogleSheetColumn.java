package constructors;

import enums.GoogleDD;

/**
 * Returned mapping of one column before writing into a Spreadsheet
 */

public class GoogleSheetColumn {
	private GoogleDD item;
	private String formatter;
	
	/**
	 * Default constructor
	 * @param _event_id
	 * @param _formatter
	 */
	
	public GoogleSheetColumn(int _item_id, String _formatter) {
		this.item = GoogleDD.valueOfId(_item_id);
		this.formatter = _formatter;
	}
	
	/**
	 * Retrieve the item
	 * @return
	 */
	
	public GoogleDD getItem() {
		return this.item;
	}
	
	/**
	 * Retrieve the formatter
	 * @return
	 */
	
	public String getFormatter() {
		return this.formatter;
	}
}
