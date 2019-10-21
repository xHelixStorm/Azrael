package constructors;

public class Guilds {
	private String name;
	private int max_level;
	private int level_id;
	private String level_description;
	private String file_type_level;
	private int rank_id;
	private String rank_description;
	private String file_type_rank;
	private int profile_id;
	private String profile_description;
	private String file_type_profile;
	private int icon_id;
	private String icon_description;
	private String file_type_icon;
	private boolean ranking_state;
	private long max_experience;
	private boolean max_exp_enabled;
	private int theme_id;
	private String currency;
	private long randomshop_price;
	
	private int inventory_startx;
	private int inventory_starty;
	private int inventory_tabx;
	private int inventory_taby;
	private int inventory_page_font_size;
	private int inventory_pagex;
	private int inventory_pagey;
	private int inventory_text_font_size;
	private int inventory_box_sizex;
	private int inventory_box_sizey;
	private int inventory_descriptiony;
	private int inventory_item_sizex;
	private int inventory_item_sizey;
	private int inventory_next_boxx;
	private int inventory_next_boxy;
	private int inventory_expiration_positiony;
	private int inventory_row_limit;
	private int inventory_max_items;
	
	private int randomshop_startx;
	private int randomshop_starty;
	private int randomshop_pagex;
	private int randomshop_pagey;
	private int randomshop_text_font_size;
	private int randomshop_box_sizex;
	private int randomshop_box_sizey;
	private int randomshop_item_sizex;
	private int randomshop_item_sizey;
	private int randomshop_next_boxx;
	private int randomshop_next_boxy;
	private int randomshop_row_limit;
	private int randomshop_max_items;
	private int randomshop_reward_item_sizex;
	private int randomshop_reward_item_sizey;
	
	private int daily_rewardx;
	private int daily_rewardy;
	private int daily_text_font_size;
	private int daily_description_mode;
	private int daily_descriptionx;
	private int daily_descriptiony;
	private int daily_description_startx;
	private int daily_field_sizex;
	
	private long messageTimeout;
	
	public void setName(String _name) {
		this.name = _name;
	}
	public void setMaxLevel(int _max_level) {
		this.max_level = _max_level;
	}
	public void setLevelID(int _level_id) {
		this.level_id = _level_id;
	}
	public void setLevelDescription(String _level_description) {
		this.level_description = _level_description;
	}
	public void setFileTypeLevel(String _file_type_level) {
		this.file_type_level = _file_type_level;
	}
	public void setRankID(int _rank_id) {
		this.rank_id = _rank_id;
	}
	public void setRankDescription(String _rank_description) {
		this.rank_description = _rank_description;
	}
	public void setFileTypeRank(String _file_type_rank) {
		this.file_type_rank = _file_type_rank;
	}
	public void setProfileID(int _profile_id) {
		this.profile_id = _profile_id;
	}
	public void setProfileDescription(String _profile_description) {
		this.profile_description = _profile_description;
	}
	public void setFileTypeProfile(String _file_type_profile) {
		this.file_type_profile = _file_type_profile;
	}
	public void setIconID(int _icon_id) {
		this.icon_id = _icon_id;
	}
	public void setIconDescription(String _icon_description) {
		this.icon_description = _icon_description;
	}
	public void setFileTypeIcon(String _file_type_icon) {
		this.file_type_icon = _file_type_icon;
	}
	public void setRankingState(boolean _ranking_state) {
		this.ranking_state = _ranking_state;
	}
	public void setMaxExperience(long _max_experience) {
		this.max_experience = _max_experience;
	}
	public void setMaxExpEnabled(boolean _max_exp_enabled) {
		this.max_exp_enabled = _max_exp_enabled;
	}
	public void setThemeID(int _theme_id) {
		this.theme_id = _theme_id;
	}
	public void setCurrency(String _currency) {
		this.currency = _currency;
	}
	public void setRandomshopPrice(long _randomshop_price) {
		this.randomshop_price = _randomshop_price;
	}
	public void setMessageTimeout(long _messageTimeout) {
		this.messageTimeout = _messageTimeout;
	}
	
	public String getName() {
		return this.name;
	}
	public int getMaxLevel() {
		return this.max_level;
	}
	public int getLevelID() {
		return this.level_id;
	}
	public String getLevelDescription() {
		return this.level_description;
	}
	public String getFileTypeLevel() {
		return this.file_type_level;
	}
	public int getRankID() {
		return this.rank_id;
	}
	public String getRankDescription() {
		return this.rank_description;
	}
	public String getFileTypeRank() {
		return this.file_type_rank;
	}
	public int getProfileID() {
		return this.profile_id;
	}
	public String getProfileDescription() {
		return this.profile_description;
	}
	public String getFileTypeProfile() {
		return this.file_type_profile;
	}
	public int getIconID() {
		return this.icon_id;
	}
	public String getIconDescription() {
		return this.icon_description;
	}
	public String getFileTypeIcon() {
		return this.file_type_icon;
	}
	public boolean getRankingState() {
		return this.ranking_state;
	}
	public long getMaxExperience() {
		return this.max_experience;
	}
	public boolean getMaxExpEnabled() {
		return this.max_exp_enabled;
	}
	public int getThemeID() {
		return this.theme_id;
	}
	public String getCurrency() {
		return this.currency;
	}
	public long getRandomshopPrice() {
		return this.randomshop_price;
	}
	public long getMessageTimeout() {
		return this.messageTimeout;
	}
}
