package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import constructors.RSS;
import core.Hashes;
import core.UserPrivs;
import enums.Translation;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import util.STATIC;

/**
 * Subscribe to an RSS feed or follow hashtags on Twitter
 * @author xHelixStorm
 *
 */

public class Subscribe implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Subscribe.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getSubscribeCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getSubscribeLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		EmbedBuilder message = new EmbedBuilder();
		if(args.length == 0) {
			//throw default message with instructions
			message.setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_HELP)).build()).queue();
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER))) {
			message.setColor(Color.BLUE);
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_REGISTER_HELP)).build()).queue();
		}
		else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER)) && !args[1].matches("[^\\d]")) {
			//select a rss model
			var type = Integer.parseInt(args[1]);
			if(type >= 1 && type <= 2) {
				if(type == 1) {
					message.setColor(Color.BLUE);
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_REGISTER_RSS)).build()).queue();
					Hashes.addTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(180000, "register", ""+type));
				}
				else if(type == 2) {
					STATIC.loginTwitter();
					if(STATIC.getTwitterFactory() != null) {
						message.setColor(Color.BLUE);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_REGISTER_HASHTAG)).build()).queue();
						Hashes.addTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(180000, "register", ""+type));
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_LOGIN_TWITTER)).build()).queue();
					}
				}
			}
			else {
				message.setColor(Color.RED);
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_REGISTER_ERR)).build()).queue();
			}
		}
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
			int counter = 1;
			StringBuilder out = new StringBuilder();
			for(RSS feed : Azrael.SQLgetRSSFeeds(e.getGuild().getIdLong())) {
				out.append((counter++)+": **"+feed.getURL()+"**\n");
			}
			message.setColor(Color.BLUE);
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_REMOVE_HELP)+(out.length() > 0 ? out.toString(): STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_NO_SUBSCRIPTIONS))).build()).queue();
			if(out.length() > 0)
				Hashes.addTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(180000, "remove"));
		}
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_FORMAT))) {
			int counter = 1;
			StringBuilder out = new StringBuilder();
			for(RSS feed : Azrael.SQLgetRSSFeeds(e.getGuild().getIdLong())) {
				out.append(counter+": **"+feed.getURL()+"**\n");
				counter++;
			}
			message.setColor(Color.BLUE);
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_FORMAT_HELP)+(out.length() > 0 ? out.toString(): STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_NO_SUBSCRIPTIONS))).build()).queue();
			if(out.length() > 0)
				Hashes.addTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(180000, "format"));
		}
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_OPTIONS))) {
			int counter = 1;
			StringBuilder out = new StringBuilder();
			for(RSS feed: Azrael.SQLgetRSSFeeds(e.getGuild().getIdLong(), 2)) {
				out.append(counter+": **"+feed.getURL()+"**\n");
				counter++;
			}
			if(out.length() > 0) {
				message.setColor(Color.BLUE);
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_OPTIONS_HELP)+out.toString()).build()).queue();
				Hashes.addTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(180000, "options"));
			}
			else {
				message.setColor(Color.RED);
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_OPTIONS_ERR)).build()).queue();
			}
		}
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_TEST))) {
			//test a feed
			int counter = 1;
			StringBuilder out = new StringBuilder();
			for(RSS feed : Azrael.SQLgetRSSFeeds(e.getGuild().getIdLong())) {
				out.append(counter+": **"+feed.getURL()+"**\n");
				counter++;
			}
			message.setColor(Color.BLUE);
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_TEST_HELP)+(out.length() > 0 ? out.toString(): STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_NO_SUBSCRIPTIONS))).build()).queue();
			if(out.length() > 0)
				Hashes.addTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(180000, "test"));
		}
		else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY))) {
			//display the registered feeds
			int counter = 1;
			StringBuilder out = new StringBuilder();
			for(RSS feed : Azrael.SQLgetRSSFeeds(e.getGuild().getIdLong())) {
				out.append(counter+": **"+feed.getURL()+"**\n");
				counter++;
			}
			message.setColor(Color.BLUE);
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_DISPLAY_HELP)+(out.length() > 0 ? out.toString(): STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_NO_SUBSCRIPTIONS))).build()).queue();
		}
		else {
			e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("The user {} has used the Rss command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
