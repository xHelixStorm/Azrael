package commandsContainer;

import java.awt.Color;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import core.Hashes;
import enums.Translation;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import sql.Competitive;
import util.STATIC;

public class JoinExecution {
	private final static Logger logger = LoggerFactory.getLogger(JoinExecution.class);
	
	@SuppressWarnings("unchecked")
	public static void registerName(GuildMessageReceivedEvent e, final Cache cache) {
		if(e.getMessage().getContentRaw().length() <= 20) {
			final int nameTaken = Competitive.SQLisNameTaken(e.getGuild().getIdLong(), e.getMessage().getContentRaw());
			if(nameTaken == 0) {
				if(Competitive.SQLInsertUserStat(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), e.getMessage().getContentRaw()) > 0) {
					final var servers = (ArrayList<String>) cache.getObject();
					if(servers.size() == 0) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.JOIN_PROFILE_CREATED).replace("{}", e.getMessage().getContentRaw())).build()).queue();
						Hashes.clearTempCache("userProfile_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					}
					else {
						StringBuilder out = new StringBuilder();
						for(int i = 0; i < servers.size(); i++) {
							if(i == 0)
								out.append("**"+servers.get(i)+"**");
							else
								out.append(", **"+servers.get(i)+"**");
						}
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.JOIN_PROFILE_CREATED_2).replace("{}", e.getMessage().getContentRaw())+out.toString()).build()).queue();
						Hashes.addTempCache("userProfile_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.updateDescription("server"));
					}
					Azrael.SQLInsertActionLog("PROFILE", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Profile page generation");
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("User profile couldn't be generated for user {} in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
					Hashes.clearTempCache("userProfile_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
				}
			}
			else if(nameTaken == 1) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.JOIN_NAME_TAKEN)).build()).queue();
				Hashes.addTempCache("userProfile_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Name verification couldn't be done for user {} in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
				Hashes.clearTempCache("userProfile_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.STATS_RENAME_ERR)).build()).queue();
			Hashes.addTempCache("userProfile_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void registerServer(GuildMessageReceivedEvent e, final Cache cache) {
		final var servers = (ArrayList<String>) cache.getObject();
		if(servers.parallelStream().filter(f -> f.equalsIgnoreCase(e.getMessage().getContentRaw())).findAny().orElse(null) != null) {
			final var server = e.getMessage().getContentRaw().toUpperCase();
			if(Competitive.SQLUpdateSelectedServerInUserStats(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), server) > 0) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.JOIN_SERVER_ADDED)).build()).queue();
				Azrael.SQLInsertActionLog("PROFILE_UPDATE", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Server update to "+server);
				Hashes.clearTempCache("userProfile_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			}
			else {
				//server update error
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Server couldn't be updated in table Azrael.user_stats for user {} in guild {}", e.getGuild().getId(), e.getMember().getUser().getId());
				Hashes.clearTempCache("userProfile_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			}
		}
		else {
			//message that the written server doesn't exist
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.JOIN_NO_SERVER)).build()).queue();
			Hashes.addTempCache("userProfile_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
		}
	}
}
