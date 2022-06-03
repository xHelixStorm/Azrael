package de.azrael.commandsContainer;

import java.awt.Color;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Guilds;
import de.azrael.constructors.UserIcon;
import de.azrael.core.Hashes;
import de.azrael.enums.Translation;
import de.azrael.sql.RankingSystem;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class SetIconDefaultSkin {
	private final static Logger logger = LoggerFactory.getLogger(SetLevelDefaultSkin.class);
	
	public static void runTask(GuildMessageReceivedEvent e, int defaultSkin, int lastTheme, ArrayList<UserIcon> skins) {
		if(defaultSkin >= 0 && defaultSkin <= lastTheme) {
			var skin = skins.parallelStream().filter(f -> f.getLine() == defaultSkin).findAny().orElse(null);
			if(skin != null || defaultSkin == 0) {
				final var skinId = (skin != null ? skin.getSkin() : defaultSkin);
				final var skinDescription = (skin != null ? skin.getSkinDescription() : STATIC.getTranslation(e.getMember(), Translation.PARAM_NONE).toUpperCase());
				if(RankingSystem.SQLUpdateIconDefaultSkin(e.getGuild().getIdLong(), e.getGuild().getName(), skinId) > 0) {
					logger.info("User {} has set the default icon skin to {} in guild {}", e.getMember().getUser().getId(), skinId, e.getGuild().getId());
					Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
					if(RankingSystem.SQLUpdateUsersDefaultIconSkin(guild_settings.getIconID(), skinId, e.getGuild().getIdLong()) != -1) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_ICON_UPDATE)+skinDescription).build()).queue();
						logger.info("The default icon skin has been updated for all users who utilized the previous icon skin {} in guild {}", guild_settings.getIconID(), e.getGuild().getId());
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("The new default icon skin {} couldn't be set for all users in guild {}", skinId, e.getGuild().getId());
					}
					Hashes.removeStatus(e.getGuild().getIdLong());
					Hashes.addOldGuildSettings(e.getGuild().getIdLong(), guild_settings);
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("The new default icon skin {} couldn't be saved in guild {}", skinId, e.getGuild().getId());
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
