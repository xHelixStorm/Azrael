package util;

import java.awt.Color;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import constructors.Channels;
import core.Hashes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import sql.Azrael;
import sql.DiscordRoles;

public class STATIC {
	private final static Logger logger = LoggerFactory.getLogger(STATIC.class);
	
	private static final String VERSION = "6.4.310";
	private static final CopyOnWriteArrayList<Thread> threads = new CopyOnWriteArrayList<Thread>();
	private static final CopyOnWriteArrayList<Timer> timers = new CopyOnWriteArrayList<Timer>();
	
	public static String getVersion() {
		return VERSION;
	}
	
	public static void addThread(Thread thread, final String name) {
		if(threads.parallelStream().filter(f -> f.getName().equals(name)).findAny().orElse(null) != null)
			return;
		
		thread.setName(name);
		threads.add(thread);
	}
	public static boolean killThread(final String name) {
		var thread = threads.parallelStream().filter(f -> f.getName().equals(name)).findAny().orElse(null);
		if(thread != null) {
			thread.interrupt();
			threads.remove(thread);
			return true;
		}
		else {
			return false;
		}
	}
	public static void removeThread(final Thread thread) {
		threads.remove(thread);
	}
	
	public static void addTimer(Timer timer) {
		if(timers.parallelStream().filter(f -> f.equals(timer)).findAny().orElse(null) != null)
			return;
		
		timers.add(timer);
	}
	
	public static void killAllTimers() {
		for(Timer timer : timers) {
			timer.cancel();
		}
		timers.clear();
	}
	
	public static String getChannels(List<Channels> channels) {
		StringBuilder out = new StringBuilder();
		var first = true;
		var last = channels.size()-1;
		for(Channels channel : channels) {
			if(first)
				out.append("<#"+channel.getChannel_ID()+">");
			else if(channels.get(last).getChannel_ID() == channel.getChannel_ID())
				out.append(" or <#"+channel.getChannel_ID()+">");
			else
				out.append(", <#"+channel.getChannel_ID()+">");
		}
		return out.toString();
	}
	
	@SuppressWarnings("preview")
	public static int getLevel(String category) {
		return switch(category) {
			case "adm" -> 100;
			case "mod" -> 20;
			case "bot" -> 10;
			case "com" -> 1;
			default    -> 0;
		};
	}
	
	@SuppressWarnings("unused")
	public static void handleRemovedMessages(GuildMessageReceivedEvent e, GuildMessageUpdateEvent e2, String [] output) {
		Logger logger = LoggerFactory.getLogger(STATIC.class);
		logger.debug("Message removed from {} in guild {}", e.getMember().getUser().getId(), e.getGuild().getName());
		var muteRole = DiscordRoles.SQLgetRole((e != null ? e.getGuild().getIdLong() : e2.getGuild().getIdLong()), "mut");
		if(muteRole == 0) {
			if(e != null)e.getChannel().sendMessage(e.getMember().getAsMention()+" "+output[0]).queue();
			else 		 e2.getChannel().sendMessage(e2.getMember().getAsMention()+" "+output[0]).queue();
		}
		else {
			var cache = Hashes.getTempCache("report_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
			if(cache == null || cache.getExpiration() - System.currentTimeMillis() <= 0) {
				if(e != null)e.getChannel().sendMessage(e.getMember().getAsMention()+" "+output[0]).queue();
				else 		 e2.getChannel().sendMessage(e2.getMember().getAsMention()+" "+output[0]).queue();
				Hashes.addTempCache("report_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId(), new Cache(300000, "1"));
			}
			else if(cache != null) {
				if(cache.getAdditionalInfo().equals("1")) {
					if(e != null)e.getChannel().sendMessage(e.getMember().getAsMention()+" "+output[1]).queue();
					else 		 e2.getChannel().sendMessage(e2.getMember().getAsMention()+" "+output[1]).queue();
					Hashes.addTempCache("report_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId(), new Cache(300000, "2"));
				}
				else if(cache.getAdditionalInfo().equals("2")) {
					if(e != null)e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(DiscordRoles.SQLgetRole(e.getGuild().getIdLong(), "mut"))).queue();
					else		 e.getGuild().addRoleToMember(e2.getMember(), e2.getGuild().getRoleById(DiscordRoles.SQLgetRole(e2.getGuild().getIdLong(), "mut"))).queue();
					Hashes.clearTempCache("report_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
				}
			}
		}
	}
	
	public static void handleUnwatch(GuildBanEvent e, GuildMemberLeaveEvent e2, short type) {
		var user_id = (e != null ? e.getUser().getIdLong() : e2.getMember().getUser().getIdLong());
		var guild_id = (e != null ? e.getGuild().getIdLong() : e2.getGuild().getIdLong());
		var unwatchReason = (type == 1 ? "ban" : "kick");
		var watchedUser = Hashes.getWatchlist(guild_id+"-"+user_id);
		if(watchedUser != null) {
			if(Azrael.SQLDeleteWatchlist(user_id, guild_id) > 0) {
				e.getGuild().getTextChannelById(watchedUser.getWatchChannel()).sendMessage(new EmbedBuilder().setColor(Color.YELLOW).setTitle("Watch lifted due to "+unwatchReason+"!")
					.setDescription("The watch for the user "+e.getUser().getName()+"#"+e.getUser().getDiscriminator()+" has been removed due to a "+unwatchReason+"!").build()).queue();
				Hashes.removeWatchlist(guild_id+"-"+user_id);
				logger.debug("The user {} has been removed from the watchlist in guild {}", user_id, guild_id);
			}
			else {
				e.getGuild().getTextChannelById(watchedUser.getWatchChannel()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Error!")
					.setDescription("An internal error occurred. The user "+e.getUser().getName()+"#"+e.getUser().getDiscriminator()+" couldn't be removed from Azrael.watchlist!").build()).queue();
				logger.error("An internal error occurred. The user {} couldn't be removed from the Azrael.watchlist table for guild {}", user_id, guild_id);
			}
		}
	}
}
