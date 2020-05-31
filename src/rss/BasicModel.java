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
import enums.Translation;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import util.STATIC;

public class BasicModel {
	private static final Logger logger = LoggerFactory.getLogger(BasicModel.class);

	public static void ModelParse(BufferedReader in, ReadyEvent e, RSS rss, long guild_id, Channels rss_channel) throws IOException {
		String format = rss.getFormat();
		String title = "";
		String description = "";
		String pubDate = "";
		String link = "";
		
		String line;
		StringBuilder collection = new StringBuilder();
		while((line = in.readLine()) != null) {
			collection.append(line);
		}
		String code = collection.toString();
		if(code.contains("<item>")) {
			code = code.substring(code.indexOf("<item>"), code.indexOf("</item>")).replaceAll("(<item>|</item>)", "");
			if(code.contains("<title>") && code.contains("</title>")) {
				int firstPos = code.indexOf("<title>");
				int lastPos = code.indexOf("</title>");
				title = code.substring(firstPos, lastPos).replaceAll("(<title>|</title>)", "");
			}
			if(code.contains("<description>") && code.contains("</description>")) {
				int firstPos = code.indexOf("<description>");
				int lastPos = code.indexOf("</description>");
				description = code.substring(firstPos, lastPos).replaceAll("(<description>|</descrption>)", "");
			}
			if(code.contains("<pubDate>") && code.contains("</pubDate>")) {
				int firstPos = code.indexOf("<pubDate>");
				int lastPos = code.indexOf("</pubDate>");
				pubDate = code.substring(firstPos, lastPos).replaceAll("(<pubDate>|</pubDate>)", "");
			}
			if(code.contains("<link>") && code.contains("</link>")) {
				int firstPos = code.indexOf("<link>");
				int lastPos = code.indexOf("</link>");
				link = code.substring(firstPos, lastPos).replaceAll("(<link>|</link>)", "");
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
			
			String title = "";
			String description = "";
			String pubDate = "";
			String link = "";
			
			String line;
			StringBuilder content = new StringBuilder();
			while((line = in.readLine()) != null) {
				content.append(line);
			}
			String code = content.toString();
			if(code.contains("<item>")) {
				code = code.substring(code.indexOf("<item>"), code.indexOf("</item>")).replaceAll("(<item>|</item>)", "");
				if(code.contains("<title>") && code.contains("</title>")) {
					int firstPos = code.indexOf("<title>");
					int lastPos = code.indexOf("</title>");
					title = code.substring(firstPos, lastPos).replaceAll("(<title>|</title>)", "");
				}
				if(code.contains("<description>") && code.contains("</description>")) {
					int firstPos = code.indexOf("<description>");
					int lastPos = code.indexOf("</description>");
					description = code.substring(firstPos, lastPos).replaceAll("(<description>|</descrption>)", "");
				}
				if(code.contains("<pubDate>") && code.contains("</pubDate>")) {
					int firstPos = code.indexOf("<pubDate>");
					int lastPos = code.indexOf("</pubDate>");
					pubDate = code.substring(firstPos, lastPos).replaceAll("(<pubDate>|</pubDate>)", "");
				}
				if(code.contains("<link>") && code.contains("</link>")) {
					int firstPos = code.indexOf("<link>");
					int lastPos = code.indexOf("</link>");
					link = code.substring(firstPos, lastPos).replaceAll("(<link>|</link>)", "");
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
				e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.RSS_NO_FEED)).queue();
			}
			in.close();
		} catch (IOException e1) {
			logger.error("Error on retrieving feed!", e1);
		}
	}
}
