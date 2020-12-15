package commandsContainer;

import java.awt.Color;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import constructors.Roles;
import core.Hashes;
import core.UserPrivs;
import enums.Translation;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.DiscordRoles;
import util.STATIC;

/**
 * Extension of the register command
 * @author xHelixStorm
 *
 */

public class RegisterRole {
	private static final Logger logger = LoggerFactory.getLogger(RegisterRole.class);
	
	public static void RegisterRoleHelper(GuildMessageReceivedEvent e){
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getSettingsThumbnail());
		StringBuilder strB = new StringBuilder();
		StringBuilder strB2 = new StringBuilder();
		
		final var roles = DiscordRoles.SQLgetCategories();
		if(roles != null) {
			for(Roles categories : DiscordRoles.SQLgetCategories()) {
				if(!categories.getCategory_ABV().equals("def")) {
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
	
	public static void runCommandWithAdminFirst(GuildMessageReceivedEvent e, long _guild_id, String [] _args, boolean adminPermission){
		String category_abv = null;
		String role;
		String role_name;
		long role_id;
		
		if(adminPermission) {
			if(_args.length == 3 && _args[1].equalsIgnoreCase("adm")) {
				category_abv = "adm";
				role = _args[2].replaceAll("[^0-9]*", "");
				if(e.getGuild().getRoleById(role) != null) {
					role_id = Long.parseLong(role);
					role_name = e.getGuild().getRoleById(role_id).getName();
					if(DiscordRoles.SQLInsertRole(_guild_id, role_id, STATIC.getLevel(category_abv), role_name, category_abv, false) > 0) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_ROLE_ADM_ADDED)).build()).queue();
						logger.info("User {} has registered the administrator role {} for guild {}", e.getMember().getUser().getId(), role_id, e.getGuild().getId());
						Hashes.removeDiscordRoles(e.getGuild().getIdLong());
						DiscordRoles.SQLgetRoles(e.getGuild().getIdLong());
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Administrator role {} couldn't be registered in guild {}", role_id, e.getGuild().getId());
					}
				}
			}
			else{
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_ROLE_INVALID_PARAM)).build()).queue();
			}
		}
		else {
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DENIED));
			e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_REQUIRED)).build()).queue();
		}
	}

	public static void runCommand(GuildMessageReceivedEvent e, long _guild_id, String [] _args, boolean adminPermission) {
		String category_abv = null;
		String role;
		String role_name;
		long role_id;
		
		if(UserPrivs.comparePrivilege(e.getMember(), GuildIni.getRegisterRoleLevel(e.getGuild().getIdLong())) || adminPermission) {
			Pattern pattern = Pattern.compile("(adm|mod|com|bot|mut|rea|boo|ver)");
			Matcher matcher = pattern.matcher(_args[1].toLowerCase());
			if(_args.length > 2 && matcher.find()) {
				category_abv = matcher.group();
				role = _args[2].replaceAll("[^0-9]*", "");
				if(e.getGuild().getRoleById(role) != null) {
					role_id = Long.parseLong(role);
					role_name = e.getGuild().getRoleById(role_id).getName();
					var level = STATIC.getLevel(category_abv);
					boolean persistant = true;
					if(category_abv.equals("rea"))
						persistant = false;
					if(DiscordRoles.SQLInsertRole(_guild_id, role_id, level, role_name, category_abv, persistant) > 0) {
						logger.info("User {} has registered the role {} with the category {} in guild {}", e.getMember().getUser().getId(), role_name, category_abv, e.getGuild().getId());
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_ROLE_ADDED)).build()).queue();
						Hashes.removeDiscordRoles(e.getGuild().getIdLong());
						if(category_abv.equals("rea")) {
							Hashes.removeReactionRoles(e.getGuild().getIdLong());
						}
						else if(category_abv.equals("ver")) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_ROLE_NOTICE)).addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_YES), "", true).addField(STATIC.getTranslation(e.getMember(), Translation.USER_REASON_NO), "", true).build()).queue();
							Hashes.addTempCache("register_role_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "ver"));
						}
						DiscordRoles.SQLgetRoles(e.getGuild().getIdLong());
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Role {} couldn't be registered in guild {}", role_id, e.getGuild().getId());
					}
				}
				else{
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_ROLE_ID)).build()).queue();
				}
			}
			else{
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
		}
		else {
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DENIED));
			e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(GuildIni.getRegisterRoleLevel(_guild_id), e.getMember())).build()).queue();
		}
	}
	
	public static void assignVerifiedRoleToMembers(GuildMessageReceivedEvent e, Cache cache) {
		if(e.getMessage().getContentRaw().equalsIgnoreCase("yes")) {
			if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
				final var ver_role = DiscordRoles.SQLgetRoles(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getCategory_ABV().equals("ver")).findAny().orElse(null);
				if(ver_role != null) {
					final Role role = e.getGuild().getRoleById(ver_role.getRole_ID());
					if(role != null) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_ROLE_ASSIGN_START)).build()).queue();
						List<Member> members = e.getGuild().getMembers().parallelStream().filter(m -> !m.getRoles().contains(role)).collect(Collectors.toList());
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
			
		}
		else if(e.getMessage().getContentRaw().equalsIgnoreCase("no")) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_ROLE_DONE)).build()).queue();
			Hashes.clearTempCache("register_role_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		}
	}
}
