package constructors;

import java.sql.Timestamp;

public class Rank {
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
	private String level_description;
	private String rank_description;
	private String profile_description;
	private String icon_description;
	private int price;
	
	private int bar_color_rank;
	private int bar_color_profile;
	private boolean additional_exp_text_rank;
	private boolean additional_percent_text_rank;
	private boolean additional_exp_text_profile;
	private boolean additional_percent_text_profile;
	
	private int color_r_level;
	private int color_r_rank;
	private int color_r_profile;
	private int color_g_level;
	private int color_g_rank;
	private int color_g_profile;
	private int color_b_level;
	private int color_b_rank;
	private int color_b_profile;
	
	private int rankx_level;
	private int rankx_rank;
	private int rankx_profile;
	private int ranky_level;
	private int ranky_rank;
	private int ranky_profile;
	
	private int rank_width_level;
	private int rank_width_rank;
	private int rank_width_profile;
	private int rank_height_level;
	private int rank_height_rank;
	private int rank_height_profile;
	
	private int daily_experience;
	private Timestamp daily_reset;
	
	private long role_id;
	private String role_name;
	private int level_requirement;
	private long guild_id;
	
	private int theme_id;
	
	public void setUser_ID(long _user_id) {
		user_id = _user_id;
	}
	public void setLevel(int _level) {
		level = _level;
	}
	public void setCurrentExperience(int _currentExperience) {
		currentExperience = _currentExperience;
	}
	public void setRankUpExperience(int _rankUpExperience) {
		rankUpExperience = _rankUpExperience;
	}
	public void setExperience(long _experience) {
		experience = _experience;
	}
	public void setCurrency(long _currency) {
		currency = _currency;
	}
	public void setCurrentRole(long _current_role) {
		current_role = _current_role;
	}
	public void setRank(int _rank) {
		rank = _rank;
	}
	public void setRankingLevel(int _ranking_level) {
		ranking_level = _ranking_level;
	}
	public void setRankingRank(int _ranking_rank) {
		ranking_rank = _ranking_rank;
	}
	public void setRankingProfile(int _ranking_profile) {
		ranking_profile = _ranking_profile;
	}
	public void setRankingIcon(int _ranking_icon) {
		ranking_icon = _ranking_icon;
	}
	public void setLevelDescription(String _level_description) {
		level_description = _level_description;
	}
	public void setRankDescription(String _rank_description) {
		rank_description = _rank_description;
	}
	public void setProfileDescription(String _profile_description) {
		profile_description = _profile_description;
	}
	public void setIconDescription(String _icon_description) {
		icon_description = _icon_description;
	}
	public void setPrice(int _price) {
		price = _price;
	}
	public void setBarColorRank(int _bar_color_rank) {
		bar_color_rank = _bar_color_rank;
	}
	public void setBarColorProfile(int _bar_color_profile) {
		bar_color_profile = _bar_color_profile;
	}
	public void setAdditionalExpTextRank(boolean _additional_exp_text_rank) {
		additional_exp_text_rank = _additional_exp_text_rank;
	}
	public void setAdditionalPercentTextRank(boolean _additional_percent_text_rank) {
		additional_percent_text_rank = _additional_percent_text_rank;
	}
	public void setAdditionalExpTextProfile(boolean _additional_exp_text_profile) {
		additional_exp_text_profile = _additional_exp_text_profile;
	}
	public void setAdditionalPercentTextProfile(boolean _additional_percent_text_profile) {
		additional_percent_text_profile = _additional_percent_text_profile;
	}
	public void setColorRLevel(int _color_r_level) {
		color_r_level = _color_r_level;
	}
	public void setColorGLevel(int _color_g_level) {
		color_g_level = _color_g_level;
	}
	public void setColorBLevel(int _color_b_level) {
		color_b_level = _color_b_level;
	}
	public void setColorRRank(int _color_r_rank) {
		color_r_rank = _color_r_rank;
	}
	public void setColorGRank(int _color_g_rank) {
		color_g_rank = _color_g_rank;
	}
	public void setColorBRank(int _color_b_rank) {
		color_b_rank = _color_b_rank;
	}
	public void setColorRProfile(int _color_r_profile) {
		color_r_profile = _color_r_profile;
	}
	public void setColorGProfile(int _color_g_profile) {
		color_g_profile = _color_g_profile;
	}
	public void setColorBProfile(int _color_b_profile) {
		color_b_profile = _color_b_profile;
	}
	public void setRankXLevel(int _rankx_level) {
		rankx_level = _rankx_level;
	}
	public void setRankXRank(int _rankx_rank) {
		rankx_rank = _rankx_rank;
	}
	public void setRankXProfile(int _rankx_profile) {
		rankx_profile = _rankx_profile;
	}
	public void setRankYLevel(int _ranky_level) {
		ranky_level = _ranky_level;
	}
	public void setRankYRank(int _ranky_rank) {
		ranky_rank = _ranky_rank;
	}
	public void setRankYProfile(int _ranky_profile) {
		ranky_profile = _ranky_profile;
	}
	public void setRankWidthLevel(int _rank_width_level) {
		rank_width_level = _rank_width_level;
	}
	public void setRankWidthRank(int _rank_width_rank) {
		rank_width_rank = _rank_width_rank;
	}
	public void setRankWidthProfile(int _rank_width_profile) {
		rank_width_profile = _rank_width_profile;
	}
	public void setRankHeightLevel(int _rank_height_level) {
		rank_height_level = _rank_height_level;
	}
	public void setRankHeightRank(int _rank_height_rank) {
		rank_height_rank = _rank_height_rank;
	}
	public void setRankHeightProfile(int _rank_height_profile) {
		rank_height_profile = _rank_height_profile;
	}
	public void setDailyExperience(int _daily_experience) {
		daily_experience = _daily_experience;
	}
	public void setDailyReset(Timestamp _daily_reset) {
		daily_reset = _daily_reset;
	}
	public void setRoleID(long _role_id) {
		role_id = _role_id;
	}
	public void setRole_Name(String _role_name) {
		role_name = _role_name;
	}
	public void setLevel_Requirement(int _level_requirement) {
		level_requirement = _level_requirement;
	}
	public void setGuildID(long _guild_id) {
		guild_id = _guild_id;
	}
	public void setThemeID(int _theme_id) {
		theme_id = _theme_id;
	}
	
