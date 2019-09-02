package listeners;

import java.awt.Color;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Bancollect;
import core.UserPrivs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.audit.AuditLogKey;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import sql.DiscordRoles;
import sql.RankingSystem;
import sql.Azrael;

public class RoleRemovedListener extends ListenerAdapter{
	
	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent e) {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setTitle("Mute role has been manually removed!");
		
		String trigger_user_name = "";
		AuditLogPaginationAction logs = e.getGuild().retrieveAuditLogs();
		first_entry: for (AuditLogEntry entry : logs)
		{
			if(entry.getChangeByKey(AuditLogKey.MEMBER_ROLES_REMOVE) != null) {
				if(entry.getChangeByKey(AuditLogKey.MEMBER_ROLES_REMOVE).toString().contains(""+DiscordRoles.SQLgetRole(e.getGuild().getIdLong(), "mut")) && entry.getGuild().getIdLong() == e.getGuild().getIdLong() && entry.getTargetIdLong() == e.getMember().getUser().getIdLong()) {
					trigger_user_name = entry.getUser().getName()+"#"+entry.getUser().getDiscriminator();
				}
			}
			break first_entry;
		}
		
		String member_name = e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator();
		long user_id = e.getMember().getUser().getIdLong();
		long guild_id = e.getGuild().getIdLong();
		
		Bancollect warnedUser = Azrael.SQLgetData(user_id, guild_id);
		
		try{
			Logger logger = LoggerFactory.getLogger(RoleRemovedListener.class);
			if(!UserPrivs.isUserMuted(e.getUser(), guild_id) && (warnedUser.getUnmute() == null || (warnedUser.getUnmute().getTime() - System.currentTimeMillis()) > 0)  && warnedUser.getMuted()) {
				var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
				if(warnedUser.getUserID() != 0) {
					if(Azrael.SQLUpdateMuted(user_id, guild_id, false) == 0) {
						logger.error("Mute information of {} couldn't be updated in Azrael.bancollect in guild {}", user_id, e.getGuild().getId());
						if(log_channel != null)e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. The mute state couldn't be updated in table Azrael.bancollect").queue();
					}
				}
				var assignedRole = RankingSystem.SQLgetAssignedRole(user_id, guild_id);
				if(assignedRole != 0) e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(assignedRole)).queue();
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				if(log_channel != null) {e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+trigger_user_name+"** has manually removed the mute role from **"+member_name+"** with the ID number **"+user_id+"**!").build()).queue();}
				logger.debug("{} got the mute role removed before the time expired in guild {}", e.getUser().getId(), e.getGuild().getId());
				Azrael.SQLInsertActionLog("MEMBER_MUTE_REMOVE_HALFWAY", user_id, guild_id, "Mute role removed manually");
			}
			else if(!UserPrivs.isUserMuted(e.getUser(), guild_id) && warnedUser.getUserID() != 0 && warnedUser.getMuted()) {
				if(Azrael.SQLUpdateMutedOnEnd(user_id, guild_id, false, false) == 999) {
					logger.error("Mute information of {} couldn't be updated in Azrael.bancollect in guild {}", user_id, e.getGuild().getId());
				}
				else {
					logger.debug("{} has been unmuted in guild {}", e.getUser().getId(), e.getGuild().getId());
					Azrael.SQLInsertActionLog("MEMBER_MUTE_REMOVE", user_id, guild_id, "Mute role removed");
				}
			}
		} catch(NullPointerException npe) {
			//do nothing
		}
	}
}
