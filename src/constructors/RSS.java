package constructors;

public class RSS {
	private String url; 
	private String format;
	
	public RSS(String _url, String _format) {
		this.url = _url;
		this.format = _format;
	}
	
	public String getURL() {
		return url;
	}
	public String getFormat() {
		return format;
	}
}
