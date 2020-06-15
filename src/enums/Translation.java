package enums;

public enum Translation {
	PARAM_ROLES					("param_roles"),
	PARAM_REGISTERED_ROLES		("param_registered_roles"),
	PARAM_RANKING_ROLES			("param_ranking_roles"),
	PARAM_TEXT_CHANNELS			("param_text_channels"),
	PARAM_VOICE_CHANNELS		("param_voice_channels"),
	PARAM_REGISTERED_CHANNELS	("param_registered_channels"),
	PARAM_DAILIES				("param_dailies"),
	PARAM_WATCHED_USERS			("param_watched_users"),
	PARAM_PERMISSIONS			("param_permissions"),
	PARAM_STATE					("param_state"),
	PARAM_ON					("param_on"),
	PARAM_OFF					("param_off"),
	PARAM_AUTO					("param_auto"),
	PARAM_ADD_REACTION			("param_add_reaction"),
	PARAM_CLEAR_REACTIONS		("param_clear_reactions"),
	PARAM_DOCS					("param_docs"),
	PARAM_SPREADSHEETS			("param_spreadsheets"),
	PARAM_DRIVE					("param_drive"),
	PARAM_ENABLE				("param_enable"),
	PARAM_DISABLE				("param_disable"),
	PARAM_RESET					("param_reset"),
	PARAM_ITEMS					("param_items"),
	PARAM_WEAPONS				("param_weapons"),
	PARAM_SKINS					("param_skins"),
	PARAM_PRIVATE				("param_private"),
	PARAM_PUBLIC				("param_public"),
	PARAM_GAME					("param_game"),
	PARAM_BOT					("param_bot"),
	PARAM_REGISTER_REWARDS		("param_register_rewards"),
	PARAM_REGISTER_QUESTIONS	("param_register_questions"),
	PARAM_CLEAR					("param_clear"),
	PARAM_RUN					("param_run"),
	PARAM_PLAY					("param_play"),
	PARAM_REPLAY				("param_replay"),
	PARAM_ROLE					("param_role"),
	PARAM_TEXT_CHANNEL			("param_text_channel"),
	PARAM_TEXT_CHANNEL_URL		("param_text_channel_url"),
	PARAM_TEXT_CHANNEL_TXT		("param_text_channel_txt"),
	PARAM_RANKING_ROLE			("param_ranking_role"),
	PARAM_USERS					("param_users"),
	PARAM_ALL					("param_all"),
	PARAM_CHANNEL_CENSOR		("param_channel_censor"),
	PARAM_PERMISSION_LEVEL		("param_permission_level"),
	PARAM_WARNINGS				("param_warnings"),
	PARAM_RANKING				("param_ranking"),
	PARAM_MAX_EXPERIENCE		("param_max_experience"),
	PARAM_DEFAULT_LEVEL_SKIN	("param_default_level_skin"),
	PARAM_DEFAULT_RANK_SKIN		("param_default_rank_skin"),
	PARAM_DEFAULT_PROFILE_SKIN	("param_default_profile_skin"),
	PARAM_DEFAULT_ICON_SKIN		("param_default_icon_skin"),
	PARAM_DAILY_ITEM			("param_daily_item"),
	PARAM_GIVEAWAY_ITEMS		("param_giveaway_items"),
	PARAM_REGISTER				("param_register"),
	PARAM_REMOVE				("param_remove"),
	PARAM_FORMAT				("param_format"),
	PARAM_OPTIONS				("param_options"),
	PARAM_TEST					("param_test"),
	PARAM_DISPLAY				("param_display"),
	PARAM_DEFAULT_LEVEL			("param_default_level"),
	PARAM_DEFAULT_RANK			("param_default_rank"),
	PARAM_DEFAULT_PROFILE		("param_default_profile"),
	PARAM_DEFAULT_ICONS			("param_default_icons"),
	PARAM_WORD_FILTER			("param_word_filter"),
	PARAM_NAME_FILTER			("param_name_filter"),
	PARAM_NAME_KICK				("param_name_kick"),
	PARAM_FUNNY_NAMES			("param_funny_names"),
	PARAM_STAFF_NAMES			("param_staff_names"),
	PARAM_PROHIBITED_URLS		("param_prohibited_urls"),
	PARAM_ALLOWED_URLS			("param_allowed_urls"),
	PARAM_PROHIBITED_TWEETS		("param_prohibited_tweets"),
	PARAM_INSERT				("param_insert"),
	PARAM_ADD_PASTEBIN			("param_add_pastebin"),
	PARAM_LOAD_PASTEBIN			("param_load_pastebin"),
	PARAM_ADD					("param_add"),
	PARAM_ENABLE_PICTURES		("param_enable_pictures"),
	PARAM_ENABLE_VIDEOS			("param_enable_videos"),
	PARAM_ENABLE_TEXT			("param_enable_text"),
	PARAM_DISABLE_PICTURES		("param_disable_pictures"),
	PARAM_DISABLE_VIDEOS		("param_disable_videos"),
	PARAM_DISABLE_TEXT			("param_disable_text"),
	PARAM_ADD_CHILD				("param_add_child"),
	PARAM_REMOVE_CHILD			("param_remove_child"),
	PARAM_INFORMATION			("param_information"),
	PARAM_DELETE_MESSAGES		("param_delete_messages"),
	PARAM_WARNING				("param_warning"),
	PARAM_MUTE					("param_mute"),
	PARAM_UNMUTE				("param_unmute"),
	PARAM_BAN					("param_ban"),
	PARAM_UNBAN					("param_unban"),
	PARAM_KICK					("param_kick"),
	PARAM_ASSIGN_ROLE			("param_assign_role"),
	PARAM_REMOVE_ROLE			("param_remove_role"),
	PARAM_HISTORY				("param_history"),
	PARAM_WATCH					("param_watch"),
	PARAM_UNWATCH				("param_unwatch"),
	PARAM_GIFT_EXPERIENCE		("param_gift_experience"),
	PARAM_SET_EXPERIENCE		("param_set_experience"),
	PARAM_SET_LEVEL				("param_set_level"),
	PARAM_GIFT_CURRENCY			("param_gift_currency"),
	PARAM_SET_CURRENCY			("param_set_currency"),
	PARAM_YES					("param_yes"),
	PARAM_NO					("param_no"),
	PARAM_PERM					("param_perm"),
	PARAM_EXIT					("param_exit"),
	PARAM_LEVEL_UPS				("param_level_ups"),
	PARAM_RANKS					("param_ranks"),
	PARAM_PROFILES				("param_profiles"),
	PARAM_ICONS					("param_icons"),
	PARAM_SKILLS				("param_skills"),
	PARAM_PURCHASE				("param_purchase"),
	PARAM_SELL					("param_sell"),
	PARAM_RETURN				("param_return"),
	PARAM_SKIP_QUESTION			("param_skip_question"),
	PARAM_INTERRUPT_QUESTIONS	("param_interrupt_questions"),
	PARAM_CREATE				("param_create"),
	PARAM_EVENTS				("param_events"),
	PARAM_SHEET					("param_sheet"),
	PARAM_MAP					("param_map"),
	PARAM_SHOW					("param_show"),
	PARAM_SET					("param_set"),
	PARAM_REMOVE_ALL			("param_remove_all"),
	
	
	LANG_ENG					("lang_eng"),
	LANG_GER					("lang_ger"),
	LANG_FRE					("lang_fre"),
	LANG_TUR					("lang_tur"),
	LANG_RUS					("lang_rus"),
	LANG_SPA					("lang_spa"),
	LANG_POR					("lang_por"),
	LANG_ITA					("lang_ita"),
	
