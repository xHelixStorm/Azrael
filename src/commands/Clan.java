package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import core.Hashes;
import core.UserPrivs;
import enums.Translation;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Competitive;
import util.STATIC;

public class Clan implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Clan.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getClanCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getClanLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		if(Join.profilePage(e, true)) {
			final var memberLevel = Competitive.SQLgetClanMemberLevel(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
			if(memberLevel > 0) {
				//is part of a clan
				final var clan = Competitive.SQLgetClanDetails(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
				if(clan != null) {
					String message = null;
					switch(memberLevel) {
						case 1 -> {
							message = STATIC.getTranslation(e.getMember(), Translation.CLAN_MEMBER);
						}
						case 2 -> {
							message = STATIC.getTranslation(e.getMember(), Translation.CLAN_STAFF);
						}
						case 3 -> {
							message = STATIC.getTranslation(e.getMember(), Translation.CLAN_OWNER);
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
					logger.error("Clan details couldn't be retrieved from Azrael.clan_view in guild {}", e.getGuild().getId());
				}
			}
			else if(memberLevel == 0) {
				//is not part of any clan
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_MEMBERLESS)).build()).queue();
				Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000));
			}
			else {
				//db error
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Member level couldn't be retrieved from Azrael.clan_members in guild {}", e.getGuild().getId());
			}
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used Clan command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
