package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Channels;
import core.Roles;
import core.UserPrivs;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import inventory.Dailies;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingSystem;
import sql.DiscordRoles;
import sql.Azrael;

public class Display implements Command{
	private static EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.MAGENTA);
	private static EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(GuildIni.getDisplayCommand(e.getGuild().getIdLong())) {
			var adminPermission = e.getMember().getUser().getIdLong() == GuildIni.getAdmin(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayLevel(e.getGuild().getIdLong())) || adminPermission) {
				long guild_id = e.getGuild().getIdLong();
				String out = "";
				
				final String prefix = GuildIni.getCommandPrefix(guild_id);
				if(args.length == 0) {
					out = "Use these parameters after the display command like **"+prefix+"display -roles** for further information on what to display:\n\n"
							+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayRolesLevel(e.getGuild().getIdLong())) || adminPermission 				? "**-roles**: Display all roles from this guild.\n" : "")
							+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayRegisteredRolesLevel(e.getGuild().getIdLong())) || adminPermission 		? "**-registered-roles**: Display all registered roles with their privileges.\n" : "")
							+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayRankingRolesLevel(e.getGuild().getIdLong())) || adminPermission 		? "**-ranking-roles**: Display all roles that can be unlocked with which level.\n" : "")
							+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayTextChannelsLevel(e.getGuild().getIdLong())) || adminPermission 		? "**-textchannels**: Display all textchannels from this guild.\n" : "")
							+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayVoiceChannelsLevel(e.getGuild().getIdLong())) || adminPermission 		? "**-voicechannels**: Display all voicechannels from this guild.\n" : "")
							+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayRegisteredChannelsLevel(e.getGuild().getIdLong())) || adminPermission 	? "**-registered-channels**: Display all registered textchannels with configured filter options.\n" : "")
							+ (UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayDailiesLevel(e.getGuild().getIdLong())) || adminPermission 				? "**-dailies**: Display all items that the "+prefix+"daily command contains." : "");
					e.getTextChannel().sendMessage(messageBuild.setDescription(out).build()).queue();
				}
				else if(args[0].equalsIgnoreCase("-roles")) {
					if(UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayRolesLevel(e.getGuild().getIdLong())) || adminPermission) {
						for(Role r : e.getGuild().getRoles()) {
							out += r.getName() + " (" + r.getId() + ") \n";
						}
						e.getTextChannel().sendMessage(messageBuild.setDescription(out).build()).queue();
					}
					else {
						e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:").build()).queue();
					}
				}
				else if(args[0].equalsIgnoreCase("-registered-roles")) {
					if(UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayRegisteredRolesLevel(e.getGuild().getIdLong())) || adminPermission) {
						for(Roles r : DiscordRoles.SQLgetRoles(guild_id)) {
							out += r.getRole_Name() + " (" + r.getRole_ID() + ") \nrole type: "+r.getCategory_Name()+"\n\n";
						}
						e.getTextChannel().sendMessage(messageBuild.setDescription(out).build()).queue();
					}
					else {
						e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:").build()).queue();
					}
				}
				else if(args[0].equalsIgnoreCase("-ranking-roles")) {
					if(UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayRankingRolesLevel(e.getGuild().getIdLong())) || adminPermission) {
						if(RankingSystem.SQLgetGuild(guild_id).getRankingState()) {
							for(rankingSystem.Rank r : RankingSystem.SQLgetRoles(guild_id)) {
								if(r.getGuildID() == guild_id) {
									out += r.getRole_Name() + " (" + r.getRoleID() + ") \nlevel to unlock: " + r.getLevel_Requirement() + "\n";
								}
							}
						}
						else {
							out = "The ranking system isn't enabled and hence no role can be displayed!";
						}
						e.getTextChannel().sendMessage(messageBuild.setDescription((out.length() > 0) ? out : "No ranking role has been registered!").build()).queue();
					}
					else {
						e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:").build()).queue();
					}
				}
				else if(args[0].equalsIgnoreCase("-textchannels")) {
					if(UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayTextChannelsLevel(e.getGuild().getIdLong())) || adminPermission) {
						for(TextChannel tc : e.getGuild().getTextChannels()) {
							out += tc.getName() + " (" + tc.getId() + ") \n";
						}
						e.getTextChannel().sendMessage(messageBuild.setDescription((out.length() > 0) ? out : "Textchannels don't exist in this server!").build()).queue();
					}
					else {
						e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:").build()).queue();
					}
				}
				else if(args[0].equalsIgnoreCase("-voicechannels")) {
					if(UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayVoiceChannelsLevel(e.getGuild().getIdLong())) || adminPermission) {
						for(VoiceChannel vc : e.getGuild().getVoiceChannels()) {
							out += vc.getName() + " (" + vc.getId() + ") \n";
						}
						e.getTextChannel().sendMessage(messageBuild.setDescription((out.length() > 0) ? out : "Voicechannels don't exist in this server!").build()).queue();
					}
					else {
						e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:").build()).queue();
					}
				}
				else if(args[0].equalsIgnoreCase("-registered-channels")) {
					if(UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayRegisteredChannelsLevel(e.getGuild().getIdLong())) || adminPermission) {
						for(Channels ch : Azrael.SQLgetChannels(guild_id)) {
							if(!out.contains(""+ch.getChannel_ID())) {
								out += "\n\n"+ch.getChannel_Name() + " (" + ch.getChannel_ID() + ") \nChannel type: "+ch.getChannel_Type_Name()+" Channel\nFilter(s) in use: "+ch.getLang_Filter();
							}
							else if(out.contains(""+ch.getChannel_ID())) {
								out += ", "+ch.getLang_Filter();
							}
						}
						e.getTextChannel().sendMessage(messageBuild.setDescription((out.length() > 0) ? out : "No channel has been registered!").build()).queue();
					}
					else{
						e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:").build()).queue();
					}
				}
				else if(args[0].equalsIgnoreCase("-dailies")) {
					if(UserPrivs.comparePrivilege(e.getMember(), GuildIni.getDisplayDailiesLevel(e.getGuild().getIdLong())) || adminPermission) {
						for(Dailies daily : RankingSystem.SQLgetDailiesAndType(guild_id, RankingSystem.SQLgetGuild(guild_id).getThemeID())) {
							out+= daily.getDescription()+"\nWeight: "+daily.getWeight()+"\n\n";
						}
						e.getTextChannel().sendMessage(messageBuild.setDescription((out.length() > 0) ? "You can receive the following items through dailies:\n\n"+out : "No daily item has been registered!").build()).queue();
					}
				}
				else {
					e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:").build()).queue();
				}
			}
			else {
				e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:").build()).queue();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		Logger logger = LoggerFactory.getLogger(Display.class);
		logger.debug("{} has used Display command", e.getMember().getUser().getIdLong());
	}

	@Override
	public String help() {
		return null;
	}

}
