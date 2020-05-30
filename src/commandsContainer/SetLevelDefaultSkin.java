package commandsContainer;

import java.awt.Color;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import constructors.Rank;
import core.Hashes;
import enums.Translation;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;
import util.STATIC;

public class SetLevelDefaultSkin {
	private final static Logger logger = LoggerFactory.getLogger(SetLevelDefaultSkin.class);
	
	public static void runTask(GuildMessageReceivedEvent e, int _default_skin, int _last_theme, List<Rank> skins) {
		if(_default_skin >= 0 && _default_skin <= _last_theme) {
			final var skin = skins.parallelStream().filter(f -> f.getLevelLine() == _default_skin).findAny().orElse(null);
			if(skin != null) {
				if(RankingSystem.SQLUpdateLevelDefaultSkin(e.getGuild().getIdLong(), e.getGuild().getName(), skin.getRankingLevel()) > 0) {
					logger.debug("{} has set the default level skin id to {} in guild {}", e.getMember().getUser().getId(), skin.getRankingLevel(), e.getGuild().getId());
					Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
					if(RankingSystem.SQLUpdateUsersDefaultLevelSkin(guild_settings.getLevelID(), skin.getRankingLevel(), e.getGuild().getIdLong()) != -1) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_LEVEL_UPDATE)+skin.getLevelDescription()).build()).queue();
						logger.debug("The default level skin has been updated for everyone who used the previous level skin for guild {}", e.getGuild().getId());
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("default level skin couldn't be updated for all users in guild {}", e.getGuild().getId());
					}
					Hashes.removeStatus(e.getGuild().getIdLong());
					Hashes.addOldGuildSettings(e.getGuild().getIdLong(), guild_settings);
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("RankingSystem.guilds couldn't be updated with the default level skin in guild {}", e.getGuild().getId());
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