	NOT_BOT_CHANNEL				("not_bot_channel"),
	NOT_QUIZ_CHANNEL			("not_quiz_channel"),
	NO_TRA_CHANNEL				("no_tra_channel"),
	NO_ROLES					("no_roles"),
	LEVEL_SYSTEM_NOT_ENABLED	("level_system_not_enabled"),
	NOT_AVAILABLE				("not_available"),
	PARAM_NOT_FOUND				("param_not_found"),
	DEFAULT_REASON				("default_reason"),
	ALREADY_MUTED				("already_muted"),
	LOW_PRIVILEGES				("low_privileges"),
	NO_MUTE_ROLE				("no_mute_role"),
	MISSING_PERMISSION			("missing_permission"),
	MISSING_PERMISSION_IN		("missing_permission_in"),
	COOLDOWN					("cooldown"),
	GENERAL_ERROR				("general_error"),
	GIF_COMPRESSION				("gif_compression"),
	GIF_COMPRESS_ERR			("gif_compress_err"),
	GIF_SEND_ERR				("gif_send_err"),
	HIGHER_PRIVILEGES_REQUIRED	("higher_privileges_required"),
	HIGHER_PRIVILEGES_ROLE		("higher_privileges_role"),
	HIGHER_PRIVILEGES_RANKING	("higher_privileges_ranking"),
	ROLE_NOT_EXISTS				("role_not_exists"),
	TEXT_CHANNEL_NOT_EXISTS		("text_channel_not_exists"),
	NO_ROLE_ID					("no_role_id"),
	NO_TEXT_CHANNEL				("no_text_channel"),
	PASTEBIN_PASTE_ERR			("pastebin_paste_err"),
	PASTEBIN_PASTE_ERR_2		("pastebin_paste_err_2"),
	PASTEBIN_READ_ERR			("pastebin_read_err"),
	PASTEBIN_READ_ERR_2			("pastebin_read_err_2"),
	URL_INVALID					("url_invalid"),
	USER_HIGHER_PERMISSION		("user_higher_permission"),
	IMAGE_ERR					("image_err"),
	MESSAGE_WATCH_ERR			("message_watch_err"),
	EXP_LIMIT					("exp_limit"),
	LEVEL_PROMOTION_FAILED		("level_promotion_failed"),
	DEFAULT_SKINS_ERR			("default_skins_err"),
	LEVEL_UP_ROLE_ERR			("level_up_role_err"),
	RSS_NO_FEED					("rss_no_feed"),
	TWITTER_NO_TWEET			("twitter_no_tweet"),
	WATCHING_LIFTED				("watching_lifted"),
	WATCHING_LIFTED_ERR			("watching_lifted_err"),
	
	EMBED_TITLE_ERROR			("embed_title_error"),
	EMBED_TITLE_ABOUT			("embed_title_about"),
	EMBED_TITLE_DAILY			("embed_title_daily"),
	EMBED_TITLE_DETAILS			("embed_title_details"),
	EMBED_TITLE_PERMISSIONS		("embed_title_permissions"),
	EMBED_TITLE_DENIED			("embed_title_denied"),
	EMBED_TITLE_PASTE			("embed_title_paste"),
	EMBED_TITLE_NOT_PASTE		("embed_title_not_paste"),
	EMBED_TITLE_PASTE_READ_ERR	("embed_title_paste_read_err"),
	EMBED_TITLE_URL				("embed_title_url"),
	EMBED_TITLE_WARNING			("embed_title_warning"),
	EMBED_TITLE_UNMUTED			("embed_title_unmuted"),
	EMBED_TITLE_WATCH			("embed_title_watch"),
	EMBED_TITLE_LEVEL_UP		("embed_title_level_up"),
	EMBED_TITLE_WATCH_LIFTED	("embed_title_watch_lifted"),
	
	ABOUT_DESCRIPTION			("about_description"),
	ABOUT_FIELD_1				("about_field_1"),
	ABOUT_FIELD_2				("about_field_2"),
	ABOUT_FIELD_3				("about_field_3"),
	ABOUT_FIELD_3_DESC			("about_field_3_desc"),
	ABOUT_FIELD_4				("about_field_4"),
	ABOUT_FIELD_4_DESC			("about_field_4_desc"),
	
	COMMAND_HEADER_1			("command_header_1"),
	COMMAND_HEADER_2			("command_header_2"),
	COMMAND_HEADER_3			("command_header_3"),
	COMMAND_REGISTER			("command_register"),
	COMMAND_SET					("command_set"),
	COMMAND_REMOVE				("command_remove"),
	COMMAND_USER				("command_user"),
	COMMAND_FILTER				("command_filter"),
	COMMAND_ROLE_REACTION		("command_role_reaction"),
	COMMAND_SUBSCRIBE			("command_subscribe"),
	COMMAND_DOUBLE_EXPERIENCE	("command_double_experience"),
	COMMAND_HEAVY_CENSORING		("command_heavy_censoring"),
	COMMAND_MUTE				("command_mute"),
	COMMAND_GOOGLE				("command_google"),
	COMMAND_WRITE				("command_write"),
	COMMAND_EDIT				("command_edit"),
	COMMAND_PUG					("command_pug"),
	COMMAND_MEOW				("command_meow"),
	COMMAND_RANK				("command_rank"),
	COMMAND_PROFILE				("command_profile"),
	COMMAND_TOP					("command_top"),
	COMMAND_USE					("command_use"),
	COMMAND_SHOP				("command_shop"),
	COMMAND_INVENTORY			("command_inventory"),
	COMMAND_DAILY				("command_daily"),
	COMMAND_QUIZ				("command_quiz"),
	COMMAND_RANDOMSHOP			("command_randomshop"),
	COMMAND_EQUIP				("command_equip"),
	COMMAND_ABOUT				("command_about"),
	COMMAND_DISPLAY				("command_display"),
	COMMAND_PATCHNOTES			("command_patchnotes"),
	COMMANDS_DISABLED			("commands_disabled"),
	
	DAILY_REWARD				("daily_reward"),
	DAILY_REWARD_SENT			("daily_reward_sent"),
	DAILY_REWARD_NOT_SENT		("daily_reward_not_sent"),
	DAILY_ERROR_1				("daily_error_1"),
	DAILY_ERROR_2				("daily_error_2"),
	DAILY_ERROR_3				("daily_error_3"),
	DAILY_EMPTY					("daily_empty"),
	DAILY_COOLDOWN				("daily_cooldown"),
	
