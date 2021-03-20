package de.azrael.enums;

public enum Category {
	VER ("ver");
	
	private String type;
	
	private Category(String _type) {
		this.type = _type;
	}
	
	public String getType() {
		return this.type;
	}
}
