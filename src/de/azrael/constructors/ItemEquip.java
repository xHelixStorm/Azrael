package de.azrael.constructors;

public class ItemEquip {
	private int item_id;
	private String description;
	private String stat;
	private String abbreviation;
	
	public ItemEquip(int _item_id, String _description) {
		this.item_id = _item_id;
		this.description = _description;
		this.stat = "";
		this.abbreviation = "";
	}
	
	public ItemEquip(int _item_id, String _description, String _stat, String _abbreviation) {
		this.item_id = _item_id;
		this.description = _description;
		this.stat = _stat;
		this.abbreviation = _abbreviation;
	}
	
	public int getItemId() {
		return item_id;
	}
	public String getDescription() {
		return description;
	}
	public String getStat() {
		return stat;
	}
	public String getAbbreviation() {
		return abbreviation;
	}
}
