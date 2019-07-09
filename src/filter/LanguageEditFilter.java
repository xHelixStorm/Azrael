package filter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import core.Hashes;
import core.UserPrivs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.DiscordRoles;
import sql.Azrael;
import util.CharacterReplacer;

public class LanguageEditFilter extends ListenerAdapter implements Runnable{
	
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
				Logger logger = LoggerFactory.getLogger(LanguageFilter.class);
				logger.debug("Edited message removed from {} in guild {}", e.getMember().getUser().getId(), e.getGuild().getName());
				var cache = Hashes.getTempCache("report_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
				
				if(cache == null || cache.getExpiration() - System.currentTimeMillis() <= 0) {
					e.getTextChannel().sendMessage(e.getMember().getAsMention()+" "+output[0]).queue();
					Hashes.addTempCache("report_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId(), new Cache(300000, "1"));
				}
				else if(cache != null) {
					if(cache.getAdditionalInfo().equals("1")) {
						e.getTextChannel().sendMessage(e.getMember().getAsMention()+" "+output[1]).queue();
						Hashes.addTempCache("report_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId(), new Cache(300000, "2"));
					}
					else if(cache.getAdditionalInfo().equals("2")) {
						e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(DiscordRoles.SQLgetRole(e.getGuild().getIdLong(), "mut"))).queue();
						Hashes.clearTempCache("report_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
					}
				}
			}
		}
	}
}
