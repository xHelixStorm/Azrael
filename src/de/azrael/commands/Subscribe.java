package de.azrael.commands;

import java.awt.Color;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.core.Hashes;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Subscribe to an RSS feed or follow hashtags on Twitter
 * @author xHelixStorm
 *
 */

public class Subscribe implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Subscribe.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.SUBSCRIBE);
	}

	@Override
	public boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		EmbedBuilder message = new EmbedBuilder();
		if(args.length == 0) {
			//throw default message with instructions
			ArrayList<String> subscriptionTypes = Azrael.SQLgetSubscriptionsTypes();
			if(subscriptionTypes != null && !subscriptionTypes.isEmpty()) {
				StringBuilder out = new StringBuilder();
				for(String type : subscriptionTypes)
					out.append(type+"\n");
				message.setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS));
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_HELP).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_EXIT))+out.toString()).build()).queue();
			}
			else {
				e.getChannel().sendMessage(message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Subscription types couldn't be retrieved in guild {}", e.getGuild().getId());
			}
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_RSS))) {
			e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.RSS_HELP)).build()).queue();
			Hashes.addTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "rss"));
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TWITTER))) {
			e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITTER_HELP)).build()).queue();
			Hashes.addTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "twitter"));
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REDDIT))) {
			e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_HELP)).build()).queue();
			Hashes.addTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "reddit"));
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_YOUTUBE))) {
			e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.YOUTUBE_HELP)).build()).queue();
			Hashes.addTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "youtube"));
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TWITCH))) {
			e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_HELP)).build()).queue();
			Hashes.addTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "twitch"));
		}
		else {
			e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Subscribe command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), out.toString().trim());
		}
	}
}
