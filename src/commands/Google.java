package commands;

import java.awt.Color;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import core.Hashes;
import core.UserPrivs;
import enums.Translation;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import util.STATIC;

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
			else
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
			if(!credentialContent.isEmpty())
				email = credentialContent.getString("client_email");
			else 
				email = STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE);
			EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS));
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGlE_HELP).replace("{}", email)).build()).queue();
		}
		else if(args.length == 1) {
			//Write in cache to display options related to google docs
			if(args[0].equalsIgnoreCase("docs")) {
				Hashes.addTempCache("google_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "docs"));
			}
			//Write in cache to display options related to google spreadsheets
			else if(args[0].equalsIgnoreCase("spreadsheets")) {
				Hashes.addTempCache("google_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "spreadsheets"));
			}
			//Write in cache to display options related to google drive
			else if(args[0].equalsIgnoreCase("drive")) {
				Hashes.addTempCache("google_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "drive"));
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
		logger.debug("{} has used Google command in guild {}", e.getMember().getUser().getIdLong(), e.getGuild().getId());
	}

}
