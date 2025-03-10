package de.azrael.commands;

import java.awt.Color;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.constructors.CompMap;
import de.azrael.constructors.Member;
import de.azrael.constructors.Room;
import de.azrael.enums.Channel;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.sql.Competitive;
import de.azrael.util.Hashes;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Join implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Join.class);

	@Override
	public boolean called(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.JOIN);
	}

	@Override
	public boolean action(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		if(profilePage(e, false)) {
			joinMatchmaking(e, args, botConfig);
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, MessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Join command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.JOIN.getColumn(), out.toString().trim());
		}
	}
	
	public static boolean profilePage(MessageReceivedEvent e, boolean clanCheck) {
		//retrieve all registered bot channels and check if the current channel is registered
		final var all_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong());
		
		if(clanCheck) {
			var clan_channels = all_channels.parallelStream().filter(f -> f.getChannel_Type() != null && (f.getChannel_Type().equals(Channel.CO3.getType()) || f.getChannel_Type().equals(Channel.CO6.getType()))).collect(Collectors.toList());
			var this_channel = clan_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null);
			if(this_channel == null && clan_channels.size() > 0) {
				e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.WRONG_CHANNEL)+STATIC.getChannels(clan_channels)).queue();
				return false;
			}
		}
		
		var bot_channels = all_channels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.BOT.getType())).collect(Collectors.toList());
		var com_channels = all_channels.parallelStream().filter(f -> f.getChannel_Type() != null && (f.getChannel_Type().equals(Channel.CO1.getType()) || f.getChannel_Type().equals(Channel.CO2.getType()) || f.getChannel_Type().equals(Channel.CO4.getType()) || f.getChannel_Type().equals(Channel.CO5.getType()))).collect(Collectors.toList());
		var this_bot_channel = bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null);
		var this_com_channel = com_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null);
		
		//if any bot channels are registered and if the current channel isn't a bot channel and no competitive channel exists, then throw a message that this command can't be executed
		if(com_channels.size() == 0 && this_bot_channel == null && bot_channels.size() > 0) {
			e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.NOT_BOT_CHANNEL)+STATIC.getChannels(bot_channels)).queue();
			return false;
		}
		//do the same for competitive channels
		else if(this_com_channel == null && com_channels.size() > 0) {
			e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.WRONG_CHANNEL)+STATIC.getChannels(com_channels)).queue();
			return false;
		}
		
		
		final var result = Competitive.SQLUserStatExists(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong());
		if(result == 1) {
			//return true if there are no problems with the user profile
			final var server = Competitive.SQLgetServerFromUserStat(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong());
			if(server != null) {
				return true;
			}
			else {
				final var result2 = Competitive.SQLgetCompServers(e.getGuild().getIdLong());
				if(result2 == null) {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Registered servers couldn't be retrieved in guild {}", e.getGuild().getId());
					return false;
				}
				else if(result2.size() == 0) {
					return true;
				}
				else {
					StringBuilder out = new StringBuilder();
					for(int i = 0; i < result2.size(); i++) {
						if(i == 0)
							out.append("**"+result2.get(i)+"**");
						else
							out.append(", **"+result2.get(i)+"**");
					}
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.JOIN_SERVER_MISSING)+out.toString()).build()).queue();
					Hashes.addTempCache("userProfile_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "server").setObject(result2));
					return false;
				}
			}
		}
		else if(result == 0) {
			//create a competitive user profile
			final var result2 = Competitive.SQLgetCompServers(e.getGuild().getIdLong());
			if(result2 != null && result2.size() == 0) {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.JOIN_NO_PROFILE)).build()).queue();
				Hashes.addTempCache("userProfile_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "name").setObject(result2));
			}
			else if(result2 != null && result2.size() > 0) {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.JOIN_NO_PROFILE_2)).build()).queue();
				Hashes.addTempCache("userProfile_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "name").setObject(result2));
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Registered servers couldn't be retrieved in guild {}", e.getGuild().getId());
			}
			return false;
		}
		else {
			return false;
		}
	}

	
	public static void joinMatchmaking(MessageReceivedEvent e, String [] args, BotConfigs botConfig) {
		//join an existing matchmaking room
		final var this_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null);
		if(this_channel != null && (this_channel.getChannel_Type().equals(Channel.CO1.getType()) || this_channel.getChannel_Type().equals(Channel.CO2.getType()) || this_channel.getChannel_Type().equals(Channel.CO4.getType()) || this_channel.getChannel_Type().equals(Channel.CO5.getType()))) {
			final String channelType = this_channel.getChannel_Type();
			switch(channelType) {
				case "co1", "co4" -> {
					//regular matchmaking room
					join(e, 1, botConfig);
				}
				case "co2", "co5" -> {
					//matchmaking room with picking
					join(e, 2, botConfig);
				}
			}
		}
		else if(this_channel != null) {
			//run help first, since nothing has been defined
			if(args.length == 0) {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.JOIN_HELP)
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_NORMAL))
						.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_PICKING))).build()).queue();
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_NORMAL))) {
				join(e, 1, botConfig);
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PICKING))) {
				join(e, 2, botConfig);
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
	
	private static void join(MessageReceivedEvent e, int type, BotConfigs botConfig) {
		final var room = Competitive.SQLgetMatchmakingRoom(e.getGuild().getIdLong(), type, 1);
		if(room != null && room.getRoomID() != 0 && (room.getLastJoined().getTime()+1800000)-System.currentTimeMillis() > 0) {
			//verify that the user hasn't already joined a different room type
			final int result = Competitive.SQLisUserInRoom(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong());
			if(result == 0) {
				//Consider the restriction settings
				final int members = room.getMemberLimit();
				if(members > 0) {
					//check if the queue is already full
					if(room.getMembers()+1 <= members) {
						//retrieve the username before joining the queue
						final String username = Competitive.SQLgetUsernameFromUserStats(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong());
						if(username != null) {
							//join the queue
							if(Competitive.SQLJoinRoom(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), room.getRoomID()) > 0) {
								e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.JOIN_QUEUE).replaceFirst("\\{\\}", username).replaceFirst("\\{\\}", ""+room.getRoomID()).replaceFirst("\\{\\}", ""+(room.getMembers()+1)).replace("{}", ""+members)).build()).queue();
								if(room.getMembers()+1 == members) {
									//queue is full. Make preparations to start the match
									queueFull(e, room, members, botConfig);
								}
							}
							else {
								e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("Queue of room {} couldn't be joined in guild {}", room.getRoomID(), e.getGuild().getId());
							}
						}
						else {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("Username couldn't be retrieved for user {} in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
						}
					}
					else {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.JOIN_QUEUE_FULL)).build()).queue();
					}
				}
				else if(members == 0) {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.JOIN_ERR_12)).build()).queue();
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Matchmaking member limit couldn't be retrieved in guild {}", e.getGuild().getId());
				}
			}
			else if(result > 0) {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.JOIN_ERR_11)).build()).queue();
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("It couldn't be verified if user {} has already joined a room in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			}
		}
		else if(room != null) {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.JOIN_ERR_10)).build()).queue();
		}
		else {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("It couldn't be verified if an already existing matchmaking room exists with type {} in guild {}", type, e.getGuild().getId());
		}
	}
	
	static void queueFull(MessageReceivedEvent e, Room room, int roomLimit, BotConfigs botConfig) {
		final var queue = Competitive.SQLgetMatchmakingMembers(e.getGuild().getIdLong(), room.getRoomID());
		if(queue != null && queue.size() > 0) {
			//shuffle the queue
			Collections.shuffle(queue);
			
			switch(room.getType()) {
				case 1 -> {
					//calculate team sizes before splitting members into teams
					int teamSize = 0;
					if(roomLimit % 2 != 0) {
						teamSize = (roomLimit/2)+1;
					}
					else {
						teamSize = roomLimit/2;
					}
					
					//create arrays, split members into teams and find out which server should be played on.
					Set<Member> sortedMembers = new HashSet<Member>();
					Map<String, Integer> serverCount = new HashMap<String, Integer>();
					Member [] team1 = new Member[teamSize];
					Member [] team2 = new Member[teamSize];
					Member master = null;
					int pointer1 = 0;
					int pointer2 = 0;
					int team1Elo = 0;
					int team2Elo = 0;
					for(int i = 0; i < queue.size(); i++) {
						Member currentMember = null;
						for(final var member : queue) {
							if(member.isMaster()) {
								master = member;
							}
							if(!sortedMembers.contains(member)) {
								if(currentMember == null)
									currentMember = member;
								else if(member.getElo() > currentMember.getElo()) {
									currentMember = member;
								}
							}
						}
						if(currentMember != null) {
							if(team1Elo <= team2Elo && (teamSize-1) >= pointer1) {
								team1[pointer1] = currentMember;
								team1Elo += currentMember.getElo();
								pointer1++;
							}
							else if(team2Elo > team1Elo && (teamSize-1) >= pointer2) {
								team2[pointer2] = currentMember;
								team2Elo += currentMember.getElo();
								pointer2++;
							}
							else {
								if(pointer1 < pointer2) {
									team1[pointer1] = currentMember;
									team1Elo += currentMember.getElo();
									pointer1++;
								}
								else {
									team2[pointer2] = currentMember;
									team2Elo += currentMember.getElo();
									pointer2++;
								}
							}
							sortedMembers.add(currentMember);
							if(currentMember.getServer() != null) {
								if(!serverCount.containsKey(currentMember.getServer())) {
									serverCount.put(currentMember.getServer(), 1);
								}
								else {
									serverCount.put(currentMember.getServer(), (serverCount.get(currentMember.getServer())+1));
								}
							}
							currentMember = null;
						}
					}
					
					//calculate average ELO of both teams
					int avgEloTeam1 = 0;
					int avgEloTeam2 = 0;
					if(team1Elo != 0)
						avgEloTeam1 = team1Elo/pointer1;
					if(team2Elo != 0)
						avgEloTeam2 = team2Elo/pointer2;
					
					//Retrieve the suggested server to play on
					String server = null;
					int thisServerCount = 0;
					Iterator<Entry<String, Integer>> it = serverCount.entrySet().iterator();
					while(it.hasNext()) {
						@SuppressWarnings("rawtypes")
						Map.Entry pair = (Map.Entry)it.next();
						if(server == null) {
							server = (String)pair.getKey();
							thisServerCount = (Integer)pair.getValue();
						}
						else if((Integer)pair.getValue() > thisServerCount) {
							server = (String)pair.getKey();
							thisServerCount = (Integer)pair.getValue();
						}
						it.remove();
					}
					
					//Retrieve the map to play if set
					CompMap map = null;
					if(room.getMapID() != 0) {
						map = Competitive.SQLgetMap(room.getMapID());
						if(map == null) {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_ERR_4)).build()).queue();
							logger.error("Map details couldn't be retrieved for map {} in guild {}", room.getMapID(), e.getGuild().getId());
						}
					}
					
					//assign the teams on table and update room status to running
					if(Competitive.SQLUpdateTeams(e.getGuild().getIdLong(), room.getRoomID(), team1, team2) > 0) {
						EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
						message.setTitle(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_ROOM)+"#"+room.getRoomID()+(server != null ? " - "+server : ""));
						if(map != null)
							message.setDescription(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_MAP)+"*"+map.getName()+"*").setThumbnail(map.getImage());
						StringBuilder team1Out = new StringBuilder();
						StringBuilder team2Out = new StringBuilder();
						for(int i = 0; i < team1.length; i++) {
							if(team1[i] != null) {
								String elo = ""+team1[i].getElo();
								elo = StringUtils.rightPad(elo, 4);
								team1Out.append("`"+elo+"` "+team1[i].getUsername()+"\n");
							}
						}
						for(int i = 0; i < team2.length; i++) {
							if(team2[i] != null) {
								String elo = ""+team2[i].getElo();
								elo = StringUtils.rightPad(elo, 4);
								team2Out.append("`"+elo+"` "+team2[i].getUsername()+"\n");
							}
						}
						final String teamName1 = botConfig.getCompetitiveTeam1Name();
						final String teamName2 = botConfig.getCompetitiveTeam2Name();
						message.addField((teamName1.length() > 0 ? teamName1 : STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_TEAM_1)), team1Out.toString()+STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_AVG_ELO)+avgEloTeam1, true);
						message.addField((teamName2.length() > 0 ? teamName2 : STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_TEAM_2)), team2Out.toString()+STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_AVG_ELO)+avgEloTeam2, true);
						if(master != null)
							message.addField(STATIC.getTranslation(e.getMember(), Translation.MASTER_TITLE), ":crown: "+master.getUsername(), false);
						if(room.getMapID() != 0)
							message.addField(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_COMMANDS), STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_CHANGEMAP)+"`"+botConfig.getCommandPrefix()+"changemap`", false);
						e.getChannel().sendMessageEmbeds(message.build()).queueAfter(3, TimeUnit.SECONDS, m -> {
							Competitive.SQLUpdateRoomMessageID(e.getGuild().getIdLong(), room.getRoomID(), e.getChannel().getIdLong(), m.getIdLong());
						});
					}
					else {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Teams for room {} couldn't be updated in guild {}", room.getRoomID(), e.getGuild().getId());
					}
				}
				case 2 -> {
					//retrieve the 2 members with the biggest ELO to make them captains
					Map<String, Integer> serverCount = new HashMap<String, Integer>();
					Member captain1 = null;
					Member captain2 = null;
					Member master = null;
					for(final var member : queue) {
						if(member.isMaster()) {
							master = member;
						}
						if(captain1 == null) {
							captain1 = member;
						}
						else if(member.getElo() > captain1.getElo()) {
							captain2 = captain1;
							captain1 = member;
						}
						else if(captain2 == null) {
							captain2 = member;
						}
						if(!serverCount.containsKey(member.getServer()))
							serverCount.put(member.getServer(), 1);
						else
							serverCount.put(member.getServer(), (serverCount.get(member.getServer())+1));
					}
					
					//get all users to list that are not captain
					final var finalCaptain1 = captain1;
					final var finalCaptain2 = captain2;
					final var pickQueue = queue.parallelStream().filter(f -> !f.equals(finalCaptain1) && !f.equals(finalCaptain2)).collect(Collectors.toList());
					
					//Retrieve the suggested server to play on
					String server = null;
					int thisServerCount = 0;
					Iterator<Entry<String, Integer>> it = serverCount.entrySet().iterator();
					while(it.hasNext()) {
						@SuppressWarnings("rawtypes")
						Map.Entry pair = (Map.Entry)it.next();
						if(server == null) {
							server = (String)pair.getKey();
							thisServerCount = (Integer)pair.getValue();
						}
						else if((Integer)pair.getValue() > thisServerCount) {
							server = (String)pair.getKey();
							thisServerCount = (Integer)pair.getValue();
						}
						it.remove();
					}
					
					//Retrieve the map to play if set
					CompMap map = null;
					if(room.getMapID() != 0) {
						map = Competitive.SQLgetMap(room.getMapID());
						if(map == null) {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_ERR_4)).build()).queue();
							logger.error("Map details couldn't be retrieved for map {} in guild {}", room.getMapID(), e.getGuild().getId());
						}
					}
					
					if(Competitive.SQLUpdateTeams(e.getGuild().getIdLong(), room.getRoomID(), finalCaptain1.getUserID(), finalCaptain2.getUserID()) > 0) {
						EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
						message.setTitle(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_ROOM)+"#"+room.getRoomID()+(server != null ? " - "+server : ""));
						if(map != null)
							message.setDescription(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_MAP)+"*"+map.getName()+"*").setThumbnail(map.getImage());
						final String teamName1 = botConfig.getCompetitiveTeam1Name();
						final String teamName2 = botConfig.getCompetitiveTeam2Name();
						message.addField((teamName1.length() > 0 ? teamName1 : STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_TEAM_1)), ":crown: "+finalCaptain1.getUsername(), false);
						message.addField((teamName2.length() > 0 ? teamName2 : STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_TEAM_2)), ":crown: "+finalCaptain2.getUsername(), false);
						message.addField(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_PICKING), finalCaptain1.getUsername(), false);
						StringBuilder out = new StringBuilder();
						for(int i = 0; i < pickQueue.size(); i++) {
							if(i == 0)
								out.append(pickQueue.get(i).getUsername());
							else
								out.append(", "+pickQueue.get(i).getUsername());
						}
						message.addField(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_PLAYER_POOL), out.toString(), false);
						if(master != null)
							message.addField(STATIC.getTranslation(e.getMember(), Translation.MASTER_TITLE), ":crown: "+master.getUsername(), false);
						message.addField(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_COMMANDS), (room.getMapID() != 0 ? STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_CHANGEMAP)+"`"+botConfig.getCommandPrefix()+"changemap`\n"+STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_PICK_USER)+"`" : "")+botConfig.getCommandPrefix()+"pick "+STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_USERNAME)+"`", false);
						e.getChannel().sendMessageEmbeds(message.build()).queueAfter(3, TimeUnit.SECONDS, m -> {
							Competitive.SQLUpdateRoomMessageID(e.getGuild().getIdLong(), room.getRoomID(), e.getChannel().getIdLong(), m.getIdLong());
						});
					}
					else {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Leaders for room {} couldn't be updated in guild {}", room.getRoomID(), e.getGuild().getId());
					}
				}
			}
		}
		else {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Users in the room {} couldn't be retrieved in guild {}", room.getRoomID(), e.getGuild().getId());
		}
	}
}
