package listeners;

import java.awt.Color;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.EquipExecution;
import core.CommandHandler;
import core.CommandParser;
import core.Hashes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class PrivateMessageListener extends ListenerAdapter{
	
	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent e) {
		//handle private messages commands
		var message = e.getMessage().getContentRaw().toLowerCase();
		if(message.equalsIgnoreCase("equip")) {
			if(e.getMessage().getAuthor().getId() != e.getJDA().getSelfUser().getId()) {
				if(!CommandHandler.handleCommand(CommandParser.parser(message, null, e))) {
					Logger logger = LoggerFactory.getLogger(GuildMessageListener.class);
					logger.warn("Private message command {} doesn't exist!", e.getMessage().getContentRaw());
				}
			}
		}
		
		var equip = Hashes.getTempCache("equip_us"+e.getAuthor().getId());
		
		if(equip != null && equip.getExpiration() - System.currentTimeMillis() > 0) {
			if(e.getMessage().getContentRaw().equalsIgnoreCase("exit")) {
				//interrupt or complete equip setup
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("Equip setup terminated!").build()).queue();
				Hashes.clearTempCache("equip_us"+e.getAuthor());
			}
			else if(equip.getAdditionalInfo2().length() == 0) {
				if(equip.getAdditionalInfo().length() == 18) {
					if(e.getMessage().getContentRaw().equalsIgnoreCase("show")) {
						
					}
					else if(e.getMessage().getContentRaw().equalsIgnoreCase("set")) {
						EquipExecution.equipmentItemScreen(e, equip.getAdditionalInfo(), "set");
					}
					else if(e.getMessage().getContentRaw().equalsIgnoreCase("remove")) {
						EquipExecution.equipmentItemScreen(e, equip.getAdditionalInfo(), "remove");
					}
					else if(e.getMessage().getContentRaw().equalsIgnoreCase("remove-all")) {
						EquipExecution.removeWholeEquipment(e, Long.parseLong(equip.getAdditionalInfo()));
					}
				}
				else if(equip.getAdditionalInfo().length() > 18) {
					//run action to select one displayed guild on the screen
					if(!e.getMessage().getContentRaw().matches("[^\\d]*"))
						EquipExecution.selectAvailableGuilds(e, equip.getAdditionalInfo(), Integer.parseInt(e.getMessage().getContentRaw())-1);
					else
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Please select one of the visible digits on the screen!").build()).queue();
				}
			}
			else {
				if(equip.getAdditionalInfo2().equals("err") && equip.getAdditionalInfo().length() > 18) {
					//run action to filter the guild by name or id in case all available guilds with enabled ranking system couldn't be displayed
					EquipExecution.findGuild(e, Arrays.asList(equip.getAdditionalInfo().split("-")), e.getMessage().getContentRaw().toLowerCase());
				}
				else if(equip.getAdditionalInfo2().equals("wait")) {
					equip.updateDescription2("").setExpiration(180000);
					Hashes.addTempCache("equip_us"+e.getAuthor().getId(), equip);
				}
				else if(equip.getAdditionalInfo2().equals("set")) {
					if(!e.getMessage().getContentRaw().matches("[^\\d]"))
						EquipExecution.slotSelection(e, equip.getAdditionalInfo(), Integer.parseInt(e.getMessage().getContentRaw()), "set");
					else
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Please select one of the visible digits on the screen!").build()).queue();
				}
				else if(equip.getAdditionalInfo2().matches("^(set-)[1-4]$")) {
					if(message.equalsIgnoreCase("return")) {
						EquipExecution.equipmentItemScreen(e, equip.getAdditionalInfo(), "set");
					}
					else {
						EquipExecution.searchInventory(e, equip.getAdditionalInfo(), Integer.parseInt(equip.getAdditionalInfo2().split("-")[1]), e.getMessage().getContentRaw().toLowerCase());
					}
				}
				else if(equip.getAdditionalInfo2().matches("^(set-)[1-4](_)[\\d-]*$")) {
					if(message.equalsIgnoreCase("return")) {
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
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Please select one of the visible digits on the screen!").build()).queue();
					}
				}
				else if(equip.getAdditionalInfo2().equals("remove")) {
					if(!e.getMessage().getContentRaw().matches("[^\\d]"))
						EquipExecution.slotSelection(e, equip.getAdditionalInfo(), Integer.parseInt(e.getMessage().getContentRaw()), "remove");
					else
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Please select one of the visible digits on the screen!").build()).queue();
				}
			}
		}
	}
}
