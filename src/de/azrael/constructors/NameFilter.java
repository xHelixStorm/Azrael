package de.azrael.constructors;

public class NameFilter {
	private String name;
	private boolean kick;
	
	public NameFilter(String _name, boolean _kick) {
		this.name = _name;
		this.kick = _kick;
	}
	
	public String getName() {
		return name;
	}
	public boolean getKick() {
		return kick;
	}
}
