package de.azrael.constructors;

import de.azrael.enums.GoogleDD;

/**
 * Returned mapping of one column before writing into a Spreadsheet
 */

public class GoogleSheetColumn {
	private GoogleDD item;
	private String formatter;
	private int column;
	
	/**
	 * Default constructor
	 * @param _event_id
	 * @param _formatter
	 * @param _column
	 */
	
	public GoogleSheetColumn(int _item_id, String _formatter, int _column) {
		this.item = GoogleDD.valueOfId(_item_id);
		this.formatter = _formatter;
		this.column = _column;
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
	
	/**
	 * Retrieve the current column
	 * @return
	 */
	
	public int getColumn() {
		return this.column;
	}
}
