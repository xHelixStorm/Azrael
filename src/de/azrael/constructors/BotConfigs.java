package de.azrael.constructors;

import java.util.ArrayList;

/**
 * Configurations of the Bot for a specific server
 */

public class BotConfigs {
	private boolean isDefault = false;
	private String commandPrefix = "H!";
	private boolean joinMessage = false;
	private boolean leaveMessage = false;
	private boolean channelLog = false;
	private boolean cacheLog = false;
	private String doubleExperience = "off";
	private int doubleExperienceStart = 7;
	private int doubleExperienceEnd = 1;
	private boolean forceReason = false;
	private boolean overrideBan = false;
	private boolean prohibitUrlsMode = false;
	private boolean selfDeletedMessages = false;
	private boolean editedMessages = false;
	private boolean editedMessagesHistory = false;
	private boolean notifications = false;
	private boolean newAccountOnJoin = false;
	private boolean reassignRoles = false;
	private boolean collectRankingRoles = false;
	private int expRateLimit = 0;
	private boolean ignoreMissingPermissions = false;
	private boolean privatePatchNotes = false;
	private boolean publicPatchNotes = false;
	private boolean googleFunctionalities = false;
	private String googleMainEmail = null;
	private boolean spamDetectionEnabled = false;
	private int spamDetectionChannelLimit = 0;
	private int spamDetectionAllChannelsLimit = 0;
	private int spamDetectionExpires = 0;
	private boolean muteMessageDeleteEnabled = false;
	private boolean muteForceMessageDeletion = false;
	private int muteAutoDeleteMessages = 0;
	private boolean muteSendReason = false;
	private boolean kickMessageDeleteEnabled = false;
	private boolean kickForceMessageDeletion = false;
	private int kickAutoDeleteMessages = 0;
	private boolean kickSendReason = false;
	private boolean banMessageDeleteEnabled = false;
	private boolean banForceMessageDeletion = false;
	private int banAutoDeleteMessages = 0;
	private boolean banSendReason = false;
	private String competitiveTeam1Name = null;
	private String competitiveTeam2Name = null;
	private boolean reactionsEnabled = false;
	private String reactionsEmoji1 = null;
	private String reactionsEmoji2 = null;
	private String reactionsEmoji3 = null;
	private String reactionsEmoji4 = null;
	private String reactionsEmoji5 = null;
	private String reactionsEmoji6 = null;
	private String reactionsEmoji7 = null;
	private String reactionsEmoji8 = null;
	private String reactionsEmoji9 = null;
	private String voteReactionThumbsUp = null;
	private String voteReactionThumbsDown = null;
	private String voteReactionShrug = null;
	
	/**
	 * Default constructor for default values
	 */
	
	public BotConfigs() {
		this.isDefault = true;
	}
	
