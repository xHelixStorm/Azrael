package de.azrael.enums;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * This enum shows all available commands and sub commands.
 * cm stands for command and signals to retrieve the status and level
 * column of the command from the database. While scm stands for sub 
 * command and will retrieve only the level column of the sub command
 * from the database.
 */

public enum Command {
	DISPLAY							("display", "cm"),
	DISPLAY_ROLES					("display_roles", "scm"),
	DISPLAY_REGISTERED_ROLES 		("display_registered_roles", "scm"),
	DISPLAY_RANKING_ROLES			("display_ranking_roles", "scm"),
	DISPLAY_TEXT_CHANNELS			("display_text_channels", "scm"),
	DISPLAY_VOICE_CHANNELS			("display_voice_channels", "scm"),
	DISPLAY_REGISTERED_CHANNELS		("display_registered_channels", "scm"),
	DISPLAY_DAILIES					("display_dailies", "scm"),
	DISPLAY_WATCHED_USERS			("display_watched_users", "scm"),
	DISPLAY_CATEGORIES				("display_categories", "scm"),
	DISPLAY_REGISTERED_CATEGORIES	("display_registered_categories", "scm"),
	HELP							("help", "cm"),
	REGISTER						("register", "cm"),
	REGISTER_ROLE					("register_role", "scm"),
	REGISTER_TEXT_CHANNEL			("register_text_channel", "scm"),
	REGISTER_TEXT_CHANNEL_URL		("register_text_channel_url", "scm"),
	REGISTER_TEXT_CHANNEL_TXT		("register_text_channel_txt", "scm"),
	REGISTER_RANKING_ROLE			("register_ranking_role", "scm"),
	REGISTER_TEXT_CHANNELS			("register_text_channels", "scm"),
	REGISTER_USERS					("register_users", "scm"),
	REGISTER_CATEGORY				("register_category", "scm"),
	SET								("set", "cm"),
	SET_PERMISSIONS					("set_permissions", "scm"),
	SET_CHANNEL_CENSOR				("set_channel_censor", "scm"),
	SET_WARNINGS					("set_warnings", "scm"),
	SET_RANKING						("set_ranking", "scm"),
	SET_MAX_EXPERIENCE				("set_max_experience", "scm"),
	SET_DEFAULT_LEVEL_SKIN			("set_default_level_skin", "scm"),
	SET_DEFAULT_RANK_SKIN			("set_default_rank_skin", "scm"),
	SET_DEFAULT_PROFILE_SKIN		("set_default_profile_skin", "scm"),
	SET_DEFAULT_ICON_SKIN			("set_default_icon_skin", "scm"),
	SET_DAILY_ITEM					("set_daily_item", "scm"),
	SET_GIVEAWAY_ITEMS				("set_giveaway_items", "scm"),
	SET_COMP_SERVER					("set_comp_server", "scm"),
	SET_MAX_CLAN_MEMBERS			("set_max_clan_members", "scm"),
	SET_MAP							("set_map", "scm"),
	SET_ROOM_LIMIT					("set_room_limit", "scm"),
	SET_LANGUAGE					("set_language", "scm"),
	USER							("user", "cm"),
	USER_INFORMATION				("user_information", "scm"),
	USER_DELETE_MESSAGES			("user_delete_messages", "scm"),
	USER_WARNING					("user_warning", "scm"),
	USER_WARNING_FORCE				("user_warning_force", "scm", false, true),
	USER_MUTE						("user_mute", "scm"),
	USER_UNMUTE						("user_unmute", "scm"),
	USER_BAN						("user_ban", "scm"),
	USER_UNBAN						("user_unban", "scm"),
	USER_KICK						("user_kick", "scm"),
	USER_ASSIGN_ROLE				("user_assign_role", "scm"),
	USER_REMOVE_ROLE				("user_remove_role", "scm"),
	USER_HISTORY					("user_history", "scm"),
	USER_WATCH						("user_watch", "scm"),
	USER_UNWATCH					("user_unwatch", "scm"),
	USER_USE_WATCH_CHANNEL			("user_use_watch_channel", "scm", false, true),
	USER_GIFT_EXPERIENCE			("user_gift_experience", "scm"),
	USER_SET_EXPERIENCE				("user_set_experience", "scm"),
	USER_SET_LEVEL					("user_set_level", "scm"),
	USER_GIFT_CURRENCY				("user_gift_currency", "scm"),
	USER_SET_CURRENCY				("user_set_currency", "scm"),
	FILTER							("filter", "cm"),
	FILTER_WORD_FILTER				("filter_word_filter", "scm"),
	FILTER_NAME_FILTER				("filter_name_filter", "scm"),
	FILTER_NAME_KICK				("filter_name_kick", "scm"),
	FILTER_FUNNY_NAMES				("filter_funny_names", "scm"),
	FILTER_STAFF_NAMES				("filter_staff_names", "scm"),
	FILTER_PROHIBITED_URLS			("filter_prohibited_urls", "scm"),
	FILTER_ALLOWED_URLS				("filter_allowed_urls", "scm"),
	FILTER_PROHIBITED_SUBS			("filter_prohibited_subs", "scm"),
	ROLE_REACTION					("role_reaction", "cm"),
	REMOVE							("remove", "cm"),
	HEAVY_CENSORING					("heavy_censoring", "cm"),
	MUTE							("mute", "cm"),
	GOOGLE							("google", "cm"),
	SUBSCRIBE						("subscribe", "cm"),
	SUBSCRIBE_RSS					("subscribe_rss", "scm"),
	SUBSCRIBE_TWITTER				("subscribe_twitter", "scm"),
	SUBSCRIBE_REDDIT				("subscribe_reddit", "scm"),
	SUBSCRIBE_YOUTUBE				("subscribe_youtube", "scm"),
	SUBSCRIBE_TWITCH				("subscribe_twitch", "scm"),
	WRITE							("write", "cm"),
	EDIT							("edit", "cm"),
	ACCEPT							("accept", "cm"),
	DENY							("deny", "cm"),
	LANGUAGE						("language", "cm"),
	SCHEDULE						("schedule", "cm"),
	PRUNE							("prune", "cm"),
	WARN							("warn", "cm"),
	INVITES							("invites", "cm"),
	ABOUT							("about", "cm"),
	DAILY							("daily", "cm"),
	INVENTORY						("inventory", "cm"),
	MEOW							("meow", "cm"),
	PUG								("pug", "cm"),
	PROFILE							("profile", "cm"),
	RANK							("rank", "cm"),
	SHOP							("shop", "cm"),
	TOP								("top", "cm"),
	USE								("use", "cm"),
	QUIZ							("quiz", "cm"),
	RANDOMSHOP						("randomshop", "cm"),
	PATCHNOTES						("patchnotes", "cm"),
	DOUBLE_EXPERIENCE				("double_experience", "cm"),
	EQUIP							("equip", "cm"),
	MATCHMAKING						("matchmaking", "cm"),
	CLAN							("clan", "cm"),
	CW								("cw", "cm"),
	ROOM							("room", "cm"),
	ROOM_CLOSE						("room_close", "scm"),
	ROOM_WINNER						("room_winner", "scm"),
	ROOM_REOPEN						("room_reopen", "scm"),
	STATS							("stats", "cm"),
	LEADERBOARD						("leaderboard", "cm"),
	REBOOT							("reboot", "cm", true),
	SHUTDOWN						("shutdown", "cm", true),
	JOIN							("join", "cm", false, false),
	LEAVE							("leave", "cm", false, false),
	QUEUE							("queue", "cm", false, false, true),
	CHANGEMAP						("changemap", "scm", false, false, true),
	MASTER							("master", "scm", false, false, true),
	PICK							("pick", "scm", false, false, true),
	RESTRICT						("restrict", "scm", false, false, true),
	START							("start", "scm", false, false, true),
	WEB								("web", "cm", false, false, true);
	
