package de.azrael.constructors;

/**
 * Configurations of the Bot for a specific server
 */

public class BotConfigs {
	private boolean isDefault;
	private String commandPrefix;
	private boolean joinMessage;
	private boolean leaveMessage;
	private boolean channelLog;
	private boolean cacheLog;
	private String doubleExperience;
	private boolean forceReason;
	private boolean overrideBan;
	private boolean urlBlacklist;
	private boolean selfDeletedMessages;
	private boolean editedMessages;
	private boolean editedMessagesHistory;
	private boolean notifications;
	private boolean newAccountOnJoin;
	private boolean reassignRoles;
	private boolean collectRankingRoles;
	private int expRateLimit;
	private boolean ignoreMissingPermissions;
	private boolean privatePatchNotes;
	private boolean publicPatchNotes;
	private boolean googleFunctionalities;
	private String googleMainEmail;
	
	/**
	 * Default constructor for default values
	 */
	
	public BotConfigs() {
		this.isDefault = true;
		this.commandPrefix = "H!";
		this.joinMessage = false;
		this.leaveMessage = false;
		this.channelLog = false;
		this.cacheLog = false;
		this.doubleExperience = "off";
		this.forceReason = false;
		this.overrideBan = false;
		this.urlBlacklist = false;
		this.selfDeletedMessages = false;
		this.editedMessages = false;
		this.editedMessagesHistory = false;
		this.notifications = false;
		this.newAccountOnJoin = false;
		this.reassignRoles = false;
		this.collectRankingRoles = false;
		this.expRateLimit = 0;
		this.ignoreMissingPermissions = false;
		this.privatePatchNotes = false;
		this.publicPatchNotes = false;
		this.googleFunctionalities = false;
		this.googleMainEmail = null;
	}
	
	/**
	 * Constructor to fill all values
	 * @param _commandPrefix
	 * @param _joinMessage
	 * @param _leaveMessage
	 * @param _channelLog
	 * @param _cacheLog
	 * @param _doubleExperience
	 * @param _forceReason
	 * @param _overrideBan
	 * @param _urlBlacklist
	 * @param _selfDeletedMessages
	 * @param _editedMessages
	 * @param _editedMessagesHistory
	 * @param _notifications
	 * @param _newAccountOnJoin
	 * @param _reassignRoles
	 * @param _collectRankingRoles
	 * @param _expRateLimit
	 * @param _ignoreMissingPermissions
	 * @param _privatePatchNotes
	 * @param _publicPatchNotes
	 * @param _googleFunctionalities
	 * @param _googleMainEmail
	 */
	
	public BotConfigs(String _commandPrefix, boolean _joinMessage, boolean _leaveMessage, boolean _channelLog
			, boolean _cacheLog, String _doubleExperience, boolean _forceReason, boolean _overrideBan, boolean _urlBlacklist
			, boolean _selfDeletedMessages, boolean _editedMessages, boolean _editedMessagesHistory, boolean _notifications, boolean _newAccountOnJoin
			, boolean _reassignRoles, boolean _collectRankingRoles, int _expRateLimit, boolean _ignoreMissingPermissions, boolean _privatePatchNotes
			, boolean _publicPatchNotes, boolean _googleFunctionalities, String _googleMainEmail) {
		
		this.isDefault = false;
		this.commandPrefix = _commandPrefix;
		this.joinMessage = _joinMessage;
		this.leaveMessage = _leaveMessage;
		this.channelLog = _channelLog;
		this.cacheLog = _cacheLog;
		this.doubleExperience = _doubleExperience;
		this.forceReason = _forceReason;
		this.overrideBan = _overrideBan;
		this.urlBlacklist = _urlBlacklist;
		this.selfDeletedMessages = _selfDeletedMessages;
		this.editedMessages = _editedMessages;
		this.editedMessagesHistory = _editedMessagesHistory;
		this.notifications = _notifications;
		this.newAccountOnJoin = _newAccountOnJoin;
		this.reassignRoles = _reassignRoles;
		this.collectRankingRoles = _collectRankingRoles;
		this.expRateLimit = _expRateLimit;
		this.ignoreMissingPermissions = _ignoreMissingPermissions;
		this.privatePatchNotes = _privatePatchNotes;
		this.publicPatchNotes = _publicPatchNotes;
		this.googleFunctionalities = _googleFunctionalities;
		this.googleMainEmail = _googleMainEmail;
	}
	
	public boolean isDefault() {
		return this.isDefault;
	}
	public String getCommandPrefix() {
		return this.commandPrefix;
	}
	public boolean getJoinMessage() {
		return this.joinMessage;
	}
	public boolean getLeaveMessage() {
		return this.leaveMessage;
	}
	public boolean getChannelLog() {
		return this.channelLog;
	}
	public boolean getCacheLog() {
		return this.cacheLog;
	}
	public String getDoubleExperience() {
		if(this.doubleExperience.equals("off") || this.doubleExperience.equals("on") || this.doubleExperience.equals("auto"))
			return this.doubleExperience;
		return "off";
	}
	public boolean getForceReason() {
		return this.forceReason;
	}
	public boolean getOverrideBan() {
		return this.overrideBan;
	}
	public boolean getUrlBlacklist() {
		return this.urlBlacklist;
	}
	public boolean getSelfDeletedMessages() {
		return this.selfDeletedMessages;
	}
	public boolean getEditedMessages() {
		return this.editedMessages;
	}
	public boolean getEditedMessagesHistory() {
		return this.editedMessagesHistory;
	}
	public boolean getNotifications() {
		return this.notifications;
	}
	public boolean getNewAccountOnJoin() {
		return this.newAccountOnJoin;
	}
	public boolean getReassignRoles() {
		return this.reassignRoles;
	}
	public boolean getCollectRankingRoles() {
		return this.collectRankingRoles;
	}
	public int getExpRateLimit() {
		if(this.expRateLimit < 0)
			return 0;
		return this.expRateLimit;
	}
	public boolean getIgnoreMissingPermissions() {
		return this.ignoreMissingPermissions;
	}
	public boolean getPrivatePatchNotes() {
		return this.privatePatchNotes;
	}
	public boolean getPublicPatchnotes() {
		return this.publicPatchNotes;
	}
	public boolean getGoogleFunctionalities() {
		return this.googleFunctionalities;
	}
	public String getGoogleMainEmail() {
		return this.googleMainEmail;
	}
}
