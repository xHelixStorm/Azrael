package preparedMessages;

import enums.Translation;
import fileManagement.GuildIni;
import net.dv8tion.jda.api.entities.Member;
import util.STATIC;

public class CommandList {
	
	public static String getHelp(Member member, boolean permissionGranted, int type) {
		long guild_id = member.getGuild().getIdLong();
		StringBuilder textCollector = new StringBuilder();
		boolean administration = false;
		boolean entertainment = false;
		boolean other = false;
		
		if(permissionGranted && (GuildIni.getRegisterCommand(guild_id) || GuildIni.getSetCommand(guild_id) || GuildIni.getUserCommand(guild_id) || GuildIni.getFilterCommand(guild_id) || GuildIni.getRoleReactionCommand(guild_id) || GuildIni.getSubscribeCommand(guild_id) || GuildIni.getRemoveCommand(guild_id) || GuildIni.getHeavyCensoringCommand(guild_id) || GuildIni.getMuteCommand(guild_id) || GuildIni.getGoogleCommand(guild_id) || GuildIni.getWriteCommand(guild_id) || GuildIni.getEditCommand(guild_id))) {
			administration = true;
		}
		
		if(GuildIni.getEquipCommand(guild_id) || GuildIni.getPugCommand(guild_id) || GuildIni.getMeowCommand(guild_id) || GuildIni.getRankCommand(guild_id) || GuildIni.getProfileCommand(guild_id) || GuildIni.getTopCommand(guild_id) || GuildIni.getUseCommand(guild_id) || GuildIni.getShopCommand(guild_id) || GuildIni.getInventoryCommand(guild_id) || GuildIni.getDailyCommand(guild_id) || GuildIni.getQuizCommand(guild_id) || GuildIni.getRandomshopCommand(guild_id) || GuildIni.getEquipCommand(guild_id) || GuildIni.getMatchmakingCommand(guild_id) || GuildIni.getJoinCommand(guild_id) || GuildIni.getLeaveCommand(guild_id) || GuildIni.getClanCommand(guild_id) || GuildIni.getQueueCommand(guild_id) || GuildIni.getCwCommand(guild_id) || GuildIni.getRoomCommand(guild_id) || GuildIni.getStatsCommand(guild_id) || GuildIni.getLeaderboardCommand(guild_id)) {
			entertainment = true;
		}
		
		if(GuildIni.getAboutCommand(guild_id) || GuildIni.getHelpCommand(guild_id) || GuildIni.getDisplayCommand(guild_id) || GuildIni.getPatchnotesCommand(guild_id)) {
			other = true;
		}
		
		final String prefix = GuildIni.getCommandPrefix(guild_id);
		if(administration == true && type == 1) {
			if(GuildIni.getRegisterCommand(guild_id))			textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_REGISTER).replace("{}", prefix));
			if(GuildIni.getSetCommand(guild_id))				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_SET).replace("{}", prefix));
			if(GuildIni.getRemoveCommand(guild_id)) 			textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_REMOVE).replace("{}", prefix));
			if(GuildIni.getUserCommand(guild_id))				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_USER).replace("{}", prefix));
			if(GuildIni.getFilterCommand(guild_id))				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_FILTER).replace("{}", prefix));
			if(GuildIni.getRoleReactionCommand(guild_id))		textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_ROLE_REACTION).replace("{}", prefix));
			if(GuildIni.getSubscribeCommand(guild_id))			textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_SUBSCRIBE).replace("{}", prefix));
			if(GuildIni.getDoubleExperienceCommand(guild_id))	textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_DOUBLE_EXPERIENCE).replace("{}", prefix));
			if(GuildIni.getHeavyCensoringCommand(guild_id))		textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_HEAVY_CENSORING).replace("{}", prefix));
			if(GuildIni.getMuteCommand(guild_id))				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_MUTE).replace("{}", prefix));
			if(GuildIni.getGoogleCommand(guild_id))				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_GOOGLE).replace("{}", prefix));
			if(GuildIni.getWriteCommand(guild_id))				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_WRITE).replace("{}", prefix));
			if(GuildIni.getEditCommand(guild_id))				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_EDIT).replace("{}", prefix));
		}
		if(entertainment == true && type == 2) {
			if(GuildIni.getPugCommand(guild_id))				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_PUG).replace("{}", prefix));
			if(GuildIni.getMeowCommand(guild_id))				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_MEOW).replace("{}", prefix));
			if(GuildIni.getRankCommand(guild_id))				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_RANK).replace("{}", prefix));
			if(GuildIni.getProfileCommand(guild_id))			textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_PROFILE).replace("{}", prefix));
			if(GuildIni.getTopCommand(guild_id))				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_TOP).replace("{}", prefix));
			if(GuildIni.getUseCommand(guild_id))				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_USE).replace("{}", prefix));
			if(GuildIni.getShopCommand(guild_id))				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_SHOP).replace("{}", prefix));
			if(GuildIni.getInventoryCommand(guild_id))			textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_INVENTORY).replace("{}", prefix));
			if(GuildIni.getDailyCommand(guild_id))				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_DAILY).replace("{}", prefix));
			if(GuildIni.getQuizCommand(guild_id))				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_QUIZ).replace("{}", prefix));
			if(GuildIni.getRandomshopCommand(guild_id))			textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_RANDOMSHOP).replace("{}", prefix));
			if(GuildIni.getEquipCommand(guild_id))				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_EQUIP).replace("{}", prefix));
			if(GuildIni.getMatchmakingCommand(guild_id))		textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_MATCHMAKING).replace("{}", prefix));
			if(GuildIni.getJoinCommand(guild_id))				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_JOIN).replace("{}", prefix));
			if(GuildIni.getLeaveCommand(guild_id))				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_LEAVE).replace("{}", prefix));
			if(GuildIni.getClanCommand(guild_id))				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_CLAN).replace("{}", prefix));
			if(GuildIni.getQueueCommand(guild_id))				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_QUEUE).replace("{}", prefix));
			if(GuildIni.getCwCommand(guild_id))					textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_CW).replace("{}", prefix));
			if(GuildIni.getRoomCommand(guild_id))				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_ROOM).replace("{}", prefix));
			if(GuildIni.getStatsCommand(guild_id))				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_STATS).replace("{}", prefix));
			if(GuildIni.getLeaderboardCommand(guild_id))		textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_LEADERBOARD).replace("{}", prefix));
		}
		if(other == true && type == 3) {
			if(GuildIni.getAboutCommand(guild_id))				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_ABOUT).replace("{}", prefix));
			if(GuildIni.getDisplayCommand(guild_id))			textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_DISPLAY).replace("{}", prefix));
			if(GuildIni.getPatchnotesCommand(guild_id))			textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_PATCHNOTES).replace("{}", prefix));
		}
		
		if(administration == false && entertainment == false && other == false){
			textCollector.append(STATIC.getTranslation(member, Translation.COMMANDS_DISABLED));
		}
		
		
		return textCollector.toString();
	}
}
