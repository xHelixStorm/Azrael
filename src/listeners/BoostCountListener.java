package listeners;

import java.awt.Color;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateBoostCountEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.Azrael;
import sql.DiscordRoles;

public class BoostCountListener extends ListenerAdapter {
	Logger logger = LoggerFactory.getLogger(BoostCountListener.class);
	
	@Override
	public void onGuildUpdateBoostCount(GuildUpdateBoostCountEvent e) {
		logger.debug("Server boos value changed to {} for guild {}", e.getNewValue(), e.getGuild().getIdLong());
		var boostRole = DiscordRoles.SQLgetRole(e.getGuild().getIdLong(), "boo");
		if(boostRole != 0) {
			List<Member> passedBoosters = e.getGuild().getMembersWithRoles(e.getGuild().getRoleById(boostRole));
			List<Member> boosters = e.getGuild().getBoosters();
			var traitor = false;
			for(final var member : passedBoosters) {
				var currentBooster = boosters.parallelStream().filter(f -> f.getIdLong() == member.getIdLong()).findAny().orElse(null);
				if(currentBooster == null) {
					e.getGuild().removeRoleFromMember(member, e.getGuild().getRoleById(boostRole)).queue();
					printMessage(e, member, false);
					traitor = true;
					break;
				}
			}
			if(!traitor) {
				for(final var member : boosters) {
					var currentBooster = passedBoosters.parallelStream().filter(f -> f.getIdLong() == member.getIdLong()).findAny().orElse(null);
					if(currentBooster == null) {
						e.getGuild().addRoleToMember(member, e.getGuild().getRoleById(boostRole)).queue();
						printMessage(e, member, true);
						break;
					}
				}
				
			}
		}
		else {
			if(e.getNewBoostCount() > e.getOldBoostCount()) {
				printMessage(e, null, true);
			}
			else {
				printMessage(e, null, false);
			}
		}
	}
	
	private void printMessage(GuildUpdateBoostCountEvent e, Member affectedMember, boolean increased) {
		var channels = Azrael.SQLgetChannels(e.getGuild().getIdLong());
		if(channels != null && channels.size() > 0) {
			var log_channel = channels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
			if(log_channel != null) {
				EmbedBuilder message = new EmbedBuilder().setColor(Color.MAGENTA).setTitle((increased ? "Server has been boosted!" : "Tier level decreased!"));
				if(affectedMember != null)message.addField((increased ? "Booster" : "Traitor"), affectedMember.getAsMention(), true);
				message.addField("Current Boost Count", "**"+e.getNewBoostCount()+"**", true);
				message.setThumbnail(affectedMember.getUser().getEffectiveAvatarUrl());
				message.setDescription((increased ? "A user has boosted our server! Party time!" : "There's a traitor among us who removed the boost!"));
				e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.build()).queue();
			}
		}
	}
}
