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
	private int barx_rank;
	private int barx_profile;
	private int bary_rank;
	private int bary_profile;
	
	private boolean additional_exp_text_rank;
	private boolean additional_percent_text_rank;
	private boolean additional_exp_text_profile;
	private boolean additional_percent_text_profile;
	
	private int exp_textx_rank;
	private int exp_textx_profile;
	private int exp_texty_rank;
	private int exp_texty_profile;
	
	private int percent_textx_rank;
	private int percent_textx_profile;
	private int percent_texty_rank;
	private int percent_texty_profile;
	
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
	
	private int levelx_level;
	private int levelx_profile;
	private int levely_level;
	private int levely_profile;
	
	private int namex_level;
	private int namex_rank;
	private int namex_profile;
	private int namey_level;
	private int namey_rank;
	private int namey_profile;
	
	private int avatarx_rank;
	private int avatarx_profile;
	private int avatary_rank;
	private int avatary_profile;
	
	private int avatar_width_rank;
	private int avatar_width_profile;
	private int avatar_height_rank;
	private int avatar_height_profile;
	
	private int placementx_rank;
	private int placementx_profile;
	private int placementy_rank;
	private int placementy_profile;
	
	private int experiencex_profile;
	private int experiencey_profile;
	
	private int currencyx_profile;
	private int currencyy_profile;
	
	private int exp_reachx_profile;
	private int exp_reachy_profile;
	
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
	public void setLevelDescription(String _level_description) {
		this.level_description = _level_description;
	}
	public void setRankDescription(String _rank_description) {
		this.rank_description = _rank_description;
	}
	public void setProfileDescription(String _profile_description) {
		this.profile_description = _profile_description;
	}
	public void setIconDescription(String _icon_description) {
		this.icon_description = _icon_description;
	}
	public void setPrice(int _price) {
		this.price = _price;
	}
	public void setBarColorRank(int _bar_color_rank) {
		this.bar_color_rank = _bar_color_rank;
	}
	public void setBarColorProfile(int _bar_color_profile) {
		this.bar_color_profile = _bar_color_profile;
	}
	public void setBarXRank(int _barx_rank) {
		this.barx_rank = _barx_rank;
	}
	public void setBarXProfile(int _barx_profile) {
		this.barx_profile = _barx_profile;
	}
	public void setBarYRank(int _bary_rank) {
		this.bary_rank = _bary_rank;
	}
	public void setBarYProfile(int _bary_profile) {
		this.bary_profile = _bary_profile;
	}
	public void setAdditionalExpTextRank(boolean _additional_exp_text_rank) {
		this.additional_exp_text_rank = _additional_exp_text_rank;
	}
	public void setAdditionalPercentTextRank(boolean _additional_percent_text_rank) {
		this.additional_percent_text_rank = _additional_percent_text_rank;
	}
	public void setAdditionalExpTextProfile(boolean _additional_exp_text_profile) {
		this.additional_exp_text_profile = _additional_exp_text_profile;
	}
	public void setAdditionalPercentTextProfile(boolean _additional_percent_text_profile) {
		this.additional_percent_text_profile = _additional_percent_text_profile;
	}
	public void setExpTextXRank(int _exp_textx_rank) {
		this.exp_textx_rank = _exp_textx_rank;
	}
	public void setExpTextXProfile(int _exp_textx_profile) {
		this.exp_textx_profile = _exp_textx_profile;
	}
	public void setExpTextYRank(int _exp_texty_rank) {
		this.exp_texty_rank = _exp_texty_rank;
	}
	public void setExpTextYProfile(int _exp_texty_profile) {
		this.exp_texty_profile= _exp_texty_profile;
	}
	public void setPercentTextXRank(int _percent_textx_rank) {
		this.percent_textx_rank = _percent_textx_rank;
	}
	public void setPercentTextXProfile(int _percent_textx_profile) {
		this.percent_textx_profile = _percent_textx_profile;
	}
	public void setPercentTextYRank(int _percent_texty_rank) {
		this.percent_texty_rank = _percent_texty_rank;
	}
	public void setPercentTextYProfile(int _percent_texty_profile) {
		this.percent_texty_profile= _percent_texty_profile;
	}
	public void setColorRLevel(int _color_r_level) {
		this.color_r_level = _color_r_level;
	}
	public void setColorGLevel(int _color_g_level) {
		this.color_g_level = _color_g_level;
	}
	public void setColorBLevel(int _color_b_level) {
		this.color_b_level = _color_b_level;
	}
	public void setColorRRank(int _color_r_rank) {
		this.color_r_rank = _color_r_rank;
	}
	public void setColorGRank(int _color_g_rank) {
		this.color_g_rank = _color_g_rank;
	}
	public void setColorBRank(int _color_b_rank) {
		this.color_b_rank = _color_b_rank;
	}
	public void setColorRProfile(int _color_r_profile) {
		this.color_r_profile = _color_r_profile;
	}
	public void setColorGProfile(int _color_g_profile) {
		this.color_g_profile = _color_g_profile;
	}
	public void setColorBProfile(int _color_b_profile) {
		this.color_b_profile = _color_b_profile;
	}
	public void setRankXLevel(int _rankx_level) {
		this.rankx_level = _rankx_level;
	}
	public void setRankXRank(int _rankx_rank) {
		this.rankx_rank = _rankx_rank;
	}
	public void setRankXProfile(int _rankx_profile) {
		this.rankx_profile = _rankx_profile;
	}
	public void setRankYLevel(int _ranky_level) {
		this.ranky_level = _ranky_level;
	}
	public void setRankYRank(int _ranky_rank) {
		this.ranky_rank = _ranky_rank;
	}
	public void setRankYProfile(int _ranky_profile) {
		this.ranky_profile = _ranky_profile;
	}
	public void setRankWidthLevel(int _rank_width_level) {
		this.rank_width_level = _rank_width_level;
	}
	public void setRankWidthRank(int _rank_width_rank) {
		this.rank_width_rank = _rank_width_rank;
	}
	public void setRankWidthProfile(int _rank_width_profile) {
		this.rank_width_profile = _rank_width_profile;
	}
	public void setRankHeightLevel(int _rank_height_level) {
		this.rank_height_level = _rank_height_level;
	}
	public void setRankHeightRank(int _rank_height_rank) {
		this.rank_height_rank = _rank_height_rank;
	}
	public void setRankHeightProfile(int _rank_height_profile) {
		this.rank_height_profile = _rank_height_profile;
	}
	public void setLevelXLevel(int _levelx_level) {
		this.levelx_level = _levelx_level;
	}
	public void setLevelXProfile(int _levelx_profile) {
		this.levelx_profile = _levelx_profile;
	}
	public void setLevelYLevel(int _levely_level) {
		this.levely_level = _levely_level;
	}
	public void setLevelYProfile(int _levely_profile) {
		this.levely_profile = _levely_profile;
	}
	public void setNameXLevel(int _namex_level) {
		this.namex_level = _namex_level;
	}
	public void setNameXRank(int _namex_rank) {
		this.namex_rank = _namex_rank;
	}
	public void setNameXProfile(int _namex_profile) {
		this.namex_profile = _namex_profile;
	}
	public void setNameYLevel(int _namey_level) {
		this.namey_level = _namey_level;
	}
	public void setNameYRank(int _namey_rank) {
		this.namey_rank = _namey_rank;
	}
	public void setNameYProfile(int _namey_profile) {
		this.namey_profile = _namey_profile;
	}
	public void setAvatarXRank(int _avatarx_rank) {
		this.avatarx_rank = _avatarx_rank;
	}
	public void setAvatarXProfile(int _avatarx_profile) {
		this.avatarx_profile = _avatarx_profile;
	}
	public void setAvatarYRank(int _avatary_rank) {
		this.avatary_rank = _avatary_rank;
	}
	public void setAvatarYProfile(int _avatary_profile) {
		this.avatary_profile = _avatary_profile;
	}
	public void setAvatarWidthRank(int _avatar_width_rank) {
		this.avatar_width_rank = _avatar_width_rank;
	}
	public void setAvatarWidthProfile(int _avatar_width_profile) {
		this.avatar_width_profile = _avatar_width_profile;
	}
	public void setAvatarHeightRank(int _avatar_height_rank) {
		this.avatar_height_rank = _avatar_height_rank;
	}
	public void setAvatarHeightProfile(int _avatar_height_profile) {
		this.avatar_height_profile = _avatar_height_profile;
	}
	public void setPlacementXRank(int _placementx_rank) {
		this.placementx_rank = _placementx_rank;
	}
	public void setPlacementXProfile(int _placementx_profile) {
		this.placementx_profile = _placementx_profile;
	}
	public void setPlacementYRank(int _placementy_rank) {
		this.placementy_rank = _placementy_rank;
	}
	public void setPlacementYProfile(int _placementy_profile) {
		this.placementy_profile = _placementy_profile;
	}
	public void setExperienceXProfile(int _experiencex_profile) {
		this.experiencex_profile = _experiencex_profile;
	}
	public void setExperienceYProfile(int _experiencey_profile) {
		this.experiencey_profile = _experiencey_profile;
	}
	public void setCurrencyXProfile(int _currencyx_profile) {
		this.currencyx_profile = _currencyx_profile;
	}
	public void setCurrencyYProfile(int _currencyy_profile) {
		this.currencyy_profile = _currencyy_profile;
	}
	public void setExpReachXProfile(int _exp_reachx_profile) {
		this.exp_reachx_profile = _exp_reachx_profile;
	}
	public void setExpReachYProfile(int _exp_reachy_profile) {
		this.exp_reachy_profile = _exp_reachy_profile;
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
	public String getLevelDescription() {
		return this.level_description;
	}
	public String getRankDescription() {
		return this.rank_description;
	}
	public String getProfileDescription() {
		return this.profile_description;
	}
	public String getIconDescription() {
		return this.icon_description;
	}
	public int getPrice() {
		return this.price;
	}
	public int getBarColorRank() {
		return this.bar_color_rank;
	}
	public int getBarColorProfile() {
		return this.bar_color_profile;
	}
	public int getBarXRank() {
		return this.barx_rank;
	}
	public int getBarXProfile() {
		return this.barx_profile;
	}
	public int getBarYRank() {
		return this.bary_rank;
	}
	public int getBarYProfile() {
		return this.bary_profile;
	}
	public boolean getAdditionalExpTextRank() {
		return this.additional_exp_text_rank;
	}
	public boolean getAdditionalPercentTextRank() {
		return this.additional_percent_text_rank;
	}
	public boolean getAdditionalExpTextProfile() {
		return this.additional_exp_text_profile;
	}
	public boolean getAdditionalPercentTextProfile() {
		return this.additional_percent_text_profile;
	}
	public int getExpTextXRank() {
		return this.exp_textx_rank;
	}
	public int getExpTextXProfile() {
		return this.exp_textx_profile;
	}
	public int getExpTextYRank() {
		return this.exp_texty_rank;
	}
	public int getExpTextYProfile() {
		return this.exp_texty_profile;
	}
	public int getPercentTextXRank() {
		return this.percent_textx_rank;
	}
	public int getPercentTextXProfile() {
		return this.percent_textx_profile;
	}
	public int getPercentTextYRank() {
		return this.percent_texty_rank;
	}
	public int getPercentTextYProfile() {
		return this.percent_texty_profile;
	}
	public int getColorRLevel() {
		return this.color_r_level;
	}
	public int getColorGLevel() {
		return this.color_g_level;
	}
	public int getColorBLevel() {
		return this.color_b_level;
	}
	public int getColorRRank() {
		return this.color_r_rank;
	}
	public int getColorGRank() {
		return this.color_g_rank;
	}
	public int getColorBRank() {
		return this.color_b_rank;
	}
	public int getColorRProfile() {
		return this.color_r_profile;
	}
	public int getColorGProfile() {
		return this.color_g_profile;
	}
	public int getColorBProfile() {
		return this.color_b_profile;
	}
	public int getRankXLevel() {
		return this.rankx_level;
	}
	public int getRankXRank() {
		return this.rankx_rank;
	}
	public int getRankXProfile() {
		return this.rankx_profile;
	}
	public int getRankYLevel() {
		return this.ranky_level;
	}
	public int getRankYRank() {
		return this.ranky_rank;
	}
	public int getRankYProfile() {
		return this.ranky_profile;
	}
	public int getRankWidthLevel() {
		return this.rank_width_level;
	}
	public int getRankWidthRank() {
		return this.rank_width_rank;
	}
	public int getRankWidthProfile() {
		return this.rank_width_profile;
	}
	public int getRankHeightLevel() {
		return this.rank_height_level;
	}
	public int getRankHeightRank() {
		return this.rank_height_rank;
	}
	public int getRankHeightProfile() {
		return this.rank_height_profile;
	}
	public int getLevelXLevel() {
		return this.levelx_level;
	}
	public int getLevelXProfile() {
		return this.levelx_profile;
	}
	public int getLevelYLevel() {
		return this.levely_level;
	}
	public int getLevelYProfile() {
		return this.levely_profile;
	}
	public int getNameXLevel() {
		return this.namex_level;
	}
	public int getNameXRank() {
		return this.namex_rank;
	}
	public int getNameXProfile() {
		return this.namex_profile;
	}
	public int getNameYLevel() {
		return this.namey_level;
	}
	public int getNameYRank() {
		return this.namey_rank;
	}
	public int getNameYProfile() {
		return this.namey_profile;
	}
	public int getAvatarXRank() {
		return this.avatarx_rank;
	}
	public int getAvatarXProfile() {
		return this.avatarx_profile;
	}
	public int getAvatarYRank() {
		return this.avatary_rank;
	}
	public int getAvatarYProfile() {
		return this.avatary_profile;
	}
	public int getAvatarWidthRank() {
		return this.avatar_width_rank;
	}
	public int getAvatarWidthProfile() {
		return this.avatar_width_profile;
	}
	public int getAvatarHeightRank() {
		return this.avatar_height_rank;
	}
	public int getAvatarHeightProfile() {
		return this.avatar_height_profile;
	}
	public int getPlacementXRank() {
		return this.placementx_rank;
	}
	public int getPlacementXProfile() {
		return this.placementx_profile;
	}
	public int getPlacementYRank() {
		return this.placementy_rank;
	}
	public int getPlacementYProfile() {
		return this.placementy_profile;
	}
	public int getExperienceXProfile() {
		return this.experiencex_profile;
	}
	public int getExperienceYProfile() {
		return this.experiencey_profile;
	}
	public int getCurrencyXProfile() {
		return this.currencyx_profile;
	}
	public int getCurrencyYProfile() {
		return this.currencyy_profile;
	}
	public int getExpReachXProfile() {
		return this.exp_reachx_profile;
	}
	public int getExpReachYProfile() {
		return this.exp_reachy_profile;
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
}
