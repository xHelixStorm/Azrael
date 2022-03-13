package de.azrael.commands;

import java.awt.Color;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.constructors.RSS;
import de.azrael.core.Hashes;
import de.azrael.enums.Command;
import de.azrael.enums.RedditMethod;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Reddit implements CommandPublic {
	Logger logger = LoggerFactory.getLogger(Reddit.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.REDDIT);
	}

	@Override
	public boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(args.length == 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_HELP)).build()).queue();
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER))) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_REGISTER_HELP)).build()).queue();
		}
		else if(args.length == 2 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER))) {
			if(args[1].matches("[a-zA-Z0-9\s_-]{3,20}")) {
				final String username = args[1];
				StringBuilder methods = new StringBuilder();
				for(final RedditMethod method : RedditMethod.values()) {
					methods.append("**"+method.type+"**\n");
				}
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_REGISTER_STEP_1)+methods.toString()).build()).queue();
				Hashes.addTempCache("reddit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "register", username));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_REGISTER_INVALID_USER)).build()).queue();
			}
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_FORMAT))) {
			final ArrayList<RSS> reddit = Azrael.SQLgetSubscriptions(e.getGuild().getIdLong(), 3);
			if(reddit != null && reddit.size() > 0) {
				int count = 0;
				StringBuilder out = new StringBuilder();
				for(final var user : reddit) {
					count++;
					out.append(count+". **"+user.getURL()+"**\n");
				}
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_FORMAT_STEP_1)+out.toString()).build()).queue();
				Hashes.addTempCache("reddit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "format").setObject(reddit));
			}
			else if(reddit != null) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_REMOVE_ERR)).build()).queue();
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Reddit subscriptions couldn't be retrieved in guild {}", e.getGuild().getId());
			}
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CHANNEL))) {
			final ArrayList<RSS> reddit = Azrael.SQLgetSubscriptions(e.getGuild().getIdLong(), 3);
			if(reddit != null && reddit.size() > 0) {
				int count = 0;
				StringBuilder out = new StringBuilder();
				for(final var user : reddit) {
					count++;
					out.append(count+". **"+user.getURL()+"**\n");
				}
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_CHANNEL_STEP_1)+out.toString()).build()).queue();
				Hashes.addTempCache("reddit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "channel").setObject(reddit));
			}
			else if(reddit != null) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_REMOVE_ERR)).build()).queue();
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Reddit subscriptions couldn't be retrieved in guild {}", e.getGuild().getId());
			}
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
			final ArrayList<RSS> reddit = Azrael.SQLgetSubscriptions(e.getGuild().getIdLong(), 3);
			if(reddit != null && reddit.size() > 0) {
				int count = 0;
				StringBuilder out = new StringBuilder();
				for(final var user : reddit) {
					count++;
					out.append(count+". **"+user.getURL()+"**\n");
				}
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_REMOVE)+out.toString()).build()).queue();
				Hashes.addTempCache("reddit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "remove").setObject(reddit));
			}
			else if(reddit != null) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_REMOVE_ERR)).build()).queue();
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Reddit subscriptions couldn't be retrieved in guild {}", e.getGuild().getId());
			}
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEST))) {
			final ArrayList<RSS> reddit = Azrael.SQLgetSubscriptions(e.getGuild().getIdLong(), 3);
			if(reddit != null && reddit.size() > 0) {
				int count = 0;
				StringBuilder out = new StringBuilder();
				for(final var user : reddit) {
					count++;
					out.append(count+". **"+user.getURL()+"**\n");
				}
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_TEST)+out.toString()).build()).queue();
				Hashes.addTempCache("reddit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "test").setObject(reddit));
			}
			else if(reddit != null) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_REMOVE_ERR)).build()).queue();
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Reddit subscriptions couldn't be retrieved in guild {}", e.getGuild().getId());
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Reddit command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.REDDIT.getColumn(), out.toString().trim());
		}
	}

}
