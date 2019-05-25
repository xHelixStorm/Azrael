package timerTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiParser;

import core.Hashes;
import core.RSS;
import fileManagement.GuildIni;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.events.ReadyEvent;
import sql.Azrael;

public class ParseRSS extends TimerTask{
	private static final Logger logger = LoggerFactory.getLogger(ParseRSS.class);
	
	private ReadyEvent e;
	private long guild_id;
	
	public ParseRSS(ReadyEvent _e, long _guild_id) {
		this.e = _e;
		this.guild_id = _guild_id;
	}

	@Override
	public void run() {
		if(Hashes.getFeed(guild_id) != null) {
			logger.info("task running for guild {}", e.getJDA().getGuildById(guild_id).getName());
			var rss_channel = Hashes.getChannels(guild_id).parallelStream().filter(f -> f.getChannel_Type().equals("rss")).findAny().orElse(null);
			if(rss_channel != null) {
				for(RSS rss : Hashes.getFeed(guild_id)) {
					try {
						String format = rss.getFormat();
						logger.debug("Retrieving rss feed for {} in guild {}", rss.getURL(), e.getJDA().getGuildById(guild_id).getName());
						URL rssUrl = new URL(rss.getURL());
						URLConnection con = rssUrl.openConnection();
						con.setConnectTimeout(5000);
						con.setReadTimeout(10000);
						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
						
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
								if(line.contains("<title>")) {
									int firstPos = line.indexOf("<title>");
									int lastPos = line.indexOf("</title>");
									title = line.substring(firstPos, lastPos).replaceAll("(<title>|</title>)", "");
								}
								if(line.contains("<description>")) {
									int firstPos = line.indexOf("<description>");
									int lastPos = line.indexOf("</description>");
									description = line.substring(firstPos, lastPos).replaceAll("(<description>|</descrption>)", "");
								}
								if(line.contains("<pubDate>")) {
									int firstPos = line.indexOf("<pubDate>");
									int lastPos = line.indexOf("</pubDate>");
									pubDate = line.substring(firstPos, lastPos).replaceAll("(<pubDate>|</pubDate>)", "");
								}
								if(line.contains("<link>")) {
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
					} catch (Exception e1) {
						logger.error("Error on retrieving feed", e1);
					}
				}
			}
			else {
				Azrael.SQLgetChannels(guild_id);
			}
		}
		else {
			if(GuildIni.getRssCommand(guild_id)) {
				Azrael.SQLgetRSSFeeds(guild_id);
				Azrael.SQLgetChannels(guild_id);
			}
		}
	}
	
	public static void runTask(ReadyEvent _e, long _guild_id){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		Timer time = new Timer("ParseRSS");
		time.schedule(new ParseRSS(_e, _guild_id), calendar.getTime(), TimeUnit.MINUTES.toMillis(10));
	}
}
