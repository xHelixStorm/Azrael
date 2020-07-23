package commandsContainer;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import enums.Translation;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Competitive;
import util.STATIC;

public class SetMatchmakingMembers {
	private final static Logger logger = LoggerFactory.getLogger(SetMatchmakingMembers.class);
	
	public static void runHelp(GuildMessageReceivedEvent e) {
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_MATCHMAKING_MEMBERS_HELP)).build()).queue();
	}
	
	public static void runTask(GuildMessageReceivedEvent e, String [] args) {
		if(args.length == 2) {
			if(args[1].replaceAll("[0-9]*", "").length() == 0) {
				final int members = Integer.parseInt(args[1]);
				if(members >= 0 && members <= 100) {
					if(Competitive.SQLUpdateMatchmakingMembers(e.getGuild().getIdLong(), members) > 0) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_MATCHMAKING_MEMBERS_SUC).replace("{}", ""+members)).build()).queue();
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Max matchmaking members couldn't be updated in table Azrael.guild for guild {}", e.getGuild().getId());
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_MATCHMAKING_MEMBERS_ERR)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_MATCHMAKING_MEMBERS_ERR)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
	}
}