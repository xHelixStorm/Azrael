package de.azrael.constructors;

public class Skins {
	private int item_id;
	private String shop_description;
	private long price;
	private String skin_type;
	private String skin_description;
	private String skin_fullDescription;
	private String thumbnail;
	
	public void setItemID(int _item_id){
		item_id = _item_id;
	}
	public void setShopDescription(String _shop_description) {
		shop_description = _shop_description;
	}
	public void setPrice(long _price) {
		price = _price;
	}
	public void setSkinType(String _skin_type) {
		skin_type = _skin_type;
	}
	public void setSkinDescription(String _skin_description) {
		skin_description = _skin_description;
	}
	public void setSkinFullDescription(String _skin_fullDescription) {
		skin_fullDescription = _skin_fullDescription;
	}
	public void setThumbnail(String _thumbnail) {
		thumbnail = _thumbnail;
	}
	
	public int getItemID() {
		return item_id;
	}
	public String getShopDescription() {
		return shop_description;
	}
	public long getPrice() {
		return price;
	}
	public String getSkinType() {
		return skin_type;
	}
	public String getSkinDescription() {
		return skin_description;
	}
	public String getSkinFullDescription() {
		return skin_fullDescription;
	}
	public String getThumbnail() {
		return thumbnail;
	}
}
