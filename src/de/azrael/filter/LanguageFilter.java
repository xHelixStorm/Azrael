package de.azrael.filter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Cache;
import de.azrael.constructors.Channels;
import de.azrael.core.Hashes;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Channel;
import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.sql.DiscordRoles;
import de.azrael.util.CharacterReplacer;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;

public class LanguageFilter implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(LanguageFilter.class);
	private final static EmbedBuilder embed = new EmbedBuilder().setColor(Color.ORANGE);
	
	private Message message;
	private ArrayList<String> filter_lang;
	private List<Channels> allChannels;
	
	public LanguageFilter(Message _message, ArrayList<String> _filter_lang, List<Channels> _allChannels) {
		this.message = _message;
		this.filter_lang = _filter_lang;
		this.allChannels = _allChannels;
	}

	@Override
	public void run() {
		if(!message.getMember().getUser().isBot() && !UserPrivs.isUserMod(message.getMember()) && !UserPrivs.isUserAdmin(message.getMember())) {
			boolean exceptionFound = false;
			String [] output = new String[2];
			
			output[0] = STATIC.getTranslation(message.getMember(), Translation.CENSOR_REMOVED_WARN_1);
			output[1] = STATIC.getTranslation(message.getMember(), Translation.CENSOR_REMOVED_WARN_2);
			
			String getMessage = message.getContentRaw();
			String channel = message.getChannel().getName();
			String thisMessage = CharacterReplacer.replace(getMessage, filter_lang).trim();
			String name = message.getMember().getUser().getName()+"#"+message.getMember().getUser().getDiscriminator()+" ("+message.getMember().getUser().getId()+")";
			
			final var parseMessage = thisMessage.toLowerCase();
			
			for(String exception : CharacterReplacer.getExceptions()) {
				if(parseMessage.matches("(.|\\s){0,}\\b"+exception+"\\b(.|\\s){0,}")) {
					exceptionFound = true;
				}
			}
			
			if(exceptionFound == false) {
				var blockHeavyCensor = false;
				for(String filter : filter_lang) {
					Optional<String> option = Azrael.SQLgetFilter(filter, message.getGuild().getIdLong()).parallelStream()
						.filter(word -> parseMessage.matches("(.|\\s){0,}\\b"+word+"\\b(.|\\s){0,}")).findAny();
					if(option.isPresent()) {
						Hashes.addTempCache("message-removed-filter_gu"+message.getGuild().getId()+"ch"+message.getChannel().getId()+"us"+message.getMember().getUser().getId(), new Cache(10000));
						message.delete().reason("Message removed due to bad manner!").queue(success -> {}, error -> {
							logger.warn("Message {} already removed in guild {}", message.getId(), message.getGuild().getId());
						});
						STATIC.handleRemovedMessages(message.getMember(), message.getTextChannel(), output);
						var tra_channel = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.TRA.getType())).findAny().orElse(null);
						if(tra_channel != null) {
							final TextChannel textChannel = message.getGuild().getTextChannelById(tra_channel.getChannel_ID());
							if(textChannel != null && (message.getGuild().getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(message.getGuild(), textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)))) {
								Matcher matcher = Pattern.compile("[\\w\\d]*").matcher(getMessage);
								while(matcher.find()) {
									var word = matcher.group();
									var convertedWord = CharacterReplacer.replace(word, filter_lang);
									if(convertedWord.equalsIgnoreCase(option.get())) {
										getMessage = getMessage.replace(word, "**__"+word+"__**");
										break;
									}
								}
								embed.setTitle(name);
								embed.setFooter(channel + "("+message.getChannel().getId()+")").setThumbnail(message.getMember().getUser().getEffectiveAvatarUrl());
								final String printMessage = STATIC.getTranslation(message.getMember(), Translation.CENSOR_TITLE_DETECTED).replaceFirst("\\{\\}", option.get()).replace("{}", filter)+"\n\n"+getMessage;
								textChannel.sendMessage(embed.setDescription((printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"...")).build()).queue();
							}
						}
						break;
					}
					else if(!blockHeavyCensor) {
						blockHeavyCensor = true;
						var heavyCensoring = Hashes.getHeavyCensoring(message.getGuild().getIdLong());
						if(heavyCensoring != null && heavyCensoring) {
							var messageDeleted = false;
							var censorMessage = Hashes.getCensorMessage(message.getGuild().getIdLong());
							if(parseMessage.length() == 1 || (censorMessage != null && censorMessage.contains(parseMessage)) || parseMessage.matches("^[^a-zA-Z0-9]*$")) {
								deleteHeavyCensoringMessage(message, allChannels, name, channel, getMessage);
								messageDeleted = true;
							}
							else if(message.getAttachments().size() > 0) {
								deleteHeavyCensoringMessage(message, allChannels, name, channel, getMessage);
								messageDeleted = true;
							}
							else {
								var splitWords = parseMessage.split(" ");
								if(splitWords.length > 9) {
									var count = 0;
									var searchWord = splitWords[0];
									for(final var word : splitWords) {
										if(word.equals(searchWord))
											count++;
										else
											break;
										if(count == 9) {
											deleteHeavyCensoringMessage(message, allChannels, name, channel, getMessage);
											messageDeleted = true;
										}
									}
								}
							}
							if(messageDeleted) {
								var threshold = Hashes.getFilterThreshold(message.getGuild().getIdLong());
								if(threshold != null) {
									var count = Integer.parseInt(threshold);
									if(++count == 30) {
										STATIC.writeToRemoteChannel(message.getGuild(), null, STATIC.getTranslation(message.getMember(), Translation.HEAVY_CENSORING_HARD), Channel.LOG.getType());
									}
									if(count >= 30) {
										var mute_role = DiscordRoles.SQLgetRoles(message.getGuild().getIdLong()).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null);
										if(mute_role != null) {
											Azrael.SQLInsertHistory(message.getMember().getUser().getIdLong(), message.getGuild().getIdLong(), "mute", STATIC.getTranslation2(message.getGuild(), Translation.HEAVY_CENSORING_REASON), 0, "");
											message.getGuild().addRoleToMember(message.getMember(), message.getGuild().getRoleById(mute_role.getRole_ID())).reason(STATIC.getTranslation2(message.getGuild(), Translation.HEAVY_CENSORING_REASON)).queue();
										}
									}
									if(count <= 99)
										Hashes.addFilterThreshold(message.getGuild().getIdLong(), ""+count);
								}
								else if(threshold == null) {
									Hashes.addFilterThreshold(message.getGuild().getIdLong(), ""+1);
								}
								break;
							}
						}
					}
				}
			}
		}
	}
	
	private static void deleteHeavyCensoringMessage(Message message, List<Channels> allChannels, String name, String channel, String getMessage) {
		Hashes.addTempCache("message-removed-filter_gu"+message.getGuild().getId()+"ch"+message.getChannel().getId()+"us"+message.getMember().getUser().getId(), new Cache(10000));
		message.delete().reason("Message removed due to heavy censoring!").queue();
		StringBuilder out = new StringBuilder();
		for(final Attachment attachment : message.getAttachments()) {
			out.append(attachment.getProxyUrl()+"\n");
		}
		EmbedBuilder embed = new EmbedBuilder().setColor(Color.ORANGE).setTitle(message.getMember().getUser().getName()+"#"+message.getMember().getUser().getDiscriminator()+" ("+message.getMember().getUser().getId()+")").setFooter(channel+" ("+message.getChannel().getId()+")").setThumbnail(message.getMember().getUser().getEffectiveAvatarUrl());
		final String printMessage = STATIC.getTranslation2(message.getGuild(), Translation.HEAVY_CENSORING_DELETED)+"\n\n"+getMessage+"\n"+out.toString();
		STATIC.writeToRemoteChannel(message.getGuild(), embed, (printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"..."), Channel.TRA.getType());
	}
}
