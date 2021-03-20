package de.azrael.constructors;

/**
 * Class for registered categories
 * @author xHelixStorm
 *
 */

public class CategoryConf {
	private long category_id;
	private String type;
	
	/**
	 * Constructor
	 * @param _category_id ID of the category
	 * @param _type categorie's type
	 */
	
	public CategoryConf(long _category_id, String _type) {
		this.category_id = _category_id;
		this.type = _type;
	}
	
	/**
	 * Retrieve the id of the category
	 * @return
	 */
	
	public long getCategoryID() {
		return this.category_id;
	}
	
	/**
	 * Retrieve the type of the category
	 * @return
	 */
	
	public String getType() {
		return this.type;
	}
}
