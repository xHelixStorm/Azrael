package constructors;

import java.sql.Timestamp;

public class Ranking {
	private long user_id;
	private int level;
	private int currentExperience;
	private int rankUpExperience;
	private long experience;
	private long currency;
	private long current_role;
	private int rank;
	
	private int ranking_level;
	private int ranking_rank;
	private int ranking_profile;
	private int ranking_icon;
	
	private int daily_experience;
	private Timestamp daily_reset;
	
	private long role_id;
	private String role_name;
	private int level_requirement;
	private long guild_id;
	
	private int theme_id;
	
	private int weapon1;
	private int weapon2;
	private int weapon3;
	private int skill;
	
	private Timestamp last_update;
	
	public void setUser_ID(long _user_id) {
		this.user_id = _user_id;
	}
	public void setLevel(int _level) {
		this.level = _level;
	}
	public void setCurrentExperience(int _currentExperience) {
		this.currentExperience = _currentExperience;
	}
	public void setRankUpExperience(int _rankUpExperience) {
		this.rankUpExperience = _rankUpExperience;
	}
	public void setExperience(long _experience) {
		this.experience = _experience;
	}
	public void setCurrency(long _currency) {
		this.currency = _currency;
	}
	public void setCurrentRole(long _current_role) {
		this.current_role = _current_role;
	}
	public void setRank(int _rank) {
		this.rank = _rank;
	}
	public void setRankingLevel(int _ranking_level) {
		this.ranking_level = _ranking_level;
	}
	public void setRankingRank(int _ranking_rank) {
		this.ranking_rank = _ranking_rank;
	}
	public void setRankingProfile(int _ranking_profile) {
		this.ranking_profile = _ranking_profile;
	}
	public void setRankingIcon(int _ranking_icon) {
		this.ranking_icon = _ranking_icon;
	}
	public void setDailyExperience(int _daily_experience) {
		this.daily_experience = _daily_experience;
	}
	public void setDailyReset(Timestamp _daily_reset) {
		this.daily_reset = _daily_reset;
	}
	public void setRoleID(long _role_id) {
		this.role_id = _role_id;
	}
	public void setRole_Name(String _role_name) {
		this.role_name = _role_name;
	}
	public void setLevel_Requirement(int _level_requirement) {
		this.level_requirement = _level_requirement;
	}
	public void setGuildID(long _guild_id) {
		this.guild_id = _guild_id;
	}
	public void setThemeID(int _theme_id) {
		this.theme_id = _theme_id;
	}
	public void setWeapon1(int _weapon1) {
		this.weapon1 = _weapon1;
	}
	public void setWeapon2(int _weapon2) {
		this.weapon2 = _weapon2;
	}
	public void setWeapon3(int _weapon3) {
		this.weapon3 = _weapon3;
	}
	public void setSkill(int _skill) {
		this.skill = _skill;
	}
	public void setLastUpdate(Timestamp _last_update) {
		this.last_update = _last_update;
	}
	
	public long getUser_ID() {
		return this.user_id;
	}
	public int getLevel() {
		return this.level;
	}
	public int getCurrentExperience() {
		return this.currentExperience;
	}
	public int getRankUpExperience() {
		return this.rankUpExperience;
	}
	public long getExperience() {
		return this.experience;
	}
	public long getCurrency() {
		return this.currency;
	}
	public long getCurrentRole() {
		return this.current_role;
	}
	public int getRank() {
		return this.rank;
	}
	public int getRankingLevel() {
		return this.ranking_level;
	}
	public int getRankingRank() {
		return this.ranking_rank;
	}
	public int getRankingProfile() {
		return this.ranking_profile;
	}
	public int getRankingIcon() {
		return this.ranking_icon;
	}
	public int getDailyExperience() {
		return this.daily_experience;
	}
	public Timestamp getDailyReset() {
		return this.daily_reset;
	}
	public long getRoleID() {
		return this.role_id;
	}
	public String getRole_Name() {
		return this.role_name;
	}
	public int getLevel_Requirement() {
		return this.level_requirement;
	}
	public long getGuildID() {
		return this.guild_id;
	}
	public int getThemeID() {
		return this.theme_id;
	}
	public int getWeapon1() {
		return this.weapon1;
	}
	public int getWeapon2() {
		return this.weapon2;
	}
	public int getWeapon3() {
		return this.weapon3;
	}
	public int getSkill() {
		return this.skill;
	}
	public Timestamp getLastUpdate() {
		return this.last_update;
	}
}
