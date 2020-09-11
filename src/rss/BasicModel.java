package rss;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiParser;

import constructors.RSS;
import core.Hashes;
import enums.Translation;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import util.STATIC;

public class BasicModel {
	private static final Logger logger = LoggerFactory.getLogger(BasicModel.class);

	public static void ModelParse(BufferedReader in, Guild guild, RSS rss, long rss_channel, boolean defaultChannel) throws IOException {
		final TextChannel textChannel = guild.getTextChannelById(rss_channel);
		if(textChannel != null) {
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
				MessageHistory history = new MessageHistory(textChannel);
				history.retrievePast(100).queue(historyList -> {
					Message historyMessage = historyList.parallelStream().filter(f -> f.getContentRaw().equals(outMessage)).findAny().orElse(null);
					if(historyMessage == null)
						textChannel.sendMessage(outMessage).queue();
				});
			}
		}
		else {
			//remove not anymore existing channel
			if(Azrael.SQLDeleteChannelConf(rss_channel, guild.getIdLong()) > 0) {
				Azrael.SQLDeleteChannel_Filter(rss_channel);
				Azrael.SQLDeleteChannels(rss_channel);
				if(defaultChannel) {
					logger.info("Not existing subscription channel {} has been removed for guild {}", rss_channel, guild.getIdLong());
				}
				else if(Azrael.SQLUpdateRSSChannel(rss.getURL(), guild.getIdLong(), 0) > 0) {
					logger.info("Not existing alternative subscription channel {} has been removed for guild {}", rss_channel, guild.getIdLong());
				}
				Hashes.removeFilterLang(rss_channel);
				Hashes.removeChannels(guild.getIdLong());
			}
			else if(Azrael.SQLUpdateRSSChannel(rss.getURL(), guild.getIdLong(), 0) > 0) {
				logger.info("Not existing alternative subscription channel {} has been removed for guild {}", rss_channel, guild.getIdLong());
			}
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