	DISPLAY_INPUT_NOT_FOUND		("display_input_not_found"),
	DISPLAY_HELP				("display_help"),
	DISPLAY_HELP_1				("display_help_1"),
	DISPLAY_HELP_2				("display_help_2"),
	DISPLAY_HELP_3				("display_help_3"),
	DISPLAY_HELP_4				("display_help_4"),
	DISPLAY_HELP_5				("display_help_5"),
	DISPLAY_HELP_6				("display_help_6"),
	DISPLAY_HELP_7				("display_help_7"),
	DISPLAY_HELP_8				("display_help_8"),
	DISPLAY_HELP_9				("display_help_9"),
	DISPLAY_ROLE_TYPE			("display_role_type"),
	DISPLAY_CHANNEL_TYPE		("display_channel_type"),
	DISPLAY_PERMISSION_LEVEL	("display_permission_level"),
	DISPLAY_PERSISTANT			("display_persistant"),
	DISPLAY_URL_CENSORING		("display_url_censoring"),
	DISPLAY_TEXT_CENSORING		("display_text_censoring"),
	DISPLAY_LANG_CENSORING		("display_lang_censoring"),
	DISPLAY_IS_PERSISTANT		("display_is_persistant"),
	DISPLAY_IS_NOT_PERSISTANT	("display_is_not_persistant"),
	DISPLAY_IS_ENABLED			("display_is_enabled"),
	DISPLAY_IS_NOT_ENABLED		("display_is_not_enabled"),
	DISPLAY_UNLOCK_LEVEL		("display_unlock_level"),
	DISPLAY_PROBABILITY			("display_probability"),
	
	DOUBLE_EXPERIENCE_HELP		("double_experience_help"),
	DOUBLE_EXPERIENCE_HELP_1	("double_experience_help_1"),
	DOUBLE_EXPERIENCE_HELP_2	("double_experience_help_2"),
	DOUBLE_EXPERIENCE_HELP_3	("double_experience_help_3"),
	DOUBLE_EXPERIENCE_STATE		("double_experience_state"),
	DOUBLE_EXPERIENCE_UPDATE	("double_experience_update"),
	DOUBLE_EXPERIENCE_MESSAGE	("double_experience_message"),
	DOUBLE_EXPERIENCE_AUTO		("double_experience_auto"),
	DOUBLE_EXPERIENCE_ERROR		("double_experience_error"),
	
	EDIT_HELP					("edit_help"),
	EDIT_NOT_BOT				("edit_not_bot"),
	EDIT_NO_TEXT_CHANNEL		("edit_no_text_channel"),
	EDIT_NOT_ENOUGH_PARAMS		("edit_not_enough_params"),
	EDIT_UPDATE					("edit_update"),
	EDIT_UPDATED				("edit_updated"),
	EDIT_NOT_EXISTS				("edit_not_exists"),
	EDIT_REACTION_ADD_HELP		("edit_reaction_add_help"),
	EDIT_SELECT_ROLE			("edit_select_role"),
	EDIT_NO_ROLES				("edit_no_roles"),
	EDIT_COMPLETE				("edit_complete"),
	EDIT_NO_NUMBER				("edit_no_number"),
	EDIT_REACTIONS_CLEAR		("edit_reactions_clear"),
	
	EQUIP_WRONG_CHANNEL			("equip_wrong_channel"),
	EQUIP_HELP					("equip_help"),
	EQUIP_DENIED				("equip_denied"),
	EQUIP_SERVER_SELECT			("equip_server_select"),
	EQUIP_SERVER_SELECT_2		("equip_server_select_2"),
	EQUIP_NO_SERVER_FOUND		("equip_no_server_found"),
	EQUIP_SELECT_ERR			("equip_select_err"),
	EQUIP_EMPTY					("equip_empty"),
	EQUIP_EQUIPMENT				("equip_equipment"),
	EQUIP_SLOT_1				("equip_slot_1"),
	EQUIP_SLOT_2				("equip_slot_2"),
	EQUIP_SLOT_3				("equip_slot_3"),
	EQUIP_SLOT_4				("equip_slot_4"),
	EQUIP_UNEQUIP_ALL			("equip_unequip_all"),
	EQUIP_UNEQUIP_ALL_EMPTY		("equip_unequip_all_empty"),
	EQUIP_TITLE_SLOT			("equip_title_slot"),
	EQUIP_TITLE_SKILL			("equip_title_skill"),
	EQUIP_SELECT_WEAPON			("equip_select_weapon"),
	EQUIP_SELECT_WEAPON_2		("equip_select_weapon_2"),
	EQUIP_SELECT_WEP_NOT_FOUND	("equip_select_wep_not_found"),
	EQUIP_SELECT_SKILL			("equip_select_skill"),
	EQUIP_SELECT_SKILL_2		("equip_select_skill_2"),
	EQUIP_SELECT_SKILL_NOT_FOUND("equip_select_skill_not_found"),
	EQUIP_WEP_UNEQUIPPED		("equip_wep_unequipped"),
	EQUIP_SKILL_UNEQUIPPED		("equip_skill_unequipped"),
	EQUIP_SLOT_EMPTY			("equip_slot_empty"),
	EQUIP_SELECT_DIGIT			("equip_select_digit"),
	EQUIP_EQUIPPED				("equip_equipped"),
	EQUIP_ALREADY_EQUIPPED		("equip_already_equipped"),
	EQUIP_ALREADY_EQUIPPED_2	("equip_already_equipped"),
	EQUIP_SKILL_EQUIPPED		("equip_skill_equipped"),
	EQUIP_SKILL_ALREADY_EQUIPPED("equip_skill_already_equipped"),
	EQUIP_EXIT					("equip_exit"),
	EQUIP_SELECT_NUMBER			("equip_select_number"),
	
	FILTER_HELP					("filter_help"),
	FILTER_ACTIONS				("filter_actions"),
	FILTER_DISPLAY				("filter_display"),
	FILTER_INSERT				("filter_insert"),
	FILTER_REMOVE				("filter_remove"),
	FILTER_ADD_PASTEBIN			("filter_add_pastebin"),
	FILTER_LOAD_PASTEBIN		("filter_load_pastebin"),
	FILTER_LANG_SELECTION		("filter_lang_selection"),
	FILTER_NO_LANGS				("filter_no_langs"),
	FILTER_LIST					("filter_list"),
	FILTER_LIST_EMPTY			("filter_list_empty"),
	FILTER_WRITE_WORD			("filter_write_word"),
	FILTER_WRITE_PASTEBIN		("filter_write_pastebin"),
	FILTER_WRITE_FQDN			("filter_write_fqdn"),
	FILTER_WRITE_PASTE_FQDN		("filter_write_paste_fqdn"),
	FILTER_WRITE_USERNAME		("filter_write_username"),
	FILTER_WRITE_PASTE_USERNAME	("filter_write_paste_username"),
	FILTER_WRITE_INSERT			("filter_write_insert"),
	FILTER_WRITE_REMOVE			("filter_write_remove"),
	FILTER_WRITE_ADD_PASTEBIN	("filter_write_add_pastebin"),
	FILTER_WRITE_INSERT_URL		("filter_write_insert_url"),
	FILTER_WRITE_REMOVE_URL		("filter_write_remove_url"),
	FILTER_WRITE_PASTEBIN_URL	("filter_write_pastebin_url"),
	FILTER_WRITE_INSERT_NICK	("filter_write_insert_nick"),
	FILTER_WRITE_REMOVE_NICK	("filter_write_remove_nick"),
	FILTER_WRITE_PASTEBIN_NICK	("filter_write_pastebin_nick"),
	FILTER_DUPLICATES			("filter_duplicates"),
	FILTER_ROLLBACK_ERR			("filter_rollback_err"),
	FILTER_NO_URL				("filter_no_url"),
	FILTER_NO_NICK				("filter_no_nick"),
	FILTER_INVALID_CHAR			("filter_invalid_char"),
	
