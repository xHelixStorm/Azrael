package de.azrael.commands;

import java.awt.Color;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Cache;
import de.azrael.core.Hashes;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.FileSetting;
import de.azrael.fileManagement.GuildIni;
import de.azrael.interfaces.CommandPublic;
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
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getGoogleCommand(e.getGuild().getIdLong())) {
			var commandLevel = GuildIni.getGoogleLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || e.getMember().getUser().getIdLong() == GuildIni.getAdmin(e.getGuild().getIdLong()))
				return true;
			else if(!GuildIni.getIgnoreMissingPermissions(e.getGuild().getIdLong()))
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		//print help message and all currently available APIs
		if(args.length == 0) {
			String email;
			JSONObject credentialContent = new JSONObject(FileSetting.readFile("./files/Google/credentials.json"));
			if(credentialContent.length() > 0)
				email = credentialContent.getString("client_email");
			else 
				email = STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE);
			EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS));
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGlE_HELP).replace("{}", email)).build()).queue();
		}
		else if(args.length == 1) {
			//Write in cache to display options related to google spreadsheets
			if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_SPREADSHEETS))) {
				Hashes.addTempCache("google_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "spreadsheets"));
			}
			//Write in cache to display options related to google docs
			/*else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DOCS))) {
				Hashes.addTempCache("google_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "docs"));
			}
			//Write in cache to display options related to google drive
			else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DRIVE))) {
				Hashes.addTempCache("google_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "drive"));
			}*/
			else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_YOUTUBE))) {
				Hashes.addTempCache("google_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "youtube"));
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
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used Google command in guild {}", e.getMember().getUser().getIdLong(), e.getGuild().getId());
	}

}
