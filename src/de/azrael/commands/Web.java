package de.azrael.commands;

import java.awt.Color;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.IniFileReader;
import de.azrael.interfaces.CommandPrivate;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.sql.AzraelWeb;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public class Web implements CommandPublic, CommandPrivate {
	private final static Logger logger = LoggerFactory.getLogger(Web.class);
	private final static String LOGIN_ENDPOINT = "/account/login.php?user_id=";
	private final static String LOGIN_ENDPOINT2 = "/account/login.php";
	private final static String CREATE_ENDPOINT = "/account/create.php?user_id=";
	
	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(!botConfig.getIgnoreMissingPermissions())
			return true;
		return false;
	}

	@Override
	public boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.WEB_WRONG_CHANNEL).replace("{}", e.getGuild().getSelfMember().getAsMention())).build()).queue();
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Web command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.WEB.getColumn(), out.toString().trim());
		}
	}

	@Override
	public boolean called(String[] args, PrivateMessageReceivedEvent e) {
		return true;
	}

	@Override
	public boolean action(String[] args, PrivateMessageReceivedEvent e) {
		final String URL = IniFileReader.getWebURL();
		if(URL != null && URL.length() > 0) {
			//Retrieve auth type
			final int auth_type = AzraelWeb.SQLgetLoginType(e.getAuthor().getIdLong(), e.getJDA().getSelfUser().getIdLong());
			if(auth_type > 0) {
				final boolean defaultBot = AzraelWeb.SQLisDefaultBot(e.getJDA().getSelfUser().getIdLong());
				if(auth_type == 4 || auth_type == 5) {
					final String key = RandomStringUtils.random(15, true, true);
					//Insert or update login table and redirect user to the website
					final int result = AzraelWeb.SQLInsertLoginInfo(e.getAuthor().getIdLong(), 1, key);
					if(result > 0) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.WEB_REDIRECT)+URL+LOGIN_ENDPOINT+e.getAuthor().getId()+"&key="+key+(!defaultBot ? "&bot="+e.getJDA().getSelfUser().getId() : "")).build()).queue();
					}
					else if(result == 0) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getAuthor(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Authentication url couldn't be generated for user {}", e.getAuthor().getId());
					}
				}
				else {
					if(defaultBot)
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.WEB_NOT_ALLOWED)+URL).build()).queue();
					else
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.WEB_LOGIN)+URL+LOGIN_ENDPOINT2+"?bot="+e.getJDA().getSelfUser().getId()).build()).queue();
				}
			}
			else if(auth_type == 0) {
				final boolean defaultBot = AzraelWeb.SQLisDefaultBot(e.getJDA().getSelfUser().getIdLong());
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.WEB_CREATE)+URL+CREATE_ENDPOINT+e.getAuthor().getId()+(!defaultBot ? "&bot="+e.getJDA().getSelfUser().getId() : "")).build()).queue();
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getAuthor(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("It couldn't be verified if user {} already has an account", e.getAuthor().getId());
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getAuthor(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.WEB_REDIRECT_ERR)).build()).queue();
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, PrivateMessageReceivedEvent e) {
		if(success) {
			logger.trace("{} has used Web command", e.getAuthor().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getAuthor().getIdLong(), 0, Command.WEB.getColumn(), out.toString().trim());
		}
	}
}