	GOOGlE_HELP					("google_help"),
	GOOGLE_API_NOT_AVAILABLE	("google_api_not_available"),
	GOOGLE_WEBSERVICE			("google_webservice"),
	GOOGLE_EVENTS				("google_events"),
	GOOGLE_NO_EVENTS			("google_no_events"),
	GOOGLE_EVENT_START			("google_event_start"),
	GOOGLE_NOT_LINKED_EVENT		("google_not_linked_event"),
	GOOGLE_INVALID_DDS			("google_invalid_dds"),
	GOOGLE_SHEET_HELP			("google_sheet_help"),
	GOOGLE_SHEET_CREATE			("google_sheet_create"),
	GOOGLE_SHEET_CREATED		("google_sheet_created"),
	GOOGLE_SHEET_CREATED_2		("google_sheet_created_2"),
	GOOGLE_SHEET_EMAIL			("google_sheet_email"),
	GOOGLE_SHEET_ADD_HELP		("google_sheet_add_help"),
	GOOGLE_SHEET_ADDED			("google_sheet_added"),
	GOOGLE_SHEET_URL			("google_sheet_url"),
	GOOGLE_SHEET_REMOVE_HELP	("google_sheet_remove_help"),
	GOOGLE_SHEET_REMOVED		("google_sheet_removed"),
	GOOGLE_SHEET_EVENT_HELP		("google_sheet_event_help"),
	GOOGLE_SHEET_NO_FILE		("google_sheet_no_file"),
	GOOGLE_SHEET_EVENTS_ADD		("google_sheet_events_add"),
	GOOGLE_SHEET_NOT_EVENT		("google_sheet_not_event"),
	GOOGLE_SHEET_EVENTS_ADDED	("google_sheet_events_added"),
	GOOGLE_SHEET_EVENTS_REMOVED	("google_sheet_events_removed"),
	GOOGLE_SHEET_START_HELP		("google_sheet_start_help"),
	GOOGLE_SHEET_EVENT_SELECT	("google_sheet_event_select"),
	GOOGLE_SHEET_START_SET		("google_sheet_start_set"),
	GOOGLE_SHEET_START_UPDATED	("google_sheet_start_updated"),
	GOOGLE_SHEET_MAP_HELP		("google_sheet_map_help"),
	GOOGLE_SHEET_MAP_ADD		("google_sheet_map_add"),
	GOOGLE_SHEET_NO_DD			("google_sheet_no_dd"),
	GOOGLE_SHEET_DD_ADDED		("google_sheet_dd_added"),
	GOOGLE_SHEET_NO_MAPPING		("google_sheet_no_mapping"),
	GOOGLE_SHEET_NO_START_POINT	("google_sheet_no_start_point"),
	GOOGLE_SHEET_NOT_FOUND		("google_sheet_not_found"),
	GOOGLE_SHEET_NOT_INSERTED	("google_row_not_inserted"),
	GOOGLE_SHEET_NO_SERVICE		("google_sheet_no_service"),
	GOOGLE_EXIT					("google_exit"),
	
	HEAVY_CENSORING_HELP		("heavy_censoring_help"),
	HEAVY_CENSORING_ENABLED		("heavy_censoring_enabled"),
	HEAVY_CENSORING_IS_ENABLED	("heavy_censoring_is_enabled"),
	HEAVY_CENSORING_DISABLED	("heavy_censoring_disabled"),
	HEAVY_CENSORING_IS_DISABLED	("heavy_censoring_is_disabled"),
	HEAVY_CENSORING_RESET		("heavy_censoring_reset"),
	HEAVY_CENSORING_RESET_ERR	("heavy_censoring_reset_err"),
	HEAVY_CENSORING_SOFT		("heavy_censoring_soft"),
	HEAVY_CENSORING_HARD		("heavy_censoring_hard"),
	HEAVY_CENSORING_REASON		("heavy_censoring_reason"),
	HEAVY_CENSORING_DELETED		("heavy_censoring_deleted"),
	
	MUTE_HELP					("mute_help"),
	MUTE_NAME_NOT_EXISTS		("mute_name_not_exists"),
	MUTE_ID_NOT_EXISTS			("mute_id_not_exists"),
	MUTE_ERR					("mute_err"),
	
	PATCHNOTES_HELP				("patchnotes_help"),
	PATCHNOTES_NOT_AVAILABLE	("patchnotes_not_available"),
	PATCHNOTES_NOT_FOUND		("patchnotes_not_found"),
	PATCHNOTES_CHOICE_1			("patchnotes_choice_1"),
	PATCHNOTES_CHOICE_2			("patchnotes_choice_2"),
	PATCHNOTES_CHOICE_3			("patchnotes_choice_3"),
	PATCHNOTES_LATEST_TITLE		("patchnotes_latest_title"),
	
	PROFILE_TITLE				("profile_title"),
	PROFILE_NO_ICONS			("profile_no_icons"),
	PROFILE_ERR					("profile_err"),
	
	RANK_TITLE					("rank_title"),
	RANK_RANK					("rank_rank"),
	RANK_NO_ICONS				("rank_no_icons"),
	RANK_ERR					("rank_err"),
	
	LEVEL_TITLE					("level_title"),
	LEVEL_MESSAGE				("level_message"),
	LEVEL_ERR					("level_err"),
	
	QUIZ_HELP					("quiz_help"),
	QUIZ_REWARDS_HELP			("quiz_rewards_help"),
	QUIZ_QUESTIONS_HELP			("quiz_questions_help"),
	QUIZ_CLEAR					("quiz_clear"),
	QUIZ_NO_Q_AND_A				("quiz_no_q_and_a"),
	QUIZ_NO_REWARDS				("quiz_no_rewards"),
	QUIZ_RUN_HELP				("quiz_run_help"),
	QUIZ_NO_QA_REWARDS			("quiz_no_qa_rewards"),
	QUIZ_REWARDS_REGISTERED		("quiz_rewards_registered"),
	QUIZ_ONLY_REWARDS			("quiz_only_rewards"),
	QUIZ_QUESTIONS_REGISTERED	("quiz_questions_registered"),
	QUIZ_NO_SETTINGS			("quiz_no_settings"),
	QUIZ_NO_QUESTIONS			("quiz_no_questions"),
	QUIZ_NO_REWARDS_FOUND		("quiz_no_rewards_found"),
	QUIZ_START_SHORTLY			("quiz_start_shortly"),
	QUIZ_ERR_1					("quiz_err_1"),
	QUIZ_ERR_2					("quiz_err_2"),
	QUIZ_ERR_3					("quiz_err_3"),
	QUIZ_ERR_4					("quiz_err_4"),
	QUIZ_ERR_5					("quiz_err_5"),
	QUIZ_REPLY_1				("quiz_reply_1"),
	QUIZ_REPLY_2				("quiz_reply_2"),
	QUIZ_REPLY_3				("quiz_reply_3"),
	QUIZ_REWARD_SENT_TITLE		("quiz_reward_sent_title"),
	QUIZ_STARTING				("quiz_starting"),
	QUIZ_FIRST_QUESTION			("quiz_first_question"),
	QUIZ_WINNER					("quiz_winner"),
	QUIZ_WINNER_DM				("quiz_winner_dm"),
	QUIZ_WINNER_NOTIFICATION	("quiz_winner_notification"),
	QUIZ_WINNER_NOTIFICATION_2	("quiz_winner_notification_2"),
	QUIZ_REWARD_SEND_ERR		("quiz_reward_send_err"),
	QUIZ_QUESTION_SKIP			("quiz_question_skip"),
	QUIZ_INTERRUPTED			("quiz_interrupted"),
	QUIZ_HINT_1					("quiz_hint_1"),
	QUIZ_HINT_2					("quiz_hint_2"),
	QUIZ_HINT_3					("quiz_hint_3"),
	QUIZ_QUESTION_REPEAT		("quiz_question_repeat"),
	QUIZ_END					("quiz_end"),
	QUIZ_ERR					("quiz_err"),
	
