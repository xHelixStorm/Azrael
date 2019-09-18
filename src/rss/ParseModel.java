package rss;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vdurmont.emoji.EmojiParser;

import constructors.Channels;
import constructors.RSS;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.ReadyEvent;
import util.STATIC;

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
		in.close();
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
		String author = "";
		
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
				else {
					message += line;
				}
				if(itemTagFound && line.contains("</item>")) {
					endPosition = line.indexOf("</item>");
					tempMessage = line.substring(startPosition, endPosition);
					message += tempMessage;
					if(message.contains("<title>") && message.contains("</title>")) {
						int firstPos = message.indexOf("<title>");
						int lastPos = message.indexOf("</title>");
						title = message.substring(firstPos, lastPos).replaceAll("(<title>|</title>)", "");
						Pattern pattern = Pattern.compile("&gt;[\\w\\d@]{1}[\\w\\d\\s]*&lt;");
						Matcher matcher = pattern.matcher(title);
						boolean atFound = false;
						while(matcher.find()) {
							var letters = matcher.group();
							if(atFound) {
								author += "@"+letters;
								break;
							}
							if(letters.contains("@"))
								atFound = true;
							if(!atFound)
								author = matcher.group()+" ";
						}
						author = author.replaceAll("(>|&gt;|<|&lt;)", "");
						pattern = Pattern.compile("href=\"[\\/\\w\\d\"]*status\\/[\\d\"]*");
						matcher = pattern.matcher(title);
						if(matcher.find()) {
							link = matcher.group();
							link = "https://twitter.com"+link.replaceAll("(href=|\")", "");
							BufferedReader stream = STATIC.retrieveWebPageCode(link);
							pattern = Pattern.compile("https:\\/\\/t.co/[\\w\\d]*");
							boolean url1Found = false;
							boolean descriptionFound = false;
							String url1 = null;
							String url2 = null;
							String streamLine;
							while((streamLine = stream.readLine()) != null) {
								if(!descriptionFound && streamLine.contains("<title>") && streamLine.contains("</title>")) {
									descriptionFound = true;
									int descPos = streamLine.indexOf("<title>");
									int descPosEnd = streamLine.indexOf("</title>");
									description = streamLine.substring(descPos, descPosEnd);
									Pattern descriptionPattern = Pattern.compile(";[\\w\\d#].+");
									matcher = descriptionPattern.matcher(description);
									if(matcher.find()) {
										description = matcher.group();
										description = description.replaceAll("(&quot;|<\\/title>|;)", "");
										description = description.replaceAll("&#10", "\n");
										description = description.replaceAll("&#39", "'");
										description = description.replace("…", "");
									}
									
								}
								if(!url1Found) {
									matcher = pattern.matcher(streamLine);
									if(matcher.find()) {
										url1 = matcher.group();
										url1Found = true;
									}
								}
								if(url1Found) {
									if(streamLine.contains("https://pbs.twimg.com/media")) {
										pattern = Pattern.compile("https:\\/\\/pbs.twimg.com\\/media\\/[\\w\\d-.]*");
										matcher = pattern.matcher(streamLine);
										if(matcher.find()) {
											url2 = matcher.group();
											break;
										}
									}
									else if(streamLine.contains("https://pbs.twimg.com/ext")){
										break;
									}
								}
							}
							stream.close();
							if(description.contains("pic.twitter.com/")) {
								description.replaceAll("pic.twitter.com\\/[\\w\\d]*", (url2 != null ? url2 : url1));
							}
							else if(!description.contains("t.co/")) {
								description += "\n"+(url2 != null ? url2 : url1);
							}
							else
								description.replaceAll("https:\\/\\/t.co\\/[\\w\\d]*", (url2 != null ? url2 : url1));
						}
						if(description.length() > 0 || pubDate.length() > 0 || link.length() > 0) {
							String out = /*format.replace("{title}", title);
							out = out*/format.replace("{description}", description);
							out = out.replace("{pubDate}", pubDate);
							out = out.replace("{link}", "<"+link+">");
							out = out.replace("{author}", author);
							out = out.replaceAll("&#039;", "'");
							final String outMessage = EmojiParser.parseToUnicode(out);
							MessageHistory history = new MessageHistory(e.getJDA().getGuildById(guild_id).getTextChannelById(rss_channel.getChannel_ID()));
							List<Message> msg = history.retrievePast(100).complete();
							Message historyMessage = msg.parallelStream().filter(f -> f.getContentRaw().equals(outMessage)).findAny().orElse(null);
							if(historyMessage == null)
								e.getJDA().getGuildById(guild_id).getTextChannelById(rss_channel.getChannel_ID()).sendMessage(outMessage).queue();
						}
					}
					/*if(message.contains("<link>") && message.contains("</link>")) {
						int firstPos = message.indexOf("<link>");
						int lastPos = message.indexOf("</link>");
						link = message.substring(firstPos, lastPos).replaceAll("(<link>|</link>)", "");
						BufferedReader stream = STATIC.retrieveWebPageCode(link);
						Pattern patternURL = Pattern.compile("https:\\/\\/twitter.com[\\w\\d\\/]*");
						Matcher matcher = null;
						String currentLine;
						while((currentLine = stream.readLine()) != null) {
							matcher = patternURL.matcher(currentLine);
							if(matcher.find()) {
								link = matcher.group();
							}
						}
						stream.close();
					}*/
					/*if(message.contains("<description>") && message.contains("</description>")) {
						Pattern patternURL = Pattern.compile("&mdash; .+\\(@[\\w\\d_]*\\)");
						Matcher matcher = null;
						int firstPos = message.indexOf("<description>");
						int lastPos = message.indexOf("</description>");
						description = message.substring(firstPos, lastPos).replaceAll("(<description>|</description>)", "");
						//description = description.substring(9, description.length()-3);
						matcher = patternURL.matcher(description);
						if(matcher.find())
							author = matcher.group().replaceAll("&mdash; ", "");
						title = title.replaceAll("(<a[\\s\\w\\d=\"\\/?:.-]*>|<s[\\s\\w\\s=\"]*>|<\\/s>|<b[\\s\\w\\d=\"]*>|<\\/span>|<span[\\s\\w\\d=\"-]*>|<\\/strong>|<\\/b>|<\\/a>)", "");
						title = title.replaceAll("(<br>|</p>)", "\n");
						//description = description.replaceAll("\\b(January|February|March|April|May|June|July|August|September|October|November|December) [\\d]{2}, [\\d]{4}.?$", "");
						//description = description.replace(author, "");
						if(description.contains("pic.twitter.com/")) {
							
							BufferedReader stream = STATIC.retrieveWebPageCode(link);
							patternURL = Pattern.compile("https:\\/\\/t.co/[\\w\\d]*");
							boolean url1Found = false;
							String url1 = null;
							String url2 = null;
							String streamLine;
							while((streamLine = stream.readLine()) != null) {
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
							stream.close();
							description = description.replaceAll("pic.twitter.com\\/[\\w\\d]*", (url2 != null ? url2 : url1));
						}
					}*/
					/*if(message.contains("<pubDate>") && message.contains("</pubDate>")) {
						int firstPos = message.indexOf("<pubDate>");
						int lastPos = message.indexOf("</pubDate>");
						pubDate = message.substring(firstPos, lastPos).replaceAll("(<pubDate>|</pubDate>)", "");
					}*/
					/*if(title.length() > 0 || description.length() > 0 || pubDate.length() > 0 || link.length() > 0) {
						String out = format.replace("{title}", title);
						out = out.replace("{description}", description);
						out = out.replace("{pubDate}", pubDate);
						out = out.replace("{link}", "<"+link+">");
						out = out.replace("{author}", author);
						out = out.replaceAll("&#039;", "'");
						final String outMessage = EmojiParser.parseToUnicode(out);
						MessageHistory history = new MessageHistory(e.getJDA().getGuildById(guild_id).getTextChannelById(rss_channel.getChannel_ID()));
						List<Message> msg = history.retrievePast(30).complete();
						Message historyMessage = msg.parallelStream().filter(f -> f.getContentRaw().equals(outMessage)).findAny().orElse(null);
						if(historyMessage == null)
							e.getJDA().getGuildById(guild_id).getTextChannelById(rss_channel.getChannel_ID()).sendMessage(outMessage).queue();
					}*/
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
		in.close();
	}
}
