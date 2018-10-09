package rankingSystem;

public class Ranks {
	int level;
	int experience;
	int currency;
	long assign_role;
	
	public void setLevel(int _level){
		level = _level;
	}
	public void setExperience(int _experience){
		experience = _experience;
	}
	public void setCurrency(int _currency){
		currency = _currency;
	}
	public void setAssignRole(long _assign_role){
		assign_role = _assign_role;
	}
	
	public int getLevel(){
		return level;
	}
	public int getExperience(){
		return experience;
	}
	public int getCurrency(){
		return currency;
	}
	public long getAssignRole(){
		return assign_role;
	}
}
