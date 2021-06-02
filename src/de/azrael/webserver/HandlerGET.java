package de.azrael.webserver;

import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import de.azrael.core.UserPrivs;
import de.azrael.fileManagement.FileSetting;
import de.azrael.fileManagement.GuildIni;
import de.azrael.sql.DiscordRoles;
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
						server.put("isUserBotAdmin", (GuildIni.getAdmin(guild.getIdLong()) == user.getIdLong()));
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
							user.put("isUserBotAdmin", (GuildIni.getAdmin(guild.getIdLong()) == member.getUser().getIdLong()));
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
					
					final var ini = GuildIni.readIni(guild.getIdLong());
					guildObject.put("General_Administrator", ini.get("General", "Administrator", long.class));
					guildObject.put("General_CommandPrefix", ini.get("General", "CommandPrefix"));
					guildObject.put("General_JoinMessage", ini.get("General", "JoinMessage", boolean.class));
					guildObject.put("General_LeaveMessage", ini.get("General", "LeaveMessage", boolean.class));
					guildObject.put("General_CacheLog", ini.get("General", "CacheLog"));
					guildObject.put("General_URLBlacklist", ini.get("General", "URLBlacklist"));
					guildObject.put("General_DoubleExperience", ini.get("General", "DoubleExperience"));
					guildObject.put("General_ForceReason", ini.get("General", "ForceReason", boolean.class));
					guildObject.put("General_OverrideBan", ini.get("General", "OverrideBan", boolean.class));
					guildObject.put("General_SelfDeletedMessage", ini.get("General", "SelfDeletedMessage", boolean.class));
					guildObject.put("General_EditedMessage", ini.get("General", "EditedMessage", boolean.class));
					guildObject.put("General_EditedMessageHistory", ini.get("General", "EditedMessageHistory", boolean.class));
					guildObject.put("General_Notifications", ini.get("General", "Notifications", boolean.class));
					guildObject.put("General_NewAccountOnJoin", ini.get("General", "NewAccountOnJoin", boolean.class));
					guildObject.put("General_ReassignRolesAfterMute", ini.get("General", "ReassignRolesAfterMute", boolean.class));
					guildObject.put("General_CollectRankingRoles", ini.get("General", "CollectRankingRoles", boolean.class));
					guildObject.put("General_MessagesExpRateLimit", ini.get("General", "MessagesExpRateLimit", int.class));
					guildObject.put("General_IgnoreMissingPermissions", ini.get("General", "IgnoreMissingPermissions", boolean.class));
					
					guildObject.put("Google_FunctionalitiesEnabled", ini.get("Google", "FunctionalitiesEnabled", boolean.class));
					guildObject.put("Google_MainEmail", ini.get("Google", "MainEmail"));
					guildObject.put("Google_SpreadsheetsEnabled", ini.get("Google", "SpreadsheetsEnabled", boolean.class));
					
					guildObject.put("Messages_SpamDetection", ini.get("Messages", "SpamDetection", boolean.class));
					guildObject.put("Messages_MessagesLimit", ini.get("Messages", "MessagesLimit", int.class));
					guildObject.put("Messages_MessagesOverChannelsLimit", ini.get("Messages", "MessagesOverChannelsLimit", int.class));
					guildObject.put("Messages_Expires", ini.get("Messages", "Expires", long.class));
					
					guildObject.put("Patch_PrivatePatchNotes", ini.get("Patch", "PrivatePatchNotes", boolean.class));
					guildObject.put("Patch_PublicPatchNotes", ini.get("Patch", "PublicPatchNotes", boolean.class));
					
					guildObject.put("Mute_MessageDeleteEnabled", ini.get("Mute", "MessageDeleteEnabled", boolean.class));
					guildObject.put("Mute_ForceMessageDeletion", ini.get("Mute", "ForceMessageDeletion", boolean.class));
					guildObject.put("Mute_AutoDeleteMessages", ini.get("Mute", "AutoDeleteMessages", int.class));
					guildObject.put("Mute_SendReason", ini.get("Mute", "SendReason", boolean.class));
					
					guildObject.put("Kick_MessageDeleteEnabled", ini.get("Kick", "MessageDeleteEnabled", boolean.class));
					guildObject.put("Kick_ForceMessageDeletion", ini.get("Kick", "ForceMessageDeletion", boolean.class));
					guildObject.put("Kick_AutoDeleteMessages", ini.get("Kick", "AutoDeleteMessages", int.class));
					guildObject.put("Kick_SendReason", ini.get("Kick", "SendReason", boolean.class));
					
					guildObject.put("Ban_MessageDeleteEnabled", ini.get("Ban", "MessageDeleteEnabled", boolean.class));
					guildObject.put("Ban_ForceMessageDeletion", ini.get("Ban", "ForceMessageDeletion", boolean.class));
					guildObject.put("Ban_AutoDeleteMessages", ini.get("Ban", "AutoDeleteMessages", int.class));
					guildObject.put("Ban_SendReason", ini.get("Ban", "SendReason", boolean.class));
					
					guildObject.put("Competitive_Team1", ini.get("Competitive", "Team1"));
					guildObject.put("Competitive_Team2", ini.get("Competitive", "Team2"));
					
					guildObject.put("Reactions_Enabled", ini.get("Reactions", "Enabled", boolean.class));
					guildObject.put("Reactions_Emoji1", ini.get("Reactions", "Emoji1"));
					guildObject.put("Reactions_Emoji2", ini.get("Reactions", "Emoji2"));
					guildObject.put("Reactions_Emoji3", ini.get("Reactions", "Emoji3"));
					guildObject.put("Reactions_Emoji4", ini.get("Reactions", "Emoji4"));
					guildObject.put("Reactions_Emoji5", ini.get("Reactions", "Emoji5"));
					guildObject.put("Reactions_Emoji6", ini.get("Reactions", "Emoji6"));
					guildObject.put("Reactions_Emoji7", ini.get("Reactions", "Emoji7"));
					guildObject.put("Reactions_Emoji8", ini.get("Reactions", "Emoji8"));
					guildObject.put("Reactions_Emoji9", ini.get("Reactions", "Emoji9"));
					guildObject.put("Reactions_VoteThumbsUp", ini.get("Reactions", "VoteThumbsUp"));
					guildObject.put("Reactions_VoteThumbsDown", ini.get("Reactions", "VoteThumbsDown"));
					guildObject.put("Reactions_VoteShrug", ini.get("Reactions", "VoteShrug"));
					
					guildObject.put("CustomMessages_reactionmessage", FileSetting.readFile("./files/Guilds/"+guild.getId()+"/reactionmessage.txt"));
					guildObject.put("CustomMessages_verificationmessage", FileSetting.readFile("./files/Guilds/"+guild.getId()+"/verificationmessage.txt"));
					guildObject.put("CustomMessages_assignmessage", FileSetting.readFile("./files/Guilds/"+guild.getId()+"/assignmessage.txt"));
					
					guildObject.put("Commands_About", ini.get("Commands", "About", boolean.class));
					guildObject.put("Commands_Daily", ini.get("Commands", "Daily", boolean.class));
					guildObject.put("Commands_Display", ini.get("Commands", "Display", boolean.class));
					guildObject.put("Commands_Help", ini.get("Commands", "Help", boolean.class));
					guildObject.put("Commands_Inventory", ini.get("Commands", "Inventory", boolean.class));
					guildObject.put("Commands_Meow", ini.get("Commands", "Meow", boolean.class));
					guildObject.put("Commands_Pug", ini.get("Commands", "Pug", boolean.class));
					guildObject.put("Commands_Profile", ini.get("Commands", "Profile", boolean.class));
					guildObject.put("Commands_Rank", ini.get("Commands", "Rank", boolean.class));
					guildObject.put("Commands_Register", ini.get("Commands", "Register", boolean.class));
					guildObject.put("Commands_Set", ini.get("Commands", "Set", boolean.class));
					guildObject.put("Commands_Shop", ini.get("Commands", "Shop", boolean.class));
					guildObject.put("Commands_Top", ini.get("Commands", "Top", boolean.class));
					guildObject.put("Commands_Use", ini.get("Commands", "Use", boolean.class));
					guildObject.put("Commands_User", ini.get("Commands", "User", boolean.class));
					guildObject.put("Commands_Filter", ini.get("Commands", "Filter", boolean.class));
					guildObject.put("Commands_Quiz", ini.get("Commands", "Quiz", boolean.class));
					guildObject.put("Commands_RoleReaction", ini.get("Commands", "RoleReaction", boolean.class));
					guildObject.put("Commands_Subscribe", ini.get("Commands", "Subscribe", boolean.class));
					guildObject.put("Commands_Randomshop", ini.get("Commands", "Randomshop", boolean.class));
					guildObject.put("Commands_Patchnotes", ini.get("Commands", "Patchnotes", boolean.class));
					guildObject.put("Commands_DoubleExperience", ini.get("Commands", "DoubleExperience", boolean.class));
					guildObject.put("Commands_Equip", ini.get("Commands", "Equip", boolean.class));
					guildObject.put("Commands_Remove", ini.get("Commands", "Remove", boolean.class));
					guildObject.put("Commands_HeavyCensoring", ini.get("Commands", "HeavyCensoring", boolean.class));
					guildObject.put("Commands_Mute", ini.get("Commands", "Mute", boolean.class));
					guildObject.put("Commands_Google", ini.get("Commands", "Google", boolean.class));
					guildObject.put("Commands_Write", ini.get("Commands", "Write", boolean.class));
					guildObject.put("Commands_Edit", ini.get("Commands", "Edit", boolean.class));
					guildObject.put("Commands_Matchmaking", ini.get("Commands", "Matchmaking", boolean.class));
					guildObject.put("Commands_Join", ini.get("Commands", "Join", boolean.class));
					guildObject.put("Commands_Clan", ini.get("Commands", "Clan", boolean.class));
					guildObject.put("Commands_Leave", ini.get("Commands", "Leave", boolean.class));
					guildObject.put("Commands_Queue", ini.get("Commands", "Queue", boolean.class));
					guildObject.put("Commands_Cw", ini.get("Commands", "Cw", boolean.class));
					guildObject.put("Commands_Room", ini.get("Commands", "Room", boolean.class));
					guildObject.put("Commands_Stats", ini.get("Commands", "Stats", boolean.class));
					guildObject.put("Commands_Leaderboard", ini.get("Commands", "Leaderboard", boolean.class));
					guildObject.put("Commands_Accept", ini.get("Commands", "Accept", boolean.class));
					guildObject.put("Commands_Deny", ini.get("Commands", "Deny", boolean.class));
					guildObject.put("Commands_Language", ini.get("Commands", "Language", boolean.class));
					guildObject.put("Commands_Schedule", ini.get("Commands", "Schedule", boolean.class));
					guildObject.put("Commands_Prune", ini.get("Commands", "Prune", boolean.class));
					guildObject.put("Commands_Warn", ini.get("Commands", "Warn", boolean.class));
					guildObject.put("Commands_Reddit", ini.get("Commands", "Reddit", boolean.class));
					guildObject.put("Commands_Invites", ini.get("Commands", "Invites", boolean.class));
					
					guildObject.put("CommandLevels_About", ini.get("CommandLevels", "About", int.class));
					guildObject.put("CommandLevels_Daily", ini.get("CommandLevels", "Daily", int.class));
					guildObject.put("CommandLevels_Display", ini.get("CommandLevels", "Display", int.class));
					guildObject.put("CommandLevels_DisplayRoles", ini.get("CommandLevels", "DisplayRoles", int.class));
					guildObject.put("CommandLevels_DisplayRegisteredRoles", ini.get("CommandLevels", "DisplayRegisteredRoles", int.class));
					guildObject.put("CommandLevels_DisplayRankingRoles", ini.get("CommandLevels", "DisplayRankingRoles", int.class));
					guildObject.put("CommandLevels_DisplayCategories", ini.get("CommandLevels", "DisplayCategories", int.class));
					guildObject.put("CommandLevels_DisplayRegisteredCategories", ini.get("CommandLevels", "DisplayRegisteredCategories", int.class));
					guildObject.put("CommandLevels_DisplayTextChannels", ini.get("CommandLevels", "DisplayTextChannels", int.class));
					guildObject.put("CommandLevels_DisplayVoiceChannels", ini.get("CommandLevels", "DisplayVoiceChannels", int.class));
					guildObject.put("CommandLevels_DisplayRegisteredChannels", ini.get("CommandLevels", "DisplayRegisteredChannels", int.class));
					guildObject.put("CommandLevels_DisplayDailies", ini.get("CommandLevels", "DisplayDailies", int.class));
					guildObject.put("CommandLevels_DisplayWatchedUsers", ini.get("CommandLevels", "DisplayWatchedUsers", int.class));
					guildObject.put("CommandLevels_DisplayCommandLevels", ini.get("CommandLevels", "DisplayCommandLevels", int.class));
					guildObject.put("CommandLevels_Help", ini.get("CommandLevels", "Help", int.class));
					guildObject.put("CommandLevels_Inventory", ini.get("CommandLevels", "Inventory", int.class));
					guildObject.put("CommandLevels_Meow", ini.get("CommandLevels", "Meow", int.class));
					guildObject.put("CommandLevels_Pug", ini.get("CommandLevels", "Pug", int.class));
					guildObject.put("CommandLevels_Profile", ini.get("CommandLevels", "Profile", int.class));
					guildObject.put("CommandLevels_Rank", ini.get("CommandLevels", "Rank", int.class));
					guildObject.put("CommandLevels_Register", ini.get("CommandLevels", "Register", int.class));
					guildObject.put("CommandLevels_RegisterRole", ini.get("CommandLevels", "RegisterRole", int.class));
					guildObject.put("CommandLevels_RegisterCategory", ini.get("CommandLevels", "RegisterCategory", int.class));
					guildObject.put("CommandLevels_RegisterTextChannel", ini.get("CommandLevels", "RegisterTextChannel", int.class));
					guildObject.put("CommandLevels_RegisterTextChannelURL", ini.get("CommandLevels", "RegisterTextChannelURL", int.class));
					guildObject.put("CommandLevels_RegisterTextChannelTXT", ini.get("CommandLevels", "RegisterTextChannelTXT", int.class));
					guildObject.put("CommandLevels_RegisterRankingRole", ini.get("CommandLevels", "RegisterRankingRole", int.class));
					guildObject.put("CommandLevels_RegisterTextChannels", ini.get("CommandLevels", "RegisterTextChannels", int.class));
					guildObject.put("CommandLevels_RegisterUsers", ini.get("CommandLevels", "RegisterUsers", int.class));
					guildObject.put("CommandLevels_Set", ini.get("CommandLevels", "Set", int.class));
					guildObject.put("CommandLevels_SetPrivilege", ini.get("CommandLevels", "SetPrivilege", int.class));
					guildObject.put("CommandLevels_SetChannelFilter", ini.get("CommandLevels", "SetChannelFilter", int.class));
					guildObject.put("CommandLevels_SetWarnings", ini.get("CommandLevels", "SetWarnings", int.class));
					guildObject.put("CommandLevels_SetRanking", ini.get("CommandLevels", "SetRanking", int.class));
					guildObject.put("CommandLevels_SetMaxExperience", ini.get("CommandLevels", "SetMaxExperience", int.class));
					guildObject.put("CommandLevels_SetDefaultLevelSkin", ini.get("CommandLevels", "SetDefaultLevelSkin", int.class));
					guildObject.put("CommandLevels_SetDefaultRankSkin", ini.get("CommandLevels", "SetDefaultRankSkin", int.class));
					guildObject.put("CommandLevels_SetDefaultProfileSkin", ini.get("CommandLevels", "SetDefaultProfileSkin", int.class));
					guildObject.put("CommandLevels_SetDefaultIconSkin", ini.get("CommandLevels", "SetDefaultIconSkin", int.class));
					guildObject.put("CommandLevels_SetDailyItem", ini.get("CommandLevels", "SetDailyItem", int.class));
					guildObject.put("CommandLevels_SetGiveawayItems", ini.get("CommandLevels", "SetGiveawayItems", int.class));
					guildObject.put("CommandLevels_SetCompServer", ini.get("CommandLevels", "SetCompServer", int.class));
					guildObject.put("CommandLevels_SetMaxClanMembers", ini.get("CommandLevels", "SetMaxClanMembers", int.class));
					guildObject.put("CommandLevels_SetMatchmakingMembers", ini.get("CommandLevels", "SetMatchmakingMembers", int.class));
					guildObject.put("CommandLevels_SetMaps", ini.get("CommandLevels", "SetMaps", int.class));
					guildObject.put("CommandLevels_SetLanguage", ini.get("CommandLevels", "SetLanguage", int.class));
					guildObject.put("CommandLevels_Shop", ini.get("CommandLevels", "Shop", int.class));
					guildObject.put("CommandLevels_Top", ini.get("CommandLevels", "Top", int.class));
					guildObject.put("CommandLevels_Use", ini.get("CommandLevels", "Use", int.class));
					guildObject.put("CommandLevels_User", ini.get("CommandLevels", "User", int.class));
					guildObject.put("CommandLevels_UserInformation", ini.get("CommandLevels", "UserInformation", int.class));
					guildObject.put("CommandLevels_UserDeleteMessages", ini.get("CommandLevels", "UserDeleteMessages", int.class));
					guildObject.put("CommandLevels_UserWarning", ini.get("CommandLevels", "UserWarning", int.class));
					guildObject.put("CommandLevels_UserWarningForce", ini.get("CommandLevels", "UserWarningForce", int.class));
					guildObject.put("CommandLevels_UserMute", ini.get("CommandLevels", "UserMute", int.class));
					guildObject.put("CommandLevels_UserUnmute", ini.get("CommandLevels", "UserUnmute", int.class));
					guildObject.put("CommandLevels_UserBan", ini.get("CommandLevels", "UserBan", int.class));
					guildObject.put("CommandLevels_UserUnban", ini.get("CommandLevels", "UserUnban", int.class));
					guildObject.put("CommandLevels_UserKick", ini.get("CommandLevels", "UserKick", int.class));
					guildObject.put("CommandLevels_UserAssignRole", ini.get("CommandLevels", "UserAssignRole", int.class));
					guildObject.put("CommandLevels_UserRemoveRole", ini.get("CommandLevels", "UserRemoveRole", int.class));
					guildObject.put("CommandLevels_UserHistory", ini.get("CommandLevels", "UserHistory", int.class));
					guildObject.put("CommandLevels_UserWatch", ini.get("CommandLevels", "UserWatch", int.class));
					guildObject.put("CommandLevels_UserUnwatch", ini.get("CommandLevels", "UserUnwatch", int.class));
					guildObject.put("CommandLevels_UserUseWatchChannel", ini.get("CommandLevels", "UserUseWatchChannel", int.class));
					guildObject.put("CommandLevels_UserGiftExperience", ini.get("CommandLevels", "UserGiftExperience", int.class));
					guildObject.put("CommandLevels_UserSetExperience", ini.get("CommandLevels", "UserSetExperience", int.class));
					guildObject.put("CommandLevels_UserSetLevel", ini.get("CommandLevels", "UserSetLevel", int.class));
					guildObject.put("CommandLevels_UserGiftCurrency", ini.get("CommandLevels", "UserGiftCurrency", int.class));
					guildObject.put("CommandLevels_UserSetCurrency", ini.get("CommandLevels", "UserSetCurrency", int.class));
					guildObject.put("CommandLevels_Filter", ini.get("CommandLevels", "Filter", int.class));
					guildObject.put("CommandLevels_FilterWordFilter", ini.get("CommandLevels", "FilterWordFilter", int.class));
					guildObject.put("CommandLevels_FilterNameFilter", ini.get("CommandLevels", "FilterNameFilter", int.class));
					guildObject.put("CommandLevels_FilterNameKick", ini.get("CommandLevels", "FilterNameKick", int.class));
					guildObject.put("CommandLevels_FilterFunnyNames", ini.get("CommandLevels", "FilterFunnyNames", int.class));
					guildObject.put("CommandLevels_FilterStaffNames", ini.get("CommandLevels", "FilterStaffNames", int.class));
					guildObject.put("CommandLevels_FilterURLBlacklist", ini.get("CommandLevels", "FilterURLBlacklist", int.class));
					guildObject.put("CommandLevels_FilterURLWhitelist", ini.get("CommandLevels", "FilterURLWhitelist", int.class));
					guildObject.put("CommandLevels_FilterTweetBlacklist", ini.get("CommandLevels", "FilterTweetBlacklist", int.class));
					guildObject.put("CommandLevels_Quiz", ini.get("CommandLevels", "Quiz", int.class));
					guildObject.put("CommandLevels_RoleReaction", ini.get("CommandLevels", "RoleReaction", int.class));
					guildObject.put("CommandLevels_Subscribe", ini.get("CommandLevels", "Subscribe", int.class));
					guildObject.put("CommandLevels_Randomshop", ini.get("CommandLevels", "Randomshop", int.class));
					guildObject.put("CommandLevels_Patchnotes", ini.get("CommandLevels", "Patchnotes", int.class));
					guildObject.put("CommandLevels_DoubleExperience", ini.get("CommandLevels", "DoubleExperience", int.class));
					guildObject.put("CommandLevels_Equip", ini.get("CommandLevels", "Equip", int.class));
					guildObject.put("CommandLevels_Remove", ini.get("CommandLevels", "Remove", int.class));
					guildObject.put("CommandLevels_HeavyCensoring", ini.get("CommandLevels", "HeavyCensoring", int.class));
					guildObject.put("CommandLevels_Mute", ini.get("CommandLevels", "Mute", int.class));
					guildObject.put("CommandLevels_Google", ini.get("CommandLevels", "Google", int.class));
					guildObject.put("CommandLevels_Write", ini.get("CommandLevels", "Write", int.class));
					guildObject.put("CommandLevels_Edit", ini.get("CommandLevels", "Edit", int.class));
					guildObject.put("CommandLevels_Matchmaking", ini.get("CommandLevels", "Matchmaking", int.class));
					guildObject.put("CommandLevels_Join", ini.get("CommandLevels", "Join", int.class));
					guildObject.put("CommandLevels_Clan", ini.get("CommandLevels", "Clan", int.class));
					guildObject.put("CommandLevels_Leave", ini.get("CommandLevels", "Leave", int.class));
					guildObject.put("CommandLevels_Queue", ini.get("CommandLevels", "Queue", int.class));
					guildObject.put("CommandLevels_Cw", ini.get("CommandLevels", "Cw", int.class));
					guildObject.put("CommandLevels_Room", ini.get("CommandLevels", "Room", int.class));
					guildObject.put("CommandLevels_RoomClose", ini.get("CommandLevels", "RoomClose", int.class));
					guildObject.put("CommandLevels_RoomWinner", ini.get("CommandLevels", "RoomWinner", int.class));
					guildObject.put("CommandLevels_RoomReopen", ini.get("CommandLevels", "RoomReopen", int.class));
					guildObject.put("CommandLevels_Stats", ini.get("CommandLevels", "Stats", int.class));
					guildObject.put("CommandLevels_Leaderboard", ini.get("CommandLevels", "Leaderboard", int.class));
					guildObject.put("CommandLevels_Accept", ini.get("CommandLevels", "Accept", int.class));
					guildObject.put("CommandLevels_Deny", ini.get("CommandLevels", "Deny", int.class));
					guildObject.put("CommandLevels_Language", ini.get("CommandLevels", "Language", int.class));
					guildObject.put("CommandLevels_Schedule", ini.get("CommandLevels", "Schedule", int.class));
					guildObject.put("CommandLevels_Prune", ini.get("CommandLevels", "Prune", int.class));
					guildObject.put("CommandLevels_Warn", ini.get("CommandLevels", "Warn", int.class));
					guildObject.put("CommandLevels_Reddit", ini.get("CommandLevels", "Reddit", int.class));
					guildObject.put("CommandLevels_Invites", ini.get("CommandLevels", "Invites", int.class));
					
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
					final long botAdmin = GuildIni.getAdmin(guild.getIdLong());
					for(final Member member : members) {
						final boolean admin = UserPrivs.isUserAdmin(member);
						final boolean mod = UserPrivs.isUserMod(member);
						if(staffFilter && !admin && !mod && member.getUser().getIdLong() != botAdmin)
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
						serverMember.put("isUserBotAdmin", (botAdmin == member.getUser().getIdLong()));
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
