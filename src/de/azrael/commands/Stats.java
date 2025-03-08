package de.azrael.commands;

import java.awt.Color;
import java.util.stream.Collectors;

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

public class Stats implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Stats.class);

	@Override
	public boolean called(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.STATS);
	}

	@Override
	public boolean action(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.BOT.getType())).collect(Collectors.toList());
		var this_channel = bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null);
		
		if(this_channel == null && bot_channels.size() > 0) {
			e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.NOT_BOT_CHANNEL)+STATIC.getChannels(bot_channels)).queue();
			return true;
		}
		
		final var member = Competitive.SQLgetUserStats(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong());
		if(member != null && member.getUserID() != 0) {
			if(args.length == 0) {
				final var ranking = Competitive.SQLgetRanking(e.getGuild().getIdLong());
				if(ranking != null && ranking.size() > 0) {
					EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
					message.setTitle(STATIC.getTranslation(e.getMember(), Translation.STATS_TITLE).replace("{}", member.getName()));
					long ratio = 100;
					if(member.getGames() != 0)
						ratio = Math.round(((double)member.getWins()/(double)member.getGames())*100);
					final String rank = ranking.parallelStream().filter(f -> f.split("-")[0].equals(""+member.getUserID())).findAny().orElse(null);
					message.setDescription(STATIC.getTranslation(e.getMember(), Translation.STATS_RANK)+(rank != null ? (int)Double.parseDouble(rank.split("-")[1]) : "0")+" ("+member.getElo()+")\n"+STATIC.getTranslation(e.getMember(), Translation.STATS_GAMES)+member.getGames()+"\n"+STATIC.getTranslation(e.getMember(), Translation.STATS_WINS)+member.getWins()+"\n"+STATIC.getTranslation(e.getMember(), Translation.STATS_LOSSES)+member.getLosses()+(member.getServer() != null ? "\n"+STATIC.getTranslation(e.getMember(), Translation.STATS_SERVER)+member.getServer() : "")+"\n"+STATIC.getTranslation(e.getMember(), Translation.STATS_WL_RATIO)+ratio+"%");
					message.addField(STATIC.getTranslation(e.getMember(), Translation.ROOM_PARAMETERS), "`"+STATIC.getTranslation(e.getMember(), Translation.PARAM_RENAME)+(member.getServer() != null ? "`\n`"+STATIC.getTranslation(e.getMember(), Translation.PARAM_SERVER)+"`" : ""), false);
					e.getChannel().sendMessageEmbeds(message.build()).queue();
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Ranking couldn't be retrieved in guild {}", e.getGuild().getId());
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_RENAME))) {
				//promt to rename user
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.STATS_RENAME_HELP)).build()).queue();
			}
			else if(args.length == 2 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_RENAME))) {
				//rename
				final String newName = args[1];
				if(newName.length() <= 20) {
					final int nameTaken = Competitive.SQLisNameTaken(e.getGuild().getIdLong(), newName);
					if(nameTaken == 0) {
						//name is free
						if(Competitive.SQLUpdateNameInUserStats(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), newName) > 0) {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.STATS_RENAME).replace("{}", newName)).build()).queue();
						}
						else {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("Username couldn't be updated for user {} in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
						}
					}
					else if(nameTaken == 1) {
						//name already taken
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.STATS_RENAME_ERR_2)).build()).queue();
					}
					else {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("It couldn't be verified if the name {} is already taken in guild {}", newName, e.getGuild().getId());
					}
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.STATS_RENAME_ERR)).build()).queue();
				}
			}
			else if(member.getServer() != null && args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_SERVER))) {
				//promt to change main server if available
				StringBuilder out = new StringBuilder();
				final var servers = Competitive.SQLgetCompServers(e.getGuild().getIdLong());
				if(servers != null && servers.size() > 0) {
					for(final var server : servers) {
						if(out.length() == 0)
							out.append("**"+server+"**");
						else
							out.append(", **"+server+"**");
					}
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.STATS_SERVER_HELP)+out.toString()).build()).queue();
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Servers couldn't be retrieved in guild {}", e.getGuild().getId());
				}
			}
			else if(member.getServer() != null && args.length == 2 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_SERVER))) {
				//change server
				final var servers = Competitive.SQLgetCompServers(e.getGuild().getIdLong());
				if(servers != null && servers.size() > 0) {
					final String server = args[1].toUpperCase();
					if(servers.parallelStream().filter(f -> f.equalsIgnoreCase(server)).findAny().orElse(null) != null) {
						if(Competitive.SQLUpdateSelectedServerInUserStats(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), server) > 0) {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.STATS_SERVER_UPDATE).replace("{}", server)).build()).queue();
						}
						else {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("Server couldn't be updated for user {} in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
						}
					}
					else {
						//server doesn't exist
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.STATS_SERVER_ERR)).build()).queue();
					}
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Servers couldn't be retrieved in guild {}", e.getGuild().getId());
				}
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
		}
		else if(member != null) {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.STATS_ERR)).build()).queue();
		}
		else {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("User stats couldn't be retrieved for user {} and guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, MessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Stats command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.STATS.getColumn(), out.toString().trim());
		}
	}

}
