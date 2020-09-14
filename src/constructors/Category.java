package constructors;

/**
 * Class which shortly summarize Category types
 * @author xHelixStorm
 *
 */

public class Category {
	private String type;
	private String typeName;
	
	/**
	 * Category constructor
	 * @param _category_id id of the constructor
	 * @param _name constructor's name
	 */
	
	public Category(String _type, String _typeName) {
		this.type = _type;
		this.typeName = _typeName;
	}
	
	/**
	 * Category type
	 * @return
	 */
	
	public String getType() {
		return this.type;
	}
	
	/**
	 * Category type name
	 * @return
	 */
	
	public String getTypeName() {
		return this.typeName;
	}
}
