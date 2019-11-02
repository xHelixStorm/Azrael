package threads;

/**
 * this class is meant to search for muted users and to restart the mute timer
 * in case it was still running before the bot has been restarted.
 * 
 * If there's a muted user on the server but there are no saved details, the mute
 * role will be removed.
 */

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import fileManagement.GuildIni;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import sql.RankingSystem;
import sql.DiscordRoles;
import sql.Azrael;

public class RoleExtend implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(RoleExtend.class);
	private final static EmbedBuilder message = new EmbedBuilder().setColor(Color.RED).setTitle("Warned users can't run away even after a reboot!");
	
	private Guild guild;
	
	public RoleExtend(Guild _guild) {
		this.guild = _guild;
	}
	
	@Override
	public void run() {
		//retrieve the mute role of the current server
		var mute_role_object = DiscordRoles.SQLgetRoles(guild.getIdLong()).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null);
		//do this step if a mute role is registered
		if(mute_role_object != null) {
			Role mute_role = guild.getRoleById(mute_role_object.getRole_ID());
			boolean banHammerFound = false;
			//get the log channel for the current server
			var log_channel = Azrael.SQLgetChannels(guild.getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
			int i = 0;
			
			Guilds guild_settings = RankingSystem.SQLgetGuild(guild.getIdLong());
			//search for all members which have a mute role
			for(Member member : guild.getMembersWithRoles(mute_role)) {
				//retrieve the mute details of the current user
				var dbData = Azrael.SQLgetData(member.getUser().getIdLong(), guild.getIdLong());
				//if no data is available (default constructor was called) then don't restart the mute but simply remove the role
				if(dbData.getUnmute() != null) {
					//retrieve the time for the user to still stay muted, if expired set it to 0 for direct unmute
					long unmute = (dbData.getUnmute().getTime() - System.currentTimeMillis());
					if(unmute < 0)
						unmute = 0;
					long assignedRole = 0;
					//get a ranking role to assign, if the user had one unlocked
					boolean rankingState = guild_settings.getRankingState();
					if(rankingState)
						assignedRole = RankingSystem.SQLgetAssignedRole(member.getUser().getIdLong(), guild.getIdLong());
					banHammerFound = true;
					//run thread to restart the timer
					new Thread(new MuteRestart(member.getUser().getIdLong(), guild, member.getUser().getName()+"#"+member.getUser().getDiscriminator(), log_channel, mute_role, unmute, assignedRole, rankingState)).start();
					i++;
				}
				//functionality temporarily removed
				/*else {
					//check if the bot has the permission to remove and add roles
					if(guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
						guild.removeRoleFromMember(member, mute_role).queue();
						if(guild_settings.getRankingState()) {
							long assignedRole = RankingSystem.SQLgetAssignedRole(member.getUser().getIdLong(), guild.getIdLong());
							if(assignedRole != 0) guild.addRoleToMember(member, guild.getRoleById(assignedRole));
						}
						if(log_channel != null) guild.getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.ORANGE).setDescription("The user **"+member.getUser().getName()+"#"+member.getUser().getDiscriminator()+"** with the id number **"+member.getUser().getId()+"** was found muted on start up but no details of an succesfull mute has been found! Mute role removed!").build()).queue();
					}
					else {
						if(log_channel != null) guild.getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("The user **"+member.getUser().getName()+"#"+member.getUser().getDiscriminator()+"** with the id number **"+member.getUser().getId()+"** was found muted on start up but no details of an succesfull mute has been found! The mute role couldn't be removed because the MANAGE ROLES permission is missing!").build()).queue();
						logger.warn("MANAGE ROLES permission to remove the mute role is missing in guild {}!", guild.getId());
					}
				}*/
			}
			//display the amount of users that are still muted
			if(banHammerFound == true && log_channel != null) {
				logger.debug("Found muted users on start up in guild {}", guild.getId());
				guild.getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription(i+" users were found muted on start up. The mute timer is restarting from where it stopped!"+(GuildIni.getOverrideBan(guild.getIdLong()) ? "\nExcluded are users that have been muted permanently on this server!" : "")).build()).queue();
			}
		}
	}
}