	RANDOMSHOP_REPLAY_ERR		("randomshop_replay_err"),
	
	REBOOT						("reboot"),
	
	REGISTER_HELP_1				("register_help_1"),
	REGISTER_HELP_2				("register_help_2"),
	REGISTER_CHANNEL_HELP		("register_channel_help"),
	REGISTER_CHANNEL_NO_TYPES	("register_channel_no_types"),
	REGISTER_CHANNEL_URL_HELP	("register_channel_url_help"),
	REGISTER_CHANNEL_TXT_HELP	("register_channel_txt_help"),
	REGISTER_CHANNEL_REGISTERED	("register_channel_registered"),
	REGISTER_CHANNEL_ERR		("register_channel_err"),
	REGISTER_ALL_CHANNELS		("register_all_channels"),
	REGISTER_RANK_ROLE_HELP		("register_rank_role_help"),
	REGISTER_RANK_ROLE_NO_LEVEL	("register_rank_role_no_level"),
	REGISTER_RANK_ROLE_ADDED	("register_rank_role_added"),
	REGISTER_ROLE_HELP			("register_role_help"),
	REGISTER_ROLE_NO_TYPES		("register_role_no_types"),
	REGISTER_ROLE_INVALID_PARAM	("register_role_invalid_param"),
	REGISTER_ROLE_ADM_ADDED		("register_role_adm_added"),
	REGISTER_ROLE_ADDED			("register_role_added"),
	
	REMOVE_HELP					("remove_help"),
	REMOVE_ROLE_HELP			("remove_role_help"),
	REMOVE_RANKING_HELP			("remove_ranking_help"),
	REMOVE_TXT_CHANNEL_HELP		("remove_txt_channel_help"),
	REMOVE_CENSOR_HELP			("remove_censor_help"),
	REMOVE_ROLES				("remove_roles"),
	REMOVE_ROLES_ERR			("remove_roles_err"),
	REMOVE_ROLE					("remove_role"),
	REMOVE_ROLE_ERR				("remove_role_err"),
	REMOVE_RANKING_ROLES		("remove_ranking_roles"),
	REMOVE_RANKING_ROLES_ERR	("remove_ranking_roles_err"),
	REMOVE_RANKING_ROLE			("remove_ranking_role"),
	REMOVE_RANKING_ROLE_ERR		("remove_ranking_role_err"),
	REMOVE_TXT_CHANNELS			("remove_txt_channels_err"),
	REMOVE_TXT_CHANNELS_ERR		("remove_txt_channels_err"),
	REMOVE_TXT_CHANNEL			("remove_txt_channel"),
	REMOVE_TXT_CHANNEL_ERR		("remove_txt_channel_err"),
	REMOVE_CENSORS				("remove_censors"),
	REMOVE_CENSORS_ERR			("remove_censors_err"),
	REMOVE_CENSOR				("remove_censor"),
	REMOVE_CENSOR_ERR			("remove_censor_err"),
	
	ROLE_REACTION_HELP			("role_reaction_help"),
	ROLE_REACTION_ENABLED		("role_reaction_enabled"),
	ROLE_REACTION_ENABLE		("role_reaction_enable"),
	ROLE_REACTION_DISABLED		("role_reaction_disabled"),
	ROLE_REACTION_DISABLE		("role_reaction_disable"),
	ROLE_REACTION_PRINT			("role_reaction_print"),
	
	SET_HELP					("set_help"),
	SET_PERMISSION				("set_permission"),
	SET_PERMISSION_ADDED		("set_permission_added"),
	SET_PERMISSION_NO_LEVEL		("set_permission_no_level"),
	SET_CENSOR					("set_censor"),
	SET_CENSOR_VALID_LANG		("set_censor_valid_lang"),
	SET_CENSOR_ERR				("set_censor_err"),
	SET_CENSOR_ADDED			("set_censor_added"),
	SET_RANKING					("set_ranking"),
	SET_MAX_EXPERIENCE			("set_max_experience"),
	SET_MAX_EXPERIENCE_ENABLED	("set_max_experience_enabled"),
	SET_MAX_EXPERIENCE_DISABLED	("set_max_experience_disabled"),
	SET_MAX_EXPERIENCE_ADDED	("set_max_experience_added"),
	SET_AVAILABLE_SKINS			("set_available_skins"),
	SET_LEVEL_HELP				("set_level_help"),
	SET_LEVEL_UPDATE			("set_level_update"),
	SET_RANK_HELP				("set_rank_help"),
	SET_RANK_UPDATE				("set_rank_update"),
	SET_PROFILE_HELP			("set_profile_help"),
	SET_PROFILE_UPDATE			("set_profile_update"),
	SET_ICON_HELP				("set_icon_help"),
	SET_ICON_UPDATE				("set_icon_update"),
	SET_INVALID_NUMBER			("set_invalid_number"),
	SET_LEVEL_SKIN_ERR			("set_level_skin_err"),
	SET_RANK_SKIN_ERR			("set_rank_skin_err"),
	SET_PROFILE_SKIN_ERR		("set_profile_skin_err"),
	SET_ICON_SKIN_ERR			("set_icon_skin_err"),
	SET_DAILY_ITEM_HELP			("set_daily_item_help"),
	SET_DAILY_INVALID_TYPE		("set_daily_invalid_type"),
	SET_DAILY_PROBABILITY_ERR	("set_daily_probability_err"),
	SET_DAILY_ALREADY_REGISTERED("set_daily_already_registered"),
	SET_DAILY_OVER_THE_LIMIT	("set_daily_over_the_limit"),
	SET_DAILY_ADDED				("set_daily_added"),
	SET_GIVEAWAY				("set_giveaway"),
	SET_GIVEAWAY_ADDED			("set_giveaway_added"),
	SET_WARNING_HELP			("set_warning_help"),
	SET_WARNING_NOT_VALID		("set_warning_not_valid"),
	SET_WARNING_1				("set_warning_1"),
	SET_WARNING_2				("set_warning_2"),
	SET_WARNINIG_ADDED			("set_warning_added"),
	SET_RANKING_ENABLE			("set_ranking_enable"),
	SET_RANKING_DISABLE			("set_ranking_disable"),
	
