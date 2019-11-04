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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import sql.Azrael;
import sql.DiscordRoles;
import util.CharacterReplacer;
import util.STATIC;

public class LanguageEditFilter implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(LanguageEditFilter.class);
	private final static EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setTitle("Message removed after edit!");
	
	private GuildMessageUpdateEvent e;
	private ArrayList<String> filter_lang;
	private List<Channels> allChannels;
	
	public LanguageEditFilter(GuildMessageUpdateEvent event, ArrayList<String> _filter_lang, List<Channels> _allChannels) {
		this.e = event;
		this.filter_lang = _filter_lang;
		this.allChannels = _allChannels;
	}

	@SuppressWarnings("preview")
	@Override
	public void run() {
		if(!UserPrivs.isUserBot(e.getMember()) && !UserPrivs.isUserMod(e.getMember()) && !UserPrivs.isUserAdmin(e.getMember())) {
			boolean exceptionFound = false;
			String [] output = new String[2];
			
			if(filter_lang.size() == 1) {
				switch(filter_lang.get(0)) {
					case "ger" -> {
						output[0] = " Die Nachricht wurde wegen schlechten Benehmens entfernt!";
						output[1] = " Dies ist deine zweite Warnung. Eine weitere entfernte Nachricht und du wirst auf diesem Server **stumm geschaltet**!";
					}
					case "fre" -> {
						output[0] = " Votre message à été supprimé pour mauvais comportement !";
						output[1] = " C'est votre deuxième avertissement. Encore une fois et vous serez **mis sous silence** sur le serveur !";
					}
					default -> {
						output[0] = " Message has been removed due to bad behaviour!";
						output[1] = " This has been the second warning. One more and you'll be **muted** from the server!";
					}
				}
			}
			else{
				output[0] = " Message has been removed due to bad behaviour!";
				output[1] = " This has been the second warning. One more and you'll be **muted** from the server!";
			}
			
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
						STATIC.handleRemovedMessages(null, e, output);
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
							message.setTitle("Message removed! The word **"+option.get()+"** from filter **"+filter+"** has been detected!");
							final String printMessage = "Removed Message from **"+name+"** in **"+channel+"**\n"+getMessage;
							e.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(message.setDescription((printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"...")).build()).queue();
						}
						break;
					}
					else if(!blockHeavyCensor) {
						blockHeavyCensor = true;
						var heavyCensoring = Hashes.getHeavyCensoring(e.getGuild().getIdLong());
						if(heavyCensoring != null && heavyCensoring) {
							var blockSaveMessage = false;
							var messageDeleted = false;
							var censorMessage = Hashes.getCensorMessage(e.getGuild().getIdLong());
							if(parseMessage.length() == 1 || (censorMessage != null && censorMessage.contains(parseMessage))) {
								deleteHeavyCensoringMessage(e, allChannels, name, channel, getMessage);
								messageDeleted = true;
								if(parseMessage.length() == 1)
									blockSaveMessage = true;
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
											blockSaveMessage = true;
										}
									}
								}
							}
							if(!blockSaveMessage) {
								if(censorMessage == null) {
									ArrayList<String> saveMessage = new ArrayList<String>();
									saveMessage.add(parseMessage);
									Hashes.addCensorMessage(e.getGuild().getIdLong(), saveMessage);
								}
								else {
									censorMessage.add(parseMessage);
									if(censorMessage.size() > 30)
										censorMessage.remove(0);
									Hashes.addCensorMessage(e.getGuild().getIdLong(), censorMessage);
								}
							}
							if(messageDeleted) {
								var threshold = Hashes.getFilterThreshold(e.getGuild().getIdLong());
								if(threshold != null) {
									var count = Integer.parseInt(threshold);
									if(++count == 30) {
										var log_channel = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
										if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage("Heavy censoring has reached the critical threshold! Everyone who will have his messages removed due to the heavy censoring will get muted!").queue();
									}
									if(count >= 30) {
										var mute_role = DiscordRoles.SQLgetRoles(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null);
										if(mute_role != null) {
											Azrael.SQLInsertHistory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "mute", "Heavy censoring mute after reaching the threshold", 0);
											e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(mute_role.getRole_ID())).queue();
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
	
	private static void deleteHeavyCensoringMessage(GuildMessageUpdateEvent e, List<Channels> allChannels, String name, String channel, String getMessage) {
		Hashes.addTempCache("message-removed-filter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(10000));
		e.getMessage().delete().reason("Message removed due to heavy censoring!").queue();
		var tra_channel = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("tra")).findAny().orElse(null);
		if(tra_channel != null) {
			message.setTitle("Message removed due to **heavy censoring**!");
			final String printMessage = "Removed Message from **"+name+"** in **"+channel+"**\n"+getMessage;
			e.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(message.setDescription((printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"...")).build()).queue();
		}
	}
}
