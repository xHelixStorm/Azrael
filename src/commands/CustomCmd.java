package commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.youtube.model.SearchListResponse;

import core.UserPrivs;
import enums.Channel;
import enums.CommandAction;
import enums.Translation;
import fileManagement.GuildIni;
import google.GoogleYoutube;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import util.STATIC;

public class CustomCmd implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(CustomCmd.class);
	private final static String YOUTUBEENDPOINT = "https://www.youtube.com/watch?v=";

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		return true;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		//Retrieve the command name only without prefix
		final String cmd = e.getMessage().getContentRaw().split(" ")[0].substring(GuildIni.getCommandPrefix(e.getGuild().getIdLong()).length());
		final var command = Azrael.SQLgetCustomCommand(e.getGuild().getIdLong(), cmd);
		if(command != null && command.isEnabled()) {
			if(UserPrivs.comparePrivilege(e.getMember(), command.getLevel()) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {		
				//channel restrictions check
				HashSet<String> restrictions = Azrael.SQLgetCustomCommandRestrictions(e.getGuild().getIdLong(), command.getCommand());
				if(restrictions != null && restrictions.size() > 0) {
					ArrayList<String> restrictedChannels = new ArrayList<String>();
					final var channels = Azrael.SQLgetChannels(e.getGuild().getIdLong());
					if(channels != null) {
						restrictions.forEach(restrChannel -> {
							if(restrChannel.length() == 3) {
								final var thisChannels = channels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(restrChannel)).collect(Collectors.toList());
								for(final var curChannel : thisChannels) {
									if(!restrictedChannels.contains(curChannel.getChannel_ID().toString())) {
										restrictedChannels.add(curChannel.getChannel_ID().toString());
									}
								}
							}
							else {
								TextChannel textChannel = e.getGuild().getTextChannelById(restrChannel);
								if(textChannel != null && !restrictedChannels.contains(textChannel.getId())) {
									restrictedChannels.add(textChannel.getId());
								}
							}
						});
						if(!restrictedChannels.contains(e.getChannel().getId())) {
							e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.NOT_BOT_CHANNEL)+STATIC.getRestrictedChannels(restrictedChannels)).queue();
							return;
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Registered channels couldn't be retrieved for command {} in guild {}", command.getCommand(), e.getGuild().getId());
						return;
					}
				}
				else if(restrictions == null) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Command restrictions couldn't be retrieved for command {} in guild {}", command.getCommand(), e.getGuild().getId());
					return;
				}
				
				if(command.getAction().inputRequired && args.length == 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
					return;
				}
				
				//default output message formatting
				String out = command.getOutput();
				if(out == null) {
					out = "";
				}
				else {
					out = out.replaceAll("\\{user\\}", e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator())
						.replaceAll("\\{username\\}", e.getMember().getUser().getName())
						.replaceAll("\\{discriminator\\}", e.getMember().getUser().getDiscriminator())
						.replaceAll("\\{user_mention\\}", e.getMember().getAsMention())
						.replaceAll("\\{nickname\\}", e.getMember().getEffectiveName())
						.replaceAll("\\{avatar\\}", e.getMember().getUser().getEffectiveAvatarUrl())
						.replaceAll("\\{user_id\\}", e.getMember().getUser().getId())
						.replaceAll("\\{server\\}", e.getGuild().getName())
						.replaceAll("\\{server_id\\}", e.getGuild().getId())
						.replaceAll("\\{server_icon\\}", e.getGuild().getIconUrl())
						.replaceAll("\\{server_members\\}", ""+e.getGuild().getMemberCount())
						.replaceAll("\\{server_created\\}", e.getGuild().getTimeCreated().toString())
						.replaceAll("\\{boost_count\\}", ""+e.getGuild().getBoostCount())
						.replaceAll("\\{channel\\}", e.getChannel().getName())
						.replaceAll("\\{channel_mention\\}", e.getChannel().getAsMention())
						.replaceAll("\\{channel_id\\}", e.getChannel().getId());
				}
				
				//Fetch single youtube video request
				if(command.getAction() == CommandAction.YOUTUBE) {
					String input = "";
					for(String arg : args) {
						input += arg+" ";
					}
					
					SearchListResponse response = null;
					try {
						response = GoogleYoutube.searchYouTubeVideo(GoogleYoutube.getService(), input.trim(), 1L);
					} catch (Exception e1) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("YouTube query couldn't be executed for command {} in guild {}", command.getCommand(), e.getGuild().getId(), e1);
						return;
					}
					
					if(response != null && response.getItems().size() > 0) {
						final var snippet = response.getItems().get(0).getSnippet();
						out = out.replaceAll("\\{youtube_url\\}", YOUTUBEENDPOINT+response.getItems().get(0).getId().getVideoId())
							.replaceAll("\\{youtube_title\\}", snippet.getTitle())
							.replaceAll("\\{youtube_description\\}", snippet.getDescription())
							.replaceAll("\\{youtube_channel\\}", snippet.getChannelTitle())
							.replaceAll("\\{youtube_published\\}", snippet.getPublishedAt().toString());
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.YOUTUBE_VIDEO_NOT_FOUND)).build()).queue();
						return;
					}
				}
				//Assign or remove role(s) to user
				else if(command.getAction() == CommandAction.ROLEADD || command.getAction() == CommandAction.ROLEREMOVE) {
					final ArrayList<Long> roles = Azrael.SQLgetCustomCommandRoles(e.getGuild().getIdLong(), command.getCommand());
					if(roles != null && roles.size() > 0) {
						ArrayList<Role> verifiedRoles = new ArrayList<Role>();
						for(final long role : roles) {
							final Role serverRole = e.getGuild().getRoleById(role);
							if(serverRole != null) {
								if(e.getGuild().getSelfMember().canInteract(serverRole)) {
									verifiedRoles.add(serverRole);
								}
							}
						}
						if(verifiedRoles.size() > 0) {
							if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
								String roleName = "";
								String roleId = "";
								String roleMention = "";
								
								for(final Role role : verifiedRoles) {
									if(roleName.length() == 0) {
										roleName = role.getName();
										roleId = role.getId();
										roleMention = role.getAsMention();
									}
									else {
										roleName += ", "+role.getName();
										roleId += ", "+role.getId();
										roleMention += ", "+role.getAsMention();
									}
									
									if(command.getAction() == CommandAction.ROLEADD)
										e.getGuild().addRoleToMember(e.getMember(), role).queue();
									else
										e.getGuild().removeRoleFromMember(e.getMember(), role).queue();
								}
								
								out = out.replaceAll("\\{roles\\}", roleName)
									.replaceAll("\\{role_ids\\}", roleId)
									.replaceAll("\\{role_mentions\\}", roleMention);
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MANAGE_ROLES.getName()).build()).queue();
								logger.error("Permission MANAGE_ROLES required to assign or remove roles in guild {}", e.getGuild().getId());
								return;
							}
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROLES_NOW_INVALID)).build()).queue();
							return;
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Roles to assign couldn't be retrieved for custom command {} in guild {}", command.getCommand(), e.getGuild().getId());
						return;
					}
				}
				
				TextChannel textChannel = e.getChannel();
				if(command.getTargetChannel() > 0) {
					textChannel = e.getGuild().getTextChannelById(command.getTargetChannel());
					if(textChannel == null)
						textChannel = e.getChannel();
				}
				
				if(out.trim().length() > 0) {
					if(e.getGuild().getSelfMember().hasPermission(textChannel, Permission.MESSAGE_WRITE) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.MESSAGE_WRITE)))
						e.getGuild().getTextChannelById(textChannel.getIdLong()).sendMessage(out).queue();
					else 
						STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_WRITE.getName())+textChannel.getAsMention(), Channel.LOG.getType());
				}
			}
			else {
				UserPrivs.throwNotEnoughPrivilegeError(e, command.getLevel());
			}
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used the custom command {} in guild {}", e.getMember().getUser().getId(), e.getMessage().getContentRaw().split(" ")[0].toLowerCase(), e.getGuild().getId());
	}

}
