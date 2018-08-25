package filter;

import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import core.UserPrivs;
import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.ServerRoles;
import sql.SqlConnect;
import threads.DelayDelete;
import util.CharacterReplacer;

public class LanguageFilter extends ListenerAdapter implements Runnable{
	
	private MessageReceivedEvent e;
	private ArrayList<String> filter_lang;
	
	public LanguageFilter(MessageReceivedEvent event, ArrayList<String> _filter_lang){
		e = event;
		filter_lang = _filter_lang;
	}

	@Override
	public void run() {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setTitle("Message removed!");
		boolean wordFound = false;
		boolean exceptionFound = false;
		
		if(!UserPrivs.isUserBot(e.getMember().getUser(), e.getGuild().getIdLong())){
			String getMessage = e.getMessage().getContentRaw();
			String channel = e.getTextChannel().getName();
			String parseMessage;
			String name = e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator();
			String filename = e.getMember().getUser().getId();
			String report;
			
			parseMessage = CharacterReplacer.replace(getMessage);
			parseMessage = parseMessage.toLowerCase();
			int letterCounter = parseMessage.length();
			
			for(String exceptions : CharacterReplacer.getExceptions()){
				if(parseMessage.equals(exceptions) || parseMessage.matches("[!\"$%&/()=?.@#^*+\\-={};':,<>]"+exceptions+"(?!\\w\\d\\s)") || parseMessage.matches("[!\"$%&�/()=?.@#^*+\\-={};':,<>\\w\\d\\s]*\\s" + exceptions + "(?!\\w\\d\\s)") || parseMessage.matches("[!\"$%&/()=?.@#^*+\\-={};':,<>\\w\\d\\s]*\\s" + exceptions + "[!\"$%&/()=?.@#^*+\\-={};':,<>]") || parseMessage.matches(exceptions+"\\s[!\"$%&/()=?.@#^*+\\-={};':,<>\\w\\d\\s]*") || parseMessage.matches("[!\"$%&/()=?.@#^*+\\-={};':,<>]"+exceptions+"\\s[!\"$%&/()=?.@#^*+\\-={};':,<>\\w\\d\\s]*") || parseMessage.contains(" "+exceptions+" ")){
					exceptionFound = true;
				}
			}
			
			if(exceptionFound == false){
				find: for(String filter : filter_lang){
					SqlConnect.SQLgetFilter(filter, e.getGuild().getIdLong());
					for(String word : SqlConnect.getFilter_Words()){
						if(wordFound == false && letterCounter > 1){
							if(parseMessage.equals(word) || parseMessage.matches("[!\"$%&/()=?.@#^*+\\-={};':,<>]"+word+"(?!\\w\\d\\s)") || parseMessage.matches("[!\"$%&�/()=?.@#^*+\\-={};':,<>\\w\\d\\s]*\\s" + word + "(?!\\w\\d\\s)") || parseMessage.matches("[!\"$%&/()=?.@#^*+\\-={};':,<>\\w\\d\\s]*\\s" + word + "[!\"$%&/()=?.@#^*+\\-={};':,<>]") || parseMessage.matches(word+"\\s[!\"$%&/()=?.@#^*+\\-={};':,<>\\w\\d\\s]*") || parseMessage.matches("[!\"$%&/()=?.@#^*+\\-={};':,<>]"+word+"\\s[!\"$%&/()=?.@#^*+\\-={};':,<>\\w\\d\\s]*") || parseMessage.contains(" "+word+" ")){
								e.getMessage().delete().reason("Message removed due to bad manner!").queue();
								long guild_id = e.getGuild().getIdLong();
								SqlConnect.SQLgetChannelID(guild_id, "tra");
								long channel_id = SqlConnect.getChannelID();
								if(channel_id != 0){e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription("Removed Message from **"+name+"** in **"+channel+"**\n"+getMessage).build()).queue();}
								wordFound = true;
								break find;
							}
						}	
					}
					SqlConnect.clearFilter_Words();
				}
			}
			SqlConnect.clearFilter_Lang();
			
			if(wordFound == true){
				Path path = Paths.get(IniFileReader.getTempDirectory()+"Reports/"+filename.toString()+".azr");
				
				if(Files.notExists(path)){
					e.getTextChannel().sendMessage(e.getMember().getAsMention()+" Message has been removed due to bad behaviour!").queue();
					FileSetting.createFile(IniFileReader.getTempDirectory()+"Reports/"+filename.toString()+".azr", "1");
					new Thread(new DelayDelete(IniFileReader.getTempDirectory()+"Reports/"+filename.toString()+".azr", 300000)).start();
				}
				else if (Files.exists(path)){
					report = FileSetting.readFile(IniFileReader.getTempDirectory()+"Reports/"+filename.toString()+".azr");
					if(report.contains("1")){
						e.getTextChannel().sendMessage(e.getMember().getAsMention()+" This has been the second warning. One more and you'll be **muted** from the server!").queue();
						FileSetting.createFile(IniFileReader.getTempDirectory()+"Reports/"+filename.toString()+".azr", "2");
					}
					else if (report.contains("2")){
						FileSetting.deleteFile(IniFileReader.getTempDirectory()+"Reports/"+filename.toString()+".azr");
						ServerRoles.SQLgetRole(e.getGuild().getIdLong(), "mut");
						long mute_role = ServerRoles.getRole_ID();
						e.getGuild().getController().addRolesToMember(e.getMember(), e.getGuild().getRoleById(mute_role)).queue();
					}
				}
			}
		}
	}
}
