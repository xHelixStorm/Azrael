package de.azrael.listeners;

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.commands.util.CommandHandler;
import de.azrael.commands.util.CommandParser;
import de.azrael.commands.util.EquipExecution;
import de.azrael.enums.Command;
import de.azrael.enums.Directory;
import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.util.FileHandler;
import de.azrael.util.Hashes;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * This class gets executed when a private message arrives
 * to the Bot. 
 * 
 * Main task of this class is to guide the user through the
 * equip command.
 * @author xHelixStorm
 * 
 */

public class PrivateMessageListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(PrivateMessageListener.class);
	
	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if(!e.isFromGuild() && e.getChannelType().equals(ChannelType.PRIVATE)) {
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(() -> {
				//handle private messages commands
				var message = e.getMessage().getContentRaw().toLowerCase();
				if(e.getMessage().getContentRaw().startsWith("!") && e.getMessage().getAuthor().getId() != e.getJDA().getSelfUser().getId()) {
					if(!CommandHandler.handleCommand(CommandParser.parser(null, message, e), null)) {
						logger.debug("Private message command {} doesn't exist", e.getMessage().getContentRaw());
					}
				}
				
				var equip = Hashes.getTempCache("equip_us"+e.getAuthor().getId());
				
				if(equip != null && equip.getExpiration() - System.currentTimeMillis() > 0) {
					//interrupt the equip setup
					if(e.getMessage().getContentRaw().equalsIgnoreCase(STATIC.getTranslation3(e.getAuthor(), Translation.PARAM_EXIT))) {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_EXIT)).build()).queue();
						Hashes.clearTempCache("equip_us"+e.getAuthor());
						Azrael.SQLInsertCommandLog(e.getAuthor().getIdLong(), 0, Command.EQUIP.getColumn(), e.getMessage().getContentRaw());
					}
					//execute this block when no main parameter has been chosen yet
					else if(equip.getAdditionalInfo2().length() == 0) {
						//run this block when a clear guild with enabled ranking system has been selected and found
						if(equip.getAdditionalInfo().length() == 18) {
							//show the current equipment
							if(e.getMessage().getContentRaw().equalsIgnoreCase(STATIC.getTranslation3(e.getAuthor(), Translation.PARAM_DISPLAY))) {
								//TODO: show equipment
							}
							//equip weapons or skills
							else if(e.getMessage().getContentRaw().equalsIgnoreCase(STATIC.getTranslation3(e.getAuthor(), Translation.PARAM_SET))) {
								EquipExecution.equipmentItemScreen(e, equip.getAdditionalInfo(), "set");
							}
							//remove an equipped weapon or skill
							else if(e.getMessage().getContentRaw().equalsIgnoreCase(STATIC.getTranslation3(e.getAuthor(), Translation.PARAM_REMOVE))) {
								EquipExecution.equipmentItemScreen(e, equip.getAdditionalInfo(), "remove");
							}
							//remove everything equipped
							else if(e.getMessage().getContentRaw().equalsIgnoreCase(STATIC.getTranslation3(e.getAuthor(), Translation.PARAM_REMOVE_ALL))) {
								EquipExecution.removeWholeEquipment(e, Long.parseLong(equip.getAdditionalInfo()));
							}
						}
						else if(equip.getAdditionalInfo().length() > 18) {
							//run action to select one displayed guild on the screen
							if(!e.getMessage().getContentRaw().matches("[^\\d]*"))
								EquipExecution.selectAvailableGuilds(e, equip.getAdditionalInfo(), Integer.parseInt(e.getMessage().getContentRaw())-1);
							else
								e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SELECT_NUMBER)).build()).queue();
						}
					}
					else {
						//run action to filter the guild by name or id in case all available guilds with enabled ranking system couldn't be displayed
						if(equip.getAdditionalInfo2().equals("err") && equip.getAdditionalInfo().length() > 18) {
							EquipExecution.findGuild(e, Arrays.asList(equip.getAdditionalInfo().split("-")), e.getMessage().getContentRaw().toLowerCase());
						}
						//notify user that a guild has to be selected
						else if(equip.getAdditionalInfo2().equals("wait")) {
							equip.updateDescription2("").setExpiration(180000);
							Hashes.addTempCache("equip_us"+e.getAuthor().getId(), equip);
						}
						//select a slot for the weapon or skill to equip after
						else if(equip.getAdditionalInfo2().equals("set")) {
							if(!e.getMessage().getContentRaw().matches("[^\\d]*"))
								EquipExecution.slotSelection(e, equip.getAdditionalInfo(), Integer.parseInt(e.getMessage().getContentRaw()), "set");
							else
								e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SELECT_NUMBER)).build()).queue();
						}
						//search for the typed weapon or skill in the inventory
						else if(equip.getAdditionalInfo2().matches("^(set-)[1-4]$")) {
							if(message.equalsIgnoreCase(STATIC.getTranslation3(e.getAuthor(), Translation.PARAM_RETURN))) {
								EquipExecution.equipmentItemScreen(e, equip.getAdditionalInfo(), "set");
							}
							else {
								EquipExecution.searchInventory(e, equip.getAdditionalInfo(), Integer.parseInt(equip.getAdditionalInfo2().split("-")[1]), e.getMessage().getContentRaw().toLowerCase());
							}
						}
						//equip selected weapon or skin
						else if(equip.getAdditionalInfo2().matches("^(set-)[1-4](_)[\\d-]*$")) {
							if(message.equalsIgnoreCase(STATIC.getTranslation3(e.getAuthor(), Translation.PARAM_RETURN))) {
								EquipExecution.equipmentItemScreen(e, equip.getAdditionalInfo(), "set");
							}
							else {
								if(!e.getMessage().getContentRaw().matches("[^\\d]") && message.length() <= 9) {
									var information = equip.getAdditionalInfo2().split("_");
									var slot = Integer.parseInt(information[0].split("-")[1]);
									var weapons = information[1].split("-");
									EquipExecution.selectItem(e, equip.getAdditionalInfo(), slot, Integer.parseInt(e.getMessage().getContentRaw())-1, weapons);
								}
								else 
									e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SELECT_NUMBER)).build()).queue();
							}
						}
						//unequip a weapon or skill from a specific slot
						else if(equip.getAdditionalInfo2().equals("remove")) {
							if(!e.getMessage().getContentRaw().matches("[^\\d]"))
								EquipExecution.slotSelection(e, equip.getAdditionalInfo(), Integer.parseInt(e.getMessage().getContentRaw()), "remove");
							else
								e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SELECT_NUMBER)).build()).queue();
						}
					}
				}
			});
			
			executor.execute(() -> {
				//log the written or received private message
				StringBuilder image_url = new StringBuilder();
				for(Attachment attch : e.getMessage().getAttachments()) {
					image_url.append((e.getMessage().getContentRaw().length() == 0 && image_url.length() == 0) ? "("+attch.getProxyUrl()+")" : "\n("+attch.getProxyUrl()+")");
				}
				FileHandler.appendFile(Directory.MESSAGE_LOG, "privChannel.txt", "["+LocalDateTime.now().toString()+" - "+e.getAuthor().getName()+"#"+e.getAuthor().getDiscriminator()+" ("+e.getAuthor().getId()+")]: "+e.getMessage().getContentRaw()+image_url.toString()+"\n");
			});
		}
	}
}