	/**
	 * Initialize all configuration values
	 * @param _commandPrefix
	 * @param _joinMessage
	 * @param _leaveMessage
	 * @param _channelLog
	 * @param _cacheLog
	 * @param _doubleExperience
	 * @param _doubleExperienceStart
	 * @param _doubleExperienceEnd
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
	 * @param _spamDetectionEnabled
	 * @param _spamDetectionChannelLimit
	 * @param _spamDetectionAllChannelsLimit
	 * @param _spamDetectionExpires
	 * @param _muteMessageDeleteEnabled
	 * @param _muteForceMessageDeletion
	 * @param _muteAutoDeleteMessages
	 * @param _muteSendReason
	 * @param _kickMessageDeleteEnabled
	 * @param _kickForceMessageDeletion
	 * @param _kickAutoDeleteMessages
	 * @param _kickSendReason
	 * @param _banMessageDeleteEnabled
	 * @param _banForceMessageDeletion
	 * @param _banAutoDeleteMessages
	 * @param _banSendReason
	 * @param _competitiveTeam1Name
	 * @param _competitiveTeam2Name
	 * @param _reactionsEnabled
	 * @param _reactionsEmoji1
	 * @param _reactionsEmoji2
	 * @param _reactionsEmoji3
	 * @param _reactionsEmoji4
	 * @param _reactionsEmoji5
	 * @param _reactionsEmoji6
	 * @param _reactionsEmoji7
	 * @param _reactionsEmoji8
	 * @param _reactionsEmoji9
	 * @param _voteReactionThumbsUp
	 * @param _voteReactionThumbsDown
	 * @param _voteReactionShrug
	 */
	public BotConfigs(String _commandPrefix, boolean _joinMessage, boolean _leaveMessage, boolean _channelLog
			, boolean _cacheLog, String _doubleExperience, int _doubleExperienceStart, int _doubleExperienceEnd
			, boolean _forceReason, boolean _overrideBan, boolean _prohibitUrlsMode, boolean _selfDeletedMessages
			, boolean _editedMessages, boolean _editedMessagesHistory, boolean _notifications, boolean _newAccountOnJoin
			, boolean _reassignRoles, boolean _collectRankingRoles, int _expRateLimit, boolean _ignoreMissingPermissions
			, boolean _privatePatchNotes, boolean _publicPatchNotes, boolean _googleFunctionalities, String _googleMainEmail
			, boolean _spamDetectionEnabled, int _spamDetectionChannelLimit, int _spamDetectionAllChannelsLimit
			, int _spamDetectionExpires, boolean _muteMessageDeleteEnabled, boolean _muteForceMessageDeletion
			, int _muteAutoDeleteMessages, boolean _muteSendReason, boolean _kickMessageDeleteEnabled
			, boolean _kickForceMessageDeletion, int _kickAutoDeleteMessages, boolean _kickSendReason
			, boolean _banMessageDeleteEnabled, boolean _banForceMessageDeletion, int _banAutoDeleteMessages
			, boolean _banSendReason, String _competitiveTeam1Name, String _competitiveTeam2Name
			, boolean _reactionsEnabled, String _reactionsEmoji1, String _reactionsEmoji2, String _reactionsEmoji3
			, String _reactionsEmoji4, String _reactionsEmoji5, String _reactionsEmoji6, String _reactionsEmoji7
			, String _reactionsEmoji8, String _reactionsEmoji9, String _voteReactionThumbsUp, String _voteReactionThumbsDown
			, String _voteReactionShrug) {
		
		this.commandPrefix = _commandPrefix;
		this.joinMessage = _joinMessage;
		this.leaveMessage = _leaveMessage;
		this.channelLog = _channelLog;
		this.cacheLog = _cacheLog;
		this.doubleExperience = _doubleExperience;
		this.doubleExperienceStart = _doubleExperienceStart;
		this.doubleExperienceEnd = _doubleExperienceEnd;
		this.forceReason = _forceReason;
		this.overrideBan = _overrideBan;
		this.prohibitUrlsMode = _prohibitUrlsMode;
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
		this.spamDetectionEnabled = _spamDetectionEnabled;
		this.spamDetectionChannelLimit = _spamDetectionChannelLimit;
		this.spamDetectionAllChannelsLimit = _spamDetectionAllChannelsLimit;
		this.spamDetectionExpires = _spamDetectionExpires;
		this.muteMessageDeleteEnabled = _muteMessageDeleteEnabled;
		this.muteForceMessageDeletion = _muteForceMessageDeletion;
		this.muteAutoDeleteMessages = _muteAutoDeleteMessages;
		this.muteSendReason = _muteSendReason;
		this.kickMessageDeleteEnabled = _kickMessageDeleteEnabled;
		this.kickForceMessageDeletion = _kickForceMessageDeletion;
		this.kickAutoDeleteMessages = _kickAutoDeleteMessages;
		this.kickSendReason = _kickSendReason;
		this.banMessageDeleteEnabled = _banMessageDeleteEnabled;
		this.banForceMessageDeletion = _banForceMessageDeletion;
		this.banAutoDeleteMessages = _banAutoDeleteMessages;
		this.banSendReason = _banSendReason;
		this.competitiveTeam1Name = _competitiveTeam1Name;
		this.competitiveTeam2Name = _competitiveTeam2Name;
		this.reactionsEnabled = _reactionsEnabled;
		this.reactionsEmoji1 = _reactionsEmoji1;
		this.reactionsEmoji2 = _reactionsEmoji2;
		this.reactionsEmoji3 = _reactionsEmoji3;
		this.reactionsEmoji4 = _reactionsEmoji4;
		this.reactionsEmoji5 = _reactionsEmoji5;
		this.reactionsEmoji6 = _reactionsEmoji6;
		this.reactionsEmoji7 = _reactionsEmoji7;
		this.reactionsEmoji8 = _reactionsEmoji8;
		this.reactionsEmoji9 = _reactionsEmoji9;
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
	public int getDoubleExperienceStart() {
		return this.doubleExperienceStart;
	}
	public int getDoubleExperienceEnd() {
		return this.doubleExperienceEnd;
	}
	public boolean getForceReason() {
		return this.forceReason;
	}
	public boolean getOverrideBan() {
		return this.overrideBan;
	}
	public boolean getProhibitUrlsMode() {
		return this.prohibitUrlsMode;
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
	public boolean isSpamDetectionEnabled() {
		return this.spamDetectionEnabled;
	}
	public int getSpamDetectionChannelLimit() {
		return this.spamDetectionChannelLimit;
	}
	public int getSpamDetectionAllChannelsLimit() {
		return this.spamDetectionAllChannelsLimit;
	}
	public int getSpamDetectionExpires() {
		return this.spamDetectionExpires;
	}
	public boolean isMuteMessageDeleteEnabled() {
		return this.muteMessageDeleteEnabled;
	}
	public boolean getMuteForceMessageDeletion() {
		return this.muteForceMessageDeletion;
	}
	public int getMuteAutoDeleteMessages() {
		return this.muteAutoDeleteMessages;
	}
	public boolean getMuteSendReason() {
		return this.muteSendReason;
	}
	public boolean isKickMessageDeleteEnabled() {
		return this.kickMessageDeleteEnabled;
	}
	public boolean getKickForceMessageDeletion() {
		return this.kickForceMessageDeletion;
	}
	public int getKickAutoDeleteMessages() {
		return this.kickAutoDeleteMessages;
	}
	public boolean getKickSendReason() {
		return this.kickSendReason;
	}
	public boolean isBanMessageDeleteEnabled() {
		return this.banMessageDeleteEnabled;
	}
	public boolean getBanForceMessageDeletion() {
		return this.banForceMessageDeletion;
	}
	public int getBanAutoDeleteMessages() {
		return this.banAutoDeleteMessages;
	}
	public boolean getBanSendReason() {
		return this.banSendReason;
	}
	public String getCompetitiveTeam1Name() {
		return this.competitiveTeam1Name;
	}
	public String getCompetitiveTeam2Name() {
		return this.competitiveTeam2Name;
	}
	public boolean isReactionsEnabled() {
		return this.reactionsEnabled;
	}
	public String getReactionsEmoji1() {
		return this.reactionsEmoji1;
	}
	public String getReactionsEmoji2() {
		return this.reactionsEmoji2;
	}
	public String getReactionsEmoji3() {
		return this.reactionsEmoji3;
	}
	public String getReactionsEmoji4() {
		return this.reactionsEmoji4;
	}
	public String getReactionsEmoji5() {
		return this.reactionsEmoji5;
	}
	public String getReactionsEmoji6() {
		return this.reactionsEmoji6;
	}
	public String getReactionsEmoji7() {
		return this.reactionsEmoji7;
	}
	public String getReactionsEmoji8() {
		return this.reactionsEmoji8;
	}
	public String getReactionsEmoji9() {
		return this.reactionsEmoji9;
	}
	public String[] getReactionEmojis() {
		ArrayList<String> reactions = new ArrayList<String>();
		reactions.add(reactionsEmoji1);
		reactions.add(reactionsEmoji2);
		reactions.add(reactionsEmoji3);
		reactions.add(reactionsEmoji4);
		reactions.add(reactionsEmoji5);
		reactions.add(reactionsEmoji6);
		reactions.add(reactionsEmoji7);
		reactions.add(reactionsEmoji8);
		reactions.add(reactionsEmoji9);
		return (String[])reactions.toArray();
	}
	public String getVoteReactionThumbsUp() {
		return this.voteReactionThumbsUp;
	}
	public String getVoteReactionThumbsDown() {
		return this.voteReactionThumbsDown;
	}
	public String getVoteReactionShrug() {
		return this.voteReactionShrug;
	}
	public String[] getVoteReactions() {
		ArrayList<String> reactions = new ArrayList<String>();
		reactions.add(voteReactionThumbsUp);
		reactions.add(voteReactionThumbsDown);
		reactions.add(voteReactionShrug);
		return (String[])reactions.toArray();
	}
}
