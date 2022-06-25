package de.azrael.commands;

import java.awt.Color;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.core.Hashes;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
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
			final var subCommands = BotConfiguration.SQLgetCommand(e.getGuild().getIdLong(), 1, Command.SUBSCRIBE_RSS, Command.SUBSCRIBE_TWITTER
					, Command.SUBSCRIBE_REDDIT, Command.SUBSCRIBE_YOUTUBE, Command.SUBSCRIBE_TWITCH);
			
			//sub commands are disabled by default in case of errors
			boolean rss = false;
			boolean twitter = false;
			boolean reddit = false;
			boolean youtube = false;
			boolean twitch = false;
			
			for(final Object command : subCommands) {
				boolean enabled = false;
				String name = "";
				for(Object values : (ArrayList<?>)command) {
					if(values instanceof Boolean)
						enabled = (Boolean)values;
					else if(values instanceof String)
						name = ((String)values).split(":")[0];
				}
				
				if(name.equals(Command.SUBSCRIBE_RSS.getColumn())) {
					rss = enabled;
				}
				else if(name.equals(Command.SUBSCRIBE_TWITTER.getColumn())) {
					twitter = enabled;
				}
				else if(name.equals(Command.SUBSCRIBE_REDDIT.getColumn())) {
					reddit = enabled;
				}
				else if(name.equals(Command.SUBSCRIBE_YOUTUBE.getColumn())) {
					youtube = enabled;
				}
				else if(name.equals(Command.SUBSCRIBE_TWITCH.getColumn())) {
					twitch = enabled;
				}
			}
			
			//throw default message with instructions
			ArrayList<String> subscriptionTypes = Azrael.SQLgetSubscriptionsTypes();
			if(subscriptionTypes != null && !subscriptionTypes.isEmpty()) {
				StringBuilder out = new StringBuilder();
				for(String type : subscriptionTypes) {
					if(Command.SUBSCRIBE_RSS.getColumn().contains(type.toLowerCase()) && rss)
						out.append("**"+type+"**\n");
					else if(Command.SUBSCRIBE_TWITTER.getColumn().contains(type.toLowerCase()) && twitter)
						out.append("**"+type+"**\n");
					else if(Command.SUBSCRIBE_REDDIT.getColumn().contains(type.toLowerCase()) && reddit)
						out.append("**"+type+"**\n");
					else if(Command.SUBSCRIBE_YOUTUBE.getColumn().contains(type.toLowerCase()) && youtube)
						out.append("**"+type+"**\n");
					else if(Command.SUBSCRIBE_TWITCH.getColumn().contains(type.toLowerCase()) && twitch)
						out.append("**"+type+"**\n");
				}
				if(out.length() > 0) {
					message.setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS));
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_HELP).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_EXIT))+out.toString()).build()).queue();
				}
				else {
					message.setColor(Color.RED);
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_DISABLED)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessage(message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Subscription types couldn't be retrieved in guild {}", e.getGuild().getId());
			}
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_RSS)) && STATIC.getCommandEnabled(e.getGuild(), Command.SUBSCRIBE_RSS)) {
			int permissionLevel = STATIC.getCommandLevel(e.getGuild(), Command.SUBSCRIBE_RSS);
			if(UserPrivs.comparePrivilege(e.getMember(), permissionLevel)) {
				e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.RSS_HELP)
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_FORMAT))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_CHANNEL))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_TEST))
						.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY))).build()).queue();
				Hashes.addTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "rss"));
			}
			else if(!botConfig.getIgnoreMissingPermissions())
				UserPrivs.throwNotEnoughPrivilegeError(e, permissionLevel);
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TWITTER)) && STATIC.getCommandEnabled(e.getGuild(), Command.SUBSCRIBE_TWITTER)) {
			int permissionLevel = STATIC.getCommandLevel(e.getGuild(), Command.SUBSCRIBE_TWITTER);
			if(UserPrivs.comparePrivilege(e.getMember(), permissionLevel)) {
				e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITTER_HELP)
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_FORMAT))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_OPTIONS))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_CHANNEL))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_TEST))
						.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY))).build()).queue();
				Hashes.addTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "twitter"));
			}
			else if(!botConfig.getIgnoreMissingPermissions())
				UserPrivs.throwNotEnoughPrivilegeError(e, permissionLevel);
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REDDIT)) && STATIC.getCommandEnabled(e.getGuild(), Command.SUBSCRIBE_REDDIT)) {
			int permissionLevel = STATIC.getCommandLevel(e.getGuild(), Command.SUBSCRIBE_REDDIT);
			if(UserPrivs.comparePrivilege(e.getMember(), permissionLevel)) {
				e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_HELP)
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_FORMAT))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_CHANNEL))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_TEST))
						.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY))).build()).queue();
				Hashes.addTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "reddit"));
			}
			else if(!botConfig.getIgnoreMissingPermissions())
				UserPrivs.throwNotEnoughPrivilegeError(e, permissionLevel);
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_YOUTUBE)) && STATIC.getCommandEnabled(e.getGuild(), Command.SUBSCRIBE_YOUTUBE)) {
			int permissionLevel = STATIC.getCommandLevel(e.getGuild(), Command.SUBSCRIBE_YOUTUBE);
			if(UserPrivs.comparePrivilege(e.getMember(), permissionLevel)) {
				e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.YOUTUBE_HELP)
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_FORMAT))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_CHANNEL))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_TEST))
						.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY))).build()).queue();
				Hashes.addTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "youtube"));
			}
			else if(!botConfig.getIgnoreMissingPermissions())
				UserPrivs.throwNotEnoughPrivilegeError(e, permissionLevel);
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TWITCH)) && STATIC.getCommandEnabled(e.getGuild(), Command.SUBSCRIBE_TWITCH)) {
			int permissionLevel = STATIC.getCommandLevel(e.getGuild(), Command.SUBSCRIBE_TWITCH);
			if(UserPrivs.comparePrivilege(e.getMember(), permissionLevel)) {
				e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_HELP)
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_FORMAT))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_CHANNEL))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_TEST))
						.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY))).build()).queue();
				Hashes.addTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "twitch"));
			}
			else if(!botConfig.getIgnoreMissingPermissions())
				UserPrivs.throwNotEnoughPrivilegeError(e, permissionLevel);
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
