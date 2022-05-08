package de.azrael.constructors;

/**
 * Class which shortly summarize Category types
 * @author xHelixStorm
 *
 */

public class Category {
	private String type;
	private String typeName;
	private int registerType;
	
	/**
	 * Category constructor
	 * @param _type
	 * @param _typeName constructor's name
	 * @param _registerType 
	 */
	
	public Category(String _type, String _typeName, int _registerType) {
		this.type = _type;
		this.typeName = _typeName;
		this.registerType = _registerType;
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
	
	/**
	 * Category register type
	 * @return
	 */
	
	public int getRegisterType() {
		return this.registerType;
	}
}
