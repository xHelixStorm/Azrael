package threads;

import java.awt.Color;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import constructors.Rank;
import fileManagement.GuildIni;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.ReadyEvent;
import sql.RankingSystem;
import sql.DiscordRoles;
import sql.Azrael;

public class RoleExtend implements Runnable{
	private ReadyEvent e;
	private long guild_id;
	
	public RoleExtend(ReadyEvent event, long _guild_id){
		e = event;
		guild_id = _guild_id;
	}
	
	@Override
	public void run() {
		Role mute_role = e.getJDA().getGuildById(guild_id).getRoleById(DiscordRoles.SQLgetRole(guild_id, "mut"));
		if(mute_role != null) {
			EmbedBuilder message = new EmbedBuilder().setColor(Color.RED).setTitle("Warned users can't run away even after a reboot!");
			boolean banHammerFound = false;
			ArrayList<Member> users = new ArrayList<Member>();
			var log_channel = Azrael.SQLgetChannels(guild_id).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
			int i = 0;
			
			Guilds guild_settings = RankingSystem.SQLgetGuild(guild_id);
			for(Member member : e.getJDA().getGuildById(guild_id).getMembersWithRoles(mute_role)) {
				var dbData = Azrael.SQLgetData(member.getUser().getIdLong(), guild_id);
				if(dbData.getUnmute() != null) {
					long unmute;
					try{
						unmute = (dbData.getUnmute().getTime() - System.currentTimeMillis());
					} catch(NullPointerException npe){
						unmute = 0;
					}
					if(unmute < 0){unmute = 0;}
					long assignedRole = 0;
					boolean rankingState = false;
					Rank user_details = RankingSystem.SQLgetWholeRankView(member.getUser().getIdLong(), guild_id, guild_settings.getThemeID());
					if(user_details != null){
						assignedRole = user_details.getCurrentRole();
						rankingState = guild_settings.getRankingState();
					}
					users.add(member);
					banHammerFound = true;
					new Thread(new MuteRestart(e, member, guild_id, log_channel, mute_role, unmute, assignedRole, rankingState)).start();
					i++;
				}
			}
			if(banHammerFound == true && log_channel != null) {
				Logger logger = LoggerFactory.getLogger(RoleExtend.class);
				logger.debug("Found muted users on start up in {}", e.getJDA().getGuildById(guild_id).getName());
				e.getJDA().getGuildById(guild_id).getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription(i+" users were found muted on start up. The mute timer is restarting from where it stopped!"+(GuildIni.getOverrideBan(guild_id) ? "\nExcluded are users that have been muted permanently on this server!" : "")).build()).queue();
			}
		}
	}
}
