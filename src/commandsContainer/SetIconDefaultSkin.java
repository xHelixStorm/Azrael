package commandsContainer;

import java.awt.Color;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import constructors.UserIcon;
import core.Hashes;
import enums.Translation;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;
import util.STATIC;

public class SetIconDefaultSkin {
	private final static Logger logger = LoggerFactory.getLogger(SetLevelDefaultSkin.class);
	
	public static void runTask(GuildMessageReceivedEvent e, int _default_skin, int _last_theme, ArrayList<UserIcon> skins) {
		if((_default_skin >= 0 && _default_skin <= _last_theme) || _default_skin == 0) {
			var skin = skins.parallelStream().filter(f -> f.getLine() == _default_skin).findAny().orElse(null);
			if(skin != null || _default_skin == 0) {
				final var skinId = (skin != null ? skin.getSkin() : _default_skin);
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
