package inventory;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import constructors.InventoryContent;
import enums.Translation;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import util.STATIC;

public class InventoryBuilder {
	private final static Logger logger = LoggerFactory.getLogger(InventoryBuilder.class);
	
	public static void DrawInventory(Guild guild, Member member, TextChannel channel, String _inventory_tab, String _sub_tab, ArrayList<InventoryContent> _items, int _current_page, int _max_page, Guilds guild_settings) {
		String lastItem = "";
		if(new File("./files/RankingSystem/Inventory/inventory_blank.png").exists()) {
			try {
				BufferedImage blank_inventory = ImageIO.read(new File("./files/RankingSystem/Inventory/inventory_blank.png"));
				BufferedImage inventory_tab = ImageIO.read(new File("./files/RankingSystem/Inventory/inventory_"+_inventory_tab+"_"+_sub_tab+".png"));
				
				final int startX = guild_settings.getInventoryStartX();
				final int startY = guild_settings.getInventoryStartY();
				final int tabX = guild_settings.getInventoryTabX();
				final int tabY = guild_settings.getInventoryTabY();
				final int pageFontSize = guild_settings.getInventoryPageFontSize();
				final int pageX = guild_settings.getInventoryPageX();
				final int pageY = guild_settings.getInventoryPageY();
				final int generalTextFontSize = guild_settings.getInventoryTextFontSize();
				final int boxSizeX = guild_settings.getInventoryBoxSizeX();
				final int boxSizeY = guild_settings.getInventoryBoxSizeY();
				final int descriptionY = guild_settings.getInventoryDescriptionY();
				final int itemSizeX = guild_settings.getInventoryItemSizeX();
				final int itemSizeY = guild_settings.getInventoryItemSizeY();
				final int nextBoxX = guild_settings.getInventoryNextBoxX();
				final int nextBoxY = guild_settings.getInventoryNextBoxY();
				final int expiration_positionY = guild_settings.getInventoryExpirationPositionY();
				final int rowLimit = guild_settings.getInventoryRowLimit();
				
				int inventory_Width = blank_inventory.getWidth();
				int inventory_Height = blank_inventory.getHeight();
				BufferedImage overlay = new BufferedImage(inventory_Width, inventory_Height, BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D g = overlay.createGraphics();
				g.drawImage(blank_inventory, 0, 0, null);
				g.drawImage(inventory_tab, tabX, tabY, null);
				g.setFont(new Font("Nexa Bold", Font.BOLD, pageFontSize));
				g.drawString(_current_page+"/"+_max_page, getRightString(""+_current_page, pageX, g), pageY);
				g.setFont(new Font("Nexa Bold", Font.BOLD, generalTextFontSize));
				
				int i = 0;
				var currentX = startX;
				var currentY = startY;
				for(InventoryContent inventory : _items) {
					lastItem = inventory.getDescription();
					
					i++;
					BufferedImage item;
					if(inventory.getType() != null && inventory.getType().equals("ite"))
						item = ImageIO.read(new File("./files/RankingSystem/Items/"+inventory.getDescription()+"."+inventory.getFileType()));
					else if(inventory.getType() != null)
						item = ImageIO.read(new File("./files/RankingSystem/Skins/"+inventory.getDescription()+"."+inventory.getFileType()));
					else if(inventory.getSkillDescription() == null)
						item = ImageIO.read(new File("./files/RankingSystem/Weapons/"+inventory.getWeaponDescription()+".png"));
					else
						item = ImageIO.read(new File("./files/RankingSystem/Skills/"+inventory.getSkillDescription()+".png"));
					g.drawImage(item, currentX+(boxSizeX/2)-(item.getWidth()/2), currentY+boxSizeY-(item.getHeight()/2), (itemSizeX != 0 ? itemSizeX : item.getWidth()), (itemSizeY != 0 ? itemSizeY : item.getHeight()), null);
					g.drawString((inventory.getDescription() != null ? inventory.getDescription() : (inventory.getWeaponDescription() != null ? inventory.getWeaponDescription()+" "+inventory.getStat() : inventory.getSkillDescription())), currentX+getCenteredString(inventory.getDescription() != null ? inventory.getDescription() : (inventory.getWeaponDescription() != null ? inventory.getWeaponDescription()+ " "+inventory.getStat() : inventory.getSkillDescription()), boxSizeX, g), currentY+boxSizeY+descriptionY);
					if(inventory.getType() == null || inventory.getType().equals("ite")){
						if(inventory.getStatus().equals("limit")){
							long time = inventory.getExpiration().getTime()-System.currentTimeMillis();
							long days = time/1000/60/60/24;
							long hours = time/1000/60/60%24;
							long minutes = time/1000/60-(days*24*60)-(hours*60);
							g.drawString(days+"D "+hours+"H "+minutes+"M", currentX+getCenteredString(days+"D "+hours+"H "+minutes+"M", boxSizeX, g), currentY+boxSizeY+expiration_positionY);
						}
						else{
							g.drawString(inventory.getNumber()+"x", currentX+getCenteredString(inventory.getNumber()+"x", boxSizeX, g), currentY+boxSizeY+expiration_positionY);
						}
					}
					if(i % rowLimit != 0) {
						currentX += nextBoxX;
					}
					else {
						currentX = startX;
						currentY += nextBoxY;
					}
				}
				
				ImageIO.write(overlay, "png", new File(IniFileReader.getTempDirectory()+"inventory_gu"+guild.getId()+"us"+member.getUser().getId()+".png"));
				File upload = new File(IniFileReader.getTempDirectory()+"inventory_gu"+guild.getId()+"us"+member.getUser().getId()+".png");
				channel.sendFile(upload, "inventory.png").queue(m -> {
					upload.delete();
				});
			} catch(IOException ioe) {
				if(guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(guild, channel, EnumSet.of(Permission.MESSAGE_EMBED_LINKS)))
					channel.sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(member, Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(member, Translation.INVENTORY_DRAW_ERR)).build()).queue();
				else
					channel.sendMessage(STATIC.getTranslation(member, Translation.INVENTORY_DRAW_ERR)).queue();
				logger.warn("Inventory couldn't be drawn for user {} and item {} in guild {}", member.getUser().getId(), lastItem, guild.getId(), ioe);
			}
			
		}
		else {
			StringBuilder out = new StringBuilder();
			for(InventoryContent inventory : _items) {
				if(inventory.getType() != null && inventory.getType().equals("ite"))
					out.append(inventory.getDescription());
				else if(inventory.getType() != null)
					out.append(inventory.getDescription());
				else if(inventory.getSkillDescription() == null)
					out.append(inventory.getWeaponDescription()+" "+inventory.getStat());
				else
					out.append(inventory.getSkillDescription());
				if(inventory.getType() == null || inventory.getType().equals("ite")) {
					if(inventory.getStatus().equals("limit")) {
						long time = inventory.getExpiration().getTime()-System.currentTimeMillis();
						long days = time/1000/60/60/24;
						long hours = time/1000/60/60%24;
						long minutes = time/1000/60-(days*24*60)-(hours*60);
						out.append(" "+days+"D "+hours+"H "+minutes+"M");
					}
				}
				out.append("\n");
			}
			channel.sendMessage("```\n"+out.toString()+"\n```");
		}
	}
	
	private static int getCenteredString(String s, int w, Graphics2D g) {
	    FontMetrics fm = g.getFontMetrics();
	    int x = (w/2) - (fm.stringWidth(s)/2);
	    return x;
	}
	
	private static int getRightString(String s, int w, Graphics2D g) {
	    FontMetrics fm = g.getFontMetrics();
	    int x = w - fm.stringWidth(s);
	    return x;
	}
}
