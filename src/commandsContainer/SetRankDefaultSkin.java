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

public class SetRankDefaultSkin {
	private final static Logger logger = LoggerFactory.getLogger(SetRankDefaultSkin.class);
	
	public static void runTask(GuildMessageReceivedEvent e, int _default_skin, int _last_theme, List<Rank> skins) {
		if(_default_skin > 0 && _default_skin <= _last_theme) {
			final var skin = skins.parallelStream().filter(f -> f.getRankLine() == _default_skin).findAny().orElse(null);
			if(skin != null) {
				if(RankingSystem.SQLUpdateRankDefaultSkin(e.getGuild().getIdLong(), e.getGuild().getName(), skin.getRankingRank()) > 0) {
					logger.debug("{} has set the default rank skin id to {} in guild {}", e.getMember().getUser().getId(), skin.getRankingRank(), e.getGuild().getId());
					Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
					if(RankingSystem.SQLUpdateUsersDefaultRankSkin(guild_settings.getRankID(), skin.getRankingRank(), e.getGuild().getIdLong()) != -1) {
						e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.SET_RANK_UPDATE)+skin.getRankDescription()).queue();
						logger.debug("The default rank skin has been updated for everyone who used the previous rank skin for guild {}", e.getGuild().getId());
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("default level skin couldn't be updated for all users for guild {}", e.getGuild().getId());
					}
					Hashes.removeStatus(e.getGuild().getIdLong());
					Hashes.addOldGuildSettings(e.getGuild().getIdLong(), guild_settings);
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("RankingSystem.guilds couldn't be updated with the default rank skin in guild {}", e.getGuild().getId());
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
