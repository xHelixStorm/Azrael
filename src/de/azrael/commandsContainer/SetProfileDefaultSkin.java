package de.azrael.commandsContainer;

import java.awt.Color;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Guilds;
import de.azrael.constructors.UserProfile;
import de.azrael.core.Hashes;
import de.azrael.enums.Translation;
import de.azrael.sql.RankingSystem;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class SetProfileDefaultSkin {
	private final static Logger logger = LoggerFactory.getLogger(SetProfileDefaultSkin.class);
	
	public static void runTask(GuildMessageReceivedEvent e, int _default_skin, int _last_theme, ArrayList<UserProfile> skins) {
		if((_default_skin > 0 && _default_skin <= _last_theme) || _default_skin == 0) {
			final var skin = skins.parallelStream().filter(f -> f.getLine() == _default_skin).findAny().orElse(null);
			if(skin != null || _default_skin == 0) {
				final var skinId = (skin != null ? skin.getSkin() : _default_skin);
				final var skinDescription = (skin != null ? skin.getDescriptionMode() : STATIC.getTranslation(e.getMember(), Translation.PARAM_NONE).toUpperCase());
				if(RankingSystem.SQLUpdateProfileDefaultSkin(e.getGuild().getIdLong(), e.getGuild().getName(), skinId) > 0) {
					logger.info("User {} has set the default profile skin to {} in guild {}", e.getMember().getUser().getId(), skinId, e.getGuild().getId());
					Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
					if(RankingSystem.SQLUpdateUsersDefaultProfileSkin(guild_settings.getProfileID(), skinId, e.getGuild().getIdLong()) != -1) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_PROFILE_UPDATE)+skinDescription).build()).queue();
						logger.info("The default profile skin has been updated for all users who utilized the previous profile skin {} in guild {}", skinId, guild_settings.getProfileID(), e.getGuild().getId());
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("The new default profile skin {} couldn't be updated for all users in guild {}", skinId, e.getGuild().getId());
					}
					Hashes.removeStatus(e.getGuild().getIdLong());
					Hashes.addOldGuildSettings(e.getGuild().getIdLong(), guild_settings);
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("The new default profile skin {} couldn't be saved in guild {}", skinId, e.getGuild().getId());
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
