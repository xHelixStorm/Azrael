package commandsContainer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import enums.Translation;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import util.STATIC;

/**
 * Extension of the set command
 * @author xHelixStorm
 *
 */

public class SetChannelFilter {
	private final static Logger logger = LoggerFactory.getLogger(SetChannelFilter.class);
	
	@SuppressWarnings("preview")
	public static void runTask(GuildMessageReceivedEvent e, String [] _input) {
		int i = 0;
		ArrayList<String> filter_lang = new ArrayList<String>();
		boolean languageError = false;
		if(_input.length == 3) {
			String channel = _input[1].replaceAll("[^0-9]*", "");
			if(e.getGuild().getTextChannelById(channel) != null) {
				long channel_id = Long.parseLong(channel);
				Azrael.SQLDeleteChannel_Filter(channel_id);
				Pattern pattern = Pattern.compile("(all|eng|fre|ger|tur|rus|spa|por|ita)");
				Matcher matcher = pattern.matcher(_input[2]);
				while(matcher.find()) {
					filter_lang.add(matcher.group());
					if(filter_lang.get(i).length() != 3) {
						languageError = true;
					}
					else {
						switch(filter_lang.get(i)) {
						case "all", "eng", "ger", "fre", "tur", "rus", "spa", "por", "ita" ->
							languageError = false;
						default -> languageError = true;
						}
					}
					i++;
				}
				if(languageError ==  false) {
					boolean filterUpdated = false;
					int errorCount = 0;
					for(String language : filter_lang) {
						if(Azrael.SQLInsertChannel_Filter(channel_id, language) > 0) {
							filterUpdated = true;
							logger.debug("{} has set the channel filter {} for channel {} in guild {}", e.getMember().getUser().getId(), language, channel_id, e.getGuild().getId());
						}
						else {
							errorCount++;
							logger.error("channel filter {} couldn't be updated for the channel {} in guild {}", language, channel_id, e.getGuild().getId());
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_CENSOR_ERR).replace("{}", language)).build()).queue();
						}
					}
					if(filterUpdated && errorCount != filter_lang.size())
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_CENSOR_ADDED)).build()).queue();
					
				}
				else{
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_CENSOR_VALID_LANG)).build()).queue();
				}
			}
			else{
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_TEXT_CHANNEL)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
	}
}
