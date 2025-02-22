package de.azrael.commands;

import java.awt.Color;
import java.util.EnumSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.commands.util.RandomshopExecution;
import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Guilds;
import de.azrael.enums.Channel;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.sql.RankingSystem;
import de.azrael.sql.RankingSystemItems;
import de.azrael.util.Hashes;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Win weapons through the randomshop with various stats
 * @author xHelixStorm
 *
 */

public class Randomshop implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Randomshop.class);

	@Override
	public boolean called(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.RANDOMSHOP);
	}

	@Override
	public boolean action(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
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
					if(e.getGuild().getSelfMember().hasPermission(e.getGuildChannel(), Permission.MESSAGE_ATTACH_FILES) || STATIC.setPermissions(e.getGuild(), e.getChannel().asTextChannel(), EnumSet.of(Permission.MESSAGE_ATTACH_FILES)))
						RandomshopExecution.runRound(e, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), false), bundleArguments(args, 1));
					else {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_ATTACH_FILES.getName()).build()).queue();
						logger.error("Permission MESSAGE_ATTACH_FILES required to display the randomshop in channel {} for guild {}", e.getChannel().getId(), e.getGuild().getId());
					}
				}
				else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REPLAY))) {
					//play another round if a match occurred within 3 minutes
					var cache = Hashes.getTempCache("randomshop_play_"+e.getMember().getUser().getId());
					if(cache != null && cache.getExpiration() - System.currentTimeMillis() > 0) {
						if(e.getGuild().getSelfMember().hasPermission(e.getGuildChannel(), Permission.MESSAGE_ATTACH_FILES) || STATIC.setPermissions(e.getGuild(), e.getChannel().asTextChannel(), EnumSet.of(Permission.MESSAGE_ATTACH_FILES)))
							RandomshopExecution.runRound(e, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), false), cache.getAdditionalInfo());
						else {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_ATTACH_FILES.getName()).build()).queue();
							logger.error("Permission MESSAGE_ATTACH_FILES required to display the randomshop in channel {} for guild {}", e.getChannel().getId(), e.getGuild().getId());
						}
					}
					else {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.RANDOMSHOP_REPLAY_ERR)).build()).queue();
						if(cache != null)
							Hashes.clearTempCache("randomshop_play_"+e.getMember().getUser().getId());
					}
				}
				else if(args.length > 0) {
					//display the weapons that can be obtained.
					if(e.getGuild().getSelfMember().hasPermission(e.getGuildChannel(), Permission.MESSAGE_ATTACH_FILES) || STATIC.setPermissions(e.getGuild(), e.getChannel().asTextChannel(), EnumSet.of(Permission.MESSAGE_ATTACH_FILES)))
						RandomshopExecution.inspectItems(e.getMember(), e.getChannel().asTextChannel(), RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), false), bundleArguments(args, 0), 1, false);
					else {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_ATTACH_FILES.getName()).build()).queue();
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
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, MessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Randomshop command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.RANDOMSHOP.getColumn(), out.toString().trim());
		}
	}
	
	private String bundleArguments(String [] args, int start) {
		var weapon = "";
		for(int i = start; i < args.length; i++) {
			weapon += args[i]+" ";
		}
		return weapon.trim();
	}

}
