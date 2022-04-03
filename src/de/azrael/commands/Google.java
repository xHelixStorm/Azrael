package de.azrael.commands;

import java.awt.Color;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.core.Hashes;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.util.FileHandler;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Google APIs set up for specific events
 * @author xHelixStorm
 *
 */

public class Google implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Google.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.GOOGLE);
	}

	@Override
	public boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		//print help message and all currently available APIs
		if(args.length == 0) {
			String email;
			JSONObject credentialContent = new JSONObject(FileHandler.readFile("./files/Google/credentials.json"));
			if(credentialContent.length() > 0)
				email = credentialContent.getString("client_email");
			else 
				email = STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE);
			EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS));
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGlE_HELP).replaceFirst("\\{\\}", email)
					.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_SPREADSHEETS))).build()).queue();
		}
		else if(args.length == 1) {
			//Write in cache to display options related to google spreadsheets
			if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_SPREADSHEETS))) {
				Hashes.addTempCache("google_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "spreadsheets"));
			}
			else {
				EmbedBuilder message = new EmbedBuilder().setColor(Color.RED);
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_API_NOT_AVAILABLE)).build()).queue();
			}
		}
		else {
			EmbedBuilder message = new EmbedBuilder().setColor(Color.RED);
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
		return true;
	}

	@Override
	public void executed(String [] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Google command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), out.toString().trim());
		}
	}

}
