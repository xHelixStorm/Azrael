package de.azrael.commands;

import java.awt.Color;
import java.util.EnumSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.commandsContainer.RandomshopExecution;
import de.azrael.constructors.Guilds;
import de.azrael.core.Hashes;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Channel;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.GuildIni;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.sql.RankingSystem;
import de.azrael.sql.RankingSystemItems;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Win weapons through the randomshop with various stats
 * @author xHelixStorm
 *
 */

public class Randomshop implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Randomshop.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getRandomshopCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getRandomshopLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else if(!GuildIni.getIgnoreMissingPermissions(e.getGuild().getIdLong()))
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.BOT.getType())).collect(Collectors.toList());
		if(bot_channels.size() == 0 || bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null) != null) {
			Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
			if(guild_settings.getRankingState()) {
				if(args.length == 0) {
					//run help and collect all possible parameters
					RandomshopExecution.runHelp(e, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), false));
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PLAY))) {
					//start a round
					if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.MESSAGE_ATTACH_FILES) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.MESSAGE_ATTACH_FILES)))
						RandomshopExecution.runRound(e, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), false), bundleArguments(args, 1));
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_ATTACH_FILES.getName()).build()).queue();
						logger.error("Permission MESSAGE_ATTACH_FILES required to display the randomshop in channel {} for guild {}", e.getChannel().getId(), e.getGuild().getId());
					}
				}
				else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REPLAY))) {
					//play another round if a match occurred within 3 minutes
					var cache = Hashes.getTempCache("randomshop_play_"+e.getMember().getUser().getId());
					if(cache != null && cache.getExpiration() - System.currentTimeMillis() > 0) {
						if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.MESSAGE_ATTACH_FILES) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.MESSAGE_ATTACH_FILES)))
							RandomshopExecution.runRound(e, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), false), cache.getAdditionalInfo());
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_ATTACH_FILES.getName()).build()).queue();
							logger.error("Permission MESSAGE_ATTACH_FILES required to display the randomshop in channel {} for guild {}", e.getChannel().getId(), e.getGuild().getId());
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.RANDOMSHOP_REPLAY_ERR)).build()).queue();
						if(cache != null)
							Hashes.clearTempCache("randomshop_play_"+e.getMember().getUser().getId());
					}
				}
				else if(args.length > 0) {
					//display the weapons that can be obtained.
					if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.MESSAGE_ATTACH_FILES) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.MESSAGE_ATTACH_FILES)))
						RandomshopExecution.inspectItems(e.getMember(), e.getChannel(), RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), false), bundleArguments(args, 0), 1, false);
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_ATTACH_FILES.getName()).build()).queue();
						logger.error("Permission MESSAGE_ATTACH_FILES required to display the randomshop in channel {} for guild {}", e.getChannel().getId(), e.getGuild().getId());
					}
				}
				else {
					//if typos occur, run help
					RandomshopExecution.runHelp(e, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), false));
				}
			}
			else {
				e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.LEVEL_SYSTEM_NOT_ENABLED)).queue();
			}
		}
		else {
			e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.NOT_BOT_CHANNEL)+STATIC.getChannels(bot_channels)).queue();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("The user {} has used Randomshop command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
	
	private String bundleArguments(String [] args, int start) {
		var weapon = "";
		for(int i = start; i < args.length; i++) {
			weapon += args[i]+" ";
		}
		return weapon.trim();
	}

}
