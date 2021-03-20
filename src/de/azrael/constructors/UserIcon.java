package de.azrael.constructors;

public class UserIcon {
	private int skin;
	private String skinDescription;
	private String fileType;
	private int line;
	private String source;
	
	public int getSkin() {
		return skin;
	}
	public String getSkinDescription() {
		return skinDescription;
	}
	public String getFileType() {
		return fileType;
	}
	public int getLine() {
		return line;
	}
	public String getSource() {
		return source;
	}
	public void setSkin(int skin) {
		this.skin = skin;
	}
	public void setSkinDescription(String skinDescription) {
		this.skinDescription = skinDescription;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public void setLine(int line) {
		this.line = line;
	}
	public void setSource(String source) {
		this.source = source;
	}
}
