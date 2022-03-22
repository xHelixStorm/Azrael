package de.azrael.commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.core.Hashes;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.sql.Competitive;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Clan implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Clan.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.CLAN);
	}

	@Override
	public boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(Join.profilePage(e, true)) {
			final var memberLevel = Competitive.SQLgetClanMemberLevel(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
			if(memberLevel > 0) {
				//is part of a clan
				final var clan = Competitive.SQLgetClanDetails(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
				if(clan != null) {
					String message = null;
					switch(memberLevel) {
						case 1 -> {
							message = STATIC.getTranslation(e.getMember(), Translation.CLAN_MEMBER)
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_SEARCH))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_MEMBERS))
									.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_LEAVE));
						}
						case 2 -> {
							message = STATIC.getTranslation(e.getMember(), Translation.CLAN_STAFF)
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_SEARCH))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_MEMBERS))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_LEAVE))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_KICK))
									.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_INVITE));
						}
						case 3 -> {
							message = STATIC.getTranslation(e.getMember(), Translation.CLAN_OWNER)
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_SEARCH))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_MEMBERS))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_LEAVE))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_KICK))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_INVITE))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_DELEGATE))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ICON))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_PROMOTE))
									.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_DISBAND));
						}
					}
					EmbedBuilder embed = new EmbedBuilder().setColor(Color.BLUE).setTitle(clan.getClanName());
					if(clan.getClanMark() != null)
						embed.setThumbnail(clan.getClanMark());
					embed.addField(STATIC.getTranslation(e.getMember(), Translation.CLAN_MEMBERS), ""+clan.getMembers()+"/"+Competitive.SQLgetMaxClanMembers(e.getGuild().getIdLong()), true);
					embed.addField(STATIC.getTranslation(e.getMember(), Translation.CLAN_MATCHES), ""+clan.getMatches(), true);
					embed.addField(STATIC.getTranslation(e.getMember(), Translation.CLAN_CREATED), clan.getCreationDate().toLocalDateTime().toLocalDate().toString(), true);
					embed.addField(STATIC.getTranslation(e.getMember(), Translation.CLAN_WINS), ""+clan.getWins(), true);
					embed.addField(STATIC.getTranslation(e.getMember(), Translation.CLAN_LOSSES), ""+clan.getLosses(), true);
					embed.addField(STATIC.getTranslation(e.getMember(), Translation.CLAN_WIN_LOSE_RATIO), (Math.round(((double)clan.getWins()/(double)clan.getMatches())*100))+"%", true);
					e.getChannel().sendMessage(embed.setDescription(message).build()).queue();
					Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, ""+clan.getClanID()));
				}
				else {
					//db error
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Clan details couldn't be retrieved in guild {}", e.getGuild().getId());
				}
			}
			else if(memberLevel == 0) {
				//is not part of any clan
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_MEMBERLESS)
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_SEARCH))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_APPLY))
						.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_CREATE))).build()).queue();
				Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000));
			}
			else {
				//db error
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Member level couldn't be retrieved in guild {}", e.getGuild().getId());
			}
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Clan command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.CLAN.getColumn(), out.toString().trim());
		}
	}
}
