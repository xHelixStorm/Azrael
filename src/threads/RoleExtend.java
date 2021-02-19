package threads;

/**
 * this class is meant to search for muted users and to restart the mute timer
 * in case it was still running before the bot has been restarted.
 * 
 * If there's a muted user on the server but there are no saved details, the mute
 * role will be removed.
 */

import java.awt.Color;
import java.util.EnumSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import enums.Channel;
import enums.Translation;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import sql.RankingSystem;
import util.STATIC;
import sql.DiscordRoles;
import sql.Azrael;

public class RoleExtend implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(RoleExtend.class);
	
	private Guild guild;
	
	public RoleExtend(Guild _guild) {
		this.guild = _guild;
	}
	
	@Override
	public void run() {
		//retrieve the mute role of the current server
		var mute_role_object = DiscordRoles.SQLgetRoles(guild.getIdLong()).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null);
		//do this step if a mute role is registered
		if(mute_role_object != null) {
			Role mute_role = guild.getRoleById(mute_role_object.getRole_ID());
			boolean banHammerFound = false;
			int i = 0;
			
			Guilds guild_settings = RankingSystem.SQLgetGuild(guild.getIdLong());
			//search for all members which have a mute role
			for(Member member : guild.getMembersWithRoles(mute_role)) {
				//retrieve the mute details of the current user
				var dbData = Azrael.SQLgetData(member.getUser().getIdLong(), guild.getIdLong());
				//if no data is available (default constructor was called) then don't restart the mute but simply remove the role
				if(dbData.getUnmute() != null) {
					//retrieve the time for the user to still stay muted, if expired set it to 0 for direct unmute
					long unmute = (dbData.getUnmute().getTime() - System.currentTimeMillis());
					if(unmute < 0)
						unmute = 0;
					long assignedRole = 0;
					//get a ranking role to assign, if the user had one unlocked
					boolean rankingState = guild_settings.getRankingState();
					if(rankingState)
						assignedRole = RankingSystem.SQLgetAssignedRole(member.getUser().getIdLong(), guild.getIdLong());
					banHammerFound = true;
					//run thread to restart the timer
					new Thread(new MuteRestart(member, guild, member.getUser().getName()+"#"+member.getUser().getDiscriminator(), mute_role, unmute, assignedRole, rankingState)).start();
					i++;
				}
			}
			//display the amount of users that are still muted
			if(banHammerFound == true && GuildIni.getNotifications(guild.getIdLong())) {
				logger.info("{} muted users found on startup in guild {}", i, guild.getId());
				EmbedBuilder message = new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.UNMUTE_RECOUNT_TITLE));
				STATIC.writeToRemoteChannel(guild, message, i+STATIC.getTranslation2(guild, Translation.UNMUTE_RECOUNT)+(GuildIni.getOverrideBan(guild.getIdLong()) ? STATIC.getTranslation2(guild, Translation.UNMUTE_RECOUNT_EXCLUDED) : ""), Channel.LOG.getType());
			}
		}
		
		//Get members that have recently joined the server, but were not yet verified, into the waiting room
		final var members = guild.getMembers().parallelStream().filter(f -> !f.getUser().isBot() && f.getRoles().size() == 0).collect(Collectors.toList());
		if(members.size() > 0) {
			int y = 0;
			final var categories = Azrael.SQLgetCategories(guild.getIdLong());
			if(categories != null && categories.size() > 0) {
				final var verification = categories.parallelStream().filter(f -> f.getType().equals("ver")).findAny().orElse(null);
				if(verification != null) {
					Category category = guild.getCategoryById(verification.getCategoryID());
					if(category != null) {
						if(guild.getSelfMember().hasPermission(category, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS) || STATIC.setPermissions(guild, category, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS))) {
							final var roles = DiscordRoles.SQLgetRoles(guild.getIdLong()).parallelStream().filter(f -> f.getCategory_ABV().equals("adm") || f.getCategory_ABV().equals("mod")).collect(Collectors.toList());
							for(final var member : members) {
								if(guild.getTextChannelsByName(member.getUser().getId(), false).size() == 0) {
									//create a new text channel under the category and add the required permissions
									category.createTextChannel(""+member.getUser().getId())
										.addPermissionOverride(guild.getSelfMember(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE, Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS), null)
										.addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE))
										.addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EMBED_LINKS), EnumSet.of(Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS))
										.queue(channel -> {
											for(final var role : roles) {
												Role serverRole = guild.getRoleById(role.getRole_ID());
												if(serverRole != null) {
													channel.getManager().putPermissionOverride(serverRole, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EMBED_LINKS), EnumSet.of(Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS)).queue();
												}
											}
											final String verificationMessage = FileSetting.readFile("files/Guilds/"+guild.getId()+"/verificationmessage.txt");
											channel.sendMessage(new EmbedBuilder().setColor(Color.BLUE).setThumbnail(guild.getIconUrl()).setDescription((verificationMessage != null && verificationMessage.length() > 0 ? verificationMessage : STATIC.getTranslation2(guild, Translation.JOIN_VERIFY).replaceFirst("\\{\\}", guild.getName()).replace("{}", member.getAsMention()))).build()).queue();
										}
									);
									y++;
								}
							}
						}
						else {
							STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(guild, Translation.MISSING_PERMISSION_IN_2).replace("{}", Permission.MANAGE_CHANNEL.getName()+" and "+Permission.MANAGE_PERMISSIONS.getName())+category.getName(), Channel.LOG.getType());
							logger.warn("MANAGE_CHANNEL and MANAGE_PERMISSIONS for category {} required to create verification channels in guild {}", verification.getCategoryID(), guild.getId());
						}
					}
					else {
						logger.warn("Category {} doesn't exist anymore in guild {}", verification.getCategoryID(), guild.getId());
					}
				}
			}
			//display the amount of users that were put into waiting rooms
			if(y > 0) {
				logger.info("{} users have been moved into waiting rooms in guild {}", y, guild.getId());
				EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(guild, Translation.UNMUTE_WAITING_TITLE));
				STATIC.writeToRemoteChannel(guild, message, y+STATIC.getTranslation2(guild, Translation.UNMUTE_WAITING_MOVED), Channel.LOG.getType());
			}
		}
	}
}