	private String column;
	private String type;
	private boolean hidden = false;
	private boolean levelOnly = false;
	private boolean stateOnly = false;
	
	private Command(String _column, String _type) {
		this.column = _column;
		this.type = _type;
	}
	
	private Command(String _column, String _type, boolean _hidden) {
		this.column = _column;
		this.type = _type;
		this.hidden = _hidden;
	}
	
	private Command(String _column, String _type, boolean _hidden, boolean _levelOnly) {
		this.column = _column;
		this.type = _type;
		this.hidden = _hidden;
		this.levelOnly = _levelOnly;
	}
	
	private Command(String _column, String _type, boolean _hidden, boolean _levelOnly, boolean _stateOnly) {
		this.column = _column;
		this.type = _type;
		this.hidden = _hidden;
		this.levelOnly = _levelOnly;
	}
	
	public String getColumn() {
		return this.column;
	}
	
	public String getType() {
		return this.type;
	}
	
	public boolean isHidden() {
		return this.hidden;
	}
	
	public boolean isLevelOnly() {
		return this.levelOnly;
	}
	
	public boolean isStateOnly() {
		return this.stateOnly;
	}
	
	public ArrayList<Object> resolveResultSet(ResultSet rs) throws SQLException {
		ArrayList<Object> objects = new ArrayList<Object>();
		if(this.type.equals("cm")) {
			objects.add(rs.getBoolean(this.column+"_cm"));
		}
		else if(this.type.equals("scm")) {
			objects.add(rs.getBoolean(this.column+"_scm"));
		}
		objects.add((this.stateOnly ? 0 : rs.getInt(this.column+"_lv")));
		objects.add(this.column+":"+this.type);
		return objects;
	}
	
	public int resolveResultSetLv(ResultSet rs) throws SQLException {
		return rs.getInt(this.column+"_lv");
	}
	
	public boolean resolveResultSetCm(ResultSet rs) throws SQLException {
		return rs.getBoolean(this.column+"_cm");
	}
	
	public boolean resolveResultSetScm(ResultSet rs) throws SQLException {
		return rs.getBoolean(this.column+"_scm");
	}
	
	public boolean resolveResultSetType(ResultSet rs) throws SQLException {
		if(this.type.equals("cm"))
			return rs.getBoolean(this.column+"_cm");
		else
			return rs.getBoolean(this.column+"_scm");
	}
}