	SHOP_TITLE					("shop_title"),
	SHOP_HELP					("shop_help"),
	SHOP_PURCHASED				("shop_purchased"),
	SHOP_NOT_ENOUGH_CUR			("shop_not_enough_cur"),
	SHOP_SOLD					("shop_sold"),
	SHOP_DEFAULT				("shop_default"),
	SHOP_BOUGHT					("shop_bought"),
	SHOP_SHOW					("shop_show"),
	SHOP_DESCRIPTION			("shop_description"),
	SHOP_PRICE					("shop_price"),
	SHOP_RETURN					("shop_return"),
	SHOP_SELL					("shop_sell"),
	SHOP_PURCHASE				("shop_purchase"),
	SHOP_RETURN_MESSAGE			("shop_return_message"),
	SHOP_INSPECT_ERR			("shop_inspect_err"),
	SHOP_DISPLAY_WEP			("shop_display_wep"),
	SHOP_WEAPONS				("shop_weapons"),
	SHOP_SKILLS					("shop_skills"),
	SHOP_TITLE_EXIT				("shop_title_exit"),
	
	SHUTDOWN					("shutdown"),
	
	SUBSCRIBE_HELP				("subscribe_help"),
	SUBSCRIBE_REGISTER_HELP		("subscribe_register_help"),
	SUBSCRIBE_REGISTER_RSS		("subscribe_register_rss"),
	SUBSCRIBE_REGISTER_HASHTAG	("subscribe_register_hashtag"),
	SUBSCRIBE_LOGIN_TWITTER		("subscribe_login_twitter"),
	SUBSCRIBE_REGISTER_ERR		("subscribe_register_err"),
	SUBSCRIBE_NO_SUBSCRIPTIONS	("subscribe_no_subscriptions"),
	SUBSCRIBE_REMOVE_HELP		("subscribe_remove_help"),
	SUBSCRIBE_FORMAT_HELP		("subscribe_format_help"),
	SUBSCRIBE_OPTIONS_HELP		("subscribe_options_help"),
	SUBSCRIBE_OPTIONS_ERR		("subscribe_options_err"),
	SUBSCRIBE_TEST_HELP			("subscribe_test_help"),
	SUBSCRIBE_DISPLAY_HELP		("subscribe_display_help"),
	SUBSCRIBE_RSS_ADDED			("subscribe_rss_added"),
	SUBSCRIBE_HASH_ADDED		("subscribe_hash_added"),
	SUBSCRIBE_ALREADY_DONE		("subscribe_already_done"),
	SUBSCRIBE_REMOVED			("subscribe_removed"),
	SUBSCRIBE_DISPLAYED_NUM		("subscribe_displayed_num"),
	SUBSCRIBE_FORMAT_RSS		("subscribe_format_rss"),
	SUBSCRIBE_FORMAT_HASH		("subscribe_format_hash"),
	SUBSCRIBE_FORMAT_UPDATED	("subscribe_format_updated"),
	SUBSCRIBE_OPTIONS_1			("subscribe_options_1"),
	SUBSCRIBE_OPTIONS_2			("subscribe_options_2"),
	SUBSCRIBE_OPTIONS_3			("subscribe_options_3"),
	SUBSCRIBE_OPTIONS_4			("subscribe_options_4"),
	SUBSCRIBE_OPTIONS_5			("subscribe_options_5"),
	SUBSCRIBE_OPTIONS_6			("subscribe_options_6"),
	SUBSCRIBE_OPTION_BOUND		("subscribe_option_bound"),
	SUBSCRIBE_OPTION_NOT_BOUND	("subscribe_option_not_bound"),
	SUBSCRIBE_EXIT				("subscribe_exit"),
	
	TOP_USER_LEFT				("top_user_left"),
	TOP_TITLE					("top_title"),
	TOP_PERSONAL_INFO			("top_personal_info"),
	TOP_RANK					("top_rank"),
	TOP_LEVEL					("top_level"),
	TOP_EXPERIENCE				("top_experience"),
	TOP_PAGE					("top_page"),
	
	USE_HELP					("use_help"),
	USE_LEVEL_RESET				("use_level_reset"),
	USE_RANK_RESET				("use_rank_reset"),
	USE_PROFILE_RESET			("use_profile_reset"),
	USE_ICON_RESET				("use_icon_reset"),
	USE_SKIN					("use_skin"),
	USE_ITEM					("use_item"),
	USE_NOT_EXISTS				("use_not_exists"),
	
