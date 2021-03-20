package de.azrael.constructors;

public class CompMap {
	private int map_id;
	private String name;
	private String image;
	
	public CompMap() {
		//empty constructor
	}
	
	public CompMap(int _map_id, String _name, String _image) {
		this.map_id = _map_id;
		this.name = _name;
		this.image = _image;
	}
	
	public int getMapID() {
		return this.map_id;
	}
	public String getName() {
		return this.name;
	}
	public String getImage() {
		return this.image;
	}
}
