package de.azrael.webserver;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import de.azrael.constructors.BotConfigs;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Command;
import de.azrael.sql.BotConfiguration;
import de.azrael.sql.DiscordRoles;
import de.azrael.util.FileHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;

public class HandlerGET {
	public static void handleRequest(ReadyEvent e, PrintWriter out, String endpoint, String [] queryParams) {
		JSONObject json = null;
		if(endpoint.startsWith("users")) {
			json = usersEndpoint(e, out, endpoint, queryParams);
		}
		else if(endpoint.startsWith("guilds")) {
			json = guildsEndpoint(e, out, endpoint, queryParams);
		}
		
		if(json != null) {
			if(!json.has("done"))
				WebserviceUtils.return200(out, json.toString(), true, true);
		}
		else {
			WebserviceUtils.return400(out, "No listener on endpoint "+endpoint, true);
		}
	}
	
	private static JSONObject usersEndpoint(ReadyEvent e, PrintWriter out, String endpoint, String [] queryParams) {
		final String [] uris = endpoint.split("/");
		if(uris.length == 2) {
			if(uris[1].matches("[0-9]{17,18}")) {
				User user = e.getJDA().getUserById(uris[1]);
				if(user != null) {
					JSONObject json = new JSONObject();
					json.put("user_id", user.getId());
					json.put("name", user.getName()+"#"+user.getDiscriminator());
					JSONArray jsonArray = new JSONArray();
					json.put("mutualGuilds", jsonArray);
					for(Guild guild : user.getMutualGuilds()) {
						JSONObject server = new JSONObject();
						server.put("guild_id", guild.getId());
						server.put("name", guild.getName());
						Member member = guild.getMember(user);
						server.put("isUserAdmin", UserPrivs.isUserAdmin(member));
						server.put("isUserMod", UserPrivs.isUserMod(member));
						server.put("isUserCommunity", UserPrivs.isUserCommunity(member));
						server.put("isUserBotAdmin", BotConfiguration.SQLisAdministrator(user.getIdLong(), guild.getIdLong()));
						JSONArray serverRoles = new JSONArray();
						server.put("roles", serverRoles);
						for(Role role : member.getRoles()) {
							JSONObject serverRole = new JSONObject();
							serverRole.put("role_id", role.getId());
							serverRole.put("name", role.getName());
							if(role.getColor() != null)
								serverRole.put("color", "#"+Integer.toHexString(role.getColorRaw()));
							else
								serverRole.put("color", "#99AAB5");
							serverRole.put("position", role.getPosition());
							serverRoles.put(serverRole);
						}
						jsonArray.put(server);
					}
					return json;
				}
				else {
					WebserviceUtils.return404(out, "User not found", true);
					return new JSONObject().append("done", true);
				}
			}
		}
		return null;
	}
	
