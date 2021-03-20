package de.azrael.constructors;

public class Quizes {
	private String question = null;
	private String answer1 = null;
	private String answer2 = null;
	private String answer3 = null;
	private String hint1 = null;
	private String hint2 = null;
	private String hint3 = null;
	private String reward = null;
	private boolean used = false;
	
	public void setQuestion(String _question) {
		this.question = _question;
	}
	public void setAnswer1(String _answer1) {
		this.answer1 = _answer1;
	}
	public void setAnswer2(String _answer2) {
		this.answer2 = _answer2;
	}
	public void setAnswer3(String _answer3) {
		this.answer3 = _answer3;
	}
	public void setHint1(String _hint1) {
		this.hint1 = _hint1;
	}
	public void setHint2(String _hint2) {
		this.hint2 = _hint2;
	}
	public void setHint3(String _hint3) {
		this.hint3 = _hint3;
	}
	public void setReward(String _reward) {
		this.reward = _reward;
	}
	public void setUsed(boolean _used) {
		this.used = _used;
	}
	
	public String getQuestion() {
		return this.question;
	}
	public String getAnswer1() {
		return this.answer1;
	}
	public String getAnswer2() {
		return this.answer2;
	}
	public String getAnswer3() {
		return this.answer3;
	}
	public String getHint1() {
		return this.hint1;
	}
	public String getHint2() {
		return this.hint2;
	}
	public String getHint3() {
		return this.hint3;
	}
	public String getReward() {
		return reward;
	}
	public boolean isUsed() {
		return this.used;
	}
}
