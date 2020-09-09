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
		if(_default_skin >= 0 && _default_skin <= _last_theme) {
			var skin = skins.parallelStream().filter(f -> f.getLine() == _default_skin).findAny().orElse(null);
			if(skin != null) {
				if(RankingSystem.SQLUpdateIconDefaultSkin(e.getGuild().getIdLong(), e.getGuild().getName(), skin.getSkin()) > 0) {
					logger.debug("{} has set the default icon skin id to {} in guild {}", e.getMember().getUser().getId(), skin.getSkin(), e.getGuild().getId());
					Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
					if(RankingSystem.SQLUpdateUsersDefaultIconSkin(guild_settings.getIconID(), skin.getSkin(), e.getGuild().getIdLong()) != -1) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_ICON_UPDATE)+skin.getSkinDescription()).build()).queue();
						logger.debug("The default icon skin has been updated for everyone who used the previous icon skin for guild {}", e.getGuild().getId());
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("default icon skin couldn't be updated for all users for guild {}", e.getGuild().getId());
					}
					Hashes.removeStatus(e.getGuild().getIdLong());
					Hashes.addOldGuildSettings(e.getGuild().getIdLong(), guild_settings);
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("RankingSystem.guilds couldn't be updated with the default icon skin in guild {}", e.getGuild().getId());
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
