package de.azrael.commandsContainer;

import java.awt.Color;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Cache;
import de.azrael.constructors.Roles;
import de.azrael.constructors.Thumbnails;
import de.azrael.core.Hashes;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.sql.DiscordRoles;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Extension of the register command
 * @author xHelixStorm
 *
 */

public class RegisterRole {
	private static final Logger logger = LoggerFactory.getLogger(RegisterRole.class);
	
	public static void RegisterRoleHelper(GuildMessageReceivedEvent e, Thumbnails thumbnails, boolean adminRestriction) {
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(thumbnails.getSettings());
		StringBuilder strB = new StringBuilder();
		StringBuilder strB2 = new StringBuilder();
		
		final var roles = DiscordRoles.SQLgetCategories();
		if(roles != null) {
			for(Roles categories : DiscordRoles.SQLgetCategories()) {
				if((!adminRestriction && !categories.getCategory_ABV().equals("def")) || (adminRestriction && categories.getCategory_ABV().equals("adm"))) {
					strB.append("**"+categories.getCategory_ABV()+"**\n");
					strB2.append(categories.getCategory_Name()+"\n");
				}
			}
			if(strB.length() > 0) {
				e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_ROLE_HELP)).addField("", strB.toString(), true).addField("", strB2.toString(), true).build()).queue();
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_ROLE_NO_TYPES)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Role types couldn't be retrieved in guild {}", e.getGuild().getId());
		}
	}
	
	public static void runCommandWithAdminFirst(GuildMessageReceivedEvent e, String [] args, Thumbnails thumbnails) {
		String category_abv = null;
		
		if(args.length == 3 && args[1].equalsIgnoreCase("adm") && args[2].matches("[0-9]*")) {
			category_abv = "adm";
			Role role = e.getGuild().getRoleById(args[2]);
			if(role != null) {
				if(DiscordRoles.SQLInsertRole(e.getGuild().getIdLong(), role.getIdLong(), STATIC.getLevel(category_abv), role.getName(), category_abv, false) > 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_ROLE_ADM_ADDED)).build()).queue();
					logger.info("User {} has registered the administrator role {} for guild {}", e.getMember().getUser().getId(), role.getId(), e.getGuild().getId());
					Hashes.removeDiscordRoles(e.getGuild().getIdLong());
					DiscordRoles.SQLgetRoles(e.getGuild().getIdLong());
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Administrator role {} couldn't be registered in guild {}", role.getId(), e.getGuild().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROLE_NOT_EXISTS)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_ROLE_INVALID_PARAM)).build()).queue();
		}
	}

	public static boolean runCommand(GuildMessageReceivedEvent e, String [] args, Thumbnails thumbnails) {
		String category_abv = null;
		
		final int commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_ROLE);
		if(UserPrivs.comparePrivilege(e.getMember(), commandLevel)) {
			final var roleTypes = DiscordRoles.SQLgetCategories();
			if(roleTypes != null && roleTypes.size() > 0) {
				StringBuilder out = new StringBuilder();
				for(final var roleType : roleTypes) {
					if(!roleType.getCategory_ABV().equals("def")) {
						if(out.length() > 0)
							out.append("|");
						out.append(roleType.getCategory_ABV());
					}
				}
				Pattern pattern = Pattern.compile("("+out.toString()+")");
				Matcher matcher = pattern.matcher(args[1].toLowerCase());
				if(args.length > 2 && matcher.find()) {
					if(args[2].matches("[0-9]*")) {
						category_abv = matcher.group();
						Role role = e.getGuild().getRoleById(args[2]);
						if(role != null) {
							var level = STATIC.getLevel(category_abv);
							boolean persistant = true;
							if(category_abv.equals("rea"))
								persistant = false;
							if(DiscordRoles.SQLInsertRole(e.getGuild().getIdLong(), role.getIdLong(), level, role.getName(), category_abv, persistant) > 0) {
								logger.info("User {} has registered the role {} with the category {} in guild {}", e.getMember().getUser().getId(), role.getName(), category_abv, e.getGuild().getId());
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_ROLE_ADDED)).build()).queue();
								Hashes.removeDiscordRoles(e.getGuild().getIdLong());
								if(category_abv.equals("rea")) {
									Hashes.removeReactionRoles(e.getGuild().getIdLong());
								}
								else if(category_abv.equals("ver")) {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_ROLE_NOTICE)).addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_YES), "", true).addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_NO), "", true).build()).queue();
									Hashes.addTempCache("register_role_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "ver"));
								}
								else if(category_abv.equals("key")) {
									if(!Azrael.SQLisGiveawayAvailable(e.getGuild().getIdLong())) {
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.ORANGE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_ROLE_KEY)).build()).queue();
									}
								}
								DiscordRoles.SQLgetRoles(e.getGuild().getIdLong());
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("Role {} couldn't be registered in guild {}", role.getId(), e.getGuild().getId());
							}
						}
						else{
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROLE_NOT_EXISTS)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_ROLE_ID)).build()).queue();
					}
				}
				else{
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
				}
				return true;
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Role types couldn't be retrieved in guild {}", e.getGuild().getId());
			}
		}
		else {
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(thumbnails.getDenied()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DENIED));
			e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(commandLevel, e.getMember())).build()).queue();
		}
		return false;
	}
	
	public static void assignVerifiedRoleToMembers(GuildMessageReceivedEvent e, Cache cache) {
		if(e.getMessage().getContentRaw().equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_YES))) {
			if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
				final var ver_role = DiscordRoles.SQLgetRoles(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getCategory_ABV().equals("ver")).findAny().orElse(null);
				if(ver_role != null) {
					final Role role = e.getGuild().getRoleById(ver_role.getRole_ID());
					if(role != null) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_ROLE_ASSIGN_START)).build()).queue();
						List<Member> members = e.getGuild().loadMembers().get().parallelStream().filter(m -> !m.getRoles().contains(role)).collect(Collectors.toList());
						members.parallelStream().forEach(m -> {
							e.getGuild().addRoleToMember(m, role).queue();
						});
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_ROLE_DONE)).build()).queue();
						Hashes.clearTempCache("register_role_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Role {} doesn't exist anymore in guild {}", ver_role.getRole_ID(), e.getGuild().getId());
						Hashes.clearTempCache("register_role_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Registered verified role couldn't be retrieved in guild {}", e.getGuild().getId());
					Hashes.clearTempCache("register_role_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_ROLE_ERR).replace("{}", Permission.MANAGE_ROLES.getName())).build()).queue();
				logger.error("MANAGE_ROLES permission required to assign roles in guild {}", e.getGuild().getId());
				Hashes.addTempCache("register_role_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			}
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.REGISTER_ROLE.getColumn(), e.getMessage().getContentRaw());
		}
		else if(e.getMessage().getContentRaw().equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_NO))) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_ROLE_DONE)).build()).queue();
			Hashes.clearTempCache("register_role_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.REGISTER_ROLE.getColumn(), e.getMessage().getContentRaw());
		}
	}
}
