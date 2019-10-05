package filter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import constructors.Cache;
import constructors.Channels;
import core.Hashes;
import core.UserPrivs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import util.STATIC;
import util.CharacterReplacer;

public class LanguageFilter implements Runnable {
	private final static EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE);
	
	private GuildMessageReceivedEvent e;
	private ArrayList<String> filter_lang;
	private List<Channels> allChannels;
	
	public LanguageFilter(GuildMessageReceivedEvent event, ArrayList<String> _filter_lang, List<Channels> _allChannels) {
		this.e = event;
		this.filter_lang = _filter_lang;
		this.allChannels = _allChannels;
	}

	@SuppressWarnings("preview")
	@Override
	public void run() {
		if(!UserPrivs.isUserBot(e.getMember().getUser(), e.getGuild().getIdLong()) && !UserPrivs.isUserMod(e.getMember().getUser(), e.getGuild().getIdLong()) && !UserPrivs.isUserAdmin(e.getMember().getUser(), e.getGuild().getIdLong())) {
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
						e.getMessage().delete().reason("Message removed due to bad manner!").complete();
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
							message.setTitle("Message removed! The word **"+option.get()+"** from filter **"+filter+"** has been detected!");
							e.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(message.setDescription("Removed Message from **"+name+"** in **"+channel+"**\n"+getMessage).build()).queue();
						}
						break;
					}
					else if(!blockHeavyCensor) {
						blockHeavyCensor = true;
						var heavyCensoring = Hashes.getHeavyCensoring(e.getGuild().getIdLong());
						if(heavyCensoring != null && heavyCensoring) {
							var censorMessage = Hashes.getCensorMessage(e.getGuild().getIdLong());
							if(parseMessage.length() == 1 || !parseMessage.matches("(.|\\s){0,}[\\w\\d](.|\\s){0,}") || (censorMessage != null && censorMessage.contains(parseMessage))) {
								Hashes.addTempCache("message-removed-filter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(10000));
								e.getMessage().delete().reason("Message removed due to heavy censoring!").complete();
								var tra_channel = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("tra")).findAny().orElse(null);
								if(tra_channel != null) {
									message.setTitle("Message removed due to **heavy censoring**!");
									e.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(message.setDescription("Removed Message from **"+name+"** in **"+channel+"**\n"+getMessage).build()).queue();
								}
								if(censorMessage == null) {
									ArrayList<String> saveMessage = new ArrayList<String>();
									saveMessage.add(parseMessage);
									Hashes.addCensorMessage(e.getGuild().getIdLong(), saveMessage);
								}
								else {
									censorMessage.add(parseMessage);
									if(censorMessage.size() > 10)
										censorMessage.remove(0);
									Hashes.addCensorMessage(e.getGuild().getIdLong(), censorMessage);
								}
								break;
							}
						}
					}
				}
			}
		}
	}
}
