package constructors;

public class Quizes {
	private String question = "";
	private String answer1 = "";
	private String answer2 = "";
	private String answer3 = "";
	private String hint1 = "";
	private String hint2 = "";
	private String hint3 = "";
	private String reward = "";
	
	public void setQuestion(String _question) {
		question = _question;
	}
	public void setAnswer1(String _answer1) {
		answer1 = _answer1;
	}
	public void setAnswer2(String _answer2) {
		answer2 = _answer2;
	}
	public void setAnswer3(String _answer3) {
		answer3 = _answer3;
	}
	public void setHint1(String _hint1) {
		hint1 = _hint1;
	}
	public void setHint2(String _hint2) {
		hint2 = _hint2;
	}
	public void setHint3(String _hint3) {
		hint3 = _hint3;
	}
	public void setReward(String _reward) {
		reward = _reward;
	}
	
	public String getQuestion() {
		return question;
	}
	public String getAnswer1() {
		return answer1;
	}
	public String getAnswer2() {
		return answer2;
	}
	public String getAnswer3() {
		return answer3;
	}
	public String getHint1() {
		return hint1;
	}
	public String getHint2() {
		return hint2;
	}
	public String getHint3() {
		return hint3;
	}
	public String getReward() {
		return reward;
	}
}
