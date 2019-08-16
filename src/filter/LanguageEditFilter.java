package filter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Optional;

import core.Hashes;
import core.UserPrivs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import sql.Azrael;
import util.CharacterReplacer;
import util.STATIC;

public class LanguageEditFilter implements Runnable {
	
	private MessageUpdateEvent e;
	private ArrayList<String> filter_lang;
	
	public LanguageEditFilter(MessageUpdateEvent event, ArrayList<String> _filter_lang) {
		e = event;
		filter_lang = _filter_lang;
	}

	@SuppressWarnings("preview")
	@Override
	public void run() {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setTitle("Message removed after edit!");
		boolean wordFound = false;
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
		
		if(!UserPrivs.isUserBot(e.getMember().getUser(), e.getGuild().getIdLong())) {
			String getMessage = e.getMessage().getContentRaw();
			String channel = e.getTextChannel().getName();
			String thisMessage;
			String name = e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator();
			
			thisMessage = CharacterReplacer.replace(getMessage);
			final var parseMessage = thisMessage.toLowerCase();
			int letterCounter = parseMessage.length();
			
			for(String exceptions : CharacterReplacer.getExceptions()) {
				if(parseMessage.equals(exceptions) || parseMessage.matches("[!\"$%&/()=?.@#^*+\\-={};':,<>]"+exceptions+"(?!\\w\\d\\s)") || parseMessage.matches("[!\"$%&�/()=?.@#^*+\\-={};':,<>\\w\\d\\s]*\\s" + exceptions + "(?!\\w\\d\\s)") || parseMessage.matches("[!\"$%&/()=?.@#^*+\\-={};':,<>\\w\\d\\s]*\\s" + exceptions + "[!\"$%&/()=?.@#^*+\\-={};':,<>]") || parseMessage.matches(exceptions+"\\s[!\"$%&/()=?.@#^*+\\-={};':,<>\\w\\d\\s]*") || parseMessage.matches("[!\"$%&/()=?.@#^*+\\-={};':,<>]"+exceptions+"\\s[!\"$%&/()=?.@#^*+\\-={};':,<>\\w\\d\\s]*") || parseMessage.contains(" "+exceptions+" ")) {
					exceptionFound = true;
				}
			}
			
			if(exceptionFound == false) {
				find: for(String filter : filter_lang) {
					Azrael.SQLgetFilter(filter, e.getGuild().getIdLong());
					if(wordFound == false && letterCounter > 1) {
						Optional<String> option = Hashes.getQuerryResult(filter+"_"+e.getGuild().getIdLong()).parallelStream()
							.filter(word -> parseMessage.equals(word) || parseMessage.matches("[!\"$%&/()=?.@#^*+\\-={};':,<>]"+word+"(?!\\w\\d\\s)") || parseMessage.matches("[!\"$%&�/()=?.@#^*+\\-={};':,<>\\w\\d\\s]*\\s" + word + "(?!\\w\\d\\s)") || parseMessage.matches("[!\"$%&/()=?.@#^*+\\-={};':,<>\\w\\d\\s]*\\s" + word + "[!\"$%&/()=?.@#^*+\\-={};':,<>]") || parseMessage.matches(word+"\\s[!\"$%&/()=?.@#^*+\\-={};':,<>\\w\\d\\s]*") || parseMessage.matches("[!\"$%&/()=?.@#^*+\\-={};':,<>]"+word+"\\s[!\"$%&/()=?.@#^*+\\-={};':,<>\\w\\d\\s]*") || parseMessage.contains(" "+word+" "))
							.findAny();
						if(option.isPresent()) {
							e.getMessage().delete().reason("Message removed due to bad manner!").complete();
							var tra_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type().equals("tra")).findAny().orElse(null);
							if(tra_channel != null) {e.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(message.setDescription("Removed Message from **"+name+"** in **"+channel+"**\n"+getMessage).build()).queue();}
							wordFound = true;
							break find;
						}
					}
				}
			}
			
			if(wordFound == true) {
				STATIC.handleRemovedMessages(null, e, output);
			}
		}
	}
}
