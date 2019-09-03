package commandsContainer;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;

public class RegisterRankingRole {
	private final static Logger logger = LoggerFactory.getLogger(RegisterRankingRole.class);
	
	public static void RegisterRankingRoleHelper(GuildMessageReceivedEvent _e) {
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.WHITE).setThumbnail(IniFileReader.getSettingsThumbnail()).setTitle("Register roles for the ranking system!");
		final String prefix = GuildIni.getCommandPrefix(_e.getGuild().getIdLong());
		_e.getChannel().sendMessage(messageBuild.setDescription("To use this command, write the role_id right after the command and add the required level to unlock this role in this format:\n"
				+ "**"+prefix+"register -ranking-role <role_id> <level>**\n\n To display all roles, type the command **"+prefix+"display -roles**. To remove all registered roles, type **"+prefix+"register -ranking-role -clear**").build()).queue();
	}
	
	public static void runCommand(GuildMessageReceivedEvent _e, long _guild_id, String [] _args, boolean adminPermission) {
		EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied");
		long guild_id = _e.getGuild().getIdLong();
		long role_id = 0;
		String role_name = "";
		String level = "";
		int level_requirement = 0;
		
		var commandLevel = GuildIni.getRegisterRankingRoleLevel(_e.getGuild().getIdLong());
		if(UserPrivs.comparePrivilege(_e.getMember(), commandLevel) || adminPermission) {
			if(_args.length > 1 && _args[1].equalsIgnoreCase("-clear")) {
				if(RankingSystem.SQLclearRoles(guild_id) > 0) {
					Hashes.removeRankingRoles(guild_id);
					_e.getChannel().sendMessage("All registered ranking roles have been cleared from the database!").queue();
				}
				else {
					logger.error("Roles couldn't be cleared from RankingSystem.roles table");
					_e.getChannel().sendMessage("An internal error occurred. Roles couldn't be cleared from the RankingSystem.roles table").queue();
				}
			}
			else {
				try {
					if(_args.length == 3) {
						if(_args[1].length() == 18) {
							role_id = Long.parseLong(_args[1]);
							role_name = _e.getGuild().getRoleById(role_id).getName();
						}
						else {
							_e.getChannel().sendMessage(_e.getMember().getAsMention()+" Please type a valid role id!").queue();
							return;
						}
						if(_args[2].length() <= 4 && _args[2].replaceAll("[0-9]", "").length() == 0) {
							level = _args[2];
							level_requirement = Integer.parseInt(level);
						}
						else {
							_e.getChannel().sendMessage(_e.getMember().getUser().getAsMention()+" Please type a level between 1 and 9999!").queue();
							return;
						}
					}
					else {
						_e.getChannel().sendMessage(_e.getMember().getAsMention()+" Something went wrong. Please recheck the syntax!").queue();
						return;
					}
					if(level.length() < 1 || level.length() > 10000) {
						_e.getChannel().sendMessage(_e.getMember().getAsMention()+" Please type a level between 1 and 9999!").queue();
					}
					else{
						if(RankingSystem.SQLInsertRoles(role_id, role_name, level_requirement, guild_id) > 0) {
							logger.debug("{} has registered the ranking role {} with the level requirement {} in the guild {}", _e.getMember().getUser().getId(), role_name, level_requirement, _e.getGuild().getName());
							_e.getChannel().sendMessage("**The role named "+role_name+" can now be unlocked by reaching level "+level_requirement+"**").queue();
							Hashes.removeRankingRoles(guild_id);
							if(RankingSystem.SQLgetRoles(guild_id) != null) {
								if(RankingSystem.SQLgetLevels(guild_id, RankingSystem.SQLgetGuild(guild_id).getThemeID()) == 0) {
									logger.error("Levels for the ranking system from RankingSystem.level_list couldn't be retrieved and cached");
									_e.getChannel().sendMessage("An internal error occurred. All levels for the ranking system couldn't be retrieved from the table RankingSystem.level_list").queue();
								}
							}
							else {
								logger.error("Roles from RankingSystem.roles couldn't be called and cached");
								_e.getChannel().sendMessage("An internal error occurred. Roles from RankingSystem.roles couldn't be called and cached").queue();
							}
						}
						else {
							logger.error("role id {} couldn't be inserted into the table RankingSystem.roles for the guild {}", role_id, _e.getGuild().getName());
							_e.getChannel().sendMessage("An internal error occurred. The role "+role_name+" with the role id "+role_id+" couldn't be inserted into RankingSystem.roles").queue();
							RankingSystem.SQLInsertActionLog("High", role_id, guild_id, "Role couldn't be registered as ranking role", "The role "+role_name+" couldn't be inserted into the RankingSystem.roles table");
						}
					}
				} catch(NullPointerException npe) {
					_e.getChannel().sendMessage(_e.getMember().getAsMention()+" Please type a valid role id!").queue();
				}
			}
		}
		else {
			_e.getChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(commandLevel, _e.getGuild())).build()).queue();
		}
	}
}
