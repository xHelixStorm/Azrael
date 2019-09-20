package listeners;

import java.awt.Color;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Bancollect;
import core.Hashes;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import sql.Azrael;
import util.STATIC;

public class GuildLeaveListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(GuildLeaveListener.class);
	
	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent e) {
		new Thread(() -> {
			var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
			if(log_channel != null) {
				logger.debug("{} has left the guild {}", e.getUser().getId(), e.getGuild().getName());
				EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(IniFileReader.getLeaveThumbnail()).setTitle("User left!");
				EmbedBuilder kick = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(IniFileReader.getKickThumbnail()).setTitle("User kicked!");

				final long user_id = e.getUser().getIdLong();
				final String user_name = e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator();
				final long guild_id = e.getGuild().getIdLong();
				final String guild_name = e.getGuild().getName();
				
				Bancollect warnedUser = Azrael.SQLgetData(e.getMember().getUser().getIdLong(), guild_id);
				if(e.getGuild().getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
					AuditLogPaginationAction kickLog = e.getGuild().retrieveAuditLogs().cache(false);
					kickLog.limit(1);
					kickLog.queue((entries) -> {
						if(!entries.isEmpty() && entries.get(0).getType() == ActionType.KICK && entries.get(0).getTargetIdLong() == user_id && !Hashes.containsActionlog(entries.get(0).getId())) {
							AuditLogEntry entry = entries.get(0);
							Hashes.addActionlog(entry.getId());
							var cache = Hashes.getTempCache("kick_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
							var kick_issuer = (cache != null ? cache.getAdditionalInfo() : entry.getUser().getAsMention());
							var kick_reason = (cache != null ? cache.getAdditionalInfo2() : (entry.getReason() != null && entry.getReason().length() > 0 ? entry.getReason() : "No reason has been provided!"));
							Hashes.clearTempCache("kick_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
							e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(kick.setDescription("["+new Timestamp(System.currentTimeMillis()).toString()+"] **"+user_name+"** with the id number **"+e.getUser().getId()+"** got kicked from **"+guild_name+"**!\n Kicked by: "+kick_issuer+"\nReason: "+kick_reason).build()).queue();
							Azrael.SQLInsertActionLog("MEMBER_KICK", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "User Kicked");
							
							//Unwatch the kicked user, if he's being watched
							STATIC.handleUnwatch(null, e, (short)2);
						}
						else if(warnedUser.getMuted() && warnedUser.getBanID() == 1) {
							e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription("["+new Timestamp(System.currentTimeMillis()).toString()+"] **"+user_name+"** has left from "+guild_name+" while being muted!").build()).queue();
							Azrael.SQLUpdateGuildLeft(user_id, guild_id, true);
						}
						else if(GuildIni.getLeaveMessage(guild_id) && warnedUser.getBanID() == 1)
							e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription("["+new Timestamp(System.currentTimeMillis()).toString()+"] **"+user_name+"** has left from **"+guild_name+"**").build()).queue();
					});
				}
				else {
					if(warnedUser.getMuted() && warnedUser.getBanID() == 1) {
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription("["+new Timestamp(System.currentTimeMillis()).toString()+"] **"+user_name+"** has left from "+guild_name+" while being muted!").build()).queue();
						Azrael.SQLUpdateGuildLeft(user_id, guild_id, true);
					}
					else if(GuildIni.getLeaveMessage(guild_id) && warnedUser.getBanID() == 1)
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription("["+new Timestamp(System.currentTimeMillis()).toString()+"] **"+user_name+"** has left from **"+guild_name+"**").build()).queue();
				}
			}
		}).start();
	}
}
