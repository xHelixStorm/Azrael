package de.azrael.commands;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.constructors.RSS;
import de.azrael.core.Hashes;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.rss.TwitchModel;
import de.azrael.sql.Azrael;
import de.azrael.timerTask.ParseSubscription;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Twitch implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Twitch.class);
	
	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.TWITCH);
	}

	@Override
	public boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(args.length == 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_HELP)).build()).queue();
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER))) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_REGISTER_HELP)).build()).queue();
		}
		else if(args.length == 2 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER))) {
			if(args[1].matches("[a-zA-Z0-9_-]{4,25}")) {
				final String username = args[1];
				boolean success = false;
				String [] user = null;
				try {
					user = TwitchModel.findUser(username);
					if(user != null) {
						success = true;
					}
				} catch (IOException e1) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Twitch request to find the user {} failed in guild {}", username, e.getGuild().getId());
				}
				
				if(success) {
					if(Azrael.SQLInsertRSS(user[0], e.getGuild().getIdLong(), 5, user[1]) > 0) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_REGISTER_STEP).replace("{}", user[1])).build()).queue();
						logger.info("User {} has subscribed to the twitch username {} in guild {}", e.getMember().getUser().getId(), user[1], e.getGuild().getId());
						if(Hashes.getFeedsSize(e.getGuild().getIdLong()) == 0 && !ParseSubscription.timerIsRunning(e.getGuild().getIdLong()))
							ParseSubscription.runTask(e.getJDA(), e.getGuild().getIdLong());
						Hashes.removeFeeds(e.getGuild().getIdLong());
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Twitch user {} couldn't be registered in guild {}", user[1], e.getGuild().getId());
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_REGISTER_INVALID_USER)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_REGISTER_INVALID_USER)).build()).queue();
			}
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_FORMAT))) {
			final ArrayList<RSS> twitch = Azrael.SQLgetSubscriptions(e.getGuild().getIdLong(), 5);
			if(twitch != null && twitch.size() > 0) {
				int count = 0;
				StringBuilder out = new StringBuilder();
				for(final var user : twitch) {
					count++;
					out.append(count+". **"+user.getName()+"**\n");
				}
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_FORMAT_STEP_1)+out.toString()).build()).queue();
				Hashes.addTempCache("twitch_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "format").setObject(twitch));
			}
			else if(twitch != null) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_REMOVE_ERR)).build()).queue();
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Twitch subscriptions couldn't be retrieved in guild {}", e.getGuild().getId());
			}
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CHANNEL))) {
			final ArrayList<RSS> twitch = Azrael.SQLgetSubscriptions(e.getGuild().getIdLong(), 5);
			if(twitch != null && twitch.size() > 0) {
				int count = 0;
				StringBuilder out = new StringBuilder();
				for(final var user : twitch) {
					count++;
					out.append(count+". **"+user.getName()+"**\n");
				}
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_CHANNEL_STEP_1)+out.toString()).build()).queue();
				Hashes.addTempCache("twitch_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "channel").setObject(twitch));
			}
			else if(twitch != null) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_REMOVE_ERR)).build()).queue();
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Twitch subscriptions couldn't be retrieved in guild {}", e.getGuild().getId());
			}
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
			final ArrayList<RSS> twitch = Azrael.SQLgetSubscriptions(e.getGuild().getIdLong(), 5);
			if(twitch != null && twitch.size() > 0) {
				int count = 0;
				StringBuilder out = new StringBuilder();
				for(final var user : twitch) {
					count++;
					out.append(count+". **"+user.getName()+"**\n");
				}
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_REMOVE)+out.toString()).build()).queue();
				Hashes.addTempCache("twitch_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "remove").setObject(twitch));
			}
			else if(twitch != null) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_REMOVE_ERR)).build()).queue();
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Twitch subscriptions couldn't be retrieved in guild {}", e.getGuild().getId());
			}
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEST))) {
			final ArrayList<RSS> twitch = Azrael.SQLgetSubscriptions(e.getGuild().getIdLong(), 5);
			if(twitch != null && twitch.size() > 0) {
				int count = 0;
				StringBuilder out = new StringBuilder();
				for(final var user : twitch) {
					count++;
					out.append(count+". **"+user.getName()+"**\n");
				}
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_TEST)+out.toString()).build()).queue();
				Hashes.addTempCache("twitch_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "test").setObject(twitch));
			}
			else if(twitch != null) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_REMOVE_ERR)).build()).queue();
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Twitch subscriptions couldn't be retrieved in guild {}", e.getGuild().getId());
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
		return true;
	}

	@Override
	public void executed(String [] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Twitch command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.TWITCH.getColumn(), out.toString().trim());
		}
	}

}
