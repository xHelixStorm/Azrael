package commands;

import java.awt.Color;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import constructors.Guilds;
import core.Hashes;
import core.UserPrivs;
import enums.Channel;
import enums.Translation;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import sql.RankingSystem;
import sql.RankingSystemItems;
import util.STATIC;

/**
 * Purchase items, skins and more from the shop
 * @author xHelixStorm
 *
 */

public class Shop implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Shop.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getShopCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getShopLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else 
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
		if(guild_settings.getRankingState()) {
			var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.BOT.getType())).collect(Collectors.toList());
			if(bot_channels.size() == 0 || bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null) != null) {
				StringBuilder out = new StringBuilder();
				final var skins = RankingSystem.SQLgetSkinshopContentAndType(e.getGuild().getIdLong(), true);
				if(skins != null && skins.size() > 0) {
					out.append(STATIC.getTranslation(e.getMember(), Translation.SHOP_HELP));
					if(skins.parallelStream().filter(f -> f.getSkinType().equals("lev")).findAny().orElse(null) != null) {
						out.append(STATIC.getTranslation(e.getMember(), Translation.SHOP_HELP_2));
					}
					if(skins.parallelStream().filter(f -> f.getSkinType().equals("ran")).findAny().orElse(null) != null) {
						out.append(STATIC.getTranslation(e.getMember(), Translation.SHOP_HELP_3));
					}
					if(skins.parallelStream().filter(f -> f.getSkinType().equals("pro")).findAny().orElse(null) != null) {
						out.append(STATIC.getTranslation(e.getMember(), Translation.SHOP_HELP_4));
					}
					if(skins.parallelStream().filter(f -> f.getSkinType().equals("ico")).findAny().orElse(null) != null) {
						out.append(STATIC.getTranslation(e.getMember(), Translation.SHOP_HELP_5));
					}
					if(skins.parallelStream().filter(f -> f.getSkinType().equals("ite")).findAny().orElse(null) != null) {
						out.append(STATIC.getTranslation(e.getMember(), Translation.SHOP_HELP_6));
					}
				}
				else if(skins == null) {
					logger.error("Shop skin couldn't be retrieved in guild {}", e.getGuild().getId());
				}
				final var weapons = RankingSystemItems.SQLgetWholeWeaponShop(e.getGuild().getIdLong());
				if(weapons != null && weapons.size() > 0 && weapons.parallelStream().filter(f -> f.getEnabled()).findAny().orElse(null) != null) {
					if(out.length() == 0)
						out.append(STATIC.getTranslation(e.getMember(), Translation.SHOP_HELP));
					out.append(STATIC.getTranslation(e.getMember(), Translation.SHOP_HELP_7));
				}
				else if(weapons == null) {
					logger.error("Shop weapons couldn't be retrieved in guild {}", e.getGuild().getId());
				}
				final var skills = RankingSystemItems.SQLgetSkills(e.getGuild().getIdLong());
				if(skills != null && skills.size() > 0 && skills.parallelStream().filter(f -> f.getEnabled()).findAny().orElse(null) != null) {
					if(out.length() == 0)
						out.append(STATIC.getTranslation(e.getMember(), Translation.SHOP_HELP));
					out.append(STATIC.getTranslation(e.getMember(), Translation.SHOP_HELP_8));
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
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used Shop command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
