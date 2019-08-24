package listeners;

import java.awt.Color;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateBoostCountEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.Azrael;
import sql.DiscordRoles;

public class BoostCountListener extends ListenerAdapter{
	
	@Override
	public void onGuildUpdateBoostCount(GuildUpdateBoostCountEvent e) {
		List<Member> members = e.getGuild().getMembers().parallelStream().filter(f -> f.getTimeBoosted() != null).collect(Collectors.toList());
		Member booster = null;
		long highestBoosterTime = 0;
		for(var member : members) {
			Timestamp serverBoostDate = Timestamp.valueOf(member.getTimeBoosted().toLocalDateTime());
			if(serverBoostDate.getTime() > highestBoosterTime) {
				highestBoosterTime = serverBoostDate.getTime();
				booster = member;
			}
		}
		
		var channels = Azrael.SQLgetChannels(e.getGuild().getIdLong());
		if(channels != null && channels.size() > 0) {
			var log_channel = channels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
			if(log_channel != null) {
				EmbedBuilder message = new EmbedBuilder().setColor(Color.getHSBColor(293, 87, 63)).setTitle("Server has been boosted!");
				message.addField("Booster", booster.getAsMention(), true);
				message.addField("Current Boost Count", "**"+e.getNewBoostCount()+"**", true);
				message.setThumbnail(booster.getUser().getEffectiveAvatarUrl());
				e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.build()).queue();
			}
		}
		
		var boostRole = DiscordRoles.SQLgetRole(e.getGuild().getIdLong(), "boo");
		if(boostRole != 0) e.getGuild().addRoleToMember(booster, e.getGuild().getRoleById(boostRole)).queue();
	}
}
