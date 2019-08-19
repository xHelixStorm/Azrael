package commands;

import java.awt.Color;
import java.io.File;

import constructors.Cache;
import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sql.Azrael;

public class DoubleExperience implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(GuildIni.getDoubleExperienceCommand(e.getGuild().getIdLong())) {
			EmbedBuilder message = new EmbedBuilder();
			final var commandLevel = GuildIni.getDoubleExperienceLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
				if(args.length == 0) {
					message.setColor(Color.BLUE).setTitle("Command details!");
					e.getTextChannel().sendMessage(message.setDescription("Use this command to change the setting of the double experience event for this guild. These are the options: \n"
							+ "**AUTO**: The double experience event will start and terminate automatically\n"
							+ "**ON**: The double experience event will be enabled\n"
							+ "**OFF**: The double experience event will be disabled\n\n"
							+ "To display the current state of the double experience event, include the **-state** parameter with the command!").build()).queue();
				}
				else if(args[0].equalsIgnoreCase("-state")) {
					message.setColor(Color.BLUE).setTitle("Current double experience state!");
					e.getTextChannel().sendMessage(message.setDescription("The double experience event is set to **"+GuildIni.getDoubleExperienceMode(e.getGuild().getIdLong())+"**").build()).queue();
				}
				else if(args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("auto")) {
					if(!GuildIni.getDoubleExperienceMode(e.getGuild().getIdLong()).equalsIgnoreCase(args[0])) {
						GuildIni.setDoubleExperienceMode(e.getGuild().getIdLong(), args[0].toLowerCase());
						message.setColor(Color.BLUE).setTitle("Double Experience state change!");
						e.getTextChannel().sendMessage(message.setDescription("The double experience state is now set to **"+args[0].toLowerCase()+"**").build()).queue();
						if(args[0].equalsIgnoreCase("on")) {
							Hashes.addTempCache("doubleExp_gu"+e.getGuild().getId(), new Cache("on"));
							File doubleEvent = new File("./files/RankingSystem/doubleweekend.jpg");
							var bot_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("bot")).findAny().orElse(null);
							if(bot_channel != null) {
								e.getGuild().getTextChannelById(bot_channel.getChannel_ID()).sendFile(doubleEvent, "doubleweekend.jpg").complete();
								e.getGuild().getTextChannelById(bot_channel.getChannel_ID()).sendMessage("```css\nThe double EXP weekend is here\nUse the chance to gain more experience points than usual to reach new heights. See you at the top!\nThe event has been activated manually. Use this chance while you can!```").queue();
							}
						}
						else if(args[0].equalsIgnoreCase("off")) {
							Hashes.addTempCache("doubleExp_gu"+e.getGuild().getId(), new Cache("off"));
						}
						else {
							Hashes.clearTempCache("doubleExp_gu"+e.getGuild().getId());
						}
					}
					else {
						message.setColor(Color.RED).setTitle("Double experience state couldn't be updated!");
						e.getTextChannel().sendMessage(message.setDescription("The double experience state is already set to **"+args[0]+"**. Hence the state wasn't changed!").build()).queue();
					}
				}
				else {
					message.setColor(Color.RED).setTitle("Wrong parameter!");
					e.getTextChannel().sendMessage(message.setDescription("Command error! Please review the command usage and then try again!").build()).queue();
				}
			}
			else {
				message.setColor(Color.RED).setTitle("Access Denied!");
				e.getTextChannel().sendMessage(message.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(commandLevel, e.getGuild())).build()).queue();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		
	}

	@Override
	public String help() {
		return null;
	}

}
