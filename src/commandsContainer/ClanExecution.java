package commandsContainer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiManager;

import constructors.Cache;
import constructors.Clan;
import constructors.ClanMember;
import core.Hashes;
import enums.Translation;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import sql.Competitive;
import util.STATIC;

public class ClanExecution {
	private final static Logger logger = LoggerFactory.getLogger(ClanExecution.class);
	
	public static void search(GuildMessageReceivedEvent e, String [] args, Cache cache) {
		final var clans = Competitive.SQLgetClans(e.getGuild().getIdLong());
		if(clans != null && clans.size() > 0) {
			//clans found
			var filteredClans = clans;
			if(args.length > 1) {
				String clanName = "";
				for(int i = 1; i < args.length; i++) {
					clanName = clanName+args[i]+" ";
				}
				final String name = clanName.trim();
				filteredClans = (ArrayList<Clan>) filteredClans.parallelStream().filter(f -> f.getName().contains(name)).collect(Collectors.toList());
			}
			if(filteredClans.size() > 0) {
				StringBuilder out = new StringBuilder();
				for(int i = 0; i < filteredClans.size(); i++) {
					final var clan = filteredClans.get(i);
					out.append("**"+clan.getName()+"** ("+clan.getMembers()+")\n");
					if(i == 9) break;
				}
				if(filteredClans.size() > 10) {
					final var fixedClans = filteredClans;
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.CLAN_TITLE)).setDescription(out.toString()).build()).queue(m -> {
						if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.MESSAGE_ADD_REACTION) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.MESSAGE_ADD_REACTION))) {
							m.addReaction(EmojiManager.getForAlias(":arrow_left:").getUnicode()).queue();
							m.addReaction(EmojiManager.getForAlias(":arrow_right:").getUnicode()).queue();
							Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"me"+m.getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "1").setObject(fixedClans));
						}
						else
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_ADD_REACTION.getName())+e.getChannel().getName()).build()).queue();
					});
				}
				else
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.CLAN_TITLE)).setDescription(out.toString()).build()).queue();
			}
			else {
				//clan hasn't been found
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_SEARCH_ERR)).build()).queue();
			}
			Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
		}
		else if(clans != null && clans.size() == 0) {
			//clans haven't been created yet
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_NO_CLANS)).build()).queue();
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Clans couldn't be retrieved in guild {}", e.getGuild().getId());
			Hashes.clearTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		}
	}
	
	public static void apply(GuildMessageReceivedEvent e, String [] args, Cache cache) {
		String name = "";
		for(int i = 1; i < args.length; i++) {
			name = name+args[i]+" ";
		}
		if(name.length() == 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_APPLY_ERR)).build()).queue();
			Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			return;
		}
		final String clanName = name.trim();
		final int clan_id = Competitive.SQLgetClanID(e.getGuild().getIdLong(), clanName);
		if(clan_id > 0) {
			//clan exists
			final var management = Competitive.SQLgetClanManagement(e.getGuild().getIdLong(), clan_id);
			if(management != null && management.size() > 0) {
				//management members found
				final var username = Competitive.SQLgetUsernameFromUserStats(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong());
				if(username != null) {
					//verify if a reservation already exists
					final var result = Competitive.SQLgetClanReservation(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), clan_id);
					if(result != null && (result.getClanID() == 0 || result.getType() != 1 || (result.getType() == 1 && result.isDone()))) {
						//create a reservation
						if(Competitive.SQLInsertClanReservation(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), clan_id, 1, e.getChannel().getIdLong()) > 0) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_APPLY_SUBMITTED)).build()).queue();
							//notify all staff members in private message
							management.parallelStream().forEach(user_id -> {
								e.getGuild().getMemberById(user_id).getUser().openPrivateChannel().queue(channel -> {
									channel.sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.CLAN_APPLY_DM_TITLE)).setFooter(e.getGuild().getId()+"-"+e.getMember().getUser().getId()+"-"+clan_id).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.CLAN_APPLY_DIRECT_MESSAGE).replaceFirst("\\{\\}", e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()).replace("{}", username)).build()).queue(m -> {
										m.addReaction(EmojiManager.getForAlias(":white_check_mark:").getUnicode()).queue();
										m.addReaction(EmojiManager.getForAlias(":x:").getUnicode()).queue();
									});
								});
							});
						}
						else {
							//error
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("Application reservation couldn't be set for user {} and clan {} in guild {}", e.getMember().getUser().getId(), clan_id, e.getGuild().getId());
							Hashes.clearTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
						}
					}
					else if(result != null) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_APPLY_ALREADY_SUBMITTED)).build()).queue();
						Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
					}
					else {
						//error
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Application reservation couldn't be retrieved for user {} and clan {} in guild {}", e.getMember().getUser().getId(), clan_id, e.getGuild().getId());
						Hashes.clearTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					}
				}
				else {
					//error
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Username couldn't be retrieved for user {} in guild {}", clan_id, e.getMember().getUser().getId(), e.getGuild().getId());
					Hashes.clearTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
				}
			}
			else if(management != null) {
				//clan with no members found
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_APPLY_NO_MANAGER)).build()).queue();
				logger.error("Clan {} is without any staff members in guild {}", clan_id, e.getGuild().getId());
				Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			}
			else {
				//error
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Management members of clan {} couldn't be retrieved in guild {}", clan_id, e.getGuild().getId());
				Hashes.clearTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			}
		}
		else if(clan_id == 0) {
			//clan doesn't exist
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_APPLY_NOT_FOUND)).build()).queue();
			Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
		}
		else {
			//error
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Clan with the name {} couldn't be retrieved in guild {}", clanName, e.getGuild().getId());
			Hashes.clearTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		}
	}
	
	public static void create(GuildMessageReceivedEvent e, String [] args, Cache cache) {
		String name = "";
		for(int i = 1; i < args.length; i++) {
			name = name + args[i]+" ";
		}
		name = name.trim();
		if(name.length() > 0) {
			if(name.length() <= 30) {
				final int clan_id = Competitive.SQLgetClanID(e.getGuild().getIdLong(), name);
				if(clan_id == 0) {
					if(Competitive.SQLCreateClan(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), name) > 0) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_CREATE_SUCCESS).replace("{}", name)).build()).queue();
						Hashes.clearTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
						Azrael.SQLInsertActionLog("CLAN_CREATE", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), name);
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Clan with the name {} couldn't be created in guild {}", name, e.getGuild().getId());
						Hashes.clearTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					}
				}
				else if(clan_id == 1) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_CREATE_ERR_2)).build()).queue();
					Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Clan name {} couldn't be verified in guild {}", name, e.getGuild().getId());
					Hashes.clearTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_CREATE_ERR_3)).build()).queue();
				Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_CREATE_ERR)).build()).queue();
			Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
		}
	}
	
	public static void members(GuildMessageReceivedEvent e, Cache cache) {
		final int clan_id = Integer.parseInt(cache.getAdditionalInfo());
		final var members = Competitive.SQLgetClanMembers(e.getGuild().getIdLong(), clan_id);
		if(members != null && members.size() > 0) {
			StringBuilder owner = new StringBuilder();
			StringBuilder staff = new StringBuilder();
			StringBuilder member = new StringBuilder();
			for(final var curMember : members) {
				if(curMember.getMemberLevel() == 1) {
					if(member.length() == 0)
						member.append("`"+curMember.getUsername()+" ("+curMember.getElo()+")`");
					else
						member.append(", `"+curMember.getUsername()+" ("+curMember.getElo()+")`");
				}
				else if(curMember.getMemberLevel() == 2) {
					if(staff.length() == 0)
						staff.append("`"+curMember.getUsername()+" ("+curMember.getElo()+")`");
					else
						staff.append(", `"+curMember.getUsername()+" ("+curMember.getElo()+")`");
				}
				else {
					if(owner.length() == 0)
						owner.append("`"+curMember.getUsername()+" ("+curMember.getElo()+")`");
					else
						owner.append(", `"+curMember.getUsername()+" ("+curMember.getElo()+")`");
				}
			}
			
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.CLAN_MEMBERS_TITLE))
				.addField(STATIC.getTranslation(e.getMember(), Translation.CLAN_MEMBERS_OWNER), owner.toString(), false)
				.addField(STATIC.getTranslation(e.getMember(), Translation.CLAN_MEMBERS_STAFF), staff.toString(), false)
				.addField(STATIC.getTranslation(e.getMember(), Translation.CLAN_MEMBERS_MEMBERS), member.toString(), false).build())
			.queue();
			Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Clan members couldn't be retrieved from clan {} in guild {}", clan_id, e.getGuild().getId());
			Hashes.clearTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		}
	}
	
	public static void leave(GuildMessageReceivedEvent e, String [] args, Cache cache) {
		final int clan_id = Integer.parseInt(cache.getAdditionalInfo());
		String submittedName = "";
		for(int i = 1; i < args.length; i++) {
			submittedName = submittedName+args[i]+" ";
		}
		if(submittedName.length() == 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_LEAVE_ERR)).build()).queue();
			Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			return;
		}
		submittedName = submittedName.trim();
		final String clanName = Competitive.SQLgetClanName(e.getGuild().getIdLong(), clan_id);
		if(clanName != null) {
			if(clanName.equals(submittedName)) {
				if(Competitive.SQLRemoveClanMember(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), clan_id) > 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_LEAVE_LEFT)).build()).queue();
					Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
					Azrael.SQLInsertActionLog("CLAN_LEAVE", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), clanName);
					final var management = Competitive.SQLgetClanManagement(e.getGuild().getIdLong(), clan_id);
					if(management != null && management.size() > 0) {
						final String username = Competitive.SQLgetUsernameFromUserStats(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong());
						if(username != null) {
							management.parallelStream().forEach(user_id -> {
								e.getGuild().getMemberById(user_id).getUser().openPrivateChannel().queue(channel -> {
									channel.sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_LEAVE_DM).replace("{}", username)).build()).queue();
								});
							});
						}
						else
							logger.error("Username couldn't be retrieved for user {} in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
					}
					else {
						logger.error("Management couldn't be retrieved for clan {} in guild {}", clan_id, e.getGuild().getId());
					}
					
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("user {} couldn't leave the clan {} in guild {}", e.getMember().getUser().getId(), clan_id, e.getGuild().getId());
					Hashes.clearTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_LEAVE_ERR_2)).build()).queue();
				Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Clan name couldn't be retrieved from clan {} in guild {}", clan_id, e.getGuild().getId());
			Hashes.clearTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		}
	}
	
	public static void kick(GuildMessageReceivedEvent e, String [] args, int memberLevel, Cache cache) {
		final int clan_id = Integer.parseInt(cache.getAdditionalInfo());
		final var member = validateUser(e, args, cache, clan_id, memberLevel, true, true, false);
		if(member != null) {
			if(Competitive.SQLRemoveClanMember(e.getGuild().getIdLong(), member.getUserID(), clan_id) > 0) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_KICKED).replace("{}", member.getUsername())).build()).queue();
				Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
				Azrael.SQLInsertActionLog("CLAN_KICK", member.getUserID(), e.getGuild().getIdLong(), member.getClanName());
				Member guildMember = e.getGuild().getMemberById(member.getUserID());
				if(guildMember != null) {
					guildMember.getUser().openPrivateChannel().queue(channel -> {
						channel.sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_KICK_DM).replace("{}", member.getClanName())).build()).queue(m -> {
							//notification received
						}, err -> {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_DM_LOCKED)).build()).queue();
						});
					});
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_NOT_IN_GUILD)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("user {} couldn't be kicked from the clan {} in guild {}", member.getUserID(), clan_id, e.getGuild().getId());
				Hashes.clearTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			}
		}
	}
	
	public static void invite(GuildMessageReceivedEvent e, String [] args, Cache cache) {
		final int clan_id = Integer.parseInt(cache.getAdditionalInfo());
		final var member = validateUser(e, args, cache, clan_id, 0, false, false, true);
		if(member != null) {
			Member guildMember = e.getGuild().getMemberById(member.getUserID());
			if(guildMember != null) {
				final String clanName = Competitive.SQLgetClanName(e.getGuild().getIdLong(), clan_id);
				if(clanName != null) {
					//verify if a reservation already exists
					final var result = Competitive.SQLgetClanReservation(e.getGuild().getIdLong(), member.getUserID(), clan_id);
					if(result != null && (result.getClanID() == 0 || result.getType() != 1 || (result.getType() == 1 && result.isDone()))) {
						//create a reservation
						if(Competitive.SQLInsertClanReservation(e.getGuild().getIdLong(), member.getUserID(), clan_id, 2, e.getChannel().getIdLong()) > 0) {
							Azrael.SQLInsertActionLog("CLAN_INVITE", member.getUserID(), e.getGuild().getIdLong(), clanName);
							guildMember.getUser().openPrivateChannel().queue(channel -> {
								channel.sendMessage(new EmbedBuilder().setColor(Color.BLUE).setFooter(e.getGuild().getId()+"-"+member.getUserID()+"-"+clan_id).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.CLAN_INVITE_DM).replace("{}", clanName)).build()).queue(m -> {
									m.addReaction(EmojiManager.getForAlias(":white_check_mark:").getUnicode()).queue();
									m.addReaction(EmojiManager.getForAlias(":x:").getUnicode()).queue();
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_INVITE_SENT)).build()).queue();
								}, err -> {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_INVITE_DM_ERR)).build()).queue();
								});
							});
							Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("Application reservation couldn't be set for user {} and clan {} in guild {}", member.getUserID(), clan_id, e.getGuild().getId());
							Hashes.clearTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
						}
					}
					else if(result != null) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_INVITE_ALREADY_SENT)).build()).queue();
						Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Application reservation couldn't be retrieved for user {} and clan {} in guild {}", member.getUserID(), clan_id, e.getGuild().getId());
						Hashes.clearTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("user {} couldn't be kicked from the clan {} in guild {}", member.getUserID(), clan_id, e.getGuild().getId());
					Hashes.clearTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_INVITE_ERR)).build()).queue();
				Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			}
		}
	}
	
	public static void promote(GuildMessageReceivedEvent e, String [] args, Cache cache) {
		final int clan_id = Integer.parseInt(cache.getAdditionalInfo());
		final var member = validateUser(e, args, cache, clan_id, 0, true, false, false);
		if(member != null) {
			final int memberLevel = Competitive.SQLgetClanMemberLevel(member.getUserID(), e.getGuild().getIdLong());
			if(memberLevel > 0) {
				int result = 0;
				if(memberLevel == 1) {
					//promote to staff
					result = Competitive.SQLUpdateClanMemberLevel(e.getGuild().getIdLong(), member.getUserID(), clan_id, 2);
					if(result > 0) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_PROMOTE_STAFF).replace("{}", member.getUsername())).build()).queue();
						Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
						Azrael.SQLInsertActionLog("CLAN_PROMOTE", member.getUserID(), e.getGuild().getIdLong(), member.getClanName());
					}
				}
				else if(memberLevel == 2) {
					//degrade to regular member
					result = Competitive.SQLUpdateClanMemberLevel(e.getGuild().getIdLong(), member.getUserID(), clan_id, 1);
					if(result > 0) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_PROMOTE_MEMBER).replace("{}", member.getUsername())).build()).queue();
						Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
						Azrael.SQLInsertActionLog("CLAN_DEMOTE", member.getUserID(), e.getGuild().getIdLong(), member.getClanName());
					}
				}
				if(result == 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Member level couldn't be updated for user {} in guild {}", member.getUserID(), e.getGuild().getId());
					Hashes.clearTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Member level couldn't be retrieved in guild {}", e.getGuild().getId());
				Hashes.clearTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			}
		}
	}
	
	public static void icon(GuildMessageReceivedEvent e, String [] args, Cache cache) {
		if(args.length == 2) {
			final int clan_id = Integer.parseInt(cache.getAdditionalInfo());
			final String url = args[1];
			if(url.startsWith("http") && (url.endsWith(".png") || url.endsWith(".jpg") || url.endsWith(".gif"))) {
				if(Competitive.SQLUpdateClanMark(e.getGuild().getIdLong(), clan_id, url) > 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_ICON_UPDATED)).build()).queue();
					Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Clan mark couldn't be updated for clan {} in guild {}", clan_id, e.getGuild().getId());
					Hashes.clearTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_ICON_ERR)).build()).queue();
				Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_ICON_ERR)).build()).queue();
			Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
		}
	}
	
	public static void delegate(GuildMessageReceivedEvent e, String [] args, Cache cache) {
		final int clan_id = Integer.parseInt(cache.getAdditionalInfo());
		final var member = validateUser(e, args, cache, clan_id, 0, true, false, false);
		if(member != null) {
			if(Competitive.SQLDelegateOwnership(e.getGuild().getIdLong(), member.getUserID(), e.getMember().getUser().getIdLong(), clan_id) > 0) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_DELEGATE_SUCCESS).replace("{}", member.getUsername())).build()).queue();
				Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
				Azrael.SQLInsertActionLog("CLAN_OWNER", member.getUserID(), e.getGuild().getIdLong(), member.getClanName());
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Ownership couldn't be passed to user {} from user {} for clan {} in guild {}", member.getUserID(), e.getMember().getUser().getId(), clan_id, e.getGuild().getId());
				Hashes.clearTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			}
		}
	}
	
	public static void disband(GuildMessageReceivedEvent e, Cache cache) {
		final int clan_id = Integer.parseInt(cache.getAdditionalInfo());
		final var members = Competitive.SQLgetClanMembers(e.getGuild().getIdLong(), clan_id);
		if(members != null && members.size() > 0) {
			if(members.size() == 1) {
				if(Competitive.SQLDisbandClan(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), clan_id) > 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_DISBAND_SUCCESS)).build()).queue();
					Hashes.clearTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					Azrael.SQLInsertActionLog("CLAN_CLOSE", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), members.get(0).getClanName());
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Clan {} couldn't be disbanded in guild {}", clan_id, e.getGuild().getId());
					Hashes.clearTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_DISBAND_ERR)).build()).queue();
				Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Clan members couldn't be retrieved from clan {} in guild {}", clan_id, e.getGuild().getId());
			Hashes.clearTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		}
	}
	
	private static ClanMember validateUser(GuildMessageReceivedEvent e, String [] args, Cache cache, int clan_id, int memberLevel, boolean validateClan, boolean validateUser, boolean validateNotInClan) {
		if(args.length > 1) {
			String firstParam = args[1].replaceAll("[<@!>]", "");
			ClanMember member = null;
			if(firstParam.replaceAll("[0-9]*", "").length() == 0) {
				long user_id = Long.parseLong(firstParam);
				member = Competitive.SQLgetClanDetails(user_id, e.getGuild().getIdLong());
			}
			else {
				String username = "";
				for(int i = 1; i < args.length; i++) {
					username = username+args[i]+" ";
				}
				username = username.trim();
				member = Competitive.SQLgetClanDetailsByName(username, e.getGuild().getIdLong());
			}
			if(member == null) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_USER_NOT_FOUND)).build()).queue();
				Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
				return member;
			}
			else {
				if(e.getMember().getUser().getIdLong() == member.getUserID()) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_NOT_ON_YOURSELF)).build()).queue();
					Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
					return null;
				}
				if(validateClan && member.getClanID() != clan_id) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_USER_NOT_IN_SAME_CLAN)).build()).queue();
					Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
					return null;
				}
				if(validateUser) {
					if(member.getMemberLevel() == memberLevel || memberLevel < member.getMemberLevel()) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_NOT_ALLOWED)).build()).queue();
						Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
						return null;
					}
				}
				if(validateNotInClan && member.getClanID() != 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_ALREADY_IN_CLAN)).build()).queue();
					Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
					return null;
				}
				return member;
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CLAN_INVALID_NAME)).build()).queue();
			Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			return null;
		}
	}
}