	USER_HELP					("user_help"),
	USER_FOUND_1				("user_found_1"),
	USER_FOUND_2				("user_found_2"),
	USER_FOUND_3				("user_found_3"),
	USER_FOUND_4				("user_found_4"),
	USER_FOUND_5				("user_found_5"),
	USER_FOUND_6				("user_found_6"),
	USER_FOUND_7				("user_found_7"),
	USER_FOUND_8				("user_found_8"),
	USER_FOUND_9				("user_found_9"),
	USER_FOUND_10				("user_found_10"),
	USER_FOUND_11				("user_found_11"),
	USER_FOUND_12				("user_found_12"),
	USER_FOUND_13				("user_found_13"),
	USER_FOUND_14				("user_found_14"),
	USER_FOUND_15				("user_found_15"),
	USER_FOUND_16				("user_found_16"),
	USER_FOUND_17				("user_found_17"),
	USER_FOUND_18				("user_found_18"),
	USER_FOUND_19				("user_found_19"),
	USER_NOT_FOUND				("user_not_found"),
	USER_EXIT					("user_exit"),
	USER_LEFT					("user_left"),
	USER_NUMBER					("user_number"),
	USER_REASON					("user_reason"),
	USER_REASON_YES				("user_reason_yes"),
	USER_REASON_YES_DESC		("user_reason_yes_desc"),
	USER_REASON_NO				("user_reason_no"),
	USER_REASON_NO_DESC			("user_reason_no_desc"),
	USER_REASON_QUESTION		("user_reason_question"),
	USER_REMINDER_SET			("user_reminder_set"),
	USER_REMINDER_NOT_SET		("user_reminder_not_set"),
	USER_INFO_TITLE				("user_info_title"),
	USER_INFO_ID				("user_info_id"),
	USER_INFO_CUR_WARNING		("user_info_cur_warning"),
	USER_INFO_TOT_WARNING		("user_info_tot_warning"),
	USER_INFO_TOT_BANS			("user_info_tot_bans"),
	USER_INFO_BANNED			("user_info_banned"),
	USER_INFO_YES				("user_info_yes"),
	USER_INFO_NO				("user_info_no"),
	USER_INFO_JOIN_DATE			("user_info_join_date"),
	USER_INFO_NEW_JOIN_DATE		("user_info_new_join_date"),
	USER_INFO_WATCH_LEVEL		("user_info_watch_level"),
	USER_INFO_LEVEL				("user_info_level"),
	USER_INFO_EXPERIENCE		("user_info_experience"),
	USER_INFO_UNLOCKED_ROLE		("user_info_unlocked_role"),
	USER_INFO_TOT_EXPERIENCE	("user_info_tot_experience"),
	USER_INFO_BALANCE			("user_info_balance"),
	USER_INFO_NAMES				("user_info_names"),
	USER_INFO_NICKNAMES			("user_info_nicknames"),
	USER_INFO_TITLE_NAMES		("user_info_title_names"),
	USER_INFO_NAMES_ERR			("user_info_names_err"),
	USER_INFO_TITLE_NICKNAMES	("user_info_title_nicknames"),
	USER_INFO_NICKNAMES_ERR		("user_info_nicknames_err"),
	USER_INFO_DELETED_MESSAGES	("user_info_deleted_messages"),
	USER_INFO_EVENTS			("user_info_events"),
	USER_INFO_ERR				("user_info_err"),
	USER_DELETE_HELP			("user_delete_help"),
	USER_DELETE_QUESTION		("user_delete_question"),
	USER_DELETE_DELETING		("user_delete_deleting"),
	USER_DELETE_ABORT			("user_delete_abort"),
	USER_DELETE_REMOVED			("user_delete_removed"),
	USER_DELETE_NOTHING			("user_delete_nothing"),
	USER_DELETE_NOTHING_2		("user_delete_nothing_2"),
	USER_WARNING_HELP			("user_warning_help"),
	USER_WARNING_CLEAR			("user_warning_clear"),
	USER_WARNING_UPDATED		("user_warning_updated"),
	USER_WARNING_UPDATED_LIMIT	("user_warning_updated_limit"),
	USER_WARNING_NEVER_WARNED	("user_warning_never_warned"),
	USER_WARNING_IS_MUTED		("user_warning_is_muted"),
	USER_MUTE_ALREADY_MUTED		("user_mute_already_muted"),
	USER_MUTE_TIME				("user_mute_time"),
	USER_MUTE_NO_TIME			("user_mute_no_time"),
	USER_MUTE_PERM				("user_mute_perm"),
	USER_MUTE_PERM_DESC			("user_mute_perm_desc"),
	USER_MUTE_CHOICE			("user_mute_choice"),
	USER_MUTE_FORMAT			("user_mute_format"),
	USER_MUTE_LEFT				("user_mute_left"),
	USER_MUTE_ORDER_1			("user_mute_order_1"),
	USER_MUTE_ORDER_2			("user_mute_order_2"),
	USER_MUTE_NUMERIC			("user_mute_numeric"),
	USER_UNMUTE_RUN				("user_unmute_run"),
	USER_UNMUTE_RUN_2			("user_unmute_run_2"),
	USER_UNMUTE_NOT_MUTED		("user_unmute_not_muted"),
	USER_UNMUTE_INFINITE		("user_unmute_infinite"),
	USER_UNMUTE_IS_BANNED		("user_unmute_is_banned"),
	USER_UNBAN_NOT_BANNED		("user_unban_not_banned"),
	USER_ASSIGN_HELP			("user_assign_help"),
	USER_ASSIGN_NO_ROLES		("user_assign_no_roles"),
	USER_ASSIGN_ADD				("user_assign_add"),
	USER_ASSIGN_ALREADY			("user_assign_already"),
	USER_REMOVE_HELP			("user_remove_help"),
	USER_REMOVE_NO_ROLES		("user_remove_no_roles"),
	USER_REMOVE_RETRACT			("user_remove_retract"),
	USER_REMOVE_ALREADY			("user_remove_already"),
	USER_HISTORY_TIME			("user_history_time"),
	USER_HISTORY_ROLE			("user_history_role"),
	USER_HISTORY_BY				("user_history_by"),
	USER_HISTORY_MINUTES		("user_history_minutes"),
	USER_HISTORY_REASON			("user_history_reason"),
	USER_HISTORY_DISPLAY		("user_history_display"),
	USER_HISTORY_TITLE_EMPTY	("user_history_title_empty"),
	USER_HISTORY_EMPTY			("user_history_empty"),
	USER_WATCH_CACHE			("user_watch_cache"),
	USER_WATCH_HELP				("user_watch_help"),
	USER_WATCH_ADDED			("user_watch_added"),
	USER_UNWATCH				("user_unwatch"),
	USER_UNWATCH_NOT_WATCHED	("user_unwatch_not_watched"),
	USER_GIFT_EXP				("user_gift_exp"),
	USER_GIFT_EXP_ADDED			("user_gift_exp_added"),
	USER_SET_EXP				("user_set_exp"),
	USER_SET_EXP_UPDATED		("user_set_exp_updated"),
	USER_LEVEL					("user_level"),
	USER_LEVEL_UPDATED			("user_level_updated"),
	USER_LEVEL_ERR				("user_level_err"),
	USER_GIFT_CUR				("user_gift_cur"),
	USER_GIFT_CUR_ADDED			("user_gift_cur_added"),
	USER_SET_CUR				("user_set_cur"),
	USER_SET_CUR_UPDATED		("user_set_cur_updated"),
	USER_BAN_DM					("user_ban_dm"),
	USER_BAN_DM_2				("user_ban_dm_2"),
	USER_BAN_REASON				("user_ban_reason"),
	USER_BAN_ORDER				("user_ban_order"),
	USER_BAN_LEFT				("user_ban_left"),
	USER_UNBAN_ORDER			("user_unban_order"),
	USER_KICK_ORDER				("user_kick_order"),
	USER_KICK_DM				("user_kick_dm"),
	USER_KICK_LEFT				("user_kick_left"),
	USER_REGISTER_COMPLETE		("user_register_complete"),
		
	WRITE_HELP					("write_help"),
	WRITE_UPDATE				("write_update"),
	WRITE_SENT					("write_sent"),
	WRITE_TOO_LONG				("write_too_long"),
	WRITE_NO_SCREENS			("write_no_screens"),
	
	RANDOMSHOP_HELP				("randomshop_help"),
	RANDOMSHOP_NOT_AVAILABLE	("randomshop_not_available"),
	RANDOMSHOP_WEP_TYPES		("randomshop_wep_types"),
	RANDOMSHOP_WEP_CATEGORIES	("randomshop_wep_categories"),
	RANDOMSHOP_WEP_TYPE_NA		("randomshop_wep_type_na"),
	RANDOMSHOP_WEP_CAT_NA		("randomshop_wep_cat_na"),
	RANDOMSHOP_BALANCE_ERR		("randomshop_balance_err"),
	RANDOMSHOP_NO_ITEMS			("randomshop_no_items"),
	RANDOMSHOP_ERR				("randomshop_err"),
	RANDOMSHOP_ERR_2			("randomshop_err_2"),
	RANDOMSHOP_REWARD			("randomshop_reward"),
	
	CENSOR_REMOVED_WARN_1		("censor_removed_warn_1"),
	CENSOR_REMOVED_WARN_2		("censor_removed_warn_2"),
	CENSOR_TITLE_DETECTED		("censor_title_detected"),
	CENSOR_URL_WARN_1			("censor_url_warn_1"),
	CENSOR_URL_WARN_2			("censor_url_warn_2"),
	CENSOR_URL_TITLE			("censor_url_title"),
	CENSOR_URL_NAME				("censor_url_name"),
	CENSOR_URL_USER_ID			("censor_url_user_id"),
	CENSOR_URL_CHANNEL			("censor_url_channel"),
	CENSOR_URL_TYPE				("censor_url_type"),
	CENSOR_URL_TYPE_NAME		("censor_url_type_name"),
	CENSOR_URL_FQDN				("censor_url_fqdn"),
	CENSOR_MUTE_REASON			("censor_mute_reason"),
	CENSOR_MUTE_REASON_2		("censor_mute_reason_2"),
	CENSOR_ROLE_ADD_ERR			("censor_role_add_err"),
	CENSOR_SPAM					("censor_spam"),
	
	INVENTORY_DRAW_ERR			("inventory_draw_err"),
	INVENTORY_EMPTY				("inventory_empty"),
	INVENTORY_NAME				("inventory_name"),
	
