package de.azrael.preparedMessages;

import java.util.ArrayList;

import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.entities.Member;

public class CommandList {
	
	public static String getHelp(Member member, boolean permissionGranted, int type, ArrayList<?> commands) {
		long guild_id = member.getGuild().getIdLong();
		StringBuilder textCollector = new StringBuilder();
		boolean administration = false;
		boolean entertainment = false;
		boolean other = false;
		
		boolean register = false;
		boolean set = false;
		boolean remove = false;
		boolean user = false;
		boolean filter = false;
		boolean roleReaction = false;
		boolean subscribe = false;
		boolean doubleExperience = false;
		boolean heavyCensoring = false;
		boolean mute = false;
		boolean google = false;
		boolean write = false;
		boolean edit = false;
		boolean accept = false;
		boolean deny = false;
		boolean schedule = false;
		boolean prune = false;
		boolean warn = false;
		boolean invites = false;
		
		boolean pug = false;
		boolean meow = false;
		boolean rank = false;
		boolean profile = false;
		boolean top = false;
		boolean use = false;
		boolean shop = false;
		boolean inventory = false;
		boolean daily = false;
		boolean quiz = false;
		boolean randomshop = false;
		boolean equip = false;
		boolean matchmaking = false;
		boolean join = false;
		boolean leave = false;
		boolean clan = false;
		boolean queue = false;
		boolean cw = false;
		boolean room = false;
		boolean stats = false;
		boolean leaderboard = false;
		
		boolean about = false;
		boolean display = false;
		boolean patchnotes = false;
		boolean language = false;
		
		for(int i = 0; i < commands.size(); i++) {
			final boolean command = (Boolean)commands.get(i);
			switch(i) {
				case 0 	-> register 		= command;
				case 1 	-> set 				= command;
				case 2 	-> remove 			= command;
				case 3 	-> user 			= command;
				case 4 	-> filter 			= command;
				case 5 	-> roleReaction 	= command;
				case 6 	-> subscribe 		= command;
				case 7 	-> doubleExperience = command;
				case 8 	-> heavyCensoring 	= command;
				case 9 	-> mute 			= command;
				case 10 -> google 			= command;
				case 11 -> write 			= command;
				case 12 -> edit 			= command;
				case 13 -> accept 			= command;
				case 14 -> deny 			= command;
				case 15 -> schedule 		= command;
				case 16 -> prune 			= command;
				case 17 -> warn 			= command;
				case 18 -> invites 			= command;
				case 19 -> pug 				= command;
				case 20 -> meow 			= command;
				case 21 -> rank 			= command;
				case 22 -> profile 			= command;
				case 23 -> top 				= command;
				case 24 -> use 				= command;
				case 25 -> shop 			= command;
				case 26 -> inventory 		= command;
				case 27 -> daily 			= command;
				case 28 -> quiz 			= command;
				case 29 -> randomshop 		= command;
				case 30 -> equip 			= command;
				case 31 -> matchmaking 		= command;
				case 32 -> join 			= command;
				case 33 -> leave 			= command;
				case 34 -> clan 			= command;
				case 35 -> queue 			= command;
				case 36 -> cw 				= command;
				case 37 -> room 			= command;
				case 38 -> stats 			= command;
				case 39 -> leaderboard 		= command;
				case 40 -> about 			= command;
				case 41 -> display 			= command;
				case 42 -> patchnotes 		= command;
				case 43 -> language 		= command;
			}
		}
		
		if(permissionGranted && (register || set || remove || user || filter || roleReaction || subscribe || doubleExperience || heavyCensoring || mute || google || write || edit || accept || deny || schedule || prune || warn || invites)) {
			administration = true;
		}
		
		if(pug || meow || rank || profile || top || use || shop || inventory || daily || quiz || randomshop || equip || matchmaking || join || leave || clan || queue || cw || room || stats || leaderboard) {
			entertainment = true;
		}
		
		if(about || display || patchnotes || language) {
			other = true;
		}
		
		final String prefix = BotConfiguration.SQLgetBotConfigs(guild_id).getCommandPrefix();
		if(administration == true && type == 1) {
			if(register)			textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_REGISTER).replace("{}", prefix));
			if(set)					textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_SET).replace("{}", prefix));
			if(remove) 				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_REMOVE).replace("{}", prefix));
			if(user)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_USER).replace("{}", prefix));
			if(filter)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_FILTER).replace("{}", prefix));
			if(roleReaction)		textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_ROLE_REACTION).replace("{}", prefix));
			if(subscribe)			textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_SUBSCRIBE).replace("{}", prefix));
			if(doubleExperience)	textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_DOUBLE_EXPERIENCE).replace("{}", prefix));
			if(heavyCensoring)		textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_HEAVY_CENSORING).replace("{}", prefix));
			if(mute)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_MUTE).replace("{}", prefix));
			if(google)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_GOOGLE).replace("{}", prefix));
			if(write)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_WRITE).replace("{}", prefix));
			if(edit)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_EDIT).replace("{}", prefix));
			if(accept)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_ACCEPT).replace("{}", prefix));
			if(deny)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_DENY).replace("{}", prefix));
			if(schedule)			textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_SCHEDULE).replace("{}", prefix));
			if(prune)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_PRUNE).replace("{}", prefix));
			if(warn)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_WARN).replace("{}", prefix));
			if(invites)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_INVITES).replace("{}", prefix));
		}
		if(entertainment == true && type == 2) {
			if(pug)					textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_PUG).replace("{}", prefix));
			if(meow)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_MEOW).replace("{}", prefix));
			if(rank)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_RANK).replace("{}", prefix));
			if(profile)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_PROFILE).replace("{}", prefix));
			if(top)					textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_TOP).replace("{}", prefix));
			if(use)					textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_USE).replace("{}", prefix));
			if(shop)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_SHOP).replace("{}", prefix));
			if(inventory)			textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_INVENTORY).replace("{}", prefix));
			if(daily)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_DAILY).replace("{}", prefix));
			if(quiz)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_QUIZ).replace("{}", prefix));
			if(randomshop)			textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_RANDOMSHOP).replace("{}", prefix));
			if(equip)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_EQUIP).replace("{}", prefix));
			if(matchmaking)			textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_MATCHMAKING).replace("{}", prefix));
			if(join)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_JOIN).replace("{}", prefix));
			if(leave)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_LEAVE).replace("{}", prefix));
			if(clan)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_CLAN).replace("{}", prefix));
			if(queue)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_QUEUE).replace("{}", prefix));
			if(cw)					textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_CW).replace("{}", prefix));
			if(room)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_ROOM).replace("{}", prefix));
			if(stats)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_STATS).replace("{}", prefix));
			if(leaderboard)			textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_LEADERBOARD).replace("{}", prefix));
		}
		if(other == true && type == 3) {
			if(about)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_ABOUT).replace("{}", prefix));
			if(display)				textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_DISPLAY).replace("{}", prefix));
			if(patchnotes)			textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_PATCHNOTES).replace("{}", prefix));
			if(language)			textCollector.append(STATIC.getTranslation(member, Translation.COMMAND_LANGUAGE).replace("{}", prefix));
		}
		if(type == 4) {
			final var customCommands = Azrael.SQLgetCustomCommands2(guild_id);
			if(customCommands != null && customCommands.size() > 0) {
				for(final var command : customCommands) {
					textCollector.append("- **"+command.getCommand()+"**\n"+command.getDescription()+"\n");
				}
			}
		}
		
		if(administration == false && entertainment == false && other == false){
			textCollector.append(STATIC.getTranslation(member, Translation.COMMANDS_DISABLED));
		}
		
		
		return textCollector.toString();
	}
}
