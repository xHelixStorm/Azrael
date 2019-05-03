package core;

public class Patchnote {
	private String message1;
	private String message2;
	private String date;
	private String title;
	
	public Patchnote(String _message1, String _message2, String _date) {
		this.message1 = _message1;
		this.message2 = _message2;
		this.date = _date;
		this.title = null;
	}
	public Patchnote(String _message1, String _message2, String _date, String _title) {
		this.message1 = _message1;
		this.message2 = _message2;
		this.date = _date;
		this.title = _title;
	}
	
	public String getMessage1() {
		return message1;
	}
	public String getMessage2() {
		return message2;
	}
	public String getDate() {
		return date;
	}
	public String getTitle() {
		return title;
	}
}
