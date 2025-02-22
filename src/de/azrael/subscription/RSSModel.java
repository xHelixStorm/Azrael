package de.azrael.subscription;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiParser;

import de.azrael.constructors.Subscription;
import de.azrael.enums.Translation;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RSSModel {
	private static final Logger logger = LoggerFactory.getLogger(RSSModel.class);

	public static boolean ModelParse(BufferedReader in, Guild guild, Subscription subscription, long subscriptionChannel, boolean defaultChannel) throws IOException {
		boolean success = false;
		final TextChannel textChannel = guild.getTextChannelById(subscriptionChannel);
		if(textChannel != null) {
			if(guild.getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY) || STATIC.setPermissions(guild, textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY))) {
				String format = subscription.getFormat();
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
					success = true;
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
				logger.warn("MESSAGE_WRITE and MESSAGE_HISTORY permission required to print into the subscription channel {} in guild {}", subscriptionChannel, guild.getId());
			}
		}
		else {
			//remove not anymore existing channel
			SubscriptionUtils.deleteRemovedChannel(subscriptionChannel, defaultChannel, subscription.getURL(), guild);
		}
		in.close();
		return success;
	}
	
	public static void ModelTest(MessageReceivedEvent e, Subscription subscription) {
		try {
			String format = subscription.getFormat();
			BufferedReader in = STATIC.retrieveWebPageCode(subscription.getURL());
			
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
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Subscription couldn't be retrieved from {} in guild {}", subscription.getURL(), e.getGuild().getId(), e1);
		}
	}
}