	private static JSONObject guildsEndpoint(ReadyEvent e, PrintWriter out, String endpoint, String [] queryParams) {
		final String [] uris = endpoint.split("/");
		if(uris.length == 2) {
			if(uris[1].equals("options")) {
				List<Guild> guilds = e.getJDA().getGuilds();
				String lookUpUser = null;
				if(queryParams!= null && queryParams.length > 0) {
					String [] lookUpGuilds = null;
					for(int i = 0; i < queryParams.length; i++) {
						final String [] subQueryParam = queryParams[i].split("=");
						if(subQueryParam[0].equals("filter")) {
							lookUpGuilds = subQueryParam[1].split(",");
						}
						else if(subQueryParam[0].equals("user")) {
							lookUpUser = subQueryParam[1];
						}
					}
					final String [] finalLookUpGuilds = lookUpGuilds;
					guilds = guilds.parallelStream().filter(guild -> {
						for(int i = 0; i < finalLookUpGuilds.length; i++) {
							if(guild.getId().equals(finalLookUpGuilds[i])) {
								return true;
							}
						}
						return false;
					}).collect(Collectors.toList());
				}
				
				JSONArray jsonArray = new JSONArray();
				for(final Guild guild : guilds) {
					JSONObject guildObject = new JSONObject();
					guildObject.put("guild_id", guild.getId());
					guildObject.put("name", guild.getName());
					
					JSONObject user = new JSONObject();
					if(lookUpUser != null) {
						final Member member = guild.getMemberById(lookUpUser);
						if(member != null) {
							user.put("user_id", member.getUser().getId());
							user.put("name", member.getUser().getName()+"#"+member.getUser().getDiscriminator());
							user.put("isUserAdmin", UserPrivs.isUserAdmin(member));
							user.put("isUserMod", UserPrivs.isUserMod(member));
							user.put("isUserCommunity", UserPrivs.isUserCommunity(member));
							user.put("isUserBotAdmin", BotConfiguration.SQLisAdministrator(member.getUser().getIdLong(), guild.getIdLong()));
							guildObject.put("user", user);
						}
						else
							guildObject.put("user", (String)null);
					}
					else
						guildObject.put("user", (String)null);
					
					JSONArray serverRoles = new JSONArray();
					guildObject.put("roles", serverRoles);
					final var roles = DiscordRoles.SQLgetRoles(guild.getIdLong());
					for(final Role role : guild.getRoles()) {
						JSONObject curRole = new JSONObject();
						curRole.put("role_id", role.getId());
						curRole.put("name", role.getName());
						curRole.put("position", role.getPosition());
						if(role.getColor() != null)
							curRole.put("color", "#"+Integer.toHexString(role.getColorRaw()));
						else
							curRole.put("color", "#99AAB5");
						final var registeredRole = roles.parallelStream().filter(f -> f.getRole_ID() == role.getIdLong()).findAny().orElse(null);
						curRole.put("type", (registeredRole != null ? registeredRole.getCategory_Name() : null));
						curRole.put("permission", (registeredRole != null ? registeredRole.getLevel() : 0));
						serverRoles.put(curRole);
					}
					
					final BotConfigs botConfig = BotConfiguration.SQLgetBotConfigs(guild.getIdLong());
					guildObject.put("General_Administrator", 0);
					guildObject.put("General_CommandPrefix", botConfig.getCommandPrefix());
					guildObject.put("General_JoinMessage", botConfig.getJoinMessage());
					guildObject.put("General_LeaveMessage", botConfig.getLeaveMessage());
					guildObject.put("General_CacheLog", botConfig.getCacheLog());
					guildObject.put("General_ProhibitUrlsMode", botConfig.getProhibitUrlsMode());
					guildObject.put("General_DoubleExperience", botConfig.getDoubleExperience());
					guildObject.put("General_DoubleExperienceStart", botConfig.getDoubleExperienceStart());
					guildObject.put("General_DoubleExperienceEnd", botConfig.getDoubleExperienceEnd());
					guildObject.put("General_ForceReason", botConfig.getForceReason());
					guildObject.put("General_OverrideBan", botConfig.getOverrideBan());
					guildObject.put("General_SelfDeletedMessages", botConfig.getSelfDeletedMessages());
					guildObject.put("General_EditedMessages", botConfig.getEditedMessages());
					guildObject.put("General_EditedMessagesHistory", botConfig.getEditedMessagesHistory());
					guildObject.put("General_Notifications", botConfig.getNotifications());
					guildObject.put("General_NewAccountOnJoin", botConfig.getNewAccountOnJoin());
					guildObject.put("General_ReassignRoles", botConfig.getReassignRoles());
					guildObject.put("General_CollectRankingRoles", botConfig.getCollectRankingRoles());
					guildObject.put("General_ExpRateLimit", botConfig.getExpRateLimit());
					guildObject.put("General_IgnoreMissingPermissions", botConfig.getIgnoreMissingPermissions());
					
					guildObject.put("Google_Functionalities", botConfig.getGoogleFunctionalities());
					guildObject.put("Google_MainEmail", botConfig.getGoogleMainEmail());
					
					guildObject.put("SpamDetection_Enabled", botConfig.isSpamDetectionEnabled());
					guildObject.put("SpamDetection_ChannelLimit", botConfig.getSpamDetectionChannelLimit());
					guildObject.put("SpamDetection_AllChannelsLimit", botConfig.getSpamDetectionAllChannelsLimit());
					guildObject.put("SpamDetection_Expires", botConfig.getSpamDetectionExpires());
					
					guildObject.put("Patch_PrivatePatchNotes", botConfig.getPrivatePatchNotes());
					guildObject.put("Patch_PublicPatchNotes", botConfig.getPublicPatchnotes());
					
					guildObject.put("Mute_MessageDeleteEnabled", botConfig.isMuteMessageDeleteEnabled());
					guildObject.put("Mute_ForceMessageDeletion", botConfig.getMuteForceMessageDeletion());
					guildObject.put("Mute_AutoDeleteMessages", botConfig.getMuteAutoDeleteMessages());
					guildObject.put("Mute_SendReason", botConfig.getMuteSendReason());
					
					guildObject.put("Kick_MessageDeleteEnabled", botConfig.isKickMessageDeleteEnabled());
					guildObject.put("Kick_ForceMessageDeletion", botConfig.getKickForceMessageDeletion());
					guildObject.put("Kick_AutoDeleteMessages", botConfig.getKickAutoDeleteMessages());
					guildObject.put("Kick_SendReason", botConfig.getKickSendReason());
					
					guildObject.put("Ban_MessageDeleteEnabled", botConfig.isBanMessageDeleteEnabled());
					guildObject.put("Ban_ForceMessageDeletion", botConfig.getBanForceMessageDeletion());
					guildObject.put("Ban_AutoDeleteMessages", botConfig.getBanAutoDeleteMessages());
					guildObject.put("Ban_SendReason", botConfig.getBanSendReason());
					
					guildObject.put("Competitive_Team1Name", botConfig.getCompetitiveTeam1Name());
					guildObject.put("Competitive_Team2Name", botConfig.getCompetitiveTeam2Name());
					
					guildObject.put("Reactions_Enabled", botConfig.isReactionsEnabled());
					guildObject.put("Reactions_Emoji1", botConfig.getReactionsEmoji1());
					guildObject.put("Reactions_Emoji2", botConfig.getReactionsEmoji2());
					guildObject.put("Reactions_Emoji3", botConfig.getReactionsEmoji3());
					guildObject.put("Reactions_Emoji4", botConfig.getReactionsEmoji4());
					guildObject.put("Reactions_Emoji5", botConfig.getReactionsEmoji5());
					guildObject.put("Reactions_Emoji6", botConfig.getReactionsEmoji6());
					guildObject.put("Reactions_Emoji7", botConfig.getReactionsEmoji7());
					guildObject.put("Reactions_Emoji8", botConfig.getReactionsEmoji8());
					guildObject.put("Reactions_Emoji9", botConfig.getReactionsEmoji9());
					
					guildObject.put("Vote_ReactionThumbsUp", botConfig.getVoteReactionThumbsUp());
					guildObject.put("Vote_ReactionThumbsDown", botConfig.getVoteReactionThumbsDown());
					guildObject.put("Vote_ReactionShrug", botConfig.getVoteReactionShrug());
					
					guildObject.put("CustomMessages_reactionmessage", FileHandler.readFile("./files/Guilds/"+guild.getId()+"/reactionmessage.txt"));
					guildObject.put("CustomMessages_verificationmessage", FileHandler.readFile("./files/Guilds/"+guild.getId()+"/verificationmessage.txt"));
					guildObject.put("CustomMessages_assignmessage", FileHandler.readFile("./files/Guilds/"+guild.getId()+"/assignmessage.txt"));
					
					final Boolean[] commands = (Boolean[])BotConfiguration.SQLgetCommand(guild.getIdLong(), 5, Command.values()).toArray();
					guildObject.put("Commands_Display", commands[0]);
					guildObject.put("Commands_DisplayRoles", commands[1]);
					guildObject.put("Commands_DisplayRegisteredRoles", commands[2]);
					guildObject.put("Commands_DisplayRankingRoles", commands[3]);
					guildObject.put("Commands_DisplayTextChannels", commands[4]);
					guildObject.put("Commands_DisplayVoiceChannels", commands[5]);
					guildObject.put("Commands_DisplayRegisteredChannels", commands[6]);
					guildObject.put("Commands_DisplayDailies", commands[7]);
					guildObject.put("Commands_DisplayWatchedUsers", commands[8]);
					guildObject.put("Commands_DisplayCategories", commands[9]);
					guildObject.put("Commands_DisplayRegisteredCategories", commands[10]);
					guildObject.put("Commands_Help", commands[11]);
					guildObject.put("Commands_Register", commands[12]);
					guildObject.put("Commands_RegisterRole", commands[13]);
					guildObject.put("Commands_RegisterTextChannel", commands[14]);
					guildObject.put("Commands_RegisterTextChannelUrl", commands[15]);
					guildObject.put("Commands_RegisterTextChannelTxt", commands[16]);
					guildObject.put("Commands_RegisterRankingRole", commands[17]);
					guildObject.put("Commands_RegisterTextChannels", commands[18]);
					guildObject.put("Commands_RegisterUsers", commands[19]);
					guildObject.put("Commands_RegisterCategory", commands[20]);
					guildObject.put("Commands_Set", commands[21]);
					guildObject.put("Commands_SetPermissions", commands[22]);
					guildObject.put("Commands_SetChannelCensor", commands[23]);
					guildObject.put("Commands_SetWarnings", commands[24]);
					guildObject.put("Commands_SetRanking", commands[25]);
					guildObject.put("Commands_SetMaxExperience", commands[26]);
					guildObject.put("Commands_SetDefaultLevelSkin", commands[27]);
					guildObject.put("Commands_SetDefaultRankSkin", commands[28]);
					guildObject.put("Commands_SetDefaultProfileSkin", commands[29]);
					guildObject.put("Commands_SetDefaultIconSkin", commands[30]);
					guildObject.put("Commands_SetDailyItem", commands[31]);
					guildObject.put("Commands_SetGiveawayItems", commands[32]);
					guildObject.put("Commands_SetCompServer", commands[33]);
					guildObject.put("Commands_SetMaxClanMembers", commands[34]);
					guildObject.put("Commands_SetMap", commands[35]);
					guildObject.put("Commands_SetMatchmakingMembers", commands[36]);
					guildObject.put("Commands_SetLanguage", commands[37]);
					guildObject.put("Commands_User", commands[38]);
					guildObject.put("Commands_UserInformation", commands[39]);
					guildObject.put("Commands_UserDeleteMessages", commands[40]);
					guildObject.put("Commands_UserWarning", commands[41]);
					guildObject.put("Commands_UserMute", commands[42]);
					guildObject.put("Commands_UserUnmute", commands[43]);
					guildObject.put("Commands_UserBan", commands[44]);
					guildObject.put("Commands_UserUnban", commands[45]);
					guildObject.put("Commands_UserKick", commands[46]);
					guildObject.put("Commands_UserAssignRole", commands[47]);
					guildObject.put("Commands_UserRemoveRole", commands[48]);
					guildObject.put("Commands_UserHistory", commands[49]);
					guildObject.put("Commands_UserWatch", commands[50]);
					guildObject.put("Commands_UserUnwatch", commands[51]);
					guildObject.put("Commands_UserGiftExperience", commands[52]);
					guildObject.put("Commands_UserSetExperience", commands[53]);
					guildObject.put("Commands_UserLevel", commands[54]);
					guildObject.put("Commands_UserGiftCurrency", commands[55]);
					guildObject.put("Commands_UserSetCurrency", commands[56]);
					guildObject.put("Commands_Filter", commands[57]);
					guildObject.put("Commands_FilterWordFilter", commands[58]);
					guildObject.put("Commands_FilterNameFilter", commands[59]);
					guildObject.put("Commands_FilterNameKick", commands[60]);
					guildObject.put("Commands_FilterFunnyNames", commands[61]);
					guildObject.put("Commands_FilterStaffNames", commands[62]);
					guildObject.put("Commands_FilterProhibitedUrls", commands[63]);
					guildObject.put("Commands_FilterAllowedUrls", commands[64]);
					guildObject.put("Commands_FilterProhibitedSubs", commands[65]);
					guildObject.put("Commands_RoleReaction", commands[66]);
					guildObject.put("Commands_Remove", commands[67]);
					guildObject.put("Commands_HeavyCensoring", commands[68]);
					guildObject.put("Commands_Mute", commands[69]);
					guildObject.put("Commands_Google", commands[70]);
					guildObject.put("Commands_Subscribe", commands[71]);
					guildObject.put("Commands_SubscribeRss", commands[72]);
					guildObject.put("Commands_SubscribeTwitter", commands[73]);
					guildObject.put("Commands_SubscribeReddit", commands[74]);
					guildObject.put("Commands_SubscribeYouTube", commands[75]);
					guildObject.put("Commands_SubscribeTwitch", commands[76]);
					guildObject.put("Commands_Write", commands[77]);
					guildObject.put("Commands_Edit", commands[78]);
					guildObject.put("Commands_Accept", commands[79]);
					guildObject.put("Commands_Deny", commands[80]);
					guildObject.put("Commands_Language", commands[81]);
					guildObject.put("Commands_Schedule", commands[82]);
					guildObject.put("Commands_Prune", commands[83]);
					guildObject.put("Commands_Warn", commands[84]);
					guildObject.put("Commands_Invites", commands[85]);
					guildObject.put("Commands_About", commands[86]);
					guildObject.put("Commands_Daily", commands[87]);
					guildObject.put("Commands_Inventory", commands[88]);
					guildObject.put("Commands_Meow", commands[89]);
					guildObject.put("Commands_Pug", commands[90]);
					guildObject.put("Commands_Profile", commands[91]);
					guildObject.put("Commands_Rank", commands[92]);
					guildObject.put("Commands_Shop", commands[93]);
					guildObject.put("Commands_Top", commands[94]);
					guildObject.put("Commands_Use", commands[95]);
					guildObject.put("Commands_Quiz", commands[96]);
					guildObject.put("Commands_Randomshop", commands[97]);
					guildObject.put("Commands_Patchnotes", commands[98]);
					guildObject.put("Commands_DoubleExperience", commands[99]);
					guildObject.put("Commands_Equip", commands[100]);
					guildObject.put("Commands_Matchmaking", commands[101]);
					guildObject.put("Commands_Clan", commands[102]);
					guildObject.put("Commands_Cw", commands[103]);
					guildObject.put("Commands_Room", commands[104]);
					guildObject.put("Commands_RoomClose", commands[105]);
					guildObject.put("Commands_RoomWinner", commands[106]);
					guildObject.put("Commands_RoomReopen", commands[107]);
					guildObject.put("Commands_Stats", commands[108]);
					guildObject.put("Commands_Join", commands[109]);
					guildObject.put("Commands_Leave", commands[110]);
					guildObject.put("Commands_Queue", commands[111]);
					guildObject.put("Commands_Changemap", commands[112]);
					guildObject.put("Commands_Master", commands[113]);
					guildObject.put("Commands_Pick", commands[114]);
					guildObject.put("Commands_Restrict", commands[115]);
					guildObject.put("Commands_Start", commands[116]);
					guildObject.put("Commands_Web", commands[117]);
					
					final Integer[] permissions = (Integer[])BotConfiguration.SQLgetCommand(guild.getIdLong(), 2, Command.values()).toArray();
					guildObject.put("CommandLevels_Display", permissions[0]);
					guildObject.put("CommandLevels_DisplayRoles", permissions[1]);
					guildObject.put("CommandLevels_DisplayRegisteredRoles", permissions[2]);
					guildObject.put("CommandLevels_DisplayRankingRoles", permissions[3]);
					guildObject.put("CommandLevels_DisplayTextChannels", permissions[4]);
					guildObject.put("CommandLevels_DisplayVoiceChannels", permissions[5]);
					guildObject.put("CommandLevels_DisplayRegisteredChannels", permissions[6]);
					guildObject.put("CommandLevels_DisplayDailies", permissions[7]);
					guildObject.put("CommandLevels_DisplayWatchedUsers", permissions[8]);
					guildObject.put("CommandLevels_DisplayCategories", permissions[9]);
					guildObject.put("CommandLevels_DisplayRegisteredCategories", permissions[10]);
					guildObject.put("CommandLevels_Help", permissions[11]);
					guildObject.put("CommandLevels_Register", permissions[12]);
					guildObject.put("CommandLevels_RegisterRole", permissions[13]);
					guildObject.put("CommandLevels_RegisterTextChannel", permissions[14]);
					guildObject.put("CommandLevels_RegisterTextChannelURL", permissions[15]);
					guildObject.put("CommandLevels_RegisterTextChannelTXT", permissions[16]);
					guildObject.put("CommandLevels_RegisterRankingRole", permissions[17]);
					guildObject.put("CommandLevels_RegisterTextChannels", permissions[18]);
					guildObject.put("CommandLevels_RegisterUsers", permissions[19]);
					guildObject.put("CommandLevels_RegisterCategory", permissions[20]);
					guildObject.put("CommandLevels_Set", permissions[21]);
					guildObject.put("CommandLevels_SetPermissions", permissions[22]);
					guildObject.put("CommandLevels_SetChannelCensor", permissions[23]);
					guildObject.put("CommandLevels_SetWarnings", permissions[24]);
					guildObject.put("CommandLevels_SetRanking", permissions[25]);
					guildObject.put("CommandLevels_SetMaxExperience", permissions[26]);
					guildObject.put("CommandLevels_SetDefaultLevelSkin", permissions[27]);
					guildObject.put("CommandLevels_SetDefaultRankSkin", permissions[28]);
					guildObject.put("CommandLevels_SetDefaultProfileSkin", permissions[29]);
					guildObject.put("CommandLevels_SetDefaultIconSkin", permissions[30]);
					guildObject.put("CommandLevels_SetDailyItem", permissions[31]);
					guildObject.put("CommandLevels_SetGiveawayItems", permissions[32]);
					guildObject.put("CommandLevels_SetCompServer", permissions[33]);
					guildObject.put("CommandLevels_SetMaxClanMembers", permissions[34]);
					guildObject.put("CommandLevels_SetMaps", permissions[35]);
					guildObject.put("CommandLevels_SetMatchmakingMembers", permissions[36]);
					guildObject.put("CommandLevels_SetLanguage", permissions[37]);
					guildObject.put("CommandLevels_User", permissions[38]);
					guildObject.put("CommandLevels_UserInformation", permissions[39]);
					guildObject.put("CommandLevels_UserDeleteMessages", permissions[40]);
					guildObject.put("CommandLevels_UserWarning", permissions[41]);
					guildObject.put("CommandLevels_UserWarningForce", permissions[42]);
					guildObject.put("CommandLevels_UserMute", permissions[43]);
					guildObject.put("CommandLevels_UserUnmute", permissions[44]);
					guildObject.put("CommandLevels_UserBan", permissions[45]);
					guildObject.put("CommandLevels_UserUnban", permissions[46]);
					guildObject.put("CommandLevels_UserKick", permissions[47]);
					guildObject.put("CommandLevels_UserAssignRole", permissions[48]);
					guildObject.put("CommandLevels_UserRemoveRole", permissions[49]);
					guildObject.put("CommandLevels_UserHistory", permissions[50]);
					guildObject.put("CommandLevels_UserWatch", permissions[51]);
					guildObject.put("CommandLevels_UserUnwatch", permissions[52]);
					guildObject.put("CommandLevels_UserUseWatchChannel", permissions[53]);
					guildObject.put("CommandLevels_UserGiftExperience", permissions[54]);
					guildObject.put("CommandLevels_UserSetExperience", permissions[55]);
					guildObject.put("CommandLevels_UserSetLevel", permissions[56]);
					guildObject.put("CommandLevels_UserGiftCurrency", permissions[57]);
					guildObject.put("CommandLevels_UserSetCurrency", permissions[58]);
					guildObject.put("CommandLevels_Filter", permissions[59]);
					guildObject.put("CommandLevels_FilterWordFilter", permissions[60]);
					guildObject.put("CommandLevels_FilterNameFilter", permissions[61]);
					guildObject.put("CommandLevels_FilterNameKick", permissions[62]);
					guildObject.put("CommandLevels_FilterFunnyNames", permissions[63]);
					guildObject.put("CommandLevels_FilterStaffNames", permissions[64]);
					guildObject.put("CommandLevels_FilterProhibitedUrls", permissions[65]);
					guildObject.put("CommandLevels_FilterAllowedUrls", permissions[66]);
					guildObject.put("CommandLevels_FilterProhibitedSubs", permissions[67]);
					guildObject.put("CommandLevels_RoleReaction", permissions[68]);
					guildObject.put("CommandLevels_Remove", permissions[69]);
					guildObject.put("CommandLevels_HeavyCensoring", permissions[70]);
					guildObject.put("CommandLevels_Mute", permissions[71]);
					guildObject.put("CommandLevels_Google", permissions[72]);
					guildObject.put("CommandLevels_Subscribe", permissions[73]);
					guildObject.put("CommandLevels_SubscribeRss", permissions[74]);
					guildObject.put("CommandLevels_SubscribeTwitter", permissions[75]);
					guildObject.put("CommandLevels_SubscribeReddit", permissions[76]);
					guildObject.put("CommandLevels_SubscribeYouTube", permissions[77]);
					guildObject.put("CommandLevels_SubscribeTwitch", permissions[78]);
					guildObject.put("CommandLevels_Write", permissions[79]);
					guildObject.put("CommandLevels_Edit", permissions[80]);
					guildObject.put("CommandLevels_Accept", permissions[81]);
					guildObject.put("CommandLevels_Deny", permissions[82]);
					guildObject.put("CommandLevels_Language", permissions[83]);
					guildObject.put("CommandLevels_Schedule", permissions[84]);
					guildObject.put("CommandLevels_Prune", permissions[85]);
					guildObject.put("CommandLevels_Warn", permissions[86]);
					guildObject.put("CommandLevels_Invites", permissions[87]);
					guildObject.put("CommandLevels_About", permissions[88]);
					guildObject.put("CommandLevels_Daily", permissions[89]);
					guildObject.put("CommandLevels_Inventory", permissions[90]);
					guildObject.put("CommandLevels_Meow", permissions[91]);
					guildObject.put("CommandLevels_Pug", permissions[92]);
					guildObject.put("CommandLevels_Profile", permissions[93]);
					guildObject.put("CommandLevels_Rank", permissions[94]);
					guildObject.put("CommandLevels_Shop", permissions[95]);
					guildObject.put("CommandLevels_Top", permissions[96]);
					guildObject.put("CommandLevels_Use", permissions[97]);
					guildObject.put("CommandLevels_Quiz", permissions[98]);
					guildObject.put("CommandLevels_Randomshop", permissions[99]);
					guildObject.put("CommandLevels_Patchnotes", permissions[100]);
					guildObject.put("CommandLevels_DoubleExperience", permissions[101]);
					guildObject.put("CommandLevels_Equip", permissions[102]);
					guildObject.put("CommandLevels_Matchmaking", permissions[103]);
					guildObject.put("CommandLevels_Clan", permissions[104]);
					guildObject.put("CommandLevels_Cw", permissions[105]);
					guildObject.put("CommandLevels_Room", permissions[106]);
					guildObject.put("CommandLevels_RoomClose", permissions[107]);
					guildObject.put("CommandLevels_RoomWinner", permissions[108]);
					guildObject.put("CommandLevels_RoomReopen", permissions[109]);
					guildObject.put("CommandLevels_Stats", permissions[110]);
					guildObject.put("CommandLevels_Leaderboard", permissions[111]);
					guildObject.put("CommandLevels_Join", permissions[112]);
					guildObject.put("CommandLevels_Leave", permissions[113]);
					
					jsonArray.put(guildObject);
				}
				return new JSONObject().put("guilds", jsonArray);
			}
		}
		else if(uris.length == 3) {
			if(uris[1].matches("[0-9]{18}") && uris[2].matches("users")) {
				boolean staffFilter = false;
				if(queryParams!= null && queryParams.length > 0) {
					for(int  i = 0; i < queryParams.length; i++) {
						final String [] subQueryParam = queryParams[i].split("=");
						if(subQueryParam.length == 2) {
							if(subQueryParam[0].equals("type") && subQueryParam[1].equals("staff")) {
								staffFilter = true;
							}
						}
					}
				}
				
				final Guild guild = e.getJDA().getGuildById(uris[1]);
				if(guild != null) {
					List<Member> members = guild.loadMembers().get();
					JSONObject json = new JSONObject();
					json.put("guild_id", guild.getId());
					json.put("name", guild.getName());
					json.put("memberCount", members.size());
					JSONArray serverMembers = new JSONArray();
					json.put("members", serverMembers);
					final ArrayList<Long> botAdmin = BotConfiguration.SQLgetAdministrators(guild.getIdLong());
					for(final Member member : members) {
						final boolean admin = UserPrivs.isUserAdmin(member);
						final boolean mod = UserPrivs.isUserMod(member);
						if(staffFilter && !admin && !mod && botAdmin.contains(member.getUser().getIdLong()))
							continue;
						
						JSONObject serverMember = new JSONObject();
						serverMember.put("user_id", member.getUser().getId());
						serverMember.put("name", member.getUser().getName());
						serverMember.put("discriminator", member.getUser().getDiscriminator());
						serverMember.put("nickname", member.getEffectiveName());
						serverMember.put("avatar_url", member.getUser().getEffectiveAvatarUrl());
						serverMember.put("isUserAdmin", admin);
						serverMember.put("isUserMod", mod);
						serverMember.put("isUserCommunity", UserPrivs.isUserCommunity(member));
						serverMember.put("isUserBotAdmin", botAdmin.contains(member.getUser().getIdLong()));
						JSONArray serverRoles = new JSONArray();
						serverMember.put("roles", serverRoles);
						final var roles = DiscordRoles.SQLgetRoles(guild.getIdLong());
						for(final Role role : member.getRoles()) {
							JSONObject curRole = new JSONObject();
							curRole.put("role_id", role.getId());
							curRole.put("name", role.getName());
							curRole.put("position", role.getPosition());
							if(role.getColor() != null)
								curRole.put("color", "#"+Integer.toHexString(role.getColorRaw()));
							else
								curRole.put("color", "#99AAB5");
							final var registeredRole = roles.parallelStream().filter(f -> f.getRole_ID() == role.getIdLong()).findAny().orElse(null);
							curRole.put("type", (registeredRole != null ? registeredRole.getCategory_Name() : (String)null));
							curRole.put("permission", (registeredRole != null ? registeredRole.getLevel() : 0));
							serverRoles.put(curRole);
						}
						serverMembers.put(serverMember);
					}
					return json;
				}
				else {
					WebserviceUtils.return404(out, "Guild not found!", true);
					return new JSONObject().append("done", true);
				}
			}
		}
		return null;
	}
}
