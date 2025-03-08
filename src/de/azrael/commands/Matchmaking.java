package de.azrael.commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.enums.Channel;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.sql.Competitive;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Matchmaking implements CommandPublic {
	private static Logger logger = LoggerFactory.getLogger(Matchmaking.class);

	@Override
	public boolean called(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.MATCHMAKING);
	}

	@Override
	public boolean action(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		if(Join.profilePage(e, false)) {
			final var this_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null);
			if(this_channel != null && (this_channel.getChannel_Type().equals(Channel.CO1.getType()) || this_channel.getChannel_Type().equals(Channel.CO2.getType()) || this_channel.getChannel_Type().equals(Channel.CO4.getType()) || this_channel.getChannel_Type().equals(Channel.CO5.getType()))) {
				final String channelType = this_channel.getChannel_Type();
				switch(channelType) {
					case "co1", "co4" -> {
						//regular matchmaking room
						regular(e, botConfig);
					}
					case "co2", "co5" -> {
						//matchmaking room with picking
						picking(e, botConfig);
					}
				}
			}
			else if(this_channel != null) {
				//run help first, since nothing has been defined
				if(args.length == 0) {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_HELP)
							.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_NORMAL))
							.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_PICKING))).build()).queue();
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_NORMAL))) {
					regular(e, botConfig);
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PICKING))) {
					picking(e, botConfig);
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Channel type couldn't be retrieved for channel {} in guild {}", e.getChannel().getId(), e.getGuild().getId());
			}
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, MessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Matchmaking command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.MATCHMAKING.getColumn(), out.toString().trim());
		}
	}
	
	private static void regular(MessageReceivedEvent e, BotConfigs botConfig) {
		//check if a regular matchmaking room is already open
		final var room = Competitive.SQLgetMatchmakingRoom(e.getGuild().getIdLong(), 1, 1);
		if(room != null && room.getRoomID() == 0) {
			makeRoom(e, 1, botConfig);
		}
		else if(room != null) {
			if((room.getLastJoined().getTime()+1800000) - System.currentTimeMillis() > 0)
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_ERR)).build()).queue();
			else {
				//delete room so that it can be replaced
				if(Competitive.SQLDeleteMatchmakingRoom(e.getGuild().getIdLong(), room.getRoomID()) > 0) {
					makeRoom(e, 1, botConfig);
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Matchmaking room {} couldn't be deleted in guild {}", room.getRoomID(), e.getGuild().getId());
				}
			}
		}
		else {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("It couldn't be verified if a matchmaking room of type 1 already exists in guild {}", e.getGuild().getId());
		}
	}
	
	private static void picking(MessageReceivedEvent e, BotConfigs botConfig) {
		//check if a picking matchmaking room is already open
		final var room = Competitive.SQLgetMatchmakingRoom(e.getGuild().getIdLong(), 2, 1);
		if(room != null && room.getRoomID() == 0) {
			makeRoom(e, 2, botConfig);
		}
		else if(room != null) {
			if((room.getLastJoined().getTime()+1800000) - System.currentTimeMillis() > 0)
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_ERR_2)).build()).queue();
			else {
				//delete room so that it can be replaced
				if(Competitive.SQLDeleteMatchmakingRoom(e.getGuild().getIdLong(), room.getRoomID()) > 0) {
					makeRoom(e, 2, botConfig);
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Matchmaking room {} couldn't be deleted in guild {}", room.getRoomID(), e.getGuild().getId());
				}
			}
		}
		else {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("It couldn't be verified if a matchmaking room of type 2 already exists in guild {}", e.getGuild().getId());
		}
	}
	
	private static void makeRoom(MessageReceivedEvent e, int type, BotConfigs botConfig) {
		//verify that the user hasn't already joined a different room type
		final int result = Competitive.SQLisUserInRoom(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong());
		if(result == 0) {
			//retrieve a random map if available 
			final var map = Competitive.SQLgetRandomMap(e.getGuild().getIdLong());
			if(map == null) {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Random map couldn't be retrieved in guild {}", e.getGuild().getId());
				return;
			}
			//Retrieve room size limit
			final int members = Competitive.SQLgetMatchmakingMembers(e.getGuild().getIdLong());
			if(members > 1) {
				//create the matchmaking room
				final int room_id = Competitive.SQLCreateMatchmakingRoom(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), type, map.getMapID(), members, e.getChannel().getIdLong());
				if(room_id > 0) {
					EmbedBuilder embed = new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_CREATED).replaceFirst("\\{\\}", ""+room_id).replace("{}", ""+members));
					if(Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong() && (f.getChannel_Type().equals(Channel.CO4.getType()) || f.getChannel_Type().equals(Channel.CO5.getType()))).findAny().orElse(null) != null) {
						embed.addField(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_COMMANDS), botConfig.getCommandPrefix()+"master\n"+botConfig.getCommandPrefix()+"restrict\n"+botConfig.getCommandPrefix()+"start", false);
					}
					e.getChannel().sendMessageEmbeds(embed.build()).queue();
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Matchmaking room couldn't be created in guild {}", e.getGuild().getId());
				}
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.JOIN_ERR_12)).build()).queue();
			}
		}
		else if(result > 0) {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_ERR_3)).build()).queue();
		}
		else {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("It couldn't be verified if user {} has already joined a room in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
		}
	}
}
