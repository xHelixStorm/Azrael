package de.azrael.commands;

import java.awt.Color;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.constructors.Guilds;
import de.azrael.core.Hashes;
import de.azrael.enums.Channel;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.IniFileReader;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.sql.RankingSystem;
import de.azrael.sql.RankingSystemItems;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Purchase items, skins and more from the shop
 */

public class Shop implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Shop.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.SHOP);
	}

	@Override
	public boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
		if(guild_settings.getRankingState()) {
			var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.BOT.getType())).collect(Collectors.toList());
			if(bot_channels.size() == 0 || bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null) != null) {
				StringBuilder out = new StringBuilder();
				final var skins = RankingSystem.SQLgetSkinshopContentAndType(e.getGuild().getIdLong(), true);
				if(skins != null && skins.size() > 0) {
					out.append(STATIC.getTranslation(e.getMember(), Translation.SHOP_HELP)
							.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_EXIT)));
					if(skins.parallelStream().filter(f -> f.getSkinType().equals("lev")).findAny().orElse(null) != null) {
						out.append(STATIC.getTranslation(e.getMember(), Translation.SHOP_HELP_2)
								.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_LEVEL_UPS).toUpperCase()));
					}
					if(skins.parallelStream().filter(f -> f.getSkinType().equals("ran")).findAny().orElse(null) != null) {
						out.append(STATIC.getTranslation(e.getMember(), Translation.SHOP_HELP_3)
								.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_RANKS).toUpperCase()));
					}
					if(skins.parallelStream().filter(f -> f.getSkinType().equals("pro")).findAny().orElse(null) != null) {
						out.append(STATIC.getTranslation(e.getMember(), Translation.SHOP_HELP_4)
								.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_PROFILES).toUpperCase()));
					}
					if(skins.parallelStream().filter(f -> f.getSkinType().equals("ico")).findAny().orElse(null) != null) {
						out.append(STATIC.getTranslation(e.getMember(), Translation.SHOP_HELP_5)
								.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ICONS).toUpperCase()));
					}
					if(skins.parallelStream().filter(f -> f.getSkinType().equals("ite")).findAny().orElse(null) != null) {
						out.append(STATIC.getTranslation(e.getMember(), Translation.SHOP_HELP_6)
								.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ITEMS).toUpperCase()));
					}
				}
				else if(skins == null) {
					logger.error("Shop skin couldn't be retrieved in guild {}", e.getGuild().getId());
				}
				final var weapons = RankingSystemItems.SQLgetWholeWeaponShop(e.getGuild().getIdLong());
				if(weapons != null && weapons.size() > 0 && weapons.parallelStream().filter(f -> f.getEnabled()).findAny().orElse(null) != null) {
					if(out.length() == 0)
						out.append(STATIC.getTranslation(e.getMember(), Translation.SHOP_HELP)
								.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_EXIT)));
					out.append(STATIC.getTranslation(e.getMember(), Translation.SHOP_HELP_7)
							.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_WEAPONS).toUpperCase()));
				}
				else if(weapons == null) {
					logger.error("Shop weapons couldn't be retrieved in guild {}", e.getGuild().getId());
				}
				final var skills = RankingSystemItems.SQLgetSkills(e.getGuild().getIdLong());
				if(skills != null && skills.size() > 0 && skills.parallelStream().filter(f -> f.getEnabled()).findAny().orElse(null) != null) {
					if(out.length() == 0)
						out.append(STATIC.getTranslation(e.getMember(), Translation.SHOP_HELP)
								.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_EXIT)));
					out.append(STATIC.getTranslation(e.getMember(), Translation.SHOP_HELP_8)
							.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_SKILLS).toUpperCase()));
				}
				else if(skills == null) {
					logger.error("Shop skills couldn't be retrieved in guild {}", e.getGuild().getId());
				}
				if(out.length() > 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.SHOP_TITLE)).setThumbnail(IniFileReader.getShopThumbnail())
						.setDescription(out.toString()).build()).queue();
					Hashes.addTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000));
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SHOP_EMPTY)).build()).queue();
				}
			}
			else{
				e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.NOT_BOT_CHANNEL)+STATIC.getChannels(bot_channels)).queue();
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEVEL_SYSTEM_NOT_ENABLED)).build()).queue();
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Shop command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SHOP.getColumn(), out.toString().trim());
		}
	}
}
