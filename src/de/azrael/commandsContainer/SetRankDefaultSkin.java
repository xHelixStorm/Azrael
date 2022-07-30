package de.azrael.commandsContainer;

import java.awt.Color;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Guilds;
import de.azrael.constructors.UserRank;
import de.azrael.core.Hashes;
import de.azrael.enums.Translation;
import de.azrael.sql.RankingSystem;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class SetRankDefaultSkin {
	private final static Logger logger = LoggerFactory.getLogger(SetRankDefaultSkin.class);
	
	public static void runTask(GuildMessageReceivedEvent e, int defaultSkin, int lastTheme, ArrayList<UserRank> skins) {
		if(defaultSkin >= 0 && defaultSkin <= lastTheme) {
			UserRank skin = null;
			if(defaultSkin > 0)
				skin = skins.get(defaultSkin-1);
			if(skin != null || defaultSkin == 0) {
				final var skinId = (skin != null ? skin.getSkin() : defaultSkin);
				final var skinDescription = (skin != null ? skin.getSkinDescription() : STATIC.getTranslation(e.getMember(), Translation.PARAM_NONE).toUpperCase());
				if(RankingSystem.SQLUpdateRankDefaultSkin(e.getGuild().getIdLong(), e.getGuild().getName(), skinId) > 0) {
					logger.info("User {} has set the default rank skin to {} in guild {}", e.getMember().getUser().getId(), skinId, e.getGuild().getId());
					Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
					if(RankingSystem.SQLUpdateUsersDefaultRankSkin(guild_settings.getRankID(), skinId, e.getGuild().getIdLong()) != -1) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_RANK_UPDATE)+skinDescription).build()).queue();
						logger.info("The default rank skin has been updated for all users who utilized the previous rank skin {} in guild {}", skinId, guild_settings.getProfileID(), e.getGuild().getId());
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("The new default rank skin {} couldn't be updated for all users in guild {}", skinId, e.getGuild().getId());
					}
					Hashes.removeStatus(e.getGuild().getIdLong());
					Hashes.addOldGuildSettings(e.getGuild().getIdLong(), guild_settings);
					Hashes.removeGuildRanking(e.getGuild().getIdLong());
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("The new default rank skin {} couldn't be saved in guild {}", skinId, e.getGuild().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_AVAILABLE_SKINS)).build()).queue();
			}
		}
		else{
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_AVAILABLE_SKINS)).build()).queue();
		}
	}
}
