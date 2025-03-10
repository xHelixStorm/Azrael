package de.azrael.commands.util;

import java.awt.Color;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.util.Hashes;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Extension of the set command
 * @author xHelixStorm
 *
 */

public class SetChannelFilter {
	private final static Logger logger = LoggerFactory.getLogger(SetChannelFilter.class);
	
	public static void runTask(MessageReceivedEvent e, String [] args) {
		if(args.length == 3) {
			args[2] = args[2].replaceAll("[^0-9]*", "");
			if(args[2].matches("[0-9]{1,}")) {
				TextChannel textChannel = e.getGuild().getTextChannelById(args[2]);
				if(textChannel != null) {
					final var languages = Azrael.SQLgetFilterLanguages();
					if(languages.size() > 0) {
						ArrayList<String> filterLanguages = new ArrayList<String>();
						ArrayList<String> errorLanguages = new ArrayList<String>();
						final String [] enteredLangs = args[1].split(",");
						boolean addLanguages = true;
						for(String lang : enteredLangs) {
							if(lang.equals("all")) {
								filterLanguages = languages;
								addLanguages = false;
							}
							if(languages.contains(lang)) {
								if(addLanguages)
									filterLanguages.add(lang);
							}
							else
								errorLanguages.add(lang);
						}
						if(errorLanguages.size() > 0) {
							StringBuilder out = new StringBuilder();
							for(String lang: errorLanguages) {
								if(out.length() > 0)
									out.append(", ");
								out.append("**"+lang+"**");
							}
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_CENSOR_ERR).replace("{}", out.toString())).build()).queue();
							return;
						}
						if(filterLanguages.size() > 0) {
							if(Azrael.SQLInsertChannel_Filter(textChannel.getIdLong(), filterLanguages) > 0) {
								final var channelConf = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_ID() == textChannel.getIdLong()).findAny().orElse(null);
								if(channelConf == null) {
									Azrael.SQLInsertChannel_Conf(textChannel.getIdLong(), e.getGuild().getIdLong(), (filterLanguages.contains("all") ? "all" : filterLanguages.get(0)));
								}
								e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_CENSOR_ADDED)).build()).queue();
								logger.info("Filter languages {} have been registered for text channel {} in guild {}", filterLanguages, textChannel.getId(), e.getGuild().getId());
								Hashes.removeChannels(e.getGuild().getIdLong());
							}
							else {
								e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("Filter languages couldn't be set for text channel {} in guild {}", textChannel.getId(), e.getGuild().getId());
							}
						}
						else{
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_CENSOR_VALID_LANG)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Available languages couldn't be retrieved in guild {}", e.getGuild().getId());
					}
				}
				else{
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TEXT_CHANNEL_NOT_EXISTS)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_TEXT_CHANNEL)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
	}
}
