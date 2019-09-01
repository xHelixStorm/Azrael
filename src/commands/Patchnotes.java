package commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Patchnote;
import core.UserPrivs;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import util.STATIC;

public class Patchnotes implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Patchnotes.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getPatchnotesCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getPatchnotesLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		var allowed_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && (f.getChannel_Type().equals("bot") || f.getChannel_Type().equals("log"))).collect(Collectors.toList());
		var bot_channels = allowed_channels.parallelStream().filter(f -> f.getChannel_Type().equals("bot")).collect(Collectors.toList());
		var this_channel = allowed_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null);
		if(this_channel == null && allowed_channels.size() > 0){
			e.getChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in "+STATIC.getChannels(bot_channels)).queue();
		}
		else {
			EmbedBuilder message = new EmbedBuilder();
			ArrayList<Patchnote> priv_notes = null;
			ArrayList<Patchnote> publ_notes = null;
			ArrayList<Patchnote> game_notes = null;
			var modRights = false;
			//retrieve patchnotes
			if(UserPrivs.isUserMod(e.getMember().getUser(), e.getGuild().getIdLong()) || UserPrivs.isUserAdmin(e.getMember().getUser(), e.getGuild().getIdLong()) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
				modRights = true;
			}
			if(modRights)
				priv_notes = sql.Patchnotes.SQLgetPrivatePatchnotesArray();
			publ_notes = sql.Patchnotes.SQLgetPublicPatchnotesArray();
			game_notes = sql.Patchnotes.SQLgetGamePatchnotesArray(e.getGuild().getIdLong());
			
			if(priv_notes == null && publ_notes == null && game_notes == null) {
				message.setTitle("No patch notes are available!").setColor(Color.RED);
				e.getChannel().sendMessage(message.setDescription("Patch notes need to be registered before displaying them!").build()).queue();
			}
			else if((priv_notes != null || publ_notes != null) && game_notes == null) {
				if(modRights) {
					if(args.length == 0)
						e.getChannel().sendMessage("Please select if you want to display the public or private patch notes!").queue();
					else if(args.length == 1 && (args[0].equalsIgnoreCase("private") || args[0].equalsIgnoreCase("public"))) {
						ArrayList<Patchnote> display_notes = null;
						if(args[0].equalsIgnoreCase("private"))
							display_notes = priv_notes;
						else
							display_notes = publ_notes;
						
						if(display_notes == null || display_notes.size() == 0) {
							message.setTitle("No patch notes available!").setColor(Color.RED);
							e.getChannel().sendMessage(message.setDescription("No Patchnotes available for this filter option").build()).queue();
						}
						else {
							collectPatchNotes(e, display_notes, message);
						}
					}
					else if(args.length == 2 && (args[0].equalsIgnoreCase("private") || args[0].equalsIgnoreCase("public"))) {
						Patchnote note = null;
						if(args[0].equalsIgnoreCase("private"))
							note = priv_notes.parallelStream().filter(f -> f.getTitle().equalsIgnoreCase(args[1])).findAny().orElse(null);
						else if(args[0].equalsIgnoreCase("public"))
							note = publ_notes.parallelStream().filter(f -> f.getTitle().equalsIgnoreCase(args[1])).findAny().orElse(null);
						if(note == null) {
							e.getChannel().sendMessage("Patch notes not found!").queue();
						}
						else {
							printPatchNotes(e, note, message);
						}
					}
					else {
						message.setTitle("Wrong parameter!").setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription("Please either write private or public as first parameter!").build()).queue();
					}
				}
				else {
					if(args.length == 0) {
						collectPatchNotes(e, publ_notes, message);
					}
					else if(args.length == 1) {
						var note = publ_notes.parallelStream().filter(f -> f.getTitle().equalsIgnoreCase(args[0])).findAny().orElse(null);
						if(note == null) {
							message.setTitle("No patch notes are available!").setColor(Color.RED);
							e.getChannel().sendMessage(message.setDescription("Patch notes need to be registered before displaying them!").build()).queue();
						}
						else {
							printPatchNotes(e, note, message);
						}
					}
				}
			}
			else if(priv_notes == null && publ_notes == null && game_notes != null) {
				if(args.length == 0) {
					collectPatchNotes(e, game_notes, message);
				}
				else if(args.length == 1) {
					var note = game_notes.parallelStream().filter(f -> f.getTitle().equalsIgnoreCase(args[0])).findAny().orElse(null);
					if(note == null) {
						message.setTitle("No patch notes are available!").setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription("Patch notes need to be registered before displaying them!").build()).queue();
					}
					else {
						printPatchNotes(e, note, message);
					}
				}
			}
			else {
				if(modRights) {
					if(args.length == 0)
						e.getChannel().sendMessage("Please select if you want to display the public, private or game patch notes!").queue();
					else if(args.length == 1 && (args[0].equalsIgnoreCase("private") || args[0].equalsIgnoreCase("public") || args[0].equalsIgnoreCase("game"))) {
						ArrayList<Patchnote> display_notes = null;
						if(args[0].equalsIgnoreCase("private"))
							display_notes = priv_notes;
						else if(args[0].equalsIgnoreCase("public"))
							display_notes = publ_notes;
						else
							display_notes = game_notes;
						
						if(display_notes == null || display_notes.size() == 0) {
							message.setTitle("No patch notes available!").setColor(Color.RED);
							e.getChannel().sendMessage(message.setDescription("No Patchnotes available for this filter option").build()).queue();
						}
						else {
							collectPatchNotes(e, display_notes, message);
						}
					}
					else if(args.length == 2 && (args[0].equalsIgnoreCase("private") || args[0].equalsIgnoreCase("public") || args[0].equalsIgnoreCase("game"))) {
						Patchnote note = null;
						if(args[0].equalsIgnoreCase("private"))
							note = priv_notes.parallelStream().filter(f -> f.getTitle().equalsIgnoreCase(args[1])).findAny().orElse(null);
						else if(args[0].equalsIgnoreCase("public"))
							note = publ_notes.parallelStream().filter(f -> f.getTitle().equalsIgnoreCase(args[1])).findAny().orElse(null);
						else if(args[0].equalsIgnoreCase("game"))
							note = game_notes.parallelStream().filter(f -> f.getTitle().equalsIgnoreCase(args[1])).findAny().orElse(null);
						if(note == null) {
							message.setTitle("No patch notes are available!").setColor(Color.RED);
							e.getChannel().sendMessage(message.setDescription("Patch notes need to be registered before displaying them!").build()).queue();
						}
						else {
							printPatchNotes(e, note, message);
						}
					}
					else {
						message.setTitle("Wrong parameter!").setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription("Please either write private, public or game as first parameter!").build()).queue();
					}
				}
				else {
					if(args.length == 0)
						e.getChannel().sendMessage("Please select if you want to display the bot or game patch notes!").queue();
					else if(args.length == 1 && (args[0].equalsIgnoreCase("bot") || args[0].equalsIgnoreCase("game"))) {
						ArrayList<Patchnote> display_notes = null;
						if(args[0].equalsIgnoreCase("bot"))
							display_notes = publ_notes;
						else
							display_notes = game_notes;
						
						if(display_notes == null || display_notes.size() == 0) {
							message.setTitle("No patch notes available!").setColor(Color.RED);
							e.getChannel().sendMessage(message.setDescription("No Patchnotes available for this filter option").build()).queue();
						}
						else {
							collectPatchNotes(e, display_notes, message);
						}
					}
					else if(args.length == 2 && (args[0].equalsIgnoreCase("bot") || args[0].equalsIgnoreCase("game"))) {
						Patchnote note = null;
						if(args[0].equalsIgnoreCase("bot"))
							note = publ_notes.parallelStream().filter(f -> f.getTitle().equalsIgnoreCase(args[1])).findAny().orElse(null);
						else if(args[0].equalsIgnoreCase("game"))
							note = game_notes.parallelStream().filter(f -> f.getTitle().equalsIgnoreCase(args[1])).findAny().orElse(null);
						if(note == null) {
							message.setTitle("No patch notes are available!").setColor(Color.RED);
							e.getChannel().sendMessage(message.setDescription("Patch notes need to be registered before displaying them!").build()).queue();
						}
						else {
							printPatchNotes(e, note, message);
						}
					}
					else {
						message.setTitle("Wrong parameter!").setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription("Please either write private, public or game as first parameter!").build()).queue();
					}
				}
			}
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used Patchnotes command", e.getMember().getUser().getId());
	}

	@Override
	public String help() {
		return null;
	}
	
	private void collectPatchNotes(GuildMessageReceivedEvent e, ArrayList<Patchnote> display_notes, EmbedBuilder message) {
		StringBuilder out = new StringBuilder();
		for(Patchnote note : display_notes) {
			out.append(note.getDate()+":\t **"+note.getTitle()+"**\n");
		}
		message.setTitle("Here a list of patch notes!").setColor(Color.BLUE);
		e.getChannel().sendMessage(message.setDescription("Please write one of the following patch note title together with the full command to display the notes.\n\n"
				+ out.toString()).build()).queue();
	}
	
	private void printPatchNotes(GuildMessageReceivedEvent e, Patchnote note, EmbedBuilder message) {
		message.setColor(Color.MAGENTA).setThumbnail(e.getJDA().getSelfUser().getAvatarUrl()).setTitle("Here the requested patch notes!");
		e.getChannel().sendMessage(message.setDescription("Bot patch notes version **"+note.getTitle()+"** "+note.getDate()+"\n\n"+note.getMessage1()).build()).complete();
		if(note.getMessage2() != null && note.getMessage2().length() > 0) {
			message.setTitle("Requested patch notes part two!");
			e.getChannel().sendMessage(message.setDescription(note.getMessage2()).build()).complete();
		}
	}

}