	public long getUser_ID() {
		return user_id;
	}
	public int getLevel() {
		return level;
	}
	public int getCurrentExperience() {
		return currentExperience;
	}
	public int getRankUpExperience() {
		return rankUpExperience;
	}
	public long getExperience() {
		return experience;
	}
	public long getCurrency() {
		return currency;
	}
	public long getCurrentRole() {
		return current_role;
	}
	public int getRank() {
		return rank;
	}
	public int getRankingLevel() {
		return ranking_level;
	}
	public int getRankingRank() {
		return ranking_rank;
	}
	public int getRankingProfile() {
		return ranking_profile;
	}
	public int getRankingIcon() {
		return ranking_icon;
	}
	public String getLevelDescription() {
		return level_description;
	}
	public String getRankDescription() {
		return rank_description;
	}
	public String getProfileDescription() {
		return profile_description;
	}
	public String getIconDescription() {
		return icon_description;
	}
	public int getPrice() {
		return price;
	}
	public int getBarColorRank() {
		return bar_color_rank;
	}
	public int getBarColorProfile() {
		return bar_color_profile;
	}
	public boolean getAdditionalExpTextRank() {
		return additional_exp_text_rank;
	}
	public boolean getAdditionalPercentTextRank() {
		return additional_percent_text_rank;
	}
	public boolean getAdditionalExpTextProfile() {
		return additional_exp_text_profile;
	}
	public boolean getAdditionalPercentTextProfile() {
		return additional_percent_text_profile;
	}
	public int getColorRLevel() {
		return color_r_level;
	}
	public int getColorGLevel() {
		return color_g_level;
	}
	public int getColorBLevel() {
		return color_b_level;
	}
	public int getColorRRank() {
		return color_r_rank;
	}
	public int getColorGRank() {
		return color_g_rank;
	}
	public int getColorBRank() {
		return color_b_rank;
	}
	public int getColorRProfile() {
		return color_r_profile;
	}
	public int getColorGProfile() {
		return color_g_profile;
	}
	public int getColorBProfile() {
		return color_b_profile;
	}
	public int getRankXLevel() {
		return rankx_level;
	}
	public int getRankXRank() {
		return rankx_rank;
	}
	public int getRankXProfile() {
		return rankx_profile;
	}
	public int getRankYLevel() {
		return ranky_level;
	}
	public int getRankYRank() {
		return ranky_rank;
	}
	public int getRankYProfile() {
		return ranky_profile;
	}
	public int getRankWidthLevel() {
		return rank_width_level;
	}
	public int getRankWidthRank() {
		return rank_width_rank;
	}
	public int getRankWidthProfile() {
		return rank_width_profile;
	}
	public int getRankHeightLevel() {
		return rank_height_level;
	}
	public int getRankHeightRank() {
		return rank_height_rank;
	}
	public int getRankHeightProfile() {
		return rank_height_profile;
	}
	public int getDailyExperience() {
		return daily_experience;
	}
	public Timestamp getDailyReset() {
		return daily_reset;
	}
	public long getRoleID() {
		return role_id;
	}
	public String getRole_Name() {
		return role_name;
	}
	public int getLevel_Requirement() {
		return level_requirement;
	}
	public long getGuildID() {
		return guild_id;
	}
	public int getThemeID() {
		return theme_id;
	}
}
