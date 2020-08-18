package commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Patchnote;
import core.UserPrivs;
import enums.Channel;
import enums.Translation;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import util.STATIC;

/**
 * The Patchnotes command allows a user to display past 
 * patch notes which are either public, private or game
 * related, if available. 
 * @author xHelixStorm
 *
 */

public class Patchnotes implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Patchnotes.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
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
		//retrieve all channels where patchnotes can be printed
		var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.BOT.getType())).collect(Collectors.toList());
		var this_channel = bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null);
		
		var modRights = false;
		//is user mod?
		if(UserPrivs.isUserMod(e.getMember()) || UserPrivs.isUserAdmin(e.getMember()) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
			modRights = true;
		}
		
		//throw error if it was printed in a channel which is not a bot channel or log channel
		//if no bot channel is registered, print anyway
		if(this_channel == null && bot_channels.size() > 0 && !modRights) {
			e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.NOT_BOT_CHANNEL)+STATIC.getChannels(bot_channels)).queue();
		}
		else {
			EmbedBuilder message = new EmbedBuilder();
			ArrayList<Patchnote> priv_notes = null;
			ArrayList<Patchnote> publ_notes = null;
			ArrayList<Patchnote> game_notes = null;
			
			//if the user is a mod, retrieve also private patch notes
			if(modRights)
				priv_notes = sql.Patchnotes.SQLgetPrivatePatchnotesArray();
			publ_notes = sql.Patchnotes.SQLgetPublicPatchnotesArray();
			game_notes = sql.Patchnotes.SQLgetGamePatchnotesArray(e.getGuild().getIdLong());
			
			//if there's no single patch note, throw a message
			if(priv_notes == null && publ_notes == null && game_notes == null) {
				e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PATCHNOTES_NOT_AVAILABLE)).build()).queue();
			}
			//execute this block if there are any public and private patch notes but no game related patch notes (e.g. url to a patchnote)
			else if((priv_notes != null || publ_notes != null) && game_notes == null) {
				//if the user is a moderator or administrator, give the user a choice to either display the private or public patch notes
				if(modRights) {
					//check if a parameter has been passed, else notify the user
					if(args.length == 0)
						e.getChannel().sendMessage(message.setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.PATCHNOTES_CHOICE_1)).build()).queue();
					//execute if the parameter equals 'private' or 'public'
					else if(args.length == 1 && (args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PRIVATE)) || args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PUBLIC)))) {
						ArrayList<Patchnote> display_notes = null;
						if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PRIVATE)))
							display_notes = priv_notes;
						else
							display_notes = publ_notes;
						
						//print patch notes list
						if(display_notes == null || display_notes.size() == 0) {
							e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PATCHNOTES_NOT_AVAILABLE)).build()).queue();
						}
						else {
							collectPatchNotes(e, display_notes, message);
						}
					}
					//print the specific patch note from the private or public category
					else if(args.length == 2 && (args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PRIVATE)) || args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PUBLIC)))) {
						Patchnote note = null;
						if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PRIVATE)))
							note = priv_notes.parallelStream().filter(f -> f.getTitle().equalsIgnoreCase(args[1])).findAny().orElse(null);
						else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PUBLIC)))
							note = publ_notes.parallelStream().filter(f -> f.getTitle().equalsIgnoreCase(args[1])).findAny().orElse(null);
						if(note == null) {
							e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PATCHNOTES_NOT_FOUND)).build()).queue();
						}
						else {
							printPatchNotes(e, note, message);
						}
					}
					else {
						e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
					}
				}
				else {
					if(args.length == 0) {
						collectPatchNotes(e, publ_notes, message);
					}
					else if(args.length == 1) {
						var note = publ_notes.parallelStream().filter(f -> f.getTitle().equalsIgnoreCase(args[0])).findAny().orElse(null);
						if(note == null) {
							e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PATCHNOTES_NOT_FOUND)).build()).queue();
						}
						else {
							printPatchNotes(e, note, message);
						}
					}
				}
			}
			//execute this block if game notes exist while everything else doesn't
			else if(priv_notes == null && publ_notes == null && game_notes != null) {
				//show the game related patch notes list without parameters
				if(args.length == 0) {
					collectPatchNotes(e, game_notes, message);
				}
				//display the specific game patch note
				else if(args.length == 1) {
					var note = game_notes.parallelStream().filter(f -> f.getTitle().equalsIgnoreCase(args[0])).findAny().orElse(null);
					if(note == null) {
						e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PATCHNOTES_NOT_FOUND)).build()).queue();
					}
					else {
						printPatchNotes(e, note, message);
					}
				}
			}
			//execute when game notes, private notes and public notes aren't empty
			else {
				//enter this block is the user is an administrator or moderator
				if(modRights) {
					//Without parameter, give the user a choice to which patch notes the user wishes to access
					if(args.length == 0)
						e.getChannel().sendMessage(message.setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.PATCHNOTES_CHOICE_2)).build()).queue();
					//enter this block if either 'private', 'public' or 'game' has been written
					else if(args.length == 1 && (args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PRIVATE)) || args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PUBLIC)) || args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_GAME)))) {
						ArrayList<Patchnote> display_notes = null;
						if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PRIVATE)))
							display_notes = priv_notes;
						else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PUBLIC)))
							display_notes = publ_notes;
						else
							display_notes = game_notes;
						
						//print the patch notes list
						if(display_notes == null || display_notes.size() == 0) {
							e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PATCHNOTES_NOT_FOUND)).build()).queue();
						}
						else {
							collectPatchNotes(e, display_notes, message);
						}
					}
					//display the selected parameter if 2 parameters have been used
					else if(args.length == 2 && (args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PRIVATE)) || args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PUBLIC)) || args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_GAME)))) {
						Patchnote note = null;
						if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PRIVATE)))
							note = priv_notes.parallelStream().filter(f -> f.getTitle().equalsIgnoreCase(args[1])).findAny().orElse(null);
						else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PUBLIC)))
							note = publ_notes.parallelStream().filter(f -> f.getTitle().equalsIgnoreCase(args[1])).findAny().orElse(null);
						else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_GAME)))
							note = game_notes.parallelStream().filter(f -> f.getTitle().equalsIgnoreCase(args[1])).findAny().orElse(null);
						if(note == null) {
							e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PATCHNOTES_NOT_AVAILABLE)).build()).queue();
						}
						else {
							printPatchNotes(e, note, message);
						}
					}
					else {
						e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
					}
				}
				//if the user doesn't have any elevated position
				else {
					//if there's no parameter, give the user a choice to select between 'bot' for public patch notes and 'game' for game patch notes
					if(args.length == 0)
						e.getChannel().sendMessage(message.setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.PATCHNOTES_CHOICE_3)).build()).queue();
					//list all available patch notes, if either 'bot' or 'game' has been selected
					else if(args.length == 1 && (args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_BOT)) || args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_GAME)))) {
						ArrayList<Patchnote> display_notes = null;
						if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_BOT)))
							display_notes = publ_notes;
						else
							display_notes = game_notes;
						
						//print the patch notes list
						if(display_notes == null || display_notes.size() == 0) {
							e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PATCHNOTES_NOT_FOUND)).build()).queue();
						}
						else {
							collectPatchNotes(e, display_notes, message);
						}
					}
					//display the selected patch notes, if 2 parameters have been passed
					else if(args.length == 2 && (args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_BOT)) || args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_GAME)))) {
						Patchnote note = null;
						if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_BOT)))
							note = publ_notes.parallelStream().filter(f -> f.getTitle().equalsIgnoreCase(args[1])).findAny().orElse(null);
						else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_GAME)))
							note = game_notes.parallelStream().filter(f -> f.getTitle().equalsIgnoreCase(args[1])).findAny().orElse(null);
						if(note == null) {
							e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PATCHNOTES_NOT_AVAILABLE)).build()).queue();
						}
						else {
							printPatchNotes(e, note, message);
						}
					}
					else {
						e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
					}
				}
			}
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used Patchnotes command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}

	private void collectPatchNotes(GuildMessageReceivedEvent e, ArrayList<Patchnote> display_notes, EmbedBuilder message) {
		StringBuilder out = new StringBuilder();
		StringBuilder out2 = new StringBuilder();
		//iterate through the patch notes and convert it into readable text list
		for(Patchnote note : display_notes) {
			out.append("**"+note.getTitle()+"**\n");
			out2.append(note.getDate()+"\n");
		}
		//print message
		message.setColor(Color.BLUE);
		e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PATCHNOTES_HELP))
			.addField(STATIC.getTranslation(e.getMember(), Translation.PATCHNOTES_TITLE), out.toString(), true)
			.addField(STATIC.getTranslation(e.getMember(), Translation.PATCHNOTES_DATE), out2.toString(), true).build()).queue();
	}
	
	private void printPatchNotes(GuildMessageReceivedEvent e, Patchnote note, EmbedBuilder message) {
		//print the selected patch notes
		message.setColor(Color.MAGENTA).setThumbnail(e.getJDA().getSelfUser().getEffectiveAvatarUrl()).setTitle("**"+note.getTitle()+"** "+note.getDate());
		e.getChannel().sendMessage(message.setDescription(note.getMessage1()).build()).queue();
		if(note.getMessage2() != null && note.getMessage2().length() > 0) {
			message.setTitle("");
			e.getChannel().sendMessage(message.setDescription(note.getMessage2()).build()).queue();
		}
	}

}
