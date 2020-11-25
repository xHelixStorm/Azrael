package commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.youtube.model.SearchListResponse;

import core.UserPrivs;
import enums.CommandAction;
import enums.Translation;
import fileManagement.GuildIni;
import google.GoogleYoutube;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
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
						if(restrictedChannels.contains(e.getChannel().getId())) {
							e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.NOT_BOT_CHANNEL)+STATIC.getRestrictedChannels(restrictedChannels)).queue();
							return;
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Registered channels couldn't be retrieved in guild {}", e.getGuild().getId());
					}
				}
				else if(restrictions == null) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Command restrictions couldn't be retrieved in guild {}", e.getGuild().getId());
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
						response = GoogleYoutube.searchYouTubeVideo(GoogleYoutube.getService(), input.trim(), 1);
					} catch (Exception e1) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("YouTube query couldn't be executed for guild {}", e.getGuild().getId(), e1);
						return;
					}
					
					if(response != null && response.getItems().size() > 0) {
						//TODO: further format the output message with more options
						out = out.replaceAll("\\{\\}", YOUTUBEENDPOINT+response.getItems().get(0).getId().getVideoId())
							.replaceAll("\\{youtube_title\\}", response.getItems().get(0).getSnippet().getTitle())
							.replaceAll("\\{youtube_description\\}", response.getItems().get(0).getSnippet().getDescription())
							.replaceAll("\\{youtube_channel\\}", response.getItems().get(0).getSnippet().getChannelTitle())
							.replaceAll("\\{youtube_published\\}", response.getItems().get(0).getSnippet().getPublishedAt().toString());
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("YouTube video couldn't be found!").build()).queue();
					}
				}
				
				TextChannel textChannel = e.getChannel();
				if(command.getTargetChannel() > 0) {
					textChannel = e.getGuild().getTextChannelById(command.getTargetChannel());
					if(textChannel == null)
						textChannel = e.getChannel();
				}
				
				e.getGuild().getTextChannelById(textChannel.getIdLong()).sendMessage(out);
			}
			else {
				UserPrivs.throwNotEnoughPrivilegeError(e, command.getLevel());
			}
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used the custom command {} in guild {}", e.getMember().getUser().getId(), e.getMessage().getContentRaw().split(" ")[0].toUpperCase(), e.getGuild().getId());
	}

}
