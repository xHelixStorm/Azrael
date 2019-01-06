package core;

public class Warning {
	private int warning_id;
	private double timer;
	private String description;
	
	public Warning() {
		this.warning_id = 0;
		this.timer = 0;
		this.description = "";
	}
	
	public Warning(int _warning_id, double _timer, String _description) {
		this.warning_id = _warning_id;
		this.timer = _timer;
		this.description = _description;
	}
	
	public int getWarningID() {
		return warning_id;
	}
	public double getTimer() {
		return timer;
	}
	public String getDescription() {
		return description;
	}
}
