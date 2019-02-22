package core;

public class Guilds {
	private String name;
	private int max_level;
	private int level_id;
	private String level_description;
	private int rank_id;
	private String rank_description;
	private int profile_id;
	private String profile_description;
	private int icon_id;
	private String icon_description;
	private boolean ranking_state;
	private long max_experience;
	private boolean max_exp_enabled;
	private String currency;
	private long randomshop_price;
	
	public void setName(String _name){
		name = _name;
	}
	public void setMaxLevel(int _max_level){
		max_level = _max_level;
	}
	public void setLevelID(int _level_id){
		level_id = _level_id;
	}
	public void setLevelDescription(String _level_description){
		level_description = _level_description;
	}
	public void setRankID(int _rank_id){
		rank_id = _rank_id;
	}
	public void setRankDescription(String _rank_description){
		rank_description = _rank_description;
	}
	public void setProfileID(int _profile_id){
		profile_id = _profile_id;
	}
	public void setProfileDescription(String _profile_description){
		profile_description = _profile_description;
	}
	public void setIconID(int _icon_id){
		icon_id = _icon_id;
	}
	public void setIconDescription(String _icon_description){
		icon_description = _icon_description;
	}
	public void setRankingState(boolean _ranking_state){
		ranking_state = _ranking_state;
	}
	public void setMaxExperience(long _max_experience){
		max_experience = _max_experience;
	}
	public void setMaxExpEnabled(boolean _max_exp_enabled){
		max_exp_enabled = _max_exp_enabled;
	}
	public void setCurrency(String _currency) {
		currency = _currency;
	}
	public void setRandomshopPrice(long _randomshop_price) {
		randomshop_price = _randomshop_price;
	}
	
	public String getName(){
		return name;
	}
	public int getMaxLevel(){
		return max_level;
	}
	public int getLevelID(){
		return level_id;
	}
	public String getLevelDescription(){
		return level_description;
	}
	public int getRankID(){
		return rank_id;
	}
	public String getRankDescription(){
		return rank_description;
	}
	public int getProfileID(){
		return profile_id;
	}
	public String getProfileDescription(){
		return profile_description;
	}
	public int getIconID(){
		return icon_id;
	}
	public String getIconDescription(){
		return icon_description;
	}
	public boolean getRankingState(){
		return ranking_state;
	}
	public long getMaxExperience(){
		return max_experience;
	}
	public boolean getMaxExpEnabled(){
		return max_exp_enabled;
	}
	public String getCurrency() {
		return currency;
	}
	public long getRandomshopPrice() {
		return randomshop_price;
	}
}
