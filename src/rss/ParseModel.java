package rss;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vdurmont.emoji.EmojiParser;

import constructors.Channels;
import constructors.RSS;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.ReadyEvent;

public class ParseModel {
	
	public static void BasicModelParse(BufferedReader in, ReadyEvent e, RSS rss, long guild_id, Channels rss_channel) throws IOException {
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
	}
	
	public static void TwitterModelParse(BufferedReader in, ReadyEvent e, RSS rss, long guild_id, Channels rss_channel) throws IOException {
		String format = rss.getFormat();
		boolean itemTagFound = false;
		boolean redo = false;
		int startPosition = 0;
		int endPosition = 0;
		String title = "";
		String description = "";
		String pubDate = "";
		String link = "";
		
		String tempMessage = "";
		String message = "";
		String line;
		while((line = in.readLine()) != null) {
			do {
				if(redo)
					line = line.substring((tempMessage+"</item>").length());
				redo = false;
				if(!itemTagFound && line.contains("<item>")) {
					itemTagFound = true;
					startPosition = line.indexOf("<item>");
					message = line.substring(startPosition);
				}
				if(itemTagFound && line.contains("</item>")) {
					endPosition = line.indexOf("</item>");
					tempMessage = line.substring(startPosition, endPosition);
					message += tempMessage;
					//break;
					if(message.contains("<title>") && message.contains("</title>")) {
						int firstPos = message.indexOf("<title>");
						int lastPos = message.indexOf("</title>");
						title = message.substring(firstPos, lastPos).replaceAll("(<title>|</title>)", "");
						title = title.substring(9, title.length()-3);
					}
					if(message.contains("<link>") && message.contains("</link>")) {
						int firstPos = message.indexOf("<link>");
						int lastPos = message.indexOf("</link>");
						link = message.substring(firstPos, lastPos).replaceAll("(<link>|</link>)", "");
					}
					if(message.contains("<description>") && message.contains("</description>")) {
						int firstPos = message.indexOf("<description>");
						int lastPos = message.indexOf("</description>");
						description = message.substring(firstPos, lastPos).replaceAll("(<description>|</description>)", "");
						description = description.substring(9, description.length()-3);
						description = description.replaceAll("(<blockquote[\\w\\d\\s=\"-]*>|<\\/blockquote>|<p[\\s\\w\\d=\"]*>|<a[\\s\\w\\d=\":\\/.?&%]*(%22%3E|>)|<\\/a>|&mdash; ?)", "");
						description = description.replaceAll("(<br>|</p>)", "\n");
						if(description.contains("pic.twitter.com/")) {
							URL url = new URL(link);
							URLConnection con = url.openConnection();
							con.setConnectTimeout(5000);
							con.setReadTimeout(10000);
							BufferedReader stream = new BufferedReader(new InputStreamReader(con.getInputStream()));
							Pattern patternURL = Pattern.compile("https:\\/\\/twitter.com[\\w\\d\\/]*");
							Matcher matcher = null;
							String redirectURL = null;
							String currentLine;
							while((currentLine = stream.readLine()) != null) {
								matcher = patternURL.matcher(currentLine);
								if(matcher.find()) {
									redirectURL = matcher.group();
								}
							}
							url = new URL(redirectURL);
							con = url.openConnection();
							con.setConnectTimeout(5000);
							con.setReadTimeout(10000);
							stream = new BufferedReader(new InputStreamReader(con.getInputStream()));
							patternURL = Pattern.compile("https:\\/\\/t.co/[\\w\\d]*");
							boolean url1Found = false;
							String url1 = null;
							String url2 = null;
							String streamLine;
							while((streamLine = stream.readLine()) != null) {
								System.out.println(streamLine);
								if(!url1Found) {
									matcher = patternURL.matcher(streamLine);
									if(matcher.find()) {
										url1 = matcher.group();
										url1Found = true;
									}
								}
								if(url1Found) {
									if(line.contains("https://pbs.twimg.com/media")) {
										Pattern pattern = Pattern.compile("https:\\/\\/pbs.twimg.com\\/media\\/[\\w\\d-.]*");
										matcher = pattern.matcher(streamLine);
										if(matcher.find()) {
											url2 = matcher.group();
											break;
										}
									}
									else if(line.contains("https://pbs.twimg.com/ext")){
										break;
									}
								}
							}
							
							description = description.replaceAll("pic.twitter.com\\/[\\w\\d]*", (url2 != null ? url2 : url1));
						}
					}
					if(message.contains("<pubDate>") && message.contains("</pubDate>")) {
						int firstPos = message.indexOf("<pubDate>");
						int lastPos = message.indexOf("</pubDate>");
						pubDate = message.substring(firstPos, lastPos).replaceAll("(<pubDate>|</pubDate>)", "");
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
					message = "";
					itemTagFound = false;
					title = "";
					description = "";
					pubDate = "";
					link = "";
					redo = true;
				}
				startPosition = 0;
			} while(redo);
		}
	}
}
