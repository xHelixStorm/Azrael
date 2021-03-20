package de.azrael.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.CompMap;
import de.azrael.constructors.Member;
import de.azrael.constructors.Room;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.GuildIni;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Competitive;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Changemap implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Changemap.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		return true;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		final int room_id = Competitive.SQLisUserInRoom(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong());
		if(room_id > 0) {
			final var room = Competitive.SQLgetMatchmakingRoom(e.getGuild().getIdLong(), room_id);
			if(room != null && room.getRoomID() != 0) {
				//confirm that its the same channel where the queue has been filled, that the room status is 2 and that maps are registered
				if(room.getStatus() == 2 && room.getChannelID() == e.getChannel().getIdLong() && room.getMapID() != 0  && (room.getLastJoined().getTime()+1800000)-System.currentTimeMillis() > 0) {
					//verify that the user is the master of the room
					final var member = Competitive.SQLRetrieveMember(e.getGuild().getIdLong(), room.getRoomID(), room.getStatus(), e.getMember().getUser().getIdLong());
					if(member != null) {
						if(member.isMaster() || room.getType() == 3) {
							//Staff and owners of clans are able to use this command as well during Clan Wars
							if(room.getType() == 3) {
								final var clanMemberLevel = Competitive.SQLgetClanMemberLevel(e.getMember().getUser().getIdLong(), room.getRoomID());
								if(clanMemberLevel == 1) {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.CW_ERR_7)).build()).queue();
									return;
								}
								else if(clanMemberLevel < 1) {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("Clan Member Level of user {} couldn't be retrieved in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
									return;
								}
							}
							
							CompMap map = null;
							if(args.length == 0) {
								//retrieve random map
								map = Competitive.SQLgetRandomMap(e.getGuild().getIdLong());
								if(map == null) {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("Random map couldn't be retrieved in guild {}", e.getGuild().getId());
									return;
								}
							}
							else {
								map = Competitive.SQLgetMap(e.getGuild().getIdLong(), args[0]);
								if(map != null && map.getMapID() == 0) {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CHANGEMAP_ERR_2).replace("{}", args[0])).build()).queue();
									return;
								}
								else if(map == null) {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("Map {} couldn't be retrieved in guild {}", args[0].toLowerCase(), e.getGuild().getId());
									return;
								}
							}
							if(map != null && map.getMapID() != 0) {
								final var selectedMap = map;
								if((e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.MESSAGE_HISTORY) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.MESSAGE_HISTORY))) && room.getType() != 3) {
									//retrieve message from history and replace it
									e.getChannel().retrieveMessageById(room.getMessageID()).queue(m -> {
										MessageEmbed embed = m.getEmbeds().get(0);
										EmbedBuilder message = new EmbedBuilder().setColor(embed.getColor()).setTitle(embed.getTitle());
										for(final var field : embed.getFields()) {
											message.addField(field.getName(), field.getValue(), field.isInline());
										}
										message.setDescription(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_MAP)+"*"+selectedMap.getName()+"*");
										if(selectedMap.getImage() != null)
											message.setThumbnail(selectedMap.getImage());
										m.delete().queue();
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.CHANGEMAP_UPDATE).replace("{}", selectedMap.getName())).build()).queue();
										e.getChannel().sendMessage(message.build()).queueAfter(1, TimeUnit.SECONDS, m2 -> {
											Competitive.SQLUpdateMatchmakingRoomMap(e.getGuild().getIdLong(), room.getRoomID(), selectedMap.getMapID());
											Competitive.SQLUpdateRoomMessageID(e.getGuild().getIdLong(), room.getRoomID(), e.getChannel().getIdLong(), m2.getIdLong());
										});
									}, err -> {
										printMessage(e, room, selectedMap, true);
									});
								}
								else {
									printMessage(e, room, selectedMap, true);
								}
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CHANGEMAP_ERR)).build()).queue();
							}
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.MASTER_ERR).replace("{}", ""+room.getRoomID())).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Information of user {} couldn't be retrieved in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
					}
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("It couldn't be verified if user {} has already joined a room in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			}
		}
		else if(room_id == -1) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("It couldn't be verified if user {} has already joined a room in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used Changemap command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}

	public static void printMessage(GuildMessageReceivedEvent e, Room room, CompMap map, boolean updateMapMessage) {
		//print new message
		final var members = Competitive.SQLgetMatchmakingMembers(e.getGuild().getIdLong(), room.getRoomID());
		if(members != null && members.size() > 0) {
			if(room.getType() == 1) {
				Map<String, Integer> serverCount = new HashMap<String, Integer>();
				StringBuilder team1 = new StringBuilder();
				StringBuilder team2 = new StringBuilder();
				Member master = null;
				int totEloTeam1 = 0;
				int totEloTeam2 = 0;
				int membersTeam1 = 0;
				int membersTeam2 = 0;
				//divide into teams
				for(final var member : members) {
					if(member.isMaster()) {
						master = member;
					}
					String elo = ""+member.getElo();
					elo = StringUtils.rightPad(elo, 4);
					if(member.getTeam() == 1) {
						totEloTeam1 += member.getElo();
						team1.append("`"+elo+"` "+member.getUsername()+"\n");
						membersTeam1++;
					}
					else {
						totEloTeam2 += member.getElo();
						team2.append("`"+elo+"` "+member.getUsername()+"\n");
						membersTeam2++;
					}
					if(!serverCount.containsKey(member.getServer()))
						serverCount.put(member.getServer(), 1);
					else
						serverCount.put(member.getServer(), (serverCount.get(member.getServer())+1));
				}
				
				//calculcate average Elo
				int avgEloTeam1 = 0;
				int avgEloTeam2 = 0;
				if(totEloTeam1 != 0)
					avgEloTeam1 = totEloTeam1/membersTeam1;
				if(totEloTeam2 != 0)
					avgEloTeam2 = totEloTeam2/membersTeam2;
				
				//retrieve the server where players are playing most
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
				
				EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
				message.setTitle(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_ROOM)+"#"+room.getRoomID()+(server != null ? " - "+server : ""));
				if(map != null)
					message.setDescription(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_MAP)+"*"+map.getName()+"*").setThumbnail(map.getImage());
				final String iniTeamName1 = GuildIni.getCompetitiveTeam1(e.getGuild().getIdLong());
				final String iniTeamName2 = GuildIni.getCompetitiveTeam2(e.getGuild().getIdLong());
				message.addField((iniTeamName1.length() > 0 ? iniTeamName1 : STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_TEAM_1)), team1.toString()+STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_AVG_ELO)+avgEloTeam1, true);
				message.addField((iniTeamName2.length() > 0 ? iniTeamName2 : STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_TEAM_2)), team2.toString()+STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_AVG_ELO)+avgEloTeam2, true);
				if(master != null)
					message.addField(STATIC.getTranslation(e.getMember(), Translation.MASTER_TITLE), ":crown: "+master.getUsername(), false);
				if(room.getRoomID() != 0)
					message.addField(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_COMMANDS), STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_CHANGEMAP)+"`"+GuildIni.getCommandPrefix(e.getGuild().getIdLong())+"changemap`", false);
				if(updateMapMessage)
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.CHANGEMAP_UPDATE).replace("{}", map.getName())).build()).queue();
				e.getChannel().sendMessage(message.build()).queueAfter(1, TimeUnit.SECONDS, m -> {
					Competitive.SQLUpdateMatchmakingRoomMap(e.getGuild().getIdLong(), room.getRoomID(), map.getMapID());
					Competitive.SQLUpdateRoomMessageID(e.getGuild().getIdLong(), room.getRoomID(), e.getChannel().getIdLong(), m.getIdLong());
				});
			}
			else if(room.getType() == 2) {
				ArrayList<Member> pickingPool = new ArrayList<Member>();
				Map<String, Integer> serverCount = new HashMap<String, Integer>();
				StringBuilder team1 = new StringBuilder();
				StringBuilder team2 = new StringBuilder();
				Member captain1 = members.parallelStream().filter(f -> f.getTeam() == 1 && f.isLeader()).findAny().orElse(null);
				Member captain2 = members.parallelStream().filter(f -> f.getTeam() == 2 && f.isLeader()).findAny().orElse(null);;
				Member picker = members.parallelStream().filter(f -> f.isLeader() && f.isPicker()).findAny().orElse(null);;
				Member master = null;
				for(final var member : members) {
					if(member.isMaster()) {
						master = member;
					}
					if(!member.equals(captain1) && !member.equals(captain2)) {
						if(member.getTeam() == 1)
							team1.append(", "+member.getUsername());
						else if(member.getTeam() == 2)
							team2.append(", "+member.getUsername());
						else
							pickingPool.add(member);
					}
					if(!serverCount.containsKey(member.getServer()))
						serverCount.put(member.getServer(), 1);
					else
						serverCount.put(member.getServer(), (serverCount.get(member.getServer())+1));
				}
				
				//retrieve the server where players are playing most
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
				
				EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
				message.setTitle(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_ROOM)+"#"+room.getRoomID()+(server != null ? " - "+server : ""));
				if(map != null)
					message.setDescription(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_MAP)+"*"+map.getName()+"*").setThumbnail(map.getImage());
				final String iniTeamName1 = GuildIni.getCompetitiveTeam1(e.getGuild().getIdLong());
				final String iniTeamName2 = GuildIni.getCompetitiveTeam2(e.getGuild().getIdLong());
				message.addField((iniTeamName1.length() > 0 ? iniTeamName1 : STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_TEAM_1)), ":crown: "+captain1.getUsername()+team1.toString(), false);
				message.addField((iniTeamName2.length() > 0 ? iniTeamName2 : STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_TEAM_2)), ":crown: "+captain2.getUsername()+team2.toString(), false);
				message.addField(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_PICKING), picker.getUsername(), false);
				StringBuilder out = new StringBuilder();
				for(int i = 0; i < pickingPool.size(); i++) {
					if(i == 0)
						out.append(pickingPool.get(i).getUsername());
					else
						out.append(", "+pickingPool.get(i).getUsername());
				}
				message.addField(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_PLAYER_POOL), out.toString(), false);
				String commandPrefix = GuildIni.getCommandPrefix(e.getGuild().getIdLong());
				if(updateMapMessage)
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.CHANGEMAP_UPDATE).replace("{}", map.getName())).build()).queue();
				if(master != null)
					message.addField(STATIC.getTranslation(e.getMember(), Translation.MASTER_TITLE), ":crown: "+master.getUsername(), false);
				message.addField(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_COMMANDS), (room.getRoomID() != 0 ? STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_CHANGEMAP)+"`"+commandPrefix+"changemap`\n" : "")+STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_PICK_USER)+"`"+commandPrefix+"pick "+STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_USERNAME)+"`", false);
				e.getChannel().sendMessage(message.build()).queueAfter(1, TimeUnit.SECONDS, m -> {
					Competitive.SQLUpdateRoomMessageID(e.getGuild().getIdLong(), room.getRoomID(), e.getChannel().getIdLong(), m.getIdLong());
				});
			}
			else if(room.getType() == 3) {
				Map<String, Integer> serverCount = new HashMap<String, Integer>();
				StringBuilder team1 = new StringBuilder();
				StringBuilder team2 = new StringBuilder();
				//divide into teams
				for(final var member : members) {
					if(member.getTeam() == 1) {
						team1.append(member.getUsername()+"\n");
					}
					else {
						team2.append(member.getUsername()+"\n");
					}
					if(!serverCount.containsKey(member.getServer()))
						serverCount.put(member.getServer(), 1);
					else
						serverCount.put(member.getServer(), (serverCount.get(member.getServer())+1));
				}
				
				//retrieve the server where players are playing most
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
				
				//retrieve the clan names
				final String clanName1 = Competitive.SQLgetClanName(e.getGuild().getIdLong(), room.getClanID1());
				if(clanName1 == null) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Clan name couldn't be retrieved for clan {} and guild {}", room.getClanID1(), e.getGuild().getId());
				}
				final String clanName2 = Competitive.SQLgetClanName(e.getGuild().getIdLong(), room.getClanID2());
				if(clanName2 == null) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Clan name couldn't be retrieved for clan {} and guild {}", room.getClanID2(), e.getGuild().getId());
				}
				
				EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
				message.setTitle(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_CLAN_WAR)+"#"+room.getRoomID()+(server != null ? " - "+server : ""));
				if(map != null)
					message.setDescription((room.getStatus() == 1 ? STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_NOT_STARTED) : STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_STARTED))+"\n"+STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_MAP)+"*"+map.getName()+"*").setThumbnail(map.getImage());
				message.addField(clanName1, team1.toString(), true);
				message.addField(clanName2, team2.toString(), true);
				final String commandPrefix = GuildIni.getCommandPrefix(e.getGuild().getIdLong());
				message.addField(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_COMMANDS), (room.getStatus() == 1 ? STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_CLAN_WAR_START)+"`"+commandPrefix+"cw "+STATIC.getTranslation(e.getMember(), Translation.PARAM_START)+"`" : STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_CHANGEMAP)+"`"+commandPrefix+"changemap`"), false);
				if(updateMapMessage)
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.CHANGEMAP_UPDATE).replace("{}", map.getName())).build()).queue();
				e.getChannel().sendMessage(message.build()).queueAfter(1, TimeUnit.SECONDS, m -> {
					Competitive.SQLUpdateMatchmakingRoomMap(e.getGuild().getIdLong(), room.getRoomID(), map.getMapID());
					Competitive.SQLUpdateRoomMessageID(e.getGuild().getIdLong(), room.getRoomID(), e.getChannel().getIdLong(), m.getIdLong());
				});
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Users in the room {} couldn't be retrieved for guild {}", room.getRoomID(), e.getGuild().getId());
		}
	}
}
