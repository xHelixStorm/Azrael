package rss;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiParser;

import constructors.Channels;
import constructors.RSS;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class BasicModel {
	private static final Logger logger = LoggerFactory.getLogger(BasicModel.class);

	public static void ModelParse(BufferedReader in, ReadyEvent e, RSS rss, long guild_id, Channels rss_channel) throws IOException {
		String format = rss.getFormat();
		boolean itemTagFound = false;
		String title = "";
		String description = "";
		String pubDate = "";
		String link = "";
		
		String line;
		while((line = in.readLine()) != null) {
			if(line.startsWith("<item>"))
				itemTagFound = true;
			else if(line.endsWith("</item>"))
				break;
			if(itemTagFound == true) {
				if(line.contains("<title>") && line.contains("</title>")) {
					int firstPos = line.indexOf("<title>");
					int lastPos = line.indexOf("</title>");
					title = line.substring(firstPos, lastPos).replaceAll("(<title>|</title>)", "");
				}
				if(line.contains("<description>") && line.contains("</description>")) {
					int firstPos = line.indexOf("<description>");
					int lastPos = line.indexOf("</description>");
					description = line.substring(firstPos, lastPos).replaceAll("(<description>|</descrption>)", "");
				}
				if(line.contains("<pubDate>") && line.contains("</pubDate>")) {
					int firstPos = line.indexOf("<pubDate>");
					int lastPos = line.indexOf("</pubDate>");
					pubDate = line.substring(firstPos, lastPos).replaceAll("(<pubDate>|</pubDate>)", "");
				}
				if(line.contains("<link>") && line.contains("</link>")) {
					int firstPos = line.indexOf("<link>");
					int lastPos = line.indexOf("</link>");
					link = line.substring(firstPos, lastPos).replaceAll("(<link>|</link>)", "");
				}
			}
		}
		if(title.length() > 0 || description.length() > 0 || pubDate.length() > 0 || link.length() > 0) {
			String out = format.replace("{title}", title);
			out = out.replace("{description}", description);
			out = out.replace("{pubDate}", pubDate);
			out = out.replace("{link}", link);
			out = out.replaceAll("&#039;", "'");
			final String outMessage = EmojiParser.parseToUnicode(out);
			MessageHistory history = new MessageHistory(e.getJDA().getGuildById(guild_id).getTextChannelById(rss_channel.getChannel_ID()));
			List<Message> msg = history.retrievePast(30).complete();
			Message historyMessage = msg.parallelStream().filter(f -> f.getContentRaw().equals(outMessage)).findAny().orElse(null);
			if(historyMessage == null)
				e.getJDA().getGuildById(guild_id).getTextChannelById(rss_channel.getChannel_ID()).sendMessage(outMessage).queue();
		}
		in.close();
	}
	
	public static void ModelTest(GuildMessageReceivedEvent e, RSS rss) {
		try {
			String format = rss.getFormat();
			URL rssUrl = new URL(rss.getURL());
			BufferedReader in = new BufferedReader(new InputStreamReader(rssUrl.openStream()));
			
			boolean itemTagFound = false;
			String title = "";
			String description = "";
			String pubDate = "";
			String link = "";
			
			String line;
			while((line = in.readLine()) != null) {
				if(line.startsWith("<item>"))
					itemTagFound = true;
				else if(line.endsWith("</item>"))
					break;
				if(itemTagFound == true) {
					if(line.contains("<title>") && line.contains("</title>")) {
						int firstPos = line.indexOf("<title>");
						int lastPos = line.indexOf("</title>");
						title = line.substring(firstPos, lastPos).replaceAll("(<title>|</title>)", "");
					}
					if(line.contains("<description>") && line.contains("</description>")) {
						int firstPos = line.indexOf("<description>");
						int lastPos = line.indexOf("</description>");
						description = line.substring(firstPos, lastPos).replaceAll("(<description>|</descrption>)", "");
					}
					if(line.contains("<pubDate>") && line.contains("</pubDate>")) {
						int firstPos = line.indexOf("<pubDate>");
						int lastPos = line.indexOf("</pubDate>");
						pubDate = line.substring(firstPos, lastPos).replaceAll("(<pubDate>|</pubDate>)", "");
					}
					if(line.contains("<link>") && line.contains("</link>")) {
						int firstPos = line.indexOf("<link>");
						int lastPos = line.indexOf("</link>");
						link = line.substring(firstPos, lastPos).replaceAll("(<link>|</link>)", "");
					}
				}
			}
			if(title.length() > 0 || description.length() > 0 || pubDate.length() > 0 || link.length() > 0) {
				String out = format.replace("{title}", title);
				out = out.replace("{description}", description);
				out = out.replace("{pubDate}", pubDate);
				out = out.replace("{link}", link);
				out = out.replaceAll("&#039;", "'");
				final String outMessage = EmojiParser.parseToUnicode(out);
				e.getChannel().sendMessage(outMessage).queue();
			}
			else {
				e.getChannel().sendMessage("No feed could be found").queue();
			}
			in.close();
		} catch (IOException e1) {
			logger.error("Error on retrieving feed!", e1);
		}
	}
}
