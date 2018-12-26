package commands;

import java.awt.Color;

import core.Channels;
import core.Hashes;
import core.Roles;
import core.UserPrivs;
import fileManagement.IniFileReader;
import inventory.Dailies;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingDB;
import sql.ServerRoles;
import sql.SqlConnect;

public class Display implements Command{
	private static EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.MAGENTA);
	private static EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getDisplayCommand().equals("true")){
			long guild_id = e.getGuild().getIdLong();
			String message = e.getMessage().getContentRaw();
			String out = "";
			
			if(message.equals(IniFileReader.getCommandPrefix()+"display")){
				out = "Use these parameters after the display command like **"+IniFileReader.getCommandPrefix()+"display -roles** for further information on what to display:\n\n"
						+ "**-roles**: Display all roles from this guild.\n"
						+ "**-registered-roles**: Display all registered roles with their privileges.\n"
						+ "**-ranking-roles**: Display all roles that can be unlocked with which level.\n"
						+ "**-textchannels**: Display all textchannels from this guild.\n"
						+ "**-voicechannels**: Display all voicechannels from this guild.\n"
						+ "**-registered-channels**: Display all registered textchannels with configured filter options.\n"
						+ "**-dailies**: Display all items that the "+IniFileReader.getCommandPrefix()+"daily command contains.";
				e.getTextChannel().sendMessage(messageBuild.setDescription(out).build()).queue();
			}
			else if(message.equals(IniFileReader.getCommandPrefix()+"display -roles")){
				for(Role r : e.getGuild().getRoles()){
					out += r.getName() + " (" + r.getId() + ") \n";
				}
				e.getTextChannel().sendMessage(messageBuild.setDescription(out).build()).queue();
			}
			else if(message.equals(IniFileReader.getCommandPrefix()+"display -registered-roles")){
				ServerRoles.SQLgetRoles(guild_id);
				for(Roles r : ServerRoles.getRoles_ID()){
					out += r.getRole_Name() + " (" + r.getRole_ID() + ") \nrole type: "+r.getCategory_Name()+"\n\n";
				}
				ServerRoles.clearRolesArray();
				e.getTextChannel().sendMessage(messageBuild.setDescription(out).build()).queue();
			}
			else if(message.equals(IniFileReader.getCommandPrefix()+"display -ranking-roles")){
				if(Hashes.getStatus(guild_id).getRankingState()){
					for(rankingSystem.Rank r : Hashes.getMapOfRankingRoles().values()){
						if(r.getGuildID() == guild_id){
							out += r.getRole_Name() + " (" + r.getRoleID() + ") \nlevel to unlock: " + r.getLevel_Requirement() + "\n";
						}
					}
				}
				else{
					out = "The ranking system isn't enabled and hence no role can be displayed!";
				}
				e.getTextChannel().sendMessage(messageBuild.setDescription((out.length() > 0) ? out : "No ranking role has been registered!").build()).queue();
			}
			else if(message.equals(IniFileReader.getCommandPrefix()+"display -textchannels")){
				if(UserPrivs.isUserAdmin(e.getMember().getUser(), e.getGuild().getIdLong()) || UserPrivs.isUserMod(e.getMember().getUser(), e.getGuild().getIdLong()) || e.getMember().getUser().getId().equals(IniFileReader.getAdmin())) {
					for(TextChannel tc : e.getGuild().getTextChannels()){
						out += tc.getName() + " (" + tc.getId() + ") \n";
					}
					e.getTextChannel().sendMessage(messageBuild.setDescription((out.length() > 0) ? out : "Textchannels don't exist in this server!").build()).queue();
				}
				else {
					e.getTextChannel().sendMessage(denied.setDescription("**"+e.getMember().getAsMention()+" sry, you're not allowed to run this command, since it may show hidden textchannels!**").build()).queue();
				}
			}
			else if(message.equals(IniFileReader.getCommandPrefix()+"display -voicechannels")){
				if(UserPrivs.isUserAdmin(e.getMember().getUser(), e.getGuild().getIdLong()) || UserPrivs.isUserMod(e.getMember().getUser(), e.getGuild().getIdLong()) || e.getMember().getUser().getId().equals(IniFileReader.getAdmin())) {
					for(VoiceChannel vc : e.getGuild().getVoiceChannels()){
						out += vc.getName() + " (" + vc.getId() + ") \n";
					}
					e.getTextChannel().sendMessage(messageBuild.setDescription((out.length() > 0) ? out : "Voicechannels don't exist in this server!").build()).queue();
				}
				else {
					e.getTextChannel().sendMessage(denied.setDescription("**"+e.getMember().getAsMention()+" sry, you're not allowed to run this command, since it may show hidden voicechannels!**").build()).queue();
				}
			}
			else if(message.equals(IniFileReader.getCommandPrefix()+"display -registered-channels")){
				if(UserPrivs.isUserAdmin(e.getMember().getUser(), guild_id) || UserPrivs.isUserMod(e.getMember().getUser(), guild_id) || IniFileReader.getAdmin().equals(e.getMember().getUser().getId())){
					SqlConnect.SQLgetChannels(guild_id);
					for(Channels ch : SqlConnect.getChannels()){
						if(!out.contains(""+ch.getChannel_ID())){
							out += "\n\n"+ch.getChannel_Name() + " (" + ch.getChannel_ID() + ") \nChannel type: "+ch.getChannel_Type_Name()+" Channel\nFilter(s) in use: "+ch.getLang_Filter();
						}
						else if(out.contains(""+ch.getChannel_ID())){
							out += ", "+ch.getLang_Filter();
						}
					}
					SqlConnect.clearChannelsArray();
					e.getTextChannel().sendMessage(messageBuild.setDescription((out.length() > 0) ? out : "No channel has been registered!").build()).queue();
				}
				else{
					e.getTextChannel().sendMessage(denied.setDescription("**"+e.getMember().getAsMention()+" sry, you're not allowed to run this command, since it may show hidden channels!**").build()).queue();
				}
			}
			else if(message.equals(IniFileReader.getCommandPrefix()+"display -dailies")){
				for(Dailies daily : RankingDB.SQLgetDailiesAndType()){
					out+= daily.getDescription()+"\nWeight: "+daily.getWeight()+"\n\n";
				}
				e.getTextChannel().sendMessage(messageBuild.setDescription((out.length() > 0) ? "You can receive the following items through dailies:\n\n"+out : "No daily item has been registered!").build()).queue();
			}
			else{
				e.getTextChannel().sendMessage("**"+e.getMember().getAsMention()+" Something went wrong, please recheck the syntax and try again!**").queue();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		RankingDB.clearAllVariables();
		ServerRoles.clearAllVariables();
		SqlConnect.clearAllVariables();
	}

	@Override
	public String help() {
		return null;
	}

}
