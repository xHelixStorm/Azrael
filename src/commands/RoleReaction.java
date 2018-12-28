package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import core.UserPrivs;
import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import preparedMessages.ReactionMessage;
import sql.Azrael;

public class RoleReaction implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		//after a channel has been registered for self role assignment, it can be disabled and enabled with this command
		if(IniFileReader.getRoleReactionCommand()) 
			if(UserPrivs.isUserAdmin(e.getMember().getUser(), e.getGuild().getIdLong()) == true || UserPrivs.isUserMod(e.getMember().getUser(), e.getGuild().getIdLong()) || e.getMember().getUser().getIdLong() == IniFileReader.getAdmin()) {
				if(e.getMessage().getContentRaw().substring(IniFileReader.getCommandPrefix().length()+13).equals("enable")) {
					Logger logger = LoggerFactory.getLogger(RoleReaction.class);
					logger.info("{} has used RoleReaction command to enable", e.getMember().getUser().getId());
					
					Azrael.SQLgetExecutionID(e.getGuild().getIdLong());
					if(Azrael.getReactions() == true) {
						e.getTextChannel().sendMessage(e.getMember().getUser().getAsMention()+" Role reactions are already enabled!").queue();
					}
					else {
						if(Azrael.SQLgetChannelID(e.getGuild().getIdLong(), "rea")) {							
							Azrael.SQLUpdateReaction(e.getGuild().getIdLong(), true);
							e.getTextChannel().sendMessage("Role Reactions have been enabled!").queue();
							String count = ""+ReactionMessage.print(e, Azrael.getChannelID());
							FileSetting.createFile(IniFileReader.getTempDirectory()+"AutoDelFiles/reaction_gu"+e.getGuild().getId()+"ch"+Azrael.getChannelID()+".azr", count);
						}
						else {
							e.getTextChannel().sendMessage(e.getMember().getUser().getAsMention()+" Please set a reaction channel before continuing!!").queue();
						}
					}
				}
				else if(e.getMessage().getContentRaw().substring(IniFileReader.getCommandPrefix().length()+13).equals("disable")) {
					Logger logger = LoggerFactory.getLogger(RoleReaction.class);
					logger.info("{} has used RoleReaction command to disable", e.getMember().getUser().getId());
					
					Azrael.SQLgetExecutionID(e.getGuild().getIdLong());
					if(Azrael.getReactions() == false) {
						e.getTextChannel().sendMessage(e.getMember().getUser().getAsMention()+" Role reactions are already disabled!").queue();
					}
					else {
						if(Azrael.SQLgetChannelID(e.getGuild().getIdLong(), "rea") && Hashes.getReactionMessage(e.getGuild().getIdLong()) != null) {
							e.getGuild().getTextChannelById(Azrael.getChannelID()).deleteMessageById(Hashes.getReactionMessage(e.getGuild().getIdLong())).queue();
						}
						for(int i = 1; i < 10; i++) {
							if(Hashes.getRoles(i+"_"+e.getGuild().getId()) != null) {
								long role_id = Hashes.getRoles(i+"_"+e.getGuild().getId()).getRole_ID();
								for(Member m : e.getGuild().getMembersWithRoles(e.getGuild().getRoleById(role_id))) {
									e.getGuild().getController().removeSingleRoleFromMember(m, e.getGuild().getRoleById(role_id)).queue();
								}
							}
						}
						Azrael.SQLUpdateReaction(e.getGuild().getIdLong(), false);
						e.getTextChannel().sendMessage("Role reactions have been disabled and the assigned roles have been removed!").queue();
					}
				}
				else {
					e.getTextChannel().sendMessage("Write enable or disable together with the the command to make the reaction message appear or to delete it and remove all self assigned roles!").queue();
				}
			}
			else {
				EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");
				e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. This command can be used only from an Administrator or an Moderator. Here a cookie** :cookie:").build()).queue();
			}
		
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		
	}

	@Override
	public String help() {
		return null;
	}

}
