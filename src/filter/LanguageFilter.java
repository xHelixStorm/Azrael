package filter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import constructors.Channels;
import core.Hashes;
import core.UserPrivs;
import enums.Translation;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import sql.DiscordRoles;
import util.STATIC;
import util.CharacterReplacer;

public class LanguageFilter implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(LanguageFilter.class);
	private final static EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE);
	
	private GuildMessageReceivedEvent e;
	private ArrayList<String> filter_lang;
	private List<Channels> allChannels;
	
	public LanguageFilter(GuildMessageReceivedEvent event, ArrayList<String> _filter_lang, List<Channels> _allChannels) {
		this.e = event;
		this.filter_lang = _filter_lang;
		this.allChannels = _allChannels;
	}

	@Override
	public void run() {
		if(!UserPrivs.isUserBot(e.getMember()) && !UserPrivs.isUserMod(e.getMember()) && !UserPrivs.isUserAdmin(e.getMember())) {
			boolean exceptionFound = false;
			String [] output = new String[2];
			
			output[0] = STATIC.getTranslation(e.getMember(), Translation.CENSOR_REMOVED_WARN_1);
			output[1] = STATIC.getTranslation(e.getMember(), Translation.CENSOR_REMOVED_WARN_2);
			
			String getMessage = e.getMessage().getContentRaw();
			String channel = e.getChannel().getName();
			String thisMessage = CharacterReplacer.replace(getMessage).trim();
			String name = e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+" ("+e.getMember().getUser().getId()+")";
			
			final var parseMessage = thisMessage.toLowerCase();
			
			for(String exception : CharacterReplacer.getExceptions()) {
				if(parseMessage.matches("(.|\\s){0,}\\b"+exception+"\\b(.|\\s){0,}")) {
					exceptionFound = true;
				}
			}
			
			if(exceptionFound == false) {
				var blockHeavyCensor = false;
				for(String filter : filter_lang) {
					Optional<String> option = Azrael.SQLgetFilter(filter, e.getGuild().getIdLong()).parallelStream()
						.filter(word -> parseMessage.matches("(.|\\s){0,}\\b"+word+"\\b(.|\\s){0,}")).findAny();
					if(option.isPresent()) {
						Hashes.addTempCache("message-removed-filter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(10000));
						e.getMessage().delete().reason("Message removed due to bad manner!").queue(success -> {}, error -> {
							logger.warn("Message already removed!");
						});
						STATIC.handleRemovedMessages(e, null, output);
						var tra_channel = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("tra")).findAny().orElse(null);
						if(tra_channel != null) {
							Matcher matcher = Pattern.compile("[\\w\\d]*").matcher(getMessage);
							while(matcher.find()) {
								var word = matcher.group();
								var convertedWord = CharacterReplacer.replace(word);
								if(convertedWord.equalsIgnoreCase(option.get())) {
									getMessage = getMessage.replace(word, "**__"+word+"__**");
									break;
								}
							}
							message.setTitle(STATIC.getTranslation(e.getMember(), Translation.CENSOR_TITLE_DETECTED).replaceFirst("\\{\\}", option.get()).replace("{}", filter));
							message.setFooter(channel + "("+e.getChannel().getId()+")").setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl());
							final String printMessage = getMessage;
							e.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(message.setDescription((printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"...")).build()).queue();
						}
						break;
					}
					else if(!blockHeavyCensor) {
						blockHeavyCensor = true;
						var heavyCensoring = Hashes.getHeavyCensoring(e.getGuild().getIdLong());
						if(heavyCensoring != null && heavyCensoring) {
							var messageDeleted = false;
							var censorMessage = Hashes.getCensorMessage(e.getGuild().getIdLong());
							if(parseMessage.length() == 1 || (censorMessage != null && censorMessage.contains(parseMessage))) {
								deleteHeavyCensoringMessage(e, allChannels, name, channel, getMessage);
								messageDeleted = true;
							}
							else {
								var splitWords = parseMessage.split(" ");
								if(splitWords.length >= 9) {
									var count = 0;
									var searchWord = splitWords[0];
									for(final var word : splitWords) {
										if(word.equals(searchWord))
											count++;
										else
											break;
										if(count == 9) {
											deleteHeavyCensoringMessage(e, allChannels, name, channel, getMessage);
											messageDeleted = true;
										}
									}
								}
							}
							if(messageDeleted) {
								var threshold = Hashes.getFilterThreshold(e.getGuild().getIdLong());
								if(threshold != null) {
									var count = Integer.parseInt(threshold);
									if(++count == 30) {
										var log_channel = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
										if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(STATIC.getTranslation(e.getMember(), Translation.HEAVY_CENSORING_HARD)).queue();
									}
									if(count >= 30) {
										var mute_role = DiscordRoles.SQLgetRoles(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null);
										if(mute_role != null) {
											Azrael.SQLInsertHistory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "mute", STATIC.getTranslation2(e.getGuild(), Translation.HEAVY_CENSORING_REASON), 0, "");
											e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(mute_role.getRole_ID())).reason(STATIC.getTranslation2(e.getGuild(), Translation.HEAVY_CENSORING_REASON)).queue();
										}
									}
									if(count <= 99)
										Hashes.addFilterThreshold(e.getGuild().getIdLong(), ""+count);
								}
								else if(threshold == null) {
									Hashes.addFilterThreshold(e.getGuild().getIdLong(), ""+1);
								}
								break;
							}
						}
					}
				}
			}
		}
	}
	
	private static void deleteHeavyCensoringMessage(GuildMessageReceivedEvent e, List<Channels> allChannels, String name, String channel, String getMessage) {
		Hashes.addTempCache("message-removed-filter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(10000));
		e.getMessage().delete().reason("Message removed due to heavy censoring!").queue();
		var tra_channel = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("tra")).findAny().orElse(null);
		if(tra_channel != null) {
			EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.HEAVY_CENSORING_DELETED)).setFooter(channel+" ("+e.getChannel().getId()+")").setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl());
			final String printMessage = getMessage;
			e.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(message.setDescription((printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"...")).build()).queue();
		}
	}
}
