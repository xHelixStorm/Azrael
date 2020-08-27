package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import enums.Translation;
import fileManagement.IniFileReader;
import interfaces.CommandPrivate;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import sql.AzraelWeb;
import util.STATIC;

public class Web implements CommandPublic, CommandPrivate {
	private final static Logger logger = LoggerFactory.getLogger(Web.class);
	private final String ENDPOINT = "/authenticate/login.php?user_id=";
	
	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		return true;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.WEB_WRONG_CHANNEL).replace("{}", e.getGuild().getSelfMember().getAsMention())).build()).queue();
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("User {} has used Web command!", e.getAuthor().getId());
	}

	@Override
	public boolean called(String[] args, PrivateMessageReceivedEvent e) {
		return true;
	}

	@Override
	public void action(String[] args, PrivateMessageReceivedEvent e) {
		final String URL = IniFileReader.getWebURL();
		if(URL != null && URL.length() > 0) {
			//Insert or update login table and redirect user to the website
			final int result = AzraelWeb.SQLInsertLoginInfo(e.getAuthor().getIdLong(), 1);
			if(result > 0) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.WEB_REDIRECT)+URL+ENDPOINT+e.getAuthor().getId()).build()).queue();
			}
			else if(result == 0) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getAuthor(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Authentication url couldn't be generated for user {}", e.getAuthor().getId());
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getAuthor(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.WEB_REDIRECT_ERR)).build()).queue();
		}
	}

	@Override
	public void executed(boolean success, PrivateMessageReceivedEvent e) {
		logger.trace("User {} has used Web command!", e.getAuthor().getId());
	}

}