	BAN_ERR						("ban_err"),
	BAN_TITLE					("ban_title"),
	BAN_MESSAGE_1				("ban_message_1"),
	BAN_MESSAGE_2				("ban_message_2"),
	BAN_MESSAGE_3				("ban_message_3"),
	BAN_MESSAGE_4				("ban_message_4"),
	BAN_DM_LOCKED				("ban_dm_locked"),
	
	KICK_TITLE					("kick_title"),
	KICK_MESSAGE				("kick_message"),
	
	LEFT_TITLE					("left_title"),
	LEFT_MESSAGE_1				("left_message_1"),
	LEFT_MESSAGE_2				("left_message_2"),
	
	JOIN_TITLE					("join_title"),
	JOIN_ERR					("join_err"),
	JOIN_ERR_2					("join_err_2"),
	JOIN_ERR_3					("join_err_3"),
	JOIN_ERR_4					("join_err_4"),
	JOIN_ERR_5					("join_err_5"),
	JOIN_ERR_6					("join_err_6"),
	JOIN_ERR_7					("join_err_7"),
	JOIN_ERR_8					("join_err_8"),
	JOIN_ERR_9					("join_err_9"),
	JOIN_MESSAGE				("join_message"),
	JOIN_PERMISSION_ERR			("join_permission_err"),
	
	NAME_TITLE					("name_title"),
	NAME_REASON					("name_reason"),
	NAME_STAFF_TITLE			("name_staff_title"),
	NAME_STAFF					("name_staff"),
	NAME_STAFF_2				("name_staff_2"),
	NAME_STAFF_IMPERSONATION	("name_staff_impersonation"),
	NAME_STAFF_ERR				("name_staff_err"),
	NAME_STAFF_ERR_2			("name_staff_err_2"),
	NAME_ASSIGN					("name_assign"),
	NAME_ASSIGN_2				("name_assign_2"),
	NAME_ASSIGN_ERR				("name_assign_err"),
	NAME_ASSIGN_ERR_2			("name_assign_err_2"),
	NAME_KICK_DM				("name_kick_dm"),
	NAME_KICK_TITLE				("name_kick_title"),
	NAME_KICK_MESSAGE_1			("name_kick_message_1"),
	NAME_KICK_MESSAGE_2			("name_kick_message_2"),
	NAME_KICK_MESSAGE_3			("name_kick_message_3"),
	NAME_KICK_MESSAGE_4			("name_kick_message_4"),
	NAME_KICK_REASON			("name_kick_reason"),
	NAME_KICK_PERMISSION_ERR	("name_kick_permission_err"),
	NAME_KICK_PERMISSION_ERR_2	("name_kick_permission_err_2"),
	
	EDIT_TITLE					("edit_title"),
	EDIT_TITLE_HISTORY			("edit_title_history"),
	EDIT_TITLE_WATCH			("edit_title_watch"),
	EDIT_WATCH_ERR				("edit_watch_err"),
	
	REACTION_ADDED				("reaction_added"),
	REACTION_REPLY_YES			("reaction_reply_yes"),
	REACTION_REPLY_NO			("reaction_reply_no"),
	REACTION_NO_PERMISSION		("reaction_no_permission"),
	
	DELETE_EDITED_MESSAGE		("delete_edited_message"),
	DELETE_MESSAGE				("delete_message"),
	DELETE_REMOVED_BY			("delete_removed_by"),
	DELETE_SELF					("delete_self"),
	DELETE_PERMISSION_ERR		("delete_permission_err"),
	DELETE_WATCHED				("delete_watched"),
	DELETE_RANK_ROLE_ERR		("delete_rank_role_err"),
	
	ROLE_MUTED_TITLE			("role_muted_title"),
	ROLE_ROLES_REMOVE_ERR		("role_roles_remove_err"),
	ROLE_AGAIN_MUTED_1			("role_again_muted_1"),
	ROLE_AGAIN_MUTED_2			("role_again_muted_2"),
	ROLE_AGAIN_MUTED_3			("role_again_muted_3"),
	ROLE_AGAIN_MUTED_4			("role_again_muted_4"),
	ROLE_MUTE_DM				("role_mute_dm"),
	ROLE_MUTE_DM_2				("role_mute_dm_2"),
	ROLE_MUTE_DM_3				("role_mute_dm_3"),
	ROLE_BAN_DM					("role_ban_dm"),
	ROLE_DM_LOCKED				("role_dm_locked"),
	ROLE_HOURS					("role_hours"),
	ROLE_MINUTES				("role_minutes"),
	ROLE_AND					("role_and"),
	ROLE_BAN_REASON				("role_ban_reason"),
	ROLE_BAN_DM_LOCKED			("role_ban_dm_locked"),
	ROLE_BAN_PERMISSION_ERR		("role_ban_permission_err"),
	ROLE_RETRACTED_TITLE		("role_retracted_title"),
	ROLE_MUTE_REMOVED			("role_mute_removed"),
	ROLE_MUTE_REMOVE_ERR		("role_mute_remove_err"),
	ROLE_FLAG_REMOVE			("role_flag_remove"),
	ROLE_REASSIGN_ERR			("role_reassign_err"),
	ROLE_MANUAL_REMOVE_TITLE	("role_manual_remove_title"),
	ROLE_MANUALLY_REMOVED		("role_manually_removed"),
	ROLE_MANUALLY_REMOVE_PERM	("role_manually_remove_perm"),
	ROLE_REMOVE_MANUALLY_REASON	("role_remove_manually_reason"),
	ROLE_REMOVE_ELAPSED_REASON	("role_remove_elapsed_reason"),
	ROLE_MUTE_MESSAGE_1			("role_mute_message_1"),
	ROLE_MUTE_MESSAGE_2			("role_mute_message_2"),
	ROLE_MUTE_MESSAGE_3			("role_mute_message_3"),
	
	UNBAN_INFO_ERR				("unban_info_err"),
	UNBAN_TITLE					("unban_title"),
	UNBAN_MESSAGE				("unban_message"),
	UNBAN_FLAG_ERR				("unban_flag_err"),
	
	MEOW_HELP					("meow_help"),
	MEOW_HELP_1					("meow_help_1"),
	MEOW_HELP_2					("meow_help_2"),
	MEOW_HELP_3					("meow_help_3"),
	
	PUG_HELP					("pug_help"),
	PUG_HELP_1					("pug_help_1"),
	PUG_HELP_2					("pug_help_2"),
	PUG_HELP_3					("pug_help_3"),
	PUG_HELP_4					("pug_help_4"),
	PUG_HELP_5					("pug_help_5"),
	
	UNMUTE_MESSAGE				("unmute_message"),
	UNMUTE_REMOVE_ERR			("unmute_remove_err"),
	UNMUTE_MESSAGE_2			("unmute_message_2"),
	UNMUTE_RECOUNT_TITLE		("unmute_recount_title"),
	UNMUTE_RECOUNT				("unmute_recount"),
	UNMUTE_RECOUNT_EXCLUDED		("unmute_recount_excluded"),
	UNMUTE_LIMBO				("unmute_limbo");
	
	private String value;
	
	private Translation(String _value) {
		this.value = _value;
	}
	
	public String section() {
		return this.value;
	}
}
